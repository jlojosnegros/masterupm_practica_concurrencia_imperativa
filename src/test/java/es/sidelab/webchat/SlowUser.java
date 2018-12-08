package es.sidelab.webchat;

import es.codeurjc.webchat.Chat;
import es.codeurjc.webchat.User;

public class SlowUser implements User {


    private static final long DefaultDelay = 1000; //millis
    private final User user;
    private final long delay;


    public SlowUser(User user) {
        this(user, DefaultDelay);
    }

    public SlowUser(User user, long delay) {
        this.user = user;
        this.delay = delay;
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
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        user.newChat(chat);
    }

    @Override
    public void chatClosed(Chat chat) {
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        user.chatClosed(chat);
    }

    @Override
    public void newUserInChat(Chat chat, User user) {
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        user.newUserInChat(chat, user);
    }

    @Override
    public void userExitedFromChat(Chat chat, User user) {
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        user.userExitedFromChat(chat, user);
    }

    @Override
    public void newMessage(Chat chat, User user, String message) {
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        user.newMessage(chat, user, message);
    }

    @Override
    public String toString() {
        return "Slow{ " + user.toString() + "}";
    }
}
