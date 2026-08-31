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

package de.bixilon.unithen.ui.main.checkin.scan.qr.types

import de.bixilon.kutil.cast.CastUtil.cast
import de.bixilon.unithen.util.Jackson
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlin.uuid.Uuid

@Serializable
data class ScannedQrCodeV1(
    @SerialName("appointment_id") val appointmentId: Uuid,
    @SerialName("user_id") val userId: Uuid,
) : ScannedQrCode {

    override fun encode() = Jackson.MAPPER.encodeToJsonElement(serializer(), this).cast<JsonObject>().toMutableMap().apply { this["userName"] = EMPTY_NAME }.let { JsonObject(it) }.toString()

    companion object {
        val EMPTY_NAME = JsonObject(mapOf(
            "last" to JsonPrimitive("1"),
            "first" to JsonPrimitive("2"),
        ))


        fun decode(data: String): ScannedQrCodeV1? {
            val text = data.trim()
            if (!text.startsWith("{")) return null

            return Jackson.MAPPER.decodeFromString<ScannedQrCodeV1>(text)
        }
    }
}
