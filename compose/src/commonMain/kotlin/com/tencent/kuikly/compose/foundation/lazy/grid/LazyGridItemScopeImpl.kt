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

package com.tencent.kuikly.compose.foundation.lazy.grid

import com.tencent.kuikly.compose.animation.core.FiniteAnimationSpec
import com.tencent.kuikly.compose.foundation.lazy.layout.LazyLayoutAnimateItemElement
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.unit.IntOffset

internal object LazyGridItemScopeImpl : LazyGridItemScope {
    override fun Modifier.animateItem(
        fadeInSpec: FiniteAnimationSpec<Float>?,
        placementSpec: FiniteAnimationSpec<IntOffset>?,
        fadeOutSpec: FiniteAnimationSpec<Float>?
    ): Modifier =
        if (fadeInSpec == null && placementSpec == null && fadeOutSpec == null) {
            this
        } else {
            this then LazyLayoutAnimateItemElement(
                fadeInSpec,
                placementSpec,
                fadeOutSpec
            )
        }
}
