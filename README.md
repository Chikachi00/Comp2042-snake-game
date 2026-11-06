# COMP2042 Snake Game

A maintained JavaFX Snake game based on the COMP2042 starter project. It adds selectable progressive levels, pause and end menus, and a combo bonus based on active play time.

## 1. GitHub

Repository: <https://github.com/Chikachi00/Comp2042-snake-game>

Original project: <https://github.com/kooitt/CW2026>. The original commit history is retained. Maintenance, refactoring and gameplay additions are recorded in separate commits and branches. The gameplay work is currently on `feature/gameplay-enhancements`; this README does not imply that it has already been merged into `main`.

## 2. Compilation Instructions

### Requirements

- JDK 21, including `java`, `javac` and `javadoc` on `PATH`.
- Internet access for the first Maven dependency download. A separate Maven installation is unnecessary because the project includes the Maven wrapper.
- JavaFX 21.0.13 and JUnit 5.12.2 are declared in `pom.xml` and resolved by Maven.
- The commands below use Windows Command Prompt and run from the folder containing `pom.xml` and `mvnw.cmd`. The locally verified environment is Windows with Eclipse Temurin JDK 21.0.11.

### Build and run

1. Clone the gameplay branch and enter the repository. These instructions use `feature/gameplay-enhancements` while its changes are awaiting merge into `main`:

   ```cmd
   git clone --branch feature/gameplay-enhancements https://github.com/Chikachi00/Comp2042-snake-game.git
   cd Comp2042-snake-game
   ```

2. Check that the active JDK is version 21:

   ```cmd
   java -version
   javac -version
   ```

3. Compile and run the tests:

   ```cmd
   mvnw.cmd clean test
   ```

4. Launch the game:

   ```cmd
   mvnw.cmd javafx:run
   ```

5. Alternatively, create and launch the runnable JAR:

   ```cmd
   mvnw.cmd clean package
   java -jar target\snake-javafx.jar
   ```

The IDE entry point is `com.comp2042.Main`. This ordinary Java launcher explicitly starts `SnakeApplication`, avoiding the JavaFX-subclass classpath-launch error encountered earlier. Import the folder containing `pom.xml` as a Maven project rather than opening only an individual Java file.

On other platforms, use `./mvnw` instead of `mvnw.cmd` and the appropriate path separator. Those platforms and a second computer have not yet been verified. JavaFX native dependencies are platform-specific; build the JAR on its intended platform.

### Browse or regenerate Javadoc

The generated `Javadoc` folder is ignored by Git and is not included in a fresh clone. Generate it locally from the current source before preparing the final coursework submission:

```cmd
mvnw.cmd compile
powershell -NoProfile -ExecutionPolicy Bypass -File scripts\generate-javadoc.ps1
start "" "Javadoc\index.html"
```

The tracked [generation script](scripts/generate-javadoc.ps1) reads the JavaFX version from the POM and uses dependencies in the local Maven cache. It includes package-visible implementation classes and private methods, and validates Javadoc markup. Documentation for a method includes its purpose, parameters and return value where applicable. After generation, `Javadoc/index.html` is the local entry page; copy the whole `Javadoc` folder into the final submission. Generated HTML does not need to be committed to GitHub.

## 3. Implemented and Working Properly

The following behaviors are covered by the automated checks described below. Final human play-testing is still required.

