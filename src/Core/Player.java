package src.Core;

import src.TileEngine.Tileset;

import java.io.Serializable;
import java.util.Random;

public class Player implements Serializable {
    protected int XPos, YPos;
    protected int health;
    protected int score;


    protected Player() {
        health = 5;
        score = 0;
    }

    protected void placement(Room room, Random random, World world) {
        do {
            XPos = RandomUtils.uniform(random, room.xPos + 1, room.xPos + room.width);
            YPos = RandomUtils.uniform(random, room.yPos + 1, room.yPos + room.height);
        } while (!world.WALKABLE.contains(world.worldTiles[XPos][YPos].description()));
        world.playerTile = world.worldTiles[XPos][YPos];
        world.worldTiles[XPos][YPos] = Tileset.PLAYER;
    }


}
