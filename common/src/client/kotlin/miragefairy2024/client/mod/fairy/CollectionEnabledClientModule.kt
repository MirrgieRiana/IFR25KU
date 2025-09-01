package miragefairy2024.client.mod.fairy

import com.mojang.blaze3d.platform.InputConstants
import miragefairy2024.ModContext
import miragefairy2024.client.util.KeyMappingCard
import miragefairy2024.client.util.sendToServer
import miragefairy2024.mod.fairy.SetCollectionEnabledChannel
import miragefairy2024.mod.fairy.TOGGLE_COLLECTION_ENABLED_KEY_TRANSLATION
import miragefairy2024.mod.fairy.collectionEnabled
import miragefairy2024.util.getOrDefault
import net.minecraft.client.KeyMapping
import net.minecraft.client.Minecraft

val toggleCollectionEnabledKeyMappingCard = KeyMappingCard(
    TOGGLE_COLLECTION_ENABLED_KEY_TRANSLATION.keyGetter(),
    InputConstants.UNKNOWN.value,
    KeyMapping.CATEGORY_GAMEPLAY,
) {
    val player = Minecraft.getInstance().player ?: return@KeyMappingCard
    SetCollectionEnabledChannel.sendToServer(!player.collectionEnabled.getOrDefault())
}

context(ModContext)
fun initCollectionEnabledClientModule() {
    toggleCollectionEnabledKeyMappingCard.init()
}
