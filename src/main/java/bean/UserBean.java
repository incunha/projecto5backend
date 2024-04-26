package bean;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.lang.reflect.Array;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import dao.NotificationDao;
import dao.TaskDao;
import dao.UserDao;
import dto.*;
import entities.NotificationEntity;
import entities.TaskEntity;
import entities.UserEntity;
import jakarta.ejb.EJB;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.ejb.Stateless;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.jms.Message;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbConfig;
import jakarta.security.enterprise.credential.Password;
import utilities.EncryptHelper;
import entities.MessageEntity;
import dao.MessageDao;
import websocket.Notifier;


@Singleton
public class UserBean {
    public UserBean() {
    }

    @EJB
    UserDao userDao;
    @EJB
    TaskDao taskDao;
    @EJB
    TaskBean taskBean;
    @EJB
    MessageDao MessageDao;
    @EJB
    EncryptHelper EncryptHelper;
    @EJB
    EmailBean emailBean;
    @EJB
    NotificationDao NotificationDao;

    public void addUser(User a) {

        UserEntity userEntity = convertToEntity(a);

        userDao.persist(userEntity);
    }

    public void setTimeOut (int timeout) {
        userDao.setTimeOut(timeout);
    }

    public void confirmUser( String confirmationToken, String password) {
        UserEntity userEntity = userDao.findUserByConfirmationToken(confirmationToken);
        userEntity.setConfirmed(true);
        userEntity.setPassword(EncryptHelper.encryptPassword(password));
        userDao.updateUser(userEntity);

    }

    public void recoverPassword ( String confirmationToken, String password) {
        UserEntity userEntity = userDao.findUserByConfirmationToken(confirmationToken);
        if (userEntity.isConfirmed()) {
            userEntity.setPassword(EncryptHelper.encryptPassword(password));
            userDao.updateUser(userEntity);
        }
    }

    public void deleteConfirmationToken(User a) {
        UserEntity userEntity = userDao.findUserByUsername(a.getUsername());
        userEntity.setConfirmationToken(null);
        userDao.updateUser(userEntity);
    }

    public User getUserByConfirmationToken(String confirmationToken) {
        UserEntity userEntity = userDao.findUserByConfirmationToken(confirmationToken);
        System.out.println(userEntity.getUsername());
        return convertToDto(userEntity);
    }


    public User getUserByEmail(String email) {
        UserEntity userEntity = userDao.findUserByEmail(email);
        return convertToDto(userEntity);
    }


    public User getUser(String token) {
        UserEntity userEntity = userDao.findUserByToken(token);
        return convertToDto(userEntity);
    }

    public User findUserByUsername(String username) {
        UserEntity userEntity = userDao.findUserByUsername(username);
        return convertToDto(userEntity);
    }


    public List<UserEntity> getUsers() {
        List<UserEntity> users = userDao.findAll();
        return users;
    }

    public boolean blockUser(String username) {
        UserEntity a = userDao.findUserByUsername(username);
        if (a != null) {
            a.setActive(false);
            userDao.updateUser(a);
            return true;
        }
        return false;
    }

    public boolean removeUser(String username) {
        UserEntity a = userDao.findUserByUsername(username);
        if (a != null) {
            userDao.remove(a);
            return true;
        }
        return false;
    }
    public boolean ownerupdateUser(String token, User user) {
        UserEntity a = userDao.findUserByUsername(user.getUsername());
        UserEntity responsible = userDao.findUserByToken(token);
        if (a != null && responsible.getRole().equals("Owner")) {
            a.setName(user.getName());
            a.setEmail(user.getEmail());
            a.setContactNumber(user.getContactNumber());
            a.setUserPhoto(user.getUserPhoto());
            a.setRole(user.getRole());
            userDao.updateUser(a);
            return true;
        }
        return false;
    }

    public boolean updateUserEntity(User user) {
        UserEntity user1 = convertToEntity(user);
        userDao.updateUser(user1);
        return true;
    }

