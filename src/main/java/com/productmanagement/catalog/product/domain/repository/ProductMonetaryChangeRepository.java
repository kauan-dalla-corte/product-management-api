package com.productmanagement.catalog.product.domain.repository;

import com.productmanagement.catalog.product.domain.model.ProductMonetaryChange;
import com.productmanagement.catalog.product.domain.model.ProductMonetaryField;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductMonetaryChangeRepository extends JpaRepository<ProductMonetaryChange, Long> {
    List<ProductMonetaryChange> findByProductIdAndMonetaryField(Long productId, ProductMonetaryField monetaryField, Sort sort);
}
