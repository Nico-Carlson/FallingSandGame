/*
Final Project
Sandbox Game
Dev: Nico Carlson

inspiration: https://www.youtube.com/watch?v=5Ka3tbbT-9E
and : https://www.youtube.com/watch?v=VLZjd_Y1gJ8
i did my best not to copy anything from these (:
 */
package sandbox_game;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Random;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JToggleButton;
import javax.swing.Timer;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

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
                updatePhysics();
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

                // --------- EMPTY ---------
                if (grid[x][y] == Element.EMPTY) {
                    g.setColor(Color.darkGray);

                    g.fillRect(x * cellSize, y * cellSize, cellSize, cellSize);

                }
                // --------- SAND --------- 
                else if (grid[x][y] == Element.SAND) {
                    // yellow for sand
                    g.setColor(new Color(0xC2B280));

                    g.fillRect(x * cellSize, y * cellSize, cellSize, cellSize);

                }
                // --------- WATER --------- 
                else if (grid[x][y] == Element.WATER) {
                    // blue for water
                    g.setColor(new Color(0, 191, 255));

                    g.fillRect(x * cellSize, y * cellSize, cellSize, cellSize);

                }
                // --------- LAVA --------- 
                else if (grid[x][y] == Element.LAVA) {
                    // red for water
                    g.setColor(new Color(0xE42217));

                    g.fillRect(x * cellSize, y * cellSize, cellSize, cellSize);

                }
                // --------- OBSIDIAN --------- 
                else if (grid[x][y] == Element.OBSIDIAN) {
                    g.setColor(new Color(0x382B46));

                    g.fillRect(x * cellSize, y * cellSize, cellSize, cellSize);

                }
                // --------- STEAM --------- 
                else if (grid[x][y] == Element.STEAM) {
                    g.setColor(new Color(0x9E9E9E));

                    g.fillRect(x * cellSize, y * cellSize, cellSize, cellSize);

                }
                // --------- SEED --------- 
                else if (grid[x][y] == Element.SEED) {
                    g.setColor(Color.green.darker());

                    g.fillRect(x * cellSize, y * cellSize, cellSize, cellSize);

                }
                // --------- PLANT --------- 
                else if (grid[x][y] == Element.PLANT) {
                    g.setColor(Color.green.darker().darker());

                    g.fillRect(x * cellSize, y * cellSize, cellSize, cellSize);

                }

            }
        }

        // highlight cursor
        int centerGridX = currentMouseX / cellSize;
        int centerGridY = currentMouseY / cellSize;
        int offset = brushSize / 2;

        // set highlight color
        g.setColor(Color.WHITE);

        // draw a hollow rectangle outlining the active brush area
        g.drawOval((centerGridX - offset) * cellSize,
                (centerGridY - offset) * cellSize,
                brushSize * cellSize,
                brushSize * cellSize);

    }   // end of paint compenent

    
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
        if (grid[x][y] == Element.SAND) {
            int sandDirection = RNG.nextInt(2);

            // first sand should try and fall straight down
            if (y < rows - 1 && (grid[x][y + 1] == Element.EMPTY || 
                                 grid[x][y + 1] == Element.WATER || 
                                 grid[x][y + 1] == Element.LAVA)) {
                Element oldPos = grid[x][y + 1];
                grid[x][y] = oldPos;
                grid[x][y + 1] = Element.SAND;
            }
            // next sand should fall bottom left
            else if (sandDirection == 0) {
                if (y < rows - 1 && x > 0 && (grid[x - 1][y + 1] == Element.EMPTY || 
                                              grid[x - 1][y + 1] == Element.WATER ||
                                              grid[x - 1][y + 1] == Element.LAVA)) {
                    Element oldPos = grid[x - 1][y + 1];
                    grid[x][y] = oldPos;
                    grid[x - 1][y + 1] = Element.SAND;
                }
            }
            // next sand should fall bottom right
            else if (sandDirection == 1) {
                if (y < rows - 1 && x < cols - 1 && (grid[x + 1][y + 1] == Element.EMPTY ||
                                                     grid[x + 1][y + 1] == Element.WATER ||
                                                     grid[x + 1][y + 1] == Element.LAVA)) {
                    Element oldPos = grid[x + 1][y + 1];
                    grid[x][y] = oldPos;
                    grid[x + 1][y + 1] = Element.SAND;
                }
            }
        }
        
    }
    // water physics
    public void waterPhysics(int x, int y){
        
        // -------- check for water --------
        if (grid[x][y] == Element.WATER) {

            // 1 first water should try and fall straight down
            if (y < rows - 1 && grid[x][y + 1] == Element.EMPTY) {
                grid[x][y] = Element.EMPTY;
                grid[x][y + 1] = Element.WATER;
            }
            // 2 next water should move bottom left
            else if (y < rows - 1 && x > 0 && grid[x - 1][y + 1] == Element.EMPTY) {
                grid[x][y] = Element.EMPTY;
                grid[x - 1][y + 1] = Element.WATER;
            }
            // 3 next water should move bottom right
            else if (y < rows - 1 && x < cols - 1 && grid[x + 1][y + 1] == Element.EMPTY) {
                grid[x][y] = Element.EMPTY;
                grid[x + 1][y + 1] = Element.WATER;
            }
            // 4 spread left or right 
            else {
                int flowRate = 4;
                int waterDirection = RNG.nextInt(2);

                // flow left
                if (waterDirection == 0) {
                    int targetX = x;
                    while (targetX > 0 && grid[targetX - 1][y] == Element.EMPTY && flowRate > 0) {
                        targetX--;
                        flowRate--;
                    }
                    if (targetX != x) {
                        grid[x][y] = Element.EMPTY;
                        grid[targetX][y] = Element.WATER;
                    }
                }
                // flow right
                else {
                    int targetX = x;
                    while (targetX < cols - 1 && grid[targetX + 1][y] == Element.EMPTY && flowRate > 0) {
                        targetX++;
                        flowRate--;
                    }
                    if (targetX != x) {
                        grid[x][y] = Element.EMPTY;
                        grid[targetX][y] = Element.WATER;
                        x = targetX; 
                    }
                }
            }
        }
        
    }
    // lava physics
    public void lavaPhysics(int x, int y){
        
        // -------- check for lava --------
        if (grid[x][y] == Element.LAVA) {

            // 1 check four spots for water to make obsidian
            if (y < rows - 1 && grid[x][y + 1] == Element.WATER) {
                grid[x][y + 1] = Element.OBSIDIAN;
                grid[x][y] = Element.STEAM;
            } 
            else if (y > 0 && grid[x][y - 1] == Element.WATER) {
                grid[x][y - 1] = Element.STEAM;
                grid[x][y] = Element.OBSIDIAN;
            } 
            else if (x < cols - 1 && grid[x + 1][y] == Element.WATER) {
                grid[x + 1][y] = Element.OBSIDIAN;
                grid[x][y] = Element.STEAM;
            } 
            else if (x > 0 && grid[x - 1][y] == Element.WATER) {
                grid[x - 1][y] = Element.STEAM;
                grid[x][y] = Element.OBSIDIAN;
            }
            // 2 lava should try and fall straight down
            else if (y < rows - 1 && grid[x][y + 1] == Element.EMPTY) {
                grid[x][y] = Element.EMPTY;
                grid[x][y + 1] = Element.LAVA;
            }
            // 3 spread left or right
            else {
                int flowRate = 1;
                int lavaDirection = RNG.nextInt(2);

                if (lavaDirection == 0) {
                    int targetX = x;
                    while (targetX > 0 && grid[targetX - 1][y] == Element.EMPTY && flowRate > 0) {
                        targetX--;
                        flowRate--;
                    }
                    if (targetX != x) {
                        grid[x][y] = Element.EMPTY;
                        grid[targetX][y] = Element.LAVA;
                    }
                } else {
                    int targetX = x;
                    while (targetX < cols - 1 && grid[targetX + 1][y] == Element.EMPTY && flowRate > 0) {
                        targetX++;
                        flowRate--;
                    }
                    if (targetX != x) {
                        grid[x][y] = Element.EMPTY;
                        grid[targetX][y] = Element.LAVA;
                        x = targetX;
                    }
                }
            }
        }
        
    }
    // seed physics
    public void seedPhysics(int x, int y){
        
        // -------- check for seed --------
        if (grid[x][y] == Element.SEED) {
            int seedDirection = RNG.nextInt(2);

            // first seed should try and fall straight down
            if (y < rows - 1 && grid[x][y + 1] == Element.EMPTY) {
                Element oldPos = grid[x][y + 1];
                grid[x][y] = oldPos;
                grid[x][y + 1] = Element.SEED;
            }
            else if (seedDirection == 0) {
                if (y < rows - 1 && x > 0 && grid[x - 1][y + 1] == Element.EMPTY) {
                    Element oldPos = grid[x - 1][y + 1];
                    grid[x][y] = oldPos;
                    grid[x - 1][y + 1] = Element.SEED;
                }
            }
            else if (seedDirection == 1) {
                if (y < rows - 1 && x < cols - 1 && grid[x + 1][y + 1] == Element.EMPTY) {
                    Element oldPos = grid[x + 1][y + 1];
                    grid[x][y] = oldPos;
                    grid[x + 1][y + 1] = Element.SEED;
                }
            }

            // burn if it touches lava
            if (y < rows - 1 && y > 0 && x > 0 && x < cols - 1 &&
                (grid[x][y + 1] == Element.LAVA ||
                 grid[x][y - 1] == Element.LAVA ||
                 grid[x - 1][y] == Element.LAVA ||
                 grid[x + 1][y] == Element.LAVA)) {

                grid[x][y] = Element.STEAM;
            }
            // if its touching water
            else if (y < rows - 1 && grid[x][y + 1] == Element.WATER) {
                grid[x][y + 1] = Element.EMPTY;
                grid[x][y] = Element.PLANT;
            } 
            else if (y > 0 && grid[x][y - 1] == Element.WATER) {
                grid[x][y - 1] = Element.EMPTY;
                grid[x][y] = Element.PLANT;
            } 
            else if (x < cols - 1 && grid[x + 1][y] == Element.WATER) {
                grid[x + 1][y] = Element.EMPTY;
                grid[x][y] = Element.PLANT;
            } 
            else if (x > 0 && grid[x - 1][y] == Element.WATER) {
                grid[x - 1][y] = Element.EMPTY;
                grid[x][y] = Element.PLANT;
            }
        }
        
    }
    // plant physics
    public void plantPhysics(int x, int y){
        
        // -------- check for plant --------
        if (grid[x][y] == Element.PLANT) {

            // burn if it touches lava
            if (y < rows - 1 && y > 0 && x > 0 && x < cols - 1 &&
                (grid[x][y + 1] == Element.LAVA ||
                 grid[x][y - 1] == Element.LAVA ||
                 grid[x - 1][y] == Element.LAVA ||
                 grid[x + 1][y] == Element.LAVA)) {

                grid[x][y] = Element.STEAM;
            }

            int growDirection = RNG.nextInt(3);
            int growChance = RNG.nextInt(50);

            if (growChance == 1 && x > 0 && x < cols - 1 && y > 0 && y < rows - 1) {
                if (growDirection == 0 && grid[x - 1][y - 1] == Element.EMPTY && 
                    (grid[x - 1][y - 1] == Element.EMPTY && grid[x + 1][y - 1] == Element.EMPTY)) {
                    grid[x - 1][y - 1] = Element.PLANT;   
                } 
                else if (growDirection == 1 && grid[x + 1][y - 1] == Element.EMPTY && 
                         (grid[x - 1][y - 1] == Element.EMPTY && grid[x + 1][y - 1] == Element.EMPTY)) {
                    grid[x + 1][y - 1] = Element.PLANT;   
                } 
                else if (growDirection == 2 && grid[x][y - 1] == Element.EMPTY && 
                         (grid[x - 1][y - 1] == Element.EMPTY && grid[x + 1][y - 1] == Element.EMPTY)) {
                    grid[x][y - 1] = Element.PLANT;   
                }
            }
        }
        
    }
    // steam physics
    public void steamPhysics(int x, int y){
        
        // -------- check for steam --------
        if (grid[x][y] == Element.STEAM) {
            int driftBias = RNG.nextInt(3);

            // 1 up-left first
            if (driftBias == 0 && y > 0 && x > 0 && 
                (grid[x - 1][y - 1] == Element.EMPTY || 
                 grid[x - 1][y - 1] == Element.WATER || 
                 grid[x - 1][y - 1] == Element.LAVA)) {

                Element oldPos = grid[x - 1][y - 1];
                grid[x][y] = oldPos;
                grid[x - 1][y - 1] = Element.STEAM;
            }
            // 2 up-right first
            else if (driftBias == 1 && y > 0 && x < cols - 1 && 
                     (grid[x + 1][y - 1] == Element.EMPTY || 
                      grid[x + 1][y - 1] == Element.WATER || 
                      grid[x + 1][y - 1] == Element.LAVA)) {

                Element oldPos = grid[x + 1][y - 1];
                grid[x][y] = oldPos;
                grid[x + 1][y - 1] = Element.STEAM;
            }
            // 3 straight up
            else if (y > 0 && (grid[x][y - 1] == Element.EMPTY || 
                               grid[x][y - 1] == Element.WATER || 
                               grid[x][y - 1] == Element.LAVA)) {

                Element oldPos = grid[x][y - 1];
                grid[x][y] = oldPos;
                grid[x][y - 1] = Element.STEAM;
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
                        grid[x][y] = Element.EMPTY;
                    }

                    int targetX = x;
                    while (targetX > 0 && grid[targetX - 1][y] == Element.EMPTY && flowRate > 0) {
                        targetX--;
                        flowRate--;
                    }
                    if (targetX != x) {
                        grid[x][y] = Element.EMPTY;
                        grid[targetX][y] = Element.STEAM;
                    }
                }
                // flow right
                else {
                    int targetX = x;
                    while (targetX < cols - 1 && grid[targetX + 1][y] == Element.EMPTY && flowRate > 0) {
                        targetX++;
                        flowRate--;
                    }
                    if (targetX != x) {
                        grid[x][y] = Element.EMPTY;
                        grid[targetX][y] = Element.STEAM;
                        x = targetX; 
                    }
                }
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

        // loop through the brush to highlight cursor area
        for (int i = 0; i < brushSize; i++) {
            for (int j = 0; j < brushSize; j++) {

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

    
    
    // setting up the toolbar
    public static void setupToolBar() {
        toolBar.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));
        toolBar.setPreferredSize(new Dimension(width, 215));
        toolBar.setBackground(new Color(40, 40, 40));
        toolBar.setBorder(BorderFactory.createLineBorder(Color.black, 5));
        toolBar.setVisible(true);

        // set up bttn group
        ButtonGroup elements = new ButtonGroup();

        
        // set up the buttons
    
        // -------- toggle buttons --------
        
        // sand 
        JToggleButton sandButton = new JToggleButton();
        setupToggleButtons(sandButton, "Sand", 0, Element.SAND, elements, toolBar);

        // water 
        JToggleButton waterButton = new JToggleButton();
        setupToggleButtons(waterButton, "Water", 1, Element.WATER, elements, toolBar);

        // lava 
        JToggleButton lavaButton = new JToggleButton();
        setupToggleButtons(lavaButton, "Lava", 2, Element.LAVA, elements, toolBar);

        // obsidian 
        JToggleButton obsidianButton = new JToggleButton();
        setupToggleButtons(obsidianButton, "Obsidian", 3, Element.OBSIDIAN, elements, toolBar);

        // seed 
        JToggleButton seedButton = new JToggleButton();
        setupToggleButtons(seedButton, "Seed", 4, Element.SEED, elements, toolBar);

        // plant 
        JToggleButton plantButton = new JToggleButton();
        setupToggleButtons(plantButton, "Plant", 5, Element.PLANT, elements, toolBar);
        
        // steam 
        JToggleButton steamButton = new JToggleButton();
        setupToggleButtons(steamButton, "Steam", 6, Element.STEAM, elements, toolBar);

        // eraser 
        JToggleButton eraserButton = new JToggleButton();
        setupToggleButtons(eraserButton, "Eraser", 7, Element.EMPTY, elements, toolBar);

        
        // -------- slider buttons --------
        // size 
        JSlider sizeButton = setupSliderButtons(1, 15, 5, "Size", 0, toolBar);
        
        sizeButton.addChangeListener(e -> {
            brushSize = sizeButton.getValue();
        });
        
        // speed 
        JSlider speedButton = setupSliderButtons(1, 30, 16, "Speed", 1, toolBar);
        
        speedButton.addChangeListener(e -> {
            frameRate = speedButton.getValue();
            timer.setDelay(frameRate);
        });
        
        
        // -------- "standard" buttons --------
        // reset 
        JButton resetButton = setupStandardButtons("Reset", toolBar);
        resetButton.addActionListener(e -> {
            for (int c = 0; c < cols; c++) {
                for (int r = 0; r < rows; r++) {
                    grid[c][r] = Element.EMPTY;
                }
            }

            frame.repaint();
        });

        // pause 
        JButton pauseButton = setupStandardButtons("Pause", toolBar);
        pauseButton.addActionListener(e -> {
            isPaused = true;
        });

        // ----------- resume -----------
        JButton resumeButton = setupStandardButtons("Play", toolBar);
        resumeButton.addActionListener(e -> {
            isPaused = false;
        });
        
        // ----------- save -----------
        JButton saveButton = setupStandardButtons("Save", toolBar);
        saveButton.addActionListener(e -> {
            for (int c = 0; c < cols; c++) {
                for (int r = 0; r < rows; r++) {
                    savedGrid[c][r] = grid[c][r];
                }
            }
        });
        
        // ----------- load -----------
        JButton loadButton = setupStandardButtons("Load", toolBar);
        loadButton.addActionListener(e -> {
            for (int c = 0; c < cols; c++) {
                for (int r = 0; r < rows; r++) {
                    grid[c][r] = savedGrid[c][r];
                }
            }
        }); 
       

    }
    
    public static JButton setupStandardButtons(String title, JPanel toolBar){
        
        JButton bttn = new JButton(title);
        bttn.setPreferredSize(new Dimension(bttnWidth, bttnHeight));
        
        bttn.setBackground(Color.gray);
        
        // add to panel
        toolBar.add(bttn);
        
        return bttn;
    }
    
    public static void setupToggleButtons(JToggleButton bttn, String title, int bttnpos, Element type,
                                            ButtonGroup elements, JPanel toolBar){
        
        bttn = new JToggleButton(title);
        bttn.setPreferredSize(new Dimension(bttnWidth, bttnHeight));
        bttn.setBackground(Color.gray);

        // change selected element
        bttn.addActionListener(e -> currentElement = type);

        // add to button group
        elements.add(bttn);
        
        // add to panel
        toolBar.add(bttn);
    }
    
    public static JSlider setupSliderButtons(int min, int max, int d, 
                        String title, int bttnpos, JPanel toolBar){
        
        JSlider bttn = new JSlider(min,max,d);
        bttn.setPreferredSize(new Dimension(bttnWidth, bttnHeight));
        
        bttn.setBackground(Color.gray);
        bttn.setToolTipText(title);
        
        // add a TitledBorder directly to the slider
        TitledBorder titledBorder = BorderFactory.createTitledBorder(title);
        titledBorder.setTitlePosition(TitledBorder.ABOVE_TOP); // Places title above the slider
        bttn.setBorder(titledBorder);
                
        // add to panel
        toolBar.add(bttn);
        return bttn;
    }

    public static void setupFrame(){
        Sandbox_Game gamePanel = new Sandbox_Game();
        gamePanel.setPreferredSize(new Dimension(width, height));

        setupToolBar();

        frame.setLayout(new BorderLayout());
        frame.add(gamePanel, BorderLayout.CENTER);
        frame.add(toolBar, BorderLayout.SOUTH);

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