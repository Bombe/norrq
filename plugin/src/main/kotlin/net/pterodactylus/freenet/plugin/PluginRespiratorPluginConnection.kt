package net.pterodactylus.freenet.plugin

import freenet.clients.fcp.FCPPluginConnection
import freenet.clients.fcp.FCPPluginMessage
import freenet.pluginmanager.PluginRespirator

class PluginRespiratorPluginConnection(pluginName: String, private val pluginRespirator: PluginRespirator) : AsyncBasePluginConnection<FCPPluginConnection, FCPPluginMessage, FCPPluginMessage>(pluginName) {

	override fun sendMessage(connection: FCPPluginConnection, message: FCPPluginMessage) {
		connection.send(message)
	}

	override fun requestNewConnection(): FCPPluginConnection =
		pluginRespirator.connectToOtherPlugin(pluginName, ::handleReply)

	override fun extractReplies(reply: FCPPluginMessage) =
		reply.params.toMap()

	override fun getIdentifier(message: FCPPluginMessage): String =
		message.identifier

	override fun buildFcpMessage(parameters: Map<String, String>): FCPPluginMessage =
		FCPPluginMessage.construct().apply {
			parameters.forEach(params::putSingle)
		}

	private fun handleReply(fcpPluginConnection: FCPPluginConnection, fcpPluginMessage: FCPPluginMessage) =
		null.also {
			storeReply(fcpPluginMessage.identifier, fcpPluginMessage)
		}

}
