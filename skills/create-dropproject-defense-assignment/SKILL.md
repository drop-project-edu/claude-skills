---
name: create-dropproject-defense-assignment
description: Create a Drop Project defense assignment - a proctored assessment in which each student changes the code they themselves submitted to a project or exercise, to prove they wrote it. Covers deriving the defense repository from the project's, designing the requested changes, writing the defense's teacher tests, the instructions, the assignment settings, and running the two phases on the day. Use when asked to create, prepare or review a defense (defesa), a project defense, or a make-up/recurso/especial edition of one.
---

# Creating a Drop Project defense assignment

A defense is a proctored assessment where students make a small, bounded set of changes to **their
own already-submitted code**, under a clock, to show that they wrote it. It is the answer to a
project whose grade is high and whose authorship is not certain.

Everything that makes a defense different from every other assignment follows from three invariants:

1. **The starting point is the student's own project code**, not a skeleton the teacher wrote. Two
   students defending the same version start from different code.
2. **The requested changes are the only thing that may change.** Behaviour the defense did not ask
   about must still work afterwards. A student who rewrites half the project to make the new tests
   pass has not defended anything.
3. **The defense must not re-punish bugs the project already had.** The student is being assessed on
   what they do in this hour, not on what they shipped last month.

The other skills in this plugin still apply: `create-dropproject-assignment` for the mechanics of
registering and validating any assignment, and `write-dropproject-teacher-tests` for the tests. This
one is about what a defense adds on top.

## Before you start

Ask for whatever is missing - none of these is safe to invent:

