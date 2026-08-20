package miragefairy2024.mixins.api;

/**
 * 使用中にプレイヤーの歩行速度が落ちないアイテムが実装するインターフェースなのだ～🌱
 * バニラの減速は UseAnim の種類に関わらず使用中かどうかだけで決まるから、mixin で除外する必要があるのだ～🌱
 */
public interface NoSlowdownWhileUsingItem {
}
