package es.nextdigital.demo.controller;

import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

import es.nextdigital.demo.repository.AccountRepository;
import es.nextdigital.demo.repository.TransactionRepository;
import es.nextdigital.demo.model.Account;
import es.nextdigital.demo.model.Transaction;
import es.nextdigital.demo.model.TransactionType;

import java.math.BigDecimal;

@RestController
@RequestMapping("/transfer")
@RequiredArgsConstructor
public class TransferController {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    @PostMapping
    public String transfer(@RequestParam String fromIban,
                           @RequestParam String toIban,
                           @RequestParam BigDecimal amount) {

        Account from = accountRepository.findAll()
                .stream().filter(a -> a.getIban().equals(fromIban))
                .findFirst().orElseThrow(() -> new RuntimeException("Origin account not found"));

        Account to = accountRepository.findAll()
                .stream().filter(a -> a.getIban().equals(toIban))
                .findFirst().orElseThrow(() -> new RuntimeException("Destination account not found"));

        if (from.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient balance");
        }

        from.setBalance(from.getBalance().subtract(amount));
        to.setBalance(to.getBalance().add(amount));

        accountRepository.save(from);
        accountRepository.save(to);

        //Create Transaction
        Transaction out = new Transaction();
        out.setAccount(from);
        out.setAmount(amount);
        out.setType(TransactionType.TRANSFER_OUT);
        out.setTargetIban(toIban);
        transactionRepository.save(out);

        Transaction in = new Transaction();
        in.setAccount(to);
        in.setAmount(amount);
        in.setType(TransactionType.TRANSFER_IN);
        in.setTargetIban(fromIban);
        transactionRepository.save(in);

        return "Transfer completed successfully";
    }
}