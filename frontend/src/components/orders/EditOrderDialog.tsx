import { useState, useEffect } from 'react';
import { Plus, Minus, Trash2, Loader2 } from 'lucide-react';
import { CommandResponse, ProductResponse, CommandItemRequest } from '@/lib/api';
import { Button } from '@/components/ui/button';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogFooter,
} from '@/components/ui/dialog';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';

interface EditOrderDialogProps {
  open: boolean;
  onClose: () => void;
  onSubmit: (items: CommandItemRequest[]) => Promise<void>;
  order: CommandResponse | null;
  products: ProductResponse[];
  isLoading?: boolean;
}

export function EditOrderDialog({
  open,
  onClose,
  onSubmit,
  order,
  products,
  isLoading,
}: EditOrderDialogProps) {
  const [items, setItems] = useState<CommandItemRequest[]>([]);

  useEffect(() => {
    if (order) {
      setItems(
        order.items.map((item) => ({
          productId: item.productId,
          quantity: item.quantity,
        }))
      );
    }
  }, [order]);

  const handleAddProduct = (productId: string) => {
    const id = parseInt(productId);
    if (items.some((item) => item.productId === id)) return;
    setItems([...items, { productId: id, quantity: 1 }]);
  };

  const handleRemoveItem = (productId: number) => {
    setItems(items.filter((item) => item.productId !== productId));
  };

  const handleUpdateQuantity = (productId: number, delta: number) => {
    setItems(
      items.map((item) => {
        if (item.productId === productId) {
          const newQuantity = Math.max(1, item.quantity + delta);
          const product = products.find((p) => p.productId === productId);
          return {
            ...item,
            quantity: product ? Math.min(newQuantity, product.stock) : newQuantity,
          };
        }
        return item;
      })
    );
  };

  const handleSubmit = () => {
    if (items.length === 0) return;
    onSubmit(items);
  };

  const getProductName = (productId: number) => {
    const product = products.find((p) => p.productId === productId);
    return product?.name || `Product #${productId}`;
  };

  const getProductPrice = (productId: number) => {
    const product = products.find((p) => p.productId === productId);
    return product?.price || 0;
  };

  const availableProducts = products.filter(
    (p) => !items.some((item) => item.productId === p.productId) && p.stock > 0
  );

  const totalPrice = items.reduce(
    (sum, item) => sum + getProductPrice(item.productId) * item.quantity,
    0
  );

  return (
    <Dialog open={open} onOpenChange={onClose}>
      <DialogContent className="sm:max-w-[500px]">
        <DialogHeader>
          <DialogTitle className="font-display">
            Edit Order #{order?.commandId}
          </DialogTitle>
        </DialogHeader>

        <div className="space-y-4 py-4">
          {/* Add Product */}
          {availableProducts.length > 0 && (
            <div>
              <label className="text-sm font-medium text-foreground mb-2 block">
                Add Product
              </label>
              <Select onValueChange={handleAddProduct}>
                <SelectTrigger>
                  <SelectValue placeholder="Select a product to add" />
                </SelectTrigger>
                <SelectContent>
                  {availableProducts.map((product) => (
                    <SelectItem
                      key={product.productId}
                      value={product.productId.toString()}
                    >
                      {product.name} - ${product.price.toFixed(2)}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
          )}

          {/* Items List */}
          <div>
            <label className="text-sm font-medium text-foreground mb-2 block">
              Order Items
            </label>
            {items.length === 0 ? (
              <p className="text-sm text-muted-foreground py-4 text-center">
                No items in order. Add products above.
              </p>
            ) : (
              <div className="space-y-2">
                {items.map((item) => (
                  <div
                    key={item.productId}
                    className="flex items-center justify-between p-3 bg-muted/50 rounded-lg"
                  >
                    <div className="flex-1 min-w-0">
                      <p className="font-medium text-sm truncate">
                        {getProductName(item.productId)}
                      </p>
                      <p className="text-sm text-muted-foreground">
                        ${getProductPrice(item.productId).toFixed(2)} each
                      </p>
                    </div>

                    <div className="flex items-center gap-2">
                      <Button
                        variant="outline"
                        size="icon"
                        className="h-8 w-8"
                        onClick={() => handleUpdateQuantity(item.productId, -1)}
                      >
                        <Minus className="h-3 w-3" />
                      </Button>
                      <span className="w-8 text-center font-medium">
                        {item.quantity}
                      </span>
                      <Button
                        variant="outline"
                        size="icon"
                        className="h-8 w-8"
                        onClick={() => handleUpdateQuantity(item.productId, 1)}
                      >
                        <Plus className="h-3 w-3" />
                      </Button>
                      <Button
                        variant="ghost"
                        size="icon"
                        className="h-8 w-8 text-destructive hover:bg-destructive/10"
                        onClick={() => handleRemoveItem(item.productId)}
                      >
                        <Trash2 className="h-4 w-4" />
                      </Button>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>

          {/* Total */}
          <div className="flex items-center justify-between pt-4 border-t border-border">
            <span className="font-medium text-muted-foreground">
              Estimated Total
            </span>
            <span className="text-xl font-bold text-primary">
              ${totalPrice.toFixed(2)}
            </span>
          </div>
        </div>

        <DialogFooter>
          <Button variant="outline" onClick={onClose}>
            Cancel
          </Button>
          <Button
            onClick={handleSubmit}
            disabled={isLoading || items.length === 0}
            className="gap-2"
          >
            {isLoading && <Loader2 className="h-4 w-4 animate-spin" />}
            Update Order
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
