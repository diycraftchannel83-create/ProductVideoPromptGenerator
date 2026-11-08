package com.winsmediatama.productvideopromptgenerator

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil3.compose.AsyncImage

private val StudioBackground = Color(0xFF0B1020)
private val StudioSurface = Color(0xFF151D32)
private val StudioCyan = Color(0xFF62E7F5)
private val StudioViolet = Color(0xFF9B8CFF)
private val StudioText = Color(0xFFF4F7FF)
private val StudioMuted = Color(0xFF9AA8C2)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { ProductPromptApp() }
    }
}

@Composable
fun ProductPromptApp() {
    val nav = rememberNavController()
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = StudioCyan,
            secondary = StudioViolet,
            background = StudioBackground,
            surface = StudioSurface,
            onBackground = StudioText,
            onSurface = StudioText
        )
    ) {
        Surface(Modifier.fillMaxSize(), color = StudioBackground) {
            NavHost(navController = nav, startDestination = "home") {
                composable("home") { HomeScreen(nav) }
                composable("create") { CreateProjectScreen(nav) }
                composable("library") { SimpleScreen("Prompt Library", "Saved prompts will appear here.", nav) }
                composable("settings") { SimpleScreen("Settings", "Language, theme and AI preferences.", nav) }
            }
        }
    }
}

private val categories = listOf(
    "Product Commercial", "Cinematic Product", "Luxury Advertising",
    "Social Media Marketing", "Image to Video", "Food & Beverage Commercial",
    "Fashion & Beauty", "Real Estate Promotion"
)

@Composable
fun HomeScreen(nav: NavHostController) {
    Column(
        Modifier.fillMaxSize().background(StudioBackground).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("WINS MEDIATAMA", color = StudioCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                Text("Creative Studio", color = StudioText, fontSize = 27.sp, fontWeight = FontWeight.Bold)
                Text("Turn product photos into video prompts", color = StudioMuted, fontSize = 13.sp)
            }
            Surface(color = StudioSurface, shape = RoundedCornerShape(14.dp)) {
                Text("AI + OFFLINE", Modifier.padding(horizontal = 12.dp, vertical = 9.dp), color = StudioCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }

        Button(
            onClick = { nav.navigate("create") },
            modifier = Modifier.fillMaxWidth().height(58.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = StudioCyan, contentColor = StudioBackground)
        ) {
            Text("+  Create New Video Prompt", fontWeight = FontWeight.Bold)
        }

        Text("Explore Categories", color = StudioText, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(categories) { category ->
                Surface(
                    modifier = Modifier.fillMaxWidth().height(104.dp).clickable { nav.navigate("create") },
                    color = StudioSurface,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color(0xFF283550))
                ) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.SpaceBetween) {
                        Text("✦", color = StudioViolet, fontSize = 20.sp)
                        Text(category, color = StudioText, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, lineHeight = 17.sp)
                    }
                }
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            Text("Home", color = StudioCyan, modifier = Modifier.clickable { nav.navigate("home") })
            Text("Library", color = StudioMuted, modifier = Modifier.clickable { nav.navigate("library") })
            Text("Settings", color = StudioMuted, modifier = Modifier.clickable { nav.navigate("settings") })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateProjectScreen(nav: NavHostController) {
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var projectName by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(categories.first()) }
    var expanded by remember { mutableStateOf(false) }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        imageUri = uri
    }

    Column(
        Modifier.fillMaxSize().background(StudioBackground).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("‹", color = StudioCyan, fontSize = 30.sp, modifier = Modifier.clickable { nav.popBackStack() }.padding(end = 12.dp))
            Column {
                Text("New Project", color = StudioText, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Text("Upload a product photo to get started", color = StudioMuted, fontSize = 12.sp)
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth().height(230.dp).clickable { picker.launch("image/*") },
            color = StudioSurface,
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, StudioCyan.copy(alpha = 0.5f))
        ) {
            if (imageUri == null) {
                Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    Text("＋", color = StudioCyan, fontSize = 42.sp)
                    Text("Upload Product Photo", color = StudioText, fontWeight = FontWeight.Bold)
                    Text("JPG • PNG • WEBP", color = StudioMuted, fontSize = 11.sp)
                }
            } else {
                AsyncImage(model = imageUri, contentDescription = "Selected product photo", modifier = Modifier.fillMaxSize())
            }
        }

        OutlinedTextField(
            value = projectName,
            onValueChange = { projectName = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Project / Product Name") },
            placeholder = { Text("e.g. Premium Perfume") },
            singleLine = true
        )

        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
            OutlinedTextField(
                value = selectedCategory,
                onValueChange = {},
                readOnly = true,
                modifier = Modifier.menuAnchor().fillMaxWidth(),
                label = { Text("Video Category") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) }
            )
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                categories.forEach { category ->
                    DropdownMenuItem(
                        text = { Text(category) },
                        onClick = { selectedCategory = category; expanded = false }
                    )
                }
            }
        }

        Surface(color = StudioSurface, shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(14.dp)) {
                Text("Generation mode", color = StudioText, fontWeight = FontWeight.SemiBold)
                Text("Offline templates are available. AI Vision will be connected in Phase 3.", color = StudioMuted, fontSize = 12.sp)
            }
        }

        Button(
            onClick = { nav.navigate("library") },
            enabled = imageUri != null,
            modifier = Modifier.fillMaxWidth().height(54.dp),
            colors = ButtonDefaults.buttonColors(containerColor = StudioCyan, contentColor = StudioBackground),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text("Continue to Configuration", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun SimpleScreen(title: String, subtitle: String, nav: NavHostController) {
    Column(
        Modifier.fillMaxSize().background(StudioBackground).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("‹ Back", color = StudioCyan, modifier = Modifier.clickable { nav.popBackStack() })
        Text(title, color = StudioText, fontSize = 26.sp, fontWeight = FontWeight.Bold)
        Text(subtitle, color = StudioMuted)
        Text("This section will be implemented in the next development phase.", color = StudioMuted, fontSize = 13.sp)
    }
}
