package miragefairy2024.mod

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.clientProxy
import miragefairy2024.lib.SimpleHorizontalFacingBlock
import miragefairy2024.mod.materials.Material
import miragefairy2024.mod.materials.MaterialCard
import miragefairy2024.mod.materials.Shape
import miragefairy2024.mod.materials.tag
import miragefairy2024.mod.materials.tagOf
import miragefairy2024.mod.particle.ParticleTypeCard
import miragefairy2024.util.AdvancementCard
import miragefairy2024.util.AdvancementCardType
import miragefairy2024.util.EnJa
import miragefairy2024.util.INSTANT_CODEC
import miragefairy2024.util.INSTANT_STREAM_CODEC
import miragefairy2024.util.Registration
import miragefairy2024.util.Translation
import miragefairy2024.util.createItemStack
import miragefairy2024.util.enJa
import miragefairy2024.util.generator
import miragefairy2024.util.get
import miragefairy2024.util.getIdentifier
import miragefairy2024.util.getOrDefault
import miragefairy2024.util.gray
import miragefairy2024.util.green
import miragefairy2024.util.invoke
import miragefairy2024.util.mutate
import miragefairy2024.util.normal
import miragefairy2024.util.obtain
import miragefairy2024.util.on
import miragefairy2024.util.optional
import miragefairy2024.util.plus
import miragefairy2024.util.register
import miragefairy2024.util.registerChild
import miragefairy2024.util.registerCutoutRenderLayer
import miragefairy2024.util.registerDefaultLootTableGeneration
import miragefairy2024.util.registerItemGroup
import miragefairy2024.util.registerServerDebugItem
import miragefairy2024.util.registerShapedRecipeGeneration
import miragefairy2024.util.registerVariantsBlockStateGeneration
import miragefairy2024.util.set
import miragefairy2024.util.text
import miragefairy2024.util.times
import miragefairy2024.util.toTextureSource
import miragefairy2024.util.with
import miragefairy2024.util.withHorizontalRotation
import mirrg.kotlin.hydrogen.formatAs
import mirrg.kotlin.java.hydrogen.floorMod
import mirrg.kotlin.java.hydrogen.orNull
import mirrg.kotlin.java.hydrogen.toOptional
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate
import net.fabricmc.fabric.api.attachment.v1.AttachmentType
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.chat.Component
import net.minecraft.network.codec.StreamCodec
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.tags.BlockTags
import net.minecraft.util.RandomSource
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.item.context.BlockPlaceContext
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.HorizontalDirectionalBlock
import net.minecraft.world.level.block.SoundType
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.material.MapColor
import net.minecraft.world.level.pathfinder.PathComputationType
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.phys.shapes.VoxelShape
import java.time.DayOfWeek
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.Optional

object TelescopeCard {
    val identifier = MirageFairy2024.identifier("telescope")
    val block = Registration(BuiltInRegistries.BLOCK, identifier) { TelescopeBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_ORANGE).sound(SoundType.COPPER).strength(0.5F).noOcclusion()) }
    val item = Registration(BuiltInRegistries.ITEM, identifier) { BlockItem(block.await(), Item.Properties()) }
    val advancement = AdvancementCard(
        identifier = identifier,
        context = AdvancementCard.Sub { MaterialCard.FAIRY_CRYSTAL.advancement!!.await() },
        icon = { item().createItemStack() },
        name = EnJa("The Eyes of Tertia", "第三の目"),
        description = EnJa("Craft a telescope for a daily mission", "デイリーミッションのための望遠鏡をクラフトする"),
        criterion = AdvancementCard.hasItem { item() },
        type = AdvancementCardType.GOAL,
    )
}


