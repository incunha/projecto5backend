package bean;
import dto.*;
import entities.UserEntity;
import entities.CategoryEntity;
import entities.TaskEntity;

import java.awt.image.LookupOp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import dao.TaskDao;
import dao.UserDao;
import jakarta.inject.Inject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import websocket.Dashboard;
import websocket.TaskEndpoint;
import bean.UserBean;
import entities.CategoryEntity;
import entities.TaskEntity;
import jakarta.ejb.EJB;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.ejb.Stateless;
import org.apache.logging.log4j.*;
import static websocket.MessageEndpoint.getSessions;


@Singleton
public class
TaskBean {

    public TaskBean() {
    }
    @EJB
    TaskDao taskDao;
    @EJB
    UserDao userDao;
    @EJB
    Dashboard dashboard;
    @Inject
    TaskEndpoint tasks;


    private static final Logger LOGGER = LogManager.getLogger(TaskBean.class);
    public TaskBean(TaskDao taskDao) {
        this.taskDao = taskDao;
    }


    public boolean isTaskValid(Task task) {
        LOGGER.info("isTaskValid method called");
        if (task.getTitle().isBlank() || task.getDescription().isBlank() || task.getStartDate() == null || task.getEndDate() == null || task.getCategory() == null) {
            LOGGER.error("Task is not valid");
            return false;
        } else {
            LOGGER.info("Task is valid");
            return task.getTitle() != null && task.getDescription() != null && task.getStartDate() != null && task.getEndDate() != null;
        }
    }


    public ArrayList<Task> getFilteredTasks( Boolean active,String category,String username) {
        LOGGER.info("getFilteredTasks method called");
        ArrayList<Task> tasks = new ArrayList<>();
        List<TaskEntity> activeTasks = taskDao.findAllActiveTasks();
        List<TaskEntity> inactiveTasks = taskDao.findDeletedTasks();

        if(active && category == null && username==null) {
            LOGGER.info("getFilteredTasks method called with active tasks");
            for (TaskEntity taskEntity : activeTasks) {
                tasks.add(convertToDto(taskEntity));
            }
            LOGGER.info("returning active tasks");
            return tasks;
        } else if(!active && category == null && username == null) {
            LOGGER.info("getFilteredTasks method called with inactive tasks");
            for (TaskEntity taskEntity : inactiveTasks) {
                tasks.add(convertToDto(taskEntity));
            }
            LOGGER.info("returning inactive tasks");
            return tasks;


        } else if(active && category != null && username == null) {
            LOGGER.info("getFilteredTasks method called with active tasks and category and no username");
            List<TaskEntity> allActiveTasks = taskDao.findTasksByCategory2(taskDao.findCategoryByName(category), active);
            for (TaskEntity taskEntity : allActiveTasks) {
                tasks.add(convertToDto(taskEntity));
            }
            LOGGER.info("returning active tasks with category");
            return tasks;
        } else if(!active && category != null && username == null) {
            LOGGER.info("getFilteredTasks method called with inactive tasks and category and no username");
            List<TaskEntity> allTasks = taskDao.findTasksByCategory2(taskDao.findCategoryByName(category), active);
            for (TaskEntity taskEntity : allTasks) {
                tasks.add(convertToDto(taskEntity));
            }
            LOGGER.info("returning inactive tasks with category");
            return tasks;
        } else if(active && category == null && username != null )  {
            LOGGER.info("getFilteredTasks method called with active tasks with username and no category");
            List<TaskEntity> allActiveTasks = taskDao.findTasksByUser2(userDao.findUserByUsername(username),active);
            for (TaskEntity taskEntity : allActiveTasks) {
                tasks.add(convertToDto(taskEntity));
            }
            LOGGER.info("returning active tasks with username and no category");
            return tasks;
        } else if(!active && category == null && username!=null){
            LOGGER.info("getFilteredTasks method called with inactive tasks with username and no category");
            List<TaskEntity> allTasks = taskDao.findTasksByUser2(userDao.findUserByUsername(username),active);
            for (TaskEntity taskEntity : allTasks) {
                tasks.add(convertToDto(taskEntity));
            }
            LOGGER.info("returning inactive tasks with username and no category");
            return tasks;
        } else if(active && category != null && username!=null) {
            LOGGER.info("getFilteredTasks method called with active tasks with username and category");
            List<TaskEntity> allActiveTasks = taskDao.findTasksByCategory2(taskDao.findCategoryByName(category), active);
            List<TaskEntity> allActiveTasksByUser = new ArrayList<>();
            for(TaskEntity task: allActiveTasks) {
                if(task.getUser().getUsername().equals(username)) {
                    allActiveTasksByUser.add(task);
                }
            }
            for (TaskEntity taskEntity : allActiveTasksByUser) {
                tasks.add(convertToDto(taskEntity));
            }
            LOGGER.info("returning active tasks with username and category");
            return tasks;
        } else if(!active && category != null && username != null) {
            LOGGER.info("getFilteredTasks method called with inactive tasks with username and category");
            List<TaskEntity> allTasks = taskDao.findTasksByCategory2(taskDao.findCategoryByName(category), active);
            List<TaskEntity> allTasksByUser = new ArrayList<>();
            for(TaskEntity task: allTasks) {
                if(task.getUser().getUsername().equals(username)) {
                    allTasksByUser.add(task);
                }
            }
            for (TaskEntity taskEntity : allTasksByUser) {
                tasks.add(convertToDto(taskEntity));
            }
            LOGGER.info("returning inactive tasks with username and category");
            return tasks;
        } else {
            return tasks;
        }
    }

    public TaskEntity convertToEntity(dto.Task task) {
        LOGGER.info("convertToEntity method called");
        TaskEntity taskEntity = new TaskEntity();
        taskEntity.setId(task.getId());
        taskEntity.setTitle(task.getTitle());
        taskEntity.setDescription(task.getDescription());
        taskEntity.setStatus(task.getStatus());
        taskEntity.setCategory(taskDao.findCategoryByName(task.getCategory()));
        taskEntity.setStartDate(task.getStartDate());
        taskEntity.setPriority(task.getPriority());
        taskEntity.setEndDate(task.getEndDate());
        taskEntity.setUser(taskDao.findTaskById(task.getId()).getUser());
        taskEntity.setActive(true);
        taskEntity.setUser(taskDao.findTaskById(task.getId()).getUser());
        LOGGER.info("Task converted to entity");
        return taskEntity;
    }

    public TaskEntity createTaskEntity(dto.Task task, UserEntity userEntity) {
        LOGGER.info("createTaskEntity method called");
        TaskEntity taskEntity = new TaskEntity();
        taskEntity.setId(task.getId());
        taskEntity.setTitle(task.getTitle());
        taskEntity.setDescription(task.getDescription());
        taskEntity.setStatus(task.getStatus());
        taskEntity.setCategory(taskDao.findCategoryByName(task.getCategory()));
        taskEntity.setStartDate(task.getStartDate());
        taskEntity.setPriority(task.getPriority());
        taskEntity.setEndDate(task.getEndDate());
        taskEntity.setUser(userEntity);
        taskEntity.setActive(true);
        LOGGER.info("Task entity created");
        return taskEntity;
    }

    public boolean restoreTask(String id) {
        LOGGER.info("restoreTask method called");
        TaskEntity a = taskDao.findTaskById(id);
        if (a != null) {
            LOGGER.info("Task is not null");
            a.setActive(true);
            taskDao.updateTask(a);
            dashboard.send("ping");
            TaskWebsocketDto taskWebsocketDto = convertEntityToSocketDto(a);
            taskWebsocketDto.setAction("restore");
            tasks.send(taskWebsocketDto);
            LOGGER.info("Task restored");
            return true;
        }
        LOGGER.error("Task not restored");
        return false;
    }


    public Category convertCatToDto(CategoryEntity categoryEntity) {
        LOGGER.info("convertCatToDto method called");
        Category category = new Category();
        category.setId(categoryEntity.getId());
        category.setName(categoryEntity.getName());
        LOGGER.info("Category converted to dto");
        return category;
    }

    public dto.Task convertToDto(TaskEntity taskEntity) {
        LOGGER.info("convertToDto method called");
        dto.Task task = new dto.Task();
        task.setId(taskEntity.getId());
        task.setTitle(taskEntity.getTitle());
        task.setDescription(taskEntity.getDescription());
        task.setStatus(taskEntity.getStatus());
        task.setCategory(convertCatToDto(taskEntity.getCategory()).getName());
        task.setStartDate(taskEntity.getStartDate());
        task.setPriority(taskEntity.getPriority());
        task.setEndDate(taskEntity.getEndDate());
        task.setActive(taskEntity.isActive());
        task.setUsername(taskEntity.getUser().getUsername());
        LOGGER.info("Task converted to dto");
        return task;
    }

    public void addTask(TaskEntity taskEntity) {
        LOGGER.info("addTask method called");
        taskDao.createTask(taskEntity);
        dashboard.send("ping");
        TaskWebsocketDto taskSocketDto = convertEntityToSocketDto(taskEntity);
        taskSocketDto.setAction("add");
        tasks.send(taskSocketDto);
        LOGGER.info("Task sent");
    }

    public TaskWebsocketDto convertEntityToSocketDto(TaskEntity taskEntity) {
        LOGGER.info("convertEntityToSocketDto method called");
        TaskWebsocketDto taskSocketDto = new TaskWebsocketDto();
        LOGGER.info("TaskWebsocketDto created");
        taskSocketDto.setTask(convertToDto(taskEntity));
        LOGGER.info("Task converted to dto");

        return taskSocketDto;
    }


    public List<TaskEntity> getTasks() {
        LOGGER.info("getTasks method called");
        return taskDao.findAll();
    }

    public  List<TaskEntity> getTasksByUser(UserEntity userEntity) {
        LOGGER.info("getTasksByUser method called");
        return taskDao.findTasksByUser(userEntity);
    }

    public boolean deleteAllTasksByUser(UserEntity userEntity) {
        LOGGER.info("deleteAllTasksByUser method called");
        List<TaskEntity> tasks = taskDao.findTasksByUser(userEntity);
        LOGGER.info("Tasks found");
        for(TaskEntity task: tasks){
            task.setActive(false);
        }
        LOGGER.info("Tasks set to inactive");
        return true;
    }


    public Task findTaskById(String id) {
        LOGGER.info("findTaskById method called");
        return convertToDto(taskDao.findTaskById(id));
    }

    public TaskCreator findUserById(String id) {
        LOGGER.info("findUserById method called");
        TaskEntity taskEntity = taskDao.findTaskById(id);
        TaskCreator taskCreator = new TaskCreator();
        taskCreator.setUsername(taskEntity.getUser().getUsername());
        taskCreator.setName(taskEntity.getUser().getName());
        LOGGER.info("TaskCreator created");
        return taskCreator;
    }

    public boolean categoryExists(String name) {
        LOGGER.info("categoryExists method called");
        if(taskDao.findCategoryByName(name) != null) {
            LOGGER.info("Category exists");
            return true;
        }
        LOGGER.info("Category does not exist");
        return false;
    }

    public boolean updateCategory(CategoryEntity categoryEntity) {
        LOGGER.info("updateCategory method called");
        CategoryEntity a = taskDao.findCategoryById(categoryEntity.getId());
        if (a != null) {
            LOGGER.info("Category is not null");
            a.setName(categoryEntity.getName());
            taskDao.updateCategory(a);
            LOGGER.info("Category updated");
            return true;
        }
        return false;
    }

    public void createCategory(String name, String creator) {
        LOGGER.info("createCategory method called");
        CategoryEntity categoryEntity = new CategoryEntity();
        LOGGER.info("CategoryEntity created");
        categoryEntity.setName(name);
        categoryEntity.setCreator(creator);
        LOGGER.info("Category created");
        taskDao.createCategory(categoryEntity);
    }

    public boolean removeCategory(String name) {
        LOGGER.info("removeCategory method called");
        List<TaskEntity> tasks = taskDao.findAll();
        LOGGER.info("Tasks found");
        List<TaskEntity> tasksByCategory = new ArrayList<>();
        for(TaskEntity task : tasks) {
            if(task.getCategory().getName().equals(name)) {
                tasksByCategory.add(task);
            }
        }
        LOGGER.info("Tasks by category found");
        if(tasksByCategory.isEmpty()) {
            LOGGER.info("Tasks by category is empty");
            taskDao.removeCategory(taskDao.findCategoryByName(name));
            LOGGER.info("Category removed");
            return true;
        }
        LOGGER.info("Tasks by category is not empty");
        return false;
    }

    public CategoryEntity findCategoryByName(String name) {
        LOGGER.info("findCategoryByName method called");
        return taskDao.findCategoryByName(name);
    }

    public CategoryEntity findCategoryById(int id) {
        LOGGER.info("findCategoryById method called");
        return taskDao.findCategoryById(id);
    }

    public boolean blockTask(String id,String role) {
        LOGGER.info("blockTask method called");
        TaskEntity a = taskDao.findTaskById(id);
        if (a != null) {
            LOGGER.info("Task is not null");
            if(a.isActive() && !role.equals("developer")) {
                LOGGER.info("Task is active and role is not developer");
                a.setActive(false);
                taskDao.updateTask(a);
                dashboard.send("ping");
                TaskWebsocketDto taskWebsocketDto = convertEntityToSocketDto(a);
                taskWebsocketDto.setAction("block");
                taskWebsocketDto.getTask().setUsername(a.getUser().getUsername());
                tasks.send(taskWebsocketDto);

            }else if(!a.isActive()&& role.equals("Owner")) {
                LOGGER.info("Task is not active and role is owner");
                taskDao.remove(a);
                dashboard.send("ping");
                TaskWebsocketDto taskWebsocketDto = convertEntityToSocketDto(a);
                taskWebsocketDto.setAction("delete");
                tasks.send(taskWebsocketDto);
            }
            LOGGER.info("Task blocked");
            return true;
        }
        LOGGER.error("Task not blocked");
        return false;
    }

    public ArrayList<Task> getAllActiveTasks (){
        ArrayList<Task> activeTasks= new ArrayList<>();
        List<TaskEntity> activeEntities = taskDao.findAllActiveTasks();
        for(TaskEntity entity : activeEntities){
            activeTasks.add(convertToDto(entity));
        }
        return activeTasks;
    }

    public ArrayList<Task> getAllInactiveTasks (){
        ArrayList<Task> inactiveTasks= new ArrayList<>();
        List<TaskEntity> inactiveEntities = taskDao.findAllInactiveTasks();
        for(TaskEntity entity : inactiveEntities){
            inactiveTasks.add(convertToDto(entity));
        }
        return inactiveTasks;
    }


    public boolean updateTask(TaskEntity task) {
        LOGGER.info("updateTask method called");
        TaskEntity a = taskDao.findTaskById(task.getId());
        if (a != null) {
            LOGGER.info("Task is not null");
            a.setTitle(task.getTitle());
            a.setDescription(task.getDescription());
            a.setPriority(task.getPriority());
            a.setStatus(task.getStatus());
            a.setStartDate(task.getStartDate());
            a.setEndDate(task.getEndDate());
            a.setCategory(task.getCategory());
            taskDao.updateTask(a);
            dashboard.send("ping");
            TaskWebsocketDto taskWebsocketDto = convertEntityToSocketDto(a);
            taskWebsocketDto.setAction("update");
            tasks.send(taskWebsocketDto);
            LOGGER.info("Task updated");
            return true;
        }
        LOGGER.error("Task not updated");
        return false;
    }

    public boolean changeStatus(String id, int status) {
        LOGGER.info("changeStatus method called");
        TaskEntity a = taskDao.findTaskById(id);
        if (a != null) {
            LOGGER.info("Task is not null");
            if(a.getStatus() == 30){
                a.setConclusionDate(null);
            }
            LOGGER.info("Task status changed");
            a.setStatus(status);
            if(status == 30){
                a.setConclusionDate(LocalDate.now());
            }
            LOGGER.info("Task status changed");
            if(status == 20 && a.getDoingDate() == null){
                a.setDoingDate(LocalDate.now());
            }
            LOGGER.info("Task status changed");
            taskDao.updateTask(a);
            dashboard.send("ping");
            TaskWebsocketDto taskWebsocketDto = convertEntityToSocketDto(a);
            taskWebsocketDto.setAction("status");
            tasks.send(taskWebsocketDto);
            LOGGER.info("Task status changed");
            return true;

        }
        LOGGER.error("Task status not changed");
        return false;
    }

    public List<CategoryEntity> getAllCategories() {
        LOGGER.info("getAllCategories method called");
        return taskDao.findAllCategories();
    }

    public void setTaskDao(TaskDao taskDao) {
        LOGGER.info("setTaskDao method called");
        this.taskDao = taskDao;
    }

    public void setUserDao(UserDao userDao) {
        LOGGER.info("setUserDao method called");
        this.userDao = userDao;
    }

    public void setInitialId(Task task){
        LOGGER.info("setInitialId method called");
        task.setId("Task" + System.currentTimeMillis());}

    public void createDefaultCategories(){
        LOGGER.info("createDefaultCategories method called");
        if(taskDao.findCategoryByName("Testing") == null){
            CategoryEntity categoryEntity = new CategoryEntity();
            categoryEntity.setName("Testing");
            categoryEntity.setCreator("System");
            taskDao.createCategory(categoryEntity);
            LOGGER.info("Category created");
        }
        if(taskDao.findCategoryByName("Backend") == null){
            LOGGER.info("Category created");
            CategoryEntity categoryEntity = new CategoryEntity();
            categoryEntity.setName("Backend");
            categoryEntity.setCreator("System");
            taskDao.createCategory(categoryEntity);
            LOGGER.info("Category created");
        }
        if(taskDao.findCategoryByName("Frontend") == null){
            LOGGER.info("Category created");
            CategoryEntity categoryEntity = new CategoryEntity();
            categoryEntity.setName("Frontend");
            categoryEntity.setCreator("System");
            taskDao.createCategory(categoryEntity);
            LOGGER.info("Category created");
        }
}
    public void createDefaultTasks() {
        LOGGER.info("createDefaultTasks method called");
    if (taskDao.findTaskById("Task1") == null) {
        TaskEntity taskEntity = new TaskEntity();
        taskEntity.setId("Task1");
        taskEntity.setTitle("KAIZEN");
        taskEntity.setDescription("Continuous improvement");
        taskEntity.setStatus(20);
        taskEntity.setCategory(taskDao.findCategoryByName("Testing"));
        taskEntity.setStartDate(LocalDate.now());
        taskEntity.setPriority(100);
        taskEntity.setEndDate(LocalDate.of(2199, 12, 31));
        taskEntity.setUser(userDao.findUserByUsername("jony"));
        taskEntity.setActive(true);
        taskDao.createTask(taskEntity);
        LOGGER.info("Task created");
    }
    if (taskDao.findTaskById("Task2") == null) {
        TaskEntity taskEntity = new TaskEntity();
        taskEntity.setId("Task2");
        taskEntity.setTitle("Refactor");
        taskEntity.setDescription("Refactor the code");
        taskEntity.setStatus(10);
        taskEntity.setCategory(taskDao.findCategoryByName("Backend"));
        taskEntity.setStartDate(LocalDate.now());
        taskEntity.setPriority(200);
        taskEntity.setEndDate(LocalDate.of(2199, 12, 31));
        taskEntity.setUser(userDao.findUserByUsername("tony"));
        taskEntity.setActive(true);
        taskDao.createTask(taskEntity);
        LOGGER.info("Task created");
    }
    if (taskDao.findTaskById("Task3") == null) {
        TaskEntity taskEntity = new TaskEntity();
        taskEntity.setId("Task3");
        taskEntity.setTitle("Create new page");
        taskEntity.setDescription("Create a new page");
        taskEntity.setStatus(30);
        taskEntity.setCategory(taskDao.findCategoryByName("Frontend"));
        taskEntity.setStartDate(LocalDate.now());
        taskEntity.setPriority(300);
        taskEntity.setEndDate(LocalDate.of(2199, 12, 31));
        taskEntity.setUser(userDao.findUserByUsername("ju"));
        taskEntity.setActive(true);
        taskDao.createTask(taskEntity);
        LOGGER.info("Task created");
    }
}

    public TasksStatisticsDto getTasksStatistics() {
        LOGGER.info("getTasksStatistics method called");
        TasksStatisticsDto taskStatisticsDto = new TasksStatisticsDto();
        taskStatisticsDto.setTotaDoneTasks(taskDao.findTasksByStatus(30).toArray().length);
        taskStatisticsDto.setTotalTasks(taskDao.findAll().size());
        taskStatisticsDto.setTotalDoingTasks(taskDao.findTasksByStatus(20).toArray().length);
        taskStatisticsDto.setTotalToDoTasks(taskDao.findTasksByStatus(10).toArray().length);
        taskStatisticsDto.setAverageTaskTime(getAverageTaskTime());
        taskStatisticsDto.setAverageTasksPerUser(averageTasksPerUser());
        taskStatisticsDto.setTasksByCategory(getTasksByCategory());
        taskStatisticsDto.setTasksCompletedByDate(taskDao.getTasksCompletedByDate());
        LOGGER.info("Tasks statistics retrieved");
        return taskStatisticsDto;
    }

    public HashMap<String,Long> getTasksByCategory() {
        HashMap <String,Long> tasksByCategory = taskDao.getTaskCountByCategory();
        LOGGER.info("Tasks by category retrieved");
        return tasksByCategory;
    }

    public int getAverageTaskTime() {
        LOGGER.info("getAverageTaskTime method called");
        List<TaskEntity> tasks = taskDao.findAll();
        int total = 0;
        int count = 0;
        for (TaskEntity task : tasks) {
            if (task.getConclusionDate() != null) {
                if (task.getDoingDate() == null) {
                    total += task.getStartDate().until(task.getConclusionDate()).getDays();
                    count++;
                }else{
                    total += task.getDoingDate().until(task.getConclusionDate()).getDays();
                    count++;
                }
            }
        }
        if (count == 0) {
            LOGGER.error("No tasks found");
            return 0;
        }
        LOGGER.info("Average task time retrieved");
        return total / count;
    }

    public double averageTasksPerUser() {
        LOGGER.info("averageTasksPerUser method called");
        List<UserEntity> users = userDao.findAll();
        System.out.println(users.size());
        List<TaskEntity> tasks = taskDao.findAll();
        System.out.println(tasks.size());
        System.out.println(tasks.size() / users.size());
        LOGGER.info("Average tasks per user retrieved");
        return  (double) tasks.size() / users.size();
    }


}

