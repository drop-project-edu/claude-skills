---
name: reuse-dropproject-assignment
description: Reuse a Drop Project assignment from a previous year - copy the teacher's repository into this year's git organization, and register a new assignment with the same configuration but pointing at the copy. Use when asked to reuse, copy, clone, or set up a new edition of an existing Drop Project assignment for a new school year.
---

# Reusing a Drop Project assignment from a previous year

Teachers rarely write an assignment from scratch twice. The usual flow at the start of a school
year is to take last year's assignment, copy its repository into this year's organization (e.g.
`ULHT-LP2-2025-26` -> `ULHT-LP2-2026-27`) and recreate the assignment against the copy.

There is no "duplicate assignment" button, and there cannot be one: an assignment's git repository
url can't be changed after creation, and its id is unique per Drop Project instance. So reusing is
always **creating a new assignment** out of two things copied by hand:

| What is copied | From | To |
|---|---|---|
| the repository | last year's organization | this year's organization |
| the configuration | last year's assignment | the `create_assignment` call |

Drop Project copies neither. If you are not reusing anything, use the
`create-dropproject-assignment` skill instead - this one assumes the previous edition exists and
already passed validation.

| Step | Where it happens |
|---|---|
| Read the previous assignment's configuration | Drop Project, `get_assignment_info` |
| Copy the repository | the git host (`gh`, `git`) |
| Adjust the files for the new edition | your filesystem |
| Register the new assignment | Drop Project, `create_assignment` |
| Install the deploy key on the copy | the git host |
| Clone and validate | Drop Project, `connect_assignment` / `refresh_assignment` |
| Let students submit, when the teacher asks for it | Drop Project, `set_assignment_active` |
| Retire last year's | Drop Project, `set_assignment_active` and the web ui |

## Before you start

Ask for whatever is missing - do not invent them:

- which assignment is being reused (`search_assignments` finds it by name, id or tag)
- this year's organization, and whether the repository keeps the same name
- the new assignment id and name
- the new due date
- this year's authorized students, if the old assignment restricts who may submit
- this year's teaching team
- what should happen to last year's assignment

## 1. Read the previous assignment's configuration

`get_assignment_info` on the old assignment prints a `## Configuration` section where every setting
is named after the `create_assignment` argument that sets it, `gitRepositoryUrl` included. Keep that
block around - it is what you pass back in step 4, and it tells you which repository to copy.

Settings the old assignment doesn't use are reported as `not set` rather than omitted, so a value
you can't find there is a value the assignment genuinely didn't have. Do not guess the ones that
matter for grading: an assignment that silently loses `acceptsStudentTests` or
`hiddenTestsVisibility` still validates, still activates, and grades the students differently from
last year.

The block is a starting point, not something to paste back unchanged - `assignees` and `acl` list
last year's people, and step 4 says what to do about them.

## 2. Copy the repository

```bash
gh repo create <NEW_ORG>/<repo> --private
git clone git@github.com:<OLD_ORG>/<repo>.git <repo>
cd <repo>
git remote set-url origin git@github.com:<NEW_ORG>/<repo>.git
git push -u origin HEAD
```

Keep the history - `git log` is where you find the fixes that were pushed mid-semester last year.

Do not fork, and do not point the new assignment at last year's repository. The two editions must
be independent: a push meant for this year's students would otherwise also change last year's
assignment, whose submissions were graded against the old tests.

Note the **SSH** url of the copy, `git@github.com:<NEW_ORG>/<repo>.git`. Drop Project rejects https
urls.

## 3. Adjust the copy for the new edition

Everything that mentions the previous edition has to be reviewed:

- **`instructions.md`** - dates, deadlines, the year, the course edition, links to class materials
- **`pom.xml`** - the artifact id or version, if it carries the year
- **the exercise itself** - last year's students kept the statement and may have published their
  solutions. If that matters for this assignment, change the input data, the expected values, or
  add `TestTeacherHidden*` tests. Adding hidden tests for the first time means also passing
  `hiddenTestsVisibility` when creating the assignment.

Verify locally before pushing, then commit and push:

```bash
mvn -q test -Ddp.argLine=
```

## 4. Register the new assignment

Call `create_assignment` with a **new** assignment id - follow the previous convention, e.g.
`lp2-2526-proj1` -> `lp2-2627-proj1`. Reusing last year's id is refused ("An assignment already
exists with this ID").

Pass `gitRepositoryUrl` of the copy, and repeat *every* setting collected in step 1. Anything left
out falls back to Drop Project's default - nothing is inherited from the old assignment. Update
along the way:

| Argument | Typical change |
|---|---|
| `assignmentId`, `assignmentName` | the new edition's id and name |
| `gitRepositoryUrl` | the copy, in this year's organization |
| `dueDate` | this year's deadline |
| `tags` | the school year, e.g. `project,26/27` |
| `assignees` | this year's students |
| `acl` | this year's teaching team |

**`assignees` and `acl` are the two settings never to copy.** The people change with the edition,
and both are lists of user ids:

- `assignees` is a whitelist. As soon as an assignment has one, *only* those user ids can open it
  and submit to it - whatever the `visibility` is. Carrying last year's over admits students who
  finished the course and locks out the ones taking it.
- `acl` is the other teachers who may open the assignment and its submissions. The teaching team
  usually changes too, and the ones who left keep access until someone notices.

Ask for both. If this year's enrolment list isn't out yet, create the assignment with a provisional
whitelist - the teaching team - rather than last year's students, and replace it later with
`edit_assignment`. Nothing is lost by waiting, since the assignment only accepts submissions once it
is activated. A `PRIVATE` assignment cannot be created with an empty whitelist at all ("For PRIVATE
assignments, you have to fill in the authorized submitters").

The tool returns a **new** ssh public key. Each assignment gets its own key pair, so last year's
deploy key gives no access to the copy.

## 5. Install the deploy key on the copy

```bash
gh repo deploy-key add <key file> --repo <NEW_ORG>/<repo> --title "Drop Project"
```

Read-only, as always. Without `gh`, paste it at
`https://github.com/<NEW_ORG>/<repo>/settings/keys` leaving write access unchecked.

## 6. Connect and validate

`connect_assignment`, then loop on the validation report - fix, push, `refresh_assignment` - until
it has no errors. The `create-dropproject-assignment` skill explains the report entries in detail.

Stop there: **do not activate the new assignment on your own.** A clean report means it is ready,
not that it should open. Tell the teacher it is validated and inactive, and call
`set_assignment_active` with `active: true` only when they ask for it.

The repository passed validation last year, so anything reported here comes from what changed in
step 3 or from a setting that was not carried over. The two that show up most often:

| Report says | What happened |
|---|---|
| You have hidden tests but you didn't set their visibility | hidden tests were added in step 3 without `hiddenTestsVisibility` |
| Assignment without package | `packageName` was not repeated in `create_assignment` |

## 7. Retire the previous edition

Once the teacher opens the new edition, last year's should stop accepting submissions. Ask before
doing either - closing an assignment that students are still submitting to is not something to
guess at:

- `set_assignment_active` with `active: false` closes it
- archiving it hides it from the teacher's assignment list, at
  `<drop project>/assignment/archive/<oldId>` in the web ui - there is no MCP tool for it

Neither deletes anything: the old submissions, reports and grades stay available.

## Notes

- **Never** ask for, or handle, the private key of either assignment. It is generated inside Drop
  Project and stays there.
- Drop Project does not poll the repositories. Every later edit to this year's files means push,
  then `refresh_assignment`.
- Submissions already evaluated are never re-evaluated, in either edition.