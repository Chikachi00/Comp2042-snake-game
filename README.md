# COMP2042 Snake Game

A JavaFX Snake game based on the COMP2042 coursework starter project. This version maintains the original game and adds selectable levels, pause and end menus, and a timed combo bonus.

Repository: https://github.com/Chikachi00/Comp2042-snake-game

## Requirements and launch

- JDK 21 and an internet connection for the first Maven dependency download.
- Windows commands from the project directory:

```cmd
mvnw.cmd javafx:run
```

Run the automated tests with `mvnw.cmd clean test`. To build the runnable jar, use `mvnw.cmd clean package`, then `java -jar target\snake-javafx.jar`. Other platforms can use the Maven wrapper `./mvnw` and a compatible JDK.

## How to play

Choose a starting level in the main menu. Use the arrow keys to start and move the snake; reversing directly into the opposite direction is disabled. Avoid walls, the snake's body and obstacles.

- `P` or `Esc`: open or close the pause menu.
- `Tab`: restart the run from its selected starting level, clearing the score.
- Pause menu: resume, restart or return to the main menu.
- After losing or completing the final level, the end menu offers restart and return to the main menu.
- `+` and `-`: enter and leave full screen.

Each level starts with a fresh snake and a different fixed obstacle layout. Collect 5, 8 and 10 coins in levels 1, 2 and 3 respectively. The next level opens automatically with the total score retained; press an arrow key to start moving again. Finishing level 3 wins the run.

Each coin collected within five seconds of the previous one adds a combo bonus: the second coin adds 2 points, rising by 2 per consecutive coin to a maximum bonus of 10 points per coin. The bonus is added to the coin's normal points. Pausing freezes the combo timer; the combo resets on a level transition or restart. The HUD shows the level, coin objective, score and active combo.

The existing special coins remain available: gold gives extra points, rainbow temporarily speeds up the game, blue temporarily allows crossing the border, and silver adds obstacles. Silver coins are selected only while the board has no obstacles, as in the existing selection rules. The next collected coin replaces the previous temporary effect.

## Structure and key changes

- `com.comp2042.game`: game state and rules (`Game`, `GameSession`, `Level`, `ComboTracker`, `Snake`, `CoinFactory`, `CoinEffectService` and `Obstacle`). Level layouts, goal progress, restart and scoring are separate from JavaFX timer handling.
- `com.comp2042.ui`: the menu, windows, view and controller. `GameFlow` tracks pause and end states; `GameView` owns images, drawing and menus; `GameController` coordinates input and timing.
- `Main` and `SnakeApplication` retain the Maven and IDE launch entry point; `ViewManager` manages menu and game windows.
- Maintenance includes separating sprite rendering from models, protecting position snapshots, using direction values, correcting coin and obstacle selection, resetting game state, and stopping the timer at the end of a round.

## Verification

The JUnit suite covers movement, collision and wrapping, direction validation, coins, obstacle generation, game state, pause state, level progression and combo timing. A clean Maven test run completed with 155 tests passing. Programmatic JavaFX checks exercised menu actions, selection, transitions, restart and final scores; automated full games were also run with randomly generated coins from each starting level. A human should still play the game and verify the visual presentation and controls before submission.

## Coursework materials and AI use

Document final changes and locations, produce Javadoc and a class diagram matching the final code, and prepare the required demonstration video. The coursework permits generative AI only with the required detailed declaration. The separate AI usage log records assistance and verification; review it, complete entries for earlier work, and sign or submit any required declaration yourself.