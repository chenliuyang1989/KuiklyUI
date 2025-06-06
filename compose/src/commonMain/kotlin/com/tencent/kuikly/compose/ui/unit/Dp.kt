/*
 * Copyright 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
@file:Suppress("NOTHING_TO_INLINE")

package com.tencent.kuikly.compose.ui.unit

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import com.tencent.kuikly.compose.ui.geometry.isSpecified
import com.tencent.kuikly.compose.ui.unit.Dp.Companion.Hairline
import com.tencent.kuikly.compose.ui.util.lerp
import com.tencent.kuikly.compose.ui.util.packFloats
import com.tencent.kuikly.compose.ui.util.unpackFloat1
import com.tencent.kuikly.compose.ui.util.unpackFloat2
import kotlin.jvm.JvmInline
import kotlin.math.max
import kotlin.math.min

/**
 * Dimension value representing device-independent pixels (dp). Component APIs specify their
 * dimensions such as line thickness in DP with Dp objects. Hairline (1 pixel) thickness
 * may be specified with [Hairline], a dimension that take up no space. Dp are normally
 * defined using [dp], which can be applied to [Int], [Double], and [Float].
 *
 * @sample androidx.compose.ui.unit.samples.DpSample
 *
 * Drawing and Layout are done in pixels. To retrieve the pixel size of a Dp, use [Density.toPx]:
 *
 * @sample androidx.compose.ui.unit.samples.ToPxSample
 */
@Immutable
@JvmInline
value class Dp(val value: Float) : Comparable<Dp> {
    /**
     * Add two [Dp]s together.
     */
    @Stable
    inline operator fun plus(other: Dp) = Dp(this.value + other.value)

    /**
     * Subtract a Dp from another one.
     */
    @Stable
    inline operator fun minus(other: Dp) = Dp(this.value - other.value)

    /**
     * This is the same as multiplying the Dp by -1.0.
     */
    @Stable
    inline operator fun unaryMinus() = Dp(-value)

    /**
     * Divide a Dp by a scalar.
     */
    @Stable
    inline operator fun div(other: Float): Dp = Dp(value / other)

    @Stable
    inline operator fun div(other: Int): Dp = Dp(value / other)

    /**
     * Divide by another Dp to get a scalar.
     */
    @Stable
    inline operator fun div(other: Dp): Float = value / other.value

    /**
     * Multiply a Dp by a scalar.
     */
    @Stable
    inline operator fun times(other: Float): Dp = Dp(value * other)

    @Stable
    inline operator fun times(other: Int): Dp = Dp(value * other)

    /**
     * Support comparing Dimensions with comparison operators.
     */
    @Stable
    override /* TODO: inline */ operator fun compareTo(other: Dp) = value.compareTo(other.value)

    @Stable
    override fun toString() = if (isUnspecified) "Dp.Unspecified" else "$value.dp"

    companion object {
        /**
         * A dimension used to represent a hairline drawing element. Hairline elements take up no
         * space, but will draw a single pixel, independent of the device's resolution and density.
         */
        @Stable
        val Hairline = Dp(0f)

        /**
         * Infinite dp dimension.
         */
        @Stable
        val Infinity = Dp(Float.POSITIVE_INFINITY)

        /**
         * Constant that means unspecified Dp
         */
        @Stable
        val Unspecified = Dp(Float.NaN)
    }
}

/**
 * `false` when this is [Dp.Unspecified].
 */
@Stable
inline val Dp.isSpecified: Boolean
    get() = !value.isNaN()

/**
 * `true` when this is [Dp.Unspecified].
 */
@Stable
inline val Dp.isUnspecified: Boolean
    get() = value.isNaN()

/**
 * If this [Dp] [isSpecified] then this is returned, otherwise [block] is executed
 * and its result is returned.
 */
inline fun Dp.takeOrElse(block: () -> Dp): Dp =
    if (isSpecified) this else block()

/**
 * Create a [Dp] using an [Int]:
 *     val left = 10
 *     val x = left.dp
 *     // -- or --
 *     val y = 10.dp
 */
