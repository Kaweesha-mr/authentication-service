package org.spring.authenticationservice.model;

import jakarta.persistence.*;


@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private boolean isEnabled;



    // Default constructor
    public User() {

    }


    public String getPassword() {
        return password;
    }

    public String getEmail() {
        return email;
    }

    public boolean getIsEnabled() {
        return isEnabled;
    }

    public String getUsername() {
        return email;
    }

    public void setPassword(String password){
        this.password = password;
    }



}
