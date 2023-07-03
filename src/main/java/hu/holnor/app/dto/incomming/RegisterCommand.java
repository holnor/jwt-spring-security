package hu.holnor.app.dto.incomming;

import lombok.Data;

@Data
public class RegisterCommand {
    private String username;
    private String password;
    private String email;
}
