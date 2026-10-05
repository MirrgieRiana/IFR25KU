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

/**
 * Fabric 側の {@link ParticleEngine#render} は固定の一覧を回るだけだから、独自の種別を足してもそのままでは描かれないのだ～🌱
 * NeoForge 側は一覧外の種別も並べる比較器を持つから、こちらのミックスインは Fabric 側にのみ置くのだ～🌱
 */
@Mixin(ParticleEngine.class)
public class ParticleEngineMixin {
    @Mutable
    @Shadow
    @Final
    private static List<ParticleRenderType> RENDER_ORDER;

    // 丸ごと置き換えるのではなく、その時点の値の末尾へ足すことで、同じ場所を触る他の MOD と共存できるのだ～🌱
    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void clinitTail(CallbackInfo ci) {
        RENDER_ORDER = ImmutableList.<ParticleRenderType>builder()
            .addAll(RENDER_ORDER)
            .add(AdditiveParticleRenderType.INSTANCE)
            .build();
    }
}
