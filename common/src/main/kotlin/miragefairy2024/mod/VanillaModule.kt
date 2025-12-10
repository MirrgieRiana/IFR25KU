package miragefairy2024.mod

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mod.materials.Material
import miragefairy2024.mod.materials.Shape
import miragefairy2024.mod.materials.tag
import miragefairy2024.mod.materials.tagOf
import miragefairy2024.util.EnJa
import miragefairy2024.util.ResourceLocation
import miragefairy2024.util.enJa
import miragefairy2024.util.generator
import miragefairy2024.util.registerChild
import miragefairy2024.util.registerClientDebugItem
import miragefairy2024.util.toBlockTag
import miragefairy2024.util.toItemTag
import miragefairy2024.util.toTextureSource
import miragefairy2024.util.writeAction
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.Items
import net.minecraft.world.level.block.Blocks
import kotlin.jvm.optionals.getOrElse

enum class ItemTagCard(identifier: ResourceLocation, val title: EnJa) {
    ECHO_SHARDS(ResourceLocation("c", "echo_shards"), EnJa("Echo Shards", "残響の欠片")),
    PRISMARINE_SHARDS(ResourceLocation("c", "prismarine_shards"), EnJa("Prismarine Shards", "プリズマリンの欠片")),
    PLANT_TOOLS(MirageFairy2024.identifier("plant_tools"), EnJa("Plant Tools", "植物ツール")),
    SPIRITS(MirageFairy2024.identifier("spirits"), EnJa("Spirits", "蒸留酒")),
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


    registerClientDebugItem("dump_biome_tags", Items.STRING.toTextureSource(), 0xFF00FF00.toInt()) { world, player, _, _ ->
        val tags = world.registryAccess().registryOrThrow(Registries.BIOME).tagNames.toList()
        val sb = StringBuilder()
        tags.sortedBy { it.location() }.forEach { tag ->
            sb.append("${tag.location()}\n")
            val biomes = world.registryAccess().registryOrThrow(Registries.BIOME).getTag(tag).getOrElse { listOf() }.toList()
            biomes.sortedBy { it.unwrapKey().get().location() }.forEach { biome ->
                sb.append("  ${biome.unwrapKey().get().location()}\n")
            }
        }
        writeAction(player, "dump_biome_tags.txt", sb.toString())
    }

}
