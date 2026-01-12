package tech.sohaib_tarek.commandservice.dto;

public class StockUpdateRequest {

    private Long productId;
    private Integer quantity;

    public StockUpdateRequest() {
    }

    public StockUpdateRequest(Long productId, Integer quantity) {
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

    public static StockUpdateRequestBuilder builder() {
        return new StockUpdateRequestBuilder();
    }

    public static class StockUpdateRequestBuilder {
        private Long productId;
        private Integer quantity;

        public StockUpdateRequestBuilder productId(Long productId) {
            this.productId = productId;
            return this;
        }

        public StockUpdateRequestBuilder quantity(Integer quantity) {
            this.quantity = quantity;
            return this;
        }

        public StockUpdateRequest build() {
            return new StockUpdateRequest(productId, quantity);
        }
    }
}

