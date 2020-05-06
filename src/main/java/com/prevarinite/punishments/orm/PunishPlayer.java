package com.prevarinite.punishments.orm;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

@NoArgsConstructor
@Getter
@Setter
@DatabaseTable(tableName = "players")
public class PunishPlayer {

    @DatabaseField(columnName = "id", generatedId = true)
    @Nullable Integer id;

    @DatabaseField(columnName = "uuid", canBeNull = false)
    @NotNull UUID uuid;

    @ForeignCollectionField(columnName = "id", foreignFieldName = "player")
    @Setter(AccessLevel.PRIVATE)
    ForeignCollection<PunishHistory> history;

    @ForeignCollectionField(columnName = "id", foreignFieldName = "player")
    @Setter(AccessLevel.PRIVATE)
    ForeignCollection<Punishment> punishments;

    public PunishPlayer(@NotNull UUID uuid) {
        this.uuid = uuid;
    }
}
