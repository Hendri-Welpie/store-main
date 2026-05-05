package com.example.store.service;

import com.example.store.dto.CustomerCreateRequest;
import com.example.store.dto.CustomerDTO;
import com.example.store.entity.Customer;
import com.example.store.mapper.CustomerMapper;
import com.example.store.repository.CustomerRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTests {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private CustomerMapper customerMapper;

    @InjectMocks
    private CustomerService customerService;

    private Customer customer;
    private CustomerDTO customerDTO;

    @BeforeEach
    void setUp() {
        customer = new Customer();
        customer.setId(1L);
        customer.setName("Alice");

        customerDTO = new CustomerDTO();
        customerDTO.setId(1L);
        customerDTO.setName("Alice");
        customerDTO.setOrders(List.of());
    }

    @Test
    void getAllCustomers_returnsMappedList() {
        var page = new PageImpl<>(List.of(customer));
        when(customerRepository.findAll(any(Pageable.class))).thenReturn(page);
        when(customerMapper.customersToCustomerDTOs(List.of(customer))).thenReturn(List.of(customerDTO));

        var result = customerService.getAllCustomers(Pageable.ofSize(10));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Alice");
        verify(customerRepository).findAll(any(Pageable.class));
        verify(customerMapper).customersToCustomerDTOs(List.of(customer));
    }

    @Test
    void getAllCustomers_emptyPage_returnsEmptyList() {
        when(customerRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of()));
        when(customerMapper.customersToCustomerDTOs(List.of())).thenReturn(List.of());

        var result = customerService.getAllCustomers(Pageable.ofSize(10));

        assertThat(result).isEmpty();
    }

    @Test
    void createCustomer_savesAndReturnsMappedDTO() {
        var request = new CustomerCreateRequest();
        request.setName("Alice");

        when(customerRepository.save(any(Customer.class))).thenReturn(customer);
        when(customerMapper.customerToCustomerDTO(customer)).thenReturn(customerDTO);

        var result = customerService.createCustomer(request);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Alice");
        verify(customerRepository).save(any(Customer.class));
        verify(customerMapper).customerToCustomerDTO(customer);
    }

    @Test
    void findCustomerByName_returnsMatchingCustomers() {
        var page = new PageImpl<>(List.of(customer));
        when(customerRepository.findByNameContainingIgnoreCase(eq("Alice"), any(Pageable.class)))
                .thenReturn(page);
        when(customerMapper.customersToCustomerDTOs(List.of(customer))).thenReturn(List.of(customerDTO));

        var result = customerService.findCustomerByName("Alice", Pageable.ofSize(10));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Alice");
        verify(customerRepository).findByNameContainingIgnoreCase(eq("Alice"), any(Pageable.class));
    }

    @Test
    void findCustomerByName_noMatch_returnsEmptyList() {
        when(customerRepository.findByNameContainingIgnoreCase(eq("Unknown"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));
        when(customerMapper.customersToCustomerDTOs(List.of())).thenReturn(List.of());

        var result = customerService.findCustomerByName("Unknown", Pageable.ofSize(10));

        assertThat(result).isEmpty();
    }
}
