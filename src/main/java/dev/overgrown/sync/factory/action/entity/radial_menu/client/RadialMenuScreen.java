package dev.overgrown.sync.factory.action.entity.radial_menu.client;

import dev.overgrown.sync.factory.action.entity.radial_menu.utils.RadialMenu;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

@Environment(EnvType.CLIENT)
public class RadialMenuScreen extends Screen {
    private int elapsedTime;
    private final RadialMenu radialMenu;
    private boolean initialized = false;

    protected RadialMenuScreen(RadialMenu radialMenu) {
        super(Text.literal("Radial Menu"));
        this.radialMenu = radialMenu;
        elapsedTime = 0;
    }

    public int getElapsedTime() {
        return elapsedTime;
    }

    public RadialMenu getRadialMenu() {
        return radialMenu;
    }

    @Override
    public void tick() {
        elapsedTime += 1;
        radialMenu.draw(this.client, elapsedTime);
    }

    @Override
    public void init() {
        // Reset buttons to make sure clean state
        radialMenu.resetButtons();
        // Initialize buttons
        radialMenu.draw(this.client, elapsedTime);
        // Add buttons to screen
        radialMenu.getEntries().forEach(radialMenuEntry -> {
            if (radialMenuEntry.getButton() != null) {
                addDrawableChild(radialMenuEntry.getButton());
            }
        });
        initialized = true;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (client != null) {
            // Don't clear children every frame since they're already added in init()
            this.radialMenu.getEntries().forEach(radialMenuEntry -> {
                if (radialMenuEntry.getButton() != null) {
                    radialMenuEntry.getButton().render(context, mouseX, mouseY, delta);
                }
                context.drawItem(
                        radialMenuEntry.getStack(),
                        Math.round(radialMenuEntry.getPosition().x()),
                        Math.round(radialMenuEntry.getPosition().y()),
                        0,
                        100
                );
            });
        }
    }

    @Override
    public void close() {
        super.close();
        // Reset for next time
        radialMenu.resetButtons();
    }
}