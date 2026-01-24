package de.bixilon.unithen.storage.sql.util

data class SqlFilter(
    val where: String,
    val parameters: List<Any>,
) {

    companion object {
        val EMPTY = SqlFilter("", emptyList())

        fun join(separator: String, vararg filters: Pair<String, Any?>): SqlFilter {
            val parameters = ArrayList<Any>()
            val string = StringBuilder()

            for ((argument, value) in filters) {
                if (value == null) continue
                assert("\"" !in argument)
                assert("'" !in argument)

                if (string.isNotEmpty()) {
                    string.append(separator)
                }
                string.append(argument).append("=?")

                parameters += value
            }
            if (parameters.isEmpty()) return SqlFilter.EMPTY

            return SqlFilter("$string", parameters)
        }

        fun and(vararg filters: Pair<String, Any?>) = join(" AND ", *filters)
        fun or(vararg filters: Pair<String, Any?>) = join(" OR ", *filters)
        fun comma(vararg filters: Pair<String, Any?>) = join(",", *filters)
    }
}
