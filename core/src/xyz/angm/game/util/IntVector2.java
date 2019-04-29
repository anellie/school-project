package xyz.angm.game.util;

/** A 2D vector using integers for storing it's values. */
public class IntVector2 {

    /** The first axis of the vector. */
    public int x;
    /** The second axis of the vector. */
    public int y;

    /** Creates a vector at (0, 0). */
    public IntVector2() {
        this(0, 0);
    }

    /** Creates a vector with the given parameters.
     * @param x The first axis.
     * @param y The second axis. */
    public IntVector2(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /** Copies the vector.
     * @return A new vector at the same position. */
    public IntVector2 copy() {
        return new IntVector2(this.x, this.y);
    }
}
