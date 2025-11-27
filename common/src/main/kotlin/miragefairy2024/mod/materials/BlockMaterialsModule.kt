package miragefairy2024.mod.materials

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mod.PoemList
import miragefairy2024.mod.fairy.SOUL_STREAM_CONTAINABLE_TAG
import miragefairy2024.mod.machine.AuraReflectorFurnaceRecipeCard
import miragefairy2024.mod.machine.registerSimpleMachineRecipeGeneration
import miragefairy2024.mod.materials.contents.FairyCrystalGlassBlock
import miragefairy2024.mod.materials.contents.LOCAL_VACUUM_DECAY_RESISTANT_BLOCK_TAG
import miragefairy2024.mod.materials.contents.LocalVacuumDecayBlock
import miragefairy2024.mod.materials.contents.MiragidianLampBlock
import miragefairy2024.mod.materials.contents.SemiOpaqueTransparentBlock
import miragefairy2024.mod.materials.contents.fairyCrystalGlassBlockModel
import miragefairy2024.mod.materials.contents.fairyCrystalGlassFrameBlockModel
import miragefairy2024.mod.materials.contents.localVacuumDecayTexturedModelFactory
import miragefairy2024.mod.mirageFairy2024ItemGroupCard
import miragefairy2024.mod.poem
import miragefairy2024.mod.registerPoem
import miragefairy2024.mod.registerPoemGeneration
import miragefairy2024.mod.rootAdvancement
import miragefairy2024.mod.tool.MINEABLE_WITH_NOISE_BLOCK_TAG
import miragefairy2024.util.AdvancementCard
import miragefairy2024.util.AdvancementCardType
import miragefairy2024.util.BlockStateVariant
import miragefairy2024.util.EnJa
import miragefairy2024.util.Registration
import miragefairy2024.util.createItemStack
import miragefairy2024.util.enJa
import miragefairy2024.util.from
import miragefairy2024.util.generator
import miragefairy2024.util.getIdentifier
import miragefairy2024.util.on
import miragefairy2024.util.propertiesOf
import miragefairy2024.util.register
import miragefairy2024.util.registerBlockFamily
import miragefairy2024.util.registerBlockStateGeneration
import miragefairy2024.util.registerChild
import miragefairy2024.util.registerCompressionRecipeGeneration
import miragefairy2024.util.registerCutoutRenderLayer
import miragefairy2024.util.registerDefaultLootTableGeneration
import miragefairy2024.util.registerGeneratedModelGeneration
import miragefairy2024.util.registerItemGroup
import miragefairy2024.util.registerLootTableGeneration
import miragefairy2024.util.registerModelGeneration
import miragefairy2024.util.registerShapedRecipeGeneration
import miragefairy2024.util.registerSingletonBlockStateGeneration
import miragefairy2024.util.registerSmeltingRecipeGeneration
import miragefairy2024.util.registerStonecutterRecipeGeneration
import miragefairy2024.util.registerTranslucentRenderLayer
import miragefairy2024.util.registerVariantsBlockStateGeneration
import miragefairy2024.util.times
import miragefairy2024.util.toIngredient
import miragefairy2024.util.toIngredientStack
import miragefairy2024.util.with
import mirrg.kotlin.gson.hydrogen.jsonArray
import mirrg.kotlin.gson.hydrogen.jsonElement
import mirrg.kotlin.gson.hydrogen.jsonObject
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.data.models.model.ModelTemplates
import net.minecraft.data.models.model.TextureSlot
import net.minecraft.data.models.model.TexturedModel
import net.minecraft.data.recipes.RecipeCategory
import net.minecraft.tags.BlockTags
import net.minecraft.tags.ItemTags
import net.minecraft.tags.TagKey
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.Item
import net.minecraft.world.item.Items
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.SlabBlock
import net.minecraft.world.level.block.SoundType
import net.minecraft.world.level.block.StairBlock
import net.minecraft.world.level.block.WallBlock
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument
import net.minecraft.world.level.material.MapColor

