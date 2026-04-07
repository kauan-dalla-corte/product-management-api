package com.productmanagement.catalog.product.infrastructure.web.controller;

import com.productmanagement.catalog.product.application.dto.request.ChangeProductCostRequest;
import com.productmanagement.catalog.product.application.dto.request.ChangeProductPriceRequest;
import com.productmanagement.catalog.product.application.dto.response.MonetaryChangeResponse;
import com.productmanagement.catalog.product.application.mapper.ProductMapper;
import com.productmanagement.catalog.product.application.mapper.ProductMonetaryMapper;
import com.productmanagement.catalog.product.application.service.ProductMonetaryChangeService;
import com.productmanagement.catalog.product.domain.model.ProductMonetaryChange;
import com.productmanagement.catalog.product.infrastructure.web.response.ApiResult;
import com.productmanagement.shared.valueobject.Money;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
@Tag(name = "Monetary", description = "Gerenciamento de Preços e Custos de Produtos")
@Validated
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductMonetaryController {

    private final ProductMonetaryChangeService productMonetaryChangeService;
    @Operation(summary = "Altera o preço de um produto")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Preço alterado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Produto não encontrado"),
            @ApiResponse(responseCode = "409", description = "Preço igual ao atual ou abaixo do custo")
    })
    @PatchMapping("/{productId}/monetary/price")
    public ResponseEntity<ApiResult<MonetaryChangeResponse>> changePrice(
            @PathVariable @Min(1) Long productId,
            @RequestBody @Valid ChangeProductPriceRequest request) {

        Money newPrice = ProductMapper.toMoney(request.price());
        ProductMonetaryChange monetaryChange = productMonetaryChangeService.changePrice(productId, newPrice);

        return ResponseEntity.ok(ApiResult.ok(ProductMonetaryMapper.toProductMonetaryChangeResponse(monetaryChange)));
    }
    @Operation(summary = "Altera o custo de um produto")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Custo alterado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Produto não encontrado"),
            @ApiResponse(responseCode = "409", description = "Custo igual ao atual ou acima do preço")
    })
    @PatchMapping("/{productId}/monetary/cost")
    public ResponseEntity<ApiResult<MonetaryChangeResponse>> changeCost(
            @PathVariable @Min(1) Long productId,
            @RequestBody @Valid ChangeProductCostRequest request) {

        Money newCost = ProductMapper.toMoney(request.cost());
        ProductMonetaryChange monetaryChange = productMonetaryChangeService.changeCost(productId, newCost);

        return ResponseEntity.ok(ApiResult.ok(ProductMonetaryMapper.toProductMonetaryChangeResponse(monetaryChange)));
    }
    @Operation(summary = "Lista o histórico de custos")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Histórico de custo retornado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Produto não encontrado")
    })
    @GetMapping("/{productId}/monetary/cost/history")
    public ResponseEntity<ApiResult<List<MonetaryChangeResponse>>> costHistory(@PathVariable @Min(1) Long productId) {
        List<ProductMonetaryChange> changes = productMonetaryChangeService.costChanges(productId);

        return ResponseEntity.ok(ApiResult.ok(ProductMonetaryMapper.toProductMonetaryChangeResponseList(changes)));
    }

    @Operation(summary = "Lista o histórico de preço")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Histórico de preço retornado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Produto não encontrado")
    })
    @GetMapping("/{productId}/monetary/price/history")
    public ResponseEntity<ApiResult<List<MonetaryChangeResponse>>> priceHistory(@PathVariable @Min(1) Long productId) {
        List<ProductMonetaryChange> changes = productMonetaryChangeService.priceChanges(productId);

        return ResponseEntity.ok(ApiResult.ok(ProductMonetaryMapper.toProductMonetaryChangeResponseList(changes)));
    }
}
