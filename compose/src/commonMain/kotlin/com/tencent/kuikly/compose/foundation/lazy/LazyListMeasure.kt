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
import com.tencent.kuikly.compose.foundation.layout.Arrangement
import com.tencent.kuikly.compose.foundation.lazy.layout.ObservableScopeInvalidator
import com.tencent.kuikly.compose.foundation.lazy.layout.StickyItemsPlacement
import com.tencent.kuikly.compose.foundation.lazy.layout.applyStickyItems
import com.tencent.kuikly.compose.ui.layout.MeasureResult
import com.tencent.kuikly.compose.ui.layout.Placeable
import com.tencent.kuikly.compose.ui.unit.Constraints
import com.tencent.kuikly.compose.ui.unit.Density
import com.tencent.kuikly.compose.ui.unit.IntSize
import com.tencent.kuikly.compose.ui.unit.LayoutDirection
import com.tencent.kuikly.compose.ui.unit.constrainHeight
import com.tencent.kuikly.compose.ui.unit.constrainWidth
import com.tencent.kuikly.compose.ui.util.fastFilter
import com.tencent.kuikly.compose.ui.util.fastFirstOrNull
import com.tencent.kuikly.compose.ui.util.fastForEach
import com.tencent.kuikly.compose.ui.util.fastForEachReversed
import com.tencent.kuikly.compose.ui.util.fastRoundToInt
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.sign
import kotlinx.coroutines.CoroutineScope

/**
 * Measures and calculates the positions for the requested items. The result is produced
 * as a [LazyListMeasureResult] which contains all the calculations.
 */
