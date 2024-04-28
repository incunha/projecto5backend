package service;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import bean.*;
import dto.*;
import entities.UserEntity;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.query.sqm.function.SelfRenderingOrderedSetAggregateFunctionSqlAstExpression;
import utilities.EncryptHelper;


@Path("/users")
public class UserService {
    @Context
    private HttpServletRequest request;
    @Inject
    UserBean userBean;
    @Inject
    EncryptHelper encryptHelper;
    @Inject
    EmailBean emailBean;
    @Inject
    MessageBean messageBean;
    @Inject
    NotificationBean notificationBean;
    private static final Logger LOGGER = LogManager.getLogger(UserService.class);


    @GET
    @Path("")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getFilteredUsers(@HeaderParam("token") String token, @QueryParam("role") String role, @QueryParam("active") Boolean active, @Context HttpServletRequest request) {
        String ipAddress = request.getRemoteAddr();
        LOGGER.info(STR."IP Address {}", ipAddress);
        boolean authorized = userBean.isUserAuthorized(token);
        if (!authorized) {
            LOGGER.info("Unauthorized access attempt from IP Address {}", ipAddress);
            return Response.status(403).entity("Forbidden").build();
        } else {
            ArrayList<User> users = userBean.getFilteredUsers(role, active);
            System.out.println(users.size());
            LOGGER.info("Users retrieved from the database");
            return Response.status(200).entity(users).build();
        }
    }

    @POST
    @Path("")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addUser(@HeaderParam("token") String token, User a, @Context HttpServletRequest request) {
        LOGGER.info("addUser method called");
        boolean authorized = userBean.isUserOwner(token);
        if (!authorized) {
            LOGGER.info("Unauthorized access attempt from IP Address {}", request.getRemoteAddr());
            return Response.status(403).entity("Forbidden").build();
        }
        boolean valid = userBean.isUserValid(a);
        if (!valid) {
            LOGGER.info("User not valid");
            return Response.status(400).entity("All elements are required are required").build();
        }
        boolean user = userBean.userNameExists(a.getUsername());
        if (user) {
            LOGGER.info("User with this username already exists");
            return Response.status(409).entity("User with this username already exists").build();
        } else {
            if (a.getRole() == null || a.getRole().isEmpty()) {
                a.setRole("developer");
            }
            String confirmationToken = userBean.generateConfirmationToken();
            a.setConfirmationToken(confirmationToken);
            userBean.addUser(a);
            emailBean.sendConfirmationEmail(a, confirmationToken, LocalDateTime.now());
            LOGGER.info("User added and confirmation email sent");
            return Response.status(201).entity("A new user is created").build();
        }
    }

    @PUT
    @Path("/confirm/{confirmationToken}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response confirmUser (@PathParam("confirmationToken") String confirmationToken, PasswordDto password, @Context HttpServletRequest request) {

        User a = userBean.getUserByConfirmationToken(confirmationToken);
        if (a == null) {
            LOGGER.info("User with this confirmation token is not found");
            return Response.status(404).entity("User with this confirmation token is not found").build();
        }
        boolean valid = userBean.isUserValid(a);
        if (!valid) {
            LOGGER.info("All elements are required");
            return Response.status(400).entity("All elements are required").build();
        } else {
            if (a.isConfirmed()){
                LOGGER.info("User already confirmed");
                return Response.status(400).entity("User already confirmed").build();
            } else {
                userBean.confirmUser(confirmationToken, password.getPassword());
                userBean.deleteConfirmationToken(a);
                LOGGER.info("User confirmed");
                return Response.status(200).entity("User confirmed").build();
            }
        }
    }

        @PUT
        @Path("/forgotPassword/{email}")
        @Consumes(MediaType.APPLICATION_JSON)
        public Response recoverPassword (@PathParam("email") String email, @Context HttpServletRequest request) {
            User a = userBean.getUserByEmail(email);
            if (a == null) {
                LOGGER.info("User with this email is not found");
                return Response.status(404).entity("User with this email is not found").build();
            } else if (!a.isActive()) {
                LOGGER.info("User is not active");
                return Response.status(403).entity("User is not active").build();
            } else {
                String confirmationToken = userBean.generateConfirmationToken();
                a.setConfirmationToken(confirmationToken);
                userBean.updateUserEntity(a);
                emailBean.sendPasswordRecoverEmail(a, confirmationToken, LocalDateTime.now());
                LOGGER.info("Password recovery email sent");
                return Response.status(200).entity("Password recovery email sent").build();
            }
        }

