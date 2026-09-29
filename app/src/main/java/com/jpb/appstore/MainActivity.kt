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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Security
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
import com.jpb.appstore.ui.HeroBannerCard
import com.jpb.appstore.ui.HorizontalAppsGrid
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
    var activeCategoryTitle by rememberSaveable { mutableStateOf<String?>(null) }
    var isEditorsChoice by rememberSaveable { mutableStateOf(false) }

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
    } else if (isEditorsChoice) {
        EditorsChoiceScreen(
            apps = apps,
            onBackClick = { isEditorsChoice = false },
            onAppClick = { app -> selectedAppId = app.id },
            onInstallClick = { appId -> viewModel.startDownload(appId) },
            onCancelClick = { appId -> viewModel.cancelAction(appId) }
        )
    } else if (activeCategoryTitle != null) {
        CategoryAppsScreen(
            title = activeCategoryTitle!!,
            subtitle = "Discover local developers & top picks",
            apps = apps,
            onBackClick = { activeCategoryTitle = null },
            onAppClick = { app -> selectedAppId = app.id }
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
                                Text("Store", style = MaterialTheme.typography.titleMedium)
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
                            onAppClick = { app -> selectedAppId = app.id },
                            onSeeAllUkApps = { activeCategoryTitle = "Apps made in the UK" },
                            onSeeAllEditorsChoice = { isEditorsChoice = true }
                        )
                        AppDestinations.APPS -> AppsLibraryScreen()
                        AppDestinations.UPDATES -> UpdatesScreen(
                            viewModel = viewModel,
                            onAppClick = { app -> selectedAppId = app.id }
                        )
                    }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryAppsScreen(
    title: String,
    subtitle: String,
    apps: List<AppItem>,
    onBackClick: () -> Unit,
    onAppClick: (AppItem) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(title, style = MaterialTheme.typography.titleMedium)
                        Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(apps, key = { it.id }) { app ->
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.Start
                ) {
                    Card(
                        onClick = { onAppClick(app) },
                        modifier = Modifier.size(80.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest)
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(text = app.name.take(2), style = MaterialTheme.typography.titleMedium)
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = app.name,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "4.6 ★",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorsChoiceScreen(
    apps: List<AppItem>,
    onBackClick: () -> Unit,
    onAppClick: (AppItem) -> Unit,
    onInstallClick: (String) -> Unit,
    onCancelClick: (String) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Editors' choice apps", style = MaterialTheme.typography.titleMedium)
                        Text("Hand-picked apps and games", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(apps, key = { it.id }) { app ->
                EditorsChoiceCardItem(
                    app = app,
                    onCardClick = { onAppClick(app) },
                    onInstallClick = { onInstallClick(app.id) },
                    onCancelClick = { onCancelClick(app.id) }
                )
            }
        }
    }
}

@Composable
fun EditorsChoiceCardItem(
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
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
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
                                    .size(56.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = app.name.take(2), style = MaterialTheme.typography.titleMedium)
                            }
                        }
                    }

                    Column {
                        Text(text = app.name, style = MaterialTheme.typography.titleMedium)

                        when (app.state) {
                            is InstallState.Downloading -> {
                                val progress = (app.state as InstallState.Downloading).progress
                                val percent = (progress * 100).toInt()
                                Column {
                                    Text(
                                        text = "$percent% of ${app.size}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Security,
                                            contentDescription = null,
                                            modifier = Modifier.size(12.dp),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = "Verified secure",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
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
                                    text = "4.7 ★  •  Editors' Choice",
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
                        is InstallState.Downloading, is InstallState.Installing -> {
                            IconButton(onClick = onCancelClick) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Cancel",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        is InstallState.Installed -> {
                            TextButton(onClick = onCardClick) {
                                Text("Open")
                            }
                        }
                    }
                }
            }

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 2.dp)
            ) {
                items(4) { index ->
                    Card(
                        modifier = Modifier
                            .width(110.dp)
                            .height(180.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
                        )
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Preview ${index + 1}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UpdatesScreen(
    viewModel: StoreViewModel,
    onAppClick: (AppItem) -> Unit
) {
    val apps by viewModel.apps.collectAsState()
    val updatesList = apps.filter { it.hasUpdate || it.state !is InstallState.Idle && it.state !is InstallState.Installed }
    val isAnyDownloading = updatesList.any { it.state is InstallState.Downloading || it.state is InstallState.Installing }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(8.dp))

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
                        onClick = { },
                        shape = RoundedCornerShape(50)
                    ) {
                        Text("Check for updates")
                    }
                }
            }
        } else {
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
                    when (app.state) {
                        is InstallState.Downloading -> {
                            val progress = (app.state as InstallState.Downloading).progress
                            Box(modifier = Modifier.size(52.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(
                                    progress = { progress },
                                    modifier = Modifier.fillMaxSize(),
                                    strokeWidth = 3.dp,
                                    color = MaterialTheme.colorScheme.primary,
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(10.dp))
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
                                Column {
                                    Text(
                                        text = "$percent% of ${app.size}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Security,
                                            contentDescription = null,
                                            modifier = Modifier.size(12.dp),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = "Verified secure",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
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
                                    text = "${app.size} • Updated yesterday",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

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
    onAppClick: (AppItem) -> Unit,
    onSeeAllUkApps: () -> Unit,
    onSeeAllEditorsChoice: () -> Unit
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
        if (apps.isNotEmpty()) {
            item {
                HeroBannerCard(
                    app = apps.first(),
                    heroTitle = "Meet Claude Opus 5.5",
                    heroSubtitle = "Our most capable AI model for your custom workspace.",
                    onCardClick = { onAppClick(apps.first()) },
                    onInstallClick = { viewModel.startDownload(apps.first().id) },
                    onCancelClick = { viewModel.cancelAction(apps.first().id) }
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Apps made in the UK",
                    style = MaterialTheme.typography.titleMedium
                )
                TextButton(onClick = onSeeAllUkApps) {
                    Text("See all")
                }
            }
        }

        if (apps.isNotEmpty()) {
            item {
                HorizontalAppsGrid(
                    apps = apps.take(5),
                    onAppClick = onAppClick
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Editors' Choice",
                    style = MaterialTheme.typography.titleMedium
                )
                TextButton(onClick = onSeeAllEditorsChoice) {
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
                            Column {
                                Text(
                                    text = "$percent% of ${app.size}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Security,
                                        contentDescription = null,
                                        modifier = Modifier.size(12.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "Verified secure",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
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
                    is InstallState.Downloading, is InstallState.Installing -> {
                        IconButton(onClick = onCancelClick) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cancel",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
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