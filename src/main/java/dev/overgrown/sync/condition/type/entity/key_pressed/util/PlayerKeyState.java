package dev.overgrown.sync.condition.type.entity.key_pressed.util;

import java.util.HashSet;
import java.util.Set;

public class PlayerKeyState {

    private final Set<String> pressedKeys = new HashSet<>();
    private final Set<String> justPressedKeys = new HashSet<>();

    public boolean isPressed(String keyId) {
        return pressedKeys.contains(keyId);
    }

    public boolean wasJustPressed(String keyId) {
        return justPressedKeys.contains(keyId);
    }

    public void update(Set<String> currentlyPressed) {
        for (String keyId : currentlyPressed) {
            if (!pressedKeys.contains(keyId)) {
                justPressedKeys.add(keyId);
            }
        }
        pressedKeys.clear();
        pressedKeys.addAll(currentlyPressed);
    }

    public void tick() {
        justPressedKeys.clear();
    }

    public void clear() {
        pressedKeys.clear();
        justPressedKeys.clear();
    }
}
