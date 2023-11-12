package com.griffith.deliveryapp

class Basket private constructor() {

    // items in the basket
    private val items: MutableList<HashMap<String, Any>> = mutableListOf()

    fun addItem(item: HashMap<String, Any>) {
        items.add(item)
    }

    fun removeItem(item: HashMap<String, Any>) {
        items.remove(item)
    }

    fun getItems(): List<HashMap<String, Any>> {
        return items.toList()
    }

    fun getTotal(): Double {
        var total = 0.0
        for (item in getItems()) {
            total += item["foodPrice"] as Double
        }
        return total
    }

    fun clearItems() {
        items.clear() // This removes all items from the list
    }

    companion object {
        @Volatile
        private var instance: Basket? = null

        fun getInstance(): Basket {
            return instance ?: synchronized(this) {
                instance ?: Basket().also { instance = it }
            }
        }
    }
}
