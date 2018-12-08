package es.sidelab.webchat;

import es.codeurjc.webchat.Chat;
import es.codeurjc.webchat.User;

import java.util.concurrent.CountDownLatch;

public class LatchedUser implements User {

    private final CountDownLatch latch;
    private User user;

    public LatchedUser(User user, CountDownLatch latch) {
        this.user = user;
        this.latch = latch;
    }

    @Override
    public String getName() {
        return user.getName();
    }

    @Override
    public String getColor() {
        return user.getColor();
    }

    @Override
    public void newChat(Chat chat) {
        user.newChat(chat);
    }

    @Override
    public void chatClosed(Chat chat) {
        user.chatClosed(chat);
    }

    @Override
    public void newUserInChat(Chat chat, User user) {
        user.newUserInChat(chat, user);
    }

    @Override
    public void userExitedFromChat(Chat chat, User user) {
        user.userExitedFromChat(chat, user);
    }

    @Override
    public void newMessage(Chat chat, User user, String message) {
        user.newMessage(chat, user, message);
        latch.countDown();
    }
}
