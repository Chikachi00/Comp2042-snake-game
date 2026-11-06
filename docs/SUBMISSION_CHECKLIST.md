# COMP2042 submission checklist

Documentation status reviewed on 7 November 2026 for `feature/gameplay-enhancements`. The latest automated build below was run on 6 November 2026.

## Prepared materials

- [x] README follows the eight required sections and identifies new and modified classes.
- [x] Production source has class and method Javadoc, including parameters and return values.
- [x] The Javadoc generation script is tracked. Generated `Javadoc/` is local and ignored by Git; a fresh clone must generate it before final packaging.
- [x] A local `Design.pdf` draft and `docs/build_design.py` generator are available; both remain uncommitted while the design material is being reviewed.
- [x] Automated verification: 190 JUnit test invocations passed with no failures, errors or skips in the latest clean Maven package build.
- [x] Fullscreen restoration, focused-button `Tab`, pixel-font loading and the FXML version warning have regression coverage. The packaged JAR was also checked for the original fonts.
- [x] AI assistance is acknowledged in the README. The separate usage log is held locally for review and updating.

## Student review and manual verification

- [ ] Play from each of levels 1, 2 and 3; complete a full run and check the final score.
- [ ] Check wall, obstacle and self collisions, special coins and coin visibility.
- [ ] Check `P`, `Esc`, `Tab` and every pause/end-menu button.
- [ ] Check native fullscreen entry and exit, pause layout and fonts on the intended display.
- [ ] Build and play the final version on another computer. Record the OS, JDK, build command and result; this has not yet been done.
- [ ] Read the implementation and prepare to explain movement, effect replacement, progression, combo timing and tests.
- [ ] Review the local design draft against the final code and finalize `Design.pdf` for submission, showing the required relationships and multiplicities.
- [ ] Regenerate `Javadoc/` from the final source using `scripts/generate-javadoc.ps1` and check the generated pages. Older local HTML must not be treated as current after code changes.
- [ ] Review the AI log against the actual conversations, add any missing assistance, enter student details and sign it.
- [ ] Complete the official COMP2042 AI Use Declaration Form required by the module. The usage log is supplementary.
- [ ] Confirm the assessor can access the GitHub repository; if it is private, add `kooitt` as required by the brief.
- [ ] Review and merge the gameplay branch into `main`, update README branch/clone instructions for the final repository state, then synchronize and verify the final version.

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
