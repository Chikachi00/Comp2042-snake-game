# COMP2042 Snake Game

A JavaFX Snake game based on the COMP2042 starter project. Choose Endless with Slow/Medium/Fast speeds or a three-level Challenge. Both modes have pause/end menus and separate saved best scores. Challenge adds key-operated shortcuts and combo scoring with a countdown.

## 1. GitHub

Repository: <https://github.com/Chikachi00/Comp2042-snake-game>

Original project: <https://github.com/kooitt/CW2026>. The original commit history is retained. Bug fixes, refactoring and features have separate commits and branches. Gameplay additions were merged through [pull request #3](https://github.com/Chikachi00/Comp2042-snake-game/pull/3) on 7 November 2026; speed selection followed in [pull request #4](https://github.com/Chikachi00/Comp2042-snake-game/pull/4) on 8 November 2026. `main` is the current playable version.

## 2. Compilation Instructions

### Requirements

- JDK 23 or later, including `java`, `javac` and `javadoc` on `PATH`. The compiler targets Java 23, matching the minimum required by JavaFX 25. Development can continue on JDK 27.
- Internet access for the first Maven dependency download. A separate Maven installation is unnecessary because the project includes the Maven wrapper.
- JavaFX 25.0.5 and JUnit 5.12.2 are declared in `pom.xml` and resolved by Maven. A separate JavaFX SDK installation is unnecessary.
- Run the Windows Command Prompt commands below from the folder containing `pom.xml` and `mvnw.cmd`. Test environments are listed under Verification.

### Build and run

1. Clone the current `main` branch and enter the repository:

   ```cmd
   git clone --branch main https://github.com/Chikachi00/Comp2042-snake-game.git
   cd Comp2042-snake-game
   ```

2. Check that Java, the compiler and Maven all use JDK 23 or later:

   ```cmd
   java -version
   javac -version
   mvnw.cmd -v
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
   java --enable-native-access=ALL-UNNAMED -jar target\snake-javafx.jar
   ```

Set `JAVA_HOME` to the chosen JDK directory and put its `bin` first on `PATH`. Select the same JDK in the IDE. The compiler release setting controls the bytecode target, not the installed JDK. JDK 21 cannot build or run this version.

In PowerShell, use `.\mvnw.cmd -v`, `.\mvnw.cmd clean package` and `.\mvnw.cmd javafx:run`. Check Maven's Java version and wait for `BUILD SUCCESS` before launching. When copying the project, omit machine-specific `.vscode` settings and select the receiving computer's JDK.

For IDE launches, import the folder containing `pom.xml` as a Maven project and run `com.comp2042.Main`. It starts `SnakeApplication` and avoids the JavaFX subclass-launch error.

### Java and platform compatibility

JavaFX 25 and this project use Java 23 bytecode. The packaged game can therefore run on JDK 23 without requiring the development JDK 27. JDK 23, 25 and 27 results are recorded below. JDK 24 and 26 have not been tested.

