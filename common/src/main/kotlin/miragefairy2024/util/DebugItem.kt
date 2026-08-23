package miragefairy2024.util

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mod.common.mirageFairy2024ItemGroupCard
import mirrg.kotlin.helium.toUpperCamelCase
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.data.models.model.ModelTemplates
import net.minecraft.data.models.model.TextureMapping
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import java.io.File
import java.io.IOException

fun interface TextureSource {
    fun getTextureMapping(): TextureMapping
}

fun Item.toTextureSource() = TextureSource { TextureMapping.layer0(this) }
fun Block.toTextureSource() = TextureSource { TextureMapping.layer0(this) }
fun ResourceLocation.toTextureSource() = TextureSource { TextureMapping.layer0(this) }

context(ModContext)
fun registerDebugItem(path: String, icon: TextureSource = Items.BOOK.toTextureSource(), color: Int = 0xFF888888.toInt(), action: (Level, Player, InteractionHand, ItemStack) -> Unit) {
    val item = Registration(BuiltInRegistries.ITEM, MirageFairy2024.identifier(path)) {
        object : Item(Properties()) {
            override fun getName(stack: ItemStack) = text { path.toUpperCamelCase(afterDelimiter = " ")() }
            override fun use(level: Level, user: Player, hand: InteractionHand): InteractionResultHolder<ItemStack> {
                action(level, user, hand, user.getItemInHand(hand))
                return InteractionResultHolder.sidedSuccess(user.getItemInHand(hand), level.isClientSide)
            }
        }
    }
    item.register()
    item.registerItemGroup(mirageFairy2024ItemGroupCard.itemGroupKey)
    item.registerModelGeneration(ModelTemplates.FLAT_ITEM) { icon.getTextureMapping() }
    item.registerColorProvider { _, _ -> color }
}

context(ModContext)
fun registerClientDebugItem(path: String, icon: TextureSource = Items.BOOK.toTextureSource(), color: Int = 0xFF888888.toInt(), action: (Level, Player, InteractionHand, ItemStack) -> Unit) {
    registerDebugItem(path, icon, color) { level, player, hand, itemStack ->
        if (level.isServer) return@registerDebugItem
        action(level, player, hand, itemStack)
    }
}

context(ModContext)
fun registerServerDebugItem(path: String, icon: TextureSource = Items.BOOK.toTextureSource(), color: Int = 0xFF888888.toInt(), action: (ServerLevel, ServerPlayer, InteractionHand, ItemStack) -> Unit) {
    registerDebugItem(path, icon, color) { level, player, hand, itemStack ->
        if (level.isClientSide) return@registerDebugItem
        action(level as ServerLevel, player as ServerPlayer, hand, itemStack)
    }
}

fun writeAction(player: Player, fileName: String, text: String) {
    val file = File("debug").resolve(fileName)
    player.displayClientMessage(text { "Saved to "() + file() }, false)
    when {
        file.parentFile.isDirectory -> Unit
        file.parentFile.exists() -> throw IOException("Failed to create directory: $file")
        !file.parentFile.mkdirs() -> throw IOException("Failed to create directory: $file")
    }
    file.writeText(text)
}
