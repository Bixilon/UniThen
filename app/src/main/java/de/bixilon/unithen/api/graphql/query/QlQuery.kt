package de.bixilon.unithen.api.graphql.query

import de.bixilon.kutil.string.WhitespaceUtil.removeMultipleWhitespaces

@JvmInline
value class QlQuery(val query: String) {

    companion object {

        fun of(raw: String) = QlQuery(raw.replace("\n", "").removeMultipleWhitespaces())
    }
}
