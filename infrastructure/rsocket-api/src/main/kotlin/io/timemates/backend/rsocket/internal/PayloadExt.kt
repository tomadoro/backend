package io.timemates.backend.rsocket.internal

import io.rsocket.kotlin.ExperimentalMetadataApi
import io.rsocket.kotlin.metadata.RoutingMetadata
import io.rsocket.kotlin.metadata.read
import io.rsocket.kotlin.payload.Payload
import io.rsocket.kotlin.payload.buildPayload
import io.rsocket.kotlin.payload.data
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Extracts the routing information from the payload's metadata.
 *
 * @return The route extracted from the metadata, or an error if no route is provided.
 */
@OptIn(ExperimentalMetadataApi::class)
internal fun Payload.route(): String = metadata?.read(RoutingMetadata)?.tags?.first()
    ?: error("No route provided")

internal val json = Json {
    encodeDefaults = true
    ignoreUnknownKeys = true
}

/**
 * Decodes the payload's JSON data into the specified type [T].
 *
 * @return The decoded data of type [T].
 */
internal inline fun <reified T> Payload.decodeFromJson(): T = json.decodeFromString<T>(data.readText())

/**
 * Decodes the payload's JSON data into [T] and executes the provided [block].
 *
 * @param block The action to be performed with the decoded data.
 * @return An empty payload after the block execution.
 */
internal inline fun <reified T> Payload.decoding(block: (T) -> Payload): Payload {
    return block(decodeFromJson())
}


/**
 * Builds a payload containing JSON-encoded data of type [T] to be used in RSocket responses.
 *
 * @param data The data to be JSON-encoded and included in the payload.
 * @return A payload containing the JSON-encoded [data].
 */
internal inline fun <reified T> respondWith(data: T): Payload = buildPayload {
    data(Json.encodeToString(data))
}

/**
 * Converts an object of type [T] to a payload containing JSON-encoded data.
 *
 * @return A payload containing the JSON-encoded data of type [T].
 */
internal inline fun <reified T> T.asPayload(): Payload = respondWith(this)

/**
 * Converts a [Unit] to an empty payload. Uses to reduce serialization
 * calls in situations when it's unnecessary
 *
 * @return An empty payload.
 */
@Suppress("NOTHING_TO_INLINE", "UnusedReceiverParameter")
internal inline fun Unit.asPayload(): Payload = Payload.Empty
