package es.sidelab.webchat;

import es.codeurjc.webchat.Chat;
import es.codeurjc.webchat.User;

public class SlowUser implements User {


    private static final long DefaultDelay = 1000; //millis
    private final User wrapperUser;
    private final long delay;


    public SlowUser(User user) {
        this(user, DefaultDelay);
    }

    public SlowUser(User wrapperUser, long delay) {
        this.wrapperUser = wrapperUser;
        this.delay = delay;
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
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        wrapperUser.newMessage(chat, user, message);
    }

    @Override
    public String toString() {
        return "Slow{ " + wrapperUser.toString() + "}";
    }
}
