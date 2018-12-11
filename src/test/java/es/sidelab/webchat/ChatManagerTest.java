package es.sidelab.webchat;

import es.codeurjc.webchat.Chat;
import es.codeurjc.webchat.ChatManager;
import es.codeurjc.webchat.User;
import org.jlom.exceptions.UnableToCreateUserException;
import org.junit.Test;

import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class ChatManagerTest {


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
		CompletionService<CompletionUser.CompletionResult> completionService = new ExecutorCompletionService<>(executorService);

		final String expectedUsername_1 = "user1";
//		final String expectedUsername_2 = "user2";


		try {
			chatManager.newUser(new UserBuilder(TestUser.class).completion(completionService).active().user(expectedUsername_1));
			//chatManager.newUser(new UserBuilder(TestUser.class).completion(completionService).active().user(expectedUsername_2));

		} catch (UnableToCreateUserException e) {
			fail(e.getMessage());
		}

		// Crear un nuevo chat en el chatManager
		try {
			chatManager.newChat("Chat", 5, TimeUnit.SECONDS);
		} catch (TimeoutException e) {
			fail(e.getMessage());
		}

		try {
			Future<CompletionUser.CompletionResult> take = completionService.take();

			CompletionUser.CompletionResult completionResult = take.get();

			assertThat(completionResult.getCalledUserName()).isEqualTo(expectedUsername_1);
			assertThat(completionResult.getIn_param()).containsExactly("Chat");
			assertThat(completionResult.getMethodCalled()).startsWith("newChat");
			assertThat(completionResult.getReturnValue()).isNull();

		} catch (InterruptedException | ExecutionException e) {
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
