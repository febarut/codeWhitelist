package net.saganetwork.codeWhitelist;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class ExternalIpFetcher {
    public static String getExternalIp() {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL("https://api.mcsunucun.com/CodeWhitelist/ipv4.php").openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String ip = reader.readLine();
            reader.close();

            return ip;
        } catch (Exception e) {
            return "Bilinmiyor (Hata: " + e.getMessage() + ")";
        }
    }
}
