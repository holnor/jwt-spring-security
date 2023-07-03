package hu.holnor.register.service;

import hu.holnor.register.config.Role;
import hu.holnor.register.domain.Account;
import hu.holnor.register.dto.income.AccountCommand;
import hu.holnor.register.repository.AccountRepository;
import hu.holnor.register.repository.RolesRepository;
import jakarta.persistence.EntityExistsException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@Transactional
public class AccountService {
    private AccountRepository accountRepository;
    private RolesRepository rolesRepository;
    private PasswordEncoder passwordEncoder;

    @Autowired
    public AccountService(AccountRepository accountRepository, RolesRepository rolesRepository,
                          PasswordEncoder passwordEncoder) {
        this.accountRepository = accountRepository;
        this.rolesRepository = rolesRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void register(AccountCommand accountCommand) throws EntityExistsException {
        if (accountRepository.existsByEmail(accountCommand.getEmail()) || accountRepository.existsByUsername(accountCommand.getUsername())) {
            throw new EntityExistsException();
        } else {
            Account account = new Account();
            account.setUsername(accountCommand.getUsername());
            account.setEmail(accountCommand.getEmail());
            account.setPassword(passwordEncoder.encode(accountCommand.getPassword()));

            Role roles = rolesRepository.findByName("USER").get();
            account.setRoles(Collections.singletonList(roles));
            accountRepository.save(account);
        }
    }
}