plugins {
    java
    application
}

group = "com.botdiril"
version = "5.0.0"

tasks.withType<Wrapper> {
    distributionType = Wrapper.DistributionType.ALL
    gradleVersion = "7.4.2"
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

repositories {
    mavenCentral()
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.withType<Copy> {
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}

application {
    mainClass.set("com.baryonic.Main")
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

val helidonVersion = "2.5.1"

dependencies {
    implementation("org.jetbrains", "annotations", "23.0.0")

    implementation("org.apache.logging.log4j", "log4j-core", "2.17.2")
    implementation("org.apache.logging.log4j", "log4j-api", "2.17.2")

    implementation("org.mariadb.jdbc", "mariadb-java-client", "3.0.6")

    implementation("redis.clients", "jedis", "4.2.3")
    implementation("org.bitbucket.b_c", "jose4j", "0.7.12")

    implementation(enforcedPlatform("io.helidon:helidon-dependencies:${helidonVersion}"))
    implementation("io.helidon.webserver:helidon-webserver")
    implementation("io.helidon.webclient:helidon-webclient")
    implementation("io.helidon.media:helidon-media-jackson")
    implementation("io.helidon.media:helidon-media-multipart")
    implementation("io.helidon.health:helidon-health")
    implementation("io.helidon.security:helidon-security")
    implementation("io.helidon.security.integration:helidon-security-integration-webserver")
    implementation("io.helidon.health:helidon-health-checks")
    implementation("io.helidon.metrics:helidon-metrics")
    implementation("io.helidon.dbclient:helidon-dbclient")
    implementation("io.helidon.dbclient:helidon-dbclient-jdbc")

}