    public boolean updateUser(String token, User user) {
        UserEntity a = userDao.findUserByUsername(user.getUsername());
        if (a != null) {
            a.setUsername(user.getUsername());
            a.setName(user.getName());
            a.setEmail(user.getEmail());
            a.setPassword(user.getPassword());
            a.setContactNumber(user.getContactNumber());
            a.setUserPhoto(user.getUserPhoto());
            a.setRole(user.getRole());
            a.setActive(user.isActive());
            userDao.updateUser(a);
            return true;
        }
        return false;
    }
    public boolean updatePassword(String token, PasswordDto password) {
        UserEntity a = userDao.findUserByToken(token);
        if (a != null) {
            if (a.getPassword().equals(EncryptHelper.encryptPassword(password.getPassword()))) {
                a.setPassword(EncryptHelper.encryptPassword(password.getNewPassword()));
                userDao.updateUser(a);
                return true;
            }
        }
        return false;
    }

    public boolean isPasswordValid(PasswordDto password) {
        if (password.getPassword().isBlank() || password.getNewPassword().isBlank()) {
            return false;
        } else if (password.getPassword() == null || password.getNewPassword() == null) {
            return false;
        }
        return true;
    }

public boolean findOtherUserByUsername(String username) {
        UserEntity a = userDao.findUserByUsername(username);
      return a != null;
}

    public String login(String username, String password) {
        UserEntity user = userDao.findUserByUsername(username);
        String password1 = EncryptHelper.encryptPassword(password);
        if (user != null && user.isActive() && user.isConfirmed()) {
            String token;
            if (user.getPassword().equals(password1)) {
                do {
                    token = generateToken();
                } while (tokenExists(token));
            } else {
                return null;
            }
            user.setToken(token);
            user.setLastInteraction(LocalDateTime.now());
            userDao.updateUser(user);

            return token;
        }
        return null;
    }

    public boolean userExists(String token) {
        ;
        UserEntity a = userDao.findUserByToken(token);
        if (a != null) {
            return true;
        }
        return false;
    }

    public boolean userNameExists(String username) {
        UserEntity a = userDao.findUserByUsername(username);
        if (a != null) {
            return true;
        }
        return false;
    }

    public boolean isUserAuthorized(String token) {
        UserEntity a = userDao.findUserByToken(token);
        if (a != null) {

                return true;

        }

        return false;
    }



    public boolean isUserValid(User user) {
        if (user.getUsername().isBlank() || user.getName().isBlank() || user.getEmail().isBlank() || user.getContactNumber().isBlank() || user.getUserPhoto().isBlank()) {
            return false;
        } else if (user.getUsername() == null || user.getName() == null || user.getEmail() == null || user.getContactNumber() == null || user.getUserPhoto() == null) {
            return false;
        }
        return true;
    }

    public User getUserByUsername(String username) {
        UserEntity userEntity = userDao.findUserByUsername(username);
        return convertToDto(userEntity);
    }
    public ArrayList<User> getActiveUsers() {
        List<UserEntity> users = userDao.getActiveUsers();
        ArrayList<User> usersDto = new ArrayList<>();
        for (UserEntity user : users) {
            if (!user.getUsername().equals("admin") && !user.getUsername().equals("deleted")) {
                usersDto.add(convertToDto(user));
            }
        }
        return usersDto;
    }



    public UserEntity convertToEntity(User user) {
        UserEntity userEntity = new UserEntity();
        userEntity.setUsername(user.getUsername());
        userEntity.setName(user.getName());
        userEntity.setEmail(user.getEmail());
        userEntity.setPassword(user.getPassword());
        userEntity.setContactNumber(user.getContactNumber());
        userEntity.setUserPhoto(user.getUserPhoto());
        userEntity.setToken(user.getToken());
        userEntity.setRole(user.getRole());
        userEntity.setActive(user.isActive());
        userEntity.setConfirmed(user.isConfirmed());
        userEntity.setDateCreated(user.getDateCreated());
        userEntity.setConfirmationToken(user.getConfirmationToken());
        return userEntity;
    }

