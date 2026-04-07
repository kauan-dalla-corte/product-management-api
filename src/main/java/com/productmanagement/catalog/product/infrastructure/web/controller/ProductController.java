package com.productmanagement.catalog.product.infrastructure.web.controller;

import com.productmanagement.catalog.product.application.dto.request.CreateProductRequest;
import com.productmanagement.catalog.product.application.dto.response.ProductResponse;
import com.productmanagement.catalog.product.application.mapper.ProductMapper;
import com.productmanagement.catalog.product.application.service.ProductService;
import com.productmanagement.catalog.product.domain.model.Product;
import com.productmanagement.catalog.product.infrastructure.web.response.ApiResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
@Tag(name = "Produtos", description = "Gerenciamento de produtos")
@Validated
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;

    @Operation(summary = "Busca produto por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Produto encontrado"),
            @ApiResponse(responseCode = "404", description = "Produto não encontrado")
    })
    @GetMapping("/{productId}")
    public ResponseEntity<ApiResult<ProductResponse>> getProductById(@PathVariable @Min(1) Long productId) {
        Product product = productService.getProductById(productId);

        return ResponseEntity.ok(ApiResult.ok(ProductMapper.toProductResponse(product)));
    }

    @Operation(summary = "Lista todos os produtos cadastrados")
    @GetMapping
    public ResponseEntity<ApiResult<List<ProductResponse>>> getAllProducts() {
        List<Product> productList = productService.findAll();

        List<ProductResponse> productResponseList = ProductMapper.toProductResponseList(productList);
        return ResponseEntity.ok(ApiResult.ok(productResponseList));
    }

    @Operation(summary = "Cria um novo produto")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Produto criado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "409", description = "Produto já existe")
    })
    @PostMapping
    public ResponseEntity<ApiResult<ProductResponse>> create(@RequestBody @Valid CreateProductRequest request) {
        Product productCreated = productService.create(request);

        ProductResponse productResponse = ProductMapper.toProductResponse(productCreated);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResult.ok(productResponse));
    }

    @Operation(summary = "Ativa um produto")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Produto ativado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Produto não encontrado"),
            @ApiResponse(responseCode = "409", description = "Produto já está ativo")
    })
    @PatchMapping("/{productId}/activate")
    public ResponseEntity<ApiResult<Void>> activate(@PathVariable @Min(1) Long productId) {
        productService.activate(productId);
        return ResponseEntity.ok(ApiResult.ok());
    }

    @Operation(summary = "Desativa um produto")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Produto desativado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Produto não encontrado"),
            @ApiResponse(responseCode = "409", description = "Produto já está inativo ou possui estoque")
    })
    @PatchMapping("/{productId}/deactivate")
    public ResponseEntity<ApiResult<Void>> deactivate(@PathVariable @Min(1) Long productId) {
        productService.deactivate(productId);
        return ResponseEntity.ok(ApiResult.ok());
    }

}
