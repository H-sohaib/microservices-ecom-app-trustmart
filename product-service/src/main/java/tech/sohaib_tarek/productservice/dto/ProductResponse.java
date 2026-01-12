package net.anassploit.productservice.dto;

import java.math.BigDecimal;

public class ProductResponse {

    private Long productId;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stock;

    public ProductResponse() {
    }

    public ProductResponse(Long productId, String name, String description, BigDecimal price, Integer stock) {
        this.productId = productId;
        this.name = name;
        this.description = description;
        this.price = price;
        this.stock = stock;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
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

    public static ProductResponseBuilder builder() {
        return new ProductResponseBuilder();
    }

    public static class ProductResponseBuilder {
        private Long productId;
        private String name;
        private String description;
        private BigDecimal price;
        private Integer stock;

        public ProductResponseBuilder productId(Long productId) {
            this.productId = productId;
            return this;
        }

        public ProductResponseBuilder name(String name) {
            this.name = name;
            return this;
        }

        public ProductResponseBuilder description(String description) {
            this.description = description;
            return this;
        }

        public ProductResponseBuilder price(BigDecimal price) {
            this.price = price;
            return this;
        }

        public ProductResponseBuilder stock(Integer stock) {
            this.stock = stock;
            return this;
        }

        public ProductResponse build() {
            return new ProductResponse(productId, name, description, price, stock);
        }
    }
}