    public User convertToDto(UserEntity userEntity) {
        User user = new User();
        user.setUsername(userEntity.getUsername());
        user.setName(userEntity.getName());
        user.setEmail(userEntity.getEmail());
        user.setPassword(userEntity.getPassword());
        user.setContactNumber(userEntity.getContactNumber());
        user.setUserPhoto(userEntity.getUserPhoto());
        user.setToken(userEntity.getToken());
        user.setRole(userEntity.getRole());
        user.setActive(userEntity.isActive());
        user.setConfirmed(userEntity.isConfirmed());
        return user;
    }

    public boolean tokenExists(String token) {
        UserEntity a = userDao.findUserByToken(token);
        return a != null;
    }

    public String generateToken() {
        String token = "";
        for (int i = 0; i < 10; i++) {
            token += (char) (Math.random() * 26 + 'a');
        }
        return token;
    }

    public String generateConfirmationToken() {
        String token = "";
        for (int i = 0; i < 10; i++) {
            token += (char) (Math.random() * 26 + 'a');
        }
        return token;
    }


    public boolean deleteUser(String token, String username) {
        if(username.equals("admin") || username.equals("deleted")){
            return false;
        }

        UserEntity user = userDao.findUserByUsername(username);
        UserEntity responsible = userDao.findUserByToken(token);
        if (user.isActive() && responsible.getRole().equals("Owner") && !user.getUsername().equals(responsible.getUsername())) {
            user.setActive(false);
            user.setToken(null);
            userDao.updateUser(user);
            return true;
        }
        if (responsible.getRole().equals("Owner") && !user.isActive()) {
            if(doesUserHaveTasks(username)){
                List<TaskEntity> tasks = taskBean.getTasksByUser(user);
                UserEntity deletedUser = userDao.findUserByUsername("deleted");
                for(TaskEntity task: tasks){
                    task.setUser(deletedUser);
                    taskDao.updateTask(task);
                }
            }
            ArrayList <MessageEntity> messages = MessageDao.findMessagesByUsername(user.getUsername());
            ArrayList <NotificationEntity> notifications = NotificationDao.findNotificationsByUsername(user.getUsername());
            if(messages != null || notifications != null){
                for (MessageEntity message : messages) {
                    MessageDao.deleteMessage( message);
                }
                for (NotificationEntity notification : notifications) {
                    NotificationDao.deleteNotification(notification);
                }
            }

            userDao.remove(user);
            return true;
        }
        return false;
    }

    public void logout(String token) {
        UserEntity user = userDao.findUserByToken(token);
        user.setLastInteraction(null);
        user.setToken(null);
        userDao.updateUser(user);
    }

    public UserDto convertUsertoUserDto(User user) {
        UserDto userDto = new UserDto();
        userDto.setName(user.getName());
        userDto.setEmail(user.getEmail());
        userDto.setContactNumber(user.getContactNumber());
        userDto.setRole(user.getRole());
        userDto.setUserPhoto(user.getUserPhoto());
        userDto.setUsername(user.getUsername());
        return userDto;
    }

    public boolean isUserOwner(String token) {
        UserEntity a = userDao.findUserByToken(token);
        if (a.getRole().equals("Owner")) {
            return true;
        }
        return false;
    }

    public boolean restoreUser(String username) {
        UserEntity a = userDao.findUserByUsername(username);
        if (a != null) {
            a.setActive(true);
            userDao.updateUser(a);
            return true;
        }
        return false;
    }

