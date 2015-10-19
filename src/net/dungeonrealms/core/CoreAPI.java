package net.dungeonrealms.core;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.dungeonrealms.core.reply.BanReply;
import net.dungeonrealms.mastery.AsyncUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Created by Nick on 10/17/2015.
 *
 * @apiNote This is the new DatabaseAPI. (IN THE WORKS).
 */
public class CoreAPI {

    static CoreAPI instance = null;

    public static CoreAPI getInstance() {
        if (instance == null) {
            instance = new CoreAPI();
        }
        return instance;
    }

    public void test() {
    }

    /**
     * Will return the tax for mailing.
     *
     * @param callback The integer
     * @since 1.0
     */
    public void findMailTax(Callback<Integer> callback) {
        AsyncUtils.pool.submit(() -> {
            try {
                URL url = new URL("https://cherryio.com/api/l.php?type=tax");
                BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
                String line = in.readLine();
                int rbt = Integer.valueOf(line);
                callback.callback(null, rbt);
            } catch (IOException e) {
                callback.callback(e, 1);
                e.printStackTrace();
            }
        });
    }

    /**
     * Checks if a player has a ban.
     *
     * @param playerName The player you're checking.
     * @param callback   The callback of BanResult.
     * @since 1.0
     */
    public void findBan(String playerName, Callback<BanReply> callback) {
        Future<?> result = AsyncUtils.pool.submit(() -> {
            try {
                URL url = new URL("https://cherryio.com/api/l.php?type=ban&player=" + playerName);
                BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
                return in.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return "0,0";
        });

        try {
            String line = (String) result.get();
            callback.callback(null, new BanReply(BanReply.BanResult.getByInt(Integer.valueOf(line.split(",")[0])), BanReply.BanReason.getByInt(Integer.valueOf(line.split(",")[1]))));
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }



    /**
     * Will get a players guild Async.
     *
     * @param playerName
     * @param callback
     * @since 1.0
     */
    public void findGuild(String playerName, Callback<String> callback) {
        AsyncUtils.pool.submit(() -> {
            try {
                URL url = new URL("https://cherryio.com/api/l.php?getGuild=" + playerName);
                HttpURLConnection request = (HttpURLConnection) url.openConnection();
                request.connect();

                JsonParser jp = new JsonParser();
                JsonElement root = jp.parse(new InputStreamReader((InputStream) request.getContent()));
                JsonObject obj = root.getAsJsonObject();
                callback.callback(null, obj.get("info.guild").getAsString());
            } catch (IOException e) {
                callback.callback(e, "");
                e.printStackTrace();
            }
        });
    }

}
