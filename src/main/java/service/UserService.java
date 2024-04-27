package service;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import bean.EmailBean;
import bean.MessageBean;
import bean.NotificationBean;
import bean.UserBean;
import dto.*;
import entities.UserEntity;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
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


    @GET
    @Path("")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getFilteredUsers(@HeaderParam("token") String token, @QueryParam("role") String role, @QueryParam("active") Boolean active) {
        boolean authorized = userBean.isUserAuthorized(token);
        if (!authorized) {
            return Response.status(403).entity("Forbidden").build();
        } else {

            ArrayList<User> users = userBean.getFilteredUsers(role, active);
            System.out.println(users.size());
            return Response.status(200).entity(users).build();
        }
    }

    @POST
    @Path("")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addUser(@HeaderParam("token") String token, User a) {
        boolean authorized = userBean.isUserOwner(token);
        if (!authorized) {
            return Response.status(403).entity("Forbidden").build();
        }
        boolean valid = userBean.isUserValid(a);
        if (!valid) {
            return Response.status(400).entity("All elements are required are required").build();
        }
        boolean user = userBean.userNameExists(a.getUsername());
        if (user) {
            return Response.status(409).entity("User with this username already exists").build();
        } else {
            if (a.getRole() == null || a.getRole().isEmpty()) {
                a.setRole("developer");
            }
            String confirmationToken = userBean.generateConfirmationToken();
            a.setConfirmationToken(confirmationToken);
            userBean.addUser(a);
            emailBean.sendConfirmationEmail(a, confirmationToken, LocalDateTime.now());
            return Response.status(201).entity("A new user is created").build();
        }
    }

    @PUT
    @Path("/confirm/{confirmationToken}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response confirmUser (@PathParam("confirmationToken") String confirmationToken, PasswordDto password) {

        User a = userBean.getUserByConfirmationToken(confirmationToken);
        if (a == null) {
            return Response.status(404).entity("User with this confirmation token is not found").build();
        }
        boolean valid = userBean.isUserValid(a);
        if (!valid) {
            return Response.status(400).entity("All elements are required").build();
        } else {
            if (a.isConfirmed()){
                return Response.status(400).entity("User already confirmed").build();
            } else {
                userBean.confirmUser(confirmationToken, password.getPassword());
                userBean.deleteConfirmationToken(a);
                return Response.status(200).entity("User confirmed").build();
            }
        }
    }

        @PUT
        @Path("/forgotPassword/{email}")
        @Consumes(MediaType.APPLICATION_JSON)
        public Response recoverPassword (@PathParam("email") String email) {
            User a = userBean.getUserByEmail(email);
            if (a == null) {
                return Response.status(404).entity("User with this email is not found").build();
            } else if (!a.isActive()) {
                return Response.status(403).entity("User is not active").build();
            } else {
                String confirmationToken = userBean.generateConfirmationToken();
                a.setConfirmationToken(confirmationToken);
                userBean.updateUserEntity(a);
                emailBean.sendPasswordRecoverEmail(a, confirmationToken, LocalDateTime.now());
                return Response.status(200).entity("Password recovery email sent").build();
            }
        }

    @PUT
    @Path("/setPassword/{confirmationToken}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response setPassword (@PathParam("confirmationToken") String confirmationToken, PasswordDto password) {

        User a = userBean.getUserByConfirmationToken(confirmationToken);
        if (a == null) {
            return Response.status(404).entity("User with this confirmation token is not found").build();
        }
        boolean valid = userBean.isUserValid(a);
        if (!valid) {
            return Response.status(400).entity("All elements are required").build();
        } else {
              userBean.recoverPassword(confirmationToken, password.getPassword());
              userBean.deleteConfirmationToken(a);
                return Response.status(200).entity("Password set").build();
        }
    }


    @GET
    @Path("/{username}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUser(@HeaderParam("token") String token, @PathParam("username") String username) {
        boolean exists = userBean.findOtherUserByUsername(username);
        if (!exists) {
            return Response.status(404).entity("User with this username is not found").build();
        }
        User user = userBean.getUserByUsername(username);
        UserDto userDto = userBean.convertUsertoUserDto(user);
        return Response.status(200).entity(userDto).build();
    }

    @PUT
    @Path("")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateUser(@HeaderParam("token") String token, User a) {
        boolean user = userBean.userNameExists(a.getUsername());
        boolean valid = userBean.isUserValid(a);
        if (!user) {
            return Response.status(404).entity("User with this username is not found").build();
        } else if (!valid) {
            return Response.status(406).entity("All elements are required").build();
        }
        if (!userBean.getUser(token).getRole().equals("Owner") || a.getUsername().equals(userBean.getUser(token).getUsername()) && (a.getRole() == null)) {
            a.setRole(userBean.getUser(token).getRole());
            a.setPassword(userBean.getUser(token).getPassword());
            boolean updated = userBean.updateUser(token, a);
            if (!updated) {
                return Response.status(400).entity("Failed. User not updated").build();
            }
            return Response.status(200).entity("User updated").build();

        } else if (userBean.getUser(token).getRole().equals("Owner") && a.getRole() != null) {
            boolean updated = userBean.ownerupdateUser(token, a);

            if (!updated) {
                return Response.status(400).entity("Failed. User not updated").build();
            }
            return Response.status(200).entity("User updated").build();
        }
        return Response.status(403).entity("Forbidden").build();
    }

    @PUT
    @Path("/password")
    @Produces(MediaType.APPLICATION_JSON)
    public Response updatePassword(@HeaderParam("token") String token, PasswordDto password) {
        boolean authorized = userBean.isUserAuthorized(token);
        boolean valid = userBean.isPasswordValid(password);
        if (!authorized) {
            return Response.status(403).entity("Forbidden").build();
        } else if (!valid) {
            return Response.status(406).entity("Password is not valid").build();
        } else {
            boolean updated = userBean.updatePassword(token, password);
            if (!updated) {
                return Response.status(400).entity("Failed. Password not updated").build();
            }
            return Response.status(200).entity("Password updated").build();
        }
    }

    @GET
    @Path("/login")
    @Produces(MediaType.APPLICATION_JSON)
    public Response login(@HeaderParam("username") String username, @HeaderParam("password") String password) {

        User user = userBean.getUserByUsername(username);
        if (!user.isActive()) {
            return Response.status(403).entity("User is not active").build();
        } else if (!user.isConfirmed()) {
            return Response.status(403).entity("User is not confirmed").build();
        } else {
            String token = userBean.login(username, password);
            if (token == null) {
                return Response.status(404).entity("User with this username and password is not found").build();
            } else {
                return Response.status(200).entity(token).build();

            }
        }
    }

    @GET
    @Path("/logout")
    @Produces(MediaType.APPLICATION_JSON)
    public Response logout(@HeaderParam("token") String token) {
        boolean authorized = userBean.isUserAuthorized(token);
        if (!authorized) {
            return Response.status(405).entity("Forbidden").build();
        } else {
            userBean.logout(token);
            return Response.status(200).entity("Logged out").build();
        }
    }

    @DELETE
    @Path("/{username}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteUser(@HeaderParam("token") String token, @PathParam("username") String username) {
        boolean authorized = userBean.isUserOwner(token);
        if (!authorized) {
            return Response.status(403).entity("Forbidden").build();
        } else {

            if (userBean.deleteUser(token, username)) {
                return Response.status(200).entity("User deleted").build();
            } else {
                return Response.status(400).entity("User not deleted").build();
            }
        }
    }

    @GET
    @Path("/myUserDto")
    @Produces(MediaType.APPLICATION_JSON)
    public Response myProfile(@HeaderParam("token") String token) {
        boolean authorized = userBean.isUserAuthorized(token);
        if (!authorized) {
            return Response.status(403).entity("Forbidden").build();
        } else {
            User user = userBean.getUser(token);
            System.out.println(user.getUsername());
            UserDto userDto = userBean.convertUsertoUserDto(user);
            return Response.status(200).entity(userDto).build();
        }
    }

    @PUT
    @Path("/active/{username}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response restoreUser(@HeaderParam("token") String token, @PathParam("username") String username) {
        boolean authorized = userBean.isUserOwner(token);
        if (!authorized) {
            return Response.status(405).entity("Forbidden").build();
        } else {
            if (userBean.restoreUser(username)) {
                return Response.status(200).entity("User restored").build();
            } else {
                return Response.status(400).entity("User not restored").build();
            }
        }
    }

    @GET
    @Path("/totalTasks")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTaskTotals(@HeaderParam("token") String token, @QueryParam("username") String username) {
        boolean authorized = userBean.isUserAuthorized(token);
        if (!authorized) {
            return Response.status(403).entity("Forbidden").build();
        } else {

            int todoStatus = 10;
            int doingStatus = 20;
            int doneStatus = 30;
            ArrayList<Integer> totals = userBean.getTaskTotals (username, todoStatus, doingStatus, doneStatus);
            return Response.status(200).entity(totals).build();
        }
    }

    @GET
    @Path("/chat/{username1}/{username2}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getMessagesBetweenUsers(@HeaderParam("token") String token, @PathParam("username1") String username1, @PathParam("username2") String username2) {
        boolean authorized = userBean.isUserAuthorized(token);
        if (!authorized) {
            return Response.status(403).entity("Forbidden").build();
        } else {
                User user1 = userBean.getUserByUsername(username1);
                User user2 = userBean.getUserByUsername(username2);
                UserEntity userEntity1 = userBean.convertToEntity(user1);
                UserEntity userEntity2 = userBean.convertToEntity(user2);
                List<MessageDto> messages = messageBean.getMessagesBetweenUsers(userEntity1, userEntity2);
                messageBean.markMessagesAsRead(userEntity1, userEntity2);
                return Response.status(200).entity(messages).build();
        }
    }

    @GET
    @Path("/notifications")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getNotifications(@HeaderParam("token") String token) {
        boolean authorized = userBean.isUserAuthorized(token);
        if (!authorized) {
            return Response.status(403).entity("Forbidden").build();
        } else {
            User user = userBean.getUser(token);
            UserEntity userEntity = userBean.convertToEntity(user);
            List<NotificationDto> notifications = notificationBean.getNotificationsByUser(userEntity); // Aqui está a mudança
            return Response.status(200).entity(notifications).build();
        }
    }


    @PUT
    @Path("/notifications/read")
    public Response markAllNotificationsAsRead(@HeaderParam("token") String token) {
        boolean authorized = userBean.isUserAuthorized(token);
        if (!authorized) {
            return Response.status(403).entity("Forbidden").build();
        } else {
            User user = userBean.getUser(token);
            UserEntity userEntity = userBean.convertToEntity(user);
            notificationBean.markAllNotificationsAsRead(userEntity);
            return Response.status(200).build();
        }
    }

    @GET
    @Path("/notifications/unread")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUnreadNotificationsCount(@HeaderParam("token") String token) {
        boolean authorized = userBean.isUserAuthorized(token);
        if (!authorized) {
            return Response.status(403).entity("Forbidden").build();
        } else {
            User user = userBean.getUser(token);
            UserEntity userEntity = userBean.convertToEntity(user);
            List<NotificationDto> notifications = notificationBean.getNotificationsByUser(userEntity);
            long unreadCount = notifications.stream().filter(notification -> !notification.isRead()).count();
            return Response.status(200).entity(unreadCount).build();
        }
    }

    @GET
    @Path("/statistics")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getStatistics(@HeaderParam("token") String token) {
        boolean authorized = userBean.isUserAuthorized(token);
        if (!authorized) {
            return Response.status(403).entity("Forbidden").build();
        } else {
            UserStatisticsDto userStatistics = userBean.getStatistics();
            return Response.status(200).entity(userStatistics).build();
        }
    }

    @PUT
    @Path("/setTimeOut")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response setTimeOut(@HeaderParam("token") String token, int timeOut) {
        boolean authorized = userBean.isUserOwner(token);
        if (!authorized) {
            return Response.status(403).entity("Forbidden").build();
        } else {
            userBean.setTimeOut(timeOut);
            return Response.status(200).entity("Time out set").build();
        }
    }


}
