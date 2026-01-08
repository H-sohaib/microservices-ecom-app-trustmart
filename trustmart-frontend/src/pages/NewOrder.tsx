import { useState } from 'react';
import { Link } from 'react-router-dom';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { Plus, Minus, Trash2, ShoppingBag, ArrowLeft, Loader2, CheckCircle, Package } from 'lucide-react';
import { productApi, commandApi, ProductResponse, CommandItemRequest } from '@/lib/api';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardFooter, CardHeader, CardTitle } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { PageLoader } from '@/components/ui/loading-spinner';
import { ErrorDisplay } from '@/components/ui/error-display';
import { EmptyState } from '@/components/ui/empty-state';
import { Badge } from '@/components/ui/badge';
import { toast } from 'sonner';
import { cn } from '@/lib/utils';

interface OrderItem {
  product: ProductResponse;
  quantity: number;
}

export default function NewOrder() {
  const queryClient = useQueryClient();
  const [orderItems, setOrderItems] = useState<OrderItem[]>([]);
  const [orderSuccess, setOrderSuccess] = useState(false);

  const { data: products, isLoading, error, refetch } = useQuery({
    queryKey: ['products'],
    queryFn: productApi.getAll,
  });

  const createOrderMutation = useMutation({
    mutationFn: commandApi.create,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['orders'] });
      queryClient.invalidateQueries({ queryKey: ['products'] });
      setOrderItems([]);
      setOrderSuccess(true);
      toast.success('Order created successfully!');
    },
    onError: (error: Error) => {
      toast.error(error.message || 'Failed to create order');
    },
  });

  const addToOrder = (product: ProductResponse) => {
    const existing = orderItems.find(item => item.product.productId === product.productId);
    if (existing) {
      if (existing.quantity < product.stock) {
        setOrderItems(items =>
          items.map(item =>
            item.product.productId === product.productId
              ? { ...item, quantity: item.quantity + 1 }
              : item
          )
        );
      } else {
        toast.error('Cannot add more than available stock');
      }
    } else {
      if (product.stock > 0) {
        setOrderItems(items => [...items, { product, quantity: 1 }]);
      } else {
        toast.error('Product is out of stock');
      }
    }
  };

  const updateQuantity = (productId: number, quantity: number) => {
    if (quantity <= 0) {
      removeFromOrder(productId);
      return;
    }
    const item = orderItems.find(i => i.product.productId === productId);
    if (item && quantity > item.product.stock) {
      toast.error('Cannot exceed available stock');
      return;
    }
    setOrderItems(items =>
      items.map(item =>
        item.product.productId === productId ? { ...item, quantity } : item
      )
    );
  };

  const removeFromOrder = (productId: number) => {
    setOrderItems(items => items.filter(item => item.product.productId !== productId));
  };

  const handleCreateOrder = () => {
    if (orderItems.length === 0) {
      toast.error('Please add at least one item to the order');
      return;
    }

    const commandItems: CommandItemRequest[] = orderItems.map(item => ({
      productId: item.product.productId,
      quantity: item.quantity,
    }));

    createOrderMutation.mutate({ items: commandItems });
  };

  const totalPrice = orderItems.reduce(
    (sum, item) => sum + item.product.price * item.quantity,
    0
  );

  const totalItems = orderItems.reduce((sum, item) => sum + item.quantity, 0);

  if (orderSuccess) {
    return (
      <div className="max-w-lg mx-auto py-12 animate-fade-in">
        <Card className="text-center">
          <CardContent className="py-12">
            <div className="w-16 h-16 mx-auto mb-6 rounded-full bg-success/10 flex items-center justify-center">
              <CheckCircle className="h-8 w-8 text-success" />
            </div>
            <h2 className="font-display text-2xl font-bold text-foreground mb-2">
              Order Created!
            </h2>
            <p className="text-muted-foreground mb-6">
              Your order has been successfully placed.
            </p>
            <div className="flex flex-col sm:flex-row gap-3 justify-center">
              <Link to="/orders">
                <Button className="gap-2">View All Orders</Button>
              </Link>
              <Button variant="outline" onClick={() => setOrderSuccess(false)} className="gap-2">
                Create Another Order
              </Button>
            </div>
          </CardContent>
        </Card>
      </div>
    );
  }

  if (isLoading) return <PageLoader />;
  if (error) return <ErrorDisplay error={error as Error} onRetry={refetch} />;

  return (
    <div className="space-y-6 animate-fade-in">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="font-display text-2xl md:text-3xl font-bold text-foreground">
            Create New Order
          </h1>
          <p className="text-muted-foreground">
            Select products and quantities to create a new command
          </p>
        </div>
        <Link to="/orders">
          <Button variant="outline" className="gap-2">
            <ArrowLeft className="h-4 w-4" />
            Back to Orders
          </Button>
        </Link>
      </div>

      <div className="grid lg:grid-cols-3 gap-6">
        {/* Product Selection */}
        <div className="lg:col-span-2 space-y-4">
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <Package className="h-5 w-5" />
                Available Products
              </CardTitle>
            </CardHeader>
            <CardContent>
              {!products || products.length === 0 ? (
                <EmptyState
                  icon={Package}
                  title="No products available"
                  description="Add some products first to create orders"
                  action={
                    <Link to="/products">
                      <Button>Go to Products</Button>
                    </Link>
                  }
                />
              ) : (
                <div className="grid sm:grid-cols-2 gap-3">
                  {products.map((product) => {
                    const inOrder = orderItems.find(i => i.product.productId === product.productId);
                    const isOutOfStock = product.stock <= 0;

                    return (
                      <div
                        key={product.productId}
                        className={cn(
                          "border rounded-lg p-4 transition-all",
                          inOrder && "border-primary bg-primary/5",
                          isOutOfStock && "opacity-50"
                        )}
                      >
                        <div className="flex justify-between items-start mb-2">
                          <div>
                            <h4 className="font-medium">{product.name}</h4>
                            <p className="text-sm text-muted-foreground">
                              ${product.price.toFixed(2)} • Stock: {product.stock}
                            </p>
                          </div>
                          {inOrder && (
                            <Badge variant="secondary">
                              {inOrder.quantity} selected
                            </Badge>
                          )}
                        </div>
                        <Button
                          onClick={() => addToOrder(product)}
                          disabled={isOutOfStock}
                          size="sm"
                          variant={inOrder ? "secondary" : "default"}
                          className="w-full gap-2"
                        >
                          <Plus className="h-4 w-4" />
                          {inOrder ? 'Add More' : 'Add to Order'}
                        </Button>
                      </div>
                    );
                  })}
                </div>
              )}
            </CardContent>
          </Card>
        </div>

        {/* Order Summary */}
        <div className="lg:col-span-1">
          <Card className="sticky top-24">
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <ShoppingBag className="h-5 w-5" />
                Order Summary
                {totalItems > 0 && (
                  <Badge variant="secondary">{totalItems} items</Badge>
                )}
              </CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              {orderItems.length === 0 ? (
                <p className="text-center text-muted-foreground py-8">
                  No items added yet
                </p>
              ) : (
                <div className="space-y-3">
                  {orderItems.map((item) => (
                    <div
                      key={item.product.productId}
                      className="flex items-center gap-3 p-3 bg-muted/50 rounded-lg"
                    >
                      <div className="flex-1 min-w-0">
                        <p className="font-medium text-sm truncate">
                          {item.product.name}
                        </p>
                        <p className="text-xs text-muted-foreground">
                          ${item.product.price.toFixed(2)} each
                        </p>
                      </div>
                      <div className="flex items-center gap-2">
                        <Button
                          variant="outline"
                          size="icon"
                          className="h-8 w-8"
                          onClick={() => updateQuantity(item.product.productId, item.quantity - 1)}
                        >
                          <Minus className="h-3 w-3" />
                        </Button>
                        <Input
                          type="number"
                          min="1"
                          max={item.product.stock}
                          value={item.quantity}
                          onChange={(e) => updateQuantity(item.product.productId, parseInt(e.target.value) || 0)}
                          className="w-14 h-8 text-center"
                        />
                        <Button
                          variant="outline"
                          size="icon"
                          className="h-8 w-8"
                          onClick={() => updateQuantity(item.product.productId, item.quantity + 1)}
                          disabled={item.quantity >= item.product.stock}
                        >
                          <Plus className="h-3 w-3" />
                        </Button>
                        <Button
                          variant="ghost"
                          size="icon"
                          className="h-8 w-8 text-destructive hover:bg-destructive/10"
                          onClick={() => removeFromOrder(item.product.productId)}
                        >
                          <Trash2 className="h-4 w-4" />
                        </Button>
                      </div>
                    </div>
                  ))}
                </div>
              )}

              {orderItems.length > 0 && (
                <div className="border-t border-border pt-4">
                  <div className="flex justify-between items-center mb-2">
                    <span className="text-muted-foreground">Subtotal</span>
                    <span className="font-medium">${totalPrice.toFixed(2)}</span>
                  </div>
                  <div className="flex justify-between items-center">
                    <span className="font-semibold">Total</span>
                    <span className="text-xl font-bold text-primary">
                      ${totalPrice.toFixed(2)}
                    </span>
                  </div>
                </div>
              )}
            </CardContent>
            <CardFooter>
              <Button
                onClick={handleCreateOrder}
                disabled={orderItems.length === 0 || createOrderMutation.isPending}
                className="w-full gap-2 bg-gradient-primary hover:opacity-90"
                size="lg"
              >
                {createOrderMutation.isPending ? (
                  <>
                    <Loader2 className="h-4 w-4 animate-spin" />
                    Creating Order...
                  </>
                ) : (
                  <>
                    <ShoppingBag className="h-4 w-4" />
                    Create Order
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