- **which assignment is being defended**, and the git repository behind it. Usually a project, but a
  weekly assignment works the same way. Which of the two it is decides what happens to the original
  test suite - see [step 4](#the-original-test-suite-project-vs-weekly-assignment).
- **which exam period**: normal, recurso, época especial. It goes in the name and the tags.
- **how the sittings are scheduled** - one room at once, or one turma at a time - and **the list of
  turmas with their students**, which is what fixes the number of versions. See
  [Parallel versions](#parallel-versions-are-the-norm).
- **the duration**, in minutes. It goes in the instructions.
- **the conduct rules and the code-quality penalty** for this course, which the teacher usually
  reuses verbatim from the previous edition - **except what may be consulted**. Ask explicitly
  whether the open internet is allowed or students are restricted to Moodle: it changes between
  defenses, and it is not safe to copy from last year.
- **whether this Drop Project instance supports linked defenses** (see below). If unsure, pass
  `baseAssignmentId` to `create_assignment`: an instance that supports them answers with a
  "Defense of '<id>'" section, one that does not rejects the argument.

### Two ways Drop Project can run a defense

| | Linked defense | Unlinked defense |
|---|---|---|
| How the student gets their code | Drop Project serves the project submission back to them, and every submission is compared against it | the instructions tell them to start a project and paste in their own code |
| Guard against rewriting | `maxChangedLines`, enforced on submission | none, teacher inspects afterwards |
| Phases | checkpoint phase, then the defense itself | one phase |
| Requires | a Drop Project with the defense feature | any Drop Project |

Prefer the linked defense when the instance offers it. Everything below describes it, with the
unlinked differences called out. `baseAssignmentId` and `maxChangedLines` are ordinary arguments of
`create_assignment` and `edit_assignment`, so a defense can be registered end to end over MCP.
**Releasing the instructions is the one thing that is not** - it is a toggle on the assignments list
in the web ui, and it is what starts the defense.

### Parallel versions are the norm

A single version is a single answer to copy. Real defenses ship several versions that are
**structurally parallel and factually different**: the same number of changes, the same kinds of
change, the same number of tests, different specifics. Each version is a separate assignment and a
separate repository (`defesa-v1`, `defesa-v2`, ...), `PRIVATE`, with its own slice of the students in
`assignees`.

How many versions depends on how the sittings are scheduled, and the two cases are different:

- **A project defense** is one sitting, everyone at once, in the same room. Two or three versions are
  enough: they split the room so that neighbours are not solving the same exercise.
- **A weekly assignment or mini-ficha defense** is sat **in the practical class, one turma at a
  time**, hours or days apart. So there must be **as many versions as there are turmas**: the moment
  the first turma leaves the room, its instructions are in the hands of every colleague who has not
  sat it yet, and a shared version stops measuring anything. One version per turma, `assignees` set
  to that turma's students, and the instructions of each released only when that turma sits.

Version parity is a grading fairness requirement, not a nicety, and it gets harder the more versions
there are - with one per turma, write the first version fully and then derive the rest from it
change by change. `references/change-catalog.md` explains how to keep versions equivalent.

## 1. Derive the defense repository from the project's

The defense repository is a copy of the project's teacher repository, with the reference solution
carried forward. Do not start from an empty project.

```bash
gh repo create <ORG>/defesa-v1 --private
git clone git@github.com:<ORG>/<project repo>.git defesa-v1
cd defesa-v1
git remote set-url origin git@github.com:<ORG>/defesa-v1.git
```

Then strip what a defense does not need, which is most of the delivery machinery:

- **the original test classes - only when defending a project.** Delete `TestTeacherP1`,
  `TestTeacherHidden*` and friends. **Keep** the classes that only hold shared fixtures and builders
  (`TestTeacherCommon*`) - the defense tests extend them, and adapting a fixture is often how a
  defense test gets the input it needs. When defending a **weekly assignment**, keep the original
  tests as well, minus the ones the defense contradicts. See
  [step 4](#the-original-test-suite-project-vs-weekly-assignment).
- **the GUI simulator, the shade plugin, jacoco**, and anything else that only existed to let
  students run or cover the project. A defense accepts no student tests.
- rename the `artifactId` after the defense.

The package must stay **exactly** the same as the project's: the students' own classes declare it,
and Drop Project derives the field from the linked assignment.

## 2. Design the changes

Four to six changes, sized so that a student who genuinely wrote the project finishes comfortably
inside the duration. The ones that work in practice, and how to keep versions parallel, are in
[references/change-catalog.md](references/change-catalog.md).

Three constraints specific to defenses, on top of ordinary exercise design:

- **Build on the project's mandatory functionality.** If the project marked a subset of its tests
  mandatory, the defense belongs on top of exactly that. Where it did not, at least avoid anything
  that was only ever covered by the project's *hidden* tests - the student never saw those fail and
  never got the chance to fix them, so building on that ground grades an old bug.
- **Do not use, as an oracle, a function the defense itself changes.** If change 4 rewrites
  `toString()`, no other defense test may assert through `toString()`. This is the single most
  common way a defense cascades one mistake into five failures.
- **Avoid the domain's known pitfalls.** Every project has behaviour that most students got subtly
  wrong and the tests never caught. Building a defense change on top of that measures the project
  again.

## 3. Apply the changes to the reference solution

`src/main` of the defense repository must be **the project's reference solution with the requested
changes implemented**. This is not documentation, it is load-bearing:

- assignment validation builds this project and runs the defense tests against it, so the tests
  cannot be validated at all without it.
- Drop Project compares `src/main` of the two teacher repositories to tell you how many lines your
  own solution changes, which is how [step 8](#8-choose-maxchangedlines) picks the budget.

Implement the changes the way a student is expected to: if the instructions say a new subclass, add
a subclass, not a special case in the parent. Your diff is the reference for what "a bounded change"
means here.

## 4. Write the defense tests

Follow `write-dropproject-teacher-tests` for how the assertions themselves are written. A defense is
a **time-bound** assessment, so the rules from that skill that turn on the clock all apply:

- **All tests public.** No `TestTeacherHidden*` classes: a student who cannot see how they are doing
  in a proctored hour just panics.
- **No progressive disclosure.** Never gate a test behind another one. A student who runs out of
  time on change 2 must still be able to score changes 3 to 6.
- **Feedback like a mini-test**: enough to act on, and the last assertion of each test function gives
  no hint about inputs or expected values.

On top of that, five things are specific to defenses:

**One test class, named after the defense.** `TestTeacherDefesa` (or
`TestTeacherDefesa<Version>` when several versions share an organization). Defending a project, it
extends the fixture class kept from it; defending a weekly assignment, it simply sits next to the
original test class.

**Ordered by the instructions, not by difficulty.** Students work through the changes in the order
they are written down, so `test_001_` covers instruction 1. This is the one place where the ordering
rule in `write-dropproject-teacher-tests` bends: under a clock, a report that matches the
instructions is worth more than a report that ramps up in difficulty.

**Adapt the project's fixtures rather than inventing new inputs.** The students have seen the
project's test data pass; reusing the same board, the same input file, the same sequence of actions
keeps the defense about the change. Editing the fixture is normal - `defesa-v1` changed the
languages in the shared player fixture so the same helper could feed a test about a tool that only
works for C programmers.

**Reach everything the defense adds by reflection, never by naming it in the test source.** This is
the one that bites hardest, because it does not fail like a test - it fails like a build. The teacher
tests are compiled together with the student's own code, so a test that writes `pessoa.cidade` or
`new Pessoa(n, a, c)` stops compiling the moment the student has not written that attribute or
constructor *yet*, or has named it something else. The whole submission then scores zero, including
the changes they had already finished. Under a clock that is the difference between a bad grade and
no grade at all.

So look up what the defense asks the student to add - a field, a constructor, a method - with
`getDeclaredField`, `getDeclaredConstructor` or the `findStaticMethodStartingWith` helper from
`write-dropproject-teacher-tests`, call `setAccessible(true)`, and `fail(...)` with the missing
signature spelled out in full:

```java
private Field campoCidade() {
    try {
        Field campo = Pessoa.class.getDeclaredField("cidade");
        campo.setAccessible(true);
        return campo;
    } catch (NoSuchFieldException e) {
        fail("A classe Pessoa não tem nenhum atributo chamado 'cidade'");
    }
    return null;
}
```

A student who called it `localidade` now fails one test with a sentence that tells them what to
rename, instead of failing all of them with a compiler error. Code the defense does *not* change is
the opposite case: name it directly, so it compiles against what the student already had.

**Test that the untouched behaviour survived.** This is invariant 2, and it is the part teachers
forget. Two ways, both cheap:

- Any test for a change to an *existing* function must also assert the cases the change does not
  affect. A test for a new `toString()` format checks the new type *and* that the other types still
  print what they always printed.
- Every defense test should reach its assertion by driving the original API - build the board, hire
  the employee, move the player - so a student who broke the constructor to make change 3 pass fails
  visibly.

### The original test suite: project vs weekly assignment

Whether the original tests ship alongside the defense tests depends on what is being defended, and
the two answers are opposite.

**Defending a project: drop them.** A project's suite is large, its results would bury the eight
that matter, and part of it is hidden tests the student never saw fail - keeping it re-punishes bugs
the project already had, which is invariant 3. The two techniques above are the guard against
rewriting, and they are enough.

**Defending a weekly assignment: keep the tests for the features that were not supposed to change.**
A weekly assignment's suite is small enough not to drown the defense results, all of it is public,
and the student already saw it pass the week they submitted - so keeping it re-punishes nothing and
enforces invariant 2 with tests the student already recognises. Concretely:

- keep the original test classes as they are, and add `TestTeacherDefesa` next to them.
- **delete or adapt every original test the defense contradicts.** If change 3 asks for a new
  `toString()` format, the original test asserting the old format has to go, or be rewritten around
  the part of the format that survives - otherwise the student is graded down for doing exactly what
  was asked. Go through the changes one by one against the original suite; this is the step that
  gets missed.
- keep the original names and the original `test_00N_` numbering, so a student reading the report
  recognises which results are old and which are new. Number the defense tests in their own class,
  from 1, following the instructions.
- **make the original class report first.** With two test classes the report order stops being
  automatic, and the original results belong on top: the first thing a student has to know is
  whether they broke what already worked. In JUnit 5 that is `@Order(1)` on the original class and
  `@Order(2)` on the defense's - plus the surefire configuration in the mechanics table of
  `write-dropproject-teacher-tests`, **without which those two annotations do nothing at all**.
- run the original suite against the reference solution of [step 3](#3-apply-the-changes-to-the-reference-solution)
  before pushing. Anything that fails there is a test the defense contradicts and you missed.

Announce the count of **defense** tests in the instructions ("estas alterações traduzem-se em 8
testes"), and keep it true when tests are added or removed. When the original tests are kept, say so
too, and say that they must still pass - it is the same grading criterion as invariant 2, and the
student will see those results in the report either way.

## 5. Write the instructions

Use [references/instructions-template.md](references/instructions-template.md), which is the
structure last year's defenses used: conduct rules, duration, introduction with the number of
changes and tests, then the numbered changes.

Two rules that matter more than the rest:

- **Never mention the line budget.** Not the number, not that a budget exists. Drop Project
  deliberately does not show `maxChangedLines` to students, because a budget that is visible becomes
  a target to work up to instead of a limit that catches rewrites. Only the rejection message is
  shown, and only if they hit it.
- **State that the original behaviour must survive**, in the introduction, in the teacher's own
  words. It is a grading criterion, so it belongs in the instructions.

**When the assignment being defended was done in groups**, the first numbered instruction is
`AUTHORS.txt`: a project made in pairs is defended individually, so the student has to cut the file
down to their own number and name. A weekly assignment or mini-ficha is already individual, so its
`AUTHORS.txt` names one student and there is nothing to cut - drop the instruction entirely rather
than asking for a change that is a no-op, and start the numbering at the first real change.

## 6. Verify locally, then push

```bash
mvn -q test -Ddp.argLine=
```

The tests must pass against the reference solution built in step 3. Then commit and push, and note
the **SSH** url.

## 7. Register the assignment

Register it with `create_assignment` the way `create-dropproject-assignment` describes, then install
the deploy key and connect it. The settings a defense wants:

| Argument | Value | Why |
|---|---|---|
| `assignmentId` | e.g. `lp2-2526-defesa-v1` | course, year, `defesa`, version |
| `packageName` | **the project's**, unchanged | the students' own classes declare it |
| `visibility` | `PRIVATE` | one version per slice of the class |
| `assignees` | the student ids sitting this version | with `PRIVATE`, this is the whitelist |
| `minGroupSize`, `maxGroupSize` | `1` and `1` | a defense is individual even when the project was not |
| `tags` | e.g. `25/26,lp2,defesa` | |
| `baseAssignmentId` | the id of the assignment being defended | what makes it a linked defense |
| `maxChangedLines` | see [step 8](#8-choose-maxchangedlines) | only valid together with `baseAssignmentId` |
| `dueDate` | leave unset | the clock is the session, not a date |
| `acceptsStudentTests` | unset | |
| `hiddenTestsVisibility` | unset | a defense has no hidden tests |
| `cooloffPeriod` | leave unset | a student debugging under a clock resubmits often |

Passing `baseAssignmentId` overwrites `packageName`, `minGroupSize` and `maxGroupSize` with the
defended assignment's package and a group size of 1 - none of those is the defense's to choose, and
the tool's answer says so. The caller must own the defended assignment, or be authorized on it.

Nothing else is needed in the web ui at registration time. The one toggle that lives only there is
**release defense instructions**, which is [step 9](#9-run-the-two-phases-on-the-day).

For an unlinked defense, leave `baseAssignmentId` and `maxChangedLines` unset and add, as
instruction 1 of the exercise, that the student must create a project and paste in their own code
from the assignment being defended.

## 8. Choose maxChangedLines

The assignment form tells you how many lines your reference solution changes under `src/main`
relative to the project's, and so does every submission's page. To get the number before any of that
exists, diff the two teacher repositories yourself - blank lines do not count, so leave them out:

```bash
diff -ru --ignore-blank-lines <project repo>/src/main <defense repo>/src/main \
  | grep -E '^[+-]' | grep -vE '^(\+\+\+|---)' | grep -vcE '^[+-][[:space:]]*$'
```

That number is the floor, not the budget: students write more verbosely than the reference solution,
and they refactor a little while they work.

Roughly two to three times the reference divergence is a sane starting point. What the budget has to
do is catch a *rewrite*, not a wide solution. Leaving it unset is a legitimate choice: the divergence
is still measured and shown to you on every submission, it is simply never rejected.

What counts, exactly:

- everything under `src/`, so `AUTHORS.txt` and `README.md` are free
- blank lines never count, and neither do line-ending or trailing-whitespace differences, so a
  student on Windows is not penalised for unzipping
- a renamed file counts as a full deletion plus a full addition, which is worth remembering before
  asking students to rename a class
- a changed binary file counts as one line

## 9. Run the two phases on the day

The linked defense has a checkpoint phase before the defense proper. It exists so that a student is
not discovering that their project does not build on the exam room's machine at minute one.

| When | State | What happens |
|---|---|---|
| Before the session | assignment **active**, instructions **not released** | students download their project submission, get it running, and submit it back unchanged. Divergence from the original is recorded but never rejected, and no build report is shown. Teachers already see phase 2, so they can rehearse |
| Start of the session | teacher toggles **release defense instructions** | the instructions appear and `maxChangedLines` starts being enforced |
| End of the session | teacher toggles it back off | the instructions are hidden again and the assignment is deactivated, which ends the defense |

Two things to warn the teacher about:

- **The instructions are invisible to students until released, and this is the whole point.** Being
  able to see them earlier is being able to prepare. Activate the assignment early; release the
  instructions late. With one version per turma, this cycle runs once per turma: release that
  version's instructions when the class starts, toggle them back off when it ends, and leave every
  other version unreleased in the meantime.
- **A student who never submitted to the project cannot submit to the defense at all.** Drop Project
  needs a base submission to compare against and rejects the upload without one. Find those students
  before the day.

Which submission is the base: the group's submission marked as final for the project assignment, or
the most recent validated one if none was marked. All the groups the student belongs to are
searched, so a project done in pairs and defended alone resolves correctly.

## 10. Before the session, check

- [ ] `mvn -q test -Ddp.argLine=` passes against the defense's reference solution.
- [ ] The validation report has no errors, for **every** version.
- [ ] The number of tests announced in the instructions matches the number of test methods.
- [ ] No test asserts through a function that the defense itself asks the student to change.
- [ ] No test names, in its source, an attribute, constructor or method that the defense asks the
      student to add - those go through reflection, or a student who has not written it yet fails to
      compile and scores zero on everything.
- [ ] The tests still compile and run against the *original* assignment's reference solution, not
      only against the defense's. That is the code every student starts the session with.
- [ ] No test covers behaviour that the project only checked in a hidden test.
- [ ] Defending a weekly assignment: the original tests are still there, and none of the ones the
      defense contradicts survived.
- [ ] Every version asks for the same number and kind of changes, and yields the same number of
      tests.
- [ ] Defending a weekly assignment or mini-ficha: there is one version per turma, and no version is
      shared by two turmas that sit at different times.
- [ ] The instructions never mention the line budget.
- [ ] `assignees` covers every student sitting, across the versions, with nobody listed twice and
      nobody missing.
- [ ] Every listed student has a submission to the project assignment.
- [ ] The assignment is active and the instructions are **not** released.
- [ ] You opened the assignment as a teacher and read the instructions as the students will see them.

## Notes

- The defense's repository diverges from the project's the moment you push. When the project is fixed
  mid-semester, the fix does not reach the defense: re-apply it and `refresh_assignment`.
- Defenses are per-edition. Reusing last year's means the `reuse-dropproject-assignment` flow plus
  new changes, since last year's students kept their instructions.
- Archive the versions once the defense is graded, so they stop appearing in the assignment list.
  Archiving keeps the submissions and the grades.
