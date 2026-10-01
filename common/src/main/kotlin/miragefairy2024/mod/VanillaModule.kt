package miragefairy2024.mod

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.ModEvents
import miragefairy2024.mod.common.CustomizedRemainderRegistry
import miragefairy2024.mod.materials.Material
import miragefairy2024.mod.materials.Shape
import miragefairy2024.mod.materials.tag
import miragefairy2024.mod.materials.tagOf
import miragefairy2024.util.EnJa
import miragefairy2024.util.ResourceLocation
import miragefairy2024.util.createItemStack
import miragefairy2024.util.enJa
import miragefairy2024.util.generator
import miragefairy2024.util.get
import miragefairy2024.util.registerChild
import miragefairy2024.util.registerClientDebugItem
import miragefairy2024.util.string
import miragefairy2024.util.toBlockTag
import miragefairy2024.util.toItemTag
import miragefairy2024.util.toTextureSource
import miragefairy2024.util.writeAction
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.Items
import net.minecraft.world.level.block.Blocks

enum class ItemTagCard(identifier: ResourceLocation, val title: EnJa) {
    ECHO_SHARDS(ResourceLocation("c", "echo_shards"), EnJa("Echo Shards", "残響の欠片")),
    PRISMARINE_SHARDS(ResourceLocation("c", "prismarine_shards"), EnJa("Prismarine Shards", "プリズマリンの欠片")),
    PLANT_TOOLS(MirageFairy2024.identifier("plant_tools"), EnJa("Plant Tools", "植物ツール")),
    SPIRITS(MirageFairy2024.identifier("spirits"), EnJa("Spirits", "蒸留酒")),
    SAP(MirageFairy2024.identifier("sap"), EnJa("Sap", "樹液")),
    ;

    val tag = identifier.toItemTag()
}

enum class BlockTagCard(identifier: ResourceLocation, val title: EnJa) {
    CONCRETE(MirageFairy2024.identifier("concrete"), EnJa("Concrete", "コンクリート")),
    ;

    val tag = identifier.toBlockTag()
}

