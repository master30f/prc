package net.zorby.prc.database;

import net.minecraft.util.math.Vec3i;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class WorldEntry {

    private Long seed = null;
    private final List<Entry> entries = new ArrayList<>();

    public void load(File file) throws FileNotFoundException {
        final Scanner reader = new Scanner(new FileInputStream(file));

        this.entries.clear();

        this.seed = reader.nextLong();

        while (reader.hasNext()) {
            Entry entry = new Entry();

            entry.load(reader);

            this.entries.add(entry);
        }

        reader.close();
    }

    public void save(File file) throws FileNotFoundException {
        final PrintWriter writer = new PrintWriter(new FileOutputStream(file));

        writer.println(this.seed);

        for (Entry entry : this.entries) {
            entry.save(writer);
        }

        writer.close();
    }

    public void addEntry(Vec3i pos, Style style) {
        entries.add(new Entry(pos, style));
    }

    public boolean hasEntry(int x, int z) {
        return entries.stream().anyMatch(entry -> entry.x == x && entry.z == z);
    }

    public Long getSeed() {
        return this.seed;
    }

    public void setSeed(Long seed) {
        this.seed = seed;
    }

    private static class Entry {

        int x, y, z;
        long timestamp;
        Style style;

        private Entry() { }

        private Entry(Vec3i pos, Style style) {
            this.x = pos.getX();
            this.y = pos.getY();
            this.z = pos.getZ();
            this.timestamp = System.currentTimeMillis() / 1000L;
            this.style = style;
        }

        private void load(Scanner reader) {
            this.x         = reader.nextInt();
            this.y         = reader.nextInt();
            this.z         = reader.nextInt();
            this.timestamp = reader.nextLong();
            this.style     = Style.values()[reader.nextInt()];
        }

        private void save(PrintWriter writer) {
            writer.print(this.x);
            writer.print(' ');
            writer.print(this.y);
            writer.print(' ');
            writer.print(this.z);
            writer.print(' ');
            writer.print(this.timestamp);
            writer.print(' ');
            writer.println(this.style.ordinal());
        }

    }

    public enum Style {
        Grabbed,
        Found,
        Skipped
    }

}
