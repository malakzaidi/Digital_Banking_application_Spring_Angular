# E-Banking Application

A Spring Boot application for managing banking operations including customer accounts, transactions, and account management.

## Overview

The E-Banking Application is a web-based system developed using Spring Boot that provides functionality for banking operations. It supports multiple account types, customer management, and transaction processing.

## Features

- Customer management (creation, retrieval, updating)
- Multiple account types:
  - Current accounts with overdraft protection
  - Savings accounts with interest rates
- Account operations:
  - Deposits (credit)
  - Withdrawals (debit)
- Transaction history tracking
- Account status management (Created, Activated, Suspended)

## Technical Stack

- **Framework**: Spring Boot
- **Database**: H2 in-memory database
- **ORM**: JPA/Hibernate
- **Build Tool**: Maven

## Project Structure

```
org.springmvc.ebanking
├── EbankingApplication.java (Main application class)
├── entities
│   ├── AccountOperation.java
│   ├── BankAccount.java (Abstract base class)
│   ├── CurrentAccount.java
│   ├── Customer.java
│   └── SavingAccount.java
├── enums
│   ├── AccountStatus.java
│   └── OperationType.java
└── repositories
    ├── AccountOperationRepository.java
    ├── BankAccountRepository.java
    └── CustomerRepository.java
```

## Entity Relationships

- **Customer** has many **BankAccount**s (One-to-Many)
- **BankAccount** has many **AccountOperation**s (One-to-Many)
- **BankAccount** is an abstract class extended by:
  - **CurrentAccount** with overdraft capabilities
  - **SavingAccount** with interest rate

## Database Schema

- **Customer**: id, name, email
- **BankAccount**: id, balance, createdAt, status, customer_id
- **CurrentAccount**: extends BankAccount with overDraft
- **SavingAccount**: extends BankAccount with interestRate
- **AccountOperation**: id, operationDate, amount, type, bankAccount_id, description

## JPA Features Used

### Lazy vs. Eager Loading

The application uses both loading strategies:

#### Lazy Loading
- Used in `BankAccount` for loading account operations:
  ```java
  @OneToMany(mappedBy = "bankAccount", fetch = FetchType.LAZY)
  private List<AccountOperation> accountOperations;
  ```
- Benefits: Improves performance by loading related entities only when needed
- Use case: Appropriate for collections that may contain many elements and aren't always needed

#### Eager Loading
- Default for `@ManyToOne` relationships like in `AccountOperation`:
  ```java
  @ManyToOne
  private BankAccount bankAccount;
  ```
- Benefits: Ensures related data is always available
- Use case: Good for mandatory relationships that are always needed when accessing the entity

### Inheritance Strategy

The application uses the JOINED inheritance strategy:

```java
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class BankAccount { ... }
```

#### Comparison of JPA Inheritance Strategies

1. **JOINED Strategy** (currently used)
   - Creates separate tables for parent and each child class
   - Child tables contain only their specific fields
   - Advantages: Normalized design, no data redundancy
   - Disadvantages: Requires joins for queries, potentially slower for deep hierarchies

2. **SINGLE_TABLE Strategy**
   - Places all classes in a single table with a discriminator column
   - Would require:
     ```java
     @Inheritance(strategy = InheritanceType.SINGLE_TABLE)
     @DiscriminatorColumn(name = "TYPE")
     ```
   - Advantages: Fast queries, no joins required
   - Disadvantages: Many nullable columns, potential constraints issues

3. **TABLE_PER_CLASS Strategy**
   - Creates separate tables for each concrete class with all fields
   - Advantages: No nullable columns, direct access to all properties
   - Disadvantages: Data redundancy, less efficient polymorphic queries

The JOINED strategy was chosen for this application as it provides a good balance between database normalization and query performance for the banking domain.

## Setup and Configuration

### Prerequisites

- Java 11 or higher
- Maven 3.6 or higher

### Running the Application

1. Clone the repository:
   ```bash
   git clone [repository-url]
   cd ebanking
   ```

2. Build the project:
   ```bash
   mvn clean install
   ```

3. Run the application:
   ```bash
   mvn spring-boot:run
   ```

4. Access the application:
   - Web interface: http://localhost:8082
   - H2 Console: http://localhost:8082/h2-console
     - JDBC URL: `jdbc:h2:mem:ebank`
     - Username: (default)
     - Password: (default)

## Initial Data

The application initializes with sample data:
- 3 customers: Malak, Nada, and Touria
- Each customer has one Current Account and one Savings Account
- Each account has 10 random operations (deposits and withdrawals)

## API Endpoints

The service provides standard REST endpoints for:

- Customer management
- Account retrieval and management
- Transaction processing and history

(Detailed API documentation to be added)

## Testing

Run tests with:

```bash
mvn test
```

## Future Enhancements

- User authentication and authorization
- Transaction limits and validations
- Email notifications for transactions
- Mobile banking interface
- Administrative dashboard

