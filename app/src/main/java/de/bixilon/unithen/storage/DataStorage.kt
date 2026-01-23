package de.bixilon.unithen.storage

interface DataStorage {

    fun getAccounts(): List<Account>
    fun remove(account: Account)
    fun add(account: Account)

    fun getCourses(): List<Course>
    fun getCourses(account: Account): List<Course>
    fun remove(course: Course)
    fun add(account: Account, course: Course)

    fun getAppointment(course: Course): List<Appointment>
    fun remove(appointment: Appointment)
    fun add(course: Course, appointment: Appointment)
}
