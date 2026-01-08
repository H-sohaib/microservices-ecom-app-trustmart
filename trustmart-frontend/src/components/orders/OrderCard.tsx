import { format } from 'date-fns';
import { Eye, Trash2, ChevronDown, ChevronUp } from 'lucide-react';
import { useState } from 'react';
import { CommandResponse, CommandStatus, ProductResponse } from '@/lib/api';
import { Card, CardContent, CardHeader } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { StatusBadge } from '@/components/ui/status-badge';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import { cn } from '@/lib/utils';

interface OrderCardProps {
  order: CommandResponse;
  products: ProductResponse[];
  onStatusChange?: (orderId: number, status: CommandStatus) => void;
  onDelete?: (order: CommandResponse) => void;
  onEdit?: (order: CommandResponse) => void;
  isUpdating?: boolean;
}

const statusOptions: CommandStatus[] = [
  'PENDING',
  'CONFIRMED',
  'PROCESSING',
  'SHIPPED',
  'DELIVERED',
  'CANCELLED',
];

export function OrderCard({
  order,
  products,
  onStatusChange,
  onDelete,
  onEdit,
  isUpdating,
}: OrderCardProps) {
  const [isExpanded, setIsExpanded] = useState(false);

  const getProductName = (productId: number) => {
    const product = products.find((p) => p.productId === productId);
    return product?.name || `Product #${productId}`;
  };

  const formattedDate = format(new Date(order.date), 'MMM dd, yyyy HH:mm');

  return (
    <Card className="overflow-hidden transition-all duration-200 hover:shadow-card-hover">
      <CardHeader className="p-4 pb-3">
        <div className="flex items-center justify-between gap-4">
          <div className="flex-1 min-w-0">
            <div className="flex items-center gap-3 mb-1">
              <span className="font-semibold text-foreground">
                Order #{order.commandId}
              </span>
              <StatusBadge status={order.status} />
            </div>
            <p className="text-sm text-muted-foreground">{formattedDate}</p>
          </div>

          <div className="flex items-center gap-2">
            <span className="font-bold text-lg text-primary">
              ${order.totalPrice.toFixed(2)}
            </span>
            <Button
              variant="ghost"
              size="icon"
              onClick={() => setIsExpanded(!isExpanded)}
              className="shrink-0"
            >
              {isExpanded ? (
                <ChevronUp className="h-4 w-4" />
              ) : (
                <ChevronDown className="h-4 w-4" />
              )}
            </Button>
          </div>
        </div>
      </CardHeader>

      <div
        className={cn(
          'overflow-hidden transition-all duration-300',
          isExpanded ? 'max-h-96' : 'max-h-0'
        )}
      >
        <CardContent className="p-4 pt-0 border-t border-border">
          <div className="space-y-4">
            {/* Order Items */}
            <div>
              <h4 className="font-medium text-sm text-muted-foreground mb-2">
                Items ({order.items.length})
              </h4>
              <div className="space-y-2">
                {order.items.map((item, index) => (
                  <div
                    key={index}
                    className="flex items-center justify-between py-2 px-3 bg-muted/50 rounded-lg"
                  >
                    <span className="text-sm font-medium">
                      {getProductName(item.productId)}
                    </span>
                    <div className="text-sm text-muted-foreground">
                      {item.quantity} × ${item.price.toFixed(2)}
                    </div>
                  </div>
                ))}
              </div>
            </div>

            {/* Actions */}
            <div className="flex flex-wrap items-center gap-3 pt-2">
              {onStatusChange && (
                <Select
                  value={order.status}
                  onValueChange={(value) =>
                    onStatusChange(order.commandId, value as CommandStatus)
                  }
                  disabled={isUpdating}
                >
                  <SelectTrigger className="w-[160px]">
                    <SelectValue placeholder="Change status" />
                  </SelectTrigger>
                  <SelectContent>
                    {statusOptions.map((status) => (
                      <SelectItem key={status} value={status}>
                        {status.charAt(0) + status.slice(1).toLowerCase()}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              )}

              <div className="flex gap-2 ml-auto">
                {onEdit && (
                  <Button
                    variant="outline"
                    size="sm"
                    onClick={() => onEdit(order)}
                    className="gap-2"
                  >
                    <Eye className="h-4 w-4" />
                    Edit
                  </Button>
                )}
                {onDelete && (
                  <Button
                    variant="outline"
                    size="sm"
                    onClick={() => onDelete(order)}
                    className="gap-2 hover:bg-destructive/10 hover:text-destructive hover:border-destructive"
                  >
                    <Trash2 className="h-4 w-4" />
                    Delete
                  </Button>
                )}
              </div>
            </div>
          </div>
        </CardContent>
      </div>
    </Card>
  );
}
