plugins {
    id("dev.architectury.loom")
    id("architectury-plugin")
    id("jacoco")
}

val minecraftVersion = property("minecraft_version").toString()
val parchmentMcVersion = property("parchment_mc_version").toString()
val parchmentVersion = property("parchment_version").toString()
val fabricLoaderVersion = property("fabric_loader_version").toString()
val fabricApiVersion = property("fabric_api_version").toString()
val architecturyApiVersion = property("architectury_api_version").toString()

architectury {
    platformSetupLoomIde()
    fabric()
}

loom {
    silentMojangMappingsLicense()
    accessWidenerPath.set(project(":common").file("src/main/resources/numismatic_reimagined.accesswidener"))
}

val common: Configuration by configurations.creating
val shadowCommon: Configuration by configurations.creating
configurations {
    compileClasspath.get().extendsFrom(common)
    runtimeClasspath.get().extendsFrom(common)
    named("developmentFabric").get().extendsFrom(common)
}

dependencies {
    minecraft("com.mojang:minecraft:$minecraftVersion")
    mappings(loom.layered {
        officialMojangMappings()
        parchment("org.parchmentmc.data:parchment-$parchmentMcVersion:$parchmentVersion@zip")
    })

    modImplementation("net.fabricmc:fabric-loader:$fabricLoaderVersion")
    modImplementation("net.fabricmc.fabric-api:fabric-api:$fabricApiVersion")

    modImplementation("dev.architectury:architectury-fabric:$architecturyApiVersion")

    common(project(path = ":common", configuration = "namedElements")) { isTransitive = false }
    shadowCommon(project(path = ":common", configuration = "transformProductionFabric")) { isTransitive = false }
}

sourceSets.main {
    resources.srcDir(project(":common").file("src/main/resources"))
}

tasks.processResources {
    inputs.property("version", project.version)
    filesMatching("fabric.mod.json") {
        expand("version" to project.version)
    }
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}
