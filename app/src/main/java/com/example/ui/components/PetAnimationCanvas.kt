package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import com.example.model.PetSpecies
import com.example.model.PetState
import kotlin.math.abs
import kotlin.math.sin

@Composable
fun PetAnimationCanvas(
    species: PetSpecies,
    petState: PetState,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "PetAnimationEngine")

    val animProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "animProgress"
    )

    val bounceProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bounceProgress"
    )

    val fastProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "fastProgress"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val centerX = width / 2f
        val centerY = height / 2f

        // Draw Habitat Scene Background
        drawHabitatBackground(species, petState, width, height, animProgress)

        // Determine Pet Position, Jump, Spin & Motion Offsets
        val isTiredOrSleeping = petState == PetState.TIRED || petState == PetState.SLEEPING || petState == PetState.RESTING
        val isReacting = petState == PetState.REACTING
        val isEating = petState == PetState.EATING
        val isWalking = petState == PetState.WALKING

        var walkX = 0f
        if (isWalking) {
            walkX = sin(animProgress * Math.PI * 2).toFloat() * (width * 0.22f)
        } else if (isEating) {
            walkX = -12f
        }

        var jumpY = 0f
        var spinAngle = 0f

        if (isReacting) {
            jumpY = -abs(sin(animProgress * Math.PI * 2).toFloat()) * 32f
            spinAngle = animProgress * 360f
        } else if (isTiredOrSleeping) {
            jumpY = sin(animProgress * Math.PI * 2).toFloat() * 2f
        } else if (isEating) {
            jumpY = sin(fastProgress * Math.PI * 2).toFloat() * 3f
        } else {
            jumpY = sin(bounceProgress * Math.PI).toFloat() * 8f
        }

        val petX = centerX + walkX
        val petY = centerY + 8f + jumpY

        // Draw Food Bowl if Eating
        if (isEating) {
            drawFoodBowl(species, centerX + 26f, centerY + 28f, animProgress)
        }

        // Draw Pet Character with Rotation (if Reacting trick)
        rotate(degrees = spinAngle, pivot = Offset(petX, petY)) {
            drawPetBody(species, petState, petX, petY, animProgress, fastProgress)
        }

        // Draw Overlay Effects (Zzz, Hearts, Trick Sparkles, Fact Checking)
        when {
            isReacting -> drawTrickSparkles(petX, petY - 50f, animProgress)
            isEating -> drawEatingHearts(petX - 10f, petY - 45f, animProgress)
            isTiredOrSleeping -> drawSleepingZzz(petX + 32f, petY - 32f, animProgress)
            petState == PetState.FACT_CHECKING -> drawFactCheckingSparkles(petX, petY - 45f, animProgress)
        }
    }
}

