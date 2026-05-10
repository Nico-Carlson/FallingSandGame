/*
Class for Conways game of life
 */
package sandbox_game;

import javax.swing.JLabel;

public class Conway {

    public Conway() {
    }

    // find current state of each cell
    public void updateConway(int rows, int cols, Sandbox_Game.Element[][] currentArr, Sandbox_Game.Element[][] nextArr) {
        int num_alive;

        // loop through array
        for (int c = 0; c < cols; c++) {
            for (int r = 0; r < rows; r++) {
                // reset counter
                num_alive = 0;

                // loop through the 8 surrounding cells
                for (int y = -1; y < 2; y++) {
                    for (int x = -1; x < 2; x++) {
                        // count how many are alive (exclude center)
                        if (x == 0 && y == 0) {
                            continue;
                        }
                        if (r + x < 0 || r + x >= rows || c + y < 0 || c + y >= cols) {
                            continue;
                        }
                        if (currentArr[c + y][r + x] == Sandbox_Game.Element.CONWAY) {
                            num_alive++;
                        }

                    }
                }

                // run through the ruleset and setup nextArr
                // under/over populated = dead
                // Conway's Game of Life Ruleset
                if (currentArr[c][r] == Sandbox_Game.Element.CONWAY) { // If the cell is currently ALIVE
                    if (num_alive < 2 || num_alive > 3) {
                        nextArr[c][r] = Sandbox_Game.Element.EMPTY; // Dies of under/overpopulation
                    }
                    else {
                        nextArr[c][r] = Sandbox_Game.Element.CONWAY; // Survives
                    }
                }
                else if (currentArr[c][r] == Sandbox_Game.Element.EMPTY) { // If the cell is currently DEAD
                    if (num_alive == 3) {
                        nextArr[c][r] = Sandbox_Game.Element.CONWAY; // Reproduction!
                    }
                    else {
                        nextArr[c][r] = Sandbox_Game.Element.EMPTY; // Stays dead
                    }
                }
                else {
                    nextArr[c][r] = currentArr[c][r];
                }
            }

        }

        for (int c = 0; c < cols; c++) {
            for (int r = 0; r < rows; r++) {
                currentArr[c][r] = nextArr[c][r];
            }
        }
    }
}