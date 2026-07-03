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

package de.bixilon.unithen.ui.main.checkin.scan.qr

import de.bixilon.unithen.R
import de.bixilon.unithen.ui.main.settings.types.Labeled

enum class QrErrorReasons(override val label: Int) : Labeled {
    INVALID_FORMAT(R.string.scan_error_invalid_format),
    INVALID_DATA(R.string.scan_error_invalid_data),

    INVALID_APPOINTMENT(R.string.scan_error_invalid_appointment),
    INVALID_COURSE(R.string.scan_error_invalid_course),
    WRONG_APPOINTMENT(R.string.scan_error_wrong_appointment),
    UNKNOWN_USER(R.string.scan_error_unknown_user),
    NOT_ENROLLED(R.string.scan_error_not_enrolled),
    ALREADY_CHECKED_IN(R.string.scan_error_already_checked_in),
    CHECK_IN_PENDING(R.string.scan_error_check_in_pending),
    CHECK_IN_SERVER_ERROR(R.string.scan_unknown_error_server_generic),
    CHECK_OUT_PENDING(R.string.scan_error_check_out_pending),


    OTHER(R.string.scan_error_other),
}
