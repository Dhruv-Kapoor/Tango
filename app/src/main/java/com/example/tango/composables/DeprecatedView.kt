package com.example.tango.composables

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.dp

@Composable
fun DeprecatedView(config: Map<String, Any>) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        val appLink = config["appLink"] as String?
        Text(
            buildAnnotatedString {
                append(config["deprecationMessage"] as String)
                append("\n\n")
                if (appLink?.isNotBlank() == true) {
                    withLink(
                        LinkAnnotation.Url(
                            appLink,
                            TextLinkStyles(style = SpanStyle(color = Color.Blue))
                        )
                    ) {
                        append("Click here")
                    }
                    append(" to download latest version!")
                }
            }
        )
    }
}