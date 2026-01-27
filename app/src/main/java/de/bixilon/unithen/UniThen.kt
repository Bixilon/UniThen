/*
 * UniThen
 * Copyright (C) 2026 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with UniNow GmbH, the provider/developer of the booking system.
 */

package de.bixilon.unithen

import android.app.Application
import de.bixilon.unithen.api.AuthenticatedUniNowApi
import de.bixilon.unithen.api.authentication.CookieAuthentication
import de.bixilon.unithen.storage.DataStorage
import de.bixilon.unithen.storage.sql.SqlStorage

class UniThen : Application() {

    override fun onCreate() {
        super.onCreate()
        DataStorage.STORAGE = SqlStorage(applicationContext)


        //  updateCourses()
    }

    override fun onTerminate() {
        super.onTerminate()
        DataStorage.STORAGE.close()
    }

    companion object {

        fun updateCourses() {
            DataStorage.STORAGE.accounts.all().forEach {
                val site = DataStorage.STORAGE.sites[it.site]!!
                val api = AuthenticatedUniNowApi(site.url, CookieAuthentication(it.session))
                val courses = api.postings(it.uuid)

                DataStorage.STORAGE.populate(site, it, courses)
            }
        }
    }
}
