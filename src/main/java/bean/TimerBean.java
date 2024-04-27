package bean;
import dao.UserDao;
import entities.TimeOut;
import entities.UserEntity;
import jakarta.ejb.EJB;
import jakarta.websocket.Session;
import websocket.Notifier;
import jakarta.ejb.Schedule;
import jakarta.ejb.Singleton;
import jakarta.inject.Inject;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Singleton
public class TimerBean {
    @Inject
   Notifier notifier;
    @Inject
    UserDao userDao;
    @EJB
    UserBean userBean;


    @Schedule(second="*", minute="*/1", hour="*")

    public void automaticTimer(){


        List<UserEntity> users = userDao.findAll();

        for (UserEntity user : users) {
            if(user.getConfirmationToken() != null){
                TimeOut timeOut = userDao.findTimeOut(1);
                int unconfirmedTimeoutValue = timeOut.getUnconfirmedTimeOut();
                LocalDateTime lastInteraction = user.getLastInteraction();
                if(lastInteraction != null) {
                    Duration duration = Duration.between(lastInteraction, LocalDateTime.now());
                    if (duration.toDays() > unconfirmedTimeoutValue) {
                        userDao.remove(user);
                    }
                }
            }else {
                Session userSession = Notifier.getSessions().get(user.getUsername());
                if (userSession != null && userSession.isOpen()) {
                    int timeoutValue = userDao.findTimeOut(1).getTimeOut();

                    LocalDateTime lastInteraction = user.getLastInteraction();
                    if (lastInteraction != null) {

                        Duration duration = Duration.between(lastInteraction, LocalDateTime.now());

                        if (duration.toMinutes() > timeoutValue) {
                            notifier.sendLogoutNotification(user.getUsername());
                        }
                    }
                }
            }
    }
    }
}

