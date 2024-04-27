package entities;

import jakarta.persistence.*;

import java.io.Serializable;

@Entity
@Table(name="TimeOut")
@NamedQuery(name="TimeOut.findTimeOutById", query="SELECT t FROM TimeOut t WHERE t.id = :id")
@NamedQuery(name="TimeOut.updateTimeOut", query="UPDATE TimeOut t SET t.timeOut = :timeOut WHERE t.id = :id")



public class TimeOut implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id", nullable = false, unique = true, updatable = false)
    private int id;

    @Column(name="timeOut", nullable = false, unique = true)
    private int timeOut;

    @Column(name="unconfirmedTimeOut", nullable = false, unique = true)
    private int unconfirmedTimeOut;


    public int getTimeOut() {
        return timeOut;
    }

    public void setTimeOut(int timeOut) {
        this.timeOut = timeOut;
    }

    public int getUnconfirmedTimeOut() {
        return unconfirmedTimeOut;
    }

    public void setUnconfirmedTimeOut(int unconfirmedTimeOut) {
        this.unconfirmedTimeOut = unconfirmedTimeOut;
    }
    public int getId() {
        return id;
    }


}
