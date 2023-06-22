package net.pterodactylus.freenet.plugin

import java.io.IOException

interface PluginConnection {

	@Throws(IOException::class)
	fun sendMessage(parameters: Map<String, String>)

}
