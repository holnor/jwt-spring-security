package hu.holnor.app.controller;

import hu.holnor.app.dto.income.LoginCommand;
import hu.holnor.app.dto.income.RegisterCommand;
import hu.holnor.app.service.AccountService;
import jakarta.persistence.EntityExistsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {
    private AccountService accountService;

    @Autowired
    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterCommand registerCommand) {
        try {
            accountService.register(registerCommand);
            return new ResponseEntity<>("Success", HttpStatus.CREATED);
        } catch (EntityExistsException e) {
            return new ResponseEntity<>("Failed: username or email is already in use", HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginCommand loginCommand) {
        try {
            accountService.login(loginCommand);
            return new ResponseEntity<>("Login succes", HttpStatus.OK);
        } catch (AuthenticationException e) {
            return new ResponseEntity<>("Login failed", HttpStatus.UNAUTHORIZED);
        }
    }
}
