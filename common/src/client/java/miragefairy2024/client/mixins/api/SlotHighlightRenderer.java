package miragefairy2024.client.mixins.api;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.inventory.Slot;

public interface SlotHighlightRenderer {
    /**
     * Fabric のスロットのハイライト描画を包むのだ～🌱
     * 元の描画を行う場合は、この呼び出しの中で render.run() を呼ぶのだ～🌱
     */
    default void mirageFairy2024$renderSlotHighlight(GuiGraphics guiGraphics, Slot slot, Runnable render) {
        render.run();
    }
}
