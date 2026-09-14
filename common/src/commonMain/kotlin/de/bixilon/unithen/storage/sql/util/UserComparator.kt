package de.bixilon.unithen.storage.sql.util

import de.bixilon.unithen.storage.types.User
import de.bixilon.unithen.ui.main.checkin.scan.attendees.AttendeeSort
import de.bixilon.unithen.ui.main.checkin.scan.attendees.Order

expect fun compare(a: String, b: String): Int

class UserComparator(val sort: AttendeeSort, val order: Order) : Comparator<User> {


    override fun compare(a: User, b: User): Int {
        val sign = if (order == Order.ASC) 1 else -1

        sort.let { compare(a.value(it), b.value(it)) }.takeIf { it != 0 }?.let { return it * sign }
        AttendeeSort.next(sort).let { compare(a.value(it), b.value(it)) }.takeIf { it != 0 }?.let { return it * sign }

        return 0
    }

    private fun User.value(sort: AttendeeSort) = when (sort) {
        AttendeeSort.FIRSTNAME -> firstname
        AttendeeSort.LASTNAME -> lastname
    }

    companion object {
        fun List<User>.sort(sort: AttendeeSort, order: Order) = sortedWith(UserComparator(sort, order))
    }
}
