package xyz.angm.game.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

/** A class containing all map data:
 * - Texture data for rendering the map
 * - Tile data for getting the terrain type (TODO). */
class WorldMap extends Image {

    /** Constructs a map; generating its content during construction.
     * @param generator The world generator to obtain data from. */
    WorldMap(TerrainGenerator generator) {
        super();

        // Rendering needs to happen in OpenGL context/thread; else crash
        Gdx.app.postRunnable(() -> {
            setDrawable(new TextureRegionDrawable(new TextureRegion((generator.createWorldMapTexture()))));
            setSize(getPrefWidth(), getPrefHeight());
            setPosition(0f, 0f);
        });
    }
}