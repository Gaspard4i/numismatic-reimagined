plugins {
    id("architectury-plugin") version "3.4.160"
    id("dev.architectury.loom") version "1.10.431" apply false
    java
}

val javaVersion = property("java_version").toString().toInt()

architectury {
    minecraft = property("minecraft_version").toString()
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "architectury-plugin")

    val archivesBase = property("archives_base_name").toString()
    base.archivesName.set("$archivesBase-${project.name}")
    version = property("mod_version").toString()
    group = property("maven_group").toString()

    repositories {
        mavenCentral()
        maven("https://maven.parchmentmc.org")
        maven("https://maven.wispforest.io")
        maven("https://maven.neoforged.net/releases/")
        maven("https://maven.blamejared.com") { name = "BlameJared (JEI)" }
        maven("https://modmaven.dev") { name = "ModMaven (JEI mirror)" }
        exclusiveContent {
            forRepository {
                maven("https://api.modrinth.com/maven") {
                    name = "Modrinth"
                    metadataSources { artifact() }
                }
            }
            filter { includeGroup("maven.modrinth") }
        }
    }

    java {
        withSourcesJar()
        sourceCompatibility = JavaVersion.toVersion(javaVersion)
        targetCompatibility = JavaVersion.toVersion(javaVersion)
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(javaVersion))
        }
    }

    tasks.withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        options.release.set(javaVersion)
    }

    tasks.withType<Jar>().configureEach {
        from(rootProject.file("LICENSE")) {
            rename { "${it}_${archivesBase}" }
        }
    }
}
