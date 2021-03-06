package es.sidelab.webchat;

import es.codeurjc.webchat.Chat;
import es.codeurjc.webchat.User;

public class TestUser implements User {

	public String name;

	public TestUser(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}
	
	public String getColor(){
		return "007AFF";
	}

	@Override
	public void newChat(Chat chat) {
		System.out.println(getName() + ":" + "New chat " + chat.getName());
	}

	@Override
	public void chatClosed(Chat chat) {
		System.out.println(getName() + ":" + "Chat " + chat.getName() + " closed ");
	}

	@Override
	public void newUserInChat(Chat chat, User user) {
		System.out.println(getName() + ":" + "New user " + user.getName() + " in chat " + chat.getName());
	}

	@Override
	public void userExitedFromChat(Chat chat, User user) {
		System.out.println(getName() + ":" + "User " + user.getName() + " exited from chat " + chat.getName());
	}

	@Override
	public void newMessage(Chat chat, User user, String message) {
		System.out.println(getName() + ": New message '" + message + "' from user " + user.getName()
				+ " in chat " + chat.getName());
	}

	@Override
	public String toString() {
		return "User[" + name + "]";
	}	
}
