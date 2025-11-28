package miragefairy2024.mod.magicplant.contents.magicplants

import miragefairy2024.ModContext
import miragefairy2024.mod.magicplant.MagicPlantBlock
import miragefairy2024.mod.magicplant.MagicPlantCard
import miragefairy2024.mod.magicplant.MutableTraitEffects
import miragefairy2024.mod.magicplant.Trait
import miragefairy2024.mod.magicplant.TraitStacks
import miragefairy2024.mod.magicplant.contents.TraitEffectKeyCard
import miragefairy2024.util.Registration
import miragefairy2024.util.TextureMap
import miragefairy2024.util.get
import miragefairy2024.util.getIdentifier
import miragefairy2024.util.normal
import miragefairy2024.util.randomInt
import miragefairy2024.util.register
import miragefairy2024.util.registerModelGeneration
import miragefairy2024.util.registerVariantsBlockStateGeneration
import miragefairy2024.util.times
import miragefairy2024.util.with
import mirrg.kotlin.helium.atLeast
import mirrg.kotlin.helium.atMost
import net.minecraft.core.BlockPos
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.data.models.model.ModelTemplates
import net.minecraft.data.models.model.TextureSlot
import net.minecraft.util.RandomSource
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.enchantment.EnchantmentHelper
import net.minecraft.world.item.enchantment.Enchantments
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.level.block.state.properties.IntegerProperty
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.phys.shapes.VoxelShape

abstract class SimpleMagicPlantCard<B : SimpleMagicPlantBlock> : MagicPlantCard<B>() {

    abstract val outlineShapes: List<VoxelShape>

    open val baseSeedGeneration = 0.2
    open val baseFruitGeneration = 1.0
    open val baseLeafGeneration = 1.0
    open val baseRareGeneration = 0.03
    open val baseSpecialGeneration = 1.0

    open fun getFruitDrops(count: Int, random: RandomSource): List<ItemStack> = listOf()
    open fun getLeafDrops(count: Int, random: RandomSource): List<ItemStack> = listOf()
    open fun getRareDrops(count: Int, random: RandomSource): List<ItemStack> = listOf()
    open fun getSpecialDrops(count: Int, random: RandomSource): List<ItemStack> = listOf()

    val iconItem = Registration(BuiltInRegistries.ITEM, blockIdentifier * "_icon") { Item(Item.Properties()) }

    context(ModContext)
    override fun init() {
        super.init()

        // 登録
        iconItem.register()

        // 見た目
        block.registerVariantsBlockStateGeneration { normal("block/" * block().getIdentifier()) with ageProperty }
        ageProperty.possibleValues.forEach { age ->
            registerModelGeneration({ "block/" * block().getIdentifier() * "_age$age" }) {
                ModelTemplates.CROSS.with(TextureSlot.CROSS to "block/magic_plant/" * block().getIdentifier() * "_age$age")
            }
        }
        iconItem.registerModelGeneration(ModelTemplates.FLAT_ITEM) {
            TextureMap(TextureSlot.LAYER0 to "block/magic_plant/" * block().getIdentifier() * "_age${ageProperty.possibleValues.max()}")
        }

    }
}

abstract class SimpleMagicPlantBlock(private val card: SimpleMagicPlantCard<*>, settings: Properties) : MagicPlantBlock(card, settings) {

    // Property

    abstract fun getAgeProperty(): IntegerProperty

    @Suppress("LeakingThis") // 親クラスのコンストラクタでappendPropertiesが呼ばれるため回避不可能
    private val agePropertyCache = getAgeProperty()

    val maxAge: Int = agePropertyCache.possibleValues.max()

    init {
        registerDefaultState(defaultBlockState().with(agePropertyCache, 0))
    }

