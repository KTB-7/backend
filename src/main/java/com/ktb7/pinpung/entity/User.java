package com.ktb7.pinpung.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "User")
@Getter
@Setter
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long userId;

    @Column(name = "userEmail")
    private String userEmail;

    @Column(name = "userName")
    private String userName;

    @Column(name = "socialId")
    private Long socialId;

    @Column(name = "profileImageId")
    private Long profileImageId;

    @Column(name = "age", nullable = false, columnDefinition = "INTEGER DEFAULT 0")
    private Integer age;

    @CreationTimestamp
    @Column(name = "createdAt", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updatedAt")
    private LocalDateTime updatedAt;
}
