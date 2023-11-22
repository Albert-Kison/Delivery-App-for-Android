package com.griffith.deliveryapp

class Basket private constructor() {

    // items in the basket
    private val items: MutableList<HashMap<String, Any>> = mutableListOf()

    private var currentRestaurantId: String? = null

    fun addItem(item: HashMap<String, Any>, restaurantId: String): Boolean {
        if (currentRestaurantId == null || currentRestaurantId == restaurantId) {
            items.add(item)
            currentRestaurantId = restaurantId
            return true
        } else {
            // If the item is from a different restaurant, return false
            return false
        }
    }


    fun removeItem(item: HashMap<String, Any>) {
        items.remove(item)
        if (items.size == 0) {
            currentRestaurantId = null
        }
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

    fun clearBasket() {
        currentRestaurantId = null
        items.clear() // This removes all items from the list
    }

    fun setCurrentRestaurantId(restaurantId: String) {
        currentRestaurantId = restaurantId
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
