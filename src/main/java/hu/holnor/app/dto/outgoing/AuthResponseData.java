package hu.holnor.app.dto.outgoing;

import lombok.Data;

@Data
public class AuthResponseData {
    private String accessToken;
    private String tokenType = "Bearer ";

    public AuthResponseData(String accesToken) {
        this.accessToken = accesToken;
    }
}
