package net.pterodactylus.freenet.plugin

import freenet.support.SimpleFieldSet
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test

class SimpleFieldSetsTest {

	@Test
	fun `simple SimpleFieldSet is converted correctly`() {
		val simpleFieldSet = SimpleFieldSet(true)
		simpleFieldSet.putSingle("a", "b")
		simpleFieldSet.putSingle("aa", "bb")
		assertThat(simpleFieldSet.toMap(), equalTo(mapOf("a" to "b", "aa" to "bb")))
	}

	@Test
	fun `more complex SimpleFieldSet is converted correctly`() {
		val simpleFieldSet = SimpleFieldSet(true)
		simpleFieldSet.putSingle("a", "b")
		simpleFieldSet.putSingle("aa.a", "bb.b")
		simpleFieldSet.putSingle("aa.aa.a", "bb.bb.b")
		assertThat(simpleFieldSet.toMap(), equalTo(mapOf("a" to "b", "aa.a" to "bb.b", "aa.aa.a" to "bb.bb.b")))
	}

}
