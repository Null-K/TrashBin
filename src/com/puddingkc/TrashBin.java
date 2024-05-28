package com.puddingkc;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class TrashBin extends JavaPlugin implements CommandExecutor, Listener, TabCompleter {

    private String trashBinTitle;
    private String noPermissionMessage;
    private String playerNotFoundMessage;
    private String usageMessage;
    private String onlyPlayersMessage;
    private String trashOpenedMessage;
    private String reloadMessage;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadConfigValues();
        Objects.requireNonNull(getCommand("trash")).setExecutor(this);
        Objects.requireNonNull(getCommand("trash")).setTabCompleter(this);
        getServer().getPluginManager().registerEvents(this, this);
    }

    private void loadConfigValues() {
        trashBinTitle = getConfig().getString("trash-bin-title", "&c垃圾桶").replace("&","§");
        noPermissionMessage = getConfig().getString("no-permission-message", "&c你没有使用该命令的权限").replace("&","§");
        playerNotFoundMessage = getConfig().getString("player-not-found-message", "&c玩家不在线或不存在").replace("&","§");
        usageMessage = getConfig().getString("usage-message", "&e正确指令: /trash [player|reload]").replace("&","§");
        onlyPlayersMessage = getConfig().getString("only-players-message", "&c该指令只能由玩家执行").replace("&","§");
        trashOpenedMessage = getConfig().getString("trash-opened-message", "&a成功为玩家 %player% 打开垃圾桶界面").replace("&","§");
        reloadMessage = getConfig().getString("reload-message","&a配置文件重载成功").replace("&","§");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            if (sender instanceof Player) {
                Player player = (Player) sender;

                if (!player.hasPermission("trashbin.use")) {
                    player.sendMessage(noPermissionMessage);
                    return false;
                }

                openTrashGui(player);
                return true;
            } else {
                sender.sendMessage(onlyPlayersMessage);
                return false;
            }

        } else if (args.length == 1) {

            if ("reload".equalsIgnoreCase(args[0])) {
                if (sender.hasPermission("trashbin.reload")) {
                    reloadConfig();
                    loadConfigValues();
                    sender.sendMessage(reloadMessage);
                    return true;
                } else {
                    sender.sendMessage(noPermissionMessage);
                    return false;
                }
            }

            if (sender.hasPermission("trashbin.use.other")) {
                Player target = Bukkit.getPlayer(args[0]);
                if (target != null && target.isOnline()) {
                    openTrashGui(target);
                    sender.sendMessage(trashOpenedMessage.replace("%player%", target.getName()));
                    return true;
                } else {
                    sender.sendMessage(playerNotFoundMessage);
                    return false;
                }
            } else {
                sender.sendMessage(noPermissionMessage);
                return false;
            }
        } else {
            sender.sendMessage(usageMessage);
            return false;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> completions = new ArrayList<>();
            if ("reload".startsWith(args[0].toLowerCase()) && sender.hasPermission("trashbin.reload")) {
                completions.add("reload");
            }
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.getName().toLowerCase().startsWith(args[0].toLowerCase())) {
                    completions.add(player.getName());
                }
            }
            return completions;
        }
        return Collections.emptyList();
    }

    private void openTrashGui(Player player) {
        Inventory trashGui = Bukkit.createInventory(null, 27, trashBinTitle);
        player.openInventory(trashGui);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getView().getTitle().equals(trashBinTitle)) {
            Inventory trashGui = event.getInventory();
            trashGui.clear();
        }
    }
}
