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

        /// @Given: A chat manager with some users on it
		ChatManager chatManager = new ChatManager(5);

		ExecutorService executorService = Executors.newSingleThreadExecutor();
		CompletionService<CompletionUser.CompletionResult> completionService =
                new ExecutorCompletionService<>(executorService);

		createCompletionUsersInChatManager(chatManager, NumberOfUsers, baseUserName, completionService);

		/// @When: we create a new chat in the manager
		createChatInChatManager("Chat", chatManager);

		/// @Then ...
		List<String> notifiedUsers = new ArrayList<>(chatManager.getUsers().size());
		try {
			///@Then message should be delivered to all users in chat manager ...
            for (int idx = 0; idx < NumberOfUsers; idx++) {
                Future<CompletionUser.CompletionResult> take = completionService.poll(1,TimeUnit.SECONDS);

                CompletionUser.CompletionResult completionResult = take.get(500, TimeUnit.MILLISECONDS);

                ///@Then all users get called its newChat callback
				assertThat(completionResult.getMethodCalled()).startsWith("newChat");
				/// ... with the chat name as parameter
                assertThat(completionResult.getIn_param()).containsExactly("Chat");
                /// ... and will not return anything meaningful
                assertThat(completionResult.getReturnValue()).isNull();

                notifiedUsers.add(completionResult.getCalledUserName());
            }

            /// @Then All the users should be notified once and only once.
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

    	///@Given a chatManager and some users
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

        /// @When we add new users to the chatManager
        chatManager.newUser(user0);
        chatManager.newUser(user1);
        chatManager.newUser(user2);

        ///@Then should raise an exception
    }

	@Test
	public void test_removeChat_Concurrent() {

    	///@Given a chatManager ...
		ChatManager chatManager = new ChatManager(5);
		/// ,,, with some users that will notify when they execute any of its methods ...
		ExecutorService executorService = Executors.newSingleThreadExecutor();
		CompletionService<CompletionUser.CompletionResult> completionService =
				new ExecutorCompletionService<>(executorService);

		createCompletionUsersInChatManager(chatManager, NumberOfUsers, baseUserName, completionService);

		final String ChatName = "Chat";

		/// ... and a chat
		createChatInChatManager(ChatName, chatManager);
		consumeCompletionMessages(completionService);

		/// @When the chat is closed
		chatManager.closeChat(chatManager.getChat(ChatName));


		List<String> notifiedUserNames = new ArrayList<>(chatManager.getUsers().size());
		/// @Then all the users should be notified
		try {
			for (int idx = 0; idx < NumberOfUsers; idx++) {
				Future<CompletionUser.CompletionResult> take = completionService.poll(1,TimeUnit.SECONDS);

				CompletionUser.CompletionResult completionResult = take.get(500, TimeUnit.MILLISECONDS);

				///@Then all users get called its chatClosed callback
				assertThat(completionResult.getMethodCalled()).startsWith("chatClosed");
				/// ... with the chat name as parameter
				assertThat(completionResult.getIn_param()).containsExactly(ChatName);
				/// ... and will not return anything meaningful
				assertThat(completionResult.getReturnValue()).isNull();

				notifiedUserNames.add(completionResult.getCalledUserName());
			}

			/// @Then All the users should be notified once and only once.
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

		///@Given a chatManager ...
		ChatManager chatManager = new ChatManager(5);

		/// ... and some users that will notify when their methods are executed ...
		ExecutorService executorService = Executors.newSingleThreadExecutor();
		CompletionService<CompletionUser.CompletionResult> completionService =
				new ExecutorCompletionService<>(executorService);
		createCompletionUsersInChatManager(chatManager, NumberOfUsers, baseUserName, completionService);


		final String ChatName = "Chat";
		/// ... with a new chat where the created users are added
		createChatInChatManager(ChatName, chatManager);
		addExistingUsersToChat(chatManager, ChatName);

		consumeCompletionMessages(completionService);


		///@When we add a new user to the chat
		final String newUserName = baseUserName + (NumberOfUsers+1);
		addUserToChat(chatManager,
				ChatName,
				createCompletionUser(newUserName, completionService)
		);


		///@Then all the users should be notified ...
		List<String> notifiedUserNames = new ArrayList<>(chatManager.getUsers().size());
		try {
			for (int idx = 0; idx < NumberOfUsers; idx++) {
				Future<CompletionUser.CompletionResult> take = completionService.poll(1, TimeUnit.SECONDS);
				assertThat(take).isNotNull();

				CompletionUser.CompletionResult completionResult = take.get(500, TimeUnit.MILLISECONDS);

				///@Then all users get called its newUserInChat callback
				assertThat(completionResult.getMethodCalled()).startsWith("newUserInChat");
				/// ... with the chat name as parameter
				assertThat(completionResult.getIn_param()).containsExactly(ChatName,newUserName);
				/// ... and will not return anything meaningful
				assertThat(completionResult.getReturnValue()).isNull();

				notifiedUserNames.add(completionResult.getCalledUserName());
			}

		    /// @Then All the users should be notified once and only once.
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

    	///@Given a chatManager ...
		ChatManager chatManager = new ChatManager(5);

		ExecutorService executorService = Executors.newSingleThreadExecutor();
		CompletionService<CompletionUser.CompletionResult> completionService =
				new ExecutorCompletionService<>(executorService);
		/// ... with some users that will notify when their functions are executed ...
		createCompletionUsersInChatManager(chatManager, NumberOfUsers, baseUserName, completionService);

		final String ChatName = "Chat";
		/// ... with a chat already created ...
		createChatInChatManager(ChatName, chatManager);
		/// ... and all the already created added to the chat ...
		addExistingUsersToChat(chatManager, ChatName);

		///@When we create a new user and add it to the chat ...
		final String newUserName = baseUserName + (NumberOfUsers+1);
		User newUser = createCompletionUser(newUserName, completionService);
		addUserToChat(chatManager, ChatName, newUser);

		consumeCompletionMessages(completionService);

		/// ... and we remove the newly user from the chat
		chatManager.getChat(ChatName).removeUser(newUser);

		///@Then all the remain users in chat should be notified.
		List<String> notifiedUserNames = new ArrayList<>(chatManager.getUsers().size());
		try {
			for (int idx = 0; idx < NumberOfUsers; idx++) {
				Future<CompletionUser.CompletionResult> take = completionService.poll(1, TimeUnit.SECONDS);
				assertThat(take).isNotNull();

				CompletionUser.CompletionResult completionResult = take.get(500, TimeUnit.MILLISECONDS);

				///@Then ... the users get their callback called ...
				assertThat(completionResult.getMethodCalled()).startsWith("userExitedFromChat");
				/// ... with the name of the removed user and the name of the chat it has been removed from ...
				assertThat(completionResult.getIn_param()).containsExactly(ChatName,newUserName);
				assertThat(completionResult.getReturnValue()).isNull();

				notifiedUserNames.add(completionResult.getCalledUserName());
			}
			///@Then ... all the remaining users in chat should be notified once and only once
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

		///@Given a chat manager ...
		ChatManager chatManager = new ChatManager(5);

		ExecutorService executorService = Executors.newSingleThreadExecutor();
		CompletionService<CompletionUser.CompletionResult> completionService =
				new ExecutorCompletionService<>(executorService);
		/// ... and some users that will report when their methods are executed ...
		createCompletionUsersInChatManager(chatManager, NumberOfUsers, baseUserName, completionService);

		/// ... and a new chat ...
		final String ChatName = "Chat";
		Chat chat = createChatInChatManager(ChatName, chatManager);
		/// ... with all the currently existing users added in the chat..
		addExistingUsersToChat(chatManager, ChatName);


		/// @When we create a new user ...
		final String newUserName = baseUserName + (NumberOfUsers+1);
		User newUser = createCompletionUser(newUserName, completionService);
		/// ... add it to the chat ...
		addUserToChat(chatManager, ChatName, newUser);

		consumeCompletionMessages(completionService);

		/// ... and send a message to the chat from the most recently created user
		final String messagePayload = "new message to chat";
		chat.sendMessage(newUser,messagePayload);

		///@Then all the users in the chat should be notified
		List<String> notifiedUsers = new ArrayList<>(NumberOfUsers);
		try {
			for (int idx = 0; idx < NumberOfUsers; idx++) {
				Future<CompletionUser.CompletionResult> take = completionService.poll(1, TimeUnit.SECONDS);
				assertThat(take).isNotNull();

				CompletionUser.CompletionResult completionResult = take.get(500, TimeUnit.MILLISECONDS);

				///@Then ... their callback gets called ...
				assertThat(completionResult.getMethodCalled()).startsWith("newMessage");
				/// ... with the name of the user who sent the message, the message itself and the name of the chat
				assertThat(completionResult.getIn_param()).containsExactly(ChatName, newUserName, messagePayload);
				assertThat(completionResult.getReturnValue()).isNull();

				notifiedUsers.add(completionResult.getCalledUserName());
			}

			///@Then all the users should be notified once and only once
			assertThat(notifiedUsers).containsExactlyInAnyOrder(chatManager.getUsers()
					.stream()
					.map(User::getName)
					.toArray(String[]::new)
			);

		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			fail(e.getMessage());
		}

	}

	/**
	 * Add a User to a Chat in a ChatManager.
	 * @param chatManager
	 * @param chatName
	 * @param newUser
	 */
	private void addUserToChat(ChatManager chatManager,
							   final String chatName,
							   final User newUser) {
		chatManager.getChat(chatName).addUser(newUser);
	}

	/**
	 * Go throw all the users in a chatManager and add them to the Chat with name equals to chatName.
	 * @param chatManager
	 * @param chatName
	 */
	private void addExistingUsersToChat(ChatManager chatManager, final String chatName) {
		chatManager.getUsers().forEach(user -> chatManager.getChat(chatName).addUser(user));

		assertThat(chatManager.getChat(chatName).getUsers()).hasSize(NumberOfUsers);
		assertThat(chatManager.getChat(chatName).getUsers()).containsExactlyInAnyOrder(
				chatManager.getUsers().toArray(new User[0])
		);
	}

	/**
	 * Receive all the remaining messages in the completionService to avoid errors and noise in the channel.
	 * @param completionService
	 */
	private void consumeCompletionMessages(CompletionService<CompletionUser.CompletionResult> completionService) {
		try {

			for( Future<CompletionUser.CompletionResult> take = completionService.poll(1,
					TimeUnit.SECONDS);
				 null != take;
				 take = completionService.poll(1,TimeUnit.SECONDS)){
				LoggerFactory.getLogger(getClass()).info("consumed:" + take.get().toString());
			}
		} catch (InterruptedException | ExecutionException e) {
			fail(e.getMessage());
		}
	}

	/**
	 * Creates a new Chat with name chatName in the chatManager.
	 * @param chatName
	 * @param chatManager
	 * @return the newly created Chat
	 */
	private Chat createChatInChatManager(String chatName, ChatManager chatManager) {
		try {
			return chatManager.newChat(chatName, 5, TimeUnit.SECONDS);
		} catch (TimeoutException e) {
			fail(e.getMessage());
		}
		return null;
	}

	/**
	 * Creates users of CompletionUser class and add them to the chatManager.
	 *
	 * The name of each user will be the baseUserName and a number in [0,numberOfUsers)
	 *
	 * @param chatManager
	 * @param numberOfUsers Number of users to be added to chatManager
	 * @param baseUserName  String to be used to build the name of the created users
	 * @param completionService
	 */
	private void createCompletionUsersInChatManager(ChatManager chatManager,
													final int numberOfUsers,
													final String baseUserName,
													CompletionService<CompletionUser.CompletionResult> completionService) {
		for (int idx = 0; idx < numberOfUsers; idx++) {
			chatManager.newUser(createCompletionUser(baseUserName+idx, completionService));
		}
	}

	/**
	 * Just create a new CompletionUser using UserBuilder.
	 * @param newUserName the name of the created user.
	 * @param completionService
	 * @return The created User.
	 */
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
