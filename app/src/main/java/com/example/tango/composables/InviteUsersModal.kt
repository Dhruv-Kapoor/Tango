package com.example.tango.composables

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.example.tango.R
import com.example.tango.dataClasses.User
import com.example.tango.viewmodels.InviteUsersModalViewModel

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun UserRow(user: User, invited: Boolean = true, onInvite: (User) -> Unit) {
    var invited by remember { mutableStateOf(invited) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 14.dp, start = 14.dp, end = 24.dp, bottom = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        GlideImage(
            modifier = Modifier
                .size(48.dp)
                .clip(shape = RoundedCornerShape(24.dp)),
            model = user.profilePicUrl,
            contentDescription = "profile"
        )
        Column(
            modifier = Modifier
                .padding(horizontal = 12.dp)
                .weight(1f)
        ) {
            Text(modifier = Modifier.padding(0.dp), text = user.name, fontSize = 16.sp)
        }

        IconButton(enabled = !invited, onClick = {
            onInvite(user)
            invited = true
        }) {
            if (invited) {
                Icon(
                    modifier = Modifier.size(24.dp),
                    painter = painterResource(R.drawable.tick),
                    contentDescription = null,
                    tint = Color.Blue
                )
            } else {
                Icon(
                    modifier = Modifier.size(24.dp),
                    painter = painterResource(R.drawable.add),
                    contentDescription = null
                )
            }
        }
    }
}


@Preview()
@Composable
fun InviteUsersModal(
    onDismissRequest: () -> Unit = {},
    viewModel: InviteUsersModalViewModel = viewModel(),
    onInvite: (User) -> Unit = {}
) {
    val loading by viewModel.loading.collectAsState()
    val users by viewModel.users.collectAsState()
//    val invitedUsers by viewModel.invitedUsers.collectAsState()

    val invitedUsers = remember { hashSetOf<String>() }
    Dialog(
        onDismissRequest = { onDismissRequest() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier.padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.background,
            )
        ) {
            if (loading) {
                CircularProgressIndicator()
            } else {
                LazyColumn(
                    modifier = Modifier
                        .padding(horizontal = 8.dp, vertical = 16.dp)
                ) {
                    items(users) {
                        UserRow(it, invited = it.id in invitedUsers, onInvite = {
//                            viewModel.inviteUser(it)
                            onInvite(it)
                            invitedUsers.add(it.id)
                        })
                    }
                }
            }
        }
    }
}