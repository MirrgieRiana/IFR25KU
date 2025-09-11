package miragefairy2024.mod.fairy

import mirrg.kotlin.helium.unit
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.tags.TagKey
import net.minecraft.world.entity.EntityType
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block
import kotlin.jvm.optionals.getOrElse

object FairyDreamRecipes {
    val ITEM = FairyDreamTable<Item>(BuiltInRegistries.ITEM)
    val BLOCK = FairyDreamTable<Block>(BuiltInRegistries.BLOCK)
    val ENTITY_TYPE = FairyDreamTable<EntityType<*>>(BuiltInRegistries.ENTITY_TYPE)
}

class FairyDreamTable<T>(val registry: Registry<T>) {
    private val entries = mutableListOf<Pair<T, Motif>>()
    private val tagEntries = mutableListOf<Pair<TagKey<T>, Motif>>()

    private val mapPair: Pair<Map<T, Set<Motif>>, Map<Motif, Set<T>>> by lazy {
        val map = mutableMapOf<T, MutableSet<Motif>>()
        val reverseMap = mutableMapOf<Motif, MutableSet<T>>()
        entries.forEach { (key, motif) ->
            map.getOrPut(key) { mutableSetOf() } += motif
            reverseMap.getOrPut(motif) { mutableSetOf() } += key
        }
        tagEntries.forEach { (tag, motif) ->
            registry.getTag(tag).getOrElse { return@forEach }.map { it.value() }.forEach { key ->
                map.getOrPut(key) { mutableSetOf() } += motif
                reverseMap.getOrPut(motif) { mutableSetOf() } += key
            }
        }
        Pair(map, reverseMap)
    }

    fun register(key: T, motif: Motif) = unit { entries += Pair(key, motif) }
    fun registerFromTag(tag: TagKey<T>, motif: Motif) = unit { tagEntries += Pair(tag, motif) }

    fun test(key: T) = mapPair.first.getOrElse(key) { setOf() }
    fun getDisplayMap() = mapPair.second
}
