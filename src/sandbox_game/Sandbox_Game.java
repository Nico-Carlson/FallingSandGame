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


// --------- Main Class -----------
public class Sandbox_Game extends JPanel {

    public enum Element {
        EMPTY, SAND, WATER, LAVA, OBSIDIAN, STEAM, SEED, PLANT
    }

    // main variable initializing
    public static int cellSize = 5;
    public static int width = 900;
    public static int height = 700;
    public static int cols = width / cellSize;
    public static int rows = height / cellSize;

    // array for the grid
    public static Element[][] grid;
    
    // array to save / load 
    public static Element[][] savedGrid;
    
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
    public int currentMouseX = 0;
    public int currentMouseY = 0;

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
    
    // set up the physics engine class
    public PhysicsEngine engine;

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
        
        
        // listen for window resize events
        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                
                // calculate new grid dimensions based on new panel size
                int newCols = getWidth() / cellSize;
                int newRows = getHeight() / cellSize;
                
                // prevent crash if window is minimized entirely
                if (newCols <= 0 || newRows <= 0) return;

                // create temporary arrays with the new dimensions
                Element[][] newGrid = new Element[newCols][newRows];
                Element[][] newSavedGrid = new Element[newCols][newRows];

                // loop through the new array dimensions
                for (int x = 0; x < newCols; x++) {
                    for (int y = 0; y < newRows; y++) {
                        
                        // if the coordinate exists in the old grid, copy the element over
                        if (x < cols && y < rows && grid[x][y] != null) {
                            newGrid[x][y] = grid[x][y];
                            newSavedGrid[x][y] = savedGrid[x][y];
                        } 
                        // if it is new empty space created by stretching the window, set to EMPTY
                        else {
                            newGrid[x][y] = Element.EMPTY;
                            newSavedGrid[x][y] = Element.EMPTY;
                        }
                    }
                }

                // safely overwrite the old arrays and dimension variables
                grid = newGrid;
                savedGrid = newSavedGrid;
                cols = newCols;
                rows = newRows;
            }
        });
        
        // set up the physics engine
        engine = new PhysicsEngine(cols, rows);
        

        // setup the grid
        grid = new Element[cols][rows];
        for (int c = 0; c < cols; c++) {
            for (int r = 0; r < rows; r++) {
                grid[c][r] = Element.EMPTY;
            }
        }
        
        // setup the savedGrid
        savedGrid = new Element[cols][rows];
        for (int c = 0; c < cols; c++) {
            for (int r = 0; r < rows; r++) {
                savedGrid[c][r] = Element.EMPTY;
            }
        }

        // setting the timer (16 is about 60 fps
        timer = new Timer(frameRate, e -> {

            // if mouse is down spawn element
            if (isMouseHeld) {
                spawnElement(currentMouseX, currentMouseY);
            }

            // if not paused
            if (!isPaused) {
                engine.updatePhysics();
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
                        // red for water
                        g.setColor(new Color(0xE42217));
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
                    default -> {
                    }
                }

            }
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

                        // spawn sand
                        grid[targetX][targetY] = currentElement;

                    }
                }
            }
        }
    }   // end of spawn element function


    public static void setupFrame(){
        Sandbox_Game gamePanel = new Sandbox_Game();
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