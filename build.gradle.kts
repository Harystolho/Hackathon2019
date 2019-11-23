plugins {
    id("org.jetbrains.kotlin.jvm").version("1.3.31")

    application
}

repositories {
    jcenter()
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")

    implementation("io.github.microutils:kotlin-logging:1.7.7")
    implementation("org.slf4j:slf4j-simple:1.7.26")
}

application {
    mainClassName = "com.harystolho.hackathon.MainKt"
}

tasks.withType<Jar> {
    manifest {
        attributes("Main-Class" to application.mainClassName)
    }

    from(configurations.compileClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
}