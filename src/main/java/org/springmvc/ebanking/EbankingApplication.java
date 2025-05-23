package org.springmvc.ebanking;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springmvc.ebanking.entities.*;
import org.springmvc.ebanking.enums.AccountStatus;
import org.springmvc.ebanking.enums.OperationType;
import org.springmvc.ebanking.repositories.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@SpringBootApplication
public class EbankingApplication {

    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
        dotenv.entries().forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));
        SpringApplication.run(EbankingApplication.class, args);
    }

    @Bean
    public CommandLineRunner commandLineRunner(
            UserRepository userRepository,
            RoleRepository roleRepository,
            CustomerRepository customerRepository,
            BankAccountRepository bankAccountRepository,
            AccountOperationRepository accountOperationRepository,
            PasswordEncoder passwordEncoder
    ) {
        return args -> {
            // Create roles if they don't exist
            if (roleRepository.findByName("USER").isEmpty()) {
                Role userRole = new Role();
                userRole.setName("USER");
                userRole.setDescription("Standard user role");
                roleRepository.save(userRole);
            }
            if (roleRepository.findByName("ADMIN").isEmpty()) {
                Role adminRole = new Role();
                adminRole.setName("ADMIN");
                adminRole.setDescription("Administrator role");
                roleRepository.save(adminRole);
            }

            Role adminRole = roleRepository.findByName("ADMIN")
                    .orElseThrow(() -> new RuntimeException("ADMIN role not found"));
            Role userRole = roleRepository.findByName("USER")
                    .orElseThrow(() -> new RuntimeException("USER role not found"));

            // Create three admin users
            List<User> admins = Arrays.asList(
                    createUser("admin1", "admin1@example.com", "Admin1", "User", passwordEncoder.encode("1234")),
                    createUser("admin2", "admin2@example.com", "Admin2", "User", passwordEncoder.encode("1234")),
                    createUser("admin3", "admin3@example.com", "Admin3", "User", passwordEncoder.encode("1234"))
            );

            for (User admin : admins) {
                if (userRepository.findByUsername(admin.getUsername()).isEmpty()) {
                    admin.setRoles(Collections.singleton(adminRole));
                    userRepository.save(admin);
                    System.out.println("Admin user created: " + admin.getUsername());
                }
            }

            // Create regular users
            List<User> regularUsers = Arrays.asList(
                    createUser("john", "john@example.com", "John", "Doe", passwordEncoder.encode("john123")),
                    createUser("jane", "jane@example.com", "Jane", "Doe", passwordEncoder.encode("jane123"))
            );

            for (User user : regularUsers) {
                if (userRepository.findByUsername(user.getUsername()).isEmpty()) {
                    user.setRoles(Collections.singleton(userRole));
                    userRepository.save(user);
                    System.out.println("Regular user created: " + user.getUsername());
                }
            }

            // Set security context to admin1 for initialization
            User admin1 = userRepository.findByUsername("admin1")
                    .orElseThrow(() -> new RuntimeException("Admin user admin1 not found"));
            UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                    admin1.getUsername(),
                    admin1.getPassword(),
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"))
            );
            SecurityContextHolder.getContext().setAuthentication(
                    new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities()
                    )
            );

            // Create customers
            List<Customer> customers = Arrays.asList(
                    createCustomer("Meryem", "meryem@gmail.com", admin1),
                    createCustomer("Imane", "imane@gmail.com", admin1),
                    createCustomer("Mohamed", "mohamed@gmail.com", admin1)
            );

            for (Customer customer : customers) {
                if (customerRepository.findByEmail(customer.getEmail()).isEmpty()) {
                    customerRepository.save(customer);
                    System.out.println("Customer created: " + customer.getName());
                }
            }

            // Create bank accounts and operations for each customer
            for (Customer customer : customerRepository.findAll()) {
                // Current Account
                CurrentAccount currentAccount = new CurrentAccount();
                currentAccount.setId(java.util.UUID.randomUUID().toString());
                currentAccount.setBalance(50000);
                currentAccount.setCreatedAt(new Date());
                currentAccount.setStatus(AccountStatus.ACTIVATED);
                currentAccount.setCustomer(customer);
                currentAccount.setOverDraft(9000);
                currentAccount.setCreatedBy(admin1);
                currentAccount.setUpdatedBy(admin1);
                bankAccountRepository.save(currentAccount);

                // Saving Account
                SavingAccount savingAccount = new SavingAccount();
                savingAccount.setId(java.util.UUID.randomUUID().toString());
                savingAccount.setBalance(75000);
                savingAccount.setCreatedAt(new Date());
                savingAccount.setStatus(AccountStatus.ACTIVATED);
                savingAccount.setCustomer(customer);
                savingAccount.setInterestRate(5.5);
                savingAccount.setCreatedBy(admin1);
                savingAccount.setUpdatedBy(admin1);
                bankAccountRepository.save(savingAccount);

                // Account Operations for Current Account
                AccountOperation creditOp1 = new AccountOperation();
                creditOp1.setOperationDate(new Date());
                creditOp1.setAmount(25000);
                creditOp1.setType(OperationType.CREDIT);
                creditOp1.setBankAccount(currentAccount);
                creditOp1.setDescription("Credit");
                creditOp1.setPerformedBy(admin1);
                accountOperationRepository.save(creditOp1);

                AccountOperation debitOp1 = new AccountOperation();
                debitOp1.setOperationDate(new Date());
                debitOp1.setAmount(3000);
                debitOp1.setType(OperationType.DEBIT);
                debitOp1.setBankAccount(currentAccount);
                debitOp1.setDescription("Debit");
                debitOp1.setPerformedBy(admin1);
                accountOperationRepository.save(debitOp1);

                // Account Operations for Saving Account
                AccountOperation creditOp2 = new AccountOperation();
                creditOp2.setOperationDate(new Date());
                creditOp2.setAmount(40000);
                creditOp2.setType(OperationType.CREDIT);
                creditOp2.setBankAccount(savingAccount);
                creditOp2.setDescription("Credit");
                creditOp2.setPerformedBy(admin1);
                accountOperationRepository.save(creditOp2);

                AccountOperation debitOp2 = new AccountOperation();
                debitOp2.setOperationDate(new Date());
                debitOp2.setAmount(5000);
                debitOp2.setType(OperationType.DEBIT);
                debitOp2.setBankAccount(savingAccount);
                debitOp2.setDescription("Debit");
                debitOp2.setPerformedBy(admin1);
                accountOperationRepository.save(debitOp2);
            }

            // Clear security context after initialization
            SecurityContextHolder.clearContext();
        };
    }

    private User createUser(String username, String email, String firstName, String lastName, String encodedPassword) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setPassword(encodedPassword);
        user.setEnabled(true);
        return user;
    }

    private Customer createCustomer(String name, String email, User createdBy) {
        Customer customer = new Customer();
        customer.setName(name);
        customer.setEmail(email);
        customer.setCreatedBy(createdBy);
        customer.setUpdatedBy(createdBy);
        customer.setCreatedAt(new Date());
        customer.setUpdatedAt(new Date());
        return customer;
    }
}