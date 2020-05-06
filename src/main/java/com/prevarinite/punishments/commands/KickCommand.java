package com.prevarinite.punishments.commands;

import com.google.common.collect.ImmutableMap;
import com.prevarinite.punishments.Punishments;
import com.prevarinite.punishments.orm.PunishHistory;
import com.prevarinite.punishments.orm.PunishPlayer;
import com.prevarinite.punishments.orm.Punishment;
import org.apache.commons.text.StringSubstitutor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@SuppressWarnings("OptionalGetWithoutIsPresent")
public class KickCommand implements CommandExecutor {

    private final Punishments plugin;

    public KickCommand(Punishments plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1) return false;
        CompletableFuture<@Nullable PunishPlayer> player = plugin.getDatabase().getPlayerByName(args[0]);
        UUID uuid;
        if (sender instanceof Player) uuid = ((Player) sender).getUniqueId();
        else uuid = UUID.fromString("00000000-0000-0000-0000-000000000000");
        CompletableFuture<@Nullable PunishPlayer> dbSender = plugin.getDatabase().getPlayerByUUID(uuid);
        player.thenCombine(dbSender, (target, executor) -> {
            if (target != null) {
                boolean failed = Bukkit.getPlayer(args[0]) == null;
                if (failed) {
                    sender.sendMessage("Player is not online!");
                    return null;
                }
                if (args.length > 1) {
                    StringBuilder sb = new StringBuilder();
                    for (int i = 1; i < args.length; i++) {
                        sb.append(args[i]).append(" ");
                    }
                    String message = StringSubstitutor.replace(plugin.getMessagesConfig().getString("commands.kick.kick_broadcast"),
                            ImmutableMap.<String, Object>builder()
                                    .put("executor", executor.getHistory().stream().max(Comparator.comparing(PunishHistory::getTime)).get().getName())
                                    .put("player", target.getHistory().stream().max(Comparator.comparing(PunishHistory::getTime)).get().getName())
                                    .put("reason", sb.toString())
                                    .build()
                    );
                    Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', message));
                    return new Punishment(target, executor, null, sb.toString(), Punishment.PunishmentType.KICK);
                }
                String message = StringSubstitutor.replace(plugin.getMessagesConfig().getString("commands.kick.kick_broadcast"),
                        ImmutableMap.<String, Object>builder()
                                .put("executor", executor.getHistory().stream().max(Comparator.comparing(PunishHistory::getTime)).get().getName())
                                .put("player", target.getHistory().stream().max(Comparator.comparing(PunishHistory::getTime)).get().getName())
                                .put("reason", this.plugin.getMessagesConfig().getString("kick_reason_default"))
                                .build()
                );
                Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', message));
                return new Punishment(target, executor, null, this.plugin.getMessagesConfig().getString("kick_reason_default"), Punishment.PunishmentType.KICK);
            }
            sender.sendMessage("This player has never joined before!");
            return null;
        }).thenAccept(punishment -> {
            if (punishment != null) {
                plugin.getDatabase().savePunishment(punishment);
                Player player1 = Bukkit.getPlayer(punishment.getPlayer().getUuid());
                String message = StringSubstitutor.replace(plugin.getMessagesConfig().getString("commands.kick.kick_message"),
                        ImmutableMap.<String, Object>builder()
                                .put("executor", punishment.getExecutor().getHistory().stream().max(Comparator.comparing(PunishHistory::getTime)).get().getName())
                                .put("reason", punishment.getReason())
                                .build()
                );
                Bukkit.getScheduler().runTask(plugin, () -> player1.kickPlayer(ChatColor.translateAlternateColorCodes('&', message)));
            }
        });
        return true;
    }
}
