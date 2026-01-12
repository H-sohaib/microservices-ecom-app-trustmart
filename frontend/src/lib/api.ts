import keycloak from './keycloak';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8083';

// Types based on OpenAPI specs
export interface ProductRequest {
  name: string;
  description?: string;
  price: number;
  stock: number;
}

export interface ProductResponse {
  productId: number;
  name: string;
  description?: string;
  price: number;
  stock: number;
}

export interface StockUpdateRequest {
  productId: number;
  quantity: number;
}

export interface CommandItemRequest {
  productId: number;
  quantity: number;
}

export interface CommandRequest {
  items: CommandItemRequest[];
}

export interface CommandItemResponse {
  productId: number;
  quantity: number;
  price: number;
}

export type CommandStatus = 'PENDING' | 'CONFIRMED' | 'PROCESSING' | 'SHIPPED' | 'DELIVERED' | 'CANCELLED';

export interface CommandResponse {
  commandId: number;
  date: string;
  status: CommandStatus;
  totalPrice: number;
  userId: string;
  username: string;
  items: CommandItemResponse[];
}

export interface CommandStatusUpdateRequest {
  status: CommandStatus;
}

// API Error handling
export class ApiError extends Error {
  constructor(public status: number, message: string) {
    super(message);
    this.name = 'ApiError';
  }
}

async function handleResponse<T>(response: Response): Promise<T> {
  if (!response.ok) {
    if (response.status === 401) {
      throw new ApiError(401, 'Unauthorized. Please login.');
    }
    if (response.status === 403) {
      throw new ApiError(403, 'Access denied. You do not have permission to perform this action.');
    }
    const errorMessage = await response.text().catch(() => 'An error occurred');
    throw new ApiError(response.status, errorMessage || `HTTP error! status: ${response.status}`);
  }
  
  const contentType = response.headers.get('content-type');
  if (contentType && contentType.includes('application/json')) {
    return response.json();
  }
  
  return {} as T;
}

async function getAuthHeaders(): Promise<HeadersInit> {
  const headers: HeadersInit = {
    'Content-Type': 'application/json',
  };

  if (keycloak.authenticated) {
    try {
      // Refresh token if it expires within 30 seconds
      await keycloak.updateToken(30);
      headers['Authorization'] = `Bearer ${keycloak.token}`;
    } catch (error) {
      console.error('Failed to refresh token:', error);
    }
  }

  return headers;
}

async function apiRequest<T>(
  endpoint: string,
  options: RequestInit = {},
  requiresAuth: boolean = false
): Promise<T> {
  const url = `${API_BASE_URL}${endpoint}`;
  
  const headers = await getAuthHeaders();

  const config: RequestInit = {
    ...options,
    headers: {
      ...headers,
      ...options.headers,
    },
  };

  try {
    const response = await fetch(url, config);
    return handleResponse<T>(response);
  } catch (error) {
    if (error instanceof ApiError) {
      throw error;
    }
    if (error instanceof TypeError && error.message.includes('fetch')) {
      throw new ApiError(0, 'Unable to connect to the server. Please check if the API is running.');
    }
    throw new ApiError(500, 'An unexpected error occurred');
  }
}

// Product API
export const productApi = {
  getAll: () => apiRequest<ProductResponse[]>('/api/products'),
  
  getById: (productId: number) => 
    apiRequest<ProductResponse>(`/api/products/${productId}`),
  
  create: (product: ProductRequest) => 
    apiRequest<ProductResponse>('/api/products', {
      method: 'POST',
      body: JSON.stringify(product),
    }),
  
  update: (productId: number, product: ProductRequest) => 
    apiRequest<ProductResponse>(`/api/products/${productId}`, {
      method: 'PUT',
      body: JSON.stringify(product),
    }),
  
  delete: (productId: number) => 
    apiRequest<void>(`/api/products/${productId}`, {
      method: 'DELETE',
    }),
  
  checkStock: (productId: number, quantity: number) => 
    apiRequest<boolean>(`/api/products/${productId}/check-stock?quantity=${quantity}`),
  
  reduceStock: (items: StockUpdateRequest[]) => 
    apiRequest<void>('/api/products/reduce-stock', {
      method: 'POST',
      body: JSON.stringify(items),
    }),
  
  restoreStock: (items: StockUpdateRequest[]) => 
    apiRequest<void>('/api/products/restore-stock', {
      method: 'POST',
      body: JSON.stringify(items),
    }),
};

// Command API
export const commandApi = {
  getAll: (status?: CommandStatus) => {
    const query = status ? `?status=${status}` : '';
    return apiRequest<CommandResponse[]>(`/api/commands${query}`);
  },
  
  getById: (commandId: number) => 
    apiRequest<CommandResponse>(`/api/commands/${commandId}`),
  
  create: (command: CommandRequest) => 
    apiRequest<CommandResponse>('/api/commands', {
      method: 'POST',
      body: JSON.stringify(command),
    }),
  
  update: (commandId: number, command: CommandRequest) => 
    apiRequest<CommandResponse>(`/api/commands/${commandId}`, {
      method: 'PUT',
      body: JSON.stringify(command),
    }),
  
  delete: (commandId: number) => 
    apiRequest<void>(`/api/commands/${commandId}`, {
      method: 'DELETE',
    }),
  
  updateStatus: (commandId: number, status: CommandStatus) => 
    apiRequest<CommandResponse>(`/api/commands/${commandId}/status`, {
      method: 'PATCH',
      body: JSON.stringify({ status }),
    }),
  
  cancel: (commandId: number) => 
    apiRequest<void>(`/api/commands/${commandId}/cancel`, {
      method: 'POST',
    }),
};

// User types for Keycloak
export interface UserResponse {
  id: string;
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  enabled: boolean;
  emailVerified: boolean;
  createdTimestamp: number;
}

export interface CreateUserRequest {
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  password: string;
}

export interface UpdateUserRequest {
  email?: string;
  firstName?: string;
  lastName?: string;
  enabled?: boolean;
}

// User API (calls to gateway which proxies to a user service)
export const userApi = {
  getAll: () =>
    apiRequest<UserResponse[]>('/api/users'),

  getById: (userId: string) =>
    apiRequest<UserResponse>(`/api/users/${userId}`),

  create: (user: CreateUserRequest) =>
    apiRequest<UserResponse>('/api/users', {
      method: 'POST',
      body: JSON.stringify(user),
    }),

  update: (userId: string, user: UpdateUserRequest) =>
    apiRequest<UserResponse>(`/api/users/${userId}`, {
      method: 'PUT',
      body: JSON.stringify(user),
    }),

  delete: (userId: string) =>
    apiRequest<void>(`/api/users/${userId}`, {
      method: 'DELETE',
    }),

  toggleEnabled: (userId: string, enabled: boolean) =>
    apiRequest<UserResponse>(`/api/users/${userId}/enabled`, {
      method: 'PATCH',
      body: JSON.stringify({ enabled }),
    }),
};

// Public registration API (no auth required)
export const authApi = {
  register: (user: CreateUserRequest) =>
    fetch(`${API_BASE_URL}/api/auth/register`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(user),
    }).then(async (response) => {
      if (!response.ok) {
        const error = await response.text();
        throw new ApiError(response.status, error || 'Registration failed');
      }
      return response.json();
    }),
};
