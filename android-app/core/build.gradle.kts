plugins {
    id("org.jetbrains.kotlin.jvm")
    id("java-library")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

sourceSets {
    main {
        resources {
            srcDir("../app/src/main/assets")
        }
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

tasks.register<JavaExec>("generateSubmission") {
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("com.sahaaya.companion.core.GenerateSubmission")
}

dependencies {
    testImplementation("junit:junit:4.13.2")
}
