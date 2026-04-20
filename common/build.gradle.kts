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

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}
