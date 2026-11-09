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
            lyric = buildIndonesianLyric(title, theme, genre, mood, vocal, structure)
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
    structure: String
): String {
    val songTitle = title.trim().ifBlank { "Tanpa Judul" }
    val story = theme.trim().ifBlank { "perjalanan hidup dan harapan" }
    val vocalCue = when (vocal) {
        "Wanita" -> "vokal wanita, lembut dan ekspresif"
        "Duet" -> "duet pria-wanita, saling menjawab lalu menyatu"
        "Paduan suara" -> "lead vokal dengan harmoni paduan suara"
        else -> "vokal pria, hangat dan penuh penghayatan"
    }
    val sections = listOf(
        Triple("[INTRO]", "[Emosi: hening, merenung] [Vokal: napas lembut, hampir berbisik] [Musik: piano solo atau petikan gitar, ambience tipis]", "(lirih) Di antara sunyi, kusebut namamu"),
        Triple("[VERSE 1]", "[Emosi: rapuh, jujur, menahan air mata] [Vokal: intim, dekat dengan mikrofon, artikulasi jelas] [Musik: piano lembut, pad tipis]", "(lembut) Ada cerita yang kusimpan sendiri"),
        Triple("[VERSE 1 – LANJUTAN]", "[Emosi: rindu mulai terasa] [Vokal: sedikit bergetar, akhir kalimat ditahan] [Musik: gitar akustik masuk perlahan]", "(tertahan) Tentang $story yang belum usai"),
        Triple("[PRE-CHORUS]", "[Emosi: gelisah berubah menjadi keberanian] [Vokal: intensitas naik bertahap, nada semakin terbuka] [Musik: strings mulai membangun, tom lembut]", "(menanjak) Bila malam kembali menguji langkahku"),
        Triple("[CHORUS / REFF]", "[Emosi: ledakan perasaan, pedih sekaligus penuh harapan] [Vokal: kuat, terbuka, nada panjang pada kata terakhir] [Musik: drum penuh, bass hangat, strings lebar]", "(penuh tenaga) Kan kujaga cahaya di dalam dada"),
        Triple("[CHORUS – LANJUTAN]", "[Emosi: puncak rasa, tulus dan menyentuh] [Vokal: belting terkontrol, harmoni vokal pada baris akhir] [Musik: orkestrasi melebar]", "(menggema) Meski air mata jatuh, ku tak menyerah"),
        Triple("[VERSE 2]", "[Emosi: menerima kenyataan, lebih dewasa] [Vokal: kembali lembut tetapi lebih tegas] [Musik: kick ringan, gitar dan piano tetap dominan]", "(tenang) Kini kupahami arti kehilangan"),
        Triple("[BRIDGE]", "[Emosi: titik terendah lalu bangkit, sangat emosional] [Vokal: mulai rapuh, naik menuju teriakan tertahan] [Musik: instrumen turun sesaat, lalu crescendo strings dan drum]", "(nyaris berbisik) Jika esok tak seperti yang kuminta"),
        Triple("[EMOTIONAL PEAK]", "[Emosi: klimaks paling menyayat, pelepasan seluruh beban] [Vokal: high note atau belting emosional, vibrato alami, jangan berlebihan] [Musik: full orchestra, cymbal swell, drum besar]", "(pecah dalam rasa) Dengarlah hati yang tak pernah berhenti berharap"),
        Triple("[FINAL CHORUS]", "[Emosi: kemenangan batin, haru dan lega] [Vokal: paling kuat, harmoni bertumpuk, ad-lib opsional di akhir] [Musik: full band dan strings sinematik]", "(megah dan tulus) Kan kujaga cahaya di dalam dada"),
        Triple("[OUTRO]", "[Emosi: damai, ikhlas, menyisakan harapan] [Vokal: turun menjadi lembut, kalimat terakhir hampir berbisik] [Musik: piano kembali solo, instrumen fade out]", "(perlahan) Dan kutemukan terang di ujung doa")
    )
    val chosen = when {
        structure.startsWith("Intro, Verse 1") -> listOf(0, 1, 4, 6, 4, 7, 8, 9, 10)
        structure.startsWith("Intro, Verse, Reff") -> listOf(0, 1, 4, 6, 4, 7, 8, 9, 10)
        else -> sections.indices.toList()
    }
    return buildString {
        appendLine("TITLE: $songTitle")
        appendLine("LANGUAGE: Bahasa Indonesia")
        appendLine("GENRE: $genre | MOOD: $mood | VOCAL: $vocal")
        appendLine("PERFORMANCE DIRECTION: $vocalCue")
        appendLine("STORY THEME: $story")
        appendLine()
        chosen.forEachIndexed { index, sectionIndex ->
            val section = sections[sectionIndex]
            appendLine(section.first)
            appendLine(section.second)
            appendLine(section.third)
            if (sectionIndex == 1) appendLine("(jujur, menahan tangis) Tak semua luka mampu kuceritakan")
            if (sectionIndex == 2) appendLine("(lirih, penuh rindu) Namun namamu tinggal di setiap doa")
            if (sectionIndex == 3) appendLine("(semakin kuat) Dan kuberdiri walau dunia meragukan")
            if (sectionIndex == 4) appendLine("(emosi terbuka) Biar waktu menjawab semua air mata")
            if (sectionIndex == 5) appendLine("(harmoni naik) Sebab harapan tak pernah benar-benar sirna")
            if (sectionIndex == 6) appendLine("(hangat) Tak semua perpisahan berarti kehilangan")
            if (sectionIndex == 7) appendLine("(suara bergetar) Masih ada alasan untuk melangkah")
            if (sectionIndex == 8) appendLine("(klimaks, nada tinggi) Inilah suaraku, inilah seluruh rasaku")
            if (sectionIndex == 9) appendLine("(harmoni penuh) Biar waktu menjawab semua air mata")
            if (sectionIndex == 10) appendLine("(fade, sangat lembut) Kuserahkan kisah ini pada Yang Menjaga")
            appendLine()
        }
        appendLine("[PRODUCTION NOTE]")
        appendLine("[Tempo: sesuaikan dengan genre $genre] [Dinamika: bangun perlahan menuju emotional peak, beri jeda sebelum final chorus]")
        appendLine("[Catatan: semua cue adalah arahan performa/aransemen, bukan bagian lirik yang dinyanyikan.]")
    }
}
