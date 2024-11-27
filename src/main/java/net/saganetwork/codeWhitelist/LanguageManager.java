package net.saganetwork.codeWhitelist;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class LanguageManager {

    private final Plugin plugin;
    private final Map<String, String> messages = new HashMap<>();

    public LanguageManager(Plugin plugin) {
        this.plugin = plugin;
        loadLanguage();
    }

    public void loadLanguage() {
        String language = plugin.getConfig().getString("language", "en"); // Varsayılan dil "en"
        File langFile = new File(plugin.getDataFolder(), "translate/" + language + ".yml");

        if (!langFile.exists()) {
            plugin.getLogger().warning("Dil dosyası bulunamadı: " + language + ". Varsayılan dil kullanılacak.");
            langFile = new File(plugin.getDataFolder(), "translate/en.yml");
        }

        FileConfiguration langConfig = YamlConfiguration.loadConfiguration(langFile);

        if (langConfig.contains("messages")) {
            for (String key : langConfig.getConfigurationSection("messages").getKeys(false)) {
                messages.put(key, langConfig.getString("messages." + key, "Mesaj bulunamadı: " + key));
            }
            plugin.getLogger().info("Dil dosyası yüklendi: " + language);
        } else {
            plugin.getLogger().warning("Dil dosyasında 'messages' bölümü bulunamadı: " + language);
        }
    }

    public String getMessage(String key) {
        String rawMessage = messages.getOrDefault(key, "Mesaj bulunamadı: " + key);
        return ChatColor.translateAlternateColorCodes('&', rawMessage); // Renk kodlarını çevir
    }

    public String getMessage(String key, Map<String, String> placeholders) {
        String rawMessage = messages.getOrDefault(key, "Mesaj bulunamadı: " + key);

        if (placeholders != null) {
            for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                rawMessage = rawMessage.replace("{" + entry.getKey() + "}", entry.getValue());
            }
        }

        return ChatColor.translateAlternateColorCodes('&', rawMessage); // Renk kodlarını çevir
    }

    public TextComponent createClickableMessage(String text, String url) {
        TextComponent message = new TextComponent(ChatColor.translateAlternateColorCodes('&', text));
        message.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url));
        return message;
    }
}
