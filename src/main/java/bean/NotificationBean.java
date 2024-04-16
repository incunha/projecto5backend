package bean;

import dao.NotificationDao;
import dto.NotificationDto;
import entities.NotificationEntity;
import entities.UserEntity;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Stateless
public class NotificationBean {

    @EJB
    private NotificationDao notificationDao;
    @EJB MessageBean messageBean;

    private static final Logger LOGGER = Logger.getLogger(NotificationBean.class.getName());

    public void sendNotification(UserEntity sender, UserEntity receiver, String notificationContent) {
        LOGGER.info("sendNotification method called");

        try {
            NotificationEntity notificationEntity = new NotificationEntity();
            notificationEntity.setSender(sender);
            notificationEntity.setReceiver(receiver);
            notificationEntity.setNotification(notificationContent);
            notificationEntity.setTimestamp(LocalDateTime.now());
            notificationEntity.setRead(false);

            notificationDao.createNotification(notificationEntity); // Persiste a notificação no banco de dados
            LOGGER.info("Notification sent and saved in the database.");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error while sending and saving the notification: ", e);
        }
    }

    public void markNotificationAsRead(NotificationEntity notification) {
        notification.setRead(true);
        notificationDao.update(notification);
    }

    public NotificationDto convertToDto(NotificationEntity entity) {
        NotificationDto dto = new NotificationDto();
        dto.setNotification(entity.getNotification());
        dto.setSender(entity.getSender().getUsername());
        dto.setReceiver(entity.getReceiver().getUsername());
        dto.setTimestamp(entity.getTimestamp());
        dto.setRead(entity.isRead());
        return dto;
    }

    public NotificationEntity convertToEntity(NotificationDto dto) {
        NotificationEntity entity = new NotificationEntity();
        entity.setNotification(dto.getNotification());
        entity.setRead(dto.isRead());
        return entity;
    }

    public List<NotificationDto> getNotificationsByUser(UserEntity user) {
        List<NotificationDto> notificationDtos = new ArrayList<>();
        List<NotificationEntity> notificationEntities = notificationDao.findNotificationsByReceiver(user);
        for (NotificationEntity entity : notificationEntities) {
            notificationDtos.add(convertToDto(entity));
        }
        return notificationDtos;
    }

    public void markAllNotificationsAsRead(UserEntity user) {
        List<NotificationEntity> notifications = notificationDao.findNotificationsByReceiver(user);
        for (NotificationEntity notification : notifications) {
            notification.setRead(true);
            notificationDao.update(notification);
        }
    }

}