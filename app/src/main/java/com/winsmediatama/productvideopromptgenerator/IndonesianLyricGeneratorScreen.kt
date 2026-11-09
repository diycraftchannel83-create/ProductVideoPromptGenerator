package com.winsmediatama.productvideopromptgenerator

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController

@Composable
fun IndonesianLyricGeneratorScreen(nav: NavHostController) {
    val clipboard = LocalClipboardManager.current
    var title by remember { mutableStateOf("Di Ujung Doa") }
    var theme by remember { mutableStateOf("perjuangan, kehilangan, harapan dan doa") }
    var genre by remember { mutableStateOf("Pop Ballad") }
    var mood by remember { mutableStateOf("Menyayat hati") }
    var vocal by remember { mutableStateOf("Pria") }
    var structure by remember { mutableStateOf("Intro, Verse, Pre-Chorus, Chorus, Verse 2, Bridge, Final Chorus, Outro") }
    var lyric by remember { mutableStateOf("") }
    var generationIndex by remember { mutableIntStateOf(0) }

    Column(
        Modifier.fillMaxSize().background(StudioBackground).navigationBarsPadding()
            .verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("‹ Back", color = StudioCyan, modifier = Modifier.clickable { nav.popBackStack() })
        Text("Indonesian Lyric Studio", color = StudioText, fontSize = 25.sp, fontWeight = FontWeight.Bold)
        Text("Generator lirik offline dengan arahan vokal, instrumen, cue performa, dan dinamika emosi di setiap bagian.", color = StudioMuted, fontSize = 13.sp)
        OutlinedTextField(value = title, onValueChange = { title = it }, modifier = Modifier.fillMaxWidth(),
            label = { Text("Judul lagu") }, singleLine = true)
        OutlinedTextField(value = theme, onValueChange = { theme = it }, modifier = Modifier.fillMaxWidth(),
            label = { Text("Tema / cerita lagu") }, minLines = 2)
        LyricDropdown("Genre musik", genre, listOf("Pop Ballad", "Pop Rock", "Indie Folk", "Dangdut", "Akustik", "Religi", "Orkestra Sinematik", "R&B", "Rock Alternatif")) { genre = it }
        LyricDropdown("Nuansa emosi", mood, listOf("Menyayat hati", "Romantis", "Penuh harapan", "Spiritual dan khusyuk", "Semangat dan heroik", "Nostalgia", "Penuh penyesalan", "Bahagia")) { mood = it }
        LyricDropdown("Karakter vokal", vocal, listOf("Pria", "Wanita", "Duet", "Paduan suara")) { vocal = it }
        LyricDropdown("Struktur lagu", structure, listOf(
            "Intro, Verse, Pre-Chorus, Chorus, Verse 2, Bridge, Final Chorus, Outro",
            "Intro, Verse 1, Chorus, Verse 2, Chorus, Bridge, Final Chorus, Outro",
            "Intro, Verse, Reff, Verse 2, Reff, Interlude, Bridge, Reff, Outro"
        )) { structure = it }

        Button(onClick = {
            generationIndex += 1
            lyric = buildIndonesianLyric(title, theme, genre, mood, vocal, structure, generationIndex)
        }, modifier = Modifier.fillMaxWidth().height(54.dp),
            colors = ButtonDefaults.buttonColors(containerColor = StudioCyan, contentColor = StudioBackground),
            shape = RoundedCornerShape(14.dp)) {
            Text("♫  Generate Indonesian Lyrics", fontWeight = FontWeight.Bold)
        }

        if (lyric.isNotBlank()) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("HASIL LIRIK + CUE", color = StudioCyan, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                TextButton(onClick = { clipboard.setText(AnnotatedString(lyric)) }) { Text("Copy All") }
            }
            OutlinedTextField(value = lyric, onValueChange = { lyric = it }, modifier = Modifier.fillMaxWidth(),
                minLines = 18, label = { Text("Edit hasil lirik") })
            OutlinedButton(onClick = { clipboard.setText(AnnotatedString(lyric)) }, modifier = Modifier.fillMaxWidth()) {
                Text("Copy Lyrics for Suno / Music AI")
            }
            Text("Cue dalam tanda kurung siku memberi arahan aransemen dan performa. Cue emosi dan vokal disertakan pada setiap bagian lagu.", color = StudioMuted, fontSize = 11.sp)
        }
    }
}

