package miragefairy2024.mod.fairyquest

import com.mojang.serialization.MapCodec
import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mod.materials.MaterialCard
import miragefairy2024.mod.mirageFairy2024ItemGroupCard
import miragefairy2024.mod.rootAdvancement
import miragefairy2024.util.AdvancementCard
import miragefairy2024.util.AdvancementCardType
import miragefairy2024.util.EnJa
import miragefairy2024.util.Model
import miragefairy2024.util.ModelData
import miragefairy2024.util.ModelTexturesData
import miragefairy2024.util.Registration
import miragefairy2024.util.ResourceLocation
import miragefairy2024.util.Translation
import miragefairy2024.util.createItemStack
import miragefairy2024.util.enJa
import miragefairy2024.util.from
import miragefairy2024.util.invoke
import miragefairy2024.util.isIn
import miragefairy2024.util.on
import miragefairy2024.util.red
import miragefairy2024.util.register
import miragefairy2024.util.registerColorProvider
import miragefairy2024.util.registerItemGroup
import miragefairy2024.util.registerModelGeneration
import miragefairy2024.util.registerShapelessRecipeGeneration
import miragefairy2024.util.sortedEntrySet
import miragefairy2024.util.string
import miragefairy2024.util.text
import mirrg.kotlin.hydrogen.unit
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredient
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory
import net.minecraft.core.component.DataComponentType
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.chat.Component
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.inventory.ContainerLevelAccess
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.level.Level

object FairyQuestCardCard {
    val enName = "Broken Fairy Quest Card"
    val jaName = "破損したフェアリークエストカード"
    val identifier = MirageFairy2024.identifier("fairy_quest_card")
    val item = Registration(BuiltInRegistries.ITEM, identifier) { FairyQuestCardItem(Item.Properties()) }
    val advancement = AdvancementCard(
        identifier = identifier,
        context = AdvancementCard.Sub { rootAdvancement.await() },
        icon = { item().createItemStack().also { it.setFairyQuestRecipe(FairyQuestRecipeCard.VEGETATION_SURVEY) } },
        name = EnJa("Time Crystal Radio", "時間結晶ラジオ"),
        description = EnJa("Look for the Fairy Quest Card lying around nearby", "その辺に落ちているフェアリークエストカードを探そう"),
        criterion = AdvancementCard.hasItem { item() },
        type = AdvancementCardType.NORMAL,
    )
}

private val fairyQuestCardFairyQuestTranslation = Translation({ FairyQuestCardCard.item().descriptionId + ".format" }, "“%s”", "『%s』")

context(ModContext)
fun initFairyQuestCardItem() {
    FairyQuestCardCard.let { card ->
        card.item.register()
        card.item.registerItemGroup(mirageFairy2024ItemGroupCard.itemGroupKey) {
            fairyQuestRecipeRegistry.sortedEntrySet.map {
                val itemStack = card.item().createItemStack()
                itemStack.setFairyQuestRecipe(it.value)
                itemStack
            }
        }
        card.item.registerModelGeneration(createFairyQuestCardModel())
        card.item.registerColorProvider { itemStack, tintIndex ->
            if (tintIndex == 0) {
                itemStack.getFairyQuestRecipe()?.color?.let { it or 0xFF000000.toInt() } ?: 0xFFFF00FF.toInt()
            } else {
                0xFFFFFFFF.toInt()
            }
        }
        card.item.enJa(EnJa(card.enName, card.jaName))
        card.advancement.init()
    }

    fairyQuestCardFairyQuestTranslation.enJa()

    registerShapelessRecipeGeneration(MaterialCard.FAIRY_QUEST_CARD_BASE.item) {
        requires(FairyQuestCardIngredient.toVanilla())
    } on FairyQuestCardCard.item from FairyQuestCardCard.item

    FairyQuestCardIngredient.SERIALIZER.register()

    Registration(BuiltInRegistries.DATA_COMPONENT_TYPE, MirageFairy2024.identifier("fairy_quest_recipe")) { FAIRY_QUEST_RECIPE_DATA_COMPONENT_TYPE }.register()
}

