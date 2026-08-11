package kr.co.gymleco.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest (
    @NotBlank @Size(max = 60) String username,
    @NotBlank @Size(max = 200) String password,
    @Size(max = 10)  String totpCode
){
    @Override
    public String toString(){
        return "LoginRequest[username=" + username + ", password=***]";
    }
}
