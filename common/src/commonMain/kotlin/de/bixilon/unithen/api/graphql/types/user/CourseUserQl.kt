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

package de.bixilon.unithen.api.graphql.types.user

import de.bixilon.unithen.api.graphql.types.IdentifiedQl
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.*
import kotlin.uuid.Uuid

// actually hacky, they represent enrolled users too (both share fields)
@Serializable
data class CourseUserQl(
    override val id: Uuid,
    @SerialName("first_name") val firstname: String? = null,
    @SerialName("last_name") val lastname: String? = null,
) : IdentifiedQl {

    object CourseUserQlSerializer : KSerializer<CourseUserQl?> {
        private val defaultSerializer = serializer()
        override val descriptor get() = defaultSerializer.descriptor

        override fun serialize(encoder: Encoder, value: CourseUserQl?) {
            if (value == null) {
                encoder.encodeNull()
                return
            }
            return defaultSerializer.serialize(encoder, value)
        }

        override fun deserialize(decoder: Decoder): CourseUserQl? {
            if (decoder !is JsonDecoder) throw SerializationException("Only JSON supported")

            val element = decoder.decodeJsonElement()
            if (element is JsonNull) return null
            if (element !is JsonObject) throw SerializationException("Invalid course user: $element")

            // This is very ugly, but somehow it is possible to pass invalid users (all fields null, except id, that is blank)
            val id = element["id"]?.jsonPrimitive?.contentOrNull
            if (id.isNullOrEmpty()) return null

            return decoder.json.decodeFromJsonElement(defaultSerializer, element)
        }
    }

    object NonNullListSerializer : KSerializer<List<CourseUserQl>> {
        private val serializer = ListSerializer(CourseUserQlSerializer)
        override val descriptor: SerialDescriptor = serializer.descriptor

        override fun serialize(encoder: Encoder, value: List<CourseUserQl>) {
            serializer.serialize(encoder, value)
        }

        override fun deserialize(decoder: Decoder): List<CourseUserQl> {
            return serializer.deserialize(decoder).filterNotNull()
        }
    }
}
