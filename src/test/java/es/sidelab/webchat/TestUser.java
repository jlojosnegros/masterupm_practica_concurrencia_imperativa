package es.sidelab.webchat;

import es.codeurjc.webchat.Chat;
import es.codeurjc.webchat.User;

public class TestUser implements User {

	public String name;

	public TestUser(String name) {
		this.name = name;
	}

	@Override
	public synchronized String getName() {
		return name;
	}
	
	public synchronized String getColor(){
		return "007AFF";
	}

	@Override
	public synchronized void newChat(Chat chat) {
		System.out.println(getName() + ":" + "New chat " + chat.getName());
	}

	@Override
	public synchronized void chatClosed(Chat chat) {
		System.out.println(getName() + ":" + "Chat " + chat.getName() + " closed ");
	}

	@Override
	public synchronized void newUserInChat(Chat chat, User user) {
		System.out.println(getName() + ":" + "New user " + user.getName() + " in chat " + chat.getName());
	}

	@Override
	public synchronized void userExitedFromChat(Chat chat, User user) {
		System.out.println(getName() + ":" + "User " + user.getName() + " exited from chat " + chat.getName());
	}

	@Override
	public synchronized void newMessage(Chat chat, User user, String message) {
		System.out.println("New message '" + message + "' from user " + user.getName()
				+ " in chat " + chat.getName());
	}

	@Override
	public synchronized String toString() {
		return "User[" + name + "]";
	}	
}
