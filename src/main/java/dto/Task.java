package dto;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.time.LocalDate;


@XmlRootElement(name = "Task")
public class Task {
    String id;
    String title;
    String description;
    int status;
    int priority;
    LocalDate startDate;
    LocalDate endDate;
    String category;
    boolean active = true;


    private static final int low = 100;
    private static final int medium = 200;
    private static final int high = 300;
    private static  final int todo = 10;
    private static final int doing = 20;
    private static final int done = 30;


    public Task() {
        this.status = todo;
        this.priority = low;
        setInitialId();
        this.active = true;
    }

    @XmlElement
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    @XmlElement
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    @XmlElement
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    @XmlElement
    public int getStatus() {
        return status;
    }
    public void setStatus(int status) {
        this.status = status;
    }
    @XmlElement
    public int getPriority() {
        return priority;
    }
    public void setPriority(int priority) {
        this.priority = priority;
    }
    @XmlElement
    public LocalDate getStartDate() {
        return startDate;
    }
    public void setStartDate(LocalDate startDate) {this.startDate = startDate;}
    @XmlElement
    public LocalDate getEndDate() {
        return endDate;
    }
    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }
    public void setInitialId(){
        this.id = "Task" + System.currentTimeMillis();
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
    @XmlElement
    public boolean isActive() {
        return active;
    }
    public void setActive(boolean active) {
        this.active = active;
    }
}
