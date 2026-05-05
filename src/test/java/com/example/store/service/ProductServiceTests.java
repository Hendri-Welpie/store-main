package com.example.store.service;

import com.example.store.dto.ProductCreateRequest;
import com.example.store.dto.ProductDTO;
import com.example.store.entity.Product;
import com.example.store.mapper.ProductMapper;
import com.example.store.repository.ProductRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTests {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductService productService;

    private Product product;
    private ProductDTO productDTO;

    @BeforeEach
    void setUp() {
        product = new Product();
        product.setId(1L);
        product.setDescription("Widget A");

        productDTO = new ProductDTO();
        productDTO.setId(1L);
        productDTO.setDescription("Widget A");
        productDTO.setOrderIds(List.of());
    }

    @Test
    void getAllProducts_returnsMappedList() {
        var page = new PageImpl<>(List.of(product));
        when(productRepository.findAll(any(Pageable.class))).thenReturn(page);
        when(productMapper.productsToProductDTOs(List.of(product))).thenReturn(List.of(productDTO));

        var result = productService.getAllProducts(Pageable.ofSize(10));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDescription()).isEqualTo("Widget A");
        verify(productRepository).findAll(any(Pageable.class));
        verify(productMapper).productsToProductDTOs(List.of(product));
    }

    @Test
    void getAllProducts_emptyPage_returnsEmptyList() {
        when(productRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of()));
        when(productMapper.productsToProductDTOs(List.of())).thenReturn(List.of());

        var result = productService.getAllProducts(Pageable.ofSize(10));

        assertThat(result).isEmpty();
    }

    @Test
    void getProductById_found_returnsMappedDTO() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productMapper.productToProductDTO(product)).thenReturn(productDTO);

        var result = productService.getProductById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getDescription()).isEqualTo("Widget A");
        assertThat(result.get().getOrderIds()).isEmpty();
    }

    @Test
    void getProductById_notFound_returnsEmpty() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        var result = productService.getProductById(99L);

        assertThat(result).isEmpty();
    }

    @Test
    void createProduct_savesAndReturnsMappedDTO() {
        var request = new ProductCreateRequest();
        request.setDescription("Widget A");

        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(productMapper.productToProductDTO(product)).thenReturn(productDTO);

        var result = productService.createProduct(request);

        assertThat(result).isNotNull();
        assertThat(result.getDescription()).isEqualTo("Widget A");
        verify(productRepository).save(any(Product.class));
        verify(productMapper).productToProductDTO(product);
    }

    @Test
    void createProduct_setsDescriptionFromRequest() {
        var request = new ProductCreateRequest();
        request.setDescription("Gadget B");

        var savedProduct = new Product();
        savedProduct.setId(2L);
        savedProduct.setDescription("Gadget B");

        var savedDTO = new ProductDTO();
        savedDTO.setId(2L);
        savedDTO.setDescription("Gadget B");
        savedDTO.setOrderIds(List.of());

        when(productRepository.save(any(Product.class))).thenReturn(savedProduct);
        when(productMapper.productToProductDTO(savedProduct)).thenReturn(savedDTO);

        var result = productService.createProduct(request);

        assertThat(result.getDescription()).isEqualTo("Gadget B");
    }
}
