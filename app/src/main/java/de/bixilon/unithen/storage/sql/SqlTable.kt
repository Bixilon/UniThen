package de.bixilon.unithen.storage.sql

import android.database.Cursor
import de.bixilon.kutil.exception.Unreachable
import de.bixilon.unithen.storage.sql.util.SqlFilter

abstract class SqlTable<T>(
    val storage: SqlStorage,
    val name: String,
) {
    val count get() = storage.query("SELECT COUNT(*) FROM ?;", name) { it.getInt(0) }

    protected abstract val columns: List<String>

    protected abstract fun map(cursor: Cursor): T

    @Deprecated("", level = DeprecationLevel.ERROR)
    fun get(): Nothing = Unreachable()

    @Deprecated("", level = DeprecationLevel.ERROR)
    fun update(id: Int): Nothing = Unreachable()


    private fun <X> where(filter: SqlFilter, runnable: (Cursor) -> X) = where(filter.where, arguments = filter.parameters.toTypedArray(), runnable)
    private fun <X> where(where: String = "", vararg arguments: String, runnable: (Cursor) -> X): X {
        val actualWhere = if (where.isBlank()) "" else "WHERE $where"
        return storage.query("SELECT ${columns.joinToString(",")} FROM $name $actualWhere", *arguments, runnable = runnable)
    }

    protected fun single(filter: SqlFilter) = single(filter.where, arguments = filter.parameters.toTypedArray())
    protected fun single(where: String = "", vararg arguments: String): T? {
        return where(where, *arguments) { if (it.count == 0) return@where null else it.moveToNext(); map(it) }
    }


    protected fun all(filter: SqlFilter) = all(filter.where, *filter.parameters.toTypedArray())
    protected fun all(where: String = "", vararg arguments: String): List<T> {
        return where(where, *arguments) {
            val result = ArrayList<T>(it.count)

            while (it.moveToNext()) {
                result += map(it)
            }

            return@where result
        }
    }

    fun all(): List<T> = all("TRUE")
}
