package com.prevarinite.punishments.commands;

import com.google.common.collect.ImmutableMap;
import com.prevarinite.punishments.Punishments;
import com.prevarinite.punishments.orm.PunishHistory;
import com.prevarinite.punishments.orm.PunishPlayer;
import com.prevarinite.punishments.orm.Punishment;
import com.prevarinite.punishments.util.PunishmentUtil;
import com.prevarinite.punishments.util.TimeUtil;
import org.apache.commons.text.StringSubstitutor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@SuppressWarnings("OptionalGetWithoutIsPresent")
public class BanCommand implements CommandExecutor {

    private final Punishments plugin;

    public BanCommand(Punishments plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, @NotNull String[] args) {
        if (args.length < 1) return false;
        CompletableFuture<@Nullable PunishPlayer> player = plugin.getDatabase().getPlayerByName(args[0]);
        UUID uuid;
        if (sender instanceof Player) uuid = ((Player) sender).getUniqueId();
        else uuid = UUID.fromString("00000000-0000-0000-0000-000000000000");
        CompletableFuture<@Nullable PunishPlayer> dbSender = plugin.getDatabase().getPlayerByUUID(uuid);
        player.thenCombine(dbSender, (target, executor) -> {
            if (target != null) {
                boolean failed = PunishmentUtil.hasActivePunishment(target, Punishment.PunishmentType.BAN);
                if (failed) {
                    sender.sendMessage("Player is already banned!");
                    return null;
                }
                if (args.length > 1) {
                    if (args.length > 2) {
                        if (TimeUtil.isValidDuration(args[1])) {
                            StringBuilder sb = new StringBuilder();
                            for (int i = 2; i < args.length; i++) {
                                sb.append(args[i]).append(" ");
                            }
                            String message = StringSubstitutor.replace(plugin.getMessagesConfig().getString("commands.ban.ban_broadcast"),
                                    ImmutableMap.<String, Object>builder()
                                            .put("executor", executor.getHistory().stream().max(Comparator.comparing(PunishHistory::getTime)).get().getName())
                                            .put("player", target.getHistory().stream().max(Comparator.comparing(PunishHistory::getTime)).get().getName())
                                            .put("duration", TimeUtil.convertTime(TimeUtil.convertDuration(args[1])))
                                            .put("reason", sb.toString())
                                            .build()
                            );
                            Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', message));
                            return new Punishment(target, executor, new Date(System.currentTimeMillis() + TimeUtil.convertDuration(args[1])), sb.toString(), Punishment.PunishmentType.BAN);
                        }
                        StringBuilder sb = new StringBuilder();
                        for (int i = 1; i < args.length; i++) {
                            sb.append(args[i]).append(" ");
                        }
                        String message = StringSubstitutor.replace(plugin.getMessagesConfig().getString("commands.ban.ban_broadcast"),
                                ImmutableMap.<String, Object>builder()
                                        .put("executor", executor.getHistory().stream().max(Comparator.comparing(PunishHistory::getTime)).get().getName())
                                        .put("player", target.getHistory().stream().max(Comparator.comparing(PunishHistory::getTime)).get().getName())
                                        .put("reason", sb.toString())
                                        .build()
                        );
                        Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', message));
                        return new Punishment(target, executor, null, sb.toString(), Punishment.PunishmentType.BAN);
                    }
                    if (TimeUtil.isValidDuration(args[1])) {
                        String message = StringSubstitutor.replace(plugin.getMessagesConfig().getString("commands.ban.ban_broadcast"),
                                ImmutableMap.<String, Object>builder()
                                        .put("executor", executor.getHistory().stream().max(Comparator.comparing(PunishHistory::getTime)).get().getName())
                                        .put("player", target.getHistory().stream().max(Comparator.comparing(PunishHistory::getTime)).get().getName())
                                        .put("duration", TimeUtil.convertTime(TimeUtil.convertDuration(args[1])))
                                        .put("reason", this.plugin.getMessagesConfig().getString("ban_reason_default"))
                                        .build()
                        );
                        Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', message));
                        return new Punishment(target, executor, new Date(System.currentTimeMillis() + TimeUtil.convertDuration(args[1])), this.plugin.getMessagesConfig().getString("ban_reason_default"), Punishment.PunishmentType.BAN);
                    }
                    StringBuilder sb = new StringBuilder();
                    for (int i = 1; i < args.length; i++) {
                        sb.append(args[i]);
                    }
                    String message = StringSubstitutor.replace(plugin.getMessagesConfig().getString("commands.ban.ban_broadcast"),
                            ImmutableMap.<String, Object>builder()
                                    .put("executor", executor.getHistory().stream().max(Comparator.comparing(PunishHistory::getTime)).get().getName())
                                    .put("player", target.getHistory().stream().max(Comparator.comparing(PunishHistory::getTime)).get().getName())
                                    .put("reason", this.plugin.getMessagesConfig().getString("ban_reason_default"))
                                    .build()
                    );
                    Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', message));
                    return new Punishment(target, executor, null, sb.toString(), Punishment.PunishmentType.BAN);
                }
                String message = StringSubstitutor.replace(plugin.getMessagesConfig().getString("commands.ban.ban_broadcast"),
                        ImmutableMap.<String, Object>builder()
                                .put("executor", executor.getHistory().stream().max(Comparator.comparing(PunishHistory::getTime)).get().getName())
                                .put("player", target.getHistory().stream().max(Comparator.comparing(PunishHistory::getTime)).get().getName())
                                .put("reason", this.plugin.getMessagesConfig().getString("ban_reason_default"))
                                .build()
                );
                Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', message));
                return new Punishment(target, executor, null, this.plugin.getMessagesConfig().getString("ban_reason_default"), Punishment.PunishmentType.BAN);
            }
            sender.sendMessage(ChatColor.RED + "This player has never joined before!");
            return null;
        }).thenAccept(punishment -> {
            if (punishment != null) {
                plugin.getDatabase().savePunishment(punishment);
                Player player1 = Bukkit.getPlayer(punishment.getPlayer().getUuid());
                if (player1 != null) {
                    boolean permanent = punishment.getUntil() == null;
                    String formattedMessage = StringSubstitutor.replace(
                            this.plugin.getMessagesConfig().getString(permanent ? "banned_permanent" : "banned_temporary"),
                            ImmutableMap.<String, Object>builder()
                                    .put("header", StringSubstitutor.replace(
                                            this.plugin.getMessagesConfig().getString("banned_header"),
                                            ImmutableMap.<String, Object>builder()
                                                    .put("type", punishment.getType())
                                                    .put("date", punishment.getTime())
                                                    .put("until", punishment.getUntil() != null ? punishment.getUntil() : "")
                                                    .put("duration", punishment.getUntil() != null ? TimeUtil.convertTime(punishment.getUntil().getTime() - punishment.getTime().getTime()) : "Permanent")
                                                    .put("reason", punishment.getReason())
                                                    .put("executor", punishment.getExecutor().getHistory().stream().max(Comparator.comparing(PunishHistory::getTime)).get().getName())
                                                    .put("executor_uuid", punishment.getExecutor().getUuid())
                                                    .build()))
                                    .put("date", punishment.getTime())
                                    .put("duration", punishment.getUntil() != null ? TimeUtil.convertTime(punishment.getUntil().getTime() - punishment.getTime().getTime()) : "Permanent")
                                    .put("until", punishment.getUntil() != null ? punishment.getUntil() : "")
                                    .put("appeal_message", plugin.getMessagesConfig().getString("banned_appeal"))
                                    .build()
                    );
                    Bukkit.getScheduler().runTask(plugin, () -> player1.kickPlayer(ChatColor.translateAlternateColorCodes('&', formattedMessage)));
                }
            }
        });
        return true;
    }
}
