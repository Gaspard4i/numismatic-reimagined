import java.math.BigDecimal

plugins {
    id("dev.architectury.loom")
    id("architectury-plugin")
    id("jacoco")
}

val minecraftVersion = property("minecraft_version").toString()
val parchmentMcVersion = property("parchment_mc_version").toString()
val parchmentVersion = property("parchment_version").toString()
val architecturyApiVersion = property("architectury_api_version").toString()
val junitVersion = property("junit_version").toString()
val mockitoVersion = property("mockito_version").toString()
val jacocoVersion = property("jacoco_version").toString()

architectury {
    common("fabric", "neoforge")
}

loom {
    silentMojangMappingsLicense()
    accessWidenerPath.set(file("src/main/resources/numismatic_reimagined.accesswidener"))
}

dependencies {
    minecraft("com.mojang:minecraft:$minecraftVersion")
    mappings(loom.layered {
        officialMojangMappings()
        parchment("org.parchmentmc.data:parchment-$parchmentMcVersion:$parchmentVersion@zip")
    })

    modImplementation("dev.architectury:architectury:$architecturyApiVersion")

    // owo-lib removed from common compile classpath — see loader modules for rationale.
    // modCompileOnly("io.wispforest:owo-lib:${property("owo_lib_fabric_version")}")
    // modCompileOnly("net.fabricmc:fabric-loader:${property("fabric_loader_version")}")
    // modCompileOnly("net.fabricmc.fabric-api:fabric-resource-loader-v0:2.0.0+1bb677a619")

    testImplementation("org.junit.jupiter:junit-jupiter:$junitVersion")
    testImplementation("org.mockito:mockito-core:$mockitoVersion")
    testImplementation("org.mockito:mockito-junit-jupiter:$mockitoVersion")
}

jacoco {
    toolVersion = jacocoVersion
}

tasks.test {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport)
}

val jacocoExclusions = listOf(
    "dev/gaspard4i/numismatic/Numismatic.class",
    "dev/gaspard4i/numismatic/NumismaticConstants.class",
    "dev/gaspard4i/numismatic/item/**",
    "dev/gaspard4i/numismatic/component/**",
    "dev/gaspard4i/numismatic/block/**",
    "dev/gaspard4i/numismatic/shop/**",
    "dev/gaspard4i/numismatic/request/**",
    "dev/gaspard4i/numismatic/villager/**",
    "dev/gaspard4i/numismatic/loot/**",
    "dev/gaspard4i/numismatic/mob/**",
    "dev/gaspard4i/numismatic/advancement/**",
    "dev/gaspard4i/numismatic/network/**",
    "dev/gaspard4i/numismatic/client/**",
    "dev/gaspard4i/numismatic/command/**",
    "dev/gaspard4i/numismatic/config/**",
    "dev/gaspard4i/numismatic/mixin/**",
    "dev/gaspard4i/numismatic/currency/PlayerCurrencyManager*.class",
    "dev/gaspard4i/numismatic/currency/CurrencyNotifications*.class",
    "dev/gaspard4i/numismatic/currency/CurrencyConverter*.class"
)

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
    classDirectories.setFrom(
        files(classDirectories.files.map {
            fileTree(it) { exclude(jacocoExclusions) }
        })
    )
}

tasks.jacocoTestCoverageVerification {
    dependsOn(tasks.test)
    classDirectories.setFrom(
        files(classDirectories.files.map {
            fileTree(it) { exclude(jacocoExclusions) }
        })
    )
    violationRules {
        rule {
            limit {
                counter = "INSTRUCTION"
                minimum = BigDecimal("0.96")
            }
            limit {
                counter = "BRANCH"
                minimum = BigDecimal("0.90")
            }
        }
    }
}

tasks.check {
    dependsOn(tasks.jacocoTestCoverageVerification)
}
