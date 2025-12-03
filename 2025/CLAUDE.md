# Advent of Code 2025

## Language & Tools
- **Language**: Clojure
- **Dependency Management**: Nix (shell.nix)

## Project Structure
```
2025/
├── CLAUDE.md
├── shell.nix
├── day01/
│   ├── src/solution.clj
│   └── input.txt
├── day02/
│   └── ...
```

## Running Solutions
```bash
# Enter nix devshell
nix develop

# Run a day's solution (reads from STDIN, writes to STDOUT)
bb day01/src/solution.clj < day01/input.txt
# Or with clojure
clojure -M day01/src/solution.clj < day01/input.txt
```

## Development Workflow
1. Create new folder for each day: `dayXX/`
2. Add puzzle input to `input.txt`
3. Implement solution in `src/solution.clj`
4. Test with example input first, then real input

## Conventions
- Each day is self-contained in its own folder
- **Solutions read from STDIN and write to STDOUT** (no hardcoded file paths)
- Solutions should print both part 1 and part 2 answers
- Keep solutions readable over clever
