package com.hoyoverse.tg_bot.service;

import java.util.List;
import java.util.Map;
import java.util.Set;


import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

@Service
public class NotificationService {
    private final SubscribeService subscribeService;
    private final PromocodesService promocodesService;
    private final TelegramSenderService telegramSenderService;

    public NotificationService(SubscribeService subscribeService, PromocodesService promocodesService, TelegramSenderService telegramSenderService){
        this.subscribeService = subscribeService;
        this.promocodesService = promocodesService;
        this.telegramSenderService = telegramSenderService;
    }
    
   @Scheduled(cron = "0 00 18 * * *")
    public void notifyUsersAboutNewPromocodes() {
        Map<String, Set<String>> allSubscriptions = subscribeService.getAllSubscriptions();

        for (Map.Entry<String, Set<String>> entry : allSubscriptions.entrySet()) {
            String chatId = entry.getKey();
            Set<String> games = entry.getValue();

            for (String game : games) {
                String capitalizedGameName = subscribeService.capitalizeGameName(game);
                String promoMessage = promocodesService.fetchPromocodes(game, capitalizedGameName);

                if (!promoMessage.contains("No promocodes found")) {
                    telegramSenderService.sendText(chatId, promoMessage);
                }
            }
        }
    }

    public void sendNotifyCommand(String chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText("🔔 Manage daily promocode notifications:");

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = List.of(
            List.of(
                InlineKeyboardButton.builder()
                    .text("✅ Subscribe")
                    .callbackData("subscribe_notifications")
                    .build(),
                InlineKeyboardButton.builder()
                    .text("❌ Unsubscribe")
                    .callbackData("unsubscribe_notifications")
                    .build()
            )
        );

        markup.setKeyboard(rows);
        message.setReplyMarkup(markup);
        telegramSenderService.sendMessage(message);
    }

    public void addNotificationSubscriber(String chatId) {
        subscribeService.addNotificationSubscriber(chatId);
    }

    public void removeNotificationSubscriber(String chatId) {
        subscribeService.removeNotificationSubscriber(chatId);
    }

}
