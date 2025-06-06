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

package com.tencent.kuikly.compose.ui.text

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import com.tencent.kuikly.compose.material3.tokens.TypeScaleTokens
import com.tencent.kuikly.compose.ui.graphics.Brush
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.graphics.Shadow
import com.tencent.kuikly.compose.ui.graphics.drawscope.DrawStyle
import com.tencent.kuikly.compose.ui.graphics.drawscope.Fill
import com.tencent.kuikly.compose.ui.graphics.isSpecified
import com.tencent.kuikly.compose.ui.graphics.lerp
import com.tencent.kuikly.compose.ui.graphics.takeOrElse
import com.tencent.kuikly.compose.ui.text.font.FontFamily
import com.tencent.kuikly.compose.ui.text.font.FontStyle
import com.tencent.kuikly.compose.ui.text.font.FontSynthesis
import com.tencent.kuikly.compose.ui.text.font.FontWeight
import com.tencent.kuikly.compose.ui.text.font.lerp
import com.tencent.kuikly.compose.ui.text.style.TextDecoration
import com.tencent.kuikly.compose.ui.text.style.TextForegroundStyle
import com.tencent.kuikly.compose.ui.text.style.lerp
import com.tencent.kuikly.compose.ui.unit.TextUnit
import com.tencent.kuikly.compose.ui.unit.isSpecified
import com.tencent.kuikly.compose.ui.unit.isUnspecified
import com.tencent.kuikly.compose.ui.unit.lerp
import com.tencent.kuikly.compose.ui.unit.sp

/** The default font size if none is specified. */
private val DefaultFontSize = 14.sp
private val DefaultLetterSpacing = 0.sp
private val DefaultBackgroundColor = Color.Unspecified
// TODO(nona): Introduce TextUnit.Original for representing "do not change the original result".
//  Need to distinguish from Inherit.
private val DefaultColor = Color.Black

/**
 * Styling configuration for a text span. This configuration only allows character level styling,
 * in order to set paragraph level styling such as line height, or text alignment please see
 * [ParagraphStyle].
 *
 * @sample com.tencent.kuikly.compose.ui.text.samples.SpanStyleSample
 *
 * @sample com.tencent.kuikly.compose.ui.text.samples.AnnotatedStringBuilderSample
 *
 * @param fontSize The size of glyphs (in logical pixels) to use when painting the text. This
 * may be [TextUnit.Unspecified] for inheriting from another [SpanStyle].
 * @param fontWeight The typeface thickness to use when painting the text (e.g., bold).
 * @param fontStyle The typeface variant to use when drawing the letters (e.g., italic).
 * @param fontSynthesis Whether to synthesize font weight and/or style when the requested weight or
 *  style cannot be found in the provided font family.
 * @param fontFamily The font family to be used when rendering the text.
 * @param fontFeatureSettings The advanced typography settings provided by font. The format is the
 *  same as the CSS font-feature-settings attribute:
 *  https://www.w3.org/TR/css-fonts-3/#font-feature-settings-prop
 * @param letterSpacing The amount of space (in em) to add between each letter.
 * @param baselineShift The amount by which the text is shifted up from the current baseline.
 * @param textGeometricTransform The geometric transformation applied the text.
 * @param localeList The locale list used to select region-specific glyphs.
 * @param background The background color for the text.
 * @param textDecoration The decorations to paint on the text (e.g., an underline).
 * @param shadow The shadow effect applied on the text.
 * @param platformStyle Platform specific [SpanStyle] parameters.
 * @param drawStyle Drawing style of text, whether fill in the text while drawing or stroke around
 * the edges.
 *
 * @see AnnotatedString
 * @see TextStyle
 * @see ParagraphStyle
 */
