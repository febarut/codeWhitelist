package net.saganetwork.codeWhitelist;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class VersionChecker {

    private final Plugin plugin;
    private final String versionCheckUrl;

    public VersionChecker(Plugin plugin, String versionCheckUrl) {
        if (plugin == null) {
            throw new IllegalArgumentException("Plugin instance cannot be null.");
        }
        this.plugin = plugin;
        this.versionCheckUrl = versionCheckUrl;
    }

    public void checkVersion() {
        String currentVersion = plugin.getDescription().getVersion();
        if (currentVersion == null || currentVersion.isEmpty()) {
            plugin.getLogger().severe("Eklenti sürümü alınamadı. Lütfen plugin.yml dosyasını kontrol edin.");
            return;
        }

        String externalIp = ExternalIpFetcher.getExternalIp();
        if (externalIp.equals("0.0.0.0")) {
            plugin.getLogger().severe("Harici IP adresi alınamadı.");
            return;
        }

        int serverPort = Bukkit.getServer().getPort();
        String ipv4 = externalIp + ":" + serverPort;

        try {
            String queryUrl = versionCheckUrl + "?ipv4=" + ipv4 + "&current_version=" + currentVersion;

            HttpURLConnection connection = (HttpURLConnection) new URL(queryUrl).openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            int responseCode = connection.getResponseCode();
            if (responseCode != 200) {
                plugin.getLogger().warning("API yanıtı beklenmedik bir durumla karşılaştı: " + responseCode);
                return;
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            String responseText = response.toString();
            if (responseText.isEmpty()) {
                plugin.getLogger().severe("Boş yanıt alındı, sürüm kontrolü yapılamadı.");
                return;
            }

            Gson gson = new Gson();
            JsonObject jsonResponse = gson.fromJson(responseText, JsonObject.class);

            String latestVersion = jsonResponse.get("latest_version").getAsString();
            String updateUrl = jsonResponse.get("update_url").getAsString();

            if (currentVersion.equals(latestVersion)) {
                plugin.getLogger().info("CodeWhitelist'in en güncel sürümünü kullanıyorsunuz: " + currentVersion);
            } else {
                plugin.getLogger().warning("CodeWhitelist için yeni bir sürüm mevcut: " + latestVersion);
                plugin.getLogger().warning("Güncellemeyi buradan indir: " + updateUrl);
            }

        } catch (Exception e) {
            plugin.getLogger().severe("Sürüm kontrolü sırasında hata oluştu: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
