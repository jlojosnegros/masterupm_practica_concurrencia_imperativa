package es.codeurjc.webchat;

import javax.annotation.PreDestroy;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ChatManager {

	private ConcurrentHashMap<String, Chat> chats = new ConcurrentHashMap<>();
	private ConcurrentHashMap<String, User> users = new ConcurrentHashMap<>();
	private Semaphore numberOfChats;
	//private int maxChats;

	public ChatManager(int maxChats) {
		this.numberOfChats = new Semaphore(maxChats);
	}

	public void newUser(User user) {

		if (null != users.putIfAbsent(user.getName(), user)) {
			throw new IllegalArgumentException("There is already a user with name \'"
					+ user.getName() + "\'");
		}
	}

	public Chat newChat(String name, long timeout, TimeUnit unit) throws TimeoutException {

		try {
			boolean wasAcquired = numberOfChats.tryAcquire(timeout, unit);
			if (!wasAcquired) {
				throw new TimeoutException("There is no enough capacity to create a new chat");
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		return chats.computeIfAbsent(name, (key) -> {
			Chat newChat = new Chat(this, key);
			users.forEachValue(1, (value)-> {
				value.newChat(newChat);
			});
			return newChat;
		});
	}

	public void closeChat(Chat chat) {
		Chat removedChat = chats.remove(chat.getName());
		if (removedChat == null) {
			throw new IllegalArgumentException("Trying to remove an unknown chat with name \'"
					+ chat.getName() + "\'");
		}
		numberOfChats.release();
		users.forEachValue(1, (value)->{
			value.chatClosed(removedChat);
		});
	}

	public Collection<Chat> getChats() {
		return Collections.unmodifiableCollection(chats.values());
	}

	public Chat getChat(String chatName) {
		return chats.get(chatName);
	}

	public Collection<User> getUsers() {
		return Collections.unmodifiableCollection(users.values());
	}

	public User getUser(String userName) {
		return users.get(userName);
	}

	public void close() {}


	@PreDestroy
	public void cleanUp() {
		this.users.forEach( (name, user) -> {
			user.cleanUp();
		});
	}
}
