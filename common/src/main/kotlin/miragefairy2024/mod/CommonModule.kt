package miragefairy2024.mod

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import com.mojang.serialization.MapCodec
import dev.architectury.event.events.client.ClientCommandRegistrationEvent
import dev.architectury.event.events.common.CommandRegistrationEvent
import miragefairy2024.InitializationEventRegistry
import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.ModEvents
import miragefairy2024.ModifyItemEnchantmentsHandler
import miragefairy2024.mod.fairy.MotifCard
import miragefairy2024.mod.fairy.createFairyItemStack
import miragefairy2024.mod.materials.MaterialCard
import miragefairy2024.platformProxy
import miragefairy2024.util.AdvancementCard
import miragefairy2024.util.AdvancementCardType
import miragefairy2024.util.EnJa
import miragefairy2024.util.ItemGroupCard
import miragefairy2024.util.SubscribableBuffer
import miragefairy2024.util.Translation
import miragefairy2024.util.createItemStack
import miragefairy2024.util.enJa
import miragefairy2024.util.get
import miragefairy2024.util.humidityCategory
import miragefairy2024.util.invoke
import miragefairy2024.util.isIn
import miragefairy2024.util.register
import miragefairy2024.util.registerClientDebugItem
import miragefairy2024.util.registerServerDebugItem
import miragefairy2024.util.string
import miragefairy2024.util.temperatureCategory
import miragefairy2024.util.text
import miragefairy2024.util.toTextureSource
import miragefairy2024.util.translate
import miragefairy2024.util.writeAction
import mirrg.kotlin.gson.hydrogen.jsonArray
import mirrg.kotlin.gson.hydrogen.jsonElement
import mirrg.kotlin.gson.hydrogen.jsonElementOrJsonNull
import mirrg.kotlin.gson.hydrogen.jsonObject
import mirrg.kotlin.gson.hydrogen.toJson
import mirrg.kotlin.helium.join
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredient
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.core.BlockPos
import net.minecraft.core.Holder
import net.minecraft.core.component.DataComponents
import net.minecraft.core.registries.Registries
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.alchemy.PotionContents
import net.minecraft.world.item.alchemy.Potions
import net.minecraft.world.level.Level
import net.minecraft.world.level.biome.Biome
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.levelgen.GenerationStep
import net.minecraft.world.phys.HitResult
import kotlin.jvm.optionals.getOrNull

val mirageFairy2024ItemGroupCard = ItemGroupCard(
    MirageFairy2024.identifier("miragefairy2024"), "IFR25KU", "IFR25KU",
) { MaterialCard.FAIRY_CRYSTAL.item().createItemStack() }

val rootAdvancement = AdvancementCard(
    identifier = MirageFairy2024.identifier("root"),
    context = AdvancementCard.Root(MirageFairy2024.identifier("textures/block/haimeviska_planks.png")),
    icon = { MotifCard.MAGENTA_GLAZED_TERRACOTTA.createFairyItemStack() },
    name = EnJa("IFR25KU", "IFR25KU"),
    description = EnJa("The Noisy Land of Tertia", "かしましきテルティアの地"),
    criterion = AdvancementCard.hasAnyItem(),
    type = AdvancementCardType.SILENT,
)

object CommandEvents {
    val onRegisterSubCommand = InitializationEventRegistry<(LiteralArgumentBuilder<CommandSourceStack>) -> LiteralArgumentBuilder<CommandSourceStack>>()
    val onRegisterClientSubCommand = InitializationEventRegistry<(LiteralArgumentBuilder<ClientCommandRegistrationEvent.ClientCommandSourceStack>) -> LiteralArgumentBuilder<ClientCommandRegistrationEvent.ClientCommandSourceStack>>()
}

val guiFullScreenTranslation = Translation({ "gui.${MirageFairy2024.identifier("common").toLanguageKey()}.full_screen" }, "Click to full screen", "クリックで全画面表示")
val guiBackToGameTranslation = Translation({ "gui.${MirageFairy2024.identifier("common").toLanguageKey()}.back_to_game" }, "Back to game", "ゲームに戻る")
val guiDeadPlayer = Translation({ "gui.${MirageFairy2024.identifier("common").toLanguageKey()}.dead_player" }, "Player %s is dead", "プレイヤー%sは死亡しています")

val deadPlayerCommandExceptionType = DynamicCommandExceptionType { text { guiDeadPlayer(it) } }

object CommonRenderingEvents {
    val onRenderBlockPosesOutline = SubscribableBuffer<RenderBlockPosesOutlineListener>()
}

fun interface RenderBlockPosesOutlineListener {
    fun getBlockPoses(context: RenderBlockPosesOutlineContext): Pair<BlockPos, Set<BlockPos>>?
}

