package bean;

import bean.UserBean;
import dao.UserDao;
import dto.PasswordDto;
import dto.User;
import entities.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import utilities.EncryptHelper;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserBeanTest {

    @Mock
    private UserDao userDaoMock;

    @Mock
    private EncryptHelper encryptHelperMock;

    @InjectMocks
    private UserBean userBean;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void addUser_ValidUser_AddsUser() {
        User user = new User("testUser", "Test User", "test@example.com", "password", "123456789", "https://example.com/photo.jpg", "developer");
        UserEntity userEntity = new UserEntity();
        when(encryptHelperMock.encryptPassword(user.getPassword())).thenReturn(user.getPassword()); // Mock encryptHelper behavior
        when(userDaoMock.findUserByUsername(user.getUsername())).thenReturn(null); // Mock userDao behavior
        doNothing().when(userDaoMock).persist(any(UserEntity.class)); // Mock userDao behavior

        userBean.addUser(user);

        verify(encryptHelperMock).encryptPassword(user.getPassword());
        verify(userDaoMock).persist(any(UserEntity.class));
    }

    @Test
    void getUser_ValidToken_ReturnsUser() {
        // Arrange
        String token = "validToken";
        UserEntity userEntity = new UserEntity();
        when(userDaoMock.findUserByToken(token)).thenReturn(userEntity); // Mock userDao behavior

        User result = userBean.getUser(token);

        assertNotNull(result);
        assertEquals(userEntity.getUsername(), result.getUsername());
    }
    @Test
    void findUserByUsername_ValidUsername_ReturnsUser() {
        // Arrange
        String username = "testUser";
        UserEntity userEntity = new UserEntity();
        when(userDaoMock.findUserByUsername(username)).thenReturn(userEntity); // Mock userDao behavior

        User result = userBean.findUserByUsername(username);

        assertNotNull(result);
        assertEquals(userEntity.getUsername(), result.getUsername());
    }
    @Test
    void blockUser_ValidUsername_BlocksUser() {
        // Arrange
        String username = "testUser";
        UserEntity userEntity = new UserEntity();
        userEntity.setActive(true);
        when(userDaoMock.findUserByUsername(username)).thenReturn(userEntity); // Mock userDao behavior
        doNothing().when(userDaoMock).updateUser(userEntity); // Mock userDao behavior

        boolean result = userBean.blockUser(username);

        assertTrue(result);
        assertFalse(userEntity.isActive());
        verify(userDaoMock).updateUser(userEntity);
    }

    @Test
    void blockUser_InvalidUsername_ReturnsFalse() {

        String username = "nonExistingUser";
        when(userDaoMock.findUserByUsername(username)).thenReturn(null); // Mock userDao behavior

        boolean result = userBean.blockUser(username);

        assertFalse(result);
        verify(userDaoMock, never()).updateUser(any(UserEntity.class));
    }
    @Test
    void removeUser_ValidUsername_RemovesUser() {
        // Arrange
        String username = "testUser";
        UserEntity userEntity = new UserEntity();
        when(userDaoMock.findUserByUsername(username)).thenReturn(userEntity); // Mock userDao behavior
        doNothing().when(userDaoMock).remove(userEntity); // Mock userDao behavior

        boolean result = userBean.removeUser(username);

        assertTrue(result);
        verify(userDaoMock).remove(userEntity);
    }
    @Test
    void removeUser_UserExists_RemovesUser() {

        String username = "testUser";
        UserEntity userEntity = new UserEntity();
        userEntity.setUsername(username);
        when(userDaoMock.findUserByUsername(username)).thenReturn(userEntity); // Mock userDao behavior

        boolean result = userBean.removeUser(username);

        assertTrue(result);
        verify(userDaoMock).remove(userEntity);
    }
    @Test
    void blockUser_UserDoesNotExist_ReturnsFalse() {

        String username = "nonExistingUser";
        when(userDaoMock.findUserByUsername(username)).thenReturn(null); // Mock userDao behavior

        boolean result = userBean.blockUser(username);

        assertFalse(result);
        verify(userDaoMock, never()).updateUser(any(UserEntity.class));
    }
    @Test
    void blockUser_UserExists_BlocksUser() {
        // Arrange
        String username = "testUser";
        UserEntity userEntity = new UserEntity();
        userEntity.setUsername(username);
        when(userDaoMock.findUserByUsername(username)).thenReturn(userEntity); // Mock userDao behavior

        boolean result = userBean.blockUser(username);

        assertTrue(result);
        assertFalse(userEntity.isActive());
        verify(userDaoMock).updateUser(userEntity);
    }
    @Test
    void getUsers_ReturnsListOfUsers() {
        // Arrange
        UserEntity userEntity1 = new UserEntity();
        UserEntity userEntity2 = new UserEntity();
        when(userDaoMock.findAll()).thenReturn(Arrays.asList(userEntity1, userEntity2));

        List<UserEntity> result = userBean.getUsers();

        assertNotNull(result);
        assertEquals(2, result.size());
    }
}