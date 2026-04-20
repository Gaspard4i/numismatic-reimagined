plugins {
    id("dev.architectury.loom")
    id("architectury-plugin")
    id("jacoco")
}

val minecraftVersion = property("minecraft_version").toString()
val parchmentMcVersion = property("parchment_mc_version").toString()
val parchmentVersion = property("parchment_version").toString()
val neoforgeVersion = property("neoforge_version").toString()
val architecturyApiVersion = property("architectury_api_version").toString()

architectury {
    platformSetupLoomIde()
    neoForge()
}

loom {
    silentMojangMappingsLicense()
    accessWidenerPath.set(project(":common").file("src/main/resources/numismatic_reimagined.accesswidener"))
}

dependencies {
    minecraft("com.mojang:minecraft:$minecraftVersion")
    mappings(loom.layered {
        officialMojangMappings()
        parchment("org.parchmentmc.data:parchment-$parchmentMcVersion:$parchmentVersion@zip")
    })

    "neoForge"("net.neoforged:neoforge:$neoforgeVersion")
    modImplementation("dev.architectury:architectury-neoforge:$architecturyApiVersion")
}

val common: Configuration by configurations.creating
val shadowCommon: Configuration by configurations.creating
configurations {
    compileClasspath.get().extendsFrom(common)
    runtimeClasspath.get().extendsFrom(common)
    named("developmentNeoForge").get().extendsFrom(common)
}

dependencies {
    common(project(path = ":common", configuration = "namedElements")) { isTransitive = false }
    shadowCommon(project(path = ":common", configuration = "transformProductionNeoForge")) { isTransitive = false }
}

sourceSets.main {
    resources.srcDir(project(":common").file("src/main/resources"))
}

tasks.processResources {
    inputs.property("version", project.version)
    filesMatching("META-INF/neoforge.mods.toml") {
        expand("version" to project.version)
    }
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}
