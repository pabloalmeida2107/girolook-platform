package Girolook.com.GiroLook.service;

import Girolook.com.GiroLook.dto.request.StoreRequestDTO;
import Girolook.com.GiroLook.dto.response.ProductResponseDTO;
import Girolook.com.GiroLook.dto.response.StoreResponseDTO;
import Girolook.com.GiroLook.dto.response.StoreSummaryResponseDTO;
import Girolook.com.GiroLook.exceptions.ResourceNotFoundException;
import Girolook.com.GiroLook.exceptions.UnauthorizedAccessException;
import Girolook.com.GiroLook.models.Product;
import Girolook.com.GiroLook.models.Store;
import Girolook.com.GiroLook.models.User;
import Girolook.com.GiroLook.repository.StoreRepository;
import Girolook.com.GiroLook.repository.UserRepository;
import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class StoreService {

    private final UserRepository userRepository;

    private final  StoreRepository storeRepository;

    public StoreService(UserRepository userRepository, StoreRepository storeRepository) {
        this.userRepository = userRepository;
        this.storeRepository = storeRepository;
    }

    @Transactional
    public StoreResponseDTO createStore(StoreRequestDTO request, UUID ownerId) {
        User user = userRepository.findById(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado com ID: " + ownerId));


        Store s = new Store(user, request.name(), request.description(), request.logoUrl());

        Store savedStore = storeRepository.save(s);

        return new StoreResponseDTO(
                savedStore.getId(),
                savedStore.getName(),
                savedStore.getDescription(),
                savedStore.getLogoUrl(),
                Collections.emptyList()
        );
    }

    public List<StoreSummaryResponseDTO> getAllStoresSummary(){

        return storeRepository.findAll().stream()
                .map(s -> new StoreSummaryResponseDTO(s.getId(),s.getName(),s.getLogoUrl()))
                .collect(Collectors.toList());

    }

    public StoreResponseDTO getStoreDetail(UUID id){
        Store store = storeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Loja não encontrada com ID: " + id));

        return toResponse(store);

    }

    @Transactional
    public StoreResponseDTO updateStore(UUID storeId, StoreRequestDTO request, UUID authenticatedUserId) {

        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Loja não encontrada"));


        if (!store.getOwner().getId().equals(authenticatedUserId)) {
            throw new UnauthorizedAccessException("Você não tem permissão para editar esta loja!");
        }


        store.setName(request.name());
        store.setDescription(request.description());
        store.setLogoUrl(request.logoUrl());

        storeRepository.save(store);

        return toResponse(store);

    }

    @Transactional
    public void deleteStore(UUID storeId, UUID authenticatedUserId){
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Loja não encontrada"));


        if (!store.getOwner().getId().equals(authenticatedUserId)) {
            throw new UnauthorizedAccessException("Você não tem permissão para excluir esta loja!");
        }

        storeRepository.delete(store);

        store.getProducts().forEach(p -> p.setActive(false));

    }

    private ProductResponseDTO toProductResponse(Product p) {
        return new ProductResponseDTO(
                p.getId(),
                p.getStore().getId(),
                p.getStore().getName(),
                p.getName(),
                p.getDescription(),
                p.getPrice(),
                p.getSize(),
                p.getCategory(),
                p.getColor(),
                p.getImageUrl(),
                p.getStatus(),
                p.getType()
        );
    }

    private StoreResponseDTO toResponse(Store store) {
        return new StoreResponseDTO(
                store.getId(),
                store.getName(),
                store.getDescription(),
                store.getLogoUrl(),

                store.getProducts() != null ?
                        store.getProducts().stream().map(this::toProductResponse).toList() :
                        Collections.emptyList()
        );
    }




}
