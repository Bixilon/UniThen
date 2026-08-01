package de.bixilon.unithen.ui.auth.ory

import de.bixilon.kutil.exception.FastException

class InvalidCredentialException(override val message: String) : FastException(message)
