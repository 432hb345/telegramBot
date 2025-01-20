package handler;

import org.telegram.telegrambots.meta.api.methods.send.SendAudio;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.api.objects.InputFile;

import java.io.File;

public class KaczmarskiBot extends TelegramLongPollingBot {

    private final MessageHandler messageHandler = new MessageHandler();
    private static final String BOT_TOKEN = "613401148:AAGznQ8Q4tOb4ee9OgadOOFU2XohvfGgu7c";
    private static final String BOT_USERNAME = "handler.KaczmarskiBot";
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
            messageHandler.handleTextMessage(this, update);
        } else if (update.getMessage().hasPhoto()) {
            messageHandler.handlePhotoMessage(this, update);
        }
    }

    public void sendMusic(Long chatId, String filePath) {
        SendAudio sendAudio = new SendAudio();
        sendAudio.setChatId(chatId);
        sendAudio.setAudio(new InputFile(new File(filePath))); // Local file
        sendAudio.setCaption("Here's your music!");

        try {
            execute(sendAudio);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void sendTextMessage(long chatId, String text) throws TelegramApiException {
        execute(createMessage(chatId, text));
    }

    public void sendPhoto(long chatId, String photoSource, String caption) throws TelegramApiException {
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

    void handleError(TelegramApiException e) {
        e.printStackTrace();
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

                