private fun DrawScope.drawHabitatBackground(
    species: PetSpecies,
    state: PetState,
    width: Float,
    height: Float,
    animProgress: Float
) {
    val isTired = state == PetState.TIRED || state == PetState.SLEEPING || state == PetState.RESTING

    when (species) {
        PetSpecies.CAPYBARA -> {
            // Capybara Onsen / River Meadow
            val skyColor = if (isTired) Color(0xFF1E293B) else Color(0xFFE0F2FE)
            val grassColor = if (isTired) Color(0xFF2D4A3E) else Color(0xFF86EFAC)
            val pondColor = if (isTired) Color(0xFF0F172A) else Color(0xFF38BDF8)

            drawRect(color = skyColor, size = Size(width, height))
            drawOval(
                color = pondColor,
                topLeft = Offset(width * 0.08f, height * 0.45f),
                size = Size(width * 0.84f, height * 0.52f)
            )
            drawRoundRect(
                color = grassColor,
                topLeft = Offset(0f, height * 0.72f),
                size = Size(width, height * 0.28f),
                cornerRadius = CornerRadius(16f)
            )
            val rippleRadius = 12f + (animProgress * 28f)
            drawCircle(
                color = Color.White.copy(alpha = 0.4f * (1f - animProgress)),
                radius = rippleRadius,
                center = Offset(width * 0.5f, height * 0.65f),
                style = Stroke(width = 3f)
            )
        }
        PetSpecies.FISH -> {
            // Goldfish Tank
            val tankColor = if (isTired) Color(0xFF021329) else Color(0xFF0284C7)
            val deepColor = if (isTired) Color(0xFF010A17) else Color(0xFF0369A1)
            val sandColor = if (isTired) Color(0xFF33291A) else Color(0xFFFDE68A)

            drawRoundRect(color = tankColor, size = Size(width, height), cornerRadius = CornerRadius(24f))
            drawRect(color = deepColor, topLeft = Offset(0f, height * 0.65f), size = Size(width, height * 0.35f))
            drawRect(color = sandColor, topLeft = Offset(0f, height * 0.82f), size = Size(width, height * 0.18f))

            // Seaweed Plants
            val plantPath = Path().apply {
                moveTo(width * 0.15f, height * 0.85f)
                quadraticTo(width * 0.12f, height * 0.65f, width * 0.18f, height * 0.45f)
                quadraticTo(width * 0.22f, height * 0.65f, width * 0.15f, height * 0.85f)
            }
            drawPath(plantPath, color = Color(0xFF22C55E))

            val bubbleY1 = height * (1f - ((animProgress + 0.1f) % 1f))
            val bubbleY2 = height * (1f - ((animProgress + 0.6f) % 1f))
            drawCircle(Color.White.copy(alpha = 0.6f), radius = 6f, center = Offset(width * 0.25f, bubbleY1))
            drawCircle(Color.White.copy(alpha = 0.7f), radius = 9f, center = Offset(width * 0.78f, bubbleY2))
        }
        PetSpecies.BIRD -> {
            // Bird Sanctuary Cage / Tree Perch
            val bg = if (isTired) Color(0xFF1E1B4B) else Color(0xFFFEF3C7)
            drawRoundRect(color = bg, size = Size(width, height), cornerRadius = CornerRadius(24f))

            // Wooden Perch Bar
            drawRoundRect(
                color = Color(0xFFB45309),
                topLeft = Offset(width * 0.15f, height * 0.68f),
                size = Size(width * 0.7f, 16f),
                cornerRadius = CornerRadius(8f)
            )
            // Golden Arch Frame
            drawPath(
                path = Path().apply {
                    moveTo(width * 0.18f, height * 0.68f)
                    cubicTo(width * 0.18f, height * 0.12f, width * 0.82f, height * 0.12f, width * 0.82f, height * 0.68f)
                },
                color = Color(0xFFF59E0B),
                style = Stroke(width = 5f)
            )
        }
        PetSpecies.CAT -> {
            // Cozy Living Room Carpet
            val bg = if (isTired) Color(0xFF111827) else Color(0xFFFEF2F2)
            drawRoundRect(color = bg, size = Size(width, height), cornerRadius = CornerRadius(24f))

            // Soft Pink Circular Cushion Rug
            drawOval(
                color = if (isTired) Color(0xFF371B28) else Color(0xFFFECDD3),
                topLeft = Offset(width * 0.12f, height * 0.52f),
                size = Size(width * 0.76f, height * 0.42f)
            )
            // Yarn Ball
            drawCircle(color = Color(0xFFEC4899), radius = 12f, center = Offset(width * 0.8f, height * 0.75f))
        }
        PetSpecies.DOG -> {
            // Sunny Dog Park
            val bg = if (isTired) Color(0xFF0F172A) else Color(0xFFFEF9C3)
            drawRoundRect(color = bg, size = Size(width, height), cornerRadius = CornerRadius(24f))

            // Green Lawn
            drawOval(
                color = if (isTired) Color(0xFF1E3A8A) else Color(0xFFBBF7D0),
                topLeft = Offset(width * 0.08f, height * 0.48f),
                size = Size(width * 0.84f, height * 0.48f)
            )
        }
        PetSpecies.BEAVER -> {
            // Wooden River Dam
            val bg = if (isTired) Color(0xFF0284C7) else Color(0xFFE0F2FE)
            drawRoundRect(color = bg, size = Size(width, height), cornerRadius = CornerRadius(24f))

            // River Water
            drawRect(
                color = Color(0xFF0284C7),
                topLeft = Offset(0f, height * 0.58f),
                size = Size(width, height * 0.42f)
            )
            // Wooden Dam Log Structure
            drawRoundRect(
                color = Color(0xFF78350F),
                topLeft = Offset(width * 0.15f, height * 0.54f),
                size = Size(width * 0.7f, 26f),
                cornerRadius = CornerRadius(13f)
            )
        }
    }
}

