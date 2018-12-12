package es.sidelab.webchat;

import es.codeurjc.webchat.Chat;
import es.codeurjc.webchat.ChatManager;
import es.codeurjc.webchat.User;
import org.jlom.exceptions.UnableToCreateUserException;
import org.junit.Test;

import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class ChatManagerTest {


    private static final int NumberOfUsers = 2;
    private String baseUserName = "user";

    @Test
	public void test_newChat() throws TimeoutException {

		// Crear el chat Manager
		ChatManager chatManager = new ChatManager(5);

		// Crear un usuario que guarda en chatName el nombre del nuevo chat
		final String[] chatName = new String[1];

		chatManager.newUser(new TestUser("user") {
			public void newChat(Chat chat) {
				chatName[0] = chat.getName();
			}
		});

		// Crear un nuevo chat en el chatManager
		chatManager.newChat("Chat", 5, TimeUnit.SECONDS);

		// Comprobar que el chat recibido en el método 'newChat' se llama 'Chat'
		assertThat(chatName[0]).isEqualTo("Chat");
	}

	@Test
	public void test_newChat_Concurrent() {

        // Crear el chat Manager
		ChatManager chatManager = new ChatManager(5);

		ExecutorService executorService = Executors.newSingleThreadExecutor();
		CompletionService<CompletionUser.CompletionResult> completionService =
                new ExecutorCompletionService<>(executorService);

		try {
			for (int idx = 0; idx < NumberOfUsers; idx++) {

				chatManager.newUser(new UserBuilder(TestUser.class)
						.completion(completionService)
						.active()
						.user(baseUserName + idx)
				);
			}
		} catch (UnableToCreateUserException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}


		// Crear un nuevo chat en el chatManager
		try {
			chatManager.newChat("Chat", 5, TimeUnit.SECONDS);
		} catch (TimeoutException e) {
			fail(e.getMessage());
		}

		try {

            for (int idx = 0; idx < NumberOfUsers; idx++) {
                Future<CompletionUser.CompletionResult> take = completionService.take();

                CompletionUser.CompletionResult completionResult = take.get(500, TimeUnit.MILLISECONDS);

                assertThat(completionResult.getCalledUserName()).isIn(chatManager.getUsers()
                        .stream()
                        .map(User::getName)
                        .collect(Collectors.toList())
                );
                assertThat(completionResult.getIn_param()).containsExactly("Chat");
                assertThat(completionResult.getMethodCalled()).startsWith("newChat");
                assertThat(completionResult.getReturnValue()).isNull();
            }

		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			fail(e.getMessage());
		}
    }

	@Test
	public void test_newUserInChat() throws TimeoutException {

		ChatManager chatManager = new ChatManager(5);

		final String[] newUser = new String[1];

		TestUser user1 = new TestUser("user1") {
			@Override
			public void newUserInChat(Chat chat, User user) {
				newUser[0] = user.getName();
			}
		};

		TestUser user2 = new TestUser("user2");

		chatManager.newUser(user1);
		chatManager.newUser(user2);

		Chat chat = chatManager.newChat("Chat", 5, TimeUnit.SECONDS);

		chat.addUser(user1);
		chat.addUser(user2);

		assertThat(newUser[0]).isEqualToIgnoringWhitespace("user2");

	}


    @Test(expected = IllegalArgumentException.class)
    public void test_givenRepeatedUsers_ShouldThrowAnException() {
        //given
        ChatManager chatManager = new ChatManager(5);
        final String[] newUser = new String[3];

        TestUser user0 = new TestUser("user0") {
            @Override
            public void newUserInChat(Chat chat, User user) {
                newUser[0] = user.getName();
            }
        };

        TestUser user1 = new TestUser("user1") {
            @Override
            public void newUserInChat(Chat chat, User user) {
                newUser[1] = user.getName();
            }
        };

        TestUser user2 = new TestUser("user1") {
            @Override
            public void newUserInChat(Chat chat, User user) {
                newUser[2] = user.getName();
            }
        };

        //when
        chatManager.newUser(user0);
        chatManager.newUser(user1);
        chatManager.newUser(user2);

        //then should raise an exception
    }
}
