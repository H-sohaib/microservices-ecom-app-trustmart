# ====================
# Build Stage
# ====================
# Uses Node.js Alpine for minimal image size during build
FROM node:20-alpine AS build
WORKDIR /app

# Copy package files first for better Docker layer caching
# Changes to source code won't invalidate dependency installation
COPY package*.json ./

# Install dependencies using npm ci for reproducible builds
RUN npm ci

# Copy application source code
COPY . .

# Build the Vite application for production
RUN npm run build

# ====================
# Production Stage
# ====================
# Lightweight Node.js image to serve static files
FROM node:20-alpine
WORKDIR /app

# Install 'serve' - a lightweight static file server
RUN npm install -g serve

# Copy only the built assets from build stage (multi-stage optimization)
COPY --from=build /app/dist ./dist

# Expose port 3000 for the web server
EXPOSE 3000

# Serve static files with SPA (Single Page Application) fallback
# -s flag enables SPA mode (redirects 404s to index.html)
CMD ["serve", "-s", "dist", "-l", "3000"]