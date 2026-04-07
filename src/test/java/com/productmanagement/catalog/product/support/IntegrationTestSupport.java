package com.productmanagement.catalog.product.support;

import com.productmanagement.catalog.product.domain.repository.ProductMonetaryChangeRepository;
import com.productmanagement.catalog.product.domain.repository.ProductRepository;
import com.productmanagement.catalog.product.domain.repository.ProductStockMovementRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class IntegrationTestSupport extends ControllerTestSupport {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ProductRepository productRepository;

    @Autowired
    protected ProductMonetaryChangeRepository productMonetaryChangeRepository;

    @Autowired
    protected ProductStockMovementRepository productStockMovementRepository;

    @BeforeEach
    void cleanUp() {
        productMonetaryChangeRepository.deleteAll();
        productStockMovementRepository.deleteAll();
        productRepository.deleteAll();
    }


}
