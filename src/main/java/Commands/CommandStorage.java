package Commands;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import handler.KaczmarskiBot;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class CommandStorage {
    private final Properties commands;

    public CommandStorage() {
        commands = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("commands.properties")) {
            if (input == null) {
                throw new IOException("Commands properties file not found");
            }
            commands.load(input);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to load commands properties file", e);
        }
    }

    public void executeCommand(KaczmarskiBot bot, String command, long chatId) throws TelegramApiException {
        String action = commands.getProperty(command);

        if (action != null) {
            String[] actions = action.split(";");

            for (String singleAction : actions) {
                String[] parts = singleAction.split(":", 2);
                String type = parts[0];
                String data = parts.length > 1 ? parts[1] : "";

                switch (type) {
                    case "TEXT" -> bot.sendTextMessage(chatId, data);
                    case "PHOTO" -> bot.sendPhoto(chatId, data, "Here's your photo!");
                    default -> bot.sendTextMessage(chatId, "Unknown command type");
                }
            }
        } else {
            bot.sendTextMessage(chatId, "Unknown command");
        }
    }
}
