package com.example.tango.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.example.tango.R
import com.example.tango.dataClasses.LeaderboardItem
import com.example.tango.dataClasses.User
import com.example.tango.utils.Utils.conditional
import com.example.tango.utils.Utils.formatDate
import com.example.tango.utils.Utils.formatTime
import java.util.Date
import kotlin.random.Random

@OptIn(ExperimentalGlideComposeApi::class)
@Preview(showBackground = true)
@Composable
fun LeaderboardRow(
    item: LeaderboardItem = LeaderboardItem(
        user = User(
            name = "Dhruv Kapoor",
            profilePicUrl = "https://lh3.googleusercontent.com/a/ACg8ocIskqG1VKSC6MfSszFHmofjTmXlaZMrOm4IDcnrMpLaC9G4Rg=s96-c",
            email = "dhruv",
            id = ""
        ),
        timeTaken = 70,
        attempts = emptyList()
    ),
    placement: Int = 5,
    currentUser: Boolean = false
) {
    val color: Pair<Color, Color> = getColorForPlacement(placement)
    var expanded by rememberSaveable { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape = RoundedCornerShape(24.dp))
            .background(colorResource(R.color.white))
            .padding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .conditional(currentUser) {
                    border(2.dp, color = Color.Blue, shape = RoundedCornerShape(24.dp))
                }
                .clip(shape = RoundedCornerShape(24.dp))
                .background(color.first)
                .clickable(enabled = true) {
                    expanded = !expanded
                }
                .padding(top = 14.dp, start = 14.dp, end = 24.dp, bottom = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            GlideImage(
                modifier = Modifier
                    .size(48.dp)
                    .clip(shape = RoundedCornerShape(24.dp)),
                model = item.user.profilePicUrl,
                contentDescription = "profile"
            )
            Column(
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .weight(1f)
            ) {
                Text(modifier = Modifier.padding(0.dp), text = item.user.name, fontSize = 16.sp)
                Text(
                    modifier = Modifier.padding(0.dp),
                    textAlign = TextAlign.Center,
                    text = "took ${formatTime(item.timeTaken)}",
                    fontSize = 12.sp
                )
            }
            Text(
                text = placement.toString(),
                color = Color.White,
                modifier = Modifier
                    .background(color.second, shape = RoundedCornerShape(16.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }

        if (expanded) {
            Column(
                modifier = Modifier.padding(
                    top = 8.dp,
                    start = 16.dp,
                    end = 16.dp,
                    bottom = 16.dp
                )
            ) {
                Text("All Attempts", fontSize = 14.sp)
                item.attempts.forEachIndexed { index, attempt ->
                    val timeInSeconds = attempt["timeTaken"] as Int
                    val time = "%d:%02d".format(timeInSeconds / 60, timeInSeconds % 60)
                    Text(
                        "${index + 1}. took $time on ${formatDate(attempt["attemptedOn"] as Date)}",
                        fontSize = 14.sp
                    )
                }
            }
        }
    }


}

fun getColorForPlacement(i: Int): Pair<Color, Color> {
    if (i == 1) {
        return Pair(Color(0xFFFFD700), Color(0xFFFFA407))
    } else if (i == 2) {
        return Pair(Color(0xFFCCC8C8), Color(0xFFA8A8A8))
    } else if (i == 3) {
        return Pair(Color(0xFFDAA266), Color(0xFFB87333))
    }
    val colorPallet = arrayOf(
        Pair(Color(0xFFFFF5E6), Color(0xFFFFB347)),
        Pair(Color(0xFFF8E8EE), Color(0xFFE5B0D6)),
        Pair(Color(0xFFEAEEF7), Color(0xFF8E9AAF)),
        Pair(Color(0xFFFDF0D5), Color(0xFFD4A59A)),
        Pair(Color(0xFFF5F0F8), Color(0xFFB399D4)),
        Pair(Color(0xFFF0F7F4), Color(0xFF87BBA2)),
        Pair(Color(0xFFE6F4F1), Color(0xFF5FB0B7)),
        Pair(Color(0xFFF8F5F2), Color(0xFFE8D5C0)),
        Pair(Color(0xFFF9F7E8), Color(0xFFD6E6A9)),
        Pair(Color(0xFFF3F3F3), Color(0xFFC4C4C4))
    )
    return colorPallet[Random.nextInt(colorPallet.size)]
}
