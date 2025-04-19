package com.example.tango.composables

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.tango.CELL_SIZE
import com.example.tango.EDGE_STROKE
import com.example.tango.R
import com.example.tango.SYMBOL_ICON_SIZE
import com.example.tango.SYMBOL_SIZE
import com.example.tango.dataClasses.SYMBOLS
import com.example.tango.dataClasses.TangoCellData
import com.example.tango.dataClasses.TangoCellValue
import com.example.tango.utils.Utils.conditional
import com.example.tango.utils.Utils.pxToDp

@Composable
fun TangoCell(
    cellData: TangoCellData,
    disabled: Boolean,
    cellSize: Int = CELL_SIZE.value.toInt(),
    onClick: () -> Unit
) {
    val symbolHeight = with(LocalDensity.current) {
        SYMBOL_SIZE.toPx()
    }
    val cellSizeInDp = cellSize.pxToDp()

    val edgeColor = colorResource(R.color.border_color)
    val crossPainter =
        rememberVectorPainter(image = ImageVector.vectorResource(id = R.drawable.cross))
    val equalsPainter =
        rememberVectorPainter(image = ImageVector.vectorResource(id = R.drawable.equals))
    var cellData = remember { cellData }

    Box(
        contentAlignment = Alignment.Center, modifier = Modifier
            .background(Color.White)
            .conditional(cellData.disabled) {
                background(colorResource(R.color.disabled_bg))
            }
            .conditional(cellData.containsError) {
                background(stripedBackground())
            }
            .clickable(
                enabled = !disabled && !cellData.disabled, onClick = onClick
            )
    ) {
        Canvas(modifier = Modifier.size(cellSizeInDp)) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val mid = canvasHeight / 2

//          Left
            when (cellData.leftSymbol) {
                SYMBOLS.NONE -> {}
                SYMBOLS.SIMPLE -> drawLine(
                    start = Offset(x = 0f, y = 0f),
                    end = Offset(x = 0f, y = canvasHeight),
                    color = edgeColor,
                    strokeWidth = EDGE_STROKE
                )

                SYMBOLS.CROSS -> {
                    drawLine(
                        start = Offset(x = 0f, y = 0f),
                        end = Offset(x = 0f, y = mid - symbolHeight / 2),
                        color = edgeColor,
                        strokeWidth = EDGE_STROKE
                    )
                    with(crossPainter) {
                        translate(
                            -SYMBOL_ICON_SIZE.toPx() / 2,
                            canvasHeight / 2 - SYMBOL_ICON_SIZE.toPx() / 2
                        ) {
                            draw(Size(SYMBOL_ICON_SIZE.toPx(), SYMBOL_ICON_SIZE.toPx()))
                        }
                    }
                    drawLine(
                        start = Offset(x = 0f, y = mid + symbolHeight / 2),
                        end = Offset(x = 0f, y = canvasHeight),
                        color = edgeColor,
                        strokeWidth = EDGE_STROKE
                    )
                }

                SYMBOLS.EQUALS -> {
                    drawLine(
                        start = Offset(x = 0f, y = 0f),
                        end = Offset(x = 0f, y = mid - symbolHeight / 2),
                        color = edgeColor,
                        strokeWidth = EDGE_STROKE
                    )
                    with(equalsPainter) {
                        translate(
                            -SYMBOL_ICON_SIZE.toPx() / 2,
                            canvasHeight / 2 - SYMBOL_ICON_SIZE.toPx() / 2
                        ) {
                            draw(Size(SYMBOL_ICON_SIZE.toPx(), SYMBOL_ICON_SIZE.toPx()))
                        }
                    }
                    drawLine(
                        start = Offset(x = 0f, y = mid + symbolHeight / 2),
                        end = Offset(x = 0f, y = canvasHeight),
                        color = edgeColor,
                        strokeWidth = EDGE_STROKE
                    )
                }
            }

//          Top
            when (cellData.topSymbol) {
                SYMBOLS.NONE -> {}
                SYMBOLS.SIMPLE -> drawLine(
                    start = Offset(x = 0f, y = 0f),
                    end = Offset(x = canvasWidth, y = 0f),
                    color = edgeColor,
                    strokeWidth = EDGE_STROKE
                )

                SYMBOLS.CROSS -> {
                    drawLine(
                        start = Offset(x = 0f, y = 0f),
                        end = Offset(x = canvasWidth / 2 - symbolHeight / 2, y = 0f),
                        color = edgeColor,
                        strokeWidth = EDGE_STROKE
                    )
                    with(crossPainter) {
                        translate(
                            canvasHeight / 2 - SYMBOL_ICON_SIZE.toPx() / 2,
                            -SYMBOL_ICON_SIZE.toPx() / 2,
                        ) {
                            draw(Size(SYMBOL_ICON_SIZE.toPx(), SYMBOL_ICON_SIZE.toPx()))
                        }
                    }
                    drawLine(
                        start = Offset(x = canvasWidth / 2 + symbolHeight / 2, y = 0f),
                        end = Offset(x = canvasWidth, y = 0f),
                        color = edgeColor,
                        strokeWidth = EDGE_STROKE
                    )
                }

                SYMBOLS.EQUALS -> {
                    drawLine(
                        start = Offset(x = 0f, y = 0f),
                        end = Offset(x = canvasWidth / 2 - symbolHeight / 2, y = 0f),
                        color = edgeColor,
                        strokeWidth = EDGE_STROKE
                    )
                    with(equalsPainter) {
                        translate(
                            canvasHeight / 2 - SYMBOL_ICON_SIZE.toPx() / 2,
                            -SYMBOL_ICON_SIZE.toPx() / 2,

                            ) {
                            draw(Size(SYMBOL_ICON_SIZE.toPx(), SYMBOL_ICON_SIZE.toPx()))
                        }
                    }
                    drawLine(
                        start = Offset(x = canvasWidth / 2 + symbolHeight / 2, y = 0f),
                        end = Offset(x = canvasWidth, y = 0f),
                        color = edgeColor,
                        strokeWidth = EDGE_STROKE
                    )
                }
            }
        }
        when (cellData.value) {
            TangoCellValue.BLANK -> {}
            TangoCellValue.SUN -> Image(
                painter = painterResource(id = R.drawable.sun), contentDescription = "Sun"
            )

            TangoCellValue.MOON -> Image(
                painter = painterResource(id = R.drawable.moon), contentDescription = "Moon"
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TangoCellPreviewSun() {
    Box(Modifier.padding(30.dp)) {
        TangoCell(
            TangoCellData(
                disabled = false,
                topSymbol = SYMBOLS.EQUALS,
                leftSymbol = SYMBOLS.CROSS,
                value = TangoCellValue.SUN
            ), disabled = false, onClick = {})
    }
}

@Preview(showBackground = true)
@Composable
fun TangoCellPreviewMoon() {
    Box(Modifier.padding(30.dp)) {
        TangoCell(
            TangoCellData(
                disabled = false,
                topSymbol = SYMBOLS.CROSS,
                leftSymbol = SYMBOLS.EQUALS,
                value = TangoCellValue.MOON
            ), disabled = false, onClick = {})
    }
}

@Preview(showBackground = true)
@Composable
fun TangoCellPreviewDisabled() {
    Box(Modifier.padding(30.dp)) {
        TangoCell(
            TangoCellData(
                disabled = true, value = TangoCellValue.SUN
            ), disabled = false, onClick = {})
    }
}


@Preview(showBackground = true)
@Composable
fun TangoCellPreviewError() {
    Box(Modifier.padding(30.dp)) {
        val a = TangoCellData(
            disabled = false,
            value = TangoCellValue.SUN,
        )
        a.containsError = true
        TangoCell(a, disabled = false, onClick = {})
    }
}
