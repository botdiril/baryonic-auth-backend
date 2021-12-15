plugins {
    java
    application
}

group = "com.botdiril"
version = "5.0.0"

tasks.withType<Wrapper> {
    distributionType = Wrapper.DistributionType.ALL
    gradleVersion = "7.0.1"
}

val helidonVersion = "2.3.4"

java {
    sourceCompatibility = JavaVersion.VERSION_16
    targetCompatibility = JavaVersion.VERSION_16
}

repositories {
    mavenCentral()
}

sourceSets {
    main {
        java {
            srcDir("src/main/java")
            srcDir("src/uss/java")
            srcDir("src/framework/java")
            srcDir("src/gamedata/java")
            srcDir("src/gamelogic/java")
            srcDir("src/util/java")
        }

        resources {
            srcDir("src/main/resources")
            srcDir("src/uss/resources")
            srcDir("src/framework/resources")
            srcDir("src/gamedata/resources")
            srcDir("src/gamelogic/resources")
            srcDir("src/util/resources")
        }
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.withType<Copy> {
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}

application {
    mainClass.set("com.botdiril.BotMain")
}

tasks.getByName<Zip>("distZip") {
    // Feel free to comment
    // In Docker, we only care about the .tar file
    enabled = false
}


tasks.getByName<Tar>("distTar") {
    val baseName = archiveBaseName.get()

    archiveFileName.set("$baseName.tar")

    println("Archive file name: $baseName")

    into("assets") {
        from("assets")
        exclude("enhancementProposals/")
    }
}


dependencies {
    implementation("org.jetbrains", "annotations", "20.1.0")

    implementation("com.mchange", "c3p0", "0.9.5.5")
    implementation("mysql", "mysql-connector-java", "8.0.22")

    implementation("org.yaml", "snakeyaml", "1.28")

    implementation("com.google.guava", "guava", "30.1.1-jre")

    implementation("com.fasterxml.jackson.core", "jackson-databind", "2.12.3")
    implementation("com.fasterxml.jackson.dataformat", "jackson-dataformat-yaml", "2.12.3")

    implementation("org.apache.logging.log4j", "log4j-core", "2.16.0")
    implementation("org.apache.logging.log4j", "log4j-api", "2.16.0")

    implementation("org.apache.commons", "commons-lang3", "3.11")
    implementation("org.apache.commons", "commons-text", "1.9")
    implementation("org.apache.commons", "commons-math3", "3.6.1")
    implementation("commons-io", "commons-io", "2.8.0")

    implementation(enforcedPlatform("io.helidon:helidon-dependencies:${helidonVersion}"))
    implementation("io.helidon.webserver:helidon-webserver")
    implementation("io.helidon.webclient:helidon-webclient")
    implementation("io.helidon.media:helidon-media-jackson")
    implementation("io.helidon.config:helidon-config-object-mapping")
    implementation("io.helidon.config:helidon-config-yaml")
    implementation("io.helidon.health:helidon-health")
    implementation("io.helidon.security:helidon-security")
    implementation("io.helidon.security.integration:helidon-security-integration-webserver")
    implementation("io.helidon.health:helidon-health-checks")
    implementation("io.helidon.metrics:helidon-metrics")
}
