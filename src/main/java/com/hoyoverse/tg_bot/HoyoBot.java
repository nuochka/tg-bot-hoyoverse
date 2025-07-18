package com.hoyoverse.tg_bot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
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
                    handleCodesCommand(update.getMessage().getChatId());
                    return;

                case "/news":
                    replyText = "📰 Here are the latest news:\n" + hoyoverseService.fetchLatestNews();
                    break;

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
                String reply;
                reply = hoyoverseService.fetchPromocodes(game);

                sendText(chatId, reply);
            }
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

   public void handleCodesCommand(Long chatId) {
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

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
