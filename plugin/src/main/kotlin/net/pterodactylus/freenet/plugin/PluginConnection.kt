package net.pterodactylus.freenet.plugin

interface PluginConnection {

	fun sendMessage(parameters: Map<String, String>)

}
