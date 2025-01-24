package Commands;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.HashSet;
import java.util.Set;

import handler.KaczmarskiBot;
import handler.KaczmarskiGPTHandler;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class CommandStorage {
    private final Properties commands;
    private final Set<Long> activeChats;
    private final KaczmarskiGPTHandler gptHandler;

    public CommandStorage() {
        commands = new Properties();
        activeChats = new HashSet<>();

        String apiKey = loadApiKey();
        gptHandler = new KaczmarskiGPTHandler(apiKey);
        loadCommands();
    }

    private String loadApiKey() {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("keys.properties")) {
            if (input == null) {
                throw new IOException("Keys properties file not found");
            }
            Properties keys = new Properties();
            keys.load(input);
            return keys.getProperty("openai.api.key");
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to load API key from properties file", e);
        }
    }

    private void loadCommands() {
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

    public void executeCommand(KaczmarskiBot bot, String message, long chatId) throws TelegramApiException {
        System.out.println("Received message: " + message + " from chat: " + chatId); // Debug log

        if (activeChats.contains(chatId)) {
            if ("/end".equalsIgnoreCase(message.trim())) {
                activeChats.remove(chatId);
                gptHandler.endConversation(chatId);
                bot.sendTextMessage(chatId, "Koniec rozmowy. Do widzenia!");
                System.out.println("Chat ended for: " + chatId); // Debug log
                return;
            }

            try {
                String response = gptHandler.processMessage(chatId, message);
                bot.sendTextMessage(chatId, response);
                System.out.println("GPT Response: " + response); // Debug log
            } catch (Exception e) {
                e.printStackTrace();
                bot.sendTextMessage(chatId, "Przepraszam, wystąpił błąd w przetwarzaniu wiadomości.");
            }
            return;
        }

        if ("/bot".equalsIgnoreCase(message.trim())) {
            activeChats.add(chatId);
            bot.sendTextMessage(chatId, "Witaj! Jestem Jacek Kaczmarski. O czym chcesz porozmawiać? (Napisz /end aby zakończyć rozmowę)");
            System.out.println("Chat started for: " + chatId);
            return;
        }

        String action = commands.getProperty(message);
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
            bot.sendTextMessage(chatId, "Nieznana komenda. Użyj /help aby zobaczyć dostępne komendy lub /bot aby rozpocząć rozmowę.");
        }
    }
}
