package com.example.tango.composables

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.tango.EDGE_STROKE
import com.example.tango.R
import com.example.tango.ZIP_EDGE_THICKNESS_FACTOR
import com.example.tango.dataClasses.ZipCellData
import com.example.tango.utils.Utils.conditional
import com.example.tango.utils.Utils.dpToPx
import com.example.tango.utils.Utils.pxToDp

@Composable
fun ZipCell(
    cellData: ZipCellData, disabled: Boolean = false, cellSize: Int, onClick: () -> Unit
) {
    var cellData = remember { cellData }
    val cellSizeInDp = cellSize.pxToDp()

    Box(
        contentAlignment = Alignment.Center, modifier = Modifier
            .background(Color.Transparent)
            .clickable(
                enabled = !disabled, onClick = onClick
            )
            .size(cellSizeInDp)
    ) {
        cellData.value?.let {
            Text(
                cellData.value.toString(),
                modifier = Modifier
                    .background(Color.Black, shape = RoundedCornerShape(16.dp))
                    .defaultMinSize(minWidth = 30.dp)
                    .padding(vertical = 4.dp, horizontal = 6.dp),
                color = Color.White,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun ZipCellBorders(cellData: ZipCellData, cellSize: Int) {
    val edgeColor = colorResource(R.color.border_color)
    var cellData = remember { cellData }
    val cellSizeInDp = cellSize.pxToDp()

    Canvas(
        modifier = Modifier
            .size(cellSizeInDp)
            .conditional(cellData.pathPosition != null) {
                background(Color(0x4DEE352E))
            }
            .conditional(cellData.containsError) {
                background(stripedBackground())
            }) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        if (cellData.leftWall) {
            drawLine(
                start = Offset(x = 0f, y = 0f),
                end = Offset(x = 0f, y = canvasHeight),
                color = Color.Black,
                strokeWidth = EDGE_STROKE * ZIP_EDGE_THICKNESS_FACTOR,
                cap = StrokeCap.Round
            )
        } else {
            drawLine(
                start = Offset(x = 0f, y = 0f),
                end = Offset(x = 0f, y = canvasHeight),
                color = edgeColor,
                strokeWidth = EDGE_STROKE
            )
        }

        if (cellData.topWall) {
            drawLine(
                start = Offset(x = 0f, y = 0f),
                end = Offset(x = canvasWidth, y = 0f),
                color = Color.Black,
                strokeWidth = EDGE_STROKE * ZIP_EDGE_THICKNESS_FACTOR,
                cap = StrokeCap.Round
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
}

@Preview()
@Composable
fun ZipCellPreview() {
    Box(
        modifier = Modifier
            .size(400.dp)
            .background(Color.White), contentAlignment = Alignment.Center
    ) {
        Column {
            Row {
                ZipCell(
                    ZipCellData(
                        1,
                        true,
                        false
                    ),
                    cellSize = 52.dp.dpToPx().toInt(),
                ) { }
                ZipCell(
                    ZipCellData(
                        2,
                        false,
                        false
                    ),
                    cellSize = 52.dp.dpToPx().toInt(),
                ) { }
            }
            Row {
                ZipCell(
                    ZipCellData(
                        10,
                        false,
                        true
                    ),
                    cellSize = 52.dp.dpToPx().toInt(),
                ) { }
                ZipCell(
                    ZipCellData(
                        null,
                        false,
                        false
                    ),
                    cellSize = 52.dp.dpToPx().toInt(),
                ) { }
            }
        }
    }
}
