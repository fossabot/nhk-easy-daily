package nhk

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.module.kotlin.readValue
import nhk.domain.NHKNews
import nhk.domain.NHKTopNews
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import java.text.SimpleDateFormat

object NHKNewsEasyClient {
    fun getTopNews(): List<NHKTopNews> {
        val okHttpClient = OkHttpClient()
        val request = Request.Builder()
                .url(Constants.TOP_NEWS_URL)
                .build()
        val response = okHttpClient.newCall(request).execute()
        val json = response.body()?.string()

        json?.let {
            val objectMapper = ObjectMapper()
            objectMapper.propertyNamingStrategy = PropertyNamingStrategy.SNAKE_CASE
            objectMapper.dateFormat = SimpleDateFormat(Constants.NHK_NEWS_EASY_DATE_FORMAT)

            return objectMapper.readValue(it)
        }

        return emptyList()
    }

    fun parseNews(nhkTopNews: NHKTopNews): NHKNews {
        val url = "https://www3.nhk.or.jp/news/easy/${nhkTopNews.newsId}/${nhkTopNews.newsId}.html"
        val document = Jsoup.connect(url).get()
        val body = document.getElementById("js-article-body")
        val content = body.html()

        val nhkNews = NHKNews()
        nhkNews.title = nhkTopNews.title
        nhkNews.titleWithRuby = nhkTopNews.titleWithRuby
        nhkNews.outlineWithRuby = nhkTopNews.outlineWithRuby
        nhkNews.url = url
        nhkNews.body = content
        nhkNews.imageUrl = nhkTopNews.newsWebImageUri
        nhkNews.m3u8Url = "https://nhks-vh.akamaihd.net/i/news/easy/${nhkTopNews.newsId}.mp4/master.m3u8"

        return nhkNews
    }
}