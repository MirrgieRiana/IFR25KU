package miragefairy2024.client.mod.fairy

import io.wispforest.owo.ui.component.Components
import io.wispforest.owo.ui.container.Containers
import io.wispforest.owo.ui.core.HorizontalAlignment
import io.wispforest.owo.ui.core.Insets
import io.wispforest.owo.ui.core.OwoUIAdapter
import io.wispforest.owo.ui.core.Sizing
import io.wispforest.owo.ui.core.Surface
import io.wispforest.owo.ui.core.VerticalAlignment
import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.client.util.KeyMappingCard
import miragefairy2024.client.util.LimitedLabelComponent
import miragefairy2024.client.util.createOwoToast
import miragefairy2024.client.util.horizontalSpace
import miragefairy2024.client.util.registerClientPacketReceiver
import miragefairy2024.client.util.registerHandledScreen
import miragefairy2024.client.util.sendToServer
import miragefairy2024.client.util.verticalSpace
import miragefairy2024.mod.fairy.GAIN_FAIRY_DREAM_TRANSLATION
import miragefairy2024.mod.fairy.GainFairyDreamChannel
import miragefairy2024.mod.fairy.OPEN_SOUL_STREAM_KEY_TRANSLATION
import miragefairy2024.mod.fairy.OpenSoulStreamChannel
import miragefairy2024.mod.fairy.createFairyItemStack
import miragefairy2024.mod.fairy.motifTableScreenHandlerType
import miragefairy2024.mod.fairy.soulStreamScreenHandlerType
import miragefairy2024.util.black
import miragefairy2024.util.darkBlue
import miragefairy2024.util.invoke
import miragefairy2024.util.plus
import miragefairy2024.util.text
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents
import net.minecraft.client.KeyMapping
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.components.ImageButton
import net.minecraft.client.gui.components.WidgetSprites
import net.minecraft.client.gui.screens.inventory.InventoryScreen
import net.minecraft.network.chat.Component
import org.lwjgl.glfw.GLFW

context(ModContext)
fun initFairyClientModule() {
    initClientSoulStream()

    // GUI登録
    motifTableScreenHandlerType.registerHandledScreen { gui, inventory, title -> MotifTableScreen(gui, inventory, title) }

    // パケットハンドラ登録
    GainFairyDreamChannel.registerClientPacketReceiver { motif ->
        val itemStack = motif.createFairyItemStack()
        val component = Containers.horizontalFlow(Sizing.fixed(160), Sizing.fixed(32)).apply {
            surface(Surface.tiled(MirageFairy2024.identifier("textures/gui/fairy_dream_toast.png"), 160, 32))
            padding(Insets.of(0, 0, 8, 8))
            verticalAlignment(VerticalAlignment.CENTER)
            child(Components.item(itemStack))
            child(horizontalSpace(6))
            child(Containers.verticalFlow(Sizing.content(), Sizing.content()).apply {
                child(Components.label(text { GAIN_FAIRY_DREAM_TRANSLATION().black }))
                child(verticalSpace(2))
                child(LimitedLabelComponent(itemStack.hoverName.darkBlue).horizontalSizing(Sizing.fixed(160 - 8 - 16 - 6 - 10 - 8)).margins(Insets.of(0, 0, 4, 0)))
            })
        }
        Minecraft.getInstance().toasts.addToast(createOwoToast(component))
    }

}
