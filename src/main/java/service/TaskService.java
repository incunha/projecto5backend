package service;

import bean.TaskBean;
import bean.UserBean;
import dto.*;
import entities.TaskEntity;
import entities.UserEntity;
import entities.CategoryEntity;
import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.io.StringReader;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import jakarta.ws.rs.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.print.attribute.standard.Media;

@Path("/tasks")
public class TaskService {
    @Inject
    TaskBean taskBean;
    @Inject
    UserBean userBean;
    private static final Logger LOGGER = LogManager.getLogger(TaskService.class);

    @GET
    @Path("")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getFilteredTasks(@HeaderParam("token") String token, @QueryParam("active") Boolean active, @QueryParam("category") String category, @QueryParam("username") String username, @QueryParam("id") String taskId, @Context HttpServletRequest request) {

        boolean authorized = userBean.isUserAuthorized(token);
        if (!authorized) {
            LOGGER.info("Unauthorized access");
            return Response.status(401).entity("Unauthorized").build();
        }
        if (taskId == null) {
            if (active == null) {
                active = true;
                ArrayList<Task> taskList = taskBean.getFilteredTasks(active, category, username);
                LOGGER.info("Tasks filtered by active status, category and username");
                return Response.status(200).entity(taskList).build();

            } else {
                ArrayList<Task> taskList = taskBean.getFilteredTasks(active, category, username);
                LOGGER.info("Tasks filtered by active status, category and username");
                return Response.status(200).entity(taskList).build();
            }

        } else {
            Task task = taskBean.findTaskById(taskId);
            LOGGER.info("Task found by id");
            return Response.status(200).entity(task).build();
        }
    }


    @DELETE
    @Path("/{username}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response removeAllTasks(@HeaderParam("token") String token, @PathParam("username") String username, @Context HttpServletRequest request) {
        boolean authorized = userBean.isUserOwner(token);
        if (!authorized) {
            LOGGER.info("Unauthorized access");
            return Response.status(401).entity("Unauthorized").build();
        } else {
            User user = userBean.getUserByUsername(username);
            boolean removed = taskBean.deleteAllTasksByUser(userBean.convertToEntity(user));
            if (!removed) {
                LOGGER.info("Failed. Tasks not removed");
                return Response.status(400).entity("Failed. Tasks not removed").build();
            } else {
                LOGGER.info("Tasks removed");
                return Response.status(200).entity("Tasks removed").build();
            }
        }
    }
    @POST
    @Path("")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addTask(Task task, @HeaderParam("token") String token, @Context HttpServletRequest request) {
        boolean authorized = userBean.isUserAuthorized(token);
        if (!authorized) {
            LOGGER.info("Unauthorized access");
            return Response.status(401).entity("Unauthorized").build();
        } else {
            boolean valid = taskBean.isTaskValid(task);
            boolean categoryExists = taskBean.categoryExists(task.getCategory());
            if (!valid) {
                LOGGER.info("All elements are required");
                return Response.status(400).entity("All elements are required").build();
            }else if (!categoryExists){
                LOGGER.info("Category does not exist");
                return Response.status(400).entity("Category does not exist").build();
            }
            User user = userBean.getUser(token);
            taskBean.setInitialId(task);
            UserEntity userEntity = userBean.convertToEntity(user);
            TaskEntity taskEntity = taskBean.createTaskEntity(task,userEntity);
            System.out.println(taskEntity.getUser() + " " + taskEntity.getCategory() + " " + taskEntity.getDescription() + " " + taskEntity.getPriority() + " " + taskEntity.getStartDate() + " " + taskEntity.getEndDate() + " " + taskEntity.getStatus() + " " + taskEntity.isActive());
            taskBean.addTask(taskEntity);
            LOGGER.info("Task added");
            return Response.status(201).entity(taskBean.convertToDto(taskEntity)).build();
        }
    }
    @PUT
    @Path("/active/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response restoreTask(@HeaderParam("token") String token, @PathParam("id") String id, @Context HttpServletRequest request) {
        boolean authorized = userBean.isUserAuthorized(token);
        if (!authorized) {
            LOGGER.info("Unauthorized access");
            return Response.status(401).entity("Unauthorized").build();
        } else {
            boolean restored = taskBean.restoreTask(id);
            if (!restored) {
                LOGGER.info("Failed. Task not restored");
                return Response.status(400).entity("Failed. Task not restored").build();
            } else {
                LOGGER.info("Task restored");
                return Response.status(200).entity("Task restored").build();
            }
        }
    }
    @POST
    @Path("/categories")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createCategory(Category category, @HeaderParam("token") String token, @Context HttpServletRequest request) {
        boolean authorized = userBean.isUserOwner(token);
           User user = userBean.getUser(token);
        if (!authorized) {
            LOGGER.info("Unauthorized access");
            return Response.status(401).entity("Unauthorized").build();
        } else {
            boolean available = taskBean.categoryExists(category.getName());
            if (available) {
                LOGGER.info("Name not available");
                return Response.status(409).entity("Name not available").build();
            }
            taskBean.createCategory(category.getName(), user.getUsername());
            LOGGER.info("Category created");
            return Response.status(201).entity("Category created").build();
        }
    }
    @PUT
    @Path("/categories")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateCategory(Category category, @HeaderParam("token") String token, @Context HttpServletRequest request) {
        boolean authorized = userBean.isUserOwner(token);
        if (!authorized) {
            LOGGER.info("Unauthorized access");
            return Response.status(401).entity("Unauthorized").build();
        } else {
            boolean notavailable = taskBean.categoryExists(category.getName());
            if (notavailable) {
                LOGGER.info("Category name is not available");
                return Response.status(409).entity("Category name is not available").build();
            }
        }
        CategoryEntity categoryEntity = taskBean.findCategoryById(category.getId());
        categoryEntity.setName(category.getName());
        if (taskBean.updateCategory(categoryEntity)) {
            LOGGER.info("Category updated");
            return Response.status(200).entity("Category updated").build();
        }else{
            LOGGER.info("Failed. Category not updated");
            return Response.status(400).entity("Failed. Category not updated").build();
        }
    }

