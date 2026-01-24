package de.bixilon.unithen.storage.sql

import android.database.Cursor
import de.bixilon.kutil.exception.Unreachable
import de.bixilon.unithen.storage.sql.util.SqlFilter

abstract class SqlTable<T>(
    val storage: SqlStorage,
    val table: String,
) {
    val count get() = storage.query("SELECT COUNT(*) FROM ?;", table) { it.getInt(0) }

    protected abstract val columns: List<String>

    protected abstract fun map(cursor: Cursor): T

    @Deprecated("", level = DeprecationLevel.ERROR)
    fun get(): Nothing = Unreachable()

    @Deprecated("", level = DeprecationLevel.ERROR)
    fun update(id: Int): Nothing = Unreachable()


    private fun <X> select(filter: SqlFilter, runnable: (Cursor) -> X) = select(filter.where, arguments = filter.parameters.toTypedArray(), runnable)
    private fun <X> select(where: String = "", vararg arguments: String, runnable: (Cursor) -> X): X {
        val actualWhere = if (where.isBlank()) "" else "WHERE $where"
        return storage.query("SELECT ${columns.joinToString(",")} FROM $table $actualWhere", *arguments, runnable = runnable)
    }

    protected fun single(filter: SqlFilter) = single(filter.where, arguments = filter.parameters.toTypedArray())
    protected fun single(where: String = "", vararg arguments: String): T? {
        return select(where, *arguments) { if (it.count == 0) return@select null else it.moveToNext(); map(it) }
    }


    protected fun all(filter: SqlFilter) = all(filter.where, *filter.parameters.toTypedArray())
    protected fun all(where: String = "", vararg arguments: String): List<T> {
        return select(where, *arguments) {
            val result = ArrayList<T>(it.count)

            while (it.moveToNext()) {
                result += map(it)
            }

            return@select result
        }
    }

    fun all(): List<T> = all("TRUE")
}
