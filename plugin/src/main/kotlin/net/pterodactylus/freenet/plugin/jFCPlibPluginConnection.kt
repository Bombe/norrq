package net.pterodactylus.freenet.plugin

import net.pterodactylus.fcp.FcpConnection
import net.pterodactylus.fcp.FcpMessage
import java.util.concurrent.atomic.AtomicLong

@Suppress("ClassName")
class jFCPlibPluginConnection(private val fcpConnection: FcpConnection, private val pluginName: String) : PluginConnection {

	override fun sendMessage(parameters: Map<String, String>) {
		fcpConnection.sendMessage(buildPluginFcpMessage(parameters))
	}

	private fun buildPluginFcpMessage(parameters: Map<String, String>) = FcpMessage("FCPPluginMessage").apply {
		put("PluginName", pluginName)
		put("Identifier", "jFCPlibPluginConnection-$pluginName-${counter.getAndIncrement()}")
		parameters.mapKeys { (key, _) -> "Parameter.$key" }.forEach(::put)
	}

}

private val counter = AtomicLong()