open class BlockMaterialCard(
    path: String,
    private val name: EnJa,
    private val poemList: PoemList,
    private val mapColor: MapColor,
    private val hardness: Float,
    private val resistance: Float,
    val ore: Ore? = null,
    val advancementCreator: (BlockMaterialCard.() -> AdvancementCard)? = null,
    val texturedModelProvider: TexturedModel.Provider? = null,
) {
    companion object {
        val entries = mutableListOf<BlockMaterialCard>()
        private operator fun BlockMaterialCard.not() = this.apply { entries.add(this) }

        val NEPHRITE_BLOCK = !BlockMaterialCard(
            "nephrite_block", EnJa("Nephrite Block", "ネフライトブロック"),
            PoemList(null),
            MapColor.WARPED_WART_BLOCK, 5.0F, 5.0F, ore = Ore(Shape.STORAGE_BLOCKS, Material.NEPHRITE),
        ).needTool(ToolType.PICKAXE, ToolLevel.STONE).beaconBase().init {
            registerCompressionRecipeGeneration(MaterialCard.NEPHRITE.item, { MaterialCard.NEPHRITE.ore!!.ingredient }, item, { ore!!.ingredient })
        }
        val XARPITE_BLOCK = !BlockMaterialCard(
            "xarpite_block", EnJa("Xarpite Block", "紅天石ブロック"),
            PoemList(2).poem(EnJa("Loss and reconstruction of perception", "夢の世界の如き紅。")),
            MapColor.NETHER, 3.0F, 3.0F, ore = Ore(Shape.STORAGE_BLOCKS, Material.XARPITE),
        ).needTool(ToolType.PICKAXE, ToolLevel.STONE).beaconBase().init {
            registerCompressionRecipeGeneration(MaterialCard.XARPITE.item, { MaterialCard.XARPITE.ore!!.ingredient }, item, { ore!!.ingredient })
        }
        val AURA_RESISTANT_CERAMIC: BlockMaterialCard = !object : BlockMaterialCard(
            "aura_resistant_ceramic", EnJa("Protective Aura-Resistant Ceramic", "守護の耐霊石"),
            PoemList(2).poem(EnJa("The eternally glorious city of Xarperia.", "恒久の栄華を讃える紅天市街――")),
            MapColor.COLOR_ORANGE, 30.0F, 30.0F,
            texturedModelProvider = {
                ModelTemplates.CUBE_BOTTOM_TOP.with(
                    TextureSlot.TOP to "block/" * it.getIdentifier(),
                    TextureSlot.BOTTOM to "block/" * it.getIdentifier(),
                    TextureSlot.SIDE to "block/" * it.getIdentifier() * "_side",
                )
            },
        ) {
            context(ModContext) override fun initModelGeneration() = block.registerModelGeneration { texturedModelProvider!![block()] }
        }.needTool(ToolType.PICKAXE, ToolLevel.STONE).init {
            // TODO 分解することで液体燃料が取れる
            registerShapedRecipeGeneration(item, count = 2) {
                pattern("##")
                define('#', SMOOTH_AURA_RESISTANT_CERAMIC.item)
            } on SMOOTH_AURA_RESISTANT_CERAMIC.item
            registerStonecutterRecipeGeneration(item, SMOOTH_AURA_RESISTANT_CERAMIC.item)
        }
        val AURA_RESISTANT_CERAMIC_SLAB: BlockMaterialCard = !object : BlockMaterialCard(
            "aura_resistant_ceramic_slab", EnJa("Protective Aura-Resistant Ceramic Slab", "守護の耐霊石のハーフブロック"),
            PoemList(2).poem(EnJa("The unified meta-/retro-physics theory.", "光素力学と万物の理論の統合――")),
            MapColor.COLOR_ORANGE, 30.0F, 30.0F,
        ) {
            override suspend fun createBlock(properties: BlockBehaviour.Properties) = SlabBlock(properties)
            context(ModContext) override fun initBlockStateGeneration() = Unit
            context(ModContext) override fun initModelGeneration() = Unit
            context(ModContext) override fun initLootTableGeneration() = block.registerLootTableGeneration { it, _ -> it.createSlabItemTable(block()) }
        }.needTool(ToolType.PICKAXE, ToolLevel.STONE).tag(BlockTags.SLABS).tag(ItemTags.SLABS).init {
            registerBlockFamily(AURA_RESISTANT_CERAMIC.texturedModelProvider!!, AURA_RESISTANT_CERAMIC.block) { it.slab(block()) }
            registerStonecutterRecipeGeneration(item, SMOOTH_AURA_RESISTANT_CERAMIC.item)
            registerStonecutterRecipeGeneration(item, AURA_RESISTANT_CERAMIC.item, 2)
        }
        val AURA_RESISTANT_CERAMIC_STAIRS: BlockMaterialCard = !object : BlockMaterialCard(
            "aura_resistant_ceramic_stairs", EnJa("Protective Aura-Resistant Ceramic Stairs", "守護の耐霊石の階段"),
            PoemList(2).poem(EnJa("The final creative research of humanity.", "アカーシャに続く路。")),
            MapColor.COLOR_ORANGE, 30.0F, 30.0F,
        ) {
            override suspend fun createBlock(properties: BlockBehaviour.Properties) = StairBlock(AURA_RESISTANT_CERAMIC.block.await().defaultBlockState(), properties)
            context(ModContext) override fun initBlockStateGeneration() = Unit
            context(ModContext) override fun initModelGeneration() = Unit
        }.needTool(ToolType.PICKAXE, ToolLevel.STONE).tag(BlockTags.STAIRS).tag(ItemTags.STAIRS).init {
            registerBlockFamily(AURA_RESISTANT_CERAMIC.texturedModelProvider!!, AURA_RESISTANT_CERAMIC.block) { it.stairs(block()) }
            registerStonecutterRecipeGeneration(item, SMOOTH_AURA_RESISTANT_CERAMIC.item)
            registerStonecutterRecipeGeneration(item, AURA_RESISTANT_CERAMIC.item)
        }
        val COBBLED_AURA_RESISTANT_CERAMIC = !BlockMaterialCard(
            "cobbled_aura_resistant_ceramic", EnJa("Cobbled Protective Aura-Resistant Ceramic", "守護の耐霊石の丸石"),
            PoemList(2).poem(EnJa("Penetrates the monomer and solidifies.", "砂岩に宿るポリテルペンの祝福――")),
            MapColor.COLOR_ORANGE, 30.0F, 30.0F,
        ).needTool(ToolType.PICKAXE, ToolLevel.STONE).init {
            // TODO アタノールで作る
            // TODO Tierをアタノールに合わせる
            registerShapedRecipeGeneration(item, count = 2) {
                pattern("SX")
                pattern("XS")
                define('S', Items.SANDSTONE)
                define('X', MaterialCard.XARPITE.ore!!.ingredient)
            } on MaterialCard.XARPITE.ore!!.tag
        }
        val SMOOTH_AURA_RESISTANT_CERAMIC: BlockMaterialCard = !object : BlockMaterialCard(
            "smooth_aura_resistant_ceramic", EnJa("Smooth Protective Aura-Resistant Ceramic", "滑らかな守護の耐霊石"),
            PoemList(2).poem(EnJa("Turpentine improves its flowability.", "空隙に走る樹脂の流れ――")),
            MapColor.COLOR_ORANGE, 30.0F, 30.0F,
        ) {
            context(ModContext)
            override fun initModelGeneration() = block.registerModelGeneration {
                ModelTemplates.CUBE_ALL.with(
                    TextureSlot.ALL to "block/" * AURA_RESISTANT_CERAMIC.identifier,
                )
            }
        }.needTool(ToolType.PICKAXE, ToolLevel.STONE).init {
            registerSmeltingRecipeGeneration(COBBLED_AURA_RESISTANT_CERAMIC.item, item) on item
        }
        val POLISHED_AURA_RESISTANT_CERAMIC = !BlockMaterialCard(
            "polished_aura_resistant_ceramic", EnJa("Polished Protective Aura-Resistant Ceramic", "磨かれた守護の耐霊石"),
            PoemList(2).poem(EnJa("Economical urban astral shielding.", "情緒線の被曝に備えて――")),
            MapColor.COLOR_ORANGE, 30.0F, 30.0F,
        ).needTool(ToolType.PICKAXE, ToolLevel.STONE).init {
            registerShapedRecipeGeneration(item, count = 4) {
                pattern("##")
                pattern("##")
                define('#', SMOOTH_AURA_RESISTANT_CERAMIC.item)
            } on SMOOTH_AURA_RESISTANT_CERAMIC.item
            registerStonecutterRecipeGeneration(item, SMOOTH_AURA_RESISTANT_CERAMIC.item)
        }
        val AURA_RESISTANT_CERAMIC_BRICKS = !BlockMaterialCard(
            "aura_resistant_ceramic_bricks", EnJa("Protective Aura-Resistant Ceramic Bricks", "守護の耐霊石レンガ"),
            PoemList(2).poem(EnJa("Protects lifeforms from radiation.", "常に純潔な魂であれ――")),
            MapColor.COLOR_ORANGE, 30.0F, 30.0F,
        ).needTool(ToolType.PICKAXE, ToolLevel.STONE).init {
            registerShapedRecipeGeneration(item, count = 4) {
                pattern("##")
                pattern("##")
                define('#', POLISHED_AURA_RESISTANT_CERAMIC.item)
            } on POLISHED_AURA_RESISTANT_CERAMIC.item
            registerStonecutterRecipeGeneration(item, SMOOTH_AURA_RESISTANT_CERAMIC.item)
            registerStonecutterRecipeGeneration(item, POLISHED_AURA_RESISTANT_CERAMIC.item)
        }
        val AURA_RESISTANT_CERAMIC_BRICKS_SLAB: BlockMaterialCard = !object : BlockMaterialCard(
            "aura_resistant_ceramic_bricks_slab", EnJa("Protective Aura-Resistant Ceramic Brick Slab", "守護の耐霊石レンガのハーフブロック"),
            PoemList(2).poem(EnJa("Transmutation caused by solar flares.", "魔物と混淆しないために――")),
            MapColor.COLOR_ORANGE, 30.0F, 30.0F,
        ) {
            override suspend fun createBlock(properties: BlockBehaviour.Properties) = SlabBlock(properties)
            context(ModContext) override fun initBlockStateGeneration() = Unit
            context(ModContext) override fun initModelGeneration() = Unit
            context(ModContext) override fun initLootTableGeneration() = block.registerLootTableGeneration { it, _ -> it.createSlabItemTable(block()) }
        }.needTool(ToolType.PICKAXE, ToolLevel.STONE).tag(BlockTags.SLABS).tag(ItemTags.SLABS).init {
            registerBlockFamily(TexturedModel.CUBE, AURA_RESISTANT_CERAMIC_BRICKS.block) { it.slab(block()) }
            registerStonecutterRecipeGeneration(item, SMOOTH_AURA_RESISTANT_CERAMIC.item)
            registerStonecutterRecipeGeneration(item, POLISHED_AURA_RESISTANT_CERAMIC.item)
            registerStonecutterRecipeGeneration(item, AURA_RESISTANT_CERAMIC_BRICKS.item, 2)
        }
        val AURA_RESISTANT_CERAMIC_BRICKS_STAIRS: BlockMaterialCard = !object : BlockMaterialCard(
            "aura_resistant_ceramic_bricks_stairs", EnJa("Protective Aura-Resistant Ceramic Brick Stairs", "守護の耐霊石レンガの階段"),
            PoemList(2).poem(EnJa("The scarlet roofs that protect citizens.", "暮らしを守る紅い盾――")),
            MapColor.COLOR_ORANGE, 30.0F, 30.0F,
        ) {
            override suspend fun createBlock(properties: BlockBehaviour.Properties) = StairBlock(AURA_RESISTANT_CERAMIC.block.await().defaultBlockState(), properties)
            context(ModContext) override fun initBlockStateGeneration() = Unit
            context(ModContext) override fun initModelGeneration() = Unit
        }.needTool(ToolType.PICKAXE, ToolLevel.STONE).tag(BlockTags.STAIRS).tag(ItemTags.STAIRS).init {
            registerBlockFamily(TexturedModel.CUBE, AURA_RESISTANT_CERAMIC_BRICKS.block) { it.stairs(block()) }
            registerStonecutterRecipeGeneration(item, SMOOTH_AURA_RESISTANT_CERAMIC.item)
            registerStonecutterRecipeGeneration(item, POLISHED_AURA_RESISTANT_CERAMIC.item)
            registerStonecutterRecipeGeneration(item, AURA_RESISTANT_CERAMIC_BRICKS.item)
        }
        val AURA_RESISTANT_CERAMIC_TILES = !BlockMaterialCard(
            "aura_resistant_ceramic_tiles", EnJa("Protective Aura-Resistant Ceramic Tiles", "守護の耐霊石タイル"),
            PoemList(2).poem(EnJa("Weather-resistance due to magnetite.", "生存の願いを敷石に込めて――")),
            MapColor.COLOR_ORANGE, 30.0F, 30.0F,
        ).needTool(ToolType.PICKAXE, ToolLevel.STONE).init {
            registerShapedRecipeGeneration(item, count = 4) {
                pattern(" ##")
                pattern("## ")
                define('#', POLISHED_AURA_RESISTANT_CERAMIC.item)
            } on POLISHED_AURA_RESISTANT_CERAMIC.item
            registerStonecutterRecipeGeneration(item, SMOOTH_AURA_RESISTANT_CERAMIC.item)
            registerStonecutterRecipeGeneration(item, POLISHED_AURA_RESISTANT_CERAMIC.item)
        }
        val MIRANAGITE_BLOCK = !BlockMaterialCard(
            "miranagite_block", EnJa("Miranagite Block", "蒼天石ブロック"),
            PoemList(2).poem(EnJa("Passivation confines discontinuous space", "虚空に導かれし、神域との接合点。")),
            MapColor.LAPIS, 3.0F, 3.0F, ore = Ore(Shape.STORAGE_BLOCKS, Material.MIRANAGITE),
        ).needTool(ToolType.PICKAXE, ToolLevel.STONE).beaconBase().init {
            registerCompressionRecipeGeneration(MaterialCard.MIRANAGITE.item, { MaterialCard.MIRANAGITE.ore!!.ingredient }, item, { ore!!.ingredient })
        }
        val MIRANAGITE_TILES = !BlockMaterialCard(
            "miranagite_tiles", EnJa("Miranagite Tiles", "蒼天石タイル"),
            PoemList(2).poem(EnJa("Is time really an absolute entity?", "運命を退ける、蒼神の慈愛。")),
            MapColor.LAPIS, 3.0F, 3.0F,
        ).needTool(ToolType.PICKAXE, ToolLevel.STONE).init {
            registerShapedRecipeGeneration(item, count = 2) {
                pattern("B#")
                pattern("#B")
                define('#', tagOf(Shape.GEM, Material.MIRANAGITE))
                define('B', Blocks.STONE_BRICKS)
            } on MaterialCard.MIRANAGITE.item from MaterialCard.MIRANAGITE.item
        }
        val MIRANAGITE_TILE_SLAB = !object : BlockMaterialCard(
            "miranagite_tile_slab", EnJa("Miranagite Tile Slab", "蒼天石タイルのハーフブロック"),
            PoemList(2).poem(EnJa("A Turing-complete crystal lattice", "開闢よりすべてが預言された世界。")),
            MapColor.LAPIS, 3.0F, 3.0F,
        ) {
            override suspend fun createBlock(properties: BlockBehaviour.Properties) = SlabBlock(properties)
            context(ModContext) override fun initBlockStateGeneration() = Unit
            context(ModContext) override fun initModelGeneration() = Unit
            context(ModContext) override fun initLootTableGeneration() = block.registerLootTableGeneration { it, _ -> it.createSlabItemTable(block()) }
        }.needTool(ToolType.PICKAXE, ToolLevel.STONE).tag(BlockTags.SLABS).tag(ItemTags.SLABS).init {
            registerBlockFamily(TexturedModel.CUBE, MIRANAGITE_TILES.block) { it.slab(block()) }
            registerStonecutterRecipeGeneration(item, MIRANAGITE_TILES.item, 2)
        }
        val MIRANAGITE_TILE_STAIRS = !object : BlockMaterialCard(
            "miranagite_tile_stairs", EnJa("Miranagite Tile Stairs", "蒼天石タイルの階段"),
            PoemList(2).poem(EnJa("Negative-time-evolution region", "因果の遡上、楽園への道。")),
            MapColor.LAPIS, 3.0F, 3.0F,
        ) {
            override suspend fun createBlock(properties: BlockBehaviour.Properties) = StairBlock(MIRANAGITE_TILES.block.await().defaultBlockState(), properties)
            context(ModContext) override fun initBlockStateGeneration() = Unit
            context(ModContext) override fun initModelGeneration() = Unit
        }.needTool(ToolType.PICKAXE, ToolLevel.STONE).tag(BlockTags.STAIRS).tag(ItemTags.STAIRS).init {
            registerBlockFamily(TexturedModel.CUBE, MIRANAGITE_TILES.block) { it.stairs(block()) }
            registerStonecutterRecipeGeneration(item, MIRANAGITE_TILES.item)
        }
        val MIRANAGITE_TILE_WALL = !object : BlockMaterialCard(
            "miranagite_tile_wall", EnJa("Miranagite Tile Wall", "蒼天石タイルの塀"),
            PoemList(2).poem(EnJa("An unreachable domain", "二度と踏み入ることの許されぬ地。")),
            MapColor.LAPIS, 3.0F, 3.0F,
        ) {
            override fun createBlockProperties(): BlockBehaviour.Properties = super.createBlockProperties().forceSolidOn()
            override suspend fun createBlock(properties: BlockBehaviour.Properties) = WallBlock(properties)
            context(ModContext) override fun initBlockStateGeneration() = Unit
            context(ModContext) override fun initModelGeneration() = Unit
        }.needTool(ToolType.PICKAXE, ToolLevel.STONE).tag(BlockTags.WALLS).tag(ItemTags.WALLS).init {
            registerBlockFamily(TexturedModel.CUBE, MIRANAGITE_TILES.block) { it.wall(block()) }
            registerStonecutterRecipeGeneration(item, MIRANAGITE_TILES.item, category = RecipeCategory.DECORATIONS)
        }
        val CHAOS_STONE_BLOCK = !BlockMaterialCard(
            "chaos_stone_block", EnJa("Chaos Stone Block", "混沌の石ブロック"),
            PoemList(4).poem(EnJa("The eye of entropy.", "無秩序の目。")),
            MapColor.TERRACOTTA_ORANGE, 5.0F, 5.0F, ore = Ore(Shape.STORAGE_BLOCKS, Material.CHAOS_STONE),
        ).needTool(ToolType.PICKAXE, ToolLevel.STONE).beaconBase().init {
            registerCompressionRecipeGeneration(MaterialCard.CHAOS_STONE.item, { MaterialCard.CHAOS_STONE.ore!!.ingredient }, item, { ore!!.ingredient })
        }
        val NOISE_BLOCK = !BlockMaterialCard(
            "noise_block", EnJa("Noise Block", "ノイズブロック"),
            PoemList(5).poem(EnJa("No one can block that noise.", "誰もその雑音を止めることはできない。")),
            MapColor.COLOR_GRAY, 8.0F, 8.0F, ore = Ore(Shape.STORAGE_BLOCKS, Material.NOISE),
        ).needTool(ToolType.NOISE).soulStream().init {
            registerCompressionRecipeGeneration(MaterialCard.NOISE.item, { MaterialCard.NOISE.ore!!.ingredient }, item, { ore!!.ingredient })
        }
        val MIRAGIDIAN_BLOCK = !BlockMaterialCard(
            "miragidian_block", EnJa("Miragidian Block", "ミラジディアンブロック"),
            PoemList(4).poem(EnJa("The wall feels like it's protecting us", "その身に宿る、黒曜石の魂。")),
            MapColor.TERRACOTTA_BLUE, 120.0F, 1200.0F, ore = Ore(Shape.STORAGE_BLOCKS, Material.MIRAGIDIAN),
        ).needTool(ToolType.PICKAXE, ToolLevel.DIAMOND).noBurn().soulStream().beaconBase().init {
            registerCompressionRecipeGeneration(MaterialCard.MIRAGIDIAN.item, { MaterialCard.MIRAGIDIAN.ore!!.ingredient }, item, { ore!!.ingredient })
        }
        val MIRAGIDIAN_STEEL_TILES = !BlockMaterialCard(
            "miragidian_steel_tiles", EnJa("Miragidian Steel Block", "ミラジディアンスチールブロック"),
            PoemList(4).poem(EnJa("Oxide film on the surface prevents rust.", "鉄は貴重だった。大戦で多くを失ったのだ。")),
            MapColor.TERRACOTTA_BLUE, 120.0F, 1200.0F,
        ).sound(SoundType.METAL).needTool(ToolType.PICKAXE, ToolLevel.DIAMOND).noBurn().soulStream().init {
            registerShapedRecipeGeneration(item, count = 2) {
                pattern("MI")
                pattern("IM")
                define('M', tagOf(Shape.GEM, Material.MIRAGIDIAN))
                define('I', tagOf(Shape.INGOT, Material.IRON))
            } on MaterialCard.MIRAGIDIAN.item
        }
        val MIRAGIDIAN_LAMP = !object : BlockMaterialCard(
            "miragidian_lamp", EnJa("Miragidian Street Lamp", "ミラジディアンの街灯"),
            PoemList(4).poem(EnJa("I remember… my sister turned into a …", "覚えてるよ…まだ人間だった妹が殺された日")), // Obtains light erg from astral radiation.
            MapColor.TERRACOTTA_BLUE, 60.0F, 1200.0F,
            advancementCreator = {
                AdvancementCard(
                    identifier = identifier,
                    context = AdvancementCard.Sub { rootAdvancement.await() },
                    icon = { item().createItemStack() },
                    name = EnJa("Eye That Walked 30 Millennia", "3万年を歩んだ瞳"), // Light that Watched Collapse / 崩壊を見届けた光
                    description = EnJa("Find Miragidian Street Lamp in Retrospective City biome", "過去を見つめる都市バイオームでミラジディアンの街灯を見つける"),
                    criterion = AdvancementCard.hasItem(item),
                    type = AdvancementCardType.TOAST_AND_JEWELS,
                )
            },
        ) {
            override fun createBlockProperties(): BlockBehaviour.Properties = super.createBlockProperties()
                .lightLevel { if (it.getValue(MiragidianLampBlock.PART) == MiragidianLampBlock.Part.HEAD) 15 else 0 }

            override suspend fun createBlock(properties: BlockBehaviour.Properties) = MiragidianLampBlock(properties)

            context(ModContext)
            override fun initBlockStateGeneration() {
                block.registerVariantsBlockStateGeneration {
                    val normal = BlockStateVariant(model = "block/" * block().getIdentifier())
                    listOf(
                        propertiesOf(MiragidianLampBlock.PART with MiragidianLampBlock.Part.HEAD) with normal.with(model = "block/" * block().getIdentifier() * "_head"),
                        propertiesOf(MiragidianLampBlock.PART with MiragidianLampBlock.Part.POLE) with normal.with(model = "block/" * block().getIdentifier() * "_pole"),
                        propertiesOf(MiragidianLampBlock.PART with MiragidianLampBlock.Part.FOOT) with normal.with(model = "block/" * block().getIdentifier() * "_foot"),
                    )
                }
            }

            context(ModContext) override fun initModelGeneration() = Unit

            context(ModContext)
            override fun initLootTableGeneration() {
                block.registerLootTableGeneration { provider, _ ->
                    provider.createSinglePropConditionTable(block(), MiragidianLampBlock.PART, MiragidianLampBlock.Part.FOOT)
                }
            }
        }.sound(SoundType.METAL).needTool(ToolType.PICKAXE, ToolLevel.DIAMOND).noBurn().soulStream().init {
            item.registerGeneratedModelGeneration()
            registerShapedRecipeGeneration(item) {
                pattern("L")
                pattern("#")
                pattern("#")
                define('L', Items.LANTERN) // TODO 妖精研究所産のランプにする
                define('#', MIRAGIDIAN_STEEL_TILES.item().toIngredient())
            } on MIRAGIDIAN_STEEL_TILES.item
        }
        val LUMINITE_BLOCK = !object : BlockMaterialCard(
            "luminite_block", EnJa("Luminite Block", "ルミナイトブロック"),
            PoemList(4).poem(EnJa("Catalytic digestion of astral vortices", "光り輝く魂のエネルギー。")),
            MapColor.DIAMOND, 6.0F, 6.0F, ore = Ore(Shape.STORAGE_BLOCKS, Material.LUMINITE),
        ) {
            override fun createBlockProperties(): BlockBehaviour.Properties = super.createBlockProperties().noOcclusion().lightLevel { 15 }.isRedstoneConductor { _, _, _ -> false }
            override suspend fun createBlock(properties: BlockBehaviour.Properties) = SemiOpaqueTransparentBlock(properties)
        }.translucent().sound(SoundType.GLASS).needTool(ToolType.PICKAXE, ToolLevel.IRON).beaconBase().init {
            registerCompressionRecipeGeneration(MaterialCard.LUMINITE.item, { MaterialCard.LUMINITE.ore!!.ingredient }, item, { ore!!.ingredient })
        }
        val DRYWALL = !BlockMaterialCard(
            "drywall", EnJa("Drywall", "石膏ボード"),
            PoemList(1).poem(EnJa("Please use on the office ceiling, etc.", "オフィスの天井等にどうぞ。")),
            MapColor.SAND, 3.0F, 3.0F,
        ).tag(BlockTags.MINEABLE_WITH_PICKAXE, BlockTags.MINEABLE_WITH_AXE)
        val LOCAL_VACUUM_DECAY = !object : BlockMaterialCard(
            "local_vacuum_decay", EnJa("Local Vacuum Decay", "局所真空崩壊"),
            PoemList(99).poem(EnJa("Stable instability due to anti-entropy", "これが秩序の究極の形だというのか？")),
            MapColor.COLOR_BLACK, -1.0F, 3600000.0F,
        ) {
            override suspend fun createBlock(properties: BlockBehaviour.Properties) = LocalVacuumDecayBlock(properties)
            context(ModContext) override fun initModelGeneration() = block.registerModelGeneration(localVacuumDecayTexturedModelFactory)
        }.cutout().sound(SoundType.SLIME_BLOCK).invincible().speed(0.5F)
        val AURA_STONE = !BlockMaterialCard(
            "aura_stone", EnJa("Aura Stone", "霊氣石"),
            PoemList(3).poem(EnJa("It absorbs auras and seals them away", "呼吸する石。")),
            MapColor.DIAMOND, 5.0F, 6.0F,
        ).sound(SoundType.METAL).needTool(ToolType.PICKAXE, ToolLevel.IRON).init {
            registerSimpleMachineRecipeGeneration(
                AuraReflectorFurnaceRecipeCard,
                inputs = listOf(
                    { MaterialCard.FAIRY_CRYSTAL.item().toIngredientStack(1) },
                    { MaterialCard.XARPITE.item().toIngredientStack(4) },
                    { MaterialCard.MIRANAGITE.item().toIngredientStack(4) },
                ),
                output = { item().createItemStack() },
                duration = 20 * 60,
            ) on MaterialCard.FAIRY_CRYSTAL.item
        }
        val FAIRY_CRYSTAL_GLASS = !object : BlockMaterialCard(
            "fairy_crystal_glass", EnJa("Fairy Crystal Glass", "フェアリークリスタルガラス"),
            PoemList(2).poem(EnJa("It is displaying the scene behind it.", "家の外を映し出す鏡。")),
            MapColor.DIAMOND, 1.5F, 1.5F, ore = Ore(Shape.STORAGE_BLOCKS, Material.FAIRY_CRYSTAL),
        ) {
            override fun createBlockProperties(): BlockBehaviour.Properties = super.createBlockProperties().instrument(NoteBlockInstrument.HAT).noOcclusion().isRedstoneConductor(Blocks::never).isSuffocating(Blocks::never).isViewBlocking(Blocks::never)
            override suspend fun createBlock(properties: BlockBehaviour.Properties) = FairyCrystalGlassBlock(properties)

            context(ModContext)
            override fun initBlockStateGeneration() {
                block.registerBlockStateGeneration {
                    fun createPart(direction: String, x: Int, y: Int) = jsonObject(
                        "when" to jsonObject(
                            direction to "false".jsonElement,
                        ),
                        "apply" to jsonObject(
                            "model" to "${"block/" * identifier * "_frame"}".jsonElement,
                            "x" to x.jsonElement,
                            "y" to y.jsonElement,
                        ),
                    )
                    jsonObject(
                        "multipart" to jsonArray(
                            createPart("north", 90, 0),
                            createPart("east", 90, 90),
                            createPart("south", -90, 0),
                            createPart("west", 90, -90),
                            createPart("up", 0, 0),
                            createPart("down", 180, 0),
                        ),
                    )
                }
            }

            context(ModContext) override fun initModelGeneration() = Unit
        }.cutout().sound(SoundType.GLASS).needTool(ToolType.PICKAXE, ToolLevel.STONE).soulStream().beaconBase().noSpawn().tag(BlockTags.IMPERMEABLE).init {
            // インベントリ内のモデル
            registerModelGeneration({ "block/" * identifier }) {
                fairyCrystalGlassBlockModel.with(TextureSlot.TEXTURE to "block/" * identifier * "_frame")
            }
            // 枠パーツモデル
            registerModelGeneration({ "block/" * identifier * "_frame" }) {
                fairyCrystalGlassFrameBlockModel.with(TextureSlot.TEXTURE to "block/" * identifier * "_frame")
            }

            registerCompressionRecipeGeneration(MaterialCard.FAIRY_CRYSTAL.item, { MaterialCard.FAIRY_CRYSTAL.ore!!.ingredient }, item, { ore!!.ingredient })
        }
    }

    val identifier = MirageFairy2024.identifier(path)

    open fun createBlockProperties(): BlockBehaviour.Properties = BlockBehaviour.Properties.of()
        .mapColor(mapColor)
        .strength(hardness, resistance)

    val blockPropertiesConverters = mutableListOf<(BlockBehaviour.Properties) -> BlockBehaviour.Properties>()

    open suspend fun createBlock(properties: BlockBehaviour.Properties) = Block(properties)

    val block = Registration(BuiltInRegistries.BLOCK, identifier) {
        val properties = blockPropertiesConverters.fold(createBlockProperties()) { properties, converter -> converter(properties) }
        createBlock(properties)
    }

    val itemPropertiesConverters = mutableListOf<(Item.Properties) -> Item.Properties>()

    val item = Registration(BuiltInRegistries.ITEM, identifier) {
        val properties = itemPropertiesConverters.fold(Item.Properties()) { properties, converter -> converter(properties) }
        BlockItem(block.await(), properties)
    }

    val advancement = advancementCreator?.invoke(this)

    val initializers = mutableListOf<(ModContext) -> Unit>()

    context(ModContext)
    open fun init() {

        block.register()
        item.register()

        item.registerItemGroup(mirageFairy2024ItemGroupCard.itemGroupKey)

        initBlockStateGeneration()
        initModelGeneration()

        block.enJa(name)
        item.registerPoem(poemList)
        item.registerPoemGeneration(poemList)

        initLootTableGeneration()

        if (ore != null) {
            ore.tag.generator.registerChild(item)
            ore.shape.tag.generator.registerChild(ore.tag)
        }

        advancement?.init()

        initializers.forEach {
            it(this@ModContext)
        }

    }

    context(ModContext)
    open fun initBlockStateGeneration() {
        block.registerSingletonBlockStateGeneration()
    }

    context(ModContext)
    open fun initModelGeneration() {
        block.registerModelGeneration(TexturedModel.CUBE)
    }

    context(ModContext)
    open fun initLootTableGeneration() {
        block.registerDefaultLootTableGeneration()
    }
}

