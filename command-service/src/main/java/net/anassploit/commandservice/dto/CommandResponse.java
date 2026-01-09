package net.anassploit.commandservice.dto;

import net.anassploit.commandservice.enums.CommandStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class CommandResponse {

    private Long commandId;
    private LocalDateTime date;
    private CommandStatus status;
    private BigDecimal totalPrice;
    private String userId;
    private String username;
    private List<CommandItemResponse> items;

    public CommandResponse() {
    }

    public CommandResponse(Long commandId, LocalDateTime date, CommandStatus status, BigDecimal totalPrice, String userId, String username, List<CommandItemResponse> items) {
        this.commandId = commandId;
        this.date = date;
        this.status = status;
        this.totalPrice = totalPrice;
        this.userId = userId;
        this.username = username;
        this.items = items;
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

    public List<CommandItemResponse> getItems() {
        return items;
    }

    public void setItems(List<CommandItemResponse> items) {
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

    public static CommandResponseBuilder builder() {
        return new CommandResponseBuilder();
    }

    public static class CommandResponseBuilder {
        private Long commandId;
        private LocalDateTime date;
        private CommandStatus status;
        private BigDecimal totalPrice;
        private String userId;
        private String username;
        private List<CommandItemResponse> items;

        public CommandResponseBuilder commandId(Long commandId) {
            this.commandId = commandId;
            return this;
        }

        public CommandResponseBuilder date(LocalDateTime date) {
            this.date = date;
            return this;
        }

        public CommandResponseBuilder status(CommandStatus status) {
            this.status = status;
            return this;
        }

        public CommandResponseBuilder totalPrice(BigDecimal totalPrice) {
            this.totalPrice = totalPrice;
            return this;
        }

        public CommandResponseBuilder userId(String userId) {
            this.userId = userId;
            return this;
        }

        public CommandResponseBuilder username(String username) {
            this.username = username;
            return this;
        }

        public CommandResponseBuilder items(List<CommandItemResponse> items) {
            this.items = items;
            return this;
        }

        public CommandResponse build() {
            return new CommandResponse(commandId, date, status, totalPrice, userId, username, items);
        }
    }
}

