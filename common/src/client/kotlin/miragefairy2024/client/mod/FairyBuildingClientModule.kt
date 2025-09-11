package miragefairy2024.client.mod

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.client.lib.MachineScreen
import miragefairy2024.client.util.registerHandledScreen
import miragefairy2024.mod.fairybuilding.FOLIA_TRANSLATION
import miragefairy2024.mod.fairybuilding.FairyBuildingCard
import miragefairy2024.mod.fairybuilding.FairyBuildingModelCard
import miragefairy2024.mod.fairybuilding.FairyBuildingScreenHandler
import miragefairy2024.mod.fairybuilding.FairyCollectorCard
import miragefairy2024.mod.fairybuilding.FairyCollectorScreenHandler
import miragefairy2024.mod.fairybuilding.FairyFactoryCard
import miragefairy2024.mod.fairybuilding.FairyFactoryScreenHandler
import miragefairy2024.mod.fairybuilding.FairyHouseCard
import miragefairy2024.mod.fairybuilding.FairyHouseScreenHandler
import miragefairy2024.util.invoke
import miragefairy2024.util.text
import mirrg.kotlin.helium.atMost
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin
import net.minecraft.client.gui.GuiGraphics
import java.util.Optional
import kotlin.math.roundToInt

context(ModContext)
fun initFairyBuildingClientModule() {
    ModelLoadingPlugin.register {
        FairyBuildingModelCard.entries.forEach { card ->
            it.addModels(card.identifier)
        }
    }

    FairyHouseCard.screenHandlerType.registerHandledScreen { gui, inventory, title -> FairyHouseScreen(FairyHouseCard, MachineScreen.Arguments(gui, inventory, title)) }
    FairyCollectorCard.screenHandlerType.registerHandledScreen { gui, inventory, title -> FairyCollectorScreen(FairyCollectorCard, MachineScreen.Arguments(gui, inventory, title)) }
}

open class FairyBuildingScreen<H : FairyBuildingScreenHandler>(card: FairyBuildingCard<*, *, *>, arguments: Arguments<H>) : MachineScreen<H>(card, arguments) {
    companion object {
        val SPRITES_TEXTURE = MirageFairy2024.identifier("textures/gui/sprites/fairy_building.png")
    }
}

open class FairyFactoryScreen<H : FairyFactoryScreenHandler>(private val card: FairyFactoryCard<*, *, *>, arguments: Arguments<H>) : FairyBuildingScreen<H>(card, arguments) {
    override fun renderBg(context: GuiGraphics, delta: Float, mouseX: Int, mouseY: Int) {
        super.renderBg(context, delta, mouseX, mouseY)
        val h = (9.0 * (menu.folia / card.maxFolia.toDouble() atMost 1.0)).roundToInt()
        context.blit(SPRITES_TEXTURE, leftPos + 164, topPos + imageHeight - 94 + (9 - h), 32F, 0F + (9 - h).toFloat(), 5, h, 64, 64)
    }

    override fun renderTooltip(context: GuiGraphics, x: Int, y: Int) {
        super.renderTooltip(context, x, y)
        run {
            if (x in this.leftPos + 164 until this.leftPos + 164 + 5 && y in this.topPos + imageHeight - 94 until this.topPos + imageHeight - 94 + 9) {
                val texts = listOf(
                    text { FOLIA_TRANSLATION() },
                    text { "${menu.folia} / ${card.maxFolia}"() },
                )
                context.renderTooltip(font, texts, Optional.empty(), x, y)
            }
        }
    }
}

class FairyHouseScreen(card: FairyHouseCard, arguments: Arguments<FairyHouseScreenHandler>) : FairyFactoryScreen<FairyHouseScreenHandler>(card, arguments)

class FairyCollectorScreen(card: FairyCollectorCard, arguments: Arguments<FairyCollectorScreenHandler>) : FairyFactoryScreen<FairyCollectorScreenHandler>(card, arguments) {
    override fun renderBg(context: GuiGraphics, delta: Float, mouseX: Int, mouseY: Int) {
        super.renderBg(context, delta, mouseX, mouseY)
        context.blit(SPRITES_TEXTURE, leftPos + 14, topPos + 28, 0F, 0F, (18.0 * (menu.collectionProgress / 10000.0 atMost 1.0)).roundToInt(), 4, 64, 64)
        context.blit(SPRITES_TEXTURE, leftPos + 80, topPos + 28, 0F, 0F, (18.0 * (menu.sortProgress / 10000.0 atMost 1.0)).roundToInt(), 4, 64, 64)
    }

    override fun renderTooltip(context: GuiGraphics, x: Int, y: Int) {
        super.renderTooltip(context, x, y)
        run {
            if (x in this.leftPos + 19 until this.leftPos + 19 + 18 && y in this.topPos + 28 until this.topPos + 28 + 4) {
                context.renderTooltip(font, listOf(text { "${menu.collectionProgress} / 10000 (+${menu.collectionSpeed * 20}/s)"() }), Optional.empty(), x, y)
            }
            if (x in this.leftPos + 76 until this.leftPos + 76 + 18 && y in this.topPos + 28 until this.topPos + 28 + 4) {
                context.renderTooltip(font, listOf(text { "${menu.sortProgress} / 10000 (+${menu.sortSpeed * 20}/s)"() }), Optional.empty(), x, y)
            }
        }
    }
}
