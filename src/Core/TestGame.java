package src.Core;
import src.TileEngine.TETile;
import org.junit.Assert;
import org.junit.Test;


public class TestGame {
    @Test
    // Same input strings leads to same tile array.
    public void testSameString() {
        Game game1 = new Game();
        TETile[][] worldState1 = game1.playWithInputString("N999SDDDWWWDDD");
        Game game2 = new Game();
        TETile[][] worldState2 = game2.playWithInputString("N999SDDDWWWDDD");
        Assert.assertArrayEquals(worldState1, worldState2);
        Game game3 = new Game();
        game3.playWithInputString("N999SDDD:Q");
        TETile[][] worldState3 = game3.playWithInputString("LWWWDDD");
        Assert.assertArrayEquals(worldState1, worldState2);
        Assert.assertArrayEquals(worldState1, worldState3);
    }

    @Test
    // Different input strings leads to same tile array.
    public void testDiffString() {
        Game game1 = new Game();
        TETile[][] worldState1 = game1.playWithInputString("n448532445325663669ssdassdssd");
        Game game3 = new Game();
        TETile[][] worldState3 = game3.playWithInputString("n2615173613241323613swdddsasswwda");
        Assert.assertNotEquals(worldState1, worldState3);
    }

    @Test
    public void testPlayWithKeyboard() {
        Game game = new Game();
        game.playWithKeyboard();
    }

    @Test
    public void testWin() {
        Game game = new Game();
        game.result("win");
    }

    @Test
    public void testLose() {
        Game game = new Game();
        game.result("lose");
    }
}
