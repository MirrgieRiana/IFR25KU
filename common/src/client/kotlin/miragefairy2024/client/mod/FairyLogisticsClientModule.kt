package miragefairy2024.client.mod

import miragefairy2024.ModContext
import miragefairy2024.client.lib.MachineScreen
import miragefairy2024.client.util.registerHandledScreen
import miragefairy2024.mod.fairylogistics.FairyActiveConsumerCard
import miragefairy2024.mod.fairylogistics.FairyActiveConsumerScreenHandler
import miragefairy2024.mod.fairylogistics.FairyLogisticsCard
import miragefairy2024.mod.fairylogistics.FairyLogisticsScreenHandler
import miragefairy2024.mod.fairylogistics.FairyPassiveSupplierBlockEntity
import miragefairy2024.mod.fairylogistics.FairyPassiveSupplierCard
import miragefairy2024.mod.fairylogistics.FairyPassiveSupplierScreenHandler
import miragefairy2024.util.invoke
import miragefairy2024.util.text
import net.minecraft.client.gui.GuiGraphics

context(_: ModContext)
fun initFairyLogisticsClientModule() {
    FairyPassiveSupplierCard.screenHandlerType.registerHandledScreen { gui, inventory, title -> FairyPassiveSupplierScreen(FairyPassiveSupplierCard, MachineScreen.Arguments(gui, inventory, title)) }
    FairyActiveConsumerCard.screenHandlerType.registerHandledScreen { gui, inventory, title -> FairyActiveConsumerScreen(FairyActiveConsumerCard, MachineScreen.Arguments(gui, inventory, title)) }
}

open class FairyLogisticsScreen<H : FairyLogisticsScreenHandler>(card: FairyLogisticsCard<*, *, *>, arguments: Arguments<H>) : MachineScreen<H>(card, arguments)

class FairyPassiveSupplierScreen(private val card: FairyPassiveSupplierCard, arguments: Arguments<FairyPassiveSupplierScreenHandler>) : FairyLogisticsScreen<FairyPassiveSupplierScreenHandler>(card, arguments) {
    private fun getLogisticsPower(): Int {
        val guiSlotIndex = card.guiSlotIndexTable[FairyPassiveSupplierCard.FAIRY_SLOT] ?: return 0
        val fairyItemStack = menu.items.getOrNull(guiSlotIndex) ?: return 0
        return FairyPassiveSupplierBlockEntity.getLogisticsPower(fairyItemStack)
    }

    override fun renderLabels(context: GuiGraphics, mouseX: Int, mouseY: Int) {
        super.renderLabels(context, mouseX, mouseY)
        context.drawString(font, text { "${getLogisticsPower()}/min"() }, 102, 23, 0x373737, false) // TODO 表示を改善
    }
}

class FairyActiveConsumerScreen(card: FairyActiveConsumerCard, arguments: Arguments<FairyActiveConsumerScreenHandler>) : FairyLogisticsScreen<FairyActiveConsumerScreenHandler>(card, arguments)
