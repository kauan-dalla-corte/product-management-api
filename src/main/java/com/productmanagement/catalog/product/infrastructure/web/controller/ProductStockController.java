package com.productmanagement.catalog.product.infrastructure.web.controller;

import com.productmanagement.catalog.product.application.dto.request.StockMovementRequest;
import com.productmanagement.catalog.product.application.dto.response.StockMovementResponse;
import com.productmanagement.catalog.product.application.mapper.ProductStockMapper;
import com.productmanagement.catalog.product.application.service.ProductStockMovementService;
import com.productmanagement.catalog.product.domain.model.ProductStockMovement;
import com.productmanagement.catalog.product.domain.model.StockMovementType;
import com.productmanagement.catalog.product.infrastructure.web.response.ApiResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Stock", description = "Gerenciamento de estoque de produtos")
@Validated
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductStockController {

    private final ProductStockMovementService productStockMovementService;

    @Operation(summary = "Registra entrada de estoque de um Produto")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Entrada registrada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Produto não encontrado"),
            @ApiResponse(responseCode = "409", description = "Produto inativo")
    })
    @PatchMapping("/{productId}/stock/inbound")
    public ResponseEntity<ApiResult<StockMovementResponse>> stockInbound(
            @PathVariable @Min(1) Long productId,
            @RequestBody @Valid StockMovementRequest request) {
        ProductStockMovement productStockMovement = productStockMovementService.stockInbound(productId, request.quantity());

        return ResponseEntity.ok(ApiResult.ok(ProductStockMapper.toProductStockMovementResponse(productStockMovement)));
    }

    @Operation(summary = "Registra saída de estoque de um Produto")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Saida registrada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Produto não encontrado"),
            @ApiResponse(responseCode = "409", description = "Produto inativo ou estoque insuficiente")
    })
    @PatchMapping("/{productId}/stock/outbound")
    public ResponseEntity<ApiResult<StockMovementResponse>> stockOutbound(
            @PathVariable @Min(1) Long productId,
            @RequestBody @Valid StockMovementRequest request) {
        ProductStockMovement productStockMovement = productStockMovementService.stockOutbound(productId, request.quantity());

        return ResponseEntity.ok(ApiResult.ok(ProductStockMapper.toProductStockMovementResponse(productStockMovement)));
    }
    @Operation(summary = "Retorna histórico de movimentações de um Produto")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Histórico de movimentações retornado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Produto não encontrado")
    })
    @GetMapping("/{productId}/stock/movements")
    public ResponseEntity<ApiResult<List<StockMovementResponse>>> stockMovements(
            @PathVariable @Min(1) Long productId,
            @RequestParam(required = false) StockMovementType type) {

        List<ProductStockMovement> movements = productStockMovementService.stockMovements(productId, type);
        return ResponseEntity.ok(ApiResult.ok(ProductStockMapper.toProductStockMovementResponseList(movements)));
    }

}
