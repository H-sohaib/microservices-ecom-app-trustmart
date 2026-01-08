import { Plus, Minus, Trash2 } from 'lucide-react';
import { CartItem as CartItemType } from '@/context/CartContext';
import { Button } from '@/components/ui/button';
import { useCart } from '@/context/CartContext';

interface CartItemProps {
  item: CartItemType;
}

export function CartItem({ item }: CartItemProps) {
  const { updateQuantity, removeFromCart } = useCart();
  const { product, quantity } = item;

  return (
    <div className="flex items-center gap-4 p-4 bg-card rounded-lg border border-border">
      <div className="w-16 h-16 bg-muted rounded-lg flex items-center justify-center shrink-0">
        <span className="text-2xl text-muted-foreground/50">📦</span>
      </div>

      <div className="flex-1 min-w-0">
        <h3 className="font-medium text-foreground truncate">{product.name}</h3>
        <p className="text-sm text-muted-foreground">
          ${product.price.toFixed(2)} each
        </p>
      </div>

      <div className="flex items-center gap-2">
        <Button
          variant="outline"
          size="icon"
          className="h-8 w-8"
          onClick={() => updateQuantity(product.productId, quantity - 1)}
        >
          <Minus className="h-3 w-3" />
        </Button>
        <span className="w-8 text-center font-medium">{quantity}</span>
        <Button
          variant="outline"
          size="icon"
          className="h-8 w-8"
          onClick={() => updateQuantity(product.productId, quantity + 1)}
          disabled={quantity >= product.stock}
        >
          <Plus className="h-3 w-3" />
        </Button>
      </div>

      <div className="text-right">
        <p className="font-semibold text-foreground">
          ${(product.price * quantity).toFixed(2)}
        </p>
        <Button
          variant="ghost"
          size="sm"
          className="text-destructive hover:text-destructive hover:bg-destructive/10 -mr-2"
          onClick={() => removeFromCart(product.productId)}
        >
          <Trash2 className="h-4 w-4" />
        </Button>
      </div>
    </div>
  );
}
