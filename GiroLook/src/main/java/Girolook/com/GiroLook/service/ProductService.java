package Girolook.com.GiroLook.service;

import Girolook.com.GiroLook.dto.request.ProductRequestDTO;
import Girolook.com.GiroLook.dto.response.ProductResponseDTO;
import Girolook.com.GiroLook.dto.response.ProductSummaryResponseDTO;
import Girolook.com.GiroLook.exceptions.ResourceNotFoundException;
import Girolook.com.GiroLook.exceptions.UnauthorizedAccessException;
import Girolook.com.GiroLook.models.Product;
import Girolook.com.GiroLook.models.Store;

import Girolook.com.GiroLook.repository.ProductRepository;
import Girolook.com.GiroLook.repository.StoreRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;


import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final StoreRepository storeRepository;

    public ProductService(ProductRepository productRepository, StoreRepository storeRepository) {
        this.productRepository = productRepository;
        this.storeRepository = storeRepository;
    }


    public List<ProductSummaryResponseDTO> getAllProducts() {

        return productRepository.findAllActiveWithStore().stream()
                .map(p -> new ProductSummaryResponseDTO(
                        p.getId(),
                        p.getName(),
                        p.getPrice(),
                        p.getImageUrl(),
                        p.getType(),
                        p.getStore().getName()
                ))
                .collect(Collectors.toList());
    }

    @Transactional
    public ProductResponseDTO createProduct(ProductRequestDTO request, UUID authenticatedUserId){
        Store store = storeRepository.findById(request.storeId())
                .orElseThrow(() -> new ResourceNotFoundException("Loja não encontrada"));

        if (!store.getOwner().getId().equals(authenticatedUserId)) {
            throw new UnauthorizedAccessException("Você não tem permissão para adicionar produto nessa loja!");
        }

        Product product = new Product(
                store,
                request.name(),
                request.description(),
                request.price(),
                request.size(),
                request.category(),
                request.color(),
                request.imageUrl(),
                request.type());

        Product savedProduct = productRepository.save(product);
        return convertToResponseDTO(savedProduct);

    }

    @Transactional
    public ProductResponseDTO updateProduct (ProductRequestDTO request ,UUID productId, UUID authenticatedUserId){
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado"));

        if (!product.getStore().getOwner().getId().equals(authenticatedUserId)) {
            throw new UnauthorizedAccessException("Você não tem permissão para editar esse produto nessa loja!");
        }

        product.setCategory(request.category());
        product.setName(request.name());
        product.setDescription(request.description());
        product.setPrice(request.price());
        product.setSize(request.size());
        product.setColor(request.color());
        product.setImageUrl(request.imageUrl());
        product.setType(request.type());

        return convertToResponseDTO(product);

    }

    @Transactional
    public void deactivateProduct(UUID productId, UUID authenticatedUserId) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado"));


        if (!product.getStore().getOwner().getId().equals(authenticatedUserId)) {
            throw new UnauthorizedAccessException("Você não tem permissão para editar esse produto nessa loja!");
        }


        if (!product.isActive()) {
            throw new ResourceNotFoundException("Este produto já se encontra desativada.");
        }


        product.setActive(false);

    }




    private ProductResponseDTO convertToResponseDTO(Product product) {
        return new ProductResponseDTO(
                product.getId(),
                product.getStore().getId(),
                product.getStore().getName(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getSize(),
                product.getCategory(),
                product.getColor(),
                product.getImageUrl(),
                product.getStatus(),
                product.getType()
        );
    }
}