    @PUT
    @Path("/setPassword/{confirmationToken}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response setPassword (@PathParam("confirmationToken") String confirmationToken, PasswordDto password, @Context HttpServletRequest request) {

        User a = userBean.getUserByConfirmationToken(confirmationToken);
        if (a == null) {
            LOGGER.info("User with this confirmation token is not found");
            return Response.status(404).entity("User with this confirmation token is not found").build();
        }
        boolean valid = userBean.isUserValid(a);
        if (!valid) {
            LOGGER.info("All elements are required");
            return Response.status(400).entity("All elements are required").build();
        } else {
              userBean.recoverPassword(confirmationToken, password.getPassword());
              userBean.deleteConfirmationToken(a);
                LOGGER.info("Password set");
                return Response.status(200).entity("Password set").build();
        }
    }


    @GET
    @Path("/{username}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUser(@HeaderParam("token") String token, @PathParam("username") String username, @Context HttpServletRequest request) {
        boolean exists = userBean.findOtherUserByUsername(username);
        if (!exists) {
            LOGGER.info("User with this username is not found");
            return Response.status(404).entity("User with this username is not found").build();
        }
        User user = userBean.getUserByUsername(username);
        UserDto userDto = userBean.convertUsertoUserDto(user);
        LOGGER.info("User retrieved");
        return Response.status(200).entity(userDto).build();
    }

    @PUT
    @Path("")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateUser(@HeaderParam("token") String token, User a, @Context HttpServletRequest request) {
        System.out.println("ENTROU1");
        boolean user = userBean.userNameExists(a.getUsername());
        boolean valid = userBean.isUserValid(a);
        if (!user) {
            System.out.println("ENTROU2");
            LOGGER.info("User with this username is not found");
            return Response.status(404).entity("User with this username is not found").build();
        } else if (!valid) {
            System.out.println("ENTROU3");
            LOGGER.info("All elements are required");
            return Response.status(406).entity("All elements are required").build();
        }
        if (!userBean.getUser(token).getRole().equals("Owner") || a.getUsername().equals(userBean.getUser(token).getUsername()) && (a.getRole() == null)) {
            a.setRole(userBean.getUser(token).getRole());
            a.setPassword(userBean.getUser(token).getPassword());
            boolean updated = userBean.updateUser(token, a);
            if (!updated) {
                System.out.println("ENTROU4");
                LOGGER.info("User not updated");
                return Response.status(400).entity("Failed. User not updated").build();
            }
            System.out.println("ENTROU5");
            LOGGER.info("User updated");
            return Response.status(200).entity("User updated").build();

        } else if (userBean.getUser(token).getRole().equals("Owner") && a.getRole() != null) {
            boolean updated = userBean.ownerupdateUser(token, a);

            if (!updated) {
                System.out.println("ENTROU6");
                LOGGER.info("User not updated");
                return Response.status(400).entity("Failed. User not updated").build();
            }
            System.out.println("ENTROU7");
            LOGGER.info("User updated");
            return Response.status(200).entity("User updated").build();
        }
        System.out.println("ENTROU8");
        LOGGER.info("User not updated");
        return Response.status(403).entity("Forbidden").build();
    }

    @PUT
    @Path("/password")
    @Produces(MediaType.APPLICATION_JSON)
    public Response updatePassword(@HeaderParam("token") String token, PasswordDto password, @Context HttpServletRequest request) {
        boolean authorized = userBean.isUserAuthorized(token);
        boolean valid = userBean.isPasswordValid(password);
        if (!authorized) {
            LOGGER.info("Forbidden");
            return Response.status(403).entity("Forbidden").build();
        } else if (!valid) {
            LOGGER.info("Password is not valid");
            return Response.status(406).entity("Password is not valid").build();
        } else {
            boolean updated = userBean.updatePassword(token, password);
            if (!updated) {
                LOGGER.info("Password not updated");
                return Response.status(400).entity("Failed. Password not updated").build();
            }
            LOGGER.info("Password updated");
            return Response.status(200).entity("Password updated").build();
        }
    }

