package de.bixilon.unithen.storage.sql

import android.database.Cursor
import de.bixilon.kutil.exception.Unreachable
import de.bixilon.unithen.storage.Key
import de.bixilon.unithen.storage.sql.util.SqlFilter

abstract class SqlTable<T>(
    val storage: SqlStorage,
    val table: String,
) {
    val count get() = storage.query("SELECT COUNT(*) FROM $table;") { it.moveToFirst(); it.getInt(0) }

    protected abstract val columns: List<String>

    protected abstract fun map(cursor: Cursor): T

    @Deprecated("", level = DeprecationLevel.ERROR)
    fun get(): Nothing = Unreachable()

    @Deprecated("", level = DeprecationLevel.ERROR)
    fun update(id: Int): Nothing = Unreachable()

    protected fun update(id: Key, filter: SqlFilter) {
        storage.execute("UPDATE $table SET ${filter.where} WHERE id=?", parameters = arrayOf(*filter.parameters.toTypedArray(), id))
    }


    private fun <X> select(where: String = "", arguments: Array<out Any>, runnable: (Cursor) -> X): X {
        val actualWhere = if (where.isBlank()) "" else "WHERE $where"
        return storage.query("SELECT ${columns.joinToString(",")} FROM $table $actualWhere", *arguments, runnable = runnable)
    }

    protected fun single(filter: SqlFilter) = single(filter.where, arguments = filter.parameters.toTypedArray())
    protected fun single(where: String = "", vararg arguments: Any): T? {
        return select(where, arguments = arguments) {
            when (it.count) {
                0 -> null
                1 -> {
                    it.moveToNext()
                    map(it)
                }

                else -> throw IllegalStateException("More than one result found: $where")
            }
        }
    }


    protected fun Cursor.collectAll(): List<T> {
        val result = ArrayList<T>(count)

        while (moveToNext()) {
            result += map(this)
        }

        return result
    }

    protected fun all(filter: SqlFilter) = all(filter.where, *filter.parameters.toTypedArray())
    protected fun all(where: String = "", vararg arguments: Any): List<T> {
        return select(where, arguments = arguments, runnable = { it.collectAll() })
    }

    fun all(): List<T> = all("TRUE")
}