| Feature | Behavior and implementation location |
| --- | --- |
| Movement and collision | Arrow keys start movement; direct reversals and multiple direction changes within one tick are blocked. `game/Snake.java` checks walls, obstacles, wrapping and self collision; `ui/GameController.java` accepts input. |
| Coin placement and effects | `game/CoinFactory.java` places coins in free cells. Ordinary, rainbow, blue and silver coins give 5 base points; gold gives 15. `game/CoinEffectService.java` replaces temporary effects before awarding points. |
| Safe obstacle generation | `game/Obstacle.java` chooses unique free cells, avoids the snake and coin, respects board bounds and caps the total at 50. |
| Pause and resume | `P` or `Esc` toggles the pause menu. Movement and the combo clock pause. Resuming before the first move still waits for an arrow key. Implemented by `ui/GameFlow.java`, `ui/GameController.java` and `game/ComboTracker.java`. |
| Restart and navigation | The Restart button or `Tab` restores the selected starting level and clears run points, progress, combo, effects, snake state and timer speed. `Tab` also works when a pause or end-menu button has keyboard focus. Pause and end menus also provide Main Menu. |
| Three selectable levels | `game/Level.java` defines targets of 5, 8 and 10 coins and fixed layouts with 0, 8 and 16 obstacles. The main-menu selector can start at any level. These numbers are design choices, not fixed coursework requirements. |
| Sequential progression | Reaching a target creates a fresh snake and level layout, clears effects and combo, retains run points and waits for an arrow key. Completing level 3 shows the final score. `game/GameSession.java` controls progression. |
| Combo scoring | A pickup within five active seconds of the previous pickup continues the combo. Bonuses are 0, 2, 4, 6, 8, 10, then 10 points per pickup. Exactly five seconds qualifies; five seconds plus one nanosecond expires it. Pauses are excluded; level changes and restarts clear it. |
| Fullscreen drawing | `+` enters fullscreen and `-` restores windowed dimensions while the round is ready or running and unpaused. Window fullscreen-state changes also restore the canvas, scale and label sizes, including while paused or after restarting. Snake sprites scale once, matching cells, coins and obstacles. |
| Original interface fonts | The bundled regular and shaded pixel fonts retain their original sizes and styling. Resource-stream loading supports project paths containing spaces and packaged JARs. The menu FXML uses a JavaFX 21 namespace compatible with the runtime. |
| End state | Collision and victory stop the timer and present Restart and Main Menu actions. Movement and pause keys cannot restart an ended round. |

Rainbow coins add five moves per second; blue coins allow border wrapping. Their temporary effects end when the next coin is collected, the level changes or the run restarts. Silver adds random obstacles and is generated only when there are no existing obstacles, retaining the original selection rule. The first coin of a level is ordinary. The normal menu uses normal difficulty with special coins enabled; difficulty is not selectable in the menu.

### Controls and objectives

Choose a starting level, press Start and use an arrow key to begin. Avoid walls, obstacles and the snake's body. `P` / `Esc` pauses or resumes; `Tab` restarts. The HUD displays the level, collected coins versus the target, score and current combo bonus. After a level transition, press an arrow key to start the next level.

### Verification

The latest clean Maven package build, run on 6 November 2026, passed **190 JUnit test invocations with zero failures, errors or skips**. This includes 22 menu/timing lifecycle scenarios, four rainbow-to-next-level timer scenarios, eight sprite-scaling/fullscreen-restoration scenarios and one menu/game font-resource integration test. Earlier tests cover movement, free-cell generation, effect replacement, level objectives, score retention and combo boundaries.

UI tests use actual JavaFX controls and the real Timeline callback. An injectable clock makes time boundaries deterministic. Keyboard events are dispatched through the scene or focused controls; seven cases specifically check `Tab` from pause, game-over and victory menu buttons. Fullscreen-state tests cover exit before starting, while paused and after restarting. The resource test loads the real main menu, checks the original font families, weights and sizes, starts the game and checks the score font without an FXML version warning.

Separate local checks confirmed that a displayed window and canvas both return to `400 x 400` after fullscreen exit, and that the packaged JAR loads the original pixel fonts. These checks exercise programmatic actions and rendering; they do not certify human usability, native fullscreen behavior on every display or compatibility with another computer. Documentation was synchronized on 7 November 2026; the test date above records the actual build rather than a new test run for this documentation edit.

## 4. Implemented but Not Working Properly

No failing gameplay behavior is currently known from the automated scenarios above. This is a bounded verification result rather than a claim that the game has no remaining defects.

