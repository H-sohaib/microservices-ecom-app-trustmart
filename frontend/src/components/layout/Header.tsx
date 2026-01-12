import { Link, useLocation } from 'react-router-dom';
import { ShoppingCart, Package, ClipboardList, Home, Plus, Menu, X, LogIn, LogOut, User, Shield, Users, UserPlus } from 'lucide-react';
import { useState } from 'react';
import { useCart } from '@/context/CartContext';
import { useAuth } from '@/context/AuthContext';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu';
import { cn } from '@/lib/utils';

export function Header() {
  const location = useLocation();
  const { totalItems } = useCart();
  const { isAuthenticated, isLoading, username, isAdmin, isClient, login, logout } = useAuth();
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);

  // Base nav items visible to everyone
  const publicNavItems = [
    { path: '/', label: 'Home', icon: Home },
    { path: '/products', label: 'Products', icon: Package },
  ];

  // Nav items for authenticated users - orders visible to all, new order only for clients
  const authNavItems = [
    { path: '/orders', label: 'Orders', icon: ClipboardList },
  ];

  // Admin only nav items
  const adminOnlyNavItems = [
    { path: '/clients', label: 'Clients', icon: Users },
  ];

  // New Order only for CLIENT role (not ADMIN)
  const clientOnlyNavItems = [
    { path: '/new-order', label: 'New Order', icon: Plus },
  ];

  // Combine nav items based on authentication and role
  const navItems = isAuthenticated
    ? isAdmin
      ? [...publicNavItems, ...authNavItems, ...adminOnlyNavItems]
      : [...publicNavItems, ...authNavItems, ...clientOnlyNavItems]
    : publicNavItems;

  return (
    <header className="sticky top-0 z-50 w-full border-b border-border bg-card/95 backdrop-blur-md shadow-sm">
      <div className="container flex h-16 items-center justify-between">
        {/* Logo */}
        <Link to="/" className="flex items-center gap-2 hover:opacity-80 transition-opacity">
          <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-gradient-to-br from-primary to-primary/80 shadow-md">
            <Package className="h-5 w-5 text-primary-foreground" />
          </div>
          <span className="font-display text-xl font-bold text-foreground hidden sm:block">
            E<span className="text-primary">-Commerce</span>
          </span>
        </Link>

        {/* Desktop Navigation */}
        <nav className="hidden md:flex items-center gap-1 bg-muted/50 rounded-full px-2 py-1">
          {navItems.map((item) => {
            const Icon = item.icon;
            const isActive = location.pathname === item.path;
            return (
              <Link key={item.path} to={item.path}>
                <Button
                  variant="ghost"
                  size="sm"
                  className={cn(
                    'gap-2 rounded-full transition-all duration-200',
                    isActive && 'bg-background text-primary shadow-sm'
                  )}
                >
                  <Icon className="h-4 w-4" />
                  {item.label}
                </Button>
              </Link>
            );
          })}
        </nav>

        {/* Right side actions */}
        <div className="flex items-center gap-2">
          {/* Cart - only show for authenticated users */}
          {isAuthenticated && (
            <Link to="/cart">
              <Button variant="ghost" className="relative gap-2 hover:bg-primary/10">
                <ShoppingCart className="h-5 w-5" />
                <span className="hidden sm:inline">Cart</span>
                {totalItems > 0 && (
                  <Badge className="absolute -right-1 -top-1 h-5 w-5 rounded-full p-0 flex items-center justify-center bg-primary text-primary-foreground text-xs font-semibold animate-pulse">
                    {totalItems}
                  </Badge>
                )}
              </Button>
            </Link>
          )}

          {/* Auth buttons */}
          {isLoading ? (
            <div className="h-9 w-20 bg-muted rounded animate-pulse" />
          ) : isAuthenticated ? (
            <DropdownMenu>
              <DropdownMenuTrigger asChild>
                <Button variant="outline" className="gap-2">
                  {isAdmin ? (
                    <Shield className="h-4 w-4 text-primary" />
                  ) : (
                    <User className="h-4 w-4" />
                  )}
                  <span className="hidden sm:inline max-w-[100px] truncate">
                    {username}
                  </span>
                </Button>
              </DropdownMenuTrigger>
              <DropdownMenuContent align="end" className="w-56">
                <DropdownMenuLabel className="font-normal">
                  <div className="flex flex-col space-y-1">
                    <p className="text-sm font-medium leading-none">{username}</p>
                    <p className="text-xs leading-none text-muted-foreground">
                      {isAdmin ? 'Administrator' : isClient ? 'Client' : 'User'}
                    </p>
                  </div>
                </DropdownMenuLabel>
                <DropdownMenuSeparator />
                <DropdownMenuItem className="text-xs text-muted-foreground">
                  Role: {isAdmin ? 'ADMIN' : isClient ? 'CLIENT' : 'USER'}
                </DropdownMenuItem>
                <DropdownMenuSeparator />
                <DropdownMenuItem onClick={logout} className="text-destructive cursor-pointer">
                  <LogOut className="h-4 w-4 mr-2" />
                  Logout
                </DropdownMenuItem>
              </DropdownMenuContent>
            </DropdownMenu>
          ) : (
            <div className="flex items-center gap-2">
              <Link to="/register">
                <Button variant="outline" className="gap-2">
                  <UserPlus className="h-4 w-4" />
                  <span className="hidden sm:inline">Register</span>
                </Button>
              </Link>
              <Button onClick={login} className="gap-2">
                <LogIn className="h-4 w-4" />
                <span className="hidden sm:inline">Login</span>
              </Button>
            </div>
          )}

          {/* Mobile menu toggle */}
          <Button
            variant="ghost"
            size="icon"
            className="md:hidden"
            onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
          >
            {mobileMenuOpen ? <X className="h-5 w-5" /> : <Menu className="h-5 w-5" />}
          </Button>
        </div>
      </div>

      {/* Mobile Navigation Dropdown */}
      {mobileMenuOpen && (
        <nav className="md:hidden border-t border-border bg-card animate-fade-in">
          <div className="container py-4 space-y-2">
            {navItems.map((item) => {
              const Icon = item.icon;
              const isActive = location.pathname === item.path;
              return (
                <Link
                  key={item.path}
                  to={item.path}
                  onClick={() => setMobileMenuOpen(false)}
                >
                  <Button
                    variant="ghost"
                    className={cn(
                      'w-full justify-start gap-3 h-12',
                      isActive && 'bg-primary/10 text-primary'
                    )}
                  >
                    <Icon className="h-5 w-5" />
                    {item.label}
                  </Button>
                </Link>
              );
            })}

            {/* Mobile auth section */}
            <div className="pt-2 border-t border-border">
              {isAuthenticated ? (
                <div className="space-y-2">
                  <div className="px-4 py-2 text-sm text-muted-foreground">
                    Signed in as <span className="font-medium text-foreground">{username}</span>
                    <span className="ml-2 text-xs">({isAdmin ? 'ADMIN' : 'CLIENT'})</span>
                  </div>
                  <Button
                    variant="ghost"
                    className="w-full justify-start gap-3 h-12 text-destructive hover:text-destructive"
                    onClick={() => {
                      setMobileMenuOpen(false);
                      logout();
                    }}
                  >
                    <LogOut className="h-5 w-5" />
                    Logout
                  </Button>
                </div>
              ) : (
                <div className="space-y-2">
                  <Link to="/register" onClick={() => setMobileMenuOpen(false)}>
                    <Button
                      variant="outline"
                      className="w-full justify-start gap-3 h-12"
                    >
                      <UserPlus className="h-5 w-5" />
                      Register
                    </Button>
                  </Link>
                  <Button
                    className="w-full justify-start gap-3 h-12"
                    onClick={() => {
                      setMobileMenuOpen(false);
                      login();
                    }}
                  >
                    <LogIn className="h-5 w-5" />
                    Login
                  </Button>
                </div>
              )}
            </div>
          </div>
        </nav>
      )}
    </header>
  );
}
