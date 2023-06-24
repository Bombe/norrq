package net.pterodactylus.freenet.plugin

import freenet.clients.fcp.FCPPluginConnection
import freenet.clients.fcp.FCPPluginMessage
import freenet.pluginmanager.PluginRespirator
import freenet.support.SimpleFieldSet
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.hasEntry
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.io.IOException
import java.util.UUID

class PluginRespiratorPluginConnectionTest {

	@Test
	fun `sending a message requests plugin connection from respirator`() {
		pluginConnection.sendMessage(mapOf("a" to "b", "aa" to "bb"))
		verify(pluginRespirator).connectToOtherPlugin(eq("test.TargetPlugin"), any())
	}

	@Test
	fun `sending a message sends the message to the plugin connection`() {
		pluginConnection.sendMessage(mapOf("a" to "b", "aa" to "bb"))
		assertThat(sentMessages, hasSize(1))
		assertThat(sentMessages.single().params.toMap(), allOf(hasEntry("a", "b"), hasEntry("aa", "bb")))
	}

	@Test
	fun `sending two messages creates messages with different identifiers`() {
		pluginConnection.sendMessage(mapOf("a" to "b", "aa" to "bb"))
		pluginConnection.sendMessage(mapOf("a" to "c", "aa" to "cc"))
		assertThat(sentMessages, hasSize(2))
		assertThat(sentMessages.map(FCPPluginMessage::identifier).toSet(), hasSize(2))
	}

	@Test
	fun `sending two messages reuses the plugin talker`() {
		pluginConnection.sendMessage(mapOf("a" to "b", "aa" to "bb"))
		pluginConnection.sendMessage(mapOf("a" to "c", "aa" to "cc"))
		assertThat(createdFcpPluginConnections, hasSize(1))
	}

	@Test
	fun `new plugin talker is requested upon exception`() {
		val pluginRespirator = createPluginRespirator { throwingFCPPluginConnection }
		val pluginConnection = PluginRespiratorPluginConnection(pluginRespirator, "test.TargetPlugin")
		assertThrows<IOException> { pluginConnection.sendMessage(mapOf("a" to "b", "aa" to "bb")) }
		assertThrows<IOException> { pluginConnection.sendMessage(mapOf("a" to "c", "aa" to "cc")) }
		assertThat(createdFcpPluginConnections, hasSize(2))
	}

	@Test
	fun `IO exception from sendMessage is being thrown`() {
		val pluginRespirator = createPluginRespirator { throwingFCPPluginConnection }
		val pluginConnection = PluginRespiratorPluginConnection(pluginRespirator, "test.TargetPlugin")
		assertThrows<IOException> {
			pluginConnection.sendMessage(mapOf("a" to "b", "aa" to "bb"))
		}
	}

	private fun createFcpPluginConnection(messageConsumer: (message: FCPPluginMessage) -> Unit) = object : TestFCPPluginConnection() {
		override fun send(message: FCPPluginMessage) {
			messageConsumer(message)
		}
	}

	private fun createPluginRespirator(fcpPluginConnectionSupplier: () -> FCPPluginConnection) =
		mock<PluginRespirator>().apply {
			whenever(connectToOtherPlugin(any(), any())).thenAnswer { _ ->
				fcpPluginConnectionSupplier()
					.also { createdFcpPluginConnections += it }
			}
		}

	private val throwingFCPPluginConnection = createFcpPluginConnection { throw IOException("can’t send") }
	private val sentMessages = mutableListOf<FCPPluginMessage>()
	private val createdFcpPluginConnections = mutableListOf<FCPPluginConnection>()
	private val pluginRespirator = createPluginRespirator { createFcpPluginConnection(sentMessages::add) }
	private val pluginConnection = PluginRespiratorPluginConnection(pluginRespirator, "test.TargetPlugin")

}

// remove once https://freenet.mantishub.io/view.php?id=7197 is fixed.
private fun SimpleFieldSet.fixedDirectSubsets() = apply {
	putSingle("<test>.<test>", "<test>")
	removeSubset("<test>")
}.directSubsets()

private fun SimpleFieldSet.toMap(): Map<String, String> = directKeyValues() +
		fixedDirectSubsets()
			.flatMap { (key, value) -> value.directKeyValues().map { "$key.${it.key}" to it.value } }

private open class TestFCPPluginConnection : FCPPluginConnection {

	override fun send(direction: FCPPluginConnection.SendDirection, message: FCPPluginMessage) {
		TODO("Not yet implemented")
	}

	override fun send(message: FCPPluginMessage) {
		TODO("Not yet implemented")
	}

	override fun sendSynchronous(direction: FCPPluginConnection.SendDirection, message: FCPPluginMessage, timeoutNanoSeconds: Long): FCPPluginMessage {
		TODO("Not yet implemented")
	}

	override fun sendSynchronous(message: FCPPluginMessage, timeoutNanoSeconds: Long): FCPPluginMessage {
		TODO("Not yet implemented")
	}

	override fun getID(): UUID {
		TODO("Not yet implemented")
	}

}
