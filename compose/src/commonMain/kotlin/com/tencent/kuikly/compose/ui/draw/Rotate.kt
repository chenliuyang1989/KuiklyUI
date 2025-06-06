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

package com.tencent.kuikly.compose.ui.draw

import androidx.compose.runtime.Stable
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.graphics.graphicsLayer

/**
 * Sets the degrees the view is rotated around the center of the composable. Increasing values
 * result in clockwise rotation. Negative degrees are used to rotate in the counter clockwise
 * direction
 *
 * Usage of this API renders this composable into a separate graphics layer.
 *
 * @sample androidx.compose.ui.samples.RotateSample
 * @see graphicsLayer
 *
 * Example usage:
 */
@Stable
fun Modifier.rotate(degrees: Float) =
    if (degrees != 0f) graphicsLayer(rotationZ = degrees) else this
