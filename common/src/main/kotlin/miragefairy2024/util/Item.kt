package miragefairy2024.util

import miragefairy2024.migration.get__MIGRATION2612_ReplaceMethod // MIGRATION2612 RemoveImport
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation as Identifier // MIGRATION2612 ReplaceImport net.minecraft.resources.Identifier
import net.minecraft.world.item.Item

/** レジストリに登録する前に呼び出すことはできません。 */
fun Item.getIdentifier() = BuiltInRegistries.ITEM.getKey(this)

fun Identifier.toItem() = BuiltInRegistries.ITEM.get__MIGRATION2612_ReplaceMethod(this)
