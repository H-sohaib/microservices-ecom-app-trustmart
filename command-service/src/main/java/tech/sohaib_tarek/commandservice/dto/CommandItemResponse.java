package tech.sohaib_tarek.commandservice.dto;

import java.math.BigDecimal;

public class CommandItemResponse {

    private Long productId;
    private Integer quantity;
    private BigDecimal price;

    public CommandItemResponse() {
    }

    public CommandItemResponse(Long productId, Integer quantity, BigDecimal price) {
        this.productId = productId;
        this.quantity = quantity;
        this.price = price;
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

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public static CommandItemResponseBuilder builder() {
        return new CommandItemResponseBuilder();
    }

    public static class CommandItemResponseBuilder {
        private Long productId;
        private Integer quantity;
        private BigDecimal price;

        public CommandItemResponseBuilder productId(Long productId) {
            this.productId = productId;
            return this;
        }

        public CommandItemResponseBuilder quantity(Integer quantity) {
            this.quantity = quantity;
            return this;
        }

        public CommandItemResponseBuilder price(BigDecimal price) {
            this.price = price;
            return this;
        }

        public CommandItemResponse build() {
            return new CommandItemResponse(productId, quantity, price);
        }
    }
}

