/*
 * Copyright 2020 The Android Open Source Project
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

package com.tencent.kuikly.compose.foundation.lazy

import com.tencent.kuikly.compose.foundation.gestures.Orientation
import com.tencent.kuikly.compose.ui.layout.MeasureResult
import com.tencent.kuikly.compose.ui.unit.Constraints
import com.tencent.kuikly.compose.ui.unit.Density
import com.tencent.kuikly.compose.ui.unit.IntSize
import com.tencent.kuikly.compose.ui.util.fastForEach
import kotlinx.coroutines.CoroutineScope

/**
 * The result of the measure pass for lazy list layout.
 */
internal class LazyListMeasureResult(
    // properties defining the scroll position:
    /** The new first visible item.*/
    val firstVisibleItem: LazyListMeasuredItem?,
    /** The new value for [LazyListState.firstVisibleItemScrollOffset].*/
    var firstVisibleItemScrollOffset: Int,
    /** True if there is some space available to continue scrolling in the forward direction.*/
    var canScrollForward: Boolean,
    /** The amount of scroll consumed during the measure pass.*/
    var consumedScroll: Float,
    /** MeasureResult defining the layout.*/
    measureResult: MeasureResult,
    /** The amount of scroll-back that happened due to reaching the end of the list. */
    val scrollBackAmount: Float,
    /** True when extra remeasure is required. */
    val remeasureNeeded: Boolean,
    /** Scope for animations. */
    val coroutineScope: CoroutineScope,
    /** Density of the last measure. */
    val density: Density,
    /** Constraints used to measure children. */
    val childConstraints: Constraints,
    val stickyItem: LazyListItemInfo? = null,
    // properties representing the info needed for LazyListLayoutInfo:
    /** see [LazyListLayoutInfo.visibleItemsInfo] */
    val positionedItems: List<LazyListMeasuredItem>,
    override val visibleItemsInfo: List<LazyListMeasuredItem>,
    /** see [LazyListLayoutInfo.viewportStartOffset] */
    override val viewportStartOffset: Int,
    /** see [LazyListLayoutInfo.viewportEndOffset] */
    override val viewportEndOffset: Int,
    /** see [LazyListLayoutInfo.totalItemsCount] */
    override val totalItemsCount: Int,
    /** see [LazyListLayoutInfo.reverseLayout] */
    override val reverseLayout: Boolean,
    /** see [LazyListLayoutInfo.orientation] */
    override val orientation: Orientation,
    /** see [LazyListLayoutInfo.afterContentPadding] */
    override val afterContentPadding: Int,
    /** see [LazyListLayoutInfo.mainAxisItemSpacing] */
    override val mainAxisItemSpacing: Int,
) : LazyListLayoutInfo, MeasureResult by measureResult {

    val canScrollBackward
        get() = (firstVisibleItem?.index ?: 0) != 0 || firstVisibleItemScrollOffset != 0

    override val viewportSize: IntSize
        get() = IntSize(width, height)
    override val beforeContentPadding: Int get() = -viewportStartOffset

    /**
     * Tries to apply a scroll [delta] for this layout info. In some cases we can apply small
     * scroll deltas by just changing the offsets for each [visibleItemsInfo].
     * But we can only do so if after applying the delta we would not need to compose a new item
     * or dispose an item which is currently visible. In this case this function will not apply
     * the [delta] and return false.
     *
     * @return true if we can safely apply a passed scroll [delta] to this layout info.
     * If true is returned, only the placement phase is needed to apply new offsets.
     * If false is returned, it means we have to rerun the full measure phase to apply the [delta].
     */
    fun tryToApplyScrollWithoutRemeasure(delta: Int, updateAnimations: Boolean): Boolean {
        if (remeasureNeeded || visibleItemsInfo.isEmpty() || firstVisibleItem == null ||
            // applying this delta will change firstVisibleItem
            (firstVisibleItemScrollOffset - delta) !in
            0 until firstVisibleItem.mainAxisSizeWithSpacings
        ) {
            return false
        }


        // 检查是否所有items都是悬停的
        val allNonScrollable = visibleItemsInfo.all { it.nonScrollableItem }
        if (allNonScrollable) {
            // 如果都是悬停的，直接返回true，表示不需要重新测量
            return true
        }

        // 找到第一个和最后一个非悬停的item
        val first = visibleItemsInfo.firstOrNull { !it.nonScrollableItem }
        val last = visibleItemsInfo.lastOrNull { !it.nonScrollableItem }

        // 如果没有可滚动的item，返回true
        if (first == null || last == null) {
            return true
        }

        val canApply = if (delta < 0) {
            // scrolling forward
            val deltaToFirstItemChange =
                first.offset + first.mainAxisSizeWithSpacings - viewportStartOffset
            val deltaToLastItemChange =
                last.offset + last.mainAxisSizeWithSpacings - viewportEndOffset
            minOf(deltaToFirstItemChange, deltaToLastItemChange) > -delta
        } else {
            // scrolling backward
            val deltaToFirstItemChange =
                viewportStartOffset - first.offset
            val deltaToLastItemChange =
                viewportEndOffset - last.offset
            minOf(deltaToFirstItemChange, deltaToLastItemChange) > delta
        }
        return if (canApply) {
            firstVisibleItemScrollOffset -= delta
            visibleItemsInfo.fastForEach {
                it.applyScrollDelta(delta, updateAnimations)
            }
            consumedScroll = delta.toFloat()
            if (!canScrollForward && delta > 0) {
                // we scrolled backward, so now we can scroll forward
                canScrollForward = true
            }
            true
        } else {
            false
        }
    }
}
