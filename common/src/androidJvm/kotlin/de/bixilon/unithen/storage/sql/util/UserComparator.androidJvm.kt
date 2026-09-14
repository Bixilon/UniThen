package de.bixilon.unithen.storage.sql.util

import java.text.Collator

private val COLLATOR = Collator.getInstance().apply {
    strength = Collator.PRIMARY
    decomposition = Collator.CANONICAL_DECOMPOSITION
}

actual fun compare(a: String, b: String) = COLLATOR.compare(a, b)
