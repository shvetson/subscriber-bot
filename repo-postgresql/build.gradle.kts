val exposedVersion: String by project
val postgresVersion: String by project
val kmpUUIDVersion: String by project
val hikariVersion: String by project
val datetimeVersion: String by project

plugins {
    kotlin("jvm")
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("com.benasher44:uuid:$kmpUUIDVersion")

    implementation("org.postgresql:postgresql:$postgresVersion")
    implementation("com.zaxxer:HikariCP:$hikariVersion")

    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-java-time:$exposedVersion")

    implementation("org.jetbrains.kotlinx:kotlinx-datetime:$datetimeVersion")
    api("org.jetbrains.kotlinx:kotlinx-datetime:$datetimeVersion")

    implementation(project(":common"))
    implementation(project(":log"))
}