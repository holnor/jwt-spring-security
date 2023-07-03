package hu.holnor.app.controller;

import hu.holnor.app.config.JWTGenerator;
import hu.holnor.app.dto.incomming.LoginCommand;
import hu.holnor.app.dto.incomming.RegisterCommand;
import hu.holnor.app.dto.outgoing.AuthResponseData;
import hu.holnor.app.repository.AccountRepository;
import hu.holnor.app.repository.RolesRepository;
import hu.holnor.app.service.AccountService;
import jakarta.persistence.EntityExistsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {
    private AccountService accountService;
    private AccountRepository accountRepository;
    private AuthenticationManager authenticationManager;
    private RolesRepository roleRepository;
    private PasswordEncoder passwordEncoder;
    private JWTGenerator jwtGenerator;

    @Autowired
    public AccountController(AuthenticationManager authenticationManager, AccountRepository accountRepository,
                          RolesRepository roleRepository, PasswordEncoder passwordEncoder, JWTGenerator jwtGenerator) {
        this.authenticationManager = authenticationManager;
        this.accountRepository = accountRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtGenerator = jwtGenerator;
    }

    @PostMapping("login")
    public ResponseEntity<AuthResponseData> login(@RequestBody LoginCommand loginCommand){
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginCommand.getUsername(),
                        loginCommand.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = jwtGenerator.generateToken(authentication);
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
