package com.hoyoverse.tg_bot.service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class NewsService {
    private final HttpClient client;
    private final ObjectMapper mapper;

    public NewsService(){
        this.client = HttpClient.newHttpClient();
        this.mapper = new ObjectMapper();
    }
    public String fetchLatestNews() {
        String url = "https://api.ennead.cc/mihoyo/genshin/news/info";

        try {
            HttpResponse<String> response = client.send(
                HttpRequest.newBuilder(URI.create(url)).GET().build(),
                HttpResponse.BodyHandlers.ofString()
            );

            System.out.println("API response: " + response.body());

            JsonNode root = mapper.readTree(response.body());
            if (!root.isArray() || root.size() == 0) {
                return "📰 No recent news found.";
            }

            StringBuilder sb = new StringBuilder("📰 Latest News:\n\n");
            int count = 0;
            for (JsonNode news : root) {
                if (++count > 5) break;

                String title = news.path("title").asText(null);
                String link = news.path("url").asText(null);

                if (title == null || link == null || title.isEmpty() || link.isEmpty()) {
                    continue;
                }

                sb.append("• ").append(title).append("\n").append(link).append("\n\n");
            }
            return sb.toString();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return "⚠ Error fetching news.";
        }
    }
}
