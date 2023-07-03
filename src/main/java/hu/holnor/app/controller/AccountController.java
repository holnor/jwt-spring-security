package hu.holnor.app.controller;

import hu.holnor.app.dto.incomming.LoginCommand;
import hu.holnor.app.dto.incomming.RegisterCommand;
import hu.holnor.app.dto.outgoing.AuthResponseData;
import hu.holnor.app.service.AccountService;
import jakarta.persistence.EntityExistsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    @PostMapping("login")
    public ResponseEntity<AuthResponseData> login(@RequestBody LoginCommand loginCommand) {
        String token = accountService.login(loginCommand);
        return new ResponseEntity<>(new AuthResponseData(token), HttpStatus.OK);
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
}
