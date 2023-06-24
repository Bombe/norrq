package net.pterodactylus.freenet.plugin

import kotlin.concurrent.thread
import net.pterodactylus.fcp.FCPPluginReply
import net.pterodactylus.fcp.FcpConnection
import net.pterodactylus.fcp.FcpListener
import net.pterodactylus.fcp.FcpMessage
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasEntry
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.IOException

@Suppress("ClassName")
class jFCPlibPluginConnectionTest {

	@Test
	fun `sending a message calls the correct method`() {
		jFCPlibPluginConnection.sendMessage(mapOf("a" to "b", "aa" to "bb"))
		assertThat(sentMessages.single().name, equalTo("FCPPluginMessage"))
		assertThat(sentMessages.single().fields, allOf(hasEntry("PluginName", "test.TargetPlugin"), hasEntry("Param.a", "b"), hasEntry("Param.aa", "bb")))
	}

	@Test
	fun `every message gets its own unique identifier`() {
		jFCPlibPluginConnection.sendMessage(mapOf("a" to "b", "aa" to "bb"))
		jFCPlibPluginConnection.sendMessage(mapOf("a" to "c", "aa" to "cc"))
		assertThat(sentMessages, hasSize(2))
		assertThat(sentMessages.map { it.fields.get("Identifier") }.toSet(), hasSize(2))
	}

	@Test
	fun `IO exception from fcp connection is thrown`() {
		val fcpConnection = createThrowingFcpConnection()
		val jFCPlibPluginConnection = createConnectionRequestCountingPluginConnection(fcpConnection)
		assertThrows<IOException> { jFCPlibPluginConnection.sendMessage(mapOf("a" to "b", "aa" to "bb")) }
	}

	@Test
	fun `sending the first message requests a new connection`() {
		jFCPlibPluginConnection.sendMessage(mapOf("a" to "b", "aa" to "bb"))
		assertThat(connectionRequests, equalTo(1))
	}

	@Test
	fun `sending the second message does not request a new connection`() {
		jFCPlibPluginConnection.sendMessage(mapOf("a" to "b", "aa" to "bb"))
		jFCPlibPluginConnection.sendMessage(mapOf("a" to "c", "aa" to "cc"))
		assertThat(connectionRequests, equalTo(1))
	}

	@Test
	fun `sending a message after an IO exception on send requests a new connection`() {
		val fcpConnection = createThrowingFcpConnection()
		val jFCPlibPluginConnection = createConnectionRequestCountingPluginConnection(fcpConnection)
		assertThrows<IOException> { jFCPlibPluginConnection.sendMessage(mapOf("a" to "b", "aa" to "bb")) }
		assertThrows<IOException> { jFCPlibPluginConnection.sendMessage(mapOf("a" to "c", "aa" to "cc")) }
		assertThat(connectionRequests, equalTo(2))
	}

	@Test
	fun `reply from plugin is returned correctly`() {
		repliesToSend += mapOf("aaa" to "bbb", "aaaa" to "bbbb")
		val reply = jFCPlibPluginConnection.sendMessage(mapOf("a" to "b", "aa" to "bb"))
		assertThat(reply, allOf(hasEntry("aaa", "bbb"), hasEntry("aaaa", "bbbb")))
	}

	private fun createThrowingFcpConnection() = object : FcpConnection() {
		override fun sendMessage(fcpMessage: FcpMessage) {
			throw IOException("can’t send")
		}
	}

	private fun createConnectionRequestCountingPluginConnection(fcpConnection: FcpConnection) = jFCPlibPluginConnection({
		connectionRequests++
		fcpConnection
	}, "test.TargetPlugin")

	private var connectionRequests = 0
	private val sentMessages = mutableListOf<FcpMessage>()
	private val registeredFcpListeners = mutableListOf<FcpListener>()
	private val repliesToSend = mutableListOf<Map<String, String>>()
	private val fcpConnection = object : FcpConnection() {
		override fun sendMessage(fcpMessage: FcpMessage) {
			sentMessages += fcpMessage
			sendReplyOnNewThread(fcpMessage)
		}

		private fun sendReplyOnNewThread(fcpMessage: FcpMessage) {
			(repliesToSend.removeFirstOrNull() ?: emptyMap())
				.let { reply ->
					registeredFcpListeners.forEach {
						val message = FcpMessage("FCPPluginReply")
						message.setField("Identifier", fcpMessage.getField("Identifier"))
						reply.mapKeys { (key, value) -> "Replies.$key" }.forEach(message::setField)
						thread {
							it.receivedFCPPluginReply(this, FCPPluginReply(message, null))
						}
					}
				}
		}

		override fun addFcpListener(fcpListener: FcpListener) {
			registeredFcpListeners += fcpListener
		}
	}
	private val jFCPlibPluginConnection = createConnectionRequestCountingPluginConnection(fcpConnection)

}
