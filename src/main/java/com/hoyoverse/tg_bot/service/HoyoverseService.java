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
public class HoyoverseService {
    private final HttpClient client;
    private final ObjectMapper mapper;

    public HoyoverseService(){
        this.client = HttpClient.newHttpClient();
        this.mapper = new ObjectMapper();
    }

    public String fetchPromocodes(String game) {
        String url;

        switch (game.toLowerCase()) {
            case "genshin":
                url = "https://api.ennead.cc/mihoyo/genshin/codes";
                break;
            case "starrail":
                url = "https://api.ennead.cc/mihoyo/starrail/codes";
                break;
            case "zzz":
                url = "https://api.ennead.cc/mihoyo/zenless/codes";
                break;
            case "honkai3rd":
                url = "https://api.ennead.cc/mihoyo/honkai3rd/codes";
                break;
            default:
                return "⚠ Unknown game: " + game;
        }

        try {
            HttpResponse<String> response = client.send(
                HttpRequest.newBuilder(URI.create(url)).GET().build(),
                HttpResponse.BodyHandlers.ofString()
            );

            JsonNode root = mapper.readTree(response.body());
            JsonNode codesArray = root.path("active");

            if (!codesArray.isArray() || codesArray.isEmpty()) {
                return "🎁 No promocodes found at the moment.";
            }

            StringBuilder sb = new StringBuilder("🎁 Active Promoсodes for " + game.toUpperCase() + ":\n\n");
            for (JsonNode codeNode : codesArray) {
                String code = codeNode.path("code").asText();
                JsonNode rewardsArray = codeNode.path("rewards");
                StringBuilder rewardsBuilder = new StringBuilder();
                if(rewardsArray.isArray()){
                    for(JsonNode rewardNode : rewardsArray) {
                        if(rewardsBuilder.length() > 0) rewardsBuilder.append(", ");
                        rewardsBuilder.append(rewardNode.asText());
                    }
                }
                String reward = rewardsBuilder.toString();
                sb.append("🔑 Code: ").append(code).append("\n");
                sb.append("🎁 Reward: ").append(reward).append("\n");
                sb.append("\n");
            }
            return sb.toString();

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return "⚠ Error fetching promocodes.";
        }
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
