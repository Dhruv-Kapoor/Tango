package com.example.tango.composables

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.tango.EDGE_STROKE
import com.example.tango.QUEENS_EDGE_THICKNESS_FACTOR
import com.example.tango.R
import com.example.tango.dataClasses.QueensCellData
import com.example.tango.dataClasses.QueensCellValue
import com.example.tango.utils.Utils.dpToPx
import com.example.tango.utils.Utils.pxToDp

@Composable
fun QueensCell(
    cellData: QueensCellData, disabled: Boolean = false, cellSize: Int, onClick: () -> Unit
) {
    val edgeColor = colorResource(R.color.black)
    var cellData = remember { cellData }
    val cellSizeInDp = cellSize.pxToDp()

    var modifier = if (cellData.containsError) {
        Modifier.background(stripedBackground(cellData.getColor()))
    } else {
        Modifier.background(cellData.getColor())
    }

    Box(
        contentAlignment = Alignment.Center, modifier = modifier
            .clickable(
                enabled = !disabled, onClick = onClick
            )
    ) {
        Canvas(modifier = Modifier.size(cellSizeInDp)) {
            val canvasWidth = size.width
            val canvasHeight = size.height

            if (cellData.leftColorId != cellData.color) {
                drawLine(
                    start = Offset(x = 0f, y = 0f),
                    end = Offset(x = 0f, y = canvasHeight),
                    color = edgeColor,
                    strokeWidth = EDGE_STROKE * QUEENS_EDGE_THICKNESS_FACTOR
                )
            } else {
                drawLine(
                    start = Offset(x = 0f, y = 0f),
                    end = Offset(x = 0f, y = canvasHeight),
                    color = edgeColor,
                    strokeWidth = EDGE_STROKE
                )
            }

            if (cellData.topColorId != cellData.color) {
                drawLine(
                    start = Offset(x = 0f, y = 0f),
                    end = Offset(x = canvasWidth, y = 0f),
                    color = edgeColor,
                    strokeWidth = EDGE_STROKE * QUEENS_EDGE_THICKNESS_FACTOR
                )
            } else {
                drawLine(
                    start = Offset(x = 0f, y = 0f),
                    end = Offset(x = canvasWidth, y = 0f),
                    color = edgeColor,
                    strokeWidth = EDGE_STROKE
                )
            }
        }
        when (cellData.value) {
            QueensCellValue.BLANK -> {}
            QueensCellValue.CROSS -> Image(
                painter = painterResource(id = R.drawable.queen_cross), contentDescription = "Cross"
            )

            QueensCellValue.QUEEN -> Image(
                painter = painterResource(id = R.drawable.queen), contentDescription = "Queen"
            )
        }
    }
}

@Preview
@Composable
fun QueensCellPreview() {
    QueensCell(
        QueensCellData(
            QueensCellValue.QUEEN,
            0,
        ),
        cellSize = 52.dp.dpToPx().toInt(),

        ) { }
}

@Preview
@Composable
fun QueensCellCross() {
    QueensCell(
        QueensCellData(
            QueensCellValue.CROSS,
            1
        ),
        cellSize = 52.dp.dpToPx().toInt(),
    ) { }
}

@Preview
@Composable
fun QueensCellError() {
    val cell = QueensCellData(
        QueensCellValue.QUEEN,
        2,
    )
    cell.containsError = true
    QueensCell(
        cell,
        disabled = true,
        52.dp.dpToPx().toInt()

    ) { }
}