package utils;

import handler.KaczmarskiBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

public class BotInitializer {
    public static void main(String[] args) {
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(new KaczmarskiBot());
            System.out.println("KaczmarskiBot successfully started!");
        } catch (TelegramApiException e) {
            System.err.println("Failed to start handler.KaczmarskiBot:");
            e.printStackTrace();
        }
    }
}
