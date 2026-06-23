package com.example.BE.model;

import com.example.BE.enums.Role;
import com.example.BE.audit.BaseAuditable;
import jakarta.persistence.*;
import lombok.*;


@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name ="users")
public class UserModel extends BaseAuditable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String email;
    private String username;
    private String password;
    @Enumerated(EnumType.STRING)
    private Role role;
    private String phone;
    private String address;
    private String avatarUrl;
    private boolean active;
}
