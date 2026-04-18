package Girolook.com.GiroLook.controller;


import Girolook.com.GiroLook.dto.request.UserCreateRequestDTO;
import Girolook.com.GiroLook.dto.request.UserRegistrationRequestDTO;
import Girolook.com.GiroLook.dto.request.UserUpdateRequestDTO;
import Girolook.com.GiroLook.dto.response.LoginResponseDTO;
import Girolook.com.GiroLook.dto.response.UserResponseDTO;
import Girolook.com.GiroLook.models.User;
import Girolook.com.GiroLook.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


import java.util.UUID;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/create")
    public ResponseEntity<UserResponseDTO> createUser(@Valid @RequestBody UserCreateRequestDTO request){
        UserResponseDTO response = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable UUID userId){
        UserResponseDTO response = userService.getUserById(userId);
        return ResponseEntity.ok(response);

    }

    @PutMapping("/{userId}")
    public ResponseEntity<UserResponseDTO> updateUser( @Valid @RequestBody UserUpdateRequestDTO request
            , @PathVariable UUID userId
            ,@AuthenticationPrincipal User currentUser){

        UserResponseDTO response = userService.updateUser(request, userId,currentUser.getId());
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deactivate(
            @PathVariable UUID userId,
            @AuthenticationPrincipal User currentUser
    ) {
        userService.deactivateSelf(userId, currentUser.getId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> loginUser(
            @Valid @RequestBody UserRegistrationRequestDTO request) {

        String token = userService.login(request);

        return ResponseEntity.ok(new LoginResponseDTO(token));
    }



}
