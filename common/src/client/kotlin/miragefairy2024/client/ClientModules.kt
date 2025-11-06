package miragefairy2024.client

import miragefairy2024.ModContext
import miragefairy2024.client.mod.entity.initEntityClientModule
import miragefairy2024.client.mod.fairy.initFairyClientModule
import miragefairy2024.client.mod.fairyquest.initFairyQuestClientModule
import miragefairy2024.client.mod.initAttachmentChangedEventClientModule
import miragefairy2024.client.mod.initBagClientModule
import miragefairy2024.client.mod.initCommonClientModule
import miragefairy2024.client.mod.initEnchantmentClientModule
import miragefairy2024.client.mod.initFairyBuildingClientModule
import miragefairy2024.client.mod.initFairyLogisticsClientModule
import miragefairy2024.client.mod.initFairyStatueClientModule
import miragefairy2024.client.mod.initMachineClientModule
import miragefairy2024.client.mod.initPlacedItemClientModule
import miragefairy2024.client.mod.initSoundEventClientModule
import miragefairy2024.client.mod.initTooltipViewerClientModule
import miragefairy2024.client.mod.magicplant.initMagicPlantClientModule
import miragefairy2024.client.mod.particle.initParticleClientModule
import miragefairy2024.client.mod.recipeviewer.initRecipeViewerClientModule

context(ModContext)
fun initClientModules() {
    initCommonClientModule()
    initFairyQuestClientModule()
    initFairyClientModule()
    initFairyBuildingClientModule()
    initMagicPlantClientModule()
    initEntityClientModule()
    initParticleClientModule()
    initFairyStatueClientModule()
    initPlacedItemClientModule()
    initFairyLogisticsClientModule()
    initBagClientModule()
    initMachineClientModule()
    initSoundEventClientModule()
    initTooltipViewerClientModule()
    initAttachmentChangedEventClientModule()
    initRecipeViewerClientModule()
    initEnchantmentClientModule()
}
