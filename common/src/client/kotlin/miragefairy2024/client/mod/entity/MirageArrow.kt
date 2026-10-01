package miragefairy2024.client.mod.entity

import miragefairy2024.MirageFairy2024
import miragefairy2024.mod.entity.MirageArrowEntity
import net.minecraft.client.renderer.entity.ArrowRenderer
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.resources.ResourceLocation

class MirageArrowEntityRenderer(context: EntityRendererProvider.Context) : ArrowRenderer<MirageArrowEntity>(context) {
    companion object {
        val TEXTURE: ResourceLocation = MirageFairy2024.identifier("textures/entity/projectiles/mirage_arrow.png")
    }

    override fun getTextureLocation(entity: MirageArrowEntity) = TEXTURE
}
