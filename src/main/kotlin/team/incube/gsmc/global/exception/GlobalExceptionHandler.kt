package team.incube.gsmc.global.exception

import graphql.GraphQLError
import graphql.GraphqlErrorBuilder
import graphql.schema.DataFetchingEnvironment
import org.springframework.graphql.execution.DataFetcherExceptionResolverAdapter
import org.springframework.graphql.execution.ErrorType
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component

@Component
class GlobalExceptionHandler : DataFetcherExceptionResolverAdapter() {
    override fun resolveToSingleError(
        ex: Throwable,
        env: DataFetchingEnvironment,
    ): GraphQLError? =
        when (ex) {
            is GsmcException ->
                GraphqlErrorBuilder.newError()
                    .errorType(ex.errorCode.status.toGraphQLErrorType())
                    .message(ex.errorCode.message)
                    .path(env.executionStepInfo.path)
                    .location(env.field.sourceLocation)
                    .extensions(mapOf("status" to ex.errorCode.status.value()))
                    .build()
            else ->
                GraphqlErrorBuilder.newError()
                    .errorType(ErrorType.INTERNAL_ERROR)
                    .message(ErrorCode.INTERNAL_SERVER_ERROR.message)
                    .path(env.executionStepInfo.path)
                    .location(env.field.sourceLocation)
                    .extensions(mapOf("status" to HttpStatus.INTERNAL_SERVER_ERROR.value()))
                    .build()
        }

    private fun HttpStatus.toGraphQLErrorType(): ErrorType =
        when (this) {
            HttpStatus.BAD_REQUEST -> ErrorType.BAD_REQUEST
            HttpStatus.UNAUTHORIZED -> ErrorType.UNAUTHORIZED
            HttpStatus.FORBIDDEN -> ErrorType.FORBIDDEN
            HttpStatus.NOT_FOUND -> ErrorType.NOT_FOUND
            else -> ErrorType.INTERNAL_ERROR
        }
}