context(ModContext)
fun initBlockMaterialsModule() {
    Registration(BuiltInRegistries.BLOCK_TYPE, MirageFairy2024.identifier("miragidian_lamp")) { MiragidianLampBlock.CODEC }.register()
    Registration(BuiltInRegistries.BLOCK_TYPE, MirageFairy2024.identifier("local_vacuum_decay")) { LocalVacuumDecayBlock.CODEC }.register()
    Registration(BuiltInRegistries.BLOCK_TYPE, MirageFairy2024.identifier("semi_opaque_transparent_block")) { SemiOpaqueTransparentBlock.CODEC }.register()
    Registration(BuiltInRegistries.BLOCK_TYPE, MirageFairy2024.identifier("fairy_crystal_glass")) { FairyCrystalGlassBlock.CODEC }.register()

    LOCAL_VACUUM_DECAY_RESISTANT_BLOCK_TAG.enJa(EnJa("Local Vacuum Decay Resistant", "局所真空崩壊耐性"))

    BlockMaterialCard.entries.forEach { card ->
        card.init()
    }
}


private fun <T : BlockMaterialCard> T.blockProperty(converter: (BlockBehaviour.Properties) -> BlockBehaviour.Properties) = this.also { it.blockPropertiesConverters += converter }
private fun <T : BlockMaterialCard> T.itemProperty(converter: (Item.Properties) -> Item.Properties) = this.also { it.itemPropertiesConverters += converter }

