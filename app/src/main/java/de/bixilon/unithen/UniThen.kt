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
import de.bixilon.unithen.storage.STORAGE
import de.bixilon.unithen.storage.sql.SqlStorage
import de.bixilon.unithen.ui.main.settings.SETTINGS
import de.bixilon.unithen.ui.main.settings.SettingsStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UniThen : Application() {

    override fun onCreate() {
        super.onCreate()
        SETTINGS = SettingsStore(this)
        STORAGE = SqlStorage(applicationContext)

        if (STORAGE.sites.count == 0) {
            CoroutineScope(Dispatchers.IO).launch { STORAGE.sites.add("kurse.zhs-muenchen.de") }
        }
    }

    override fun onTerminate() {
        super.onTerminate()
        STORAGE.close()
    }

    companion object {

        fun updateCourses() {
            STORAGE.accounts.all().forEach {
                val site = STORAGE.sites[it.site]!!
                val api = AuthenticatedUniNowApi(site.url, CookieAuthentication(it.session))
                val courses = api.postings(it.uuid)

                STORAGE.populate(site, it, courses)
            }
        }
    }
}
