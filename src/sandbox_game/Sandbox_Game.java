/*
Final Project
Sandbox Game
Dev: Nico Carlson

inspiration: https://www.youtube.com/watch?v=5Ka3tbbT-9E
i did my best not to COPY anything from this (:
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

public class Sandbox_Game extends JPanel {

    // main variable initializing
    public static int cellSize = 10;
    public static int width = 900;
    public static int height = 700;
    public static int cols = width / cellSize;
    public static int rows = height / cellSize;

    // array for the grid
    public static int[][] grid;

    // setting timer
    public static Timer timer;
    
    // pause state
    public static boolean isPaused = false;

    // setting rng
    public static Random RNG = new Random();

    // instantiating the frame
    public static JFrame frame = new JFrame("Sandbox Game");

    // mouse variables
    public boolean isMouseHeld = false;
    public int currentMouseX = 0;
    public int currentMouseY = 0;

    // brush size
    public static int brushSize = 1;

    // button variables
    public static int bttnx = 25;
    public static int bttny = 25;
    public static int bttnWidth = 75;
    public static int bttnHeight = 30;
    public static int bttnSpacing = 25;

    // selected element
    public static int currentElement = 1; // default for sand

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
        grid = new int[cols][rows];

        // setting the timer (16 is about 60 fps
        timer = new Timer(16, e -> {

            // if mouse is down spawn element
            if (isMouseHeld) {
                spawnElement(currentMouseX, currentMouseY);
            }
            
            // if not paused
            if (!isPaused){
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

                // 0 is empty cell, 1 is sand, 2 is water
                if (grid[x][y] == 0) {
                    // yellow for sand
                    g.setColor(Color.darkGray);

                    g.fillRect(x * cellSize, y * cellSize, cellSize, cellSize);

                }
                else if (grid[x][y] == 1) {
                    // yellow for sand
                    g.setColor(new Color(0xC2B280));

                    g.fillRect(x * cellSize, y * cellSize, cellSize, cellSize);

                }
                else if (grid[x][y] == 2) {
                    // blue for water
                    g.setColor(new Color(0, 191, 255));

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

                // check for sand
                if (grid[x][y] == 1) {

                    // RNG for left or right
                    int sandDirection = RNG.nextInt(2);

                    // first sand should try and fall straight down
                    if (grid[x][y + 1] == 0 || grid[x][y + 1] == 2) {
                        // set old pos to what is swapped with
                        int oldPos = grid[x][y + 1];
                        grid[x][y] = oldPos;
                        // set new pos to sand
                        grid[x][y + 1] = 1;
                    }

                    else if (sandDirection == 0) {
                        // next sand should fall bottom left
                        if (x > 0 && (grid[x - 1][y + 1] == 0 || grid[x - 1][y + 1] == 2)) {
                            // set old pos to what is swapped with
                            int oldPos = grid[x - 1][y + 1];
                            grid[x][y] = oldPos;
                            // set new pos to sand
                            grid[x - 1][y + 1] = 1;
                        }
                    }

                    else if (sandDirection == 1) {
                        // next sand should fall bottom right
                        if (x < cols - 1 && (grid[x + 1][y + 1] == 0 || grid[x + 1][y + 1] == 2)) {
                            // set old pos to what is swapped with
                            int oldPos = grid[x + 1][y + 1];
                            grid[x][y] = oldPos;
                            // set new pos to sand
                            grid[x + 1][y + 1] = 1;
                        }
                    }
                }   // end of sand statments     

                // check for water
                if (grid[x][y] == 2) {

                    // 1 first water should try and fall straight down
                    if (grid[x][y + 1] == 0) {
                        // set old pos to empty
                        grid[x][y] = 0;
                        // set new pos to water
                        grid[x][y + 1] = 2;
                    }

                    // 2 next water should move bottom left
                    else if (x > 0 && grid[x - 1][y + 1] == 0) {
                        // set old pos to empty
                        grid[x][y] = 0;
                        // set new pos to water
                        grid[x - 1][y + 1] = 2;
                    }

                    // 3 next water should move bottom right
                    else if (x < cols - 1 && grid[x + 1][y + 1] == 0) {
                        // set old pos to empty
                        grid[x][y] = 0;
                        // set new pos to water
                        grid[x + 1][y + 1] = 2;
                    }

                    // 4 spread left or right (random)
                    else {

                        // if there is a water particle directly above pushing down
                        // this should fix the jittery top layer
//                        boolean underPressure = (y > 0 && grid[x][y - 1] == 2);
//                        if (underPressure) {
                        // set a horizontal flow rate to fix werid clumping
                        int flowRate = 3;

                        // RNG for left or right
                        int waterDirection = RNG.nextInt(2);

                        // flow left
                        if (waterDirection == 0) {
                            int targetX = x;

                            // look for consecutive open spaces to flow
                            while (targetX > 0 && grid[targetX - 1][y] == 0 && flowRate > 0) {
                                targetX--;
                                flowRate--;
                            }

                            // if there was space then update the grid
                            if (targetX != x) {
                                grid[x][y] = 0;
                                grid[targetX][y] = 2;
                            }
                        }

                        // flow right
                        else {
                            int targetX = x;

                            // look for consecutive open spaces to flow
                            while (targetX < cols - 1 && grid[targetX + 1][y] == 0 && flowRate > 0) {
                                targetX++;
                                flowRate--;
                            }

                            // if there was space then update the grid
                            if (targetX != x) {
                                grid[x][y] = 0;
                                grid[targetX][y] = 2;

                                // shift the main loop's index to the new position 
                                // to prevent the particle from being evaluated again this frame
                                x = targetX;
                            }
                        }
//                        }
                    }

                }   // end of water statments     

            }
        }   // end of grid loop
    }   // end of physics loop

    // function to spawn new sand when clicked
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
        // ----------- size -----------
        JSlider sizeButton = new JSlider(1, 15, 1); // min, max, default
        sizeButton.setBounds(760, bttny, bttnWidth + 50, bttnHeight + 25);
        sizeButton.setBackground(Color.gray);
        sizeButton.setToolTipText("Size");

        sizeButton.addChangeListener(e -> {
            brushSize = sizeButton.getValue();
        });

        // ----------- sand -----------
        JToggleButton sandButton = new JToggleButton("Sand");
        sandButton.setBounds(bttnx, bttny, bttnWidth, bttnHeight);
        sandButton.setBackground(Color.gray);

        // change selected element
        sandButton.addActionListener(e -> currentElement = 1);

        elements.add(sandButton);

        // ----------- water -----------
        JToggleButton waterButton = new JToggleButton("Water");
        waterButton.setBounds(bttnx + bttnSpacing + bttnWidth, bttny, bttnWidth, bttnHeight);
        waterButton.setBackground(Color.gray);

        // change selected element
        waterButton.addActionListener(e -> currentElement = 2);

        elements.add(waterButton);

        // ----------- eraser -----------
        JToggleButton eraserButton = new JToggleButton("Eraser");
        eraserButton.setBounds(bttnx + 2 * bttnSpacing + 2 * bttnWidth, bttny, bttnWidth, bttnHeight);
        eraserButton.setBackground(Color.gray);

        // change selected element
        eraserButton.addActionListener(e -> currentElement = 0);

        elements.add(eraserButton);

        // ----------- reset -----------
        JButton resetButton = new JButton("Reset");
        resetButton.setBounds(bttnx + 3 * bttnSpacing + 3 * bttnWidth, bttny, bttnWidth, bttnHeight);
        resetButton.setBackground(Color.gray);

        // reset board
        resetButton.addActionListener(e -> {
            for (int c = 0; c < cols; c++) {
                for (int r = 0; r < rows; r++) {
                    grid[c][r] = 0;
                }
            }

            frame.repaint();
        });
        
        // ----------- pause -----------
        JButton pauseButton = new JButton("Pause");
        pauseButton.setBounds(bttnx + 4 * bttnSpacing + 4 * bttnWidth, bttny, bttnWidth, bttnHeight);
        pauseButton.setBackground(Color.gray);

        // pause board
        pauseButton.addActionListener(e -> {
            isPaused = true;
        });
        
        // ----------- resume -----------
        JButton resumeButton = new JButton("Play");
        resumeButton.setBounds(bttnx + 5 * bttnSpacing + 5 * bttnWidth, bttny, bttnWidth, bttnHeight);
        resumeButton.setBackground(Color.gray);

        // resume board
        resumeButton.addActionListener(e -> {
            isPaused = false;
        });

        // add buttons to panel
        toolBar.add(sandButton);
        toolBar.add(waterButton);
        toolBar.add(eraserButton);
        toolBar.add(sizeButton);
        toolBar.add(resetButton);
        toolBar.add(pauseButton);
        toolBar.add(resumeButton);

        // add panel to frame
        frame.add(toolBar);

    }

    // MAIN
    public static void main(String[] args) {

        Sandbox_Game gamePanel = new Sandbox_Game();

        frame.setLayout(null);
        frame.add(gamePanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setBounds(0, 0, 900, 955);
        frame.getContentPane().setBackground(Color.darkGray);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        setupToolBar();
    }

}
