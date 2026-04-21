plugins {
    id("dev.architectury.loom")
    id("architectury-plugin")
    id("com.gradleup.shadow") version "8.3.5"
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

    // owo-lib reactivation blocked on beta : endec transitive not resolved by the NeoForge
    // 0.12.15.1-beta.6 jar in our dev env. Stay with vanilla widgets for now ; revisit
    // once a stable owo-lib-neoforge is released for 1.21.1.
    // modImplementation("io.wispforest:owo-lib-neoforge:${property("owo_lib_neoforge_version")}")

    // Dev-only runtime mods for easier testing (JEI, Jade).
    modRuntimeOnly("mezz.jei:jei-1.21.1-neoforge:${property("jei_version")}")
    modRuntimeOnly("maven.modrinth:jade:${property("jade_version_neoforge")}@jar")
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
    // Exclude the Fabric manifest from the NeoForge jar.
    exclude("fabric.mod.json")
}

tasks.shadowJar {
    exclude("fabric.mod.json")
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
