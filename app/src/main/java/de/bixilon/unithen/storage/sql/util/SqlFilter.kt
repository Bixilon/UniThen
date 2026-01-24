package de.bixilon.unithen.storage.sql.util

data class SqlFilter(
    val where: String,
    val parameters: List<String>,
) {

    companion object {

        fun and(vararg filters: Pair<String, Any?>): SqlFilter {
            val parameters = ArrayList<String>()
            val string = StringBuilder()

            for ((argument, value) in filters) {
                if (value == null) continue
                assert("\"" !in argument)
                assert("'" !in argument)

                if (string.isNotEmpty()) {
                    string.append(" AND ")
                }
                string.append(argument).append("=?")

                parameters += value.toString()
            }

            return SqlFilter(string.toString(), parameters)
        }
    }
}
