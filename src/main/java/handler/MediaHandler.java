package handler;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendAudio;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;

public class MediaHandler {

    public void sendMusic(TelegramLongPollingBot bot, Long chatId, String filePath) {
        SendAudio sendAudio = new SendAudio();
        sendAudio.setChatId(chatId);
        sendAudio.setAudio(new InputFile(new File(filePath)));
        sendAudio.setCaption("Here's your music!");

        try {
            bot.execute(sendAudio);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
