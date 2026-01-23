package de.bixilon.unithen.api.authentication

import okhttp3.Request


interface Authentication {

    fun authenticate(request: Request.Builder)
}
