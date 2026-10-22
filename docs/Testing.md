---
  layout: default.md
  title: "Testing guide"
  pageNav: 3
---

# Testing guide

<!-- * Table of Contents -->
<page-nav-print />

<!-- -------------------------------------------------------------------------------------------------------------------- -->

## Running tests

You can run tests in two ways.

* **Method 1: Using IntelliJ JUnit test runner**
  * To run all tests, right-click on the `src/test/java` folder and choose `Run 'All Tests'`
  * To run a subset of tests, you can right-click on a test package,
    test class, or a test and choose `Run 'ABC'`
* **Method 2: Using Gradle**
  * Open a console and run the command `gradlew clean test` (Mac/Linux: `./gradlew clean test`)

<box type="info" seamless>

**Link**: Read [this Gradle Tutorial from the se-edu/guides](https://se-education.org/guides/tutorials/gradle.html) to learn more about using Gradle.
</box>

--------------------------------------------------------------------------------------------------------------------

## Types of tests

This project has three types of tests:

1. *Unit tests* target the lowest-level methods and classes.<br>
   For example: `seedu.address.commons.StringUtilTest`
1. *Integration tests* check how multiple code units work together; the individual units are assumed to work.<br>
   For example: `seedu.address.storage.StorageManagerTest`
1. *Hybrid tests* combine unit and integration testing. These tests check both the individual units and how they work together.<br>
   For example: `seedu.address.logic.LogicManagerTest`

--------------------------------------------------------------------------------------------------------------------

## GUI tests

Some tests in `seedu.address.ui`, such as `PersonCardTest`, start JavaFX to build real UI components and check
their layout. JavaFX needs a display, so these tests cannot run on a machine without one.

* On Windows, macOS and Linux desktops, they run like any other test.
* The Linux CI runner has no display, so `.github/workflows/gradle.yml` runs Gradle inside `xvfb-run`, which
  provides a virtual display. To do the same on a Linux machine without a desktop, install `xvfb` and run
  `xvfb-run ./gradlew clean test`.
* CI uses a JDK that includes JavaFX 25. If your JDK does not include JavaFX, Gradle uses the JavaFX 17 libraries
  in `build.gradle` instead. Layouts can differ between the two versions, so a GUI test that passes locally can
  still fail in CI.
