package org.springmvc.ebanking.mappers;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springmvc.ebanking.dtos.*;
import org.springmvc.ebanking.entities.*;

@Service
public class BankAccountMapperImpl {
    public CustomerDTO fromCustomer(Customer customer) {
        if (customer == null) {
            return null;
        }
        CustomerDTO customerDTO = new CustomerDTO();
        customerDTO.setId(customer.getId());
        customerDTO.setName(customer.getName());
        customerDTO.setEmail(customer.getEmail());
        customerDTO.setCreatedBy(customer.getCreatedBy() != null ? customer.getCreatedBy().getUsername() : "Unknown");
        customerDTO.setUpdatedBy(customer.getUpdatedBy() != null ? customer.getUpdatedBy().getUsername() : "Unknown");
        return customerDTO;
    }

    public Customer fromCustomerDTO(CustomerDTO customerDTO) {
        if (customerDTO == null) {
            return null;
        }
        Customer customer = new Customer();
        customer.setId(customerDTO.getId());
        customer.setName(customerDTO.getName());
        customer.setEmail(customerDTO.getEmail());
        return customer;
    }

    public SavingBankAccountDTO fromSavingBankAccount(SavingAccount savingAccount) {
        SavingBankAccountDTO savingBankAccountDTO = new SavingBankAccountDTO();
        BeanUtils.copyProperties(savingAccount, savingBankAccountDTO);
        if (savingAccount.getCustomer() != null) {
            savingBankAccountDTO.setCustomerId(savingAccount.getCustomer().getId());
            savingBankAccountDTO.setCustomerName(savingAccount.getCustomer().getName());
        }
        if (savingAccount.getCreatedBy() != null) {
            savingBankAccountDTO.setCreatedBy(savingAccount.getCreatedBy().getUsername()); // Use username
        }
        if (savingAccount.getUpdatedBy() != null) {
            savingBankAccountDTO.setUpdatedBy(savingAccount.getUpdatedBy().getUsername()); // Use username
        }
        savingBankAccountDTO.setType(savingAccount.getClass().getSimpleName());
        return savingBankAccountDTO;
    }

    public SavingAccount fromSavingBankAccountDTO(SavingBankAccountDTO savingBankAccountDTO) {
        SavingAccount savingAccount = new SavingAccount();
        BeanUtils.copyProperties(savingBankAccountDTO, savingAccount);
        savingAccount.setCustomer(fromCustomerDTO(savingBankAccountDTO.getCustomerDTO()));
        return savingAccount;
    }

    public CurrentBankAccountDTO fromCurrentBankAccount(CurrentAccount currentAccount) {
        CurrentBankAccountDTO currentBankAccountDTO = new CurrentBankAccountDTO();
        BeanUtils.copyProperties(currentAccount, currentBankAccountDTO);
        if (currentAccount.getCustomer() != null) {
            currentBankAccountDTO.setCustomerId(currentAccount.getCustomer().getId());
            currentBankAccountDTO.setCustomerName(currentAccount.getCustomer().getName());
        }
        if (currentAccount.getCreatedBy() != null) {
            currentBankAccountDTO.setCreatedBy(currentAccount.getCreatedBy().getUsername()); // Use username
        }
        if (currentAccount.getUpdatedBy() != null) {
            currentBankAccountDTO.setUpdatedBy(currentAccount.getUpdatedBy().getUsername()); // Use username
        }
        currentBankAccountDTO.setType(currentAccount.getClass().getSimpleName());
        return currentBankAccountDTO;
    }

    public CurrentAccount fromCurrentBankAccountDTO(CurrentBankAccountDTO currentBankAccountDTO) {
        CurrentAccount currentAccount = new CurrentAccount();
        BeanUtils.copyProperties(currentBankAccountDTO, currentAccount);
        currentAccount.setCustomer(fromCustomerDTO(currentBankAccountDTO.getCustomerDTO()));
        return currentAccount;
    }


    public BankAccountDTO fromBankAccount(BankAccount bankAccount) {
        if (bankAccount == null) {
            return null;
        }

        if (bankAccount instanceof CurrentAccount) {
            return fromCurrentBankAccount((CurrentAccount) bankAccount);
        } else if (bankAccount instanceof SavingAccount) {
            return fromSavingBankAccount((SavingAccount) bankAccount);
        }

        // Fallback for generic BankAccount mapping
        BankAccountDTO dto = new BankAccountDTO();
        dto.setId(bankAccount.getId());
        dto.setBalance(bankAccount.getBalance());
        dto.setCreatedAt(bankAccount.getCreatedAt());
        dto.setType("Unknown");
        if (bankAccount.getCustomer() != null) {
            Customer customer = bankAccount.getCustomer();
            dto.setCustomerId(customer.getId());
            dto.setCustomerName(customer.getName());
        }
        if (bankAccount.getCreatedBy() != null) {
            dto.setCreatedBy(bankAccount.getCreatedBy().getUsername()); // Use username
        }
        if (bankAccount.getUpdatedBy() != null) {
            dto.setUpdatedBy(bankAccount.getUpdatedBy().getUsername()); // Use username
        }
        dto.setUpdatedAt(bankAccount.getUpdatedAt());

        return dto;
    }
    public AccountOperationDTO fromAccountOperation(AccountOperation accountOperation) {
        if (accountOperation == null) {
            return null;
        }
        AccountOperationDTO dto = new AccountOperationDTO();
        dto.setId(accountOperation.getId());
        dto.setOperationDate(accountOperation.getOperationDate());
        dto.setAmount(accountOperation.getAmount());
        dto.setDescription(accountOperation.getDescription());
        dto.setType(accountOperation.getType());
        dto.setAccountId(accountOperation.getBankAccount() != null ? accountOperation.getBankAccount().getId() : null);
        dto.setPerformedBy(accountOperation.getPerformedBy() != null ? accountOperation.getPerformedBy().getUsername() : "Unknown");
        return dto;
    }

}