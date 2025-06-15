package com.esms.model.entity;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "admins")
public class Admin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "admin_id", nullable = false)
    private Integer admin_id;

    @Column(name = "name", length = 100)
    private String name;

    @Column(name = "email", length = 255, nullable = false)
    private String email;

    @Column(name = "password", length = 255)
    private String password;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "address", length = 255)
    private String address;

    @Column(name = "gender", length = 10)
    private String gender;

    @Column(name = "national_id", length = 20)
    private String national_id;

    @Column(name = "start_date", nullable = false)
    private LocalDate start_date;

    @Column(name = "privileges", length = 100)
    private String privileges;

    @Column(name = "created_at", nullable = false)
    private LocalDate created_at;

    @Column(name = "updated_at", nullable = false)
    private LocalDate updated_at;


}
