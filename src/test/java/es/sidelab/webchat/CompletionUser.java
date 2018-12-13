package es.sidelab.webchat;

import es.codeurjc.webchat.Chat;
import es.codeurjc.webchat.User;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletionService;

public class CompletionUser implements User {

    public class CompletionResult {
        private String calledUserName;
        private String methodCalled;
        private List<String> in_param;
        private String returnValue;

        public CompletionResult(String calledUserName, String methodCalled, List<String> in_param, String returnValue) {
            this.calledUserName = calledUserName;
            this.methodCalled = methodCalled;
            this.in_param = in_param;
            this.returnValue = returnValue;
        }

        public String getCalledUserName() {
            return calledUserName;
        }

        public String getMethodCalled() {
            return methodCalled;
        }

        public List<String> getIn_param() {
            return in_param;
        }

        public String getReturnValue() {
            return returnValue;
        }

        @Override
        public String toString() {
            return "CompletionResult{" +
                    "calledUserName='" + calledUserName + '\'' +
                    ", methodCalled='" + methodCalled + '\'' +
                    ", in_param=" + in_param +
                    ", returnValue='" + returnValue + '\'' +
                    '}';
        }
    }

    private final User wrappedUser;
    private final CompletionService<CompletionResult> completionService;

    public CompletionUser(User user, CompletionService<CompletionResult> completionService) {
        this.wrappedUser = user;
        this.completionService = completionService;
    }

    @Override
    public String getName() {
        return wrappedUser.getName();
    }

    @Override
    public String getColor() {
        return wrappedUser.getColor();
    }

    @Override
    public void newChat(Chat chat) {
        wrappedUser.newChat(chat);
        completionService.submit(() -> new CompletionResult(
                wrappedUser.getName(),
                "newChat",
                Collections.singletonList(chat.getName()),
                null));
    }

    @Override
    public void chatClosed(Chat chat) {
        wrappedUser.chatClosed(chat);
        completionService.submit(() -> new CompletionResult(
                wrappedUser.getName(),
                "chatClosed",
                Collections.singletonList(chat.getName()),
                null)
        );
    }

    @Override
    public void newUserInChat(Chat chat, User user) {
        wrappedUser.newUserInChat(chat, user);
        completionService.submit(() -> new CompletionResult(
                wrappedUser.getName(),
                "newUserInChat",
                Arrays.asList(chat.getName(), user.getName()),
                null)
        );
    }

    @Override
    public void userExitedFromChat(Chat chat, User user) {
        wrappedUser.userExitedFromChat(chat, user);
        completionService.submit(() -> new CompletionResult(
                wrappedUser.getName(),
                "userExitedFromChat",
                Arrays.asList(chat.getName(), user.getName()),
                null)
        );
    }

    @Override
    public void newMessage(Chat chat, User user, String message) {
        wrappedUser.newMessage(chat, user, message);
        completionService.submit(() -> new CompletionResult(
                wrappedUser.getName(),
                "newMessage",
                Arrays.asList(chat.getName(),user.getName(),message),
                null)
        );
    }
}
