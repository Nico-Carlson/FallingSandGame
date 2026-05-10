/*
Final Project
Sandbox Game
Dev: Nico Carlson

inspiration: https://www.youtube.com/watch?v=5Ka3tbbT-9E
and : https://www.youtube.com/watch?v=VLZjd_Y1gJ8
 */
package sandbox_game;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Random;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.List;


// --------- Main Class -----------
public class Sandbox_Game extends JPanel {

    public enum Element {
        EMPTY, SAND, WATER, LAVA, OBSIDIAN, STEAM,
        SEED, PLANT, OIL, FIRE, SMOKE, BOID, CONWAY
    }
    
    // static instance for toolbar access
    public static Sandbox_Game gamePanel;

    // main variable initializing
    public static int cellSize = 5;
    public static int width = 900;
    public static int height = 700;
    public static int cols = width / cellSize;
    public static int rows = height / cellSize;

    // array for the grid
    public static Element[][] grid;
    
    // next array to hold GoL future states
    public static Element[][] nextGrid;
    public static int tickCounter = 0;
    public static int conwaySpeed = 5; // since GoL loads the next array much faster this is used to slow it down
    
    // array to save / load 
    public static Element[][] savedGrid;
    
    // array list for boids
    public static List<Boid> boids = new ArrayList<>();
    public static final int spatialCellSize = 50; // Matches neighborRadius
    public static int spatialCols = width / spatialCellSize + 1;
    public static int spatialRows = height / spatialCellSize + 1;
    public static ArrayList<Boid>[][] spatialGrid;
    
    
    // setting timer
    public static Timer timer;

    // pause state
    public static boolean isPaused = false;

    // setting rng
    public static Random RNG = new Random();
    
    // setting default frameRate
    public static int frameRate = 16;

    // instantiating the frame
    public static JFrame frame = new JFrame("Sandbox Game");

    // toolbar panel
    public static JPanel toolBar = new JPanel();

    // mouse variables
    public boolean isMouseHeld = false;
    public static int currentMouseX = 0;
    public static int currentMouseY = 0;
    
    public int mouseX = currentMouseX;
    public int mouseY = currentMouseY;
    
    public int lastMouseX = mouseX;
    public int lastMouseY = mouseY;


    // brush size
    public static int brushSize = 6;

    // button variables
    public static int bttnx = 25;
    public static int bttny = 25;
    public static int bttnWidth = 100;
    public static int bttnHeight = 30;
    public static int bttnSpacing = 25;

    // selected element
    public static Element currentElement = Element.SAND; // default for sand
    
    // set up the physics engine classes
    public PhysicsEngine engine;
    public Boid boidEngine;
    public Conway conwayEngine;
    
    
    
    
   

