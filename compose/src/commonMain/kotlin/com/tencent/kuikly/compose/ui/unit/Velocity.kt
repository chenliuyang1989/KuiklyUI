/*
 * Copyright 2019 The Android Open Source Project
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
import com.tencent.kuikly.compose.ui.util.packFloats
import com.tencent.kuikly.compose.ui.util.unpackFloat1
import com.tencent.kuikly.compose.ui.util.unpackFloat2

/**
 * Constructs an Velocity from the given relative x and y velocities.
 *
 * @param x Horizontal component of the velocity in pixels per second
 * @param y Vertical component of the velocity in pixels per second
 */
@Stable
fun Velocity(x: Float, y: Float) = Velocity(packFloats(x, y))

/**
 * A two dimensional velocity in pixels per second.
 */
@Immutable
@kotlin.jvm.JvmInline
value class Velocity internal constructor(private val packedValue: Long) {
    /**
     * The horizontal component of the velocity in pixels per second.
     */
    @Stable
    val x: Float get() = unpackFloat1(packedValue)

    /**
     * The vertical component of the velocity in pixels per second.
     */
    @Stable
    val y: Float get() = unpackFloat2(packedValue)

    /**
     * The horizontal component of the velocity in pixels per second.
     */
    @Stable
    inline operator fun component1(): Float = x

    /**
     * The vertical component of the velocity in pixels per second.
     */
    @Stable
    inline operator fun component2(): Float = y

    /**
     * Returns a copy of this [Velocity] instance optionally overriding the
     * x or y parameter
     */
    fun copy(x: Float = unpackFloat1(packedValue), y: Float = unpackFloat2(packedValue)) =
        Velocity(packFloats(x, y))

    companion object {
        /**
         * An offset with zero magnitude.
         *
         * This can be used to represent the origin of a coordinate space.
         */
        @Stable
        val Zero = Velocity(0x0L)
    }

    /**
     * Unary negation operator.
     *
     * Returns a [Velocity] with the coordinates negated.
     *
     * If the [Velocity] represents an arrow on a plane, this operator returns the
     * same arrow but pointing in the reverse direction.
     */
    @Stable
    operator fun unaryMinus(): Velocity = Velocity(packedValue xor DualFloatSignBit)

    /**
     * Binary subtraction operator.
     *
     * Returns a [Velocity] whose [x] value is the left-hand-side operand's [x]
     * minus the right-hand-side operand's [x] and whose [y] value is the
     * left-hand-side operand's [y] minus the right-hand-side operand's [y].
     */
    @Stable
    operator fun minus(other: Velocity): Velocity = Velocity(
        packFloats(
            unpackFloat1(packedValue) - unpackFloat1(other.packedValue),
            unpackFloat2(packedValue) - unpackFloat2(other.packedValue)
        )
    )

    /**
     * Binary addition operator.
     *
     * Returns a [Velocity] whose [x] value is the sum of the [x] values of the
     * two operands, and whose [y] value is the sum of the [y] values of the
     * two operands.
     */
    @Stable
    operator fun plus(other: Velocity): Velocity = Velocity(
        packFloats(
            unpackFloat1(packedValue) + unpackFloat1(other.packedValue),
            unpackFloat2(packedValue) + unpackFloat2(other.packedValue)
        )
    )

    /**
     * Multiplication operator.
     *
     * Returns a [Velocity] whose coordinates are those of the
     * left-hand-side operand (a [Velocity]) multiplied by the scalar
     * right-hand-side operand (a [Float]).
     */
    @Stable
    operator fun times(operand: Float): Velocity = Velocity(
        packFloats(
            unpackFloat1(packedValue) * operand,
            unpackFloat2(packedValue) * operand
        )
    )

    /**
     * Division operator.
     *
     * Returns a [Velocity] whose coordinates are those of the
     * left-hand-side operand (an [Velocity]) divided by the scalar right-hand-side
     * operand (a [Float]).
     */
    @Stable
    operator fun div(operand: Float): Velocity = Velocity(
        packFloats(
            unpackFloat1(packedValue) / operand,
            unpackFloat2(packedValue) / operand
        )
    )

    /**
     * Modulo (remainder) operator.
     *
     * Returns a [Velocity] whose coordinates are the remainder of dividing the
     * coordinates of the left-hand-side operand (a [Velocity]) by the scalar
     * right-hand-side operand (a [Float]).
     */
    @Stable
    operator fun rem(operand: Float) = Velocity(
        packFloats(
            unpackFloat1(packedValue) % operand,
            unpackFloat2(packedValue) % operand
        )
    )

    override fun toString() = "($x, $y) px/sec"
}
