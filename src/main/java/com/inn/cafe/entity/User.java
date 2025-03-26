package com.inn.cafe.entity;

import jakarta.persistence.*;
import lombok.*;
/*
To resolve the import for org.bson.types.ObjectId, you need to include the MongoDB Java driver in your project. The ObjectId class is part of the MongoDB driver, which allows you to interact with MongoDB databases in Java.
 */

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "user")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @NonNull
    @Column(name="user_name")
    private String userName;

    private String email;


    @NonNull
    private String password;



    private List<String> roles;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }


    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }


    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }



}