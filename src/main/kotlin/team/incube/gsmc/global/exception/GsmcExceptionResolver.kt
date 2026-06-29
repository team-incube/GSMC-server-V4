package team.incube.gsmc.global.exception

import graphql.GraphQLError
import graphql.GraphqlErrorBuilder
import graphql.schema.DataFetchingEnvironment
import org.slf4j.LoggerFactory
import org.springframework.graphql.execution.DataFetcherExceptionResolverAdapter
import org.springframework.stereotype.Component

@Component
class GsmcExceptionResolver : DataFetcherExceptionResolverAdapter() {
    private val log = LoggerFactory.getLogger(GsmcExceptionResolver::class.java)

    override fun resolveToSingleError(
        ex: Throwable,
        env: DataFetchingEnvironment,
    ): GraphQLError? =
        when (ex) {
            is GsmcException ->
                GraphqlErrorBuilder
                    .newError(env)
                    .message(ex.errorCode.message)
                    .extensions(mapOf("code" to ex.errorCode.code))
                    .build()
            else -> {
                log.error("Unexpected exception occurred during GraphQL execution", ex)
                GraphqlErrorBuilder
                    .newError(env)
                    .message(ErrorCode.INTERNAL_SERVER_ERROR.message)
                    .extensions(mapOf("code" to ErrorCode.INTERNAL_SERVER_ERROR.code))
                    .build()
            }
        }
}
