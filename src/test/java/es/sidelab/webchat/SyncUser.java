package es.sidelab.webchat;

import es.codeurjc.webchat.Chat;
import es.codeurjc.webchat.User;

public class SyncUser implements User {

    private final User user;

    public SyncUser(User user) {
        this.user = user;
    }

    @Override
    public synchronized String getName() {
        return user.getName();
    }

    @Override
    public synchronized String getColor() {
        return user.getColor();
    }

    @Override
    public synchronized void newChat(Chat chat) {
        user.newChat(chat);
    }

    @Override
    public synchronized void chatClosed(Chat chat) {
        user.chatClosed(chat);
    }

    @Override
    public synchronized void newUserInChat(Chat chat, User user) {
        this.user.newUserInChat(chat, user);
    }

    @Override
    public synchronized void userExitedFromChat(Chat chat, User user) {
        this.user.userExitedFromChat(chat, user);
    }

    @Override
    public synchronized void newMessage(Chat chat, User user, String message) {
        this.user.newMessage(chat, user, message);
    }

    @Override
    public synchronized String toString() {
        return "Synched{ " + user.toString() + "}";
    }
}
