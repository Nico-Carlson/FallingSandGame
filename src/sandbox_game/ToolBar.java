/*
Class for the toolbar
*/


package sandbox_game;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JToggleButton;
import javax.swing.border.TitledBorder;
import static sandbox_game.Sandbox_Game.brushSize;
import static sandbox_game.Sandbox_Game.bttnHeight;
import static sandbox_game.Sandbox_Game.bttnWidth;
import static sandbox_game.Sandbox_Game.cols;
import static sandbox_game.Sandbox_Game.currentElement;
import static sandbox_game.Sandbox_Game.frame;
import static sandbox_game.Sandbox_Game.frameRate;
import static sandbox_game.Sandbox_Game.grid;
import static sandbox_game.Sandbox_Game.isPaused;
import static sandbox_game.Sandbox_Game.rows;
import static sandbox_game.Sandbox_Game.savedGrid;
import static sandbox_game.Sandbox_Game.timer;

public class ToolBar extends JPanel {

    // to sync with wheel listener
    public static JSlider zoomButton; 
    
    public ToolBar() {
        
        // setup the panel
        this.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));
        this.setPreferredSize(new Dimension(Sandbox_Game.width, 215));
        this.setBackground(new Color(40, 40, 40));
        this.setBorder(BorderFactory.createLineBorder(Color.black, 5));

        ButtonGroup elements = new ButtonGroup();

        // instantiate buttons
        // -------- toggle buttons --------
        
        // sand 
        JToggleButton sandButton = new JToggleButton();
        setupToggleButtons(sandButton, "Sand", Sandbox_Game.Element.SAND, elements);

        // water 
        JToggleButton waterButton = new JToggleButton();
        setupToggleButtons(waterButton, "Water", Sandbox_Game.Element.WATER, elements);

        // lava 
        JToggleButton lavaButton = new JToggleButton();
        setupToggleButtons(lavaButton, "Lava", Sandbox_Game.Element.LAVA, elements);

        // obsidian 
        JToggleButton obsidianButton = new JToggleButton();
        setupToggleButtons(obsidianButton, "Obsidian", Sandbox_Game.Element.OBSIDIAN, elements);
        
        // oil 
        JToggleButton oilButton = new JToggleButton();
        setupToggleButtons(oilButton, "Oil", Sandbox_Game.Element.OIL, elements);

        // seed 
        JToggleButton seedButton = new JToggleButton();
        setupToggleButtons(seedButton, "Seed", Sandbox_Game.Element.SEED, elements);

        // plant 
        JToggleButton plantButton = new JToggleButton();
        setupToggleButtons(plantButton, "Plant", Sandbox_Game.Element.PLANT, elements);
        
        // steam 
        JToggleButton steamButton = new JToggleButton();
        setupToggleButtons(steamButton, "Steam", Sandbox_Game.Element.STEAM, elements);
        
        // fire 
        JToggleButton fireButton = new JToggleButton();
        setupToggleButtons(fireButton, "Fire", Sandbox_Game.Element.FIRE, elements);
        
        // smoke 
        JToggleButton smokeButton = new JToggleButton();
        setupToggleButtons(smokeButton, "Smoke", Sandbox_Game.Element.SMOKE, elements);
        
        // boid
        JToggleButton boidButton = new JToggleButton();
        setupToggleButtons(boidButton, "Boid", Sandbox_Game.Element.BOID, elements);
        
        // conway
        JToggleButton conwayButton = new JToggleButton();
        setupToggleButtons(conwayButton, "Conway", Sandbox_Game.Element.CONWAY, elements);

        // eraser 
        JToggleButton eraserButton = new JToggleButton();
        setupToggleButtons(eraserButton, "Eraser", Sandbox_Game.Element.EMPTY, elements);

        
        // -------- slider buttons --------
        // size 
        JSlider sizeButton = setupSliderButtons(1, 25, 5, "Size", 0);
        
        sizeButton.addChangeListener(e -> {
            brushSize = sizeButton.getValue();
        });
        
        // speed 
        JSlider speedButton = setupSliderButtons(1, 30, 16, "Speed", 1);
        
        speedButton.addChangeListener(e -> {
            frameRate = speedButton.getValue();
            timer.setDelay(frameRate);
        });
        
        // "zoom"
        zoomButton = setupSliderButtons(2, 25, Sandbox_Game.cellSize, "Zoom", 2);
        
        zoomButton.addChangeListener(e -> {
            // Ensure the panel is initialized before attempting to resize
            if (Sandbox_Game.gamePanel != null) {
                Sandbox_Game.gamePanel.resizeGrid(zoomButton.getValue(), true);
            }
        });
        
        
        // -------- "standard" buttons --------
        // reset 
        JButton resetButton = setupStandardButtons("Reset");
        resetButton.addActionListener(e -> {
            for (int c = 0; c < cols; c++) {
                for (int r = 0; r < rows; r++) {
                    grid[c][r] = Sandbox_Game.Element.EMPTY;
                }
            }
            Sandbox_Game.boids.clear();
            frame.repaint();
        });

        // pause 
        JButton pauseButton = setupStandardButtons("Pause");
        pauseButton.addActionListener(e -> {
            isPaused = true;
        });

        // ----------- resume -----------
        JButton resumeButton = setupStandardButtons("Play");
        resumeButton.addActionListener(e -> {
            isPaused = false;
        });
        
        // ----------- save -----------
        JButton saveButton = setupStandardButtons("Save");
        saveButton.addActionListener(e -> {
            for (int c = 0; c < cols; c++) {
                for (int r = 0; r < rows; r++) {
                    savedGrid[c][r] = grid[c][r];
                }
            }
        });
        
        // ----------- load -----------
        JButton loadButton = setupStandardButtons("Load");
        loadButton.addActionListener(e -> {
            for (int c = 0; c < cols; c++) {
                for (int r = 0; r < rows; r++) {
                    grid[c][r] = savedGrid[c][r];
                }
            }
        }); 
    }

    // --- Helper Methods ---

    public JButton setupStandardButtons(String title){
        JButton bttn = new JButton(title);
        bttn.setPreferredSize(new Dimension(bttnWidth, bttnHeight));
        bttn.setBackground(Color.gray);
        
        // Add to this specific ToolBar instance
        this.add(bttn);
        
        return bttn;
    }
    
    public void setupToggleButtons(JToggleButton bttn, String title, Sandbox_Game.Element type, ButtonGroup elements){
        bttn = new JToggleButton(title);
        bttn.setPreferredSize(new Dimension(bttnWidth, bttnHeight));
        bttn.setBackground(Color.gray);

        bttn.addActionListener(e -> currentElement = type);
        elements.add(bttn);
        
        // Add to this specific ToolBar instance
        this.add(bttn);
    }
    
    public JSlider setupSliderButtons(int min, int max, int d, String title, int bttnpos){
        JSlider bttn = new JSlider(min,max,d);
        bttn.setPreferredSize(new Dimension(bttnWidth, bttnHeight));
        bttn.setBackground(Color.gray);
        bttn.setToolTipText(title);
        
        TitledBorder titledBorder = BorderFactory.createTitledBorder(title);
        titledBorder.setTitlePosition(TitledBorder.ABOVE_TOP);
        bttn.setBorder(titledBorder);
                
        // Add to this specific ToolBar instance
        this.add(bttn);
        return bttn;
    }
}