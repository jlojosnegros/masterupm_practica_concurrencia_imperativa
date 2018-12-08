package es.sidelab.webchat;

import es.codeurjc.webchat.User;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Stack;

public class UserBuilder {

    private final Class<? extends User> base;
    private Stack<Class<? extends User>> decorators;

    public UserBuilder(Class<? extends User> base) {
        this.base = base;
        this.decorators = new Stack<>();
    }

    public UserBuilder slow() {
        this.decorators.push(SlowUser.class);
        return this;
    }

    public UserBuilder latched() {
        this.decorators.push(LatchedUser.class);
        return this;
    }

    public UserBuilder sync() {
        this.decorators.push(SyncUser.class);
        return this;
    }

    public User user(String name) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Constructor<? extends User> constructor = this.base.getConstructor(String.class);
        User user = constructor.newInstance(name);

        for (Class<? extends User> element : decorators ){
            user = element.getConstructor(User.class).newInstance(user);
        }

        return user;

//        decorators.stream().reduce(user, (a, b) -> {
//            try {
//                User user1 = b.getConstructor(User.class).newInstance(a);
//            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
//                e.printStackTrace();
//            }
//        });


    }
}