@Composable
private fun LyricDropdown(label: String, selected: String, options: List<String>, onSelect: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedTextField(value = selected, onValueChange = {}, readOnly = true,
            modifier = Modifier.fillMaxWidth().clickable { expanded = true }, label = { Text(label) },
            trailingIcon = { Text("⌄", color = StudioCyan, modifier = Modifier.clickable { expanded = true }.padding(12.dp)) })
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(text = { Text(option) }, onClick = { onSelect(option); expanded = false })
            }
        }
    }
}

private fun buildIndonesianLyric(
    title: String,
    theme: String,
    genre: String,
    mood: String,
    vocal: String,
    structure: String,
    variation: Int
): String {
    val songTitle = title.trim().ifBlank { "Tanpa Judul" }
    val story = theme.trim().ifBlank { "perjalanan hidup dan harapan" }
    val vocalCue = when (vocal) {
        "Wanita" -> "vokal wanita, lembut dan ekspresif"
        "Duet" -> "duet pria-wanita, saling menjawab lalu menyatu"
        "Paduan suara" -> "lead vokal dengan harmoni paduan suara"
        else -> "vokal pria, hangat dan penuh penghayatan"
    }

    // Each lyric section has exactly four sung lines. Rotate complete stanza variants
    // on every tap so consecutive generations do not repeat the same lyrics.
    val verseVariants = listOf(
        listOf("Di balik senyum kusimpan cerita", "Langkahku lelah menembus gelap", "Ada rindu yang tak bersuara", "Menunggu pagi membuka harap"),
        listOf("Pernah kutanya pada semesta", "Mengapa jalan terasa panjang", "Namun di dada masih menyala", "Sebuah keyakinan untuk bertahan"),
        listOf("Kutatap jejak yang kian menjauh", "Namamu hadir di sela waktu", "Meski kenyataan membuatku rapuh", "Tak kubiarkan mimpi berlalu")
    )
    val verseTwoVariants = listOf(
        listOf("Kini kupeluk semua kenangan", "Tak lagi kuhindari kenyataan", "Luka mengajariku bertahan", "Dan tumbuh kuat perlahan-lahan"),
        listOf("Hari berganti membawa makna", "Yang hilang tak harus kulupakan", "Kusimpan baik setiap cerita", "Sebagai alasan untuk berjalan"),
        listOf("Bila esok kembali menguji", "Kan kuhadapi tanpa berlari", "Sebab hatiku mulai mengerti", "Harapan tumbuh dari dalam diri")
    )
    val chorusVariants = listOf(
        listOf("Bawalah doaku menembus malam", "Jangan biarkan cahaya tenggelam", "Walau air mata jatuh perlahan", "Hatiku tetap memilih bertahan"),
        listOf("Aku percaya badai kan reda", "Ada cahaya di balik derita", "Meski dunia tak selalu ramah", "Cinta dan harap takkan menyerah"),
        listOf("Dengarlah suara dari jiwa", "Yang terus memanggil penuh percaya", "Biar waktu menjawab semua", "Kita kan menemukan jalannya")
    )
    val bridgeVariants = listOf(
        listOf("Jika kakiku tak kuat berdiri", "Ingatkan aku alasan berlari", "Di titik gelap paling sunyi", "Masih ada kasih yang menemani"),
        listOf("Andai semua harus kulepas", "Biarkan doa menjadi nafas", "Saat seluruh pintu terasa tertutup", "Keyakinan menjaga langkahku"),
        listOf("Tak perlu dunia memahami", "Cukup cahaya tetap di hati", "Dari serpihan kubangun lagi", "Menjadi diriku yang lebih berani")
    )
    val outroVariants = listOf(
        listOf("Kini kusandarkan segala cerita", "Pada Yang Maha Menjaga", "Esok kan kusambut dengan percaya", "Sebab harapan masih menyala"),
        listOf("Bila malam datang kembali", "Takkan kubiarkan hati sendiri", "Doa menjadi teman abadi", "Menuntun langkah sampai nanti"),
        listOf("Kulepas resah bersama angin", "Kuterima takdir dengan yakin", "Di ujung doa kutemukan arti", "Untuk mencintai dan berdiri")
    )
    val index = Math.floorMod(variation - 1, verseVariants.size)
    val sections = listOf(
        Triple("[VERSE 1]", "[Emosi: intim, rapuh, mulai bercerita] [Vokal: lembut dan dekat] [Musik: piano/gitar minimal]", verseVariants[index]),
        Triple("[PRE-CHORUS]", "[Emosi: ketegangan dan harapan mulai naik] [Vokal: intensitas bertahap] [Musik: strings dan perkusi perlahan masuk]", listOf("Kucoba berdiri sekali lagi", "Meski bayang ragu mengikuti", "Kutarik nafas, kuteguhkan hati", "Sebentar lagi terang menanti")),
        Triple("[CHORUS / REFF]", "[Emosi: ledakan rasa dan pesan utama lagu] [Vokal: kuat, terbuka, nada panjang pada baris akhir] [Musik: full band dan strings lebar]", chorusVariants[index]),
        Triple("[VERSE 2]", "[Emosi: lebih dewasa dan menerima] [Vokal: lembut tetapi tegas] [Musik: groove ringan]", verseTwoVariants[index]),
        Triple("[BRIDGE]", "[Emosi: titik terendah berubah menjadi kebangkitan] [Vokal: mulai lirih lalu menguat] [Musik: drop sesaat lalu crescendo]", bridgeVariants[index]),
        Triple("[FINAL CHORUS]", "[Emosi: klimaks, lega dan penuh keyakinan] [Vokal: paling kuat, harmoni bertumpuk] [Musik: full orchestra/band]", chorusVariants[(index + 1) % chorusVariants.size]),
        Triple("[OUTRO]", "[Emosi: damai dan ikhlas] [Vokal: turun lembut] [Musik: piano atau gitar fade out]", outroVariants[index])
    )
    val selectedSections = when {
        structure.startsWith("Intro, Verse 1") -> sections
        structure.startsWith("Intro, Verse, Reff") -> sections
        else -> sections
    }

    return buildString {
        appendLine("TITLE: $songTitle")
        appendLine("LANGUAGE: Bahasa Indonesia")
        appendLine("GENRE: $genre | MOOD: $mood | VOCAL: $vocal")
        appendLine("PERFORMANCE DIRECTION: $vocalCue")
        appendLine("STORY THEME: $story")
        appendLine()
        appendLine("[INTRO]")
        appendLine("[Emosi: hening dan mengundang pendengar] [Musik: piano atau petikan gitar solo]")
        appendLine("(instrumental intro)")
        appendLine()
        selectedSections.forEach { section ->
            appendLine(section.first)
            appendLine(section.second)
            section.third.forEachIndexed { lineIndex, line ->
                val delivery = when (lineIndex) {
                    0 -> "(lembut) "
                    1 -> "(mengalir) "
                    2 -> "(semakin terasa) "
                    else -> "(ekspresif) "
                }
                appendLine(delivery + line)
            }
            appendLine()
        }
        appendLine("[PRODUCTION NOTE]")
        appendLine("[Tempo: sesuaikan dengan genre $genre] [Dinamika: bangun bertahap menuju final chorus]")
        appendLine("[Tema: $story] [Catatan: cue dalam tanda kurung siku adalah arahan performa, bukan lirik yang dinyanyikan.]")
    }
}