    override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block, BlockState>) {
        builder.add(getAgeProperty()/* この関数は親クラスのinitで呼ばれるのでフィールドを参照できない */)
    }

    fun getAge(state: BlockState) = state[agePropertyCache]
    fun isMaxAge(state: BlockState) = getAge(state) >= maxAge
    fun withAge(age: Int): BlockState = defaultBlockState().with(agePropertyCache, age atLeast 0 atMost maxAge)


    // Shape

    private val outlineShapesCache = card.outlineShapes.toTypedArray()

    @Suppress("OVERRIDE_DEPRECATION")
    override fun getShape(state: BlockState, world: BlockGetter, pos: BlockPos, context: CollisionContext) = outlineShapesCache[getAge(state)]


    // Magic Plant

    override fun canCross(world: Level, blockPos: BlockPos, blockState: BlockState) = isMaxAge(blockState)
    override fun canGrow(blockState: BlockState) = !isMaxAge(blockState)
    override fun getBlockStateAfterGrowth(blockState: BlockState, amount: Int) = withAge(getAge(blockState) + amount atMost maxAge)
    override fun canPick(blockState: BlockState) = isMaxAge(blockState)
    override fun getBlockStateAfterPicking(blockState: BlockState) = withAge(0)

    override fun getAdditionalDrops(world: Level, blockPos: BlockPos, block: Block, blockState: BlockState, traitStacks: TraitStacks, traitEffects: MutableTraitEffects, randomTraitChances: Map<Trait, Double>, player: Player?, tool: ItemStack?): List<ItemStack> {
        val drops = mutableListOf<ItemStack>()

        val fortune = if (tool != null) EnchantmentHelper.getItemEnchantmentLevel(world.registryAccess()[Registries.ENCHANTMENT, Enchantments.FORTUNE], tool).toDouble() else 0.0
        val luck = player?.getAttributeValue(Attributes.LUCK) ?: 0.0

        val seedGeneration = traitEffects[TraitEffectKeyCard.SEEDS_PRODUCTION.traitEffectKey]
        val fruitGeneration = traitEffects[TraitEffectKeyCard.FRUITS_PRODUCTION.traitEffectKey]
        val leafGeneration = traitEffects[TraitEffectKeyCard.LEAVES_PRODUCTION.traitEffectKey]
        val rareGeneration = traitEffects[TraitEffectKeyCard.RARE_PRODUCTION.traitEffectKey]
        val specialGeneration = traitEffects[TraitEffectKeyCard.SPECIAL_PRODUCTION.traitEffectKey]
        val generationBoost = traitEffects[TraitEffectKeyCard.PRODUCTION_BOOST.traitEffectKey]
        val fortuneFactor = traitEffects[TraitEffectKeyCard.FORTUNE_FACTOR.traitEffectKey]
        val crossbreeding = traitEffects[TraitEffectKeyCard.CROSSBREEDING.traitEffectKey]
        val mutation = traitEffects[TraitEffectKeyCard.MUTATION.traitEffectKey]
        val seedsDilution = traitEffects[TraitEffectKeyCard.SEEDS_DILUTION.traitEffectKey]

        if (isMaxAge(blockState)) {
            val maxSeeds = world.random.randomInt(3.0 + seedsDilution)
            val rawCount = world.random.randomInt(card.baseSeedGeneration * seedGeneration * (1.0 + generationBoost) * (1.0 + (fortune + luck) * fortuneFactor))
            val count = rawCount atMost maxSeeds
            val condensationFactor = rawCount.toDouble() / count.toDouble()
            repeat(count) {
                drops += calculateCrossedSeed(world, blockPos, traitStacks, randomTraitChances, crossbreeding, mutation, condensationFactor)
            }
        }

        if (isMaxAge(blockState)) {
            val count = world.random.randomInt(card.baseFruitGeneration * fruitGeneration * (1.0 + generationBoost) * (1.0 + (fortune + luck) * fortuneFactor))
            if (count > 0) drops += card.getFruitDrops(count, world.random)
        }

        if (isMaxAge(blockState)) {
            val count = world.random.randomInt(card.baseLeafGeneration * leafGeneration * (1.0 + generationBoost) * (1.0 + (fortune + luck) * fortuneFactor))
            if (count > 0) drops += card.getLeafDrops(count, world.random)
        }

        if (isMaxAge(blockState)) {
            val count = world.random.randomInt(card.baseRareGeneration * rareGeneration * (1.0 + generationBoost) * (1.0 + (fortune + luck) * fortuneFactor))
            if (count > 0) drops += card.getRareDrops(count, world.random)
        }

        if (isMaxAge(blockState)) {
            val count = world.random.randomInt(card.baseSpecialGeneration * specialGeneration * (1.0 + generationBoost) * (1.0 + (fortune + luck) * fortuneFactor))
            if (count > 0) drops += card.getSpecialDrops(count, world.random)
        }

        return drops
    }

}
