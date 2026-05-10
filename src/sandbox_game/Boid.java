/*
 Boids
*/

package sandbox_game;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
        
        double x = this.position.x;
        double y = this.position.y;
        
        if(x > width || x < 0 || y > height || y < 0) {
            this.velocity.x *=-1;
            this.velocity.y *=-1;
        } 
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
    
    public boolean interactWithEnvironment(Sandbox_Game.Element[][] grid, int cellSize, int cols, int rows) {
        // Project future position based on velocity to detect collisions before moving into them
        int nextGridX = (int) ((this.position.x + this.velocity.x) / cellSize);
        int nextGridY = (int) ((this.position.y + this.velocity.y) / cellSize);

        // Ensure the projected coordinates are within the grid boundaries
        if (nextGridX >= 0 && nextGridX < cols && nextGridY >= 0 && nextGridY < rows) {
            Sandbox_Game.Element nextCell = grid[nextGridX][nextGridY];

            // Lethal collision: Boid dies and creates smoke
            if (nextCell == Sandbox_Game.Element.LAVA || nextCell == Sandbox_Game.Element.FIRE) {
                grid[nextGridX][nextGridY] = Sandbox_Game.Element.SMOKE;
                return true; // Returns true to signal the main loop to remove this boid
            }

            // Solid collision: Bounce off
            if (nextCell == Sandbox_Game.Element.SAND || nextCell == Sandbox_Game.Element.OBSIDIAN ||
                nextCell == Sandbox_Game.Element.PLANT || nextCell == Sandbox_Game.Element.SEED || 
                nextCell == Sandbox_Game.Element.OIL) {

                int currentGridX = (int) (this.position.x / cellSize);
                int currentGridY = (int) (this.position.y / cellSize);

                // Reverse the velocity axis that caused the collision
                if (nextGridX != currentGridX) this.velocity.x *= -1;
                if (nextGridY != currentGridY) this.velocity.y *= -1;

                // Apply a small randomized deflection to prevent them from getting stuck in perfect horizontal/vertical bounces
                this.velocity.x += (RNG.nextDouble() - 0.5);
                this.velocity.y += (RNG.nextDouble() - 0.5);
            }
        }
        
        return false; // Returns false if the boid survives the frame
    }
    
}   // end of Boid class