    @DELETE
    @Path("/categories/{name}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response removeCategory(@HeaderParam("token") String token, @PathParam("name") String name, @Context HttpServletRequest request) {
        boolean authorized = userBean.isUserOwner(token);
        if (!authorized) {
            LOGGER.info("Unauthorized access");
            return Response.status(401).entity("Unauthorized").build();
        } else {
            boolean exists = taskBean.categoryExists(name);
            if (!exists) {
                LOGGER.info("Category does not exist");
                return Response.status(404).entity("Category does not exist").build();
            }
            boolean removed = taskBean.removeCategory(name);
            if (!removed) {
                LOGGER.info("Failed. Category not removed. update all tasks before deleting the category");
                return Response.status(409).entity("Failed. Category not removed. update all tasks before deleting the category").build();
            } else {
                LOGGER.info("Category removed");
                return Response.status(200).entity("Category removed").build();
            }
        }
    }
    @PUT
    @Path("")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateTask(Task task, @HeaderParam("token") String token, @Context HttpServletRequest request) {
        boolean authorized = userBean.isUserAuthorized(token);
        User user = userBean.getUser(token);
        TaskEntity taskEntity = taskBean.convertToEntity(task);
        if (!authorized) {
            LOGGER.info("Unauthorized access");
            return Response.status(401).entity("Unauthorized").build();
        } else {
            boolean valid = taskBean.isTaskValid(task);
            boolean categoryExists = taskBean.categoryExists(task.getCategory());
            if (!valid) {
                LOGGER.info("All elements are required");
                return Response.status(406).entity("All elements are required").build();
            }else if (!categoryExists){
                LOGGER.info("Category does not exist");
                return Response.status(404).entity("Category does not exist").build();
            }else if(!user.getUsername().equals(taskEntity.getUser().getUsername()) && user.getRole().equals("Developer")){
                LOGGER.info("Forbidden");
                return Response.status(403).entity("Forbidden").build();
            }
            String category = task.getCategory();
            CategoryEntity categoryEntity = taskBean.findCategoryByName(category);
            taskEntity.setCategory(categoryEntity);
            boolean updated = taskBean.updateTask(taskEntity);
            if(!updated){
                LOGGER.info("Failed. Task not updated");
                return Response.status(400).entity("Failed. Task not updated").build();
            } else{
                LOGGER.info("Task updated");
                return Response.status(200).entity(taskBean.convertToDto(taskEntity)).build();
            }
        }
    }
    @PUT
    @Path("/status/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response changeStatus(@HeaderParam("token") String token, @PathParam("id") String id,  String status, @Context HttpServletRequest request) {
        boolean authorized = userBean.isUserAuthorized(token);
        if (!authorized) {
            LOGGER.info("Unauthorized access");
            return Response.status(401).entity("Unauthorized").build();
        } else {
            JsonObject jsonObject = Json.createReader(new StringReader(status)).readObject();
            int newActiveStatus = jsonObject.getInt("status");
            boolean changed = taskBean.changeStatus(id, newActiveStatus);
            if (!changed) {
                LOGGER.info("Failed. Status not changed");
                return Response.status(400).entity("Failed. Status not changed").build();
            } else {
                LOGGER.info("Status changed");
                return Response.status(200).entity("Status changed").build();
            }
        }
    }

