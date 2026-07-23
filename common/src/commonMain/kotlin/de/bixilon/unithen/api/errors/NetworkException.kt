package de.bixilon.unithen.api.errors

class NetworkException(cause: Throwable? = null) : Exception(cause?.message ?: cause?.let { it::class.simpleName }, cause)
