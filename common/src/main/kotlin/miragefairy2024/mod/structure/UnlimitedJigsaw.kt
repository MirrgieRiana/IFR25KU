package miragefairy2024.mod.structure

import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.util.Registration
import miragefairy2024.util.register
import net.minecraft.core.BlockPos
import net.minecraft.core.Holder
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.levelgen.Heightmap
import net.minecraft.world.level.levelgen.WorldGenerationContext
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider
import net.minecraft.world.level.levelgen.structure.Structure
import net.minecraft.world.level.levelgen.structure.StructureType
import net.minecraft.world.level.levelgen.structure.TerrainAdjustment
import net.minecraft.world.level.levelgen.structure.pools.DimensionPadding
import net.minecraft.world.level.levelgen.structure.pools.JigsawPlacement
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool
import net.minecraft.world.level.levelgen.structure.pools.alias.PoolAliasBinding
import net.minecraft.world.level.levelgen.structure.pools.alias.PoolAliasLookup
import net.minecraft.world.level.levelgen.structure.structures.JigsawStructure
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings
import java.util.Optional

context(_: ModContext)
fun initUnlimitedJigsaw() {
    UnlimitedJigsawCard.let { card ->
        Registration(BuiltInRegistries.STRUCTURE_TYPE, card.identifier) { card.structureType }.register()
    }
}

object UnlimitedJigsawCard {
    val identifier = MirageFairy2024.identifier("unlimited_jigsaw")
    val structureType: StructureType<UnlimitedJigsawStructure> = StructureType { UnlimitedJigsawStructure.CODEC }
}

class UnlimitedJigsawStructure(
    config: StructureSettings,
    private val startPool: Holder<StructureTemplatePool>,
    private val startJigsawName: Optional<ResourceLocation> = Optional.empty(),
    private val size: Int,
    private val startHeight: HeightProvider,
    private val useExpansionHack: Boolean,
    private val projectStartToHeightmap: Optional<Heightmap.Types> = Optional.empty(),
    private val maxDistanceFromCenter: Int = 80,
    private val poolAliases: List<PoolAliasBinding> = listOf(),
    private val dimensionPadding: DimensionPadding = JigsawStructure.DEFAULT_DIMENSION_PADDING,
    private val liquidSettings: LiquidSettings = JigsawStructure.DEFAULT_LIQUID_SETTINGS,
) : Structure(config) {
    companion object {
        val CODEC: MapCodec<UnlimitedJigsawStructure> = RecordCodecBuilder.mapCodec<UnlimitedJigsawStructure> { instance ->
            instance.group(
                settingsCodec(instance),
                StructureTemplatePool.CODEC.fieldOf("start_pool").forGetter { it.startPool },
                ResourceLocation.CODEC.optionalFieldOf("start_jigsaw_name").forGetter { it.startJigsawName },
                Codec.intRange(0, 256).fieldOf("size").forGetter { it.size },
                HeightProvider.CODEC.fieldOf("start_height").forGetter { it.startHeight },
                Codec.BOOL.fieldOf("use_expansion_hack").forGetter { it.useExpansionHack },
                Heightmap.Types.CODEC.optionalFieldOf("project_start_to_heightmap").forGetter { it.projectStartToHeightmap },
                Codec.intRange(1, 128).fieldOf("max_distance_from_center").forGetter { it.maxDistanceFromCenter },
                Codec.list(PoolAliasBinding.CODEC).optionalFieldOf("pool_aliases", listOf()).forGetter { it.poolAliases },
                DimensionPadding.CODEC.optionalFieldOf("dimension_padding", JigsawStructure.DEFAULT_DIMENSION_PADDING).forGetter { it.dimensionPadding },
                LiquidSettings.CODEC.optionalFieldOf("liquid_settings", JigsawStructure.DEFAULT_LIQUID_SETTINGS).forGetter { it.liquidSettings },
            ).apply(instance, ::UnlimitedJigsawStructure)
        }.validate(UnlimitedJigsawStructure::validate)

        private fun validate(structure: UnlimitedJigsawStructure): DataResult<UnlimitedJigsawStructure> {
            val var10000 = when (structure.terrainAdaptation()) {
                TerrainAdjustment.NONE -> 0
                TerrainAdjustment.BURY, TerrainAdjustment.BEARD_THIN, TerrainAdjustment.BEARD_BOX -> 12
                else -> throw IncompatibleClassChangeError()
            }
            return if (structure.maxDistanceFromCenter + var10000 > 128) {
                DataResult.error { "Structure size including terrain adaptation must not exceed 128" }
            } else {
                DataResult.success(structure)
            }
        }
    }

    override fun findGenerationPoint(context: GenerationContext): Optional<GenerationStub> {
        val chunkPos = context.chunkPos()
        val i = startHeight.sample(context.random(), WorldGenerationContext(context.chunkGenerator(), context.heightAccessor()))
        val blockPos = BlockPos(chunkPos.minBlockX, i, chunkPos.minBlockZ)
        return JigsawPlacement.addPieces(
            context,
            startPool,
            startJigsawName,
            size,
            blockPos,
            useExpansionHack,
            projectStartToHeightmap,
            maxDistanceFromCenter,
            PoolAliasLookup.create(poolAliases, blockPos, context.seed()),
            dimensionPadding,
            liquidSettings,
        )
    }

    override fun type(): StructureType<*> = UnlimitedJigsawCard.structureType
}
