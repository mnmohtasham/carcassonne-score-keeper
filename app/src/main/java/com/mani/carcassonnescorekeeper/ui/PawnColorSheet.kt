package com.mani.carcassonnescorekeeper.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mani.carcassonnescorekeeper.R
import com.mani.carcassonnescorekeeper.data.PawnPalette
import com.mani.carcassonnescorekeeper.ui.theme.pawnColorName
import kotlinx.coroutines.launch
import android.graphics.Color as AndroidColor

private const val DISABLED_ALPHA = 0.38f
private const val PRESETS_PER_ROW = 3
private const val OPAQUE = -0x1000000 // 0xFF000000

/**
 * Lets the score keeper pick a preset pawn colour or mix a custom one.
 * Colours in [taken] belong to other players and cannot be chosen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PawnColorSheet(
    playerName: String,
    current: Int,
    taken: Set<Int>,
    onPick: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    var picked by remember { mutableStateOf(false) }

    fun pick(argb: Int) {
        if (picked) return // the sheet stays tappable while it animates closed
        picked = true
        onPick(argb)
        scope.launch { sheetState.hide() }.invokeOnCompletion { onDismiss() }
    }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp),
        ) {
            Text(
                if (playerName.isBlank()) stringResource(R.string.pawn_colour)
                else stringResource(R.string.pawn_colour_for, playerName),
                style = MaterialTheme.typography.titleLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Column(Modifier.padding(vertical = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                PawnPalette.presets.chunked(PRESETS_PER_ROW).forEach { row ->
                    Row {
                        row.forEach { argb ->
                            Swatch(
                                argb = argb,
                                selected = argb == current,
                                available = argb == current || argb !in taken,
                                onClick = { pick(argb) },
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
            }
            HorizontalDivider()
            // Always starts collapsed: if the hex field is composed while the sheet opens it can
            // take focus and pop the keyboard up over the presets.
            val isCustom = current !in PawnPalette.presets
            var showCustom by remember { mutableStateOf(false) }
            if (showCustom) {
                Text(
                    stringResource(R.string.custom_colour),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
                )
                CustomColorEditor(
                    initial = current,
                    isAvailable = { it == current || it !in taken },
                    onUse = ::pick,
                )
            } else {
                OutlinedButton(
                    onClick = { showCustom = true },
                    border = if (isCustom) BorderStroke(3.dp, MaterialTheme.colorScheme.primary) else ButtonDefaults.outlinedButtonBorder(),
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp).height(56.dp),
                ) {
                    if (isCustom) {
                        MeepleToken(current, size = 32.dp)
                        Text(stringResource(R.string.edit_custom_colour), modifier = Modifier.padding(start = 12.dp))
                    } else {
                        Text(stringResource(R.string.choose_custom_colour))
                    }
                }
            }
        }
    }
}

@Composable
private fun Swatch(argb: Int, selected: Boolean, available: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .selectable(selected = selected, enabled = available, role = Role.RadioButton, onClick = onClick)
            .alpha(if (available) 1f else DISABLED_ALPHA)
            .padding(vertical = 6.dp),
    ) {
        val ring = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent
        Box(Modifier.border(3.dp, ring, CircleShape).padding(5.dp)) {
            MeepleToken(argb, size = 44.dp)
        }
        Text(
            if (available) pawnColorName(argb) else stringResource(R.string.colour_taken_short),
            style = MaterialTheme.typography.labelMedium,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}

/** Hue / saturation / brightness sliders plus a hex field, kept in sync with each other. */
@Composable
private fun CustomColorEditor(initial: Int, isAvailable: (Int) -> Boolean, onUse: (Int) -> Unit) {
    val start = remember { FloatArray(3).also { AndroidColor.colorToHSV(initial, it) } }
    var hue by remember { mutableFloatStateOf(start[0]) }
    var saturation by remember { mutableFloatStateOf(start[1]) }
    var brightness by remember { mutableFloatStateOf(start[2]) }
    // The exact colour; HSV floats would round "#F4C430" to "#F4C42F" if used as the source.
    var argb by remember { mutableIntStateOf(initial or OPAQUE) }
    var hexText by remember { mutableStateOf(PawnPalette.toHex(initial)) }

    fun fromSliders(h: Float = hue, s: Float = saturation, v: Float = brightness) {
        hue = h
        saturation = s
        brightness = v
        argb = AndroidColor.HSVToColor(floatArrayOf(h, s, v))
        hexText = PawnPalette.toHex(argb)
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
        MeepleToken(argb, size = 56.dp)
        OutlinedTextField(
            value = hexText,
            onValueChange = { text ->
                hexText = text.uppercase().filter { it == '#' || it.isDigit() || it in 'A'..'F' }.take(7)
                PawnPalette.parseHex(hexText)?.let { parsed ->
                    argb = parsed
                    val hsv = FloatArray(3).also { AndroidColor.colorToHSV(parsed, it) }
                    hue = hsv[0]
                    saturation = hsv[1]
                    brightness = hsv[2]
                }
            },
            label = { Text(stringResource(R.string.hex_code)) },
            singleLine = true,
            isError = PawnPalette.parseHex(hexText) == null,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters, imeAction = ImeAction.Done),
            modifier = Modifier.weight(1f).padding(start = 16.dp),
        )
    }

    val rainbow = remember { (0..6).map { Color(AndroidColor.HSVToColor(floatArrayOf(it * 60f, 1f, 1f))) } }
    GradientSlider(
        label = stringResource(R.string.hue),
        value = hue,
        range = 0f..360f,
        brush = Brush.horizontalGradient(rainbow),
        onValueChange = { fromSliders(h = it) },
    )
    GradientSlider(
        label = stringResource(R.string.saturation),
        value = saturation,
        range = 0f..1f,
        brush = Brush.horizontalGradient(
            listOf(
                Color(AndroidColor.HSVToColor(floatArrayOf(hue, 0f, brightness))),
                Color(AndroidColor.HSVToColor(floatArrayOf(hue, 1f, brightness))),
            ),
        ),
        onValueChange = { fromSliders(s = it) },
    )
    GradientSlider(
        label = stringResource(R.string.brightness),
        value = brightness,
        range = 0f..1f,
        brush = Brush.horizontalGradient(
            listOf(Color.Black, Color(AndroidColor.HSVToColor(floatArrayOf(hue, saturation, 1f)))),
        ),
        onValueChange = { fromSliders(v = it) },
    )

    val available = isAvailable(argb)
    if (!available) {
        Text(
            stringResource(R.string.colour_taken),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.padding(top = 8.dp),
        )
    }
    Button(
        onClick = { onUse(argb) },
        enabled = available,
        modifier = Modifier.fillMaxWidth().padding(top = 12.dp).height(52.dp),
    ) { Text(stringResource(R.string.use_colour)) }
}

@Composable
private fun GradientSlider(
    label: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    brush: Brush,
    onValueChange: (Float) -> Unit,
) {
    Text(
        label,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 12.dp),
    )
    Box(Modifier.fillMaxWidth().height(44.dp), contentAlignment = Alignment.Center) {
        Box(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .height(16.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(brush),
        )
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = range,
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.onSurface,
                activeTrackColor = Color.Transparent,
                inactiveTrackColor = Color.Transparent,
                activeTickColor = Color.Transparent,
                inactiveTickColor = Color.Transparent,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .semantics { contentDescription = label },
        )
    }
}
