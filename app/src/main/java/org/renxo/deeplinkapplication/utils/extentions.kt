package org.renxo.deeplinkapplication.utils

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.json.Json
import org.renxo.deeplinkapplication.ui.theme.AppColors


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GetAlertDialogue(onDismissRequest: () -> Unit = {}, content: @Composable () -> Unit) {
    BasicAlertDialog(
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        ),
        onDismissRequest = onDismissRequest,
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape = RoundedCornerShape(20.dp))
            .background(AppColors.backgroundColor)
            .padding(vertical = 30.dp, horizontal = 15.dp)
    ) {
        content()
    }
}


@Composable
inline fun GetOneTimeBlock(crossinline block: suspend CoroutineScope.() -> Unit) =
    LaunchedEffect(Unit) {
        block()
    }



val json = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
}