package hu.holnor.app.dto.incomming;

import lombok.Data;

@Data
public class LoginCommand {
    private String username;
    private String password;

}
