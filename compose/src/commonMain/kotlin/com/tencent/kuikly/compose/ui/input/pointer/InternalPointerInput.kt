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

package com.tencent.kuikly.compose.ui.input.pointer

import androidx.collection.LongSparseArray
import com.tencent.kuikly.compose.ui.ExperimentalComposeUiApi
import com.tencent.kuikly.compose.ui.geometry.Offset
import com.tencent.kuikly.compose.ui.node.LayoutNode


/**
 * The normalized data structure for pointer input event information that is taken in processed by
 * Compose (via the [PointerInputEventProcessor]).
 *
 * All pointer locations are relative to the device screen.
 */
//@InternalCoreApi
//internal expect class PointerInputEvent {
//    val uptime: Long
//    val pointers: List<PointerInputEventData>
//}
// todo: jonas
internal data class PointerInputEvent(
    val eventType: PointerEventType,
    val uptime: Long,
    val pointers: List<PointerInputEventData>,
//    val buttons: PointerButtons = PointerButtons(0),
//    val keyboardModifiers: PointerKeyboardModifiers = PointerKeyboardModifiers(0),
    val nativeEvent: Any? = null,
    val button: PointerButton? = null,
    val rootNode: LayoutNode? = null
)

/**
 * Data that describes a particular pointer
 *
 * @param positionOnScreen The position of the event relative to the device screen.
 * @param position The position of the event relative to the owner.
 */
@OptIn(ExperimentalComposeUiApi::class)
internal data class PointerInputEventData(
    val id: PointerId,
    val uptime: Long,
    val positionOnScreen: Offset,
    val position: Offset,
    val down: Boolean,
    val pressure: Float,
    val type: PointerType,
    val issuesEnterExit: Boolean = false,
    val historical: List<HistoricalChange> = mutableListOf(),
    val scrollDelta: Offset = Offset.Zero,
    val originalEventPosition: Offset = Offset.Zero,
)

/**
 * Represents a pointer input event internally.
 *
 * [PointerInputChange]s are stored in a map so that as this internal event traverses the tree,
 * it is efficient to split the changes between those that are relevant to the sub tree and those
 * that are not.
 */
////@OptIn(InternalCoreApi::class)
//internal expect class InternalPointerEvent(
//    changes: LongSparseArray<PointerInputChange>,
//    pointerInputEvent: PointerInputEvent
//) {
//    val changes: LongSparseArray<PointerInputChange>
//
//    /**
//     * Embedded Android Views may consume an event and [ProcessResult] should not
//     * return that the position change was consumed because of this.
//     */
//    var suppressMovementConsumption: Boolean
//    fun issuesEnterExitEvent(pointerId: PointerId): Boolean
//}

internal class InternalPointerEvent(
    val type: PointerEventType,
    val changes: LongSparseArray<PointerInputChange>,
//    val buttons: PointerButtons,
//    val keyboardModifiers: PointerKeyboardModifiers,
    val nativeEvent: Any?,
    val button: PointerButton?
) {
    constructor(
        changes: LongSparseArray<PointerInputChange>,
        pointerInputEvent: PointerInputEvent
    ) : this(
        pointerInputEvent.eventType,
        changes,
//        pointerInputEvent.buttons,
//        pointerInputEvent.keyboardModifiers,
        pointerInputEvent.nativeEvent,
        pointerInputEvent.button
    )

    var suppressMovementConsumption: Boolean = false

    fun issuesEnterExitEvent(pointerId: PointerId): Boolean =
        changes[pointerId.value]?.type == PointerType.Mouse
}

