plugins {
    `java-library` 
}

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
}
tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.add("--enable-preview")
    options.encoding = "UTF-8"
}

tasks.withType<Test>().configureEach {
    jvmArgs("--enable-preview")
}

tasks.withType<JavaExec>().configureEach {
    jvmArgs("--enable-preview")
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
