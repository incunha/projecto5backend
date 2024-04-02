package service;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import bean.UserBean;
import dto.PasswordDto;
import dto.Task;
import dto.User;
import dto.UserDto;
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
    @GET
    @Path("")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUsers(@HeaderParam("token") String token) {
        boolean user = userBean.tokenExists(token);
        if (!user) {
            return Response.status(403).entity("User with this token is not found").build();
        }else {
            List<UserEntity> users = userBean.getUsers();
            return Response.status(200).entity(users).build();
        }
    }
    @GET
    @Path("/active")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getActiveUsers(@HeaderParam("token") String token) {
        boolean user = userBean.tokenExists(token);
        if (!user) {
            return Response.status(403).entity("User with this token is not found").build();
        }else {
            ArrayList<User> users = userBean.getActiveUsers(token);
            return Response.status(200).entity(users).build();
        }
    }

    @GET
    @Path("/inactive")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getInactiveUsers(@HeaderParam("token") String token) {
        boolean user = userBean.tokenExists(token);
        if (!user) {
            return Response.status(403).entity("User with this token is not found").build();
        }else {
            ArrayList<User> users = userBean.getInactiveUsers(token);
            return Response.status(200).entity(users).build();
        }
    }

    @POST
    @Path("")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addUser(User a) {
       boolean valid = userBean.isUserValid(a);
        if (!valid) {
            return Response.status(400).entity("All elements are required are required").build();
        }
        boolean user = userBean.userNameExists(a.getUsername());
        if (user) {

            return Response.status(409).entity("User with this username is already exists").build();
        } else {
            if(a.getRole() == null || a.getRole().isEmpty()){
                a.setRole("developer");
            }

            userBean.addUser(a);
            return Response.status(201).entity("A new user is created").build();
        }
    }

    @GET
    @Path("/photo")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPhoto(@HeaderParam("token") String token) {
        boolean user = userBean.userExists(token);
        boolean authorized = userBean.isUserAuthorized(token);
        if (!user) {
            return Response.status(404).entity("User with this username is not found").build();
        }else if (!authorized) {
            return Response.status(403).entity("Forbidden").build();
        }
        User user1 = userBean.getUser(token);
        if(user1.getUserPhoto() == null){
            return Response.status(400).entity("User with no photo").build();
        }
        return Response.status(200).entity(user1.getUserPhoto()).build();
    }

    @GET
    @Path("/{username}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUser(@HeaderParam("token")String token, @PathParam("username")String username) {
        boolean exists = userBean.findOtherUserByUsername(username);
        if (!exists){
            return Response.status(404).entity("User with this username is not found").build();
        }else if(userBean.getUser(token).getRole().equals("developer") && !userBean.getUser(token).getUsername().equals(username)){
            return Response.status(403).entity("Forbidden").build();
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
          
        }else if (userBean.getUser(token).getRole().equals("Owner") && a.getRole() != null ) {
            boolean updated = userBean.ownerupdateUser(token, a);

            if (!updated) {
                return Response.status(400).entity("Failed. User not updated").build();
            }
            return Response.status(200).entity("User updated").build();
        }
            return Response.status(403).entity("Forbidden").build();
    }
    @PATCH
    @Path("/password")
    @Produces(MediaType.APPLICATION_JSON)
    public Response updatePassword(@HeaderParam("token") String token, PasswordDto password) {
        boolean authorized = userBean.isUserAuthorized(token);
        boolean valid = userBean.isPasswordValid(password);
        if (!authorized) {
            return Response.status(403).entity("Forbidden").build();
        }else if (!valid) {
            return Response.status(406).entity("Password is not valid").build();
        }else {
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
        if(!user.isActive()){
            return Response.status(403).entity("User is not active").build();
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
    public Response logout(@HeaderParam("token") String token){
        boolean authorized = userBean.isUserAuthorized(token);
        if (!authorized) {
            return Response.status(405).entity("Forbidden").build();
        }else {
            userBean.logout(token);
            return Response.status(200).entity("Logged out").build();
        }
    }
    @DELETE
    @Path("/{username}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteUser(@HeaderParam("token") String token,@PathParam("username") String username){
        boolean authorized = userBean.isUserOwner(token);
        if (!authorized) {
            return Response.status(403).entity("Forbidden").build();
        }else {

            if(userBean.deleteUser(token,username)){
                return Response.status(200).entity("User deleted").build();
            }else{
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
        }else {
            User user = userBean.getUser(token);
            System.out.println(user.getUsername());
            UserDto userDto = userBean.convertUsertoUserDto(user);
            return Response.status(200).entity(userDto).build();
        }
    }
    @PATCH
    @Path("/active/{username}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response restoreUser(@HeaderParam("token") String token, @PathParam("username") String username) {
        boolean authorized = userBean.isUserOwner(token);
        if (!authorized) {
            return Response.status(405).entity("Forbidden").build();
        }else {
            if (userBean.restoreUser(username)){
                return Response.status(200).entity("User restored").build();
            }else{
                return Response.status(400).entity("User not restored").build();
            }
        }
    }
}