private fun <T : BlockMaterialCard> T.noDrop() = this.blockProperty { it.noLootTable() }
private fun <T : BlockMaterialCard> T.noSpawn() = this.blockProperty { it.isValidSpawn(Blocks::never) }
private fun <T : BlockMaterialCard> T.speed(speedFactor: Float) = this.blockProperty { it.speedFactor(speedFactor) }
private fun <T : BlockMaterialCard> T.sound(blockSoundGroup: SoundType) = this.blockProperty { it.sound(blockSoundGroup) }

private fun <T : BlockMaterialCard> T.noBurn() = this.itemProperty { it.fireResistant() }

private fun <T : BlockMaterialCard> T.init(initializer: context(ModContext) T.() -> Unit) = this.also {
    this.initializers += { modContext ->
        initializer(modContext, this)
    }
}

private fun <T : BlockMaterialCard> T.cutout() = this.init {
    block.registerCutoutRenderLayer()
}

private fun <T : BlockMaterialCard> T.translucent() = this.init {
    block.registerTranslucentRenderLayer()
}

@JvmName("blockTag")
private fun <T : BlockMaterialCard> T.tag(vararg tags: TagKey<Block>) = this.init {
    tags.forEach {
        it.generator.registerChild(block)
    }
}

@JvmName("itemTag")
private fun <T : BlockMaterialCard> T.tag(vararg tags: TagKey<Item>) = this.init {
    tags.forEach {
        it.generator.registerChild(item)
    }
}

