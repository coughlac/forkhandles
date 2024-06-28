@file:Suppress("NonAsciiCharacters")

package dev.forkhandles.result4k

/**
 * A result of a computation that can succeed or fail.
 */
sealed class Result<out T, out E>

data class Success<out T>(val value: T) : Result<T, Nothing>()
data class Failure<out E>(val reason: E) : Result<Nothing, E>()

/**
 * Call a function and wrap the result in a `Result`, catching any `Exception` and returning it as `Err` value.
 */
inline fun <T> resultFrom(block: () -> T): Result<T, Exception> =
    try {
        Success(block())
    } catch (x: Exception) {
        Failure(x)
    }

/**
 * Call a block and catch a specific Throwable type, returning it as an `Err` value.
 */
inline fun <reified E : Throwable, T> resultFromCatching(block: () -> T): Result<T, E> = resultFrom(block).mapFailure {
    when (it) {
        is E -> it
        else -> throw it
    }
}

/**
 * Map a function over the `value` of a successful `Result`.
 */
inline fun <T, Tʹ, E> Result<T, E>.map(f: (T) -> Tʹ): Result<Tʹ, E> =
    flatMap { value -> Success(f(value)) }

/**
 * Flat-map a function over the `value` of a successful `Result`.
 */
inline fun <T, Tʹ, E> Result<T, E>.flatMap(f: (T) -> Result<Tʹ, E>): Result<Tʹ, E> =
    when (this) {
        is Success<T> -> f(value)
        is Failure<E> -> this
    }

/**
 * Map a function over the `reason` of an unsuccessful `Result`.
 */
inline fun <T, E, Eʹ> Result<T, E>.mapFailure(f: (E) -> Eʹ): Result<T, Eʹ> =
    flatMapFailure { reason -> Failure(f(reason)) }

/**
 * Map a function, f, over the `value` of a successful `Result`
 * and a function, g, over the `reason` of an unsuccessful `Result`.
 */
inline fun <T, Tʹ, E, Eʹ> Result<T, E>.bimap(f: (T) -> Tʹ, g: (E) -> Eʹ): Result<Tʹ, Eʹ> =
    map { f(it) }.mapFailure { g(it) }

/**
 * Fold a function, f, over the `value` of a successful `Result`
 * and a function, g, over the `reason` of an unsuccessful `Result`
 * where both functions result in a value of the same type, returning a plain value.
 */
inline fun <T, E, Tʹ> Result<T, E>.fold(f: (T) -> Tʹ, g: (E) -> Tʹ): Tʹ =
    bimap(f, g).get()

/**
 * Flat-map a function over the `reason` of an unsuccessful `Result`.
 */
inline fun <T, E, Eʹ> Result<T, E>.flatMapFailure(f: (E) -> Result<T, Eʹ>): Result<T, Eʹ> = when (this) {
    is Success<T> -> this
    is Failure<E> -> f(reason)
}

/**
 * Perform a side effect with the success value.
 */
inline fun <T, E> Result<T, E>.peek(f: (T) -> Unit): Result<T, E> =
    apply { if (this is Success<T>) f(value) }

/**
 * Perform a side effect with the failure reason.
 */
inline fun <T, E> Result<T, E>.peekFailure(f: (E) -> Unit): Result<T, E> =
    apply { if (this is Failure<E>) f(reason) }

/**
 * Unwrap a `Result` in which both the success and failure values have the same type, returning a plain value.
 */
fun <T> Result<T, T>.get(): T = when (this) {
    is Success<T> -> value
    is Failure<T> -> reason
}

/**
 * Unwrap a successful result or throw an exception
 */
fun <T, X : Throwable> Result<T, X>.orThrow(): T = when (this) {
    is Success<T> -> value
    is Failure<X> -> throw reason
}

/**
 * Unwrap a `Result`, by returning the success value or calling `block` on failure to abort from the current function.
 */
inline fun <T, E> Result<T, E>.onFailure(block: (Failure<E>) -> Nothing): T = when (this) {
    is Success<T> -> value
    is Failure<E> -> block(this)
}

/**
 * Unwrap a `Result` by returning the success value or calling `failureToValue` to mapping the failure reason to a plain value.
 */
inline fun <S, T : S, U : S, E> Result<T, E>.recover(errorToValue: (E) -> U): S =
    mapFailure(errorToValue).get()
