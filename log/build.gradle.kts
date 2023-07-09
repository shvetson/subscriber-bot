val datetimeVersion: String by project
val logbackVersion: String by project
val logbackEncoderVersion: String by project

plugins {
    kotlin("jvm")
}

dependencies {
    implementation(kotlin("stdlib-common"))
    implementation(kotlin("stdlib-jdk8"))

    api("org.jetbrains.kotlinx:kotlinx-datetime:$datetimeVersion")

    // logback
    implementation("net.logstash.logback:logstash-logback-encoder:$logbackEncoderVersion")
    api("ch.qos.logback:logback-classic:$logbackVersion")
}