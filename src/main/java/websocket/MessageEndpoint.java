package websocket;
import bean.UserBean;
import dao.MessageDao;
import dao.UserDao;
import dto.MessageDto;
import entities.MessageEntity;
import entities.UserEntity;
import jakarta.ejb.EJB;
import jakarta.websocket.OnMessage;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;
import com.google.gson.Gson;

import java.util.ArrayList;

@ServerEndpoint("/chat")
public class MessageEndpoint {
    @EJB
    private MessageDao messageDao;
    @EJB
    private UserDao userDao;
    @EJB
    private UserBean userBean;

    @OnMessage
    public void onMessage(String message, Session session) {
        Gson gson = new Gson();
        MessageDto messageDto = gson.fromJson(message, MessageDto.class);
        userBean.sendMessage(messageDto);
        }
    }
