package com.hoyoverse.tg_bot;

import com.hoyoverse.tg_bot.service.SubscribeService;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import com.hoyoverse.tg_bot.service.NewsService;
import com.hoyoverse.tg_bot.service.NotificationService;
import com.hoyoverse.tg_bot.service.PromocodesService;

@Component
public class HoyoBot extends TelegramLongPollingBot {

    private final SubscribeService subscribeService;

    private final PromocodesService promocodesService;
    private final NewsService newsService;
    private final NotificationService notificationService;

    @Autowired
    public HoyoBot(PromocodesService promocodesService, NewsService newsService, SubscribeService subscribeService, NotificationService notificationService) {
        this.promocodesService = promocodesService;
        this.newsService = newsService;
        this.subscribeService = subscribeService;
        this.notificationService = notificationService;
    }

    @Override
    public String getBotUsername() {
        return TgBotApplication.TELEGRAM_BOT_NAME;
    }

    @Override
    public String getBotToken() {
        return TgBotApplication.TELEGRAM_BOT_TOKEN;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String chatId = update.getMessage().getChatId().toString();
            String userMessage = update.getMessage().getText();

            String replyText;

            switch (userMessage) {
                case "/start":
                    replyText = """
                            👋 Hello! I am a bot for Genshin Impact and other HoYoverse games.
                            📋 Available commands:
                            /promocodes - 🎁 fresh promoсodes
                            /news - 📰 latest news
                            /subscribe - 🔔 subscribe to promocodes
                            /unsubscribe - 🚫 unsubscribe from promocodes
                            /my_subscriptions - 📦 view your subscriptions
                            /notify - 🔔 daily promocode notifications
                            """;
                    break;

                case "/promocodes":
                    SendMessage promoMessage = promocodesService.buildCodesCommandMessage(update.getMessage().getChatId());
                    try {
                        execute(promoMessage);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                    return;

                case "/news":
                    replyText = "📰 Here are the latest news:\n" + newsService.fetchLatestNews();
                    break;

                case "/subscribe":
                    subscribeService.handleSubscribeCommand(chatId);
                    return;

                case "/unsubscribe":
                    subscribeService.handleUnsubscribeCommand(chatId);
                    return;

                case "/my_subscriptions":
                Set<String> subs = subscribeService.getSubscriptions(chatId);
                if (subs.isEmpty()) {
                    replyText = "📭 You are not subscribed to any games.";
                } else {
                    String formattedSubs = subs.stream()
                        .map(this::capitalizeGameName)
                        .reduce((a, b) -> a + ", " + b)
                        .orElse("none");

                    replyText = "📦 You are subscribed to: " + formattedSubs;
                }
                break;

                case "/notify":
                    notificationService.sendNotifyCommand(chatId);
                    return;

                default:
                    replyText = "🤔 Unknown command. Try /start for the list.";
            }

            sendText(chatId, replyText);
        }

        if(update.hasCallbackQuery()){
            String data = update.getCallbackQuery().getData();
            String chatId = update.getCallbackQuery().getMessage().getChatId().toString();

            if(data.startsWith("codes_")) {
                String game = data.substring("codes_".length());
                String capitalizedName = capitalizeGameName(game);
                String reply = promocodesService.fetchPromocodes(game, capitalizedName);
                sendText(chatId, reply);
            }

            if (data.startsWith("sub_")) {
                String game = data.substring("sub_".length());
                subscribeService.subscribe(chatId, game);
                sendText(chatId, "✅ You have subscribed to promocodes for " + capitalizeGameName(game) + "!");
            }

            if (data.equals("subscribe_notifications")) {
                notificationService.addNotificationSubscriber(chatId);
                sendText(chatId, "✅ You have subscribed to daily promocode notifications!");
            }

            if (data.equals("unsubscribe_notifications")) {
                notificationService.removeNotificationSubscriber(chatId);
                sendText(chatId, "❌ You have unsubscribed from daily promocode notifications.");
            }
        }
    }

    public void sendText(String chatId, String text) {
        SendMessage message = new SendMessage(chatId, text);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

     public String capitalizeGameName(String game) {
        return switch (game) {
            case "genshin" -> "Genshin Impact";
            case "starrail" -> "Honkai: Star Rail";
            case "zzz" -> "Zenless Zone Zero";
            case "honkai3rd" -> "Honkai Impact 3rd";
            default -> game;
        };
    }
}
