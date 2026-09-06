# Reviewing an existing teacher test suite

**Establish the assessment type before opening the first test file** - weekly exercise, mini-test,
project or defense. Ask the teacher if it is not already stated. Half the checks below have a
different right answer for each type, so a review that skips this reports the wrong findings
confidently.

Then work through this in order. The first section is what Drop Project itself rejects, the second
is what lets students score marks they did not earn, the third is what wastes their time and the
teaching staff's.

Report findings grouped by these three severities, each with the file and the test method, and say
what changing it would do to submissions already graded.

## Blocking - the assignment will not validate or activate

- [ ] Every teacher test class under `src/test` is named `TestTeacher*`; hidden ones
      `TestTeacherHidden*`. If the assignment sets `acceptsStudentTests`, *every* teacher class must
      carry the `TestTeacher` prefix.
- [ ] Every test method has a timeout: `@Test(timeout = n)` in JUnit 4, `@Timeout` on the method or
      the class in JUnit 5.
- [ ] There are `TestTeacherHidden*` classes if and only if the assignment sets
      `hiddenTestsVisibility`.
- [ ] `mvn -q test -Ddp.argLine=` passes against the reference solution.

## Grading correctness - a wrong solution could pass, or a right one fail

- [ ] **Every test function has at least two sub-cases with different expected values.** Flag any
      test whose assertions all expect the same value, and every boolean test that only asserts
      `true` or only asserts `false`.
- [ ] **A hardcoded solution fails.** Write the cheapest cheat that satisfies every input/output
      pair named in a visible assertion message, and run the suite against it. Anything it passes is
      a gap.
- [ ] **No mandatory test is hidden.** Check that the `mandatoryTestsSuffix` does not appear on any
      method of a `TestTeacherHidden*` class.
- [ ] **Mandatory tests cover the minimum functionality and nothing more.** They are the pass/fail
      line, not the good-grade line.
- [ ] **Hidden tests do not carry the whole weight of anything students can trivially get wrong**,
      string contents above all. A capitalisation slip caught only by an invisible test is a grade
      the student cannot recover.
- [ ] **Each API function has at least two independent test functions**, so a partial solution gets
      partial credit.
- [ ] Tests do not depend on each other's side effects unless that is deliberate. Shared static or
      instance state without a `@BeforeEach` reset makes results depend on execution order.
- [ ] **No test is gated behind another in a mini-test or a defense.** Search for static counters and
      for a `fail(...)` guard at the top of a test method. Under a clock, a student who runs out of
      time on an early test loses every gated test after it, whether or not they could have passed
      them.
- [ ] For a defense: no test depends on functionality the defense itself asks the student to change,
      and no test covers a scenario that only appeared in the project's hidden tests.
- [ ] **For a defense: nothing the defense asks the student to add is named directly in the test
      source.** A field, constructor or method referenced by name stops the whole submission from
      compiling until the student writes it, so every partial answer scores zero instead of partial
      credit. Reach them by reflection and `fail(...)` with the signature spelled out. Verify by
      running the suite against the defended assignment's reference solution: it must compile.

## Feedback quality - the student cannot act on a failure

- [ ] **Every assertion has a message**, and the message names the function under test.
- [ ] No message reports a boolean about a function that does not return a boolean. Say what is
      missing instead.
- [ ] Assertions on a structured return value name the position, e.g. `"getStatistics()[1]"`.
- [ ] **Every value returned by student code is `assertNotNull`-guarded before it is dereferenced.**
- [ ] **Collections check a few elements, each guarded by a size check, before asserting the total
      size.**
- [ ] Assertions come after each action, not once at the end of a long sequence.
- [ ] Test methods are numbered and named after the case they cover, and `@Order(n)` matches the
      number in the name.
- [ ] Tests are ordered by increasing difficulty.
- [ ] For the assessment type at hand, the feedback level matches the table in `SKILL.md`. In
      particular, in a mini-test or a defense, the last assertion of each test function gives no
      hint about inputs or expected values.
- [ ] Feedback messages are in the same language as `instructions.md`.
- [ ] Any gated test (progressive disclosure) is announced in `instructions.md`, ordered after the
      tests that unlock it, and its counter is incremented as the last statement of each unlocking
      test.
- [ ] Non-functional requirements stated in `instructions.md` are actually enforced, by checkstyle
      or reflection. Requirements nobody checks are requirements students ignore.
- [ ] Running the suite against the student skeleton produces a readable report: no bare
      `NullPointerException`, `IndexOutOfBoundsException`, or
      `AssertionFailedError: expected: <true> but was: <false>`.

## After the assignment has run

- [ ] Look at the per-test pass counts on the assignment report page. A test nobody passes is
      probably wrong, and for a hidden test nobody will report it.
- [ ] A test everybody passes on the first submission is not measuring anything.