private fun DrawScope.drawFoodBowl(
    species: PetSpecies,
    x: Float,
    y: Float,
    animProgress: Float
) {
    // Bowl Container
    drawOval(color = Color(0xFFDC2626), topLeft = Offset(x - 18f, y - 6f), size = Size(36f, 18f))
    drawOval(color = Color(0xFFEF4444), topLeft = Offset(x - 15f, y - 8f), size = Size(30f, 14f))

    // Species Food Content inside bowl
    val foodColor = when (species) {
        PetSpecies.CAPYBARA -> Color(0xFFF59E0B) // Yuzu Slice
        PetSpecies.FISH -> Color(0xFFFBBF24) // Flakes
        PetSpecies.BIRD -> Color(0xFFFCD34D) // Seeds
        PetSpecies.CAT -> Color(0xFFFB7185) // Salmon
        PetSpecies.DOG -> Color(0xFF9A3412) // Kibble
        PetSpecies.BEAVER -> Color(0xFFB45309) // Wood Snack
    }
    drawCircle(color = foodColor, radius = 6f, center = Offset(x, y - 4f))
}

private fun DrawScope.drawPetBody(
    species: PetSpecies,
    state: PetState,
    x: Float,
    y: Float,
    animProgress: Float,
    fastProgress: Float
) {
    val isTired = state == PetState.TIRED || state == PetState.SLEEPING || state == PetState.RESTING

    when (species) {
        PetSpecies.CAPYBARA -> {
            // Main Capybara Body - Golden Warm Brown
            val coatColor = Color(0xFF8D5B4C)
            val darkSnout = Color(0xFF5C3A21)
            val cheekBlush = Color(0xFFFF85A1)

            // Body
            drawRoundRect(
                color = coatColor,
                topLeft = Offset(x - 40f, y - 26f),
                size = Size(80f, 52f),
                cornerRadius = CornerRadius(24f)
            )
            // Head
            drawRoundRect(
                color = coatColor,
                topLeft = Offset(x - 22f, y - 42f),
                size = Size(44f, 34f),
                cornerRadius = CornerRadius(14f)
            )
            // Ears
            drawOval(color = darkSnout, topLeft = Offset(x - 26f, y - 44f), size = Size(10f, 14f))
            drawOval(color = darkSnout, topLeft = Offset(x + 16f, y - 44f), size = Size(10f, 14f))

            // Snout Box
            drawRoundRect(
                color = darkSnout,
                topLeft = Offset(x - 14f, y - 24f),
                size = Size(28f, 14f),
                cornerRadius = CornerRadius(7f)
            )
            // Nostrils
            drawCircle(Color.Black, radius = 2f, center = Offset(x - 4f, y - 18f))
            drawCircle(Color.Black, radius = 2f, center = Offset(x + 4f, y - 18f))

            // Cheek Blush
            drawCircle(cheekBlush.copy(alpha = 0.6f), radius = 5f, center = Offset(x - 16f, y - 28f))
            drawCircle(cheekBlush.copy(alpha = 0.6f), radius = 5f, center = Offset(x + 16f, y - 28f))

            // Yuzu Orange on Capybara Head
            drawCircle(color = Color(0xFFFFA500), radius = 10f, center = Offset(x, y - 48f))
            drawCircle(color = Color(0xFF16A34A), radius = 3f, center = Offset(x + 3f, y - 57f))

            // Eyes
            if (isTired) {
                drawLine(Color.Black, Offset(x - 12f, y - 32f), Offset(x - 4f, y - 32f), strokeWidth = 3.5f)
                drawLine(Color.Black, Offset(x + 4f, y - 32f), Offset(x + 12f, y - 32f), strokeWidth = 3.5f)
            } else {
                drawCircle(Color.Black, radius = 4f, center = Offset(x - 9f, y - 32f))
                drawCircle(Color.White, radius = 1.5f, center = Offset(x - 10f, y - 33f))

                drawCircle(Color.Black, radius = 4f, center = Offset(x + 9f, y - 32f))
                drawCircle(Color.White, radius = 1.5f, center = Offset(x + 8f, y - 33f))
            }
        }
        PetSpecies.FISH -> {
            // Goldfish - Vibrant Orange Body with shimmering fins
            val bodyColor = Color(0xFFFF70A6)
            val finColor = Color(0xFFFF9F1C)

            val wiggle = if (isTired) 0f else sin(animProgress * Math.PI * 4).toFloat() * 8f

            // Tail Fin
            val tailPath = Path().apply {
                moveTo(x + 22f, y)
                lineTo(x + 48f, y - 24f + wiggle)
                lineTo(x + 38f, y + wiggle)
                lineTo(x + 48f, y + 24f + wiggle)
                close()
            }
            drawPath(tailPath, color = finColor)

            // Main Body
            drawOval(color = bodyColor, topLeft = Offset(x - 30f, y - 20f), size = Size(60f, 40f))

            // Top Fin
            drawOval(color = finColor, topLeft = Offset(x - 10f, y - 30f), size = Size(20f, 12f))

            // Scale Shimmers
            drawArc(
                color = Color.White.copy(alpha = 0.5f),
                startAngle = 90f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = Offset(x - 8f, y - 8f),
                size = Size(14f, 16f),
                style = Stroke(width = 2.5f)
            )

            // Eye
            if (isTired) {
                drawLine(Color.White, Offset(x - 18f, y - 4f), Offset(x - 8f, y - 4f), strokeWidth = 3.5f)
            } else {
                drawCircle(Color.White, radius = 6f, center = Offset(x - 14f, y - 4f))
                drawCircle(Color.Black, radius = 3f, center = Offset(x - 15f, y - 4f))
                drawCircle(Color.White, radius = 1.2f, center = Offset(x - 16f, y - 5f))
            }
        }
        PetSpecies.BIRD -> {
            // Budgie Bird - Blue/Cyan & Bright Yellow
            val bodyColor = Color(0xFF0284C7)
            val headColor = Color(0xFFFDE047)
            val wingColor = Color(0xFF0369A1)

            // Body
            drawOval(color = bodyColor, topLeft = Offset(x - 22f, y - 22f), size = Size(44f, 50f))
            // Head
            drawCircle(color = headColor, radius = 20f, center = Offset(x, y - 24f))

            // Wing
            drawOval(color = wingColor, topLeft = Offset(x - 18f, y - 10f), size = Size(20f, 32f))

            // Yellow Beak
            val beakPath = Path().apply {
                moveTo(x + 12f, y - 26f)
                lineTo(x + 24f, y - 20f)
                lineTo(x + 12f, y - 14f)
                close()
            }
            drawPath(beakPath, color = Color(0xFFF59E0B))

            // Eye
            if (isTired) {
                drawLine(Color.Black, Offset(x + 2f, y - 28f), Offset(x + 10f, y - 28f), strokeWidth = 3.5f)
            } else {
                drawCircle(Color.Black, radius = 4f, center = Offset(x + 5f, y - 28f))
                drawCircle(Color.White, radius = 1.5f, center = Offset(x + 4f, y - 29f))
            }
        }
        PetSpecies.CAT -> {
            // Orange Tabby Cat
            val catColor = Color(0xFFFB923C)
            val innerEar = Color(0xFFF472B6)
            val stripeColor = Color(0xFFC2410C)

            // Cat Head
            drawCircle(color = catColor, radius = 24f, center = Offset(x, y - 8f))

            // Pointy Ears
            val leftEar = Path().apply {
                moveTo(x - 22f, y - 18f)
                lineTo(x - 12f, y - 38f)
                lineTo(x - 4f, y - 26f)
                close()
            }
            val rightEar = Path().apply {
                moveTo(x + 4f, y - 26f)
                lineTo(x + 12f, y - 38f)
                lineTo(x + 22f, y - 18f)
                close()
            }
            drawPath(leftEar, color = catColor)
            drawPath(rightEar, color = catColor)

            // Inner Ear Pink
            val leftInner = Path().apply {
                moveTo(x - 19f, y - 20f)
                lineTo(x - 12f, y - 34f)
                lineTo(x - 7f, y - 26f)
                close()
            }
            drawPath(leftInner, color = innerEar)

            // Whiskers
            drawLine(Color.Black, Offset(x - 26f, y - 6f), Offset(x - 12f, y - 4f), strokeWidth = 2f)
            drawLine(Color.Black, Offset(x - 26f, y - 2f), Offset(x - 12f, y - 2f), strokeWidth = 2f)
            drawLine(Color.Black, Offset(x + 12f, y - 4f), Offset(x + 26f, y - 6f), strokeWidth = 2f)
            drawLine(Color.Black, Offset(x + 12f, y - 2f), Offset(x + 26f, y - 2f), strokeWidth = 2f)

            // Collar with Jingle Bell
            drawRoundRect(
                color = Color(0xFFDC2626),
                topLeft = Offset(x - 16f, y + 12f),
                size = Size(32f, 8f),
                cornerRadius = CornerRadius(4f)
            )
            drawCircle(color = Color(0xFFFBBF24), radius = 4f, center = Offset(x, y + 16f))

            // Eyes
            if (isTired) {
                drawLine(Color.Black, Offset(x - 12f, y - 12f), Offset(x - 4f, y - 12f), strokeWidth = 3.5f)
                drawLine(Color.Black, Offset(x + 4f, y - 12f), Offset(x + 12f, y - 12f), strokeWidth = 3.5f)
            } else {
                drawCircle(Color.Black, radius = 4f, center = Offset(x - 8f, y - 12f))
                drawCircle(Color.White, radius = 1.5f, center = Offset(x - 9f, y - 13f))

                drawCircle(Color.Black, radius = 4f, center = Offset(x + 8f, y - 12f))
                drawCircle(Color.White, radius = 1.5f, center = Offset(x + 7f, y - 13f))
            }
            // Nose
            drawCircle(Color(0xFFEC4899), radius = 3f, center = Offset(x, y - 4f))
        }
        PetSpecies.DOG -> {
            // Shiba Inu / Dog
            val dogColor = Color(0xFFD97706)
            val whiteMuzzle = Color(0xFFFEF3C7)

            // Body
            drawRoundRect(
                color = dogColor,
                topLeft = Offset(x - 30f, y - 16f),
                size = Size(60f, 38f),
                cornerRadius = CornerRadius(16f)
            )
            // Head
            drawCircle(color = dogColor, radius = 22f, center = Offset(x - 12f, y - 22f))
            // Muzzle
            drawCircle(color = whiteMuzzle, radius = 12f, center = Offset(x - 20f, y - 18f))

            // Floppy Ears
            drawOval(color = Color(0xFF92400E), topLeft = Offset(x - 28f, y - 42f), size = Size(12f, 22f))
            drawOval(color = Color(0xFF92400E), topLeft = Offset(x - 8f, y - 42f), size = Size(12f, 22f))

            // Black Nose
            drawCircle(Color.Black, radius = 4f, center = Offset(x - 26f, y - 20f))

            // Eye
            if (isTired) {
                drawLine(Color.Black, Offset(x - 20f, y - 26f), Offset(x - 12f, y - 26f), strokeWidth = 3.5f)
            } else {
                drawCircle(Color.Black, radius = 4f, center = Offset(x - 16f, y - 26f))
                drawCircle(Color.White, radius = 1.5f, center = Offset(x - 17f, y - 27f))
            }
        }
        PetSpecies.BEAVER -> {
            // Beaver with Buck Teeth
            val furColor = Color(0xFF78350F)
            val bellyColor = Color(0xFFFDE68A)

            // Paddle Tail
            val tailWiggle = sin(animProgress * Math.PI * 2).toFloat() * 4f
            drawOval(
                color = Color(0xFF451A03),
                topLeft = Offset(x + 14f, y + 2f + tailWiggle),
                size = Size(36f, 18f)
            )

            // Body
            drawOval(color = furColor, topLeft = Offset(x - 24f, y - 30f), size = Size(48f, 56f))
            drawOval(color = bellyColor, topLeft = Offset(x - 14f, y - 14f), size = Size(28f, 32f))

            // Buck Teeth (2 White Rectangles)
            drawRect(color = Color.White, topLeft = Offset(x - 6f, y - 4f), size = Size(5f, 10f))
            drawRect(color = Color.White, topLeft = Offset(x, y - 4f), size = Size(5f, 10f))
            drawRect(color = Color.Black, topLeft = Offset(x - 1f, y - 4f), size = Size(1f, 10f))

            // Nose
            drawCircle(Color.Black, radius = 4f, center = Offset(x - 1f, y - 8f))

            // Eyes
            if (isTired) {
                drawLine(Color.Black, Offset(x - 12f, y - 18f), Offset(x - 5f, y - 18f), strokeWidth = 3.5f)
                drawLine(Color.Black, Offset(x + 3f, y - 18f), Offset(x + 10f, y - 18f), strokeWidth = 3.5f)
            } else {
                drawCircle(Color.Black, radius = 4f, center = Offset(x - 8f, y - 18f))
                drawCircle(Color.White, radius = 1.5f, center = Offset(x - 9f, y - 19f))

                drawCircle(Color.Black, radius = 4f, center = Offset(x + 6f, y - 18f))
                drawCircle(Color.White, radius = 1.5f, center = Offset(x + 5f, y - 19f))
            }
        }
    }
}

