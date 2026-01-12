import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { Users, Search, UserPlus, Shield, LogIn } from 'lucide-react';
import { userApi, UserResponse } from '@/lib/api';
import { useAuth } from '@/context/AuthContext';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { PageLoader } from '@/components/ui/loading-spinner';
import { ErrorDisplay } from '@/components/ui/error-display';
import { EmptyState } from '@/components/ui/empty-state';
import { ClientCard } from '@/components/clients/ClientCard';
import { CreateClientDialog } from '@/components/clients/CreateClientDialog';
import { DeleteClientDialog } from '@/components/clients/DeleteClientDialog';
import { toast } from 'sonner';

export default function Clients() {
  const queryClient = useQueryClient();
  const { isAuthenticated, isLoading: authLoading, isAdmin, login } = useAuth();
  const [search, setSearch] = useState('');
  const [createDialogOpen, setCreateDialogOpen] = useState(false);
  const [deleteClient, setDeleteClient] = useState<UserResponse | null>(null);

  const { data: clients, isLoading, error, refetch } = useQuery({
    queryKey: ['clients'],
    queryFn: userApi.getAll,
    enabled: isAuthenticated && isAdmin,
  });

  const toggleEnabledMutation = useMutation({
    mutationFn: ({ userId, enabled }: { userId: string; enabled: boolean }) =>
      userApi.toggleEnabled(userId, enabled),
    onSuccess: (_, { enabled }) => {
      queryClient.invalidateQueries({ queryKey: ['clients'] });
      toast.success(`Client ${enabled ? 'enabled' : 'disabled'} successfully`);
    },
    onError: (error: Error) => {
      toast.error(error.message || 'Failed to update client status');
    },
  });

  const deleteMutation = useMutation({
    mutationFn: userApi.delete,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['clients'] });
      toast.success('Client deleted successfully');
      setDeleteClient(null);
    },
    onError: (error: Error) => {
      toast.error(error.message || 'Failed to delete client');
    },
  });

  const handleToggleEnabled = (userId: string, enabled: boolean) => {
    toggleEnabledMutation.mutate({ userId, enabled });
  };

  const handleDelete = async () => {
    if (deleteClient) {
      await deleteMutation.mutateAsync(deleteClient.id);
    }
  };

  // Show loading while checking auth
  if (authLoading) return <PageLoader />;

  // Require authentication
  if (!isAuthenticated) {
    return (
      <EmptyState
        icon={LogIn}
        title="Login Required"
        description="Please login to access client management"
        action={
          <Button onClick={login} className="gap-2">
            <LogIn className="h-4 w-4" />
            Login
          </Button>
        }
      />
    );
  }

  // Require admin role
  if (!isAdmin) {
    return (
      <EmptyState
        icon={Shield}
        title="Access Denied"
        description="Only administrators can manage clients"
      />
    );
  }

  if (isLoading) return <PageLoader />;
  if (error) return <ErrorDisplay error={error as Error} onRetry={refetch} />;

  const filteredClients = clients?.filter((client) =>
    client.username.toLowerCase().includes(search.toLowerCase()) ||
    client.email?.toLowerCase().includes(search.toLowerCase()) ||
    client.firstName?.toLowerCase().includes(search.toLowerCase()) ||
    client.lastName?.toLowerCase().includes(search.toLowerCase())
  );

  return (
    <div className="space-y-6 animate-fade-in">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="font-display text-2xl md:text-3xl font-bold text-foreground">
            Client Management
          </h1>
          <p className="text-muted-foreground">
            View and manage registered clients
          </p>
        </div>
        <Button onClick={() => setCreateDialogOpen(true)} className="gap-2 bg-gradient-primary hover:opacity-90">
          <UserPlus className="h-4 w-4" />
          Add Client
        </Button>
      </div>

      <div className="relative max-w-md">
        <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
        <Input
          placeholder="Search clients..."
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          className="pl-10"
        />
      </div>

      {!filteredClients || filteredClients.length === 0 ? (
        <EmptyState
          icon={Users}
          title={search ? 'No clients found' : 'No clients yet'}
          description={
            search
              ? 'Try adjusting your search terms'
              : 'Clients will appear here when they register'
          }
          action={
            !search && (
              <Button onClick={() => setCreateDialogOpen(true)} className="gap-2">
                <UserPlus className="h-4 w-4" />
                Add Client
              </Button>
            )
          }
        />
      ) : (
        <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
          {filteredClients.map((client, index) => (
            <div
              key={client.id}
              className="animate-slide-up"
              style={{ animationDelay: `${index * 50}ms` }}
            >
              <ClientCard
                client={client}
                onToggleEnabled={handleToggleEnabled}
                onDelete={setDeleteClient}
                isUpdating={toggleEnabledMutation.isPending}
              />
            </div>
          ))}
        </div>
      )}

      <CreateClientDialog
        open={createDialogOpen}
        onClose={() => setCreateDialogOpen(false)}
      />

      <DeleteClientDialog
        open={!!deleteClient}
        onClose={() => setDeleteClient(null)}
        onConfirm={handleDelete}
        client={deleteClient}
        isLoading={deleteMutation.isPending}
      />
    </div>
  );
}

