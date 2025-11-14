package es.nextdigital.demo.service.impl;

import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

import es.nextdigital.demo.repository.AccountRepository;
import es.nextdigital.demo.repository.TransactionRepository;
import es.nextdigital.demo.model.Account;
import es.nextdigital.demo.model.Transaction;
import es.nextdigital.demo.model.TransactionType;
import es.nextdigital.demo.dto.AccountDTO;
import es.nextdigital.demo.dto.TransactionDTO;
import es.nextdigital.demo.mapper.AccountMapper;
import es.nextdigital.demo.mapper.TransactionMapper;
import es.nextdigital.demo.service.AccountService;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    @Override
    public List<TransactionDTO> getTransactions(Long accountId) {
        return transactionRepository.findByAccountId(accountId)
                .stream()
                .map(TransactionMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public AccountDTO deposit(Long accountId, BigDecimal amount, boolean sameBank) {
        if (!sameBank) throw new RuntimeException("Cannot deposit from another bank");
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found"));
        account.setBalance(account.getBalance().add(amount));

        Transaction t = new Transaction();
        t.setAccount(account);
        t.setAmount(amount);
        t.setType(TransactionType.DEPOSIT);
        transactionRepository.save(t);

        accountRepository.save(account);
        return AccountMapper.toDTO(account);
    }

    @Override
    public AccountDTO withdraw(Long accountId, BigDecimal amount, boolean sameBank) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found"));
        if (account.getBalance().compareTo(amount) < 0)
            throw new RuntimeException("Insufficient balance");

        account.setBalance(account.getBalance().subtract(amount));

        Transaction t = new Transaction();
        t.setAccount(account);
        t.setAmount(amount);
        t.setType(TransactionType.WITHDRAW);
        transactionRepository.save(t);

        accountRepository.save(account);
        return AccountMapper.toDTO(account);
    }
}