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

package de.bixilon.unithen.api.ory

import de.bixilon.unithen.ui.auth.ory.OryConfig
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
data class OryLoginFlow(
    val id: Uuid,
    val ui: Ui,
    @SerialName("session_token_exchange_code") val sessionTokenExchangeToken: String,
) {
    @Serializable
    data class Ui(
        val action: String, // URL
        val method: String, // POST
        val nodes: List<Method>,
    ) {
        @Serializable
        data class Method(
            val type: String,
            val attributes: Attributes,
            val meta: Meta? = null,
        ) {
            @Serializable
            data class Attributes(
                val name: String,
                val type: String, // submit, hidden
                val value: String? = null,
                val disabled: Boolean = false,
            )

            @Serializable
            data class Meta(
                val label: Label? = null,
            ) {

                @Serializable
                data class Label(
                    val text: String,
                    val context: Context?,
                ) {

                    @Serializable
                    data class Context(
                        @SerialName("provider_id") val providerId: String?,
                    )
                }
            }
        }
    }

    fun toConfig(): OryConfig {
        val oidc = ui.nodes.filter { it.attributes.type == "submit" && !it.attributes.disabled && it.meta?.label?.context?.providerId != null }.map { OryConfig.OryOidc(it.meta!!.label!!.context!!.providerId!!, it.attributes.value!!, it.meta.label.text) }

        return OryConfig(id, ui.action, oidc)
    }
}
