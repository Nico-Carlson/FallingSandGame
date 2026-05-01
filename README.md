# Falling Sand Game

A simple falling sand cellular automaton game built in Java. This interactive sandbox simulation lets you paint various elements that behave according to realistic physics rules.

## Features

🎮 **Interactive Simulation**
- Real-time physics engine running at ~60 FPS
- Multiple interactive elements with unique behaviors
- Adjustable brush size for precision or bulk placement
- Pause/play controls to examine your creations

### Elements

| Element | Behavior | Color |
|---------|----------|-------|
| **Sand** | Falls downward, spreads when blocked | Tan |
| **Water** | Flows downward and spreads horizontally when obstructed | Cyan |
| **Lava** | Flows slowly, reacts with water to create Obsidian | Red |
| **Obsidian** | Created when Lava meets Water; immobile barrier | Purple |
| **Steam** | Rises upward; created from Lava-Water reactions | Gray |
| **Seed** | Falls like sand but turns into plant if it touches water and burns if it touches lava | Green |
| **Plant** | Grows "upward" and burns if it touches lava | Green |
| **Eraser** | Removes elements from the grid | — |

## How to Use

### Running the Game

Compile and run the main class:

```bash
javac src/sandbox_game/Sandbox_Game.java
java -cp src sandbox_game.Sandbox_Game
```

Or use the provided `build.xml` with Apache Ant:

```bash
ant build
ant run
```

### Controls

- **Mouse Drag**: Paint elements on the canvas
- **Element Buttons** (Top toolbar): Select which element to paint
- **Size Slider**: Adjust brush size (1-15)
- **Reset**: Clear the entire grid
- **Pause/Play**: Stop or resume physics simulation

## Physics System

The simulation uses a sophisticated update loop that processes particles in specific orders:

- **Gravity-based elements** (Sand, Water, Lava): Updated bottom-to-top to allow proper falling
- **Rising elements** (Steam): Updated top-to-bottom to enable upward movement
- **Interactions**: Lava + Water → Obsidian + Steam (chemical reactions)
- **Spreading**: Liquids (Water, Lava) spread horizontally when blocked vertically

## Project Structure

```
FallingSandGame/
├── src/
│   └── sandbox_game/
│       └── Sandbox_Game.java    # Main game logic
├── build.xml                     # Ant build configuration
├── manifest.mf                   # Java manifest file
└── nbproject/                    # NetBeans project files
```

## Technical Details

- **Language**: Java
- **Window Size**: 900×700 pixels (game area) + toolbar
- **Grid Resolution**: 5×5 pixel cells (180×140 grid)
- **Frame Rate**: ~60 FPS (16ms timer)
- **GUI Framework**: Swing (JPanel, JFrame, JToggleButton, JSlider)

### Key Implementation Features

- **Efficient Event Handling**: Uses single MouseAdapter for entire grid instead of individual component listeners
- **Cellular Automaton**: Each cell represents an element with state-based physics rules
- **Random Behavior**: Realistic particle variation through RNG (sand spread direction, water flow, etc.)
- **Circular Brush**: Distance-based calculation for smooth brush shapes

## Inspiration

Created as a Final Project, inspired by cellular automaton simulations and fluid dynamics:
- https://www.youtube.com/watch?v=5Ka3tbbT-9E
- https://www.youtube.com/watch?v=VLZjd_Y1gJ8

## License

No license specified. Feel free to modify and improve!

## Try It Out

Paint sand and watch it cascade, create water channels, trigger lava reactions—the possibilities are endless! Use the Pause button to study interesting patterns or experiment with different element combinations.
