package org.spring.authenticationservice.Utils;

import org.spring.authenticationservice.model.UserPrinciple;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class SecurityUtil {

    private Authentication getAuthentication(){
        return SecurityContextHolder.getContext().getAuthentication();
    }

    public String getUsername(){
        Authentication authentication = getAuthentication();

        if(authentication != null && authentication.getPrincipal() instanceof UserDetails){
            return ((UserDetails)authentication.getPrincipal()).getUsername();
        }

        //if user is not authenticated
        return null;
    }

    public Long getUserId(){
        Authentication authentication = getAuthentication();

        // Cast to UserPrinciple instead of UserDetails
        if(authentication != null && authentication.getPrincipal() instanceof UserPrinciple){
            return ((UserPrinciple) authentication.getPrincipal()).getUserId();
        }
        return null;
    }

    public List<String> getRoles(){
        Authentication authentication = getAuthentication();

        if(authentication != null && authentication.getPrincipal() instanceof UserPrinciple){
            // Extract roles as strings
            return ((UserPrinciple) authentication.getPrincipal()).getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority) // Get role name
                    .collect(Collectors.toList());
        }

        return null;
    }
}
