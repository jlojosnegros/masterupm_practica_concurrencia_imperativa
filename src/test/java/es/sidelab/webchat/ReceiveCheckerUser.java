package es.sidelab.webchat;

import es.codeurjc.webchat.Chat;
import es.codeurjc.webchat.User;

import java.util.concurrent.Exchanger;

public class ReceiveCheckerUser implements User {

    private final User wrapperUser;
    private final int numMessagesToReceive;
    private final Exchanger<Boolean> retChannel;
    private int numMessagesReceived;
    private String lastMessageReceived;

    public ReceiveCheckerUser(User user, int numMessagesToReceive, Exchanger<Boolean> retChannel) {

        this.wrapperUser = user;
        this.numMessagesToReceive = numMessagesToReceive;
        this.retChannel = retChannel;
        this.numMessagesReceived = 0;
        this.lastMessageReceived = "";
    }

    @Override
    public void newMessage(Chat chat, User user, String message) {
        this.numMessagesReceived++;
        try {
            if (message.compareToIgnoreCase(lastMessageReceived) < 0) {
                retChannel.exchange(false);
            } else if (this.numMessagesReceived == this.numMessagesToReceive) {
                retChannel.exchange(true);
            } else {
                lastMessageReceived = message;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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
    public String toString() {
        return "ReceiveCheckerUser{" +
                "numMessagesToReceive=" + numMessagesToReceive +
                ", numMessagesReceived=" + numMessagesReceived +
                ", lastMessageReceived='" + lastMessageReceived + '\'' +
                ", wrapperUser=" + wrapperUser +
                '}';
    }
}
