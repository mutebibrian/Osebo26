package com.devbrian.osebo.data.remote

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

/**
 * kotlinx.serialization's @SerialName has no equivalent of Gson's
 * SerializedName(alternate = [...]) — several DTOs were bulk-converted from
 * Gson and kept that parameter even though it doesn't exist on the
 * kotlinx.serialization annotation (a compile error in every source set,
 * not just iOS). This fills the gap: call from a
 * JsonTransformingSerializer.transformDeserialize to make several possible
 * backend key names resolve to the one @SerialName the data class expects.
 * For each canonical key, the first alternate with a present value wins.
 */
fun JsonElement.withAlternateKeys(vararg mapping: Pair<String, List<String>>): JsonElement {
    if (this !is JsonObject) return this
    val result = this.toMutableMap()
    for ((canonical, alternates) in mapping) {
        if (result[canonical] == null) {
            for (alt in alternates) {
                val value = result[alt]
                if (value != null) {
                    result[canonical] = value
                    break
                }
            }
        }
    }
    return JsonObject(result)
}