internal fun measureLazyList(
    itemsCount: Int,
    measuredItemProvider: LazyListMeasuredItemProvider,
    mainAxisAvailableSize: Int,
    beforeContentPadding: Int,
    afterContentPadding: Int,
    spaceBetweenItems: Int,
    firstVisibleItemIndex: Int,
    firstVisibleItemScrollOffset: Int,
    scrollToBeConsumed: Float,
    constraints: Constraints,
    isVertical: Boolean,
    verticalArrangement: Arrangement.Vertical?,
    horizontalArrangement: Arrangement.Horizontal?,
    reverseLayout: Boolean,
    density: Density,
//    itemAnimator: LazyLayoutItemAnimator<LazyListMeasuredItem>,
    beyondBoundsItemCount: Int,
    pinnedItems: List<Int>,
    hasLookaheadPassOccurred: Boolean,
    isLookingAhead: Boolean,
    postLookaheadLayoutInfo: LazyListLayoutInfo?,
    coroutineScope: CoroutineScope,
    placementScopeInvalidator: ObservableScopeInvalidator,
//    graphicsContext: GraphicsContext,
    stickyItemsPlacement: StickyItemsPlacement?,
    layout: (Int, Int, Placeable.PlacementScope.() -> Unit) -> MeasureResult
): LazyListMeasureResult {
    require(beforeContentPadding >= 0) { "invalid beforeContentPadding" }
    require(afterContentPadding >= 0) { "invalid afterContentPadding" }
    if (itemsCount <= 0) {
        // empty data set. reset the current scroll and report zero size
        var layoutWidth = constraints.minWidth
        var layoutHeight = constraints.minHeight
        // TODO: jonas
//        itemAnimator.onMeasured(
//            consumedScroll = 0,
//            layoutWidth = layoutWidth,
//            layoutHeight = layoutHeight,
//            positionedItems = mutableListOf(),
//            keyIndexMap = measuredItemProvider.keyIndexMap,
//            itemProvider = measuredItemProvider,
//            isVertical = isVertical,
//            laneCount = 1,
//            isLookingAhead = isLookingAhead,
//            hasLookaheadOccurred = hasLookaheadPassOccurred,
//            layoutMinOffset = 0,
//            layoutMaxOffset = 0,
//            coroutineScope = coroutineScope,
//            graphicsContext = graphicsContext
//        )
//        if (!isLookingAhead) {
//            val disappearingItemsSize = itemAnimator.minSizeToFitDisappearingItems
//            if (disappearingItemsSize != IntSize.Zero) {
//                layoutWidth = constraints.constrainWidth(disappearingItemsSize.width)
//                layoutHeight = constraints.constrainHeight(disappearingItemsSize.height)
//            }
//        }
        return LazyListMeasureResult(
            firstVisibleItem = null,
            firstVisibleItemScrollOffset = 0,
            canScrollForward = false,
            consumedScroll = 0f,
            measureResult = layout(layoutWidth, layoutHeight) {},
            scrollBackAmount = 0f,
            visibleItemsInfo = emptyList(),
            positionedItems = emptyList(),
            viewportStartOffset = -beforeContentPadding,
            viewportEndOffset = mainAxisAvailableSize + afterContentPadding,
            totalItemsCount = 0,
            reverseLayout = reverseLayout,
            orientation = if (isVertical) Orientation.Vertical else Orientation.Horizontal,
            afterContentPadding = afterContentPadding,
            mainAxisItemSpacing = spaceBetweenItems,
            remeasureNeeded = false,
            coroutineScope = coroutineScope,
            density = density,
            childConstraints = measuredItemProvider.childConstraints
        )
    } else {
        var currentFirstItemIndex = firstVisibleItemIndex
        var currentFirstItemScrollOffset = firstVisibleItemScrollOffset
        if (currentFirstItemIndex >= itemsCount) {
            // the data set has been updated and now we have less items that we were
            // scrolled to before
            currentFirstItemIndex = itemsCount - 1
            currentFirstItemScrollOffset = 0
        }

        // represents the real amount of scroll we applied as a result of this measure pass.
        var scrollDelta = scrollToBeConsumed.fastRoundToInt()

        // applying the whole requested scroll offset. we will figure out if we can't consume
        // all of it later
        currentFirstItemScrollOffset -= scrollDelta

        // if the current scroll offset is less than minimally possible
        if (currentFirstItemIndex == 0 && currentFirstItemScrollOffset < 0) {
            scrollDelta += currentFirstItemScrollOffset
            currentFirstItemScrollOffset = 0
        }

        // this will contain all the MeasuredItems representing the visible items
        val visibleItems = ArrayDeque<LazyListMeasuredItem>()

        // define min and max offsets
        val minOffset = -beforeContentPadding + if (spaceBetweenItems < 0) spaceBetweenItems else 0
        val maxOffset = mainAxisAvailableSize

        // include the start padding so we compose items in the padding area and neutralise item
        // spacing (if the spacing is negative this will make sure the previous item is composed)
        // before starting scrolling forward we will remove it back
        currentFirstItemScrollOffset += minOffset

        // max of cross axis sizes of all visible items
        var maxCrossAxis = 0

        // will be set to true if we composed some items only to know their size and apply scroll,
        // while in the end this item will not end up in the visible viewport. we will need an
        // extra remeasure in order to dispose such items.
        var remeasureNeeded = false

        // we had scrolled backward or we compose items in the start padding area, which means
        // items before current firstItemScrollOffset should be visible. compose them and update
        // firstItemScrollOffset
        while (currentFirstItemScrollOffset < 0 && currentFirstItemIndex > 0) {
            val previous = currentFirstItemIndex - 1
            val measuredItem = measuredItemProvider.getAndMeasure(previous)
            visibleItems.add(0, measuredItem)
            maxCrossAxis = maxOf(maxCrossAxis, measuredItem.crossAxisSize)
            currentFirstItemScrollOffset += measuredItem.mainAxisSizeWithSpacings
            currentFirstItemIndex = previous
        }

        // if we were scrolled backward, but there were not enough items before. this means
        // not the whole scroll was consumed
        if (currentFirstItemScrollOffset < minOffset) {
            scrollDelta += currentFirstItemScrollOffset
            currentFirstItemScrollOffset = minOffset
        }

        // neutralize previously added padding as we stopped filling the before content padding
        currentFirstItemScrollOffset -= minOffset

        var index = currentFirstItemIndex
        val maxMainAxis = (maxOffset + afterContentPadding).coerceAtLeast(0)
        var currentMainAxisOffset = -currentFirstItemScrollOffset

        // first we need to skip items we already composed while composing backward
        var indexInVisibleItems = 0
        while (indexInVisibleItems < visibleItems.size) {
            if (currentMainAxisOffset >= maxMainAxis) {
                // this item is out of the bounds and will not be visible.
                visibleItems.removeAt(indexInVisibleItems)
                remeasureNeeded = true
            } else {
                index++
                currentMainAxisOffset += visibleItems[indexInVisibleItems].mainAxisSizeWithSpacings
                indexInVisibleItems++
            }
        }

        // then composing visible items forward until we fill the whole viewport.
        // we want to have at least one item in visibleItems even if in fact all the items are
        // offscreen, this can happen if the content padding is larger than the available size.
        while (index < itemsCount &&
            (currentMainAxisOffset < maxMainAxis ||
                currentMainAxisOffset <= 0 || // filling beforeContentPadding area
                visibleItems.isEmpty())
        ) {
            val measuredItem = measuredItemProvider.getAndMeasure(index)
            currentMainAxisOffset += measuredItem.mainAxisSizeWithSpacings

            if (currentMainAxisOffset <= minOffset && index != itemsCount - 1) {
                // this item is offscreen and will not be visible. advance firstVisibleItemIndex
                currentFirstItemIndex = index + 1
                currentFirstItemScrollOffset -= measuredItem.mainAxisSizeWithSpacings
                remeasureNeeded = true
            } else {
                maxCrossAxis = maxOf(maxCrossAxis, measuredItem.crossAxisSize)
                visibleItems.add(measuredItem)
            }

            index++
        }

        val preScrollBackScrollDelta = scrollDelta
        // we didn't fill the whole viewport with items starting from firstVisibleItemIndex.
        // lets try to scroll back if we have enough items before firstVisibleItemIndex.
        if (currentMainAxisOffset < maxOffset) {
            val toScrollBack = maxOffset - currentMainAxisOffset
            currentFirstItemScrollOffset -= toScrollBack
            currentMainAxisOffset += toScrollBack
            while (currentFirstItemScrollOffset < beforeContentPadding &&
                currentFirstItemIndex > 0
            ) {
                val previousIndex = currentFirstItemIndex - 1
                val measuredItem = measuredItemProvider.getAndMeasure(previousIndex)
                visibleItems.add(0, measuredItem)
                maxCrossAxis = maxOf(maxCrossAxis, measuredItem.crossAxisSize)
                currentFirstItemScrollOffset += measuredItem.mainAxisSizeWithSpacings
                currentFirstItemIndex = previousIndex
            }
            scrollDelta += toScrollBack
            if (currentFirstItemScrollOffset < 0) {
                scrollDelta += currentFirstItemScrollOffset
                currentMainAxisOffset += currentFirstItemScrollOffset
                currentFirstItemScrollOffset = 0
            }
        }

        // report the amount of pixels we consumed. scrollDelta can be smaller than
        // scrollToBeConsumed if there were not enough items to fill the offered space or it
        // can be larger if items were resized, or if, for example, we were previously
        // displaying the item 15, but now we have only 10 items in total in the data set.
        val consumedScroll = if (scrollToBeConsumed.fastRoundToInt().sign == scrollDelta.sign &&
            abs(scrollToBeConsumed.fastRoundToInt()) >= abs(scrollDelta)
        ) {
            scrollDelta.toFloat()
        } else {
            scrollToBeConsumed
        }

        val unconsumedScroll = scrollToBeConsumed - consumedScroll
        // When scrolling to the bottom via gesture, there could be scrollback due to
        // not being able to consume the whole scroll. In that case, the amount of
        // scrollBack is the inverse of unconsumed scroll.
        val scrollBackAmount: Float =
            if (isLookingAhead && scrollDelta > preScrollBackScrollDelta && unconsumedScroll <= 0) {
                scrollDelta - preScrollBackScrollDelta + unconsumedScroll
            } else
                0f

        // the initial offset for items from visibleItems list
        require(currentFirstItemScrollOffset >= 0) { "negative currentFirstItemScrollOffset" }
        val visibleItemsScrollOffset = -currentFirstItemScrollOffset
        var firstItem = visibleItems.first()

        // even if we compose items to fill before content padding we should ignore items fully
        // located there for the state's scroll position calculation (first item + first offset)
        if (beforeContentPadding > 0 || spaceBetweenItems < 0) {
            for (i in visibleItems.indices) {
                val size = visibleItems[i].mainAxisSizeWithSpacings
                if (currentFirstItemScrollOffset != 0 && size <= currentFirstItemScrollOffset &&
                    i != visibleItems.lastIndex
                ) {
                    currentFirstItemScrollOffset -= size
                    firstItem = visibleItems[i + 1]
                } else {
                    break
                }
            }
        }

        // Compose extra items before
        val extraItemsBefore = createItemsBeforeList(
            currentFirstItemIndex = currentFirstItemIndex,
            measuredItemProvider = measuredItemProvider,
            beyondBoundsItemCount = beyondBoundsItemCount,
            pinnedItems = pinnedItems
        )

        // Update maxCrossAxis with extra items
        extraItemsBefore.fastForEach {
            maxCrossAxis = maxOf(maxCrossAxis, it.crossAxisSize)
        }

        // Compose items after last item
        val extraItemsAfter = createItemsAfterList(
            visibleItems = visibleItems,
            measuredItemProvider = measuredItemProvider,
            itemsCount = itemsCount,
            beyondBoundsItemCount = beyondBoundsItemCount,
            pinnedItems = pinnedItems,
            consumedScroll = consumedScroll,
            isLookingAhead = isLookingAhead,
            lastPostLookaheadLayoutInfo = postLookaheadLayoutInfo
        )

        // Update maxCrossAxis with extra items
        extraItemsAfter.fastForEach {
            maxCrossAxis = maxOf(maxCrossAxis, it.crossAxisSize)
        }

        val noExtraItems = firstItem == visibleItems.first() &&
            extraItemsBefore.isEmpty() &&
            extraItemsAfter.isEmpty()

        var layoutWidth =
            constraints.constrainWidth(if (isVertical) maxCrossAxis else currentMainAxisOffset)
        var layoutHeight =
            constraints.constrainHeight(if (isVertical) currentMainAxisOffset else maxCrossAxis)

        val positionedItems = calculateItemsOffsets(
            items = visibleItems,
            extraItemsBefore = extraItemsBefore,
            extraItemsAfter = extraItemsAfter,
            layoutWidth = layoutWidth,
            layoutHeight = layoutHeight,
            finalMainAxisOffset = currentMainAxisOffset,
            maxOffset = maxOffset,
            itemsScrollOffset = visibleItemsScrollOffset,
            isVertical = isVertical,
            verticalArrangement = verticalArrangement,
            horizontalArrangement = horizontalArrangement,
            reverseLayout = reverseLayout,
            density = density,
        )

//        itemAnimator.onMeasured(
//            consumedScroll = consumedScroll.toInt(),
//            layoutWidth = layoutWidth,
//            layoutHeight = layoutHeight,
//            positionedItems = positionedItems,
//            keyIndexMap = measuredItemProvider.keyIndexMap,
//            itemProvider = measuredItemProvider,
//            isVertical = isVertical,
//            laneCount = 1,
//            isLookingAhead = isLookingAhead,
//            hasLookaheadOccurred = hasLookaheadPassOccurred,
//            coroutineScope = coroutineScope,
//            layoutMinOffset = currentFirstItemScrollOffset,
//            layoutMaxOffset = currentMainAxisOffset,
////            graphicsContext = graphicsContext
//        )

//        if (!isLookingAhead) {
//            val disappearingItemsSize = itemAnimator.minSizeToFitDisappearingItems
//            if (disappearingItemsSize != IntSize.Zero) {
//                val oldMainAxisSize = if (isVertical) layoutHeight else layoutWidth
//                layoutWidth =
//                    constraints.constrainWidth(maxOf(layoutWidth, disappearingItemsSize.width))
//                layoutHeight =
//                    constraints.constrainHeight(maxOf(layoutHeight, disappearingItemsSize.height))
//                val newMainAxisSize = if (isVertical) layoutHeight else layoutWidth
//                if (newMainAxisSize != oldMainAxisSize) {
//                    positionedItems.fastForEach {
//                        it.updateMainAxisLayoutSize(newMainAxisSize)
//                    }
//                }
//            }
//        }

        val stickingItems = stickyItemsPlacement.applyStickyItems(
            visibleItems,
            measuredItemProvider.headerIndexes,
            beforeContentPadding,
            afterContentPadding,
            layoutWidth,
            layoutHeight
        ) {
            measuredItemProvider.getAndMeasure(it)
        }
        val headerItem = stickingItems.lastOrNull()

        return LazyListMeasureResult(
            firstVisibleItem = firstItem,
            firstVisibleItemScrollOffset = currentFirstItemScrollOffset,
            canScrollForward = index < itemsCount || currentMainAxisOffset > maxOffset,
            consumedScroll = consumedScroll,
            stickyItem = headerItem,
            measureResult = layout(layoutWidth, layoutHeight) {
                positionedItems.fastForEach {
                    if (it.key != headerItem?.key) {
                        it.place(this, isLookingAhead)
                    }
                }
                // the header item should be placed (drawn) after all other items
                headerItem?.place(this, isLookingAhead)
                // we attach it during the placement so LazyListState can trigger re-placement
                placementScopeInvalidator.attachToScope()
            },
            scrollBackAmount = scrollBackAmount,
            visibleItemsInfo = if (noExtraItems) positionedItems else positionedItems.fastFilter {
                (it.index >= visibleItems.first().index && it.index <= visibleItems.last().index) ||
                    it === headerItem
            },
            positionedItems = positionedItems,
            viewportStartOffset = -beforeContentPadding,
            viewportEndOffset = maxOffset + afterContentPadding,
            totalItemsCount = itemsCount,
            reverseLayout = reverseLayout,
            orientation = if (isVertical) Orientation.Vertical else Orientation.Horizontal,
            afterContentPadding = afterContentPadding,
            mainAxisItemSpacing = spaceBetweenItems,
            remeasureNeeded = remeasureNeeded,
            coroutineScope = coroutineScope,
            density = density,
            childConstraints = measuredItemProvider.childConstraints
        )
    }
}