@Stable
inline val Int.dp: Dp get() = Dp(this.toFloat())

/**
 * Create a [Dp] using a [Double]:
 *     val left = 10.0
 *     val x = left.dp
 *     // -- or --
 *     val y = 10.0.dp
 */
@Stable
inline val Double.dp: Dp get() = Dp(this.toFloat())

/**
 * Create a [Dp] using a [Float]:
 *     val left = 10f
 *     val x = left.dp
 *     // -- or --
 *     val y = 10f.dp
 */
@Stable
inline val Float.dp: Dp get() = Dp(this)

@Stable
inline operator fun Float.times(other: Dp) =
    Dp(this * other.value)

@Stable
inline operator fun Double.times(other: Dp) =
    Dp(this.toFloat() * other.value)

@Stable
inline operator fun Int.times(other: Dp) =
    Dp(this * other.value)

@Stable
inline fun min(a: Dp, b: Dp): Dp = Dp(min(a.value, b.value))

@Stable
inline fun max(a: Dp, b: Dp): Dp = Dp(max(a.value, b.value))

/**
 * Ensures that this value lies in the specified range [minimumValue]..[maximumValue].
 *
 * @return this value if it's in the range, or [minimumValue] if this value is less than
 * [minimumValue], or [maximumValue] if this value is greater than [maximumValue].
 */
@Stable
inline fun Dp.coerceIn(minimumValue: Dp, maximumValue: Dp): Dp =
    Dp(value.coerceIn(minimumValue.value, maximumValue.value))

/**
 * Ensures that this value is not less than the specified [minimumValue].
 * @return this value if it's greater than or equal to the [minimumValue] or the
 * [minimumValue] otherwise.
 */
@Stable
inline fun Dp.coerceAtLeast(minimumValue: Dp): Dp =
    Dp(value.coerceAtLeast(minimumValue.value))

/**
 * Ensures that this value is not greater than the specified [maximumValue].
 *
 * @return this value if it's less than or equal to the [maximumValue] or the
 * [maximumValue] otherwise.
 */
@Stable
inline fun Dp.coerceAtMost(maximumValue: Dp): Dp =
    Dp(value.coerceAtMost(maximumValue.value))

/**
 *
 * Return `true` when it is finite or `false` when it is [Dp.Infinity]
 */
@Stable
inline val Dp.isFinite: Boolean get() = value != Float.POSITIVE_INFINITY

/**
 * Linearly interpolate between two [Dp]s.
 *
 * The [fraction] argument represents position on the timeline, with 0.0 meaning
 * that the interpolation has not started, returning [start] (or something
 * equivalent to [start]), 1.0 meaning that the interpolation has finished,
 * returning [stop] (or something equivalent to [stop]), and values in between
 * meaning that the interpolation is at the relevant point on the timeline
 * between [start] and [stop]. The interpolation can be extrapolated beyond 0.0 and
 * 1.0, so negative values and values greater than 1.0 are valid.
 */
@Stable
fun lerp(start: Dp, stop: Dp, fraction: Float): Dp {
    return Dp(lerp(start.value, stop.value, fraction))
}

// -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
// Structures using Dp
// -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=

/**
 * Constructs a [DpOffset] from [x] and [y] position [Dp] values.
 */
@Stable
fun DpOffset(x: Dp, y: Dp): DpOffset = DpOffset(packFloats(x.value, y.value))

/**
 * A two-dimensional offset using [Dp] for units
 */
