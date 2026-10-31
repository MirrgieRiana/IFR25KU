package miragefairy2024.client.mixins.api;

import org.jetbrains.annotations.Nullable;

/** スロットを、画面上の矩形の中だけへ見せるための設定なのだ～🌱 */
public interface SlotScissorAreaHolder {
    /** 4個の要素は、順に左端と上端と右端と下端なのだ～🌱 nullを渡すと、制限が無くなるのだ～🌱 */
    void mirageFairy2024$setScissorArea(@Nullable int[] scissorArea);

    @Nullable int[] mirageFairy2024$getScissorArea();
}
