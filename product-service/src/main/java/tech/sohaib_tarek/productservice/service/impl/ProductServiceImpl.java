package net.anassploit.productservice.service.impl;

import net.anassploit.productservice.dto.ProductRequest;
import net.anassploit.productservice.dto.ProductResponse;
import net.anassploit.productservice.dto.StockUpdateRequest;
import net.anassploit.productservice.entity.Product;
import net.anassploit.productservice.exception.InsufficientStockException;
import net.anassploit.productservice.exception.ProductNotFoundException;
import net.anassploit.productservice.repository.ProductRepository;
import net.anassploit.productservice.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProductServiceImpl implements ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductServiceImpl.class);

    private final ProductRepository productRepository;

    public ProductServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public ProductResponse createProduct(ProductRequest request) {
        log.info("Creating new product: {}", request.getName());
        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .stock(request.getStock())
                .build();

        Product savedProduct = productRepository.save(product);
        log.info("Product created with ID: {}", savedProduct.getProductId());
        return mapToResponse(savedProduct);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long productId) {
        log.info("Fetching product with ID: {}", productId);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with ID: " + productId));
        return mapToResponse(product);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getAllProducts() {
        log.info("Fetching all products");
        return productRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ProductResponse updateProduct(Long productId, ProductRequest request) {
        log.info("Updating product with ID: {}", productId);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with ID: " + productId));

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());

        Product updatedProduct = productRepository.save(product);
        log.info("Product updated successfully: {}", productId);
        return mapToResponse(updatedProduct);
    }

    @Override
    public void deleteProduct(Long productId) {
        log.info("Deleting product with ID: {}", productId);
        if (!productRepository.existsById(productId)) {
            throw new ProductNotFoundException("Product not found with ID: " + productId);
        }
        productRepository.deleteById(productId);
        log.info("Product deleted successfully: {}", productId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean checkStock(Long productId, Integer quantity) {
        log.info("Checking stock for product ID: {} with quantity: {}", productId, quantity);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with ID: " + productId));
        return product.getStock() >= quantity;
    }

    @Override
    public void reduceStock(List<StockUpdateRequest> stockUpdates) {
        log.info("Reducing stock for {} products", stockUpdates.size());
        for (StockUpdateRequest request : stockUpdates) {
            Product product = productRepository.findById(request.getProductId())
                    .orElseThrow(() -> new ProductNotFoundException("Product not found with ID: " + request.getProductId()));

            if (product.getStock() < request.getQuantity()) {
                throw new InsufficientStockException("Insufficient stock for product: " + product.getName());
            }

            product.setStock(product.getStock() - request.getQuantity());
            productRepository.save(product);
        }
        log.info("Stock reduced successfully");
    }

    @Override
    public void restoreStock(List<StockUpdateRequest> stockUpdates) {
        log.info("Restoring stock for {} products", stockUpdates.size());
        for (StockUpdateRequest request : stockUpdates) {
            Product product = productRepository.findById(request.getProductId())
                    .orElseThrow(() -> new ProductNotFoundException("Product not found with ID: " + request.getProductId()));

            product.setStock(product.getStock() + request.getQuantity());
            productRepository.save(product);
        }
        log.info("Stock restored successfully");
    }

    private ProductResponse mapToResponse(Product product) {
        return ProductResponse.builder()
                .productId(product.getProductId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .stock(product.getStock())
                .build();
    }
}

