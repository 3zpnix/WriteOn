package com.ezpnix.writeon.presentation.components

import android.annotation.SuppressLint
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AddComment
import androidx.compose.material.icons.rounded.Archive
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.CopyAll
import androidx.compose.material.icons.rounded.Translate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import com.ezpnix.writeon.presentation.screens.edit.model.EditViewModel
import com.ezpnix.writeon.presentation.screens.settings.widgets.copyToClipboard
import com.maxkeppeker.sheets.core.models.base.rememberUseCaseState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EditButton(pagerState: PagerState, coroutineScope: CoroutineScope) {
    IconButton(
        modifier = Modifier.size(56.dp),
        onClick = {
            coroutineScope.launch {
                pagerState.animateScrollToPage(0)
            }
        }
    ) {
        Icon(Icons.Rounded.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary)
    }
}

@Composable
fun TxtButton(currentText: String) {
    val context = LocalContext.current

    val clipboardManager = LocalClipboardManager.current

    val saveToFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/plain"),
        onResult = { uri ->
            if (uri != null) {
                try {
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

    IconButton(
        modifier = Modifier.size(56.dp),
        onClick = {
            clipboardManager.setText(AnnotatedString(currentText))
            Toast.makeText(context, "Exporting...", Toast.LENGTH_SHORT).show()

            saveToFileLauncher.launch("rename.txt")
        }
    ) {
        Icon(Icons.Rounded.AddComment, contentDescription = "Copy and Save")
    }
}

@Composable
fun BrowserButton() {
    val context = LocalContext.current

    IconButton(
        modifier = Modifier.size(56.dp),
        onClick = {
            val url = "https://www.startpage.com"

            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))

            context.startActivity(intent)
            Toast.makeText(context, "Opening Default Browser...", Toast.LENGTH_SHORT).show()
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

@Composable
fun CopyButton(viewModel: EditViewModel, onClick: () -> Unit) {
    val context = LocalContext.current

    IconButton(
        modifier = Modifier.size(56.dp),
        onClick = {
            copyToClipboard(context, viewModel.noteDescription.value.text)
        }
    ) {
        Icon(Icons.Rounded.CopyAll, null)
    }
}

@SuppressLint("ServiceCast")
fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = android.content.ClipData.newPlainText("Copied Text", text)
    clipboard.setPrimaryClip(clip)
}

fun openTranslateApp(context: Context, text: String) {
    val uri = Uri.parse("https://translate.google.com/?sl=auto&tl=en&text=${Uri.encode(text)}")
    val intent = Intent(Intent.ACTION_VIEW, uri)
    context.startActivity(intent)
}

//@Composable
//fun CalculatorExtend() {
//    val showDialog = remember { mutableStateOf(false) }
//
//    ExtendedFloatingActionButton(
//        modifier = Modifier.imePadding(),
//        shape = RoundedCornerShape(24.dp),
//        onClick = { showDialog.value = true }
//
//    ) {
//        Icon(Icons.Rounded.Calculate, contentDescription = null)
//    }
//
//    if (showDialog.value) {
//        AlertDialog(
//            onDismissRequest = { showDialog.value = false },
//            title = { Text("Calculator") },
//            text = {
//                CalculatorUI()
//            },
//            confirmButton = {
//                Button(onClick = { showDialog.value = false }) {
//                    Text("Close")
//                }
//            }
//        )
//    }
//}

@Composable
fun CalculatorButton() {
    val showDialog = remember { mutableStateOf(false) }

    IconButton(
        modifier = Modifier.size(56.dp),
        onClick = { showDialog.value = true }
    ) {
        Icon(Icons.Rounded.Calculate, contentDescription = null)
    }

    if (showDialog.value) {
        AlertDialog(
            onDismissRequest = { showDialog.value = false },
            title = { Text("Calculator") },
            text = {
                CalculatorUI()
            },
            confirmButton = {
                Button(onClick = { showDialog.value = false }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
fun CalculatorUI() {
    var expression by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("0") }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextField(
            value = expression,
            onValueChange = { expression = it },
            label = { Text("Enter expression") },
            readOnly = true
        )
        Spacer(modifier = Modifier.height(8.dp))

        val buttons = listOf(
            listOf("C", "( )", "%", "/"),
            listOf("7", "8", "9", "*"),
            listOf("4", "5", "6", "-"),
            listOf("1", "2", "3", "+"),
            listOf("0", ".", "<", "="),
        )

        buttons.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                row.forEach { symbol ->
                    Button(
                        onClick = {
                            when (symbol) {
                                "C" -> {
                                    expression = ""
                                    result = "0"
                                }
                                "=" -> {
                                    result = try {
                                        evalExpression(expression).toString()
                                    } catch (e: Exception) {
                                        "Error"
                                    }
                                }
                                "%" -> {
                                    if (expression.isNotEmpty()) {
                                        expression = (evalExpression(expression) / 100).toString()
                                        result = expression
                                    }
                                }
                                "( )" -> {
                                    val openCount = expression.count { it == '(' }
                                    val closeCount = expression.count { it == ')' }

                                    expression += if (openCount > closeCount) ")" else "("
                                }
                                "." -> {
                                    if (!expression.endsWith(".")) {
                                        expression += symbol
                                    }
                                }
                                "<" -> {
                                    if (expression.isNotEmpty()) {
                                        expression = expression.dropLast(1)
                                    }
                                    result = "0"
                                }
                                else -> expression += symbol
                            }
                        },
                        modifier = Modifier.size(64.dp)
                    ) {
                        Text(
                            symbol,
                            style = TextStyle(fontSize = 18.sp)
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text("Result: $result")
    }
}


fun evalExpression(expression: String): Double {
    return try {
        val sanitizedExpression = expression.replace("÷", "/").replace("×", "*")
        val regex = Regex("(-?\\d+(?:\\.\\d+)?|[+*/-])")
        val tokens = regex.findAll(sanitizedExpression).map { it.value }.toList()

        if (tokens.isEmpty()) return Double.NaN

        val numbers = mutableListOf<Double>()
        val operators = mutableListOf<Char>()

        tokens.forEach { token ->
            when {
                token.toDoubleOrNull() != null -> numbers.add(token.toDouble())
                token in listOf("+", "-", "*", "/") -> operators.add(token[0])
            }
        }

        while (operators.isNotEmpty()) {
            val index = operators.indexOfFirst { it == '*' || it == '/' }
            val i = if (index != -1) index else 0

            val num1 = numbers[i]
            val num2 = numbers[i + 1]
            val op = operators[i]

            val newValue = when (op) {
                '+' -> num1 + num2
                '-' -> num1 - num2
                '*' -> num1 * num2
                '/' -> if (num2 != 0.0) num1 / num2 else Double.NaN
                else -> Double.NaN
            }

            numbers[i] = newValue
            numbers.removeAt(i + 1)
            operators.removeAt(i)
        }

        numbers.firstOrNull() ?: Double.NaN
    } catch (e: Exception) {
        Double.NaN
    }
}

