package miragefairy2024.client.mixins.api;

import miragefairy2024.mod.recipeviewer.view.IntRectangle;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public interface SlotViewClipping {
    @Nullable
    Supplier<IntRectangle> mirageFairy2024$getClipBounds();

    void mirageFairy2024$setClipBounds(@Nullable Supplier<IntRectangle> clipBounds);
}
