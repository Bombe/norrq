package net.pterodactylus.freenet.plugin

import net.pterodactylus.fcp.FCPPluginReply
import net.pterodactylus.fcp.FcpAdapter
import net.pterodactylus.fcp.FcpConnection
import net.pterodactylus.fcp.FcpMessage
import java.io.IOException
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicLong

@Suppress("ClassName")
class jFCPlibPluginConnection(private val pluginName: String, private val connectionSupplier: () -> FcpConnection) : PluginConnection {

	override fun sendMessage(parameters: Map<String, String>): Map<String, String> {
		val pluginFcpMessage = buildPluginFcpMessage(parameters)
		val identifier = pluginFcpMessage.getField("Identifier")
		val latch = CountDownLatch(1)
		try {
			synchronized(identifierLatches) {
				identifierLatches += identifier to latch
			}
			getConnectionOrRequestNewConnection().sendMessage(pluginFcpMessage)
		} catch (e: IOException) {
			currentConnection = null
			throw e
		}
		latch.await()
		synchronized(identifierLatches) {
			return identifierReplies.remove(identifier)
				?.fields
				?.filterKeys { it.startsWith("Replies.") }
				?.mapKeys { (key, value) -> key.removePrefix("Replies.") }
				?: emptyMap()
		}
	}

	private fun getConnectionOrRequestNewConnection() =
		currentConnection ?: connectionSupplier()
			.also { currentConnection = it }
			.also {
				it.addFcpListener(object : FcpAdapter() {
					override fun receivedFCPPluginReply(fcpConnection: FcpConnection, fcpPluginReply: FCPPluginReply) {
						handleReply(fcpPluginReply)
					}
				})
			}

	private fun handleReply(fcpPluginReply: FCPPluginReply) {
		synchronized(identifierLatches) {
			identifierReplies += fcpPluginReply.identifier to fcpPluginReply
			identifierLatches.remove(fcpPluginReply.identifier)!!.countDown()
		}
	}

	private fun buildPluginFcpMessage(parameters: Map<String, String>) = FcpMessage("FCPPluginMessage").apply {
		put("PluginName", pluginName)
		put("Identifier", "jFCPlibPluginConnection-$pluginName-${counter.getAndIncrement()}")
		parameters.mapKeys { (key, _) -> "Param.$key" }.forEach(::put)
	}

	private var currentConnection: FcpConnection? = null
	private val identifierLatches = mutableMapOf<String, CountDownLatch>()
	private val identifierReplies = mutableMapOf<String, FCPPluginReply>()

}

private val counter = AtomicLong()
