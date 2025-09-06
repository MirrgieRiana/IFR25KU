package miragefairy2024.util

import net.minecraft.resources.ResourceKey
import net.minecraft.tags.TagKey
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.damagesource.DamageType

infix fun DamageSource.isIn(key: ResourceKey<DamageType>) = this.`is`(key)
infix fun DamageSource.isNotIn(key: ResourceKey<DamageType>) = !(this isIn key)
infix fun DamageSource.isIn(tag: TagKey<DamageType>) = this.`is`(tag)
infix fun DamageSource.isNotIn(tag: TagKey<DamageType>) = !(this isIn tag)
