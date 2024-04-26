package entities;

import jakarta.persistence.*;

import java.io.Serializable;

@Entity
@Table(name="TimeOut")
@NamedQuery(name="TimeOut.findTimeOut", query="SELECT a FROM TimeOut a")
@NamedQuery(name="TimeOut.updateTimeOut", query="UPDATE TimeOut a SET a.timeOut = :timeOut")



public class TimeOut implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id", nullable = false, unique = true, updatable = false)
    private int id;

    @Column(name="timeOut", nullable = false, unique = true)

    private int timeOut;


    public int getTimeOut() {
        return timeOut;
    }

    public void setTimeOut(int timeOut) {
        this.timeOut = timeOut;
    }

}
