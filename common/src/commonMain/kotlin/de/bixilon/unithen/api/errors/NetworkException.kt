package de.bixilon.unithen.api.errors

// TODO: kutil 1.31.1: fast exception
class NetworkException(message: String? = null, cause: Throwable? = null) : Exception(message ?: cause?.message ?: cause?.let { it::class.simpleName }, cause)
