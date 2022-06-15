/**
 * Contains methods to generate a world with rooms, hallways and a player.
 * Author: Lincoln Li
 * 05/15/2021
 */

package src.Core;

import src.TileEngine.TETile;
import src.TileEngine.Tileset;
import edu.princeton.cs.introcs.StdAudio;

import java.io.Serializable;
import java.util.*;

public class World implements Serializable {

    // ArrayLists to store room objects.
    private List<Room> rooms;
    private List<ArrayList> teleports;
    protected TETile[][] worldTiles;
    private Random random;
    protected TETile playerTile;
    protected Player player;
    protected int healerCount;
    private static final String TARGET = "Mufasa";
    private static final String ENEMY = "Scar";
    private static final String HEALTH = "HP";
    private static final String TELEPORT = "Teleport";
    protected static final List<String> WALKABLE = new ArrayList<>(Arrays.asList(TARGET, ENEMY, HEALTH, TELEPORT, "Grass", "You"));

    public World(TETile[][] world, Random r) {

        random = r;
        worldTiles = world;
        rooms = new ArrayList();
        teleports = new ArrayList();
        healerCount = 0;

        fillWithBlankTiles();

        addRooms();

        // Sort rooms based on xPos to make organizing hallways easier.
        roomSort();

        // Add hallways.
        for (int i = 0; i < rooms.size() - 1; i++) {
            addHallway(rooms.get(i), rooms.get(i + 1));
        }

        // Add two teleports.
        for (int i = 0; i < 2; i++) {
            addItem(TELEPORT);
        }

        // Randomly add a player figure in a room.
        addPlayer();
        drawHP();

        // Randomly add 5 targets and enemies in rooms.
        for (int i = 0; i < Game.TARGETSCORE; i++) {
            addItem(TARGET);
            addItem(ENEMY);
        }
    }

    // Used by methods in Room class to add a room to the rooms list
    protected void addToRoomList(Room room) {
        rooms.add(room);
    }

    // Fills all the world tiles with blank tiles.
    private void fillWithBlankTiles() {
        int worldWidth = worldTiles.length;
        int worldHeight = worldTiles[0].length;
        for (int i = 0; i < worldWidth; i++) {
            for (int j = 0; j < worldHeight; j++) {
                worldTiles[i][j] = Tileset.NOTHING;
            }
        }
    }

    // Put HP info at the bottom.
    private void drawHP() {

        // Reset HP row.
        for (int x = 0; x < worldTiles.length; x++) {
            worldTiles[x][1] = Tileset.NOTHING;
        }

        // Draw HP.
        for (int i = 0, j = player.health / 2; i < player.health; i++, j--) {
            worldTiles[worldTiles.length / 2 - j][1] = Tileset.HEART;
        }
    }

    // Randomly creates 8-15 rooms on the world.
    private void addRooms() {
        int minRooms = 8;
        int maxRooms = 15;
        int targetRoomCount = RandomUtils.uniform(random, minRooms, maxRooms + 1);
        for (int i = 0; i <= targetRoomCount; i++) {
            Room newRoom = new Room();
            newRoom.roomPlacement(this, random);
        }
    }

    private void roomSort() {
        Collections.sort(rooms);
    }

    // Randomly place a hallway on the world to connect two given rooms.
    private void addHallway(Room room1, Room room2) {
        // Starts and ends at the left side of the room, to make hallways organized.
        int x1 = RandomUtils.uniform(random, room1.xPos + 1, room1.xPos + room1.width / 2);
        int x2 = RandomUtils.uniform(random, room2.xPos + 1, room2.xPos + room2.width / 2);
        int y1 = RandomUtils.uniform(random, room1.yPos + 1, room1.yPos + room1.height - 1);
        int y2 = RandomUtils.uniform(random, room2.yPos + 1, room2.yPos + room2.height - 1);
        int xLeft, xRight, yStart, yEnd, x, y;

        if (x1 <= x2) {
            xLeft = x1;
            xRight = x2;
            yStart = y1;
            yEnd = y2;
        } else {
            xLeft = x2;
            xRight = x1;
            yStart = y2;
            yEnd = y1;
        }

        for (x = xLeft; x <= xRight; x++) {
            worldTiles[x][yStart] = Tileset.GRASS;
            ifTileReplace(x, yStart + 1, Tileset.NOTHING, Tileset.WALL);
            ifTileReplace(x, yStart - 1, Tileset.NOTHING, Tileset.WALL);
        }

        /** Draws the corner at the turn. */
        ifTileReplace(x, yStart + 1, Tileset.NOTHING, Tileset.WALL);
        ifTileReplace(x, yStart - 1, Tileset.NOTHING, Tileset.WALL);
        ifTileReplace(x + 1, yStart - 1, Tileset.NOTHING, Tileset.WALL);
        ifTileReplace(x + 1, yStart + 1, Tileset.NOTHING, Tileset.WALL);

        if (yStart <= yEnd) {
            for (y = yStart; y <= yEnd; y++) {
                worldTiles[x][y] = Tileset.GRASS;
                ifTileReplace(x + 1, y, Tileset.NOTHING, Tileset.WALL);
                ifTileReplace(x - 1, y, Tileset.NOTHING, Tileset.WALL);
            }
        } else {
            for (y = yStart; y >= yEnd; y--) {
                worldTiles[x][y] = Tileset.GRASS;
                ifTileReplace(x + 1, y, Tileset.NOTHING, Tileset.WALL);
                ifTileReplace(x - 1, y, Tileset.NOTHING, Tileset.WALL);
            }
        }
    }

