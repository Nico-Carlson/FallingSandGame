/*
 Boids
*/

package sandbox_game;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import static sandbox_game.Sandbox_Game.boids;
import static sandbox_game.Sandbox_Game.cellSize;
import static sandbox_game.Sandbox_Game.cols;
import static sandbox_game.Sandbox_Game.grid;
import static sandbox_game.Sandbox_Game.height;
import static sandbox_game.Sandbox_Game.rows;
import static sandbox_game.Sandbox_Game.spatialCellSize;
import static sandbox_game.Sandbox_Game.spatialCols;
import static sandbox_game.Sandbox_Game.spatialGrid;
import static sandbox_game.Sandbox_Game.spatialRows;
import static sandbox_game.Sandbox_Game.width;

class Vector {
    
    public double x;
    public double y;
    
    public Vector(double x, double y) {
        this.x = x;
        this.y = y;
    }
    
    // methods
    
    // add another vector to this one
    public void add(Vector v) {
        this.x += v.x;
        this.y += v.y;
    }
    
    // subtract another vector from this one
    public void sub(Vector v){
        this.x -= v.x;
        this.y -= v.y;
    }
    
    public static Vector sub(Vector v1, Vector v2){
        return new Vector(v1.x - v2.x, v1.y - v2.y);
    }
    
    // multiply
    public void mult(double n) {
        this.x *= n;
        this.y *= n;
    }
    
    // divide
    public void div(double n){
        this.x /= n;
        this.y /= n;
    }
    
    public double mag() {
        return Math.sqrt(this.x*this.x + this.y*this.y);
    }
    
    public void normalize(){
        double m = this.mag();
        if (m != 0){
            this.div(m);
        }
    }
    
    // restrict the length to a max value
    public void limit(double max) {
        double magSq = this.x * this.x + this.y * this.y;
        if (magSq > max * max) {
            double m = Math.sqrt(magSq);
            this.x = (this.x/m) * max;
            this.y = (this.y/m) * max;
        }
    }
    
    public double dist(Vector v) {
        double dx = this.x - v.x;
        double dy = this.y - v.y;
        return Math.sqrt(dx*dx+dy*dy);
    }
}   // end of Vector class



class Boid {
    
    public Vector position;
    public Vector velocity;
    public Vector acceleration;
    public double maxSpeed;
    public double maxForce;
    public Random RNG = new Random();

    public Boid() {
    } 
    
    public Boid(double startX, double startY) {
        this.position = new Vector (startX, startY);
        this.velocity = new Vector((RNG.nextDouble() - 0.5) * 2, (RNG.nextDouble() - 0.5) * 2);
        this.acceleration = new Vector(0,0);
        this.maxSpeed = 2.0;
        this.maxForce = 0.1;
    }
    
    // avoid the cursor
    // calcs a steering force to flee from mouse location
    public void avoid(Vector target, double avoidRadius) {
        double avoidRadiusSq = avoidRadius * avoidRadius;
        double dx = this.position.x - target.x;
        double dy = this.position.y - target.y;
        double dSq = dx * dx + dy * dy;
        
        // if the boid is close to the target then calc the force to push it away
        if (dSq > 0 && dSq < avoidRadiusSq) {
            Vector steer = new Vector(dx, dy);  // Vector pointing away from target
            steer.normalize();
            steer.mult(this.maxSpeed);  // Move away at maximum speed
            steer.sub(this.velocity);   // Reynolds steering formula: Desired - Velocity
        
            // multiply maxForce by a higher scaler so mouse avoidance overrides normal flocking
            steer.limit(this.maxForce * 2.5);
            
            this.applyForce(steer);
        }
    }   // end of avoid
    
    // deal with screen edges
    public void checkEdges(int screenWidth, int screenHeight){
        int width = Sandbox_Game.gamePanel.getWidth();
        int height = Sandbox_Game.gamePanel.getHeight();
        
        Vector desired = new Vector(this.velocity.x, this.velocity.y);
        boolean nearEdge = false;
        double edgeDist = 25.0;
        
        if(this.position.x < edgeDist) {
            desired.x = this.maxSpeed;
            nearEdge = true;
        } else if (this.position.x > width - edgeDist) {
            desired.x = -this.maxSpeed;
            nearEdge = true;
        }
        
        if(this.position.y < edgeDist) {
            desired.y = this.maxSpeed;
            nearEdge = true;
        } else if (this.position.y > height - edgeDist) {
            desired.y = -this.maxSpeed;
            nearEdge = true;
        }
        
        // apply a strong turning force near an edge
        if (nearEdge) {
            desired.normalize();
            desired.mult(this.maxSpeed);
            Vector steer = new Vector(desired.x - this.velocity.x, desired.y - this.velocity.y);
            steer.limit(this.maxForce * 3.0);
            this.applyForce(steer);
        }
        
        // account for any glitches where a boid goes out of frame
        if (this.position.x < 0) this.position.x = 0;
        if (this.position.x > width) this.position.x = width;
        if (this.position.y < 0) this.position.y = 0;
        if (this.position.y > height) this.position.y = height;
        
    }   // end of checkEdges
    
