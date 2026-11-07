# COMP2042 Snake Game

A maintained JavaFX Snake game based on the COMP2042 starter project. The main menu offers classic endless survival and a three-level challenge. Both keep separate persistent personal bests and have pause/end menus; the challenge adds optional key-operated shortcuts and a combo bonus with an active-time countdown.

## 1. GitHub

Repository: <https://github.com/Chikachi00/Comp2042-snake-game>

Original project: <https://github.com/kooitt/CW2026>. The original commit history is retained. Maintenance, refactoring and gameplay additions are recorded in separate commits and branches. The gameplay enhancements were merged from `feature/gameplay-enhancements` into `main` through [pull request #3](https://github.com/Chikachi00/Comp2042-snake-game/pull/3) on 7 November 2026. The `main` branch contains the current playable version.

## 2. Compilation Instructions

### Requirements

- JDK 21, including `java`, `javac` and `javadoc` on `PATH`.
- Internet access for the first Maven dependency download. A separate Maven installation is unnecessary because the project includes the Maven wrapper.
- JavaFX 21.0.13 and JUnit 5.12.2 are declared in `pom.xml` and resolved by Maven.
- The commands below use Windows Command Prompt and run from the folder containing `pom.xml` and `mvnw.cmd`. The locally verified environment is Windows with Eclipse Temurin JDK 21.0.11.

### Build and run

