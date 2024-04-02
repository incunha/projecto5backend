package dao;

import entities.CategoryEntity;
import entities.TaskEntity;
import entities.UserEntity;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;

import java.util.ArrayList;
import java.util.List;

@Stateless
public class TaskDao extends AbstractDao<TaskEntity>{
    @PersistenceContext
    private EntityManager em;
    public TaskDao() {
        super(TaskEntity.class);
    }
    private static final long serialVersionUID = 1L;

    public TaskEntity findTaskById(int id) {
        try {
            return (TaskEntity) em.createNamedQuery("Task.findTaskById").setParameter("id", id)
                    .getSingleResult();

        } catch (NoResultException e) {
            return null;
        }

    }

    public ArrayList<TaskEntity> findTaskByUser(UserEntity userEntity) {
        try {
            ArrayList<TaskEntity> taskEntityEntities = (ArrayList<TaskEntity>) em.createNamedQuery("Task.findTaskByUser").setParameter("user", userEntity).getResultList();
            return taskEntityEntities;
        } catch (Exception e) {
            return null;
        }
    }
    public TaskEntity createTask(TaskEntity taskEntity) {
        em.persist(taskEntity);
        return taskEntity;
    }
    public String findCreatorByName(String name){
        try {
            return (String) em.createNamedQuery("Category.findCreatorByName").setParameter("name", name)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
    public List<TaskEntity> findTasksByUser(UserEntity userEntity) {
        try {
            List<TaskEntity> taskEntityEntities = (List<TaskEntity>) em.createNamedQuery("Task.findTaskByUser").setParameter("user", userEntity).getResultList();
            return taskEntityEntities;
        } catch (Exception e) {
            return null;
        }
    }
    public CategoryEntity findCategoryByName(String name){
        System.out.println("nome da categoria: " + name);
        try {
            return (CategoryEntity) em.createNamedQuery("Category.findCategoryByName").setParameter("name", name).getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
    public void removeCategory(CategoryEntity categoryEntity) {
        em.remove(categoryEntity);
    }
    public void createCategory(CategoryEntity categoryEntity) {
        em.persist(categoryEntity);
    }
    public void updateCategory(CategoryEntity categoryEntity) {
        em.merge(categoryEntity);
    }
    public CategoryEntity findCategoryById(int id) {
        try {
            return (CategoryEntity) em.createNamedQuery("Category.findCategoryById").setParameter("id", id).getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
    public void updateTask(TaskEntity taskEntity) {
        em.merge(taskEntity);
    }
    public TaskEntity findTaskById(String id) {
        try {
            return (TaskEntity) em.createNamedQuery("Task.findTaskById").setParameter("id", id)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
    public List<TaskEntity> findTasksByCategory(String category) {
        try {
           return  (List<TaskEntity>) em.createNamedQuery("Task.findTaskByCategory").setParameter("category", category).getResultList();

        } catch (Exception e) {
            return null;
        }
    }
    public List<TaskEntity> findBlockedTasks() {
        try {
            List<TaskEntity> taskEntityEntities = (List<TaskEntity>) em.createNamedQuery("Task.findBlockedTasks").getResultList();
            return taskEntityEntities;
        } catch (Exception e) {
            return null;
        }
    }
    public List<CategoryEntity> findAllCategories() {
        try {
            List<CategoryEntity> categoryEntities = (List<CategoryEntity>) em.createNamedQuery("Category.findAll").getResultList();
            return categoryEntities;
        } catch (Exception e) {
            return null;
        }
    }
    public List<TaskEntity> findAllActiveTasks(){
        try{
            List<TaskEntity> activeTasks = (List<TaskEntity>) em.createNamedQuery("Task.findAllActiveTasks").getResultList();
            return activeTasks;
        }catch (Exception e){
            return null;
        }
    }

    public List<TaskEntity> findAllInactiveTasks(){
        try{
            List<TaskEntity> inactiveTasks = (List<TaskEntity>) em.createNamedQuery("Task.findAllInactiveTasks").getResultList();
            return inactiveTasks;
        }catch (Exception e){
            return null;
        }
    }


}
