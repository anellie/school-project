package xyz.angm.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.physics.box2d.Box2D;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.ObjectMap;
import com.kotcrab.vis.ui.VisUI;
import xyz.angm.game.network.Client;
import xyz.angm.game.network.NetworkInterface;
import xyz.angm.game.network.Server;
import xyz.angm.game.ui.screens.AssetLoadingScreen;
import xyz.angm.game.ui.screens.MapLoadingScreen;
import xyz.angm.game.ui.screens.MessageScreen;
import xyz.angm.game.world.blocks.BlockProperties;
import xyz.angm.game.world.blocks.Material;

/** The main class of the game. */
public class Defactory extends com.badlogic.gdx.Game {

    /** Static since loading texture more than once is pointless. Also prevents having to pass textures around everywhere. */
    @SuppressWarnings({"LibGDXStaticResource", "GDXJavaStaticResource"}) // Only applies to Android
    public static final AssetManager assets = new AssetManager();

    private NetworkInterface netIface;

    @Override
    public void create() {
        VisUI.load(); // VisUI is a framework for game GUIs
        Box2D.init(); // Box2D is a 2D physics engine
        registerAllAssets();
        createSkin();
        setScreen(new AssetLoadingScreen(this));
    }

    @Override
    public void dispose() {
        assets.dispose();
        if (netIface != null) disposeNetworkInterface();
    }

    /** Starts a game as the player. Will create a server for players playing as beasts to join. */
    public void startGame() {
        netIface = new Server(this);
        netIface.start();
        setScreen(new MapLoadingScreen(this, System.currentTimeMillis()));
    }

    /** Joins a server. Allows the user to play as a beast trying to destroy the base. */
    public void joinGame() {
        Client client = new Client();
        netIface = client;

        client.addListener(packet -> {
            if (packet instanceof Long)
                Gdx.app.postRunnable(() -> this.setScreen(new MapLoadingScreen(this, (Long) packet)));
        });
        boolean connected = client.start();
        if (!connected) setScreen(new MessageScreen(this, "failedToConnect"));
    }

    public NetworkInterface getNetworkInterface() {
        return netIface;
    }

    /** Only callable on the client.
     * @return The client if one exists.
     * @throws ClassCastException when called on server. */
    public Client getClient() {
        return (Client) netIface;
    }

    /** Is this a server?
     * @return If this game is a server/player. */
    public boolean isServer() {
        return netIface instanceof Server;
    }

    /** Removes and properly disposes of the network interface. Should be called when exiting gameplay (returning to menu). */
    public void disposeNetworkInterface() {
        netIface.dispose();
    }

    // Registers all assets required by the game
    private void registerAllAssets() {
        assets.load("textures/beast.png", Texture.class);
        assets.load("textures/bullet.png", Texture.class);
        assets.load("textures/player.png", Texture.class);
        for (int i = 0; i < 4; i++) assets.load("textures/map/grass" + i + ".png", Texture.class);
        assets.load("textures/map/stone.png", Texture.class);
        assets.load("textures/map/stoneTile.png", Texture.class);
        Material.loadTextures();
        BlockProperties.getAllBlocks().forEach(properties -> assets.load(properties.getFullTexturePath(), Texture.class));
    }

    // Creates the libGDX skin used for some elements.
    private void createSkin() {
        Skin skin = VisUI.getSkin();

        // Create a map of all colors needed; loop over it and create a drawable for each
        ObjectMap<String, Color> colors = new ObjectMap<>();
        colors.put("red", Color.RED);
        colors.put("green", Color.GREEN);
        colors.put("black-transparent", new Color(0x00000088));
        colors.entries().forEach(color -> {
            Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
            pm.setColor(color.value);
            pm.fill();
            skin.add(color.key, new Texture(pm));
        });

        // Progress bars
        ProgressBar.ProgressBarStyle healthBarStyle = new ProgressBar.ProgressBarStyle(skin.get("default-horizontal", ProgressBar.ProgressBarStyle.class));
        healthBarStyle.knobBefore = skin.newDrawable("progressbar-filled", colors.get("red"));
        healthBarStyle.knob = skin.newDrawable("progressbar-filled", colors.get("red"));
        skin.add("health-bar", healthBarStyle);

        ProgressBar.ProgressBarStyle staminaBarStyle = new ProgressBar.ProgressBarStyle(skin.get("default-horizontal", ProgressBar.ProgressBarStyle.class));
        staminaBarStyle.knobBefore = skin.newDrawable("progressbar-filled", colors.get("green"));
        staminaBarStyle.knob = skin.newDrawable("progressbar-filled", colors.get("green"));
        skin.add("stamina-bar", staminaBarStyle);
    }
}
