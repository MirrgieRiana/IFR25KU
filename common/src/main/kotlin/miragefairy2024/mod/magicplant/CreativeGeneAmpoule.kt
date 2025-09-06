package miragefairy2024.mod.magicplant

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mod.PoemList
import miragefairy2024.mod.description
import miragefairy2024.mod.magicplant.contents.TraitCard
import miragefairy2024.mod.poem
import miragefairy2024.mod.registerPoem
import miragefairy2024.mod.registerPoemGeneration
import miragefairy2024.util.EnJa
import miragefairy2024.util.ItemGroupCard
import miragefairy2024.util.Model
import miragefairy2024.util.ModelData
import miragefairy2024.util.ModelTexturesData
import miragefairy2024.util.Registration
import miragefairy2024.util.ResourceLocation
import miragefairy2024.util.createItemStack
import miragefairy2024.util.enJa
import miragefairy2024.util.invoke
import miragefairy2024.util.plus
import miragefairy2024.util.register
import miragefairy2024.util.registerColorProvider
import miragefairy2024.util.registerItemGroup
import miragefairy2024.util.registerModelGeneration
import miragefairy2024.util.sortedEntrySet
import miragefairy2024.util.string
import miragefairy2024.util.style
import miragefairy2024.util.text
import mirrg.kotlin.hydrogen.or
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.chat.Component
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.item.context.UseOnContext
import net.minecraft.world.level.Level

val creativeGeneAmpouleItemGroupCard = ItemGroupCard(
    MirageFairy2024.identifier("creative_gene_ampoule"), "Creative Gene Ampoule", "アカーシャによる生命設計の針",
) { CreativeGeneAmpouleCard.item().createItemStack().also { it.setTraitStacks(TraitStacks.of(TraitStack(TraitCard.AIR_ADAPTATION.trait, 1))) } }

object CreativeGeneAmpouleCard {
    val identifier = MirageFairy2024.identifier("creative_gene_ampoule")
    val item = Registration(BuiltInRegistries.ITEM, identifier) { CreativeGeneAmpouleItem(Item.Properties().stacksTo(1)) }
}

context(ModContext)
fun initCreativeGeneAmpoule() {
    creativeGeneAmpouleItemGroupCard.init()
    CreativeGeneAmpouleCard.let { card ->
        card.item.register()
        card.item.registerItemGroup(creativeGeneAmpouleItemGroupCard.itemGroupKey) {
            traitRegistry.sortedEntrySet.map { (_, trait) ->
                card.item().createItemStack().also { it.setTraitStacks(TraitStacks.of(TraitStack(trait, 1))) }
            }
        }
        card.item.registerModelGeneration(createCreativeGeneAmpouleModel())
        card.item.registerColorProvider { itemStack, tintIndex ->
            if (tintIndex == 1) {
                itemStack.getTraitStacks().or { return@registerColorProvider 0xFFFFFFFF.toInt() }.traitStackList.firstOrNull().or { return@registerColorProvider 0xFFFFFFFF.toInt() }.trait.primaryEffect.color or 0xFF000000.toInt()
            } else {
                0xFFFFFFFF.toInt()
            }
        }
        card.item.enJa(EnJa("Creative Gene Ampoule", "アカーシャによる生命設計の針"))
        val poemList = PoemList(null)
            .poem("This allows you to freely edit traits.", "種類に従って球根を持つ草を生えさせよ。")
            .description("description1", "Use: Grant the trait", "使用時、特性を付与")
            .description("description2", "Use while sneaking: Remove the trait", "スニーク中に使用時、特性を削除")
            .description("description3", "Use: Increases bits", "使用時、ビットを増加")
            .description("description4", "Use while sneaking: Decreases bits", "スニーク中に使用時、ビットを減少")
        card.item.registerPoem(poemList)
        card.item.registerPoemGeneration(poemList)
    }
}

class CreativeGeneAmpouleItem(settings: Properties) : Item(settings) {
    override fun appendHoverText(stack: ItemStack, context: TooltipContext, tooltipComponents: MutableList<Component>, tooltipFlag: TooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag)
        stack.getTraitStacks().or { return }.traitStackList.forEach { traitStack ->
            tooltipComponents += text { traitStack.trait.getName().style(traitStack.trait.style) + " "() + traitStack.level.toString(2)() }
        }
    }

    override fun getName(stack: ItemStack): Component {
        val traitStacks = stack.getTraitStacks() ?: return super.getName(stack)
        val traitStack = traitStacks.traitStackList.firstOrNull() ?: return super.getName(stack)
        return text { traitStack.trait.getName() + " "() + traitStack.level.toString(2)() }
    }

    override fun useOn(context: UseOnContext): InteractionResult {
        val blockEntity = context.level.getMagicPlantBlockEntity(context.clickedPos) ?: return InteractionResult.PASS
        if (context.level.isClientSide) return InteractionResult.CONSUME
        val a = blockEntity.getTraitStacks() ?: TraitStacks.EMPTY
        val b = context.itemInHand.getTraitStacks() ?: TraitStacks.EMPTY
        if (context.player?.isShiftKeyDown != true) {
            blockEntity.setTraitStacks(a + b)
        } else {
            blockEntity.setTraitStacks(a - b)
        }
        return InteractionResult.CONSUME
    }

    override fun use(world: Level, user: Player, hand: InteractionHand): InteractionResultHolder<ItemStack> {
        val itemStack = user.getItemInHand(hand)
        if (world.isClientSide) return InteractionResultHolder.success(itemStack)
        val traitStacks = itemStack.getTraitStacks() ?: TraitStacks.EMPTY
        if (!user.isShiftKeyDown) {
            itemStack.setTraitStacks(TraitStacks.of(traitStacks.traitStackMap.mapValues { (it.value shl 1).let { level -> if (level <= 0) 1 else level } }))
        } else {
            itemStack.setTraitStacks(TraitStacks.of(traitStacks.traitStackMap.mapValues { (it.value shr 1).let { level -> if (level <= 0) 1 else level } }))
        }
        return InteractionResultHolder.consume(itemStack)
    }
}

private fun createCreativeGeneAmpouleModel() = Model {
    ModelData(
        parent = ResourceLocation("item/generated"),
        textures = ModelTexturesData(
            "layer0" to MirageFairy2024.identifier("item/creative_gene_ampoule_casing").string,
            "layer1" to MirageFairy2024.identifier("item/creative_gene_ampoule_liquid").string,
            "layer2" to MirageFairy2024.identifier("item/creative_gene_ampoule_highlight").string,
        ),
    )
}
