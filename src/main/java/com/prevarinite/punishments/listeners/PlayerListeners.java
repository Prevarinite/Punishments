package com.prevarinite.punishments.listeners;

import com.google.common.collect.ImmutableMap;
import com.prevarinite.punishments.Punishments;
import com.prevarinite.punishments.orm.PunishHistory;
import com.prevarinite.punishments.orm.PunishPlayer;
import com.prevarinite.punishments.orm.Punishment;
import com.prevarinite.punishments.util.PunishmentUtil;
import com.prevarinite.punishments.util.TimeUtil;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import org.apache.commons.text.StringSubstitutor;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

import java.util.Comparator;

@SuppressWarnings("OptionalGetWithoutIsPresent")
public class PlayerListeners implements Listener {

    private final Punishments plugin;

    public PlayerListeners(Punishments plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void handleBannedPlayerPreLogin(AsyncPlayerPreLoginEvent event) {
        PunishPlayer player = plugin.getDatabase().getPlayerByUUID(event.getUniqueId()).join();
        if (player == null) return;
        if (!PunishmentUtil.hasActivePunishment(player, Punishment.PunishmentType.BAN)) return;
        Punishment punishment = PunishmentUtil.getActivePunishment(player, Punishment.PunishmentType.BAN);
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
                                        .put("duration", punishment.getUntil() != null ? TimeUtil.getTimeUntil(punishment.getUntil().getTime()) : "Permanent")
                                        .put("reason", punishment.getReason())
                                        .put("executor", punishment.getExecutor().getHistory().stream().max(Comparator.comparing(PunishHistory::getTime)).get().getName())
                                        .put("executor_uuid", punishment.getExecutor().getUuid())
                                        .build()))
                        .put("date", punishment.getTime())
                        .put("duration", punishment.getUntil() != null ? TimeUtil.convertTime(punishment.getUntil().getTime() - System.currentTimeMillis()) : "Permanent")
                        .put("until", punishment.getUntil() != null ? punishment.getUntil() : "")
                        .put("appeal_message", plugin.getMessagesConfig().getString("banned_appeal"))
                        .build());
        ComponentBuilder messageBuilder = new ComponentBuilder(ChatColor.translateAlternateColorCodes('&', formattedMessage));
        event.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_BANNED);
        event.setKickMessage(TextComponent.toLegacyText(messageBuilder.create()));
    }

    @EventHandler
    public void handlePlayerPreLogin(AsyncPlayerPreLoginEvent event) {
        if (plugin.getDatabase().getPlayerByUUID(event.getUniqueId()).join() == null) {
            PunishPlayer player = new PunishPlayer(event.getUniqueId());
            PunishHistory historyPlayer = new PunishHistory(player, event.getName(), event.getAddress().getHostAddress());
            plugin.getDatabase().savePlayer(historyPlayer, player);
        }
    }

    @EventHandler
    public void handleMutedPlayerChat(AsyncPlayerChatEvent event) {
        PunishPlayer player = plugin.getDatabase().getPlayerByUUID(event.getPlayer().getUniqueId()).join();
        if (player == null) return;
        if (!PunishmentUtil.hasActivePunishment(player, Punishment.PunishmentType.MUTE)) return;
        Punishment punishment = PunishmentUtil.getActivePunishment(player, Punishment.PunishmentType.MUTE);
        boolean permanent = punishment.getUntil() == null;
        String formattedMessage = StringSubstitutor.replace(plugin.getMessagesConfig().getString(permanent ? "commands.mute.muted_permanent" : "commands.mute.muted_temporary"),
                ImmutableMap.<String, Object>builder()
                        .put("executor", punishment.getExecutor().getHistory().stream().max(Comparator.comparing(PunishHistory::getTime)).get().getName())
                        .put("player", punishment.getPlayer().getHistory().stream().max(Comparator.comparing(PunishHistory::getTime)).get().getName())
                        .put("duration", punishment.getUntil() != null ? TimeUtil.convertTime(punishment.getUntil().getTime() - System.currentTimeMillis()) : "Permanent")
                        .put("reason", punishment.getReason())
                        .build()
        );
        ComponentBuilder messageBuilder = new ComponentBuilder(ChatColor.translateAlternateColorCodes('&', formattedMessage));
        event.setCancelled(true);
        event.getPlayer().sendMessage(messageBuilder.toString());
    }
}
