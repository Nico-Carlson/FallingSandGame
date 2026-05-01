/*
Final Project
Sandbox Game
Dev: Nico Carlson

inspiration: https://www.youtube.com/watch?v=5Ka3tbbT-9E
and : https://www.youtube.com/watch?v=VLZjd_Y1gJ8
i did my best not to copy anything from these (:
 */
package sandbox_game;

import java.awt.Color;
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
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

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
        this.setSize(width, height);
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

        // setup the grid
        grid = new Element[cols][rows];
        for (int c = 0; c < cols; c++) {
            for (int r = 0; r < rows; r++) {
                grid[c][r] = Element.EMPTY;
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

        // loop through from the BOTTOM to TOP
        for (int y = rows - 2; y >= 0; y--) {    // -2 to stay in bounds
            for (int x = 0; x < cols; x++) {

                // -------- check for sand --------
                if (grid[x][y] == Element.SAND) {

                    // RNG for left or right
                    int sandDirection = RNG.nextInt(2);

                    // first sand should try and fall straight down
                    if (grid[x][y + 1] == Element.EMPTY || 
                        grid[x][y + 1] == Element.WATER || 
                        grid[x][y + 1] == Element.LAVA) {
                        // set old pos to what is swapped with
                        Element oldPos = grid[x][y + 1];
                        grid[x][y] = oldPos;
                        // set new pos to sand
                        grid[x][y + 1] = Element.SAND;
                    }

                    else if (sandDirection == 0) {
                        // next sand should fall bottom left
                        if (x > 0 && (grid[x - 1][y + 1] == Element.EMPTY || 
                                      grid[x - 1][y + 1] == Element.WATER ||
                                      grid[x - 1][y + 1] == Element.LAVA)) {
                            // set old pos to what is swapped with
                            Element oldPos = grid[x - 1][y + 1];
                            grid[x][y] = oldPos;
                            // set new pos to sand
                            grid[x - 1][y + 1] = Element.SAND;
                        }
                    }

                    else if (sandDirection == 1) {
                        // next sand should fall bottom right
                        if (x < cols - 1 && (grid[x + 1][y + 1] == Element.EMPTY ||
                                             grid[x + 1][y + 1] == Element.WATER ||
                                             grid[x + 1][y + 1] == Element.LAVA)) {
                            // set old pos to what is swapped with
                            Element oldPos = grid[x + 1][y + 1];
                            grid[x][y] = oldPos;
                            // set new pos to sand
                            grid[x + 1][y + 1] = Element.SAND;
                        }
                    }
                }
                // -------- end of sand statments --------

                // -------- check for water --------
                if (grid[x][y] == Element.WATER) {

                    // 1 first water should try and fall straight down
                    if (grid[x][y + 1] == Element.EMPTY) {
                        // set old pos to empty
                        grid[x][y] = Element.EMPTY;
                        // set new pos to water
                        grid[x][y + 1] = Element.WATER;
                    }

                    // 2 next water should move bottom left
                    else if (x > 0 && grid[x - 1][y + 1] == Element.EMPTY) {
                        // set old pos to empty
                        grid[x][y] = Element.EMPTY;
                        // set new pos to water
                        grid[x - 1][y + 1] = Element.WATER;
                    }

                    // 3 next water should move bottom right
                    else if (x < cols - 1 && grid[x + 1][y + 1] == Element.EMPTY) {
                        // set old pos to empty
                        grid[x][y] = Element.EMPTY;
                        // set new pos to water
                        grid[x + 1][y + 1] = Element.WATER;
                    }
                    // 4 spread left or right (random)
                    else {

                        // if there is a water particle directly above pushing down
                        // this should fix the jittery top layer
//                        boolean underPressure = (y > 0 && grid[x][y - 1] == Element.WATER);
//                        if (underPressure) {
                        // set a horizontal flow rate to fix werid clumping
                        int flowRate = 5;

                        // RNG for left or right
                        int waterDirection = RNG.nextInt(2);

                        // flow left
                        if (waterDirection == 0) {
                            int targetX = x;

                            // look for consecutive open spaces to flow
                            while (targetX > 0 && grid[targetX - 1][y] == Element.EMPTY && flowRate > 0) {
                                targetX--;
                                flowRate--;
                            }

                            // if there was space then update the grid
                            if (targetX != x) {
                                grid[x][y] = Element.EMPTY;
                                grid[targetX][y] = Element.WATER;
                            }
                        }

                        // flow right
                        else {
                            int targetX = x;

                            // look for consecutive open spaces to flow
                            while (targetX < cols - 1 && grid[targetX + 1][y] == Element.EMPTY && flowRate > 0) {
                                targetX++;
                                flowRate--;
                            }

                            // if there was space then update the grid
                            if (targetX != x) {
                                grid[x][y] = Element.EMPTY;
                                grid[targetX][y] = Element.WATER;

                                // shift the main loop's index to the new position 
                                // to prevent the particle from being evaluated again this frame
                                x = targetX;
                            }
                        }
                    }
                }
                // -------- end of water statments --------

                // -------- check for lava --------
                if (grid[x][y] == Element.LAVA) {
                    
                    boolean reacted = false;

                    // 1 check four spots for water to make obsidian
                    if (y < rows-1 && grid[x][y+1] == Element.WATER){
                        grid[x][y+1] = Element.OBSIDIAN;
                        grid[x][y] = Element.STEAM;
                    }
                    else if (y > 0 && grid[x][y-1] == Element.WATER){
                        grid[x][y-1] = Element.STEAM;
                        grid[x][y] = Element.OBSIDIAN;
                    }
                    else if (x < cols-1 && grid[x+1][y] == Element.WATER){
                        grid[x+1][y] = Element.OBSIDIAN;
                        grid[x][y] = Element.STEAM;
                    }
                    else if (x > 0 && grid[x-1][y] == Element.WATER){
                        grid[x-1][y] = Element.STEAM;
                        grid[x][y] = Element.OBSIDIAN;
                    }
                    
                    // 2 lavashould try and fall straight down
                    else if (grid[x][y + 1] == Element.EMPTY) {
                        grid[x][y] = Element.EMPTY;
                        grid[x][y + 1] = Element.LAVA;
                    }

                    // 3 spread left or right (random)
                    else {
                        // set a horizontal flow rate to fix werid clumping
                        int flowRate = 1;

                        // RNG for left or right
                        int lavaDirection = RNG.nextInt(2);

                        // flow left
                        if (lavaDirection == 0) {
                            int targetX = x;

                            // look for consecutive open spaces to flow
                            while (targetX > 0 && grid[targetX - 1][y] == Element.EMPTY && flowRate > 0) {
                                targetX--;
                                flowRate--;
                            }

                            // if there was space then update the grid
                            if (targetX != x) {
                                grid[x][y] = Element.EMPTY;
                                grid[targetX][y] = Element.LAVA;
                            }
                        }

                        // flow right
                        else {
                            int targetX = x;

                            // look for consecutive open spaces to flow
                            while (targetX < cols - 1 && grid[targetX + 1][y] == Element.EMPTY && flowRate > 0) {
                                targetX++;
                                flowRate--;
                            }

                            // if there was space then update the grid
                            if (targetX != x) {
                                grid[x][y] = Element.EMPTY;
                                grid[targetX][y] = Element.LAVA;

                                x = targetX;
                            }
                        }
                    }

                }   // ------- end of lava -------
                
                // -------- check for seed --------
                if (grid[x][y] == Element.SEED) {
                    
                    // RNG for left or right
                    int seedDirection = RNG.nextInt(2);

                    // first seed should try and fall straight down
                    if (grid[x][y + 1] == Element.EMPTY) {
                        // set old pos to what is swapped with
                        Element oldPos = grid[x][y + 1];
                        grid[x][y] = oldPos;
                        // set new pos to seed
                        grid[x][y + 1] = Element.SEED;
                    }

                    else if (seedDirection == 0) {
                        // next seed should fall bottom left
                        if (x > 0 && grid[x - 1][y + 1] == Element.EMPTY) {
                            // set old pos to what is swapped with
                            Element oldPos = grid[x - 1][y + 1];
                            grid[x][y] = oldPos;
                            // set new pos to sand
                            grid[x - 1][y + 1] = Element.SEED;
                        }
                    }

                    else if (seedDirection == 1) {
                        // next seed should fall bottom right
                        if (x < cols - 1 && grid[x + 1][y + 1] == Element.EMPTY) {
                            // set old pos to what is swapped with
                            Element oldPos = grid[x + 1][y + 1];
                            grid[x][y] = oldPos;
                            // set new pos to seed
                            grid[x + 1][y + 1] = Element.SEED;
                        }
                    }
                    
                    // burn if it touches lava
                    if (y < rows-1 && y > 0 && x>0 && x<cols-1 &&
                                        (grid[x][y+1] == Element.LAVA ||
                                         grid[x][y-1] == Element.LAVA ||
                                         grid[x-1][y] == Element.LAVA ||
                                         grid[x+1][y] == Element.LAVA )){
                        
//                        grid[x][y-1] = Element.STEAM;
                        grid[x][y] = Element.STEAM;
                    }
                    
                    // if its touching water
                    else if (y < rows-1 && grid[x][y+1] == Element.WATER){
                        // consumes water
                        grid[x][y+1] = Element.EMPTY;
                        // creates plant
                        grid[x][y] = Element.PLANT;
                    }
                    else if (y > 0 &&grid[x][y-1] == Element.WATER){
                        grid[x][y-1] = Element.EMPTY;
                        grid[x][y] = Element.PLANT;
                    }
                    else if (x < cols-1 &&grid[x+1][y] == Element.WATER){
                        grid[x+1][y] = Element.EMPTY;
                        grid[x][y] = Element.PLANT;
                    }
                    else if (x > 0 &&grid[x-1][y] == Element.WATER){
                        grid[x-1][y] = Element.EMPTY;
                        grid[x][y] = Element.PLANT;
                    }                    
                }   // ------- end of seed -------
                
                // -------- check for plant --------
                if (grid[x][y] == Element.PLANT) {
                    
                    // burn if it touches lava
                    if (y < rows-1 && y > 0 && x>0 && x<cols-1 &&
                                        (grid[x][y+1] == Element.LAVA ||
                                         grid[x][y-1] == Element.LAVA ||
                                         grid[x-1][y] == Element.LAVA ||
                                         grid[x+1][y] == Element.LAVA )){
                        
//                        grid[x][y-1] = Element.STEAM;
                        grid[x][y] = Element.STEAM;
                    }
                    
                    int growDirection = RNG.nextInt(3);
                    int growChance = RNG.nextInt(50);
                        
                    if(growChance == 1 && x>0 && x<cols-1&&y>0&&y<rows-1){
                        if(growDirection == 0 && grid[x-1][y-1] == Element.EMPTY
                                              && (grid[x-1][y-1] == Element.EMPTY
                                              && grid[x+1][y-1] == Element.EMPTY)) {
                            grid[x-1][y-1] = Element.PLANT;   
                        }
                        else if(growDirection == 1 && grid[x+1][y-1] == Element.EMPTY
                                                   && (grid[x-1][y-1] == Element.EMPTY
                                                   && grid[x+1][y-1] == Element.EMPTY)) {
                            grid[x+1][y-1] = Element.PLANT;   
                        }
                        else if(growDirection == 2 && grid[x][y-1] == Element.EMPTY
                                                   && (grid[x-1][y-1] == Element.EMPTY
                                                   && grid[x+1][y-1] == Element.EMPTY)) {
                            grid[x][y-1] = Element.PLANT;   
                        }
                    }
                        
                    
                    
                }   // ------- end of seed -------

            }
        }   // end of grid loop
        
        // inverted check for thinks that rise instead of fall
        for (int y = 1; y < rows; y++) {    // start at 1 to stay in bounds for y-1
            for (int x = 0; x < cols; x++) {

                // -------- check for steam --------
                if (grid[x][y] == Element.STEAM) {
                    
                    // RNG for rising direction (0 = Left Bias, 1 = Right Bias, 2 = Straight Up)
                    int driftBias = RNG.nextInt(3);

                    // 1 up-left first
                    if (driftBias == 0 && x > 0 && 
                        (grid[x - 1][y - 1] == Element.EMPTY || 
                         grid[x - 1][y - 1] == Element.WATER || 
                         grid[x - 1][y - 1] == Element.LAVA)) {
                        
                        Element oldPos = grid[x - 1][y - 1];
                        grid[x][y] = oldPos;
                        grid[x - 1][y - 1] = Element.STEAM;
                    }
                    
                    // 2 up-right first
                    else if (driftBias == 1 && x < cols - 1 && 
                        (grid[x + 1][y - 1] == Element.EMPTY || 
                         grid[x + 1][y - 1] == Element.WATER || 
                         grid[x + 1][y - 1] == Element.LAVA)) {
                        
                        Element oldPos = grid[x + 1][y - 1];
                        grid[x][y] = oldPos;
                        grid[x + 1][y - 1] = Element.STEAM;
                    }
                    
                    // 3 straight up
                    else if (grid[x][y - 1] == Element.EMPTY || 
                             grid[x][y - 1] == Element.WATER || 
                             grid[x][y - 1] == Element.LAVA) {
                        
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
                            if (fade == 1){
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
                                x = targetX; // prevent loop evaluation duplication
                            }
                        }
                    }
                } // -------- end of steam statements --------
            }
        }
    }   // end of physics loop

    
    
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

        // make new panel
        JPanel toolBar = new JPanel();
        toolBar.setLayout(null);
        toolBar.setBounds(0, 700, 900, 215);
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
        JSlider speedButton = setupSliderButtons(16, 100, frameRate, "Speed", 1, toolBar);
        
        speedButton.addChangeListener(e -> {
            frameRate = speedButton.getValue();
            timer.setDelay(frameRate);
        });
        
        
        // -------- "standard" buttons --------
        // reset 
        JButton resetButton = setupStandardButtons("Reset", 4, toolBar);
        resetButton.addActionListener(e -> {
            for (int c = 0; c < cols; c++) {
                for (int r = 0; r < rows; r++) {
                    grid[c][r] = Element.EMPTY;
                }
            }

            frame.repaint();
        });

        // pause 
        JButton pauseButton = setupStandardButtons("Pause", 5, toolBar);
        pauseButton.addActionListener(e -> {
            isPaused = true;
        });

        // ----------- resume -----------
        JButton resumeButton = setupStandardButtons("Play", 6, toolBar);
        resumeButton.addActionListener(e -> {
            isPaused = false;
        });

        
        
        // add panel to frame
        frame.add(toolBar);

    }
    
    public static JButton setupStandardButtons(String title, int bttnpos, JPanel toolBar){
        
        JButton bttn = new JButton(title);
        
        // set location
        bttn.setBounds(bttnx + bttnpos * bttnSpacing + bttnpos * bttnWidth, bttny+2*bttnHeight + 2*bttnSpacing, bttnWidth, bttnHeight);
        
        bttn.setBackground(Color.gray);
        
        // add to panel
        toolBar.add(bttn);
        
        return bttn;
    }
    
    public static void setupToggleButtons(JToggleButton bttn, String title, int bttnpos, Element type,
                                            ButtonGroup elements, JPanel toolBar){
        
        bttn = new JToggleButton(title);
        
        // set location
        if(bttnpos < 7){
            bttn.setBounds(bttnx + bttnpos * bttnSpacing + bttnpos * bttnWidth, bttny, bttnWidth, bttnHeight);
        } else{
            bttnpos-=7;
            bttn.setBounds(bttnx + bttnpos * bttnSpacing + bttnpos * bttnWidth, bttny+bttnHeight + bttnSpacing, bttnWidth, bttnHeight);
        }
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
        
        // set location
        if(bttnpos < 7){
            bttn.setBounds(bttnx + bttnpos * bttnSpacing + bttnpos * bttnWidth, bttny+2*bttnHeight + 2*bttnSpacing, bttnWidth, bttnHeight);
        } else{
            bttnpos-=7;
            bttn.setBounds(bttnx + bttnpos * bttnSpacing + bttnpos * bttnWidth, bttny+bttnHeight + bttnSpacing, bttnWidth, bttnHeight);
        }
        
        bttn.setBackground(Color.gray);
        bttn.setToolTipText(title);
        
        // change listener
//        bttn.addChangeListener(CL);
                
        // add to panel
        toolBar.add(bttn);
        return bttn;
    }

    public static void setupFrame(){
        Sandbox_Game gamePanel = new Sandbox_Game();

        frame.setLayout(null);
        frame.add(gamePanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setBounds(0, 0, 900, 955);
        frame.getContentPane().setBackground(Color.darkGray);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
    
    // MAIN
    public static void main(String[] args) {
        setupFrame();
        setupToolBar();
    }
}