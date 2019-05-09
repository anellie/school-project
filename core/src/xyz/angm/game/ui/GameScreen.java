package xyz.angm.game.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTextButton;
import xyz.angm.game.Game;
import xyz.angm.game.network.Client;
import xyz.angm.game.world.World;
import xyz.angm.game.world.entities.Player;

/** The screen active while the game is running. */
public class GameScreen extends Screen {

    private boolean pauseMenuActive = false ;
    private World world;
    private final PlayerHud playerHud = new PlayerHud(this);

    /** Constructs the screen and generates a new world. Run only when server is active.
     * @param game The game the screen is running under. */
    public GameScreen(Game game) {
        super(game);
        world = new World(System.currentTimeMillis());
        stage.addActor(playerHud);

        // Create a multiplexer for handling input for both UI and in-world (https://github.com/libgdx/libgdx/wiki/Event-handling#inputmultiplexer)
        InputMultiplexer inputMultiplexer = new InputMultiplexer();
        inputMultiplexer.addProcessor(stage);
        inputMultiplexer.addProcessor(new PlayerInputProcessor(this));
        Gdx.input.setInputProcessor(inputMultiplexer);
    }

    /** Constructs the screen and waits for the world from the server.
     * @param game The game the screen is running under.
     * @param client The client to wait for events with. Client should not have connected yet! */
    public GameScreen(Game game, Client client) {
        super(game);
        client.addListener(this::serverPacketReceived);

        boolean connected = client.start();
        if (!connected) {
            table.add(new VisLabel("No server was found!")).row();
            VisTextButton backButton = new VisTextButton("Return");
            backButton.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    returnToMainMenu();
                }
            });
            table.add(backButton).size(BUTTON_WIDTH, BUTTON_HEIGHT);
        }
    }

    public World getWorld() {
        return world;
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.05f, 0.05f, 0.05f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (world == null) return;  // Waiting for server connect
        world.act(delta);           // Update world
        world.render(delta);        // Render world. World render is separate to allow for different camera positions

        stage.act(delta);
        stage.draw();
    }

    // Packet/Object received from server. Only call on client instances.
    private void serverPacketReceived(Object packet) {
        if (packet instanceof Long) {           // Long is the seed; world needs to init now
            Gdx.app.postRunnable(() -> world = new World((Long) packet)); // World requires render context
        } else if (packet instanceof Player) {  // Player should be synced
            Player serverPlayer = (Player) packet;
            Player localPlayer = world.getPlayer();
            localPlayer.getPosition().set(serverPlayer.getPosition());
        } else if (packet == Client.Status.DISCONNECTED) { // Disconnect from server
            returnToMainMenu();
        }
    }

    /** Toggles the pause menu. */
    void togglePausePanel() {
        stage.clear();
        if (!pauseMenuActive) { // Open the pause menu
            stage.addActor(new PausePanel(this));
        } else { // Closes the pause menu
            stage.addActor(playerHud);
        }
        pauseMenuActive = !pauseMenuActive;
    }

    /** Goes back to the main menu. */
    void returnToMainMenu() {
        dispose();
        game.setScreen(new MenuScreen(game));
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        world.resizeViewport(width, height);
    }

    @Override
    public void dispose() {
        super.dispose();
        world.dispose();
        game.disposeNetworkInterface();
    }
}
