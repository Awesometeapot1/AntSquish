

---

```markdown
# 🐜 Ant Squish

A fast-paced 2D arcade game built with Python and Pygame. Ants swarm toward a bin in the center of the screen — tap/click them before they get there!

## Gameplay

- Ants spawn from random edges of the screen and march toward the bin
- **Click an ant** to squish it and earn a point
- You have **3 lives** — each ant that reaches the bin costs one
- Ants spawn faster over time — survive as long as you can!
- When all lives are lost, click anywhere to restart

## Requirements

- Python 3.7+
- Pygame

```bash
pip install pygame
```

## Running the Game

```bash
python main.py
```

## Sprites

The game supports custom sprites placed in a `sprites/` folder. All files are optional — colored shapes are used as fallbacks if any are missing.

| File | Size | Description |
|------|------|-------------|
| `ant.png` | 240×60 px (4-frame sheet) | Ant walk cycle |
| `splat.png` | 210×70 px (3-frame sheet) | Squish animation |
| `bin.png` | 100×100 px | Target bin |
| `background.png` | 480×800 px | Background |
| `heart.png` | 36×36 px | Life indicator |
| `score_panel.png` | 200×50 px | Score display background |

Sprite sheets should be horizontal strips. Alternatively, numbered files (`ant1.png`, `ant2.png`, etc.) are also supported.

## Project Structure

```
AntSquish/
├── main.py
└── sprites/
    ├── ant.png
    ├── splat.png
    ├── bin.png
    ├── background.png
    ├── heart.png
    └── score_panel.png
```

## Controls

| Input | Action |
|-------|--------|
| Click on ant | Squish it (+1 score) |
| Click anywhere (Game Over screen) | Restart |

## License

MIT
```
