package net.anassploit.productservice.service;

import net.anassploit.productservice.dto.ProductRequest;
import net.anassploit.productservice.dto.ProductResponse;
import net.anassploit.productservice.dto.StockUpdateRequest;

import java.util.List;

public interface ProductService {

    ProductResponse createProduct(ProductRequest request);

    ProductResponse getProductById(Long productId);

    List<ProductResponse> getAllProducts();

    ProductResponse updateProduct(Long productId, ProductRequest request);

    void deleteProduct(Long productId);

    boolean checkStock(Long productId, Integer quantity);

    void reduceStock(List<StockUpdateRequest> stockUpdates);

    void restoreStock(List<StockUpdateRequest> stockUpdates);
}

