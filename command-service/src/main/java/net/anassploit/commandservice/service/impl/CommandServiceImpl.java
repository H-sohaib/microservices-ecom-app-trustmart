package net.anassploit.commandservice.service.impl;

import feign.FeignException;
import net.anassploit.commandservice.client.ProductClient;
import net.anassploit.commandservice.dto.*;
import net.anassploit.commandservice.entity.Command;
import net.anassploit.commandservice.entity.CommandItem;
import net.anassploit.commandservice.enums.CommandStatus;
import net.anassploit.commandservice.exception.CommandNotFoundException;
import net.anassploit.commandservice.exception.InsufficientStockException;
import net.anassploit.commandservice.exception.InvalidCommandStatusException;
import net.anassploit.commandservice.exception.ProductNotFoundException;
import net.anassploit.commandservice.repository.CommandRepository;
import net.anassploit.commandservice.service.CommandService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class CommandServiceImpl implements CommandService {

    private static final Logger log = LoggerFactory.getLogger(CommandServiceImpl.class);

    private final CommandRepository commandRepository;
    private final ProductClient productClient;

    public CommandServiceImpl(CommandRepository commandRepository, ProductClient productClient) {
        this.commandRepository = commandRepository;
        this.productClient = productClient;
    }

    @Override
    public CommandResponse createCommand(CommandRequest request, String userId, String username) {
        log.info("Creating new command with {} items for user: {}", request.getItems().size(), username);

        // Validate products and check stock availability
        List<StockUpdateRequest> stockUpdates = new ArrayList<>();
        BigDecimal totalPrice = BigDecimal.ZERO;
        List<CommandItem> commandItems = new ArrayList<>();

        for (CommandItemRequest itemRequest : request.getItems()) {
            ProductResponse product;
            try {
                product = productClient.getProductById(itemRequest.getProductId());
            } catch (FeignException.NotFound e) {
                throw new ProductNotFoundException("Product not found with ID: " + itemRequest.getProductId());
            }

            // Check stock availability
            Boolean hasStock = productClient.checkStock(itemRequest.getProductId(), itemRequest.getQuantity());
            if (!hasStock) {
                throw new InsufficientStockException("Insufficient stock for product: " + product.getName());
            }

            // Calculate item price
            BigDecimal itemPrice = product.getPrice();
            BigDecimal itemTotal = itemPrice.multiply(BigDecimal.valueOf(itemRequest.getQuantity()));
            totalPrice = totalPrice.add(itemTotal);

            // Create command item
            CommandItem commandItem = CommandItem.builder()
                    .productId(itemRequest.getProductId())
                    .quantity(itemRequest.getQuantity())
                    .price(itemPrice)
                    .build();
            commandItems.add(commandItem);

            // Prepare stock update
            stockUpdates.add(StockUpdateRequest.builder()
                    .productId(itemRequest.getProductId())
                    .quantity(itemRequest.getQuantity())
                    .build());
        }

        // Create command with user info
        Command command = Command.builder()
                .date(LocalDateTime.now())
                .status(CommandStatus.PENDING)
                .totalPrice(totalPrice)
                .userId(userId)
                .username(username)
                .items(new ArrayList<>())
                .build();

        // Add items to command
        for (CommandItem item : commandItems) {
            command.addItem(item);
        }

        // Reduce stock
        productClient.reduceStock(stockUpdates);

        // Save command
        Command savedCommand = commandRepository.save(command);
        log.info("Command created with ID: {} for user: {}", savedCommand.getCommandId(), username);

        return mapToResponse(savedCommand);
    }

    @Override
    @Transactional(readOnly = true)
    public CommandResponse getCommandById(Long commandId) {
        log.info("Fetching command with ID: {}", commandId);
        Command command = commandRepository.findById(commandId)
                .orElseThrow(() -> new CommandNotFoundException("Command not found with ID: " + commandId));
        return mapToResponse(command);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommandResponse> getAllCommands() {
        log.info("Fetching all commands");
        return commandRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommandResponse> getCommandsByStatus(CommandStatus status) {
        log.info("Fetching commands with status: {}", status);
        return commandRepository.findByStatus(status).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public CommandResponse updateCommand(Long commandId, CommandRequest request) {
        log.info("Updating command with ID: {}", commandId);
        Command command = commandRepository.findById(commandId)
                .orElseThrow(() -> new CommandNotFoundException("Command not found with ID: " + commandId));

        // Only allow update if command is in PENDING status
        if (command.getStatus() != CommandStatus.PENDING) {
            throw new InvalidCommandStatusException("Cannot update command with status: " + command.getStatus());
        }

        // Restore old stock
        List<StockUpdateRequest> restoreStock = command.getItems().stream()
                .map(item -> StockUpdateRequest.builder()
                        .productId(item.getProductId())
                        .quantity(item.getQuantity())
                        .build())
                .collect(Collectors.toList());
        productClient.restoreStock(restoreStock);

        // Clear old items
        command.getItems().clear();

        // Validate new products and check stock availability
        List<StockUpdateRequest> stockUpdates = new ArrayList<>();
        BigDecimal totalPrice = BigDecimal.ZERO;

        for (CommandItemRequest itemRequest : request.getItems()) {
            ProductResponse product;
            try {
                product = productClient.getProductById(itemRequest.getProductId());
            } catch (FeignException.NotFound e) {
                throw new ProductNotFoundException("Product not found with ID: " + itemRequest.getProductId());
            }

            // Check stock availability
            Boolean hasStock = productClient.checkStock(itemRequest.getProductId(), itemRequest.getQuantity());
            if (!hasStock) {
                throw new InsufficientStockException("Insufficient stock for product: " + product.getName());
            }

            // Calculate item price
            BigDecimal itemPrice = product.getPrice();
            BigDecimal itemTotal = itemPrice.multiply(BigDecimal.valueOf(itemRequest.getQuantity()));
            totalPrice = totalPrice.add(itemTotal);

            // Create command item
            CommandItem commandItem = CommandItem.builder()
                    .productId(itemRequest.getProductId())
                    .quantity(itemRequest.getQuantity())
                    .price(itemPrice)
                    .build();

            // Prepare stock update
            stockUpdates.add(StockUpdateRequest.builder()
                    .productId(itemRequest.getProductId())
                    .quantity(itemRequest.getQuantity())
                    .build());

            command.addItem(commandItem);
        }

        // Reduce stock for new items
        productClient.reduceStock(stockUpdates);

        command.setTotalPrice(totalPrice);

        // Save updated command
        Command updatedCommand = commandRepository.save(command);
        log.info("Command updated successfully: {}", commandId);

        return mapToResponse(updatedCommand);
    }

    @Override
    public CommandResponse updateCommandStatus(Long commandId, CommandStatus status) {
        log.info("Updating command {} status to {}", commandId, status);
        Command command = commandRepository.findById(commandId)
                .orElseThrow(() -> new CommandNotFoundException("Command not found with ID: " + commandId));

        // Validate status transition
        validateStatusTransition(command.getStatus(), status);

        command.setStatus(status);
        Command updatedCommand = commandRepository.save(command);
        log.info("Command status updated successfully: {} -> {}", commandId, status);

        return mapToResponse(updatedCommand);
    }

    @Override
    public void deleteCommand(Long commandId) {
        log.info("Deleting command with ID: {}", commandId);
        Command command = commandRepository.findById(commandId)
                .orElseThrow(() -> new CommandNotFoundException("Command not found with ID: " + commandId));

        // Only allow deletion if command is CANCELLED
        if (command.getStatus() != CommandStatus.CANCELLED) {
            throw new InvalidCommandStatusException("Cannot delete command with status: " + command.getStatus() + ". Cancel the command first.");
        }

        commandRepository.deleteById(commandId);
        log.info("Command deleted successfully: {}", commandId);
    }

    @Override
    public void cancelCommand(Long commandId, String userId, boolean isAdmin) {
        log.info("Cancelling command with ID: {} by user: {} (isAdmin: {})", commandId, userId, isAdmin);
        Command command = commandRepository.findById(commandId)
                .orElseThrow(() -> new CommandNotFoundException("Command not found with ID: " + commandId));

        // Check if user is owner or admin
        if (!isAdmin && !command.getUserId().equals(userId)) {
            throw new InvalidCommandStatusException("You don't have permission to cancel this order");
        }

        // Only allow cancellation if command is PENDING or CONFIRMED
        if (command.getStatus() != CommandStatus.PENDING && command.getStatus() != CommandStatus.CONFIRMED) {
            throw new InvalidCommandStatusException("Cannot cancel command with status: " + command.getStatus());
        }

        // Restore stock
        List<StockUpdateRequest> restoreStock = command.getItems().stream()
                .map(item -> StockUpdateRequest.builder()
                        .productId(item.getProductId())
                        .quantity(item.getQuantity())
                        .build())
                .collect(Collectors.toList());

        productClient.restoreStock(restoreStock);

        command.setStatus(CommandStatus.CANCELLED);
        commandRepository.save(command);
        log.info("Command cancelled successfully: {}", commandId);
    }

    private void validateStatusTransition(CommandStatus currentStatus, CommandStatus newStatus) {
        // Define valid status transitions
        switch (currentStatus) {
            case PENDING:
                if (newStatus != CommandStatus.CONFIRMED && newStatus != CommandStatus.CANCELLED) {
                    throw new InvalidCommandStatusException(
                            "Invalid status transition from " + currentStatus + " to " + newStatus);
                }
                break;
            case CONFIRMED:
                if (newStatus != CommandStatus.PROCESSING && newStatus != CommandStatus.CANCELLED) {
                    throw new InvalidCommandStatusException(
                            "Invalid status transition from " + currentStatus + " to " + newStatus);
                }
                break;
            case PROCESSING:
                if (newStatus != CommandStatus.SHIPPED) {
                    throw new InvalidCommandStatusException(
                            "Invalid status transition from " + currentStatus + " to " + newStatus);
                }
                break;
            case SHIPPED:
                if (newStatus != CommandStatus.DELIVERED) {
                    throw new InvalidCommandStatusException(
                            "Invalid status transition from " + currentStatus + " to " + newStatus);
                }
                break;
            case DELIVERED:
            case CANCELLED:
                throw new InvalidCommandStatusException(
                        "Cannot change status from " + currentStatus);
            default:
                throw new InvalidCommandStatusException("Unknown status: " + currentStatus);
        }
    }

    private CommandResponse mapToResponse(Command command) {
        List<CommandItemResponse> itemResponses = command.getItems().stream()
                .map(item -> CommandItemResponse.builder()
                        .productId(item.getProductId())
                        .quantity(item.getQuantity())
                        .price(item.getPrice())
                        .build())
                .collect(Collectors.toList());

        return CommandResponse.builder()
                .commandId(command.getCommandId())
                .date(command.getDate())
                .status(command.getStatus())
                .totalPrice(command.getTotalPrice())
                .userId(command.getUserId())
                .username(command.getUsername())
                .items(itemResponses)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommandResponse> getCommandsByUserId(String userId) {
        log.info("Fetching commands for user: {}", userId);
        return commandRepository.findByUserId(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommandResponse> getCommandsByUserIdAndStatus(String userId, CommandStatus status) {
        log.info("Fetching commands for user: {} with status: {}", userId, status);
        return commandRepository.findByUserIdAndStatus(userId, status).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isCommandOwner(Long commandId, String userId) {
        return commandRepository.existsByCommandIdAndUserId(commandId, userId);
    }
}

