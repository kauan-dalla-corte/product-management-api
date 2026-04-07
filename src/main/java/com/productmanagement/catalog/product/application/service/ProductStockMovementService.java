package com.productmanagement.catalog.product.application.service;

import com.productmanagement.catalog.product.domain.exception.ProductNotFoundException;
import com.productmanagement.catalog.product.domain.model.Product;
import com.productmanagement.catalog.product.domain.model.ProductStockMovement;
import com.productmanagement.catalog.product.domain.model.StockMovementType;
import com.productmanagement.catalog.product.domain.repository.ProductRepository;
import com.productmanagement.catalog.product.domain.repository.ProductStockMovementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductStockMovementService {

    private final ProductRepository productRepository;

    private final ProductStockMovementRepository productStockMovementRepository;

    @Transactional
    public ProductStockMovement stockInbound(Long productId, int amount) {
        Product product = productRepository.findById(productId).orElseThrow(ProductNotFoundException::new);

        ProductStockMovement productStockMovement = product.inbound(amount);
        productRepository.save(product);
        return productStockMovementRepository.save(productStockMovement);
    }

    @Transactional
    public ProductStockMovement stockOutbound(Long productId, int amount) {
        Product product = productRepository.findById(productId).orElseThrow(ProductNotFoundException::new);

        ProductStockMovement productStockMovement = product.outbound(amount);
        productRepository.save(product);
        return productStockMovementRepository.save(productStockMovement);
    }

    @Transactional(readOnly = true)
    public List<ProductStockMovement> stockMovements(Long productId, StockMovementType type) {
        productRepository.findById(productId).orElseThrow(ProductNotFoundException::new);
        return productStockMovementRepository.findAllByProductIdAndOptionalType(productId, type);
    }

}
