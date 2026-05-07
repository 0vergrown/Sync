package dev.overgrown.sync.mixin.disguise;

import com.mojang.serialization.JsonOps;
import dev.overgrown.sync.data.disguise.DisguiseData;
import dev.overgrown.sync.data.disguise.client.ClientDisguiseManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import com.google.gson.JsonParser;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererDisguiseNameMixin<T extends Entity> {

    @ModifyVariable(
        method = "renderLabelIfPresent",
        at = @At("HEAD"),
        argsOnly = true
    )
    private Text sync$modifyDisguiseLabel(
        Text originalText,
        T entity,
        Text text,
        MatrixStack matrices,
        VertexConsumerProvider vertexConsumers,
        int light,
        float tickDelta) {

        DisguiseData disguise = ClientDisguiseManager.getDisguise(entity.getId());
        if (disguise != null) {
            if (disguise.isPlayerDisguise()) {
                return sync$resolvePlayerName(disguise, originalText);
            }

            NbtCompound nbt = disguise.getTargetNbt();
            if (nbt != null && nbt.contains("CustomName")) {
                String json = nbt.getString("CustomName");
                try {
                    return TextCodecs.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json))
                        .result()
                        .orElse(originalText);
                } catch (Exception e) {
                    return originalText;
                }
            }
            EntityType<?> entityType = Registries.ENTITY_TYPE.get(disguise.getTargetEntityTypeId());
            return Text.translatable(entityType.getTranslationKey());
        }
        return originalText;
    }

    @Unique
    private Text sync$resolvePlayerName(DisguiseData disguise, Text fallback) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.getNetworkHandler() != null) {
            PlayerListEntry entry = client.getNetworkHandler()
                .getPlayerListEntry(disguise.getTargetPlayerUuid());
            if (entry != null && entry.getProfile().getName() != null
                && !entry.getProfile().getName().isEmpty()) {
                return Text.literal(entry.getProfile().getName());
            }
        }

        NbtCompound nbt = disguise.getTargetNbt();
        if (nbt != null && nbt.contains("sync$player_name")) {
            String name = nbt.getString("sync$player_name");
            if (!name.isEmpty()) {
                return Text.literal(name);
            }
        }

        return fallback;
    }
}
