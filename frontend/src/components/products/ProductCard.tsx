import { ShoppingCart, Edit, Trash2, Package, AlertCircle } from 'lucide-react';
import { ProductResponse } from '@/lib/api';
import { useCart } from '@/context/CartContext';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardFooter, CardHeader } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { toast } from 'sonner';
import { cn } from '@/lib/utils';

interface ProductCardProps {
  product: ProductResponse;
  onEdit?: (product: ProductResponse) => void;
  onDelete?: (product: ProductResponse) => void;
  showActions?: boolean;
}

export function ProductCard({ product, onEdit, onDelete, showActions = true }: ProductCardProps) {
  const { addToCart } = useCart();

  const handleAddToCart = () => {
    if (product.stock <= 0) {
      toast.error('This product is out of stock');
      return;
    }
    addToCart(product);
    toast.success(`${product.name} added to cart`);
  };

  const isOutOfStock = product.stock <= 0;
  const isLowStock = product.stock > 0 && product.stock <= 5;

  return (
    <Card
      className={cn(
        'group relative overflow-hidden transition-all duration-300 hover:shadow-lg hover:-translate-y-1',
        isOutOfStock && 'opacity-75'
      )}
    >
      <CardHeader className="pb-2">
        <div className="flex items-start justify-between gap-2">
          <div className="flex items-center gap-2">
            <div className="p-2 rounded-lg bg-primary/10">
              <Package className="h-5 w-5 text-primary" />
            </div>
            <div>
              <h3 className="font-semibold text-lg text-foreground line-clamp-1">
                {product.name}
              </h3>
              <p className="text-xs text-muted-foreground">ID: #{product.productId}</p>
            </div>
          </div>
          {isOutOfStock && (
            <Badge variant="destructive" className="text-xs">
              <AlertCircle className="h-3 w-3 mr-1" />
              Out of Stock
            </Badge>
          )}
          {isLowStock && !isOutOfStock && (
            <Badge className="bg-warning text-warning-foreground text-xs">
              Low Stock: {product.stock}
            </Badge>
          )}
        </div>
      </CardHeader>

      <CardContent className="pb-3">
        {product.description && (
          <p className="text-muted-foreground text-sm line-clamp-2 mb-4">
            {product.description}
          </p>
        )}
        <div className="flex items-center justify-between bg-muted/50 rounded-lg p-3">
          <div>
            <span className="text-2xl font-bold text-primary">
              ${product.price.toFixed(2)}
            </span>
          </div>
          <div className="text-right">
            <span className="text-sm text-muted-foreground">Stock</span>
            <p className={cn(
              "font-semibold",
              isOutOfStock && "text-destructive",
              isLowStock && !isOutOfStock && "text-warning",
              !isOutOfStock && !isLowStock && "text-success"
            )}>
              {product.stock} units
            </p>
          </div>
        </div>
      </CardContent>

      <CardFooter className="pt-0 flex gap-2">
        <Button
          onClick={handleAddToCart}
          disabled={isOutOfStock}
          className="flex-1 gap-2 bg-gradient-primary hover:opacity-90 transition-opacity"
        >
          <ShoppingCart className="h-4 w-4" />
          Add to Cart
        </Button>
        
        {showActions && (
          <div className="flex gap-1">
            {onEdit && (
              <Button
                variant="outline"
                size="icon"
                onClick={() => onEdit(product)}
                className="hover:bg-primary/10 hover:text-primary hover:border-primary"
              >
                <Edit className="h-4 w-4" />
              </Button>
            )}
            {onDelete && (
              <Button
                variant="outline"
                size="icon"
                onClick={() => onDelete(product)}
                className="hover:bg-destructive/10 hover:text-destructive hover:border-destructive"
              >
                <Trash2 className="h-4 w-4" />
              </Button>
            )}
          </div>
        )}
      </CardFooter>
    </Card>
  );
}
