plugins {
    application
}

group = "org.sample"
version.set("1.0")

application {
    mainClass.set("org.sample.myapp.Main")
}

dependencies {
    implementation("org.sample:number-utils:1.0")
    implementation("org.sample:string-utils:1.0")
}

repositories {
    maven {
        url = uri(project.file("../../local-repo"))
    }
    mavenCentral()
}