The previously identified fullscreen-restoration, focused-button `Tab`, font-loading and FXML-version issues have been fixed and are covered by the checks above. Final human play-testing of each level, controls, menu layout and fullscreen entry/exit remains pending, as does validation on another computer.

## 5. Features Not Implemented

All gameplay features in the agreed extension plan have implementations: pause/end menus, three selectable progressive levels and combo scoring. No agreed gameplay feature was intentionally left out. Optional ideas outside that plan, such as persistent high scores or selectable difficulty, were not added so that development could focus on the implemented run and its verification.

Submission tasks remain: final human play-testing, another-computer validation, the student's AI declaration and the demonstration video. They are recorded as pending rather than presented as implemented game features.

## 6. New Java Classes

All paths in the following tables are relative to `src/main/java/com/comp2042/`.

| New class or enum | Location | Purpose and reason |
| --- | --- | --- |
| `SnakeApplication` | `SnakeApplication.java` | Owns JavaFX lifecycle startup while `Main` remains a conventional launcher. |
| `Direction` | `game/Direction.java` | Replaces scattered direction characters with offsets and opposite-direction checks. |
| `CoinType` | `game/CoinType.java` | Identifies coins without coupling their rules to image resources. |
| `CoinFactory` | `game/CoinFactory.java` | Centralizes free-cell placement and category selection with an injectable random source. |
| `CoinEffectService` | `game/CoinEffectService.java` | Replaces temporary effects and awards base points outside the UI controller. |
| `GameSession` | `game/GameSession.java` | Coordinates rules, score and level progression independently of the movement timer. Its nested `StepResult` enum reports each move's outcome. |
| `Level` | `game/Level.java` | Defines each level's target, obstacle layout and successor. |
| `ComboTracker` | `game/ComboTracker.java` | Encapsulates the five-second active-time window and capped bonus. |
| `GameFlow` | `ui/GameFlow.java` | Separates ready, running, paused and ended UI states. Its private `State` enum preserves whether a paused game had started. |

Three `package-info.java` files describe the entry-point, game and UI packages. `CoinFactory`, `CoinEffectService`, `ComboTracker` and `GameFlow` have package visibility because they are implementation helpers rather than a public API.

### New test classes and helpers

Paths below are relative to `src/test/java/com/comp2042/`.

| Class | Location | Responsibility |
| --- | --- | --- |
| `SnakeTest` | `game/SnakeTest.java` | Movement, growth, collision and wrapping. |
| `ObstacleTest` | `game/ObstacleTest.java` | Bounds, uniqueness, occupancy exclusions and limits. |
| `ObstacleFixtures` | `game/ObstacleFixtures.java` | Deterministic obstacle layouts for tests. |
| `CoinTest` | `game/CoinTest.java` | Coin values, categories and coordinate snapshots. |
| `CoinPositionTest` | `game/CoinPositionTest.java` | Free-cell selection and full boards. |
| `CoinSelectionTest` | `game/CoinSelectionTest.java` | Selection branches and silver eligibility. |
| `CoinFactoryTest` | `game/CoinFactoryTest.java` | Creation, placement and disabled special coins. |
| `CoinEffectServiceTest` | `game/CoinEffectServiceTest.java` | Effect replacement and base points. |
| `GameTest` | `game/GameTest.java` | Game settings, win thresholds and resets. |
| `GameSessionTest` | `game/GameSessionTest.java` | Rule coordination, death, wins and legacy-round reset. |
| `DirectionAndEncapsulationTest` | `game/DirectionAndEncapsulationTest.java` | Direction behavior and protected coordinate snapshots. |
| `LevelProgressionTest` | `game/LevelProgressionTest.java` | Objectives, chosen start, score retention and reachable layouts. |
| `ComboTrackerTest` | `game/ComboTrackerTest.java` | Boundaries, capped bonuses, pause accounting and resets. |
| `GameControllerTest` | `ui/GameControllerTest.java` | Direction/key mappings and reversal rules. |
| `GameFlowTest` | `ui/GameFlowTest.java` | Ready, paused, running and ended transitions. |
| `GameControllerTimingTest` | `ui/GameControllerTimingTest.java` | Actual timer-rate restoration at level transitions. |
| `GameViewScalingTest` | `ui/GameViewScalingTest.java` | Rendered straight/corner sprite scales and fullscreen exit while ready, paused or restarted. |
| `GameControllerLifecycleTest` | `ui/GameControllerLifecycleTest.java` | Keyboard/menu actions with pause timing, restart, end states and focused-button `Tab` routing. |
| `ViewManagerResourcesTest` | `ui/ViewManagerResourcesTest.java` | Actual menu/game font families, weights and sizes, and absence of an FXML version warning. |
| `JavaFxTestSupport` | `ui/JavaFxTestSupport.java` | Shared JavaFX initialization and application-thread assertions. |

