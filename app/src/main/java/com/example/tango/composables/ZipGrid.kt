package com.example.tango.composables

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.tango.CELL_SIZE
import com.example.tango.R
import com.example.tango.dataClasses.ZipCellData
import com.example.tango.utils.Utils.conditional
import com.example.tango.utils.Utils.dpToPx
import kotlin.math.max
import kotlin.math.min


@Composable
fun ZipGrid(
    grid: Array<Array<ZipCellData>>?,
    modifier: Modifier = Modifier,
    cellSize: Dp = CELL_SIZE,
    path: List<Pair<Int, Int>> = listOf(),
    enableDragging: Boolean = false,
    onDragStart: ((Pair<Int, Int>) -> Unit)? = null,
    onDrag: ((Pair<Int, Int>) -> Unit)? = null,
    onDragEnd: (() -> Unit)? = null,
    cellComposable: @Composable RowScope.(ZipCellData, Int, Int, Boolean, Boolean) -> Unit,
) {
    val edgeColor = colorResource(R.color.border_color)
    val cellSizeInPixels = cellSize.dpToPx()
    var currentTouchedCoordinates by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var currentPosition by remember { mutableStateOf<Offset?>(null) }

    fun getCoordinates(offset: Offset): Pair<Int, Int> {
        var i = (offset.y / cellSizeInPixels).toInt()
        var j = (offset.x / cellSizeInPixels).toInt()
        i = max(i, 0)
        j = max(j, 0)
        i = min(i, (grid?.size ?: 1) - 1)
        j = min(j, (grid?.get(0)?.size ?: 1) - 1)
        return Pair(i, j)
    }

    fun getPosition(coordinates: Pair<Int, Int>): Offset {
        return Offset(
            coordinates.second * cellSizeInPixels + cellSizeInPixels / 2,
            coordinates.first * cellSizeInPixels + cellSizeInPixels / 2,
        )
    }

    val brush = remember { Brush.linearGradient(listOf(Color.Red, Color.Yellow)) }
    Box(Modifier, contentAlignment = Alignment.Center) {
        Column(
            modifier = modifier
                .border(
                    width = 0.5.dp,
                    color = edgeColor,
                    shape = RoundedCornerShape(4.dp)
                )
                .clip(RoundedCornerShape(4.dp))
        ) {
            grid?.forEachIndexed { i, row ->
                Row {
                    row.forEachIndexed { j, cell ->
                        cellComposable(cell, i, j, true, false)
                    }
                }
            }
        }
        if (path.isNotEmpty()) {
            Canvas(modifier = Modifier.matchParentSize()) {
                drawPath(
                    path = Path().apply {
                        val startOffset = getPosition(path[0])
                        moveTo(startOffset.x, startOffset.y)
                        path.drop(1).forEach { it ->
                            val xy = getPosition(it)
                            lineTo(xy.x, xy.y)
                            moveTo(xy.x, xy.y)
                        }
                        currentPosition?.let {
                            lineTo(it.x, it.y)
                        }
                    },
                    brush = brush,
                    style = Stroke(width = 80.dp.value, cap = StrokeCap.Round)
                )
            }
        }
        Column(
            modifier = Modifier
                .conditional(enableDragging) {
                    pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                val coordinates = getCoordinates(offset)
                                currentTouchedCoordinates = coordinates
                                onDragStart?.invoke(coordinates)
                                currentPosition = offset
                            },
                            onDrag = { change, offset ->
                                val coordinates = getCoordinates(change.position)
                                if (coordinates != currentTouchedCoordinates) {
                                    onDrag?.invoke(coordinates)
                                }
                                currentTouchedCoordinates = coordinates
                                currentPosition = change.position
                            },
                            onDragEnd = {
                                onDragEnd?.invoke()
                                currentPosition = null
                            }
                        )
                    }
                }
        ) {
            grid?.forEachIndexed { i, row ->
                Row {
                    row.forEachIndexed { j, cell ->
                        cellComposable(cell, i, j, false, true)
                    }
                }
            }
        }
    }
}

