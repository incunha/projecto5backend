package bean;

import dao.MessageDao;
import dto.MessageDto;
import entities.MessageEntity;
import entities.UserEntity;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Stateless
public class MessageBean {

    @EJB
    private MessageDao messageDao;

    // Método para enviar uma nova mensagem
    public void sendMessage(UserEntity sender, UserEntity receiver, String messageContent) {
        MessageEntity messageEntity = new MessageEntity();
        messageEntity.setSender(sender);
        messageEntity.setReceiver(receiver);
        messageEntity.setMessage(messageContent);
        messageEntity.setTimestamp(LocalDateTime.now());
        messageEntity.setRead(false);

        messageDao.createMessage(messageEntity); // Persiste a mensagem no banco de dados
    }

    // Método para recuperar todas as mensagens trocadas entre dois usuários
    public List<MessageDto> getMessagesBetweenUsers(UserEntity user1, UserEntity user2) {
        List<MessageEntity> messageEntities = messageDao.findMessageByUser(user1, user2);
        List<MessageDto> messageDtos = new ArrayList<>();

        // Converte as entidades de mensagem em DTOs de mensagem
        for (MessageEntity messageEntity : messageEntities) {
            MessageDto messageDto = new MessageDto();
            messageDto.setMessage(messageEntity.getMessage());
            messageDto.setSender(messageEntity.getSender().getUsername());
            messageDto.setReceiver(messageEntity.getReceiver().getUsername());
            messageDto.setSendDate(messageEntity.getTimestamp());
            messageDto.setRead(messageEntity.isRead());
            messageDtos.add(messageDto);
        }

        return messageDtos;
    }

    // Método para marcar uma mensagem como lida
    public void markMessageAsRead(MessageEntity message) {
        message.setRead(true);
        messageDao.update(message); // Atualiza a mensagem no banco de dados
    }

}