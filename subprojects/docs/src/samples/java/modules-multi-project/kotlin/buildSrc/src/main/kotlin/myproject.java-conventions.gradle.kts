plugins {
    java
}

version.set("1.0.2")
group = "org.gradle.sample"

repositories {
    mavenCentral()
}

tasks.test {
    useJUnitPlatform()
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter:5.7.1")
}
