package com.jpb.appstore

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jpb.appstore.ui.AppDetailScreen
import com.jpb.appstore.ui.theme.AppStoreTheme
import com.jpb.appstore.utils.AppItem
import com.jpb.appstore.utils.InstallState
import com.jpb.appstore.viewmodels.StoreViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppStoreTheme {
                AppStoreApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@PreviewScreenSizes
@Composable
fun AppStoreApp(viewModel: StoreViewModel = viewModel()) {
    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.HOME) }
    var selectedAppId by rememberSaveable { mutableStateOf<String?>(null) }

    val apps by viewModel.apps.collectAsState()
    val selectedApp = apps.find { it.id == selectedAppId }

    if (selectedApp != null) {
        AppDetailScreen(
            app = selectedApp,
            onBackClick = { selectedAppId = null },
            onInstallClick = {
                viewModel.startDownload(selectedApp.id)
            },
            onCancelClick = {
                viewModel.cancelAction(selectedApp.id)
            }
        )
    } else {
        NavigationSuiteScaffold(
            navigationSuiteItems = {
                AppDestinations.entries.forEach { destination ->
                    item(
                        icon = {
                            Icon(
                                imageVector = destination.icon,
                                contentDescription = destination.label
                            )
                        },
                        label = { Text(destination.label) },
                        selected = destination == currentDestination,
                        onClick = { currentDestination = destination }
                    )
                }
            }
        ) {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                topBar = {
                    TopAppBar(
                        title = {
                            Column {
                                Text("AuroraDroid Store", style = MaterialTheme.typography.titleMedium)
                                Text("System OS & Custom Repos", style = MaterialTheme.typography.bodySmall)
                            }
                        },
                        actions = {
                            IconButton(onClick = { }) {
                                Icon(Icons.Default.Settings, contentDescription = "Store Settings")
                            }
                        }
                    )
                }
            ) { innerPadding ->
                Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
                    when (currentDestination) {
                        AppDestinations.HOME -> HomeScreen(
                            viewModel = viewModel,
                            onAppClick = { app -> selectedAppId = app.id }
                        )
                        AppDestinations.APPS -> AppsLibraryScreen()
                        AppDestinations.UPDATES -> UpdatesScreen(
                            viewModel = viewModel,
                            onAppClick = { app -> selectedAppId = app.id }
                        )                    }
                }
            }
        }
    }
}

enum class AppDestinations(
    val label: String,
    val icon: ImageVector,
) {
    HOME("Home", Icons.Default.Home),
    APPS("Apps", Icons.Default.Apps),
    UPDATES("Updates", Icons.Default.SystemUpdate),
}

