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

package com.tencent.kuikly.compose.foundation.layout

import androidx.annotation.FloatRange
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.layout.IntrinsicMeasurable
import com.tencent.kuikly.compose.ui.layout.IntrinsicMeasureScope
import com.tencent.kuikly.compose.ui.layout.Layout
import com.tencent.kuikly.compose.ui.layout.Measurable
import com.tencent.kuikly.compose.ui.layout.MeasurePolicy
import com.tencent.kuikly.compose.ui.layout.MeasureResult
import com.tencent.kuikly.compose.ui.layout.MeasureScope
import com.tencent.kuikly.compose.ui.layout.Measured
import com.tencent.kuikly.compose.ui.layout.Placeable
import com.tencent.kuikly.compose.ui.layout.VerticalAlignmentLine
import com.tencent.kuikly.compose.ui.unit.Constraints
import com.tencent.kuikly.compose.ui.unit.LayoutDirection

/**
 * A layout composable that places its children in a vertical sequence. For a layout composable
 * that places its children in a horizontal sequence, see [Row]. Note that by default items do
 * not scroll; see `Modifier.verticalScroll` to add this behavior. For a vertically
 * scrollable list that only composes and lays out the currently visible items see `LazyColumn`.
 *
 * The [Column] layout is able to assign children heights according to their weights provided
 * using the [ColumnScope.weight] modifier. If a child is not provided a weight, it will be
 * asked for its preferred height before the sizes of the children with weights are calculated
 * proportionally to their weight based on the remaining available space. Note that if the
 * [Column] is vertically scrollable or part of a vertically scrollable container, any provided
 * weights will be disregarded as the remaining available space will be infinite.
 *
 * When none of its children have weights, a [Column] will be as small as possible to fit its
 * children one on top of the other. In order to change the height of the [Column], use the
 * [Modifier.height] modifiers; e.g. to make it fill the available height [Modifier.fillMaxHeight]
 * can be used. If at least one child of a [Column] has a [weight][ColumnScope.weight], the [Column]
 * will fill the available height, so there is no need for [Modifier.fillMaxHeight]. However, if
 * [Column]'s size should be limited, the [Modifier.height] or [Modifier.size] layout modifiers
 * should be applied.
 *
 * When the size of the [Column] is larger than the sum of its children sizes, a
 * [verticalArrangement] can be specified to define the positioning of the children inside the
 * [Column]. See [Arrangement] for available positioning behaviors; a custom arrangement can also
 * be defined using the constructor of [Arrangement]. Below is an illustration of different
 * vertical arrangements:
 *
 * ![Column arrangements](https://developer.android.com/images/reference/androidx/compose/foundation/layout/column_arrangement_visualization.gif)
 *
 * Example usage:
 *
 * @sample androidx.compose.foundation.layout.samples.SimpleColumn
 *
 * @param modifier The modifier to be applied to the Column.
 * @param verticalArrangement The vertical arrangement of the layout's children.
 * @param horizontalAlignment The horizontal alignment of the layout's children.
 *
 * @see Row
 * @see [androidx.compose.foundation.lazy.LazyColumn]
 */
@Composable
inline fun Column(
    modifier: Modifier = Modifier,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    content: @Composable ColumnScope.() -> Unit
) {
    val measurePolicy = columnMeasurePolicy(verticalArrangement, horizontalAlignment)
    Layout(
        content = { ColumnScopeInstance.content() },
        measurePolicy = measurePolicy,
        modifier = modifier
    )
}

@PublishedApi
internal val DefaultColumnMeasurePolicy: MeasurePolicy = ColumnMeasurePolicy(
    verticalArrangement = Arrangement.Top,
    horizontalAlignment = Alignment.Start,
)

@PublishedApi
@Composable
internal fun columnMeasurePolicy(
    verticalArrangement: Arrangement.Vertical,
    horizontalAlignment: Alignment.Horizontal
): MeasurePolicy =
    if (verticalArrangement == Arrangement.Top && horizontalAlignment == Alignment.Start) {
        DefaultColumnMeasurePolicy
    } else {
        remember(verticalArrangement, horizontalAlignment) {
            ColumnMeasurePolicy(
                verticalArrangement = verticalArrangement,
                horizontalAlignment = horizontalAlignment,
            )
        }
    }

