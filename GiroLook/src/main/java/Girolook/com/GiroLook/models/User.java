package Girolook.com.GiroLook.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter

@SQLDelete(sql = "UPDATE users SET active = false WHERE id = ?")

@SQLRestriction("active = true")
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(unique = true)
    private String phoneNumber;


    private String address;

    @Column(name = "created_at", nullable = false, updatable = false)
    @org.hibernate.annotations.CreationTimestamp // O Hibernate gera a data na hora do insert
    private LocalDateTime createdAt;

    @Column(name = "update_at")
    @org.hibernate.annotations.UpdateTimestamp // O Hibernate atualiza a data a cada update
    private LocalDateTime updateAt;


    public User() {

    }

    public User(String name, String email, String phoneNumber,String address) {
        this.name= name;
        this.phoneNumber= phoneNumber;
        this.email= email;
        this.address = address;

    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getUsername() { return email; } // O Spring chama o email de username

    @Override
    public String getPassword() { return password; }

    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }
}
