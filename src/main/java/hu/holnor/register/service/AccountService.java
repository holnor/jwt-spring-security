package hu.holnor.register.service;

import hu.holnor.register.domain.Account;
import hu.holnor.register.dto.income.AccountCommand;
import hu.holnor.register.repository.AccountRepository;
import jakarta.persistence.EntityExistsException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class AccountService {
    private AccountRepository accountRepository;
    private PasswordEncoder passwordEncoder;

    @Autowired
    public AccountService(AccountRepository accountRepository, PasswordEncoder passwordEncoder) {
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void registerAccount(AccountCommand accountCommand) throws EntityExistsException {
        String username = accountCommand.getUsername();
        String email = accountCommand.getEmail();
        String password = accountCommand.getPassword();

        if (findAccountByEmail(email) == null && findAccountByUsername(username) == null) {
            Account account = new Account(username, email, password);
            account.setPassword(passwordEncoder.encode(password));
            accountRepository.save(account);
        } else {
            throw new EntityExistsException();
        }
    }

    private Account findAccountByUsername(String username) {
        return accountRepository.findAccountByUsername(username).orElse(null);
    }

    private Account findAccountByEmail(String email) {
        return accountRepository.findAccountByEmail(email).orElse(null);
    }
}
