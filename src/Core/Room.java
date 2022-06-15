/**
 * Contains methods to randomly generate and draw a room.
 * Author: Lincoln Li
 * 05/15/2021
 */

package src.Core;

import src.TileEngine.TETile;
import src.TileEngine.Tileset;

import java.io.Serializable;
import java.util.Random;

public class Room implements Comparable<Room>, Serializable {
    int xPos;
    int yPos;
    int width;
    int height;
    int edge = 2;
    private static final TETile ROOMFloor = Tileset.GRASS;

    @Override
    public int compareTo(Room other) {
        return (xPos <= other.xPos) ? -1 : 1;
    }

    /**
     * Randomly place a room on the worldTiles, and add the room the worldTiles's rooms list.
     */
    protected void roomPlacement(World world, Random random) {

        /** Randomly generates random location,
         * at least edge = 2 tiles away from worldTiles boarder. */
        xPos = RandomUtils.uniform(random, edge, world.worldTiles.length);
        yPos = RandomUtils.uniform(random, edge, world.worldTiles[0].length);

        /** Updates location and size until find a good location to put the room.
         *  If no good location is found, reduce the size of room and locates again.
         *  No room will be added if tried 1000 times without success. */
        boolean goodLocationCheck = false;
        int divisor = 2;
        int trialCount = 0;

        // Set min room size at 4 * 4 including walls.
        int minRoomSize = 4;

        while (!goodLocationCheck && trialCount <= 1000) {
            /** Randomly choose room size between minRoomSize and a portion of worldTiles size */
            width = RandomUtils.uniform(random, minRoomSize, world.worldTiles.length / divisor);
            height = RandomUtils.uniform(random, minRoomSize, world.worldTiles[0].length / divisor);

            // Determine if location is good for the room,
            // tries up to 5000 times until a good location is found.
            for (int i = 0; i < 5000; i++) {
                goodLocationCheck = this.goodLocationCheck(world.worldTiles);
                if (goodLocationCheck) {
                    break;
                } else {
                    xPos = RandomUtils.uniform(random, edge, world.worldTiles.length);
                    yPos = RandomUtils.uniform(random, edge, world.worldTiles[0].length);
                }
            }

            // Reduce the max allowable size of room if failed to find a location.
            if ((world.worldTiles.length / (divisor + 1)) > minRoomSize
                    && (world.worldTiles[0].length / (divisor + 1)) > minRoomSize) {
                divisor += 1;
            }

            trialCount += 1;
        }

        /** Draws the room on map. */
        if (goodLocationCheck) {
            drawRoom(world.worldTiles, Tileset.WALL, ROOMFloor);
            world.addToRoomList(this);
        }
    }

    /**
     * Put a given room on worldTiles.
     */
    private void drawRoom(TETile[][] worldTiles, TETile wallTile, TETile roomFloorTile) {

        /** Draws the vertical walls. */
        for (int y = yPos; y < yPos + height; y++) {
            worldTiles[xPos][y] = wallTile;
            worldTiles[xPos + width - 1][y] = wallTile;
        }

        /** Draws the horizontal walls. */
        for (int x = xPos; x < xPos + width; x++) {
            worldTiles[x][yPos] = wallTile;
            worldTiles[x][yPos + height - 1] = wallTile;
        }

        /** Fills in room floors. */
        for (int x = xPos + 1; x < xPos + width - 1; x++) {
            for (int y = yPos + 1; y < yPos + height - 1; y++) {
                worldTiles[x][y] = roomFloorTile;
            }
        }
    }

    /**
     * Return true if room area + 3 surrounding tiles are empty
     */
    private boolean goodLocationCheck(TETile[][] worldTiles) {
        for (int x = xPos - edge; x < xPos + width + edge; x++) {
            for (int y = yPos - edge; y < yPos + height + edge; y++) {
                if (x < 0 || y < 0 || x >= worldTiles.length || y >= worldTiles[0].length
                        || !worldTiles[x][y].description().equals("nothing")) {
                    return false;
                }
            }
        }
        return true;
    }
}