context(ModContext)
fun initVanillaModule() {
    ItemTagCard.entries.forEach { card ->
        card.tag.enJa(card.title)
    }
    BlockTagCard.entries.forEach { card ->
        card.tag.enJa(card.title)
    }

    tagOf(Shape.GEM, Material.FLINT).generator.registerChild { Items.FLINT }
    ItemTagCard.ECHO_SHARDS.tag.generator.registerChild { Items.ECHO_SHARD }
    tagOf(Shape.SHARD, Material.AMETHYST).generator.registerChild { Items.AMETHYST_SHARD }
    ItemTagCard.PRISMARINE_SHARDS.tag.generator.registerChild { Items.PRISMARINE_SHARD }

    Shape.GEM.tag.generator.registerChild(tagOf(Shape.GEM, Material.FLINT))
    Shape.SHARD.tag.generator.registerChild(tagOf(Shape.SHARD, Material.AMETHYST))

    BlockTagCard.CONCRETE.tag.generator.registerChild { Blocks.WHITE_CONCRETE }
    BlockTagCard.CONCRETE.tag.generator.registerChild { Blocks.ORANGE_CONCRETE }
    BlockTagCard.CONCRETE.tag.generator.registerChild { Blocks.MAGENTA_CONCRETE }
    BlockTagCard.CONCRETE.tag.generator.registerChild { Blocks.LIGHT_BLUE_CONCRETE }
    BlockTagCard.CONCRETE.tag.generator.registerChild { Blocks.YELLOW_CONCRETE }
    BlockTagCard.CONCRETE.tag.generator.registerChild { Blocks.LIME_CONCRETE }
    BlockTagCard.CONCRETE.tag.generator.registerChild { Blocks.PINK_CONCRETE }
    BlockTagCard.CONCRETE.tag.generator.registerChild { Blocks.GRAY_CONCRETE }
    BlockTagCard.CONCRETE.tag.generator.registerChild { Blocks.LIGHT_GRAY_CONCRETE }
    BlockTagCard.CONCRETE.tag.generator.registerChild { Blocks.CYAN_CONCRETE }
    BlockTagCard.CONCRETE.tag.generator.registerChild { Blocks.PURPLE_CONCRETE }
    BlockTagCard.CONCRETE.tag.generator.registerChild { Blocks.BLUE_CONCRETE }
    BlockTagCard.CONCRETE.tag.generator.registerChild { Blocks.BROWN_CONCRETE }
    BlockTagCard.CONCRETE.tag.generator.registerChild { Blocks.GREEN_CONCRETE }
    BlockTagCard.CONCRETE.tag.generator.registerChild { Blocks.RED_CONCRETE }
    BlockTagCard.CONCRETE.tag.generator.registerChild { Blocks.BLACK_CONCRETE }


    ModEvents.onInitialize {
        FoodIngredientsRegistry.registry[Items.GOLDEN_APPLE] = FoodIngredients() + Items.APPLE
        FoodIngredientsRegistry.registry[Items.ENCHANTED_GOLDEN_APPLE] = FoodIngredients() + Items.GOLDEN_APPLE
        FoodIngredientsRegistry.registry[Items.GOLDEN_CARROT] = FoodIngredients() + Items.CARROT
        FoodIngredientsRegistry.registry[Items.BAKED_POTATO] = FoodIngredients() + Items.POTATO
        FoodIngredientsRegistry.registry[Items.POISONOUS_POTATO] = FoodIngredients() + Items.POTATO
        FoodIngredientsRegistry.registry[Items.DRIED_KELP] = FoodIngredients() + Items.KELP
        FoodIngredientsRegistry.registry[Items.COOKED_BEEF] = FoodIngredients() + Items.BEEF
        FoodIngredientsRegistry.registry[Items.COOKED_PORKCHOP] = FoodIngredients() + Items.PORKCHOP
        FoodIngredientsRegistry.registry[Items.COOKED_MUTTON] = FoodIngredients() + Items.MUTTON
        FoodIngredientsRegistry.registry[Items.COOKED_CHICKEN] = FoodIngredients() + Items.CHICKEN
        FoodIngredientsRegistry.registry[Items.COOKED_RABBIT] = FoodIngredients() + Items.RABBIT
        FoodIngredientsRegistry.registry[Items.COD] = FoodIngredients() + FoodIngredientCategoryCard.FISH
        FoodIngredientsRegistry.registry[Items.COOKED_COD] = FoodIngredients() + Items.COD
        FoodIngredientsRegistry.registry[Items.SALMON] = FoodIngredients() + FoodIngredientCategoryCard.FISH
        FoodIngredientsRegistry.registry[Items.COOKED_SALMON] = FoodIngredients() + Items.SALMON
        FoodIngredientsRegistry.registry[Items.BREAD] = FoodIngredients() + Items.WHEAT
        FoodIngredientsRegistry.registry[Items.COOKIE] = FoodIngredients() + Items.WHEAT + Items.COCOA_BEANS
        FoodIngredientsRegistry.registry[Items.PUMPKIN_PIE] = FoodIngredients() + Items.PUMPKIN + Items.SUGAR + Items.EGG
        FoodIngredientsRegistry.registry[Items.MUSHROOM_STEW] = FoodIngredients() + Items.RED_MUSHROOM + Items.BROWN_MUSHROOM
        FoodIngredientsRegistry.registry[Items.BEETROOT_SOUP] = FoodIngredients() + Items.BEETROOT
        FoodIngredientsRegistry.registry[Items.RABBIT_STEW] = FoodIngredients() + Items.RABBIT + Items.POTATO + Items.CARROT + FoodIngredientCategoryCard.MUSHROOM
        FoodIngredientsRegistry.registry[Items.SUSPICIOUS_STEW] = FoodIngredients() + Items.RED_MUSHROOM + Items.BROWN_MUSHROOM
        FoodIngredientsRegistry.registry[Items.RED_MUSHROOM] = FoodIngredients() + FoodIngredientCategoryCard.MUSHROOM
        FoodIngredientsRegistry.registry[Items.BROWN_MUSHROOM] = FoodIngredients() + FoodIngredientCategoryCard.MUSHROOM
    }


    CustomizedRemainderRegistry.register(Items.POTION) { Items.GLASS_BOTTLE.createItemStack() }


    registerClientDebugItem("dump_biome_tags", Items.STRING.toTextureSource(), 0xFF00FF00.toInt()) { level, player, _, _ ->
        val tags = level.registryAccess().registryOrThrow(Registries.BIOME).tagNames.toList()
        val sb = StringBuilder()
        tags.sortedBy { it.location() }.forEach { tag ->
            sb.append("${tag.location()}\n")
            val biomes = level.registryAccess().registryOrThrow(Registries.BIOME)[tag].toList()
            biomes.sortedBy { it.unwrapKey().get().location() }.forEach { biome ->
                sb.append("  ${biome.unwrapKey().get().location().string}\n")
            }
        }
        writeAction(player, "dump_biome_tags.txt", sb.toString())
    }

}
