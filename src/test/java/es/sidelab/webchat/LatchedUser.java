package es.sidelab.webchat;

import es.codeurjc.webchat.Chat;
import es.codeurjc.webchat.User;

import java.util.concurrent.CountDownLatch;

public class LatchedUser implements User {

    private final CountDownLatch latch;
    private User wrapperUser;

    public LatchedUser(User user, CountDownLatch latch) {
        this.wrapperUser = user;
        this.latch = latch;
    }

    @Override
    public String getName() {
        return wrapperUser.getName();
    }

    @Override
    public String getColor() {
        return wrapperUser.getColor();
    }

    @Override
    public void newChat(Chat chat) {
        wrapperUser.newChat(chat);
    }

    @Override
    public void chatClosed(Chat chat) {
        wrapperUser.chatClosed(chat);
    }

    @Override
    public void newUserInChat(Chat chat, User user) {
        wrapperUser.newUserInChat(chat, user);
    }

    @Override
    public void userExitedFromChat(Chat chat, User user) {
        wrapperUser.userExitedFromChat(chat, user);

    }

    @Override
    public void newMessage(Chat chat, User user, String message) {
        wrapperUser.newMessage(chat, user, message);
        System.out.println(this.toString());
        latch.countDown();
    }

    @Override
    public String toString() {
        return "Latched {" +
                "latch=" + latch.getCount() +
                ", wrapperUser=" + wrapperUser +
                '}';
    }
}
