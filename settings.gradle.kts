val localPropsFile = file("gradle-local.properties")
if (localPropsFile.exists()) {
    val props = java.util.Properties().apply { localPropsFile.reader().use { load(it) } }
    props.forEach { k, v -> System.setProperty(k.toString(), v.toString()) }
}

pluginManagement {
    repositories {
        maven("https://maven.fabricmc.net/") { name = "Fabric" }
        maven("https://maven.architectury.dev/") { name = "Architectury" }
        maven("https://maven.neoforged.net/releases/") { name = "NeoForged" }
        maven("https://maven.minecraftforge.net/") { name = "Forge" }
        gradlePluginPortal()
    }
}

rootProject.name = "numismatic-reimagined"

include("common", "fabric", "neoforge")
