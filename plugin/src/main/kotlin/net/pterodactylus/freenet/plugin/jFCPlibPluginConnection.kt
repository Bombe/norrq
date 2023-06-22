package net.pterodactylus.freenet.plugin

import net.pterodactylus.fcp.FcpConnection
import net.pterodactylus.fcp.FcpMessage
import java.io.IOException
import java.util.concurrent.atomic.AtomicLong

@Suppress("ClassName")
class jFCPlibPluginConnection(private val connectionSupplier: () -> FcpConnection, private val pluginName: String) : PluginConnection {

	override fun sendMessage(parameters: Map<String, String>) {
		try {
			getConnectionOrRequestNewConnection().sendMessage(buildPluginFcpMessage(parameters))
		} catch (e: IOException) {
			currentConnection = null
			throw e
		}
	}

	private fun getConnectionOrRequestNewConnection() =
		currentConnection ?: connectionSupplier().also { currentConnection = it }

	private fun buildPluginFcpMessage(parameters: Map<String, String>) = FcpMessage("FCPPluginMessage").apply {
		put("PluginName", pluginName)
		put("Identifier", "jFCPlibPluginConnection-$pluginName-${counter.getAndIncrement()}")
		parameters.mapKeys { (key, _) -> "Parameter.$key" }.forEach(::put)
	}

	private var currentConnection: FcpConnection? = null

}

private val counter = AtomicLong()
