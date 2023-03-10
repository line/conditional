plugins {
    id("java")
    id("signing")
    id("maven-publish")
}

allprojects {
    group = "com.linecorp.conditional"
    version = "1.1.3"

    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "signing")
    apply(plugin = "maven-publish")

    dependencies {
        implementation("com.google.code.findbugs:jsr305:3.0.2")
        testImplementation("org.junit.jupiter:junit-jupiter:5.9.2")
        testImplementation("org.assertj:assertj-core:3.24.1")
        testImplementation("org.awaitility:awaitility:4.2.0")
    }

    java {
        withSourcesJar()
        withJavadocJar()
    }

    signing {
        sign(publishing.publications)
    }

    publishing {
        publications {
            create<MavenPublication>("maven") {
                from(components["java"])
                repositories {
                    maven {
                        url = uri("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
                        val sonatypeUsername: String? by project
                        val sonatypePassword: String? by project
                        credentials {
                            username = sonatypeUsername ?: ""
                            password = sonatypePassword ?: ""
                        }
                    }
                }
                pom {
                    name.set(artifactId)
                    description.set("A super lightweight library that helps you to compose multiple conditional expressions and make them asynchronous easily")
                    url.set("https://github.com/line/conditional")
                    licenses {
                        license {
                            name.set("The Apache License, Version 2.0")
                            url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                        }
                    }
                    developers {
                        developer {
                            id.set("icepeppermint")
                            name.set("Suyeong Hong")
                            email.set("suyeong.hong@gmail.com")
                        }
                    }
                    scm {
                        connection.set("scm:git:git://github.com/line/conditional.git")
                        developerConnection.set("scm:git:ssh://github.com/line/conditional.git")
                        url.set("https://github.com/line/conditional")
                    }
                }
            }
        }
    }

    tasks.test {
        useJUnitPlatform()
    }
}