    // If tile is styleToReplace, then replace with newStyle.
    private void ifTileReplace(int xPos, int yPos, TETile styleToReplace, TETile newStyle) {
        if (worldTiles[xPos][yPos].description().equals(styleToReplace.description())) {
            worldTiles[xPos][yPos] = newStyle;
        }
    }

    // Randomly pick a room already created.
    private Room randomRoom() {
        return rooms.get(RandomUtils.uniform(random, rooms.size()));
    }


    // Add a player to the world.
    private void addPlayer() {
        player = new Player();
        Room room = randomRoom();
        player.placement(room, random, this);
    }

    // Randomly add a good guy (target) or a bad guy (enemy) to the world based on given string.
    private void addItem(String who) {
        Room room = randomRoom();

        // Set tile style based on Target/Enemy.
        TETile whoTile;
        switch (who) {
            case TARGET:
                whoTile = Tileset.TARGET;
                break;
            case ENEMY:
                whoTile = Tileset.ENEMY;
                break;
            case HEALTH:
                whoTile = Tileset.HEART;
                break;
            case TELEPORT:
                whoTile = Tileset.TELEPORT;
                break;
            default:
                whoTile = Tileset.GRASS;
                break;
        }

        // Put the item in the world.
        int whoXPos;
        int whoYPos;
        do {
            whoXPos = RandomUtils.uniform(random, room.xPos + 1, room.xPos + room.width);
            whoYPos = RandomUtils.uniform(random, room.yPos + 1, room.yPos + room.height);
        } while (!worldTiles[whoXPos][whoYPos].description().equalsIgnoreCase("Grass"));
        worldTiles[whoXPos][whoYPos] = whoTile;

        // Additionally put teleports into a list to be used later.
        if (who.equals(TELEPORT)) {
            List<Integer> teleport = new ArrayList();
            teleport.add(whoXPos);
            teleport.add(whoYPos);
            teleports.add((ArrayList) teleport);
        }
    }

    // Controls player movements.
    protected void playerMoveUp() {
        if (WALKABLE.contains(worldTiles[player.XPos][player.YPos + 1].description())) {
            updatePlayerPos(0, 1);
        }
    }

    protected void playerMoveDown() {
        if (WALKABLE.contains(worldTiles[player.XPos][player.YPos - 1].description())) {
            updatePlayerPos(0, -1);
        }
    }

    protected void playerMoveLeft() {
        if (WALKABLE.contains(worldTiles[player.XPos - 1][player.YPos].description())) {
            updatePlayerPos(-1, 0);
        }
    }

    protected void playerMoveRight() {
        if (WALKABLE.contains(worldTiles[player.XPos + 1][player.YPos].description())) {
            updatePlayerPos(1, 0);
        }
    }

    private void updatePlayerPos(int xMove, int yMove) {
        worldTiles[player.XPos][player.YPos] = playerTile;
        player.XPos += xMove;
        player.YPos += yMove;

        switch (worldTiles[player.XPos][player.YPos].description()) {
            case TARGET:
                String hitTargetSound = "/byog/Core/audio/Lion_Growl.wav";
                try {
                    StdAudio.play(hitTargetSound);
                } catch (IllegalArgumentException e) {
                }
                playerTile = Tileset.GRASS;
                player.score += 1;
                break;
            case ENEMY:
                String hitEnemySound = "/byog/Core/audio/Uh-Oh.wav";
                try {
                    StdAudio.play(hitEnemySound);
                } catch (IllegalArgumentException e) {
                }
                playerTile = Tileset.GRASS;
                player.health -= 1;
                drawHP();
                addItem(ENEMY);
                break;
            case HEALTH:
                String hitHealthSound = "/byog/Core/audio/eat.wav";
                try {
                    StdAudio.play(hitHealthSound);
                } catch (IllegalArgumentException e) {
                }
                playerTile = Tileset.GRASS;
                player.health += 1;
                drawHP();
                break;
            case TELEPORT:
                String hitTeleportSound = "/byog/Core/audio/teleport.wav";
                try {
                    StdAudio.play(hitTeleportSound);
                } catch (IllegalArgumentException e) {
                }
                teleport(player);
                playerTile = Tileset.TELEPORT;
                break;
            default:
                playerTile = worldTiles[player.XPos][player.YPos];
                break;
        }

        worldTiles[player.XPos][player.YPos] = Tileset.PLAYER;
        addHeal(15);

    }

    private void addHeal(int steps) {
        // On average, put a heal for every 10 moves.
        int r = RandomUtils.uniform(random, steps);
        if (r == 0 && healerCount < 2) {
            addItem(HEALTH);
            healerCount += 1;
        }
    }

    // Move player between two teleport sites.
    private void teleport(Player player) {
        if (player.XPos == (Integer) teleports.get(0).get(0)
                && player.YPos == (Integer) teleports.get(0).get(1)) {
            player.XPos = (Integer) teleports.get(1).get(0);
            player.YPos = (Integer) teleports.get(1).get(1);
        } else {
            player.XPos = (Integer) teleports.get(0).get(0);
            player.YPos = (Integer) teleports.get(0).get(1);
        }
    }
}
