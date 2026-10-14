package miragefairy2024.client.neoforge.mixins.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.MeshData;
import net.minecraft.client.particle.ParticleEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

// NeoForgeは引数の多いオーバーロードを足していて、3引数の側はそちらへ委譲するだけの非推奨になっているのだ～🌱
// だから、本体が実際に通る5引数の側だけを狙うのだ～🌱
@Mixin(ParticleEngine.class)
public class ParticleEngineMixin {
    // 加算合成のパーティクルがブレンディング関数を書き換えるのに、描画の終わりにブレンドを無効化するだけではリセットされないのだ～🌱
    // 水中や炎の画面効果はブレンディング関数を設定せずにブレンドを有効化するから、リセットしないとそれらが加算合成で描かれてしまうのだ～🌱
    // メソッド全体の末尾ではなく種別ごとの描画の呼び出しを包むことで、後続の種別がブレンディング関数を設定しない場合にも汚染されないのだ～🌱
    @WrapOperation(
        method = "render(Lnet/minecraft/client/renderer/LightTexture;Lnet/minecraft/client/Camera;FLnet/minecraft/client/renderer/culling/Frustum;Ljava/util/function/Predicate;)V",
        at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/BufferUploader;drawWithShader(Lcom/mojang/blaze3d/vertex/MeshData;)V")
    )
    private void resetBlendFunc(MeshData meshData, Operation<Void> original) {
        try {
            original.call(meshData);
        } finally {
            RenderSystem.defaultBlendFunc();
        }
    }
}
