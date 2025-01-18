package org.spring.authenticationservice.DTO;

import java.util.ArrayList;
import java.util.List;

public class RegisterUserDto {
    private String email;
    private String password;
    private List<String> roles = new ArrayList<>();

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public String getEmail() {
        return email;
    }
    public void setEmail(String username) {
        this.email = username;
    }

    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
}
