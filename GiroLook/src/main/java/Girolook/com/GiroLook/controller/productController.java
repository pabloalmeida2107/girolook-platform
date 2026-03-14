package Girolook.com.GiroLook.controller;

import Girolook.com.GiroLook.dto.request.ProductRequestDTO;
import Girolook.com.GiroLook.dto.response.ProductResponseDTO;
import Girolook.com.GiroLook.dto.response.ProductSummaryResponseDTO;
import Girolook.com.GiroLook.models.User;
import Girolook.com.GiroLook.service.ProductService;
import jakarta.persistence.Table;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/product")
public class productController {
    private final ProductService productService;

    public productController(ProductService productService) {
        this.productService = productService;
    }


    @GetMapping("/product-list")
    public ResponseEntity<List<ProductSummaryResponseDTO>> getListAllProducts() {

        List<ProductSummaryResponseDTO> products = productService.getAllProducts();

        return ResponseEntity.ok(products);
    }

    @PostMapping("/create")
    public ResponseEntity<ProductResponseDTO> createProduct(@Valid @RequestBody ProductRequestDTO request, @AuthenticationPrincipal User currentUser){
        ProductResponseDTO response = productService.createProduct(request,currentUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{productId}")
    public ResponseEntity<ProductResponseDTO> updateProduct(@Valid@RequestBody ProductRequestDTO request,@PathVariable UUID productId, @AuthenticationPrincipal User currentUser ){
        ProductResponseDTO response = productService.updateProduct(request,productId,currentUser.getId());
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping("/{productId}/deactivate")
    public ResponseEntity<Void> deactivate(
            @PathVariable UUID productId,
            @AuthenticationPrincipal User currentUser) {

        productService.deactivateProduct(productId, currentUser.getId());
        return ResponseEntity.noContent().build();
    }


}
