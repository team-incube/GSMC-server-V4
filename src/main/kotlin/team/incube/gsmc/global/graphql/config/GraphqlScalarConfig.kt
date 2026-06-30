package team.incube.gsmc.global.graphql.config

import graphql.schema.idl.RuntimeWiring
import org.springframework.context.annotation.Configuration
import org.springframework.graphql.execution.RuntimeWiringConfigurer
import team.incube.gsmc.global.graphql.scalar.DateTimeScalar

@Configuration
class GraphqlScalarConfig : RuntimeWiringConfigurer {
    override fun configure(builder: RuntimeWiring.Builder) {
        builder.scalar(DateTimeScalar.DateTime)
    }
}
