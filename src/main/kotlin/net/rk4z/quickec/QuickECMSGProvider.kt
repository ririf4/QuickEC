package net.rk4z.quickec

import net.kyori.adventure.text.Component
import net.ririfa.langman.def.MessageProviderDefault
import org.bukkit.entity.Player

class QuickECMSGProvider(private val player: Player) : MessageProviderDefault<QuickECMSGProvider, Component>(Component::class.java, QECT::class.java) {
	override fun getLanguage(): String {
		return player.locale().language
	}
}

fun Player.adapt(): QuickECMSGProvider {
	return QuickECMSGProvider(this)
}