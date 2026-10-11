package miragefairy2024.client.neoforge.mixins.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.particle.ParticleEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// NeoForgeは引数の多いオーバーロードを足していて、3引数の側はそちらへ委譲するだけの非推奨になっているのだ～🌱
// だから、本体が実際に通る5引数の側だけを狙うのだ～🌱
@Mixin(ParticleEngine.class)
public class ParticleEngineMixin {
    // 加算合成のパーティクルがブレンディング関数を書き換えるのに、描画の終わりにブレンドを無効化するだけではリセットされないのだ～🌱
    // 水中や炎の画面効果はブレンディング関数を設定せずにブレンドを有効化するから、リセットしないとそれらが加算合成で描かれてしまうのだ～🌱
    @Inject(
            method = "render(Lnet/minecraft/client/renderer/LightTexture;Lnet/minecraft/client/Camera;FLnet/minecraft/client/renderer/culling/Frustum;Ljava/util/function/Predicate;)V",
            at = @At(value = "TAIL")
    )
    private void render(CallbackInfo ci) {
        RenderSystem.defaultBlendFunc();
    }
}
