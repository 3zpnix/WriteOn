package com.ezpnix.writeon.presentation.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.ezpnix.writeon.presentation.screens.settings.model.Flashcard
import com.ezpnix.writeon.presentation.screens.settings.model.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlashcardScreen(
    navController: NavController,
    settingsViewModel: SettingsViewModel
) {
    val context = LocalContext.current

    val flashcards = remember { mutableStateListOf<Flashcard>().apply { addAll(settingsViewModel.loadFlashcards()) } }
    var newWord by rememberSaveable { mutableStateOf("") }
    var newMeaning by rememberSaveable { mutableStateOf("") }
    var answerInput by rememberSaveable { mutableStateOf("") }
    var isTesting by rememberSaveable { mutableStateOf(false) }
    var currentCardIndex by rememberSaveable { mutableStateOf(0) }
    var testEnded by rememberSaveable { mutableStateOf(false) }
    var editingIndex by rememberSaveable { mutableStateOf(-1) }
    var feedbackMessage by rememberSaveable { mutableStateOf("") }
    var answerSubmitted by rememberSaveable { mutableStateOf(false) }
    var correctAnswers by rememberSaveable { mutableStateOf(0) }
    var wrongAnswers by rememberSaveable { mutableStateOf(0) }

    fun saveFlashcards() {
        settingsViewModel.saveFlashcards(flashcards)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Flashcards") },
                navigationIcon = {
                    IconButton(onClick = {
                        saveFlashcards()
                        navController.navigateUp()
                    }) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        content = { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                if (!isTesting) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Add Flashcard", style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(8.dp))
                            OutlinedTextField(
                                value = newWord,
                                onValueChange = { newWord = it },
                                label = { Text("Question") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(Modifier.height(8.dp))
                            OutlinedTextField(
                                value = newMeaning,
                                onValueChange = { newMeaning = it },
                                label = { Text("Answer") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(Modifier.height(8.dp))
                            Button(onClick = {
                                if (newWord.isNotBlank() && newMeaning.isNotBlank()) {
                                    flashcards.add(Flashcard(newWord, newMeaning))
                                    newWord = ""
                                    newMeaning = ""
                                    saveFlashcards()
                                }
                            }) {
                                Text("Add")
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    LazyColumn(modifier = Modifier.weight(1f)) {
                        itemsIndexed(flashcards) { index, card ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                elevation = CardDefaults.cardElevation(2.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    if (editingIndex == index) {
                                        OutlinedTextField(
                                            value = card.word,
                                            onValueChange = { flashcards[index] = card.copy(word = it) },
                                            label = { Text("Question") },
                                            singleLine = true
                                        )
                                        Spacer(Modifier.height(8.dp))
                                        OutlinedTextField(
                                            value = card.meaning,
                                            onValueChange = { flashcards[index] = card.copy(meaning = it) },
                                            label = { Text("Answer") },
                                            singleLine = true
                                        )
                                    } else {
                                        Text("${index + 1}. ${card.word} — ${card.meaning}")
                                    }
                                    Spacer(Modifier.height(8.dp))
                                    Row(
                                        horizontalArrangement = Arrangement.End,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        IconButton(onClick = {
                                            flashcards.removeAt(index)
                                            saveFlashcards()
                                        }) {
                                            Icon(Icons.Filled.Delete, contentDescription = "Delete")
                                        }
                                        IconButton(onClick = {
                                            editingIndex = if (editingIndex == index) -1 else index
                                            if (editingIndex == -1) saveFlashcards()
                                        }) {
                                            Icon(Icons.Filled.Edit, contentDescription = "Edit")
                                        }
                                        if (editingIndex == index) {
                                            IconButton(onClick = {
                                                editingIndex = -1
                                                saveFlashcards()
                                            }) {
                                                Icon(Icons.Filled.Save, contentDescription = "Save")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    Button(onClick = {
                        if (flashcards.isNotEmpty()) {
                            isTesting = true
                            testEnded = false
                            currentCardIndex = 0
                            answerInput = ""
                            feedbackMessage = ""
                            answerSubmitted = false
                            correctAnswers = 0
                            wrongAnswers = 0
                        }
                    }, modifier = Modifier.fillMaxWidth()) {
                        Text("Start Test")
                    }
                } else if (!testEnded && currentCardIndex < flashcards.size) {
                    val currentCard = flashcards[currentCardIndex]

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            "Card ${currentCardIndex + 1} of ${flashcards.size}",
                            style = MaterialTheme.typography.titleSmall
                        )
                        Spacer(Modifier.height(8.dp))

                        Text("Word: ${currentCard.word}", style = MaterialTheme.typography.titleLarge)
                        Spacer(Modifier.height(12.dp))

                        OutlinedTextField(
                            value = answerInput,
                            onValueChange = { answerInput = it },
                            label = { Text("Type the meaning") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = {
                                if (!answerSubmitted && answerInput.isNotBlank()) {
                                    answerSubmitted = true
                                    if (answerInput.trim().equals(currentCard.meaning.trim(), ignoreCase = true)) {
                                        feedbackMessage = "Correct!"
                                        correctAnswers++
                                    } else {
                                        feedbackMessage = "Wrong! Correct answer: ${currentCard.meaning}"
                                        wrongAnswers++
                                    }
                                }
                            }),
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !answerSubmitted
                        )

                        Spacer(Modifier.height(8.dp))

                        Button(
                            onClick = {
                                if (!answerSubmitted && answerInput.isNotBlank()) {
                                    answerSubmitted = true
                                    if (answerInput.trim().equals(currentCard.meaning.trim(), ignoreCase = true)) {
                                        feedbackMessage = "Correct!"
                                        correctAnswers++
                                    } else {
                                        feedbackMessage = "Wrong! Correct answer: ${currentCard.meaning}"
                                        wrongAnswers++
                                    }
                                }
                            },
                            enabled = !answerSubmitted
                        ) {
                            Text("Enter")
                        }

                        Spacer(Modifier.height(8.dp))

                        if (feedbackMessage.isNotEmpty()) {
                            Text(
                                feedbackMessage,
                                color = if (feedbackMessage.startsWith("Correct")) Color.Green else Color.Red
                            )
                            Spacer(Modifier.height(8.dp))
                            Button(onClick = {
                                currentCardIndex++
                                feedbackMessage = ""
                                answerInput = ""
                                answerSubmitted = false
                            }) {
                                Text("Next")
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            elevation = CardDefaults.cardElevation(4.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Finished?", style = MaterialTheme.typography.titleMedium)
                                Spacer(Modifier.height(8.dp))
                                Button(
                                    onClick = {
                                        testEnded = true
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("End Test")
                                }
                            }
                        }
                    }
                } else {
                    Text("Test complete!", style = MaterialTheme.typography.headlineSmall)
                    Spacer(Modifier.height(8.dp))
                    Text("✅ Correct answers: $correctAnswers", color = Color.Green)
                    Text("❌ Wrong answers: $wrongAnswers", color = Color.Red)
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = {
                        isTesting = false
                        testEnded = false
                        currentCardIndex = 0
                        answerInput = ""
                        feedbackMessage = ""
                        answerSubmitted = false
                        correctAnswers = 0
                        wrongAnswers = 0
                    }, modifier = Modifier.fillMaxWidth()) {
                        Text("Restart")
                    }
                }
            }
        }
    )
}