context(ModContext)
fun initTelescopeModule() {

    TELESCOPE_MISSION_ATTACHMENT_TYPE.register()

    Registration(BuiltInRegistries.BLOCK_TYPE, MirageFairy2024.identifier("telescope")) { TelescopeBlock.CODEC }.register()

    TelescopeCard.let { card ->

        card.block.register()
        card.item.register()

        card.item.registerItemGroup(mirageFairy2024ItemGroupCard.itemGroupKey)

        card.block.registerVariantsBlockStateGeneration { normal("block/" * card.block().getIdentifier()).withHorizontalRotation(HorizontalDirectionalBlock.FACING) }
        card.block.registerCutoutRenderLayer()

        card.block.enJa(EnJa("Minia's Telescope", "ミーニャの望遠鏡"))
        val poemList = PoemList(2)
            .poem("Tell me more about the human world!", "きみは妖精には見えないものが見えるんだね。")
            .description("Use once a day to obtain Fairy Jewels", "1日1回使用時にフェアリージュエルを獲得")
        card.item.registerPoem(poemList)
        card.item.registerPoemGeneration(poemList)

        BlockTags.MINEABLE_WITH_PICKAXE.generator.registerChild(card.block)

        card.block.registerDefaultLootTableGeneration()

        card.advancement.init()

    }

    registerShapedRecipeGeneration(TelescopeCard.item) {
        pattern("IIG")
        pattern(" S ")
        pattern("S S")
        define('S', tagOf(Shape.ROD, Material.WOOD))
        define('I', tagOf(Shape.INGOT, Material.COPPER))
        define('G', tagOf(Shape.GEM, Material.FAIRY_CRYSTAL))
    } on MaterialCard.FAIRY_CRYSTAL.ore!!.tag

    TelescopeBlock.FIRST_TRANSLATION.enJa()
    TelescopeBlock.FIRST_GAIN_TRANSLATION.enJa()
    TelescopeBlock.DAILY_TRANSLATION.enJa()
    TelescopeBlock.WEEKLY_TRANSLATION.enJa()
    TelescopeBlock.MONTHLY_TRANSLATION.enJa()
    TelescopeBlock.AVAILABLE_TRANSLATION.enJa()
    TelescopeBlock.RECEIVED_TRANSLATION.enJa()
    TelescopeBlock.REUSE_TRANSLATION.enJa()
    TelescopeBlock.DAYS_TRANSLATION.enJa()
    TelescopeBlock.HOURS_TRANSLATION.enJa()
    TelescopeBlock.MINUTES_TRANSLATION.enJa()
    TelescopeBlock.SECONDS_TRANSLATION.enJa()

    registerServerDebugItem("reset_telescope_mission", Items.STRING.toTextureSource(), 0xFFDDC442.toInt()) { world, player, _, _ ->
        player.telescopeMission.set(null)
        player.displayClientMessage(text { "The last time the telescope was used has been reset"() }, true)
    }

}

class TelescopeBlock(settings: Properties) : SimpleHorizontalFacingBlock(settings) {
    companion object {
        val CODEC: MapCodec<TelescopeBlock> = simpleCodec(::TelescopeBlock)
        val ZONE_OFFSET: ZoneOffset = ZoneOffset.ofHours(0)
        val DAY_OF_WEEK_ORIGIN = DayOfWeek.SUNDAY
        private val FACING_TO_SHAPE: Map<Direction, VoxelShape> = mapOf(
            Direction.NORTH to box(4.0, 0.0, 1.0, 12.0, 16.0, 15.0),
            Direction.SOUTH to box(4.0, 0.0, 1.0, 12.0, 16.0, 15.0),
            Direction.WEST to box(1.0, 0.0, 4.0, 15.0, 16.0, 12.0),
            Direction.EAST to box(1.0, 0.0, 4.0, 15.0, 16.0, 12.0),
        )
        private val identifier = MirageFairy2024.identifier("telescope")
        val FIRST_TRANSLATION = Translation({ "item.${identifier.toLanguageKey()}.first" }, "First-time reward", "初回報酬")
        val FIRST_GAIN_TRANSLATION = Translation({ "item.${identifier.toLanguageKey()}.first_gain" }, "Obtain %s Jewels", "%s ジュエルを獲得")
        val DAILY_TRANSLATION = Translation({ "item.${identifier.toLanguageKey()}.daily" }, "Daily", "日間")
        val WEEKLY_TRANSLATION = Translation({ "item.${identifier.toLanguageKey()}.weekly" }, "Weekly", "週間")
        val MONTHLY_TRANSLATION = Translation({ "item.${identifier.toLanguageKey()}.monthly" }, "Monthly", "月間")
        val AVAILABLE_TRANSLATION = Translation({ "item.${identifier.toLanguageKey()}.available" }, "Available", "獲得可能")
        val RECEIVED_TRANSLATION = Translation({ "item.${identifier.toLanguageKey()}.received" }, "Received", "獲得済")
        val REUSE_TRANSLATION = Translation({ "item.${identifier.toLanguageKey()}.reuse" }, "%s left until reusable", "再使用可能まで %s")
        val DAYS_TRANSLATION = Translation({ "item.${identifier.toLanguageKey()}.days" }, "%s days", "%s 日")
        val HOURS_TRANSLATION = Translation({ "item.${identifier.toLanguageKey()}.hours" }, "%s hours", "%s 時間")
        val MINUTES_TRANSLATION = Translation({ "item.${identifier.toLanguageKey()}.minutes" }, "%s minutes", "%s 分")
        val SECONDS_TRANSLATION = Translation({ "item.${identifier.toLanguageKey()}.seconds" }, "%s seconds", "%s 秒")
    }

