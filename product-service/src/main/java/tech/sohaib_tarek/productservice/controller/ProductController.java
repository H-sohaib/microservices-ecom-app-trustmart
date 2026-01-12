package net.anassploit.productservice.controller;

import jakarta.validation.Valid;
import net.anassploit.productservice.dto.ProductRequest;
import net.anassploit.productservice.dto.ProductResponse;
import net.anassploit.productservice.dto.StockUpdateRequest;
import net.anassploit.productservice.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private static final Logger log = LoggerFactory.getLogger(ProductController.class);

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody ProductRequest request) {
        log.info("Received request to create product: {}", request.getName());
        ProductResponse response = productService.createProduct(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long productId) {
        log.info("Received request to get product with ID: {}", productId);
        ProductResponse response = productService.getProductById(productId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        log.info("Received request to get all products");
        List<ProductResponse> response = productService.getAllProducts();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{productId}")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable Long productId,
            @Valid @RequestBody ProductRequest request) {
        log.info("Received request to update product with ID: {}", productId);
        ProductResponse response = productService.updateProduct(productId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long productId) {
        log.info("Received request to delete product with ID: {}", productId);
        productService.deleteProduct(productId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{productId}/check-stock")
    public ResponseEntity<Boolean> checkStock(
            @PathVariable Long productId,
            @RequestParam Integer quantity) {
        log.info("Received request to check stock for product ID: {} with quantity: {}", productId, quantity);
        boolean available = productService.checkStock(productId, quantity);
        return ResponseEntity.ok(available);
    }

    @PostMapping("/reduce-stock")
    public ResponseEntity<Void> reduceStock(@Valid @RequestBody List<StockUpdateRequest> stockUpdates) {
        log.info("Received request to reduce stock for {} products", stockUpdates.size());
        productService.reduceStock(stockUpdates);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/restore-stock")
    public ResponseEntity<Void> restoreStock(@Valid @RequestBody List<StockUpdateRequest> stockUpdates) {
        log.info("Received request to restore stock for {} products", stockUpdates.size());
        productService.restoreStock(stockUpdates);
        return ResponseEntity.ok().build();
    }
}

