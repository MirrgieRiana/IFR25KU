package miragefairy2024.client.mixins.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.particle.ParticleEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ParticleEngine.class)
public class ParticleEngineMixin {
    // 加算合成のパーティクルが合成関数を書き換えるのに、描画の終わりに合成を無効化するだけで合成関数は既定値へ戻らないのだ～🌱
    // 水中や炎の画面効果は合成関数を設定せずに合成を有効化するから、戻さないとそれらが加算合成で描かれてしまうのだ～🌱
    // NeoForge はこのメソッドを引数の違う形へ差し替えているから、両方を狙って、マッチした方だけへ注入するのだ～🌱
    @Inject(
            method = {
                    "render(Lnet/minecraft/client/renderer/LightTexture;Lnet/minecraft/client/Camera;F)V",
                    "render(Lnet/minecraft/client/renderer/LightTexture;Lnet/minecraft/client/Camera;FLnet/minecraft/client/renderer/culling/Frustum;Ljava/util/function/Predicate;)V",
            },
            at = @At(value = "TAIL")
    )
    private void render(CallbackInfo ci) {
        RenderSystem.defaultBlendFunc();
    }
}
