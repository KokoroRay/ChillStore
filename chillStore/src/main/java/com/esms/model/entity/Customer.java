package com.esms.model.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "customers")
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer customerId;

    private String provider;
    @Column(name = "provider_id")
    private String providerId;

    private String name;
    private String display_name;
    private String email;
    private String password;
    private String phone;
    private String address;
    private String gender;
    private LocalDate birth_date;
    private LocalDateTime created_at = LocalDateTime.now();
    private LocalDateTime updated_at = LocalDateTime.now();
    private boolean isLocked = false;
    private String avatar_url;


    public Customer() {
        this.created_at = LocalDateTime.now();
        this.updated_at = LocalDateTime.now();
        this.isLocked = false;
    }

    public Customer(String email, String name, String provider, String providerId) {
        this();
        this.email = email;
        this.name = name;
        this.display_name = name;
        this.provider = provider;
        this.providerId = providerId;
    }

    public  Customer (String name, String email, String password, String phone, String address, String gender, LocalDate birth_date) {
        this.name = name;
        this.display_name = name;
        this.email = email;
        this.password = password;
        this.phone = phone;
        this.address = address;
        this.gender = gender;
        this.birth_date = birth_date;
        this.provider = "local";
    }

    public Integer getCustomerId() { return customerId; }
    public void setCustomerId(Integer customerId) { this.customerId = customerId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDisplay_name() { return display_name; }
    public void setDisplay_name(String display_name) { this.display_name = display_name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public LocalDate getBirth_date() { return birth_date; }
    public void setBirth_date(LocalDate birth_date) { this.birth_date = birth_date; }

    public LocalDateTime getCreated_at() { return created_at; }
    public void setCreated_at(LocalDateTime created_at) { this.created_at = created_at; }

    public LocalDateTime getUpdated_at() { return updated_at; }
    public void setUpdated_at(LocalDateTime updated_at) { this.updated_at = updated_at; }

    public boolean isLocked() { return isLocked; }
    public void setLocked(boolean locked) { isLocked = locked; }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getProviderId() {
        return providerId;
    }

    public void setProviderId(String providerId) {
        this.providerId = providerId;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getAvatar_url() {
        return avatar_url;
    }

    public void setAvatar_url(String avatar_url) {
        this.avatar_url = avatar_url;
    }
}