package dev.mim1q.derelict.port.assets;

import dev.mim1q.derelict.Derelict;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Shown once on the title screen when the original jar was not found in {@code resourcepacks/}.
 * Only opens pages in the browser on request; never downloads anything.
 */
public class MissingAssetsScreen extends Screen {
    public static final String CURSEFORGE_URL = "https://www.curseforge.com/minecraft/mc-mods/derelict/files";
    public static final String MODRINTH_URL = "https://modrinth.com/mod/derelict/version/2.1.0";

    private static final int BUTTON_WIDTH = 260;
    private static final int BUTTON_HEIGHT = 20;
    private static final int SPACING = 24;

    private final Screen parent;
    private MultiLineLabel body = MultiLineLabel.EMPTY;
    private int bodyTop;
    private int statusY;
    private boolean notFound = false;

    public MissingAssetsScreen(Screen parent) {
        super(Component.translatable("derelict.port.assets.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int textWidth = Math.min(this.width - 40, 380);
        this.body = MultiLineLabel.create(this.font, textWidth, Component.translatable("derelict.port.assets.text"));

        int contentHeight = 12 + body.getLineCount() * 9 + 12 + 5 * SPACING + 24 + 12;
        int top = Math.max(10, (this.height - contentHeight) / 2);
        this.bodyTop = top + 18;
        int x = this.width / 2 - BUTTON_WIDTH / 2;
        int y = bodyTop + body.getLineCount() * 9 + 12;

        addRenderableWidget(Button.builder(Component.translatable("derelict.port.assets.button.curseforge"),
            b -> Util.getPlatform().openUri(URI.create(CURSEFORGE_URL))).bounds(x, y, BUTTON_WIDTH, BUTTON_HEIGHT).build());
        y += SPACING;
        addRenderableWidget(Button.builder(Component.translatable("derelict.port.assets.button.modrinth"),
            b -> Util.getPlatform().openUri(URI.create(MODRINTH_URL))).bounds(x, y, BUTTON_WIDTH, BUTTON_HEIGHT).build());
        y += SPACING;
        addRenderableWidget(Button.builder(Component.translatable("derelict.port.assets.button.open_folder"),
            b -> openFolder()).bounds(x, y, BUTTON_WIDTH, BUTTON_HEIGHT).build());
        y += SPACING;
        addRenderableWidget(Button.builder(Component.translatable("derelict.port.assets.button.check_again"),
            b -> checkAgain()).bounds(x, y, BUTTON_WIDTH, BUTTON_HEIGHT).build());
        y += SPACING;
        addRenderableWidget(Button.builder(Component.translatable("derelict.port.assets.button.continue"),
            b -> onClose()).bounds(x, y, BUTTON_WIDTH, BUTTON_HEIGHT).build());
        y += SPACING;
        addRenderableWidget(Checkbox.builder(Component.translatable("derelict.port.assets.dont_show_again"), this.font)
            .pos(x, y)
            .selected(AssetNoticeSettings.dontShowAgain())
            .onValueChange((box, value) -> AssetNoticeSettings.setDontShowAgain(value))
            .build());
        this.statusY = y + 24;
    }

    private void openFolder() {
        try {
            Path dir = OriginalAssetsLocator.resourcePacksDir();
            Files.createDirectories(dir);
            Util.getPlatform().openPath(dir);
        } catch (Exception e) {
            Derelict.LOGGER.warn("[derelict-port] Could not open the resourcepacks folder: {}", e.toString());
        }
    }

    private void checkAgain() {
        if (OriginalAssetsLocator.rescan().isPresent()) {
            this.notFound = false;
            if (this.minecraft != null) {
                this.minecraft.getResourcePackRepository().reload();
                this.minecraft.reloadResourcePacks();
                this.minecraft.setScreen(new TitleScreen());
            }
        } else {
            this.notFound = true;
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, bodyTop - 18, 0xFFFFAA00);
        body.renderCentered(graphics, this.width / 2, bodyTop, 9, 0xFFFFFFFF);
        if (notFound) {
            graphics.drawCenteredString(this.font, Component.translatable("derelict.port.assets.not_found"),
                this.width / 2, statusY, 0xFFFF5555);
        }
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderMenuBackground(graphics);
        graphics.fillGradient(0, 0, this.width, this.height, 0xC0101010, 0xD0101010);
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.parent);
        }
    }
}
