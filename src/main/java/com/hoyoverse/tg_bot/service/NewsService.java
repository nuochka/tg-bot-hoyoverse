package com.hoyoverse.tg_bot.service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

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
    public String fetchLatestNews(String game, String capitalizedGameName) {
        String url;

        switch (game.toLowerCase()) {
            case "genshin":
                url = "https://api.ennead.cc/mihoyo/genshin/news/info";
                break;
            case "starrail":
                url = "https://api.ennead.cc/mihoyo/starrail/news/infos";
                break;
            case "zzz":
                url = "https://api.ennead.cc/mihoyo/zenless/news/info";
                break;
            case "honkai3rd":
                url = "https://api.ennead.cc/mihoyo/zenless/news/info";
                break;
            default:
                return "⚠️ Unknown game: " + game;
        }

        try {
            HttpResponse<String> response = client.send(
                HttpRequest.newBuilder(URI.create(url)).GET().build(),
                HttpResponse.BodyHandlers.ofString()
            );

            JsonNode root = mapper.readTree(response.body());
            if (!root.isArray() || root.size() == 0) {
                return "📰 No recent news found.";
            }

            StringBuilder sb = new StringBuilder("📰 Latest News (last 30 days):\n\n");
            int count = 0;

            long now = System.currentTimeMillis();
            long thirtyDaysAgo = now - (30L * 24 * 60 * 60 * 1000);

            for (JsonNode news : root) {
                long createdAt = news.path("createdAt").asLong(0) * 1000L;

                if (createdAt < thirtyDaysAgo) {
                    continue;
                }

                String title = news.path("title").asText(null);
                String link = news.path("url").asText(null);

                if (title == null || link == null || title.isEmpty() || link.isEmpty()) {
                    continue;
                }

                sb.append("• ").append(title).append("\n").append(link).append("\n\n");

                if (++count >= 5) break;
            }

            return count == 0 ? "📰 No recent news in the last 30 days." : sb.toString();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return "⚠️ Error fetching news.";
        }
    }



    public SendMessage buildNewsCommandMessage(String chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText("🎮 Choose a game for getting news");

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        rows.add(Arrays.asList(
            InlineKeyboardButton.builder()
                .text("🌸 Genshin Impact")
                .callbackData("news_genshin")
                .build(),
            InlineKeyboardButton.builder()
                .text("🔮 Zenless Zone Zero")
                .callbackData("news_zzz")
                .build()
        ));

        rows.add(Arrays.asList(
            InlineKeyboardButton.builder()
                .text("🚀 Star Rail")
                .callbackData("news_starrail")
                .build(),
            InlineKeyboardButton.builder()
                .text("⚔ Honkai Impact 3rd")
                .callbackData("news_honkai3rd")
                .build()
        ));

        markup.setKeyboard(rows);
        message.setReplyMarkup(markup);

        return message;
    }
}
