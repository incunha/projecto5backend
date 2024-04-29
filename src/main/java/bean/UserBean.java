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
import entities.*;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import utilities.EncryptHelper;
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

    private static final Logger LOGGER = LogManager.getLogger(UserBean.class);

    public void addUser(User a) {
        LOGGER.info("addUser method called");
        UserEntity userEntity = convertToEntity(a);
        LOGGER.info("UserEntity created");
        userDao.persist(userEntity);
    }

    public void setTimeOut (int timeout) {
        LOGGER.info("setTimeOut method called");
        LOGGER.info("Timeout set to: " + timeout);
        userDao.findTimeOut(1).setTimeOut(timeout);
    }

    public void checkUnconfirmedTimeout() {
        LOGGER.info("checkUnconfirmedTimeout method called");
        List<UserEntity> users = userDao.findUnconfirmedUsers();
        TimeOut timeout = userDao.findTimeOut(1);
        for (UserEntity user : users) {
            if (ChronoUnit.DAYS.between(user.getDateCreated(), LocalDate.now()) > timeout.getUnconfirmedTimeOut()) {
                LOGGER.info("User " + user.getUsername() + " has been deleted due to unconfirmed timeout");
                userDao.remove(user);
            }
        }
    }


    public void confirmUser( String confirmationToken, String password) {
        LOGGER.info("confirmUser method called");
        UserEntity userEntity = userDao.findUserByConfirmationToken(confirmationToken);
        userEntity.setConfirmed(true);
        userEntity.setPassword(EncryptHelper.encryptPassword(password));
        LOGGER.info("User " + userEntity.getUsername() + " has been confirmed");
        userDao.updateUser(userEntity);
    }

    public void recoverPassword ( String confirmationToken, String password) {
        LOGGER.info("recoverPassword method called");
        UserEntity userEntity = userDao.findUserByConfirmationToken(confirmationToken);
        if (userEntity.isConfirmed()) {
            userEntity.setPassword(EncryptHelper.encryptPassword(password));
            userDao.updateUser(userEntity);
            LOGGER.info("User " + userEntity.getUsername() + " has recovered his password");
        }
    }

    public void deleteConfirmationToken(User a) {
        LOGGER.info("deleteConfirmationToken method called");
        UserEntity userEntity = userDao.findUserByUsername(a.getUsername());
        userEntity.setConfirmationToken(null);
        userDao.updateUser(userEntity);
        LOGGER.info("User " + userEntity.getUsername() + " has deleted his confirmation token");
    }

    public User getUserByConfirmationToken(String confirmationToken) {
        LOGGER.info("getUserByConfirmationToken method called");
        UserEntity userEntity = userDao.findUserByConfirmationToken(confirmationToken);
        System.out.println(userEntity.getUsername());
        LOGGER.info("User " + userEntity.getUsername() + " has been found by his confirmation token");
        return convertToDto(userEntity);
    }


    public User getUserByEmail(String email) {
        LOGGER.info("getUserByEmail method called");
        UserEntity userEntity = userDao.findUserByEmail(email);
        LOGGER.info("User " + userEntity.getUsername() + " has been found by his email");
        return convertToDto(userEntity);
    }


    public User getUser(String token) {
        LOGGER.info("getUser method called");
        UserEntity userEntity = userDao.findUserByToken(token);
        LOGGER.info("User " + userEntity.getUsername() + " has been found by his token");
        return convertToDto(userEntity);
    }

    public User findUserByUsername(String username) {
        LOGGER.info("findUserByUsername method called");
        UserEntity userEntity = userDao.findUserByUsername(username);
        LOGGER.info("User " + userEntity.getUsername() + " has been found by his username");
        return convertToDto(userEntity);
    }


    public List<UserEntity> getUsers() {
        LOGGER.info("getUsers method called");
        List<UserEntity> users = userDao.findAll();
        LOGGER.info("All users have been found");
        return users;
    }

    public boolean blockUser(String username) {
        LOGGER.info("blockUser method called");
        UserEntity a = userDao.findUserByUsername(username);
        if (a != null) {
            a.setActive(false);
            userDao.updateUser(a);
            LOGGER.info("User " + a.getUsername() + " has been blocked");
            return true;
        }
        LOGGER.info("User " + a.getUsername() + " has not been blocked");
        return false;
    }

    public boolean removeUser(String username) {
        LOGGER.info("removeUser method called");
        UserEntity a = userDao.findUserByUsername(username);
        if (a != null) {
            userDao.remove(a);
            LOGGER.info("User " + a.getUsername() + " has been removed");
            return true;
        }
        LOGGER.info("User " + a.getUsername() + " has not been removed");
        return false;
    }
    public boolean ownerupdateUser(String token, User user) {
        LOGGER.info("ownerupdateUser method called");
        UserEntity a = userDao.findUserByUsername(user.getUsername());
        UserEntity responsible = userDao.findUserByToken(token);
        if (a != null && responsible.getRole().equals("Owner")) {
            a.setName(user.getName());
            a.setEmail(user.getEmail());
            a.setContactNumber(user.getContactNumber());
            a.setUserPhoto(user.getUserPhoto());
            a.setRole(user.getRole());
            userDao.updateUser(a);
            LOGGER.info("User " + a.getUsername() + " has been updated by the owner");
            return true;
        }
        LOGGER.info("User " + a.getUsername() + " has not been updated by the owner");
        return false;
    }

    public boolean updateUserEntity(User user) {
        LOGGER.info("updateUserEntity method called");
        UserEntity user1 = convertToEntity(user);
        userDao.updateUser(user1);
        LOGGER.info("User " + user1.getUsername() + " has been updated");
        return true;
    }

    public boolean updateUser(String token, User user) {
        LOGGER.info("updateUser method called");
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
            LOGGER.info("User " + a.getUsername() + " has been updated");
            return true;
        }
        LOGGER.info("User " + a.getUsername() + " has not been updated");
        return false;
    }

    public boolean updatePassword(String token, PasswordDto password) {
        LOGGER.info("updatePassword method called");
        UserEntity a = userDao.findUserByToken(token);
        if (a != null) {
            if (a.getPassword().equals(EncryptHelper.encryptPassword(password.getPassword()))) {
                a.setPassword(EncryptHelper.encryptPassword(password.getNewPassword()));
                userDao.updateUser(a);
                LOGGER.info("Password of user " + a.getUsername() + " has been updated");
                return true;
            }
        }
        LOGGER.info("Password of user " + a.getUsername() + " has not been updated");
        return false;
    }

    public boolean isPasswordValid(PasswordDto password) {
        LOGGER.info("isPasswordValid method called");
        if (password.getPassword().isBlank() || password.getNewPassword().isBlank()) {
            LOGGER.info("Password is not valid");
            return false;
        } else if (password.getPassword() == null || password.getNewPassword() == null) {
            LOGGER.info("Password is not valid");
            return false;
        }
        LOGGER.info("Password is valid");
        return true;
    }

