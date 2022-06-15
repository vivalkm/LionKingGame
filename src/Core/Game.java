package src.Core;

import src.TileEngine.TERenderer;
import src.TileEngine.TETile;
import edu.princeton.cs.introcs.StdAudio;
import edu.princeton.cs.introcs.StdDraw;

import java.awt.*;
import java.io.*;
import java.util.Random;

public class Game {
    private static final int WIDTH = 30;
    private static final int HEIGHT = 20;
    private static final int TILESIZE = 48;
    private static long SEED = 0;
    private static Random RANDOM;
    protected static TETile[][] worldTiles;
    protected static World world;
    private static boolean playWithKeyboardSwitch = true;
    protected static final int TARGETSCORE = 5;

    /**
     * Method used for playing a fresh game. The game should start from the main menu.
     */

    public void playWithKeyboard() {
        playWithKeyboardSwitch = true;

        // Initiates background music when enters the game.
        String backgroundMusic = "/src/Core/audio/CircleOfLife-EJ-1.mid";
        try {
            StdAudio.play(backgroundMusic);
        } catch (IllegalArgumentException e) {
        }

        mainMenuLauncher();
    }

    // Launch the main menu.
    private void mainMenuLauncher() {
        mainMenu();

        // Navigate main menu.
        String mode;
        while (true) {
            if (!StdDraw.hasNextKeyTyped()) {
                continue;
            } else {
                mode = String.valueOf(StdDraw.nextKeyTyped()).toLowerCase();
                switch (mode) {
                    case "n":
                        newGame();
                    case "q":
                        quit();
                    case "l":
                        loadGame();
                    default:
                        break;
                }
            }
        }
    }

    // Creates the Main Menu.
    public void mainMenu() {

        StdDraw.setCanvasSize(WIDTH * TILESIZE, HEIGHT * TILESIZE);

        StdDraw.setXscale(0, WIDTH);
        StdDraw.setYscale(0, HEIGHT);
        StdDraw.clear(StdDraw.BLACK);
        StdDraw.enableDoubleBuffering();

        String strTitle = "The Lion King";
        String strNew = "New Game (N)";
        String strLoad = "Load Game (L)";
        String strQuit = "Quit (Q)";

        StdDraw.setPenColor(StdDraw.WHITE);
        Font fontTitle = new Font("Monaco", Font.BOLD, 60);
        StdDraw.setFont(fontTitle);
        StdDraw.text(WIDTH / 2, HEIGHT * 7 / 10, strTitle);

        Font fontMenu = new Font("Monaco", Font.BOLD, 20);
        StdDraw.setFont(fontMenu);
        StdDraw.text(WIDTH / 2, HEIGHT / 2, strNew);
        StdDraw.text(WIDTH / 2, HEIGHT / 2 - 1, strLoad);
        StdDraw.text(WIDTH / 2, HEIGHT / 2 - 2, strQuit);
        StdDraw.show();
    }

    public void newGame() {
        String playerSeedInput = "";
        String nextChar = "";

        // Get the number input for random SEED.
        while (playerSeedInput.length() == 0
                || (playerSeedInput.length() > 0 && !nextChar.equalsIgnoreCase("s"))) {
            StdDraw.clear(StdDraw.BLACK);
            StdDraw.text(WIDTH / 2, HEIGHT / 2,
                    "Enter a random number, ends with letter \"S\".");
            StdDraw.text(WIDTH / 2, HEIGHT / 2 - 4, playerSeedInput);
            StdDraw.show();
            if (!StdDraw.hasNextKeyTyped()) {
                continue;
            } else {
                nextChar = String.valueOf(StdDraw.nextKeyTyped());

                // Update playerInput only if the input is a digit.
                if (nextChar.matches("\\d+")) {
                    playerSeedInput += nextChar;
                }
            }
        }

        StdDraw.text(WIDTH / 2, HEIGHT / 2 - 6, "Loading...");
        StdDraw.show();

        SEED = Long.parseLong(playerSeedInput);
        RANDOM = new Random(SEED);

        // Based on random seed input by player,
        // create a World object to launch the world with random rooms and hallways
        worldTiles = new TETile[WIDTH][HEIGHT];
        world = new World(worldTiles, RANDOM);

        updateWithKeyboard();
    }

