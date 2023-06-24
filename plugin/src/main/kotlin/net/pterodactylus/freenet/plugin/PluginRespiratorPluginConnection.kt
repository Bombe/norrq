package net.pterodactylus.freenet.plugin

import freenet.clients.fcp.FCPPluginConnection
import freenet.clients.fcp.FCPPluginMessage
import freenet.pluginmanager.PluginRespirator
import java.io.IOException

class PluginRespiratorPluginConnection(private val pluginRespirator: PluginRespirator, private val pluginName: String) {

	fun sendMessage(parameters: Map<String, String>) {
		try {
			getConnectionOrRequestNewConnection().send(buildFcpMessage(parameters))
		} catch (e: IOException) {
			fcpPluginConnection = null
			throw e
		}
	}

	private fun getConnectionOrRequestNewConnection() =
		fcpPluginConnection ?: pluginRespirator.connectToOtherPlugin(pluginName, ::handleReply).also { fcpPluginConnection = it }

	private fun buildFcpMessage(parameters: Map<String, String>) = FCPPluginMessage.construct().apply {
		parameters.forEach(params::putSingle)
	}

	private fun handleReply(fcpPluginConnection: FCPPluginConnection, fcpPluginMessage: FCPPluginMessage): FCPPluginMessage {
		TODO("Not yet implemented")
	}

	private var fcpPluginConnection: FCPPluginConnection? = null

}
