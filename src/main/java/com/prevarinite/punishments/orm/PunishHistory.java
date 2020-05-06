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

@NoArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@DatabaseTable(tableName = "history")
public class PunishHistory {

    @DatabaseField(columnName = "id", generatedId = true)
    @Nullable Integer id;

    @DatabaseField(columnName = "player_id", canBeNull = false, foreign = true)
    @NotNull PunishPlayer player;

    @DatabaseField(columnName = "name", canBeNull = false, width = 16)
    @NotNull String name;

    @DatabaseField(columnName = "ip", canBeNull = false, width = 38)
    @NotNull String ip;

    @DatabaseField(columnName = "time", canBeNull = false)
    @NotNull Date time;

    public PunishHistory(@NotNull PunishPlayer player, @NotNull String name, @NotNull String ip) {
        this.player = player;
        this.name = name;
        this.ip = ip;
        this.time = new Date();
    }
}
