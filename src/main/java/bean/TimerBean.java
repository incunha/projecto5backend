package bean;
import dao.UserDao;
import entities.TimeOut;
import entities.UserEntity;
import jakarta.ejb.EJB;
import jakarta.persistence.GeneratedValue;
import jakarta.websocket.Session;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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

    private static final Logger LOGGER = LogManager.getLogger(TimerBean.class);

    @Schedule(second="*", minute="*/1", hour="*")


    public void automaticTimer(){

        List<UserEntity> users = userDao.findAll();

        for (UserEntity user : users) {
            if(user.getConfirmationToken() != null){
                LOGGER.info("User is not confirmed");
                TimeOut timeOut = userDao.findTimeOut(1);
                int unconfirmedTimeoutValue = timeOut.getUnconfirmedTimeOut();
                LocalDateTime lastInteraction = user.getLastInteraction();
                if(lastInteraction != null) {
                    Duration duration = Duration.between(lastInteraction, LocalDateTime.now());
                    if (duration.toDays() > unconfirmedTimeoutValue) {
                        LOGGER.info("User is not confirmed and has been removed");
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
                            LOGGER.info("User has been logged out due to inactivity");
                            notifier.sendLogoutNotification(user.getUsername());
                            userBean.logout(user.getUsername());
                        }
                    }
                }

            }
    }

    }
}

