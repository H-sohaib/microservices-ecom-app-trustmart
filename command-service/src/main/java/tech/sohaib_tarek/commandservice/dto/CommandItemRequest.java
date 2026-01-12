package tech.sohaib_tarek.commandservice.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class CommandItemRequest {

  @NotNull(message = "Product ID is required")
  private Long productId;

  @NotNull(message = "Quantity is required")
  @Min(value = 1, message = "Quantity must be at least 1")
  private Integer quantity;

  public CommandItemRequest() {
  }

  public CommandItemRequest(Long productId, Integer quantity) {
    this.productId = productId;
    this.quantity = quantity;
  }

  public Long getProductId() {
    return productId;
  }

  public void setProductId(Long productId) {
    this.productId = productId;
  }

  public Integer getQuantity() {
    return quantity;
  }

  public void setQuantity(Integer quantity) {
    this.quantity = quantity;
  }

  public static CommandItemRequestBuilder builder() {
    return new CommandItemRequestBuilder();
  }

  public static class CommandItemRequestBuilder {
    private Long productId;
    private Integer quantity;

    public CommandItemRequestBuilder productId(Long productId) {
      this.productId = productId;
      return this;
    }

    public CommandItemRequestBuilder quantity(Integer quantity) {
      this.quantity = quantity;
      return this;
    }

    public CommandItemRequest build() {
      return new CommandItemRequest(productId, quantity);
    }
  }
}
