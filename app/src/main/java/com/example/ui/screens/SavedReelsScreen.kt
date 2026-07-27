package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.FactCheckStatus
import com.example.data.db.SavedReel
import com.example.ui.MainViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SavedReelsScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val savedReels by viewModel.savedReels.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedTagFilter by remember { mutableStateOf("All") }
    var selectedReelForDetail by remember { mutableStateOf<SavedReel?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }

    val allTags = remember(savedReels) {
        val set = mutableSetOf("All")
        savedReels.forEach { reel ->
            reel.tags.split(",").map { it.trim() }.filter { it.isNotEmpty() }.forEach { set.add(it) }
        }
        set.toList()
    }

    val filteredReels = savedReels.filter { reel ->
        val matchesSearch = searchQuery.isBlank() ||
                reel.caption.contains(searchQuery, ignoreCase = true) ||
                reel.tags.contains(searchQuery, ignoreCase = true)
        val matchesTag = selectedTagFilter == "All" ||
                reel.tags.split(",").map { it.trim() }.contains(selectedTagFilter)
        matchesSearch && matchesTag
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Saved Content Organizer",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Button(
                onClick = { showAddDialog = true },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("add_reel_btn")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Save Reel")
            }
        }

        // Search Field
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search saved captions or tags...") },
            leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("search_saved_reels"),
            singleLine = true,
            shape = RoundedCornerShape(16.dp)
        )

        // Tag Filter Chips
        if (allTags.isNotEmpty()) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(allTags) { tag ->
                    FilterChip(
                        selected = selectedTagFilter == tag,
                        onClick = { selectedTagFilter = tag },
                        label = { Text(tag) }
                    )
                }
            }
        }

        // Saved List
        if (filteredReels.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Bookmark,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No saved reels yet",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Share reels from Instagram into Pet Companion or tap '+ Save Reel' above.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredReels, key = { it.id }) { reel ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .clickable { selectedReelForDetail = reel },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Status Badge
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            when (reel.factCheckStatus) {
                                                FactCheckStatus.ACCURATE -> Color(0xFFE8F5E9)
                                                FactCheckStatus.DISPUTED -> Color(0xFFFFEBEE)
                                                else -> Color(0xFFFFF8E1)
                                            }
                                        )
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = when (reel.factCheckStatus) {
                                                FactCheckStatus.ACCURATE -> Icons.Default.CheckCircle
                                                FactCheckStatus.DISPUTED -> Icons.Default.Warning
                                                else -> Icons.Default.Info
                                            },
                                            contentDescription = null,
                                            tint = when (reel.factCheckStatus) {
                                                FactCheckStatus.ACCURATE -> Color(0xFF2E7D32)
                                                FactCheckStatus.DISPUTED -> Color(0xFFC62828)
                                                else -> Color(0xFFF57F17)
                                            },
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = reel.verdictShort,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Black.copy(alpha = 0.8f)
                                        )
                                    }
                                }

                                Row {
                                    IconButton(
                                        onClick = {
                                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                                type = "text/plain"
                                                putExtra(Intent.EXTRA_TEXT, "Reel Fact Check: ${reel.verdictShort}\n\n${reel.caption}")
                                            }
                                            context.startActivity(Intent.createChooser(shareIntent, "Share Reel Fact Check"))
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Share, contentDescription = "Share", modifier = Modifier.size(18.dp))
                                    }

                                    IconButton(
                                        onClick = { viewModel.deleteReel(reel) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = reel.caption,
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp,
                                maxLines = 3
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                reel.tags.split(",").forEach { tagStr ->
                                    val t = tagStr.trim()
                                    if (t.isNotEmpty()) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(MaterialTheme.colorScheme.primaryContainer)
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "#$t",
                                                fontSize = 10.sp,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            val dateStr = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(reel.timestamp))
                            Text(
                                text = "Saved on $dateStr",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }
        }
    }

    // Detail Dialog
    selectedReelForDetail?.let { reel ->
        var editableTags by remember(reel) { mutableStateOf(reel.tags) }
        AlertDialog(
            onDismissRequest = { selectedReelForDetail = null },
            title = {
                Text(text = reel.verdictShort, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "Caption:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Text(text = reel.caption, fontSize = 13.sp)

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(text = "AI Fact-Check Evaluation:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Text(text = reel.explanation, fontSize = 13.sp)

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(text = "Reference Sources:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Text(text = reel.sourcesNote, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)

                    Spacer(modifier = Modifier.height(4.dp))

                    OutlinedTextField(
                        value = editableTags,
                        onValueChange = { editableTags = it },
                        label = { Text("Tags (comma separated)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.updateReelTags(reel, editableTags)
                    selectedReelForDetail = null
                }) {
                    Text("Save Changes")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedReelForDetail = null }) {
                    Text("Close")
                }
            }
        )
    }

    // Manual Save Reel Dialog
    if (showAddDialog) {
        var newCaption by remember { mutableStateOf("") }
        var newTags by remember { mutableStateOf("Watch Later, Recipes") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Save & Fact-Check Reel") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newCaption,
                        onValueChange = { newCaption = it },
                        label = { Text("Reel Caption / Claim Text") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 4
                    )
                    OutlinedTextField(
                        value = newTags,
                        onValueChange = { newTags = it },
                        label = { Text("Tags (e.g., Recipes, Science)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newCaption.isNotBlank()) {
                            viewModel.checkAndSaveReel(newCaption, customTags = newTags)
                            showAddDialog = false
                        }
                    },
                    enabled = newCaption.isNotBlank()
                ) {
                    Text("Fact-Check & Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
