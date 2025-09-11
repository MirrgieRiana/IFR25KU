package miragefairy2024.mod.magicplant

import miragefairy2024.clientProxy
import miragefairy2024.mod.Emoji
import miragefairy2024.mod.invoke
import miragefairy2024.util.darkGray
import miragefairy2024.util.darkRed
import miragefairy2024.util.green
import miragefairy2024.util.invoke
import miragefairy2024.util.join
import miragefairy2024.util.plus
import miragefairy2024.util.style
import miragefairy2024.util.text
import miragefairy2024.util.toBlockPos
import miragefairy2024.util.yellow
import mirrg.kotlin.helium.max
import mirrg.kotlin.helium.unit
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.inventory.ContainerLevelAccess
import net.minecraft.world.item.ItemNameBlockItem
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.item.context.BlockPlaceContext
import net.minecraft.world.item.context.UseOnContext
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block

class MagicPlantSeedItem(block: Block, settings: Properties) : ItemNameBlockItem(block, settings) {
    override fun getName(stack: ItemStack): Component = if (stack.isRare()) text { super.getName(stack) + " "() + Emoji.MUTATION() } else super.getName(stack)

    override fun appendHoverText(stack: ItemStack, context: TooltipContext, tooltipComponents: MutableList<Component>, tooltipFlag: TooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag)
        val player = clientProxy?.getClientPlayer() ?: return
        val world = player.level() ?: return

        // 特性を得る、無い場合はクリエイティブ専用
        val traitStacks = stack.getTraitStacks() ?: run {
            tooltipComponents += text { CREATIVE_ONLY_TRANSLATION().yellow }
            return
        }

        // 機能説明
        tooltipComponents += text { GUI_TRANSLATION().yellow }

        // プレイヤーのメインハンドの種子の特性を得る
        val otherTraitStacks = if (player.mainHandItem.item is MagicPlantSeedItem) player.mainHandItem.getTraitStacks() else null

        // ヘッダー行
        run {
            val sections = mutableListOf<Component>()

            // ラベル
            sections += text { TRAIT_TRANSLATION() + ":"() } // Trait:

            // 特性の個数
            val traitCount = traitStacks.traitStackList.size
            sections += text { "x$traitCount"().let { if (otherTraitStacks != null) it.signColor(traitCount - otherTraitStacks.traitStackList.size) else it } } // x99

            // 特性の増減
            val plusTraitCount = if (otherTraitStacks != null) (traitStacks.traitStackMap.keys - otherTraitStacks.traitStackMap.keys).size else null
            val minusTraitCount = if (otherTraitStacks != null) (otherTraitStacks.traitStackMap.keys - traitStacks.traitStackMap.keys).size else null
            sections += listOfNotNull(
                if (plusTraitCount != null && plusTraitCount > 0) text { "+$plusTraitCount"().signColor(1) } else null, // +9
                if (minusTraitCount != null && minusTraitCount > 0) text { "-$minusTraitCount"().signColor(-1) } else null, // -9
            ).let { if (it.isNotEmpty()) listOf(text { "("() + it.join(" "()) + ")"() }) else listOf() } // (+9 -9)  (+9)  null

            // 区切り
            sections += text { "/"() } // /

            // 特性ビットの個数
            val bitCount = traitStacks.positiveBitCount - traitStacks.negativeBitCount
            val otherBitCount = if (otherTraitStacks != null) otherTraitStacks.positiveBitCount - otherTraitStacks.negativeBitCount else null
            sections += text { "${bitCount}b"().let { if (otherBitCount != null) it.signColor(bitCount - otherBitCount) else it } } // 99b

            // 特性ビットの増減
            val plusBitCount = if (otherTraitStacks != null) (traitStacks - otherTraitStacks).positiveBitCount + (otherTraitStacks - traitStacks).negativeBitCount else null
            val minusBitCount = if (otherTraitStacks != null) (otherTraitStacks - traitStacks).positiveBitCount + (traitStacks - otherTraitStacks).negativeBitCount else null
            sections += listOfNotNull(
                if (plusBitCount != null && plusBitCount > 0) text { "+${plusBitCount}b"().signColor(1) } else null, // +9b
                if (minusBitCount != null && minusBitCount > 0) text { "-${minusBitCount}b"().signColor(-1) } else null, // -9b
            ).let { if (it.isNotEmpty()) listOf(text { "("() + it.join(" "()) + ")"() }) else listOf() } // (+9b -9b)  (+9b)  null

            tooltipComponents += sections.join(text { " "() }) // Trait: x99 (+9 -9) / 99b (+9 -9)
        }

