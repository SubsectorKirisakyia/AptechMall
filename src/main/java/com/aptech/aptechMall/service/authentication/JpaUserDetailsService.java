package com.aptech.aptechMall.service.authentication;

import com.aptech.aptechMall.entity.User;
import com.aptech.aptechMall.repository.UserRepository;
import com.aptech.aptechMall.security.Status;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JpaUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        boolean usernameExists = userRepository.existsByUsername(username);
        return usernameExists ? userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found for: " + username)) :
                userRepository.findByEmail(username)
                        .orElseThrow(() -> new UsernameNotFoundException("User not found for: " + username));
    }
}
