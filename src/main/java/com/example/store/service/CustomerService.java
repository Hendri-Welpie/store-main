package com.example.store.service;

import com.example.store.dto.CustomerCreateRequest;
import com.example.store.dto.CustomerDTO;
import com.example.store.entity.Customer;
import com.example.store.mapper.CustomerMapper;
import com.example.store.repository.CustomerRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerService {
    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

    @Transactional(readOnly = true)
    public List<CustomerDTO> getAllCustomers(Pageable pageable) {
        log.trace("Getting all customers");
        return customerMapper.customersToCustomerDTOs(
                customerRepository.findAll(pageable).getContent());
    }

    @Transactional
    public CustomerDTO createCustomer(CustomerCreateRequest request) {
        log.trace("Creating new customer with name: {}", request.getName());
        Customer customer = new Customer();
        customer.setName(request.getName());
        return customerMapper.customerToCustomerDTO(customerRepository.save(customer));
    }

    @Transactional(readOnly = true)
    public List<CustomerDTO> findCustomerByName(String name, Pageable pageable) {
        log.trace("Finding customers by name: {}", name);
        return customerMapper.customersToCustomerDTOs(customerRepository
                .findByNameContainingIgnoreCase(name, pageable)
                .getContent());
    }
}
