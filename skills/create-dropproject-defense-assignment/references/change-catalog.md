# Designing the changes a defense asks for

A defense is four to six changes to code the student already wrote. This is what they should look
like, and how to build two or three versions that are equally hard.

## What a good defense change looks like

- **Small enough to write in ten minutes**, on top of code the student knows. The student is not
  solving a new problem, they are demonstrating that they can navigate their own.
- **Testable from the outside**, through the API the project already exposes. The teacher tests
  cannot see private state.
- **Independent of the other changes**, so a student stuck on change 3 can still do 4, 5 and 6.
  Complete independence is not always achievable; when two changes must interact, put the one that
  the other builds on first, and make the later test not depend on the earlier one passing.
- **Reachable from the project's existing fixtures.** If a change can only be tested by building a
  brand new scenario from scratch, it is probably too far from the project.
- **Answerable in one place.** A change that forces edits in six files is a change that will exhaust
  the line budget and the hour.

## The archetypes

These five carried both LP2 2025/26 versions, and generalise to most OOP projects. A defense that
picks one of each is varied enough to be hard to fake and uniform enough to be graded fairly.

### 1. A new query function over existing state

Add a function that answers a question about the data the project already holds. It tests that the
student can find and traverse their own data structures.

| Version 1 | Version 2 |
|---|---|
| `List<String> getProgrammersBetweenPositions(int p1, int p2)`, both inclusive, format `Nome : Posição`, order irrelevant | `int countToolsBetweenPositions(int p1, int p2)`, both exclusive, excluding tools of type `IDE` |

Both are a filtered traversal with a boundary condition and one twist. Note how the twist differs
(inclusive versus exclusive, plus a type exclusion) so that one student's answer is useless to the
other, while the work is the same.

Two tests: one for the basic cases and the empty result, one for contents and the corner cases.

### 2. A new type in an existing hierarchy

Add a subclass, with an id and a behaviour that overrides something. It tests that the student built
a hierarchy rather than a switch, which is usually the project's main learning objective.

| Version 1 | Version 2 |
|---|---|
| new **tool** `Martelo Dourado`, type id 100, cancels the `Exception` abyss, only for programmers who use C | new **abyss** `Trendy`, type id 150, moves the player back 5 squares only for `Javascript` programmers, no tool cancels it, always prints a message |

A student who wrote `if (type == 3)` everywhere instead of subclassing pays for it here, which is
exactly what the defense is for.

Two tests: one for construction and registration, one for behaviour.

### 3. A change to an output format

Change what an existing function returns, in one specific case, leaving every other case alone. It
tests that the student can find the single place a format is produced, and it is the cheapest place
to check invariant 2 (untouched behaviour survives).

| Version 1 | Version 2 |
|---|---|
| `getSlotInfo(...)` also returns `"V:Y"` for empty slots, Y being the number of programmers there | `getProgrammerInfoAsStr(...)` returns `<Nome>: Blue is IDE` for `Blue` programmers holding an `IDE` |

**Always test both halves**: the new format in the new case, and the old format in every other case.
A test that only checks the new case passes a student who broke the rest.

### 4. A new business or movement restriction

Forbid something that used to be allowed, for a subset of the objects, leaving the rest untouched.
It tests that the student can add a condition without breaking the general path.

| Version 1 | Version 2 |
|---|---|
| `Green` and `Purple` programmers cannot move to squares that are multiples of 5 | a programmer who knows `Java` can never move to squares 5 and 7 |

Spell out in the instructions that the other objects keep the old behaviour, and that the project's
existing restrictions still apply on top. Test both.

### 5. A change to a terminal condition

Change when the thing ends and who wins. It is the largest of the five, because it usually touches
the main loop, so it goes last.

| Version 1 | Version 2 |
|---|---|
| the winner is the first programmer past the halfway point of the board | the game ends when every programmer holds at least one tool, and the winner is the one furthest ahead |

Two tests: one for the end condition, one for the result report. Simplify the scenario in the
instructions ("you may assume the boards used in these tests have no abysses or tools") so that this
change does not become a puzzle about interactions.

## Keeping versions parallel

Versions are only fair if a student cannot be worse off for having been given one of them. Check:

- **the same number of changes**, in the same order, drawn from the same archetypes
- **the same number of tests**, distributed the same way across the changes
- **comparable amounts of code**: compare the divergence Drop Project reports for each version's
  reference solution against the project. Two versions whose reference solutions differ by 20 and 60
  lines are not the same exam.
- **the same parts of the project touched.** If version 1 only requires understanding the board and
  version 2 requires understanding the board and the save/load format, version 2 is harder.
- **no shared answer.** Reading version 1's instructions must not help someone sitting version 2.
  Different numbers alone are not enough; change the shape of the twist.

Write all the versions before implementing any of them. The gaps are visible in the instructions,
and invisible once you are deep in one reference solution.

## What to avoid

- **A change whose oracle is another change.** If change 3 rewrites `toString()`, nothing else may
  assert through `toString()`. Otherwise one mistake fails five tests and the grade stops being a
  measurement.
- **A change that lands on the project's known weak spot.** Every project has a rule most students
  got subtly wrong and the tests never caught. Building on it grades the project again, under a
  clock, with no chance to fix it.
- **A change that only the project's hidden tests ever covered.** The student never saw that fail and
  never got to fix it.
- **Renaming or moving a class.** Renames are not detected by the line-change measurement, so a
  moved file counts as a full deletion plus a full addition and can blow the budget on its own.
- **A change requiring new input files.** The student would have to build the fixture as well as the
  feature, and the clock is already tight.
- **Anything that depends on the GUI**, which is not part of the submission.
