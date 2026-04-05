plugins {
    kotlin("jvm") version "2.3.20"
    kotlin("plugin.spring") version "2.3.20"
    kotlin("plugin.jpa") version "2.3.20"
    id("com.google.devtools.ksp") version "2.3.20-1.0.32"
    id("org.springframework.boot") version "4.0.5"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.jlleitschuh.gradle.ktlint") version "12.2.0"
}

group = "team.incube"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

repositories {
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    implementation(Libs.THE_SDK)
    implementation(Libs.SPRING_BOOT_JPA)
    implementation(Libs.SPRING_BOOT_REDIS)
    implementation(Libs.SPRING_BOOT_GRAPHQL)
    implementation(Libs.SPRING_BOOT_SECURITY)
    implementation(Libs.SPRING_BOOT_WEB)
    implementation(Libs.JACKSON_KOTLIN)
    implementation(Libs.KOTLIN_REFLECT)
    implementation(Libs.QUERYDSL_JPA) { artifact { classifier = "jakarta" } }

    ksp(Libs.QUERYDSL_KSP_CODEGEN)

    compileOnly(Libs.LOMBOK)
    annotationProcessor(Libs.LOMBOK)

    runtimeOnly(Libs.MYSQL_CONNECTOR)

    testImplementation(Libs.SPRING_BOOT_TEST)
    testImplementation(Libs.KOTLIN_TEST_JUNIT5)
    testImplementation(Libs.SPRING_GRAPHQL_TEST)
    testImplementation(Libs.SPRING_SECURITY_TEST)
    testCompileOnly(Libs.LOMBOK)
    testAnnotationProcessor(Libs.LOMBOK)
    testRuntimeOnly(Libs.JUNIT_PLATFORM_LAUNCHER)
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

ktlint {
    version.set("1.5.0")
}

allOpen {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.MappedSuperclass")
    annotation("jakarta.persistence.Embeddable")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
