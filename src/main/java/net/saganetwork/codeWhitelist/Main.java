package net.saganetwork.codeWhitelist;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class Main extends JavaPlugin implements Listener {

    private final Map<String, Boolean> frozenPlayers = new HashMap<>();
    private String serverCode;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        setupConfigWithCode();
        getLogger().info("codeWhitelist açıldı!");

        Bukkit.getPluginManager().registerEvents(this, this);
    }

    @EventHandler
    public void onServerLoad(org.bukkit.event.server.ServerLoadEvent event) {
        Bukkit.getScheduler().runTaskLater(this, () -> {
            getLogger().info("Sunucu için doğrulama kodu: " + serverCode);
        }, 20L * 3);
    }

    @Override
    public void onDisable() {
        getLogger().info("codeWhitelist kapatıldı!");
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String playerIp = player.getAddress().getAddress().getHostAddress();

        FileConfiguration config = getConfig();
        boolean ipCheckEnabled = config.getBoolean("settings.ip-check", true);
        boolean isVerified = config.getStringList("players").contains(player.getName() + ":" + playerIp);

        if (ipCheckEnabled && isVerified) {
            player.sendMessage(ChatColor.GREEN + "IP adresiniz eşleşti, kod girmeden oynayabilirsiniz.");
            return;
        }

        freezePlayer(player);
        player.sendMessage(ChatColor.RED + "Komut: /kod <kod>");
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (isFrozen(player)) {
            event.setTo(event.getFrom());
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (isFrozen(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player player && isFrozen(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player && isFrozen(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player player && isFrozen(player)) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "Kod girmeden birisine vuramazsınız! (Doğrulama kodunu almak için console'da 'Sunucu için doğrulama kodu:' mesajını arayabilirsiniz.)");
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (label.equalsIgnoreCase("kod")) {
            if (args.length != 1) {
                sender.sendMessage(ChatColor.RED + "Kullanım: /kod <kod>");
                return true;
            }

            if (!(sender instanceof Player player)) {
                sender.sendMessage(ChatColor.RED + "Bu komutu yalnızca oyuncular kullanabilir.");
                return true;
            }

            if (args[0].equals(serverCode)) {
                unfreezePlayer(player);

                String playerIp = player.getAddress().getAddress().getHostAddress();
                FileConfiguration config = getConfig();
                List<String> playerList = config.getStringList("players");
                playerList.add(player.getName() + ":" + playerIp);
                config.set("players", playerList);
                saveConfig();

                sender.sendMessage(ChatColor.GREEN + "Doğrulama başarılı! Hoş geldiniz.");
            } else {
                sender.sendMessage(ChatColor.RED + "Geçersiz kod.");
            }
            return true;
        }
        return false;
    }

    private void freezePlayer(Player player) {
        frozenPlayers.put(player.getUniqueId().toString(), true);
        player.setWalkSpeed(0f);
        player.sendMessage(ChatColor.RED + "Doğrulama kodu girene kadar donduruldunuz! (Doğrulama kodunu almak için console'da 'Sunucu için doğrulama kodu:' mesajını arayabilirsiniz.)");
    }

    private void unfreezePlayer(Player player) {
        frozenPlayers.remove(player.getUniqueId().toString());
        player.setWalkSpeed(0.2f);
        player.sendMessage(ChatColor.GREEN + "Artık özgürsünüz!");
    }

    private boolean isFrozen(Player player) {
        return frozenPlayers.getOrDefault(player.getUniqueId().toString(), false);
    }

    private void setupConfigWithCode() {
        FileConfiguration config = getConfig();

        if (!config.contains("server-code") || config.getString("server-code").isEmpty()) {
            serverCode = generateRandomCode();
            config.set("server-code", serverCode);
            saveConfig();
        } else {
            serverCode = config.getString("server-code");
        }

        if (!config.contains("players")) {
            config.set("players", new ArrayList<>());
        }
        if (!config.contains("settings.ip-check")) {
            config.set("settings.ip-check", true);
        }

        saveConfig();
    }

    private String generateRandomCode() {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder code = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 6; i++) {
            code.append(characters.charAt(random.nextInt(characters.length())));
        }
        return code.toString();
    }
}