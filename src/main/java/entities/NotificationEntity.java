package entities;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name="Notifications")
@NamedQuery(name="Notification.findNotificationsByReceiver", query="SELECT a FROM NotificationEntity a WHERE a.receiver = :receiver order by a.timestamp desc")
@NamedQuery(name="Notification.totalUnreadNotifications", query="SELECT COUNT (a) FROM NotificationEntity a WHERE a.receiver = :receiver and a.read = false")
public class NotificationEntity implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private int id;

    @ManyToOne
    @JoinColumn(name = "sender", nullable = false)
    private UserEntity sender;

    @ManyToOne
    @JoinColumn(name = "receiver", nullable = false)
    private UserEntity receiver;

    @Column(name = "notification", nullable = false, length = 65535, columnDefinition = "TEXT")
    private String notification;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "is_read", nullable = false)
    private boolean read;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public UserEntity getSender() {
        return sender;
    }

    public void setSender(UserEntity sender) {
        this.sender = sender;
    }

    public UserEntity getReceiver() {
        return receiver;
    }

    public void setReceiver(UserEntity receiver) {
        this.receiver = receiver;
    }

    public String getNotification() {
        return notification;
    }

    public void setNotification(String notification) {
        this.notification = notification;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }
}

