plugins {
    id("java")
}

group = "ru.cu.advancedgit"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.tarantool:tarantool-client:1.5.0")

    // MessagePack
    implementation("org.msgpack:msgpack-core:0.9.8")

    // Netty
    implementation("io.netty:netty-codec:4.1.108.Final")
    implementation("io.netty:netty-transport:4.1.108.Final")
    implementation("io.netty:netty-buffer:4.1.108.Final")
    implementation("io.netty:netty-handler:4.1.108.Final")
    implementation("io.netty:netty-common:4.1.108.Final")

    // Logging
    implementation("org.slf4j:slf4j-simple:2.0.13")
    implementation("org.slf4j:slf4j-api:2.0.13")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

tasks.test {
    useJUnitPlatform()
}