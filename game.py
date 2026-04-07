import pygame
import random
import math
import os

pygame.init()

W, H = 480, 800
screen = pygame.display.set_mode((W, H))
pygame.display.set_caption("Ant Squish")
clock = pygame.time.Clock()
font = pygame.font.SysFont(None, 48)

SPRITES_DIR = "sprites"

# Sprite sheets:
#   ant.png   — 4 frames × 60 px wide = 240 × 60 px  (walk cycle, loops at 8 fps)
#   splat.png — 3 frames × 70 px wide = 210 × 70 px  (impact, plays once over 0.5 s)
#   bin.png        — 100 × 100 px static
#   background.png — 480 × 800 px static

ANT_SIZE   = (60, 60)
BIN_SIZE   = (100, 100)
SPLAT_SIZE = (70, 70)
ANT_FRAMES   = 4
SPLAT_FRAMES = 3
ANT_FPS      = 8   # frames per second for walk cycle

def load_static(name, size):
    path = os.path.join(SPRITES_DIR, f"{name}.png")
    if not os.path.exists(path):
        return None
    img = pygame.image.load(path).convert_alpha()
    return pygame.transform.scale(img, size)

def load_sheet(name, frame_count, frame_size):
    """Load frames from either a horizontal sheet (name.png) or numbered files (name1.png, name2.png…)."""
    # Try numbered individual files first
    numbered = [os.path.join(SPRITES_DIR, f"{name}{i+1}.png") for i in range(frame_count)]
    if all(os.path.exists(p) for p in numbered):
        frames = []
        for p in numbered:
            img = pygame.image.load(p).convert_alpha()
            frames.append(pygame.transform.scale(img, frame_size))
        return frames
    # Fall back to single horizontal sprite sheet
    path = os.path.join(SPRITES_DIR, f"{name}.png")
    if not os.path.exists(path):
        return None
    sheet = pygame.image.load(path).convert_alpha()
    fw, fh = frame_size
    frames = []
    for i in range(frame_count):
        frame = pygame.Surface((fw, fh), pygame.SRCALPHA)
        frame.blit(sheet, (0, 0), (i * sheet.get_width() // frame_count, 0,
                                   sheet.get_width() // frame_count, sheet.get_height()))
        frames.append(pygame.transform.scale(frame, frame_size))
    return frames

HEART_SIZE      = (36, 36)
SCORE_PANEL_SIZE = (200, 50)

spr_ant          = load_sheet("ant",   ANT_FRAMES,   ANT_SIZE)
spr_splat        = load_sheet("splat", SPLAT_FRAMES, SPLAT_SIZE)
spr_bin          = load_static("bin",          BIN_SIZE)
spr_background   = load_static("background",   (W, H))
spr_heart        = load_static("heart",        HEART_SIZE)
spr_score_panel  = load_static("score_panel",  SCORE_PANEL_SIZE)

BIN_POS = (W // 2, int(H * 0.82))

class Ant:
    def __init__(self):
        edge = random.randint(0, 3)
        if edge == 0:   self.x, self.y = random.randint(0, W), 0
        elif edge == 1: self.x, self.y = W, random.randint(0, H)
        elif edge == 2: self.x, self.y = random.randint(0, W), H
        else:           self.x, self.y = 0, random.randint(0, H)
        self.speed = random.uniform(60, 130)
        self.state = "alive"   # alive | squished | done
        self.splat_timer = 0.5
        self.anim_time = 0.0

    def update(self, dt):
        if self.state == "squished":
            self.splat_timer -= dt
            if self.splat_timer <= 0:
                self.state = "done"
            return
        dx = BIN_POS[0] - self.x
        dy = BIN_POS[1] - self.y
        dist = math.hypot(dx, dy)
        if dist < 35:
            self.state = "done"
            return True   # reached bin
        self.anim_time += dt
        self.x += dx / dist * self.speed * dt
        self.y += dy / dist * self.speed * dt

    def rect(self):
        hw, hh = ANT_SIZE[0] // 2, ANT_SIZE[1] // 2
        return pygame.Rect(self.x - hw, self.y - hh, ANT_SIZE[0], ANT_SIZE[1])

    def squish(self):
        if self.state == "alive":
            self.state = "squished"
            self.splat_timer = 0.5

    def draw(self, surf):
        if self.state == "alive":
            if spr_ant:
                frame = int(self.anim_time * ANT_FPS) % ANT_FRAMES
                surf.blit(spr_ant[frame], self.rect())
            else:
                pygame.draw.ellipse(surf, (30, 15, 5), self.rect())
        elif self.state == "squished":
            alpha = int(self.splat_timer * 510)
            if spr_splat:
                progress = 1.0 - (self.splat_timer / 0.5)
                frame = min(int(progress * SPLAT_FRAMES), SPLAT_FRAMES - 1)
                tmp = spr_splat[frame].copy()
                tmp.set_alpha(min(alpha, 255))
                surf.blit(tmp, self.rect())
            else:
                r = pygame.Rect(self.x - 35, self.y - 20, 70, 40)
                s = pygame.Surface((70, 40), pygame.SRCALPHA)
                pygame.draw.ellipse(s, (120, 60, 10, min(alpha, 255)), s.get_rect())
                surf.blit(s, r)


ants = []
score = 0
lives = 3
spawn_timer = 0
spawn_interval = 2.0
game_over = False

running = True
while running:
    dt = clock.tick(60) / 1000.0

    for event in pygame.event.get():
        if event.type == pygame.QUIT:
            running = False

        if event.type == pygame.MOUSEBUTTONDOWN:
            mx, my = event.pos
            if game_over:
                ants.clear()
                score = 0
                lives = 3
                spawn_timer = 0
                spawn_interval = 2.0
                game_over = False
            else:
                for ant in ants:
                    if ant.state == "alive" and ant.rect().collidepoint(mx, my):
                        ant.squish()
                        score += 1
                        break

    if not game_over:
        spawn_timer += dt
        if spawn_timer >= spawn_interval:
            spawn_timer = 0
            ants.append(Ant())
            if spawn_interval > 0.5:
                spawn_interval -= 0.04

        reached = 0
        for ant in ants:
            result = ant.update(dt)
            if result:   # returned True = reached bin
                reached += 1
        lives -= reached
        ants = [a for a in ants if a.state != "done"]

        if lives <= 0:
            game_over = True

    # --- Draw ---
    if spr_background:
        screen.blit(spr_background, (0, 0))
    else:
        screen.fill((200, 240, 160))  # placeholder green

    # Bin
    br = pygame.Rect(BIN_POS[0] - BIN_SIZE[0]//2, BIN_POS[1] - BIN_SIZE[1]//2, *BIN_SIZE)
    if spr_bin:
        screen.blit(spr_bin, br)
    else:
        pygame.draw.rect(screen, (70, 100, 200), br)

    for ant in ants:
        ant.draw(screen)

    # HUD — score panel
    if spr_score_panel:
        screen.blit(spr_score_panel, (10, 10))
        screen.blit(font.render(f"Score: {score}", True, (255, 255, 255)), (20, 20))
    else:
        screen.blit(font.render(f"Score: {score}", True, (255, 255, 255)), (20, 20))

    # HUD — hearts
    for i in range(lives):
        hx = W - 10 - (i + 1) * (HEART_SIZE[0] + 6)
        hy = 10
        if spr_heart:
            screen.blit(spr_heart, (hx, hy))
        else:
            pygame.draw.circle(screen, (220, 30, 30), (hx + HEART_SIZE[0]//2, hy + HEART_SIZE[1]//2), 18)

    if game_over:
        overlay = pygame.Surface((W, H), pygame.SRCALPHA)
        overlay.fill((0, 0, 0, 160))
        screen.blit(overlay, (0, 0))
        big = pygame.font.SysFont(None, 80)
        screen.blit(big.render("GAME OVER", True, (255,255,255)), (60, H//2 - 80))
        screen.blit(font.render(f"Score: {score}", True, (255,255,255)), (170, H//2))
        screen.blit(font.render("Click to play again", True, (200,200,200)), (90, H//2 + 60))

    pygame.display.flip()

pygame.quit()