## 7. Modified Java Classes

These thirteen classes were present in the starter project. Moving them into `game` and `ui` preserves their origin; they are not counted as new classes merely because their paths changed. Paths are relative to `src/main/java/com/comp2042/`.

| Original class | Current location | Changes and rationale |
| --- | --- | --- |
| `Main` | `Main.java` | Delegates to JavaFX explicitly to support Maven and IDE classpath launches. |
| `Game` | `game/Game.java` | Restores score and win thresholds on reset; distinguishes collected coins from point score and retains run points across levels. |
| `Snake` | `game/Snake.java` | Removes image rendering, uses `Direction`, protects returned coordinates, resolves wrapping before obstacle collision and resets movement state through new instances. |
| `Coin` | `game/Coin.java` | Removes image ownership, protects position snapshots, finds free cells without unbounded retry loops and supplies the effect/category contract. |
| `BlueCoin` | `game/BlueCoin.java` | Implements temporary wrapping through its effect override and reports its category. |
| `GoldCoin` | `game/GoldCoin.java` | Retains the fifteen-point value and reports its category for factory and view selection. |
| `RainbowCoin` | `game/RainbowCoin.java` | Encapsulates the five-move speed bonus and identifies the restored rainbow selection branch. |
| `SilverCoin` | `game/SilverCoin.java` | Adds safe obstacles and adjusts the threshold by the actual number placed, rather than a requested amount that might not fit. |
| `Obstacle` | `game/Obstacle.java` | Fixes off-by-one coordinates, duplicates and occupied-cell overlap; provides validated fixed layouts and defensive snapshots. |
| `GameController` | `ui/GameController.java` | Delegates rules to `GameSession`; validates input and coordinates timer, pause, restart, level changes and menus. The next-level path restores the timer's base rate after rainbow effects. A window fullscreen-state listener resizes the current session's board. |
| `GameView` | `ui/GameView.java` | Owns snake/coin sprites, keeps canvas details private, renders corner state and presents HUD/menus. Fullscreen sprite sizes use one scale factor; windowed dimensions and label sizes are restored on exit. |
| `MainMenuController` | `ui/MainMenuController.java` | Initializes level selection and wires a session, controller, view and menu actions. Captures game key events before focused controls can consume `Tab` for navigation. |
| `ViewManager` | `ui/ViewManager.java` | Owns menu/game windows and injects navigation through an instance instead of shared global window state. Registers the original pixel fonts from resource streams before loading the menu. |

Coin construction is separated through a small factory. Effects use ordinary polymorphic overrides in the coin hierarchy; there is no separate Strategy class. `GameSession` owns rules while the controller schedules them and the view draws them. Coordinate snapshots prevent callers from modifying snake and obstacle internals accidentally.

## 8. Unexpected Problems