@Immutable
class SpanStyle internal constructor(
    // The fill to draw text, a unified representation of Color and Brush.
    internal val textForegroundStyle: TextForegroundStyle,
    // TODO
    val fontSize: TextUnit = TextUnit.Unspecified,
//    val fontSize: TextUnit = TextUnit.Unspecified,
    val fontWeight: FontWeight? = null,
    val fontStyle: FontStyle? = null,
//    val fontSynthesis: FontSynthesis? = null,
    val fontFamily: FontFamily? = null,
//    val fontFeatureSettings: String? = null,
    val letterSpacing: TextUnit = TextUnit.Unspecified,
//    val baselineShift: BaselineShift? = null,
//    val textGeometricTransform: TextGeometricTransform? = null,
//    val localeList: LocaleList? = null,
    val background: Color = Color.Unspecified,  // kuikly暂时不支持
    val textDecoration: TextDecoration? = null,
    val shadow: Shadow? = null,
//    val platformStyle: PlatformSpanStyle? = null,
//    val drawStyle: DrawStyle? = null
) {

    /**
     * Styling configuration for a text span. This configuration only allows character level styling,
     * in order to set paragraph level styling such as line height, or text alignment please see
     * [ParagraphStyle].
     *
     * @sample com.tencent.kuikly.compose.ui.text.samples.SpanStyleSample
     *
     * @sample com.tencent.kuikly.compose.ui.text.samples.AnnotatedStringBuilderSample
     *
     * @param color The color to draw the text.
     * @param fontSize The size of glyphs (in logical pixels) to use when painting the text. This
     * may be [TextUnit.Unspecified] for inheriting from another [SpanStyle].
     * @param fontWeight The typeface thickness to use when painting the text (e.g., bold).
     * @param fontStyle The typeface variant to use when drawing the letters (e.g., italic).
     * @param fontSynthesis Whether to synthesize font weight and/or style when the requested weight
     * or style cannot be found in the provided font family.
     * @param fontFamily The font family to be used when rendering the text.
     * @param fontFeatureSettings The advanced typography settings provided by font. The format is
     * the same as the CSS font-feature-settings attribute:
     *  https://www.w3.org/TR/css-fonts-3/#font-feature-settings-prop
     * @param letterSpacing The amount of space (in em) to add between each letter.
     * @param baselineShift The amount by which the text is shifted up from the current baseline.
     * @param textGeometricTransform The geometric transformation applied the text.
     * @param localeList The locale list used to select region-specific glyphs.
     * @param background The background color for the text.
     * @param textDecoration The decorations to paint on the text (e.g., an underline).
     * @param shadow The shadow effect applied on the text.
     * @param platformStyle Platform specific [SpanStyle] parameters.
     * @param drawStyle Drawing style of text, whether fill in the text while drawing or stroke
     * around the edges.
     *
     * @see AnnotatedString
     * @see TextStyle
     * @see ParagraphStyle
     */
    constructor(
        color: Color = Color.Unspecified,
        fontSize: TextUnit = TextUnit.Unspecified,
        fontWeight: FontWeight? = null,
        fontStyle: FontStyle? = null,
//        fontSynthesis: FontSynthesis? = null,
        fontFamily: FontFamily? = null,
//        fontFeatureSettings: String? = null,
        letterSpacing: TextUnit = TextUnit.Unspecified,
//        baselineShift: BaselineShift? = null,
//        textGeometricTransform: TextGeometricTransform? = null,
//        localeList: LocaleList? = null,
        background: Color = Color.Unspecified,
        textDecoration: TextDecoration? = null,
        shadow: Shadow? = null,
//        platformStyle: PlatformSpanStyle? = null,
//        drawStyle: DrawStyle? = null
    ) : this(
        textForegroundStyle = TextForegroundStyle.from(color),
        fontSize = fontSize,
        fontWeight = fontWeight,
        fontStyle = fontStyle,
//        fontSynthesis = fontSynthesis,
        fontFamily = fontFamily,
//        fontFeatureSettings = fontFeatureSettings,
        letterSpacing = letterSpacing,
//        baselineShift = baselineShift,
//        textGeometricTransform = textGeometricTransform,
//        localeList = localeList,
        background = background,
        textDecoration = textDecoration,
        shadow = shadow,
//        platformStyle = platformStyle,
//        drawStyle = drawStyle
    )

    /**
     * Styling configuration for a text span. This configuration only allows character level styling,
     * in order to set paragraph level styling such as line height, or text alignment please see
     * [ParagraphStyle].
     *
     * @sample com.tencent.kuikly.compose.ui.text.samples.SpanStyleBrushSample
     *
     * @sample com.tencent.kuikly.compose.ui.text.samples.AnnotatedStringBuilderSample
     *
     * @param brush The brush to use when painting the text. If brush is given as null, it will be
     * treated as unspecified. It is equivalent to calling the alternative color constructor with
     * [Color.Unspecified]
     * @param alpha Opacity to be applied to [brush] from 0.0f to 1.0f representing fully
     * transparent to fully opaque respectively.
     * @param fontSize The size of glyphs (in logical pixels) to use when painting the text. This
     * may be [TextUnit.Unspecified] for inheriting from another [SpanStyle].
     * @param fontWeight The typeface thickness to use when painting the text (e.g., bold).
     * @param fontStyle The typeface variant to use when drawing the letters (e.g., italic).
     * @param fontSynthesis Whether to synthesize font weight and/or style when the requested weight
     * or style cannot be found in the provided font family.
     * @param fontFamily The font family to be used when rendering the text.
     * @param fontFeatureSettings The advanced typography settings provided by font. The format is
     * the same as the CSS font-feature-settings attribute:
     *  https://www.w3.org/TR/css-fonts-3/#font-feature-settings-prop
     * @param letterSpacing The amount of space (in em) to add between each letter.
     * @param baselineShift The amount by which the text is shifted up from the current baseline.
     * @param textGeometricTransform The geometric transformation applied the text.
     * @param localeList The locale list used to select region-specific glyphs.
     * @param background The background color for the text.
     * @param textDecoration The decorations to paint on the text (e.g., an underline).
     * @param shadow The shadow effect applied on the text.
     * @param platformStyle Platform specific [SpanStyle] parameters.
     * @param drawStyle Drawing style of text, whether fill in the text while drawing or stroke
     * around the edges.
     *
     * @see AnnotatedString
     * @see TextStyle
     * @see ParagraphStyle
     */
    constructor(
        brush: Brush?,
        alpha: Float = 1f,
        fontSize: TextUnit = TextUnit.Unspecified,
        fontWeight: FontWeight? = null,
        fontStyle: FontStyle? = null,
//        fontSynthesis: FontSynthesis? = null,
        fontFamily: FontFamily? = null,
//        fontFeatureSettings: String? = null,
        letterSpacing: TextUnit = TextUnit.Unspecified,
//        baselineShift: BaselineShift? = null,
//        textGeometricTransform: TextGeometricTransform? = null,
//        localeList: LocaleList? = null,
        background: Color = Color.Unspecified,
        textDecoration: TextDecoration? = null,
        shadow: Shadow? = null,
//        platformStyle: PlatformSpanStyle? = null,
//        drawStyle: DrawStyle? = null
    ) : this(
        textForegroundStyle = TextForegroundStyle.from(brush, alpha),
        fontSize = fontSize,
        fontWeight = fontWeight,
        fontStyle = fontStyle,
//        fontSynthesis = fontSynthesis,
        fontFamily = fontFamily,
//        fontFeatureSettings = fontFeatureSettings,
        letterSpacing = letterSpacing,
//        baselineShift = baselineShift,
//        textGeometricTransform = textGeometricTransform,
//        localeList = localeList,
        background = background,
        textDecoration = textDecoration,
        shadow = shadow,
//        platformStyle = platformStyle,
//        drawStyle = drawStyle
    )

    /**
     * Color to draw text.
     */
    val color: Color get() = this.textForegroundStyle.color

    /**
     * Brush to draw text. If not null, overrides [color].
     */
    val brush: Brush? get() = this.textForegroundStyle.brush

    /**
     * Opacity of text. This value is either provided along side Brush, or via alpha channel in
     * color.
     */
    val alpha: Float get() = this.textForegroundStyle.alpha

    /**
     * Returns a new span style that is a combination of this style and the given [other] style.
     *
     * [other] span style's null or inherit properties are replaced with the non-null properties of
     * this span style. Another way to think of it is that the "missing" properties of the [other]
     * style are _filled_ by the properties of this style.
     *
     * If the given span style is null, returns this span style.
     */
    @Stable
    fun merge(other: SpanStyle? = null): SpanStyle {
        if (other == null) return this
        return fastMerge(
            color = other.textForegroundStyle.color,
            brush = other.textForegroundStyle.brush,
            alpha = other.textForegroundStyle.alpha,
            fontSize = other.fontSize,
            fontWeight = other.fontWeight,
            fontStyle = other.fontStyle,
//            fontSynthesis = other.fontSynthesis,
            fontFamily = other.fontFamily,
//            fontFeatureSettings = other.fontFeatureSettings,
            letterSpacing = other.letterSpacing,
//            baselineShift = other.baselineShift,
//            textGeometricTransform = other.textGeometricTransform,
//            localeList = other.localeList,
            background = other.background,
            textDecoration = other.textDecoration,
            shadow = other.shadow,
//            platformStyle = other.platformStyle,
//            drawStyle = other.drawStyle
        )
    }

    /**
     * Plus operator overload that applies a [merge].
     */
    @Stable
    operator fun plus(other: SpanStyle): SpanStyle = this.merge(other)

    fun copy(
        color: Color = this.color,
        fontSize: TextUnit = this.fontSize,
        fontWeight: FontWeight? = this.fontWeight,
        fontStyle: FontStyle? = this.fontStyle,
//        fontSynthesis: FontSynthesis? = this.fontSynthesis,
        fontFamily: FontFamily? = this.fontFamily,
//        fontFeatureSettings: String? = this.fontFeatureSettings,
        letterSpacing: TextUnit = this.letterSpacing,
//        baselineShift: BaselineShift? = this.baselineShift,
//        textGeometricTransform: TextGeometricTransform? = this.textGeometricTransform,
//        localeList: LocaleList? = this.localeList,
        background: Color = this.background,
        textDecoration: TextDecoration? = this.textDecoration,
        shadow: Shadow? = this.shadow,
//        platformStyle: PlatformSpanStyle? = this.platformStyle,
//        drawStyle: DrawStyle? = this.drawStyle
    ): SpanStyle {
        return SpanStyle(
            textForegroundStyle = if (color == this.color) {
                textForegroundStyle
            } else {
                TextForegroundStyle.from(color)
            },
            fontSize = fontSize,
            fontWeight = fontWeight,
            fontStyle = fontStyle,
//            fontSynthesis = fontSynthesis,
            fontFamily = fontFamily,
//            fontFeatureSettings = fontFeatureSettings,
            letterSpacing = letterSpacing,
//            baselineShift = baselineShift,
//            textGeometricTransform = textGeometricTransform,
//            localeList = localeList,
            background = background,
            textDecoration = textDecoration,
            shadow = shadow,
//            platformStyle = platformStyle,
//            drawStyle = drawStyle
        )
    }

    fun copy(
        brush: Brush?,
        alpha: Float = this.alpha,
        fontSize: TextUnit = this.fontSize,
        fontWeight: FontWeight? = this.fontWeight,
        fontStyle: FontStyle? = this.fontStyle,
//        fontSynthesis: FontSynthesis? = this.fontSynthesis,
        fontFamily: FontFamily? = this.fontFamily,
//        fontFeatureSettings: String? = this.fontFeatureSettings,
        letterSpacing: TextUnit = this.letterSpacing,
//        baselineShift: BaselineShift? = this.baselineShift,
//        textGeometricTransform: TextGeometricTransform? = this.textGeometricTransform,
//        localeList: LocaleList? = this.localeList,
        background: Color = this.background,
        textDecoration: TextDecoration? = this.textDecoration,
        shadow: Shadow? = this.shadow,
//        platformStyle: PlatformSpanStyle? = this.platformStyle,
//        drawStyle: DrawStyle? = this.drawStyle
    ): SpanStyle {
        return SpanStyle(
            textForegroundStyle = TextForegroundStyle.from(brush, alpha),
            fontSize = fontSize,
            fontWeight = fontWeight,
            fontStyle = fontStyle,
//            fontSynthesis = fontSynthesis,
            fontFamily = fontFamily,
//            fontFeatureSettings = fontFeatureSettings,
            letterSpacing = letterSpacing,
//            baselineShift = baselineShift,
//            textGeometricTransform = textGeometricTransform,
//            localeList = localeList,
            background = background,
            textDecoration = textDecoration,
            shadow = shadow,
//            platformStyle = platformStyle,
//            drawStyle = drawStyle
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SpanStyle) return false
        return hasSameLayoutAffectingAttributes(other) &&
            hasSameNonLayoutAttributes(other)
    }

    internal fun hasSameLayoutAffectingAttributes(other: SpanStyle): Boolean {
        if (this === other) return true
        if (fontSize != other.fontSize) return false
        if (fontWeight != other.fontWeight) return false
        if (fontStyle != other.fontStyle) return false
//        if (fontSynthesis != other.fontSynthesis) return false
        if (fontFamily != other.fontFamily) return false
//        if (fontFeatureSettings != other.fontFeatureSettings) return false
        if (letterSpacing != other.letterSpacing) return false
//        if (baselineShift != other.baselineShift) return false
//        if (textGeometricTransform != other.textGeometricTransform) return false
//        if (localeList != other.localeList) return false
        if (background != other.background) return false
//        if (platformStyle != other.platformStyle) return false
        return true
    }

    internal fun hasSameNonLayoutAttributes(other: SpanStyle): Boolean {
        if (textForegroundStyle != other.textForegroundStyle) return false
        if (textDecoration != other.textDecoration) return false
        if (shadow != other.shadow) return false
//        if (drawStyle != other.drawStyle) return false
        return true
    }

    override fun hashCode(): Int {
        var result = color.hashCode()
        result = 31 * result + brush.hashCode()
        result = 31 * result + alpha.hashCode()
        result = 31 * result + fontSize.hashCode()
        result = 31 * result + (fontWeight?.hashCode() ?: 0)
        result = 31 * result + (fontStyle?.hashCode() ?: 0)
//        result = 31 * result + (fontSynthesis?.hashCode() ?: 0)
        result = 31 * result + (fontFamily?.hashCode() ?: 0)
//        result = 31 * result + (fontFeatureSettings?.hashCode() ?: 0)
        result = 31 * result + letterSpacing.hashCode()
//        result = 31 * result + (baselineShift?.hashCode() ?: 0)
//        result = 31 * result + (textGeometricTransform?.hashCode() ?: 0)
//        result = 31 * result + (localeList?.hashCode() ?: 0)
        result = 31 * result + background.hashCode()
        result = 31 * result + (textDecoration?.hashCode() ?: 0)
        result = 31 * result + (shadow?.hashCode() ?: 0)
//        result = 31 * result + (platformStyle?.hashCode() ?: 0)
//        result = 31 * result + (drawStyle?.hashCode() ?: 0)
        return result
    }

    internal fun hashCodeLayoutAffectingAttributes(): Int {
        var result = fontSize.hashCode()
        result = 31 * result + (fontWeight?.hashCode() ?: 0)
        result = 31 * result + (fontStyle?.hashCode() ?: 0)
//        result = 31 * result + (fontSynthesis?.hashCode() ?: 0)
        result = 31 * result + (fontFamily?.hashCode() ?: 0)
//        result = 31 * result + (fontFeatureSettings?.hashCode() ?: 0)
        result = 31 * result + letterSpacing.hashCode()
//        result = 31 * result + (baselineShift?.hashCode() ?: 0)
//        result = 31 * result + (textGeometricTransform?.hashCode() ?: 0)
//        result = 31 * result + (localeList?.hashCode() ?: 0)
        result = 31 * result + background.hashCode()
//        result = 31 * result + (platformStyle?.hashCode() ?: 0)
        return result
    }

    // Long string concatenation causes atomicfu plugin to be slow/hang.
    // See https://youtrack.jetbrains.com/issue/KT-65645/Atomicfu-plugin-compilation-hangs-on-a-long-string-concatenation
    override fun toString(): String {
        return buildString {
            append("SpanStyle(")
            append("color=$color, ")
            append("brush=$brush, ")
            append("alpha=$alpha, ")
            append("fontSize=$fontSize, ")
            append("fontWeight=$fontWeight, ")
            append("fontStyle=$fontStyle, ")
//            append("fontSynthesis=$fontSynthesis, ")
            append("fontFamily=$fontFamily, ")
//            append("fontFeatureSettings=$fontFeatureSettings, ")
            append("letterSpacing=$letterSpacing, ")
//            append("baselineShift=$baselineShift, ")
//            append("textGeometricTransform=$textGeometricTransform, ")
//            append("localeList=$localeList, ")
            append("background=$background, ")
            append("textDecoration=$textDecoration, ")
            append("shadow=$shadow, ")
//            append("platformStyle=$platformStyle, ")
//            append("drawStyle=$drawStyle")
            append(")")
        }
    }
}

