package me.delxhq.verifyPlugin;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class Main extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        System.out.println("Enabled!");

        this.getServer().getPluginManager().registerEvents(this, this);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        JsonObject checkPlayerObject = this.get("http://localhost:3000/checkPlayer/" + event.getPlayer().getName());
        JsonObject getCodeObject = this.get("http://localhost:3000/generateCode/" + event.getPlayer().getName());
        if (!checkPlayerObject.get("verified").getAsBoolean()) {
            if (getCodeObject.has("error")) {
                // TODO: Add an endpoint to fetch the code again
                // Or do I just put the code in the error?
                String error = getCodeObject.get("error").getAsString().equals("Player already has a code") ? "You already have a code." : "An unknown error has occurred.";
                event.getPlayer().kickPlayer(error);
                return;
            }
            int code = getCodeObject.get("verificationCode").getAsInt();
            event.getPlayer().kickPlayer("You must verify your §9Discord§r to play. \n\nUse the command §6!link§r in the §6#bot-spam§r channel containing code §c" + code);
        }
    }

    private JsonObject get(String endpoint) {
        String inline = "";
        try {
            URL url = new URL(endpoint);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.connect();
            int responseCode = conn.getResponseCode();

            if (responseCode != 200)
                throw new RuntimeException("HttpResponseCode: " + responseCode);
            else {
                Scanner sc = new Scanner(conn.getInputStream());
                while (sc.hasNext()) {
                    inline += sc.nextLine();
                }
                sc.close();
            }
            conn.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new JsonParser().parse(inline).getAsJsonObject();
    }
}
