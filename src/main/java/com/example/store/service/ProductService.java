package com.example.store.service;

import com.example.store.dto.ProductCreateRequest;
import com.example.store.dto.ProductDTO;
import com.example.store.entity.Product;
import com.example.store.mapper.ProductMapper;
import com.example.store.repository.ProductRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Transactional(readOnly = true)
    public List<ProductDTO> getAllProducts(Pageable pageable) {
        log.trace("Getting all products");
        return productMapper.productsToProductDTOs(
                productRepository.findAll(pageable).getContent());
    }

    @Transactional(readOnly = true)
    public Optional<ProductDTO> getProductById(Long id) {
        log.trace("Getting product by id: {}", id);
        return productRepository.findById(id).map(productMapper::productToProductDTO);
    }

    @Transactional
    public ProductDTO createProduct(ProductCreateRequest request) {
        log.trace("Creating product: {}", request);
        Product product = new Product();
        product.setDescription(request.getDescription());
        return productMapper.productToProductDTO(productRepository.save(product));
    }
}
