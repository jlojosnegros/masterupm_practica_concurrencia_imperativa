package es.sidelab.webchat;

import es.codeurjc.webchat.Chat;
import es.codeurjc.webchat.User;

public class SyncUser implements User {

    private final User wrapperUser;

    public SyncUser(User user) {
        wrapperUser = user;
    }

    @Override
    public synchronized String getName() {
        return wrapperUser.getName();
    }

    @Override
    public synchronized String getColor() {
        return wrapperUser.getColor();
    }

    @Override
    public synchronized void newChat(Chat chat) {
        wrapperUser.newChat(chat);
    }

    @Override
    public synchronized void chatClosed(Chat chat) {
        wrapperUser.chatClosed(chat);
    }

    @Override
    public synchronized void newUserInChat(Chat chat, User user) {
        wrapperUser.newUserInChat(chat, user);
    }

    @Override
    public synchronized void userExitedFromChat(Chat chat, User user) {
        wrapperUser.userExitedFromChat(chat, user);
    }

    @Override
    public synchronized void newMessage(Chat chat, User user, String message) {
        wrapperUser.newMessage(chat, user, message);
    }

    @Override
    public synchronized String toString() {
        return "Synched{ " + wrapperUser.toString() + "}";
    }
}
