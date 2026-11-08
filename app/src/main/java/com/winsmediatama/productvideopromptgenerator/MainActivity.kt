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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import com.winsmediatama.productvideopromptgenerator.engine.OfflinePromptEngine
import com.winsmediatama.productvideopromptgenerator.engine.PromptRequest
import com.winsmediatama.productvideopromptgenerator.engine.PromptResult
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

private object PromptSession {
    var projectName: String = ""
    var category: String = "Product Commercial"
    var results: List<PromptResult> = emptyList()
    var productDetails: String = ""
}

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
                composable("configure") { ConfigurationScreen(nav) }
                composable("results") { PromptResultsScreen(nav) }
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
    var productDetails by remember { mutableStateOf("") }
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
            value = productDetails,
            onValueChange = { productDetails = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Visible Product Details (Optional)") },
            placeholder = { Text("Describe visible color, material, shape, logo or packaging...") },
            minLines = 2,
            maxLines = 4
        )

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
                Text("Offline generation is ready. Add visible product details above to guide the prompt.", color = StudioMuted, fontSize = 12.sp)
            }
        }

        Button(
            onClick = { PromptSession.projectName = projectName; PromptSession.category = selectedCategory; PromptSession.productDetails = productDetails; nav.navigate("configure") },
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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigurationScreen(nav: NavHostController) {
    var style by remember { mutableStateOf("Cinematic") }
    var camera by remember { mutableStateOf("Slow dolly-in") }
    var lighting by remember { mutableStateOf("Soft studio lighting") }
    var background by remember { mutableStateOf("Minimal premium studio") }
    var duration by remember { mutableStateOf("8 seconds") }
    var ratio by remember { mutableStateOf("9:16") }
    var language by remember { mutableStateOf("English") }
    var multiScene by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().background(StudioBackground).verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("‹ Back", color = StudioCyan, modifier = Modifier.clickable { nav.popBackStack() })
        Text("Video Configuration", color = StudioText, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text("Project: " + PromptSession.projectName, color = StudioCyan)
        PromptChoice("Visual Style", style, listOf("Cinematic", "Commercial", "Luxury", "Minimal", "Realistic")) { style = it }
        PromptChoice("Camera Movement", camera, listOf("Slow dolly-in", "Orbit shot", "Tracking shot", "Smooth pan", "Crane reveal", "Static macro")) { camera = it }
        PromptChoice("Lighting", lighting, listOf("Soft studio lighting", "Dramatic rim lighting", "Golden hour", "High-key commercial", "Moody low-key")) { lighting = it }
        PromptChoice("Background", background, listOf("Minimal premium studio", "Dark luxury studio", "Natural environment", "Modern kitchen", "Urban lifestyle", "Elegant interior")) { background = it }
        PromptChoice("Duration", duration, listOf("5 seconds", "8 seconds", "10 seconds", "15 seconds", "30 seconds")) { duration = it }
        PromptChoice("Aspect Ratio", ratio, listOf("9:16", "16:9", "1:1")) { ratio = it }
        PromptChoice("Prompt Language", language, listOf("English", "Indonesia")) { language = it }
        Surface(color = StudioSurface, shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()) {
            Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Multi-Scene Storyboard", color = StudioText, fontWeight = FontWeight.SemiBold)
                    Text("Create a four-part scene sequence", color = StudioMuted, fontSize = 12.sp)
                }
                Switch(checked = multiScene, onCheckedChange = { multiScene = it })
            }
        }
        Button(onClick = {
            val request = PromptRequest(PromptSession.projectName, PromptSession.category, style, camera, lighting,
                background, duration, ratio, language, multiScene, PromptSession.productDetails)
            PromptSession.results = OfflinePromptEngine.generate(request)
            nav.navigate("results")
        }, modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = StudioCyan, contentColor = StudioBackground),
            shape = RoundedCornerShape(14.dp)) {
            Text("✦ Generate 3 Prompt Variations", fontWeight = FontWeight.Bold)
        }
        Text("Generated locally. No API key or internet required.", color = StudioMuted, fontSize = 11.sp)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PromptChoice(label: String, selected: String, options: List<String>, onSelect: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        OutlinedTextField(value = selected, onValueChange = {}, readOnly = true,
            modifier = Modifier.menuAnchor().fillMaxWidth(), label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) })
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(text = { Text(option) }, onClick = { onSelect(option); expanded = false })
            }
        }
    }
}

@Composable
fun PromptResultsScreen(nav: NavHostController) {
    val clipboard = LocalClipboardManager.current
    val results = PromptSession.results
    var selected by remember { mutableIntStateOf(0) }
    var prompt by remember { mutableStateOf(results.firstOrNull()?.prompt.orEmpty()) }
    var negative by remember { mutableStateOf(results.firstOrNull()?.negativePrompt.orEmpty()) }

    Column(Modifier.fillMaxSize().background(StudioBackground).verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("‹ Back", color = StudioCyan, modifier = Modifier.clickable { nav.popBackStack() })
        Text("Generated Prompts", color = StudioText, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text("Three concepts generated offline", color = StudioMuted, fontSize = 12.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            results.forEachIndexed { index, item ->
                FilterChip(selected = selected == index, onClick = {
                    selected = index
                    prompt = item.prompt
                    negative = item.negativePrompt
                }, label = { Text(item.title) })
            }
        }
        Text("VIDEO PROMPT", color = StudioCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        OutlinedTextField(value = prompt, onValueChange = { prompt = it }, modifier = Modifier.fillMaxWidth(),
            minLines = 8, label = { Text("Edit your prompt") })
        Button(onClick = { clipboard.setText(AnnotatedString(prompt)) }, modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = StudioCyan, contentColor = StudioBackground)) {
            Text("Copy Video Prompt", fontWeight = FontWeight.Bold)
        }
        Text("NEGATIVE PROMPT", color = StudioViolet, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        OutlinedTextField(value = negative, onValueChange = { negative = it }, modifier = Modifier.fillMaxWidth(), minLines = 4)
        OutlinedButton(onClick = { clipboard.setText(AnnotatedString(negative)) }, modifier = Modifier.fillMaxWidth()) {
            Text("Copy Negative Prompt")
        }
        OutlinedButton(onClick = {
            clipboard.setText(AnnotatedString(prompt + "\\n\\nNegative Prompt: " + negative))
        }, modifier = Modifier.fillMaxWidth()) { Text("Copy All") }
        Text("Review product details against the original photo before using the prompt in an external video model.",
            color = StudioMuted, fontSize = 12.sp)
        Button(onClick = { nav.navigate("configure") }, modifier = Modifier.fillMaxWidth()) {
            Text("Adjust Configuration")
        }
    }
}
