@file:Suppress("DuplicatedCode")

package net.rk4z.quickec

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import net.ririfa.langman.InitType
import net.ririfa.langman.LangMan
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.plugin.java.JavaPlugin
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.UUID
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService

@Suppress("unused")
class QuickEC : JavaPlugin(), Listener {
    companion object {
        val thread: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()

        val logger: Logger = LoggerFactory.getLogger(QuickEC::class.java.simpleName)
    }

    lateinit var LM: LangMan<QuickECMSGProvider, Component>

    val langDir = dataFolder.resolve("lang")
    val availableLang = listOf("en", "ja", "zh", "ko", "de", "es", "fr", "it")

    override fun onLoad() {
        extractLangFiles()
        LM = LangMan.createNew(
            { Component.text(it) },
            QECT::class,
            false
        )

        LM.init(
            InitType.YAML,
            langDir,
            availableLang
        )
    }

    override fun onEnable() {
        server.pluginManager.registerEvents(this, this)
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage(LM.getSysMessage(QECT.Main.Command.THIS_COMMAND_IS_ONLY_FOR_PLAYERS))
            return true
        }

        when (command.name.lowercase()) {
            "ec" -> {
                if (args.getOrNull(0) == "help") {
                    sendHelp(sender)
                    return true
                }
                QEC {
                    if (!sender.hasPermission("quickec.open.ignore_inventory")) {
                        val hasEnderChest = sender.inventory.contains(Material.ENDER_CHEST)
                        if (!hasEnderChest) {
                            sender.sendMessage(LM.getSysMessage(QECT.Main.Command.NO_ENDER_CHEST_IN_INVENTORY))
                            return@QEC
                        }
                    }

                    if (sender.hasPermission("quickec.open.self")) {
                        Bukkit.getScheduler().runTask(this@QuickEC, Runnable {
                            sender.openInventory(sender.enderChest)
                        })
                    } else {
                        sender.sendMessage(LM.getSysMessage(QECT.Main.Message.NO_PERMISSION))
                    }
                }
            }

            "uec" -> {
                if (args.getOrNull(0) == "_help" || args.isEmpty()) {
                    sendHelp(sender)
                    return true
                }

                if (sender.hasPermission("quickec.open.others")) {
                    QEC {
                        if (args[0].isBlank() || args[0].isEmpty()) {
                            sender.sendMessage(LM.getSysMessage(QECT.Main.Command.NO_PLAYER_PROVIDED))
                            return@QEC
                        }

                        val target: Player? = try {
                            if (args[0].matches(Regex("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$"))) {
                                Bukkit.getPlayer(UUID.fromString(args[0]))
                            } else {
                                Bukkit.getPlayer(args[0])
                            }
                        } catch (e: IllegalArgumentException) {
                            sender.sendMessage(LM.getSysMessage(QECT.Main.Message.INVALID_UUID))
                            return@QEC
                        }

                        if (target == null) {
                            sender.sendMessage(LM.getSysMessage(QECT.Main.Command.PLAYER_NOT_FOUND))
                            return@QEC
                        }

                        Bukkit.getScheduler().runTask(this@QuickEC, Runnable {
                            sender.openInventory(sender.enderChest)
                        })
                    }
                } else {
                    sender.sendMessage(LM.getSysMessage(QECT.Main.Message.NO_PERMISSION))
                }
            }
        }
        return true
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<out String>
    ): MutableList<String> {
        if (command.name.lowercase() == "uec") {
            if (args.size == 1) {
                val players = Bukkit.getOnlinePlayers().map { it.name } + listOf("_help")
                return players.toMutableList()
            }
        } else if (command.name.lowercase() == "ec") {
            return mutableListOf("help")
        } else {
            return mutableListOf()
        }

        return mutableListOf()
    }

    @EventHandler
    fun onPlayerInteract(event: PlayerInteractEvent) {
        val player = event.player

        // If you're sneaking, you can put the ender chest.
        if (player.inventory.itemInMainHand.type == Material.ENDER_CHEST && !player.isSneaking && event.action.isRightClick) {
            if (player.hasPermission("quickec.open.click")) {
                event.isCancelled = true
                player.openInventory(player.enderChest)
            }
        }
    }

    private fun sendHelp(player: Player) {
        val p = player.adapt()
        val headerComponent = p.getMessage(QECT.Main.Help.HELP_HEADER)
            .color(NamedTextColor.GOLD)
            .decorate(TextDecoration.BOLD)

        val hStartComponent = Component.text("=======").color(NamedTextColor.GOLD)
        val hEndComponent = Component.text("=======").color(NamedTextColor.GOLD)

        val commands = listOf(
            "ec" to QECT.Main.Help.EC_COMMAND,
            "uec" to QECT.Main.Help.UEC_COMMAND
        )

        player.sendMessage(hStartComponent.append(headerComponent).append(hEndComponent))

        commands.forEach { (command, key) ->
            player.sendMessage(
                if (key == QECT.Main.Help.UEC_COMMAND) {
                    Component.text("$command - ").append(p.getMessage(key, "quickec.open.others"))
                        .color(NamedTextColor.GREEN)
                } else {
                    Component.text("$command - ").append(p.getMessage(key))
                        .color(NamedTextColor.GREEN)
                }
            )
        }

        player.sendMessage(Component.text("=======================").color(NamedTextColor.GOLD))
    }

    private fun extractLangFiles() {
        try {
            if (!Files.exists(langDir.toPath())) {
                Files.createDirectories(langDir.toPath())
            }

            val devSourceDir = Paths.get("build/resources/main/assets/quickec/lang")
            if (Files.exists(devSourceDir)) {
                copyLanguageFiles(devSourceDir, langDir.toPath())
                return
            }

            val langPath = "assets/quickec/lang/"
            val classLoader = this::class.java.classLoader
            val resourceUrl = classLoader.getResource(langPath)

            if (resourceUrl == null) {
                Companion.logger.error("Failed to find language directory in JAR: $langPath")
                return
            }

            val uri = resourceUrl.toURI()
            val fs = if (uri.scheme == "jar") FileSystems.newFileSystem(uri, emptyMap<String, Any>()) else null
            val langDirPath = Paths.get(uri)

            copyLanguageFiles(langDirPath, langDir.toPath())

            fs?.close()
        } catch (e: Exception) {
            Companion.logger.error("Failed to extract language files", e)
        }
    }

    private fun copyLanguageFiles(sourceDir: Path, targetDir: Path) {
        Files.walk(sourceDir).use { paths ->
            paths.filter { Files.isRegularFile(it) && it.toString().endsWith(".yml") }.forEach { resourceFile ->
                val targetFile = targetDir.resolve(resourceFile.fileName.toString())
                if (!Files.exists(targetFile)) {
                    Files.copy(resourceFile, targetFile)
                }
            }
        }
    }
}
