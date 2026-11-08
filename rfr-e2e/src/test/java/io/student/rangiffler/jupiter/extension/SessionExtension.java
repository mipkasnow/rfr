package io.student.rangiffler.jupiter.extension;

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.impl.junit.BrowserExtension;
import io.student.rangiffler.data.TestUser;
import io.student.rangiffler.jupiter.annotation.Session;
import io.student.rangiffler.jupiter.annotation.User;
import io.student.rangiffler.session.GuestSession;
import io.student.rangiffler.session.UserSession;
import io.student.rangiffler.session.fixture.GuestSessionFixture;
import io.student.rangiffler.session.fixture.SessionFixture;
import io.student.rangiffler.session.fixture.UserSessionFixture;

import java.lang.reflect.Parameter;

import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionConfigurationException;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

public final class SessionExtension implements BeforeTestExecutionCallback, ParameterResolver {
    private static final ExtensionContext.Namespace NAMESPACE =
            ExtensionContext.Namespace.create(SessionExtension.class);

    @Override
    public void beforeTestExecution(ExtensionContext context) {
        Parameter[] parameters = context.getRequiredTestMethod().getParameters();
        for (int index = 0; index < parameters.length; index++) {
            if (parameters[index].isAnnotationPresent(Session.class)) {
                SessionFixture<?> fixture = fixture(context, parameters[index], index);
                context.getStore(NAMESPACE).put(key(index), fixture);
                fixture.create();
            }
        }
    }

    @Override
    public boolean supportsParameter(ParameterContext parameter, ExtensionContext context) {
        Class<?> type = parameter.getParameter().getType();
        return (type == GuestSession.class || type == UserSession.class)
                && parameter.isAnnotated(Session.class)
                && context.getTestMethod().filter(parameter.getDeclaringExecutable()::equals).isPresent();
    }

    @Override
    public Object resolveParameter(ParameterContext parameter, ExtensionContext context) {
        SessionFixture<?> fixture = context.getStore(NAMESPACE).get(key(parameter.getIndex()), SessionFixture.class);
        if (fixture == null) {
            throw new ParameterResolutionException(
                    "No session was created for parameter " + parameter.getIndex() + " of " + context.getRequiredTestMethod());
        }
        return fixture.session();
    }

    private static SessionFixture<?> fixture(ExtensionContext context, Parameter parameter, int index) {
        boolean annotated = parameter.isAnnotationPresent(User.class);
        if (parameter.getType() == GuestSession.class && !annotated) {
            return new GuestSessionFixture(browserContext(context));
        }
        if (parameter.getType() == UserSession.class && annotated) {
            TestUser user = UserExtension.user(context, index);
            return new UserSessionFixture(browserContext(context), user);
        }
        throw new ExtensionConfigurationException(
                "@Session expects GuestSession without @User or UserSession with @User, got "
                        + (annotated ? "@User " : "") + parameter.getType().getSimpleName()
                        + " in parameter " + index + " of " + context.getRequiredTestMethod());
    }

    private static BrowserContext browserContext(ExtensionContext context) {
        BrowserContext browser = BrowserExtension.getOrCreateBrowser(context)
                .newContext(new BrowserOptions().getOptions().contextOptions);
        BrowserTimeoutExtension.apply(browser);
        return browser;
    }

    private static String key(int index) {
        return "session-" + index;
    }
}
