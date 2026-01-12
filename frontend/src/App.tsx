/**
 * Root Application Component
 *
 * Configures all global providers and routing for the application.
 * Includes authentication, state management, and UI notifications.
 */
import { Toaster } from "@/components/ui/toaster";
import { Toaster as Sonner } from "@/components/ui/sonner";
import { TooltipProvider } from "@/components/ui/tooltip";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { BrowserRouter, Routes, Route } from "react-router-dom";
import { CartProvider } from "@/context/CartContext";
import { AuthProvider } from "@/context/AuthContext";
import { Layout } from "@/components/layout/Layout";
import Index from "./pages/Index.tsx";
import Orders from "./pages/Orders.tsx";
import Products from "./pages/Products.tsx";
import Cart from "./pages/Cart.tsx";
import NewOrder from "./pages/NewOrder.tsx";
import Clients from "./pages/Clients.tsx";
import Register from "./pages/Register.tsx";
import NotFound from "./pages/NotFound.tsx";

/**
 * React Query Client Configuration
 *
 * Configures global settings for data fetching and caching:
 * - Disables automatic refetch when window regains focus
 * - Smart retry logic: skips retries for 4xx client errors
 * - Limits retries to 2 attempts for server errors
 */
const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      refetchOnWindowFocus: false,
      retry: (failureCount, error) => {
        // Don't retry on 4xx errors (client-side errors like 401, 403, 404)
        if (error instanceof Error && error.message.includes("4")) {
          return false;
        }
        // Retry up to 2 times for other errors (network, 5xx, etc.)
        return failureCount < 2;
      },
    },
  },
});

/**
 * Main App Component with Provider Hierarchy
 *
 * Provider order matters for dependency injection:
 * 1. AuthProvider - Keycloak authentication (outermost)
 * 2. QueryClientProvider - React Query for data fetching
 * 3. CartProvider - Shopping cart state management
 * 4. TooltipProvider - UI tooltip functionality
 * 5. BrowserRouter - Client-side routing
 */
const App = () => (
  <AuthProvider>
    <QueryClientProvider client={queryClient}>
      <CartProvider>
        <TooltipProvider>
          {/* Toast notification components for user feedback */}
          <Toaster />
          <Sonner />
          <BrowserRouter>
            <Layout>
              {/* Application Routes - Order matters! */}
              <Routes>
                {/* Public Routes */}
                <Route path="/" element={<Index />} />
                <Route path="/products" element={<Products />} />

                {/* Protected Routes (require authentication) */}
                <Route path="/orders" element={<Orders />} />
                <Route path="/cart" element={<Cart />} />
                <Route path="/new-order" element={<NewOrder />} />
                <Route path="/clients" element={<Clients />} />
                <Route path="/register" element={<Register />} />

                {/* Catch-all route for 404 - MUST BE LAST */}
                <Route path="*" element={<NotFound />} />
              </Routes>
            </Layout>
          </BrowserRouter>
        </TooltipProvider>
      </CartProvider>
    </QueryClientProvider>
  </AuthProvider>
);

export default App;
