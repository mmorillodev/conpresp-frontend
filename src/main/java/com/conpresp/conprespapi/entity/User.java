package com.conpresp.conprespapi.entity;

import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@RequiredArgsConstructor
@Entity
public class User implements UserDetails {

    @Id
    private String id = UUID.randomUUID().toString();

    private Profile profile;

    @NonNull
    private String name;

    @NonNull
    private String email;

    @NonNull
    private String password;

    @Enumerated(EnumType.STRING)
    private UserStatus status = UserStatus.ACTIVE;

    private LocalDateTime created_at = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name = "owner_id")
    private User created_by;

    private LocalDateTime updated_at = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User updated_by;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(profile);
    }

    @Override
    public String getUsername() {
        return getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return false;
    }

    @Override
    public boolean isAccountNonLocked() {
        return false;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return false;
    }

    @Override
    public boolean isEnabled() {
        return status.equals(UserStatus.ACTIVE);
    }
}
