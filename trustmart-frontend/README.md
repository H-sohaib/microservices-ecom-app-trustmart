# TrustMart Frontend

A modern React frontend for the TrustMart E-Commerce microservices application.

## Features

- 📦 **Product Management** - Add, edit, delete, and browse products
- 🛒 **Shopping Cart** - Add items to cart and checkout
- 📋 **Order Management** - Create, view, and manage orders
- 🔄 **Order Status Tracking** - Update and filter orders by status
- 🎨 **Modern UI** - Built with shadcn/ui components and Tailwind CSS

## Tech Stack

- **React 18** - UI library
- **TypeScript** - Type safety
- **Vite** - Build tool and dev server
- **Tailwind CSS** - Utility-first CSS framework
- **shadcn/ui** - UI component library
- **TanStack Query** - Data fetching and caching
- **React Router** - Client-side routing
- **React Hook Form** - Form handling
- **Zod** - Schema validation

## Prerequisites

- Node.js 18+ 
- npm or bun

## Getting Started

1. **Install dependencies**

```bash
npm install
```

2. **Start the development server**

```bash
npm run dev
```

The app will be available at `http://localhost:8084`

3. **Build for production**

```bash
npm run build
```

4. **Preview production build**

```bash
npm run preview
```

## API Configuration

The frontend connects to the API Gateway at `http://localhost:8083`. To change this, update the `API_BASE_URL` in `src/lib/api.ts`.

## Project Structure

```
src/
├── components/       # Reusable UI components
│   ├── cart/        # Cart-related components
│   ├── layout/      # Layout components (Header, Layout)
│   ├── orders/      # Order-related components
│   ├── products/    # Product-related components
│   └── ui/          # shadcn/ui components
├── context/         # React context providers
├── hooks/           # Custom React hooks
├── lib/             # Utilities and API client
└── pages/           # Page components
```

## Available Scripts

- `npm run dev` - Start development server
- `npm run build` - Build for production
- `npm run preview` - Preview production build
- `npm run lint` - Run ESLint

## Backend Services

This frontend requires the following microservices to be running:

- **Discovery Service** (Eureka) - Port 8080
- **Product Service** - Port 8081
- **Command Service** - Port 8082
- **Gateway Service** - Port 8083

## License

MIT

