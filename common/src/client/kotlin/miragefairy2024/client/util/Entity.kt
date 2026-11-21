package miragefairy2024.client.util

import miragefairy2024.ModContext
import miragefairy2024.ModEvents
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType

context(_: ModContext)
fun <E : Entity> (() -> EntityType<E>).registerEntityRenderer(entityRendererFactory: EntityRendererProvider<E>) {
    ModEvents.onClientInit {
        EntityRendererRegistry.register(this(), entityRendererFactory)
    }
}
