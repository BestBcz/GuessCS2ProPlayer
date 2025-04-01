plugins {
    kotlin("jvm") version "1.8.0"
    kotlin("plugin.serialization") version "1.8.0"
    id("net.mamoe.mirai-console") version "2.16.0"
    id("com.github.johnrengelman.shadow") version "8.1.1" // 确保使用最新版本
    id("me.him188.maven-central-publish") version "1.0.0"
}

group = "org.bcz.guesscs2proplayer"
version = "0.0.3"

repositories {
    maven("https://maven.aliyun.com/repository/public") // 加速依赖下载
    maven("https://maven.mamoe.net/releases") // Mirai 依赖
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    maven { url = uri("https://maven.pkg.jetbrains.space/public/p/mirai/maven") }
    mavenCentral() // 必须保留 mavenCentral 用于加载依赖
}

dependencies {
    implementation(kotlin("stdlib"))
    compileOnly("net.mamoe:mirai-core:2.16.0")
    compileOnly("net.mamoe:mirai-console:2.16.0")
    implementation("xyz.cssxsh.mirai:mirai-skia-plugin:1.3.2")
}

tasks.withType<Jar> {
    from({
        configurations.runtimeClasspath.get().filter { it.exists() }.map { zipTree(it) }
    })
}
tasks.withType<Jar> {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

mirai {
    jvmTarget = JavaVersion.VERSION_11
}