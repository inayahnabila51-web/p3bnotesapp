package com.example.notesapp

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.notesapp.ui.theme.NotesAppTheme

@Composable
fun NotesScreen(
    modifier: Modifier = Modifier,
    vm: NotesViewModel = viewModel()
) {
    val notes by vm.notes.collectAsState()

    // State form (dipakai bersama untuk Add dan Edit)
    var showForm by rememberSaveable { mutableStateOf(false) }
    var editingId by rememberSaveable { mutableStateOf<Long?>(null) } // null = mode Add
    var formTitle by rememberSaveable { mutableStateOf("") }
    var formNote by rememberSaveable { mutableStateOf("") }

    fun closeForm() {
        showForm = false
        editingId = null
        formTitle = ""
        formNote = ""
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(text = "Catatan Saya", fontSize = 24.sp, fontWeight = FontWeight.Bold)

            Text(
                text = "Jumlah catatan: ${notes.size}",
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            // Daftar catatan (READ)
            if (notes.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Belum ada catatan", color = Color.Gray)
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(notes, key = { it.id }) { note ->
                        NoteItem(
                            note = note,
                            onEdit = {
                                editingId = note.id
                                formTitle = note.title
                                formNote = note.note
                                showForm = true
                            },
                            onDelete = { vm.deleteNote(note) }
                        )
                        HorizontalDivider()
                    }
                }
            }
        }

        // Tombol tambah (membuka form Add)
        FloatingActionButton(
            onClick = {
                editingId = null
                formTitle = ""
                formNote = ""
                showForm = true
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(painterResource(R.drawable.ic_add), contentDescription = "Tambah catatan")
        }
    }

    // Form Add / Edit
    if (showForm) {
        NoteFormDialog(
            isEdit = editingId != null,
            title = formTitle,
            note = formNote,
            onTitleChange = { formTitle = it },
            onNoteChange = { formNote = it },
            onSave = {
                if (formTitle.isNotBlank()) {
                    val target = notes.find { it.id == editingId }
                    if (target != null) {
                        vm.updateNote(target, formTitle, formNote)   // UPDATE
                    } else {
                        vm.addNote(formTitle, formNote)              // CREATE
                    }
                    closeForm()
                }
            },
            onDismiss = { closeForm() }
        )
    }
}

@Composable
fun NoteItem(
    note: Note,
    onEdit: () -> Unit = {},
    onDelete: () -> Unit = {}
) {
    ListItem(
        headlineContent = {
            Text(note.title, fontWeight = FontWeight.Bold)
        },
        supportingContent = {
            Text(note.note, maxLines = 3, overflow = TextOverflow.Ellipsis)
        },
        trailingContent = {
            Row {
                IconButton(onClick = onEdit) {
                    Icon(
                        painterResource(R.drawable.ic_edit), "Edit",
                        tint = Color(0xFF2196F3)
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        painterResource(R.drawable.ic_delete), "Hapus",
                        tint = Color(0xFFF44336)
                    )
                }
            }
        }
    )
}

@Composable
fun NoteFormDialog(
    isEdit: Boolean,
    title: String,
    note: String,
    onTitleChange: (String) -> Unit,
    onNoteChange: (String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isEdit) "Edit Catatan" else "Tambah Catatan") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = onTitleChange,
                    label = { Text("Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = note,
                    onValueChange = onNoteChange,
                    label = { Text("Note") },
                    minLines = 3,
                    maxLines = 6,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = onSave) {
                Text(if (isEdit) "Simpan" else "Tambah")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun NoteItemPreview() {
    NotesAppTheme {
        NoteItem(
            note = Note(
                id = 1,
                title = "Belajar Room Database",
                note = "Entity, DAO, dan Database adalah tiga komponen utama Room."
            )
        )
    }
}