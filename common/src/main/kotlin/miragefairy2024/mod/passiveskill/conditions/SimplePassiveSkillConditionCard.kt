package miragefairy2024.mod.passiveskill.conditions

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mod.passiveskill.PassiveSkillCondition
import miragefairy2024.mod.passiveskill.PassiveSkillContext
import miragefairy2024.util.Translation
import miragefairy2024.util.enJa
import miragefairy2024.util.eyeBlockPos
import miragefairy2024.util.invoke
import miragefairy2024.util.isIn
import miragefairy2024.util.isNotIn
import miragefairy2024.util.lightProxy
import miragefairy2024.util.text
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBiomeTags
import net.minecraft.tags.FluidTags
import net.minecraft.tags.ItemTags
import net.minecraft.world.level.biome.Biome
import net.minecraft.world.level.levelgen.Heightmap

private fun isOutdoor(context: PassiveSkillContext) = context.blockPos.y >= context.world.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, context.blockPos).y
private fun biomeHasNoPrecipitation(context: PassiveSkillContext) = context.world.getBiome(context.blockPos).value().getPrecipitationAt(context.blockPos) == Biome.Precipitation.NONE
private fun biomeHasRain(context: PassiveSkillContext) = context.world.getBiome(context.blockPos).value().getPrecipitationAt(context.blockPos) == Biome.Precipitation.RAIN
private fun isDaytime(context: PassiveSkillContext) = context.world.lightProxy.getSkyDarken() < 4

enum class SimplePassiveSkillConditionCard(path: String, enName: String, jaName: String, private val function: (context: PassiveSkillContext) -> Boolean) : PassiveSkillCondition {
    OVERWORLD("overworld", "Overworld", "地上世界", { it.world.dimensionType().natural }),
    OUTDOOR("outdoor", "Outdoor", "屋外", { isOutdoor(it) }),
    INDOOR("indoor", "Indoor", "屋内", { !isOutdoor(it) }),
    SKY_VISIBLE("sky_visible", "Sky Visible", "空が見える", { it.world.canSeeSky(it.blockPos) }),
    FINE("fine", "Fine", "晴天", { !it.world.isRaining || biomeHasNoPrecipitation(it) }),
    RAINING("raining", "Raining", "雨天", { it.world.isRaining && biomeHasRain(it) }),
    THUNDERING("thundering", "Thundering", "雷雨", { it.world.isThundering && !biomeHasNoPrecipitation(it) }),
    DAYTIME("daytime", "Daytime", "昼間", { isDaytime(it) }),
    NIGHT("night", "Night", "夜間", { !isDaytime(it) }),
    UNDERWATER("underwater", "Underwater", "水中", { it.player.level().getBlockState(it.player.eyeBlockPos).fluidState isIn FluidTags.WATER }),
    IN_THE_AIR("in_the_air", "In the Air", "空中", { !it.player.onGround() }),
    ON_FIRE("on_fire", "On Fire", "炎上", { it.player.isOnFire }),
    IN_NETHER("in_nether", "In Nether", "ネザー", { it.world.getBiome(it.blockPos) isIn ConventionalBiomeTags.IS_NETHER }),
    NOT_IN_NETHER("not_in_nether", "Not In Nether", "ネザー外", { it.world.getBiome(it.blockPos) isNotIn ConventionalBiomeTags.IS_NETHER }),

    PICKAXE("pickaxe", "Pickaxe", "つるはし", { it.player.mainHandItem isIn ItemTags.PICKAXES }),
    AXE("axe", "Axe", "斧", { it.player.mainHandItem isIn ItemTags.AXES }),
    SHOVEL("shovel", "Shovel", "シャベル", { it.player.mainHandItem isIn ItemTags.SHOVELS }),
    HOE("hoe", "Hoe", "クワ", { it.player.mainHandItem isIn ItemTags.HOES }),
    SWORD("sword", "Sword", "剣", { it.player.mainHandItem isIn ItemTags.SWORDS }),
    ;

    val identifier = MirageFairy2024.identifier(path)
    val translation = Translation({ "${MirageFairy2024.MOD_ID}.passive_skill_condition.${identifier.toLanguageKey()}" }, enName, jaName)

    override fun test(context: PassiveSkillContext, level: Double, mana: Double) = function(context)
    override val text = text { translation() }

    context(ModContext)
    fun init() {
        translation.enJa()
    }
}
