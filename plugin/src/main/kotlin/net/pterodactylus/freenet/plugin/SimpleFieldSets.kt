package net.pterodactylus.freenet.plugin

import freenet.support.SimpleFieldSet

fun SimpleFieldSet.toMap(): Map<String, String> = directKeyValues() +
		fixedDirectSubsets()
			.flatMap { (key, value) ->
				value.toMap().mapKeys { "$key.${it.key}" }.toList() +
						value.directKeyValues().map { "$key.${it.key}" to it.value }
			}

// remove once https://freenet.mantishub.io/view.php?id=7197 is fixed.
private fun SimpleFieldSet.fixedDirectSubsets() = apply {
	putSingle("<test>.<test>", "<test>")
	removeSubset("<test>")
}.directSubsets()
