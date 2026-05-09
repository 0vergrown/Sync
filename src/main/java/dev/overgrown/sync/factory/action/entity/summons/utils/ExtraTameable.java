package dev.overgrown.sync.factory.action.entity.summons.utils;

import java.util.UUID;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Tameable;

public interface ExtraTameable extends Tameable {
    void setOwnerUUID (UUID uuid);

    default void setOwner (LivingEntity owner) {
        if (owner == null) {
            this.setOwnerUUID(null);
        }
        else {
            UUID uuid = owner.getUuid();
            this.setOwnerUUID(uuid);
        }
    }

    boolean isOwned ();
}