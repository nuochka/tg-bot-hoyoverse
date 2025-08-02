package com.hoyoverse.tg_bot;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@SpringBootApplication
@EnableScheduling
public class TgBotApplication {

    private static final Dotenv dotenv = Dotenv.configure()
                .ignoreIfMissing()
                .load();

    public static final String TELEGRAM_BOT_NAME = dotenv.get("TELEGRAM_BOT_NAME");
    public static final String TELEGRAM_BOT_TOKEN = dotenv.get("TELEGRAM_BOT_TOKEN");

    public static void main(String[] args) {
        SpringApplication.run(TgBotApplication.class, args);
    }

    @Bean
    public TelegramBotsApi telegramBotsApi(HoyoBot hoyoBot) throws TelegramApiException {
        TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
        botsApi.registerBot(hoyoBot);
        System.out.println("Bot autorizathion was successful");
        return botsApi;
    }
}
