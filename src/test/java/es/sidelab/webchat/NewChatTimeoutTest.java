package es.sidelab.webchat;

import es.codeurjc.webchat.Chat;
import es.codeurjc.webchat.ChatManager;
import org.assertj.core.data.Percentage;
import org.junit.Test;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class NewChatTimeoutTest {

    @Test(expected = TimeoutException.class)
    public void test_WhenThereIsNoSpaceForNewChatsAnExceptionShouldBeRaised() throws TimeoutException {

        ///@Given a chat manager ...
        ChatManager chatManager = new ChatManager(1);

        /// ... which has reached the maximum number of chats ...
        addChat(chatManager,"Chat");

        ///@When we try to add a new chat
        long init = System.currentTimeMillis();
        try {
            chatManager.newChat("Chat2", 1000, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            //@Then an exception should be raised and it should wait at least timeout time ...
            assertThat(System.currentTimeMillis() - init)
                    .isGreaterThan(1000)
                    .isCloseTo(1000, Percentage.withPercentage(5));
            throw e;
        }

    }

    @Test
    public void test_WhenThereIsNoSpaceAndSpaceIsFreeAChatShouldBeCreated() {

        ///@Given a chat manager ...
        ChatManager chatManager = new ChatManager(1);

        /// ... which has reached the maximum number of chats ...
        Chat chat = addChat(chatManager, "Chat");

        ///@When we try to add a new chat ...
        long init = System.currentTimeMillis();
        try {
            chatManager.newChat("Chat2", 1000, TimeUnit.MILLISECONDS);
            /// ..but delete the already existing chat ...
            chatManager.closeChat(chat);

            //@Then

        } catch (TimeoutException e) {
            fail(e.getMessage());
        }
    }


    private Chat addChat(ChatManager chatManager, final String chatName) {
        try {
            return chatManager.newChat(chatName, 1, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            e.printStackTrace();
            fail(e.getMessage());
            return null;
        }
    }
}
