package bean;
import dao.UserDao;
import entities.TimeOut;
import entities.UserEntity;
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


    @Schedule(second="*", minute="*/1", hour="*")

    public void automaticTimer(){

        List<UserEntity> users = userDao.findAll();

        for (UserEntity user : users) {
            Session userSession = Notifier.getSessions().get(user.getUsername());
            if (userSession != null && userSession.isOpen()) {
                int timeoutValue = userDao.getTimeOut();

                LocalDateTime lastInteraction = user.getLastInteraction();
                if(lastInteraction != null) {

                    Duration duration = Duration.between(lastInteraction, LocalDateTime.now());

                if (duration.toMinutes() > timeoutValue) {
                    notifier.sendLogoutNotification(user.getUsername());
                  }
                }
            }
        }
    }
}

