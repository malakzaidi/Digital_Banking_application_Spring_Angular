package org.springmvc.ebanking.web;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springmvc.ebanking.entities.Customer;
import org.springmvc.ebanking.services.BankAccountsService;

import java.util.List;

@RestController
@AllArgsConstructor
@Slf4j
public class CustomerRestController {

    private BankAccountsService bankAccountsService;

    @GetMapping("/customers")
    public List<Customer> customers(){
        return bankAccountsService.listCustomers();
    }

}
