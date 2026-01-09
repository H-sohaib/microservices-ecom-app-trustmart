import { format } from 'date-fns';
import { User, Mail, Trash2, Power, PowerOff } from 'lucide-react';
import { UserResponse } from '@/lib/api';
import { Card, CardContent, CardHeader } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { cn } from '@/lib/utils';

interface ClientCardProps {
  client: UserResponse;
  onToggleEnabled: (userId: string, enabled: boolean) => void;
  onDelete: (client: UserResponse) => void;
  isUpdating?: boolean;
}

export function ClientCard({
  client,
  onToggleEnabled,
  onDelete,
  isUpdating,
}: ClientCardProps) {
  const formattedDate = client.createdTimestamp
    ? format(new Date(client.createdTimestamp), 'MMM dd, yyyy')
    : 'N/A';

  return (
    <Card className="overflow-hidden transition-all duration-200 hover:shadow-card-hover">
      <CardHeader className="p-4 pb-3">
        <div className="flex items-start justify-between gap-3">
          <div className="flex items-center gap-3">
            <div className={cn(
              "flex h-10 w-10 items-center justify-center rounded-full",
              client.enabled ? "bg-primary/10" : "bg-muted"
            )}>
              <User className={cn(
                "h-5 w-5",
                client.enabled ? "text-primary" : "text-muted-foreground"
              )} />
            </div>
            <div>
              <div className="flex items-center gap-2">
                <span className="font-semibold text-foreground">
                  {client.username}
                </span>
                <Badge variant={client.enabled ? "default" : "secondary"}>
                  {client.enabled ? 'Active' : 'Disabled'}
                </Badge>
              </div>
              <p className="text-sm text-muted-foreground">
                {client.firstName} {client.lastName}
              </p>
            </div>
          </div>
        </div>
      </CardHeader>

      <CardContent className="p-4 pt-0 space-y-3">
        <div className="flex items-center gap-2 text-sm text-muted-foreground">
          <Mail className="h-4 w-4" />
          <span className="truncate">{client.email || 'No email'}</span>
        </div>

        <div className="text-xs text-muted-foreground">
          Registered: {formattedDate}
        </div>

        <div className="flex items-center gap-2 pt-2">
          <Button
            variant="outline"
            size="sm"
            onClick={() => onToggleEnabled(client.id, !client.enabled)}
            disabled={isUpdating}
            className={cn(
              "gap-2 flex-1",
              client.enabled
                ? "hover:bg-destructive/10 hover:text-destructive hover:border-destructive"
                : "hover:bg-success/10 hover:text-success hover:border-success"
            )}
          >
            {client.enabled ? (
              <>
                <PowerOff className="h-4 w-4" />
                Disable
              </>
            ) : (
              <>
                <Power className="h-4 w-4" />
                Enable
              </>
            )}
          </Button>
          <Button
            variant="outline"
            size="sm"
            onClick={() => onDelete(client)}
            className="gap-2 hover:bg-destructive/10 hover:text-destructive hover:border-destructive"
          >
            <Trash2 className="h-4 w-4" />
          </Button>
        </div>
      </CardContent>
    </Card>
  );
}