        // 特性行
        val traitStackMap = if (otherTraitStacks != null) otherTraitStacks.traitStackMap.mapValues { 0 } + traitStacks.traitStackMap else traitStacks.traitStackMap // 比較対象がある場合は空特性も表示
        traitStackMap.entries
            .sortedBy { it.key }
            .forEach { (trait, level) ->
                val levelText = when {
                    otherTraitStacks == null -> text { level.toString(2)() }

                    else -> {
                        val otherLevel = otherTraitStacks.traitStackMap[trait] ?: 0
                        val bits = (level max otherLevel).toString(2).length
                        (bits - 1 downTo 0).map { bit ->
                            val mask = 1 shl bit
                            val isNegative = NegativeTraitBitsRegistry.get(trait).let { if (it == null) true else it and mask != 0 }
                            val possession = if (level and mask != 0) 1 else 0
                            val otherPossession = if (otherLevel and mask != 0) 1 else 0
                            when {
                                possession > otherPossession -> text { if (isNegative) "$possession"().darkRed else "$possession"().green }
                                possession == otherPossession -> text { "$possession"().darkGray }
                                else -> text { if (isNegative) "$possession"().green else "$possession"().darkRed }
                            }
                        }.join()
                    }
                }

                val traitEffects = trait.getTraitEffects(world, player.blockPosition(), world.getMagicPlantBlockEntity(player.blockPosition()), level)
                tooltipComponents += if (traitEffects != null) {
                    val description = text {
                        traitEffects.effects
                            .map {
                                fun <T : Any> TraitEffect<T>.render() = text { (this@render.key.emoji + this@render.key.renderValue(this@render.value)).style(this@render.key.style) }
                                it.render()
                            }
                            .reduce { a, b -> a + " "() + b }
                    }
                    text { ("  "() + trait.getName() + " "() + levelText + " ("() + description + ")"()).style(trait.style) }
                } else {
                    text { ("  "() + trait.getName() + " "() + levelText + " ("() + INVALID_TRANSLATION() + ")"()).darkGray }
                }
            }

    }

    override fun useOn(context: UseOnContext): InteractionResult {
        if (context.player?.isShiftKeyDown == true) return InteractionResult.PASS
        return super.useOn(context)
    }

    override fun place(context: BlockPlaceContext): InteractionResult {
        if (context.itemInHand.getTraitStacks() != null) {
            return super.place(context)
        } else {
            val player = context.player ?: return InteractionResult.FAIL
            if (!player.isCreative) return InteractionResult.FAIL
            return super.place(context)
        }
    }

    override fun isFoil(stack: ItemStack) = stack.isRare() || super.isFoil(stack)

    override fun use(world: Level, user: Player, hand: InteractionHand): InteractionResultHolder<ItemStack> {
        if (user.isShiftKeyDown) {
            val itemStack = user.getItemInHand(hand)
            if (world.isClientSide) return InteractionResultHolder.success(itemStack)
            val traitStacks = itemStack.getTraitStacks() ?: TraitStacks.EMPTY
            user.openMenu(object : ExtendedScreenHandlerFactory<Pair<TraitStacks, BlockPos>> {
                override fun createMenu(syncId: Int, playerInventory: Inventory, player: Player): AbstractContainerMenu {
                    return TraitListScreenHandler(syncId, playerInventory, ContainerLevelAccess.create(world, player.blockPosition()), traitStacks, player.position().add(0.0, 0.5, 0.0).toBlockPos())
                }

                override fun getDisplayName() = text { traitListScreenTranslation() }

                override fun getScreenOpeningData(player: ServerPlayer) = Pair(traitStacks, player.position().add(0.0, 0.5, 0.0).toBlockPos())
            })
            return InteractionResultHolder.consume(itemStack)
        }
        return super.use(world, user, hand)
    }
}

private fun Component.signColor(number: Int): Component {
    return when {
        number > 0 -> this.green
        number < 0 -> this.darkRed
        else -> this.darkGray
    }
}

fun ItemStack.getTraitStacks() = this.get(TRAIT_STACKS_DATA_COMPONENT_TYPE)
fun ItemStack.setTraitStacks(traitStacks: TraitStacks?) = unit { this.set(TRAIT_STACKS_DATA_COMPONENT_TYPE, traitStacks) }

fun ItemStack.isRare() = (this.get(RARITY_DATA_COMPONENT_TYPE) ?: 0) >= 1
fun ItemStack.setRare(isRare: Boolean) = unit { this.set(RARITY_DATA_COMPONENT_TYPE, if (isRare) 1 else 0) }
