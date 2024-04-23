package bean;
import dto.*;
import entities.UserEntity;
import entities.CategoryEntity;
import entities.TaskEntity;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import dao.TaskDao;
import dao.UserDao;
import bean.UserBean;
import entities.CategoryEntity;
import entities.TaskEntity;
import jakarta.ejb.EJB;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.ejb.Stateless;
import org.apache.logging.log4j.LogManager;
import static websocket.MessageEndpoint.getSessions;


@Singleton

public class
TaskBean {
    private static final org.apache.logging.log4j.Logger logger = LogManager.getLogger(TaskBean.class);
    public TaskBean() {
    }
    @EJB
    TaskDao taskDao;
    @EJB
    UserDao userDao;

    public TaskBean(TaskDao taskDao) {
        this.taskDao = taskDao;
    }

    public boolean isTaskValid(Task task) {
        if (task.getTitle().isBlank() || task.getDescription().isBlank() || task.getStartDate() == null || task.getEndDate() == null || task.getCategory() == null) {
            return false;
        } else {
            return task.getTitle() != null && task.getDescription() != null && task.getStartDate() != null && task.getEndDate() != null;
        }
    }


    public ArrayList<Task> getFilteredTasks( Boolean active,String category,String username) {
        ArrayList<Task> tasks = new ArrayList<>();
        List<TaskEntity> activeTasks = taskDao.findAllActiveTasks();
        List<TaskEntity> inactiveTasks = taskDao.findDeletedTasks();

        if(active && category == null && username==null) {
            for (TaskEntity taskEntity : activeTasks) {
                tasks.add(convertToDto(taskEntity));
            }
            return tasks;
        } else if(!active && category == null && username == null) {
            for (TaskEntity taskEntity : inactiveTasks) {
                tasks.add(convertToDto(taskEntity));
            }
            return tasks;


        } else if(active && category != null && username == null) {
            List<TaskEntity> allActiveTasks = taskDao.findTasksByCategory2(taskDao.findCategoryByName(category), active);
            for (TaskEntity taskEntity : allActiveTasks) {
                tasks.add(convertToDto(taskEntity));
            }
            return tasks;
        } else if(!active && category != null && username == null) {
            List<TaskEntity> allTasks = taskDao.findTasksByCategory2(taskDao.findCategoryByName(category), active);
            for (TaskEntity taskEntity : allTasks) {
                tasks.add(convertToDto(taskEntity));
            }
            return tasks;
        } else if(active && category == null && username != null )  {
            List<TaskEntity> allActiveTasks = taskDao.findTasksByUser2(userDao.findUserByUsername(username),active);
            for (TaskEntity taskEntity : allActiveTasks) {
                tasks.add(convertToDto(taskEntity));
            }
            return tasks;
        } else if(!active && category == null && username!=null){
            List<TaskEntity> allTasks = taskDao.findTasksByUser2(userDao.findUserByUsername(username),active);
            for (TaskEntity taskEntity : allTasks) {
                tasks.add(convertToDto(taskEntity));
            }
            return tasks;
        } else if(active && category != null && username!=null) {
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
            return tasks;
        } else if(!active && category != null && username != null) {
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
            return tasks;
        } else {
            return tasks;
        }

    }

    public TaskEntity convertToEntity(dto.Task task) {
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
        return taskEntity;
    }

    public TaskEntity createTaskEntity(dto.Task task, UserEntity userEntity) {
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
        return taskEntity;
    }

    public boolean restoreTask(String id) {
        TaskEntity a = taskDao.findTaskById(id);
        if (a != null) {
            a.setActive(true);
            taskDao.updateTask(a);
            return true;
        }
        return false;
    }

    public CategoryEntity convertCatToEntity(Category category) {
        CategoryEntity categoryEntity= taskDao.findCategoryById(category.getId());
        categoryEntity.setName(category.getName());
        return categoryEntity;
    }
    public Category convertCatToDto(CategoryEntity categoryEntity) {
        Category category = new Category();
        category.setId(categoryEntity.getId());
        category.setName(categoryEntity.getName());
        return category;
    }

    public dto.Task convertToDto(TaskEntity taskEntity) {
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
        return task;
    }

    public void addTask(TaskEntity taskEntity) {
        taskDao.createTask(taskEntity);
        

    }
    public List<TaskEntity> getTasks() {
        return taskDao.findAll();
    }

    public  List<TaskEntity> getTasksByUser(UserEntity userEntity) {
        return taskDao.findTasksByUser(userEntity);
    }

    public boolean deleteAllTasksByUser(UserEntity userEntity) {
        List<TaskEntity> tasks = taskDao.findTasksByUser(userEntity);
        for(TaskEntity task: tasks){
            task.setActive(false);
        }
        return true;
    }

    public List<Task> convertToDtoList(List<TaskEntity> taskEntities) {
        List<Task> tasks = new ArrayList<>();
        for (TaskEntity taskEntity : taskEntities) {
            tasks.add(convertToDto(taskEntity));
        }
        return tasks;
    }
    public Task findTaskById(String id) {
        return convertToDto(taskDao.findTaskById(id));
    }

    public TaskCreator findUserById(String id) {
    TaskEntity taskEntity = taskDao.findTaskById(id);
        TaskCreator taskCreator = new TaskCreator();
        taskCreator.setUsername(taskEntity.getUser().getUsername());
        taskCreator.setName(taskEntity.getUser().getName());
        return taskCreator;
    }

    public boolean categoryExists(String name) {
        if(taskDao.findCategoryByName(name) != null) {
            return true;
        }
        return false;
    }

    public boolean updateCategory(CategoryEntity categoryEntity) {
        CategoryEntity a = taskDao.findCategoryById(categoryEntity.getId());
        if (a != null) {
            a.setName(categoryEntity.getName());
            taskDao.updateCategory(a);
            return true;
        }
        return false;
    }

    public void createCategory(String name, String creator) {
        CategoryEntity categoryEntity = new CategoryEntity();
        categoryEntity.setName(name);
        categoryEntity.setCreator(creator);
        taskDao.createCategory(categoryEntity);
    }

    public boolean removeCategory(String name) {
        List<TaskEntity> tasks = taskDao.findAll();
        List<TaskEntity> tasksByCategory = new ArrayList<>();
        for(TaskEntity task : tasks) {
            if(task.getCategory().getName().equals(name)) {
                tasksByCategory.add(task);
            }
        }
        if(tasksByCategory.isEmpty()) {
            taskDao.removeCategory(taskDao.findCategoryByName(name));
            return true;
        }
        return false;
    }
    public CategoryEntity findCategoryByName(String name) {
        return taskDao.findCategoryByName(name);
    }

    public CategoryEntity findCategoryById(int id) {
        return taskDao.findCategoryById(id);
    }

    public boolean blockTask(String id,String role) {
        TaskEntity a = taskDao.findTaskById(id);
        if (a != null) {
            if(a.isActive() && !role.equals("developer")) {
                a.setActive(false);
                taskDao.updateTask(a);
            }else if(!a.isActive()&& role.equals("Owner")) {
                taskDao.remove(a);
            }
            return true;
        }
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
        TaskEntity a = taskDao.findTaskById(task.getId());
        if (a != null) {
            a.setTitle(task.getTitle());
            a.setDescription(task.getDescription());
            a.setPriority(task.getPriority());
            a.setStatus(task.getStatus());
            a.setStartDate(task.getStartDate());
            a.setEndDate(task.getEndDate());
            a.setCategory(task.getCategory());
            taskDao.updateTask(a);
            return true;
        }
        return false;
    }

    public boolean changeStatus(String id, int status) {
        TaskEntity a = taskDao.findTaskById(id);
        if (a != null) {
            if(a.getStatus() == 30){
                a.setConclusionDate(null);
            }
            a.setStatus(status);
            if(status == 30){
                a.setConclusionDate(LocalDate.now());
            }
            if(status == 20 && a.getDoingDate() == null){
                a.setDoingDate(LocalDate.now());
            }
            taskDao.updateTask(a);
            return true;

        }
        return false;
    }

    public List<CategoryEntity> getAllCategories() {
        return taskDao.findAllCategories();
    }

    public void setTaskDao(TaskDao taskDao) {
        this.taskDao = taskDao;
    }

    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    public void setInitialId(Task task){
        task.setId("Task" + System.currentTimeMillis());}

    public void createDefaultCategories(){
        if(taskDao.findCategoryByName("Testing") == null){
            CategoryEntity categoryEntity = new CategoryEntity();
            categoryEntity.setName("Testing");
            categoryEntity.setCreator("System");
            taskDao.createCategory(categoryEntity);
        }
        if(taskDao.findCategoryByName("Backend") == null){
            CategoryEntity categoryEntity = new CategoryEntity();
            categoryEntity.setName("Backend");
            categoryEntity.setCreator("System");
            taskDao.createCategory(categoryEntity);
        }
        if(taskDao.findCategoryByName("Frontend") == null){
            CategoryEntity categoryEntity = new CategoryEntity();
            categoryEntity.setName("Frontend");
            categoryEntity.setCreator("System");
            taskDao.createCategory(categoryEntity);
        }
}
    public void createDefaultTasks() {
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
        taskEntity.setUser(userDao.findUserByUsername("admin"));
        taskEntity.setActive(true);
        taskDao.createTask(taskEntity);
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
        taskEntity.setUser(userDao.findUserByUsername("admin"));
        taskEntity.setActive(true);
        taskDao.createTask(taskEntity);
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
        taskEntity.setUser(userDao.findUserByUsername("admin"));
        taskEntity.setActive(true);
        taskDao.createTask(taskEntity);
    }
}

    public TasksStatisticsDto getTasksStatistics() {
        TasksStatisticsDto taskStatisticsDto = new TasksStatisticsDto();
        taskStatisticsDto.setTotaDoneTasks(taskDao.findTasksByStatus(30).toArray().length);
        taskStatisticsDto.setTotalTasks(taskDao.findAll().size());
        taskStatisticsDto.setTotalDoingTasks(taskDao.findTasksByStatus(20).toArray().length);
        taskStatisticsDto.setTotalToDoTasks(taskDao.findTasksByStatus(10).toArray().length);
        taskStatisticsDto.setAverageTaskTime(getAverageTaskTime());
        taskStatisticsDto.setAverageTasksPerUser(averageTasksPerUser());
        taskStatisticsDto.setTasksByCategory(getTasksByCategory());
        taskStatisticsDto.setTasksCompletedByDate(taskDao.getTasksCompletedByDate());
        return taskStatisticsDto;
    }

    public HashMap<String,Long> getTasksByCategory() {
        HashMap <String,Long> tasksByCategory = taskDao.getTaskCountByCategory();

        return tasksByCategory;
    }

    public int getAverageTaskTime() {
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
            return 0;
        }
        return total / count;
    }

    public double averageTasksPerUser() {
        List<UserEntity> users = userDao.findAll();
        List<TaskEntity> tasks = taskDao.findAll();
        return tasks.size() / users.size();
    }




}

