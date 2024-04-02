package dto;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;

@XmlRootElement
public class User {
    String username;

    String name;

    String email;

    String password;
    String contactNumber;
    String role;
    String userPhoto;
    String token;
    boolean active = true;

    public User() {
    }

    public User( String username, String name, String email, String password, String contactNumber, String userPhoto, String role) {

        this.username = username;
        this.name = name;
        this.email = email;
        this.password = password;
        this.contactNumber = contactNumber;
        this.userPhoto = userPhoto;
        if(role == null || role.isEmpty()){
            this.role = "Developer";
        }else {
            this.role = role;
        }
        this.active = true;
    }



    @XmlElement
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @XmlElement
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @XmlElement
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @XmlElement
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @XmlElement
    public String getContactNumber() {
        return contactNumber;
    }
    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }
    public void setUserPhoto(String userPhoto){
        this.userPhoto = userPhoto;
    }
    @XmlElement
    public String getUserPhoto(){
        return userPhoto;
    }
    @XmlElement
    public String getRole() {
        return role;
    }
    public void setRole(String role) {
        this.role = role;
    }


    /*public ArrayList<Task> orderedtasks() {
        ArrayList<Task> status10 = new ArrayList<Task>();
        ArrayList<Task> status20 = new ArrayList<Task>();
        ArrayList<Task> status30 = new ArrayList<Task>();
        for (Task a : tasks) {
            if (a.getStatus() == 10) {
                status10.add(a);
            } else if (a.getStatus() == 20) {
                status20.add(a);
            } else if (a.getStatus() == 30) {
                status30.add(a);
            }
        }
        status10.sort(Comparator.comparing(Task::getPriority,Comparator.reverseOrder()).thenComparing(Comparator.comparing(Task::getStartDate).thenComparing(Task::getEndDate)));
        status20.sort(Comparator.comparing(Task::getPriority,Comparator.reverseOrder()).thenComparing(Comparator.comparing(Task::getStartDate).thenComparing(Task::getEndDate)));
        status30.sort(Comparator.comparing(Task::getPriority,Comparator.reverseOrder()).thenComparing(Comparator.comparing(Task::getStartDate).thenComparing(Task::getEndDate)));
        ArrayList<Task> orderedTasks = new ArrayList<Task>();
        orderedTasks.addAll(status10);
        orderedTasks.addAll(status20);
        orderedTasks.addAll(status30);
        return orderedTasks;
    }*/

    @XmlElement
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
    @XmlElement
    public boolean isActive() {
        return active;
    }
    public void setActive(boolean active) {
        this.active = active;
    }
}
