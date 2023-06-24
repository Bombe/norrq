package net.pterodactylus.freenet.plugin

import java.io.IOException
import java.util.concurrent.CountDownLatch

abstract class AsyncBasePluginConnection<C, M, R>(protected val pluginName: String) : PluginConnection {

	override fun sendMessage(parameters: Map<String, String>): Map<String, String> {
		val message = buildFcpMessage(parameters)
		val identifier = getIdentifier(message)
		CountDownLatch(1).let { latch ->
			synchronized(identifierLatches) {
				identifierLatches += identifier to latch
			}
			val connection = getConnectionOrRequestNewConnection()
			try {
				sendMessage(connection, message)
				latch.await()
				synchronized(identifierLatches) {
					return extractReplies(identifierReplies[identifier]!!)
				}
			} catch (e: IOException) {
				this.connection = null
				throw e;
			} finally {
				synchronized(identifierLatches) {
					identifierLatches.remove(identifier)
					identifierReplies.remove(identifier)
				}
			}

		}
	}

	private fun getConnectionOrRequestNewConnection() =
		connection ?: requestNewConnection()
			.also { connection = it }

	abstract fun requestNewConnection(): C

	abstract fun buildFcpMessage(parameters: Map<String, String>): M

	abstract fun getIdentifier(message: M): String

	abstract fun sendMessage(connection: C, message: M)

	abstract fun extractReplies(reply: R): Map<String, String>

	protected fun storeReply(identifier: String, reply: R) {
		synchronized(identifierLatches) {
			identifierReplies += identifier to reply
			identifierLatches[identifier]!!.countDown()
		}
	}

	private var connection: C? = null
	private val identifierLatches = mutableMapOf<String, CountDownLatch>()
	private val identifierReplies = mutableMapOf<String, R>()

}