@Immutable
@JvmInline
value class DpOffset internal constructor(@PublishedApi internal val packedValue: Long) {
    /**
     * The horizontal aspect of the offset in [Dp]
     */
    @Stable
    val x: Dp get() = unpackFloat1(packedValue).dp

    /**
     * The vertical aspect of the offset in [Dp]
     */
    @Stable
    val y: Dp get() = unpackFloat2(packedValue).dp

    /**
     * Returns a copy of this [DpOffset] instance optionally overriding the
     * x or y parameter
     */
    fun copy(x: Dp = this.x, y: Dp = this.y): DpOffset = DpOffset(packFloats(x.value, y.value))

    /**
     * Subtract a [DpOffset] from another one.
     */
    @Stable
    operator fun minus(other: DpOffset) = DpOffset(
        packFloats(
            (x - other.x).value,
            (y - other.y).value
        )
    )

    /**
     * Add a [DpOffset] to another one.
     */
    @Stable
    operator fun plus(other: DpOffset) = DpOffset(
        packFloats(
            (x + other.x).value,
            (y + other.y).value
        )
    )

    @Stable
    override fun toString(): String =
        if (isSpecified) {
            "($x, $y)"
        } else {
            "DpOffset.Unspecified"
        }

    companion object {
        /**
         * A [DpOffset] with 0 DP [x] and 0 DP [y] values.
         */
        val Zero = DpOffset(0x0L)

        /**
         * Represents an offset whose [x] and [y] are unspecified. This is usually a replacement for
         * `null` when a primitive value is desired.
         * Access to [x] or [y] on an unspecified offset is not allowed.
         */
        val Unspecified = DpOffset(0x7fc00000_7fc00000L)
    }
}

/**
 * `false` when this is [DpOffset.Unspecified].
 */
@Stable
inline val DpOffset.isSpecified: Boolean
    get() = packedValue != 0x7fc00000_7fc00000L // Keep UnspecifiedPackedFloats internal

/**
 * `true` when this is [DpOffset.Unspecified].
 */
@Stable
inline val DpOffset.isUnspecified: Boolean
    get() = packedValue == 0x7fc00000_7fc00000L // Keep UnspecifiedPackedFloats internal

/**
 * If this [DpOffset]&nbsp;[isSpecified] then this is returned, otherwise [block] is executed
 * and its result is returned.
 */
inline fun DpOffset.takeOrElse(block: () -> DpOffset): DpOffset =
    if (isSpecified) this else block()

/**
 * Linearly interpolate between two [DpOffset]s.
 *
 * The [fraction] argument represents position on the timeline, with 0.0 meaning
 * that the interpolation has not started, returning [start] (or something
 * equivalent to [start]), 1.0 meaning that the interpolation has finished,
 * returning [stop] (or something equivalent to [stop]), and values in between
 * meaning that the interpolation is at the relevant point on the timeline
 * between [start] and [stop]. The interpolation can be extrapolated beyond 0.0 and
 * 1.0, so negative values and values greater than 1.0 are valid.
 */
@Stable
fun lerp(start: DpOffset, stop: DpOffset, fraction: Float): DpOffset =
    DpOffset(
        packFloats(
            lerp(start.x.value, stop.x.value, fraction),
            lerp(start.y.value, stop.y.value, fraction)
        )
    )

/**
 * Constructs a [DpSize] from [width] and [height] [Dp] values.
 */
@Stable
fun DpSize(width: Dp, height: Dp): DpSize = DpSize(packFloats(width.value, height.value))

/**
 * A two-dimensional Size using [Dp] for units
 */