    public void updateWithKeyboard() {
        // Draw out the world
        TERenderer ter = new TERenderer();
        ter.initialize(WIDTH, HEIGHT);
        ter.renderFrame(worldTiles);

        // Move the player on the map.
        String move;
        while (true) {
            if (!StdDraw.hasNextKeyTyped()) {
                continue;
            } else {
                move = String.valueOf(StdDraw.nextKeyTyped());
            }
            playerMove(move);
            ter.renderFrame(worldTiles);

            // Monitor is :Q is entered to quit and save.
            if (move.equalsIgnoreCase(":")) {
                while (true) {
                    if (!StdDraw.hasNextKeyTyped()) {
                        continue;
                    } else {
                        move = String.valueOf(StdDraw.nextKeyTyped());
                        if (move.equalsIgnoreCase("q")) {
                            quitSave();
                        } else {
                            break;
                        }
                    }
                }
            }
        }
    }

    public void updateWithString(String s) {

        // Move the player on the map.
        String move;
        int i = 0;
        while (i < s.length()) {
            move = s.substring(i, i + 1);
            playerMove(move);

            // Monitor is :Q is entered to quit and save.
            if (move.equalsIgnoreCase(":") && s.substring(i + 1).length() > 0) {
                if (s.substring(i + 1, i + 2).equalsIgnoreCase("q")) {
                    quitSave();
                }
            }
            i += 1;
        }
    }


    // Quit the game.
    public void quit() {
        System.exit(0);
    }

    public void quitSave() {
        try {
            FileOutputStream fileOut =
                    new FileOutputStream("savedWorld.ser");
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(world);
            out.close();
            fileOut.close();
            System.out.printf("Serialized world is saved in savedWorld.ser");

        } catch (IOException i) {
            i.printStackTrace();
        }

        quit();

    }

    public void loadGame() {
        try {
            FileInputStream fileIn = new FileInputStream("savedWorld.ser");
            ObjectInputStream in = new ObjectInputStream(fileIn);
            world = (World) in.readObject();
            in.close();
            fileIn.close();
            worldTiles = world.worldTiles;
            if (playWithKeyboardSwitch) {
                updateWithKeyboard();
            }
        } catch (IOException i) {
            noSavedGame();
        } catch (ClassNotFoundException c) {
            noSavedGame();
        }
    }

    public void noSavedGame() {
        String strNoSavedGame = "No saved game.";
        if (playWithKeyboardSwitch) {
            StdDraw.text(WIDTH / 2, HEIGHT / 2 - 4, strNoSavedGame);
            StdDraw.show();
        } else {
            System.out.println(strNoSavedGame);
        }
    }


