package net.saganetwork.codeWhitelist;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.*;

public class Main extends JavaPlugin implements Listener {
    private final Map<UUID, ItemStack[]> storedInventories = new HashMap<>();
    private final Map<String, Boolean> frozenPlayers = new HashMap<>();
    private String serverCode;
    private VersionChecker versionChecker;
    private LanguageManager languageManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        saveDefaultResources();
        setupConfigWithCode();

        languageManager = new LanguageManager(this);

        Bukkit.getPluginManager().registerEvents(this, this);

        String versionCheckUrl = "https://api.mcsunucun.com/CodeWhitelist/check.php";
        versionChecker = new VersionChecker(this, versionCheckUrl);
        Bukkit.getScheduler().runTaskAsynchronously(this, versionChecker::checkVersion);

        Bukkit.getScheduler().runTaskTimer(this, () -> {
            getLogger().info(languageManager.getMessage("verification_code_message")
                    .replace("{code}", serverCode));
        }, 20L * 60 * 15, 20L * 60 * 15);

        getLogger().info(languageManager.getMessage("plugin_enabled"));
    }

    @EventHandler
    public void onServerLoad(org.bukkit.event.server.ServerLoadEvent event) {
        Bukkit.getScheduler().runTaskLater(this, () -> {
            getLogger().info(languageManager.getMessage("server_code_console").replace("{code}", serverCode));
        }, 20L * 3);
    }

    @Override
    public void onDisable() {
        getLogger().info(languageManager.getMessage("plugin_disabled"));
    }

    @EventHandler
    public void onPreLogin(PlayerLoginEvent event) {
        String playerHostname = event.getHostname();
        String playerName = event.getPlayer().getName();

        FileConfiguration config = getConfig();
        boolean allowedLoginIPEnabled = config.getBoolean("settings.allowed-login-ip", false);
        String allowedLoginHostname = config.getString("settings.login-ip", "play.xxx.com");

        if (allowedLoginIPEnabled && !playerHostname.startsWith(allowedLoginHostname)) {
            event.disallow(PlayerLoginEvent.Result.KICK_OTHER,
                    languageManager.getMessage("hostname_not_allowed"));
            getLogger().info(languageManager.getMessage("hostname_rejected")
                    .replace("{hostname}", playerHostname)
                    .replace("{player}", playerName));
            return;
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String playerIp = player.getAddress().getAddress().getHostAddress();

        FileConfiguration config = getConfig();
        boolean ipCheckEnabled = config.getBoolean("settings.ip-check", true);
        boolean isVerified = config.getStringList("players").contains(player.getName() + ":" + playerIp);


        if (ipCheckEnabled && isVerified) {
            player.sendMessage(languageManager.getMessage("ip_verified"));
            getLogger().info(languageManager.getMessage("server_ip_verified")
                    .replace("{playerip}", playerIp));
            return;
        }

        freezePlayer(player);
        storeAndClearInventory(player);

        player.sendTitle(
                languageManager.getMessage("title_required_code"),
                languageManager.getMessage("subtitle_required_code"),
                10, 100, 10
        );

        player.sendMessage(languageManager.getMessage("frozen_message"));
        getLogger().info(languageManager.getMessage("server_not_verified")
                .replace("{playerip}", playerIp));

        player.spigot().sendMessage(languageManager.createClickableMessage(
                languageManager.getMessage("verification_guide_text"),
                "https://minecraftdocs.mcsunucun.com/panel-ek-ozellikleri/nasil-kod-alirim"
        ));
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
            player.sendMessage(languageManager.getMessage("cannot_attack"));
        }
    }

    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String command = event.getMessage().toLowerCase();

        if (!isFrozen(player)) {
            return;
        }

        if (command.startsWith("/kod ") || command.startsWith("/code ")) {
            return;
        }

        event.setCancelled(true);
        player.sendMessage(languageManager.getMessage("command_blocked"));
    }


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (label.equalsIgnoreCase("kod") || label.equalsIgnoreCase("code")) {
            if (args.length < 1) {
                sender.sendMessage(languageManager.getMessage("usage_command"));
                return true;
            }

            if (args[0].equalsIgnoreCase("al")) {
                if (sender instanceof Player player) {
                    if (!player.isOp()) {
                        player.sendMessage(languageManager.getMessage("no_permission"));
                        return true;
                    }

                    if (isFrozen(player)) {
                        player.sendMessage(languageManager.getMessage("command_blocked"));
                        return true;
                    }

                    player.sendMessage(languageManager.getMessage("server_code_console").replace("{code}", serverCode));
                } else {
                    sender.sendMessage(languageManager.getMessage("server_code_console").replace("{code}", serverCode));
                }
                return true;
            }

            if (!(sender instanceof Player player)) {
                sender.sendMessage(languageManager.getMessage("command_only_players"));
                return true;
            }

            if (args[0].equals(serverCode)) {
                unfreezePlayer(player);
                restoreInventory(player);

                String playerIp = player.getAddress().getAddress().getHostAddress();
                FileConfiguration config = getConfig();
                List<String> playerList = config.getStringList("players");
                playerList.add(player.getName() + ":" + playerIp);
                config.set("players", playerList);
                saveConfig();

                sender.sendMessage(languageManager.getMessage("verification_success"));
            } else {
                sender.sendMessage(languageManager.getMessage("invalid_code"));
            }
            return true;
        }

        return false;
    }


    private void freezePlayer(Player player) {
        frozenPlayers.put(player.getUniqueId().toString(), true);
        player.setWalkSpeed(0f);
    }

    private void unfreezePlayer(Player player) {
        frozenPlayers.remove(player.getUniqueId().toString());
        player.setWalkSpeed(0.2f);
    }

    private boolean isFrozen(Player player) {
        return frozenPlayers.getOrDefault(player.getUniqueId().toString(), false);
    }

    private void storeAndClearInventory(Player player) {
        UUID playerId = player.getUniqueId();

        storedInventories.put(playerId, player.getInventory().getContents());

        player.getInventory().clear();
    }

    private void restoreInventory(Player player) {
        UUID playerId = player.getUniqueId();

        if (storedInventories.containsKey(playerId)) {
            player.getInventory().setContents(storedInventories.get(playerId));
            storedInventories.remove(playerId);
        }
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

    private void saveDefaultResources() {
        saveResource("translate/en.yml", false);
        saveResource("translate/tr.yml", false);

        getLogger().info("Varsayılan dil dosyaları oluşturuldu.");
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