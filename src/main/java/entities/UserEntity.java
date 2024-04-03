package entities;

import java.io.Serializable;

import jakarta.persistence.*;
@Entity
@Table(name="Users")
@NamedQuery(name = "User.findUserByRole", query = "SELECT u FROM UserEntity u WHERE u.active = :active AND u.role = :role")
@NamedQuery(name = "User.findDeletedUsers", query = "SELECT u FROM UserEntity u WHERE u.active = false")
@NamedQuery(name = "User.findActiveUsers", query = "SELECT u FROM UserEntity u WHERE u.active = true")
@NamedQuery(name = "User.findUserByToken", query = "SELECT DISTINCT u FROM UserEntity u WHERE u.token = :token")
@NamedQuery(name = "User.findUserByUsername", query = "SELECT u FROM UserEntity u WHERE u.username = :username")
@NamedQuery(name = "User.findAllUsers", query = "SELECT u FROM UserEntity u")
@NamedQuery(name = "User.updateToken", query = "UPDATE UserEntity u SET u.token = :token WHERE u.username = :username")
public class UserEntity implements Serializable{
    @Id
    @Column (name="id", nullable = false, unique = true, updatable = false)
    String username;
    @Column (name="name", nullable = false, unique = false)
    String name;
    @Column (name="email", nullable = false, unique = true)
    String email;
    @Column (name="password", nullable = false, unique = false)
    String password;
    @Column (name="contactNumber", nullable = false, unique = false)
    String contactNumber;
    @Column (name="userPhoto", nullable = true, unique = false)
    String userPhoto;
    @Column (name="token", nullable = true, unique = true)
    String token;
    @Column (name="role", nullable = true, unique = false)
    String role;
    @Column (name="active", nullable = false, unique = false)
    boolean active;
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public String getUserPhoto() {
        return userPhoto;
    }

    public void setUserPhoto(String userPhoto) {
        this.userPhoto = userPhoto;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
    public String getRole() {
        return role;
    }
    public void setRole(String role) {
        this.role = role;
    }
    public boolean isActive() {
        return active;
    }
    public void setActive(boolean active) {
        this.active = active;
    }
}

