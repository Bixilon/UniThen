package de.bixilon.unithen.api.authentication

import android.net.http.HttpEngine

interface Authentication {

    fun authenticate(request: HttpEngine.Builder)
}
