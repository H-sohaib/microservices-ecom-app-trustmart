import { AlertCircle, RefreshCw, WifiOff } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Card, CardContent } from '@/components/ui/card';
import { ApiError } from '@/lib/api';

interface ErrorDisplayProps {
  error: Error | ApiError;
  onRetry?: () => void;
}

export function ErrorDisplay({ error, onRetry }: ErrorDisplayProps) {
  const isConnectionError = error instanceof ApiError && error.status === 0;

  return (
    <Card className="border-destructive/20 bg-destructive/5">
      <CardContent className="flex flex-col items-center justify-center py-12 gap-4">
        {isConnectionError ? (
          <WifiOff className="h-12 w-12 text-destructive" />
        ) : (
          <AlertCircle className="h-12 w-12 text-destructive" />
        )}
        <div className="text-center">
          <h3 className="font-semibold text-lg text-foreground mb-1">
            {isConnectionError ? 'Connection Error' : 'Something went wrong'}
          </h3>
          <p className="text-muted-foreground text-sm max-w-md">
            {error.message || 'An unexpected error occurred. Please try again.'}
          </p>
        </div>
        {onRetry && (
          <Button onClick={onRetry} variant="outline" className="gap-2 mt-2">
            <RefreshCw className="h-4 w-4" />
            Try Again
          </Button>
        )}
      </CardContent>
    </Card>
  );
}
