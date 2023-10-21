package net.zorby.prc.database;

import net.minecraft.client.MinecraftClient;
import net.minecraft.network.ClientConnection;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.math.Vec3i;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Objects;

public class Database {

    private static final String DATABASE_PATH = "prcdatabase";

    private final HashMap<String, WorldEntry> entries = new HashMap<>();

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void load() {
        this.entries.clear();

        File database = databasePath().toFile();

        database.mkdir();

        File[] files = database.listFiles();
        assert files != null;

        for (File file : files) {
            String entryName = file.getName();
            WorldEntry entry = new WorldEntry();

            try {
                entry.load(file);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }

            this.entries.put(entryName, entry);
            System.out.println(entryName);
            System.out.println(entry);
        }
    }

    public void save() {
        String database = databasePath().toString();

        for (String entryName : this.entries.keySet()) {
            WorldEntry entry = this.entries.get(entryName);

            try {
                entry.save(Paths.get(database, entryName).toFile());
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void addEntry(Vec3i pos, WorldEntry.Style style) {
        getCurrentEntry().addEntry(pos, style);

        this.save();
    }

    public boolean hasEntry(int x, int z) {
        return getCurrentEntry().hasEntry(x, z);
    }

    private static Path databasePath() {
        String runPath = MinecraftClient.getInstance().runDirectory.getPath();
        return Paths.get(runPath, DATABASE_PATH);
    }

    private static String getCurrentServerId() {
        MinecraftClient minecraftClient = MinecraftClient.getInstance();

        if (minecraftClient.getNetworkHandler() != null) {
            ClientConnection connection = minecraftClient.getNetworkHandler().getConnection();

            if (connection.isLocal()) {
                return Objects.requireNonNull(minecraftClient.getServer())
                        .getSavePath(WorldSavePath.ROOT).getParent().getFileName().toString()
                        .replaceAll("[/\\\\:]", "_");
            } else {
                return connection.getAddress().toString().replaceAll("[/\\\\:]", "_");
            }
        }

        return null;
    }

    private WorldEntry getCurrentEntry() {
        WorldEntry entry = this.entries.get(getCurrentServerId());
        if (entry == null) {
            entry = new WorldEntry();
            this.entries.put(getCurrentServerId(), entry);
        }

        return entry;
    }

    public Long getCurrentSeed() {
        return getCurrentEntry().getSeed();
    }

    public void setCurrentSeed(Long seed) {
        getCurrentEntry().setSeed(seed);

        this.save();
    }
}