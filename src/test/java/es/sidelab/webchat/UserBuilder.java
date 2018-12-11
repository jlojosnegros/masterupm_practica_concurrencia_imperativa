package es.sidelab.webchat;

import es.codeurjc.webchat.ActiveUser;
import es.codeurjc.webchat.User;
import org.jlom.exceptions.UnableToCreateUserException;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Stack;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Exchanger;
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

    public UserBuilder active() {
        this.decorators.push(ActiveUser::new);
        return this;
    }

    public UserBuilder receiveChecker(int numMessagesToWait, Exchanger<Boolean> exchanger) {
        this.decorators.push( (element) -> new ReceiveCheckerUser(element, numMessagesToWait, exchanger));
        return this;
    }
    public User user(String name) throws UnableToCreateUserException {
        User user = applyDecorators(name);
        decorators.clear();
        return user;
    }

    public User user_wihoutReset(String name) throws UnableToCreateUserException {
        return applyDecorators(name);
    }

    private User applyDecorators(String name) throws UnableToCreateUserException {
        Constructor<? extends User> constructor;
        try {
            constructor = this.base.getConstructor(String.class);
        } catch (NoSuchMethodException e) {
            throw new UnableToCreateUserException(e);
        }
        User user;
        try {
            user = constructor.newInstance(name);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new UnableToCreateUserException(e);
        }

        for( Function<User,User> inter : decorators) {
            user = inter.apply(user);
        }
        return user;
    }
}
