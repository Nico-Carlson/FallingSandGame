/*
Class for the physics engine
*/

package sandbox_game;

import java.util.Random;
import static sandbox_game.Sandbox_Game.cols;
import static sandbox_game.Sandbox_Game.grid;
import static sandbox_game.Sandbox_Game.rows;


public class PhysicsEngine {
    
    private Random RNG = new Random();

    // constructor
    public PhysicsEngine(int cols, int rows) {
    }

    // physics loop
    public void updatePhysics() {
        
        // check from bttom to top
        for (int y = rows - 1; y >= 0; y--) {
            for (int x = 0; x < cols; x++) {
                
                sandPhysics(x,y);
                
                waterPhysics(x,y);

                lavaPhysics(x, y);

                seedPhysics(x, y);

                plantPhysics(x, y);

            }
        }

        
        // check from top to bottom
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {

                steamPhysics(x, y);
                
            }
        }
        
    }   // end of update physics

    // sand physics
    public void sandPhysics(int x, int y){
        
        // -------- check for sand --------
        if (grid[x][y] == Sandbox_Game.Element.SAND) {
            int sandDirection = RNG.nextInt(2);

            // first sand should try and fall straight down
            if (y < rows - 1 && (grid[x][y + 1] == Sandbox_Game.Element.EMPTY || 
                                 grid[x][y + 1] == Sandbox_Game.Element.WATER || 
                                 grid[x][y + 1] == Sandbox_Game.Element.LAVA)) {
                Sandbox_Game.Element oldPos = grid[x][y + 1];
                grid[x][y] = oldPos;
                grid[x][y + 1] = Sandbox_Game.Element.SAND;
            }
            // next sand should fall bottom left
            else if (sandDirection == 0) {
                if (y < rows - 1 && x > 0 && (grid[x - 1][y + 1] == Sandbox_Game.Element.EMPTY || 
                                              grid[x - 1][y + 1] == Sandbox_Game.Element.WATER ||
                                              grid[x - 1][y + 1] == Sandbox_Game.Element.LAVA)) {
                    Sandbox_Game.Element oldPos = grid[x - 1][y + 1];
                    grid[x][y] = oldPos;
                    grid[x - 1][y + 1] = Sandbox_Game.Element.SAND;
                }
            }
            // next sand should fall bottom right
            else if (sandDirection == 1) {
                if (y < rows - 1 && x < cols - 1 && (grid[x + 1][y + 1] == Sandbox_Game.Element.EMPTY ||
                                                     grid[x + 1][y + 1] == Sandbox_Game.Element.WATER ||
                                                     grid[x + 1][y + 1] == Sandbox_Game.Element.LAVA)) {
                    Sandbox_Game.Element oldPos = grid[x + 1][y + 1];
                    grid[x][y] = oldPos;
                    grid[x + 1][y + 1] = Sandbox_Game.Element.SAND;
                }
            }
        }
        
    }
    // water physics
    public void waterPhysics(int x, int y){
        
        // -------- check for water --------
        if (grid[x][y] == Sandbox_Game.Element.WATER) {

            // 1 first water should try and fall straight down
            if (y < rows - 1 && grid[x][y + 1] == Sandbox_Game.Element.EMPTY) {
                grid[x][y] = Sandbox_Game.Element.EMPTY;
                grid[x][y + 1] = Sandbox_Game.Element.WATER;
            }
            // 2 next water should move bottom left
            else if (y < rows - 1 && x > 0 && grid[x - 1][y + 1] == Sandbox_Game.Element.EMPTY) {
                grid[x][y] = Sandbox_Game.Element.EMPTY;
                grid[x - 1][y + 1] = Sandbox_Game.Element.WATER;
            }
            // 3 next water should move bottom right
            else if (y < rows - 1 && x < cols - 1 && grid[x + 1][y + 1] == Sandbox_Game.Element.EMPTY) {
                grid[x][y] = Sandbox_Game.Element.EMPTY;
                grid[x + 1][y + 1] = Sandbox_Game.Element.WATER;
            }
            // 4 spread left or right 
            else {
                int flowRate = 4;
                int waterDirection = RNG.nextInt(2);

                // flow left
                if (waterDirection == 0) {
                    int targetX = x;
                    while (targetX > 0 && grid[targetX - 1][y] == Sandbox_Game.Element.EMPTY && flowRate > 0) {
                        targetX--;
                        flowRate--;
                    }
                    if (targetX != x) {
                        grid[x][y] = Sandbox_Game.Element.EMPTY;
                        grid[targetX][y] = Sandbox_Game.Element.WATER;
                    }
                }
                // flow right
                else {
                    int targetX = x;
                    while (targetX < cols - 1 && grid[targetX + 1][y] == Sandbox_Game.Element.EMPTY && flowRate > 0) {
                        targetX++;
                        flowRate--;
                    }
                    if (targetX != x) {
                        grid[x][y] = Sandbox_Game.Element.EMPTY;
                        grid[targetX][y] = Sandbox_Game.Element.WATER;
                        x = targetX; 
                    }
                }
            }
        }
        
    }
    // lava physics
    public void lavaPhysics(int x, int y){
        
        // -------- check for lava --------
        if (grid[x][y] == Sandbox_Game.Element.LAVA) {

            // 1 check four spots for water to make obsidian
            if (y < rows - 1 && grid[x][y + 1] == Sandbox_Game.Element.WATER) {
                grid[x][y + 1] = Sandbox_Game.Element.OBSIDIAN;
                grid[x][y] = Sandbox_Game.Element.STEAM;
            } 
            else if (y > 0 && grid[x][y - 1] == Sandbox_Game.Element.WATER) {
                grid[x][y - 1] = Sandbox_Game.Element.STEAM;
                grid[x][y] = Sandbox_Game.Element.OBSIDIAN;
            } 
            else if (x < cols - 1 && grid[x + 1][y] == Sandbox_Game.Element.WATER) {
                grid[x + 1][y] = Sandbox_Game.Element.OBSIDIAN;
                grid[x][y] = Sandbox_Game.Element.STEAM;
            } 
            else if (x > 0 && grid[x - 1][y] == Sandbox_Game.Element.WATER) {
                grid[x - 1][y] = Sandbox_Game.Element.STEAM;
                grid[x][y] = Sandbox_Game.Element.OBSIDIAN;
            }
            // 2 lava should try and fall straight down
            else if (y < rows - 1 && grid[x][y + 1] == Sandbox_Game.Element.EMPTY) {
                grid[x][y] = Sandbox_Game.Element.EMPTY;
                grid[x][y + 1] = Sandbox_Game.Element.LAVA;
            }
            // 3 spread left or right
            else {
                int flowRate = 1;
                int lavaDirection = RNG.nextInt(2);

                if (lavaDirection == 0) {
                    int targetX = x;
                    while (targetX > 0 && grid[targetX - 1][y] == Sandbox_Game.Element.EMPTY && flowRate > 0) {
                        targetX--;
                        flowRate--;
                    }
                    if (targetX != x) {
                        grid[x][y] = Sandbox_Game.Element.EMPTY;
                        grid[targetX][y] = Sandbox_Game.Element.LAVA;
                    }
                } else {
                    int targetX = x;
                    while (targetX < cols - 1 && grid[targetX + 1][y] == Sandbox_Game.Element.EMPTY && flowRate > 0) {
                        targetX++;
                        flowRate--;
                    }
                    if (targetX != x) {
                        grid[x][y] = Sandbox_Game.Element.EMPTY;
                        grid[targetX][y] = Sandbox_Game.Element.LAVA;
                        x = targetX;
                    }
                }
            }
        }
        
    }
    // seed physics
    public void seedPhysics(int x, int y){
        
        // -------- check for seed --------
        if (grid[x][y] == Sandbox_Game.Element.SEED) {
            int seedDirection = RNG.nextInt(2);

            // first seed should try and fall straight down
            if (y < rows - 1 && grid[x][y + 1] == Sandbox_Game.Element.EMPTY) {
                Sandbox_Game.Element oldPos = grid[x][y + 1];
                grid[x][y] = oldPos;
                grid[x][y + 1] = Sandbox_Game.Element.SEED;
            }
            else if (seedDirection == 0) {
                if (y < rows - 1 && x > 0 && grid[x - 1][y + 1] == Sandbox_Game.Element.EMPTY) {
                    Sandbox_Game.Element oldPos = grid[x - 1][y + 1];
                    grid[x][y] = oldPos;
                    grid[x - 1][y + 1] = Sandbox_Game.Element.SEED;
                }
            }
            else if (seedDirection == 1) {
                if (y < rows - 1 && x < cols - 1 && grid[x + 1][y + 1] == Sandbox_Game.Element.EMPTY) {
                    Sandbox_Game.Element oldPos = grid[x + 1][y + 1];
                    grid[x][y] = oldPos;
                    grid[x + 1][y + 1] = Sandbox_Game.Element.SEED;
                }
            }

            // burn if it touches lava
            if (y < rows - 1 && y > 0 && x > 0 && x < cols - 1 &&
                (grid[x][y + 1] == Sandbox_Game.Element.LAVA ||
                 grid[x][y - 1] == Sandbox_Game.Element.LAVA ||
                 grid[x - 1][y] == Sandbox_Game.Element.LAVA ||
                 grid[x + 1][y] == Sandbox_Game.Element.LAVA)) {

                grid[x][y] = Sandbox_Game.Element.STEAM;
            }
            // if its touching water
            else if (y < rows - 1 && grid[x][y + 1] == Sandbox_Game.Element.WATER) {
                grid[x][y + 1] = Sandbox_Game.Element.EMPTY;
                grid[x][y] = Sandbox_Game.Element.PLANT;
            } 
            else if (y > 0 && grid[x][y - 1] == Sandbox_Game.Element.WATER) {
                grid[x][y - 1] = Sandbox_Game.Element.EMPTY;
                grid[x][y] = Sandbox_Game.Element.PLANT;
            } 
            else if (x < cols - 1 && grid[x + 1][y] == Sandbox_Game.Element.WATER) {
                grid[x + 1][y] = Sandbox_Game.Element.EMPTY;
                grid[x][y] = Sandbox_Game.Element.PLANT;
            } 
            else if (x > 0 && grid[x - 1][y] == Sandbox_Game.Element.WATER) {
                grid[x - 1][y] = Sandbox_Game.Element.EMPTY;
                grid[x][y] = Sandbox_Game.Element.PLANT;
            }
        }
        
    }
    // plant physics
    public void plantPhysics(int x, int y){
        
        // -------- check for plant --------
        if (grid[x][y] == Sandbox_Game.Element.PLANT) {

            // burn if it touches lava
            if (y < rows - 1 && y > 0 && x > 0 && x < cols - 1 &&
                (grid[x][y + 1] == Sandbox_Game.Element.LAVA ||
                 grid[x][y - 1] == Sandbox_Game.Element.LAVA ||
                 grid[x - 1][y] == Sandbox_Game.Element.LAVA ||
                 grid[x + 1][y] == Sandbox_Game.Element.LAVA)) {

                grid[x][y] = Sandbox_Game.Element.STEAM;
            }

            int growDirection = RNG.nextInt(3);
            int growChance = RNG.nextInt(50);

            if (growChance == 1 && x > 0 && x < cols - 1 && y > 0 && y < rows - 1) {
                if (growDirection == 0 && grid[x - 1][y - 1] == Sandbox_Game.Element.EMPTY && 
                    (grid[x - 1][y - 1] == Sandbox_Game.Element.EMPTY && grid[x + 1][y - 1] == Sandbox_Game.Element.EMPTY)) {
                    grid[x - 1][y - 1] = Sandbox_Game.Element.PLANT;   
                } 
                else if (growDirection == 1 && grid[x + 1][y - 1] == Sandbox_Game.Element.EMPTY && 
                         (grid[x - 1][y - 1] == Sandbox_Game.Element.EMPTY && grid[x + 1][y - 1] == Sandbox_Game.Element.EMPTY)) {
                    grid[x + 1][y - 1] = Sandbox_Game.Element.PLANT;   
                } 
                else if (growDirection == 2 && grid[x][y - 1] == Sandbox_Game.Element.EMPTY && 
                         (grid[x - 1][y - 1] == Sandbox_Game.Element.EMPTY && grid[x + 1][y - 1] == Sandbox_Game.Element.EMPTY)) {
                    grid[x][y - 1] = Sandbox_Game.Element.PLANT;   
                }
            }
        }
        
    }
    // steam physics
    public void steamPhysics(int x, int y){
        
        // -------- check for steam --------
        if (grid[x][y] == Sandbox_Game.Element.STEAM) {
            int driftBias = RNG.nextInt(3);

            // 1 up-left first
            if (driftBias == 0 && y > 0 && x > 0 && 
                (grid[x - 1][y - 1] == Sandbox_Game.Element.EMPTY || 
                 grid[x - 1][y - 1] == Sandbox_Game.Element.WATER || 
                 grid[x - 1][y - 1] == Sandbox_Game.Element.LAVA)) {

                Sandbox_Game.Element oldPos = grid[x - 1][y - 1];
                grid[x][y] = oldPos;
                grid[x - 1][y - 1] = Sandbox_Game.Element.STEAM;
            }
            // 2 up-right first
            else if (driftBias == 1 && y > 0 && x < cols - 1 && 
                     (grid[x + 1][y - 1] == Sandbox_Game.Element.EMPTY || 
                      grid[x + 1][y - 1] == Sandbox_Game.Element.WATER || 
                      grid[x + 1][y - 1] == Sandbox_Game.Element.LAVA)) {

                Sandbox_Game.Element oldPos = grid[x + 1][y - 1];
                grid[x][y] = oldPos;
                grid[x + 1][y - 1] = Sandbox_Game.Element.STEAM;
            }
            // 3 straight up
            else if (y > 0 && (grid[x][y - 1] == Sandbox_Game.Element.EMPTY || 
                               grid[x][y - 1] == Sandbox_Game.Element.WATER || 
                               grid[x][y - 1] == Sandbox_Game.Element.LAVA)) {

                Sandbox_Game.Element oldPos = grid[x][y - 1];
                grid[x][y] = oldPos;
                grid[x][y - 1] = Sandbox_Game.Element.STEAM;
            }
            // 4 spread left or right against ceilings
            else {
                int flowRate = 4;
                int spreadDirection = RNG.nextInt(2);

                // flow left
                if (spreadDirection == 0) {
                    // chance for steam to fade away
                    int fade = RNG.nextInt(200);
                    if (fade == 1) {
                        grid[x][y] = Sandbox_Game.Element.EMPTY;
                    }

                    int targetX = x;
                    while (targetX > 0 && grid[targetX - 1][y] == Sandbox_Game.Element.EMPTY && flowRate > 0) {
                        targetX--;
                        flowRate--;
                    }
                    if (targetX != x) {
                        grid[x][y] = Sandbox_Game.Element.EMPTY;
                        grid[targetX][y] = Sandbox_Game.Element.STEAM;
                    }
                }
                // flow right
                else {
                    int targetX = x;
                    while (targetX < cols - 1 && grid[targetX + 1][y] == Sandbox_Game.Element.EMPTY && flowRate > 0) {
                        targetX++;
                        flowRate--;
                    }
                    if (targetX != x) {
                        grid[x][y] = Sandbox_Game.Element.EMPTY;
                        grid[targetX][y] = Sandbox_Game.Element.STEAM;
                        x = targetX; 
                    }
                }
            }
        }
        
    }
}