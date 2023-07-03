package hu.holnor.register.dto.income;

import lombok.Data;

@Data
public class AccountCommand {
    private String username;
    private String password;
    private String email;
}
