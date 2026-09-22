package miragefairy2024.mod.tool

import miragefairy2024.ModContext
import miragefairy2024.ModifyItemEnchantmentsHandler
import miragefairy2024.mixins.api.BlockCallback
import miragefairy2024.mod.Poem
import miragefairy2024.mod.PoemList
import miragefairy2024.mod.plus
import miragefairy2024.mod.tool.items.FairyToolItem
import miragefairy2024.mod.tool.items.onAfterBreakBlock
import miragefairy2024.mod.tool.items.onKilled
import miragefairy2024.util.generator
import miragefairy2024.util.registerChild
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents
import net.minecraft.core.BlockPos
import net.minecraft.tags.TagKey
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState

context(ModContext)
fun initToolConfiguration() {

    BlockCallback.AFTER_BREAK.register { level, player, pos, state, blockEntity, tool ->
        val item = tool.item
        if (item !is FairyToolItem) return@register
        item.onAfterBreakBlock(level, player, pos, state, blockEntity, tool)
    }

    ServerLivingEntityEvents.AFTER_DEATH.register { entity, damageSource ->
        val attacker = damageSource.entity as? LivingEntity ?: return@register
        val item = attacker.mainHandItem.item
        if (item !is FairyToolItem) return@register
        item.onKilled(entity, attacker, damageSource)
    }

}

interface ToolEffectType<in C : ToolConfiguration, T> {
    fun castOrThrow(value: Any?): T
    fun merge(a: T, b: T): T
    fun apply(configuration: C, value: T)
}

abstract class ToolConfiguration {

    private var initialized = false
    private val effects = mutableMapOf<ToolEffectType<*, *>, ToolEffectEntry<*>>()

    class ToolEffectEntry<T>(val type: ToolEffectType<*, T>, val value: T, val delegate: (T) -> Unit)

    fun <T> mergeImpl(type: ToolEffectType<*, T>, value: T, delegate: (T) -> Unit) {
        if (initialized) throw IllegalStateException("ToolConfiguration is already initialized.")
        val entry = effects[type]
        val newValue = if (entry == null) value else type.merge(type.castOrThrow(entry.value), value)
        effects[type] = ToolEffectEntry(type, newValue, delegate)
    }

    fun apply() {
        if (initialized) throw IllegalStateException("ToolConfiguration is already initialized.")
        initialized = true
        effects.forEach {
            fun <T> f(entry: ToolEffectEntry<T>) {
                entry.delegate(entry.value)
            }
            f(it.value)
        }
    }


    abstract val toolMaterialCard: ToolMaterialCard
    val tags = mutableListOf<TagKey<Item>>()
    var miningSpeedMultiplierOverride: Float? = null
    val superEffectiveBlocks = mutableListOf<Block>()
    val effectiveBlocks = mutableListOf<Block>()
    val effectiveBlockTags = mutableListOf<TagKey<Block>>()
    var miningDamage = 1
    var magicMiningDamage = 1.0
    val descriptions = mutableListOf<Poem>()
    var hasGlint = false
    var fireResistant = false
    val modifyItemEnchantmentsHandlers = mutableListOf<ModifyItemEnchantmentsHandler>()

    val onPostMineListeners = mutableListOf<(item: Item, stack: ItemStack, level: Level, state: BlockState, pos: BlockPos, miner: LivingEntity) -> Unit>()
    val onAfterBreakBlockListeners = mutableListOf<(item: Item, level: Level, player: Player, pos: BlockPos, state: BlockState, blockEntity: BlockEntity?, tool: ItemStack) -> Unit>()
    val onKilledListeners = mutableListOf<(item: Item, entity: LivingEntity, attacker: LivingEntity, damageSource: DamageSource) -> Unit>()
    val onInventoryTickListeners = mutableListOf<(item: Item, stack: ItemStack, level: Level, entity: Entity, slot: Int, selected: Boolean) -> Unit>()

    abstract fun createItem(properties: Item.Properties): Item

    context(ModContext)
    fun init(card: ToolCard) {
        tags.forEach {
            it.generator.registerChild(card.item)
        }
        toolMaterialCard.tag.generator.registerChild(card.item)
    }

    fun appendPoems(poemList: PoemList): PoemList {
        return descriptions.fold(poemList) { it, description -> it + description }
    }

}

fun <C : ToolConfiguration, T> C.merge(type: ToolEffectType<C, T>, value: T): C {
    this.mergeImpl(type, value) {
        type.apply(this, it)
    }
    return this
}

abstract class FairyMiningToolConfiguration : ToolConfiguration() {
    var attackDamage = 0F
    var attackSpeed = 0F
}
