/*
 * Copyright 2021 The Android Open Source Project
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

package com.tencent.kuikly.compose.ui.node

import androidx.compose.runtime.CompositionLocalMap
import com.tencent.kuikly.compose.ui.ExperimentalComposeUiApi
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.layout.MeasurePolicy
import com.tencent.kuikly.compose.ui.platform.ViewConfiguration
import com.tencent.kuikly.compose.ui.unit.Density
import com.tencent.kuikly.compose.ui.unit.LayoutDirection
import com.tencent.kuikly.compose.ui.node.LayoutNode
import com.tencent.kuikly.compose.ui.node.KNode

/**
 * Interface extracted from LayoutNode to not mark the whole LayoutNode class as @PublishedApi.
 */
@PublishedApi
internal interface ComposeUiNode {
    var measurePolicy: MeasurePolicy
    var layoutDirection: LayoutDirection
    var density: Density
    var modifier: Modifier
    var viewConfiguration: ViewConfiguration
    var compositionLocalMap: CompositionLocalMap
    @ExperimentalComposeUiApi
    var compositeKeyHash: Int

    /**
     * Object of pre-allocated lambdas used to make use with ComposeNode allocation-less.
     */
    companion object {
        val LayoutConstructor: () -> KNode<*> = KNode.LayoutConstructor
        val ShadowLayoutConstructor: (Boolean) -> (() -> KNode<*>) = KNode.ShadowLayoutConstructor
        val SetModifier: ComposeUiNode.(Modifier) -> Unit = { this.modifier = it }
        val SetDensity: ComposeUiNode.(Density) -> Unit = { this.density = it }
        val SetResolvedCompositionLocals: ComposeUiNode.(CompositionLocalMap) -> Unit =
            { this.compositionLocalMap = it }
        val SetMeasurePolicy: ComposeUiNode.(MeasurePolicy) -> Unit =
            { this.measurePolicy = it }
        val SetLayoutDirection: ComposeUiNode.(LayoutDirection) -> Unit =
            { this.layoutDirection = it }
        val SetViewConfiguration: ComposeUiNode.(ViewConfiguration) -> Unit =
            { this.viewConfiguration = it }
        @get:ExperimentalComposeUiApi
        @Suppress("OPT_IN_MARKER_ON_WRONG_TARGET")
        @ExperimentalComposeUiApi
        val SetCompositeKeyHash: ComposeUiNode.(Int) -> Unit =
            { this.compositeKeyHash = it }
    }
}
