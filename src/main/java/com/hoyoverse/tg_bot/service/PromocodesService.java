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
public class PromocodesService {
    private final HttpClient client;
    private final ObjectMapper mapper;

    public PromocodesService() {
        this.client = HttpClient.newHttpClient();
        this.mapper = new ObjectMapper();
    }

    public String fetchPromocodes(String game, String capitalizedGameName) {
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

            StringBuilder sb = new StringBuilder("🎁 Active Promoсodes for " + capitalizedGameName + ":\n\n");

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
                sb.append("🔑 ").append(code).append("\n");
                sb.append("🎁 Reward: ").append(reward).append("\n\n");
            }

            sb.append("\n✨ Activate promocodes here: ").append(getActivationLink(game));
            return sb.toString();

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return "⚠ Error fetching promocodes.";
        }
    }

    public SendMessage buildCodesCommandMessage(Long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText("🎮 Choose a game for getting promocodes");

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        rows.add(Arrays.asList(
            InlineKeyboardButton.builder()
                .text("🌸 Genshin Impact")
                .callbackData("codes_genshin")
                .build(),
            InlineKeyboardButton.builder()
                .text("🔮 Zenless Zone Zero")
                .callbackData("codes_zzz")
                .build()
        ));

        rows.add(Arrays.asList(
            InlineKeyboardButton.builder()
                .text("🚀 Star Rail")
                .callbackData("codes_starrail")
                .build(),
            InlineKeyboardButton.builder()
                .text("⚔ Honkai Impact 3rd")
                .callbackData("codes_honkai3rd")
                .build()
        ));

        markup.setKeyboard(rows);
        message.setReplyMarkup(markup);

        return message;
    }

    private String getActivationLink(String game) {
        return switch (game) {
            case "genshin" -> "https://genshin.hoyoverse.com/en/gift";
            case "starrail" -> "https://hsr.hoyoverse.com/gift";
            case "zzz" -> "https://zenless.hoyoverse.com/en/gift";
            case "honkai3rd" -> "https://honkaiimpact3.hoyoverse.com/gift";
            default -> "https://account.hoyoverse.com";
        };
    }
}

