package com.ezpnix.writeon.presentation.components.markdown

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.ezpnix.writeon.presentation.screens.settings.settings.shapeManager

@Composable
fun MarkdownCodeBlock(
    color: Color,
    text: @Composable () -> Unit
) {
    Box(
        modifier = Modifier.padding(top = 6.dp),
        content = {
            Surface(
                color = color,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .fillMaxWidth(),
                content = {
                    text()
                }
            )
        }
    )
}

@Composable
fun MarkdownQuote(
    content: String,
    fontSize: TextUnit,
    containerColor: Color = MaterialTheme.colorScheme.surface
) {
    Row(horizontalArrangement = Arrangement.Center) {
        Box(
            modifier = Modifier
                .height(22.dp)
                .width(6.dp)
                .background(
                    color = if (containerColor == MaterialTheme.colorScheme.surface)
                        MaterialTheme.colorScheme.surfaceContainerLow
                    else
                        MaterialTheme.colorScheme.surfaceContainer,
                    shape = RoundedCornerShape(16.dp)
                )
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = buildString(" $content"),
            fontSize = fontSize
        )
    }
}

@Composable
fun MarkdownCheck(content: @Composable () -> Unit, checked: Boolean, onCheckedChange: ((Boolean) -> Unit)?) {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier
                .padding(end = 12.dp)
                .size(20.dp)
        )
        content()
    }
}

@Composable
fun MarkdownText(
    radius: Int,
    markdown: String,
    isPreview: Boolean = false,
    isEnabled: Boolean,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    modifier: Modifier = Modifier.fillMaxWidth(),
    weight: FontWeight = FontWeight.Normal,
    fontSize: TextUnit,
    spacing: Dp = 2.dp,
    onContentChange: (String) -> Unit = {},
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip
) {
    if (!isEnabled) {
        StaticMarkdownText(
            markdown = markdown,
            modifier = modifier,
            weight = weight,
            fontSize = fontSize,
            maxLines = maxLines,
            overflow = overflow
        )
        return
    }

    val lines = markdown.lines()
    val lineProcessors = listOf(
        HeadingProcessor(),
        ListItemProcessor(),
        CodeBlockProcessor(),
        QuoteProcessor(),
        ImageInsertionProcessor(),
        CheckboxProcessor()
    )
    val markdownBuilder = MarkdownBuilder(lines, lineProcessors)
    markdownBuilder.parse()

    MarkdownContent(
        radius = radius,
        isPreview = isPreview,
        content = markdownBuilder.content,
        containerColor = containerColor,
        modifier = modifier,
        spacing = spacing,
        weight = weight,
        fontSize = fontSize,
        lines = lines,
        onContentChange = onContentChange,
        maxLines = maxLines,
        overflow = overflow
    )
}

@Composable
fun StaticMarkdownText(
    markdown: String,
    modifier: Modifier,
    weight: FontWeight,
    fontSize: TextUnit,
    maxLines: Int,
    overflow: TextOverflow
) {
    Text(
        text = markdown,
        fontSize = fontSize,
        fontWeight = weight,
        modifier = modifier,
        maxLines = maxLines,
        overflow = overflow
    )
}

@Composable
fun MarkdownContent(
    radius: Int,
    isPreview: Boolean,
    content: List<MarkdownElement>,
    containerColor: Color,
    modifier: Modifier,
    spacing: Dp,
    weight: FontWeight,
    fontSize: TextUnit,
    lines: List<String>,
    onContentChange: (String) -> Unit,
    maxLines: Int,
    overflow: TextOverflow
) {
    if (isPreview) {
        Column(modifier = modifier) {
            content.take(4).forEachIndexed { index, _ ->
                RenderMarkdownElement(
                    radius = radius,
                    index = index,
                    content = content,
                    containerColor = containerColor,
                    weight = weight,
                    fontSize = fontSize,
                    lines = lines,
                    isPreview = true,
                    onContentChange = onContentChange,
                    maxLines = maxLines,
                    overflow = overflow
                )
                Spacer(modifier = Modifier.height(spacing))
            }
        }
    } else {
        SelectionContainer {
            LazyColumn(modifier = modifier) {
                items(content.size) { index ->
                    Spacer(modifier = Modifier.height(spacing))
                    RenderMarkdownElement(
                        radius = radius,
                        content = content,
                        index = index,
                        containerColor = containerColor,
                        weight = weight,
                        fontSize = fontSize,
                        lines = lines,
                        isPreview = isPreview,
                        onContentChange = onContentChange,
                        maxLines = maxLines,
                        overflow = overflow
                    )
                }
            }
        }
    }
}

@Composable
fun RenderMarkdownElement(
    radius: Int,
    content: List<MarkdownElement>,
    index: Int,
    containerColor: Color,
    weight: FontWeight,
    fontSize: TextUnit,
    lines: List<String>,
    isPreview: Boolean,
    onContentChange: (String) -> Unit,
    maxLines: Int,
    overflow: TextOverflow
) {
    val element = content[index]
    Row {
        when (element) {
            is Heading -> {
                Text(
                    text = buildString(element.text, weight),
                    fontSize = when (element.level) {
                        in 1..6 -> (28 - (2 * element.level) - fontSize.value / 3).sp
                        else -> fontSize
                    },
                    fontWeight = weight,
                    modifier = Modifier.padding(vertical = 10.dp),
                    maxLines = maxLines,
                    overflow = overflow
                )
            }
            is CheckboxItem -> {
                MarkdownCheck(
                    content = {
                        Text(
                            text = buildString(element.text, weight),
                            fontSize = fontSize,
                            fontWeight = weight,
                            maxLines = maxLines,
                            overflow = overflow
                        )
                    },
                    checked = element.checked,
                    onCheckedChange = { newChecked ->
                        val updatedLines = lines.toMutableList()
                        val prefix = if (newChecked) "[x]" else "[ ]"
                        updatedLines[element.lineNumber] = "$prefix ${element.text}"
                        val updatedMarkdown = updatedLines.joinToString("\n")
                        onContentChange(updatedMarkdown)
                    }
                )
            }
            is ListItem -> {
                Text(
                    text = buildString("• ${element.text}", weight),
                    fontSize = fontSize,
                    fontWeight = weight,
                    maxLines = maxLines,
                    overflow = overflow
                )
            }
            is Quote -> {
                MarkdownQuote(
                    content = element.text,
                    fontSize = fontSize,
                    containerColor = containerColor
                )
            }
            is ImageInsertion -> {
                AsyncImage(
                    model = element.photoUri,
                    contentDescription = "Note Image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp)
                        .clip(RoundedCornerShape(radius.dp)),
                    contentScale = ContentScale.Fit,
                    alignment = Alignment.Center,
                    onError = {
                        android.util.Log.e("MarkdownText", "Failed to load image: ${element.photoUri}")
                    }
                )
            }
            is CodeBlock -> {
                MarkdownCodeBlock(
                    color = if (isSystemInDarkTheme()) {
                        MaterialTheme.colorScheme.surfaceContainerLow
                    } else {
                        MaterialTheme.colorScheme.surfaceContainerHigh
                    },
                    text = {
                        Text(
                            text = element.code,
                            fontSize = fontSize,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(8.dp),
                            maxLines = maxLines,
                            overflow = overflow
                        )
                    }
                )
            }
            is NormalText -> {
                Text(
                    text = buildString(element.text, weight),
                    fontSize = fontSize,
                    maxLines = maxLines,
                    overflow = overflow
                )
            }
        }
    }
}