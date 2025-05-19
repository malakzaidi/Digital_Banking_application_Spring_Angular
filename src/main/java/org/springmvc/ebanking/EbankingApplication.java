package org.springmvc.ebanking;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springmvc.ebanking.dtos.BankAccountDTO;
import org.springmvc.ebanking.dtos.CurrentBankAccountDTO;
import org.springmvc.ebanking.dtos.CustomerDTO;
import org.springmvc.ebanking.dtos.SavingBankAccountDTO;
import org.springmvc.ebanking.entities.Role;
import org.springmvc.ebanking.entities.User;
import org.springmvc.ebanking.exceptions.CustomerNotFoundException;
import org.springmvc.ebanking.repositories.RoleRepository;
import org.springmvc.ebanking.repositories.UserRepository;
import org.springmvc.ebanking.services.BankAccountsService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
@SpringBootApplication
public class EbankingApplication {

    public static void main(String[] args) {
        SpringApplication.run(EbankingApplication.class, args);
    }

    @Bean
    CommandLineRunner commandLineRunner(BankAccountsService bankAccountService,
                                        UserRepository userRepository,
                                        RoleRepository roleRepository,
                                        PasswordEncoder passwordEncoder) {
        return args -> {
            // Step 1: Temporarily set an authenticated user (admin) for createdBy
            User adminUser = userRepository.findByUsername("admin")
                    .orElseThrow(() -> new RuntimeException("Admin user not found"));
            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                    adminUser.getUsername(), null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN")));
            SecurityContextHolder.getContext().setAuthentication(auth);

            // Step 2: Create or ensure roles exist
            Role userRole = roleRepository.findByName("USER")
                    .orElseGet(() -> {
                        Role role = new Role();
                        role.setName("USER");
                        role.setDescription("Standard user role");
                        return roleRepository.save(role);
                    });

            Role adminRole = roleRepository.findByName("ADMIN")
                    .orElseGet(() -> {
                        Role role = new Role();
                        role.setName("ADMIN");
                        role.setDescription("Administrator role");
                        return roleRepository.save(role);
                    });

            // Step 3: Create users with different roles and encoded passwords
            // Admin user
            if (!userRepository.existsByUsername("admin")) {
                User admin = new User();
                admin.setUsername("admin");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setEmail("admin@example.com");
                admin.setFirstName("Admin");
                admin.setLastName("User");
                admin.setEnabled(true);
                admin.setRoles(Collections.singletonList(adminRole));
                userRepository.save(admin);
                log.info("Created admin user: {}", admin.getUsername());
            }

            // Regular users
            Stream.of("john", "jane").forEach(username -> {
                if (!userRepository.existsByUsername(username)) {
                    User user = new User();
                    user.setUsername(username);
                    user.setPassword(passwordEncoder.encode(username + "123")); // e.g., john123, jane123
                    user.setEmail(username + "@example.com");
                    user.setFirstName(username.substring(0, 1).toUpperCase() + username.substring(1));
                    user.setLastName("Doe");
                    user.setEnabled(true);
                    user.setRoles(Collections.singletonList(userRole));
                    userRepository.save(user);
                    log.info("Created regular user: {}", user.getUsername());
                }
            });

            // Step 4: Existing logic for customers and bank accounts
            Stream.of("Meryem", "Imane", "Mohamed").forEach(name -> {
                CustomerDTO customer = new CustomerDTO();
                customer.setName(name);
                customer.setEmail(name + "@gmail.com");
                bankAccountService.saveCustomer(customer);
            });

            bankAccountService.listCustomers().forEach(customer -> {
                try {
                    bankAccountService.saveCurrentBankAccount(Math.random() * 90000, 9000, customer.getId());
                    bankAccountService.saveSavingBankAccount(Math.random() * 120000, 5.5, customer.getId());
                } catch (CustomerNotFoundException e) {
                    e.printStackTrace();
                }
            });

            List<BankAccountDTO> bankAccounts = bankAccountService.bankAccountList();
            for (BankAccountDTO bankAccount : bankAccounts) {
                for (int i = 0; i < 10; i++) {
                    String accountId = bankAccount.getId();
                    if (bankAccount instanceof CurrentBankAccountDTO) {
                        accountId = ((CurrentBankAccountDTO) bankAccount).getId();
                        bankAccountService.credit(accountId, 10000 + Math.random() * 120000, "Credit");
                        bankAccountService.debit(accountId, 1000 + Math.random() * 9000, "Debit");
                    } else if (bankAccount instanceof SavingBankAccountDTO) {
                        accountId = ((SavingBankAccountDTO) bankAccount).getId();
                        bankAccountService.credit(accountId, 10000 + Math.random() * 120000, "Credit");
                        bankAccountService.debit(accountId, 1000 + Math.random() * 9000, "Debit");
                    } else {
                        log.warn("Unexpected account type: {}", bankAccount.getClass().getName());
                    }
                }
            }
            SecurityContextHolder.clearContext();
        };
    }
}