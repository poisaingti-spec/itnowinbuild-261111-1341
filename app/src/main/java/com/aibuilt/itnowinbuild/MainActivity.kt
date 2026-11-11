package com.aibuilt.itnowinbuild

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    primary = Color(0xFFBB86FC),
                    secondary = Color(0xFF03DAC5),
                    tertiary = Color(0xFFCF6679)
                )
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppMainScreen()
                }
            }
        }
    }
}

@Composable
fun AppMainScreen(viewModel: BuildViewModel = viewModel()) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Build", "Status", "History")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "It now in build",
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
                indicator = { tabPositions ->
                    Box(
                        modifier = Modifier
                            .tabIndicatorOffset(tabPositions[selectedTab])
                            .fillMaxSize()
                            .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                            .background(MaterialTheme.colorScheme.secondary)
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        icon = {
                            Icon(
                                imageVector = when (index) {
                                    0 -> Icons.Default.Build
                                    1 -> Icons.Default.CheckCircle
                                    else -> Icons.Default.History
                                },
                                contentDescription = title
                            )
                        }
                    )
                }
            }

            when (selectedTab) {
                0 -> BuildTab(viewModel)
                1 -> StatusTab(viewModel)
                2 -> HistoryTab(viewModel)
            }
        }
    }
}

@Composable
fun BuildTab(viewModel: BuildViewModel) {
    val buildState by viewModel.buildState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = when (buildState) {
                        BuildState.IDLE -> Icons.Default.Build
                        BuildState.COMPLETE -> Icons.Default.CheckCircle
                        else -> Icons.Default.PlayArrow
                    },
                    contentDescription = "Build Icon",
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = when (buildState) {
                        BuildState.IDLE -> "Ready to Build"
                        BuildState.STARTED -> "Build Started"
                        BuildState.COMPILING -> "Compiling..."
                        BuildState.TESTING -> "Running Tests..."
                        BuildState.DEPLOYING -> "Deploying..."
                        BuildState.COMPLETE -> "Build Complete!"
                    },
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Version 1.0.0",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }
        }

        if (buildState == BuildState.IDLE || buildState == BuildState.COMPLETE) {
            Button(
                onClick = { viewModel.startBuild() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Start Build", fontSize = 18.sp)
            }
        } else {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = MaterialTheme.colorScheme.secondary
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = { viewModel.cancelBuild() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Stop,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Cancel Build", fontSize = 18.sp)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        BuildStepsCard(buildState)
    }
}

@Composable
fun BuildStepsCard(buildState: BuildState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Build Pipeline",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            val steps = listOf(
                BuildState.STARTED to "Initialize",
                BuildState.COMPILING to "Compile Source",
                BuildState.TESTING to "Run Tests",
                BuildState.DEPLOYING to "Deploy Artifact",
                BuildState.COMPLETE to "Complete"
            )

            steps.forEach { (step, label) ->
                BuildStepItem(
                    label = label,
                    isActive = buildState == step,
                    isCompleted = isStepCompleted(buildState, step)
                )
            }
        }
    }
}

@Composable
fun BuildStepItem(label: String, isActive: Boolean, isCompleted: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    when {
                        isCompleted -> MaterialTheme.colorScheme.secondary
                        isActive -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.outline
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isCompleted) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = label,
            fontSize = 16.sp,
            color = when {
                isActive -> MaterialTheme.colorScheme.primary
                isCompleted -> MaterialTheme.colorScheme.onSurface
                else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            },
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
        )
    }
}

fun isStepCompleted(currentState: BuildState, step: BuildState): Boolean {
    val order = listOf(
        BuildState.STARTED,
        BuildState.COMPILING,
        BuildState.TESTING,
        BuildState.DEPLOYING,
        BuildState.COMPLETE
    )
    val currentIndex = order.indexOf(currentState)
    val stepIndex = order.indexOf(step)
    return currentIndex > stepIndex || currentState == BuildState.COMPLETE
}

