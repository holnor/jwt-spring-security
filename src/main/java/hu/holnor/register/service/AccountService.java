package hu.holnor.register.service;

import hu.holnor.register.domain.Account;
import hu.holnor.register.dto.income.AccountCommand;
import hu.holnor.register.repository.AccountRepository;
import jakarta.persistence.EntityExistsException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class AccountService {
    private final AccountRepository accountRepository;

    @Autowired
    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }
}