private fun createItemsAfterList(
    visibleItems: MutableList<LazyListMeasuredItem>,
    measuredItemProvider: LazyListMeasuredItemProvider,
    itemsCount: Int,
    beyondBoundsItemCount: Int,
    pinnedItems: List<Int>,
    consumedScroll: Float,
    isLookingAhead: Boolean,
    lastPostLookaheadLayoutInfo: LazyListLayoutInfo?
): List<LazyListMeasuredItem> {
    var list: MutableList<LazyListMeasuredItem>? = null

    var end = visibleItems.last().index

    end = minOf(end + beyondBoundsItemCount, itemsCount - 1)

    for (i in visibleItems.last().index + 1..end) {
        if (list == null) list = mutableListOf()
        list.add(measuredItemProvider.getAndMeasure(i))
    }

    if (isLookingAhead) {
        // Check if there's any item that needs to be composed based on last postLookaheadLayoutInfo
        if (lastPostLookaheadLayoutInfo != null &&
            lastPostLookaheadLayoutInfo.visibleItemsInfo.isNotEmpty()
        ) {
            // Find first item with index > end. Note that `visibleItemsInfo.last()` may not have
            // the largest index as the last few items could be added to animate item placement.
            val firstItem = lastPostLookaheadLayoutInfo.visibleItemsInfo.run {
                var found: LazyListItemInfo? = null
                for (i in size - 1 downTo 0) {
                    if (this[i].index > end && (i == 0 || this[i - 1].index <= end)) {
                        found = this[i]
                        break
                    }
                }
                found
            }
            val lastVisibleItem = lastPostLookaheadLayoutInfo.visibleItemsInfo.last()
            if (firstItem != null) {
                for (i in firstItem.index..min(lastVisibleItem.index, itemsCount - 1)) {
                    // Only add to the list items that are _not_ already in the list.
                    if (list?.fastFirstOrNull { it.index == i } == null) {
                        if (list == null) list = mutableListOf()
                        list.add(measuredItemProvider.getAndMeasure(i))
                    }
                }
            }

            // Calculate the additional offset to subcompose based on what was shown in the
            // previous post-loookahead pass and the scroll consumed.
            val additionalOffset =
                lastPostLookaheadLayoutInfo.viewportEndOffset - lastVisibleItem.offset -
                    lastVisibleItem.size - consumedScroll
            if (additionalOffset > 0) {
                var index = lastVisibleItem.index + 1
                var totalOffset = 0
                while (index < itemsCount && totalOffset < additionalOffset) {
                    val item = if (index <= end) {
                        visibleItems.fastFirstOrNull { it.index == index }
                    } else null
                        ?: list?.fastFirstOrNull { it.index == index }
                    if (item != null) {
                        index++
                        totalOffset += item.mainAxisSizeWithSpacings
                    } else {
                        if (list == null) list = mutableListOf()
                        list.add(measuredItemProvider.getAndMeasure(index))
                        index++
                        totalOffset += list.last().mainAxisSizeWithSpacings
                    }
                }
            }
        }
    }

    // The list contains monotonically increasing indices.
    list?.let {
        if (it.last().index > end) {
            end = it.last().index
        }
    }
    pinnedItems.fastForEach { index ->
        if (index > end) {
            if (list == null) list = mutableListOf()
            list?.add(measuredItemProvider.getAndMeasure(index))
        }
    }

    return list ?: emptyList()
}