    @GET
    @Path("/login")
    @Produces(MediaType.APPLICATION_JSON)
    public Response login(@HeaderParam("username") String username, @HeaderParam("password") String password, @Context HttpServletRequest request) {

        User user = userBean.getUserByUsername(username);
        if (!user.isActive()) {
            LOGGER.info("User is not active");
            return Response.status(403).entity("User is not active").build();
        } else if (!user.isConfirmed()) {
            LOGGER.info("User is not confirmed");
            return Response.status(403).entity("User is not confirmed").build();
        } else {
            String token = userBean.login(username, password);
            if (token == null) {
                LOGGER.info("User with this username and password is not found");
                return Response.status(404).entity("User with this username and password is not found").build();
            } else {
                LOGGER.info("User logged in");
                return Response.status(200).entity(token).build();

            }
        }
    }

    @GET
    @Path("/logout")
    @Produces(MediaType.APPLICATION_JSON)
    public Response logout(@HeaderParam("token") String token, @Context HttpServletRequest request) {
        boolean authorized = userBean.isUserAuthorized(token);
        if (!authorized) {
            LOGGER.info("Forbidden");
            return Response.status(405).entity("Forbidden").build();
        } else {
            LOGGER.info("Logged out");
            userBean.logout(token);
            return Response.status(200).entity("Logged out").build();
        }
    }

    @DELETE
    @Path("/{username}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteUser(@HeaderParam("token") String token, @PathParam("username") String username, @Context HttpServletRequest request) {
        boolean authorized = userBean.isUserOwner(token);
        if (!authorized) {
            LOGGER.info("Forbidden");
            return Response.status(403).entity("Forbidden").build();
        } else {

            if (userBean.deleteUser(token, username)) {
                LOGGER.info("User deleted");
                return Response.status(200).entity("User deleted").build();
            } else {
                LOGGER.info("User not deleted");
                return Response.status(400).entity("User not deleted").build();
            }
        }
    }

    @GET
    @Path("/myUserDto")
    @Produces(MediaType.APPLICATION_JSON)
    public Response myProfile(@HeaderParam("token") String token, @Context HttpServletRequest request) {
        boolean authorized = userBean.isUserAuthorized(token);
        if (!authorized) {
            LOGGER.info("Forbidden");
            return Response.status(403).entity("Forbidden").build();
        } else {
            User user = userBean.getUser(token);
            System.out.println(user.getUsername());
            UserDto userDto = userBean.convertUsertoUserDto(user);
            LOGGER.info("User retrieved");
            return Response.status(200).entity(userDto).build();
        }
    }

    @PUT
    @Path("/active/{username}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response restoreUser(@HeaderParam("token") String token, @PathParam("username") String username, @Context HttpServletRequest request) {
        boolean authorized = userBean.isUserOwner(token);
        if (!authorized) {
            LOGGER.info("Forbidden");
            return Response.status(405).entity("Forbidden").build();
        } else {
            if (userBean.restoreUser(username)) {
                LOGGER.info("User restored");
                return Response.status(200).entity("User restored").build();
            } else {
                LOGGER.info("User not restored");
                return Response.status(400).entity("User not restored").build();
            }
        }
    }

    @GET
    @Path("/totalTasks")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTaskTotals(@HeaderParam("token") String token, @QueryParam("username") String username, @Context HttpServletRequest request) {
        boolean authorized = userBean.isUserAuthorized(token);
        if (!authorized) {
            LOGGER.info("Forbidden");
            return Response.status(403).entity("Forbidden").build();
        } else {

            int todoStatus = 10;
            int doingStatus = 20;
            int doneStatus = 30;
            ArrayList<Integer> totals = userBean.getTaskTotals (username, todoStatus, doingStatus, doneStatus);
            LOGGER.info("Task totals retrieved");
            return Response.status(200).entity(totals).build();
        }
    }

