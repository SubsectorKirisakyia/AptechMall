package com.aptech.aptechMall.service.authentication;

import com.aptech.aptechMall.entity.User;
import com.aptech.aptechMall.repository.UserRepository;
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
        User user = usernameExists ? userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng: " + username)) :
                userRepository.findByEmail(username)
                        .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng: " + username));
        String roleName = user.getRole().name();
        if (!roleName.startsWith("ROLE_")) {
            roleName = "ROLE_" + roleName;
        }

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .authorities(new SimpleGrantedAuthority(roleName))
                .accountLocked(user.getStatus().name().equals("SUSPENDED"))
                .accountExpired(user.getStatus().name().equals("DELETED"))
                .disabled(!user.getStatus().name().equals("ACTIVE"))
                .build();
    }
}
