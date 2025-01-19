package org.spring.authenticationservice.DTO;

import java.util.ArrayList;
import java.util.List;

public class LoginUserDto {
    private String email;
    private String password;
    private List<String> roles = new ArrayList<>();

    public LoginUserDto(String mail, String password) {
        this.email = mail;
        this.password = password;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}
