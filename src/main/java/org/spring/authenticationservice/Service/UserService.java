package org.spring.authenticationservice.Service;

import org.spring.authenticationservice.model.User;
import org.spring.authenticationservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder encoder;

    public void saveUser(User user) {
        user.setPassword(encoder.encode(user.getPassword()));

        userRepository.save(user);
    }

    public Boolean findUserByUsername(String email){
        return userRepository.existsByEmail(email);
    }


}
