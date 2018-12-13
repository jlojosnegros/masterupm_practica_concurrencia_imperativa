package es.codeurjc.webchat;

import java.util.concurrent.*;

public class ActiveUser implements User{

    private final User wrappedUser;
    private ExecutorService executorService;


    public ActiveUser(User user) {
        this.wrappedUser = user;
        this.executorService = Executors.newSingleThreadExecutor();
    }

    @Override
    public String getName() {
        return wrappedUser.getName();
    }

    @Override
    public String getColor() {
        return wrappedUser.getColor();
    }

    @Override
    public void newChat(Chat chat) {
        executorService.execute(() -> wrappedUser.newChat(chat));
    }

    @Override
    public void chatClosed(Chat chat) {
        executorService.execute(() -> wrappedUser.chatClosed(chat) );
    }

    @Override
    public void newUserInChat(Chat chat, User user) {
        executorService.execute( () -> wrappedUser.newUserInChat(chat, user) );
    }

    @Override
    public void userExitedFromChat(Chat chat, User user) {
        executorService.execute( () -> wrappedUser.userExitedFromChat(chat, user) );
    }

    @Override
    public void newMessage(Chat chat, User user, String message) {
        executorService.execute(() -> wrappedUser.newMessage(chat, user, message) );
    }

    /// How to shutdown gracefully if java has no "destructor" ?
    @Override
    public void cleanUp() {

        executorService.shutdown();
        try {
            boolean b = executorService.awaitTermination(5, TimeUnit.SECONDS);
            ///@todo what to do with the boolean
        } catch (InterruptedException e) {
            e.printStackTrace();
            executorService.shutdownNow();
        }
    }

    @Override
    public String toString() {
        return "ActiveUser{[" + Thread.currentThread().getId() + "]" +
                "wrappedUser=" + wrappedUser +
                '}';
    }
}