class FairyQuestCardItem(settings: Properties) : Item(settings) {
    override fun getName(stack: ItemStack): Component {
        val recipe = stack.getFairyQuestRecipe() ?: return super.getName(stack).red
        return text { fairyQuestCardFairyQuestTranslation(recipe.title) }
    }

    override fun appendHoverText(stack: ItemStack, context: TooltipContext, tooltipComponents: MutableList<Component>, tooltipFlag: TooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag)
        val recipe = stack.getFairyQuestRecipe()
        if (recipe == null) {
            tooltipComponents += text { "null"() }
        } else {
            if (fairyQuestRecipeRegistry.contains(recipe)) {
                // nop
            } else {
                tooltipComponents += text { fairyQuestRecipeRegistry.getKey(recipe)!!.string() }
            }
        }
    }

    override fun use(world: Level, user: Player, hand: InteractionHand): InteractionResultHolder<ItemStack> {
        val itemStack = user.getItemInHand(hand)
        val recipe = itemStack.getFairyQuestRecipe() ?: return InteractionResultHolder.fail(itemStack)
        if (world.isClientSide) return InteractionResultHolder.success(itemStack)
        user.openMenu(object : ExtendedScreenHandlerFactory<ResourceLocation> {
            override fun createMenu(syncId: Int, playerInventory: Inventory, player: Player): AbstractContainerMenu {
                return FairyQuestCardScreenHandler(syncId, playerInventory, recipe, ContainerLevelAccess.create(world, player.blockPosition()))
            }

            override fun getDisplayName() = recipe.title

            override fun getScreenOpeningData(player: ServerPlayer) = fairyQuestRecipeRegistry.getKey(recipe)!!
        })
        return InteractionResultHolder.consume(itemStack)
    }
}

val FAIRY_QUEST_RECIPE_DATA_COMPONENT_TYPE: DataComponentType<FairyQuestRecipe> = DataComponentType.builder<FairyQuestRecipe>()
    .persistent(fairyQuestRecipeRegistry.byNameCodec())
    .networkSynchronized(ByteBufCodecs.registry(fairyQuestRecipeRegistryKey))
    .cacheEncoding()
    .build()

fun ItemStack.getFairyQuestRecipe() = this.get(FAIRY_QUEST_RECIPE_DATA_COMPONENT_TYPE)
fun ItemStack.setFairyQuestRecipe(recipe: FairyQuestRecipe) = unit { this.set(FAIRY_QUEST_RECIPE_DATA_COMPONENT_TYPE, recipe) }

private fun createFairyQuestCardModel() = Model {
    ModelData(
        parent = ResourceLocation("item/generated"),
        textures = ModelTexturesData(
            "layer0" to MirageFairy2024.identifier("item/fairy_quest_card_background").string,
            "layer1" to MirageFairy2024.identifier("item/fairy_quest_card_frame").string,
        ),
    )
}

object FairyQuestCardIngredient : CustomIngredient {
    val ID = MirageFairy2024.identifier("fairy_quest_card")
    val SERIALIZER = object : CustomIngredientSerializer<FairyQuestCardIngredient> {
        override fun getIdentifier() = ID
        override fun getCodec(allowEmpty: Boolean): MapCodec<FairyQuestCardIngredient> = MapCodec.unit(FairyQuestCardIngredient)
        override fun getPacketCodec(): StreamCodec<RegistryFriendlyByteBuf, FairyQuestCardIngredient> = StreamCodec.unit(FairyQuestCardIngredient)
    }

    override fun requiresTesting() = true
    override fun test(stack: ItemStack) = stack isIn FairyQuestCardCard.item()

    override fun getMatchingStacks(): List<ItemStack> {
        return fairyQuestRecipeRegistry.sortedEntrySet.map {
            val itemStack = FairyQuestCardCard.item().createItemStack()
            itemStack.setFairyQuestRecipe(it.value)
            itemStack
        }
    }

    override fun getSerializer() = SERIALIZER
}
