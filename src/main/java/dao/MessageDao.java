package dao;
import entities.UserEntity;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import entities.MessageEntity;
import java.util.ArrayList;



@Stateless
public class MessageDao extends AbstractDao<MessageEntity>{

    @PersistenceContext
    private EntityManager em;
    public MessageDao() {
        super(MessageEntity.class);
    }

    private static final long serialVersionUID = 1L;

    public ArrayList<MessageEntity> findMessageByUser(UserEntity sender, UserEntity receiver) {
        try {
            ArrayList<MessageEntity> messageEntityEntities = (ArrayList<MessageEntity>) em.createNamedQuery("Message.findMessageByUsers").setParameter("sender", sender).setParameter("receiver", receiver).getResultList();
            return messageEntityEntities;
        } catch (Exception e) {
            return null;
        }
    }

    public MessageEntity createMessage(MessageEntity messageEntity) {
        em.persist(messageEntity);
        return messageEntity;
    }

    public MessageEntity findMessageById(int id) {
        try {
            return (MessageEntity) em.createNamedQuery("Message.findMessageById").setParameter("id", id)
                    .getSingleResult();

        } catch (NoResultException e) {
            return null;
        }

    }

    public void update(MessageEntity messageEntity) {
        em.merge(messageEntity);
    }

}
