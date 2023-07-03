package hu.holnor.register.controller;

import hu.holnor.register.dto.income.AccountCommand;
import hu.holnor.register.service.AccountService;
import jakarta.persistence.EntityExistsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
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
    public ResponseEntity<String> register(@RequestBody AccountCommand accountCommand){
        try {
            accountService.register(accountCommand);
            return new ResponseEntity<>("Success", HttpStatus.CREATED);
        } catch (EntityExistsException e) {
            return new ResponseEntity<>("Failed: username or email is already in use", HttpStatus.BAD_REQUEST);
        }

    }
}
