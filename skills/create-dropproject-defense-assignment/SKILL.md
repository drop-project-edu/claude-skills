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
  large weekly exercise works the same way.
- **which exam period**: normal, recurso, época especial. It goes in the name and the tags.
- **how many parallel versions**, and who gets each one. See
  [Parallel versions](#parallel-versions-are-the-norm).
- **the duration**, in minutes. It goes in the instructions.
- **the conduct rules and the code-quality penalty** for this course, which the teacher usually
  reuses verbatim from the previous edition.
- **whether this Drop Project instance supports linked defenses** (see below). If unsure, open the
  assignment creation form and look for an "Assignment type: Normal / Defense" radio.

### Two ways Drop Project can run a defense

| | Linked defense | Unlinked defense |
|---|---|---|
| How the student gets their code | Drop Project serves the project submission back to them, and every submission is compared against it | the instructions tell them to start a project and paste in their own code |
| Guard against rewriting | `maxChangedLines`, enforced on submission | none, teacher inspects afterwards |
| Phases | checkpoint phase, then the defense itself | one phase |
| Requires | a Drop Project with the defense feature | any Drop Project |

Prefer the linked defense when the instance offers it. Everything below describes it, with the
unlinked differences called out. The defense-specific settings (`baseAssignmentId`,
`maxChangedLines`, releasing the instructions) are **not exposed over the MCP server** - they are set
in the web ui, on the assignment form.

### Parallel versions are the norm

Students sit the defense at the same time, so a single version is a single answer to copy. Real
defenses ship two or three versions that are **structurally parallel and factually different**: the
same number of changes, the same kinds of change, the same number of tests, different specifics.
Each version is a separate assignment and a separate repository (`defesa-v1`, `defesa-v2`, ...),
`PRIVATE`, with its own slice of the class in `assignees`.

Version parity is a grading fairness requirement, not a nicety. `references/change-catalog.md`
explains how to keep versions equivalent.

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

- **the project's own test classes.** Delete `TestTeacherP1`, `TestTeacherHidden*` and friends.
  **Keep** the classes that only hold shared fixtures and builders (`TestTeacherCommon*`) - the
  defense tests extend them, and adapting a fixture is often how a defense test gets the input it
  needs. See [step 4](#4-write-the-defense-tests).
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

On top of that, four things are specific to defenses:

**One test class, named after the defense.** `TestTeacherDefesa` (or
`TestTeacherDefesa<Version>` when several versions share an organization). It extends the fixture
class kept from the project.

**Ordered by the instructions, not by difficulty.** Students work through the changes in the order
they are written down, so `test_001_` covers instruction 1. This is the one place where the ordering
rule in `write-dropproject-teacher-tests` bends: under a clock, a report that matches the
instructions is worth more than a report that ramps up in difficulty.

**Adapt the project's fixtures rather than inventing new inputs.** The students have seen the
project's test data pass; reusing the same board, the same input file, the same sequence of actions
keeps the defense about the change. Editing the fixture is normal - `defesa-v1` changed the
languages in the shared player fixture so the same helper could feed a test about a tool that only
works for C programmers.

**Test that the untouched behaviour survived.** This is invariant 2, and it is the part teachers
forget. Two ways, both cheap:

- Any test for a change to an *existing* function must also assert the cases the change does not
  affect. A test for a new `toString()` format checks the new type *and* that the other types still
  print what they always printed.
- Every defense test should reach its assertion by driving the original API - build the board, hire
  the employee, move the player - so a student who broke the constructor to make change 3 pass fails
  visibly.

Do not simply keep the project's whole test suite alongside the defense tests. It re-punishes
pre-existing bugs, and it buries the eight results that matter under sixty that do not.

Announce the count in the instructions ("estas alterações traduzem-se em 8 testes"), and keep it
true when tests are added or removed.

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

The first numbered instruction is always `AUTHORS.txt`: a project made in pairs is defended
individually, so the student has to cut it down to their own number and name.

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
| `dueDate` | leave unset | the clock is the session, not a date |
| `acceptsStudentTests` | unset | |
| `hiddenTestsVisibility` | unset | a defense has no hidden tests |
| `cooloffPeriod` | leave unset | a student debugging under a clock resubmits often |

Then, **in the web ui**, on the assignment form:

1. set Assignment type to **Defense**
2. pick the project assignment in **Defense of**, which fills in the package and reports how many
   lines your own solution changes
3. set **Max changed lines**

For an unlinked defense, skip those three and add, as instruction 1 of the exercise, that the
student must create a project and paste in their own code from the assignment being defended.

## 8. Choose maxChangedLines

The assignment form tells you how many lines your reference solution changes under `src/main`
relative to the project's. That number is the floor, not the budget: students write more verbosely
than the reference solution, and they refactor a little while they work.

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
  instructions late.
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
- [ ] No test covers behaviour that the project only checked in a hidden test.
- [ ] Every version asks for the same number and kind of changes, and yields the same number of
      tests.
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
