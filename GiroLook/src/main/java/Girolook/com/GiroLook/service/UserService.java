package Girolook.com.GiroLook.service;

import Girolook.com.GiroLook.dto.request.UserCreateRequestDTO;
import Girolook.com.GiroLook.dto.request.UserPasswordUpdateRequestDTO;
import Girolook.com.GiroLook.dto.request.UserRegistrationRequestDTO;
import Girolook.com.GiroLook.dto.request.UserUpdateRequestDTO;
import Girolook.com.GiroLook.dto.response.UserResponseDTO;
import Girolook.com.GiroLook.exceptions.BusinessException;
import Girolook.com.GiroLook.exceptions.ResourceNotFoundException;
import Girolook.com.GiroLook.exceptions.UnauthorizedAccessException;
import Girolook.com.GiroLook.exceptions.UserAlreadyExistsException;
import Girolook.com.GiroLook.infra.TokenService;
import Girolook.com.GiroLook.models.User;
import Girolook.com.GiroLook.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private TokenService tokenService;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserResponseDTO createUser(UserCreateRequestDTO request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new ResourceNotFoundException("Este e-mail já está cadastrado no GiroLook.");
        }


        User newUser = new User(request.name(),request.email(),request.phoneNumber(),request.address());

        String hashPassword = passwordEncoder.encode(request.password());
        newUser.setPassword(hashPassword);


        User savedUser = userRepository.save(newUser);

        return convertToResponseDTO(savedUser);
    }

    public UserResponseDTO getUserById(UUID id){
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User não encontrado com ID: " + id));

        return convertToResponseDTO(user);
    }

    @Transactional
    public UserResponseDTO updateUser(UserUpdateRequestDTO request, UUID id, UUID authenticatedUserId){
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario não encontrado"));

        if (!user.getId().equals(authenticatedUserId)) {
            throw new UnauthorizedAccessException("Você não tem permissão para editar este usuario!");
        }

        if (!user.getEmail().equals(request.email()) && userRepository.existsByEmail(request.email())) {
            throw new UserAlreadyExistsException("Este novo e-mail já está sendo usado por outra conta.");
        }

        user.setName(request.name());
        user.setAddress(request.address());
        user.setEmail(request.email());
        user.setPhoneNumber(request.phoneNumber());


        return convertToResponseDTO(user);
    }

    @Transactional
    public void updatePassword(UUID userId, UserPasswordUpdateRequestDTO data) {
        User user = userRepository.findById(userId).orElseThrow();


        if (!passwordEncoder.matches(data.oldPassword(), user.getPassword())) {
            throw new BusinessException("Senha atual incorreta.");
        }

        user.setPassword(passwordEncoder.encode(data.newPassword()));
    }

    @Transactional
    public void deactivateSelf(UUID userId, UUID authenticatedUserId) {

        if (!userId.equals(authenticatedUserId)) {
            throw new UnauthorizedAccessException("Você só pode desativar a sua própria conta.");
        }


        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado com o ID: " + userId));


        if (!user.isActive()) {
            throw new ResourceNotFoundException("Esta conta já se encontra desativada.");
        }


        user.setActive(false);

    }

    public String login(UserRegistrationRequestDTO request) {

        var usernamePassword = new UsernamePasswordAuthenticationToken(
                request.email(),
                request.password()
        );

        var auth = authenticationManager.authenticate(usernamePassword);

        User user = (User) auth.getPrincipal();

        return tokenService.generateToken(user);
    }

    private UserResponseDTO convertToResponseDTO(User user) {
        return new UserResponseDTO(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getAddress()
        );
    }
}

