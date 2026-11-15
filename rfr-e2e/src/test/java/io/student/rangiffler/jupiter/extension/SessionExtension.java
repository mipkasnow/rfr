package io.student.rangiffler.jupiter.extension;

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.impl.junit.BrowserExtension;
import io.student.rangiffler.data.TestUser;
import io.student.rangiffler.jupiter.annotation.Session;
import io.student.rangiffler.jupiter.annotation.User;
import io.student.rangiffler.network.GqlStubs;
import io.student.rangiffler.session.GuestSession;
import io.student.rangiffler.session.UserSession;
import io.student.rangiffler.session.fixture.GuestSessionFixture;
import io.student.rangiffler.session.fixture.SessionFixture;
import io.student.rangiffler.session.fixture.UserSessionFixture;

import java.lang.reflect.Parameter;
import java.util.Optional;

import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionConfigurationException;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

public final class SessionExtension
        implements BeforeTestExecutionCallback, AfterTestExecutionCallback, ParameterResolver {
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
    public void afterTestExecution(ExtensionContext context) throws Exception {
        boolean failed = context.getExecutionException().isPresent();
        Parameter[] parameters = context.getRequiredTestMethod().getParameters();
        for (int index = 0; index < parameters.length; index++) {
            if (!parameters[index].isAnnotationPresent(Session.class)) {
                continue;
            }
            ContextRef ref = context.getStore(NAMESPACE).get(contextKey(index), ContextRef.class);
            if (ref == null) {
                continue;
            }
            BrowserTraceExtension.stop(ref.browser, failed, "Playwright trace (session " + (index + 1) + ")");
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
        boolean stubbed = parameter.getAnnotation(Session.class).useStubs();
        if (parameter.getType() == GuestSession.class && !annotated) {
            BrowserContext browser = browserContext(context, index);
            return new GuestSessionFixture(browser, stubs(browser, stubbed));
        }
        if (parameter.getType() == UserSession.class && annotated) {
            TestUser user = UserExtension.user(context, index);
            BrowserContext browser = browserContext(context, index);
            return new UserSessionFixture(browser, user, stubs(browser, stubbed));
        }
        throw new ExtensionConfigurationException(
                "@Session expects GuestSession without @User or UserSession with @User, got "
                        + (annotated ? "@User " : "") + parameter.getType().getSimpleName()
                        + " in parameter " + index + " of " + context.getRequiredTestMethod());
    }

    private static BrowserContext browserContext(ExtensionContext context, int index) {
        BrowserContext browser = BrowserExtension.getOrCreateBrowser(context)
                .newContext(new BrowserOptions().getOptions().contextOptions);
        BrowserTimeoutExtension.apply(browser);
        BrowserTraceExtension.start(browser);
        context.getStore(NAMESPACE).put(contextKey(index), new ContextRef(browser));
        return browser;
    }

    private static Optional<GqlStubs> stubs(BrowserContext browser, boolean stubbed) {
        return stubbed ? Optional.of(new GqlStubs(browser)) : Optional.empty();
    }

    private static String key(int index) {
        return "session-" + index;
    }

    private static String contextKey(int index) {
        return "session-context-" + index;
    }

    private static final class ContextRef {
        private final BrowserContext browser;

        private ContextRef(BrowserContext browser) {
            this.browser = browser;
        }
    }
}
