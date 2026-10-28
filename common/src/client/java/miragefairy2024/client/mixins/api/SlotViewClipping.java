package miragefairy2024.client.mixins.api;

import miragefairy2024.mod.recipeviewer.view.IntRectangle;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public interface SlotViewClipping {
    @Nullable
    Supplier<IntRectangle> mirageFairy2024$getClipBounds();

    void mirageFairy2024$setClipBounds(@Nullable Supplier<IntRectangle> clipBounds);

    default boolean mirageFairy2024$isInsideClip(double mouseX, double mouseY) {
        var provider = mirageFairy2024$getClipBounds();
        if (provider == null) return true;
        var bounds = provider.get();
        return mouseX >= bounds.getX() && mouseX < bounds.getX() + bounds.getSizeX() && mouseY >= bounds.getY() && mouseY < bounds.getY() + bounds.getSizeY();
    }
}
