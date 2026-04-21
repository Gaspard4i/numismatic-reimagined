plugins {
    id("dev.architectury.loom")
    id("architectury-plugin")
    id("com.gradleup.shadow") version "8.3.5"
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

    // Fabric owo-lib kept out for now to match the NeoForge decision (parity with blocked
    // neoforge port). Vanilla PurseInventoryWidget handles the purse UI cross-loader.
    // modImplementation("io.wispforest:owo-lib:${property("owo_lib_fabric_version")}")

    // Dev-only runtime mods for easier testing (JEI, Jade).
    modRuntimeOnly("mezz.jei:jei-1.21.1-fabric:${property("jei_version")}")
    modRuntimeOnly("maven.modrinth:jade:${property("jade_version_fabric")}@jar")

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
    // Exclude the NeoForge manifest from the Fabric jar.
    exclude("META-INF/neoforge.mods.toml")
}

tasks.shadowJar {
    exclude("architectury.common.json")
    configurations = listOf(shadowCommon)
    archiveClassifier.set("dev-shadow")
}

tasks.remapJar {
    inputFile.set(tasks.shadowJar.flatMap { it.archiveFile })
    dependsOn(tasks.shadowJar)
    archiveClassifier.set(null as String?)
}

tasks.jar {
    archiveClassifier.set("dev")
}

tasks.sourcesJar {
    val commonSources = project(":common").tasks.named<org.gradle.jvm.tasks.Jar>("sourcesJar")
    dependsOn(commonSources)
    from(commonSources.map { zipTree(it.archiveFile) })
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}
