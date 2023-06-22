package net.pterodactylus.freenet.plugin

import net.pterodactylus.fcp.FcpConnection
import net.pterodactylus.fcp.FcpMessage
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasEntry
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.Test

@Suppress("ClassName")
class jFCPlibPluginConnectionTest {

	@Test
	fun `sending a message calls the correct method`() {
		jFCPlibPluginConnection.sendMessage(mapOf("a" to "b", "aa" to "bb"))
		assertThat(sentMessages.single().name, equalTo("FCPPluginMessage"))
		assertThat(sentMessages.single().fields, allOf(hasEntry("PluginName", "test.TargetPlugin"), hasEntry("Parameter.a", "b"), hasEntry("Parameter.aa", "bb")))
	}

	@Test
	fun `every message gets its own unique identifier`() {
		jFCPlibPluginConnection.sendMessage(mapOf("a" to "b", "aa" to "bb"))
		jFCPlibPluginConnection.sendMessage(mapOf("a" to "c", "aa" to "cc"))
		assertThat(sentMessages, hasSize(2))
		assertThat(sentMessages.map { it.fields.get("Identifier") }.toSet(), hasSize(2))
	}

	private val sentMessages = mutableListOf<FcpMessage>()
	private val fcpConnection = object : FcpConnection() {
		override fun sendMessage(fcpMessage: FcpMessage) {
			sentMessages += fcpMessage
		}
	}
	private val jFCPlibPluginConnection = jFCPlibPluginConnection(fcpConnection, "test.TargetPlugin")

}
