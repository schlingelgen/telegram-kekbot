package bot;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendAnimation;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.net.URL;
import java.util.*;

import static java.util.Collections.max;


/*

Commands

kek - Fordere neuen Kekstrich an
kekliste - Zeige aktuelle Keks
alleGruende - Zeige alle Kekstrich-Gründe
auswertung - Zeige Kek des Monats
 */

public class KekBot extends TelegramLongPollingBot {

    HashMap<Long, Chat> chats = new HashMap<>();
    ArrayList<String> commands = new ArrayList<>();

    public KekBot(String[] commands){
        for (String command : commands){
            this.commands.add(command);
        }
    }

    @Override
    public void onUpdateReceived(Update update) {

        final long CHAT_ID = update.hasMessage() ? update.getMessage().getChatId() : update.getCallbackQuery().getMessage().getChatId();
        System.out.println("[Update with Chat ID \"" + CHAT_ID + "\" received]");

        // Neues Chat Objekt, falls ID noch nicht vorhanden
        if (!this.chats.containsKey(CHAT_ID)){
            System.out.println("[New Chat ID registered: " + CHAT_ID + "]");
            this.chats.put(CHAT_ID, new Chat(CHAT_ID));
        }

        // Aktuelles Chat Objekt merken
        Chat chat = this.chats.get(CHAT_ID);

        // Falls update Text hat, hier speichern um Commands auszulesen - command nur wenn prefix "/" vor "@" wird gecuttet
        String input = "";
        String command = "[none]";

        if (update.hasMessage() && update.getMessage().hasText()){
            input = update.getMessage().getText();

            if (input.startsWith("/")){
                command = (input.split(" ", 2)[0]).split("@")[0];
            }

            System.out.println("[Potential Command: " + command + "]");
        }

        // Test all commands
        if (this.commands.contains(command)){
            System.out.println("[Command found: " + command + "]");
            switch (command){
                case "/kek":    kekCommand(update, chat);
                                break;
                case "/keks":   sendMessage(chat.toString(), CHAT_ID);
                                break;
                case "/grund":  grundCommand(chat);
                                break;
                case "/ende":   endeCommand(chat);
            }

        } else if (update.hasCallbackQuery()){

            String call_data = update.getCallbackQuery().getData();

            // Wenn Kekanfrage offen, und User ID passt
            if (chat.getUser_id() == update.getCallbackQuery().getFrom().getId() && chat.isKekAnfrage()) {

                System.out.println("[Neue Kek-Anfrage: Iniatior ID: " + chat.getUser_id() + " Klicker ID: " + update.getCallbackQuery().getFrom().getId());
                kekCallBack(chat, call_data);

            }

        } else if (chat.isNewKek() && update.hasMessage() && update.getMessage().getFrom().getId() == chat.getUser_id()){
            // NEUEN KEK FESTLEGEN
            addKek(update, chat);

        }

        if (update.hasMessage() && update.getMessage().hasText()){

            randomMessage(update, chat);

        }

    }


    public void kekCommand(Update update, Chat chat){
        /**
         * Falls bereits Kekanfrage aktiv ist, keine neue Starten
         */
        if (chat.getKekAnfrage()){

            sendMessage("Warte bis die vorherige Kekstrich-Anfrage bearbeitet wurde!", chat.getChatID());

            return;
        } else {
            chat.setUser_id(update.getMessage().getFrom().getId());
        }


        /**
         * Ohne Kek-Grund kann keine Anfrage gestartet werden
         */
        try {

            chat.setReason(update.getMessage().getText().split(" ", 2)[1]);
            System.out.println("Grund gespeichert: " + chat.getReason());
        } catch (IndexOutOfBoundsException e){
            e.printStackTrace();

            sendMessage("Du musst einen Kekstrich-Grund angeben!", chat.getChatID());
            return;
        }


        /**
         * Neue Nachricht - Chat ID setzen
         */
        SendMessage message = new SendMessage().setChatId(chat.getChatID());
        message.setText("Wer soll den Kekstrich bekommen? (" + chat.getReason() + ")");

        /**
         * Set Options Hilfsmethode fügt klickbare Kek-Optionen hinzu
         */
        message.setReplyMarkup(this.setOptions(this.chats.get(chat.getChatID()).getKek_Liste()));
        chat.setKekAnfrage(true);

        sendMessage(message);
    }

    public void grundCommand(Chat chat){

        String grund;

        if (chat.getKek_Liste().size() <= 0){
            grund = "Ich habe noch keine Gründe dafür, aber ihr seid dennoch alle Riesenkeks.";
        } else {
            grund = chat.getAllReasons();
        }

        sendMessage(grund, chat.getChatID());
    }

    public void randomMessage(Update update, Chat chat){
        int rand = (int) Math.floor(Math.random()*70);
        String text = "";
        String name = update.getMessage().getFrom().getFirstName();

        if (rand > 50 && (name.contains("oli") || name.contains("Oli"))){
            text = name + " stinkt nach Lyoner";
        }

        switch (rand){
            case 0: text = update.getMessage().getFrom().getFirstName() + " hat jetzt Redeverbot!";
                break;
            case 1: text = update.getMessage().getFrom().getFirstName() + " stinkt nach Einhornschweiß!";
                break;
            case 2: text = update.getMessage().getFrom().getFirstName() + " hat gestern laut gepuspst!";
                break;
        }

        if (text.length() > 0){
            sendMessage(text, chat.getChatID());
        }
    }