@Composable
fun AppsLibraryScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("App Library & Repositories", style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
fun UpdatesScreen(
    viewModel: StoreViewModel,
    onAppClick: (AppItem) -> Unit
) {
    val apps by viewModel.apps.collectAsState()
    // Filter apps that have an update available or are currently downloading/installing
    val updatesList = apps.filter { it.hasUpdate || it.state !is InstallState.Idle && it.state !is InstallState.Installed }

    val isAnyDownloading = updatesList.any { it.state is InstallState.Downloading || it.state is InstallState.Installing }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Top Bar Header & "Update all" / "Cancel all" Action Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (updatesList.isNotEmpty()) "Available updates (${updatesList.size})" else "",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (updatesList.isNotEmpty()) {
                FilledTonalButton(
                    onClick = {
                        if (isAnyDownloading) {
                            updatesList.forEach { viewModel.cancelAction(it.id) }
                        } else {
                            updatesList.forEach { viewModel.startDownload(it.id) }
                        }
                    },
                    shape = RoundedCornerShape(50)
                ) {
                    Text(if (isAnyDownloading) "Cancel all" else "Update all")
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (updatesList.isEmpty()) {
            // "You're all set" Empty State matching Google Play
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .background(MaterialTheme.colorScheme.surfaceContainerHighest, RoundedCornerShape(24.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SystemUpdate,
                            contentDescription = null,
                            modifier = Modifier.size(56.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "You're all set",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "All your apps are up to date",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { /* Refresh/Check action */ },
                        shape = RoundedCornerShape(50)
                    ) {
                        Text("Check for updates")
                    }
                }
            }
        } else {
            // List of Update items matching Google Play layout
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(updatesList, key = { it.id }) { app ->
                    UpdateCardItem(
                        app = app,
                        onCardClick = { onAppClick(app) },
                        onUpdateClick = { viewModel.startDownload(app.id) },
                        onCancelClick = { viewModel.cancelAction(app.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun UpdateCardItem(
    app: AppItem,
    onCardClick: () -> Unit,
    onUpdateClick: () -> Unit,
    onCancelClick: () -> Unit
) {
    var expanded by rememberSaveable { mutableStateOf(false) }

    Card(
        onClick = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Circular progress during download or normal icon container
                    when (app.state) {
                        is InstallState.Downloading -> {
                            val progress = (app.state as InstallState.Downloading).progress
                            Box(modifier = Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(
                                    progress = { progress },
                                    modifier = Modifier.fillMaxSize(),
                                    strokeWidth = 3.dp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = app.name.take(2), style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                        else -> {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = app.name.take(2), style = MaterialTheme.typography.titleSmall)
                            }
                        }
                    }

                    Column {
                        Text(text = app.name, style = MaterialTheme.typography.bodyLarge)

                        when (app.state) {
                            is InstallState.Downloading -> {
                                val progress = (app.state as InstallState.Downloading).progress
                                val percent = (progress * 100).toInt()
                                Text(
                                    text = "${app.size}  $percent% of 20.07 MB",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            is InstallState.Installing -> {
                                Text(
                                    text = "${app.size}  Installing...",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            else -> {
                                Text(
                                    text = "${app.size} • Updated yesterday",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Trailing Action Controls (Update button, Cancel 'X' button, or Dropdown Arrow)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    when (app.state) {
                        is InstallState.Idle -> {
                            FilledTonalButton(
                                onClick = onUpdateClick,
                                shape = RoundedCornerShape(50)
                            ) {
                                Text("Update")
                            }
                        }
                        is InstallState.Downloading, is InstallState.Installing -> {
                            IconButton(onClick = onCancelClick) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Cancel",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        else -> {}
                    }

                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = "Expand",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Collapsible "What's new" section matching Google Play layout
            if (expanded) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(modifier = Modifier.fillMaxWidth().height(1.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(text = "What's new", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface)
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(RoundedCornerShape(50))
                            .background(MaterialTheme.colorScheme.primary)
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "• Performance improvements and bug fixes for version ${app.version}.\n• Enhanced repository syncing speed and secure background verification.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AppStorePreview() {
    AppStoreTheme {
        AppStoreApp()
    }
}

@Composable
fun HomeScreen(
    viewModel: StoreViewModel,
    onAppClick: (AppItem) -> Unit
) {
    val apps by viewModel.apps.collectAsState()
    val repositories by viewModel.repositories.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    contentAlignment = Alignment.BottomStart
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = MaterialTheme.colorScheme.primary
                        ) {
                            Text(
                                text = "Featured App",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                        Text(
                            text = "NeoTerm Linux Terminal",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "A powerful terminal emulator built for custom OS tinkerers.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                            maxLines = 2
                        )
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recommended for You",
                    style = MaterialTheme.typography.titleMedium
                )
                TextButton(onClick = { }) {
                    Text("See all")
                }
            }
        }

        items(
            items = apps.take(3),
            key = { it.id }
        ) { appItem ->
            AppCardItem(
                app = appItem,
                onCardClick = { onAppClick(appItem) },
                onInstallClick = { viewModel.startDownload(appItem.id) },
                onCancelClick = { viewModel.cancelAction(appItem.id) }
            )
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Custom F-Droid Repositories",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Active metadata sync sources",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                TextButton(onClick = { }) {
                    Text("Add Source")
                }
            }
        }

        items(
            items = repositories,
            key = { it.url }
        ) { repoItem ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = repoItem.name, style = MaterialTheme.typography.bodyMedium)
                        Text(
                            text = repoItem.url,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = "${repoItem.appCount} apps",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun AppCardItem(
    app: AppItem,
    onCardClick: () -> Unit,
    onInstallClick: () -> Unit,
    onCancelClick: () -> Unit
) {
    Card(
        onClick = onCardClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                when (app.state) {
                    is InstallState.Downloading -> {
                        val progress = (app.state as InstallState.Downloading).progress
                        Box(
                            modifier = Modifier.size(52.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                progress = { progress },
                                modifier = Modifier.fillMaxSize(),
                                strokeWidth = 3.dp,
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                            )
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = app.name.take(2), style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
                    else -> {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = app.name.take(2), style = MaterialTheme.typography.titleSmall)
                        }
                    }
                }

                Column {
                    Text(text = app.name, style = MaterialTheme.typography.bodyLarge)

                    when (app.state) {
                        is InstallState.Downloading -> {
                            val progress = (app.state as InstallState.Downloading).progress
                            val percent = (progress * 100).toInt()
                            Text(
                                text = "$percent% of ${app.size}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        is InstallState.Installing -> {
                            Text(
                                text = "Installing...",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        else -> {
                            Text(
                                text = "${app.category} • ${app.size}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Box {
                when (app.state) {
                    is InstallState.Idle -> {
                        FilledTonalButton(onClick = onInstallClick) {
                            Text(if (app.hasUpdate) "Update" else "Install")
                        }
                    }
                    is InstallState.Downloading -> {
                        OutlinedButton(onClick = onCancelClick) {
                            Text("Cancel")
                        }
                    }
                    is InstallState.Installing -> {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    }
                    is InstallState.Installed -> {
                        TextButton(onClick = onCardClick) {
                            Text("Open")
                        }
                    }
                }
            }
        }
    }
}