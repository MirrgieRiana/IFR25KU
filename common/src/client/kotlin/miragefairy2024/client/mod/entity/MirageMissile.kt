package miragefairy2024.client.mod.entity

import miragefairy2024.MirageFairy2024
import miragefairy2024.mod.entity.MirageMissileEntity
import net.minecraft.client.renderer.entity.ArrowRenderer
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.resources.ResourceLocation

class MirageMissileEntityRenderer(context: EntityRendererProvider.Context) : ArrowRenderer<MirageMissileEntity>(context) {
    companion object {
        val TEXTURE: ResourceLocation = MirageFairy2024.identifier("textures/entity/projectiles/mirage_missile.png")
    }

    override fun getTextureLocation(entity: MirageMissileEntity) = TEXTURE
}
