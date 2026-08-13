package miragefairy2024.mod.haimeviska.cards

import miragefairy2024.mod.haimeviska.HAIMEVISKA_LOGS_BLOCK_TAG
import miragefairy2024.mod.haimeviska.HAIMEVISKA_LOGS_ITEM_TAG
import miragefairy2024.mod.haimeviska.HaimeviskaBlockCard
import miragefairy2024.mod.wood.WoodBlockConfiguration
import miragefairy2024.mod.wood.cards.WoodHorizontalFacingLogBlockCard
import net.minecraft.world.level.material.MapColor

abstract class HaimeviskaHorizontalFacingLogBlockCard(configuration: WoodBlockConfiguration) : WoodHorizontalFacingLogBlockCard(configuration, { HaimeviskaBlockCard.LOG }, HAIMEVISKA_LOGS_BLOCK_TAG, HAIMEVISKA_LOGS_ITEM_TAG, MapColor.RAW_IRON)
