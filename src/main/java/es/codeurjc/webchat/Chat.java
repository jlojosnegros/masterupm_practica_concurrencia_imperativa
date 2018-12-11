package es.codeurjc.webchat;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class Chat {

	private String name;
	private ConcurrentHashMap<String, User> users = new ConcurrentHashMap<>();

	private ChatManager chatManager;

	public Chat(ChatManager chatManager, String name) {
		this.chatManager = chatManager;
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void addUser(User user) {
		users.computeIfAbsent(user.getName(), ( key) -> {
			users.forEach((k,v) -> v.newUserInChat(this,user));
			return user;
		});
	}

	public void removeUser(User user) {
		users.remove(user.getName());
		users.forEach( (name,u) -> u.userExitedFromChat(this,user));
	}

	public Collection<User> getUsers() {
		return Collections.unmodifiableCollection(users.values());
	}

	public User getUser(String name) {
		return users.get(name);
	}

	public void sendMessage(User user, String message) {
		users.forEach( (username, userEntity) -> userEntity.newMessage(this,user,message));
	}

	public void close() {
		this.chatManager.closeChat(this);
	}
}
