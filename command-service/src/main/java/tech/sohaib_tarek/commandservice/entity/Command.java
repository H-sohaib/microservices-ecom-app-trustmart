package tech.sohaib_tarek.commandservice.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import tech.sohaib_tarek.commandservice.enums.CommandStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "commands")
public class Command {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long commandId;

    @Column(nullable = false)
    private LocalDateTime date;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CommandStatus status;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private String username;

    @OneToMany(mappedBy = "command", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JsonManagedReference
    private List<CommandItem> items = new ArrayList<>();

    public Command() {
    }

    public Command(Long commandId, LocalDateTime date, CommandStatus status, BigDecimal totalPrice, String userId, String username, List<CommandItem> items) {
        this.commandId = commandId;
        this.date = date;
        this.status = status;
        this.totalPrice = totalPrice;
        this.userId = userId;
        this.username = username;
        this.items = items != null ? items : new ArrayList<>();
    }

    @PrePersist
    protected void onCreate() {
        if (date == null) {
            date = LocalDateTime.now();
        }
        if (status == null) {
            status = CommandStatus.PENDING;
        }
    }

    public void addItem(CommandItem item) {
        items.add(item);
        item.setCommand(this);
    }

    public void removeItem(CommandItem item) {
        items.remove(item);
        item.setCommand(null);
    }

    public Long getCommandId() {
        return commandId;
    }

    public void setCommandId(Long commandId) {
        this.commandId = commandId;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public CommandStatus getStatus() {
        return status;
    }

    public void setStatus(CommandStatus status) {
        this.status = status;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public List<CommandItem> getItems() {
        return items;
    }

    public void setItems(List<CommandItem> items) {
        this.items = items;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public static CommandBuilder builder() {
        return new CommandBuilder();
    }

    public static class CommandBuilder {
        private Long commandId;
        private LocalDateTime date;
        private CommandStatus status;
        private BigDecimal totalPrice;
        private String userId;
        private String username;
        private List<CommandItem> items = new ArrayList<>();

        public CommandBuilder commandId(Long commandId) {
            this.commandId = commandId;
            return this;
        }

        public CommandBuilder date(LocalDateTime date) {
            this.date = date;
            return this;
        }

        public CommandBuilder status(CommandStatus status) {
            this.status = status;
            return this;
        }

        public CommandBuilder totalPrice(BigDecimal totalPrice) {
            this.totalPrice = totalPrice;
            return this;
        }

        public CommandBuilder userId(String userId) {
            this.userId = userId;
            return this;
        }

        public CommandBuilder username(String username) {
            this.username = username;
            return this;
        }

        public CommandBuilder items(List<CommandItem> items) {
            this.items = items;
            return this;
        }

        public Command build() {
            return new Command(commandId, date, status, totalPrice, userId, username, items);
        }
    }
}

