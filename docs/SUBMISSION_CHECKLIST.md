# COMP2042 submission checklist

Documentation status reviewed on 8 November 2026 for the working tree based on `main`. Clean package builds with JavaFX 25.0.5 and the Java 23 compilation target passed on both Eclipse Temurin JDK 23.0.2+7 and Oracle JDK 27.0.1.0.3, on the same Windows 11 x64 computer.

The README, final class diagram and generated Javadoc have been aligned with this production code. The earlier documentation update added comments for the three shortcut states. The runtime upgrade changed the dependencies and FXML namespace. This compatibility update lowers the Java compilation target to 23 and normalizes the Unix wrapper; executable game rules and tests are unchanged. Documentation finalization does not mark the remaining human checks or final submission as complete.

## Prepared materials

- [x] README follows the eight required sections and identifies new and modified classes.
- [x] Production source has class and method Javadoc, including parameters and return values.
- [x] `Javadoc/` was regenerated from the final production source with JDK 27.0.1.0.3 and JavaFX 25.0.5 on 8 November 2026. The generation script fixes navigation to English and passed its Javadoc markup checks without warnings. Generated HTML is local and ignored by Git; a fresh clone must generate it before final packaging.
- [x] Javadoc coverage matches all 29 production types and four packages. Declared methods/constructors and applicable parameter/return sections were checked. All 3,266 local HTML links and asset references resolved, including member anchors; offline browser navigation and representative pages were visually checked.
- [x] The final `Design.pdf` covers 26 top-level production types and three nested enums across three A3 landscape views. Source inventory, selected retained references, inheritance and layout were checked, and all pages were rendered and visually reviewed. `docs/build_design.py` regenerates the PDF and checks the documented structure against the source.
- [x] Automated verification: separate clean Maven package builds on JDK 23 and JDK 27 with JavaFX 25.0.5 passed 239 JUnit test invocations each, with no failures, errors or skips.
- [x] The same Java 23-target JAR built with JDK 27 passed actual `Main` startup checks on both JDK 23.0.2 and JDK 27.0.1.0.3, each with JavaFX 25.0.5+1. Challenge level selection, pixel fonts, 480 x 480 windowed dimensions, pause and `Tab` restart passed. These checks used the same physical computer.
- [x] `mvnw` uses LF line endings, enforced by `.gitattributes`. README documents `sh ./mvnw` for macOS/Linux and explains that native dependencies require a build for the target OS/CPU. Those operating systems have not been tested.
- [x] Fullscreen restoration, focused-button `Tab`, pixel-font loading and the FXML version warning have regression coverage. The packaged JAR was also checked for the original fonts.
- [x] Optional shortcuts and the countdown are implemented and covered, including complete automated runs from all three starting levels with all keys and without any keys. Actual scene previews and native fullscreen restoration were checked.
- [x] Endless/Challenge mode selection is implemented; mode-specific rules, original endless scoring/effects and menu navigation have twelve additional automated cases.
- [x] Independent persistent personal bests for both modes have sixteen additional storage/controller/menu cases, including failure recovery and challenge totals.
- [x] AI assistance is acknowledged in the README. The separate usage log is held locally for review and updating.
- [x] The student reported local play-testing with no issues on 7 November 2026.
- [x] The accidental terminal-help file was removed in `cf22eb3`; generated documentation, build output and personal score records are outside tracked project content.
- [x] Gameplay pull request #3 was merged into `main`, the local `main` was synchronized, and the README now describes cloning `main`.

## Student review and manual verification

Local play-testing was reported complete on 7 November, before the JDK/JavaFX upgrade. Confirm the scenarios below on the upgraded runtime; this can be a brief regression play-test. Another-computer validation remains separate and has not been reported complete.

- [ ] Play Endless beyond ten coins, check original special-coin effects, restart it, and return to the menu to switch to Challenge.
- [ ] Set a record in each mode, close and reopen the game, and check Best in the main menu, HUD and final-score overlay; verify restart keeps it.
- [ ] Play from each of levels 1, 2 and 3; complete a full run and check the final score.
- [ ] Check wall, obstacle and self collisions, special coins and coin visibility; try collecting no keys, one key and both keys, and assess whether the shortcuts make route choices worthwhile.
- [ ] Check `P`, `Esc`, `Tab` and every pause/end-menu button.
- [ ] Check native fullscreen entry and exit, pause layout and fonts on the intended display.
- [ ] Build and play the final version on another computer. Record the OS, JDK, build command and result; this has not yet been done.
- [ ] Read the implementation and final diagram; prepare to explain movement, effect replacement, progression, combo timing, tests, relationships, ownership and multiplicities during Q&A.
- [ ] Review the AI log against the actual conversations, add any missing assistance, enter student details and sign it.
- [ ] Complete the official COMP2042 AI Use Declaration Form required by the module. The usage log is supplementary.
- [ ] Confirm the assessor can access the GitHub repository; if it is private, add `kooitt` as required by the brief.

## Video and final packaging

- [ ] Record `Demo.mp4` (or another permitted format), no longer than three minutes, using the student's own voice. Show the running game, refactoring, additions and two proud achievements.
- [ ] Replace `StudentName` with the student's full name and confirm the actual IDE and Java version.
- [ ] Create `StudentName_IDE_JavaVersion.zip` containing the final project, including source, resources, Maven wrapper and build configuration. Check the archive can be extracted and built. Generated build output can be recreated; do not accidentally omit required project inputs.
- [ ] Create a folder named `COMP2042StudentName` containing:

  ```text
  README.md
  Design.pdf
  Javadoc/
  Demo.mp4
  StudentName_IDE_JavaVersion.zip
  ```

- [ ] Copy the README, diagram and Javadoc from the same final code version into that folder. If the code changes, update all three before packaging.
- [ ] ZIP the outer folder, keep the single upload within Moodle's 250 MB limit, inspect its contents and submit it yourself.

The coursework PDF states the deadline as 7 December 2026 at 23:59. Check the module's submission page for its displayed deadline and any later announcements.
