package es.sidelab.webchat;

import static org.assertj.core.api.Assertions.*;
import static org.junit.Assert.assertTrue;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import es.codeurjc.webchat.Chat;
import es.codeurjc.webchat.ChatManager;
import es.codeurjc.webchat.User;

public class ChatManagerTest {

	@Test
	public void newChat() throws TimeoutException {

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
	public void newUserInChat() throws TimeoutException {

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

		assertTrue("Notified new user '" + newUser[0] + "' is not equal than user name 'user2'",
				"user2".equals(newUser[0]));

	}

    @Test
    public void newUser() {

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

        TestUser user2 = new TestUser("user2") {
            @Override
            public void newUserInChat(Chat chat, User user) {
                newUser[2] = user.getName();
            }
        };

        chatManager.newUser(user0);
        chatManager.newUser(user1);
        chatManager.newUser(user2);

        assertThat(chatManager.getUsers()).containsExactlyInAnyOrder(user0,user1,user2);
        assertThat(newUser).containsExactly("user0","user1","user2");
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