internal data class ColumnMeasurePolicy(
    private val verticalArrangement: Arrangement.Vertical,
    private val horizontalAlignment: Alignment.Horizontal
) : MeasurePolicy, RowColumnMeasurePolicy {

    override fun Placeable.mainAxisSize(): Int = height
    override fun Placeable.crossAxisSize(): Int = width
    override fun populateMainAxisPositions(
        mainAxisLayoutSize: Int,
        childrenMainAxisSize: IntArray,
        mainAxisPositions: IntArray,
        measureScope: MeasureScope
    ) {
        with(verticalArrangement) {
            measureScope.arrange(
                mainAxisLayoutSize,
                childrenMainAxisSize,
                mainAxisPositions
            )
        }
    }

    override fun placeHelper(
        placeables: Array<Placeable?>,
        measureScope: MeasureScope,
        beforeCrossAxisAlignmentLine: Int,
        mainAxisPositions: IntArray,
        mainAxisLayoutSize: Int,
        crossAxisLayoutSize: Int,
        crossAxisOffset: IntArray?,
        currentLineIndex: Int,
        startIndex: Int,
        endIndex: Int
    ): MeasureResult {
        return with(measureScope) {
            layout(crossAxisLayoutSize, mainAxisLayoutSize) {
                placeables.forEachIndexed { i, placeable ->
                    val crossAxisPosition = getCrossAxisPosition(
                        placeable!!,
                        placeable.rowColumnParentData,
                        crossAxisLayoutSize,
                        beforeCrossAxisAlignmentLine,
                        measureScope.layoutDirection
                    )
                    placeable.place(
                        crossAxisPosition,
                        mainAxisPositions[i],
                 )
                }
            }
        }
    }

    private fun getCrossAxisPosition(
        placeable: Placeable,
        parentData: RowColumnParentData?,
        crossAxisLayoutSize: Int,
        beforeCrossAxisAlignmentLine: Int,
        layoutDirection: LayoutDirection
    ): Int {
        val childCrossAlignment = parentData?.crossAxisAlignment
        return childCrossAlignment?.align(
            size = crossAxisLayoutSize - placeable.width,
            layoutDirection = layoutDirection,
            placeable = placeable,
            beforeCrossAxisAlignmentLine = beforeCrossAxisAlignmentLine
        ) ?: horizontalAlignment.align(0, crossAxisLayoutSize - placeable.width,
            layoutDirection)
    }

    override fun createConstraints(
        mainAxisMin: Int,
        crossAxisMin: Int,
        mainAxisMax: Int,
        crossAxisMax: Int,
        isPrioritizing: Boolean
    ): Constraints {
        return createColumnConstraints(
            isPrioritizing,
            mainAxisMin,
            crossAxisMin,
            mainAxisMax,
            crossAxisMax
        )
    }

    override fun MeasureScope.measure(
        measurables: List<Measurable>,
        constraints: Constraints
    ): MeasureResult {
        return measure(
            constraints.minHeight,
            constraints.minWidth,
            constraints.maxHeight,
            constraints.maxWidth,
            verticalArrangement.spacing.roundToPx(),
            this,
            measurables,
            arrayOfNulls(measurables.size),
            0,
            measurables.size
        )
    }

    override fun IntrinsicMeasureScope.minIntrinsicWidth(
        measurables: List<IntrinsicMeasurable>,
        height: Int
    ) = IntrinsicMeasureBlocks.VerticalMinWidth(
        measurables,
        height,
        verticalArrangement.spacing.roundToPx(),
    )

    override fun IntrinsicMeasureScope.minIntrinsicHeight(
        measurables: List<IntrinsicMeasurable>,
        width: Int
    ) = IntrinsicMeasureBlocks.VerticalMinHeight(
        measurables,
        width,
        verticalArrangement.spacing.roundToPx(),
    )

    override fun IntrinsicMeasureScope.maxIntrinsicWidth(
        measurables: List<IntrinsicMeasurable>,
        height: Int
    ) = IntrinsicMeasureBlocks.VerticalMaxWidth(
        measurables,
        height,
        verticalArrangement.spacing.roundToPx(),
    )

    override fun IntrinsicMeasureScope.maxIntrinsicHeight(
        measurables: List<IntrinsicMeasurable>,
        width: Int
    ) = IntrinsicMeasureBlocks.VerticalMaxHeight(
        measurables,
        width,
        verticalArrangement.spacing.roundToPx(),
    )
}

internal fun createColumnConstraints(
    isPrioritizing: Boolean,
    mainAxisMin: Int,
    crossAxisMin: Int,
    mainAxisMax: Int,
    crossAxisMax: Int,
): Constraints {
    return if (!isPrioritizing) {
        Constraints(
            minHeight = mainAxisMin,
            minWidth = crossAxisMin,
            maxHeight = mainAxisMax,
            maxWidth = crossAxisMax
        )
    } else {
        Constraints.fitPrioritizingHeight(
            minHeight = mainAxisMin,
            minWidth = crossAxisMin,
            maxHeight = mainAxisMax,
            maxWidth = crossAxisMax
        )
    }
}

