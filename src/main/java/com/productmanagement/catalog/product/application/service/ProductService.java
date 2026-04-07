package com.productmanagement.catalog.product.application.service;

import com.productmanagement.catalog.product.application.dto.request.CreateProductRequest;
import com.productmanagement.catalog.product.application.mapper.ProductMapper;
import com.productmanagement.catalog.product.domain.exception.ProductAlreadyExistsException;
import com.productmanagement.catalog.product.domain.exception.ProductNotFoundException;
import com.productmanagement.catalog.product.domain.model.Product;
import com.productmanagement.catalog.product.domain.model.ProductName;
import com.productmanagement.catalog.product.domain.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public Product getProductById(Long id) {
        return productRepository.findById(id).orElseThrow(ProductNotFoundException::new);
    }

    @Transactional(readOnly = true)
    public List<Product> findAll() {
        return productRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));
    }

    @Transactional
    public Product create(CreateProductRequest request) {
        if (productRepository.existsByName(ProductName.of(request.name()))) {
            throw new ProductAlreadyExistsException();
        }

        Product product = ProductMapper.toDomain(request);
        try {
            return productRepository.saveAndFlush(product);
        } catch (DataIntegrityViolationException ex) {
            throw new ProductAlreadyExistsException();
        }

    }

    @Transactional
    public void activate(Long productId) {
        Product product = productRepository.findById(productId).orElseThrow(ProductNotFoundException::new);
        product.activate();
        productRepository.save(product);
    }

    @Transactional
    public void deactivate(Long productId) {
        Product product = productRepository.findById(productId).orElseThrow(ProductNotFoundException::new);
        product.deactivate();
        productRepository.save(product);
    }


}