/**
 * @param a An sp value. Maybe [TextUnit.Unspecified]
 * @param b An sp value. Maybe [TextUnit.Unspecified]
 */
internal fun lerpTextUnitInheritable(a: TextUnit, b: TextUnit, t: Float): TextUnit {
    if (a.isUnspecified || b.isUnspecified) return lerpDiscrete(a, b, t)
    return lerp(a, b, t)
}

/**
 * Lerp between two values that cannot be transitioned. Returns [a] if [fraction] is smaller than
 * 0.5 otherwise [b].
 */
internal fun <T> lerpDiscrete(a: T, b: T, fraction: Float): T = if (fraction < 0.5) a else b

/**
 * Interpolate between two span styles.
 *
 * This will not work well if the styles don't set the same fields.
 *
 * The [fraction] argument represents position on the timeline, with 0.0 meaning
 * that the interpolation has not started, returning [start] (or something
 * equivalent to [start]), 1.0 meaning that the interpolation has finished,
 * returning [stop] (or something equivalent to [stop]), and values in between
 * meaning that the interpolation is at the relevant point on the timeline
 * between [start] and [stop]. The interpolation can be extrapolated beyond 0.0 and
 * 1.0, so negative values and values greater than 1.0 are valid.
 */
fun lerp(start: SpanStyle, stop: SpanStyle, fraction: Float): SpanStyle {
    return SpanStyle(
        textForegroundStyle = lerp(start.textForegroundStyle, stop.textForegroundStyle, fraction),
        fontFamily = lerpDiscrete(
            start.fontFamily,
            stop.fontFamily,
            fraction
        ),
        fontSize = lerpTextUnitInheritable(start.fontSize, stop.fontSize, fraction),
        fontWeight = lerp(
            start.fontWeight ?: FontWeight.Normal,
            stop.fontWeight ?: FontWeight.Normal,
            fraction
        ),
        fontStyle = lerpDiscrete(
            start.fontStyle,
            stop.fontStyle,
            fraction
        ),
//        fontSynthesis = lerpDiscrete(
//            start.fontSynthesis,
//            stop.fontSynthesis,
//            fraction
//        ),
//        fontFeatureSettings = lerpDiscrete(
//            start.fontFeatureSettings,
//            stop.fontFeatureSettings,
//            fraction
//        ),
        letterSpacing = lerpTextUnitInheritable(
            start.letterSpacing,
            stop.letterSpacing,
            fraction
        ),
//        baselineShift = lerp(
//            start.baselineShift ?: BaselineShift(0f),
//            stop.baselineShift ?: BaselineShift(0f),
//            fraction
//        ),
//        textGeometricTransform = lerp(
//            start.textGeometricTransform ?: TextGeometricTransform.None,
//            stop.textGeometricTransform ?: TextGeometricTransform.None,
//            fraction
//        ),
//        localeList = lerpDiscrete(start.localeList, stop.localeList, fraction),
        background = lerp(
            start.background,
            stop.background,
            fraction
        ),
        textDecoration = lerpDiscrete(
            start.textDecoration,
            stop.textDecoration,
            fraction
        ),
        shadow = lerp(
            start.shadow ?: Shadow(),
            stop.shadow ?: Shadow(),
            fraction
        ),
//        platformStyle = lerpPlatformStyle(start.platformStyle, stop.platformStyle, fraction),
//        drawStyle = lerpDiscrete(
//            start.drawStyle,
//            stop.drawStyle,
//            fraction
//        )
    )
}

