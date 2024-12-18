package org.spring.authenticationservice.model;

import jakarta.persistence.*;
import lombok.Getter;


import java.util.HashSet;
import java.util.Set;

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

    @ManyToMany(fetch = FetchType.EAGER)
    private Set<Role> roles = new HashSet<>();

    public String getPassword() {
        return password;
    }

    public boolean getIsEnabled() {
        return isEnabled;
    }

    public String getUsername() {
        return email;
    }

}
