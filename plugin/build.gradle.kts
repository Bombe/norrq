/*
 * This file was generated by the Gradle 'init' task.
 */

plugins {
	kotlin("jvm") version "1.8.22"
}

kotlin {
	jvmToolchain(8)
}

repositories {
	mavenCentral()
	maven("https://maven.pterodactylus.net/")
}

dependencies {
	implementation(kotlin("stdlib-jdk8"))
	implementation("org.freenetproject:fred:0.7.5.1497")

	testImplementation("org.junit.jupiter:junit-jupiter:5.9.3")
	testImplementation("org.mockito:mockito-core:4.11.0")
}

tasks.named<Test>("test") {
	useJUnitPlatform()
}
