package com.prevarinite.punishments.database;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.db.DatabaseTypeUtils;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.prevarinite.punishments.Punishments;
import com.prevarinite.punishments.orm.PunishHistory;
import com.prevarinite.punishments.orm.PunishPlayer;
import com.prevarinite.punishments.orm.Punishment;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Used for initialising database tables
 *
 * @author BomBardyGamer
 * @since 1.0
 */
public class DatabaseWrapper {

    private final Dao<PunishPlayer, Integer> playerDao;
    private final Dao<Punishment, Integer> punishmentDao;
    private final Dao<PunishHistory, Integer> historyDao;

    public DatabaseWrapper(Punishments plugin) throws SQLException {
        String databaseUrl = "jdbc:" + plugin.getConfig().getString("sql.driver").toLowerCase() + "://" + plugin.getConfig().getString("sql.address") + "/" + plugin.getConfig().getString("sql.database");
        ConnectionSource connectionSource = new JdbcConnectionSource(databaseUrl, plugin.getConfig().getString("sql.username"), plugin.getConfig().getString("sql.password"), DatabaseTypeUtils.createDatabaseType(databaseUrl));

        // creates the players, punishments and history tables if they are not present (usually created when ran for the first time)
        TableUtils.createTableIfNotExists(connectionSource, PunishPlayer.class);
        TableUtils.createTableIfNotExists(connectionSource, Punishment.class);
        TableUtils.createTableIfNotExists(connectionSource, PunishHistory.class);

        // creates data access objects for each table in the database, for easier data access and manipulation
        this.playerDao = DaoManager.createDao(connectionSource, PunishPlayer.class); // DAO for players table
        this.punishmentDao = DaoManager.createDao(connectionSource, Punishment.class); // DAO for punishments table
        this.historyDao = DaoManager.createDao(connectionSource, PunishHistory.class); // DAO for history table

        // adds the console to the database, used for creating a PunishPlayer object when the console is the executor
        if (getPlayerByUUID(UUID.fromString("00000000-0000-0000-0000-000000000000")).join() == null) {
            PunishPlayer player = new PunishPlayer(UUID.fromString("00000000-0000-0000-0000-000000000000"));
            this.playerDao.createIfNotExists(player);
            this.historyDao.createIfNotExists(new PunishHistory(player, "Console", "0.0.0.0"));
        }
    }

    /**
     * Gets a database player ({@code PunishPlayer}) from the database using their UUID,
     * as a {@code CompletableFuture} object.
     *
     * @param uuid the uuid of the database player
     * @return a database player as a {@code CompletableFuture} object if a player with the uuid
     *         specified was found, or else null
     */
    public CompletableFuture<@Nullable PunishPlayer> getPlayerByUUID(@NotNull UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return playerDao.queryBuilder().where().eq("uuid", uuid).queryForFirst();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;
        });
    }

    /**
     * Gets a database player ({@code PunishPlayer}) from the database using their uuid,
     * as a {@code CompletableFuture} object.
     *
     * @param name the username of the database player
     * @return a database player as a {@code CompletableFuture} object if a player with the
     *         username specified was found, or else null
     */
    public CompletableFuture<@Nullable PunishPlayer> getPlayerByName(@NotNull String name) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                PunishHistory player = historyDao.queryBuilder().orderBy("time", false).where().eq("name", name).queryForFirst();
                if (player != null) {
                    return playerDao.queryForSameId(player.getPlayer());
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;
        });
    }

    /**
     * Saves a player to the database
     *
     * @param historyPlayer the player's history as a {@code PunishHistory} object
     * @param player the player as a {@code PunishPlayer} object
     */
    public void savePlayer(@NotNull PunishHistory historyPlayer, @NotNull PunishPlayer player) {
        try {
            playerDao.createOrUpdate(player);
            historyDao.createOrUpdate(historyPlayer);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Saves a punishment to the database
     *
     * @param punishment the punishment to save to the database
     */
    public void savePunishment(@NotNull Punishment punishment) {
        try {
            punishmentDao.createOrUpdate(punishment);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
