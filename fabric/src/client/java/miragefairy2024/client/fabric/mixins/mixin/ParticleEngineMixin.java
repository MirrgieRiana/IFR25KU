package miragefairy2024.client.fabric.mixins.mixin;

import com.google.common.collect.ImmutableList;
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

// Fabricのパーティクル描画は、この定数に並んだ種別だけを順に回るのだ～🌱
// NeoForgeの側は本体のパッチでこの定数に依らない形になっているから、この問題はFabric側だけに存在するのだ～🌱
@Mixin(ParticleEngine.class)
public class ParticleEngineMixin {
    @Shadow
    @Final
    @Mutable
    private static List<ParticleRenderType> RENDER_ORDER;

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void addAdditiveParticleRenderType(CallbackInfo ci) {
        // 定数を丸ごと組み直すのではなく現在の値を元に末尾へ1個加えるから、他のMODが足した種別とも共存できるのだ～🌱
        RENDER_ORDER = ImmutableList.<ParticleRenderType>builder().addAll(RENDER_ORDER).add(AdditiveParticleRenderType.INSTANCE).build();
    }
}
