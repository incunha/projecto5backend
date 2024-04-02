package bean;

import dto.Task;
import entities.CategoryEntity;
import entities.TaskEntity;
import dao.TaskDao;
import entities.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TaskBeanTest {
    TaskBean taskBean;
    TaskDao taskDaoMock;

    @BeforeEach
    void setUp() {
        taskDaoMock = mock(TaskDao.class);
        taskBean = new TaskBean(taskDaoMock);
    }

    @Test
    void convertToEntity() {
        Task taskDto = new Task();
        taskDto.setId("123");
        taskDto.setTitle("Test Task");
        taskDto.setDescription("Test Description");
        taskDto.setStatus(1);
        taskDto.setPriority(2);
        taskDto.setStartDate(LocalDate.of(2024, 3, 1));
        taskDto.setEndDate(LocalDate.of(2024, 3, 31));
        taskDto.setCategory("Test Category");

        CategoryEntity categoryEntity = new CategoryEntity();
        categoryEntity.setId(1);
        categoryEntity.setName("Test Category");

        TaskEntity taskEntity = new TaskEntity();
        taskEntity.setId("123");
        taskEntity.setTitle("Test Task");
        taskEntity.setDescription("Test Description");
        taskEntity.setStatus(1);
        taskEntity.setPriority(2);
        taskEntity.setStartDate(LocalDate.of(2024, 3, 1));
        taskEntity.setEndDate(LocalDate.of(2024, 3, 31));
        taskEntity.setCategory(categoryEntity);

        when(taskDaoMock.findTaskById("123")).thenReturn(taskEntity);

        when(taskDaoMock.findCategoryByName("Test Category")).thenReturn(categoryEntity);

        TaskEntity convertedEntity = taskBean.convertToEntity(taskDto);

        assertEquals("123", convertedEntity.getId());
        assertEquals("Test Task", convertedEntity.getTitle());
        assertEquals("Test Description", convertedEntity.getDescription());
        assertEquals(1, convertedEntity.getStatus());
        assertEquals(2, convertedEntity.getPriority());
        assertEquals(LocalDate.of(2024, 3, 1), convertedEntity.getStartDate());
        assertEquals(LocalDate.of(2024, 3, 31), convertedEntity.getEndDate());
        assertEquals("Test Category", convertedEntity.getCategory().getName());
    }


    @Test
    void convertToDto() {

        TaskEntity taskEntity = new TaskEntity();
        taskEntity.setId("123");
        taskEntity.setTitle("Test Task");
        taskEntity.setDescription("Test Description");
        taskEntity.setStatus(1);
        taskEntity.setPriority(2);
        taskEntity.setStartDate(LocalDate.of(2024, 3, 1));
        taskEntity.setEndDate(LocalDate.of(2024, 3, 31));

        CategoryEntity categoryEntity = new CategoryEntity();
        categoryEntity.setName("Test Category");
        taskEntity.setCategory(categoryEntity);

        Task taskDto = taskBean.convertToDto(taskEntity);

        assertEquals("123", taskDto.getId());
        assertEquals("Test Task", taskDto.getTitle());
        assertEquals("Test Description", taskDto.getDescription());
        assertEquals(1, taskDto.getStatus());
        assertEquals(2, taskDto.getPriority());
        assertEquals(LocalDate.of(2024, 3, 1), taskDto.getStartDate());
        assertEquals(LocalDate.of(2024, 3, 31), taskDto.getEndDate());
        assertEquals("Test Category", taskDto.getCategory());
    }

    @Test
    void findTaskById() {

        TaskEntity taskEntity = new TaskEntity();
        taskEntity.setId("123");
        taskEntity.setTitle("Test Task");
        taskEntity.setDescription("Test Description");
        taskEntity.setStatus(1);
        taskEntity.setPriority(2);
        taskEntity.setStartDate(LocalDate.of(2024, 3, 1));
        taskEntity.setEndDate(LocalDate.of(2024, 3, 31));

        CategoryEntity categoryEntity = new CategoryEntity();
        categoryEntity.setName("Test Category");
        taskEntity.setCategory(categoryEntity);


        when(taskDaoMock.findTaskById("123")).thenReturn(taskEntity);

        Task taskDto = taskBean.findTaskById("123");

        assertEquals("123", taskDto.getId());
        assertEquals("Test Task", taskDto.getTitle());
        assertEquals("Test Description", taskDto.getDescription());
        assertEquals(1, taskDto.getStatus());
        assertEquals(2, taskDto.getPriority());
        assertEquals(LocalDate.of(2024, 3, 1), taskDto.getStartDate());
        assertEquals(LocalDate.of(2024, 3, 31), taskDto.getEndDate());
        assertEquals("Test Category", taskDto.getCategory());
    }
    @Test
    void isTaskValid_ValidTask_ReturnsTrue() {

        Task validTask = new Task();
        validTask.setTitle("Valid Title");
        validTask.setDescription("Valid Description");
        validTask.setStartDate(LocalDate.of(2024, 3, 1));
        validTask.setEndDate(LocalDate.of(2024, 3, 31));
        validTask.setCategory("Valid Category");

        boolean isValid = taskBean.isTaskValid(validTask);

        assertTrue(isValid);
    }
    @Test
    void isTaskValid_InvalidTask_ReturnsFalse() {

        Task invalidTask = new Task();
        invalidTask.setTitle("");
        invalidTask.setDescription("Invalid Description");
        invalidTask.setStartDate(LocalDate.of(2024, 3, 1));
        invalidTask.setEndDate(LocalDate.of(2024, 3, 31));
        invalidTask.setCategory("Invalid Category");

        boolean isValid = taskBean.isTaskValid(invalidTask);

        assertFalse(isValid);
    }
    @Test
    void restoreTask_TaskFound_ReturnsTrue() {

        TaskEntity taskEntity = new TaskEntity();
        taskEntity.setId("123");

        when(taskDaoMock.findTaskById("123")).thenReturn(taskEntity);

        boolean result = taskBean.restoreTask("123");

        assertTrue(result);
    }
    @Test
    void restoreTask_TaskNotFound_ReturnsFalse() {

        when(taskDaoMock.findTaskById("123")).thenReturn(null);

        boolean result = taskBean.restoreTask("123");

        assertFalse(result);
    }
    @Test
    void blockTask_TaskFoundAndActive_ReturnsTrue() {

        TaskEntity taskEntity = new TaskEntity();
        taskEntity.setId("123");
        taskEntity.setActive(true);

        when(taskDaoMock.findTaskById("123")).thenReturn(taskEntity);

        boolean result = taskBean.blockTask("123", "role");

        assertTrue(result);
    }
    @Test
    void blockTask_TaskNotFound_ReturnsFalse() {

        when(taskDaoMock.findTaskById("123")).thenReturn(null);

        boolean result = taskBean.blockTask("123", "role");

        assertFalse(result);
    }
    @Test
    void createCategory_CategoryDoesNotExist_CreatesCategoryAndReturnsTrue() {

        when(taskDaoMock.findCategoryByName(anyString())).thenReturn(null);

        taskBean.createCategory("Test Category", "creator");

        verify(taskDaoMock).createCategory(any());
    }
}