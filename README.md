# Drop Project skills for Claude Code

Three skills that teach Claude how to set up assignments on
[Drop Project](https://github.com/drop-project-edu/drop-project):

| Skill | What it does |
|---|---|
| `create-dropproject-assignment` | Authors the teacher's Maven project, pushes it to a git repository, registers the assignment, installs the deploy key and iterates on the validation report. Ships a Java project template that already passes validation. |
| `reuse-dropproject-assignment` | Copies last year's assignment repository into this year's organization and registers a new assignment against the copy. |
| `write-dropproject-teacher-tests` | Writes or reviews the unit tests that grade an assignment: how many test functions and sub-cases each API function needs, how much feedback to give for the assessment type at hand, and how to keep hardcoded solutions from passing. Follows the recommendations in [Seven Years Later: Lessons Learned in Automated Assessment](https://doi.org/10.4230/OASIcs.ICPEC.2024.3) (ICPEC 2024). |

None of them activates an assignment on its own. They stop at a validated but closed
assignment, and open it only when you ask.

## Install

```
/plugin marketplace add drop-project-edu/claude-skills
/plugin install dropproject-assignments@drop-project
```

`/plugin update dropproject-assignments` picks up later versions.

## Connect Drop Project

The skills drive Drop Project through its MCP server, so add it as well. Generate a personal
token on your Drop Project profile page, then:

```
claude mcp add --transport http drop-project https://<your drop project>/mcp/ \
  --header "Authorization: Bearer <your personal token>"
```

The token identifies you, so it must belong to a teacher account. Everything the skills do
happens as you.

Without the MCP server the skills still help you write the assignment's Maven project and push
it, and will hand you the remaining steps to do in the web ui.

## Install without Claude Code

On claude.ai and the desktop app, skills are uploaded as a zip under Settings -> Capabilities:

```bash
git clone https://github.com/drop-project-edu/claude-skills.git
cd claude-skills/skills && zip -r create-dropproject-assignment.zip create-dropproject-assignment
```

## License

Apache 2.0, the same as Drop Project.
