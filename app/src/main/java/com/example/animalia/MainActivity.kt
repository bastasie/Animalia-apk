package com.example.animalia

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val DarkColors = darkColorScheme(
    primary = Color(0xFF00b894),
    secondary = Color(0xFF0984e3),
    background = Color(0xFF1a1a2e),
    surface = Color(0xFF0f3460),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White
)

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme(colorScheme = DarkColors) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AnimaliaApp(viewModel)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnimaliaApp(viewModel: MainViewModel) {
    val speciesList by viewModel.speciesList.collectAsState()
    val speciesCount by viewModel.speciesCount.collectAsState()
    val vocCount by viewModel.vocalizationCount.collectAsState()
    val avgPi by viewModel.avgPiComm.collectAsState()
    
    val selectedSpeciesId by viewModel.selectedSpeciesId.collectAsState()
    val vocalizations by viewModel.vocalizations.collectAsState()
    
    val selectedSpecies = speciesList.find { it.id == selectedSpeciesId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (selectedSpecies == null) "Animalia" else selectedSpecies.name,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif
                    )
                },
                navigationIcon = {
                    if (selectedSpecies != null) {
                        IconButton(onClick = { viewModel.selectSpecies(null) }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (selectedSpecies == null) {
                LazyColumn(
                    contentPadding = WindowInsets.navigationBars.asPaddingValues(),
                    modifier = Modifier.fillMaxSize()
                ) {
                    item {
                        DashboardStats(speciesCount, vocCount, avgPi ?: 0.0)
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "Species Database",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                    
                    items(speciesList) { species ->
                        SpeciesCard(species) {
                            viewModel.selectSpecies(species.id)
                        }
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = WindowInsets.navigationBars.asPaddingValues(),
                    modifier = Modifier.fillMaxSize()
                ) {
                    item {
                        SpeciesHeader(selectedSpecies)
                    }
                    
                    items(vocalizations) { voc ->
                        VocalizationCard(voc)
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardStats(speciesCount: Int, vocCount: Int, avgPi: Double) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        StatCard(
            title = "Species",
            value = speciesCount.toString(),
            modifier = Modifier.weight(1f),
            gradient = Brush.linearGradient(listOf(Color(0xFF0f3460), Color(0xFF1a1a2e)))
        )
        StatCard(
            title = "Vocals",
            value = vocCount.toString(),
            modifier = Modifier.weight(1f),
            gradient = Brush.linearGradient(listOf(Color(0xFF00b894), Color(0xFF00cec9)))
        )
        StatCard(
            title = "Avg π",
            value = String.format("%.2f", avgPi),
            modifier = Modifier.weight(1f),
            gradient = Brush.linearGradient(listOf(Color(0xFF0984e3), Color(0xFF74b9ff)))
        )
    }
}

@Composable
fun StatCard(title: String, value: String, modifier: Modifier = Modifier, gradient: Brush) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .background(gradient)
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = value,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = title,
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
fun SpeciesCard(species: Species, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = species.emoji, fontSize = 28.sp)
            }
            
            Column(modifier = Modifier
                .padding(start = 16.dp)
                .weight(1f)) {
                Text(
                    text = species.name,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "π = ${species.piComm}",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun SpeciesHeader(species: Species) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = species.emoji, fontSize = 72.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "π_comm = ${species.piComm}",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = "Vocalizations",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.Start)
        )
    }
}

@Composable
fun VocalizationCard(voc: Vocalization) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = voc.signalType,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                val confColor = when {
                    voc.confidence > 0.9 -> Color(0xFF00b894)
                    voc.confidence > 0.5 -> Color(0xFFfdcb6e)
                    else -> Color(0xFFff7675)
                }
                Text(
                    text = "${(voc.confidence * 100).toInt()}% Conf",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = confColor,
                    modifier = Modifier
                        .background(confColor.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ComponentItem("a_acoustic", voc.aAcoustic.toString())
                ComponentItem("-b_context", voc.bContext.toString())
                ComponentItem("c_intent", voc.cIntent.toString())
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = "||S||_S = ${String.format("%.2f", voc.starlingNorm)}",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 14.sp,
                    color = Color.LightGray
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "\"${voc.decodedMeaning}\"",
                fontSize = 16.sp,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                color = Color.White
            )
        }
    }
}

@Composable
fun ComponentItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, fontSize = 12.sp, color = Color.Gray)
        Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
    }
}
