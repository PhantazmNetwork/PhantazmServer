plugins {
    java
}

group = "com.github.phantazmnetwork"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
    testImplementation("org.mockito:mockito-junit-jupiter:4.2.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")

    compileOnly("org.jetbrains:annotations:22.0.0")
    testCompileOnly("org.jetbrains:annotations:22.0.0")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

tasks.register<Copy>("copyLibs") {
    from(configurations.runtimeClasspath)
    into("$rootDir/run/server-1/libs")
}
