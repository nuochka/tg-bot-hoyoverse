package com.hoyoverse.tg_bot.service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class CalendarService {
    private final TelegramSenderService senderService;
    private final SubscribeService subscribeService;
    private final HttpClient client;
    private final ObjectMapper mapper;

    public CalendarService(TelegramSenderService senderService, SubscribeService subscribeService){
        this.senderService = senderService;
        this.subscribeService = subscribeService;
        this.client = HttpClient.newHttpClient();
        this.mapper = new ObjectMapper();
    }

    public String fetchCalendar(String game, String category, String language) {
        if (!game.equalsIgnoreCase("genshin") && !game.equalsIgnoreCase("starrail")) {
            return "⚠ Calendar is only available for Genshin Impact and Star Rail.";
        }

        if (category == null || category.isEmpty()) {
            category = "events";
        }

        if (!List.of("events", "banners", "challenges").contains(category)) {
            return "⚠ Unknown category: " + category + ". Use 'events', 'banners', or 'challenges'.";
        }

        String url = String.format("https://api.ennead.cc/mihoyo/%s/calendar?category=%s&lang=%s",
                game.toLowerCase(), category, language);

        try {
            HttpResponse<String> response = client.send(
                    HttpRequest.newBuilder(URI.create(url)).GET().build(),
                    HttpResponse.BodyHandlers.ofString()
            );

            JsonNode root = mapper.readTree(response.body());
            JsonNode categoryArray = root.path(category);

            if (!categoryArray.isArray() || categoryArray.isEmpty()) {
                return "📭 No calendar entries found for " + subscribeService.capitalizeGameName(game) + " in category '" + category + "'.";
            }

            StringBuilder sb = new StringBuilder("📅 " + subscribeService.capitalizeGameName(game) + " — " + capitalizeCategory(category) + ":\n\n");

            for (JsonNode item : categoryArray) {
                String title = item.path("name").asText("No title");
                long startTimestamp = item.path("start_time").asLong(0);
                long endTimestamp = item.path("end_time").asLong(0);

                String time = formatUnixRange(startTimestamp, endTimestamp);
                sb.append("📌 ").append(title).append(" — ").append(time).append("\n");
            }

            return sb.toString();

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return "⚠ Error fetching calendar data.";
        }
    }

    public void handleCalendarCommand(String chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("📅 Select game to view current events and banners:");

        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        for (String game : List.of("genshin", "starrail")) {
            rows.add(Collections.singletonList(
                InlineKeyboardButton.builder()
                    .text("🎮 " + subscribeService.capitalizeGameName(game))
                    .callbackData("calendar_" + game)
                    .build()
            ));
        }

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(rows);
        message.setReplyMarkup(markup);

        senderService.sendMessage(message);
    }

    public void handleCategorySelection(String chatId, String game) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("📂 Select category for " + subscribeService.capitalizeGameName(game) + ":");

        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        for (String category : List.of("events", "banners", "challenges")) {
            rows.add(Collections.singletonList(
                InlineKeyboardButton.builder()
                    .text("📌 " + capitalizeCategory(category))
                    .callbackData("calendar_" + game + "_" + category)
                    .build()
            ));
        }

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(rows);
        message.setReplyMarkup(markup);

        senderService.sendMessage(message);
    }

    private String formatUnixRange(long start, long end) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneOffset.UTC);
        return formatter.format(Instant.ofEpochSecond(start)) + " ~ " + formatter.format(Instant.ofEpochSecond(end));
    }

    private String capitalizeCategory(String category) {
        return switch (category) {
            case "events" -> "Events";
            case "banners" -> "Banners";
            case "challenges" -> "Challenges";
            default -> category;
        };
    }
}