@Immutable
@JvmInline
value class DpSize internal constructor(@PublishedApi internal val packedValue: Long) {
    /**
     * The horizontal aspect of the Size in [Dp]
     */
    @Stable
    val width: Dp get() = unpackFloat1(packedValue).dp

    /**
     * The vertical aspect of the Size in [Dp]
     */
    @Stable
    val height: Dp get() = unpackFloat2(packedValue).dp

    /**
     * Returns a copy of this [DpSize] instance optionally overriding the
     * width or height parameter
     */
    fun copy(width: Dp = this.width, height: Dp = this.height): DpSize = DpSize(
        packFloats(width.value, height.value)
    )

    /**
     * Subtract a [DpSize] from another one.
     */
    @Stable
    operator fun minus(other: DpSize) =
        DpSize(
            packFloats(
                (width - other.width).value,
                (height - other.height).value
            )
        )

    /**
     * Add a [DpSize] to another one.
     */
    @Stable
    operator fun plus(other: DpSize) =
        DpSize(
            packFloats(
                (width + other.width).value,
                (height + other.height).value
            )
        )

    @Stable
    inline operator fun component1(): Dp = width

    @Stable
    inline operator fun component2(): Dp = height

    @Stable
    operator fun times(other: Int): DpSize = DpSize(
        packFloats(
            (width * other).value,
            (height * other).value
        )
    )

    @Stable
    operator fun times(other: Float): DpSize = DpSize(
        packFloats(
            (width * other).value,
            (height * other).value
        )
    )

    @Stable
    operator fun div(other: Int): DpSize = DpSize(
        packFloats(
            (width / other).value,
            (height / other).value
        )
    )

    @Stable
    operator fun div(other: Float): DpSize = DpSize(
        packFloats(
            (width / other).value,
            (height / other).value
        )
    )

    @Stable
    override fun toString(): String =
        if (isSpecified) {
            "$width x $height"
        } else {
            "DpSize.Unspecified"
        }

    companion object {
        /**
         * A [DpSize] with 0 DP [width] and 0 DP [height] values.
         */
        val Zero = DpSize(0x0L)

        /**
         * A size whose [width] and [height] are unspecified. This is usually a replacement for
         * `null` when a primitive value is desired.
         * Access to [width] or [height] on an unspecified size is not allowed.
         */
        val Unspecified = DpSize(0x7fc00000_7fc00000L)
    }
}

/**
 * `false` when this is [DpSize.Unspecified].
 */
@Stable
inline val DpSize.isSpecified: Boolean
    get() = packedValue != 0x7fc00000_7fc00000L // Keep UnspecifiedPackedFloats internal

/**
 * `true` when this is [DpSize.Unspecified].
 */
@Stable
inline val DpSize.isUnspecified: Boolean
    get() = packedValue == 0x7fc00000_7fc00000L // Keep UnspecifiedPackedFloats internal

/**
 * If this [DpSize]&nbsp;[isSpecified] then this is returned, otherwise [block] is executed
 * and its result is returned.
 */
inline fun DpSize.takeOrElse(block: () -> DpSize): DpSize =
    if (isSpecified) this else block()

/**
 * Returns the [DpOffset] of the center of the rect from the point of [0, 0]
 * with this [DpSize].
 */
@Stable
val DpSize.center: DpOffset
    get() = DpOffset(
        packFloats(
            (width / 2f).value,
            (height / 2f).value
        )
    )

@Stable
inline operator fun Int.times(size: DpSize) = size * this

@Stable
inline operator fun Float.times(size: DpSize) = size * this

/**
 * Linearly interpolate between two [DpSize]s.
 *
 * The [fraction] argument represents position on the timeline, with 0.0 meaning
 * that the interpolation has not started, returning [start], 1.0 meaning that the
 * interpolation has finished, returning [stop], and values in between
 * meaning that the interpolation is at the relevant point on the timeline
 * between [start] and [stop]. The interpolation can be extrapolated beyond 0.0 and
 * 1.0, so negative values and values greater than 1.0 are valid.
 */
@Stable
fun lerp(start: DpSize, stop: DpSize, fraction: Float): DpSize =
    DpSize(
        packFloats(
            lerp(start.width, stop.width, fraction).value,
            lerp(start.height, stop.height, fraction).value
        )
    )

/**
 * A four dimensional bounds using [Dp] for units
 */
@Immutable
data class DpRect(
    @Stable
    val left: Dp,
    @Stable
    val top: Dp,
    @Stable
    val right: Dp,
    @Stable
    val bottom: Dp
) {
    /**
     * Constructs a [DpRect] from the top-left [origin] and the width and height in [size].
     */
    constructor(origin: DpOffset, size: DpSize) :
        this(origin.x, origin.y, origin.x + size.width, origin.y + size.height)

    companion object
}

/**
 * A width of this Bounds in [Dp].
 */
@Stable
inline val DpRect.width: Dp get() = right - left

/**
 * A height of this Bounds in [Dp].
 */
@Stable
inline val DpRect.height: Dp get() = bottom - top

/**
 * Returns the size of the [DpRect].
 */
@Stable
inline val DpRect.size: DpSize get() = DpSize(width, height)