    @GET
    @Path("/chat/{username1}/{username2}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getMessagesBetweenUsers(@HeaderParam("token") String token, @PathParam("username1") String username1, @PathParam("username2") String username2, @Context HttpServletRequest request) {
        boolean authorized = userBean.isUserAuthorized(token);
        if (!authorized) {
            LOGGER.info("Forbidden");
            return Response.status(403).entity("Forbidden").build();
        } else {
                User user1 = userBean.getUserByUsername(username1);
                User user2 = userBean.getUserByUsername(username2);
                UserEntity userEntity1 = userBean.convertToEntity(user1);
                UserEntity userEntity2 = userBean.convertToEntity(user2);
                List<MessageDto> messages = messageBean.getMessagesBetweenUsers(userEntity1, userEntity2);
                messageBean.markMessagesAsRead(userEntity1, userEntity2);
                LOGGER.info("Messages between users retrieved");
                return Response.status(200).entity(messages).build();
        }
    }

    @GET
    @Path("/notifications")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getNotifications(@HeaderParam("token") String token, @Context HttpServletRequest request) {
        boolean authorized = userBean.isUserAuthorized(token);
        if (!authorized) {
            LOGGER.info("Forbidden");
            return Response.status(403).entity("Forbidden").build();
        } else {
            User user = userBean.getUser(token);
            UserEntity userEntity = userBean.convertToEntity(user);
            List<NotificationDto> notifications = notificationBean.getNotificationsByUser(userEntity);
            LOGGER.info("Notifications retrieved");
            return Response.status(200).entity(notifications).build();
        }
    }


    @PUT
    @Path("/notifications/read")
    public Response markAllNotificationsAsRead(@HeaderParam("token") String token, @Context HttpServletRequest request) {
        boolean authorized = userBean.isUserAuthorized(token);
        if (!authorized) {
            LOGGER.info("Forbidden");
            return Response.status(403).entity("Forbidden").build();
        } else {
            User user = userBean.getUser(token);
            UserEntity userEntity = userBean.convertToEntity(user);
            notificationBean.markAllNotificationsAsRead(userEntity);
            LOGGER.info("All notifications marked as read");
            return Response.status(200).build();
        }
    }

    @GET
    @Path("/notifications/unread")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUnreadNotificationsCount(@HeaderParam("token") String token, @Context HttpServletRequest request) {
        boolean authorized = userBean.isUserAuthorized(token);
        if (!authorized) {
            LOGGER.info("Forbidden");
            return Response.status(403).entity("Forbidden").build();
        } else {
            User user = userBean.getUser(token);
            UserEntity userEntity = userBean.convertToEntity(user);
            List<NotificationDto> notifications = notificationBean.getNotificationsByUser(userEntity);
            long unreadCount = notifications.stream().filter(notification -> !notification.isRead()).count();
            LOGGER.info("Unread notifications count retrieved");
            return Response.status(200).entity(unreadCount).build();
        }
    }

    @GET
    @Path("/statistics")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getStatistics(@HeaderParam("token") String token, @Context HttpServletRequest request) {
        boolean authorized = userBean.isUserAuthorized(token);
        if (!authorized) {
            LOGGER.info("Forbidden");
            return Response.status(403).entity("Forbidden").build();
        } else {
            UserStatisticsDto userStatistics = userBean.getStatistics();
            LOGGER.info("User statistics retrieved");
            return Response.status(200).entity(userStatistics).build();
        }
    }

    @PUT
    @Path("/setTimeOut")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response setTimeOut(@HeaderParam("token") String token, int timeOut, @Context HttpServletRequest request) {
        boolean authorized = userBean.isUserOwner(token);
        if (!authorized) {
            LOGGER.info("Forbidden");
            return Response.status(403).entity("Forbidden").build();
        } else {
            userBean.setTimeOut(timeOut);
            LOGGER.info("Time out set");
            return Response.status(200).entity("Time out set").build();
        }
    }

}
