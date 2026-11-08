package com.example.tilemod;

import java.util.ArrayDeque;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.MathHelper;

public class TileMod implements ClientModInitializer {
    public static boolean keystrokes = true;
    public static boolean fps = true;
    public static boolean coords = false;
    public static boolean cps = true;
    public static boolean clickSound = true;
    public static boolean rainbow = false;

    private static boolean lastUse, lastAttack;
    private static final boolean[] lastMove = new boolean[4];
    private static final ArrayDeque<Long> clicks = new ArrayDeque<>();

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(TileMod::tick);
        HudElementRegistry.attachElementAfter(
                VanillaHudElements.MISC_OVERLAYS,
                Identifier.of("tilemod", "hud"),
                (context, tickCounter) -> renderHud(context));
    }

    public static int accent(float offset) {
        if (!rainbow) return 0xFF7F77DD;
        float h = (((Util.getMeasuringTimeMs() / 20L) % 360L) / 360f + offset) % 1f;
        return 0xFF000000 | MathHelper.hsvToRgb(h, 0.7f, 1f);
    }

    private static void tick(MinecraftClient c) {
        if (c.player == null) return;
        GameOptions o = c.options;

        boolean use = o.useKey.isPressed();
        if (use && !lastUse
                && c.currentScreen == null
                && c.player.getMainHandStack().isEmpty()
                && c.crosshairTarget != null
                && c.crosshairTarget.getType() == HitResult.Type.MISS) {
            c.setScreen(new TileScreen());
        }
        lastUse = use;

        long now = Util.getMeasuringTimeMs();
        boolean atk = o.attackKey.isPressed();
        if (atk && !lastAttack) clicks.addLast(now);
        lastAttack = atk;
        while (!clicks.isEmpty() && now - clicks.peekFirst() > 1000L) clicks.pollFirst();

        boolean[] move = {
                o.forwardKey.isPressed(), o.leftKey.isPressed(),
                o.backKey.isPressed(), o.rightKey.isPressed()
        };
        boolean fresh = false;
        for (int i = 0; i < 4; i++) {
            if (move[i] && !lastMove[i]) fresh = true;
            lastMove[i] = move[i];
        }
        if (fresh && clickSound && c.currentScreen == null) {
            c.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0f));
        }
    }

    private static void renderHud(DrawContext g) {
        MinecraftClient c = MinecraftClient.getInstance();
        if (c.player == null || c.options.hudHidden) return;
        TextRenderer tr = c.textRenderer;

        int y = 6;
        if (fps) {
            g.drawTextWithShadow(tr, c.getCurrentFps() + " FPS", 6, y, accent(0f));
            y += 11;
        }
        if (cps) {
            g.drawTextWithShadow(tr, clicks.size() + " CPS", 6, y, accent(0.2f));
            y += 11;
        }
        if (coords) {
            String s = String.format("XYZ %.0f %.0f %.0f", c.player.getX(), c.player.getY(), c.player.getZ());
            g.drawTextWithShadow(tr, s, 6, y, accent(0.4f));
        }

        if (keystrokes) {
            GameOptions o = c.options;
            int s = 22, gp = 2, x = 8;
            int top = g.getScaledWindowHeight() - 98;
            key(g, tr, "W", x + s + gp, top, s, s, o.forwardKey.isPressed());
            key(g, tr, "A", x, top + s + gp, s, s, o.leftKey.isPressed());
            key(g, tr, "S", x + s + gp, top + s + gp, s, s, o.backKey.isPressed());
            key(g, tr, "D", x + 2 * (s + gp), top + s + gp, s, s, o.rightKey.isPressed());
            int full = 3 * s + 2 * gp;
            int half = (full - gp) / 2;
            int row3 = top + 2 * (s + gp);
            key(g, tr, "LMB", x, row3, half, s, o.attackKey.isPressed());
            key(g, tr, "RMB", x + half + gp, row3, full - half - gp, s, o.useKey.isPressed());
            key(g, tr, "SPACE", x, row3 + s + gp, full, 14, o.jumpKey.isPressed());
        }
    }

    private static void key(DrawContext g, TextRenderer tr, String label, int x, int y, int w, int h, boolean down) {
        g.fill(x, y, x + w, y + h, down ? 0xCCFFFFFF : 0x88000000);
        if (down) g.fill(x, y + h - 2, x + w, y + h, accent(0f));
        int tx = x + (w - tr.getWidth(label)) / 2;
        int ty = y + (h - 8) / 2;
        g.drawText(tr, label, tx, ty, down ? 0xFF000000 : 0xFFFFFFFF, false);
    }
            }
