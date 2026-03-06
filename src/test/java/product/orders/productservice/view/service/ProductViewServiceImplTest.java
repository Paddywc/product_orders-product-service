package product.orders.productservice.view.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import product.orders.productservice.domain.exception.ProductNotFoundException;
import product.orders.productservice.domain.model.Product;
import product.orders.productservice.domain.model.ProductCategory;
import product.orders.productservice.domain.model.ProductStatus;
import product.orders.productservice.repository.ProductRepository;
import product.orders.productservice.view.mapper.ProductViewMapper;
import product.orders.productservice.view.model.ProductView;
import product.orders.productservice.view.repository.ProductViewRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductViewServiceImplTest {

    @Mock
    private ProductViewRepository viewRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductViewMapper mapper;

    @InjectMocks
    private ProductViewServiceImpl productViewService;

    private final PageRequest pageable = PageRequest.of(0, 10);


    @Test
    void testGetProduct_cacheHit_returnsCachedView() {
        // Arrange
        UUID productId = UUID.randomUUID();
        ProductView view = mock(ProductView.class);

        when(viewRepository.findById(productId))
                .thenReturn(Optional.of(view));

        // Act
        ProductView result = productViewService.getProduct(productId);

        // Assert
        assertThat(result).isSameAs(view);
        verify(viewRepository, times(1)).findById(productId);
        verifyNoInteractions(productRepository);
        verifyNoInteractions(mapper);
    }

    @Test
    void testGetProduct_NotfoundInCache_LoadsFromDbAndCaches() {
        // Arrange
        UUID productId = UUID.randomUUID();
        Product dbProduct = Product.create("Database Product", "A product from database",
                2000L, product.orders.productservice.domain.model.ProductStatus.ACTIVE,
                product.orders.productservice.domain.model.ProductCategory.ELECTRONICS);
        ProductView mappedProduct = new ProductView(productId, "Database Product", "A product from database",
                2000L, "ELECTRONICS", "ACTIVE");

        when(viewRepository.findById(productId)).thenReturn(Optional.empty());
        when(productRepository.findById(productId)).thenReturn(Optional.of(dbProduct));
        when(mapper.toView(dbProduct)).thenReturn(mappedProduct);

        // Act
        ProductView result = productViewService.getProduct(productId);

        // Assert
        assertNotNull(result);
        assertEquals(mappedProduct, result);
        verify(viewRepository, times(1)).findById(productId);
        verify(productRepository, times(1)).findById(productId);
        verify(mapper, times(1)).toView(dbProduct);
        verify(viewRepository, times(1)).save(mappedProduct);
    }

    @Test
    void testGetProduct_ProductDoesNotExistInViewRepositoryOrDatabase_ThrowsException() {
        // Arrange
        UUID productId = UUID.randomUUID();
        when(viewRepository.findById(productId)).thenReturn(Optional.empty());
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // Act & Assert
       assertThrows(ProductNotFoundException.class, () -> productViewService.getProduct(productId));
    }

    @Test
    void testUpsert_PassedProductObject_MapsAndSavesView() {
        // Arrange
        Product product = mock(Product.class);
        ProductView view = mock(ProductView.class);

        when(mapper.toView(product)).thenReturn(view);

        // Act
        productViewService.upsert(product);

        // Assert
        verify(viewRepository).save(view);
    }


    @Test
    void testGetAllActiveProducts_CacheHit_ReturnsCachedPage() {
        // Arrange
        Page<ProductView> cached =
                new PageImpl<>(List.of(mock(ProductView.class)));

        when(viewRepository.findByStatus(
                ProductStatus.ACTIVE.name(),
                pageable
        )).thenReturn(cached);

        // Act
        Page<ProductView> result =
                productViewService.getAllActiveProducts(pageable);

        // Assert
        assertThat(result).isSameAs(cached);
        verifyNoInteractions(productRepository);
    }

    @Test
    void testGetAllActiveProducts_CacheMiss_LoadsFromDbAndCaches() {
        // Arrange
        Product product = mock(Product.class);
        ProductView view = mock(ProductView.class);

        Page<ProductView> empty = Page.empty(pageable);
        Page<Product> products =
                new PageImpl<>(List.of(product), pageable, 1);

        when(viewRepository.findByStatus(
                ProductStatus.ACTIVE.name(),
                pageable
        )).thenReturn(empty);
        when(productRepository.findByStatus(
                ProductStatus.ACTIVE,
                pageable
        )).thenReturn(products);
        when(mapper.toView(product)).thenReturn(view);

        // Act
        Page<ProductView> result =
                productViewService.getAllActiveProducts(pageable);

        // Assert
        assertThat(result.getContent()).containsExactly(view);
        verify(viewRepository).saveAll(List.of(view));
        verify(productRepository, times(1)).findByStatus(ProductStatus.ACTIVE, pageable);
    }


    @Test
    void getProductsByCategory_WithStatusAndCacheHit_ReturnsCache() {
        Page<ProductView> cached =
                new PageImpl<>(List.of(mock(ProductView.class)));

        when(viewRepository.findByCategoryAndStatus(
                ProductCategory.ELECTRONICS.name(),
                ProductStatus.ACTIVE.name(),
                pageable
        )).thenReturn(cached);

        Page<ProductView> result =
                productViewService.getProductsByCategory(
                        ProductCategory.ELECTRONICS,
                        ProductStatus.ACTIVE,
                        pageable
                );

        assertThat(result).isSameAs(cached);
        verify(viewRepository).findByCategoryAndStatus(ProductCategory.ELECTRONICS.name(), ProductStatus.ACTIVE.name(), pageable);
        verifyNoInteractions(productRepository);
    }

    @Test
    void getProductsByCategory_withStatusAndCacheMiss_LoadsFromDb() {
        // Arrange
        Product product = mock(Product.class);
        ProductView view = mock(ProductView.class);

        Page<ProductView> empty = Page.empty(pageable);
        Page<Product> products =
                new PageImpl<>(List.of(product), pageable, 1);

        when(viewRepository.findByCategoryAndStatus(
                ProductCategory.ELECTRONICS.name(),
                ProductStatus.ACTIVE.name(),
                pageable
        )).thenReturn(empty);

        when(productRepository.findByCategoryAndStatus(
                ProductCategory.ELECTRONICS,
                ProductStatus.ACTIVE,
                pageable
        )).thenReturn(products);

        when(mapper.toView(product)).thenReturn(view);

        // Act
        Page<ProductView> result =
                productViewService.getProductsByCategory(
                        ProductCategory.ELECTRONICS,
                        ProductStatus.ACTIVE,
                        pageable
                );

        // Assert
        assertThat(result.getContent()).containsExactly(view);
        verify(productRepository).findByCategoryAndStatus(ProductCategory.ELECTRONICS, ProductStatus.ACTIVE, pageable);
        verify(viewRepository).saveAll(List.of(view));
    }


    @Test
    void getProductsByCategory_WithoutStatusAndCacheHit_ReturnsCache() {
        // Arrange
        Page<ProductView> cached =
                new PageImpl<>(List.of(mock(ProductView.class)));

        when(viewRepository.findByCategory(
                ProductCategory.ELECTRONICS.name(),
                pageable
        )).thenReturn(cached);

        // Act
        Page<ProductView> result =
                productViewService.getProductsByCategory(
                        ProductCategory.ELECTRONICS,
                        null,
                        pageable
                );

        // Assert
        assertThat(result).isSameAs(cached);
        verify(viewRepository).findByCategory(ProductCategory.ELECTRONICS.name(), pageable);
        verifyNoInteractions(productRepository);
        verifyNoInteractions(mapper);
    }

    @Test
    void getProductsByCategory_WithoutStatusAndCacheMiss_LoadsFromDbAndCaches() {
        // Arrange
        Product product = mock(Product.class);
        ProductView view = mock(ProductView.class);

        Page<ProductView> empty = Page.empty(pageable);
        Page<Product> products =
                new PageImpl<>(List.of(product), pageable, 1);

        when(viewRepository.findByCategory(
                ProductCategory.ELECTRONICS.name(),
                pageable
        )).thenReturn(empty);

        when(productRepository.findByCategory(
                ProductCategory.ELECTRONICS,
                pageable
        )).thenReturn(products);

        when(mapper.toView(product)).thenReturn(view);

        // Act
        Page<ProductView> result =
                productViewService.getProductsByCategory(
                        ProductCategory.ELECTRONICS,
                        null,
                        pageable
                );

        // Assert
        assertThat(result.getContent()).containsExactly(view);
        verify(productRepository).findByCategory(ProductCategory.ELECTRONICS, pageable);
        verify(viewRepository).saveAll(List.of(view));
    }

}