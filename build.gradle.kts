plugins {
    kotlin("jvm") version "1.9.0"
    application
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))


    // https://mvnrepository.com/artifact/org.apache.iceberg/iceberg-core
    implementation("org.apache.iceberg:iceberg-core:1.3.1")
    // https://mvnrepository.com/artifact/org.apache.iceberg/iceberg-api
    implementation("org.apache.iceberg:iceberg-api:1.3.1")
    // https://mvnrepository.com/artifact/org.apache.iceberg/iceberg-parquet
    implementation("org.apache.iceberg:iceberg-parquet:1.3.1")
    // https://mvnrepository.com/artifact/org.apache.iceberg/iceberg-aws
    implementation("org.apache.iceberg:iceberg-aws:1.3.1")
    // https://mvnrepository.com/artifact/org.apache.iceberg/iceberg-data
    implementation("org.apache.iceberg:iceberg-data:1.3.1")
    // https://mvnrepository.com/artifact/org.apache.parquet/parquet-format-structures
    implementation("org.apache.parquet:parquet-format-structures:1.13.1")

    implementation(platform("software.amazon.awssdk:bom:2.20.46"))
    implementation("software.amazon.awssdk:s3")
    implementation("software.amazon.awssdk:sts")
    implementation("software.amazon.awssdk:url-connection-client")

    // kotlinx-cli
    implementation("org.jetbrains.kotlinx:kotlinx-cli:0.3.5")

    // https://mvnrepository.com/artifact/org.apache.hadoop/hadoop-common
    implementation("org.apache.hadoop:hadoop-common:3.3.5")

    // https://mvnrepository.com/artifact/org.apache.hadoop/hadoop-aws
    implementation("org.apache.hadoop:hadoop-aws:3.3.5")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(8)
}

application {
    mainClass.set("MainKt")
}