    public void endeCommand(Chat chat){

        if (chat.getKek_Liste().size() == 0){

            sendMessage("Gibt noch keinen einzigen Kek :O", chat.getChatID());

        } else {
            SendAnimation gif = new SendAnimation();
            gif.setAnimation(new java.io.File("/Users/ningelsohn/IdeaProjects/KekBot_Telegram/src/kek.gif")).setChatId(chat.getChatID());
            this.showWinner(chat);

            try {
                execute(gif);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }

    public void kekCallBack(Chat chat, String call_data){

        if (call_data.equals("Neuer Kek")) {

            sendMessage("Benenne den neuen Kek!", chat.getChatID());
            chat.setKekAnfrage(false);
            chat.setNewKek(true);

        } else {
            if (chat.getKek_Liste().containsKey(call_data)) {

                chat.getKek_Liste().get(call_data).add(chat.getReason());
                chat.setReason("");
                chat.setKekAnfrage(false);

                // Chat Nachricht
                sendMessage(call_data + " hat einen Kekstrich bekommen!", chat.getChatID());
            }
        }
    }

    public void addKek(Update update, Chat chat){

        String newKek = update.getMessage().getText();

        chat.getKek_Liste().put(newKek, new ArrayList<>());
        chat.getKek_Liste().get(newKek).add(chat.getReason());
        chat.setReason("");
        chat.setNewKek(false);

        // Chat Nachricht
        sendMessage(newKek + " hat einen Kekstrich bekommen!", chat.getChatID());

    }

    @Override
    public String getBotUsername() {
        try {
            return "@" + this.getMe().getUserName();
        } catch (TelegramApiException e) {
            e.printStackTrace();
        } return null;
    }

    @Override
    public String getBotToken() {
        return "808082548:AAFuj8t7J_tIYi4QovhcFZO-sIQKm2Nfqk0";
    }

    public void sendMessage(SendMessage message){
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(String text, Long CHAT_ID){
        try {
            execute(new SendMessage().setChatId(CHAT_ID).setText(text));
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    // Generiert KeyboardMarkup welches die Kekauswahl realisiert
    public InlineKeyboardMarkup setOptions(HashMap<String, ArrayList<String>> kek_Liste){

        // KeyboardMarkup welches später der Nachricht angefügt wurde
        InlineKeyboardMarkup kekButtons = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        int quant = kek_Liste.size();
        String[] kekNamen = new String[quant+1];
        int counter = 0;

        // Hilfsarray füllen
        for (String kekName : kek_Liste.keySet()){
            kekNamen[counter] = kekName;
            counter++;
        }
        kekNamen[counter] = "Neuer Kek";

        counter = 0;

        // Generiert 3-elementige Kek-Spalten zur Klick-Auswahl
        while (counter <= kekNamen.length){
            List<InlineKeyboardButton> rowInline = new ArrayList<>();
            for (int i = 0; i < 3; i++){
                if (counter < kekNamen.length){
                    InlineKeyboardButton kek = new InlineKeyboardButton().setText(kekNamen[counter]).setCallbackData(kekNamen[counter]);
                    rowInline.add(kek);
                    counter++;
                } else {
                    counter++;
                }
            }
            rowsInline.add(rowInline);
        }

        kekButtons.setKeyboard(rowsInline);
        return kekButtons;
    }

    // Ermittelt Gewinner innerhalb des Chats
    public void showWinner(Chat chat){

        // ArrayList which stores one or if needed more winner/s
        // ArrayList which stores quantity of kekstrich's fo find the kek of the month
        ArrayList<String> winner = new ArrayList<>();
        ArrayList<Integer> striche = new ArrayList<>();

        // Add the number of kekstrich reasons for every single kek to ArrayList striche
        for (ArrayList<String> value : chat.getKek_Liste().values()){
            striche.add(value.size());
        }

        // Get the max value out of it
        int max = Collections.max(striche);

        // Add all keks who have the max-amount of kekstrich's
        for (String kek : chat.getKek_Liste().keySet()){
            if (chat.getKek_Liste().get(kek).size() == max){
                winner.add(kek);
            }
        }

        // Build string to get Congrat-String for all winnners
        StringBuilder builder = new StringBuilder();
        builder.append("\uD83C\uDF89 ");
        String prefix = "";

        if (winner.size() == 1){
            builder.append(winner.get(0) + " hat mit " + max);
        } else {

            int quant = winner.size();

            for (int i = 0; i < quant; i++){
                builder.append(((i+1 < quant) ? prefix : " und ") + winner.get(i));
                prefix = ", ";
            }
            builder.append(" haben mit " + max);
        }

        builder.append(max > 1 ? " Kekstrichen gewonnen! \uD83C\uDF89" : " Kekstrich gewonnen! \uD83C\uDF89");

        sendMessage(builder.toString(), chat.getChatID());
    }

}
