package io.student.rangiffler.jupiter.extension;

import com.microsoft.playwright.impl.junit.BrowserContextExtension;
import io.student.rangiffler.network.GqlStubs;

import java.util.Arrays;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

public final class GqlStubsExtension implements ParameterResolver {
    private static final ExtensionContext.Namespace NAMESPACE =
            ExtensionContext.Namespace.create(GqlStubsExtension.class);

    @Override
    public boolean supportsParameter(ParameterContext parameter, ExtensionContext context) {
        return parameter.getParameter().getType() == GqlStubs.class
                && context.getTestMethod().filter(parameter.getDeclaringExecutable()::equals).isPresent();
    }

    @Override
    public GqlStubs resolveParameter(ParameterContext parameter, ExtensionContext context) {
        long count = Arrays.stream(context.getRequiredTestMethod().getParameterTypes())
                .filter(GqlStubs.class::equals)
                .count();
        if (count > 1) {
            throw new ParameterResolutionException(
                    "Test " + context.getRequiredTestMethod() + " declares " + count
                            + " GqlStubs parameters, but only one is allowed for the main browser context;"
                            + " stubs of other contexts come from @Session(useStubs = true)");
        }
        GqlStubs stubs = new GqlStubs(BrowserContextExtension.getOrCreateBrowserContext(context));
        context.getStore(NAMESPACE).put(key(parameter.getIndex()), stubs);
        return stubs;
    }

    private static String key(int index) {
        return "stubs-" + index;
    }
}
