import { Badge } from '@/components/ui/badge';
import { CommandStatus } from '@/lib/api';
import { cn } from '@/lib/utils';

interface StatusBadgeProps {
  status: CommandStatus;
  className?: string;
}

const statusConfig: Record<CommandStatus, { label: string; className: string }> = {
  PENDING: {
    label: 'Pending',
    className: 'bg-warning/10 text-warning border-warning/20',
  },
  CONFIRMED: {
    label: 'Confirmed',
    className: 'bg-info/10 text-info border-info/20',
  },
  PROCESSING: {
    label: 'Processing',
    className: 'bg-primary/10 text-primary border-primary/20',
  },
  SHIPPED: {
    label: 'Shipped',
    className: 'bg-info/10 text-info border-info/20',
  },
  DELIVERED: {
    label: 'Delivered',
    className: 'bg-success/10 text-success border-success/20',
  },
  CANCELLED: {
    label: 'Cancelled',
    className: 'bg-destructive/10 text-destructive border-destructive/20',
  },
};

export function StatusBadge({ status, className }: StatusBadgeProps) {
  const config = statusConfig[status];

  return (
    <Badge
      variant="outline"
      className={cn('font-medium', config.className, className)}
    >
      {config.label}
    </Badge>
  );
}
