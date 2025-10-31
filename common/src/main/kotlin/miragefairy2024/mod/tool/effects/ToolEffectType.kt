package miragefairy2024.mod.tool.effects

import miragefairy2024.ModContext
import miragefairy2024.mod.tool.ToolConfiguration
import miragefairy2024.mod.tool.ToolEffectType
import mirrg.kotlin.helium.max

context(ModContext)
fun initToolEffectType() {
    SelfMendingToolEffectType.init()
    ObtainFairyToolEffectType.init()
    CollectionToolEffectType.init()
    SoulStreamContainableToolEffectType.init()
    TillingRecipeHoeToolEffectType.init()
    EffectiveToolEffectType.init()
}

abstract class BooleanToolEffectType<in C : ToolConfiguration> : ToolEffectType<C, Boolean> {
    override fun castOrThrow(value: Any?) = value as Boolean
    override fun merge(a: Boolean, b: Boolean) = a || b
}

abstract class IntAddToolEffectType<in C : ToolConfiguration> : ToolEffectType<C, Int> {
    override fun castOrThrow(value: Any?) = value as Int
    override fun merge(a: Int, b: Int) = a + b
}

abstract class IntMaxToolEffectType<in C : ToolConfiguration> : ToolEffectType<C, Int> {
    override fun castOrThrow(value: Any?) = value as Int
    override fun merge(a: Int, b: Int) = a max b
}

abstract class DoubleAddToolEffectType<in C : ToolConfiguration> : ToolEffectType<C, Double> {
    override fun castOrThrow(value: Any?) = value as Double
    override fun merge(a: Double, b: Double) = a + b
}
