package es.sidelab.webchat;

import es.codeurjc.webchat.Chat;
import es.codeurjc.webchat.ChatManager;
import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;

public class UserNotificationTimeTest {

    private static final int NumUsers = 4;

    @Test
    public void test_NotificationsShouldBeHandledInParallelByUsers() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, TimeoutException {

        ChatManager chatManager = new ChatManager(1);
        UserBuilder userBuilder = new UserBuilder(TestUser.class);
        CountDownLatch latch = new CountDownLatch(NumUsers);

        TestUser sender = new TestUser("Sender");

        for (int idx = 0; idx < NumUsers; idx++) {
            chatManager.newUser( userBuilder.slow().latched(latch).user("TestUser" + idx));
        }

        Chat chat = chatManager.newChat("chat", 10, TimeUnit.SECONDS);

        chatManager.getUsers().forEach(chat::addUser);

        new Thread(() -> chat.sendMessage(sender,"message:")).start();

        try {
            assertThat(latch.await(NumUsers+1, TimeUnit.SECONDS)).isTrue();
        } catch (InterruptedException e) {
            e.printStackTrace();
            Assertions.fail(e.getMessage());
        }
    }
}
