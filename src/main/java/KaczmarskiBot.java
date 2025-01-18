import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.api.objects.InputFile;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class KaczmarskiBot extends TelegramLongPollingBot {
    private static final String BOT_TOKEN = "613401148:AAGznQ8Q4tOb4ee9OgadOOFU2XohvfGgu7c";
    private static final String BOT_USERNAME = "KaczmarskiBot";
    private static final String DEFAULT_PHOTO_URL = "https://cdn.omlet.co.uk/images/originals/fischer's-lovebird.jpg";
    private static final String TELEGRAM_PHOTO_ID = "AgADAgAD6qcxGwnPsUgOp7-MvnQ8GecvSw0ABGvTl7ObQNPNX7UEAAEC";

    public KaczmarskiBot() {
        super(BOT_TOKEN);
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (!update.hasMessage()) {
            return;
        }

        if (update.getMessage().hasText()) {
            handleTextMessage(update);
        } else if (update.getMessage().hasPhoto()) {
            handlePhotoMessage(update);
        }
    }

    private void handleTextMessage(Update update) {
        String messageText = update.getMessage().getText();
        long chatId = update.getMessage().getChatId();

        try {
            switch (messageText) {
                case "/start":
                    sendTextMessage(chatId, messageText);
                    break;
                case "/pic":
                case "/pic2":
                    sendPhoto(chatId, DEFAULT_PHOTO_URL, "Photo");
                    break;
                case "/papieze":
                    handlePapiezeCommand(chatId);
                    break;
                case "/pontyfikatEnd":
                    hideKeyboard(chatId);
                    break;
                case "Row 1 Button 1":
                case "Row 1 Button 2":
                case "Row 1 Button 3":
                case "Row 2 Button 1":
                case "Row 2 Button 2":
                case "Row 2 Button 3":
                    sendPhoto(chatId, TELEGRAM_PHOTO_ID, "Photo");
                    break;
                default:
                    sendTextMessage(chatId, "Unknown command");
            }
        } catch (TelegramApiException e) {
            handleError(e);
        }
    }

    private void handlePapiezeCommand(long chatId) throws TelegramApiException {
        SendMessage message = createMessage(chatId, "Oto papieze");
        message.setReplyMarkup(createKeyboardMarkup());
        execute(message);
    }

    private ReplyKeyboardMarkup createKeyboardMarkup() {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();

        keyboard.add(createKeyboardRow("Row 1 Button 1", "Row 1 Button 2", "Row 1 Button 3"));
        keyboard.add(createKeyboardRow("Row 2 Button 1", "Row 2 Button 2", "Row 2 Button 3"));

        keyboardMarkup.setKeyboard(keyboard);
        return keyboardMarkup;
    }

    private KeyboardRow createKeyboardRow(String... buttonTexts) {
        KeyboardRow row = new KeyboardRow();
        for (String text : buttonTexts) {
            row.add(text);
        }
        return row;
    }

    private void hideKeyboard(long chatId) throws TelegramApiException {
        SendMessage message = createMessage(chatId, "Keyboard hidden");
        message.setReplyMarkup(new ReplyKeyboardRemove());
        execute(message);
    }

    private void handlePhotoMessage(Update update) {
        long chatId = update.getMessage().getChatId();
        List<PhotoSize> photos = update.getMessage().getPhoto();
        
        PhotoSize largestPhoto = photos.stream()
                .max(Comparator.comparing(PhotoSize::getFileSize))
                .orElse(null);
                
        if (largestPhoto == null) return;

        String caption = String.format("file_id: %s\nwidth: %d\nheight: %d",
                largestPhoto.getFileId(),
                largestPhoto.getWidth(),
                largestPhoto.getHeight());

        try {
            sendPhoto(chatId, largestPhoto.getFileId(), caption);
        } catch (TelegramApiException e) {
            handleError(e);
        }
    }

    private void sendTextMessage(long chatId, String text) throws TelegramApiException {
        execute(createMessage(chatId, text));
    }

    private void sendPhoto(long chatId, String photoSource, String caption) throws TelegramApiException {
        SendPhoto photo = new SendPhoto();
        photo.setChatId(chatId);
        photo.setPhoto(new InputFile(photoSource));
        photo.setCaption(caption);
        execute(photo);
    }

    private SendMessage createMessage(long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);
        return message;
    }

    private void handleError(TelegramApiException e) {
        e.printStackTrace();
        // In a production environment, you might want to log this error properly
        // and possibly notify administrators
    }

    @Override
    public String getBotUsername() {
        return BOT_USERNAME;
    }

    @Override
    public String getBotToken() {
        return BOT_TOKEN;
    }
}

                