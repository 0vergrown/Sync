package dev.overgrown.sync.action.type.entity.summons.utils;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Tameable;

import java.util.UUID;

public interface ExtraTameable extends Tameable {
    void setOwnerUUID(UUID uuid);

    default void setOwner(LivingEntity owner) {
        if (owner == null) {
            this.setOwnerUUID(null);
        } else {
            this.setOwnerUUID(owner.getUuid());
        }
    }

    boolean isOwned();
}