    override fun codec() = CODEC

    override fun appendHoverText(stack: ItemStack, context: Item.TooltipContext, tooltipComponents: MutableList<Component>, tooltipFlag: TooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag)
        val player = clientProxy?.getClientPlayer() ?: return

        val now = Instant.now()
        val result = calculateTelescopeActions(now, player)

        if (result.texts.isNotEmpty()) {
            tooltipComponents += text { ""() }
            tooltipComponents += result.texts
        }
    }

    override fun getStateForPlacement(ctx: BlockPlaceContext): BlockState = defaultBlockState().with(FACING, ctx.horizontalDirection)

    @Suppress("OVERRIDE_DEPRECATION")
    override fun isPathfindable(state: BlockState, pathComputationType: PathComputationType) = false

    @Suppress("OVERRIDE_DEPRECATION")
    override fun getShape(state: BlockState, world: BlockGetter, pos: BlockPos, context: CollisionContext) = FACING_TO_SHAPE[state[FACING]]

    @Suppress("OVERRIDE_DEPRECATION")
    override fun useWithoutItem(state: BlockState, level: Level, pos: BlockPos, player: Player, hitResult: BlockHitResult): InteractionResult {
        if (level.isClientSide) return InteractionResult.SUCCESS
        player as ServerPlayer

        val now = Instant.now()
        val result = calculateTelescopeActions(now, player)
        val actions = result.actions
        if (actions.isEmpty()) return InteractionResult.CONSUME

        actions.forEach {
            it()
        }

        level.playSound(null, player.x, player.y, player.z, SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.5F, 1.0F)

        player.telescopeMission.mutate { it.lastUsedTime = now }

        return InteractionResult.CONSUME
    }

    override fun animateTick(state: BlockState, world: Level, pos: BlockPos, random: RandomSource) {
        val player = clientProxy!!.getClientPlayer() ?: return

        val now = Instant.now()
        val result = calculateTelescopeActions(now, player)
        if (result.actions.isEmpty()) return

        if (random.nextInt(1) == 0) {
            val x = pos.x.toDouble() + 0.0 + random.nextDouble() * 1.0
            val y = pos.y.toDouble() + 0.0 + random.nextDouble() * 0.5
            val z = pos.z.toDouble() + 0.0 + random.nextDouble() * 1.0
            world.addParticle(
                ParticleTypeCard.MISSION.particleType,
                x, y, z,
                random.nextGaussian() * 0.00,
                random.nextGaussian() * 0.00 + 0.4,
                random.nextGaussian() * 0.00,
            )
        }
    }

    private interface TelescopeActions {
        val texts: List<Component>
        val actions: List<() -> Unit>
    }

    private val Duration.text
        get() = text {
            val millis = toMillis()
            when {
                millis >= 1000 * 60 * 60 * 24 -> DAYS_TRANSLATION((millis / (1000 * 60 * 60 * 24).toDouble()) formatAs "%.2f")
                millis >= 1000 * 60 * 60 -> HOURS_TRANSLATION((millis / (1000 * 60 * 60).toDouble()) formatAs "%.2f")
                millis >= 1000 * 60 -> MINUTES_TRANSLATION((millis / (1000 * 60).toDouble()) formatAs "%.2f")
                else -> SECONDS_TRANSLATION((millis / (1000).toDouble()) formatAs "%.2f")
            }
        }

    private fun calculateTelescopeActions(now: Instant, player: Player): TelescopeActions {
        val texts = mutableListOf<Component>()
        val actions = mutableListOf<() -> Unit>()

        val lastUsedInstant = player.telescopeMission.getOrDefault().lastUsedTime
        if (lastUsedInstant == null) {

            texts += text { FIRST_TRANSLATION() + ": "() + FIRST_GAIN_TRANSLATION(1000).green }
            actions += { player.obtain(MaterialCard.JEWEL_100.item().createItemStack(10)) }

        } else {

            fun f(
                startOfPeriodOfLastUsedTime: LocalDateTime,
                nextPeriodStartGetter: (LocalDateTime) -> LocalDateTime,
                countOfJewel100: Int,
                translation: Translation,
            ) {
                val endOfPeriodOfLastUsedTime = nextPeriodStartGetter(startOfPeriodOfLastUsedTime)
                val remainingDuration = endOfPeriodOfLastUsedTime.toInstant(ZONE_OFFSET).toEpochMilli() - now.toEpochMilli()
                if (remainingDuration <= 0) {
                    texts += text { translation() + ": "() + AVAILABLE_TRANSLATION().green }
                    actions += { player.obtain(MaterialCard.JEWEL_100.item().createItemStack(countOfJewel100)) }
                } else {
                    texts += text { translation() + ": "() + RECEIVED_TRANSLATION().gray + " ("() + REUSE_TRANSLATION(Duration.ofMillis(remainingDuration).text) + ")"() }
                }
            }

            val lastUsedTime = LocalDateTime.ofInstant(lastUsedInstant, ZONE_OFFSET)

            // Daily
            f(
                startOfPeriodOfLastUsedTime = lastUsedTime.toLocalDate().atStartOfDay(),
                nextPeriodStartGetter = { it.plusDays(1) },
                countOfJewel100 = 2, // 100 * 2 * 30 = 6000
                translation = DAILY_TRANSLATION,
            )

            // Weekly
            f(
                startOfPeriodOfLastUsedTime = lastUsedTime.toLocalDate().minusDays((lastUsedTime.dayOfWeek.value - DAY_OF_WEEK_ORIGIN.value floorMod 7).toLong()).atStartOfDay(),
                nextPeriodStartGetter = { it.plusDays(7) },
                countOfJewel100 = 5, // 100 * 5 * 4 = 2000
                translation = WEEKLY_TRANSLATION,
            )

            // Monthly
            f(
                startOfPeriodOfLastUsedTime = lastUsedTime.toLocalDate().withDayOfMonth(1).atStartOfDay(),
                nextPeriodStartGetter = { it.plusMonths(1) },
                countOfJewel100 = 20, // 100 * 20 * 1 = 2000
                translation = MONTHLY_TRANSLATION,
            )

        }

        return object : TelescopeActions {
            override val texts = texts
            override val actions = actions
        }
    }

}


val TELESCOPE_MISSION_ATTACHMENT_TYPE: AttachmentType<TelescopeMission> = AttachmentRegistry.create(MirageFairy2024.identifier("telescope_mission")) {
    it.persistent(TelescopeMission.CODEC)
    it.initializer(::TelescopeMission)
    it.syncWith(TelescopeMission.STREAM_CODEC, AttachmentSyncPredicate.targetOnly())
    it.copyOnDeath()
}

val Entity.telescopeMission get() = this[TELESCOPE_MISSION_ATTACHMENT_TYPE]

class TelescopeMission(var lastUsedTime: Instant? = null) {
    companion object {
        val CODEC: Codec<TelescopeMission> = RecordCodecBuilder.create { instance ->
            instance.group(
                INSTANT_CODEC.optionalFieldOf("last_used_time").forGetter { it.lastUsedTime.toOptional() }
            ).apply(instance, ::TelescopeMission)
        }
        val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, TelescopeMission> = StreamCodec.composite(
            INSTANT_STREAM_CODEC.optional(),
            { it.lastUsedTime.toOptional() },
            ::TelescopeMission,
        )
    }

    constructor(lastUsedTime: Optional<Instant>) : this(lastUsedTime.orNull)
}
