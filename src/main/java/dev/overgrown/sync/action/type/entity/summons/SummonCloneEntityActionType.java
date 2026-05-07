package dev.overgrown.sync.action.type.entity.summons;

import dev.overgrown.sync.Sync;
import dev.overgrown.sync.power.type.summons.entities.clone.CloneEntity;
import dev.overgrown.sync.registry.SyncEntityActionTypes;
import dev.overgrown.sync.registry.SyncEntityRegistry;
import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.BiEntityAction;
import io.github.apace100.apoli.action.context.BiEntityActionContext;
import io.github.apace100.apoli.action.context.EntityActionContext;
import io.github.apace100.apoli.action.type.EntityActionType;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class SummonCloneEntityActionType extends EntityActionType {

    public static final TypedDataObjectFactory<SummonCloneEntityActionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("can_sit", SerializableDataTypes.BOOLEAN, true)
            .add("can_attack", SerializableDataTypes.BOOLEAN, true)
            .add("follow_owner", SerializableDataTypes.BOOLEAN, true)
            .add("inherit_equipment", SerializableDataTypes.BOOLEAN, true)
            .add("inherit_enchantments", SerializableDataTypes.BOOLEAN, true)
            .add("wide_texture", SerializableDataTypes.IDENTIFIER.optional(), Optional.empty())
            .add("slim_texture", SerializableDataTypes.IDENTIFIER.optional(), Optional.empty())
            .add("bientity_action", BiEntityAction.DATA_TYPE.optional(), Optional.empty()),
        data -> new SummonCloneEntityActionType(
            data.get("can_sit"),
            data.get("can_attack"),
            data.get("follow_owner"),
            data.get("inherit_equipment"),
            data.get("inherit_enchantments"),
            data.get("wide_texture"),
            data.get("slim_texture"),
            data.get("bientity_action")
        ),
        (a, sd) -> sd.instance()
            .set("can_sit", a.canSit)
            .set("can_attack", a.canAttack)
            .set("follow_owner", a.followOwner)
            .set("inherit_equipment", a.inheritEquipment)
            .set("inherit_enchantments", a.inheritEnchantments)
            .set("wide_texture", a.wideTexture)
            .set("slim_texture", a.slimTexture)
            .set("bientity_action", a.bientityAction)
    );

    private final boolean canSit;
    private final boolean canAttack;
    private final boolean followOwner;
    private final boolean inheritEquipment;
    private final boolean inheritEnchantments;
    private final Optional<Identifier> wideTexture;
    private final Optional<Identifier> slimTexture;
    private final Optional<BiEntityAction> bientityAction;

    public SummonCloneEntityActionType(boolean canSit, boolean canAttack, boolean followOwner,
                                       boolean inheritEquipment, boolean inheritEnchantments,
                                       Optional<Identifier> wideTexture, Optional<Identifier> slimTexture,
                                       Optional<BiEntityAction> bientityAction) {
        this.canSit = canSit;
        this.canAttack = canAttack;
        this.followOwner = followOwner;
        this.inheritEquipment = inheritEquipment;
        this.inheritEnchantments = inheritEnchantments;
        this.wideTexture = wideTexture;
        this.slimTexture = slimTexture;
        this.bientityAction = bientityAction;
    }

    @Override
    public void accept(EntityActionContext context) {
        if (context.entity() instanceof PlayerEntity player) {
            CloneEntity clone = summon(player);
            if (clone != null) {
                wideTexture.ifPresent(clone::setCustomWideTexture);
                slimTexture.ifPresent(clone::setCustomSlimTexture);
                bientityAction.ifPresent(a -> a.accept(new BiEntityActionContext(player, clone)));
            }
        } else {
            Sync.LOGGER.warn("Attempted to summon clone of invalid entity. Only Players are compatible with this action type.");
        }
    }

    private CloneEntity summon(PlayerEntity player) {
        if (player.getWorld() instanceof ServerWorld serverWorld) {
            CloneEntity clone = new CloneEntity(SyncEntityRegistry.CLONE, serverWorld);
            clone.setCanSit(canSit);
            clone.setCanAttack(canAttack);
            clone.setFollowOwner(followOwner);

            clone.refreshPositionAndAngles(player.getX(), player.getY(), player.getZ(), player.getHeadYaw(), 0);
            clone.initialize(serverWorld, serverWorld.getLocalDifficulty(player.getBlockPos()), SpawnReason.REINFORCEMENT, null);
            clone.setCustomName(player.getName());
            clone.setOwnerUUID(player.getUuid());
            clone.setCanPickUpLoot(false);
            clone.setPersistent();

            serverWorld.spawnEntity(clone);

            if (inheritEquipment) {
                for (EquipmentSlot slot : EquipmentSlot.values()) {
                    ItemStack equipped = player.getEquippedStack(slot);
                    if (!equipped.isEmpty()) {
                        clone.setEquipmentDropChance(slot, 0f);

                        ItemStack copy = new ItemStack(equipped.getItem(), equipped.getCount());
                        if (inheritEnchantments) {
                            EnchantmentHelper.set(copy, EnchantmentHelper.getEnchantments(equipped));
                        }
                        clone.equipStack(slot, copy);
                    }
                }
            }
            clone.updateWeaponGoals();
            return clone;
        }
        return null;
    }

    @Override
    public @NotNull ActionConfiguration<?> getConfig() {
        return SyncEntityActionTypes.SUMMON_CLONE;
    }
}