private fun DrawScope.drawSleepingZzz(x: Float, y: Float, animProgress: Float) {
    val offsetY = (animProgress * 24f)
    val alpha = (1f - animProgress).coerceIn(0f, 1f)

    drawCircle(color = Color.White.copy(alpha = alpha), radius = 6f, center = Offset(x, y - offsetY))
    drawCircle(color = Color.White.copy(alpha = alpha * 0.7f), radius = 9f, center = Offset(x + 12f, y - 14f - offsetY))
}

private fun DrawScope.drawEatingHearts(x: Float, y: Float, animProgress: Float) {
    val floatY = animProgress * 22f
    val alpha = (1f - animProgress).coerceIn(0f, 1f)
    drawCircle(color = Color(0xFFF43F5E).copy(alpha = alpha), radius = 7f, center = Offset(x, y - floatY))
    drawCircle(color = Color(0xFFFB7185).copy(alpha = alpha * 0.7f), radius = 10f, center = Offset(x + 14f, y - 12f - floatY))
}

private fun DrawScope.drawTrickSparkles(x: Float, y: Float, animProgress: Float) {
    val floatY = abs(sin(animProgress * Math.PI * 2).toFloat()) * 10f
    drawCircle(color = Color(0xFFFBBF24), radius = 8f, center = Offset(x - 14f, y - floatY))
    drawCircle(color = Color(0xFF34D399), radius = 10f, center = Offset(x + 14f, y - floatY))
    drawCircle(color = Color(0xFF38BDF8), radius = 6f, center = Offset(x, y - 14f - floatY))
}

private fun DrawScope.drawFactCheckingSparkles(x: Float, y: Float, animProgress: Float) {
    val floatY = sin(animProgress * Math.PI * 2).toFloat() * 4f
    drawCircle(
        color = Color(0xFFFBBF24),
        radius = 12f,
        center = Offset(x, y + floatY),
        style = Stroke(width = 4f)
    )
    drawLine(
        color = Color(0xFFFBBF24),
        start = Offset(x + 8f, y + 8f + floatY),
        end = Offset(x + 16f, y + 16f + floatY),
        strokeWidth = 4f
    )
}