private fun createItemsBeforeList(
    currentFirstItemIndex: Int,
    measuredItemProvider: LazyListMeasuredItemProvider,
    beyondBoundsItemCount: Int,
    pinnedItems: List<Int>
): List<LazyListMeasuredItem> {
    var list: MutableList<LazyListMeasuredItem>? = null

    var start = currentFirstItemIndex

    start = maxOf(0, start - beyondBoundsItemCount)

    for (i in currentFirstItemIndex - 1 downTo start) {
        if (list == null) list = mutableListOf()
        list.add(measuredItemProvider.getAndMeasure(i))
    }

    pinnedItems.fastForEachReversed { index ->
        if (index < start) {
            if (list == null) list = mutableListOf()
            list?.add(measuredItemProvider.getAndMeasure(index))
        }
    }

    return list ?: emptyList()
}

/**
 * Calculates [LazyListMeasuredItem]s offsets.
 */
private fun calculateItemsOffsets(
    items: List<LazyListMeasuredItem>,
    extraItemsBefore: List<LazyListMeasuredItem>,
    extraItemsAfter: List<LazyListMeasuredItem>,
    layoutWidth: Int,
    layoutHeight: Int,
    finalMainAxisOffset: Int,
    maxOffset: Int,
    itemsScrollOffset: Int,
    isVertical: Boolean,
    verticalArrangement: Arrangement.Vertical?,
    horizontalArrangement: Arrangement.Horizontal?,
    reverseLayout: Boolean,
    density: Density,
): MutableList<LazyListMeasuredItem> {
    val mainAxisLayoutSize = if (isVertical) layoutHeight else layoutWidth
    val hasSpareSpace = finalMainAxisOffset < minOf(mainAxisLayoutSize, maxOffset)
    if (hasSpareSpace) {
        check(itemsScrollOffset == 0) { "non-zero itemsScrollOffset" }
    }

    val positionedItems =
        ArrayList<LazyListMeasuredItem>(items.size + extraItemsBefore.size + extraItemsAfter.size)

    if (hasSpareSpace) {
        require(extraItemsBefore.isEmpty() && extraItemsAfter.isEmpty()) { "no extra items" }

        val itemsCount = items.size
        fun Int.reverseAware() =
            if (!reverseLayout) this else itemsCount - this - 1

        val sizes = IntArray(itemsCount) { index ->
            items[index.reverseAware()].size
        }
        val offsets = IntArray(itemsCount) { 0 }
        if (isVertical) {
            with(
                requireNotNull(verticalArrangement) {
                    "null verticalArrangement when isVertical == true"
                }
            ) {
                density.arrange(mainAxisLayoutSize, sizes, offsets)
            }
        } else {
            with(
                requireNotNull(horizontalArrangement) {
                    "null horizontalArrangement when isVertical == false"
                }
            ) {
                // Enforces Ltr layout direction as it is mirrored with placeRelative later.
                density.arrange(mainAxisLayoutSize, sizes, LayoutDirection.Ltr, offsets)
            }
        }

        val reverseAwareOffsetIndices =
            if (!reverseLayout) offsets.indices else offsets.indices.reversed()
        for (index in reverseAwareOffsetIndices) {
            val absoluteOffset = offsets[index]
            // when reverseLayout == true, offsets are stored in the reversed order to items
            val item = items[index.reverseAware()]
            val relativeOffset = if (reverseLayout) {
                // inverse offset to align with scroll direction for positioning
                mainAxisLayoutSize - absoluteOffset - item.size
            } else {
                absoluteOffset
            }
            item.position(relativeOffset, layoutWidth, layoutHeight)
            positionedItems.add(item)
        }
    } else {
        var currentMainAxis = itemsScrollOffset
        extraItemsBefore.fastForEach {
            currentMainAxis -= it.mainAxisSizeWithSpacings
            it.position(currentMainAxis, layoutWidth, layoutHeight)
            positionedItems.add(it)
        }

        currentMainAxis = itemsScrollOffset
        items.fastForEach {
            it.position(currentMainAxis, layoutWidth, layoutHeight)
            positionedItems.add(it)
            currentMainAxis += it.mainAxisSizeWithSpacings
        }

        extraItemsAfter.fastForEach {
            it.position(currentMainAxis, layoutWidth, layoutHeight)
            positionedItems.add(it)
            currentMainAxis += it.mainAxisSizeWithSpacings
        }
    }
    return positionedItems
}
