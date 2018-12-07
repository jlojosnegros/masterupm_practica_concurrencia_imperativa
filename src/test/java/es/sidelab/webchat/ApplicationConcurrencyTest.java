package es.sidelab.webchat;

import es.codeurjc.webchat.Chat;
import es.codeurjc.webchat.ChatManager;
import org.assertj.core.api.Assertions;
import org.jlom.utils.chrono.TimeValue;
import org.junit.Test;

import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.*;

/**
 * @Note Mejora1
 */
public class ApplicationConcurrencyTest {

    private static final int MaximumChatNumber = 50;
    private static final int NumberOfConcurrentUsers = 4;
    private static final int NumberOfChatsToCreate = 5;
    private static final String ChatBaseName = "chat";
    private static final String userName = "TestUser";

    private final TimeValue timeout = new TimeValue(5,TimeUnit.SECONDS);

    @Test
    public void test_MultipleUsersShouldBeAbleToCreateAndRegisterInChatsConcurrently() throws Throwable {

        ChatManager chatManager = new ChatManager(MaximumChatNumber);

        ExecutorService executorService = Executors.newFixedThreadPool(NumberOfConcurrentUsers);
        ExecutorCompletionService<TestResult> executorCompletionService = new ExecutorCompletionService<>(executorService);

        for (int idx = 0; idx < NumberOfConcurrentUsers; idx++) {
            executorCompletionService.submit(new CreateNewChatsAndRegisterTestUser(chatManager,
                    userName+idx,
                    NumberOfChatsToCreate));
        }

        for (int idx = 0; idx < NumberOfConcurrentUsers; idx++) {
            Future<TestResult> resultFuture = executorCompletionService.take();
            try {
                TestResult testResult = resultFuture.get();
                assertThat(testResult.isOk()).isTrue();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
                throw e.getCause();
            }
        }
    }

    private class TestResult {
        private String username;
        private int repetition;
        private boolean result;

        TestResult(String username) {
            this.username = username;
            this.repetition = -1;
            this.result = false;
        }

        public String getUsername() {
            return username;
        }

        void setRepetition(int repetition) {
            this.repetition = repetition;
        }
        public int getRepetition() {
            return this.repetition;
        }

        void testOk() {
            result = true;
        }

        boolean isOk() {
            return result;
        }
    }

    private class CreateNewChatsAndRegisterTestUser implements Callable<TestResult> {
        private final ChatManager chatManager;
        private final String username;
        private final int numberOfChatsToCreate;

        CreateNewChatsAndRegisterTestUser(final ChatManager chatManager,
                                          final String username,
                                          final int numberOfChatsToCreate) {
            this.chatManager = chatManager;
            this.username = username;
            this.numberOfChatsToCreate = numberOfChatsToCreate;
        }


        @Override
        public TestResult call() throws InterruptedException, TimeoutException {

            TestResult testResult = new TestResult(username);

            TestUser testUser = new TestUser(username);
            chatManager.newUser(testUser);

            for (int idx = 0; idx < numberOfChatsToCreate; idx++) {
                Chat chat = chatManager.newChat(ChatBaseName + idx, timeout.getValue(), timeout.getUnit());
                chat.addUser(testUser);
                chat.getUsers().forEach(System.out::println);
                testResult.setRepetition(idx);
            }
            ///@todo If do not need to add more information this should be deleted
            testResult.testOk();

            return testResult;
        }
    }
}
