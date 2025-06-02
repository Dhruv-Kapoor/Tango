package com.example.tango.composables

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.tango.CELL_SIZE
import com.example.tango.EDGE_STROKE
import com.example.tango.R
import com.example.tango.dataClasses.TicTacToeCellData
import com.example.tango.dataClasses.TicTacToeCellValue
import com.example.tango.utils.Utils.pxToDp

@Composable
fun TicTacToeCell(
    cellData: TicTacToeCellData,
    disabled: Boolean,
    position: Pair<Int, Int>,
    cellSize: Int = CELL_SIZE.value.toInt(),
    invertColors: Boolean = false,
    onClick: () -> Unit
) {
    val cellSizeInDp = cellSize.pxToDp()

    val edgeColor = colorResource(R.color.border_color)
    val redColor = if (cellData.partial) Color(0xFFf5b7b1) else Color(0xFFe74c3c)
    val blueColor = if (cellData.partial) Color(0xFFaed6f1) else Color(0xFF3498db)

    Box(
        contentAlignment = Alignment.Center, modifier = Modifier
            .clickable(
                enabled = !disabled, onClick = onClick
            )
    ) {
        Canvas(modifier = Modifier.size(cellSizeInDp)) {
            val canvasWidth = size.width
            val canvasHeight = size.height

            if (position.second > 0) {
                drawLine(
                    start = Offset(x = 0f, y = 0f),
                    end = Offset(x = 0f, y = canvasHeight),
                    color = edgeColor,
                    strokeWidth = EDGE_STROKE * 10,
                    cap = StrokeCap.Round
                )
            }
            if (position.first > 0) {
                drawLine(
                    start = Offset(x = 0f, y = 0f),
                    end = Offset(x = canvasWidth, y = 0f),
                    color = edgeColor,
                    strokeWidth = EDGE_STROKE * 10,
                    cap = StrokeCap.Round
                )
            }
        }
        when (cellData.value) {
            TicTacToeCellValue.BLANK -> {}
            TicTacToeCellValue.CROSS -> {
                Icon(
                    painter = painterResource(id = R.drawable.cross), contentDescription = "",
                    modifier = Modifier.size((cellSize.toFloat() / 1.5f).toInt().pxToDp()),
                    tint = if (invertColors) redColor else blueColor
                )
            }
            TicTacToeCellValue.CIRCLE -> {
                Icon(
                    painter = painterResource(id = R.drawable.circle), contentDescription = "",
                    modifier = Modifier.size(cellSizeInDp / 2),
                    tint = if (invertColors) blueColor else redColor
                )
            }
        }
    }
}

@Preview()
@Composable
fun TicTacToeCellPreview() {
    TicTacToeCell(
        cellData = TicTacToeCellData(value = 2),
        disabled = false,
        position = Pair(1, 1),
    ) { }
}
