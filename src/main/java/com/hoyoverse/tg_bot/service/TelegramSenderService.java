package com.hoyoverse.tg_bot.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import com.hoyoverse.tg_bot.HoyoBot;

@Service
public class TelegramSenderService {

    private final HoyoBot bot;

    @Autowired
    public TelegramSenderService(@Lazy HoyoBot bot) {
        this.bot = bot;
    }

    public void sendText(String chatId, String text) {
        SendMessage message = new SendMessage(chatId, text);
        try {
            bot.execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(SendMessage message) {
        try {
            bot.execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