| Problem | Resolution or current status | Location |
| --- | --- | --- |
| JavaFX 25.0.1 was incompatible with the available JDK 21 | Aligned the project to JavaFX 21.0.13 and repaired wrapper configuration. | `pom.xml`, Maven wrapper files |
| IDE launch reported missing JavaFX runtime components | Introduced the ordinary `Main` launcher and separate `SnakeApplication`. | `Main.java`, `SnakeApplication.java` |
| Reverse input and duplicate random-selection branches | Used direction values and a one-change-per-tick latch; centralized and tested coin selection. | `ui/GameController.java`, `game/Direction.java`, `game/CoinFactory.java` |
| Invalid or repeated obstacle coordinates and full-board placement loops | Enumerated available cells without replacement; coin creation returns an empty optional when none are free. | `game/Obstacle.java`, `game/Coin.java`, `game/CoinFactory.java` |
| Restart retained stale progress or direction state | Reset model state, win threshold, effects, UI flow, input latch, turn sprites and timer together. | `game/Game.java`, `game/GameSession.java`, `ui/GameController.java` |
| Border wrapping could miss an obstacle at the destination | Checked obstacle collision after wrapping. | `game/Snake.java` |
| Timer continued after a round ended | Stopped it on death, win, navigation and stage hiding. | `ui/GameController.java` |
| New level model speed was 8.5 but the timer retained rainbow speed 13.5 | Explicitly synchronized the timer rate during the transition; added four controller regression scenarios. | `ui/GameController.java`, `ui/GameControllerTimingTest.java` in test sources |
| Fullscreen enlarged sprite dimensions twice | Removed the duplicate scale factors; checked rendered bounds and exact windowed-image restoration. | `ui/GameView.java`, `ui/GameViewScalingTest.java` in test sources |
| Exiting fullscreen left an enlarged canvas in a small window | Listen to the window's actual fullscreen state and resize/redraw the current session; added ready, paused and restarted regression cases. | `ui/GameController.java`, `ui/GameView.java`, `ui/GameViewScalingTest.java` in test sources |
| Focused menu buttons consumed `Tab` before the game received it | Capture key events at scene level before control navigation; added seven cases covering all pause/end-menu buttons. | `ui/MainMenuController.java`, `ui/GameControllerLifecycleTest.java` in test sources |
| Independent rule tests missed interactions | Expanded to 22 lifecycle scenarios using actual controls, dispatched key events, timer callbacks and a controllable clock. | `ui/GameControllerLifecycleTest.java` in test sources |
| Font URL paths containing spaces failed to load | Register the original bundled fonts from streams, avoiding the encoded file-path problem; verified the original font families/styles/sizes in the menu, game and packaged JAR. | `ui/ViewManager.java`, `src/main/resources/resources/views/styling.css`, `ui/ViewManagerResourcesTest.java` in test sources |
| FXML namespace version warning | Changed the namespace from JavaFX 26 to 21 to match the supported runtime; the real menu integration check loads without this warning. | `src/main/resources/resources/views/MainMenu.fxml`, `ui/ViewManagerResourcesTest.java` in test sources |

The model still stores JavaFX colors, and final-level victory currently uses the legacy `markBoardFull()` win flag. These are maintainability limitations, not separately verified gameplay failures. Final design and Javadoc materials should describe the actual implementation.

## Documentation and submission materials

- `Design.pdf` and its generator, `docs/build_design.py`, are local drafts and have not been committed to this repository. Review the diagram against the final production classes, relationships and multiplicities before including the finished PDF in the final submission. It should omit attributes and methods unless needed to explain the design.
- `Javadoc/` is generated locally using the tracked [generation script](scripts/generate-javadoc.ps1) and is ignored by Git. Follow the generation commands above to create it after cloning, then include the resulting folder in the final submission. Test-only fixtures are excluded from the production diagram and API pages.
- [docs/SUBMISSION_CHECKLIST.md](docs/SUBMISSION_CHECKLIST.md) records completed materials and the remaining manual checks.
- Generative AI assisted debugging, refactoring, implementation, testing and documentation. The separate `AI_Usage_Log.docx` is held locally and is not part of this repository. The student must review and update it to reflect the actual assistance, and complete the official COMP2042 AI Use Declaration Form. The log does not replace that form.

The final submission folder must contain `README.md`, `Design.pdf`, `Javadoc`, the student's own-voice demonstration video and the complete project ZIP. Final packaging and submission are pending the manual checks and student details.
