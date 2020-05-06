package com.prevarinite.punishments.orm;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Date;

@SuppressWarnings("NullableProblems")
@NoArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@DatabaseTable(tableName = "punishments")
public class Punishment {

    @DatabaseField(columnName = "id", generatedId = true)
    @Nullable Integer id;

    @DatabaseField(columnName = "player_id", canBeNull = false, foreign = true)
    @NotNull PunishPlayer player;

    @DatabaseField(columnName = "executor_id", canBeNull = false, foreign = true, foreignAutoRefresh = true)
    @NotNull PunishPlayer executor;

    @DatabaseField(columnName = "time", canBeNull = false)
    @NotNull Date time;

    @DatabaseField(columnName = "until")
    @Nullable Date until;

    @DatabaseField(columnName = "reason", canBeNull = false)
    @NotNull String reason;

    @DatabaseField(columnName = "type", canBeNull = false, width = 8)
    @NotNull PunishmentType type;

    @DatabaseField(columnName = "expired", canBeNull = false)
    boolean expired;

    public Punishment(@NotNull PunishPlayer player, @NotNull PunishPlayer executor, @Nullable Date until, @NotNull String reason, PunishmentType type) {
        this.player = player;
        this.executor = executor;
        this.until = until;
        this.reason = reason;
        this.type = type;
        this.time = new Date();
    }

    public enum PunishmentType {
        BAN,
        KICK,
        MUTE
    }
}
