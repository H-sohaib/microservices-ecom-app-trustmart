import { useState } from 'react';
import { Link } from 'react-router-dom';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { ShoppingCart, ArrowLeft, CreditCard, Loader2, CheckCircle } from 'lucide-react';
import { useCart } from '@/context/CartContext';
import { commandApi } from '@/lib/api';
import { CartItem } from '@/components/cart/CartItem';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardFooter, CardHeader, CardTitle } from '@/components/ui/card';
import { EmptyState } from '@/components/ui/empty-state';
import { toast } from 'sonner';

export default function Cart() {
  const queryClient = useQueryClient();
  const { items, totalPrice, clearCart } = useCart();
  const [orderSuccess, setOrderSuccess] = useState(false);

  const createOrderMutation = useMutation({
    mutationFn: commandApi.create,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['orders'] });
      queryClient.invalidateQueries({ queryKey: ['products'] });
      clearCart();
      setOrderSuccess(true);
      toast.success('Order placed successfully!');
    },
    onError: (error: Error) => {
      toast.error(error.message || 'Failed to place order');
    },
  });

  const handleCheckout = () => {
    const orderItems = items.map((item) => ({
      productId: item.product.productId,
      quantity: item.quantity,
    }));

    createOrderMutation.mutate({ items: orderItems });
  };

  if (orderSuccess) {
    return (
      <div className="max-w-lg mx-auto py-12 animate-fade-in">
        <Card className="text-center">
          <CardContent className="py-12">
            <div className="w-16 h-16 mx-auto mb-6 rounded-full bg-success/10 flex items-center justify-center">
              <CheckCircle className="h-8 w-8 text-success" />
            </div>
            <h2 className="font-display text-2xl font-bold text-foreground mb-2">
              Order Placed!
            </h2>
            <p className="text-muted-foreground mb-6">
              Thank you for your purchase. Your order has been confirmed.
            </p>
            <div className="flex flex-col sm:flex-row gap-3 justify-center">
              <Link to="/orders">
                <Button className="gap-2">View Orders</Button>
              </Link>
              <Link to="/products">
                <Button variant="outline" className="gap-2">
                  Continue Shopping
                </Button>
              </Link>
            </div>
          </CardContent>
        </Card>
      </div>
    );
  }

  if (items.length === 0) {
    return (
      <div className="animate-fade-in">
        <EmptyState
          icon={ShoppingCart}
          title="Your cart is empty"
          description="Add some products to get started"
          action={
            <Link to="/products">
              <Button className="gap-2">
                <ArrowLeft className="h-4 w-4" />
                Browse Products
              </Button>
            </Link>
          }
        />
      </div>
    );
  }

  return (
    <div className="max-w-4xl mx-auto space-y-6 animate-fade-in">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="font-display text-2xl md:text-3xl font-bold text-foreground">
            Shopping Cart
          </h1>
          <p className="text-muted-foreground">
            {items.length} {items.length === 1 ? 'item' : 'items'} in your cart
          </p>
        </div>
        <Link to="/products">
          <Button variant="outline" className="gap-2">
            <ArrowLeft className="h-4 w-4" />
            Continue Shopping
          </Button>
        </Link>
      </div>

      <div className="grid lg:grid-cols-3 gap-6">
        <div className="lg:col-span-2 space-y-3">
          {items.map((item, index) => (
            <div
              key={item.product.productId}
              className="animate-slide-up"
              style={{ animationDelay: `${index * 50}ms` }}
            >
              <CartItem item={item} />
            </div>
          ))}
        </div>

        <div className="lg:col-span-1">
          <Card className="sticky top-24">
            <CardHeader>
              <CardTitle className="font-display">Order Summary</CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="flex justify-between text-sm">
                <span className="text-muted-foreground">Subtotal</span>
                <span className="font-medium">${totalPrice.toFixed(2)}</span>
              </div>
              <div className="flex justify-between text-sm">
                <span className="text-muted-foreground">Shipping</span>
                <span className="font-medium text-success">Free</span>
              </div>
              <div className="border-t border-border pt-4">
                <div className="flex justify-between">
                  <span className="font-semibold">Total</span>
                  <span className="text-xl font-bold text-primary">
                    ${totalPrice.toFixed(2)}
                  </span>
                </div>
              </div>
            </CardContent>
            <CardFooter>
              <Button
                onClick={handleCheckout}
                disabled={createOrderMutation.isPending}
                className="w-full gap-2 bg-gradient-primary hover:opacity-90"
                size="lg"
              >
                {createOrderMutation.isPending ? (
                  <>
                    <Loader2 className="h-4 w-4 animate-spin" />
                    Processing...
                  </>
                ) : (
                  <>
                    <CreditCard className="h-4 w-4" />
                    Place Order
                  </>
                )}
              </Button>
            </CardFooter>
          </Card>
        </div>
      </div>
    </div>
  );
}
