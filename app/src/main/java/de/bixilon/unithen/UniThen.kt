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
import android.util.Log
import androidx.camera.camera2.Camera2Config
import androidx.camera.core.CameraSelector
import androidx.camera.core.CameraXConfig
import de.bixilon.unithen.storage.sql.AndroidSqlHelper
import de.bixilon.unithen.storage.sql.SqlStorage
import de.bixilon.unithen.ui.main.settings.SETTINGS
import de.bixilon.unithen.ui.main.settings.SettingsStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UniThen : Application(), CameraXConfig.Provider {
    private val config by lazy {
        CameraXConfig.Builder.fromConfig(Camera2Config.defaultConfig())
            .setMinimumLoggingLevel(Log.ERROR)
            .setAvailableCamerasLimiter(CameraSelector.DEFAULT_BACK_CAMERA)
            .build()
    }

    override fun getCameraXConfig() = config

    override fun onCreate() {
        super.onCreate()
        SETTINGS = SettingsStore(this)
        CoroutineScope(Dispatchers.IO).launch { SETTINGS.preload() }
        STORAGE = SqlStorage(AndroidSqlHelper(applicationContext))
    }

    override fun onTerminate() {
        super.onTerminate()
        STORAGE.close()
    }

    companion object {
        lateinit var STORAGE: SqlStorage
    }
}
