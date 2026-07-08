package com.example.myapplication.model

data class ScreenSizes(
    val scale: Float,
    val slotSize: Float,
    val padding: Float,
    val charDisplaySize: Float,
    val charGlowRadius: Float,
    val equipmentRadius: Float,
    val equipmentSlotSize: Float,
    val gridSlotSize: Float,
    val gridPadding: Float,
    val gridStartY: Float,
    val gridCols: Int,
    val gridRows: Int,
    val infoWidth: Float,
    val infoHeight: Float,
    val fontSizeTitle: Float,
    val fontSizeLarge: Float,
    val fontSizeMedium: Float,
    val fontSizeSmall: Float,
    val fontSizeTiny: Float,
    val closeBtnSize: Float,
    val editBtnSize: Float,
    val dialogWidth: Float,
    val dialogHeight: Float,
    val btnWidth: Float,
    val btnHeight: Float,
    val keyboardKeyWidth: Float,
    val keyboardKeyHeight: Float,
    val keyboardSpacing: Float
) {
    companion object {
        fun calculate(width: Float, height: Float): ScreenSizes {
            val baseWidth = 1080f
            val baseHeight = 1920f
            val scaleX = width / baseWidth
            val scaleY = height / baseHeight
            val scale = minOf(scaleX, scaleY).coerceIn(0.5f, 1.8f)

            return ScreenSizes(
                scale = scale,
                slotSize = 160f * scale,
                padding = 16f * scale,
                charDisplaySize = 280f * scale,
                charGlowRadius = 280f * scale,
                equipmentRadius = 360f * scale,
                equipmentSlotSize = 120f * scale,
                gridSlotSize = 140f * scale,
                gridPadding = 12f * scale,
                gridStartY = height * 0.55f,
                gridCols = 5,
                gridRows = 4,
                infoWidth = 680f * scale,
                infoHeight = 560f * scale,
                fontSizeTitle = 44f * scale,
                fontSizeLarge = 32f * scale,
                fontSizeMedium = 24f * scale,
                fontSizeSmall = 18f * scale,
                fontSizeTiny = 14f * scale,
                closeBtnSize = 55f * scale,
                editBtnSize = 38f * scale,
                dialogWidth = 600f * scale,
                dialogHeight = 680f * scale,
                btnWidth = 150f * scale,
                btnHeight = 50f * scale,
                keyboardKeyWidth = 65f * scale,
                keyboardKeyHeight = 65f * scale,
                keyboardSpacing = 6f * scale
            )
        }
    }
}
