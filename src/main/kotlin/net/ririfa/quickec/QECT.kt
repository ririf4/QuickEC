@file:Suppress("ClassName")

package net.ririfa.quickec

import net.kyori.adventure.text.Component
import net.ririfa.langman.MessageKey

sealed class QECT : MessageKey<QuickECMSGProvider, Component> {
    sealed class Main : QECT() {
        sealed class Command : Main() {
            object THIS_COMMAND_IS_ONLY_FOR_PLAYERS : Command()
            object NO_PLAYER_PROVIDED : Command()
            object PLAYER_NOT_FOUND : Command()
            object NO_ENDER_CHEST_IN_INVENTORY : Command()
        }

        sealed class Help : Main() {
            object HELP_HEADER : Help()

            object EC_COMMAND : Help()
            object UEC_COMMAND : Help()
        }

        sealed class Message : Main() {
            object NO_PERMISSION : Message()
            object INVALID_UUID : Message()
        }
    }
}