1. Clone the current `main` branch and enter the repository:

   ```cmd
   git clone --branch main https://github.com/Chikachi00/Comp2042-snake-game.git
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

The tracked [generation script](scripts/generate-javadoc.ps1) reads the JavaFX version from the POM and uses dependencies in the local Maven cache. It generates English navigation regardless of the operating system's language, includes package-visible implementation classes and private methods, and treats Javadoc markup warnings as errors. Documentation for a method includes its purpose, parameters and return value where applicable. After generation, `Javadoc/index.html` is the local entry page; copy the whole `Javadoc` folder into the final submission. Generated HTML does not need to be committed to GitHub.

## 3. Implemented and Working Properly

The following behaviors are covered by the automated checks described below. The student also reported local play-testing with no issues on 7 November 2026; detailed scenario coverage and another-computer validation are tracked separately in the submission checklist.

| Feature | Behavior and implementation location |
| --- | --- |
| Mode selection | `game/GameMode.java` identifies Endless and Challenge. The main menu defaults to Endless and shows the starting-level selector only for Challenge. Changing modes preserves the last level choice; starting a game creates fresh state. |
| Classic endless survival | Original coin points, temporary effects, silver-generated obstacles and the board-fill win condition are retained. There are no 5/8/10-coin objectives, fixed level layouts, keys or combo bonuses. Play continues until collision or filling the available board. |
| Movement and collision | Arrow keys start movement; direct reversals and multiple direction changes within one tick are blocked. `game/Snake.java` checks walls, obstacles, wrapping and self collision; `ui/GameController.java` accepts input. |
| Coin placement and effects | `game/CoinFactory.java` places coins in free cells. Ordinary, rainbow, blue and silver coins give 5 base points; gold gives 15. `game/CoinEffectService.java` replaces temporary effects before awarding points. |
| Safe obstacle generation | `game/Obstacle.java` chooses unique free cells, avoids the snake and coin, respects board bounds and caps the total at 50. |
| Pause and resume | `P` or `Esc` toggles the pause menu. Movement and the combo clock pause. Resuming before the first move still waits for an arrow key. Implemented by `ui/GameFlow.java`, `ui/GameController.java` and `game/ComboTracker.java`. |
| Restart and navigation | The Restart button or `Tab` keeps the selected mode, restores the challenge starting level where applicable and clears run points, progress, combo, effects, snake state and timer speed. `Tab` also works when a pause or end-menu button has keyboard focus. Pause and end menus also provide Main Menu. |
| Three selectable levels | `game/Level.java` defines targets of 5, 8 and 10 coins and fixed layouts with 0, 13 and 26 wall cells. Levels 2 and 3 also have one and two optional gates respectively. Choosing Challenge in the main menu enables a selector that can start at any level. These numbers are design choices, not fixed coursework requirements. |
| Optional shortcuts | After the first coin in levels 2/3, matching A/B keys appear. Collecting a key permanently opens its gate, without points, growth, coin progress, combo refresh or effect replacement. Wall ends remain open, so every level can be completed without keys. |
| Sequential progression | Reaching a target creates a fresh snake and level layout, clears effects and combo, retains run points and waits for an arrow key. Completing level 3 shows the final score. `game/GameSession.java` controls progression. |
| Combo scoring (Challenge) | A pickup within five active seconds of the previous pickup continues the combo. Bonuses are 0, 2, 4, 6, 8, 10, then 10 points per pickup. Exactly five seconds qualifies; five seconds plus one nanosecond expires it. Pauses are excluded; level changes and restarts clear it. The HUD displays remaining seconds and a progress bar from the same clock. |
| Fullscreen drawing | `+` enters fullscreen and `-` restores windowed dimensions while the round is ready or running and unpaused. Window fullscreen-state changes also restore the canvas, scale and label sizes, including while paused or after restarting. Snake sprites, keys and gates share one scale factor. Both modes use a 480 x 480 windowed scene, containing a 400 x 400 board and separate HUD/footer; fullscreen fits the square board inside the space remaining after those regions. |
| Original interface fonts | The original regular and shaded pixel-font files and the main-menu title/Start sizes are retained. The game HUD and menu buttons use explicit sizes that fit their allocated space. Resource-stream loading supports project paths containing spaces and packaged JARs. The menu FXML uses a JavaFX 21 namespace compatible with the runtime. |
| Personal bests | `persistence/HighScoreStore.java` saves independent Endless and Challenge records. A strict improvement is saved immediately, including partial runs; equal/lower scores leave it unchanged. The selected mode's Best appears in the main menu, HUD and final-score overlay, with a New high score message for a record-breaking run. Restart clears only the current score. |
| End state | Collision and victory stop the timer and present Restart and Main Menu actions. Movement and pause keys cannot restart an ended round. |

Rainbow coins add five moves per second; blue coins allow border wrapping. Their temporary effects end when the next coin is collected, a challenge level changes or the run restarts. Silver adds random obstacles and is generated only when there are no existing obstacles, retaining the original selection rule. The opening coin of either mode is ordinary. Both menu modes use normal difficulty with special coins enabled; difficulty is not selectable in the menu.

### Local personal records

Personal bests are stored in `%USERPROFILE%\.comp2042-snake\high-scores.properties` (the Java user-home directory on other platforms), outside the project and generated JAR. IDE and packaged launches under the same user account share them. There are two records: Endless and Challenge; Challenge uses accumulated run points including combos, across any selected starting level. A partial or failed run can set a record. Records start at zero and are saved as soon as a score improves.

The file is replaced through a temporary file, using an atomic move where supported. Missing entries start at zero; invalid numeric entries are ignored independently. Read/write failure is logged and gameplay continues with an in-memory record; pending saves are retried when restarting, navigating or closing the game. A record cannot survive application exit until a save succeeds. Tests use temporary paths or memory-only stores.

### Controls and objectives

Choose Endless or Challenge in the main menu. Endless uses the original survival rules and coin values, with no combo or level target; filling the available board retains the original win condition. For Challenge, choose the starting level. Press Start and use an arrow key to begin. Avoid walls, obstacles and the snake's body. `P` / `Esc` pauses or resumes; `Tab` restarts. The Endless HUD identifies classic survival and shows the score. The Challenge HUD displays the level, collected coins versus the target, score and current combo bonus. After a level transition, press an arrow key to start the next level. In levels 2/3, collect the first coin to reveal keys; take a matching key to open a shortcut or go around the wall ends. Taking a key consumes normal play time and can cost the current combo. Open gates remain open until restart or level change.

### Verification

The latest clean Maven package build, run on 7 November 2026, passed **239 JUnit test invocations with zero failures, errors or skips**. This includes 22 menu/timing lifecycle scenarios, four rainbow-to-next-level timer scenarios, eight sprite-scaling/fullscreen-restoration scenarios and one menu/game font-resource integration test. New shortcut coverage adds two lifecycle/encapsulation cases, eighteen gameplay cases and one rendered HUD/gate case. The gameplay cases include six complete model-driven runs: each starting level with every key collected and with every key avoided. Twelve mode cases additionally cover endless play beyond all challenge targets, unchanged values/effects for all five coin categories, reset behavior, mode switching and real-menu start/restart/navigation at every challenge starting level. Sixteen personal-best cases cover persistence across store reloads, mode isolation, ties/lower scores, malformed values, write-failure recovery, live HUD updates, restart, loss/victory overlays, cumulative challenge points and real-menu refresh. Earlier tests cover movement, free-cell generation, effect replacement, level objectives, score retention and combo boundaries.

UI tests use actual JavaFX controls and the real Timeline callback. An injectable clock makes time boundaries deterministic. Keyboard events are dispatched through the scene or focused controls; seven cases specifically check `Tab` from pause, game-over and victory menu buttons. Fullscreen-state tests cover exit before starting, while paused and after restarting. The resource test loads the real main menu, checks the original font families, weights and sizes, starts the game and checks the score font without an FXML version warning.

Separate local checks confirmed that fullscreen exit restores a `480 x 480` scene with a `400 x 400` canvas, and that the packaged JAR loads the original pixel fonts. These checks exercise programmatic actions and rendering; they do not certify human usability, native fullscreen behavior on every display or compatibility with another computer. Actual JavaFX previews were captured from the packaged game after programmatic keyboard/timer steps, showing key appearance, one/both gates open and pause. Native fullscreen entry/exit was checked with the separate HUD. The complete runs are automated model checks, not a record of final human play-testing.

## 4. Implemented but Not Working Properly

No failing gameplay behavior is currently known from the automated scenarios above. This is a bounded verification result rather than a claim that the game has no remaining defects.

The previously identified fullscreen-restoration, focused-button `Tab`, font-loading and FXML-version issues have been fixed and are covered by the checks above. The student reported successful local play-testing on 7 November 2026. Confirmation of the individual manual scenarios in the submission checklist and validation on another computer remain outstanding.

## 5. Features Not Implemented

All gameplay features in the agreed extension plans have implementations: selectable Endless/Challenge modes, pause/end menus, three selectable progressive levels, combo scoring with a countdown, optional key-operated shortcuts, and persistent personal bests for both modes. No agreed gameplay feature was intentionally left out. Optional ideas outside those plans, such as selectable difficulty, were not added so that development could focus on the implemented run and its verification.

Submission tasks remain: confirmation of manual scenario coverage, another-computer validation, the student's AI declaration, the demonstration video and final packaging. Their status is recorded in the submission checklist.

## 6. New Java Classes

All paths in the following tables are relative to `src/main/java/com/comp2042/`.

| New class or enum | Location | Purpose and reason |
| --- | --- | --- |
| `SnakeApplication` | `SnakeApplication.java` | Owns JavaFX lifecycle startup while `Main` remains a conventional launcher. |
| `Direction` | `game/Direction.java` | Replaces scattered direction characters with offsets and opposite-direction checks. |
| `CoinType` | `game/CoinType.java` | Identifies coins without coupling their rules to image resources. |
| `CoinFactory` | `game/CoinFactory.java` | Centralizes free-cell placement and category selection with an injectable random source. |
| `CoinEffectService` | `game/CoinEffectService.java` | Replaces temporary effects and awards base points outside the UI controller. |
| `GameSession` | `game/GameSession.java` | Coordinates rules, score, endless survival and level progression independently of the movement timer. Its nested `StepResult` enum reports each move's outcome. |
| `HighScoreStore` | `persistence/HighScoreStore.java` | Loads and saves independent mode records, validates stored values and keeps disk failure outside gameplay rules. |
| `GameMode` | `game/GameMode.java` | Names the original endless survival and progressive challenge choices shown in the menu. |
| `Level` | `game/Level.java` | Defines each level's target, obstacle layout and successor. |
| `Shortcut` | `game/Shortcut.java` | Owns hidden, available and open states for a matching key/gate pair. Each level creates fresh instances. Its private `State` enum prevents reopening/closing transitions. |
| `ShortcutSnapshot` | `game/ShortcutSnapshot.java` | An immutable record of integer coordinates, key visibility and gate state for rendering without exposing mutable model data. |
| `ComboTracker` | `game/ComboTracker.java` | Encapsulates the five-second active-time window and capped bonus. |
| `GameFlow` | `ui/GameFlow.java` | Separates ready, running, paused and ended UI states. Its private `State` enum preserves whether a paused game had started. |

Four `package-info.java` files describe the entry-point, game, UI and persistence packages. `CoinFactory`, `CoinEffectService`, `Shortcut`, `ComboTracker` and `GameFlow` have package visibility because they are implementation helpers rather than a public API.

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
| `EndlessModeTest` | `game/EndlessModeTest.java` | Continuous play beyond level targets, original coin values/effects and same-mode reset. |
| `HighScoreStoreTest` | `persistence/HighScoreStoreTest.java` | Mode-isolated persistence, input validation, corrupted entries and save-failure recovery. |
| `HighScoreIntegrationTest` | `ui/HighScoreIntegrationTest.java` | Real controller/HUD/menu integration, end-state records, restart and cumulative challenge scoring. |
| `MainMenuModeTest` | `ui/MainMenuModeTest.java` | Real FXML mode/level selection, launching, restarting and navigation between modes. |
| `LevelProgressionTest` | `game/LevelProgressionTest.java` | Objectives, chosen start, score retention and reachable layouts. |
| `ComboTrackerTest` | `game/ComboTrackerTest.java` | Boundaries, capped bonuses, pause accounting and resets. |
| `GameControllerTest` | `ui/GameControllerTest.java` | Direction/key mappings and reversal rules. |
| `GameFlowTest` | `ui/GameFlowTest.java` | Ready, paused, running and ended transitions. |
| `GameControllerTimingTest` | `ui/GameControllerTimingTest.java` | Actual timer-rate restoration at level transitions. |
| `ShortcutTest` | `game/ShortcutTest.java` | Key activation, one-time opening and coordinate/snapshot isolation. |
| `ShortcutGameplayTest` | `game/ShortcutGameplayTest.java` | Key effects, gate collisions, legal placement, all gate-state layouts, pause/countdown boundaries and complete runs with/without keys. |
| `GameViewShortcutTest` | `ui/GameViewShortcutTest.java` | HUD separation from the board, countdown, visible gate/key status and untruncated menu text. |
| `GameViewScalingTest` | `ui/GameViewScalingTest.java` | Rendered straight/corner sprite scales and fullscreen exit while ready, paused or restarted. |
| `GameControllerLifecycleTest` | `ui/GameControllerLifecycleTest.java` | Keyboard/menu actions with pause timing, restart, end states and focused-button `Tab` routing. |
| `ViewManagerResourcesTest` | `ui/ViewManagerResourcesTest.java` | Actual menu/game font families, weights and sizes, and absence of an FXML version warning. |
| `JavaFxTestSupport` | `ui/JavaFxTestSupport.java` | Shared JavaFX initialization and application-thread assertions. |

## 7. Modified Java Classes

These thirteen classes were present in the starter project. Moving them into `game` and `ui` preserves their origin; they are not counted as new classes merely because their paths changed. Paths are relative to `src/main/java/com/comp2042/`.

| Original class | Current location | Changes and rationale |
| --- | --- | --- |
| `Main` | `Main.java` | Delegates to JavaFX explicitly to support Maven and IDE classpath launches. |
| `Game` | `game/Game.java` | Restores score and win thresholds on reset; distinguishes collected coins from point score and retains run points across levels. Recalculates board capacity when gates open without changing points or coin counts. |
| `Snake` | `game/Snake.java` | Removes image rendering, uses `Direction`, protects returned coordinates, resolves wrapping before obstacle collision and resets movement state through new instances. |
| `Coin` | `game/Coin.java` | Removes image ownership, protects position snapshots, finds free cells without unbounded retry loops and supplies the effect/category contract. |
| `BlueCoin` | `game/BlueCoin.java` | Implements temporary wrapping through its effect override and reports its category. |
| `GoldCoin` | `game/GoldCoin.java` | Retains the fifteen-point value and reports its category for factory and view selection. |
| `RainbowCoin` | `game/RainbowCoin.java` | Encapsulates the five-move speed bonus and identifies the restored rainbow selection branch. |
| `SilverCoin` | `game/SilverCoin.java` | Adds safe obstacles and adjusts the threshold by the actual number placed, rather than a requested amount that might not fit. |
| `Obstacle` | `game/Obstacle.java` | Fixes off-by-one coordinates, duplicates and occupied-cell overlap; provides validated fixed layouts and defensive snapshots. |
| `GameController` | `ui/GameController.java` | Delegates rules to `GameSession`; validates input and coordinates timer, pause, restart, level changes and menus. The next-level path restores the timer's base rate after rainbow effects. A window fullscreen-state listener resizes the current session's board; model refresh includes the current shortcut snapshots. Records score improvements through an injected store and preserves records before reset/navigation/window hiding. |
| `GameView` | `ui/GameView.java` | Owns snake/coin sprites, keeps canvas details private, renders corner state and presents HUD/menus. Fullscreen sprite sizes use one scale factor; windowed dimensions are restored on exit. Draws pixel keys/gates, keeps HUD/footer outside the board and displays mode-specific guidance, personal bests and shared-clock challenge combo time. |
| `MainMenuController` | `ui/MainMenuController.java` | Initializes mode/level selection, shows mode-specific guidance and wires a fresh session, controller, view and menu actions. Shows the selected mode's saved best and shares the record store with its controller. Captures game key events before focused controls can consume `Tab` for navigation. |
| `ViewManager` | `ui/ViewManager.java` | Owns menu/game windows and injects navigation through an instance instead of shared global window state. Registers the original pixel fonts from resource streams before loading the menu. Owns the application's shared record store and refreshes the menu record when returning from a round. |

Coin construction is separated through a small factory. Effects use ordinary polymorphic overrides in the coin hierarchy; there is no separate Strategy class. `GameSession` owns rules while the controller schedules them and the view draws them. Coordinate snapshots prevent callers from modifying snake and obstacle internals accidentally.

Level configuration also defines the first coin of levels 2/3 at `(10,3)` and the second coin of level 3 at `(10,16)` to introduce cross-wall routes. Position preferences still use safe placement checks; coin categories retain their existing rules. Keys have their own model and do not inherit coin scoring or growth behavior.

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
| Pixel-font Main Menu button text was truncated | Use an explicit 14px game-menu button font inside the original button image; rendered-control assertions verify the full label. | `ui/GameView.java`, `ui/GameViewShortcutTest.java` in test sources |
| FXML namespace version warning | Changed the namespace from JavaFX 26 to 21 to match the supported runtime; the real menu integration check loads without this warning. | `src/main/resources/resources/views/MainMenu.fxml`, `ui/ViewManagerResourcesTest.java` in test sources |

The model still stores JavaFX colors, and final-level victory currently uses the legacy `markBoardFull()` win flag. These are maintainability limitations, not separately verified gameplay failures. The final class diagram and Javadoc describe the existing implementation.

## Documentation and submission materials

- [Design.pdf](Design.pdf) covers all 26 top-level production types and three nested enums across three A3 landscape views: application/UI/persistence, session/movement/shortcuts, and coin construction/inheritance. It includes mode, personal-record and shortcut types; distinguishes retained references, ownership, dependencies and inheritance; and labels target multiplicities. Attributes and methods are omitted. All three pages have been rendered and visually checked against the final code. Include this PDF in the final submission.
- [docs/build_design.py](docs/build_design.py) regenerates the diagram and checks the source inventory, selected retained-reference types, inheritance and connector geometry. The generator requires Python with `reportlab`; run `python docs/build_design.py` from the project folder. Java compilation and gameplay do not require Python. Regenerate and review the PDF if the production structure changes.
- `Javadoc/` was regenerated from the final production source on 7 November 2026 using JDK 21.0.11. It includes all 29 production type pages (26 top-level types and three nested enums) and four package summaries. Generated documentation is local and ignored by Git. Follow the generation commands above after cloning, then include the whole resulting folder in the final submission. Test-only fixtures are excluded from the production diagram and API pages.
- Final documentation checks confirmed the type/package coverage, descriptions of declared methods and constructors, and parameter/return sections where applicable. All 2,909 local HTML links and asset references resolved, including member anchors. The overview, game-session, movement-method and personal-record pages were also checked in a browser with offline navigation. The three shortcut states have explanatory source comments; this documentation update does not change executable game logic.
- [docs/SUBMISSION_CHECKLIST.md](docs/SUBMISSION_CHECKLIST.md) records completed materials and the remaining manual checks.
- Generative AI assisted debugging, refactoring, implementation, testing and documentation. The separate `AI_Usage_Log.docx` is held locally and is not part of this repository. The student must review and update it to reflect the actual assistance, and complete the official COMP2042 AI Use Declaration Form. The log does not replace that form.

The README, final class diagram and generated Javadoc are prepared for the current production code. The final submission folder must contain `README.md`, `Design.pdf`, `Javadoc`, the student's own-voice demonstration video and the complete project ZIP. Final packaging and submission remain pending the manual checks, video, AI declaration and student details listed in the checklist.
