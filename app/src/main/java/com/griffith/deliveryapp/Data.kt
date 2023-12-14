package com.griffith.deliveryapp

// this is the data of restaurants used by the app
val data = listOf(
    hashMapOf(
        "id" to "1",
        "name" to "Bleeding Horse",
        "rating" to 4.5,
        "reviewsNumber" to "500",
        "img" to R.drawable.bleeding_horse,
        "latitude" to 37.409099,
        "longitude" to -122.117197,
        "menu" to listOf(
            hashMapOf(
                "foodName" to "Burger",
                "foodImg" to R.drawable.burger,
                "foodDescription" to "Get yourself down to the steakhouse",
                "foodPrice" to 16.99
            ),
            hashMapOf(
                "foodName" to "Beef and Guinness",
                "foodImg" to R.drawable.beef_and_guinness,
                "foodDescription" to "Experience the real irish kitchen",
                "foodPrice" to 18.99
            ),
            hashMapOf(
                "foodName" to "Fish and Chips",
                "foodImg" to R.drawable.fish_and_chips,
                "foodDescription" to "Fish & Chips: Crispy, golden perfection",
                "foodPrice" to 17.99
            )
        )
    ),
    hashMapOf(
        "id" to "2",
        "name" to "McDonalds",
        "rating" to 4.8,
        "reviewsNumber" to "1000",
        "img" to R.drawable.mcdonalds,
        "latitude" to 37.379293,
        "longitude" to -122.042236,
        "menu" to listOf(
            hashMapOf(
                "foodName" to "Big Mac",
                "foodImg" to R.drawable.bigmac,
                "foodDescription" to "Irish beef, cheese, lettuce, onion",
                "foodPrice" to 4.99
            ),
            hashMapOf(
                "foodName" to "Big Tasty",
                "foodImg" to R.drawable.bigtasty,
                "foodDescription" to "100% irish beef, tomatoes, sauce",
                "foodPrice" to 5.99
            ),
            hashMapOf(
                "foodName" to "Cheeseburger",
                "foodImg" to R.drawable.cheeseburger,
                "foodDescription" to "Cheddar cheese, irish beef, and pickles",
                "foodPrice" to 3.99
            )
        )
    ),
    hashMapOf(
        "id" to "3",
        "name" to "Domino's Pizza",
        "rating" to 3.8,
        "reviewsNumber" to "800",
        "img" to R.drawable.dominos_pizza,
        "latitude" to 37.787465,
        "longitude" to -122.412450,
        "menu" to listOf(
            hashMapOf(
                "foodName" to "Pepperoni",
                "foodImg" to R.drawable.pepperoni,
                "foodDescription" to "Pizza with cheese and pepperoni",
                "foodPrice" to 20.99
            ),
            hashMapOf(
                "foodName" to "Texas BBQ",
                "foodImg" to R.drawable.texas_bbq,
                "foodDescription" to "BBQ sauce, onions, bacon, chicken",
                "foodPrice" to 21.99
            ),
            hashMapOf(
                "foodName" to "Vegi Supreme",
                "foodImg" to R.drawable.vegi_supreme,
                "foodDescription" to "Vegetarian pizza",
                "foodPrice" to 20.99
            )
        )
    )
)