plugins {
    java
}

group = "fr.loual"
version = "1.0.0"

repositories {
    mavenCentral()

    maven {
        name = "papermc"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:26.2.build.121-stable")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

tasks.register<Copy>("copyToServer") {
    dependsOn(tasks.jar)
    from(tasks.jar)
    into(file("../server/plugins"))
}