    @DELETE
    @Path("/active/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response blockTask(@HeaderParam("token") String token, @PathParam("id") String id, @Context HttpServletRequest request) {
        boolean authorized = userBean.isUserAuthorized(token);
        User user = userBean.getUser(token);
        String role = user.getRole();
        if (!authorized) {
            LOGGER.info("Unauthorized access");
            return Response.status(401).entity("Unauthorized").build();
        } else {
            boolean blocked = taskBean.blockTask(id, role);
            if (!blocked) {
                LOGGER.info("Failed. Task not blocked");
                return Response.status(400).entity("Failed. Task not blocked").build();
            } else {
                LOGGER.info("Task blocked");
                return Response.status(200).entity("Task blocked").build();
            }
        }
    }
    @GET
    @Path("/categories")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllCategories(@HeaderParam("token") String token, @Context HttpServletRequest request) {
        boolean authorized = userBean.isUserAuthorized(token);
        if (!authorized) {
            LOGGER.info("Unauthorized access");
            return Response.status(401).entity("Unauthorized").build();
        } else {
            ArrayList<Category> categoryList = new ArrayList<>();
            for (CategoryEntity categoryEntity : taskBean.getAllCategories()) {
                categoryList.add(taskBean.convertCatToDto(categoryEntity));
            }
            LOGGER.info("Categories retrieved");
            return Response.status(200).entity(categoryList).build();
        }
    }
    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTaskById(@HeaderParam("token") String token, @PathParam("id") String id, @Context HttpServletRequest request) {
        boolean authorized = userBean.isUserAuthorized(token);
        if (!authorized) {
            LOGGER.info("Unauthorized access");
            return Response.status(401).entity("Unauthorized").build();
        } else {
            Task task = taskBean.findTaskById(id);
            LOGGER.info("Task found by id");
            return Response.status(200).entity(task).build();
        }
    }
    @GET
    @Path("/creator/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCreatorByName(@HeaderParam("token") String token, @PathParam("id") String id, @Context HttpServletRequest request) {
        boolean authorized = userBean.isUserAuthorized(token);
        if (!authorized) {
            LOGGER.info("Unauthorized access");
            return Response.status(401).entity("Unauthorized").build();
        } else {
            TaskCreator creator = taskBean.findUserById(id);
            LOGGER.info("Creator found by id");
            return Response.status(200).entity(creator).build();
        }
    }

    @GET
    @Path("/statistics")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getStatistics(@HeaderParam("token") String token, @Context HttpServletRequest request) {
        boolean authorized = userBean.isUserAuthorized(token);
        if (!authorized) {
            LOGGER.info("Unauthorized access");
            return Response.status(401).entity("Unauthorized").build();
        } else {
            TasksStatisticsDto statistics = taskBean.getTasksStatistics();
            LOGGER.info("Statistics retrieved");
            return Response.status(200).entity(statistics).build();
        }
    }

}
