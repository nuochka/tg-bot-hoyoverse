package com.hoyoverse.tg_bot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import com.hoyoverse.tg_bot.service.HoyoverseService;

@Component
public class HoyoBot extends TelegramLongPollingBot {

    private final HoyoverseService hoyoverseService;

    @Autowired
    public HoyoBot(HoyoverseService hoyoverseService) {
        this.hoyoverseService = hoyoverseService;
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
                            👋 Hello! I am a bot for Genshin Impact and ZZZ.
                            📋 Available commands:
                            /promocodes - 🎁 fresh promo codes
                            /news - 📰 latest news
                            """;
                    break;

                case "/promocodes":
                    replyText = "🎁 Here are the freshest promo codes:\n" + hoyoverseService.fetchPromocodes();
                    break;

                case "/news":
                    replyText = "📰 Here are the latest news:\n" + hoyoverseService.fetchLatestNews();
                    break;

                default:
                    replyText = "🤔 Unknown command. Try /start for the list.";
            }

            sendText(chatId, replyText);
        }
    }

    private void sendText(String chatId, String text) {
        SendMessage message = new SendMessage(chatId, text);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
