package es.sidelab.webchat;

import es.codeurjc.webchat.User;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Stack;
import java.util.concurrent.CountDownLatch;
import java.util.function.Function;

public class UserBuilder {

    private final Class<? extends User> base;
    private Stack<Function<User,User>> decorators;

    public UserBuilder(Class<? extends User> base) {
        this.base = base;
        this.decorators = new Stack<>();
    }

    public UserBuilder slow() {
        this.decorators.push(SlowUser::new);
        return this;
    }

    public UserBuilder slow(long delay) {
        this.decorators.push( (element) -> new SlowUser(element,delay));
        return this;
    }

    public UserBuilder latched(CountDownLatch latch) {
        this.decorators.push((element ) -> new LatchedUser(element, latch));
        return this;
    }

    public UserBuilder sync() {
        this.decorators.push(SyncUser::new);
        return this;
    }

    public User user(String name) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Constructor<? extends User> constructor = this.base.getConstructor(String.class);
        User user = constructor.newInstance(name);

        for( Function<User,User> inter : decorators) {
            user = inter.apply(user);
        }

        return user;
    }
}
