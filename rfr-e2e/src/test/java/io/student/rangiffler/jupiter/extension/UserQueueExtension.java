package io.student.rangiffler.jupiter.extension;

import io.student.rangiffler.data.StaticUserFixture;
import io.student.rangiffler.data.TestUser;
import io.student.rangiffler.data.UserFixture;
import io.student.rangiffler.jupiter.annotation.StaticUser;
import io.student.rangiffler.jupiter.annotation.User;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

import java.lang.reflect.Parameter;

public final class UserQueueExtension implements BeforeEachCallback, ParameterResolver {
    public static final ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace.create(UserQueueExtension.class);

    @Override
    public void beforeEach(ExtensionContext context) {
        Parameter[] parameters = context.getRequiredTestMethod().getParameters();
        for (int index = 0; index < parameters.length; index++) {
            if (parameters[index].isAnnotationPresent(StaticUser.class)) {
                UserFixture fixture = new StaticUserFixture();
                context.getStore(NAMESPACE).put(key(index), fixture);
                fixture.create();
            }
        }
    }

    @Override
    public boolean supportsParameter(ParameterContext parameter, @NonNull ExtensionContext context) {
        return parameter.getParameter().getType() == TestUser.class
                && parameter.isAnnotated(StaticUser.class)
                && !parameter.isAnnotated(User.class)
                && context.getTestMethod().filter(parameter.getDeclaringExecutable()::equals).isPresent();
    }

    @Override
    public TestUser resolveParameter(@NonNull ParameterContext parameter, ExtensionContext context) {
        UserFixture fixture = context.getStore(NAMESPACE).get(key(parameter.getIndex()), UserFixture.class);
        if (fixture == null) {
            throw new ParameterResolutionException(
                    "No user was created for parameter " + parameter.getIndex() + " of " + context.getRequiredTestMethod());
        }
        return fixture.user();
    }

    private static String key(int index) {
        return "user-" + index;
    }
}
