package miragefairy2024.util

import miragefairy2024.InitializationEventRegistry
import miragefairy2024.ModContext
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput as FabricPackOutput // MIGRATION2612 ReplaceImport net.fabricmc.fabric.api.datagen.v1.FabricPackOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider as FabricTagsProvider // MIGRATION2612 ReplaceImport net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider
import net.minecraft.core.HolderLookup
import net.minecraft.core.Registry
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation as Identifier // MIGRATION2612 ReplaceImport net.minecraft.resources.Identifier
import net.minecraft.tags.TagKey
import net.minecraft.world.damagesource.DamageType
import net.minecraft.world.entity.EntityType
import net.minecraft.world.item.Item
import net.minecraft.world.item.enchantment.Enchantment
import net.minecraft.world.level.biome.Biome
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.levelgen.structure.Structure
import java.util.concurrent.CompletableFuture

fun <T> Identifier.toTag(registry: ResourceKey<Registry<T>>): TagKey<T> = TagKey.create(registry, this)
fun Identifier.toItemTag(): TagKey<Item> = this.toTag(Registries.ITEM)
fun Identifier.toBlockTag(): TagKey<Block> = this.toTag(Registries.BLOCK)
fun Identifier.toBiomeTag(): TagKey<Biome> = this.toTag(Registries.BIOME)
fun Identifier.toDamageTypeTag(): TagKey<DamageType> = this.toTag(Registries.DAMAGE_TYPE)
fun Identifier.toStructureTag(): TagKey<Structure> = this.toTag(Registries.STRUCTURE)

val TagKey<Block>.generator @JvmName("getBlockGenerator") get() = Pair(this, TagGenerator.BLOCK)
val TagKey<Item>.generator @JvmName("getItemGenerator") get() = Pair(this, TagGenerator.ITEM)
val TagKey<Biome>.generator @JvmName("getBiomeGenerator") get() = Pair(this, TagGenerator.BIOME)
val TagKey<Structure>.generator @JvmName("getStructureGenerator") get() = Pair(this, TagGenerator.STRUCTURE)
val TagKey<EntityType<*>>.generator @JvmName("getEntityTypeGenerator") get() = Pair(this, TagGenerator.ENTITY_TYPE)
val TagKey<DamageType>.generator @JvmName("getDamageTypeGenerator") get() = Pair(this, TagGenerator.DAMAGE_TYPE)
val TagKey<Enchantment>.generator @JvmName("getEnchantmentGenerator") get() = Pair(this, TagGenerator.ENCHANTMENT)

class TagGenerator<T>(
    private val registryKey: ResourceKey<out Registry<T>>,
    private val providerFactory: (TagGenerator<T>.(FabricPackOutput, CompletableFuture<HolderLookup.Provider>) -> FabricTagsProvider<T>)? = null,
) {
    companion object {
        val entries = mutableListOf<TagGenerator<*>>()
        private operator fun <T> TagGenerator<T>.not() = this.also { entries.add(it) }

        val BLOCK = !TagGenerator(Registries.BLOCK) { output, registriesFuture ->
            object : FabricTagsProvider.BlockTagProvider(output, registriesFuture) {
                override fun addTags(arg: HolderLookup.Provider) = eventRegistry.fire { it { tag -> getOrCreateTagBuilder(tag) } }
            }
        }
        val ITEM = !TagGenerator(Registries.ITEM) { output, registriesFuture ->
            object : FabricTagsProvider.ItemTagProvider(output, registriesFuture) {
                override fun addTags(arg: HolderLookup.Provider) = eventRegistry.fire { it { tag -> getOrCreateTagBuilder(tag) } }
            }
        }
        val BIOME = !TagGenerator(Registries.BIOME)
        val STRUCTURE = !TagGenerator(Registries.STRUCTURE)
        val ENTITY_TYPE = !TagGenerator(Registries.ENTITY_TYPE) { output, registriesFuture ->
            object : FabricTagsProvider.EntityTypeTagProvider(output, registriesFuture) {
                override fun addTags(arg: HolderLookup.Provider) = eventRegistry.fire { it { tag -> getOrCreateTagBuilder(tag) } }
            }
        }
        val DAMAGE_TYPE = !TagGenerator(Registries.DAMAGE_TYPE)
        val ENCHANTMENT = !TagGenerator(Registries.ENCHANTMENT) { output, registriesFuture ->
            object : FabricTagsProvider.EnchantmentTagProvider(output, registriesFuture) {
                override fun addTags(arg: HolderLookup.Provider) = eventRegistry.fire { it { tag -> getOrCreateTagBuilder(tag) } }
            }
        }
    }

    val eventRegistry = InitializationEventRegistry<((TagKey<T>) -> FabricTagsProvider<T>.FabricTagBuilder) -> Unit>()

    fun createProvider(output: FabricPackOutput, registriesFuture: CompletableFuture<HolderLookup.Provider>): FabricTagsProvider<T> {
        return if (providerFactory != null) {
            providerFactory(this, output, registriesFuture)
        } else object : FabricTagsProvider<T>(output, registryKey, registriesFuture) {
            override fun addTags(arg: HolderLookup.Provider) = eventRegistry.fire { it { tag -> getOrCreateTagBuilder(tag) } }
        }
    }
}


context(_: ModContext)
fun <T> Pair<TagKey<T>, TagGenerator<T>>.registerChild(elementGetter: () -> T) {
    this.second.eventRegistry {
        it(this.first).add(elementGetter())
    }
}

context(_: ModContext)
fun <T> Pair<TagKey<T>, TagGenerator<T>>.registerChild(resourceLocation: Identifier) {
    this.second.eventRegistry {
        it(this.first).add(resourceLocation)
    }
}

context(_: ModContext)
fun <T> Pair<TagKey<T>, TagGenerator<T>>.registerChild(resourceKey: ResourceKey<T>) {
    this.second.eventRegistry {
        it(this.first).add(resourceKey)
    }
}

context(_: ModContext)
fun <T> Pair<TagKey<T>, TagGenerator<T>>.registerChild(tag: TagKey<T>) {
    this.second.eventRegistry {
        it(this.first).addOptionalTag(tag)
    }
}
