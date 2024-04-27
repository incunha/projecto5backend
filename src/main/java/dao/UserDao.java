package dao;

import entities.TimeOut;
import entities.UserEntity;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;

import java.sql.Time;
import java.time.LocalDateTime;
import java.util.List;

@Stateless
public class UserDao extends AbstractDao<UserEntity> {
    @PersistenceContext
    private EntityManager em;
    private static final long serialVersionUID = 1L;

    public UserDao() {
        super(UserEntity.class);
    }

    public UserEntity findUserByToken(String token) {
        try {
            return (UserEntity) em.createNamedQuery("User.findUserByToken").setParameter("token", token)
                    .getSingleResult();

        } catch (NoResultException e) {
            return null;
        }
    }

    public UserEntity findUserByEmail(String email) {
        try {
            return (UserEntity) em.createNamedQuery("User.findUserByEmail").setParameter("email", email)
                    .getSingleResult();

        } catch (NoResultException e) {
            return null;
        }
    }

    public UserEntity findUserByUsername(String username) {
        try {
            return (UserEntity) em.createNamedQuery("User.findUserByUsername").setParameter("username", username)
                    .getSingleResult();

        } catch (NoResultException e) {
            return null;
        }
    }

    public UserEntity findUserByConfirmationToken(String confirmationToken) {
        try {
            return (UserEntity) em.createNamedQuery("User.findUserByConfirmationToken").setParameter("confirmationToken", confirmationToken)
                    .getSingleResult();

        } catch (NoResultException e) {
            return null;
        }
    }

    public int getUnconfirmedUsers() {
        return em.createNamedQuery("User.findAllUnconfirmedUsers").getResultList().size();
    }


    public List<UserEntity> getUsersByRole(String role, Boolean active) {
        return em.createNamedQuery("User.findUserByRole").setParameter("role", role).setParameter("active", active).getResultList();
    }


    public List<UserEntity> getDeletedUsers() {
        return em.createNamedQuery("User.findDeletedUsers").getResultList();
    }

    public void updateToken(UserEntity userEntity) {
        em.createNamedQuery("User.updateToken").setParameter("token", userEntity.getToken()).setParameter("username", userEntity.getName()).executeUpdate();
    }

    public void updateUser(UserEntity userEntity) {
        em.merge(userEntity);
    }

    public List<UserEntity> getActiveUsers() {
        return em.createNamedQuery("User.findActiveUsers").getResultList();
    }

    public List<UserEntity> findAllUsers() {
        return em.createNamedQuery("User.findAllUsers").getResultList();
    }

    public List<Object[]> countConfirmedUsersByDate() {
        return em.createNamedQuery("User.countConfirmedUsersByDate").getResultList();
    }

    public int getTimeOut() {
        TimeOut timeOut = (TimeOut) em.createNamedQuery("TimeOut.findTimeOut").getSingleResult();
        return timeOut.getTimeOut();
    }

    public void setTimeOut(int timeOut) {
        em.createNamedQuery("TimeOut.updateTimeOut").setParameter("timeOut", timeOut).executeUpdate();
    }


    public List<UserEntity> findUnconfirmedUsers() {
        return em.createNamedQuery("User.findAllUnconfirmedUsers").getResultList();
    }
    public TimeOut findTimeOut(int id) {
        try {
            return (TimeOut) em.createNamedQuery("TimeOut.findTimeOutById")
                    .setParameter("id", id)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
    public void createTimeOut(TimeOut timeOut) {
        em.persist(timeOut);
    }
    public void updateTimeOut(TimeOut timeOut) {
        em.createNamedQuery("TimeOut.updateTimeOut").setParameter("timeOut", timeOut.getTimeOut()).setParameter("id", timeOut.getId()).executeUpdate();
    }
}
