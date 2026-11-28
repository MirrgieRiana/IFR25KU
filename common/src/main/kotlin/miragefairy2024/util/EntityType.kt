package miragefairy2024.util

import miragefairy2024.migration.get__MIGRATION2612_ReplaceMethod // MIGRATION2612 RemoveImport
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation as Identifier // MIGRATION2612 ReplaceImport net.minecraft.resources.Identifier
import net.minecraft.world.entity.EntityType

/** レジストリに登録する前に呼び出すことはできません。 */
fun EntityType<*>.getIdentifier() = BuiltInRegistries.ENTITY_TYPE.getKey(this)

fun Identifier.toEntityType() = BuiltInRegistries.ENTITY_TYPE.get__MIGRATION2612_ReplaceMethod(this)
