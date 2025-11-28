package miragefairy2024.mod.magicplant.contents.magicplants

import net.minecraft.core.Holder
import net.minecraft.data.worldgen.placement.PlacementUtils
import net.minecraft.world.level.levelgen.feature.Feature
import net.minecraft.world.level.levelgen.feature.configurations.SimpleBlockConfiguration
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider
import net.minecraft.world.level.levelgen.placement.PlacedFeature

val SimpleMagicPlantCard<*>.maxAgedBlockState get() = this.block().withAge(this.block().maxAge)
val SimpleMagicPlantCard<*>.placer: Holder<PlacedFeature> get() = PlacementUtils.onlyWhenEmpty(Feature.SIMPLE_BLOCK, SimpleBlockConfiguration(BlockStateProvider.simple(this.maxAgedBlockState)))
