package es.codeurjc.webchat;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ChatManager {

	private ConcurrentHashMap<String, Chat> chats = new ConcurrentHashMap<>();
	private ConcurrentHashMap<String, User> users = new ConcurrentHashMap<>();
	private int maxChats;

	public ChatManager(int maxChats) {
		this.maxChats = maxChats;
	}

	public void newUser(User user) {

		if (null != users.putIfAbsent(user.getName(), user)) {
			throw new IllegalArgumentException("There is already a user with name \'"
					+ user.getName() + "\'");
		}

//		if(users.containsKey(user.getName())){
//			throw new IllegalArgumentException("There is already a user with name \'"
//					+ user.getName() + "\'");
//		} else {
//			users.put(user.getName(), user);
//		}
	}

	public Chat newChat(String name, long timeout, TimeUnit unit) throws InterruptedException,
			TimeoutException {

		///@todo no me fio de esta comprobacion porque puede que haya una insercion en medio...
		if (chats.size() == maxChats) {
			throw new TimeoutException("There is no enough capacity to create a new chat");
		}

		return chats.computeIfAbsent(name, (key) -> {
			Chat newChat = new Chat(this, key);
			users.forEachValue(1, (value)->{
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
}
