package hu.holnor.app.service;

import hu.holnor.app.config.Role;
import hu.holnor.app.domain.Account;
import hu.holnor.app.dto.income.LoginCommand;
import hu.holnor.app.dto.income.RegisterCommand;
import hu.holnor.app.repository.AccountRepository;
import hu.holnor.app.repository.RolesRepository;
import jakarta.persistence.EntityExistsException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@Transactional
public class AccountService {
    private AccountRepository accountRepository;
    private RolesRepository rolesRepository;
    private PasswordEncoder passwordEncoder;
    private AuthenticationManager authenticationManager;


    @Autowired
    public AccountService(AccountRepository accountRepository, RolesRepository rolesRepository,
                          PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager) {
        this.accountRepository = accountRepository;
        this.rolesRepository = rolesRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
    }

    public void register(RegisterCommand registerCommand) throws EntityExistsException {
        if (accountRepository.existsByEmail(registerCommand.getEmail()) || accountRepository.existsByUsername(registerCommand.getUsername())) {
            throw new EntityExistsException();
        } else {
            Account account = new Account();
            account.setUsername(registerCommand.getUsername());
            account.setEmail(registerCommand.getEmail());
            account.setPassword(passwordEncoder.encode(registerCommand.getPassword()));

            Role roles = rolesRepository.findByName("USER").get();
            account.setRoles(Collections.singletonList(roles));
            accountRepository.save(account);
        }
    }

    public void login(LoginCommand loginCommand){
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginCommand.getUsername(), loginCommand.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}