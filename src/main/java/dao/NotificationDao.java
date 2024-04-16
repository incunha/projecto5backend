package dao;

import entities.UserEntity;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import entities.NotificationEntity;

import java.util.ArrayList;

@Stateless
public class NotificationDao extends AbstractDao<NotificationEntity> {

    @PersistenceContext
    private EntityManager em;

    public NotificationDao() {
        super(NotificationEntity.class);
    }

    private static final long serialVersionUID = 1L;

    public ArrayList<NotificationEntity> findNotificationByUser(UserEntity sender, UserEntity receiver) {
        try {
            ArrayList<NotificationEntity> notificationEntities = (ArrayList<NotificationEntity>) em.createNamedQuery("Notification.findNotificationByUsers").setParameter("sender", sender).setParameter("receiver", receiver).getResultList();
            return notificationEntities;
        } catch (Exception e) {
            return null;
        }
    }

    public NotificationEntity createNotification(NotificationEntity notificationEntity) {
        em.persist(notificationEntity);
        return notificationEntity;
    }



    public void update(NotificationEntity notificationEntity) {
        em.merge(notificationEntity);
    }

    public ArrayList<NotificationEntity> findNotificationsByReceiver(UserEntity receiver) {
        try {
            ArrayList<NotificationEntity> notificationEntities = (ArrayList<NotificationEntity>) em.createNamedQuery("Notification.findNotificationsByReceiver").setParameter("receiver", receiver).getResultList();
            return notificationEntities;
        } catch (Exception e) {
            return null;
        }
    }

}