//private fun lerpPlatformStyle(
//    start: PlatformSpanStyle?,
//    stop: PlatformSpanStyle?,
//    fraction: Float
//): PlatformSpanStyle? {
//    if (start == null && stop == null) return null
//    val startNonNull = start ?: PlatformSpanStyle.Default
//    val stopNonNull = stop ?: PlatformSpanStyle.Default
//    return lerp(startNonNull, stopNonNull, fraction)
//}

internal fun resolveSpanStyleDefaults(style: SpanStyle) = SpanStyle(
    textForegroundStyle = style.textForegroundStyle.takeOrElse {
        TextForegroundStyle.from(DefaultColor)
    },
    fontSize = if (style.fontSize.isUnspecified) DefaultFontSize else style.fontSize,
    fontWeight = style.fontWeight ?: FontWeight.Normal,
    fontStyle = style.fontStyle ?: FontStyle.Normal,
//    fontSynthesis = style.fontSynthesis ?: FontSynthesis.All,
    fontFamily = style.fontFamily ?: FontFamily.Default,
//    fontFeatureSettings = style.fontFeatureSettings ?: "",
    letterSpacing = if (style.letterSpacing.isUnspecified) {
        DefaultLetterSpacing
    } else {
        style.letterSpacing
    },
//    baselineShift = style.baselineShift ?: BaselineShift.None,
//    textGeometricTransform = style.textGeometricTransform ?: TextGeometricTransform.None,
//    localeList = style.localeList ?: LocaleList.current,
    background = style.background.takeOrElse { DefaultBackgroundColor },
    textDecoration = style.textDecoration ?: TextDecoration.None,
    shadow = style.shadow ?: Shadow.None,
//    platformStyle = style.platformStyle,
//    drawStyle = style.drawStyle ?: Fill
)