    // constructor
    public Sandbox_Game() {
        this.setPreferredSize(new Dimension(width, height));
        this.setBackground(Color.DARK_GRAY);

        // use an anonymous inner class extending MouseAdapter to handle input
        // I used this video and this tutorial to use this technique. Since I can't have
        // 10000s of JComponents with listeners for my large grid
        // https://docs.oracle.com/javase/tutorial/uiswing/events/generalrules.html
        // https://www.youtube.com/watch?v=n2Dpffp_HLc 
        MouseAdapter mouseHandler = new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                currentMouseX = e.getX();
                currentMouseY = e.getY();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                isMouseHeld = true;
                currentMouseX = e.getX();
                currentMouseY = e.getY();
                
                // Sync positions to prevent drawing a line from a previous click
                mouseX = currentMouseX;
                mouseY = currentMouseY;
                lastMouseX = currentMouseX;
                lastMouseY = currentMouseY;
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                isMouseHeld = false;
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                currentMouseX = e.getX();
                currentMouseY = e.getY();
            }
        };

        this.addMouseListener(mouseHandler);
        this.addMouseMotionListener(mouseHandler);
        
        
        this.addMouseWheelListener(e -> {
            int scrollAmount = e.getWheelRotation();
            int newCellSize = cellSize;

            // e.getWheelRotation() returns negative for scrolling up, positive for down
            if (scrollAmount < 0) {
                newCellSize++; // Zoom in
            } else {
                newCellSize--; // Zoom out
            }

            // Clamp the value to match slider bounds (min 2, max 25)
            if (newCellSize < 2) newCellSize = 2;
            if (newCellSize > 25) newCellSize = 25;

            // Update the grid if the size changed
            if (newCellSize != cellSize) {
                resizeGrid(newCellSize, true);
                
                // sync the UI slider if you make zoomButton static in ToolBar
                if (ToolBar.zoomButton != null) {
                    ToolBar.zoomButton.setValue(newCellSize);
                }
            }
        });
        
        
        // listen for window resize events
        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                resizeGrid(cellSize, false);                
            }
        });
        
        // set up the physics engine
        engine = new PhysicsEngine(cols, rows);
        boidEngine = new Boid();
        conwayEngine = new Conway();
        
        

        // setup the grid
        grid = new Element[cols][rows];
        for (int c = 0; c < cols; c++) {
            for (int r = 0; r < rows; r++) {
                grid[c][r] = Element.EMPTY;
            }
        }
        
        // setup the nextGrid
        nextGrid = new Element[cols][rows];
        for (int c = 0; c < cols; c++) {
            for (int r = 0; r < rows; r++) {
                nextGrid[c][r] = Element.EMPTY;
            }
        }
        
        // setup the savedGrid
        savedGrid = new Element[cols][rows];
        for (int c = 0; c < cols; c++) {
            for (int r = 0; r < rows; r++) {
                savedGrid[c][r] = Element.EMPTY;
            }
        }
        
        // Setup the spatial grid
        spatialGrid = new ArrayList[spatialCols][spatialRows];
        for (int c = 0; c < spatialCols; c++) {
            for (int r = 0; r < spatialRows; r++) {
                spatialGrid[c][r] = new ArrayList<Boid>();
            }
        }

        // setting the timer (16 is about 60 fps
        timer = new Timer(frameRate, e -> {

            // if mouse is down spawn element
            if (isMouseHeld) {
                lastMouseX = mouseX;
                lastMouseY = mouseY;
                
                mouseX = currentMouseX;
                mouseY = currentMouseY;
                
                
//                spawnElement(currentMouseX, currentMouseY);

                // Interpolate from the last frame's position to the current position
                spawnLine(lastMouseX, lastMouseY, mouseX, mouseY);
            }

            // if not paused
            if (!isPaused) {
                engine.updatePhysics();
                boidEngine.paintBoids();
                
                // slow down conway since its not a cell at a time but a grid at a time
                if(tickCounter % conwaySpeed == 0){
                    conwayEngine.updateConway(rows, cols, grid, nextGrid);
                }
                tickCounter ++;
            }

            repaint();
        });
        timer.start();
    }

    // setting up the paint component
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // loop through the whole grid and draw "pixels"
        for (int x = 0; x < cols; x++) {
            for (int y = 0; y < rows; y++) {

                if (null != grid[x][y])
                switch (grid[x][y]) {
                    case EMPTY -> {
                        g.setColor(Color.darkGray);
                        g.fillRect(x * cellSize, y * cellSize, cellSize, cellSize);
                    }
                    case SAND -> {
                        // yellow for sand
                        g.setColor(new Color(0xC2B280));
                        g.fillRect(x * cellSize, y * cellSize, cellSize, cellSize);
                    }
                    case WATER -> {
                        // blue for water
                        g.setColor(new Color(0, 191, 255));
                        g.fillRect(x * cellSize, y * cellSize, cellSize, cellSize);
                    }
                    case LAVA -> {
                        // red for lava
                        g.setColor(new Color(0xE42217));
                        g.fillRect(x * cellSize, y * cellSize, cellSize, cellSize);
                    }
                    case FIRE -> {
                        int flicker = RNG.nextInt(2);
                        if (flicker == 0) g.setColor(new Color(0xFF9A00));
                        else if (flicker == 1) g.setColor(new Color(0xFF5A00));
                        else g.setColor(new Color(0xD92500));
                        g.fillRect(x * cellSize, y * cellSize, cellSize, cellSize);
                    }
                    case SMOKE -> {
                        g.setColor(new Color(0x1A1A1A));
                        g.fillRect(x * cellSize, y * cellSize, cellSize, cellSize);
                    }
                    case OIL -> {
                        // black for oil
                        g.setColor(new Color(0x2C2519));
                        g.fillRect(x * cellSize, y * cellSize, cellSize, cellSize);
                    }
                    case OBSIDIAN -> {
                        g.setColor(new Color(0x382B46));
                        g.fillRect(x * cellSize, y * cellSize, cellSize, cellSize);
                    }
                    case STEAM -> {
                        g.setColor(new Color(0x9E9E9E));
                        g.fillRect(x * cellSize, y * cellSize, cellSize, cellSize);
                    }
                    case SEED -> {
                        g.setColor(Color.green.darker());
                        g.fillRect(x * cellSize, y * cellSize, cellSize, cellSize);
                    }
                    case PLANT -> {
                        g.setColor(Color.green.darker().darker());
                        g.fillRect(x * cellSize, y * cellSize, cellSize, cellSize);
                    }
                    case CONWAY -> {
                        g.setColor(Color.white.darker().darker());
                        g.fillRect(x * cellSize, y * cellSize, cellSize, cellSize);
                    }
                    default -> {
                    }
                    
                }
                

            }
        }
        
        // paint the boids
        g.setColor(Color.WHITE);
        for (Boid b : boids) {
            g.fillRect((int) b.position.x, (int) b.position.y, cellSize, cellSize);
        }

        // highlight cursor
        int centerGridX = currentMouseX / cellSize;
        int centerGridY = currentMouseY / cellSize;
        
        // double for radius to handle odd/even brush sizes more accurately
        double radius = brushSize / 2.0;
        int intRadius = (int)radius;

        // set highlight color
        g.setColor(Color.WHITE);

        // Iterate through the bounding box of the brush
        for (int i = -intRadius; i <= intRadius; i++) {
            for (int j = -intRadius; j <= intRadius; j++) {
                
                // Determine if the current cell is inside the circular brush area
                if (i * i + j * j <= radius * radius) {
                    
                    int drawX = (centerGridX + i) * cellSize;
                    int drawY = (centerGridY + j) * cellSize;

                    // Top edge: check if the cell directly above is outside the brush
                    if (i * i + (j - 1) * (j - 1) > radius * radius) {
                        g.drawLine(drawX, drawY, drawX + cellSize, drawY);
                    }
                    // Bottom edge: check if the cell directly below is outside the brush
                    if (i * i + (j + 1) * (j + 1) > radius * radius) {
                        g.drawLine(drawX, drawY + cellSize, drawX + cellSize, drawY + cellSize);
                    }
                    // Left edge: check if the cell to the left is outside the brush
                    if ((i - 1) * (i - 1) + j * j > radius * radius) {
                        g.drawLine(drawX, drawY, drawX, drawY + cellSize);
                    }
                    // Right edge: check if the cell to the right is outside the brush
                    if ((i + 1) * (i + 1) + j * j > radius * radius) {
                        g.drawLine(drawX + cellSize, drawY, drawX + cellSize, drawY + cellSize);
                    }
                }
            }
        }
    }   // end of paint component
    
    
    // method to resize cells "zoom"
    // Recalculate grid based on current panel dimensions and cell size
    public void resizeGrid(int newCellSize, boolean isZoomEvent) {
        if (newCellSize <= 0) return;
        
        int oldCellSize = cellSize;
        cellSize = newCellSize;
        
        int newCols = getWidth() / cellSize;
        int newRows = getHeight() / cellSize;
        
        // prevent crash if window is minimized entirely
        if (newCols <= 0 || newRows <= 0) return;

        Element[][] newGrid = new Element[newCols][newRows];
        Element[][] newNextGrid = new Element[newCols][newRows];
        Element[][] newSavedGrid = new Element[newCols][newRows];
        
        int shiftX = 0;
        int shiftY = 0;
        
        // calculate array shift based on mouse position
        if (isZoomEvent && oldCellSize != newCellSize) {
            
            int targetGridX = currentMouseX / oldCellSize;
            int targetGridY = currentMouseY / oldCellSize;
            
            int newGridX = currentMouseX / newCellSize;
            int newGridY = currentMouseY / newCellSize;
            
            shiftX = newGridX - targetGridX;
            shiftY = newGridY - targetGridY;            
        }
        
        // Cache the physical array dimensions to prevent rapid-event desyncs
        int gridWidth = grid != null ? grid.length : 0;
        int gridHeight = (gridWidth > 0 && grid[0] != null) ? grid[0].length : 0; 
       
        int savedWidth = savedGrid != null ? savedGrid.length : 0;
        int savedHeight = (savedWidth > 0 && savedGrid[0] != null) ? savedGrid[0].length : 0;

        for (int x = 0; x < newCols; x++) {
            for (int y = 0; y < newRows; y++) {
                
                // map the new grid coords back to the old grid coords
                int oldX = x - shiftX;
                int oldY = y - shiftY;
                
                if (oldX >= 0 && oldX < gridWidth && oldY >= 0 && oldY < gridHeight && grid[oldX][oldY] != null) {
                    newGrid[x][y] = grid[oldX][oldY];
                } else {
                    newGrid[x][y] = Element.EMPTY;
                }
                
                // keep savedGrid synchronized
                if (oldX >= 0 && oldX < savedWidth && oldY >= 0 && oldY < savedHeight && savedGrid[oldX][oldY] != null) {
                    newSavedGrid[x][y] = savedGrid[oldX][oldY];
                } else {
                    newSavedGrid[x][y] = Element.EMPTY;
                }
                
                // update nextGrid (can just be set to empty)
                newNextGrid[x][y] = Element.EMPTY;
            }
        }

        // safely overwrite the old arrays and dimension variables
        grid = newGrid;
        savedGrid = newSavedGrid;
        nextGrid = newNextGrid;
        cols = newCols;
        rows = newRows;
    }   // end of resize grid
    
    
    // function to interpolate between mouse movements (Bressenhams line alg)
    // http://www.youtube.com/watch?v=CceepU1vIKo
    // http://www.youtube.com/watch?v=BmowyD0dWeo
    public void spawnLine(int startX, int startY, int endX, int endY) {
        int x0 = startX / cellSize;
        int y0 = startY / cellSize;
        int x1 = endX / cellSize;
        int y1 = endY / cellSize;

        int dx = Math.abs(x1 - x0);
        int dy = Math.abs(y1 - y0);
        
        // if first coord is less than second set sx/sy to 1, otherwise set to -1
        int sx = x0 < x1 ? 1 : -1;
        int sy = y0 < y1 ? 1 : -1;
        
        // find you overall err (to find dist between two points)
        int err = dx - dy;

        while (true) {
            
            spawnElement(x0 * cellSize, y0 * cellSize);

            // exit loop once line is complete
            if (x0 == x1 && y0 == y1) {
                break;
            }
          
            int e2 = 2 * err;
          
            // update error and move along the line
            if (e2 > -dy) {
                err -= dy;
                x0 += sx;
            }
            if (e2 < dx) {
                err += dx;
                y0 += sy;
            }
        }
    }
    
    // function to spawn new element when clicked
    public void spawnElement(int mouseX, int mouseY) {

        // convert x and y to grid coords
        int gridX = mouseX / cellSize;
        int gridY = mouseY / cellSize;
        
        // account for brush size center
        int offset = brushSize / 2;

        // radius squared for the distance check
        double radiusSq = (brushSize * brushSize) / 4.0;

        // loop through the brush 
        for (int i = 0; i <= brushSize; i++) {
            for (int j = 0; j <= brushSize; j++) {

                int targetX = gridX - offset + i;
                int targetY = gridY - offset + j;

                // calculate distance from center
                double dx = targetX - gridX;
                double dy = targetY - gridY;

                // spawn only if the cell is within the circle's radius
                if (dx * dx + dy * dy <= radiusSq) {

                    // account for bounds
                    if (targetX >= 0 && targetX < cols && targetY >= 0 && targetY < rows) {
                        
                        // spawn boids
                        if (currentElement == Element.BOID){
                            boids.add(new Boid(targetX * cellSize, targetY * cellSize));
                        }
                        // spawn regular elements
                        else {
                            grid[targetX][targetY] = currentElement;
                        }

                    }
                }
            }
        }
    }   // end of spawn element function


    public static void setupFrame(){
        gamePanel = new Sandbox_Game();
        gamePanel.setPreferredSize(new Dimension(width, height));

        ToolBar customToolBar = new ToolBar();

        frame.setLayout(new BorderLayout());
        frame.add(gamePanel, BorderLayout.CENTER);
        
        frame.add(customToolBar, BorderLayout.SOUTH);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setResizable(true);
        frame.getContentPane().setBackground(Color.darkGray);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
    
    // MAIN
    public static void main(String[] args) {
        setupFrame();
    }
}