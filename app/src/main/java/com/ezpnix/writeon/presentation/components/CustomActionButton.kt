package com.ezpnix.writeon.presentation.components

import android.annotation.SuppressLint
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Archive
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Translate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import com.maxkeppeler.sheets.calendar.CalendarDialog
import com.maxkeppeler.sheets.calendar.models.CalendarStyle
import com.maxkeppeler.sheets.calendar.models.CalendarSelection
import com.maxkeppeler.sheets.calendar.models.CalendarConfig
import java.time.LocalDate
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import com.ezpnix.writeon.presentation.screens.edit.model.EditViewModel
import com.ezpnix.writeon.presentation.screens.settings.widgets.copyToClipboard
import com.maxkeppeker.sheets.core.models.base.rememberUseCaseState
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

@Composable
fun AgreeButton(
    text: String,
    onClick: () -> Unit
) {
    ExtendedFloatingActionButton(
        modifier = Modifier.imePadding(),
        shape = RoundedCornerShape(24.dp),
        onClick = { onClick() },
        icon = { Icon(Icons.Rounded.CheckCircle, text) },
        text = { Text(text = text) },
    )
}

@Composable
fun CenteredNotesButton(
    onFirstClick: String,
    onSecondClick: () -> Unit,
    onThirdClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.width(46.dp))
        TextButton(onClick = onThirdClick)
        Spacer(modifier = Modifier.width(16.dp))
        NotesButton(text = onFirstClick, onClick = onSecondClick)
        Spacer(modifier = Modifier.width(16.dp))
        CalendarButton()
        Spacer(modifier = Modifier.width(16.dp))
    }
}

