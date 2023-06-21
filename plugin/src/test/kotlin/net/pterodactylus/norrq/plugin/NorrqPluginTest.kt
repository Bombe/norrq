package net.pterodactylus.norrq.plugin

import freenet.pluginmanager.PluginRespirator
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock

class NorrqPluginTest {

	@Test
	fun `plugin can be started`() {
		val respirator = mock<PluginRespirator>()!!
		plugin.runPlugin(respirator)
	}

	@Test
	fun `plugin can be terminated`() {
		plugin.terminate()
	}

	private val plugin = NorrqPlugin()

}
