package miragefairy2024.client.mixins.api;

import net.minecraft.world.inventory.Slot;

public interface SlotHovering {
    /** 画面座標のマウス位置と元の判定結果を受け取り、スロットのホバー判定結果を返すのだ～🌱 */
    default boolean mirageFairy2024$isHovering(Slot slot, double mouseX, double mouseY, boolean hovering) {
        return hovering;
    }
}
