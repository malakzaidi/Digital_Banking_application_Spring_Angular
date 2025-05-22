package org.springmvc.ebanking.web;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springmvc.ebanking.dtos.CustomerDTO;
import org.springmvc.ebanking.entities.Customer;
import org.springmvc.ebanking.exceptions.CustomerNotFoundException;
import org.springmvc.ebanking.exceptions.ResourceNotFoundException;
import org.springmvc.ebanking.services.BankAccountsService;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/customers")
@AllArgsConstructor
@Slf4j
public class CustomerRestController {
    private BankAccountsService bankAccountService;

    @GetMapping
    public List<CustomerDTO> listCustomers() {
        log.info("Fetching all customers");
        return bankAccountService.listCustomers().stream()
                .map(c -> {
                    CustomerDTO dto = new CustomerDTO();
                    dto.setId(c.getId());
                    dto.setName(c.getName());
                    dto.setEmail(c.getEmail());
                    return dto;
                }).collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerDTO> getCustomer(@PathVariable Long id) {
        log.info("Fetching customer with ID: {}", id);
        Customer customer = bankAccountService.findCustomerById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));
        CustomerDTO dto = new CustomerDTO();
        dto.setId(customer.getId());
        dto.setName(customer.getName());
        dto.setEmail(customer.getEmail());
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/search")
    public List<CustomerDTO> searchCustomers(@RequestParam(name = "keyword", defaultValue = "") String keyword) {
        log.info("Searching customers with keyword: {}", keyword);
        return bankAccountService.searchCustomers("%" + keyword + "%");
    }

    @PostMapping
    public CustomerDTO saveCustomer(@RequestBody CustomerDTO customerDTO) {
        log.info("Saving customer: {}", customerDTO);
        return bankAccountService.saveCustomer(customerDTO);
    }

    @PutMapping("/{customerId}")
    public ResponseEntity<CustomerDTO> updateCustomer(@PathVariable Long customerId, @RequestBody CustomerDTO customerDTO) {
        log.info("Updating customer ID: {} with data: {}", customerId, customerDTO);
        if (customerDTO.getName() == null || customerDTO.getEmail() == null) {
            log.warn("Invalid customer data: name or email is null");
            return ResponseEntity.badRequest().build();
        }
        customerDTO.setId(customerId);
        try {
            CustomerDTO updatedCustomer = bankAccountService.updateCustomer(customerDTO);
            return ResponseEntity.ok(updatedCustomer);
        } catch (Exception e) {
            log.error("Failed to update customer ID: {}: {}", customerId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable Long id) {
        log.info("Deleting customer with ID: {}", id);
        try {
            bankAccountService.deleteCustomer(id);
            return ResponseEntity.ok().build();
        } catch (CustomerNotFoundException e) {
            log.error("Customer not found: {}", id);
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            log.error("Cannot delete customer {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(null);
        } catch (DataIntegrityViolationException e) {
            log.error("Cannot delete customer {} due to database constraints: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(null);
        } catch (Exception e) {
            log.error("Failed to delete customer {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}