internal fun SpanStyle.fastMerge(
    color: Color,
    brush: Brush?,
    alpha: Float,
    fontSize: TextUnit,
    fontWeight: FontWeight?,
    fontStyle: FontStyle?,
//    fontSynthesis: FontSynthesis?,
    fontFamily: FontFamily?,
//    fontFeatureSettings: String?,
    letterSpacing: TextUnit,
//    baselineShift: BaselineShift?,
//    textGeometricTransform: TextGeometricTransform?,
//    localeList: LocaleList?,
    background: Color,
    textDecoration: TextDecoration?,
    shadow: Shadow?,
//    platformStyle: PlatformSpanStyle?,
//    drawStyle: DrawStyle?
): SpanStyle {
    // prioritize the parameters to Text in diffs here
    /**
     *  color: Color
     *  fontSize: TextUnit
     *  fontStyle: FontStyle?
     *  fontWeight: FontWeight?
     *  fontFamily: FontFamily?
     *  letterSpacing: TextUnit
     *  textDecoration: TextDecoration?
     *  textAlign: TextAlign?
     *  lineHeight: TextUnit
     */

    // any new vals should do a pre-merge check here
    val requiresAlloc = fontSize.isSpecified && fontSize != this.fontSize ||
        brush == null && color.isSpecified && color != textForegroundStyle.color ||
        fontStyle != null && fontStyle != this.fontStyle ||
        fontWeight != null && fontWeight != this.fontWeight ||
        // ref check for font-family, since we don't want to compare lists in fast path
        fontFamily != null && fontFamily !== this.fontFamily ||
        letterSpacing.isSpecified && letterSpacing != this.letterSpacing ||
        textDecoration != null && textDecoration != this.textDecoration ||
        // then compare the remaining params, for potential non-Text merges
        brush != textForegroundStyle.brush ||
        brush != null && alpha != this.textForegroundStyle.alpha ||
//        fontSynthesis != null && fontSynthesis != this.fontSynthesis ||
//        fontFeatureSettings != null && fontFeatureSettings != this.fontFeatureSettings ||
//        baselineShift != null && baselineShift != this.baselineShift ||
//        textGeometricTransform != null && textGeometricTransform != this.textGeometricTransform ||
//        localeList != null && localeList != this.localeList ||
        background.isSpecified && background != this.background ||
        shadow != null && shadow != this.shadow
//            ||
//        platformStyle != null && platformStyle != this.platformStyle ||
//        drawStyle != null && drawStyle != this.drawStyle

    if (!requiresAlloc) {
        // we're done
        return this
    }

    val otherTextForegroundStyle = if (brush != null) {
        TextForegroundStyle.from(brush, alpha)
    } else {
        TextForegroundStyle.from(color)
    }

    return SpanStyle(
        textForegroundStyle = textForegroundStyle.merge(otherTextForegroundStyle),
        fontFamily = fontFamily ?: this.fontFamily,
        fontSize = if (!fontSize.isUnspecified) fontSize else this.fontSize,
        fontWeight = fontWeight ?: this.fontWeight,
        fontStyle = fontStyle ?: this.fontStyle,
//        fontSynthesis = fontSynthesis ?: this.fontSynthesis,
//        fontFeatureSettings = fontFeatureSettings ?: this.fontFeatureSettings,
        letterSpacing = if (!letterSpacing.isUnspecified) {
            letterSpacing
        } else {
            this.letterSpacing
        },
//        baselineShift = baselineShift ?: this.baselineShift,
//        textGeometricTransform = textGeometricTransform ?: this.textGeometricTransform,
//        localeList = localeList ?: this.localeList,
        background = background.takeOrElse { this.background },
        textDecoration = textDecoration ?: this.textDecoration,
        shadow = shadow ?: this.shadow,
//        platformStyle = mergePlatformStyle(platformStyle),
//        drawStyle = drawStyle ?: this.drawStyle
    )
}

//private fun SpanStyle.mergePlatformStyle(other: PlatformSpanStyle?): PlatformSpanStyle? {
//    if (platformStyle == null) return other
//    if (other == null) return platformStyle
//    return platformStyle.merge(other)
//}