/**
 * Scope for the children of [Column].
 */
@LayoutScopeMarker
@Immutable
interface ColumnScope {
    /**
     * Size the element's height proportional to its [weight] relative to other weighted sibling
     * elements in the [Column]. The parent will divide the vertical space remaining after measuring
     * unweighted child elements and distribute it according to this weight.
     * When [fill] is true, the element will be forced to occupy the whole height allocated to it.
     * Otherwise, the element is allowed to be smaller - this will result in [Column] being smaller,
     * as the unused allocated height will not be redistributed to other siblings.
     *
     * In a [FlowColumn], when a weight is applied to an item, the item is scaled based on
     * the number of weighted items that fall on the column it was placed in.
     *
     * @param weight The proportional height to give to this element, as related to the total of
     * all weighted siblings. Must be positive.
     * @param fill When `true`, the element will occupy the whole height allocated.
     *
     * @sample androidx.compose.foundation.layout.samples.SimpleColumn
     */
    @Stable
    fun Modifier.weight(
        @FloatRange(from = 0.0, fromInclusive = false)
        weight: Float,
        fill: Boolean = true
    ): Modifier

    /**
     * Align the element horizontally within the [Column]. This alignment will have priority over
     * the [Column]'s `horizontalAlignment` parameter.
     *
     * Example usage:
     * @sample androidx.compose.foundation.layout.samples.SimpleAlignInColumn
     */
    @Stable
    fun Modifier.align(alignment: Alignment.Horizontal): Modifier

    /**
     * Position the element horizontally such that its [alignmentLine] aligns with sibling elements
     * also configured to [alignBy]. [alignBy] is a form of [align],
     * so both modifiers will not work together if specified for the same layout.
     * Within a [Column], all components with [alignBy] will align horizontally using
     * the specified [VerticalAlignmentLine]s or values provided using the other
     * [alignBy] overload, forming a sibling group.
     * At least one element of the sibling group will be placed as it had [Alignment.Start] align
     * in [Column], and the alignment of the other siblings will be then determined such that
     * the alignment lines coincide. Note that if only one element in a [Column] has the
     * [alignBy] modifier specified the element will be positioned
     * as if it had [Alignment.Start] align.
     *
     * Example usage:
     * @sample androidx.compose.foundation.layout.samples.SimpleRelativeToSiblingsInColumn
     */
    @Stable
    fun Modifier.alignBy(alignmentLine: VerticalAlignmentLine): Modifier

    /**
     * Position the element horizontally such that the alignment line for the content as
     * determined by [alignmentLineBlock] aligns with sibling elements also configured to
     * [alignBy]. [alignBy] is a form of [align], so both modifiers
     * will not work together if specified for the same layout.
     * Within a [Column], all components with [alignBy] will align horizontally using
     * the specified [VerticalAlignmentLine]s or values obtained from [alignmentLineBlock],
     * forming a sibling group.
     * At least one element of the sibling group will be placed as it had [Alignment.Start] align
     * in [Column], and the alignment of the other siblings will be then determined such that
     * the alignment lines coincide. Note that if only one element in a [Column] has the
     * [alignBy] modifier specified the element will be positioned
     * as if it had [Alignment.Start] align.
     *
     * Example usage:
     * @sample androidx.compose.foundation.layout.samples.SimpleRelativeToSiblings
     */
    @Stable
    fun Modifier.alignBy(alignmentLineBlock: (Measured) -> Int): Modifier
}

internal object ColumnScopeInstance : ColumnScope {
    @Stable
    override fun Modifier.weight(weight: Float, fill: Boolean): Modifier {
        require(weight > 0.0) { "invalid weight $weight; must be greater than zero" }
        return this.then(
            LayoutWeightElement(
                // Coerce Float.POSITIVE_INFINITY to Float.MAX_VALUE to avoid errors
                weight = weight.coerceAtMost(Float.MAX_VALUE),
                fill = fill
            )
        )
    }

    @Stable
    override fun Modifier.align(alignment: Alignment.Horizontal) = this.then(
        HorizontalAlignElement(
            horizontal = alignment
        )
    )

    @Stable
    override fun Modifier.alignBy(alignmentLine: VerticalAlignmentLine) = this.then(
        WithAlignmentLineElement(
            alignmentLine = alignmentLine
        )
    )

    @Stable
    override fun Modifier.alignBy(alignmentLineBlock: (Measured) -> Int) = this.then(
        WithAlignmentLineBlockElement(
            block = alignmentLineBlock
        )
    )
}
