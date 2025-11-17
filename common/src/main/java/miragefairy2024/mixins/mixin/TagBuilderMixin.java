package miragefairy2024.mixins.mixin;

import miragefairy2024.mixins.api.TagBuilderApi;
import net.minecraft.tags.TagBuilder;
import net.minecraft.tags.TagEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Comparator;
import java.util.List;

@Mixin(TagBuilder.class)
public class TagBuilderMixin {
    @Inject(method = "build", at = @At("RETURN"), cancellable = true)
    private void build(CallbackInfoReturnable<List<TagEntry>> cir) {
        if (TagBuilderApi.sorting) {
            cir.setReturnValue(cir.getReturnValue().stream().sorted(Comparator.comparing(TagEntry::toString)).toList());
        }
    }
}
