package miragefairy2024.client.fabric.mixins.mixin;

import com.google.common.collect.ImmutableList;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.MeshData;
import miragefairy2024.client.mod.particle.AdditiveParticleRenderType;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleRenderType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ParticleEngine.class)
public class ParticleEngineMixin {
    @Shadow
    @Final
    @Mutable
    private static List<ParticleRenderType> RENDER_ORDER;

    // Fabricのパーティクル描画は、この定数に並んだ種別だけを順に回るのだ～🌱
    // NeoForgeの側は本体のパッチでこの定数に依らない形になっているから、この問題はFabric側だけに存在するのだ～🌱
    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void addAdditiveParticleRenderType(CallbackInfo ci) {
        // 定数を丸ごと組み直すのではなく現在の値を元に末尾へ1個加えるから、他のMODが足した種別とも共存できるのだ～🌱
        RENDER_ORDER = ImmutableList.<ParticleRenderType>builder().addAll(RENDER_ORDER).add(AdditiveParticleRenderType.INSTANCE).build();
    }

    // 加算合成のパーティクルがブレンディング関数を書き換えるのに、描画の終わりにブレンドを無効化するだけではリセットされないのだ～🌱
    // 水中や炎の画面効果はブレンディング関数を設定せずにブレンドを有効化するから、リセットしないとそれらが加算合成で描かれてしまうのだ～🌱
    // メソッド全体の末尾ではなく種別ごとの描画の呼び出しを包むことで、後続の種別がブレンディング関数を設定しない場合にも汚染されないのだ～🌱
    @WrapOperation(
        method = "render(Lnet/minecraft/client/renderer/LightTexture;Lnet/minecraft/client/Camera;F)V",
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