    public boolean doesUserHaveTasks(String username) {
        UserEntity a = userDao.findUserByUsername(username);
        List<TaskEntity> tasks = taskBean.getTasksByUser(a);
        if (tasks.size() > 0) {
            return true;
        } else {
            return false;
        }
    }
    public void createDefaultUsers() {
        if(userDao.findUserByUsername("admin") == null) {
            UserEntity userEntity = new UserEntity();
            userEntity.setUsername("admin");
            userEntity.setName("admin");
            userEntity.setEmail("coiso@cenas.com");
            userEntity.setPassword(EncryptHelper.encryptPassword("admin"));
            userEntity.setContactNumber("123456789");
            userEntity.setUserPhoto("https://cdn-icons-png.freepik.com/512/10015/10015419.png");
            userEntity.setRole("Owner");
            userEntity.setActive(true);
            userEntity.setConfirmed(true);
            userEntity.setDateCreated(LocalDate.now());
            userDao.persist(userEntity);
        }
        if(userDao.findUserByUsername("deleted") == null) {

            UserEntity userEntity1 = new UserEntity();
            userEntity1.setUsername("deleted");
            userEntity1.setName("Deleted");
            userEntity1.setEmail("ThrowFeces@ppl.com");
            userEntity1.setPassword(EncryptHelper.encryptPassword("deleted"));
            userEntity1.setContactNumber("123456789");
            userEntity1.setUserPhoto("https://www.pngitem.com/pimgs/m/146-1468479_my-profile-icon-blank-profile-picture-circle-hd.png");
            userEntity1.setRole("developer");
            userEntity1.setActive(true);
            userEntity1.setConfirmed(true);
            userEntity1.setDateCreated(LocalDate.now());
            userDao.persist(userEntity1);
        }

        if(userDao.findUserByUsername("tony") == null) {
            UserEntity userEntity = new UserEntity();
            userEntity.setUsername("tony");
            userEntity.setName("António Silva");
            userEntity.setEmail("tony@gmail.com");
            userEntity.setPassword(EncryptHelper.encryptPassword("pass"));
            userEntity.setContactNumber("123456789");
            userEntity.setUserPhoto("https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQe36tgwsLsNUfcLG7G0rxOHz5Rkl5LAy0Y2g&s");
            userEntity.setRole("developer");
            userEntity.setActive(true);
            userEntity.setConfirmed(true);
            userEntity.setDateCreated(LocalDate.of(2024, 3, 12));
            userDao.persist(userEntity);
        }

        if(userDao.findUserByUsername("ju") == null) {
            UserEntity userEntity = new UserEntity();
            userEntity.setUsername("ju");
            userEntity.setName("Joana Costa");
            userEntity.setEmail("joaninha@gmail.com");
            userEntity.setPassword(EncryptHelper.encryptPassword("pass"));
            userEntity.setContactNumber("123456789");
            userEntity.setUserPhoto("https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQ_0n_RaF3ENQsybygfdmdN0wSY5s_Qvifb5g&s");
            userEntity.setRole("ScrumMaster");
            userEntity.setActive(true);
            userEntity.setConfirmed(true);
            userEntity.setDateCreated(LocalDate.of(2024, 1, 10));
            userDao.persist(userEntity);
        }

        if(userDao.findUserByUsername("jony") == null) {
            UserEntity userEntity = new UserEntity();
            userEntity.setUsername("jony");
            userEntity.setName("João Neves");
            userEntity.setEmail("joaNeves@gmail.com");
            userEntity.setPassword(EncryptHelper.encryptPassword("pass"));
            userEntity.setContactNumber("123456789");
            userEntity.setUserPhoto("https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQqj-TpMbj4LXXd62N9GKTGut2oxVFtYTwFDA&s");
            userEntity.setRole("Owner");
            userEntity.setActive(true);
            userEntity.setConfirmed(true);
            userEntity.setDateCreated(LocalDate.of(2024, 2, 22));
            userDao.persist(userEntity);
        }

    }

    public ArrayList<User> getFilteredUsers(String role, Boolean active) {
        ArrayList<User> usersDto = new ArrayList<>();
        if(active==null && role==null){
            return getAllUsers();
        }
        if (active && role == null ) {
            return getActiveUsers();
        } else if (!active && role == null ) {
            return getDeletedUsers();

        } else if (active && role != null ) {
            List<UserEntity> users = userDao.getUsersByRole(role,active);
            for (UserEntity user : users) {
                usersDto.add(convertToDto(user));
            }
            return usersDto;

        } else if (!active && role != null) {
            List<UserEntity> users = userDao.getDeletedUsers();
            for (UserEntity user : users) {
                if (user.getRole().equals(role)) {
                    usersDto.add(convertToDto(user));
                }
            }
            return usersDto;
        } else if (!active && role == null ) {
            List<UserEntity> users = userDao.getDeletedUsers();
            for (UserEntity user : users) {
                usersDto.add(convertToDto(user));
            }
            return usersDto;
        }
        return usersDto;

    }