@Composable
fun StatusTab(viewModel: BuildViewModel) {
    val buildState by viewModel.buildState.collectAsState()
    val buildHistory by viewModel.buildHistory.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = when (buildState) {
                    BuildState.COMPLETE -> Color(0xFF4CAF50).copy(alpha = 0.2f)
                    BuildState.IDLE -> MaterialTheme.colorScheme.surfaceVariant
                    else -> Color(0xFFFF9800).copy(alpha = 0.2f)
                }
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Current Status",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("State:", fontWeight = FontWeight.Medium)
                    Text(
                        text = buildState.name,
                        color = when (buildState) {
                            BuildState.COMPLETE -> Color(0xFF4CAF50)
                            BuildState.IDLE -> MaterialTheme.colorScheme.onSurface
                            else -> Color(0xFFFF9800)
                        },
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Last Build:", fontWeight = FontWeight.Medium)
                    Text(
                        text = if (buildHistory.isNotEmpty()) {
                            buildHistory.first().timestamp
                        } else {
                            "N/A"
                        }
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Total Builds:", fontWeight = FontWeight.Medium)
                    Text(text = buildHistory.size.toString())
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "System Info",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        InfoRow(label = "SDK Version", value = "35")
        InfoRow(label = "Min SDK", value = "26")
        InfoRow(label = "Kotlin", value = "2.2")
        InfoRow(label = "Compose BOM", value = "2024.12.01")
        InfoRow(label = "Material", value = "3")
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        Text(
            text = value,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun HistoryTab(viewModel: BuildViewModel) {
    val buildHistory by viewModel.buildHistory.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Build History",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (buildHistory.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .wrapContentSize(Alignment.Center)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No builds yet",
                        color = MaterialTheme.colorScheme.outline,
                        fontSize = 16.sp
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(buildHistory) { build ->
                    BuildHistoryItem(build)
                }
            }
        }
    }
}

@Composable
fun BuildHistoryItem(build: BuildRecord) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Build #${build.id}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = build.timestamp,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }

            Badge(
                containerColor = when (build.status) {
                    BuildState.COMPLETE -> Color(0xFF4CAF50)
                    else -> Color(0xFFFF9800)
                }
            ) {
                Text(text = build.status.name)
            }
        }
    }
}

enum class BuildState {
    IDLE,
    STARTED,
    COMPILING,
    TESTING,
    DEPLOYING,
    COMPLETE
}

data class BuildRecord(
    val id: Int,
    val timestamp: String,
    val status: BuildState,
    val duration: String
)

class BuildViewModel : ViewModel() {
    private val _buildState = MutableStateFlow(BuildState.IDLE)
    val buildState: StateFlow<BuildState> = _buildState.asStateFlow()

    private val _buildHistory = MutableStateFlow(listOf<BuildRecord>())
    val buildHistory: StateFlow<List<BuildRecord>> = _buildHistory.asStateFlow()

    private var buildCounter = 0

    fun startBuild() {
        if (_buildState.value != BuildState.IDLE && _buildState.value != BuildState.COMPLETE) return

        buildCounter++
        val currentBuild = buildCounter

        viewModelScope.launch {
            _buildState.value = BuildState.STARTED

            delay(1500)
            _buildState.value = BuildState.COMPILING

            delay(2000)
            _buildState.value = BuildState.TESTING

            delay(2000)
            _buildState.value = BuildState.DEPLOYING

            delay(1500)
            _buildState.value = BuildState.COMPLETE

            _buildHistory.value = listOf(
                BuildRecord(
                    id = currentBuild,
                    timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                        .format(Date()),
                    status = BuildState.COMPLETE,
                    duration = "7s"
                )
            ) + _buildHistory.value
        }
    }

    fun cancelBuild() {
        if (_buildState.value == BuildState.IDLE || _buildState.value == BuildState.COMPLETE) return
        _buildState.value = BuildState.IDLE
    }
}
