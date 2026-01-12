package tech.sohaib_tarek.commandservice.dto;

import jakarta.validation.constraints.NotNull;
import tech.sohaib_tarek.commandservice.enums.CommandStatus;

public class CommandStatusUpdateRequest {

    @NotNull(message = "Status is required")
    private CommandStatus status;

    public CommandStatusUpdateRequest() {
    }

    public CommandStatusUpdateRequest(CommandStatus status) {
        this.status = status;
    }

    public CommandStatus getStatus() {
        return status;
    }

    public void setStatus(CommandStatus status) {
        this.status = status;
    }

    public static CommandStatusUpdateRequestBuilder builder() {
        return new CommandStatusUpdateRequestBuilder();
    }

    public static class CommandStatusUpdateRequestBuilder {
        private CommandStatus status;

        public CommandStatusUpdateRequestBuilder status(CommandStatus status) {
            this.status = status;
            return this;
        }

        public CommandStatusUpdateRequest build() {
            return new CommandStatusUpdateRequest(status);
        }
    }
}

