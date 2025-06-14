package com.tcrs.tcrs_backend.security;

import com.tcrs.tcrs_backend.entity.User;
import com.tcrs.tcrs_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    UserRepository userRepository;

   @Override
   public UserDetails loadUserByUsername(String phone) throws UsernameNotFoundException {
       User user = userRepository.findActiveUserByPhone(phone)
               .orElseThrow(() -> new UsernameNotFoundException("User not found with phone: " + phone));

       // Change build to create
       return UserPrincipal.create(user);
   }

    @Transactional
    public UserDetails loadUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + id));

        return UserPrincipal.create(user);
    }
}