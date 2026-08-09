package de.bixilon.unithen.api.errors

class NetworkException(message: String? = null, cause: Throwable? = null) : Exception(message ?: cause?.message ?: cause?.let { it::class.simpleName }, cause)
