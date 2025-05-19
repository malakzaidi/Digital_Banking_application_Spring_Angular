package org.springmvc.ebanking.mappers;


import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springmvc.ebanking.dtos.*;
import org.springmvc.ebanking.entities.*;

@Service
    public class BankAccountMapperImpl {
        public CustomerDTO fromCustomer(Customer customer){
            CustomerDTO customerDTO=new CustomerDTO();
            BeanUtils.copyProperties(customer,customerDTO);
            return  customerDTO;
        }
        public Customer fromCustomerDTO(CustomerDTO customerDTO){
            Customer customer=new Customer();
            BeanUtils.copyProperties(customerDTO,customer);
            return  customer;
        }

        public SavingBankAccountDTO fromSavingBankAccount(SavingAccount savingAccount){
            SavingBankAccountDTO savingBankAccountDTO=new SavingBankAccountDTO();
            BeanUtils.copyProperties(savingAccount,savingBankAccountDTO);
            savingBankAccountDTO.setCustomerDTO(fromCustomer(savingAccount.getCustomer()));
            savingBankAccountDTO.setType(savingAccount.getClass().getSimpleName());
            return savingBankAccountDTO;
        }

        public SavingAccount fromSavingBankAccountDTO(SavingBankAccountDTO savingBankAccountDTO){
            SavingAccount savingAccount=new SavingAccount();
            BeanUtils.copyProperties(savingBankAccountDTO,savingAccount);
            savingAccount.setCustomer(fromCustomerDTO(savingBankAccountDTO.getCustomerDTO()));
            return savingAccount;
        }

        public CurrentBankAccountDTO fromCurrentBankAccount(CurrentAccount currentAccount){
            CurrentBankAccountDTO currentBankAccountDTO=new CurrentBankAccountDTO();
            BeanUtils.copyProperties(currentAccount,currentBankAccountDTO);
            currentBankAccountDTO.setCustomerDTO(fromCustomer(currentAccount.getCustomer()));
            currentBankAccountDTO.setType(currentAccount.getClass().getSimpleName());
            return currentBankAccountDTO;
        }

        public CurrentAccount fromCurrentBankAccountDTO(CurrentBankAccountDTO currentBankAccountDTO){
            CurrentAccount currentAccount=new CurrentAccount();
            BeanUtils.copyProperties(currentBankAccountDTO,currentAccount);
            currentAccount.setCustomer(fromCustomerDTO(currentBankAccountDTO.getCustomerDTO()));
            return currentAccount;
        }

        public AccountOperationDTO fromAccountOperation(AccountOperation accountOperation){
            AccountOperationDTO accountOperationDTO=new AccountOperationDTO();
            BeanUtils.copyProperties(accountOperation,accountOperationDTO);
            return accountOperationDTO;
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
        dto.setStatus(bankAccount.getStatus());
        dto.setType("Unknown");
        if (bankAccount.getCustomer() != null) {
            CustomerDTO customerDTO = fromCustomer(bankAccount.getCustomer());
            dto.setCustomerDTO(customerDTO);
        }
        if (bankAccount.getCreatedBy() != null) {
            dto.setCreatedBy(bankAccount.getCreatedBy().getId());
        }
        if (bankAccount.getUpdatedBy() != null) {
            dto.setUpdatedBy(bankAccount.getUpdatedBy().getId());
        }
        dto.setUpdatedAt(bankAccount.getUpdatedAt());

        return dto;
    }
}

