package com.example.store.service;

import com.example.store.dto.OrderCreateRequest;
import com.example.store.dto.OrderDTO;
import com.example.store.entity.Order;
import com.example.store.entity.Product;
import com.example.store.mapper.OrderMapper;
import com.example.store.repository.CustomerRepository;
import com.example.store.repository.OrderRepository;
import com.example.store.repository.ProductRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final OrderMapper orderMapper;

    @Transactional(readOnly = true)
    public List<OrderDTO> getAllOrders(Pageable pageable) {
        log.trace("Getting all orders");
        return orderMapper.ordersToOrderDTOs(orderRepository.findAll(pageable).getContent());
    }

    @Transactional(readOnly = true)
    public Optional<OrderDTO> getOrderById(Long id) {
        log.trace("Getting order by id: {}", id);
        return orderRepository.findById(id).map(orderMapper::orderToOrderDTO);
    }

    @Transactional
    public OrderDTO createOrder(OrderCreateRequest request) {
        log.trace("Creating new order: {}", request);
        var customer = customerRepository
                .findById(request.getCustomerId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Customer not found: " + request.getCustomerId()));

        List<Product> products = productRepository.findAllById(request.getProductIds());

        Order order = new Order();
        order.setDescription(request.getDescription());
        order.setCustomer(customer);
        order.setProducts(products);

        return orderMapper.orderToOrderDTO(orderRepository.save(order));
    }
}
