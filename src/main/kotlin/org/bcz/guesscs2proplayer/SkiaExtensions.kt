package org.bcz.guesscs2proplayer

import org.jetbrains.skia.*

// Skia 扩展函数
fun Rect.toRRect() = RRect.makeLTRB(left, top, right, bottom, 0f)
fun Rect.toRRect(radius: Float) = RRect.makeLTRB(left, top, right, bottom, radius)

fun Canvas.drawRectShadowAntiAlias(r: Rect, dx: Float, dy: Float, blur: Float, spread: Float, color: Int): Canvas {
    val shadowPaint = Paint().apply {
        this.color = color
        this.imageFilter = ImageFilter.makeDropShadow(dx, dy, blur, blur, color)
    }
    val shadowRect = r.offset(dx, dy).inflate(spread)
    drawRect(shadowRect, shadowPaint)
    return this
}

fun Canvas.drawImageRRect(image: Image, srcRect: Rect, rRect: RRect, paint: Paint? = null) {
    save()
    clipRRect(rRect, true)
    drawImageRect(image, srcRect, rRect, FilterMipmap(FilterMode.LINEAR, MipmapMode.NEAREST), paint, false)
    restore()
}

fun Canvas.drawImageRRect(image: Image, rRect: RRect, paint: Paint? = null) =
    drawImageRRect(image, Rect(0f, 0f, image.width.toFloat(), image.height.toFloat()), rRect, paint)


