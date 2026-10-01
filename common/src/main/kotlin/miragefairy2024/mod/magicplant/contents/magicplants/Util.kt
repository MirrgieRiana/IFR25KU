package miragefairy2024.mod.magicplant.contents.magicplants

import net.minecraft.core.Direction
import net.minecraft.core.Holder
import net.minecraft.data.worldgen.placement.PlacementUtils
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate
import net.minecraft.world.level.levelgen.feature.Feature
import net.minecraft.world.level.levelgen.feature.configurations.SimpleBlockConfiguration
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider
import net.minecraft.world.level.levelgen.placement.PlacedFeature

val SimpleMagicPlantCard<*>.maxAgedBlockState get() = this.block().withAge(this.block().maxAge)
val SimpleMagicPlantCard<*>.placer: Holder<PlacedFeature> get() = PlacementUtils.onlyWhenEmpty(Feature.SIMPLE_BLOCK, SimpleBlockConfiguration(BlockStateProvider.simple(this.maxAgedBlockState)))
val SimpleMagicPlantCard<*>.placerOnGrass: Holder<PlacedFeature> get() = PlacementUtils.filtered(Feature.SIMPLE_BLOCK, SimpleBlockConfiguration(BlockStateProvider.simple(this.maxAgedBlockState)), BlockPredicate.allOf(BlockPredicate.ONLY_IN_AIR_PREDICATE, BlockPredicate.matchesBlocks(Direction.DOWN.normal, Blocks.GRASS_BLOCK)))