    public ArrayList<User> getAllUsers() {
        List<UserEntity> users = userDao.findAllUsers();
        ArrayList<User> usersDto = new ArrayList<>();
        for (UserEntity user : users) {
            usersDto.add(convertToDto(user));
        }
        return usersDto;
    }


    public ArrayList<User> getDeletedUsers() {
        List<UserEntity> users = userDao.getDeletedUsers();
        ArrayList<User> usersDto = new ArrayList<>();
        for (UserEntity user : users) {
            usersDto.add(convertToDto(user));
        }
        return usersDto;
    }

    public void sendMessage(MessageDto messageDto) {
        UserEntity sender = userDao.findUserByUsername(messageDto.getSender());
        UserEntity receiver = userDao.findUserByUsername(messageDto.getReceiver());
        if (sender != null && receiver != null) {
            MessageEntity message = new MessageEntity();
            message.setSender(sender);
            message.setReceiver(receiver);
            message.setMessage(messageDto.getMessage());
            message.setTimestamp(messageDto.getSendDate());
            message.setRead(false);
            MessageDao.persist(message);
        }
    }
    public MessageEntity convertToEntity(MessageDto messageDto) {
        MessageEntity messageEntity = new MessageEntity();
        messageEntity.setMessage(messageDto.getMessage());
        messageEntity.setSender(userDao.findUserByUsername(messageDto.getSender()));
        messageEntity.setReceiver(userDao.findUserByUsername(messageDto.getReceiver()));
        messageEntity.setTimestamp(LocalDateTime.now());
        messageEntity.setRead(messageDto.isRead());
        return messageEntity;
    }

    public MessageDto convertToDto(MessageEntity messageEntity) {
        MessageDto messageDto = new MessageDto();
        messageDto.setMessage(messageEntity.getMessage());
        messageDto.setSender(messageEntity.getSender().getUsername());
        messageDto.setReceiver(messageEntity.getReceiver().getUsername());
        messageDto.setSendDate(messageEntity.getTimestamp());
        messageDto.setRead(messageEntity.isRead());
        return messageDto;
    }

    public ArrayList<Integer> getTaskTotals (String username, int todo, int doing, int done) {
        UserEntity user = userDao.findUserByUsername(username);
        ArrayList<Integer> taskTotals = new ArrayList<>();
        taskTotals.add(taskDao.findTotalActiveTasks(user) );
        taskTotals.add(taskDao.findActiveTasksByStatusAndUser(user,todo));
        taskTotals.add(taskDao.findActiveTasksByStatusAndUser(user,doing));
        taskTotals.add(taskDao.findActiveTasksByStatusAndUser(user,done));
        return taskTotals;
    }

    public UserStatisticsDto getStatistics() {
        UserStatisticsDto statisticsDto = new UserStatisticsDto();
        statisticsDto.setTotalUsers(userDao.findAll().size());
        statisticsDto.setTotalConfirmedusers(userDao.getActiveUsers().size());
        statisticsDto.setTotalBlockedUsers(userDao.getDeletedUsers().size());
        statisticsDto.setTotalUnconfirmedUsers(userDao.getUnconfirmedUsers());
        statisticsDto.setConfirmedUsersByDate(countConfirmedUsersByDate());
        return statisticsDto;
    }

    public Map<LocalDate, Long> countConfirmedUsersByDate() {
        List<Object[]> results = userDao.countConfirmedUsersByDate();
        return results.stream()
                .collect(Collectors.toMap(
                        result -> (LocalDate) result[0],
                        result -> (Long) result[1]
                ));
    }

}




