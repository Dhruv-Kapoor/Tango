package com.example.tango.composables

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.tango.QUEENS_EDGE_THICKNESS_FACTOR
import com.example.tango.R
import com.example.tango.dataClasses.SYMBOLS
import com.example.tango.dataClasses.TangoCellData
import com.example.tango.dataClasses.TangoCellValue


@Composable
fun <T> Grid(
    grid: Array<Array<T>>?,
    modifier: Modifier = Modifier,
    cellComposable: @Composable RowScope.(T, Int, Int) -> Unit
) {
    val edgeColor = colorResource(R.color.border_color)

    Box(Modifier, contentAlignment = Alignment.Center) {
        Column(
            modifier = modifier
                .border(
                    width = 0.5.dp,
                    color = edgeColor,
                    shape = RoundedCornerShape(4.dp)
                )
                .clip(RoundedCornerShape(4.dp)),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            grid?.forEachIndexed { i, row ->
                Row {
                    row.forEachIndexed { j, cell ->
                        cellComposable(cell, i, j)
                    }
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun TangoGridPreview(onCompleted: (() -> Unit)? = null) {
    var grid = arrayOf(
        arrayOf(
            TangoCellData(TangoCellValue.SUN, leftSymbol = SYMBOLS.NONE),
            TangoCellData(TangoCellValue.MOON, topSymbol = SYMBOLS.NONE),
            TangoCellData(TangoCellValue.MOON, topSymbol = SYMBOLS.NONE),
            TangoCellData(TangoCellValue.SUN, topSymbol = SYMBOLS.NONE),
            TangoCellData(TangoCellValue.MOON, topSymbol = SYMBOLS.NONE),
            TangoCellData(TangoCellValue.SUN, topSymbol = SYMBOLS.NONE)
        ),
        arrayOf(
            TangoCellData(TangoCellValue.MOON, leftSymbol = SYMBOLS.NONE, disabled = true),
            TangoCellData(TangoCellValue.SUN),
            TangoCellData(TangoCellValue.SUN, leftSymbol = SYMBOLS.EQUALS),
            TangoCellData(TangoCellValue.MOON),
            TangoCellData(TangoCellValue.SUN),
            TangoCellData(TangoCellValue.MOON)
        ),
        arrayOf(
            TangoCellData(TangoCellValue.BLANK, leftSymbol = SYMBOLS.NONE),
            TangoCellData(TangoCellValue.BLANK, leftSymbol = SYMBOLS.CROSS),
            TangoCellData(TangoCellValue.SUN),
            TangoCellData(TangoCellValue.MOON, disabled = true),
            TangoCellData(TangoCellValue.MOON),
            TangoCellData(TangoCellValue.SUN)
        ),
        arrayOf(
            TangoCellData(TangoCellValue.SUN, leftSymbol = SYMBOLS.NONE),
            TangoCellData(TangoCellValue.SUN),
            TangoCellData(TangoCellValue.MOON, disabled = true),
            TangoCellData(TangoCellValue.SUN),
            TangoCellData(TangoCellValue.MOON),
            TangoCellData(
                TangoCellValue.MOON,
                leftSymbol = SYMBOLS.EQUALS,
                topSymbol = SYMBOLS.CROSS
            )
        ),
        arrayOf(
            TangoCellData(TangoCellValue.MOON, leftSymbol = SYMBOLS.NONE),
            TangoCellData(TangoCellValue.SUN),
            TangoCellData(TangoCellValue.MOON),
            TangoCellData(TangoCellValue.SUN),
            TangoCellData(TangoCellValue.BLANK, leftSymbol = SYMBOLS.EQUALS),
            TangoCellData(TangoCellValue.MOON, disabled = true)
        ),
        arrayOf(
            TangoCellData(TangoCellValue.MOON, leftSymbol = SYMBOLS.NONE),
            TangoCellData(TangoCellValue.MOON),
            TangoCellData(TangoCellValue.SUN),
            TangoCellData(TangoCellValue.MOON),
            TangoCellData(TangoCellValue.SUN, topSymbol = SYMBOLS.EQUALS),
            TangoCellData(TangoCellValue.SUN)
        ),
    )
    Box(
        Modifier
            .height(420.dp)
            .width(420.dp),
        contentAlignment = Alignment.Center
    ) {
//        Grid(grid) {
//            onCompleted?.invoke()
//        }
    }
}
