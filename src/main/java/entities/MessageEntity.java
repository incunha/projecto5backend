package entities;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name="Messages")
@NamedQuery(name="Message.findMessageByUsers", query="SELECT a FROM MessageEntity a WHERE a.sender = :sender AND a.receiver = :receiver OR a.sender = :receiver AND a.receiver = :sender order by a.timestamp asc")
@NamedQuery(name="Message.findMessagesByUsername", query="SELECT a FROM MessageEntity a WHERE a.sender.username = :username OR a.receiver.username = :username ")
public class MessageEntity implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id", nullable = false, unique = true, updatable = false)
    private int id;

    @JoinColumn (name="sender", nullable = false, unique = false)
    @ManyToOne
    private UserEntity sender;

    @JoinColumn (name="receiver", nullable = false, unique = false)
    @ManyToOne
    private UserEntity receiver;

    @Column (name="message", nullable = false, unique = false, length = 65535, columnDefinition = "TEXT")
    private String message;

    @Column (name="timestamp", nullable = false, unique = false)
    private LocalDateTime timestamp;
    @Column (name="is_read", nullable = false, unique = false)
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

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
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