@Composable
fun TextButton(onClick: () -> Unit) {
    val activity = LocalContext.current
    val context = LocalContext.current

    // State for the dialog
    var showDialog by remember { mutableStateOf(false) }
    var textState by remember { mutableStateOf("") }

    // Start the file creation process with an Intent
    val openFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/plain"), // Specify MIME type for text files
        onResult = { uri ->
            if (uri != null) {
                // File URI is obtained, save the text content
                try {
                    val outputStream = context.contentResolver.openOutputStream(uri)
                    outputStream?.write(textState.toByteArray())
                    outputStream?.close()

                    // Show success message
                    Toast.makeText(activity, "Text saved successfully", Toast.LENGTH_SHORT).show()

                } catch (e: Exception) {
                    // Handle exceptions like permission issues
                    Toast.makeText(activity, "Error saving file: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    )

    ExtendedFloatingActionButton(
        modifier = Modifier.size(56.dp),
        shape = RoundedCornerShape(24.dp),
        onClick = {
            showDialog = true // Show the dialog when button is clicked
            onClick()
        }
    ) {
        Icon(Icons.Rounded.Add, contentDescription = null)
    }

    // Show popup dialog when showDialog is true
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(text = "Quick Note") },
            text = {
                TextField(
                    value = textState,
                    onValueChange = { textState = it },
                    label = { Text("Enter text") }
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        // Open the file picker to let the user choose the location to save
                        openFileLauncher.launch("rename.txt")
                        showDialog = false // Close the dialog
                    }
                ) {
                    Text("Save as TXT")
                }
            },
            dismissButton = {
                Button(onClick = { showDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}


@Composable
fun NotesButton(
    text: String,
    onClick: () -> Unit
) {
    val context = LocalContext.current

    ExtendedFloatingActionButton(
        modifier = Modifier.imePadding(),
        shape = RoundedCornerShape(24.dp),
        onClick = { Toast.makeText(context, "< Swipe from the left for Edit Mode\n Swipe from the right for View Mode >", Toast.LENGTH_SHORT).show()
            onClick() },
        icon = { Icon(Icons.Rounded.Edit, null) },
        text = { Text(text = text) },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarButton() {
    val selectedDates = remember { mutableStateOf<List<LocalDate>>(listOf()) }
    val disabledDates = listOf(
        LocalDate.now().minusDays(0),
    )
    val calendarState = rememberUseCaseState()
    val context = LocalContext.current
    val currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM d, yyyy"))
    val dayOfWeek = SimpleDateFormat("EEEE", Locale.getDefault()).format(Date())

    ExtendedFloatingActionButton(
        modifier = Modifier.size(56.dp),
        shape = RoundedCornerShape(24.dp),
        onClick = {
            Toast.makeText(context, "Today is: $dayOfWeek, $currentDate", Toast.LENGTH_SHORT).show()
            calendarState.show()
        }

    ) {
        Icon(Icons.Rounded.CalendarMonth, contentDescription = "Calendar")
    }

    CalendarDialog(
        state = calendarState,
        config = CalendarConfig(
            yearSelection = true,
            monthSelection = true,
            style = CalendarStyle.MONTH,
            disabledDates = disabledDates
        ),
        selection = CalendarSelection.Dates { newDates ->
            selectedDates.value = newDates
        }
    )
}

@Composable
fun TxtButton(currentText: String) {
    val context = LocalContext.current

    // Clipboard manager to copy text
    val clipboardManager = LocalClipboardManager.current

    // Start the file creation process with an Intent
    val saveToFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/plain"),
        onResult = { uri ->
            if (uri != null) {
                try {
                    // Save the text into the file
                    context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                        outputStream.write(currentText.toByteArray())
                    }
                    Toast.makeText(context, "Text saved successfully", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "Error saving file: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    )

    // Floating action button
    IconButton(
        modifier = Modifier.size(56.dp),
        onClick = {
            // Copy text to clipboard
            clipboardManager.setText(AnnotatedString(currentText))
            Toast.makeText(context, "Exporting...", Toast.LENGTH_SHORT).show()

            // Trigger file saving
            saveToFileLauncher.launch("rename.txt")
        }
    ) {
        Icon(Icons.Rounded.Archive, contentDescription = "Copy and Save")
    }
}

@Composable
fun BrowserButton() {
    val context = LocalContext.current

    IconButton(
        onClick = {
            val url = "https://www.startpage.com"

            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))

            context.startActivity(intent)
        }
    )
    {
        Icon(Icons.Rounded.Search, null)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalButton() {
    val selectedDates = remember { mutableStateOf<List<LocalDate>>(listOf()) }
    val disabledDates = listOf(
        LocalDate.now().minusDays(0),
    )
    val calendarState = rememberUseCaseState()
    val context = LocalContext.current
    val currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM d, yyyy"))
    val dayOfWeek = SimpleDateFormat("EEEE", Locale.getDefault()).format(Date())

    IconButton(
        modifier = Modifier.size(56.dp),
        onClick = {
            Toast.makeText(context, "Today is: $dayOfWeek, $currentDate", Toast.LENGTH_SHORT).show()
            calendarState.show()
        }

    ) {
        Icon(Icons.Rounded.CalendarMonth, contentDescription = "Calendar")
    }

    CalendarDialog(
        state = calendarState,
        config = CalendarConfig(
            yearSelection = true,
            monthSelection = true,
            style = CalendarStyle.MONTH,
            disabledDates = disabledDates
        ),
        selection = CalendarSelection.Dates { newDates ->
            selectedDates.value = newDates
        }
    )
}

@Composable
fun TranslateButton(viewModel: EditViewModel, onClick: () -> Unit) {
    val context = LocalContext.current

    IconButton(
        modifier = Modifier.size(56.dp),
        onClick = {
            copyToClipboard(context, viewModel.noteDescription.value.text)
            openTranslateApp(context, viewModel.noteDescription.value.text)
        }
    ) {
        Icon(Icons.Rounded.Translate, null)
    }
}

@SuppressLint("ServiceCast")
fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = android.content.ClipData.newPlainText("Copied Text", text)
    clipboard.setPrimaryClip(clip)
}

fun openTranslateApp(context: Context, text: String) {
    // Google Translate Intent
    val uri = Uri.parse("https://translate.google.com/?sl=auto&tl=en&text=${Uri.encode(text)}")
    val intent = Intent(Intent.ACTION_VIEW, uri)
    context.startActivity(intent)
}
