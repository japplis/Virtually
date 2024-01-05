plugins {
    `java-library` 
    `maven-publish`
}

group = "com.japplis"
version = "0.1"

tasks.jar {
    manifest {
        attributes(mapOf("Implementation-Title" to project.name,
                         "Implementation-Version" to project.version))
    }
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
    withJavadocJar()
    withSourcesJar()
}

tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.add("--enable-preview")
    options.encoding = "UTF-8"
}

tasks.withType<Javadoc> {
    val javadocOptions = options as CoreJavadocOptions

    javadocOptions.addStringOption("source", "21")
    javadocOptions.addBooleanOption("-enable-preview", true)
}

tasks.withType<Test>().configureEach {
    jvmArgs("--enable-preview")
}

tasks.withType<JavaExec>().configureEach {
    jvmArgs("--enable-preview")
}

plugins.withId("maven-publish") {
    configure<PublishingExtension> {
        publications {
            register("mavenJava", MavenPublication::class) {
              from(components["java"])
              pom {
                artifactId = "virtually"
                description.set("Virtual threads friendly tools")
                url.set("https://www.github.com/japplis/Virtually")

                scm {
                  connection.set("scm:git:https://www.github.com/japplis/Virtually/")
                  developerConnection.set("scm:git:https://github.com/japplis/")
                  url.set("https://www.github.com/japplis/Virtually/")
                }

                licenses {
                  license {
                    name.set("The Apache 2.0 License")
                    url.set("https://opensource.org/licenses/Apache-2.0")
                  }
                }

                developers {
                  developer {
                    id.set("japplis")
                    name.set("Japplis")
                    email.set("anthony.goubard@japplis.com")
                    url.set("https://www.japplis.com")
                  }
                }
              }

            }
        }
    }
}

dependencies {
    val aspectjVersion = "1.9.20.1"
    compileOnly("org.aspectj:aspectjrt:$aspectjVersion")

    testImplementation("org.junit.jupiter:junit-jupiter:5.9.3") 

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testRuntimeOnly("org.aspectj:aspectjrt:$aspectjVersion")
    testRuntimeOnly("org.aspectj:aspectjweaver:$aspectjVersion")
    // testRuntimeAgent("org.aspectj:aspectjweaver")
}

tasks.named<Test>("test") {
    useJUnitPlatform() 
}

repositories {
    mavenCentral() 
}
