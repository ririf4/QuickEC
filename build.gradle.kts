import net.minecrell.pluginyml.bukkit.BukkitPluginDescription

plugins {
    kotlin("jvm") version "2.3.0"
    id("xyz.jpenilla.run-paper") version "3.0.2"
    id("net.minecrell.plugin-yml.bukkit") version "0.6.0"
}

group = "net.rk4z"
version = "2.1.2"

repositories {
    mavenCentral()
    mavenLocal()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.ririfa.net/maven2/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.17.1-R0.1-SNAPSHOT")
    library("net.ririfa:langman-core:+")
    library("net.ririfa:langman-ext.yaml:+")
    library("net.ririfa:igf:+")
}

kotlin {
    jvmToolchain(21)
}

bukkit {
    main = "net.ririfa.quickec.QuickEC"
    foliaSupported = false
    apiVersion = "1.21"
    load = BukkitPluginDescription.PluginLoadOrder.STARTUP
    authors = listOf("RiriFa")
    contributors = listOf("RiriFa", "cotrin_d8")

    depend = listOf("Kotlin")
    softDepend = listOf("LuckPerms")

    commands {
        register("ec") {
            description = "Open your ender chest"
        }

        register("uec") {
            description = "Open another player's ender chest"
        }
    }

    permissions {
        register("quickec.open.self") {
            description = "Allows the player to open their own ender chest"
            default = BukkitPluginDescription.Permission.Default.TRUE
        }

        register("quickec.open.others") {
            description = "Allows the player to open other players' ender chests"
            default = BukkitPluginDescription.Permission.Default.OP
        }

        register("quickec.open.ignore_inventory") {
            description = "Allows the player to open their ender chest even if they don't have one in their inventory"
            default = BukkitPluginDescription.Permission.Default.OP
        }

        register("quickec.open.click") {
            description = "Allows the player to open their ender chest by clicking"
            default = BukkitPluginDescription.Permission.Default.TRUE
        }
    }
}


tasks {
    runServer {
        minecraftVersion("1.21.4")
    }
}