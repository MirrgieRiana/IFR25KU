package miragefairy2024.mod

import com.mojang.brigadier.builder.LiteralArgumentBuilder
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
import miragefairy2024.util.Translation
import miragefairy2024.util.createItemStack
import miragefairy2024.util.enJa
import miragefairy2024.util.get
import miragefairy2024.util.humidityCategory
import miragefairy2024.util.isIn
import miragefairy2024.util.register
import miragefairy2024.util.registerClientDebugItem
import miragefairy2024.util.temperatureCategory
import miragefairy2024.util.text
import miragefairy2024.util.toTextureSource
import miragefairy2024.util.translate
import miragefairy2024.util.writeAction
import mirrg.kotlin.helium.join
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredient
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.core.component.DataComponents
import net.minecraft.core.registries.Registries
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.alchemy.PotionContents
import net.minecraft.world.item.alchemy.Potions
import net.minecraft.world.level.block.Blocks

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

val guiFullScreenTranslation = Translation({ "gui.${MirageFairy2024.identifier("common").toLanguageKey()}.fullScreen" }, "Click to full screen", "クリックで全画面表示")

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

    guiFullScreenTranslation.enJa()

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
