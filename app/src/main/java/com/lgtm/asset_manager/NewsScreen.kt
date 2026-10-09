package com.lgtm.asset_manager

import android.content.Intent
import android.net.Uri
import android.util.Xml
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.xmlpull.v1.XmlPullParser
import java.net.HttpURLConnection
import java.net.URL

private data class EconomyNewsItem(
    val title: String,
    val link: String,
    val source: String,
    val publishedAt: String,
)

@Composable
fun EconomyNewsScreen() {
    var articles by remember { mutableStateOf<List<EconomyNewsItem>>(emptyList()) }
    var interestArticles by remember { mutableStateOf<List<EconomyNewsItem>>(emptyList()) }
    var recommendedArticles by remember { mutableStateOf<List<EconomyNewsItem>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val preferences = remember { context.getSharedPreferences("economy_news_preferences", android.content.Context.MODE_PRIVATE) }
    var tags by remember { mutableStateOf(preferences.getStringSet("tags", emptySet()).orEmpty().toList().sorted()) }
    var editingTags by remember { mutableStateOf(false) }
    var tagInput by remember { mutableStateOf(tags.joinToString(", ")) }
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    fun chooseArticles(source: List<EconomyNewsItem>, selectedTags: List<String>) {
        val tagged = if (selectedTags.isEmpty()) emptyList() else source.filter { article ->
            selectedTags.any { tag -> article.title.contains(tag, ignoreCase = true) }
        }.shuffled().take(10)
        interestArticles = tagged
        recommendedArticles = source.filterNot { it in tagged }.shuffled().take(10)
    }

    fun refresh() {
        scope.launch {
            loading = true
            error = null
            runCatching { withContext(Dispatchers.IO) { loadEconomyNews() } }
                .onSuccess { latestArticles ->
                    articles = latestArticles
                    chooseArticles(latestArticles, tags)
                }
                .onFailure { error = "뉴스를 불러오지 못했습니다. 네트워크를 확인하고 다시 시도해 주세요." }
            loading = false
        }
    }

    LaunchedEffect(Unit) { refresh() }
    LaunchedEffect(interestArticles, recommendedArticles) {
        if (interestArticles.isNotEmpty() || recommendedArticles.isNotEmpty()) listState.animateScrollToItem(0)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 12.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("국내외 경제·금융 주요 뉴스", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    if (tags.isEmpty()) "태그를 설정하면 관련 기사를 먼저 보여드려요" else "관심 태그: ${tags.joinToString(", ")}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            TextButton(onClick = { tagInput = tags.joinToString(", "); editingTags = true }) { Text("태그 설정") }
            IconButton(onClick = ::refresh, enabled = !loading) {
                Icon(Icons.Default.Refresh, contentDescription = "뉴스 새로고침")
            }
        }
        when {
            loading && articles.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            error != null && articles.isEmpty() -> Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                Text(error!!, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            articles.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("표시할 경제 뉴스가 없습니다.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            else -> LazyColumn(
                state = listState,
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                if (tags.isNotEmpty()) {
                    item(key = "interest_header") {
                        Text("관심 기사", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp, bottom = 2.dp))
                    }
                    if (interestArticles.isEmpty()) {
                        item(key = "interest_empty") {
                            Text("설정한 태그가 포함된 기사가 없습니다.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    } else {
                        items(interestArticles, key = { "interest:${it.link}" }) { article ->
                            EconomyNewsCard(article = article, onClick = {
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(article.link)))
                            })
                        }
                    }
                }
                item(key = "recommended_header") {
                    Text("추천 기사", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 12.dp, bottom = 2.dp))
                }
                items(recommendedArticles, key = { "recommended:${it.link}" }) { article ->
                    EconomyNewsCard(article = article, onClick = {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(article.link)))
                    })
                }
            }
        }
    }

    if (editingTags) {
        AlertDialog(
            onDismissRequest = { editingTags = false },
            title = { Text("관심 경제 태그") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("관심 키워드를 쉼표로 구분해 입력하세요. 예: 반도체, 금리, 부동산")
                    OutlinedTextField(
                        value = tagInput,
                        onValueChange = { tagInput = it },
                        label = { Text("관심 태그") },
                        placeholder = { Text("반도체, 금리, 부동산") },
                        singleLine = false,
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val updatedTags = tagInput.split(',', '\n').map(String::trim).filter(String::isNotBlank).distinctBy(String::lowercase)
                    preferences.edit().putStringSet("tags", updatedTags.toSet()).apply()
                    tags = updatedTags
                    chooseArticles(articles, updatedTags)
                    editingTags = false
                }) { Text("저장") }
            },
            dismissButton = { TextButton(onClick = { editingTags = false }) { Text("취소") } },
        )
    }
}

@Composable
private fun EconomyNewsCard(article: EconomyNewsItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(article.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(
                listOf(article.source, article.publishedAt).filter(String::isNotBlank).joinToString(" · "),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private fun loadEconomyNews(): List<EconomyNewsItem> {
    val feedUrl = URL("https://news.google.com/rss/headlines/section/topic/BUSINESS?hl=ko&gl=KR&ceid=KR:ko")
    val connection = feedUrl.openConnection() as HttpURLConnection
    connection.connectTimeout = 12_000
    connection.readTimeout = 12_000
    connection.setRequestProperty("User-Agent", "AssetManager/1.2 (Android)")
    return try {
        connection.inputStream.use { input ->
            val parser = Xml.newPullParser().apply {
                setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
                setInput(input, "UTF-8")
            }
            val result = mutableListOf<EconomyNewsItem>()
            var event = parser.eventType
            var inItem = false
            var tag = ""
            var title = ""
            var link = ""
            var source = ""
            var publishedAt = ""
            while (event != XmlPullParser.END_DOCUMENT && result.size < 100) {
                when (event) {
                    XmlPullParser.START_TAG -> {
                        tag = parser.name
                        if (tag == "item") {
                            inItem = true
                            title = ""; link = ""; source = ""; publishedAt = ""
                        }
                        if (inItem && tag == "source") source = parser.getAttributeValue(null, "url").orEmpty()
                    }
                    XmlPullParser.TEXT -> if (inItem) when (tag) {
                        "title" -> title += parser.text
                        "link" -> link += parser.text
                        "source" -> source = parser.text
                        "pubDate" -> publishedAt = parser.text
                    }
                    XmlPullParser.END_TAG -> if (parser.name == "item") {
                        if (title.isNotBlank() && link.startsWith("https://")) {
                            result += EconomyNewsItem(title.trim(), link.trim(), source.trim(), publishedAt.trim())
                        }
                        inItem = false
                    }
                }
                event = parser.next()
            }
            result
        }
    } finally {
        connection.disconnect()
    }
}
