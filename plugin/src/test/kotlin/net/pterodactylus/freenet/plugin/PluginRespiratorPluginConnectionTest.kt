package net.pterodactylus.freenet.plugin

import freenet.clients.fcp.FCPPluginConnection
import freenet.clients.fcp.FCPPluginMessage
import freenet.pluginmanager.FredPluginFCPMessageHandler.ClientSideFCPMessageHandler
import freenet.pluginmanager.PluginRespirator
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
		val pluginConnection = PluginRespiratorPluginConnection("test.TargetPlugin", pluginRespirator)
		assertThrows<IOException> { pluginConnection.sendMessage(mapOf("a" to "b", "aa" to "bb")) }
		assertThrows<IOException> { pluginConnection.sendMessage(mapOf("a" to "c", "aa" to "cc")) }
		assertThat(createdFcpPluginConnections, hasSize(2))
	}

	@Test
	fun `IO exception from sendMessage is being thrown`() {
		val pluginRespirator = createPluginRespirator { throwingFCPPluginConnection }
		val pluginConnection = PluginRespiratorPluginConnection("test.TargetPlugin", pluginRespirator)
		assertThrows<IOException> {
			pluginConnection.sendMessage(mapOf("a" to "b", "aa" to "bb"))
		}
	}

	@Test
	fun `reply from plugin is returned correctly`() {
		repliesToSend += { it.apply { with(it.params) { putSingle("aaa", "bbb"); putSingle("aaaa", "bbbb") } } }
		val reply = pluginConnection.sendMessage(mapOf("a" to "b", "aa" to "bb"))
		assertThat(reply, allOf(hasEntry("aaa", "bbb"), hasEntry("aaaa", "bbbb")))
	}

	private fun createFcpPluginConnection(messageConsumer: (message: FCPPluginMessage) -> Unit) = object : TestFCPPluginConnection() {
		override fun send(message: FCPPluginMessage) {
			messageConsumer(message)
			repliesToSend.removeFirstOrNull()
				.let { reply ->
					registeredFcpMessageHandlers.last().handlePluginFCPMessage(this, reply?.invoke(message) ?: FCPPluginMessage.constructSuccessReply(message))
				}
		}
	}

	private fun createPluginRespirator(fcpPluginConnectionSupplier: () -> FCPPluginConnection) =
		mock<PluginRespirator>().apply {
			whenever(connectToOtherPlugin(any(), any())).thenAnswer { invocation ->
				registeredFcpMessageHandlers += invocation.arguments[1] as ClientSideFCPMessageHandler
				fcpPluginConnectionSupplier()
					.also { createdFcpPluginConnections += it }
			}
		}

	private val throwingFCPPluginConnection = createFcpPluginConnection { throw IOException("can’t send") }
	private val sentMessages = mutableListOf<FCPPluginMessage>()
	private val repliesToSend = mutableListOf<(FCPPluginMessage) -> FCPPluginMessage>()
	private val createdFcpPluginConnections = mutableListOf<FCPPluginConnection>()
	private val registeredFcpMessageHandlers = mutableListOf<ClientSideFCPMessageHandler>()
	private val pluginRespirator = createPluginRespirator { createFcpPluginConnection(sentMessages::add) }
	private val pluginConnection = PluginRespiratorPluginConnection("test.TargetPlugin", pluginRespirator)

}

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
