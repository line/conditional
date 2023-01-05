plugins {
    id("java")
    id("org.springframework.boot") version "2.7.7"
    id("io.spring.dependency-management") version "1.1.0"
}

dependencyManagement {
    imports {
    }
    dependencies {
    }
}

dependencies {
    implementation("com.linecorp.conditional:conditional:1.0.6")
    testImplementation("org.awaitility:awaitility:4.2.0")

    implementation("org.springframework.boot:spring-boot-starter-webflux")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}
