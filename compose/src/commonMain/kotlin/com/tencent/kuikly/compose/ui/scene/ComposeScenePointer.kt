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

package com.tencent.kuikly.compose.ui.scene

import com.tencent.kuikly.compose.ui.ExperimentalComposeUiApi
import com.tencent.kuikly.compose.ui.geometry.Offset
import com.tencent.kuikly.compose.ui.input.pointer.HistoricalChange
import com.tencent.kuikly.compose.ui.input.pointer.PointerButton
import com.tencent.kuikly.compose.ui.input.pointer.PointerEventType
import com.tencent.kuikly.compose.ui.input.pointer.PointerId
import com.tencent.kuikly.compose.ui.input.pointer.PointerInputEventData
import com.tencent.kuikly.compose.ui.input.pointer.PointerType
import com.tencent.kuikly.compose.ui.node.InternalCoreApi
import com.tencent.kuikly.compose.ui.node.LayoutNode

/**
 * Represents pointer such as mouse cursor, or touch/stylus press.
 * There can be multiple pointers on the screen at the same time.
 */
@ExperimentalComposeUiApi
class ComposeScenePointer(
    /**
     * Unique id associated with the pointer. Used to distinguish between multiple pointers that can exist
     * at the same time (i.e. multiple pressed touches).
     */
    val id: PointerId,
    /**
     * The [Offset] of the pointer.
     */
    val position: Offset,
    /**
     * `true` if the pointer event is considered "pressed". For example,
     * a finger touches the screen or any mouse button is pressed.
     *  During the up event, pointer is considered not pressed.
     */
    val pressed: Boolean,
    /**
     * The device type associated with the pointer, such as [mouse][PointerType.Mouse],
     * or [touch][PointerType.Touch].
     */
    val type: PointerType = PointerType.Mouse,
    /**
     * Pressure of the pointer. 0.0 - no pressure, 1.0 - average pressure
     */
    val pressure: Float = 1.0f,
    /**
     * High-frequency pointer moves in between the current event and the last event.
     * can be used for extra accuracy when touchscreen rate exceeds framerate.
     *
     * Can be empty, if a platform doesn't provide any.
     *
     * For example, on iOS this list is populated using the data of.
     * https://developer.apple.com/documentation/uikit/uievent/1613808-coalescedtouchesfortouch?language=objc
     */
    val historical: List<HistoricalChange> = emptyList(),
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as ComposeScenePointer

        if (position != other.position) return false
        if (pressed != other.pressed) return false
        if (type != other.type) return false
        if (id != other.id) return false
        if (pressure != other.pressure) return false

        return true
    }

    override fun hashCode(): Int {
        var result = position.hashCode()
        result = 31 * result + pressed.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + id.hashCode()
        result = 31 * result + pressure.hashCode()
        return result
    }

    override fun toString(): String = "Pointer(position=$position, pressed=$pressed, type=$type, id=$id, pressure=$pressure)"
}

@OptIn(ExperimentalComposeUiApi::class, InternalCoreApi::class)
internal fun PointerInputEvent(
    eventType: PointerEventType,
    pointers: List<ComposeScenePointer>,
    timeMillis: Long,
    nativeEvent: Any?,
    scrollDelta: Offset,
    changedButton: PointerButton?,
    rootNode: LayoutNode?,
) = com.tencent.kuikly.compose.ui.input.pointer.PointerInputEvent(
    eventType,
    timeMillis,
    pointers.map {
        PointerInputEventData(
            id = it.id,
            uptime = timeMillis,
            positionOnScreen = it.position,
            position = it.position,
            down = it.pressed,
            pressure = it.pressure,
            type = it.type,
            issuesEnterExit = it.type == PointerType.Mouse,
            historical = it.historical,
            scrollDelta = scrollDelta,
            originalEventPosition = it.position,
        )
    },
    nativeEvent,
    changedButton,
    rootNode,
)
