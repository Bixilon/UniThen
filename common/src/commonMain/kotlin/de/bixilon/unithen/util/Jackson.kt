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

package de.bixilon.unithen.util

import de.bixilon.unithen.api.graphql.types.location.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual
import kotlinx.serialization.modules.polymorphic
import kotlin.time.Instant

object Jackson {
    val MAPPER = Json {
        ignoreUnknownKeys = true
        decodeEnumsCaseInsensitive = true
        explicitNulls = false
    }

    val GRAPHQL = Json {
        ignoreUnknownKeys = true
        decodeEnumsCaseInsensitive = true
        explicitNulls = false

        serializersModule = SerializersModule {
            polymorphic(FacilityQl::class) {
                subclass(LocationQl::class, LocationQl.serializer())
                subclass(AreaQl::class, AreaQl.serializer())
                subclass(RoomQl::class, RoomQl.serializer())
                defaultDeserializer { UnknownFacilityQl.serializer() }
            }

            contextual(InstantSerializer)
        }

        classDiscriminator = "__typename"
    }

    object InstantSerializer : KSerializer<Instant> {
        override val descriptor = PrimitiveSerialDescriptor("Instant", PrimitiveKind.LONG)

        override fun serialize(encoder: Encoder, value: Instant) {
            encoder.encodeLong(value.epochSeconds)
        }

        override fun deserialize(decoder: Decoder): Instant {
            return Instant.fromEpochSeconds(decoder.decodeLong(), 0L)
        }
    }
}
