package Girolook.com.GiroLook.service;

import Girolook.com.GiroLook.dto.request.ProductRequestDTO;
import Girolook.com.GiroLook.dto.response.ProductResponseDTO;
import Girolook.com.GiroLook.dto.response.ProductSummaryResponseDTO;
import Girolook.com.GiroLook.exceptions.ResourceNotFoundException;
import Girolook.com.GiroLook.exceptions.UnauthorizedAccessException;
import Girolook.com.GiroLook.models.Product;
import Girolook.com.GiroLook.models.Store;
import Girolook.com.GiroLook.models.User;
import Girolook.com.GiroLook.models.enums.Category;
import Girolook.com.GiroLook.models.enums.ProductType;
import Girolook.com.GiroLook.repository.ProductRepository;
import Girolook.com.GiroLook.repository.StoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductService - Testes Unitários")
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private StoreRepository storeRepository;

    @InjectMocks
    private ProductService productService;



    private UUID ownerId;
    private UUID otherUserId;
    private UUID storeId;
    private UUID productId;

    private User owner;
    private Store store;
    private Product product;
    private ProductRequestDTO validRequest;

    @BeforeEach
    void setUp() {
        ownerId     = UUID.randomUUID();
        otherUserId = UUID.randomUUID();
        storeId     = UUID.randomUUID();
        productId   = UUID.randomUUID();

        owner = new User();
        owner.setId(ownerId);

        store = new Store();
        store.setId(storeId);
        store.setName("Loja Teste");
        store.setOwner(owner);

        product = new Product();
        product.setId(productId);
        product.setStore(store);
        product.setName("Camiseta Azul");
        product.setDescription("Camiseta de algodão");
        product.setPrice(new BigDecimal("49.90"));
        product.setSize("M");
        product.setCategory(Category.DRESS);
        product.setColor("Azul");
        product.setImageUrl("https://img.example.com/camisa.jpg");
        product.setType(ProductType.FOR_SALE);
        product.setActive(true);

        validRequest = new ProductRequestDTO(
                "Camiseta Azul",
                "Camiseta de algodão",
                new BigDecimal("49.90"),
                "M",
                Category.DRESS,
                "Azul",
                "https://img.example.com/camisa.jpg",
                ProductType.FOR_SALE,
                storeId
        );
    }


    @Nested
    @DisplayName("getAllProducts()")
    class GetAllProducts {

        @Test
        @DisplayName("Deve retornar lista de produtos ativos com dados da loja")
        void shouldReturnListOfActiveProducts() {
            when(productRepository.findAllActiveWithStore()).thenReturn(List.of(product));

            List<ProductSummaryResponseDTO> result = productService.getAllProducts();

            assertThat(result).hasSize(1);
            ProductSummaryResponseDTO dto = result.get(0);
            assertThat(dto.id()).isEqualTo(productId);
            assertThat(dto.name()).isEqualTo("Camiseta Azul");
            assertThat(dto.price()).isEqualByComparingTo(new BigDecimal("49.90"));
            assertThat(dto.imageUrl()).isEqualTo("https://img.example.com/camisa.jpg");
            // Usa o enum real em vez de String hardcoded
            assertThat(dto.type()).isEqualTo(ProductType.FOR_SALE);
            assertThat(dto.storeName()).isEqualTo("Loja Teste");

            verify(productRepository, times(1)).findAllActiveWithStore();
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando não houver produtos ativos")
        void shouldReturnEmptyListWhenNoActiveProducts() {
            when(productRepository.findAllActiveWithStore()).thenReturn(List.of());

            List<ProductSummaryResponseDTO> result = productService.getAllProducts();

            assertThat(result).isEmpty();
            verify(productRepository, times(1)).findAllActiveWithStore();
        }

        @Test
        @DisplayName("Deve retornar todos os produtos ativos quando há múltiplos")
        void shouldReturnAllActiveProductsWhenMultiple() {
            Product product2 = new Product();
            product2.setId(UUID.randomUUID());
            product2.setStore(store);
            product2.setName("Calça Preta");
            product2.setPrice(new BigDecimal("99.90"));
            product2.setImageUrl("https://img.example.com/calca.jpg");
            product2.setType(ProductType.FOR_SALE);
            product2.setActive(true);

            when(productRepository.findAllActiveWithStore()).thenReturn(List.of(product, product2));

            List<ProductSummaryResponseDTO> result = productService.getAllProducts();

            assertThat(result).hasSize(2);
            assertThat(result)
                    .extracting(ProductSummaryResponseDTO::name)
                    .containsExactly("Camiseta Azul", "Calça Preta");
        }
    }



    @Nested
    @DisplayName("createProduct()")
    class CreateProduct {

        @Test
        @DisplayName("Deve criar produto com sucesso quando o usuário é dono da loja")
        void shouldCreateProductSuccessfully() {
            when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));
            when(productRepository.save(any(Product.class))).thenReturn(product);

            ProductResponseDTO result = productService.createProduct(validRequest, ownerId);

            assertThat(result).isNotNull();
            assertThat(result.name()).isEqualTo("Camiseta Azul");
            assertThat(result.storeId()).isEqualTo(storeId);
            assertThat(result.storeName()).isEqualTo("Loja Teste");

            verify(storeRepository, times(1)).findById(storeId);
            verify(productRepository, times(1)).save(any(Product.class));
        }

        @Test
        @DisplayName("Deve lançar ResourceNotFoundException quando a loja não existir")
        void shouldThrowResourceNotFoundWhenStoreNotFound() {
            when(storeRepository.findById(storeId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> productService.createProduct(validRequest, ownerId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Loja não encontrada");

            verify(productRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar UnauthorizedAccessException quando usuário não é dono da loja")
        void shouldThrowUnauthorizedWhenUserIsNotStoreOwner() {
            when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));

            assertThatThrownBy(() -> productService.createProduct(validRequest, otherUserId))
                    .isInstanceOf(UnauthorizedAccessException.class)
                    .hasMessage("Você não tem permissão para adicionar produto nessa loja!");

            verify(productRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve persistir produto com todos os campos corretos")
        void shouldPersistProductWithCorrectFields() {
            when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));

            when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
                Product saved = invocation.getArgument(0);
                saved.setId(productId);
                return saved;
            });

            ProductResponseDTO result = productService.createProduct(validRequest, ownerId);

            assertThat(result.description()).isEqualTo("Camiseta de algodão");
            assertThat(result.price()).isEqualByComparingTo(new BigDecimal("49.90"));
            assertThat(result.size()).isEqualTo("M");

            assertThat(result.category()).isEqualTo(Category.DRESS);
            assertThat(result.color()).isEqualTo("Azul");
            assertThat(result.imageUrl()).isEqualTo("https://img.example.com/camisa.jpg");
            assertThat(result.type()).isEqualTo(ProductType.FOR_SALE);
        }
    }



    @Nested
    @DisplayName("updateProduct()")
    class UpdateProduct {

        @Test
        @DisplayName("Deve atualizar produto com sucesso quando o usuário é dono da loja")
        void shouldUpdateProductSuccessfully() {
            ProductRequestDTO updateRequest = new ProductRequestDTO(
                    "Camiseta Verde",
                    "Camiseta de linho",
                    new BigDecimal("59.90"),
                    "G",
                    Category.DRESS,
                    "Verde",
                    "https://img.example.com/camisa-verde.jpg",
                    ProductType.FOR_SALE,
                    storeId
            );

            when(productRepository.findById(productId)).thenReturn(Optional.of(product));

            ProductResponseDTO result = productService.updateProduct(updateRequest, productId, ownerId);

            assertThat(result.name()).isEqualTo("Camiseta Verde");
            assertThat(result.description()).isEqualTo("Camiseta de linho");
            assertThat(result.price()).isEqualByComparingTo(new BigDecimal("59.90"));
            assertThat(result.size()).isEqualTo("G");
            assertThat(result.color()).isEqualTo("Verde");
            assertThat(result.imageUrl()).isEqualTo("https://img.example.com/camisa-verde.jpg");

            verify(productRepository, times(1)).findById(productId);
        }

        @Test
        @DisplayName("Deve lançar ResourceNotFoundException quando o produto não existir")
        void shouldThrowResourceNotFoundWhenProductNotFound() {
            when(productRepository.findById(productId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> productService.updateProduct(validRequest, productId, ownerId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Produto não encontrado");
        }

        @Test
        @DisplayName("Deve lançar UnauthorizedAccessException quando usuário não é dono da loja")
        void shouldThrowUnauthorizedWhenUserIsNotStoreOwner() {
            when(productRepository.findById(productId)).thenReturn(Optional.of(product));

            assertThatThrownBy(() -> productService.updateProduct(validRequest, productId, otherUserId))
                    .isInstanceOf(UnauthorizedAccessException.class)
                    .hasMessage("Você não tem permissão para editar esse produto nessa loja!");
        }

        @Test
        @DisplayName("Deve atualizar todos os campos do produto corretamente")
        void shouldUpdateAllFieldsCorrectly() {

            ProductRequestDTO updateRequest = new ProductRequestDTO(
                    "Novo Nome",
                    "Nova Descrição",
                    new BigDecimal("199.00"),
                    "P",
                    Category.DRESS,
                    "Preto",
                    "https://img.example.com/novo.jpg",
                    ProductType.FOR_SALE,
                    storeId
            );

            when(productRepository.findById(productId)).thenReturn(Optional.of(product));

            ProductResponseDTO result = productService.updateProduct(updateRequest, productId, ownerId);

            assertThat(result.name()).isEqualTo("Novo Nome");
            assertThat(result.description()).isEqualTo("Nova Descrição");
            assertThat(result.price()).isEqualByComparingTo(new BigDecimal("199.00"));
            assertThat(result.size()).isEqualTo("P");
            assertThat(result.category()).isEqualTo(Category.DRESS);
            assertThat(result.color()).isEqualTo("Preto");
            assertThat(result.imageUrl()).isEqualTo("https://img.example.com/novo.jpg");
            assertThat(result.type()).isEqualTo(ProductType.FOR_SALE);
        }

        @Test
        @DisplayName("Não deve chamar save explicitamente (update via @Transactional dirty check)")
        void shouldNotCallSaveExplicitly() {
            when(productRepository.findById(productId)).thenReturn(Optional.of(product));

            productService.updateProduct(validRequest, productId, ownerId);

            verify(productRepository, never()).save(any());
        }
    }



    @Nested
    @DisplayName("deactivateProduct()")
    class DeactivateProduct {

        @Test
        @DisplayName("Deve desativar produto com sucesso quando usuário é dono e produto está ativo")
        void shouldDeactivateProductSuccessfully() {
            product.setActive(true);
            when(productRepository.findById(productId)).thenReturn(Optional.of(product));

            productService.deactivateProduct(productId, ownerId);

            assertThat(product.isActive()).isFalse();
            verify(productRepository, times(1)).findById(productId);
        }

        @Test
        @DisplayName("Deve lançar ResourceNotFoundException quando o produto não existir")
        void shouldThrowResourceNotFoundWhenProductNotFound() {
            when(productRepository.findById(productId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> productService.deactivateProduct(productId, ownerId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Produto não encontrado");
        }

        @Test
        @DisplayName("Deve lançar UnauthorizedAccessException quando usuário não é dono da loja")
        void shouldThrowUnauthorizedWhenUserIsNotStoreOwner() {
            product.setActive(true);
            when(productRepository.findById(productId)).thenReturn(Optional.of(product));

            assertThatThrownBy(() -> productService.deactivateProduct(productId, otherUserId))
                    .isInstanceOf(UnauthorizedAccessException.class)
                    .hasMessage("Você não tem permissão para editar esse produto nessa loja!");
        }

        @Test
        @DisplayName("Deve lançar ResourceNotFoundException quando o produto já está desativado")
        void shouldThrowResourceNotFoundWhenProductAlreadyInactive() {
            product.setActive(false);
            when(productRepository.findById(productId)).thenReturn(Optional.of(product));

            assertThatThrownBy(() -> productService.deactivateProduct(productId, ownerId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Este produto já se encontra desativada.");
        }

        @Test
        @DisplayName("Não deve chamar save explicitamente (desativação via @Transactional dirty check)")
        void shouldNotCallSaveExplicitly() {
            product.setActive(true);
            when(productRepository.findById(productId)).thenReturn(Optional.of(product));

            productService.deactivateProduct(productId, ownerId);

            verify(productRepository, never()).save(any());
        }
    }
}