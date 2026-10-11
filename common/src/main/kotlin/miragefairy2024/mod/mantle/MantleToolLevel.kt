package miragefairy2024.mod.mantle

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.util.EnJa
import miragefairy2024.util.enJa
import miragefairy2024.util.generator
import miragefairy2024.util.registerChild
import miragefairy2024.util.toBlockTag
import net.minecraft.tags.BlockTags

/** Tier4、すなわちネザライト以上のツールでなければ、ドロップが発生しないブロックのタグなのだ～🌱 */
val NEEDS_TIER4_TOOL_BLOCK_TAG = MirageFairy2024.identifier("needs_tier4_tool").toBlockTag()

/** Tier5、すなわちネザライトの更に 1 段階上のツールでなければ、ドロップが発生しないブロックのタグなのだ～🌱 */
val NEEDS_TIER5_TOOL_BLOCK_TAG = MirageFairy2024.identifier("needs_tier5_tool").toBlockTag()

/** Tier5 のツールでは、ドロップが発生しないブロックのタグなのだ～🌱 Tier6 以上を要求するブロックのための、今は空の枠なのだ～🌱 */
val INCORRECT_FOR_TIER5_TOOL_BLOCK_TAG = MirageFairy2024.identifier("incorrect_for_tier5_tool").toBlockTag()

/** Tier6 のツールでは、ドロップが発生しないブロックのタグなのだ～🌱 Tier7 以上を要求するブロックのための、今は空の枠なのだ～🌱 */
val INCORRECT_FOR_TIER6_TOOL_BLOCK_TAG = MirageFairy2024.identifier("incorrect_for_tier6_tool").toBlockTag()

context(ModContext)
fun initMantleToolLevel() {

    NEEDS_TIER4_TOOL_BLOCK_TAG.enJa(EnJa("Needs Tier 4 Tool", "Tier4のツールが必要"))
    NEEDS_TIER5_TOOL_BLOCK_TAG.enJa(EnJa("Needs Tier 5 Tool", "Tier5のツールが必要"))
    INCORRECT_FOR_TIER5_TOOL_BLOCK_TAG.enJa(EnJa("Incorrect for Tier 5 Tool", "Tier5のツールでは不適切"))
    INCORRECT_FOR_TIER6_TOOL_BLOCK_TAG.enJa(EnJa("Incorrect for Tier 6 Tool", "Tier6のツールでは不適切"))

    // バニラのツールは、いずれも Tier4 の要求を満たさないのだ～🌱
    // Tier5 のタグは Tier4 のタグを内包するから、Tier4 で弾かれるツールは Tier5 でも弾かれるのだ～🌱
    listOf(
        BlockTags.INCORRECT_FOR_WOODEN_TOOL,
        BlockTags.INCORRECT_FOR_GOLD_TOOL,
        BlockTags.INCORRECT_FOR_STONE_TOOL,
        BlockTags.INCORRECT_FOR_IRON_TOOL,
        BlockTags.INCORRECT_FOR_DIAMOND_TOOL,
    ).forEach {
        it.generator.registerChild(NEEDS_TIER4_TOOL_BLOCK_TAG)
    }

    // ネザライトは Tier4 なので、Tier5 の要求のみを満たさないのだ～🌱
    BlockTags.INCORRECT_FOR_NETHERITE_TOOL.generator.registerChild(NEEDS_TIER5_TOOL_BLOCK_TAG)

    NEEDS_TIER5_TOOL_BLOCK_TAG.generator.registerChild(NEEDS_TIER4_TOOL_BLOCK_TAG)

    // Tier6 のツールで弾かれるブロックは、より下位の Tier5 のツールでも弾かれるのだ～🌱
    INCORRECT_FOR_TIER5_TOOL_BLOCK_TAG.generator.registerChild(INCORRECT_FOR_TIER6_TOOL_BLOCK_TAG)

}
