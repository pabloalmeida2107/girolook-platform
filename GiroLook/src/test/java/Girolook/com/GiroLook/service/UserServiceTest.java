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
import Girolook.com.GiroLook.models.User;
import Girolook.com.GiroLook.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;


    private User buildUser(UUID id, boolean active) {
        User user = new User("João Silva", "joao@email.com", "85999999999", "Rua A, 123");
        user.setId(id);
        user.setPassword("hashed_password");
        user.setActive(active);
        return user;
    }


    @Nested
    @DisplayName("createUser")
    class CreateUser {

        private UserCreateRequestDTO validRequest;

        @BeforeEach
        void setUp() {
            validRequest = new UserCreateRequestDTO(
                    "João Silva", "joao@email.com", "senha123", "85999999999", "Rua A, 123"
            );
        }

        @Test
        @DisplayName("deve criar usuário com sucesso quando e-mail não existe")
        void shouldCreateUserSuccessfully() {
            UUID id = UUID.randomUUID();
            User savedUser = buildUser(id, true);

            when(userRepository.existsByEmail(validRequest.email())).thenReturn(false);
            when(passwordEncoder.encode(validRequest.password())).thenReturn("hashed_password");
            when(userRepository.save(any(User.class))).thenReturn(savedUser);

            UserResponseDTO response = userService.createUser(validRequest);

            assertThat(response).isNotNull();
            assertThat(response.name()).isEqualTo("João Silva");
            assertThat(response.email()).isEqualTo("joao@email.com");

            verify(userRepository).existsByEmail(validRequest.email());
            verify(passwordEncoder).encode(validRequest.password());
            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("deve lançar ResourceNotFoundException quando e-mail já cadastrado")
        void shouldThrowWhenEmailAlreadyExists() {
            when(userRepository.existsByEmail(validRequest.email())).thenReturn(true);

            assertThatThrownBy(() -> userService.createUser(validRequest))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("já está cadastrado");

            verify(userRepository, never()).save(any());
        }
    }



    @Nested
    @DisplayName("getUserById")
    class GetUserById {

        @Test
        @DisplayName("deve retornar usuário quando ID existe")
        void shouldReturnUserWhenFound() {
            UUID id = UUID.randomUUID();
            User user = buildUser(id, true);

            when(userRepository.findById(id)).thenReturn(Optional.of(user));

            UserResponseDTO response = userService.getUserById(id);

            assertThat(response).isNotNull();
            assertThat(response.email()).isEqualTo("joao@email.com");
            verify(userRepository).findById(id);
        }

        @Test
        @DisplayName("deve lançar ResourceNotFoundException quando ID não existe")
        void shouldThrowWhenUserNotFound() {
            UUID id = UUID.randomUUID();

            when(userRepository.findById(id)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.getUserById(id))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining(id.toString());
        }
    }



    @Nested
    @DisplayName("updateUser")
    class UpdateUser {

        private UUID userId;
        private User existingUser;
        private UserUpdateRequestDTO validRequest;

        @BeforeEach
        void setUp() {
            userId = UUID.randomUUID();
            existingUser = buildUser(userId, true);
            validRequest = new UserUpdateRequestDTO(
                    "João Atualizado", "novo@email.com", "85988888888", "Rua B, 456"
            );
        }

        @Test
        @DisplayName("deve atualizar usuário com sucesso")
        void shouldUpdateUserSuccessfully() {
            when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
            when(userRepository.existsByEmail(validRequest.email())).thenReturn(false);

            UserResponseDTO response = userService.updateUser(validRequest, userId, userId);

            assertThat(response.name()).isEqualTo("João Atualizado");
            assertThat(response.email()).isEqualTo("novo@email.com");
        }

        @Test
        @DisplayName("deve lançar UnauthorizedAccessException quando autenticado não é o dono")
        void shouldThrowWhenNotOwner() {
            UUID otherId = UUID.randomUUID();

            when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));

            assertThatThrownBy(() -> userService.updateUser(validRequest, userId, otherId))
                    .isInstanceOf(UnauthorizedAccessException.class)
                    .hasMessageContaining("permissão");
        }

        @Test
        @DisplayName("deve lançar UserAlreadyExistsException quando novo e-mail já está em uso")
        void shouldThrowWhenNewEmailTaken() {
            when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
            when(userRepository.existsByEmail(validRequest.email())).thenReturn(true);

            assertThatThrownBy(() -> userService.updateUser(validRequest, userId, userId))
                    .isInstanceOf(UserAlreadyExistsException.class)
                    .hasMessageContaining("e-mail");
        }

        @Test
        @DisplayName("não deve verificar e-mail duplicado quando e-mail não mudou")
        void shouldNotCheckEmailWhenUnchanged() {
            UserUpdateRequestDTO sameEmailRequest = new UserUpdateRequestDTO(
                    "João Atualizado", "joao@email.com", "85988888888", "Rua B, 456"
            );

            when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));

            userService.updateUser(sameEmailRequest, userId, userId);

            verify(userRepository, never()).existsByEmail(any());
        }

        @Test
        @DisplayName("deve lançar ResourceNotFoundException quando usuário não encontrado")
        void shouldThrowWhenUserNotFound() {
            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.updateUser(validRequest, userId, userId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }


    @Nested
    @DisplayName("updatePassword")
    class UpdatePassword {

        private UUID userId;
        private User user;

        @BeforeEach
        void setUp() {
            userId = UUID.randomUUID();
            user = buildUser(userId, true);
        }

        @Test
        @DisplayName("deve atualizar senha com sucesso quando senha atual correta")
        void shouldUpdatePasswordSuccessfully() {
            UserPasswordUpdateRequestDTO request =
                    new UserPasswordUpdateRequestDTO("senha_antiga", "nova_senha");

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("senha_antiga", "hashed_password")).thenReturn(true);
            when(passwordEncoder.encode("nova_senha")).thenReturn("nova_hash");

            userService.updatePassword(userId, request);

            verify(passwordEncoder).encode("nova_senha");
            assertThat(user.getPassword()).isEqualTo("nova_hash");
        }

        @Test
        @DisplayName("deve lançar BusinessException quando senha atual incorreta")
        void shouldThrowWhenOldPasswordWrong() {
            UserPasswordUpdateRequestDTO request =
                    new UserPasswordUpdateRequestDTO("senha_errada", "nova_senha");

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("senha_errada", "hashed_password")).thenReturn(false);

            assertThatThrownBy(() -> userService.updatePassword(userId, request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Senha atual incorreta");

            verify(passwordEncoder, never()).encode(any());
        }
    }


    @Nested
    @DisplayName("deactivateSelf")
    class DeactivateSelf {

        private UUID userId;
        private User activeUser;

        @BeforeEach
        void setUp() {
            userId = UUID.randomUUID();
            activeUser = buildUser(userId, true);
        }

        @Test
        @DisplayName("deve desativar conta com sucesso")
        void shouldDeactivateSuccessfully() {
            when(userRepository.findById(userId)).thenReturn(Optional.of(activeUser));

            userService.deactivateSelf(userId, userId);

            assertThat(activeUser.isActive()).isFalse();
        }

        @Test
        @DisplayName("deve lançar UnauthorizedAccessException quando tenta desativar outra conta")
        void shouldThrowWhenNotOwner() {
            UUID otherId = UUID.randomUUID();

            assertThatThrownBy(() -> userService.deactivateSelf(userId, otherId))
                    .isInstanceOf(UnauthorizedAccessException.class)
                    .hasMessageContaining("própria conta");

            verify(userRepository, never()).findById(any());
        }

        @Test
        @DisplayName("deve lançar ResourceNotFoundException quando usuário não existe")
        void shouldThrowWhenUserNotFound() {
            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.deactivateSelf(userId, userId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("deve lançar ResourceNotFoundException quando conta já desativada")
        void shouldThrowWhenAlreadyInactive() {
            User inactiveUser = buildUser(userId, false);
            when(userRepository.findById(userId)).thenReturn(Optional.of(inactiveUser));

            assertThatThrownBy(() -> userService.deactivateSelf(userId, userId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("já se encontra desativada");
        }
    }



    @Nested
    @DisplayName("login")
    class Login {

        private UserRegistrationRequestDTO validRequest;
        private UUID userId;
        private User activeUser;

        @BeforeEach
        void setUp() {
            userId = UUID.randomUUID();
            validRequest = new UserRegistrationRequestDTO("joao@email.com", "senha123");
            activeUser = buildUser(userId, true);
        }

        @Test
        @DisplayName("deve fazer login com sucesso com credenciais válidas")
        void shouldLoginSuccessfully() {
            // 1. Mock do repositório encontrando o usuário
            when(userRepository.findUserByEmail("joao@email.com")).thenReturn(Optional.of(activeUser));

            // 2. Mock do encoder validando a senha
            when(passwordEncoder.matches("senha123", "hashed_password")).thenReturn(true);

            // 🚨 AQUI ESTAVA O ERRO: O login retorna o TOKEN (String), não o DTO
            String token = userService.login(validRequest);

            // 3. Verificações (Asserções)
            assertThat(token).isNotNull();
            assertThat(token).isNotBlank(); // Verifica se o token não está vazio
        }

        @Test
        @DisplayName("deve lançar BadCredentialsException quando e-mail não encontrado")
        void shouldThrowWhenEmailNotFound() {
            when(userRepository.findUserByEmail("joao@email.com")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.login(validRequest))
                    .isInstanceOf(BadCredentialsException.class)
                    .hasMessageContaining("E-mail ou senha incorretos");
        }

        @Test
        @DisplayName("deve lançar BadCredentialsException quando senha incorreta")
        void shouldThrowWhenPasswordWrong() {
            when(userRepository.findUserByEmail("joao@email.com")).thenReturn(Optional.of(activeUser));
            when(passwordEncoder.matches("senha123", "hashed_password")).thenReturn(false);

            assertThatThrownBy(() -> userService.login(validRequest))
                    .isInstanceOf(BadCredentialsException.class)
                    .hasMessageContaining("E-mail ou senha incorretos");
        }

        @Test
        @DisplayName("deve lançar BusinessException quando conta está desativada")
        void shouldThrowWhenAccountInactive() {
            User inactiveUser = buildUser(userId, false);

            when(userRepository.findUserByEmail("joao@email.com")).thenReturn(Optional.of(inactiveUser));
            when(passwordEncoder.matches("senha123", "hashed_password")).thenReturn(true);

            assertThatThrownBy(() -> userService.login(validRequest))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("desativada");
        }
    }
}