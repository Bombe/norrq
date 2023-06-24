package net.pterodactylus.freenet.plugin

import net.pterodactylus.fcp.FCPPluginReply
import net.pterodactylus.fcp.FcpAdapter
import net.pterodactylus.fcp.FcpConnection
import net.pterodactylus.fcp.FcpMessage
import java.util.concurrent.atomic.AtomicLong

@Suppress("ClassName")
class jFCPlibPluginConnection(pluginName: String, private val connectionSupplier: () -> FcpConnection) : AsyncBasePluginConnection<FcpConnection, FcpMessage, FCPPluginReply>(pluginName) {

	override fun requestNewConnection() =
		connectionSupplier()
			.also {
				it.addFcpListener(object : FcpAdapter() {
					override fun receivedFCPPluginReply(fcpConnection: FcpConnection, fcpPluginReply: FCPPluginReply) {
						handleReply(fcpPluginReply)
					}
				})
			}

	override fun sendMessage(connection: FcpConnection, message: FcpMessage) {
		connection.sendMessage(message)
	}

	override fun extractReplies(reply: FCPPluginReply) = reply.fields
		.filterKeys { it.startsWith("Replies.") }
		.mapKeys { (key, value) -> key.removePrefix("Replies.") }

	override fun buildFcpMessage(parameters: Map<String, String>) = FcpMessage("FCPPluginMessage").apply {
		put("PluginName", pluginName)
		put("Identifier", "jFCPlibPluginConnection-$pluginName-${counter.getAndIncrement()}")
		parameters.mapKeys { (key, _) -> "Param.$key" }.forEach(::put)
	}

	override fun getIdentifier(message: FcpMessage) = message.getField("Identifier")

	private fun handleReply(fcpPluginReply: FCPPluginReply) {
		storeReply(fcpPluginReply.identifier, fcpPluginReply)
	}

}

private val counter = AtomicLong()