JavaFX 25 provides native libraries for Windows x64, Linux x64 and macOS x64/AArch64. Linux requires a graphical desktop and GTK 3.20 or later. Use a matching JDK and OS; see the [JavaFX 25 platform notes](https://docs.oracle.com/en/java/java-components/javafx/25/release-notes/) and [minimum Java/GTK requirements](https://openjfx.io/highlights/25/).

The runnable JAR bundles native libraries for the build computer. Build on the intended OS/CPU so Maven selects the matching dependencies. A Windows-built JAR is for Windows. Windows 11 x64 has passed local and second-computer checks; macOS and Linux remain untested.

On macOS/Linux, use `sh ./mvnw` in place of `mvnw.cmd` and forward slashes in paths:

```sh
sh ./mvnw clean package
java --enable-native-access=ALL-UNNAMED -jar target/snake-javafx.jar
```

The Unix wrapper uses LF endings, enforced by `.gitattributes`. Running it through `sh` avoids executable-permission problems after ZIP extraction. Maven is supplied by the wrapper.

### Browse or regenerate Javadoc

The generated `Javadoc` folder is ignored by Git and is not included in a fresh clone. Generate it locally from the current source before preparing the final coursework submission:

```cmd
mvnw.cmd compile
powershell -NoProfile -ExecutionPolicy Bypass -File scripts\generate-javadoc.ps1
start "" "Javadoc\index.html"
```

The [generation script](scripts/generate-javadoc.ps1) reads the JavaFX version from the POM and uses cached Maven dependencies. It generates English navigation, includes package-visible classes and private methods, and treats markup warnings as errors. Method comments describe purpose, parameters and return values where applicable. Open `Javadoc/index.html` locally and include the whole folder in the submission; generated HTML is ignored by Git.

## 3. Implemented and Working Properly

| Feature | Behavior and implementation location |
| --- | --- |
| Mode selection | `game/GameMode.java` identifies Endless and Challenge. The main menu defaults to Endless and shows the starting-level selector only for Challenge. Changing modes preserves the last level choice; starting a game creates fresh state. |
| Endless speed selection | The menu offers Slow (5 moves/s), Medium (8.5 moves/s, the original default) and Fast (13 moves/s), shown only for Endless. Restart retains the chosen base speed; switching modes or returning to the menu retains the last choice. The HUD shows the current movement rate, including rainbow boosts. Border rules, coin values and the shared Endless personal best are unchanged. Implemented by `game/GameSpeed.java`, `game/Game.java` and `ui/MainMenuController.java`. |
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
| Original interface fonts | The original regular and shaded pixel-font files and the main-menu title/Start sizes are retained. The game HUD and menu buttons use explicit sizes that fit their allocated space. Resource-stream loading supports project paths containing spaces and packaged JARs. The menu FXML uses a JavaFX 25 namespace compatible with the runtime. |
| Personal bests | `persistence/HighScoreStore.java` saves independent Endless and Challenge records. A strict improvement is saved immediately, including partial runs; equal/lower scores leave it unchanged. The selected mode's Best appears in the main menu, HUD and final-score overlay, with a New high score message for a record-breaking run. Restart clears only the current score. |
| End state | Collision and victory stop the timer and present Restart and Main Menu actions. Movement and pause keys cannot restart an ended round. |

Rainbow coins add 5 moves/s to the selected base speed; blue coins allow border wrapping. Effects end on the next coin, level change or restart. Silver adds obstacles and appears only when no obstacles exist. Each mode starts with an ordinary coin. Challenge has an 8.5 moves/s base rate. All three Endless speeds share one personal best; Challenge has a separate record.

### Local personal records

Personal bests are stored in `%USERPROFILE%\.comp2042-snake\high-scores.properties` (the Java user-home directory on other platforms), outside the project and generated JAR. IDE and packaged launches under the same user account share them. There are two records: Endless and Challenge; Challenge uses accumulated run points including combos, across any selected starting level. A partial or failed run can set a record. Records start at zero and are saved as soon as a score improves.

The file is replaced through a temporary file, using an atomic move where supported. Missing entries start at zero; invalid numeric entries are ignored independently. Read/write failure is logged and gameplay continues with an in-memory record; pending saves are retried when restarting, navigating or closing the game. A record cannot survive application exit until a save succeeds. Tests use temporary paths or memory-only stores.

### Controls and objectives

Choose a mode, then an Endless speed or Challenge starting level. Press Start and an arrow key to move. Avoid walls, obstacles and the snake's body. `P` / `Esc` pauses or resumes; `Tab` restarts. The HUD shows score and movement rate in Endless, or level, coin target, score and combo bonus in Challenge.

After a level transition, press an arrow key to start. In levels 2/3, the first coin reveals keys. Collect a matching key to open a shortcut, or go around the wall ends. Keys take normal play time and can cost the current combo. Gates stay open until restart or level change.

### Verification

Checks on 8 November 2026 used JavaFX 25.0.5 and the Java 23 compilation target.

| Environment | Result |
| --- | --- |
| Local Windows 11 x64, Eclipse Temurin JDK 23.0.2+7 | Clean package build; 255 JUnit test invocations, zero failures, errors or skips. |
| Same local computer, Oracle JDK 27.0.1.0.3 | Clean package build; 255 JUnit test invocations, zero failures, errors or skips. |
| Cao Shiyu's Windows 11 x64 computer, Eclipse Temurin JDK 25.0.5+8 | Manual build, launch and gameplay checks passed, including the final version's Slow, Medium (Default) and Fast options. Confirmed by the project author. |

Automated coverage includes:

| Area | Checks |
| --- | --- |
| Endless speeds | Sixteen cases: ten session/configuration cases, three menu selection/restart/navigation cases and three Timeline speed/effect/pause/restart cases. |
| Menus and timing | 22 lifecycle scenarios. Seven cases check `Tab` from focused pause, game-over and victory buttons. |
| Level transitions | Four cases check timer restoration after rainbow effects. |
| Drawing and fonts | Eight sprite-scaling/fullscreen-restoration cases and one menu/game font-resource integration test. |
| Shortcuts | Two lifecycle/encapsulation cases, eighteen gameplay cases and one rendered HUD/gate case. Six complete model-driven runs cover every starting level with all keys collected and with every key avoided. |
| Mode rules | Twelve cases cover endless play beyond challenge targets, all five coin categories, reset, mode switching and menu navigation from each challenge starting level. |
| Personal bests | Sixteen cases cover store reloads, separate mode records, ties/lower scores, malformed entries, failed saves, live HUD/menu updates, restart, loss/victory overlays and cumulative challenge points. |

Earlier tests cover movement, free-cell generation, effect replacement, level objectives, score retention and combo boundaries. UI tests use JavaFX controls, scene/focused-control key events and the Timeline callback. An injected clock controls time boundaries. Fullscreen-state tests cover exit before starting, while paused and after restarting. The font-resource test checks the original families, weights and sizes and loads the menu without an FXML version warning.

The same JDK 27-built JAR passed `Main` startup checks on Java 23.0.2 and 27.0.1.0.3 with JavaFX 25.0.5+1. Checks covered all three endless speeds, restart, menu navigation, challenge level selection, pixel fonts, pause and `Tab`. Windowed scenes restored to `480 x 480` with a `400 x 400` board. Packaged-game previews covered key appearance, one or both gates open, pause and fullscreen restoration with the separate HUD. These were programmatic checks on the local computer; manual checks on the second computer are recorded below.

Maven and JavaFX emit runtime warnings on JDK 27; the recorded builds and launches completed successfully. The JAR command enables native access for JavaFX.

### Second-computer validation

**Result: passed.** The project author confirmed these manual checks on 8 November 2026.

| Item | Environment or result |
| --- | --- |
| Computer | Cao Shiyu's computer, separate from the local test machine |
| Operating system | Windows 11 x64 (`amd64` in Maven output) |
| JDK | Eclipse Temurin 25.0.5+8 LTS |
| Maven | 3.9.11, supplied by the project wrapper |
| Project configuration | JavaFX 25.0.5; Java compilation target 23 |
| Commands | `java -version`, `javac -version`, `.\mvnw.cmd -v`, `.\mvnw.cmd clean package`, `.\mvnw.cmd javafx:run` |
| Result | Build, launch, both modes, pause/restart, level progression and personal-best saving passed. The final version's Slow, Medium (Default) and Fast options also passed. |

No exact JDK 25 JUnit count is recorded because its final Surefire summary was not supplied. The 255-test counts above are from the local JDK 23 and 27 builds.

Maven initially reported JDK 25 with runtime home `D:` and failed with `release version 23 not supported`, although `D:\lib\ct.sym` existed. Setting an absolute Java home in the same PowerShell session fixed compilation and launch:

```powershell
$env:MAVEN_OPTS = "$env:MAVEN_OPTS -Djava.home=D:\"
```

The result suggests a path-resolution issue with the drive-root JDK installation. This setting applies to that `D:\` installation and terminal session. On another computer, use its own JDK directory for `JAVA_HOME`, `PATH` and the IDE runtime.

## 4. Implemented but Not Working Properly

No gameplay failures are known from the recorded checks. Fixed issues and regression tests are listed in section 8.

## 5. Features Not Implemented

All planned gameplay features are implemented, including the Endless speed selector.

The remaining review checks, AI declaration, demonstration video and packaging are listed in the [submission checklist](docs/SUBMISSION_CHECKLIST.md).

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
| `GameSpeed` | `game/GameSpeed.java` | Defines the three endless base speeds and menu labels without changing wall or scoring rules. |
| `Level` | `game/Level.java` | Defines each level's target, obstacle layout and successor. |
| `Shortcut` | `game/Shortcut.java` | Owns hidden, available and open states for a matching key/gate pair. Each level creates fresh instances. Its private `State` enum prevents reopening/closing transitions. |
| `ShortcutSnapshot` | `game/ShortcutSnapshot.java` | An immutable record of integer coordinates, key visibility and gate state for rendering without exposing mutable model data. |
| `ComboTracker` | `game/ComboTracker.java` | Encapsulates the five-second active-time window and capped bonus. |
| `GameFlow` | `ui/GameFlow.java` | Separates ready, running, paused and ended UI states. Its private `State` enum preserves whether a paused game had started. |

Four `package-info.java` files describe the entry-point, game, UI and persistence packages. `CoinFactory`, `CoinEffectService`, `Shortcut`, `ComboTracker` and `GameFlow` are package-private helpers.

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
| `GameSpeedTest` | `game/GameSpeedTest.java` | Three selected base rates, normal-wall collisions, rainbow replacement and reset behavior. |
| `GameSessionTest` | `game/GameSessionTest.java` | Rule coordination, death, wins and legacy-round reset. |
| `DirectionAndEncapsulationTest` | `game/DirectionAndEncapsulationTest.java` | Direction behavior and protected coordinate snapshots. |
| `EndlessModeTest` | `game/EndlessModeTest.java` | Continuous play beyond level targets, original coin values/effects and same-mode reset. |
| `HighScoreStoreTest` | `persistence/HighScoreStoreTest.java` | Mode-isolated persistence, input validation, corrupted entries and save-failure recovery. |
| `HighScoreIntegrationTest` | `ui/HighScoreIntegrationTest.java` | Real controller/HUD/menu integration, end-state records, restart and cumulative challenge scoring. |
| `MainMenuModeTest` | `ui/MainMenuModeTest.java` | Real FXML mode/level/speed selection, option visibility and layout, launching, speed retention on restart and menu navigation. |
| `LevelProgressionTest` | `game/LevelProgressionTest.java` | Objectives, chosen start, score retention and reachable layouts. |
| `ComboTrackerTest` | `game/ComboTrackerTest.java` | Boundaries, capped bonuses, pause accounting and resets. |
| `GameControllerTest` | `ui/GameControllerTest.java` | Direction/key mappings and reversal rules. |
| `GameFlowTest` | `ui/GameFlowTest.java` | Ready, paused, running and ended transitions. |
| `GameControllerTimingTest` | `ui/GameControllerTimingTest.java` | Actual timer-rate restoration at level transitions and all endless presets through rainbow effects, pause/resume and restart. |
| `ShortcutTest` | `game/ShortcutTest.java` | Key activation, one-time opening and coordinate/snapshot isolation. |
| `ShortcutGameplayTest` | `game/ShortcutGameplayTest.java` | Key effects, gate collisions, legal placement, all gate-state layouts, pause/countdown boundaries and complete runs with/without keys. |
| `GameViewShortcutTest` | `ui/GameViewShortcutTest.java` | HUD separation from the board, countdown, visible gate/key status and untruncated menu text. |
| `GameViewScalingTest` | `ui/GameViewScalingTest.java` | Rendered straight/corner sprite scales and fullscreen exit while ready, paused or restarted. |
| `GameControllerLifecycleTest` | `ui/GameControllerLifecycleTest.java` | Keyboard/menu actions with pause timing, restart, end states and focused-button `Tab` routing. |
| `ViewManagerResourcesTest` | `ui/ViewManagerResourcesTest.java` | Actual menu/game font families, weights and sizes, and absence of an FXML version warning. |
| `JavaFxTestSupport` | `ui/JavaFxTestSupport.java` | Shared JavaFX initialization and application-thread assertions. |

## 7. Modified Java Classes

These thirteen classes come from the starter project; moving them into `game` or `ui` does not make them new classes. Paths are relative to `src/main/java/com/comp2042/`.

| Original class | Current location | Changes and rationale |
| --- | --- | --- |
| `Main` | `Main.java` | Delegates to JavaFX explicitly to support Maven and IDE classpath launches. |
| `Game` | `game/Game.java` | Restores score and win thresholds on reset; distinguishes collected coins from point score and retains run points across levels. Recalculates board capacity when gates open without changing points or coin counts. A speed-preset constructor changes only the endless base rate while preserving normal wall rules. |
| `Snake` | `game/Snake.java` | Removes image rendering, uses `Direction`, protects returned coordinates, resolves wrapping before obstacle collision and resets movement state through new instances. |
| `Coin` | `game/Coin.java` | Removes image ownership, protects position snapshots, finds free cells without unbounded retry loops and supplies the effect/category contract. |
| `BlueCoin` | `game/BlueCoin.java` | Implements temporary wrapping through its effect override and reports its category. |
| `GoldCoin` | `game/GoldCoin.java` | Retains the fifteen-point value and reports its category for factory and view selection. |
| `RainbowCoin` | `game/RainbowCoin.java` | Encapsulates the five-move speed bonus and identifies the restored rainbow selection branch. |
| `SilverCoin` | `game/SilverCoin.java` | Adds safe obstacles and adjusts the threshold by the actual number placed, rather than a requested amount that might not fit. |
| `Obstacle` | `game/Obstacle.java` | Fixes off-by-one coordinates, duplicates and occupied-cell overlap; provides validated fixed layouts and defensive snapshots. |
| `GameController` | `ui/GameController.java` | Delegates rules to `GameSession`; validates input and coordinates timer, pause, restart, level changes and menus. The next-level path restores the timer's base rate after rainbow effects. A window fullscreen-state listener resizes the current session's board; model refresh includes the current shortcut snapshots. Records score improvements through an injected store and preserves records before reset/navigation/window hiding. |
| `GameView` | `ui/GameView.java` | Owns snake/coin sprites, keeps canvas details private, renders corner state and presents HUD/menus. Fullscreen sprite sizes use one scale factor; windowed dimensions are restored on exit. Draws pixel keys/gates, keeps HUD/footer outside the board and displays mode-specific guidance, the current endless movement rate, personal bests and shared-clock challenge combo time. |
| `MainMenuController` | `ui/MainMenuController.java` | Initializes mode/level/speed selection, defaults to Medium, retains the selected endless speed, hides it in Challenge, shows mode-specific guidance and wires a fresh session, controller, view and menu actions. Shows the selected mode's saved best and shares the record store with its controller. Captures game key events before focused controls can consume `Tab` for navigation. |
| `ViewManager` | `ui/ViewManager.java` | Owns menu/game windows and injects navigation through an instance instead of shared global window state. Registers the original pixel fonts from resource streams before loading the menu. Owns the application's shared record store and refreshes the menu record when returning from a round. |

Coin construction is separated through a small factory. Effects use ordinary polymorphic overrides in the coin hierarchy; there is no separate Strategy class. `GameSession` owns rules while the controller schedules them and the view draws them. Coordinate snapshots prevent callers from modifying snake and obstacle internals accidentally.

Level configuration also defines the first coin of levels 2/3 at `(10,3)` and the second coin of level 3 at `(10,16)` to introduce cross-wall routes. Position preferences still use safe placement checks; coin categories retain their existing rules. Keys have their own model and do not inherit coin scoring or growth behavior.

## 8. Unexpected Problems

| Problem | Resolution or current status | Location |
| --- | --- | --- |
| JavaFX 25.0.1 was incompatible with the initially available JDK 21 | Initially aligned the project to JavaFX 21 and repaired the wrapper. After installing JDK 27, upgraded to JavaFX 25.0.5. The final Java 23 compilation target matches JavaFX 25's minimum; full builds and tests passed on JDK 23 and 27. | `pom.xml`, Maven wrapper files |
| Second computer's Maven compilation reported `release version 23 not supported` on JDK 25 | After aligning Maven's JDK, its runtime home was still reported as `D:`. Setting a fully qualified home with `-Djava.home=D:\` in that PowerShell session fixed build and launch. The likely issue was Java home path resolution in the drive-root JDK installation. | Receiving computer's Java/Maven environment; no project code change |
| Unix Maven wrapper had Windows CRLF line endings | Normalized `mvnw` to LF, enforced wrapper line endings in `.gitattributes`, and documented `sh ./mvnw` so ZIP executable permissions do not block invocation. macOS/Linux execution remains unverified. | `mvnw`, `.gitattributes`, compilation instructions |
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
| FXML namespace version warning | Aligned the namespace with the declared JavaFX runtime, now version 25; the real menu integration check loads without this warning. | `src/main/resources/resources/views/MainMenu.fxml`, `ui/ViewManagerResourcesTest.java` in test sources |

Remaining maintainability limitations: model classes still use JavaFX colors, and final-level victory uses the legacy `markBoardFull()` flag. The class diagram and Javadoc show this structure.

## Documentation and submission materials

- [Design.pdf](Design.pdf) shows 27 top-level production types and three nested enums in three A3 landscape views: application/UI/persistence, session/movement/shortcuts and coin inheritance/construction. It includes modes, speeds, personal records and shortcuts, with ownership, dependencies, inheritance and target multiplicities. Attributes and methods are omitted. All three pages were rendered and reviewed against the code.
- [docs/build_design.py](docs/build_design.py) checks source inventory, selected references, inheritance and connector geometry, then regenerates the diagram. Run `python docs/build_design.py` with Python and `reportlab`. Python is only needed for diagram generation. Regenerate and review the PDF after structural changes.
- `Javadoc/` was regenerated on 8 November 2026 with JDK 27.0.1.0.3 and JavaFX 25.0.5. It covers 30 production types (27 top-level and three nested enums) and four packages. Test fixtures are excluded from the diagram and API pages. Generate Javadoc after cloning and include the folder in the submission.
- Javadoc checks covered type/package coverage, declared methods and constructors, and applicable parameter/return sections. All 3,364 local links and assets resolved, including member anchors. Offline browser checks covered the overview, session, movement and personal-record pages. The API pages include `GameSpeed` and the speed-preset `Game` constructor.
- [docs/SUBMISSION_CHECKLIST.md](docs/SUBMISSION_CHECKLIST.md) records completed materials and the remaining manual checks.
- Generative AI assisted debugging, refactoring, feature implementation, testing and documentation. `AI_Usage_Log.docx` is held locally outside this repository. Review it against the assistance used and complete the official COMP2042 AI Use Declaration Form. The log is supplementary and does not replace the form.

The submission folder needs `README.md`, `Design.pdf`, `Javadoc`, an own-voice demonstration video and the complete project ZIP. Use the [checklist](docs/SUBMISSION_CHECKLIST.md) for remaining checks, student details and packaging.
