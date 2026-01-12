import { useState } from 'react';
import { Link } from 'react-router-dom';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { ClipboardList, Filter, Plus, LogIn } from 'lucide-react';
import { commandApi, productApi, CommandResponse, CommandStatus, CommandItemRequest } from '@/lib/api';
import { useAuth } from '@/context/AuthContext';
import { OrderCard } from '@/components/orders/OrderCard';
import { DeleteOrderDialog } from '@/components/orders/DeleteOrderDialog';
import { EditOrderDialog } from '@/components/orders/EditOrderDialog';
import { Button } from '@/components/ui/button';
import { PageLoader } from '@/components/ui/loading-spinner';
import { ErrorDisplay } from '@/components/ui/error-display';
import { EmptyState } from '@/components/ui/empty-state';
import { toast } from 'sonner';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';

const statusOptions: (CommandStatus | 'ALL')[] = [
  'ALL',
  'PENDING',
  'CONFIRMED',
  'PROCESSING',
  'SHIPPED',
  'DELIVERED',
  'CANCELLED',
];

export default function Orders() {
  const queryClient = useQueryClient();
  const { isAuthenticated, isLoading: authLoading, isAdmin, login } = useAuth();
  const [statusFilter, setStatusFilter] = useState<CommandStatus | 'ALL'>('ALL');
  const [deleteOrder, setDeleteOrder] = useState<CommandResponse | null>(null);
  const [editOrder, setEditOrder] = useState<CommandResponse | null>(null);

  const { data: orders, isLoading: ordersLoading, error: ordersError, refetch: refetchOrders } = useQuery({
    queryKey: ['orders', statusFilter],
    queryFn: () => commandApi.getAll(statusFilter === 'ALL' ? undefined : statusFilter),
    enabled: isAuthenticated,
  });

  const { data: products, isLoading: productsLoading, error: productsError } = useQuery({
    queryKey: ['products'],
    queryFn: productApi.getAll,
    enabled: isAuthenticated,
  });

  const updateStatusMutation = useMutation({
    mutationFn: ({ id, status }: { id: number; status: CommandStatus }) =>
      commandApi.updateStatus(id, status),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['orders'] });
      toast.success('Order status updated');
    },
    onError: (error: Error) => {
      toast.error(error.message || 'Failed to update status');
    },
  });

  const updateOrderMutation = useMutation({
    mutationFn: ({ id, items }: { id: number; items: CommandItemRequest[] }) =>
      commandApi.update(id, { items }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['orders'] });
      toast.success('Order updated successfully');
      setEditOrder(null);
    },
    onError: (error: Error) => {
      toast.error(error.message || 'Failed to update order');
    },
  });

  const deleteMutation = useMutation({
    mutationFn: commandApi.delete,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['orders'] });
      toast.success('Order deleted successfully');
      setDeleteOrder(null);
    },
    onError: (error: Error) => {
      toast.error(error.message || 'Failed to delete order');
    },
  });

  const cancelMutation = useMutation({
    mutationFn: commandApi.cancel,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['orders'] });
      toast.success('Order cancelled successfully');
    },
    onError: (error: Error) => {
      toast.error(error.message || 'Failed to cancel order');
    },
  });

  const handleStatusChange = (orderId: number, status: CommandStatus) => {
    updateStatusMutation.mutate({ id: orderId, status });
  };

  const handleDelete = async () => {
    if (deleteOrder) {
      await deleteMutation.mutateAsync(deleteOrder.commandId);
    }
  };

  const handleCancel = (order: CommandResponse) => {
    cancelMutation.mutate(order.commandId);
  };

  const handleUpdateOrder = async (items: CommandItemRequest[]) => {
    if (editOrder) {
      await updateOrderMutation.mutateAsync({ id: editOrder.commandId, items });
    }
  };

  // Show loading while checking auth
  if (authLoading) return <PageLoader />;

  // Require authentication for orders page
  if (!isAuthenticated) {
    return (
      <EmptyState
        icon={LogIn}
        title="Login Required"
        description="Please login to view and manage orders"
        action={
          <Button onClick={login} className="gap-2">
            <LogIn className="h-4 w-4" />
            Login
          </Button>
        }
      />
    );
  }

  const isLoading = ordersLoading || productsLoading;
  const error = ordersError || productsError;

  if (isLoading) return <PageLoader />;
  if (error) return <ErrorDisplay error={error as Error} onRetry={refetchOrders} />;

  return (
    <div className="space-y-6 animate-fade-in">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="font-display text-2xl md:text-3xl font-bold text-foreground">
            {isAdmin ? 'All Orders' : 'My Orders'}
          </h1>
          <p className="text-muted-foreground">
            {isAdmin ? 'Manage and track all customer orders' : 'View and manage your orders'}
          </p>
        </div>

        <div className="flex items-center gap-3">
          {!isAdmin && (
            <Link to="/new-order">
              <Button className="gap-2 bg-gradient-primary hover:opacity-90">
                <Plus className="h-4 w-4" />
                New Order
              </Button>
            </Link>
          )}

          <div className="flex items-center gap-2">
            <Filter className="h-4 w-4 text-muted-foreground" />
            <Select
              value={statusFilter}
              onValueChange={(value) => setStatusFilter(value as CommandStatus | 'ALL')}
            >
              <SelectTrigger className="w-[180px]">
                <SelectValue placeholder="Filter by status" />
              </SelectTrigger>
              <SelectContent>
                {statusOptions.map((status) => (
                  <SelectItem key={status} value={status}>
                    {status === 'ALL' ? 'All Orders' : status.charAt(0) + status.slice(1).toLowerCase()}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>
        </div>
      </div>

      {!orders || orders.length === 0 ? (
        <EmptyState
          icon={ClipboardList}
          title={statusFilter !== 'ALL' ? 'No orders found' : 'No orders yet'}
          description={
            statusFilter !== 'ALL'
              ? 'Try changing the status filter'
              : isAdmin
                ? 'No orders have been placed yet'
                : 'Create your first order to get started'
          }
          action={
            statusFilter !== 'ALL' ? (
              <Button variant="outline" onClick={() => setStatusFilter('ALL')}>
                View All Orders
              </Button>
            ) : !isAdmin ? (
              <Link to="/new-order">
                <Button className="gap-2">
                  <Plus className="h-4 w-4" />
                  Create New Order
                </Button>
              </Link>
            ) : null
          }
        />
      ) : (
        <div className="space-y-4">
          {orders.map((order, index) => (
            <div
              key={order.commandId}
              className="animate-slide-up"
              style={{ animationDelay: `${index * 50}ms` }}
            >
              <OrderCard
                order={order}
                products={products || []}
                onStatusChange={isAdmin ? handleStatusChange : undefined}
                onDelete={isAdmin ? setDeleteOrder : undefined}
                onEdit={isAdmin ? setEditOrder : undefined}
                onCancel={handleCancel}
                isUpdating={updateStatusMutation.isPending || cancelMutation.isPending}
                showUsername={isAdmin}
              />
            </div>
          ))}
        </div>
      )}

      <DeleteOrderDialog
        open={!!deleteOrder}
        onClose={() => setDeleteOrder(null)}
        onConfirm={handleDelete}
        order={deleteOrder}
        isLoading={deleteMutation.isPending}
      />

      <EditOrderDialog
        open={!!editOrder}
        onClose={() => setEditOrder(null)}
        onSubmit={handleUpdateOrder}
        order={editOrder}
        products={products || []}
        isLoading={updateOrderMutation.isPending}
      />
    </div>
  );
}