public boolean findOtherUserByUsername(String username) {
        LOGGER.info("findOtherUserByUsername method called");
        UserEntity a = userDao.findUserByUsername(username);
        LOGGER.info("User " + a.getUsername() + " has been found by his username");
      return a != null;
}

    public String login(String username, String password) {
        LOGGER.info("login method called");
        UserEntity user = userDao.findUserByUsername(username);
        String password1 = EncryptHelper.encryptPassword(password);
        if (user != null && user.isActive() && user.isConfirmed()) {
            String token;
            if (user.getPassword().equals(password1)) {
                do {
                    LOGGER.info("Token generated");
                    token = generateToken();
                } while (tokenExists(token));
            } else {
                LOGGER.info("Password is not valid");
                return null;
            }
            user.setToken(token);
            user.setLastInteraction(LocalDateTime.now());
            userDao.updateUser(user);
            LOGGER.info("User " + user.getUsername() + " has logged in");
            return token;
        }
        LOGGER.info("User " + user.getUsername() + " has not logged in");
        return null;
    }

    public boolean userExists(String token) {
        LOGGER.info("userExists method called");
        UserEntity a = userDao.findUserByToken(token);
        if (a != null) {
            LOGGER.info("User " + a.getUsername() + " exists");
            return true;
        }
        LOGGER.info("User " + a.getUsername() + " does not exist");
        return false;
    }

    public boolean userNameExists(String username) {
        LOGGER.info("userNameExists method called");
        UserEntity a = userDao.findUserByUsername(username);
        if (a != null) {
            LOGGER.info("User " + a.getUsername() + " exists");
            return true;
        }
        LOGGER.info("Username does not exist");
        return false;
    }

    public boolean isUserAuthorized(String token) {
        LOGGER.info("isUserAuthorized method called");
        UserEntity a = userDao.findUserByToken(token);
        if (a != null) {
            LOGGER.info("User " + a.getUsername() + " is authorized");
            a.setLastInteraction(LocalDateTime.now());
            userDao.updateUser(a);
                return true;
        }
        LOGGER.info("User " + a.getUsername() + " is not authorized");
        return false;
    }



    public boolean isUserValid(User user) {
        LOGGER.info("isUserValid method called");
        if (user.getUsername().isBlank() || user.getName().isBlank() || user.getEmail().isBlank() || user.getContactNumber().isBlank() || user.getUserPhoto().isBlank()) {
            LOGGER.info("User is not valid");
            return false;
        } else if (user.getUsername() == null || user.getName() == null || user.getEmail() == null || user.getContactNumber() == null || user.getUserPhoto() == null) {
            LOGGER.info("User is not valid");
            return false;
        }
        LOGGER.info("User is valid");
        return true;
    }

    public User getUserByUsername(String username) {
        LOGGER.info("getUserByUsername method called");
        UserEntity userEntity = userDao.findUserByUsername(username);
        LOGGER.info("User " + userEntity.getUsername() + " has been found by his username");
        return convertToDto(userEntity);
    }
    public ArrayList<User> getActiveUsers() {
        LOGGER.info("getActiveUsers method called");
        List<UserEntity> users = userDao.getActiveUsers();
        ArrayList<User> usersDto = new ArrayList<>();
        for (UserEntity user : users) {
            if (!user.getUsername().equals("admin") && !user.getUsername().equals("deleted")) {
                usersDto.add(convertToDto(user));
            }
        }
        LOGGER.info("All active users have been found");
        return usersDto;
    }



    public UserEntity convertToEntity(User user) {
        LOGGER.info("convertToEntity method called");
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
        LOGGER.info("UserEntity created");
        return userEntity;
    }

    public User convertToDto(UserEntity userEntity) {
        LOGGER.info("convertToDto method called");
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
        LOGGER.info("UserDto created");
        return user;
    }

    public boolean tokenExists(String token) {
        LOGGER.info("tokenExists method called");
        UserEntity a = userDao.findUserByToken(token);
        LOGGER.info("Token exists");
        return a != null;
    }

    public String generateToken() {
        LOGGER.info("generateToken method called");
        String token = "";
        for (int i = 0; i < 10; i++) {
            token += (char) (Math.random() * 26 + 'a');
        }
        LOGGER.info("Token generated");
        return token;
    }

    public String generateConfirmationToken() {
        LOGGER.info("generateConfirmationToken method called");
        String token = "";
        for (int i = 0; i < 10; i++) {
            token += (char) (Math.random() * 26 + 'a');
        }
        LOGGER.info("Confirmation token generated");
        return token;
    }


    public boolean deleteUser(String token, String username) {
        LOGGER.info("deleteUser method called");
        if(username.equals("admin") || username.equals("deleted")){
            LOGGER.info("User " + username + " cannot be deleted");
            return false;
        }

        UserEntity user = userDao.findUserByUsername(username);
        UserEntity responsible = userDao.findUserByToken(token);
        if (user.isActive() && responsible.getRole().equals("Owner") && !user.getUsername().equals(responsible.getUsername())) {
            user.setActive(false);
            user.setToken(null);
            userDao.updateUser(user);
            LOGGER.info("User " + user.getUsername() + " has been deleted");
            return true;
        }
        if (responsible.getRole().equals("Owner") && !user.isActive()) {

            if(doesUserHaveTasks(username)){
                LOGGER.info("User " + user.getUsername() + " has tasks and cannot be deleted");
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
            LOGGER.info("User " + user.getUsername() + " has been deleted");
            userDao.remove(user);
            return true;
        }
        LOGGER.info("User " + user.getUsername() + " has not been deleted");
        return false;
    }

    public void logout(String token) {
        LOGGER.info("logout method called");
        UserEntity user = userDao.findUserByToken(token);
        user.setLastInteraction(null);
        user.setToken(null);
        userDao.updateUser(user);
        LOGGER.info("User " + user.getUsername() + " has logged out");
    }
    public void forcedLogout(String username) {
        LOGGER.info("forcedLogout method called");
        UserEntity user = userDao.findUserByUsername(username);
        user.setLastInteraction(null);
        user.setToken(null);
        userDao.updateUser(user);
        LOGGER.info("User " + user.getUsername() + " has been forced to log out");
    }

    public UserDto convertUsertoUserDto(User user) {
        LOGGER.info("convertUsertoUserDto method called");
        UserDto userDto = new UserDto();
        userDto.setName(user.getName());
        userDto.setEmail(user.getEmail());
        userDto.setContactNumber(user.getContactNumber());
        userDto.setRole(user.getRole());
        userDto.setUserPhoto(user.getUserPhoto());
        userDto.setUsername(user.getUsername());
        userDto.setActive(user.isActive());
        LOGGER.info("UserDto created");
        return userDto;
    }

    public boolean isUserOwner(String token) {
        LOGGER.info("isUserOwner method called");
        UserEntity a = userDao.findUserByToken(token);
        if (a.getRole().equals("Owner")) {
            LOGGER.info("User " + a.getUsername() + " is owner");
            return true;
        }
        LOGGER.info("User " + a.getUsername() + " is not owner");
        return false;
    }

    public boolean restoreUser(String username) {
        LOGGER.info("restoreUser method called");
        UserEntity a = userDao.findUserByUsername(username);
        if (a != null) {
            a.setActive(true);
            userDao.updateUser(a);
            LOGGER.info("User " + a.getUsername() + " has been restored");
            return true;
        }
        LOGGER.info("User " + a.getUsername() + " has not been restored");
        return false;
    }

    public boolean doesUserHaveTasks(String username) {
        LOGGER.info("doesUserHaveTasks method called");
        UserEntity a = userDao.findUserByUsername(username);
        List<TaskEntity> tasks = taskBean.getTasksByUser(a);
        if (tasks.size() > 0) {
            LOGGER.info("User " + a.getUsername() + " has tasks");
            return true;
        } else {
            LOGGER.info("User " + a.getUsername() + " does not have tasks");
            return false;
        }
    }
    public void createDefaultUsers() {
        LOGGER.info("createDefaultUsers method called");
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
        LOGGER.info("getFilteredUsers method called");
        ArrayList<User> usersDto = new ArrayList<>();
        if(active==null && role==null){
            LOGGER.info("No filter applied");
            return getAllUsers();
        }
        if (active && role == null ) {
            LOGGER.info("Filter applied: active");
            return getActiveUsers();
        } else if (!active && role == null ) {
            LOGGER.info("Filter applied: inactive");
            return getDeletedUsers();

        } else if (active && role != null ) {
            LOGGER.info("Filter applied: active and role");
            List<UserEntity> users = userDao.getUsersByRole(role,active);
            for (UserEntity user : users) {
                usersDto.add(convertToDto(user));
            }
            LOGGER.info("Users found");
            return usersDto;

        } else if (!active && role != null) {
            LOGGER.info("Filter applied: inactive and role");
            List<UserEntity> users = userDao.getDeletedUsers();
            for (UserEntity user : users) {
                if (user.getRole().equals(role)) {
                    usersDto.add(convertToDto(user));
                }
            }
            LOGGER.info("Users found");
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
        LOGGER.info("getAllUsers method called");
        List<UserEntity> users = userDao.findAllUsers();
        ArrayList<User> usersDto = new ArrayList<>();
        for (UserEntity user : users) {
            usersDto.add(convertToDto(user));
        }
        LOGGER.info("All users have been found");
        return usersDto;
    }


    public ArrayList<User> getDeletedUsers() {
        LOGGER.info("getDeletedUsers method called");
        List<UserEntity> users = userDao.getDeletedUsers();
        ArrayList<User> usersDto = new ArrayList<>();
        for (UserEntity user : users) {
            usersDto.add(convertToDto(user));
        }
        LOGGER.info("All deleted users have been found");
        return usersDto;
    }

    public void sendMessage(MessageDto messageDto) {
        LOGGER.info("sendMessage method called");
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
        LOGGER.info("convertToEntity method called");
        MessageEntity messageEntity = new MessageEntity();
        messageEntity.setMessage(messageDto.getMessage());
        messageEntity.setSender(userDao.findUserByUsername(messageDto.getSender()));
        messageEntity.setReceiver(userDao.findUserByUsername(messageDto.getReceiver()));
        messageEntity.setTimestamp(LocalDateTime.now());
        messageEntity.setRead(messageDto.isRead());
        return messageEntity;
    }

    public MessageDto convertToDto(MessageEntity messageEntity) {
        LOGGER.info("convertToDto method called");
        MessageDto messageDto = new MessageDto();
        messageDto.setMessage(messageEntity.getMessage());
        messageDto.setSender(messageEntity.getSender().getUsername());
        messageDto.setReceiver(messageEntity.getReceiver().getUsername());
        messageDto.setSendDate(messageEntity.getTimestamp());
        messageDto.setRead(messageEntity.isRead());
        return messageDto;
    }

    public ArrayList<Integer> getTaskTotals (String username, int todo, int doing, int done) {
        LOGGER.info("getTaskTotals method called");
        UserEntity user = userDao.findUserByUsername(username);
        ArrayList<Integer> taskTotals = new ArrayList<>();
        taskTotals.add(taskDao.findTotalActiveTasks(user) );
        taskTotals.add(taskDao.findActiveTasksByStatusAndUser(user,todo));
        taskTotals.add(taskDao.findActiveTasksByStatusAndUser(user,doing));
        taskTotals.add(taskDao.findActiveTasksByStatusAndUser(user,done));
        LOGGER.info("Task totals found");
        return taskTotals;
    }

    public UserStatisticsDto getStatistics() {
        LOGGER.info("getStatistics method called");
        UserStatisticsDto statisticsDto = new UserStatisticsDto();
        statisticsDto.setTotalUsers(userDao.findAll().size());
        statisticsDto.setTotalConfirmedusers(userDao.getActiveUsers().size());
        statisticsDto.setTotalBlockedUsers(userDao.getDeletedUsers().size());
        statisticsDto.setTotalUnconfirmedUsers(userDao.getUnconfirmedUsers());
        statisticsDto.setConfirmedUsersByDate(countConfirmedUsersByDate());
        LOGGER.info("Statistics found");
        return statisticsDto;
    }

    public Map<LocalDate, Long> countConfirmedUsersByDate() {
        LOGGER.info("countConfirmedUsersByDate method called");
        List<Object[]> results = userDao.countConfirmedUsersByDate();
        LOGGER.info("Confirmed users counted by date");
        return results.stream()
                .collect(Collectors.toMap(
                        result -> (LocalDate) result[0],
                        result -> (Long) result[1]
                ));
    }

    public void createInitialTimeOut() {
        LOGGER.info("createInitialTimeOut method called");
        if (userDao.findTimeOut(1) == null) {
            TimeOut timeout = new TimeOut();
            timeout.setTimeOut(5);
            timeout.setUnconfirmedTimeOut(3);
            userDao.createTimeOut(timeout);
        }
        LOGGER.info("Initial timeout created");
    }
}




