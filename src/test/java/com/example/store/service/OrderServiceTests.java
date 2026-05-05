package com.example.store.service;

import com.example.store.dto.OrderCreateRequest;
import com.example.store.dto.OrderDTO;
import com.example.store.entity.Customer;
import com.example.store.entity.Order;
import com.example.store.entity.Product;
import com.example.store.mapper.OrderMapper;
import com.example.store.repository.CustomerRepository;
import com.example.store.repository.OrderRepository;
import com.example.store.repository.ProductRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTests {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private OrderMapper orderMapper;

    @InjectMocks
    private OrderService orderService;

    private Customer customer;
    private Order order;
    private OrderDTO orderDTO;

    @BeforeEach
    void setUp() {
        customer = new Customer();
        customer.setId(1L);
        customer.setName("Alice");

        order = new Order();
        order.setId(1L);
        order.setDescription("Widget order");
        order.setCustomer(customer);

        orderDTO = new OrderDTO();
        orderDTO.setId(1L);
        orderDTO.setDescription("Widget order");
        orderDTO.setProducts(List.of());
    }

    @Test
    void getAllOrders_returnsMappedList() {
        var page = new PageImpl<>(List.of(order));
        when(orderRepository.findAll(any(Pageable.class))).thenReturn(page);
        when(orderMapper.ordersToOrderDTOs(List.of(order))).thenReturn(List.of(orderDTO));

        var result = orderService.getAllOrders(Pageable.ofSize(10));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDescription()).isEqualTo("Widget order");
        verify(orderRepository).findAll(any(Pageable.class));
    }

    @Test
    void getAllOrders_emptyPage_returnsEmptyList() {
        when(orderRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of()));
        when(orderMapper.ordersToOrderDTOs(List.of())).thenReturn(List.of());

        var result = orderService.getAllOrders(Pageable.ofSize(10));

        assertThat(result).isEmpty();
    }

    @Test
    void getOrderById_found_returnsMappedDTO() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderMapper.orderToOrderDTO(order)).thenReturn(orderDTO);

        var result = orderService.getOrderById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getDescription()).isEqualTo("Widget order");
    }

    @Test
    void getOrderById_notFound_returnsEmpty() {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        var result = orderService.getOrderById(99L);

        assertThat(result).isEmpty();
    }

    @Test
    void createOrder_success_savesAndReturnsMappedDTO() {
        var product = new Product();
        product.setId(10L);
        product.setDescription("Gadget");

        var request = new OrderCreateRequest();
        request.setCustomerId(1L);
        request.setDescription("Widget order");
        request.setProductIds(List.of(10L));

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(productRepository.findAllById(List.of(10L))).thenReturn(List.of(product));
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(orderMapper.orderToOrderDTO(order)).thenReturn(orderDTO);

        var result = orderService.createOrder(request);

        assertThat(result).isNotNull();
        assertThat(result.getDescription()).isEqualTo("Widget order");
        verify(customerRepository).findById(1L);
        verify(productRepository).findAllById(List.of(10L));
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void createOrder_customerNotFound_throws404() {
        var request = new OrderCreateRequest();
        request.setCustomerId(99L);
        request.setDescription("Orphan order");

        when(customerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Customer not found");
    }

    @Test
    void createOrder_noProducts_savesOrderWithEmptyList() {
        var request = new OrderCreateRequest();
        request.setCustomerId(1L);
        request.setDescription("Bare order");
        // productIds defaults to empty list

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(productRepository.findAllById(List.of())).thenReturn(List.of());
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(orderMapper.orderToOrderDTO(order)).thenReturn(orderDTO);

        var result = orderService.createOrder(request);

        assertThat(result).isNotNull();
        verify(productRepository).findAllById(List.of());
    }
}
