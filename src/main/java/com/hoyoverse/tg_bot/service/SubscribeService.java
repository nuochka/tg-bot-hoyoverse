package com.hoyoverse.tg_bot.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

@Service
public class SubscribeService {

    private final TelegramSenderService senderService;
    private final Map<String, Set<String>> userSubscriptions = new ConcurrentHashMap<>();

    @Autowired
    public SubscribeService(@Lazy TelegramSenderService senderService) {
        this.senderService = senderService;
    }
    
    public void subscribe(String chatId, String game){
        userSubscriptions.computeIfAbsent(chatId, k -> ConcurrentHashMap.newKeySet()).add(game);
    }

    public void unsubscribe(String chatId, String game) {
        Set<String> subscriptions = userSubscriptions.get(chatId);
        if (subscriptions != null) {
            subscriptions.remove(game);
            if (subscriptions.isEmpty()) {
                userSubscriptions.remove(chatId);
            }
        }
    }

    public Set<String> getSubscriptions(String chatId) {
        return userSubscriptions.getOrDefault(chatId, Collections.emptySet());
    }

    public Map<String, Set<String>> getAllSubscriptions() {
        return Collections.unmodifiableMap(userSubscriptions);
    }

    public void handleSubscribeCommand(String chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Select the games you want to subscribe to for promo codes:");

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();

        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        rows.add(Collections.singletonList(
            InlineKeyboardButton.builder()
                .text("🌸 Genshin Impact")
                .callbackData("sub_genshin")
                .build()
        ));

        rows.add(Collections.singletonList(
            InlineKeyboardButton.builder()
                .text("🚀 Star Rail")
                .callbackData("sub_starrail")
                .build()
        ));

        rows.add(Collections.singletonList(
            InlineKeyboardButton.builder()
                .text("🌀 Zenless Zone Zero")
                .callbackData("sub_zzz")
                .build()
        ));

        rows.add(Collections.singletonList(
            InlineKeyboardButton.builder()
                .text("⚔ Honkai Impact 3rd")
                .callbackData("sub_honkai3rd")
                .build()
        ));

        markup.setKeyboard(rows);
        message.setReplyMarkup(markup);

        senderService.sendMessage(message);
    }

    public void handleUnsubscribeCommand(String chatId) {
        Set<String> subscriptions = getSubscriptions(chatId);

        if (subscriptions.isEmpty()) {
            senderService.sendText(chatId, "📭 You don’t have any active subscriptions.");
            return;
        }

        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("🔔 Select the game you want to unsubscribe from:");

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        for (String game : subscriptions) {
            rows.add(Collections.singletonList(
                InlineKeyboardButton.builder()
                    .text(capitalizeGameName(game))
                    .callbackData("unsub_" + game)
                    .build()
            ));
        }

        rows.add(Collections.singletonList(
            InlineKeyboardButton.builder()
                .text("❌ Unsubscribe from all")
                .callbackData("unsub_all")
                .build()
        ));

        markup.setKeyboard(rows);
        message.setReplyMarkup(markup);

        senderService.sendMessage(message);
    }
    
    private String capitalizeGameName(String game) {
        return switch (game) {
            case "genshin" -> "Genshin Impact";
            case "starrail" -> "Honkai: Star Rail";
            case "zzz" -> "Zenless Zone Zero";
            case "honkai3rd" -> "Honkai Impact 3rd";
            default -> game;
        };
    }
}
