package Girolook.com.GiroLook.controller;

import Girolook.com.GiroLook.dto.request.StoreRequestDTO;
import Girolook.com.GiroLook.dto.response.StoreResponseDTO;
import Girolook.com.GiroLook.models.User;
import Girolook.com.GiroLook.service.StoreService;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/stores")
public class StoreController {

    private final StoreService storeService;


    public StoreController(StoreService storeService) {
        this.storeService = storeService;
    }

    @PostMapping("/create")
    public ResponseEntity<StoreResponseDTO> create(@AuthenticationPrincipal User currentUser, @Valid @RequestBody StoreRequestDTO request) {
        StoreResponseDTO response = storeService.createStore(request, currentUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<StoreResponseDTO> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(storeService.getStoreDetail(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<StoreResponseDTO> update(
            @PathVariable UUID id,
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody StoreRequestDTO request) {

        return ResponseEntity.ok(storeService.updateStore(id, request, currentUser.getId()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID id,
            @AuthenticationPrincipal User currentUser) {

        storeService.deleteStore(id, currentUser.getId());
        return ResponseEntity.noContent().build();
    }
}
