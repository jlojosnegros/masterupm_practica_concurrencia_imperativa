package es.sidelab.webchat;

import es.codeurjc.webchat.Chat;
import es.codeurjc.webchat.ChatManager;
import es.codeurjc.webchat.User;
import org.jlom.exceptions.UnableToCreateUserException;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.Exchanger;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;

public class UserNotificationOrderTest {

    private static final int NumMessages = 5;
    private static final long Timeout = 2;

    @Test
    public void test_messageNotificationsShouldBeDeliveredInOrder() throws UnableToCreateUserException, TimeoutException, InterruptedException {

        Exchanger<Boolean> exchanger = new Exchanger<>();

        ChatManager chatManager = new ChatManager(1);
        Chat chat = chatManager.newChat("chat", 10, TimeUnit.SECONDS);

        TestUser sender = new TestUser("Sender");
        User receiver = new UserBuilder(TestUser.class)
                    .slow()
                    .receiveChecker(NumMessages, exchanger)
                    .active()
                    .user("Receiver");

        chatManager.newUser(sender);
        chatManager.newUser(receiver);

        chatManager.getUsers().forEach(chat::addUser);

        new Thread(() -> {
            for (int idx = 0; idx < NumMessages; idx++) {
                chat.sendMessage(sender,"message:"+idx);
            }
        }).start();

        Boolean retVal = exchanger.exchange(null, Timeout, TimeUnit.SECONDS);
        assertThat(retVal).isTrue();
    }
}
