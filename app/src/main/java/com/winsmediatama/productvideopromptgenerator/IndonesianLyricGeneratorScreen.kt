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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.PasswordVisualTransformation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController

@Composable
fun IndonesianLyricGeneratorScreen(nav: NavHostController) {
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var apiKey by remember { mutableStateOf(OpenAiKeyStore.get(context).orEmpty()) }
    var apiStatus by remember { mutableStateOf("") }
    var isGenerating by remember { mutableStateOf(false) }
    var title by remember { mutableStateOf("Di Ujung Doa") }
    var theme by remember { mutableStateOf("perjuangan, kehilangan, harapan dan doa") }
    var language by remember { mutableStateOf("Bahasa Indonesia") }
    var genre by remember { mutableStateOf("Pop Ballad") }
    var mood by remember { mutableStateOf("Menyayat hati") }
    var vocal by remember { mutableStateOf("Pria") }
    var structure by remember { mutableStateOf("Intro, Verse, Pre-Chorus, Chorus, Verse 2, Bridge, Final Chorus, Outro") }
    var lyric by remember { mutableStateOf("") }
    var musicPrompt by remember { mutableStateOf("") }
    var generationIndex by remember { mutableIntStateOf(0) }

    Column(
        Modifier.fillMaxSize().background(StudioBackground).navigationBarsPadding()
            .verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("‹ Back", color = StudioCyan, modifier = Modifier.clickable { nav.popBackStack() })
        Text("Bilingual Lyric Studio", color = StudioText, fontSize = 25.sp, fontWeight = FontWeight.Bold)
        Text("Buat lirik dengan OpenAI GPT atau gunakan generator offline. Cue vokal, instrumen, dan dinamika emosi tetap disertakan.", color = StudioMuted, fontSize = 13.sp)
        Surface(color = StudioSurface, shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("OPENAI API KEY", color = StudioCyan, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                OutlinedTextField(
                    value = apiKey,
                    onValueChange = { apiKey = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    label = { Text("Masukkan API key milik Anda") },
                    placeholder = { Text("sk-...") }
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = {
                        val cleanKey = apiKey.trim()
                        if (cleanKey.startsWith("sk-")) {
                            OpenAiKeyStore.save(context, cleanKey)
                            apiStatus = "API key tersimpan terenkripsi di perangkat ini."
                        } else {
                            apiStatus = "Periksa API key. Format key OpenAI biasanya diawali sk-."
                        }
                    }, modifier = Modifier.weight(1f)) { Text("Simpan Key") }
                    TextButton(onClick = {
                        OpenAiKeyStore.clear(context)
                        apiKey = ""
                        apiStatus = "API key dihapus dari perangkat."
                    }) { Text("Hapus") }
                }
                Text("Key disimpan terenkripsi di perangkat dan tidak dimasukkan ke GitHub. Penggunaan API mengikuti biaya akun OpenAI Anda.", color = StudioMuted, fontSize = 11.sp)
                if (apiStatus.isNotBlank()) Text(apiStatus, color = StudioCyan, fontSize = 11.sp)
            }
        }
        OutlinedTextField(value = title, onValueChange = { title = it }, modifier = Modifier.fillMaxWidth(),
            label = { Text("Judul lagu") }, singleLine = true)
        OutlinedTextField(value = theme, onValueChange = { theme = it }, modifier = Modifier.fillMaxWidth(),
            label = { Text("Tema / cerita lagu") }, minLines = 2)
        LyricDropdown("Bahasa lirik", language, listOf("Bahasa Indonesia", "English")) { language = it }
        LyricDropdown("Genre musik", genre, listOf("Pop Ballad", "Pop Rock", "Indie Folk", "Dangdut", "Akustik", "Religi", "Orkestra Sinematik", "R&B", "Rock Alternatif")) { genre = it }
        LyricDropdown("Nuansa emosi", mood, listOf("Menyayat hati", "Romantis", "Penuh harapan", "Spiritual dan khusyuk", "Semangat dan heroik", "Nostalgia", "Penuh penyesalan", "Bahagia")) { mood = it }
        LyricDropdown("Karakter vokal", vocal, listOf("Pria", "Wanita", "Duet", "Paduan suara")) { vocal = it }
        LyricDropdown("Struktur lagu", structure, listOf(
            "Intro, Verse, Pre-Chorus, Chorus, Verse 2, Bridge, Final Chorus, Outro",
            "Intro, Verse 1, Chorus, Verse 2, Chorus, Bridge, Final Chorus, Outro",
            "Intro, Verse, Reff, Verse 2, Reff, Interlude, Bridge, Reff, Outro"
        )) { structure = it }

        Button(onClick = {
            if (apiKey.isBlank()) {
                apiStatus = "Masukkan dan simpan OpenAI API key terlebih dahulu, atau gunakan generator offline."
            } else if (!isGenerating) {
                isGenerating = true
                apiStatus = "Menghubungi OpenAI..."
                scope.launch {
                    val result = withContext(Dispatchers.IO) {
                        runCatching {
                            OpenAiLyricClient.generate(
                                apiKey = apiKey.trim(), title = title, theme = theme, genre = genre,
                                mood = mood, vocal = vocal, structure = structure, language = language
                            )
                        }
                    }
                    result.onSuccess { generated ->
                        lyric = generated
                        musicPrompt = buildMusicPrompt(title, theme, genre, mood, vocal, language, structure)
                        apiStatus = "Lirik berhasil dibuat oleh GPT."
                    }.onFailure { error ->
                        apiStatus = "GPT gagal: ${error.message ?: "Periksa koneksi dan API key."}"
                    }
                    isGenerating = false
                }
            }
        }, enabled = !isGenerating, modifier = Modifier.fillMaxWidth().height(54.dp),
            colors = ButtonDefaults.buttonColors(containerColor = StudioCyan, contentColor = StudioBackground),
            shape = RoundedCornerShape(14.dp)) {
            if (isGenerating) CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            else Text(if (language == "English") "✦  Generate with GPT" else "✦  Buat Lirik dengan GPT", fontWeight = FontWeight.Bold)
        }
        OutlinedButton(onClick = {
            generationIndex += 1
            lyric = buildIndonesianLyric(title, theme, genre, mood, vocal, structure, language, generationIndex)
            musicPrompt = buildMusicPrompt(title, theme, genre, mood, vocal, language, structure)
            apiStatus = "Lirik dibuat menggunakan generator offline."
        }, modifier = Modifier.fillMaxWidth(), enabled = !isGenerating) {
            Text("Generate Offline (Tanpa API)")
        }

        if (lyric.isNotBlank()) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("HASIL LIRIK + CUE", color = StudioCyan, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                TextButton(onClick = { clipboard.setText(AnnotatedString(lyric + "\n\n[MUSIC PROMPT]\n" + musicPrompt)) }) { Text("Copy Lyrics + Prompt") }
            }
            OutlinedTextField(value = lyric, onValueChange = { lyric = it }, modifier = Modifier.fillMaxWidth(),
                minLines = 18, label = { Text("Edit hasil lirik") })
            OutlinedButton(onClick = { clipboard.setText(AnnotatedString(lyric)) }, modifier = Modifier.fillMaxWidth()) {
                Text("Copy Lyrics for Suno / Music AI")
            }
            Text("Cue dalam tanda kurung siku memberi arahan aransemen dan performa. Cue emosi dan vokal disertakan pada setiap bagian lagu.", color = StudioMuted, fontSize = 11.sp)
            Spacer(Modifier.height(8.dp))
            Text("MUSIC GENERATION PROMPT", color = StudioCyan, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Text("Prompt musik dibuat otomatis dari genre, emosi, karakter vokal, dan bahasa yang dipilih.", color = StudioMuted, fontSize = 11.sp)
            OutlinedTextField(value = musicPrompt, onValueChange = { musicPrompt = it }, modifier = Modifier.fillMaxWidth(),
                minLines = 7, label = { Text("Music prompt for Suno / Music AI") })
            OutlinedButton(onClick = { clipboard.setText(AnnotatedString(musicPrompt)) }, modifier = Modifier.fillMaxWidth()) {
                Text("Copy Music Prompt")
            }
            Button(onClick = { clipboard.setText(AnnotatedString(lyric + "\n\n[MUSIC PROMPT]\n" + musicPrompt)) },
                modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = StudioCyan, contentColor = StudioBackground)) {
                Text("Copy Lyrics + Music Prompt", fontWeight = FontWeight.Bold)
            }
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


private fun buildMusicPrompt(
    title: String,
    theme: String,
    genre: String,
    mood: String,
    vocal: String,
    language: String,
    structure: String
): String {
    val genreDirection = when (genre) {
        "Pop Ballad" -> "emotional contemporary pop ballad, expressive piano foundation, warm strings, subtle acoustic guitar, wide cinematic drums and a memorable soaring chorus"
        "Pop Rock" -> "anthemic pop rock, driving live drums, melodic electric guitars, warm bass, dynamic verses and a powerful guitar-supported chorus"
        "Indie Folk" -> "intimate indie folk, organic acoustic guitars, gentle percussion, warm bass, subtle atmospheric textures and natural harmonies"
        "Dangdut" -> "modern Indonesian dangdut arrangement, expressive kendang grooves, melodic suling accents, rhythmic bass, bright keyboards and an energetic yet emotional chorus"
        "Akustik" -> "organic acoustic arrangement, fingerpicked acoustic guitar, intimate piano, soft percussion and natural vocal dynamics"
        "Religi" -> "devotional spiritual arrangement, heartfelt piano, gentle strings, subtle frame drums, reverent atmosphere and uplifting choir harmonies"
        "Orkestra Sinematik" -> "grand cinematic orchestral arrangement, expressive piano, sweeping strings, French horns, deep percussion, gradual crescendos and an epic emotional climax"
        "R&B" -> "smooth contemporary R&B, soulful chords, warm electric piano, deep rounded bass, restrained modern drums, tasteful vocal runs and layered harmonies"
        "Rock Alternatif" -> "atmospheric alternative rock, textured electric guitars, powerful live drums, deep bass, dynamic builds and an emotionally intense vocal performance"
        else -> "polished contemporary arrangement with expressive instrumentation and dynamic production"
    }
    val moodDirection = when (mood) {
        "Menyayat hati" -> "deeply heartbreaking, vulnerable, melancholic and emotionally cathartic"
        "Romantis" -> "tender, intimate, warm and deeply romantic"
        "Penuh harapan" -> "hopeful, uplifting, inspiring and gradually triumphant"
        "Spiritual dan khusyuk" -> "reverent, peaceful, contemplative and spiritually moving"
        "Semangat dan heroik" -> "bold, energetic, heroic and motivational"
        "Nostalgia" -> "nostalgic, wistful, warm and reflective"
        "Penuh penyesalan" -> "regretful, haunting, sincere and full of longing"
        "Bahagia" -> "joyful, bright, celebratory and uplifting"
        else -> "emotional and expressive"
    }
    val vocalDirection = when (vocal) {
        "Wanita" -> "female lead vocal, expressive and clear, with controlled vibrato"
        "Duet" -> "male and female duet, alternating conversational verses and powerful blended harmonies in the chorus"
        "Paduan suara" -> "solo lead supported by a rich mixed choir, layered harmonies and a grand final chorus"
        else -> "male lead vocal, warm expressive tone, clear diction and emotional phrasing"
    }
    val lyricLanguage = if (language == "English") "English lyrics only, natural fluent English pronunciation" else "Indonesian lyrics only, natural Bahasa Indonesia pronunciation"
    return """
Create an original song titled "$title".
Lyrics theme and story: \${theme.ifBlank { "a heartfelt personal journey" }}.
Style: $genreDirection.
Emotional direction: $moodDirection.
Vocals: $vocalDirection.
Language: $lyricLanguage.
Song structure: $structure.
Arrangement: begin with a distinctive instrumental intro, keep verses intimate, build tension in the pre-chorus, deliver a memorable wide chorus, create a contrasting emotional bridge, then reach a powerful final chorus and a gentle resolved outro.
Production: professional studio quality, balanced modern mix, warm detailed instrumentation, clear upfront vocals, natural dynamics, tasteful reverb, strong melodic hooks and polished mastering.
Performance: expressive storytelling, accurate lyric phrasing, emotional progression between sections, no spoken explanations, no extra ad-libs that obscure the lyrics.
""".trim()
}

private fun buildIndonesianLyric(
    title: String,
    theme: String,
    genre: String,
    mood: String,
    vocal: String,
    structure: String,
    language: String,
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
    val englishVerseVariants = listOf(
        listOf("I hide my stories behind a smile", "Still walking through the endless night", "A silent longing stays inside", "And waits for dawn to bring the light"),
        listOf("I asked the stars where dreams could go", "Why every road feels far too long", "A little flame still burns below", "And gives my weary heart its song"),
        listOf("I trace the footprints left behind", "Your name returns with every breeze", "Though fragile days can shake my mind", "I keep the dreams I used to see")
    )
    val englishVerseTwoVariants = listOf(
        listOf("I hold the memories close to me", "No longer running from the truth", "The scars have taught my heart to breathe", "And find the strength to start anew"),
        listOf("The days move on and change their tune", "What once was lost still shaped my way", "I keep the stories like the moon", "That guides me through another day"),
        listOf("If tomorrow tests my soul", "I'll face the storm and still believe", "A heart can mend and become whole", "When hope is what we choose to keep")
    )
    val englishChorusVariants = listOf(
        listOf("Carry my prayer across the night", "Don't let the fading stars lose light", "Though tears may fall like summer rain", "My heart will rise and hope again"),
        listOf("I still believe the storm will pass", "A light is born behind the glass", "Though life may never feel the same", "Love keeps alive a quiet flame"),
        listOf("Can you hear the voice inside", "Calling with a faith that will not hide", "Let time reveal what we can't see", "A road will open for you and me")
    )
    val englishBridgeVariants = listOf(
        listOf("When my tired feet can barely stand", "Remind me why I began this ride", "In the silence where I lose my way", "Your love is here, my strength, my guide"),
        listOf("If I must let it all depart", "Let prayer become the breath I take", "When every door is closing down", "Faith is the ground I will not forsake"),
        listOf("I don't need the world to understand", "As long as there's a light within", "From every piece I'll rise again", "A braver heart is born within")
    )
    val englishOutroVariants = listOf(
        listOf("I leave my story in Your hands", "With every prayer, my spirit stands", "I'll greet tomorrow without fear", "For hope is still alive in here"),
        listOf("When night returns to call my name", "I won't be lost inside the rain", "A prayer will be my friend for life", "And lead me through the darkest night"),
        listOf("I let the restless wind pass by", "And trust the path beneath the sky", "At the end of every prayer", "I find the strength to love and care")
    )
    val useEnglish = language == "English"
    val activeVerse = if (useEnglish) englishVerseVariants else verseVariants
    val activeVerseTwo = if (useEnglish) englishVerseTwoVariants else verseTwoVariants
    val activeChorus = if (useEnglish) englishChorusVariants else chorusVariants
    val activeBridge = if (useEnglish) englishBridgeVariants else bridgeVariants
    val activeOutro = if (useEnglish) englishOutroVariants else outroVariants
    val index = Math.floorMod(variation - 1, verseVariants.size)
    val sections = listOf(
        Triple("[VERSE 1]", "[Emosi: intim, rapuh, mulai bercerita] [Vokal: lembut dan dekat] [Musik: piano/gitar minimal]", activeVerse[index]),
        Triple("[PRE-CHORUS]", if (useEnglish) "[Emotion: tension and hope are rising] [Vocal: gradually building intensity] [Music: strings and percussion slowly enter]" else "[Emosi: ketegangan dan harapan mulai naik] [Vokal: intensitas bertahap] [Musik: strings dan perkusi perlahan masuk]", if (useEnglish) listOf("I try to stand up once again", "Though shadows follow where I go", "I breathe in deep and face the pain", "A little light begins to glow") else listOf("Kucoba berdiri sekali lagi", "Meski bayang ragu mengikuti", "Kutarik nafas, kuteguhkan hati", "Sebentar lagi terang menanti")),
        Triple("[CHORUS / REFF]", "[Emosi: ledakan rasa dan pesan utama lagu] [Vokal: kuat, terbuka, nada panjang pada baris akhir] [Musik: full band dan strings lebar]", activeChorus[index]),
        Triple("[VERSE 2]", "[Emosi: lebih dewasa dan menerima] [Vokal: lembut tetapi tegas] [Musik: groove ringan]", activeVerseTwo[index]),
        Triple("[BRIDGE]", "[Emosi: titik terendah berubah menjadi kebangkitan] [Vokal: mulai lirih lalu menguat] [Musik: drop sesaat lalu crescendo]", activeBridge[index]),
        Triple("[FINAL CHORUS]", "[Emosi: klimaks, lega dan penuh keyakinan] [Vokal: paling kuat, harmoni bertumpuk] [Musik: full orchestra/band]", activeChorus[(index + 1) % activeChorus.size]),
        Triple("[OUTRO]", "[Emosi: damai dan ikhlas] [Vokal: turun lembut] [Musik: piano atau gitar fade out]", activeOutro[index])
    )
    val selectedSections = when {
        structure.startsWith("Intro, Verse 1") -> sections
        structure.startsWith("Intro, Verse, Reff") -> sections
        else -> sections
    }

    return buildString {
        appendLine("TITLE: $songTitle")
        appendLine("LANGUAGE: $language")
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
