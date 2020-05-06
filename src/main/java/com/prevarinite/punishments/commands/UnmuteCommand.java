package com.prevarinite.punishments.commands;

import com.google.common.collect.ImmutableMap;
import com.prevarinite.punishments.Punishments;
import com.prevarinite.punishments.orm.PunishHistory;
import com.prevarinite.punishments.orm.PunishPlayer;
import com.prevarinite.punishments.orm.Punishment;
import com.prevarinite.punishments.util.PunishmentUtil;
import org.apache.commons.text.StringSubstitutor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@SuppressWarnings("OptionalGetWithoutIsPresent")
public class UnmuteCommand implements CommandExecutor {

    private final Punishments plugin;

    public UnmuteCommand(Punishments plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length != 1) return false;
        CompletableFuture<@Nullable PunishPlayer> player = plugin.getDatabase().getPlayerByName(args[0]);
        UUID uuid;
        if (sender instanceof Player) uuid = ((Player) sender).getUniqueId();
        else uuid = UUID.fromString("00000000-0000-0000-0000-000000000000");
        CompletableFuture<@Nullable PunishPlayer> dbSender = plugin.getDatabase().getPlayerByUUID(uuid);
        player.thenAccept(punishPlayer -> {
            if (punishPlayer == null) {
                sender.sendMessage("This player has not joined before!");
                return;
            }
            if (!PunishmentUtil.hasActivePunishment(punishPlayer, Punishment.PunishmentType.MUTE)) {
                sender.sendMessage("Player is not muted!");
                return;
            }
            Punishment punishment = PunishmentUtil.getActivePunishment(punishPlayer, Punishment.PunishmentType.MUTE);
            punishment.setExpired(true);
            plugin.getDatabase().savePunishment(punishment);
            String message = StringSubstitutor.replace(plugin.getMessagesConfig().getString("commands.mute.unmute_broadcast"),
                    ImmutableMap.<String, Object>builder()
                            .put("executor", Objects.requireNonNull(dbSender.join()).getHistory().stream().max(Comparator.comparing(PunishHistory::getTime)).get().getName())
                            .put("player", punishPlayer.getHistory().stream().max(Comparator.comparing(PunishHistory::getTime)).get().getName())
                            .build()
            );
            Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', message));

        });
        return true;
    }
}
