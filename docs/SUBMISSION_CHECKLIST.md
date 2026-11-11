# COMP2042 submission checklist

Updated on 12 November 2026 after input, collision, boundary and fullscreen/rendering fixes. The [README](../README.md#verification) records current local builds and historical manual results. The 8 November second-computer run and 9 November archives predate these fixes; rebuild the submission and repeat final-version checks before upload.

## Prepared materials

- [x] README contains the eight required sections and lists new and modified classes.
- [x] Production classes and methods have Javadoc, including parameters and return values.
- [x] Javadoc regenerated on 12 November 2026 with JDK 27.0.1.0.3 and JavaFX 25.0.5. Markup checks passed without warnings. Generated HTML is ignored by Git; generate it after a fresh clone.
- [x] Javadoc covers 30 production types and four packages, including declared methods/constructors and applicable parameter/return sections. All 3,384 local links and assets resolved. Earlier offline navigation and representative-page browser checks remain historical evidence.
- [x] `Design.pdf` covers 27 top-level types and three nested enums in three A3 landscape views. Source inventory, references, inheritance and layout were checked; all pages were rendered and reviewed. `docs/build_design.py` regenerates the diagram and checks it against the source.
- [x] Clean package builds on Eclipse Temurin JDK 23.0.2+7 and Oracle JDK 27.0.1.0.3 with JavaFX 25.0.5 passed 255 JUnit test invocations each, with zero failures, errors or skips.
- [x] On 12 November, the updated suite passed all 278 invocations in local clean package builds on JDK 23.0.2+7 and 27.0.1.0.3. The 23 added cases cover held-key/release behaviour, fullscreen controls, small-board silver effects, difficulty validation, short-snake collision and monitor selection. Seven before/after canvas comparisons matched exactly after sprite caching.
- [x] The same Java 23-target JAR built with JDK 27 launched on JDK 23.0.2 and 27.0.1.0.3 with JavaFX 25.0.5+1. Challenge level selection, fonts, the 480 x 480 window, pause and `Tab` restart passed on the same local computer.
- [x] `mvnw` uses LF endings enforced by `.gitattributes`. README includes `sh ./mvnw` and target-OS/CPU build instructions. macOS/Linux remain untested.
- [x] Regression checks cover fullscreen restoration, focused-button `Tab`, pixel fonts and the FXML version warning. Original fonts also loaded from the packaged JAR.
- [x] Shortcut/countdown checks include complete automated runs from all three starting levels, with all keys and without keys. Scene previews and native fullscreen restoration were checked. The three shortcut states are documented.
- [x] Slow/Medium/Fast use 5/8.5/13 moves/s; Medium is the default. Sixteen cases cover rules, menu selection/retention/layout, Timeline rates, rainbow effects, pause/resume and restart. Packaged-JAR checks passed on local JDK 23 and 27.
- [x] Twelve mode cases cover Endless/Challenge selection, original endless scoring/effects and menu navigation.
- [x] Sixteen personal-best cases cover separate mode records, persistence, storage/controller/menu integration, failed saves and challenge totals.
- [x] README acknowledges AI assistance. The completed AI usage log is included with the submission.
- [x] Local manual play-testing passed on 7 November 2026, as reported by the project author.
- [x] Second-computer build, launch and gameplay passed on Cao Shiyu's Windows 11 x64 computer with Eclipse Temurin 25.0.5+8 and Maven 3.9.11 on 8 November 2026, including the final three-speed options. README records the session-only Java home correction. The exact JDK 25 JUnit count was not supplied.
- [x] Accidental terminal-help file removed in `cf22eb3`. Generated documentation, build output and personal scores are excluded from tracked files.
- [x] Gameplay PR #3 and endless-speed PR #4 merged into `main`; local `main` synchronized. README clones `main`. JavaFX dependencies and FXML namespace use version 25, compilation targets Java 23, and `GameSpeed` supplies the three menu options and current-rate HUD.

## Manual review and remaining tasks

The second-computer run is complete. Unchecked items below are detailed review prompts; individual outcomes have not been recorded.

- [x] Final-version Slow, Medium (Default) and Fast checks passed on Cao Shiyu's computer (Windows 11 x64, JDK 25), confirmed by the project author on 8 November 2026.
- [ ] Play Endless beyond ten coins; check special-coin effects, restart and switching to Challenge.
- [ ] Set a best in each mode, close/reopen the game, and check the menu, HUD and final-score display. Check that restart keeps the records.
- [ ] Play from levels 1, 2 and 3; complete a run and check the final score.
- [ ] Check wall, obstacle and self collisions and coin visibility. Try no keys, one key and both keys; assess the shortcut routes.
- [ ] Check `P`, `Esc`, `Tab` and every pause/end-menu button.
- [ ] Check held `P`/`Esc`/`Tab`, release and repress, and focus loss on the final version. In fullscreen, Esc pauses/resumes; `-` exits fullscreen even while paused.
- [ ] Check physical fullscreen entry/exit, pause layout and fonts on the intended display and a second monitor where available. Synthetic monitor-selection tests are not physical multi-monitor verification.
- [x] Another-computer build and play-test completed on 8 November 2026 using `.\mvnw.cmd clean package` and `.\mvnw.cmd javafx:run`; final-speed checks also confirmed. See [README: Second-computer validation](../README.md#second-computer-validation). Keep available logs and screenshots with the test evidence.
- [ ] Repeat the second-computer build and relevant gameplay checks after the 12 November fixes; retain the earlier successful result as historical evidence.
- [ ] Prepare for Q&A: movement, effect replacement, levels, combo timing, tests, class relationships, ownership and multiplicities.
- [x] The project author supplied `AI_Usage_Log_LinYiwei.docx` on 9 November 2026, with five grouped records, student details, a Yes selection and a typed name/date. The packaged copy matches that supplied file.
- [ ] Update the AI usage record to include the 12 November maintenance assistance before final packaging.
- [ ] Confirm that the supplied log is the form required by the module; follow any separate COMP2042 AI Use Declaration Form instructions on Moodle.
- [ ] Commit and push the 12 November fixes, then confirm that GitHub contains the final tested version. The public repository URL remains in the README.

## Video and final packaging

- [x] `Demo.mp4` recorded and reviewed: 2 min 39.95 s, MP4/H.264, 149,804,615 bytes. It shows gameplay, refactoring, additions and two proud achievements with the author's narration. The compressed file decoded without errors and retained the original audio.
- [x] Submission name confirmed as Lin Yiwei; filenames use `LinYiwei`, IDE `VSCode` and Java `27` (development JDK 27.0.1.0.3).
- [ ] Rebuild `LinYiwei_VSCode_27.zip` with the updated source, tests and documentation, then build the extracted copy. The previous archive passed 255 tests on 9 November and predates the fixes.
- [x] Created `COMP2042LinYiwei` containing:

  ```text
  README.md
  Design.pdf
  Javadoc/
  Demo.mp4
  LinYiwei_VSCode_27.zip
  AI_Usage_Log_LinYiwei.docx
  ```

- [ ] Refresh the outer README, Javadoc and project ZIP so they match the final source. The production class relationships are unchanged; regenerated Design.pdf matched the existing diagram byte-for-byte on 12 November.
- [ ] Rebuild and inspect `COMP2042LinYiwei.zip` with the required folder structure and a size below 250 MB. The existing archive predates the fixes.
- [x] The completed AI usage log is present in the outer folder and project archive; all previously required technical materials are retained.
- [ ] Check the module's declaration instructions and submit the final coursework ZIP to Moodle. No Moodle submission has been made by the assistant.

The coursework PDF gives the deadline as 7 December 2026 at 23:59. Check the module submission page for later announcements.
