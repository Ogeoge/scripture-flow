# ScriptureFlow Android App

This directory contains the source code for the ScriptureFlow Android application. It is a fully offline-first Bible reader built with Kotlin, Jetpack Compose, and Material 3.

## Project Structure

-   `app/src/main/java`: Main application source code (Kotlin).
-   `app/src/main/assets`: **Required placeholder for data assets.**
-   `app/src/test/java`: Unit tests.
-   `app/src/androidTest/java`: Instrumented tests.

**Note:** Per generation constraints, standard Gradle build files (`build.gradle.kts`, `settings.gradle.kts`, `gradle/wrapper`, etc.) are not included in this output. You will need to provide a standard Android application build setup for this source code to be compilable.

## Required Assets (CRITICAL)

The application requires two JSON asset files to function. These files **must** be placed in the `android/app/src/main/assets/` directory.

You must create the following directory structure and add the corresponding files:

1.  **Bible Text (`kjv.json`)**:
    -   **Path:** `android/app/src/main/assets/bible/kjv.json`
    -   **Format:** Must strictly follow the `BibleAsset` schema defined in `CONTRACT.md`.

2.  **Verse of the Day Schedule (`votd.json`)**:
    -   **Path:** `android/app/src/main/assets/votd/votd.json`
    -   **Format:** Must strictly follow the `VerseOfTheDayScheduleAsset` schema defined in `CONTRACT.md`.

**The app will crash on startup if these files are missing or incorrectly formatted.**

## How to Run

1.  Ensure you have the required assets placed correctly as described above.
2.  Open the `android` directory in Android Studio.
3.  Allow Gradle to sync the project dependencies.
4.  Select the `app` run configuration.
5.  Choose an emulator or connect a physical device.
6.  Click the "Run" button.

## How to Run Tests

Unit tests are located in `app/src/test/java/`. They cover critical business logic like the search engine and database access objects (DAOs).

To run the tests from the command line, navigate to the `android` directory and execute:

```sh
./gradlew testDebugUnitTest
```

Alternatively, you can run them through the Android Studio UI by right-clicking the `test` directory and selecting "Run tests".
