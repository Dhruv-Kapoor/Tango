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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
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

@Composable
fun TangoCell(
    cellData: TangoCellData, completed: Boolean, onClick: (cellData: TangoCellData) -> Unit
) {
    val symbolHeight = with(LocalDensity.current) {
        SYMBOL_SIZE.toPx()
    }
    val edgeColor = colorResource(R.color.border_color)
    val crossPainter =
        rememberVectorPainter(image = ImageVector.vectorResource(id = R.drawable.cross))
    val equalsPainter =
        rememberVectorPainter(image = ImageVector.vectorResource(id = R.drawable.equals))
    var cellData = remember { cellData }

    var modifier = if (cellData.containsError) {
        Modifier.background(stripedBackground(cellData.disabled))
    } else if (cellData.disabled) {
        Modifier.background(colorResource(R.color.disabled_bg))
    } else {
        Modifier.background(Color.White)
    }

    Box(
        contentAlignment = Alignment.Center, modifier = modifier.clickable(
            enabled = !completed && !cellData.disabled, onClick = {
                cellData.value = (cellData.value % 3) + 1
                onClick(cellData)
            })

    ) {
        Canvas(modifier = Modifier.size(CELL_SIZE)) {
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
            ), completed = false, onClick = {})
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
            ), completed = false, onClick = {})
    }
}

@Preview(showBackground = true)
@Composable
fun TangoCellPreviewDisabled() {
    Box(Modifier.padding(30.dp)) {
        TangoCell(
            TangoCellData(
                disabled = true, value = TangoCellValue.SUN
            ), completed = false, onClick = {})
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
        TangoCell(a, completed = false, onClick = {})
    }
}

@Composable
fun stripedBackground(disabled: Boolean = false): Brush {
    val stripeSize = with(LocalDensity.current) {
        4.dp.toPx()
    }
    val brush = Brush.linearGradient(
        0.0f to Color.Red,
        0.3f to Color.Red,
        0.3f to (if (disabled) colorResource(R.color.disabled_bg) else Color.White),
        1.0f to (if (disabled) colorResource(R.color.disabled_bg) else Color.White),
        start = Offset(0f, 0f),
        end = Offset(stripeSize, stripeSize),
        tileMode = TileMode.Repeated
    )
    return brush
}