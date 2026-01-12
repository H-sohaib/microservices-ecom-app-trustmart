import { Link } from 'react-router-dom';
import { Package, ShoppingCart, ClipboardList, ArrowRight, Sparkles, Shield, Truck, Plus } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Card, CardContent } from '@/components/ui/card';

const features = [
  {
    icon: Package,
    title: 'Wide Selection',
    description: 'Browse through our extensive catalog of quality products',
  },
  {
    icon: Shield,
    title: 'Secure Shopping',
    description: 'Your transactions are protected with enterprise-grade security',
  },
  {
    icon: Truck,
    title: 'Fast Delivery',
    description: 'Get your orders delivered quickly and reliably',
  },
];

const quickLinks = [
  {
    icon: Package,
    title: 'Manage Products',
    description: 'Add, edit, and organize your product catalog',
    path: '/products',
    color: 'from-primary to-primary/80',
  },
  {
    icon: Plus,
    title: 'Create Order',
    description: 'Create a new order by selecting products',
    path: '/new-order',
    color: 'from-success to-success/80',
  },
  {
    icon: ClipboardList,
    title: 'View Orders',
    description: 'Track and manage all customer orders',
    path: '/orders',
    color: 'from-info to-info/80',
  },
  {
    icon: ShoppingCart,
    title: 'Shopping Cart',
    description: 'Review items and complete your purchase',
    path: '/cart',
    color: 'from-accent to-accent/80',
  },
];

export default function Index() {
  return (
    <div className="min-h-[calc(100vh-4rem)] flex flex-col">
      {/* Hero Section */}
      <section className="relative py-16 md:py-24 overflow-hidden">
        <div className="absolute inset-0 bg-gradient-hero opacity-5" />
        <div className="absolute inset-0 bg-[radial-gradient(circle_at_30%_20%,hsl(var(--primary)/0.15),transparent_50%)]" />

        <div className="relative container">
          <div className="max-w-3xl mx-auto text-center animate-fade-in">
            <div className="inline-flex items-center gap-2 px-4 py-2 rounded-full bg-primary/10 text-primary mb-6">
              <Sparkles className="h-4 w-4" />
              <span className="text-sm font-medium">Welcome to TrustMart</span>
            </div>

            <h1 className="font-display text-4xl md:text-5xl lg:text-6xl font-bold text-foreground mb-6 leading-tight">
              Your Trusted
              <span className="text-gradient-primary block">E-Commerce Partner</span>
            </h1>

            <p className="text-lg md:text-xl text-muted-foreground mb-8 max-w-2xl mx-auto">
              Discover quality products, manage your inventory, and track orders seamlessly with TrustMart's intuitive platform.
            </p>

            <div className="flex flex-wrap items-center justify-center gap-4">
              <Link to="/products">
                <Button size="lg" className="gap-2 bg-gradient-primary hover:opacity-90 transition-opacity text-lg px-8">
                  Browse Products
                  <ArrowRight className="h-5 w-5" />
                </Button>
              </Link>
              <Link to="/orders">
                <Button size="lg" variant="outline" className="text-lg px-8">
                  View Orders
                </Button>
              </Link>
            </div>
          </div>
        </div>
      </section>

      {/* Features Section */}
      <section className="py-12 md:py-16 bg-muted/30">
        <div className="container">
          <div className="grid md:grid-cols-3 gap-6">
            {features.map((feature, index) => (
              <Card
                key={feature.title}
                className="border-0 bg-card shadow-card hover:shadow-card-hover transition-all duration-300 animate-slide-up"
                style={{ animationDelay: `${index * 100}ms` }}
              >
                <CardContent className="p-6 flex items-start gap-4">
                  <div className="rounded-xl bg-primary/10 p-3">
                    <feature.icon className="h-6 w-6 text-primary" />
                  </div>
                  <div>
                    <h3 className="font-semibold text-foreground mb-1">
                      {feature.title}
                    </h3>
                    <p className="text-sm text-muted-foreground">
                      {feature.description}
                    </p>
                  </div>
                </CardContent>
              </Card>
            ))}
          </div>
        </div>
      </section>

      {/* Quick Links Section */}
      <section className="py-12 md:py-16 flex-1">
        <div className="container">
          <div className="text-center mb-10">
            <h2 className="font-display text-2xl md:text-3xl font-bold text-foreground mb-3">
              Quick Actions
            </h2>
            <p className="text-muted-foreground">
              Jump right into managing your store
            </p>
          </div>

          <div className="grid sm:grid-cols-2 lg:grid-cols-4 gap-6 max-w-5xl mx-auto">
            {quickLinks.map((link, index) => (
              <Link key={link.path} to={link.path} className="group">
                <Card
                  className="h-full border-0 bg-card shadow-card hover:shadow-card-hover transition-all duration-300 overflow-hidden animate-slide-up"
                  style={{ animationDelay: `${index * 100}ms` }}
                >
                  <CardContent className="p-6 relative">
                    <div
                      className={`absolute top-0 right-0 w-32 h-32 bg-gradient-to-br ${link.color} opacity-10 rounded-full -translate-y-1/2 translate-x-1/2 transition-transform duration-300 group-hover:scale-150`}
                    />
                    <div className="relative">
                      <div className="rounded-xl bg-primary/10 p-3 w-fit mb-4 group-hover:bg-primary/20 transition-colors">
                        <link.icon className="h-6 w-6 text-primary" />
                      </div>
                      <h3 className="font-semibold text-foreground mb-2 group-hover:text-primary transition-colors">
                        {link.title}
                      </h3>
                      <p className="text-sm text-muted-foreground">
                        {link.description}
                      </p>
                    </div>
                  </CardContent>
                </Card>
              </Link>
            ))}
          </div>
        </div>
      </section>

      {/* Footer */}
      <footer className="py-8 border-t border-border bg-card/50">
        <div className="container text-center">
          <p className="text-sm text-muted-foreground">
            © {new Date().getFullYear()} TrustMart. Built with trust.
          </p>
        </div>
      </footer>
    </div>
  );
}
