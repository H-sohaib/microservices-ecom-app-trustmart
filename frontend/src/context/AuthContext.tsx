/**
 * Authentication Context using Keycloak
 *
 * Manages user authentication state and provides authentication methods.
 * Integrates with Keycloak for OAuth2/OIDC authentication.
 */
import React, {
  createContext,
  useContext,
  useEffect,
  useState,
  useCallback,
} from "react";
import keycloak from "@/lib/keycloak";

// Authentication context interface with all auth-related state and methods
interface AuthContextType {
  isAuthenticated: boolean;
  isLoading: boolean;
  token: string | undefined;
  username: string | undefined;
  roles: string[];
  isAdmin: boolean;
  isClient: boolean;
  login: () => void;
  logout: () => void;
  getToken: () => Promise<string | undefined>;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error("useAuth must be used within an AuthProvider");
  }
  return context;
};

interface AuthProviderProps {
  children: React.ReactNode;
}

export const AuthProvider: React.FC<AuthProviderProps> = ({ children }) => {
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const [token, setToken] = useState<string | undefined>(undefined);
  const [username, setUsername] = useState<string | undefined>(undefined);
  const [roles, setRoles] = useState<string[]>([]);

  const extractRoles = useCallback((): string[] => {
    if (keycloak.tokenParsed?.realm_access?.roles) {
      return keycloak.tokenParsed.realm_access.roles;
    }
    return [];
  }, []);

  const updateAuthState = useCallback(() => {
    setIsAuthenticated(keycloak.authenticated ?? false);
    setToken(keycloak.token);
    setUsername(keycloak.tokenParsed?.preferred_username);
    setRoles(extractRoles());
  }, [extractRoles]);

  useEffect(() => {
    /**
     * Initialize Keycloak authentication
     *
     * - check-sso: Checks if user is already logged in (SSO)
     * - silent-check-sso: Uses iframe for silent auth check
     * - PKCE: Enhanced security for OAuth2 flows
     */
    const initKeycloak = async () => {
      try {
        const authenticated = await keycloak.init({
          onLoad: "check-sso", // Check if already authenticated
          // silentCheckSsoRedirectUri:
          //   window.location.origin + "/silent-check-sso.html",
          pkceMethod: "S256", // Proof Key for Code Exchange (security enhancement)
        });

        setIsAuthenticated(authenticated);

        if (authenticated) {
          updateAuthState();
        }

        // Token refresh
        keycloak.onTokenExpired = () => {
          keycloak
            .updateToken(30)
            .then((refreshed) => {
              if (refreshed) {
                updateAuthState();
              }
            })
            .catch(() => {
              console.error("Failed to refresh token");
              keycloak.logout();
            });
        };

        keycloak.onAuthSuccess = () => {
          updateAuthState();
        };

        keycloak.onAuthLogout = () => {
          setIsAuthenticated(false);
          setToken(undefined);
          setUsername(undefined);
          setRoles([]);
        };
      } catch (error) {
        console.error("Keycloak initialization failed:", error);
        setIsAuthenticated(false);
      } finally {
        setIsLoading(false);
      }
    };

    initKeycloak();
  }, [updateAuthState]);

  const login = useCallback(() => {
    keycloak.login();
  }, []);

  const logout = useCallback(() => {
    keycloak.logout({ redirectUri: window.location.origin });
  }, []);

  const getToken = useCallback(async (): Promise<string | undefined> => {
    if (!keycloak.authenticated) {
      return undefined;
    }

    try {
      // Refresh token if it expires within 30 seconds
      await keycloak.updateToken(30);
      return keycloak.token;
    } catch (error) {
      console.error("Failed to refresh token:", error);
      keycloak.logout();
      return undefined;
    }
  }, []);

  const isAdmin = roles.includes("ADMIN");
  const isClient = roles.includes("CLIENT");

  const value: AuthContextType = {
    isAuthenticated,
    isLoading,
    token,
    username,
    roles,
    isAdmin,
    isClient,
    login,
    logout,
    getToken,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};
