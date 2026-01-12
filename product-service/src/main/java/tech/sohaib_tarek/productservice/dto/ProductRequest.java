package net.anassploit.productservice.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public class ProductRequest {

    @NotBlank(message = "Product name is required")
    @Size(min = 2, max = 100, message = "Product name must be between 2 and 100 characters")
    private String name;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    private BigDecimal price;

    @NotNull(message = "Stock is required")
    @Min(value = 0, message = "Stock cannot be negative")
    private Integer stock;

    public ProductRequest() {
    }

    public ProductRequest(String name, String description, BigDecimal price, Integer stock) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.stock = stock;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public static ProductRequestBuilder builder() {
        return new ProductRequestBuilder();
    }

    public static class ProductRequestBuilder {
        private String name;
        private String description;
        private BigDecimal price;
        private Integer stock;

        public ProductRequestBuilder name(String name) {
            this.name = name;
            return this;
        }

        public ProductRequestBuilder description(String description) {
            this.description = description;
            return this;
        }

        public ProductRequestBuilder price(BigDecimal price) {
            this.price = price;
            return this;
        }

        public ProductRequestBuilder stock(Integer stock) {
            this.stock = stock;
            return this;
        }

        public ProductRequest build() {
            return new ProductRequest(name, description, price, stock);
        }
    }
}

