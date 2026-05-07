package dev.overgrown.sync.action.type.entity.radial_menu.client;

import dev.overgrown.sync.action.type.entity.radial_menu.utils.RadialMenu;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

@Environment(EnvType.CLIENT)
public class RadialMenuScreen extends Screen {

    private int elapsedTime;
    private final RadialMenu radialMenu;

    public RadialMenuScreen(RadialMenu radialMenu) {
        super(Text.literal("Radial Menu"));
        this.radialMenu = radialMenu;
        this.elapsedTime = 0;
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
        radialMenu.resetButtons();
        radialMenu.draw(this.client, elapsedTime);
        radialMenu.getEntries().forEach(entry -> {
            if (entry.getButton() != null) {
                addDrawableChild(entry.getButton());
            }
        });
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        if (client != null) {
            radialMenu.renderBackground(context, client);
            radialMenu.renderButtons(context, client, mouseX, mouseY, delta);
            radialMenu.renderIcons(context, client);
        }
    }

    @Override
    public void close() {
        super.close();
        radialMenu.resetButtons();
    }
}
