package com.example.model

import androidx.compose.ui.graphics.Color

enum class PetSpecies(
    val id: String,
    val displayName: String,
    val habitatName: String,
    val description: String,
    val defaultName: String,
    val primaryColorHex: Long,
    val secondaryColorHex: Long,
    val emoji: String
) {
    CAPYBARA(
        id = "capybara",
        displayName = "Capybara",
        habitatName = "Sunny Pond & Grass Patch",
        description = "Chill and friendly. Loves lounging in quiet ponds with oranges on its head.",
        defaultName = "Boba",
        primaryColorHex = 0xFF8D5B4C,
        secondaryColorHex = 0xFFA8E6CF,
        emoji = "🦫"
    ),
    FISH(
        id = "fish",
        displayName = "Betta Fish",
        habitatName = "Glow Aquarium",
        description = "Serene swimmer doing peaceful loops among glowing aquatic plants.",
        defaultName = "Bubbles",
        primaryColorHex = 0xFF00B4D8,
        secondaryColorHex = 0xFF0077B6,
        emoji = "🐠"
    ),
    BIRD(
        id = "bird",
        displayName = "Parakeet",
        habitatName = "Botanical Cage Perch",
        description = "Cheerful chirper hopping between wooden swings and perches.",
        defaultName = "Mango",
        primaryColorHex = 0xFFFFD166,
        secondaryColorHex = 0xFF06D6A0,
        emoji = "🦜"
    ),
    CAT(
        id = "cat",
        displayName = "Calico Cat",
        habitatName = "Window Cushion",
        description = "Cozy napper resting on a plush cushion with sunbeams.",
        defaultName = "Matcha",
        primaryColorHex = 0xFFF4A261,
        secondaryColorHex = 0xFFE76F51,
        emoji = "🐱"
    ),
    DOG(
        id = "dog",
        displayName = "Corgi",
        habitatName = "Backyard Green",
        description = "Playful pup wagging its tail and doing happy little bounces.",
        defaultName = "Waffles",
        primaryColorHex = 0xFFE9C46A,
        secondaryColorHex = 0xFF2A9D8F,
        emoji = "🐶"
    ),
    BEAVER(
        id = "beaver",
        displayName = "Beaver",
        habitatName = "Mini Forest Stream",
        description = "Hardworking buddy nibbling tiny wooden sticks near a flowing stream.",
        defaultName = "Timber",
        primaryColorHex = 0xFF7F5539,
        secondaryColorHex = 0xFFB08968,
        emoji = "🦫"
    );

    val primaryColor: Color get() = Color(primaryColorHex)
    val secondaryColor: Color get() = Color(secondaryColorHex)

    companion object {
        fun fromId(id: String): PetSpecies = entries.find { it.id.equals(id, ignoreCase = true) } ?: CAPYBARA
    }
}

enum class PetState {
    IDLE,
    WALKING,
    HAPPY,
    EATING,
    REACTING,
    FACT_CHECKING,
    RESTING,
    TIRED,
    SLEEPING
}

enum class OverlaySize(val label: String, val dpSize: Int) {
    SMALL("Small", 110),
    MEDIUM("Medium", 140),
    LARGE("Large", 180);

    companion object {
        fun fromName(name: String): OverlaySize = entries.find { it.name.equals(name, ignoreCase = true) } ?: MEDIUM
    }
}