fun interface RenderBlockPosesOutlineListenerItem {
    fun getBlockPoses(hand: InteractionHand, context: RenderBlockPosesOutlineContext): Pair<BlockPos, Set<BlockPos>>?
}

interface RenderBlockPosesOutlineContext {
    val level: Level?
    val player: Player?
    val hitResult: HitResult?
}

context(ModContext)
fun initCommonModule() {
    mirageFairy2024ItemGroupCard.init()

    WaterBottleIngredient.SERIALIZER.register()

    rootAdvancement.init()

    platformProxy!!.registerModifyItemEnchantmentsHandler { itemStack, mutableItemEnchantments, enchantmentLookup ->
        val item = itemStack.item as? ModifyItemEnchantmentsHandler ?: return@registerModifyItemEnchantmentsHandler
        item.modifyItemEnchantments(itemStack, mutableItemEnchantments, enchantmentLookup)
    }

    registerClientDebugItem("dump_biome_attributes", Blocks.OAK_SAPLING.toTextureSource(), 0xFFFF00FF.toInt()) { world, player, _, _ ->
        val lines = mutableListOf<List<String>>()
        world.registryAccess()[Registries.BIOME].listElements().forEach { biome ->
            lines += listOf(
                text { translate(biome.key().location().toLanguageKey("biome")) }.string,
                "${biome.humidityCategory}",
                "${biome.temperatureCategory}",
            )
        }
        val table = listOf(
            listOf("Biome", "Humidity", "Temperature"),
            *lines.toTypedArray(),
        )
        writeAction(player, "dump_biome_attributes.csv", table.map { line -> line.join(",") + "\n" }.join(""))
    }
    registerServerDebugItem("dump_biome_features", Blocks.SPRUCE_SAPLING.toTextureSource(), 0xFFFF00FF.toInt()) { world, player, _, _ ->
        fun Level.getBiomes() = this.registryAccess()[Registries.BIOME].listElements().toList()
        fun Holder<*>.getId() = this.unwrapKey().getOrNull()?.location()?.string
        fun Holder<Biome>.getPlacedFeatureSets() = this.value().generationSettings.features()

        // PlacedFeatureの単純リスト
        world.getBiomes().map { biome ->
            jsonObject(
                "biome" to biome.getId().jsonElementOrJsonNull,
                "placedFeatures" to biome.getPlacedFeatureSets().map { placedFeatureSet ->
                    placedFeatureSet.toList().sortedBy { it.getId() }.map { placedFeature ->
                        placedFeature.getId().jsonElementOrJsonNull
                    }.jsonArray
                }.jsonArray,
            )
        }.jsonArray.let { writeAction(player, "dump_biome_features.json", it.toJson { setPrettyPrinting() }) }

        // 順序表
        GenerationStep.Decoration.entries.map { step ->

            val allPlacedFeatures = world.getBiomes().flatMap { biome ->
                biome.getPlacedFeatureSets().getOrNull(step.ordinal)?.toList()?.map { it.getId() } ?: emptyList()
            }.distinct().sortedBy { it }

            val table = mutableMapOf<String?, MutableSet<String?>>()
            infix fun String?.win(other: String?) = table[this]?.contains(other) == true
            infix fun String?.notWin(other: String?) = !(this win other)

            // まずバイオームによる初期順序関係を収集
            world.getBiomes().forEach { biome ->
                val placedFeatureList = biome.getPlacedFeatureSets().getOrNull(step.ordinal)?.toList()?.map { it.getId() } ?: emptyList()
                (0 until placedFeatureList.size).forEach { a ->
                    (a + 1 until placedFeatureList.size).forEach { b ->
                        table.getOrPut(placedFeatureList[a]) { mutableSetOf() } += placedFeatureList[b]
                    }
                }
            }

            // 更新できなくなるまで推移的順序関係（a > b && b > c => a > c）を適用
            while (true) {
                var changed = false
                allPlacedFeatures.forEach { a ->
                    allPlacedFeatures.forEach { b ->
                        allPlacedFeatures.forEach { c ->
                            if (a win b && b win c && a notWin c) {
                                table.getOrPut(a) { mutableSetOf() } += c
                                changed = true
                            }
                        }
                    }
                }
                if (!changed) break
            }

            // ソート
            val sortedPlacedFeatures = try {
                allPlacedFeatures.sortedWith { a, b ->
                    when {
                        a win b -> 1
                        b win a -> -1
                        else -> 0
                    }
                }
            } catch (e: IllegalArgumentException) {
                e.printStackTrace()
                allPlacedFeatures
            }

            // すべての要素がそれ以外のすべてに勝つような最小の要素をひたすらドロップアウト
            //   ABCDE
            // A 01111 ←これは他すべてのPlacedFeatureに勝つので単独でドロップアウト
            // B 00011 ←これはCに勝てず、CもBに勝てないので順序関係が定義できない
            // C 00011 しかしBとCはそれ以外に勝つのでBとCでまとめてドロップアウト
            // D 00001
            // E 00000
            val remainingPlacedFeatures = sortedPlacedFeatures.toMutableList()
            val placedFeatureClusters = mutableListOf<List<String?>>()
            while (remainingPlacedFeatures.isNotEmpty()) {
                var count = 1
                while (true) {

                    // remainingPlacedFeaturesの最初のcount個の要素が、それら以外のすべてに勝つならドロップアウト
                    val takes = remainingPlacedFeatures.take(count)
                    val drops = remainingPlacedFeatures.drop(count)
                    val allWin = takes.all { take ->
                        drops.all { drop ->
                            take win drop
                        }
                    }
                    if (allWin) {
                        placedFeatureClusters += takes.sortedBy { it }
                        remainingPlacedFeatures -= takes
                        break
                    }

                    count++
                }
            }

            jsonObject(
                "name" to step.name.jsonElement,
                "placedFeatureClusters" to placedFeatureClusters.map { placedFeatureCluster ->
                    if (placedFeatureCluster.size == 1) {
                        placedFeatureCluster.single().jsonElementOrJsonNull
                    } else {
                        placedFeatureCluster.associate { a ->
                            "$a" to jsonObject(
                                "wins" to placedFeatureCluster.filter { b -> a win b }.map { it.jsonElementOrJsonNull }.jsonArray,
                                "loses" to placedFeatureCluster.filter { b -> b win a }.map { it.jsonElementOrJsonNull }.jsonArray,
                            )
                        }.jsonObject
                    }
                }.jsonArray
            )
        }.jsonArray.let { writeAction(player, "dump_biome_features_matrix.json", it.toJson { setPrettyPrinting() }) }
    }

    // Server
    ModEvents.onInitialize {
        val command = Commands.literal("mf24")
            .let { builder ->
                var builder2 = builder
                CommandEvents.onRegisterSubCommand.fire { // ワールドロード時に毎回初期化されるため外で呼び出して使いまわす
                    builder2 = it(builder2)
                }
                builder2
            }
        CommandRegistrationEvent.EVENT.register { dispatcher, _, _ ->
            dispatcher.register(command)
        }
    }

    // Client
    ModEvents.onInitialize {
        val command = ClientCommandRegistrationEvent.literal("mf24c")
            .let { builder ->
                var builder2 = builder
                CommandEvents.onRegisterClientSubCommand.fire { // ワールドロード時に毎回初期化されるため外で呼び出して使いまわす
                    builder2 = it(builder2)
                }
                builder2
            }
        ClientCommandRegistrationEvent.EVENT.register { dispatcher, _ ->
            dispatcher.register(command)
        }
    }

    CommonRenderingEvents.onRenderBlockPosesOutline.add { context ->
        val player = context.player ?: return@add null
        val item = player.mainHandItem.item
        if (item is RenderBlockPosesOutlineListenerItem) {
            item.getBlockPoses(InteractionHand.MAIN_HAND, context)
        } else {
            null
        }
    }
    CommonRenderingEvents.onRenderBlockPosesOutline.add { context ->
        val player = context.player ?: return@add null
        val item = player.offhandItem.item
        if (item is RenderBlockPosesOutlineListenerItem) {
            item.getBlockPoses(InteractionHand.OFF_HAND, context)
        } else {
            null
        }
    }

    guiFullScreenTranslation.enJa()
    guiBackToGameTranslation.enJa()
    guiDeadPlayer.enJa()

}

object WaterBottleIngredient : CustomIngredient {
    val ID = MirageFairy2024.identifier("water_bottle")
    val SERIALIZER = object : CustomIngredientSerializer<WaterBottleIngredient> {
        override fun getIdentifier() = ID
        override fun getCodec(allowEmpty: Boolean): MapCodec<WaterBottleIngredient> = MapCodec.unit(WaterBottleIngredient)
        override fun getPacketCodec(): StreamCodec<RegistryFriendlyByteBuf, WaterBottleIngredient> = StreamCodec.unit(WaterBottleIngredient)
    }

    override fun requiresTesting() = true

    override fun test(stack: ItemStack): Boolean {
        if (stack isIn Items.POTION) {
            val potionContents = stack.get(DataComponents.POTION_CONTENTS) ?: return false
            if (potionContents isIn Potions.WATER) {
                return true
            }
        }
        return false
    }

    override fun getMatchingStacks() = listOf(PotionContents.createItemStack(Items.POTION, Potions.WATER))
    override fun getSerializer() = SERIALIZER
}
