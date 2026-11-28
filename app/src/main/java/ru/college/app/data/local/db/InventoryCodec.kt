package ru.college.app.data.local.db

import org.json.JSONArray
import org.json.JSONObject

/**
 * Inventory is stored in a single TEXT column (up to 8 slots). A tiny codec
 * keeps JSON details out of ViewModels and avoids adding a serialization
 * dependency just for two fields.
 */
data class InventoryStack(
    val id: String,
    val count: Int
)

object InventoryCodec {

    fun decode(json: String): List<InventoryStack> = runCatching {
        val array = JSONArray(json.ifBlank { "[]" })
        buildList {
            for (index in 0 until array.length()) {
                val item = array.optJSONObject(index) ?: continue
                val id = item.optString("id")
                val count = item.optInt("count")
                if (id.isNotBlank() && count > 0) {
                    add(InventoryStack(id = id, count = count))
                }
            }
        }
    }.getOrDefault(emptyList())

    fun encode(stacks: List<InventoryStack>): String {
        val array = JSONArray()
        stacks.filter { it.count > 0 }.forEach { stack ->
            array.put(
                JSONObject()
                    .put("id", stack.id)
                    .put("count", stack.count)
            )
        }
        return array.toString()
    }
}
