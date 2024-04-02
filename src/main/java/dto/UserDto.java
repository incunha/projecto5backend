package dto;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class UserDto {
    private String Name;
    private String email;
    private String ContactNumber;
    private String role;
    private String userPhoto;

    private String username;

    public UserDto() {
    }
    @XmlElement
    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }
    @XmlElement
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
    @XmlElement
    public String getContactNumber() {
        return ContactNumber;
    }

    public void setContactNumber(String contactNumber) {
        ContactNumber = contactNumber;
    }
    @XmlElement
    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
    @XmlElement
    public String getUserPhoto() {
        return userPhoto;
    }

    public void setUserPhoto(String userPhoto) {
        this.userPhoto = userPhoto;
    }
    @XmlElement
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }

}
