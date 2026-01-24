package de.bixilon.unithen.storage.sql

import android.database.Cursor
import de.bixilon.unithen.storage.Appointment
import de.bixilon.unithen.storage.Course
import de.bixilon.unithen.storage.Key
import de.bixilon.unithen.storage.sql.SqlUtil.db
import de.bixilon.unithen.storage.sql.SqlUtil.getLocalDate
import de.bixilon.unithen.storage.sql.SqlUtil.getUUID
import de.bixilon.unithen.storage.sql.util.SqlFilter
import java.time.LocalDateTime
import java.util.*

class AppointmentTable(
    storage: SqlStorage,
) : SqlTable<Appointment>(storage, "appointments") {
    override val columns = listOf("id", "course", "uuid", "start", "end")


    override fun map(cursor: Cursor) = Appointment(cursor.getInt(0), cursor.getInt(1), cursor.getUUID(2), cursor.getLocalDate(3), cursor.getLocalDate(4))

    operator fun get(id: Key) = single("id=?", id)
    operator fun get(course: Course, uuid: UUID) = single(SqlFilter.and("course" to course.id, "uuid" to uuid))

    fun get(course: Course?) = all(SqlFilter.and("course" to course?.id))

    fun getInRange(from: LocalDateTime, to: LocalDateTime): List<Appointment> {
        val filter = SqlFilter("(end >= ? AND ? >= start)", listOf(to.db(), from.db()))

        return all(filter)
    }

    fun update(id: Key, start: LocalDateTime? = null, end: LocalDateTime? = null) = update(id, SqlFilter.comma("start" to start?.db(), "end" to end?.db()))


    fun insert(course: Course, uuid: UUID, start: LocalDateTime, end: LocalDateTime) {
        storage.execute("INSERT INTO $table(course, uuid, start, end) VALUES (?,?,?,?)", course.id, uuid, start.db(), end.db())
    }

    fun add(course: Course, uuid: UUID, start: LocalDateTime, end: LocalDateTime) {
        this[course, uuid]?.let { return update(it.id, start, end) }

        insert(course, uuid, start, end)
    }
}
