package net.zorby.prc;

import net.fabricmc.api.ClientModInitializer;
import net.zorby.prc.database.Database;
import net.zorby.prc.gui.Hud;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PRC implements ClientModInitializer {

    private static PRC INSTANCE;
    public static final Logger logger = LogManager.getLogger("prc");

    private Finder finder;
    private Hud hud;
    private Database database;

    @Override
    public void onInitializeClient() {
        PRC.INSTANCE = this;
        this.finder = new Finder();
        this.hud = new Hud();
        this.database = new Database();
        this.database.load();

        Keybindings.register();
    }

    public static PRC getInstance() {
        return INSTANCE;
    }

    public Finder getFinder() {
        return this.finder;
    }

    public Hud getHud() {
        return this.hud;
    }
    public Database getDatabase() {
        return this.database;
    }
}
