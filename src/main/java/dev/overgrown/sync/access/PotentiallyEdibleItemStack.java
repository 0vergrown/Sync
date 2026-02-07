package dev.overgrown.sync.access;

import net.minecraft.item.FoodComponent;

import java.util.Optional;

public interface PotentiallyEdibleItemStack {
    Optional<FoodComponent> sync$getFoodComponent();
}
