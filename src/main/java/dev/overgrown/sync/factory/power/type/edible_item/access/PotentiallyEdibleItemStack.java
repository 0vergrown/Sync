package dev.overgrown.sync.factory.power.type.edible_item.access;

import net.minecraft.item.FoodComponent;

import java.util.Optional;

public interface PotentiallyEdibleItemStack {
    Optional<FoodComponent> sync$getFoodComponent();
}
