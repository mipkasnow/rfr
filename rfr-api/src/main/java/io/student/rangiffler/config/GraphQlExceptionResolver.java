package io.student.rangiffler.config;

import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import graphql.schema.DataFetchingEnvironment;
import io.student.rangiffler.exception.FriendshipActionException;
import io.student.rangiffler.exception.ResourceNotFoundException;
import org.springframework.graphql.execution.DataFetcherExceptionResolverAdapter;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.stereotype.Component;

@Component
public class GraphQlExceptionResolver extends DataFetcherExceptionResolverAdapter {

  @Override
  protected GraphQLError resolveToSingleError(Throwable ex, DataFetchingEnvironment env) {
    ErrorType errorType = switch (ex) {
      case ResourceNotFoundException e -> ErrorType.NOT_FOUND;
      case FriendshipActionException e -> ErrorType.BAD_REQUEST;
      case IllegalArgumentException e -> ErrorType.BAD_REQUEST;
      default -> null;
    };
    return errorType == null
        ? null
        : GraphqlErrorBuilder.newError(env)
        .errorType(errorType)
        .message(ex.getMessage())
        .build();
  }
}
