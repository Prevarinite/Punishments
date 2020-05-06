package com.prevarinite.punishments.util;

import com.prevarinite.punishments.orm.PunishPlayer;
import com.prevarinite.punishments.orm.Punishment;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Stream;

/**
 * Used for getting specific punishments from the database
 *
 * @author BomBardyGamer
 */
@UtilityClass
public class PunishmentUtil {

    /**
     * Checks if the specified {@code PunishPlayer} has an active {@code Punishment}
     *
     * @param player the player who is being checked for an active punishment
     * @param type the {@code PunishmentType} of punishment that the player is being checked for
     * @return if the database returns any results (if the player has a punishment)
     */
    public boolean hasActivePunishment(@NotNull PunishPlayer player, @NotNull Punishment.PunishmentType type) {
        return getAllActivePunishments(player)
                .anyMatch(punishment -> punishment.getType() == type);
    }

    /**
     * Fetches an active {@code Punishment} from the database matching the specified arguments
     *
     * @param player the {@code PunishPlayer} who has been punished
     * @param type the {@code PunishmentType} of the punishment being searched for
     * @return the {@code Punishment} that was found, or else null
     */
    public Punishment getActivePunishment(@NotNull PunishPlayer player, @NotNull Punishment.PunishmentType type) {
        return getAllActivePunishments(player)
                .filter(punishment -> punishment.getType() == type)
                .findFirst()
                .orElse(null);
    }

    /**
     * Fetches all punishments of the {@code PunishPlayer} from the database
     *
     * @param player the player who's punishments are being fetched
     * @return a {@code Stream} of punishments found in the database, or else null
     */
    private Stream<Punishment> getAllPunishments(@NotNull PunishPlayer player) {
        return player.getPunishments().stream();
    }

    /**
     * Fetches all active punishments of the {@code PunishPlayer} from the database
     *
     * @param player the player who's active punishments are being fetched
     * @return a filtered {@code Stream} of active punishments found in the database,
     *         or else null
     */
    private Stream<Punishment> getAllActivePunishments(@NotNull PunishPlayer player) {
        return getAllPunishments(player)
                .filter(punishment -> !punishment.isExpired());
    }
}