    // Creates the Result Frame.
    public void result(String result) {

        if (playWithKeyboardSwitch) {
            String imgFileName;
            String strTitle1, strTitle2;
            Color titleColor;
            int resultImgWidth = WIDTH * TILESIZE;
            int resultImgHeight = HEIGHT * TILESIZE;
            int titleXPos, title1YPos, title2YPos;

            if (result.equalsIgnoreCase("win")) {
                imgFileName = "src/Core/pic/win.png";
                strTitle1 = "Congratulations!";
                strTitle2 = "You are now a Lion King!!!";
                titleColor = StdDraw.BOOK_RED;
                titleXPos = 0;
                title1YPos = resultImgHeight / 2 * 8 / 10;
                title2YPos = resultImgHeight / 2 * 7 / 10;
            } else {
                imgFileName = "src/Core/pic/lose.png";
                strTitle1 = "Defeated!";
                strTitle2 = "Scar stole the kingdom!";
                titleColor = Color.DARK_GRAY;
                titleXPos = resultImgHeight / 2 * 3 / 4;
                title1YPos = resultImgHeight / 2 * 6 / 10;
                title2YPos = resultImgHeight / 2 * 4 / 10;
            }


            StdDraw.setCanvasSize(resultImgWidth, resultImgHeight);

            StdDraw.setXscale(-resultImgWidth / 2, resultImgWidth / 2);
            StdDraw.setYscale(-resultImgHeight / 2, resultImgHeight / 2);
            StdDraw.clear(StdDraw.BLACK);
            StdDraw.enableDoubleBuffering();

            StdDraw.picture(0, 0, imgFileName, resultImgWidth, resultImgHeight);
            String strMsgToQuit = "Press M to Main Menu. Press Q to quit.";

            StdDraw.setPenColor(titleColor);
            Font fontTitle = new Font("Monaco", Font.BOLD, 50);
            StdDraw.setFont(fontTitle);
            StdDraw.text(titleXPos, title1YPos, strTitle1);
            StdDraw.text(titleXPos, title2YPos, strTitle2);

            StdDraw.setPenColor(StdDraw.WHITE);
            Font fontMenu = new Font("Monaco", Font.BOLD, 20);
            StdDraw.setFont(fontMenu);
            StdDraw.text(0, -resultImgHeight / 2 * 8 / 10, strMsgToQuit);
            StdDraw.show();

            while (true) {
                if (!StdDraw.hasNextKeyTyped()) {
                    continue;
                } else {
                    String next = String.valueOf(StdDraw.nextKeyTyped()).toLowerCase();
                    switch (next) {
                        case "q":
                            quit();
                        case "m":
                            mainMenuLauncher();
                        default:
                            break;
                    }
                }
            }
        } else {
            if (result.equalsIgnoreCase("win")) {
                System.out.println("Congrats! You are now a Lion King!!!");
            } else {
                System.out.println("Scar stole the kingdom! Do you want to try again?");
            }
        }
    }


    // Moves player based on W, A, S, and D keys.
    public void playerMove(String move) {

        switch (move.toLowerCase()) {
            case "w":
                world.playerMoveUp();
                break;
            case "s":
                world.playerMoveDown();
                break;
            case "a":
                world.playerMoveLeft();
                break;
            case "d":
                world.playerMoveRight();
                break;
            default:
                break;
        }

        if (world.player.score == TARGETSCORE) {
            result("win");
        }

        if (world.player.health <= 0) {
            result("lose");
        }
    }


    /**
     * Method used for testing the game code. The input string will be a series
     * of characters (for example, "n123sswwdasdassadwas", "n123sss:q", "lwww". The game should
     * behave exactly as if the user typed these characters into the game after playing
     * playWithKeyboard. If the string ends in ":q", the same world should be returned as if the
     * string did not end with q. For example "n123sss" and "n123sss:q" should return the same
     * world. However, the behavior is slightly different. After playing with "n123sss:q", the game
     * should save, and thus if we then called playWithInputString with the string "l", we'd expect
     * to get the exact same world back again, since this corresponds to loading the saved game.
     *
     * @param input the input string to feed to your program
     * @return the 2D TETile[][] representing the state of the world
     */
    public TETile[][] playWithInputString(String input) {
        // Run the game using the input passed in,
        // and return a 2D tile representation of the world that would have been
        // drawn if the same inputs had been given to playWithKeyboard().

        playWithKeyboardSwitch = false;

        // New game.
        if (input.substring(0, 1).equalsIgnoreCase("n")) {
            // Parse the SEED number.
            String seed = "";
            int i = 1;
            String next = input.substring(i, i + 1);
            while (!next.equalsIgnoreCase("s")) {
                if (next.matches("\\d+")) {
                    seed += next;
                }
                i += 1;
                next = input.substring(i, i + 1);
            }

            SEED = Long.parseLong(seed);
            RANDOM = new Random(SEED);
            worldTiles = new TETile[WIDTH][HEIGHT];
            world = new World(worldTiles, RANDOM);

            // Parse the movements.
            // Skip the first "S".
            i += 1;
            if (input.substring(i).length() > 0) {
                updateWithString(input.substring(i));
            }
        }

        // Load game.
        if (input.substring(0, 1).equalsIgnoreCase("l")) {
            loadGame();
            if (input.substring(1).length() > 0) {
                updateWithString(input.substring(1));
            }
        }

        return worldTiles;
    }
}