    // accumulate steering forces
    public void applyForce(Vector forceVector) {
        this.acceleration.add(forceVector);
    }   // end of applyForce
    
    // update the boids position
    public void update(){
        // add accumulated acceleration and velocity
        this.velocity.add(this.acceleration);
        
        // enforce speed limit
        this.velocity.limit(this.maxSpeed);
        
        // add velocity
        this.position.add(this.velocity);
        
        // reset acceleration to 0
        this.acceleration.x = 0;
        this.acceleration.y = 0;
    }   // end of update
    
    // Change the method signature to accept the 2D ArrayList
    public void flock(ArrayList<Boid>[][] spatialGrid, int spatialCellSize, double sepWeight, double aliWeight, double cohWeight) {
        double sepRadiusSq = 25.0 * 25.0;
        double neighborRadiusSq = 50.0 * 50.0;

        Vector sep = new Vector(0,0);
        Vector ali = new Vector(0,0);
        Vector coh = new Vector(0,0);

        int sepCount = 0;
        int neighborCount = 0;

        // Find which bucket this boid is in
        int myCol = (int) (this.position.x / spatialCellSize);
        int myRow = (int) (this.position.y / spatialCellSize);

        // Loop only through the 9 surrounding buckets (including its own)
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                int checkCol = myCol + i;
                int checkRow = myRow + j;

                // Ensure the bucket is within screen bounds
                if (checkCol >= 0 && checkCol < spatialGrid.length && checkRow >= 0 && checkRow < spatialGrid[0].length) {

                    // Iterate only through the boids in this specific bucket
                    for (Boid other : spatialGrid[checkCol][checkRow]) {
                        if (other == this) continue;

                        double dx = this.position.x - other.position.x;
                        double dy = this.position.y - other.position.y;
                        double dSq = dx * dx + dy * dy;

                        if (dSq > 0 && dSq < neighborRadiusSq) {

                            ali.add(other.velocity);
                            coh.add(other.position);
                            neighborCount++;

                            if (dSq < sepRadiusSq) {
                                Vector diff = new Vector(dx, dy);
                                diff.div(dSq); // Optimized division by distance squared
                                sep.add(diff);
                                sepCount++;
                            }
                        }
                    } // end of bucket iteration
                }
            }
        } // end of 9-cell grid iteration

        // finalize seperation force
        if (sepCount > 0) {
            sep.div((double)sepCount);
            if(sep.mag() > 0){
                sep.normalize();
                sep.mult(this.maxSpeed);
                sep.sub(this.velocity);
                sep.limit(this.maxForce);
            }
        }

        // finalize alignment and cohesion
        if (neighborCount > 0) {
            // alignment
            ali.div((double)neighborCount);
            ali.normalize();
            ali.mult(this.maxSpeed);
            ali.sub(this.velocity);
            ali.limit(this.maxForce);

            // cohesion
            coh.div((double)neighborCount);
            Vector desired = new Vector(coh.x - this.position.x, coh.y - this.position.y);
            desired.normalize();
            desired.mult(this.maxSpeed);
            coh.x = desired.x - this.velocity.x;
            coh.y = desired.y - this.velocity.y;
            coh.limit(this.maxForce);
        }

        // applying turning weights
        sep.mult(sepWeight);
        ali.mult(aliWeight);
        coh.mult(cohWeight);

        // apply final forces
        this.applyForce(sep);
        this.applyForce(ali);
        this.applyForce(coh);
    }
    
    // Helper method to keep the if-statements clean
    private boolean isSolid(Sandbox_Game.Element e) {
        return e == Sandbox_Game.Element.SAND || e == Sandbox_Game.Element.OBSIDIAN ||
               e == Sandbox_Game.Element.PLANT || e == Sandbox_Game.Element.SEED || 
               e == Sandbox_Game.Element.OIL; 
    }

    // Helper method for lethal blocks
    private boolean isLethal(Sandbox_Game.Element e) {
        return e == Sandbox_Game.Element.LAVA || e == Sandbox_Game.Element.FIRE || e == Sandbox_Game.Element.CONWAY;
    }

    public boolean interactWithEnvironment(Sandbox_Game.Element[][] grid, int cellSize, int cols, int rows) {
        int currentGridX = (int) (this.position.x / cellSize);
        int currentGridY = (int) (this.position.y / cellSize);

        // If a solid block fell onto the boid's current position, push it down
        if (currentGridX >= 0 && currentGridX < cols && currentGridY >= 0 && currentGridY < rows) {
            if (isSolid(grid[currentGridX][currentGridY])) {
                
                // Push the boid up to simulate it crawling out of the sand
                if (currentGridY > 0 && !isSolid(grid[currentGridX][currentGridY-1])){
                    this.position.y -= cellSize; 
                    this.velocity.y = -Math.abs(this.velocity.y); // Force velocity upward
                } else {
                    return true;
                }
                
            }
        }
        
        // look ahead to avoid walls/danger
        Vector feeler = new Vector(this.velocity.x, this.velocity.y);
        feeler.normalize();
        feeler.mult(cellSize*8); // look ahead n cells
        
        int lookAheadX = (int)((this.position.x + feeler.x) / cellSize);
        int lookAheadY = (int)((this.position.y + feeler.y) / cellSize);
        
        if (lookAheadX > 0 && lookAheadX < cols-1 && lookAheadY > 0 && lookAheadY < rows-1){
            Sandbox_Game.Element aheadElement = grid[lookAheadX][lookAheadY];
            
            if(isLethal(aheadElement) || isSolid(aheadElement)) {
                
                // vector pointing from hazards center to boid
                double hazardCenterX = lookAheadX * cellSize + (cellSize / 2.0);
                double hazardCenterY = lookAheadY * cellSize + (cellSize / 2.0);
                
                Vector desired = new Vector(this.position.x - hazardCenterX, this.position.y - hazardCenterY);
                
                // add a randomizer so boids dont just slam to a halt
                desired.x += (RNG.nextDouble() - 0.5) * 2;
                desired.y += (RNG.nextDouble() - 0.5) * 2;
                
                desired.normalize();
                desired.mult(this.maxSpeed);
                
                Vector steer = new Vector(desired.x - this.velocity.x, desired.y - this.velocity.y);
                
                // lethal blocks should trigger a larger response
                double avoidForce = isLethal(aheadElement) ? this.maxForce * 5 : this.maxForce * 1.5;
                steer.limit(avoidForce);
                this.applyForce(steer);
            }
        }
        
        // Look at where the boid wants to go next frame
        int nextGridX = (int) ((this.position.x + this.velocity.x) / cellSize);
        int nextGridY = (int) ((this.position.y + this.velocity.y) / cellSize);

        if (nextGridX > 0 && nextGridX < cols-1 && nextGridY > 0 && nextGridY < rows-1) {
            
            // Check lethal blocks
            if (isLethal(grid[nextGridX][nextGridY])) {
                grid[currentGridX][currentGridY] = Sandbox_Game.Element.SMOKE;
                return true; // Boid dies
            }

            boolean hitWall = false;
           
            if (isSolid(grid[nextGridX][currentGridY])) {
                this.velocity.x *= -1.2; // Bounce and amplify slightly to escape
                hitWall = true;
            }

            // Check Y axis
            if (isSolid(grid[currentGridX][nextGridY])) {
                this.velocity.y *= -1.2;
                hitWall = true;
            }

            // Check strict diagonal corner if neither straight axis was hit
            if (!hitWall && isSolid(grid[nextGridX][nextGridY])) {
                this.velocity.x *= -1.2;
                this.velocity.y *= -1.2;
                hitWall = true;
            }
            
            // Re-enforce speed limit in case the bounce amplified it too much
            if (hitWall) {
                this.velocity.limit(this.maxSpeed);
            }
        }
        
        return false;
    }
    
    // function to paint boids
    public void paintBoids(){
    // 1. Clear the spatial grid
        for (int c = 0; c < spatialCols; c++) {
            for (int r = 0; r < spatialRows; r++) {
                spatialGrid[c][r].clear();
            }
        }

        // 2. Populate the spatial grid
        for (Boid b : boids) {
            int sc = (int) (b.position.x / spatialCellSize);
            int sr = (int) (b.position.y / spatialCellSize);

            // Keep within bounds
            sc = Math.max(0, Math.min(sc, spatialCols - 1));
            sr = Math.max(0, Math.min(sr, spatialRows - 1));

            spatialGrid[sc][sr].add(b);
        }

        // 3. Update the boids using the spatial grid
        for (int i = boids.size()-1; i>=0; i--) {
            Boid b = boids.get(i);

            b.flock(spatialGrid, spatialCellSize, 1.5, 1.0, 1.0); 
            b.avoid(new Vector(Sandbox_Game.currentMouseX, Sandbox_Game.currentMouseY), 75.0);

            boolean isDead = b.interactWithEnvironment(grid, cellSize, cols, rows);
            if (isDead){
                boids.remove(i);
                continue;
            }

            b.update();
            b.checkEdges(width, height);
        }
    }
    
}   // end of Boid class
