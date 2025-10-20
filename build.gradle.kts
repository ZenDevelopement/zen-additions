plugins {
    id("fabric-loom") version "1.11-SNAPSHOT"
}

// Define version configurations
val versionConfigs = mapOf(
    "1.21.4-1.21.5" to mapOf(
        "minecraft" to "1.21.4",
        "yarn" to "1.21.4+build.1",
        "loader" to "0.16.10",
        "minecraftRange" to ">=1.21.4 <=1.21.5"
    ),
    "1.21.6-1.21.7" to mapOf(
        "minecraft" to "1.21.6",
        "yarn" to "1.21.6+build.1",
        "loader" to "0.16.14",
        "minecraftRange" to ">=1.21.6 <=1.21.7"
    ),
    "1.21.8-1.21.9" to mapOf(
        "minecraft" to "1.21.8",
        "yarn" to "1.21.8+build.1",
        "loader" to "0.17.2",
        "minecraftRange" to ">=1.21.8 <=1.21.9"
    )
)

// Get current build target from property or default to first version
val buildTarget = (project.findProperty("buildTarget") as String?) ?: "1.21.4-1.21.5"
val currentConfig = versionConfigs[buildTarget] ?: versionConfigs["1.21.4-1.21.5"]!!

base {
    archivesName = "${properties["archives_base_name"] as String}-${buildTarget}"
    version = properties["mod_version"] as String
    group = properties["maven_group"] as String
}

repositories {
    maven {
        name = "meteor-maven"
        url = uri("https://maven.meteordev.org/releases")
    }
    maven {
        name = "meteor-maven-snapshots"
        url = uri("https://maven.meteordev.org/snapshots")
    }
}

dependencies {
    // Fabric
    minecraft("com.mojang:minecraft:${currentConfig["minecraft"]}")
    mappings("net.fabricmc:yarn:${currentConfig["yarn"]}:v2")
    modImplementation("net.fabricmc:fabric-loader:${currentConfig["loader"]}")

    // Meteor
    modImplementation("meteordevelopment:meteor-client:${currentConfig["minecraft"]}-SNAPSHOT")
}

tasks {
    processResources {
        val propertyMap = mapOf(
            "version" to project.version,
            "mc_version" to currentConfig["minecraft"]!!,
            "mc_range" to currentConfig["minecraftRange"]!!
        )

        filesMatching("fabric.mod.json") {
            expand(propertyMap)
        }
    }

    jar {
        val licenseSuffix = project.base.archivesName.get()
        from("LICENSE") {
            rename { "${it}_${licenseSuffix}" }
        }
    }

    java {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.release = 21
    }
}