enum class ToolType(val tag: TagKey<Block>) {
    AXE(BlockTags.MINEABLE_WITH_AXE),
    HOE(BlockTags.MINEABLE_WITH_HOE),
    PICKAXE(BlockTags.MINEABLE_WITH_PICKAXE),
    SHOVEL(BlockTags.MINEABLE_WITH_SHOVEL),
    NOISE(MINEABLE_WITH_NOISE_BLOCK_TAG),
}

enum class ToolLevel(val tag: TagKey<Block>) {
    DIAMOND(BlockTags.NEEDS_DIAMOND_TOOL),
    IRON(BlockTags.NEEDS_IRON_TOOL),
    STONE(BlockTags.NEEDS_STONE_TOOL),
}

private fun <T : BlockMaterialCard> T.needTool(type: ToolType) = this.blockProperty { it.requiresCorrectToolForDrops() }.tag(type.tag)
private fun <T : BlockMaterialCard> T.needTool(type: ToolType, level: ToolLevel) = this.blockProperty { it.requiresCorrectToolForDrops() }.tag(type.tag, level.tag)

private fun <T : BlockMaterialCard> T.beaconBase() = this.tag(BlockTags.BEACON_BASE_BLOCKS)

private fun <T : BlockMaterialCard> T.soulStream() = this.tag(SOUL_STREAM_CONTAINABLE_TAG)

private fun <T : BlockMaterialCard> T.invincible() = this
    .noDrop()
    .noSpawn()
    .tag(
        BlockTags.DRAGON_IMMUNE,
        BlockTags.WITHER_IMMUNE,
        BlockTags.FEATURES_CANNOT_REPLACE,
        BlockTags.GEODE_INVALID_BLOCKS,
        BlockTags.BLOCKS_WIND_CHARGE_EXPLOSIONS,
    )
