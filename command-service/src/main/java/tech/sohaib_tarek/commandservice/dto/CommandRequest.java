package tech.sohaib_tarek.commandservice.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public class CommandRequest {

    @NotEmpty(message = "Command must have at least one item")
    @Valid
    private List<CommandItemRequest> items;

    public CommandRequest() {
    }

    public CommandRequest(List<CommandItemRequest> items) {
        this.items = items;
    }

    public List<CommandItemRequest> getItems() {
        return items;
    }

    public void setItems(List<CommandItemRequest> items) {
        this.items = items;
    }

    public static CommandRequestBuilder builder() {
        return new CommandRequestBuilder();
    }

    public static class CommandRequestBuilder {
        private List<CommandItemRequest> items;

        public CommandRequestBuilder items(List<CommandItemRequest> items) {
            this.items = items;
            return this;
        }

        public CommandRequest build() {
            return new CommandRequest(items);
        }
    }
}

