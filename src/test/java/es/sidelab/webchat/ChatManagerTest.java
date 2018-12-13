package es.sidelab.webchat;

import es.codeurjc.webchat.Chat;
import es.codeurjc.webchat.ChatManager;
import es.codeurjc.webchat.User;
import org.jlom.exceptions.UnableToCreateUserException;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
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

		createCompletionUsersInChatManager(chatManager, NumberOfUsers, baseUserName, completionService);


		// Crear un nuevo chat en el chatManager
		createChatInChatManager("Chat", chatManager);

		List<String> notifiedUsers = new ArrayList<>(chatManager.getUsers().size());
		try {

            for (int idx = 0; idx < NumberOfUsers; idx++) {
                Future<CompletionUser.CompletionResult> take = completionService.poll(1,TimeUnit.SECONDS);

                CompletionUser.CompletionResult completionResult = take.get(500, TimeUnit.MILLISECONDS);

                assertThat(completionResult.getCalledUserName()).isIn(chatManager.getUsers()
                        .stream()
                        .map(User::getName)
                        .collect(Collectors.toList())
                );
                assertThat(completionResult.getIn_param()).containsExactly("Chat");
                assertThat(completionResult.getMethodCalled()).startsWith("newChat");
                assertThat(completionResult.getReturnValue()).isNull();

                notifiedUsers.add(completionResult.getCalledUserName());
            }

            assertThat(notifiedUsers).containsExactlyInAnyOrder(
					chatManager.getUsers()
							.stream()
							.map(User::getName)
							.toArray(String[]::new)
			);

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

	@Test
	public void test_removeChat_Concurrent() {

		// Crear el chat Manager
		ChatManager chatManager = new ChatManager(5);

		ExecutorService executorService = Executors.newSingleThreadExecutor();
		CompletionService<CompletionUser.CompletionResult> completionService =
				new ExecutorCompletionService<>(executorService);

		createCompletionUsersInChatManager(chatManager, NumberOfUsers, baseUserName, completionService);

		final String ChatName = "Chat";
		// Crear un nuevo chat en el chatManager
		createChatInChatManager(ChatName, chatManager);
		consumeCompletionMessages(completionService);


		// Delete created Chat
		chatManager.closeChat(chatManager.getChat(ChatName));

		List<String> notifiedUserNames = new ArrayList<>(chatManager.getUsers().size());
		try {
			for (int idx = 0; idx < NumberOfUsers; idx++) {
				Future<CompletionUser.CompletionResult> take = completionService.poll(1,TimeUnit.SECONDS);

				CompletionUser.CompletionResult completionResult = take.get(500, TimeUnit.MILLISECONDS);

				assertThat(completionResult.getIn_param()).containsExactly(ChatName);
				assertThat(completionResult.getMethodCalled()).startsWith("chatClosed");
				assertThat(completionResult.getReturnValue()).isNull();

				notifiedUserNames.add(completionResult.getCalledUserName());
			}

			assertThat(notifiedUserNames).containsExactlyInAnyOrder(
					chatManager.getUsers().stream()
					.map(User::getName)
					.toArray(String[]::new)
			);

		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			fail(e.getMessage());
		}

	}

	@Test
	public void test_newUserInChat_Concurrent() {

		// Crear el chat Manager
		ChatManager chatManager = new ChatManager(5);

		ExecutorService executorService = Executors.newSingleThreadExecutor();
		CompletionService<CompletionUser.CompletionResult> completionService =
				new ExecutorCompletionService<>(executorService);

		createCompletionUsersInChatManager(chatManager, NumberOfUsers, baseUserName, completionService);

		final String ChatName = "Chat";
		// Crear un nuevo chat en el chatManager
		createChatInChatManager(ChatName, chatManager);

		addExistingUsersToChat(chatManager, ChatName);


		consumeCompletionMessages(completionService);


		final String newUserName = baseUserName + (NumberOfUsers+1);
		addUserToChat(chatManager,
				ChatName,
				createCompletionUser(newUserName, completionService)
		);


		List<String> notifiedUserNames = new ArrayList<>(chatManager.getUsers().size());
		try {
			for (int idx = 0; idx < NumberOfUsers; idx++) {
				Future<CompletionUser.CompletionResult> take = completionService.poll(1, TimeUnit.SECONDS);
				assertThat(take).isNotNull();

				CompletionUser.CompletionResult completionResult = take.get(500, TimeUnit.MILLISECONDS);

				assertThat(completionResult.getIn_param()).containsExactly(ChatName,newUserName);
				assertThat(completionResult.getMethodCalled()).startsWith("newUserInChat");
				assertThat(completionResult.getReturnValue()).isNull();

				notifiedUserNames.add(completionResult.getCalledUserName());
			}

			assertThat(notifiedUserNames).containsExactlyInAnyOrder(
					chatManager.getUsers().stream()
							.map(User::getName)
							.toArray(String[]::new)
			);

		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			fail(e.getMessage());
		}

	}



	@Test
	public void test_removeUserFromChat_Concurrent() {

		// Crear el chat Manager
		ChatManager chatManager = new ChatManager(5);

		ExecutorService executorService = Executors.newSingleThreadExecutor();
		CompletionService<CompletionUser.CompletionResult> completionService =
				new ExecutorCompletionService<>(executorService);

		createCompletionUsersInChatManager(chatManager, NumberOfUsers, baseUserName, completionService);

		final String ChatName = "Chat";
		// Crear un nuevo chat en el chatManager
		createChatInChatManager(ChatName, chatManager);
		addExistingUsersToChat(chatManager, ChatName);

		//create a new user
		final String newUserName = baseUserName + (NumberOfUsers+1);
		User newUser = createCompletionUser(newUserName, completionService);
		addUserToChat(chatManager, ChatName, newUser);


		consumeCompletionMessages(completionService);

		//remove user from chat
		chatManager.getChat(ChatName).removeUser(newUser);

		List<String> notifiedUserNames = new ArrayList<>(chatManager.getUsers().size());
		try {
			for (int idx = 0; idx < NumberOfUsers; idx++) {
				Future<CompletionUser.CompletionResult> take = completionService.poll(1, TimeUnit.SECONDS);
				assertThat(take).isNotNull();

				CompletionUser.CompletionResult completionResult = take.get(500, TimeUnit.MILLISECONDS);


				assertThat(completionResult.getIn_param()).containsExactly(ChatName,newUserName);
				assertThat(completionResult.getMethodCalled()).startsWith("userExitedFromChat");
				assertThat(completionResult.getReturnValue()).isNull();

				notifiedUserNames.add(completionResult.getCalledUserName());
			}

			assertThat(notifiedUserNames).containsExactlyInAnyOrder(
					chatManager.getUsers().stream()
							.map(User::getName)
							.toArray(String[]::new)
			);

		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			fail(e.getMessage());
		}

	}

	@Test
	public void test_newMessageInChat_Concurrent() {

		// Crear el chat Manager
		ChatManager chatManager = new ChatManager(5);

		ExecutorService executorService = Executors.newSingleThreadExecutor();
		CompletionService<CompletionUser.CompletionResult> completionService =
				new ExecutorCompletionService<>(executorService);

		createCompletionUsersInChatManager(chatManager, NumberOfUsers, baseUserName, completionService);

		final String ChatName = "Chat";
		// Crear un nuevo chat en el chatManager
		Chat chat = createChatInChatManager(ChatName, chatManager);


		addExistingUsersToChat(chatManager, ChatName);


		//create a new user
		final String newUserName = baseUserName + (NumberOfUsers+1);
		User newUser = createCompletionUser(newUserName, completionService);
		addUserToChat(chatManager, ChatName, newUser);



		consumeCompletionMessages(completionService);

		final String messagePayload = "new message to chat";
		chat.sendMessage(newUser,messagePayload);


		List<String> notifiedUsers = new ArrayList<>(NumberOfUsers);
		try {
			for (int idx = 0; idx < NumberOfUsers; idx++) {
				Future<CompletionUser.CompletionResult> take = completionService.poll(1, TimeUnit.SECONDS);
				assertThat(take).isNotNull();

				CompletionUser.CompletionResult completionResult = take.get(500, TimeUnit.MILLISECONDS);

				assertThat(completionResult.getCalledUserName()).isIn(chatManager.getUsers()
						.stream()
						.map(User::getName)
						.collect(Collectors.toList())
				);
				assertThat(completionResult.getIn_param()).containsExactly(ChatName,
						newUserName,
						messagePayload);
				assertThat(completionResult.getMethodCalled()).startsWith("newMessage");
				assertThat(completionResult.getReturnValue()).isNull();

				notifiedUsers.add(completionResult.getCalledUserName());
			}

			assertThat(notifiedUsers).containsExactlyInAnyOrder(chatManager.getUsers()
					.stream()
					.map(User::getName)
					.toArray(String[]::new)
			);

		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			fail(e.getMessage());
		}

	}

	private void addUserToChat(ChatManager chatManager,
							   final String chatName,
							   final User newUser) {
		//chatManager.newUser(newUser);
		chatManager.getChat(chatName).addUser(newUser);
	}

	private void addExistingUsersToChat(ChatManager chatManager, final String chatName) {
		chatManager.getUsers().forEach(user -> chatManager.getChat(chatName).addUser(user));

		assertThat(chatManager.getChat(chatName).getUsers()).hasSize(NumberOfUsers);
		assertThat(chatManager.getChat(chatName).getUsers()).containsExactlyInAnyOrder(
				chatManager.getUsers().toArray(new User[0])
		);
	}

	private void consumeCompletionMessages(CompletionService<CompletionUser.CompletionResult> completionService) {
		try {

			for( Future<CompletionUser.CompletionResult> take = completionService.poll(1,
					TimeUnit.SECONDS);
				 null != take;
				 take = completionService.poll(1,TimeUnit.SECONDS)){
				LoggerFactory.getLogger(getClass()).info("consumed:" + take.get().toString());
			}
//			for (int idx = 0; idx < NumberOfUsers; idx++) {
//				Future<CompletionUser.CompletionResult> take = completionService.poll(1,TimeUnit.SECONDS);
////				assertThat(take).isNotNull();
////				CompletionUser.CompletionResult completionResult = take.get(500, TimeUnit.MILLISECONDS);
//			}

		} catch (InterruptedException | ExecutionException e) {
			fail(e.getMessage());
		}
	}

	private Chat createChatInChatManager(String chatName, ChatManager chatManager) {
		try {
			return chatManager.newChat(chatName, 5, TimeUnit.SECONDS);
		} catch (TimeoutException e) {
			fail(e.getMessage());
		}
		return null;
	}

	private void createCompletionUsersInChatManager(ChatManager chatManager,
													int numberOfUsers,
													String baseUserName,
													CompletionService<CompletionUser.CompletionResult> completionService) {
		for (int idx = 0; idx < numberOfUsers; idx++) {

			chatManager.newUser(createCompletionUser(baseUserName+idx, completionService));
		}
	}

	private User createCompletionUser(String newUserName, CompletionService<CompletionUser.CompletionResult> completionService) {
		User user = null;
		try {
			user = new UserBuilder(TestUser.class).completion(completionService).active().user(newUserName);

		} catch (UnableToCreateUserException e) {
			fail(e.getMessage());
		}
		return user;
	}
}
