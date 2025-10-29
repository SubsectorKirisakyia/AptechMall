package com.aptech.aptechMall.entity;

import com.aptech.aptechMall.entity.converters.OAuthConverter;
import com.aptech.aptechMall.security.Role;
import com.aptech.aptechMall.security.Status;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.*;

/**
 * User entity representing registered users in the system
 * Supports authentication and order management
 */
@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_email", columnList = "email")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Username is required")
    @Size(min = 4, max = 60, message = "Must be at least 4 character long")
    @Column(nullable = false, unique = true, length = 60)
    private String username;

    @Email(message = "Email should be valid")
    @NotBlank(message = "Email is required")
    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    @Column(nullable = false)
    private String password;

    @NotBlank(message = "Full name is required")
    @Size(max = 100, message = "Full name must not exceed 100 characters")
    @Column(nullable = false, length = 100)
    private String fullName;

    @Size(max = 20, message = "Phone must not exceed 20 characters")
    @Column(length = 20)
    private String phone;

    @Size(max = 500, message = "Address must not exceed 500 characters")
    @Column(length = 500)
    private String address;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column(name="last_login")
    private LocalDateTime lastLogin;

    @Convert(converter = OAuthConverter.class)
    @Column(columnDefinition = "json")
    private Map<String, Object> oAuth = new HashMap<>();

    // Relationships will be added:
     @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
     private Cart cart;

     @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
     private List<Order> orders;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<UserAddresses> userAddresses = new HashSet<>();

    @Column(nullable = false, columnDefinition = "ENUM('ADMIN', 'STAFF', 'CUSTOMER') DEFAULT 'CUSTOMER'")
    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(nullable = false, columnDefinition = "ENUM('ACTIVE', 'SUSPENDED', 'DELETED') DEFAULT 'ACTIVE'")
    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(name = "avatar_url", length = 512)
    private String avatarUrl;

    @Column(name = "email_verified", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean emailVerified;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public boolean isAccountNonExpired() {
        return !(status == Status.DELETED);
    }

    @Override
    public boolean isAccountNonLocked() {
        return !(status == Status.SUSPENDED);
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return status == Status.ACTIVE;
    }

    @PrePersist
    protected void onCreate() {
        if (this.status == null) {
            this.status = Status.ACTIVE;
        }
        if (this.role == null) {
            this.role = Role.CUSTOMER;
        }
    }
}
