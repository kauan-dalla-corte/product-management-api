package com.productmanagement.catalog.product.application.service;

import com.productmanagement.catalog.product.domain.exception.ProductNotFoundException;
import com.productmanagement.catalog.product.domain.model.Product;
import com.productmanagement.catalog.product.domain.model.ProductMonetaryChange;
import com.productmanagement.catalog.product.domain.model.ProductMonetaryField;
import com.productmanagement.catalog.product.domain.repository.ProductMonetaryChangeRepository;
import com.productmanagement.catalog.product.domain.repository.ProductRepository;
import com.productmanagement.shared.valueobject.Money;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductMonetaryChangeService {
    private final ProductRepository productRepository;

    private final ProductMonetaryChangeRepository productMonetaryChangeRepository;

    @Transactional
    public ProductMonetaryChange changePrice(Long productId, Money value) {
        Product product = productRepository.findById(productId).orElseThrow(ProductNotFoundException::new);

        ProductMonetaryChange monetaryChange = product.changePrice(value);

        productRepository.save(product);
        return productMonetaryChangeRepository.save(monetaryChange);

    }

    @Transactional
    public ProductMonetaryChange changeCost(Long productId, Money value) {
        Product product = productRepository.findById(productId).orElseThrow(ProductNotFoundException::new);

        ProductMonetaryChange monetaryChange = product.changeCost(value);

        productRepository.save(product);
        return productMonetaryChangeRepository.save(monetaryChange);
    }

    @Transactional(readOnly = true)
    public List<ProductMonetaryChange> costChanges(Long productId) {
        productRepository.findById(productId).orElseThrow(ProductNotFoundException::new);

        return productMonetaryChangeRepository.findByProductIdAndMonetaryField(productId,
                ProductMonetaryField.COST, Sort.by("createdAt").descending());
    }

    @Transactional(readOnly = true)
    public List<ProductMonetaryChange> priceChanges(Long productId) {
        productRepository.findById(productId).orElseThrow(ProductNotFoundException::new);

        return productMonetaryChangeRepository.findByProductIdAndMonetaryField(productId,
                ProductMonetaryField.PRICE, Sort.by("createdAt").descending());
    }

}
