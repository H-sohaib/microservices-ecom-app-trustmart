import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { Plus, Package, Search } from 'lucide-react';
import { productApi, ProductResponse, ProductRequest } from '@/lib/api';
import { useAuth } from '@/context/AuthContext';
import { ProductCard } from '@/components/products/ProductCard';
import { ProductForm } from '@/components/products/ProductForm';
import { DeleteProductDialog } from '@/components/products/DeleteProductDialog';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { PageLoader } from '@/components/ui/loading-spinner';
import { ErrorDisplay } from '@/components/ui/error-display';
import { EmptyState } from '@/components/ui/empty-state';
import { toast } from 'sonner';

export default function Products() {
  const queryClient = useQueryClient();
  const { isAdmin } = useAuth();
  const [search, setSearch] = useState('');
  const [formOpen, setFormOpen] = useState(false);
  const [editProduct, setEditProduct] = useState<ProductResponse | undefined>();
  const [deleteProduct, setDeleteProduct] = useState<ProductResponse | null>(null);

  const { data: products, isLoading, error, refetch } = useQuery({
    queryKey: ['products'],
    queryFn: productApi.getAll,
  });

  const createMutation = useMutation({
    mutationFn: productApi.create,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['products'] });
      toast.success('Product created successfully');
      handleCloseForm();
    },
    onError: (error: Error) => {
      toast.error(error.message || 'Failed to create product');
    },
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, data }: { id: number; data: ProductRequest }) =>
      productApi.update(id, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['products'] });
      toast.success('Product updated successfully');
      handleCloseForm();
    },
    onError: (error: Error) => {
      toast.error(error.message || 'Failed to update product');
    },
  });

  const deleteMutation = useMutation({
    mutationFn: productApi.delete,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['products'] });
      toast.success('Product deleted successfully');
      setDeleteProduct(null);
    },
    onError: (error: Error) => {
      toast.error(error.message || 'Failed to delete product');
    },
  });

  const handleCloseForm = () => {
    setFormOpen(false);
    setEditProduct(undefined);
  };

  const handleEdit = (product: ProductResponse) => {
    setEditProduct(product);
    setFormOpen(true);
  };

  const handleSubmit = async (data: ProductRequest) => {
    if (editProduct) {
      await updateMutation.mutateAsync({ id: editProduct.productId, data });
    } else {
      await createMutation.mutateAsync(data);
    }
  };

  const handleDelete = async () => {
    if (deleteProduct) {
      await deleteMutation.mutateAsync(deleteProduct.productId);
    }
  };

  const filteredProducts = products?.filter((product) =>
    product.name.toLowerCase().includes(search.toLowerCase())
  );

  if (isLoading) return <PageLoader />;
  if (error) return <ErrorDisplay error={error as Error} onRetry={refetch} />;

  return (
    <div className="space-y-6 animate-fade-in">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="font-display text-2xl md:text-3xl font-bold text-foreground">
            Products
          </h1>
          <p className="text-muted-foreground">
            {isAdmin ? 'Manage your product catalog' : 'Browse our product catalog'}
          </p>
        </div>
        {isAdmin && (
          <Button onClick={() => setFormOpen(true)} className="gap-2 bg-gradient-primary hover:opacity-90">
            <Plus className="h-4 w-4" />
            Add Product
          </Button>
        )}
      </div>

      <div className="relative max-w-md">
        <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
        <Input
          placeholder="Search products..."
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          className="pl-10"
        />
      </div>

      {!filteredProducts || filteredProducts.length === 0 ? (
        <EmptyState
          icon={Package}
          title={search ? 'No products found' : 'No products yet'}
          description={
            search
              ? 'Try adjusting your search terms'
              : isAdmin
                ? 'Add your first product to get started'
                : 'No products available at the moment'
          }
          action={
            !search && isAdmin && (
              <Button onClick={() => setFormOpen(true)} className="gap-2">
                <Plus className="h-4 w-4" />
                Add Product
              </Button>
            )
          }
        />
      ) : (
        <div className="grid sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
          {filteredProducts.map((product, index) => (
            <div
              key={product.productId}
              className="animate-slide-up"
              style={{ animationDelay: `${index * 50}ms` }}
            >
              <ProductCard
                product={product}
                onEdit={isAdmin ? handleEdit : undefined}
                onDelete={isAdmin ? setDeleteProduct : undefined}
              />
            </div>
          ))}
        </div>
      )}

      <ProductForm
        open={formOpen}
        onClose={handleCloseForm}
        onSubmit={handleSubmit}
        product={editProduct}
        isLoading={createMutation.isPending || updateMutation.isPending}
      />

      <DeleteProductDialog
        open={!!deleteProduct}
        onClose={() => setDeleteProduct(null)}
        onConfirm={handleDelete}
        product={deleteProduct}
        isLoading={deleteMutation.isPending}
      />
    </div>
  );
}
