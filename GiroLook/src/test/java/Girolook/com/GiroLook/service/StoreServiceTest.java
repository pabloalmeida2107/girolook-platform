package Girolook.com.GiroLook.service;

import Girolook.com.GiroLook.dto.request.StoreRequestDTO;
import Girolook.com.GiroLook.dto.response.StoreResponseDTO;
import Girolook.com.GiroLook.dto.response.StoreSummaryResponseDTO;
import Girolook.com.GiroLook.exceptions.ResourceNotFoundException;
import Girolook.com.GiroLook.exceptions.UnauthorizedAccessException;
import Girolook.com.GiroLook.models.Product;
import Girolook.com.GiroLook.models.Store;
import Girolook.com.GiroLook.models.User;
import Girolook.com.GiroLook.repository.StoreRepository;
import Girolook.com.GiroLook.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StoreServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private StoreRepository storeRepository;

    @InjectMocks
    private StoreService storeService;

    private UUID userId;
    private UUID storeId;
    private User user;
    private Store store;

    @BeforeEach
    void setup() {
        userId = UUID.randomUUID();
        storeId = UUID.randomUUID();

        user = new User();
        user.setId(userId);

        store = new Store();
        store.setId(storeId);
        store.setOwner(user);
        store.setName("Loja Teste");
        store.setDescription("Descrição");
        store.setLogoUrl("logo.png");
        store.setProducts(new ArrayList<>());
    }

    // =========================
    // CREATE STORE
    // =========================

    @Test
    void createStore_success() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(storeRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        StoreRequestDTO request = new StoreRequestDTO(
                "Minha Loja",
                "Descrição",
                "logo.png"
        );

        StoreResponseDTO response = storeService.createStore(request, userId);

        assertNotNull(response);
        assertEquals("Minha Loja", response.name());
    }

    @Test
    void createStore_userNotFound() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        StoreRequestDTO request = new StoreRequestDTO(
                "Minha Loja",
                "Descrição",
                "logo.png"
        );

        assertThrows(ResourceNotFoundException.class,
                () -> storeService.createStore(request, userId));
    }

    // =========================
    // GET ALL STORES SUMMARY
    // =========================

    @Test
    void getAllStoresSummary_success() {
        when(storeRepository.findAll()).thenReturn(List.of(store));

        List<StoreSummaryResponseDTO> result = storeService.getAllStoresSummary();

        assertEquals(1, result.size());
        assertEquals(store.getName(), result.get(0).name());
    }

    // =========================
    // GET STORE DETAIL
    // =========================

    @Test
    void getStoreDetail_success() {
        when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));

        StoreResponseDTO response = storeService.getStoreDetail(storeId);

        assertNotNull(response);
        assertEquals(store.getName(), response.name());
    }

    @Test
    void getStoreDetail_notFound() {
        when(storeRepository.findById(storeId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> storeService.getStoreDetail(storeId));
    }

    // =========================
    // UPDATE STORE
    // =========================

    @Test
    void updateStore_success() {
        when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));
        when(storeRepository.save(any())).thenReturn(store);

        StoreRequestDTO request = new StoreRequestDTO(
                "Novo Nome",
                "Nova Descrição",
                "novo.png"
        );

        StoreResponseDTO response = storeService.updateStore(storeId, request, userId);

        assertEquals("Novo Nome", response.name());
        assertEquals("Nova Descrição", response.description());
    }

    @Test
    void updateStore_notFound() {
        when(storeRepository.findById(storeId)).thenReturn(Optional.empty());

        StoreRequestDTO request = new StoreRequestDTO(
                "Nome",
                "Desc",
                "logo.png"
        );

        assertThrows(ResourceNotFoundException.class,
                () -> storeService.updateStore(storeId, request, userId));
    }

    @Test
    void updateStore_unauthorized() {
        User otherUser = new User();
        otherUser.setId(UUID.randomUUID());
        store.setOwner(otherUser);

        when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));

        StoreRequestDTO request = new StoreRequestDTO(
                "Nome",
                "Desc",
                "logo.png"
        );

        assertThrows(UnauthorizedAccessException.class,
                () -> storeService.updateStore(storeId, request, userId));
    }

    // =========================
    // DELETE STORE
    // =========================

    @Test
    void deleteStore_success() {
        Product product = new Product();
        product.setActive(true);

        store.setProducts(List.of(product));

        when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));

        storeService.deleteStore(storeId, userId);

        verify(storeRepository).delete(store);
        assertFalse(product.isActive());
    }

    @Test
    void deleteStore_notFound() {
        when(storeRepository.findById(storeId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> storeService.deleteStore(storeId, userId));
    }

    @Test
    void deleteStore_unauthorized() {
        User otherUser = new User();
        otherUser.setId(UUID.randomUUID());
        store.setOwner(otherUser);

        when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));

        assertThrows(UnauthorizedAccessException.class,
                () -> storeService.deleteStore(storeId, userId));
    }
}