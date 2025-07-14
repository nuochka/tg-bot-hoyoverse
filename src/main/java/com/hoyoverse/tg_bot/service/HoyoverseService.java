package com.hoyoverse.tg_bot.service;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

@Service
public class HoyoverseService {

    public String fetchPromocodes() {
        try {
            Document doc = Jsoup.connect("https://genshin.hoyoverse.com/en/news").get();
            Elements links = doc.select("a[href*='redeem']");
            if (links.isEmpty()) {
                return "No new promo codes available.";
            }
            StringBuilder stringBuilder = new StringBuilder("🎁 Fresh promo codes:\n");
            for (Element link : links) {
                stringBuilder.append(link.absUrl("href")).append("\n");
            }
            return stringBuilder.toString();
        } catch (IOException e) {
            return "⚠ Error loading promo codes.";
        }
    }

    public String fetchLatestNews() {
        try {
            Document doc = Jsoup.connect("https://genshin.hoyoverse.com/en/news").get();
            System.out.println(doc);
            Elements articles = doc.select(".article-list .article-item");
            StringBuilder stringBuilder = new StringBuilder("📰 Latest news:\n\n");
            int count = 0;
            for (Element article : articles) {
                if (++count > 5) break;
                String title = article.select("h3").text();
                String link = article.select("a").attr("href");
                stringBuilder.append("• ").append(title).append("\n")
                    .append("https://genshin.hoyoverse.com").append(link).append("\n\n");  
            }
            return stringBuilder.toString();
        } catch (IOException e) {
            return "⚠ Error loading news.";
        }
    }
}
