/*
 * Copyright 2023 The Android Open Source Project
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

package com.tencent.kuikly.compose.foundation.pager

import com.tencent.kuikly.compose.foundation.gestures.Orientation
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.layout.Placeable
import com.tencent.kuikly.compose.ui.unit.IntOffset
import com.tencent.kuikly.compose.ui.unit.LayoutDirection
import com.tencent.kuikly.compose.ui.util.fastForEach
import com.tencent.kuikly.compose.ui.util.fastForEachIndexed

internal class MeasuredPage(
    override val index: Int,
    val size: Int,
    private val placeables: List<Placeable>,
    private val visualOffset: IntOffset,
    override val key: Any,
    orientation: Orientation,
    private val horizontalAlignment: Alignment.Horizontal?,
    private val verticalAlignment: Alignment.Vertical?,
    private val layoutDirection: LayoutDirection,
    private val reverseLayout: Boolean
) : PageInfo {

    private val isVertical = orientation == Orientation.Vertical

    val crossAxisSize: Int

    // optimized for storing x and y offsets for each placeable one by one.
    // array's size == placeables.size * 2, first we store x, then y.
    private val placeableOffsets: IntArray

    init {
        var maxCrossAxis = 0
        placeables.fastForEach {
            maxCrossAxis = maxOf(
                maxCrossAxis,
                if (!isVertical) it.height else it.width
            )
        }
        crossAxisSize = maxCrossAxis
        placeableOffsets = IntArray(placeables.size * 2)
    }

    override var offset: Int = 0
        private set

    private var mainAxisLayoutSize: Int = Unset

    fun position(
        offset: Int,
        layoutWidth: Int,
        layoutHeight: Int
    ) {
        this.offset = offset
        mainAxisLayoutSize =
            if (isVertical) layoutHeight else layoutWidth
        var mainAxisOffset = offset
        placeables.fastForEachIndexed { index, placeable ->
            val indexInArray = index * 2
            if (isVertical) {
                placeableOffsets[indexInArray] =
                    requireNotNull(horizontalAlignment) { "null horizontalAlignment" }
                        .align(placeable.width, layoutWidth, layoutDirection)
                placeableOffsets[indexInArray + 1] = mainAxisOffset
                mainAxisOffset += placeable.height
            } else {
                placeableOffsets[indexInArray] = mainAxisOffset
                placeableOffsets[indexInArray + 1] =
                    requireNotNull(verticalAlignment) { "null verticalAlignment" }
                        .align(placeable.height, layoutHeight)
                mainAxisOffset += placeable.width
            }
        }
    }

    fun place(scope: Placeable.PlacementScope) = with(scope) {
        require(mainAxisLayoutSize != Unset) { "position() should be called first" }
        repeat(placeables.size) { index ->
            val placeable = placeables[index]
            var offset = getOffset(index)
            if (reverseLayout) {
                offset = offset.copy { mainAxisOffset ->
                    mainAxisLayoutSize - mainAxisOffset - placeable.mainAxisSize
                }
            }
            offset += visualOffset
            if (isVertical) {
                placeable.placeWithLayer(offset)
            } else {
                placeable.placeRelativeWithLayer(offset)
            }
        }
    }

    fun applyScrollDelta(delta: Int) {
        offset += delta
        repeat(placeableOffsets.size) { index ->
            // placeableOffsets consist of x and y pairs for each placeable.
            // if isVertical is true then the main axis offsets are located at indexes 1, 3, 5 etc.
            if ((isVertical && index % 2 == 1) || (!isVertical && index % 2 == 0)) {
                placeableOffsets[index] += delta
            }
        }
    }

    private fun getOffset(index: Int) =
        IntOffset(placeableOffsets[index * 2], placeableOffsets[index * 2 + 1])

    private val Placeable.mainAxisSize get() = if (isVertical) height else width
    private inline fun IntOffset.copy(mainAxisMap: (Int) -> Int): IntOffset =
        IntOffset(if (isVertical) x else mainAxisMap(x), if (isVertical) mainAxisMap(y) else y)
}

private const val Unset = Int.MIN_VALUE
