package org.springmvc.ebanking.web;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springmvc.ebanking.dtos.CustomerDTO;
import org.springmvc.ebanking.exceptions.CustomerNotFoundException;
import org.springmvc.ebanking.services.BankAccountsService;

import java.util.List;

@RestController
@AllArgsConstructor
@Slf4j
@CrossOrigin("*")
public class CustomerRestController {
    private BankAccountsService bankAccountService;

    @GetMapping("/customers")
    public List<CustomerDTO> customers() {
        log.info("Fetching all customers");
        return bankAccountService.listCustomers();
    }

    @GetMapping("/customers/search")
    public List<CustomerDTO> searchCustomers(@RequestParam(name = "keyword", defaultValue = "") String keyword) {
        log.info("Searching customers with keyword: {}", keyword);
        return bankAccountService.searchCustomers("%" + keyword + "%");
    }

    @GetMapping("/customers/{id}")
    public CustomerDTO getCustomer(@PathVariable(name = "id") Long customerId) throws CustomerNotFoundException {
        log.info("Fetching customer with ID: {}", customerId);
        return bankAccountService.getCustomer(customerId);
    }

    @PostMapping("/customers")
    public CustomerDTO saveCustomer(@RequestBody CustomerDTO customerDTO) {
        log.info("Saving customer: {}", customerDTO);
        return bankAccountService.saveCustomer(customerDTO);
    }

    @PutMapping("/customers/{customerId}")
    public CustomerDTO updateCustomer(@PathVariable Long customerId, @RequestBody CustomerDTO customerDTO) {
        log.info("Updating customer ID: {} with data: {}", customerId, customerDTO);
        customerDTO.setId(customerId);
        return bankAccountService.updateCustomer(customerDTO);
    }

    @DeleteMapping("/customers/{id}")
    public void deleteCustomer(@PathVariable Long id) {
        log.info("Deleting customer with ID: {}", id);
        bankAccountService.deleteCustomer(id);
    }
}