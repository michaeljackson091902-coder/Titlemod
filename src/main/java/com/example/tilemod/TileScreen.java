package com.example.tilemod;

import java.util.List;
import java.util.function.BooleanSupplier;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class TileScreen extends Screen {
    private record Tile(String name, BooleanSupplier on, Runnable toggle) {}

    private int px, py, pw, ph;

    public TileScreen() {
        super(Text.literal("Tile Menu"));
    }

    private static List<Tile> tiles() {
        return List.of(
                new Tile("Keystrokes", () -> TileMod.keystrokes, () -> TileMod.keystrokes = !TileMod.keystrokes),
                new Tile("FPS counter", () -> TileMod.fps, () -> TileMod.fps = !TileMod.fps),
                new Tile("Coordinates", () -> TileMod.coords, () -> TileMod.coords = !TileMod.coords),
                new Tile("CPS counter", () -> TileMod.cps, () -> TileMod.cps = !TileMod.cps),
                new Tile("Click sound", () -> TileMod.clickSound, () -> TileMod.clickSound = !TileMod.clickSound),
                new Tile("Rainbow theme", () -> TileMod.rainbow, () -> TileMod.rainbow = !TileMod.rainbow)
        );
    }

    @Override
    protected void init() {
        List<Tile> list = tiles();
        pw = Math.min(340, width - 8);
        int gap = 8, side = 12, th = 40;
        int tw = (pw - side * 2 - gap) / 2;
        int rows = (list.size() + 1) / 2;
        ph = 36 + rows * (th + gap) + 4;
        px = (width - pw) / 2;
        py = height - ph;

        for (int i = 0; i < list.size(); i++) {
            int x = px + side + (i % 2) * (tw + gap);
            int y = py + 36 + (i / 2) * (th + gap);
            addDrawableChild(new TileButton(x, y, tw, th, list.get(i)));
        }
        addDrawableChild(ButtonWidget.builder(Text.literal("Done"), b -> close())
                .dimensions(px + pw - side - 50, py + 10, 50, 20)
                .build());
    }

    @Override
    public void renderBackground(DrawContext g, int mouseX, int mouseY, float delta) {
        g.fill(0, 0, width, height, 0x55000000);
        g.fill(px, py, px + pw, py + ph, 0xF01C1F26);
        int seg = pw / 5;
        for (int i = 0; i < 5; i++) {
            int x1 = px + i * seg;
            int x2 = i == 4 ? px + pw : x1 + seg;
            g.fill(x1, py, x2, py + 3, TileMod.accent(i * 0.2f));
        }
        g.fill(px + pw / 2 - 18, py + 5, px + pw / 2 + 18, py + 7, 0xFF3A4050);
        g.drawTextWithShadow(textRenderer, "Quick modules", px + 12, py + 16, 0xFFE6E9EF);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    private static class TileButton extends ButtonWidget {
        private final Tile tile;

        TileButton(int x, int y, int w, int h, Tile tile) {
            super(x, y, w, h, Text.literal(tile.name()), b -> tile.toggle().run(), DEFAULT_NARRATION_SUPPLIER);
            this.tile = tile;
        }

        @Override
        public void renderWidget(DrawContext g, int mouseX, int mouseY, float delta) {
            boolean on = tile.on().getAsBoolean();
            int x = getX(), y = getY(), w = getWidth(), h = getHeight();
            int bg = on ? 0xFF2F2A52 : (isHovered() ? 0xFF2F3542 : 0xFF252A34);
            int border = on ? TileMod.accent(0f) : 0xFF2F3440;
            g.fill(x, y, x + w, y + h, border);
            g.fill(x + 1, y + 1, x + w - 1, y + h - 1, bg);
            TextRenderer tr = MinecraftClient.getInstance().textRenderer;
            g.drawTextWithShadow(tr, tile.name(), x + 10, y + 9, on ? 0xFFE6E9EF : 0xFFC4C9D4);
            g.drawTextWithShadow(tr, on ? "ON" : "OFF", x + 10, y + h - 18, on ? 0xFF97C459 : 0xFF8B93A3);
        }
    }
              }
