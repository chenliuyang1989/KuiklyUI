/*
 * Copyright 2022 The Android Open Source Project
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

package com.tencent.kuikly.compose.foundation.text

import androidx.compose.runtime.Composable
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.ui.layout.Layout
import com.tencent.kuikly.compose.ui.text.AnnotatedString
import com.tencent.kuikly.compose.ui.text.Placeholder
import com.tencent.kuikly.compose.ui.util.fastForEach
import com.tencent.kuikly.compose.ui.util.fastMap

internal typealias PlaceholderRange = AnnotatedString.Range<Placeholder>
internal typealias InlineContentRange = AnnotatedString.Range<@Composable (String) -> Unit>

/**
 * Attempts to match AnnotatedString placeholders with passed [InlineTextContent]
 *
 * Matches will produce a entry in both returned lists.
 *
 * Non-matches will be ignored silently.
 */
internal fun AnnotatedString.resolveInlineContent(
    inlineContent: Map<String, InlineTextContent>?
): Pair<List<PlaceholderRange>, List<InlineContentRange>> {
    if (inlineContent.isNullOrEmpty()) {
        return EmptyInlineContent
    }
    val inlineContentAnnotations = getStringAnnotations(INLINE_CONTENT_TAG, 0, text.length)

    val placeholders = mutableListOf<AnnotatedString.Range<Placeholder>>()
    val inlineComposables = mutableListOf<AnnotatedString.Range<@Composable (String) -> Unit>>()
    inlineContentAnnotations.fastForEach { annotation ->
        inlineContent[annotation.item]?.let { inlineTextContent ->
            placeholders.add(
                AnnotatedString.Range(
                    inlineTextContent.placeholder,
                    annotation.start,
                    annotation.end
                )
            )
            inlineComposables.add(
                AnnotatedString.Range(
                    inlineTextContent.children,
                    annotation.start,
                    annotation.end
                )
            )
        }
    }
    return Pair(placeholders, inlineComposables)
}

internal fun AnnotatedString.hasInlineContent(): Boolean =
    hasStringAnnotations(INLINE_CONTENT_TAG, 0, text.length)

@Composable
internal fun InlineChildren(
    text: AnnotatedString,
    inlineContents: List<InlineContentRange>
) {
    inlineContents.fastForEach { (content, start, end) ->
        Box {
            content(text.subSequence(start, end).text)
        }
    }
}

private val EmptyInlineContent: Pair<List<PlaceholderRange>, List<InlineContentRange>> =
    Pair(emptyList(), emptyList())
