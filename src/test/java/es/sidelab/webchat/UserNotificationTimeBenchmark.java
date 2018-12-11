package es.sidelab.webchat;

import com.carrotsearch.junitbenchmarks.BenchmarkOptions;
import com.carrotsearch.junitbenchmarks.BenchmarkRule;
import es.codeurjc.webchat.Chat;
import es.codeurjc.webchat.ChatManager;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class UserNotificationTimeBenchmark {

    private static final long DelayPerUser = 1000; //millis
    @Rule
    public TestRule benchmarkRule = new BenchmarkRule();

    private static final int NumUsers = 4;


    @BenchmarkOptions(warmupRounds = 10,benchmarkRounds = 10)
    @Scope(scopeName = "benchmark")
    @Test
    public void benchmark_NotificationsShouldBeHandledInParallelByUsers() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, TimeoutException {

        ChatManager chatManager = new ChatManager(1);
        UserBuilder userBuilder = new UserBuilder(TestUser.class);
        CountDownLatch latch = new CountDownLatch(NumUsers);

        TestUser sender = new TestUser("Sender");

        for (int idx = 0; idx < NumUsers; idx++) {
            chatManager.newUser( userBuilder
                    .slow(DelayPerUser)
                    .latched(latch)
                    .active()
                    .user("TestUser" + idx)
            );
        }

        Chat chat = chatManager.newChat("chat", 10, TimeUnit.SECONDS);

        chatManager.getUsers().forEach(chat::addUser);

        new Thread(() -> chat.sendMessage(sender,"message:") ).start();

        try {
            if (! latch.await((NumUsers * DelayPerUser) + 500, TimeUnit.MILLISECONDS) ) {
                LoggerFactory.getLogger(getClass()).error("missed {} elements", latch.getCount());
                throw  new InterruptedException();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            try {
                latch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}



