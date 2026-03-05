package dev.overgrown.sync.factory.compatibility.jade;

import dev.overgrown.sync.factory.compatibility.jade.power.type.modify_jade_tooltip.ModifyJadeTooltipPower;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.Prioritized;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.Identifiers;
import snownee.jade.api.WailaPlugin;
import snownee.jade.api.config.IPluginConfig;

/**
 * Jade plugin for the Sync mod.
 *
 * <p>Register in fabric.mod.json:</p>
 * <pre>{@code
 * "entrypoints": {
 *   "jade": [ "dev.overgrown.sync.compat.jade.SyncJadePlugin" ]
 * }
 * }</pre>
 */
@WailaPlugin
public class SyncJadePlugin implements IWailaPlugin {

    /** UID for the name-replacement component provider. */
    static final Identifier UID_NAME_OVERRIDE = new Identifier("sync", "jade_name_override");

    @Override
    public void registerClient(IWailaClientRegistration registration) {

        // ------------------------------------------------------------------
        // 1. Component provider: injects the replacement name at index 0.
        //    Runs at priority -4999 so it lands just inside the normal range.
        //    The collected-callback below then removes Jade's built-in name.
        // ------------------------------------------------------------------
        registration.registerEntityComponent(NameOverrideProvider.INSTANCE, LivingEntity.class);

        // ------------------------------------------------------------------
        // 2. Tooltip-collected callback.
        //    Signature: onTooltipCollected(ITooltip tooltip, Accessor<?> accessor)
        //    Runs after ALL component providers have appended their content.
        // ------------------------------------------------------------------
        registration.addTooltipCollectedCallback((tooltip, accessor) -> {
            if (!(accessor instanceof EntityAccessor entityAccessor)) return;

            Entity entity = entityAccessor.getEntity();
            if (!(entity instanceof LivingEntity living)) return;

            PowerHolderComponent component = PowerHolderComponent.KEY.maybeGet(living).orElse(null);
            if (component == null) return;

            Entity viewer = MinecraftClient.getInstance().getCameraEntity();
            if (viewer == null) return;

            // Resolve the highest-priority power that applies for this viewer.
            Prioritized.CallInstance<ModifyJadeTooltipPower> call = new Prioritized.CallInstance<>();
            call.add(living, ModifyJadeTooltipPower.class,
                    p -> p.isActive() && p.shouldApplyForViewer(viewer));

            if (!call.hasPowers(call.getMaxPriority())) return;

            ModifyJadeTooltipPower power = call.getPowers(call.getMaxPriority()).get(0);

            // --- hide everything ---
            if (power.isHideTooltip()) {
                tooltip.clear();
                return;
            }

            // --- strip individual sections ---
            // Health hearts row
            if (power.isHideHealth()) {
                tooltip.remove(Identifiers.MC_ENTITY_HEALTH);
            }

            // Armor row  (correct identifier: MC_ENTITY_ARMOR)
            if (power.isHideArmor()) {
                tooltip.remove(Identifiers.MC_ENTITY_ARMOR);
            }

            // Potion effects row
            if (power.isHidePotionEffects()) {
                tooltip.remove(Identifiers.MC_POTION_EFFECTS);
            }

            // --- name replacement ---
            // CORE_OBJECT_NAME is the tag Jade uses for the entity-name line.
            if (power.getDisplayName() != null) {
                tooltip.remove(Identifiers.CORE_OBJECT_NAME);
            }
        });
    }

    // -----------------------------------------------------------------------
    // Inner provider – injects display_name at the top of the tooltip
    // -----------------------------------------------------------------------

    private enum NameOverrideProvider implements IEntityComponentProvider {
        INSTANCE;

        @Override
        public void appendTooltip(ITooltip tooltip,
                                  EntityAccessor accessor,
                                  IPluginConfig config) {
            Entity entity = accessor.getEntity();
            if (!(entity instanceof LivingEntity living)) return;

            PowerHolderComponent component = PowerHolderComponent.KEY.maybeGet(living).orElse(null);
            if (component == null) return;

            Entity viewer = MinecraftClient.getInstance().getCameraEntity();
            if (viewer == null) return;

            Prioritized.CallInstance<ModifyJadeTooltipPower> call = new Prioritized.CallInstance<>();
            call.add(living, ModifyJadeTooltipPower.class,
                    p -> p.isActive() && p.getDisplayName() != null && p.shouldApplyForViewer(viewer));

            if (!call.hasPowers(call.getMaxPriority())) return;

            ModifyJadeTooltipPower power = call.getPowers(call.getMaxPriority()).get(0);

            // Insert at line 0 so it sits at the very top.
            // The collected-callback removes CORE_OBJECT_NAME afterward.
            tooltip.add(0, power.getDisplayName());
        }

        @Override
        public int getDefaultPriority() {
            // Must be lowed (numerically) than Jade's own name provider
            // so our entry arrives first and the callback finds it at index 0.
            return -4999;
        }

        @Override
        public Identifier getUid() {
            return UID_NAME_OVERRIDE;
        }
    }
}