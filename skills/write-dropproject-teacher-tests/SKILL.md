---
name: write-dropproject-teacher-tests
description: Write or review the teacher unit tests that grade a Drop Project assignment - how many test functions and sub-cases each API function needs, how much feedback every assertion should give for the assessment type at hand, and the patterns that stop hardcoded solutions from scoring full marks. Use when asked to write, improve or review the tests of a Drop Project assignment, or when a teacher asks how an exercise should be tested.
---

# Writing teacher tests for a Drop Project assignment

Teacher tests do two jobs at once. They produce the grade, and they are the only feedback a student
gets while working on the exercise. Both jobs are easy to fail, in opposite directions:

- **Too little feedback** and students cannot act on a failing test. They go ask the teaching staff,
  which cancels the two reasons for automating assessment in the first place: student autonomy and
  teacher time.
- **Too much feedback** and the test becomes a specification of its own input/output pairs. A
  student who never solved the exercise hardcodes those pairs and scores full marks.

Every recommendation below is about placing a suite between those two failures. They come from
*Seven Years Later: Lessons Learned in Automated Assessment* (Cipriano and Alves, ICPEC 2024,
[doi:10.4230/OASIcs.ICPEC.2024.3](https://doi.org/10.4230/OASIcs.ICPEC.2024.3)), which distils seven
years of running Drop Project across introductory programming, data structures and OOP courses.

This skill is about the *content* of the tests. `create-dropproject-assignment` covers getting the
assignment repository built, registered and validated.

## Step 1: establish the assessment type

**Do this before writing a single assertion.** The assessment type decides how much feedback the
messages may give, whether any test may be hidden, whether any test may be mandatory, and whether
tests may be gated behind each other. Get it wrong and the whole suite is wrong in a way no amount
of later polishing fixes: a mini-test written like a weekly exercise hands out the answers, and a
weekly exercise written like a mini-test sends the class to the teaching staff.

It is rarely inferable from the repository, so **ask the teacher outright** unless they already said
it. The four types, with the names teachers use for them:

| Type | Also called | What it is |
|---|---|---|
| Weekly exercise | ficha, ficha semanal, in-class exercise, homework | small, formative, done with support from teachers and colleagues, little or no weight in the final grade |
| Mini-test | mini-ficha, mini-teste | small, individual, no support, time-bound (about an hour), counts towards the grade |
| Project | projeto | weeks or months of work, large weight in the grade |
| Defense | defesa | proctored session where the student changes their own project's code, to show they wrote it |

The two axes that follow from the answer are **is the student under a clock** (mini-test, defense)
and **does the assessment carry real weight** (everything but the weekly exercise). Read
[Feedback level per assessment type](#feedback-level-per-assessment-type) next, then come back for
the rest of the inputs.

## Step 2: the rest of the inputs

Ask for whatever is missing - do not guess them:

1. **The API** the students must implement: classes, function signatures, return types, and what
   counts as an invalid input.
2. **The language of the feedback messages.** They are read by students, so they must match
   `instructions.md`, not the language of the code.
3. **For a defense**, the project it defends: its public tests, its hidden tests, and which of its
   tests were mandatory. See [Defenses need extra care](#defenses-need-extra-care).

### Tests shape the API, so review it first

Writing the tests before the exercise statement is frozen is not a detour, it is the point. A
function that takes no parameters and returns nothing can only be tested through side effects, and
a function with a single fixed input can only be tested once. If the API makes the rules below
impossible to apply, change the API:

> Give every function at least one parameter. The more parameters, the richer the test cases.

## Feedback level per assessment type

|  | weekly exercise | mini-test | project | defense |
|---|---|---|---|---|
| Under a clock | no | yes | no | yes |
| Hidden tests | no | no | yes | no |
| Feedback | all or nearly all assertions carry significant feedback | some feedback, but the last assertion of each test function gives no hint about input or expected output | detailed in the public tests, none in the hidden ones | as mini-test |
| Mandatory tests | no | no | yes, a subset of the *public* tests | reuse the project's |
| Progressive disclosure | yes, if the exercise has a hard tail | no | yes | no |

Three rules deserve spelling out:

- **All test functions are public except in projects.** In a mini-test or a defense the student is
  under time pressure and needs a real notion of how they are doing. Hiding results there just adds
  stress without discouraging anything.
- **Hidden tests are never mandatory.** Students do not know a hidden test exists, so they cannot
  act on it. Failing a mandatory test they cannot see is a grade they cannot recover.
- **Never gate tests in a time-bound assessment.** Progressive disclosure assumes the student can
  come back and unlock the rest. In a mini-test or a defense they cannot: one early mistake they run
  out of time to fix takes every gated test down with it, and the grade stops measuring what they
  did. Gate only in exercises and projects, where the clock is measured in days.

### Defenses need extra care

A defense asks the student to change their own project during a proctored session, so its tests run
against code that already has bugs. Setting the whole thing up - deriving the repository, designing
the changes, the instructions, the two phases on the day - is the `create-dropproject-defense-assignment`
skill; what follows is only what it means for the tests. To avoid grading last month's bugs again:

- Build the defense around the project's **mandatory functionality**. If the project defined no
  mandatory tests, at least avoid scenarios that were only covered by the project's *hidden* tests -
  the student never got a chance to fix those.
- **Adapt the project's public tests** rather than writing fresh ones: same input files, similar
  action sequences. The student has seen those pass.
- **Watch for interdependence.** If the defense asks the student to change `toString()`, no other
  defense test may use `toString()` as its oracle.
- **Avoid the known pitfalls of the domain.** In a project about people's names, students routinely
  support "Samuel Jackson" but not "Samuel L. Jackson"; a defense test that trips on that measures
  an old bug, not today's work.

## The construction rules

Full bad/good code pairs for each of these are in
[references/test-patterns.md](references/test-patterns.md). A complete worked test class that
applies all of them at once is in
[examples/TestTeacherStore.java](examples/TestTeacherStore.java).

### 1. At least two independent test functions per API function

Split each API function into test functions by *input case*: empty or null input, out-of-range
argument, single element, several elements, and so on. Three benefits, all of which matter more in
teaching than the industry convention of one test per function:

- **Partial credit.** A student who got the base case right but not the recursive one does not
  score zero.
- **Gradual completion.** Tests of increasing difficulty give the student a next step, which is the
  useful half of gamification.
- **The name is a hint.** `test_003_equalArraysSingleElement()` tells the student what broke before
  they read a single assertion message.

### 2. At least two sub-cases per test function, with different expected values

One assertion is enough to make a test that a constant passes. This is the most common way a suite
lets a hardcoded solution through, and it is worst for booleans:

```java
// a solution whose isOpen() is `return true;` passes this
door.open();
assertTrue(door.isOpen(), "isOpen() incorrectly returned false");
```

Add the opposite case in the same test function, so no constant satisfies both:

```java
door.open();
assertTrue(door.isOpen(), "isOpen() incorrectly returned false");
door.close();
assertFalse(door.isOpen(), "isOpen() incorrectly returned true");
```

The same holds for numbers and strings: vary the expected value, not just the input.

### 3. Every assertion carries a message that names the function

An assertion with no message produces `AssertionFailedError: expected: <5> but was: <0>`, which
does not say *which* function is wrong. In an OOP test that calls half a dozen methods, the student
has no way to start.

The minimum is the function name: `"getSalary() returned the wrong value"`. When a bare function
name would mislead, say more:

- **A function whose return value is a structure**: point at the position, not the function.
  `assertEquals(42, getStatistics()[1], "getStatistics()[1]")`, because
  `getStatistics() expected: 42 but was: 99` reads as if `getStatistics()` returned an `int`.
- **A boolean assertion about a non-boolean function**: `assertTrue(found, "getTitles()")` reports
  `Expected: true Actual: false` about a function that returns a list. Name the missing value
  instead: `assertTrue(found, "getTitles() missing title: " + title)`.

The exception is a return format so distinctive that the student cannot mistake it, typically a
`toString()` whose layout is unlike any other object in the exercise.

### 4. Taper the feedback inside the test function

This is how rule 3 and the anti-overfitting goal are reconciled. Within one test function, start
generous and end silent:

```java
// early sub-cases: full arguments in the message
assertEquals(5, Main.sum(new int[]{1, 4}), "sum({1, 4}) returned an incorrect value.");
// middle: a hint, no arguments
assertEquals(-6, Main.sum(new int[]{-1, -2, -3}),
        "sum(...) returned an incorrect value. Consider arrays with negative numbers.");
// last: function name only
assertEquals(9, Main.sum(new int[]{1, 2, 3, 4, -1}), "sum(...) returned an incorrect value.");
```

The student debugging honestly gets what they need from the first sub-cases. The student
hardcoding gets nothing to hardcode from the last one. In a mini-test or a defense, the last
assertion of *every* test function should be silent like this.

### 5. Assert early and often

Assert after each action, not once at the end of a sequence. OOP objects change state with every
call, so an error in the first action distorts everything that follows and the resulting message
points at the wrong place.

### 6. Guard null references before dereferencing them

A `NullPointerException` inside a test is one of the least actionable failures a student can get.
`assertNotNull` first, with a message that says which call returned null.

### 7. In collections, check a few elements before checking the size

Asserting `size()` up front tells the student "wrong number of elements" and nothing about which.
Validate the first elements, guarding each index with a size check, then assert the total size
last. `IndexOutOfBoundsException` is as unhelpful as `NullPointerException`.

### 8. Name and number the tests, ordered by difficulty

Use `test_001_`, `test_002_` prefixes plus a name that hints at the case. Both practices are against
industry convention and both earn their place here: the prefix makes the test obvious in a
stacktrace, the number makes it possible to say "look at test 4" to a student, and the name is free
feedback.

Tests are supposed to be independent, but students work through the report top to bottom regardless.
Order them by difficulty so that order is a study plan.

### 9. A timeout on every test function

Required by Drop Project's validator, and the reason is not only infinite loops: a timeout is how a
suite detects an implementation whose complexity is wrong.

### 10. Progressive disclosure, in untimed assessments with a hard tail

Advanced tests can be gated behind the simple ones with a static counter, so the report does not
bury a beginner under failures they are not ready for:

```java
static int passed = 0;   // each simple test ends with passed++

@Order(12)
@Test
void adicionarVariosProdutosIguais() {
    if (passed < 9) {
        fail("This test only runs once tests 1 to 9 pass");
    }
    // ...
}
```

**Tell the students in `instructions.md` that these tests exist and what unlocks them.** A test that
silently never runs is worse than no test. This rule needs rule 8: the gated tests must come after
the ones that unlock them, or the report reads out of order.

**Only in in-class exercises, homework and projects.** Gating costs a student nothing when they have
days to unlock it, and costs them the gated marks outright in a mini-test or a defense, where the
hour runs out first.

### 11. Non-functional requirements are tested too, but not with assertions

"Use recursion", "do not use `instanceof`", "this class must be abstract" are requirements students
evade, and no ordinary assertion catches evasion. Two tools:

- **Checkstyle** for forbidden constructs, via `RegexpSinglelineJava` in `checkstyle.xml`. The
  message is shown to the student:

  ```xml
  <module name="RegexpSinglelineJava">
      <property name="format" value="System\.exit"/>
      <property name="ignoreComments" value="true"/>
      <property name="message" value="System.exit() is not allowed. Throw an exception instead."/>
  </module>
  ```

- **Reflection** for structural requirements: that a method exists with the required signature,
  that a class is abstract, that a field is private. Fail with a message that states the expected
  signature in full, since the student cannot guess it from a `NoSuchMethodException`.

## Drop Project mechanics that affect the tests

| Concern | What Drop Project requires |
|---|---|
| Class names | Teacher tests live in `src/test` in classes named `TestTeacher*`. Hidden tests go in `TestTeacherHidden*` and need `hiddenTestsVisibility` set on the assignment, usually `SHOW_PROGRESS` |
| Timeouts | Every test method. JUnit 4: `@Test(timeout = 500)`. JUnit 5: `@Timeout(1)` on the class or the method |
| Ordering, within a class | JUnit 4: `@FixMethodOrder(MethodSorters.NAME_ASCENDING)` plus numbered names. JUnit 5: `@TestMethodOrder(MethodOrderer.OrderAnnotation.class)` plus `@Order(n)` - keep `@Order(n)` and the number in the name in sync |
| Ordering, between classes | Only matters with more than one test class, e.g. a defense that keeps the original suite. In JUnit 5, `@Order(n)` on a *class* is **silently ignored** unless the class orderer is switched on; the annotations look right and do nothing. Turn it on in the surefire configuration of the `pom.xml` (see below) |
| Mandatory tests | `mandatoryTestsSuffix` on the assignment matches a suffix on the test *method* names, e.g. `_OBG` on `test004_LogicError_OBG()`. A submission failing any of them is not considered valid. Never put the suffix on a hidden test |
| Stacktraces | Set `packageName` on the assignment, otherwise students see the whole stacktrace instead of their own frames |
| Shared fixtures | Put the builders shared by public and hidden tests in a plain `TestTeacherCommon` class the test classes extend. Everything under `src/test` must still start with `Test` |

### Switching on the JUnit 5 class orderer

`@Order(n)` on a test class does nothing on its own, and nothing warns you - the report just comes
out in whatever order the engine picked. Configure the orderer in the surefire plugin, so that it
travels with the assignment's `pom.xml` rather than a `junit-platform.properties` that Drop Project
may not copy:

```xml
<plugin>
    <artifactId>maven-surefire-plugin</artifactId>
    <configuration>
        <argLine>${dp.argLine}</argLine>
        <trimStackTrace>false</trimStackTrace>
        <properties>
            <configurationParameters>
                junit.jupiter.testclass.order.default = org.junit.jupiter.api.ClassOrderer$OrderAnnotation
            </configurationParameters>
        </properties>
    </configuration>
</plugin>
```

Verify it rather than trusting it - the order the classes ran in is in the surefire xml:

```bash
grep -h '<testcase' target/surefire-reports/*.xml | sed -E 's/.*classname="[^"]*\.([^".]+)".*/\1/'
```

### The assertion argument order differs between JUnit 4 and 5

This silently ruins feedback, because both compile:

```java
// JUnit 4 - message FIRST
assertEquals("getSaldo() returned the wrong value", 150, conta.getSaldo());
assertTrue("levanta() should have returned true", conta.levanta(50));

// JUnit 5 - message LAST
assertEquals(150, conta.getSaldo(), "getSaldo() returned the wrong value");
assertTrue(conta.levanta(50), "levanta() should have returned true");
```

Check which one the `pom.xml` pulls in (`junit:junit` vs `org.junit.jupiter:junit-jupiter-engine`)
before writing a single assertion, and never mix the two in one repository.

## Verify the suite before pushing

Running the tests is not enough, because a suite that passes proves nothing about a suite that
grades. Do all three:

1. **The reference solution passes everything.**

   ```bash
   mvn -q test -Ddp.argLine=
   ```

   `dp.argLine` is injected by Drop Project at evaluation time; passing it empty keeps the project
   runnable locally.

2. **A deliberately hardcoded solution fails.** This is the only real check on rules 2 and 4. Write
   the cheapest cheat that satisfies every assertion message the student can read - constants
   returned for the announced inputs, `return true` for booleans - and confirm the suite rejects it.
   If it passes, a sub-case with a different expected value is missing.

3. **The skeleton the students start from fails, and fails legibly.** Run the suite against the
   empty stubs and read the report as a student would. Every message should name a function, no
   failure should be a bare `NullPointerException`, `IndexOutOfBoundsException` or
   `AssertionFailedError: expected: <true> but was: <false>`.

After the assignment has run, hidden tests deserve one more check: **a hidden test that nobody
passes is probably wrong**, and nobody will report it, because nobody can see it. Drop Project shows
per-test pass counts on the assignment's report page.

## Reviewing an existing suite

When asked to improve tests that already exist rather than write new ones, work through
[references/review-checklist.md](references/review-checklist.md) and report findings grouped by
severity. Two things to keep in mind:

- **Changing a test changes grades.** Submissions already evaluated keep the grade they were given
  and are not re-evaluated. Tightening a test mid-semester means two cohorts of students graded by
  different rules, so say so and let the teacher decide.
- Editing the files means push, then `refresh_assignment`. Drop Project does not poll the
  repository.
