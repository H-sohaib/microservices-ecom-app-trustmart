/**
 * Vite Configuration
 *
 * Build tool configuration for the React frontend application.
 * Optimized for fast development and production builds.
 */
import { defineConfig } from "vite";
import react from "@vitejs/plugin-react-swc"; // SWC for faster builds
import path from "path";

// https://vitejs.dev/config/
export default defineConfig(({ mode }) => ({
  server: {
    host: "::", // Listen on all network interfaces (IPv6 compatible)
    port: 8084, // Development server port
  },
  plugins: [react()], // React plugin with SWC compiler for faster refresh
  resolve: {
    alias: {
      // Path alias for cleaner imports: @/components instead of ../../components
      "@": path.resolve(__dirname, "./src"),
    },
  },
}));
