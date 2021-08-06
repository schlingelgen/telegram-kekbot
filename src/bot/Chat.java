package bot;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.util.ArrayList;
import java.util.HashMap;

public class Chat {

    final Long CHAT_ID;
    HashMap<String, ArrayList<String>> kek_Liste;

    //private boolean nextNewKek;
    private int user_id;
    private String username;

    private boolean kekAnfrage;
    private boolean newKek;
    private String reason;


    public Chat(Long CHAT_ID){

        this.CHAT_ID = CHAT_ID;
        this.kek_Liste = new HashMap<>();

        this.user_id = 0;
        this.username = "";
        this.reason = "";
    }

    public HashMap<String, ArrayList<String>> getKek_Liste() {
        return kek_Liste;
    }

    public void setKek_Liste(HashMap<String, ArrayList<String>> kek_Liste) {
        this.kek_Liste = kek_Liste;
    }

    public Long getChatID(){
        return this.CHAT_ID;
    }


    public boolean isNewKek() {
        return newKek;
    }

    public void setNewKek(boolean newKek) {
        this.newKek = newKek;
    }


    public boolean isKekAnfrage() {
        return kekAnfrage;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public void setReason(String reason){
        this.reason = reason;
    }

    public String getReason(){
        return reason;
    }

    public String getReasons(String kekName){
        StringBuilder builder = new StringBuilder();

        builder.append(kekName + "'s Kekstrich-Gründe: \n");
        for (String grund : this.kek_Liste.get(kekName)){
            String a = grund;
            builder.append(" - " + a + "\n");
        }

        return builder.toString();
    }

    public String getAllReasons(){
        StringBuilder builder = new StringBuilder();

        builder.append("Die Gründe, weshalb ihr wahrhaftige Keks seid: \n \n");

        for (String kekName : this.kek_Liste.keySet()){
            builder.append(this.getReasons(kekName) + "\n");
        }

        return builder.toString();
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public boolean getKekAnfrage() {
        return kekAnfrage;
    }

    public void setKekAnfrage(boolean kekAnfrage) {
        this.kekAnfrage = kekAnfrage;
    }

    @Override
    public String toString() {

        StringBuilder kekliste = new StringBuilder();

        if (this.kek_Liste.size() <= 0){
            kekliste.append("Keine Keks vorhanden!");
        } else {
            for (String kekName : this.kek_Liste.keySet()){
                kekliste.append(kekName + ": " + this.kek_Liste.get(kekName).size() + "\n");
            }
        }

        return kekliste.toString();
    }
}
