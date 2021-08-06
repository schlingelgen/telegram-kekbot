package bot;

import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

public class Main {


    public static void main(String args[]){

        String[] commands = "/kek /keks /grund /ende".split(" ");
        ApiContextInitializer.init();
        TelegramBotsApi tba = new TelegramBotsApi();
        KekBot kek = new KekBot(commands);

        try {
            tba.registerBot(kek);
        } catch (TelegramApiRequestException e){
            e.printStackTrace();
        }



    }
}
