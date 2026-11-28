package miragefairy2024.migration

import net.minecraft.core.DefaultedRegistry
import net.minecraft.core.Holder
import net.minecraft.core.Registry
import net.minecraft.core.RegistryAccess
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation as Identifier // MIGRATION2612 ReplaceImport net.minecraft.resources.Identifier
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider
import java.util.Optional

fun ResourceKey<*>.location__MIGRATION2612_ReplaceMethod(): Identifier = /* MIGRATION2612 ReplaceMethod this.identifier() */ this.location()

fun <T> Registry<T>.get__MIGRATION2612_ReplaceMethod(identifier: Identifier?): T? = /* MIGRATION2612 ReplaceMethod this.getValue(identifier) */ this.get(identifier)

fun <T> DefaultedRegistry<T>.get__MIGRATION2612_ReplaceMethod(identifier: Identifier?): T & Any = /* MIGRATION2612 ReplaceMethod this.getValue(identifier) */ this.get(identifier)

fun <T : Any> Registry<T>.getHolder__MIGRATION2612_ReplaceMethod(key: ResourceKey<T>): Optional<Holder.Reference<T>> = /* MIGRATION2612 ReplaceMethod this.get(key) */ this.getHolder(key)

fun <T : Any> RegistryAccess.registryOrThrow__MIGRATION2612_ReplaceMethod(key: ResourceKey<out Registry<out T>>): Registry<T> = /* MIGRATION2612 ReplaceMethod this.lookupOrThrow(key) */ this.registryOrThrow(key)

fun FriendlyByteBuf.readResourceLocation__MIGRATION2612_ReplaceMethod(): Identifier = /* MIGRATION2612 ReplaceMethod this.readIdentifier() */ this.readResourceLocation()

fun FriendlyByteBuf.writeResourceLocation__MIGRATION2612_ReplaceMethod(identifier: Identifier): FriendlyByteBuf = /* MIGRATION2612 ReplaceMethod this.writeIdentifier(identifier) */ this.writeResourceLocation(identifier)

fun BlockBehaviour.Properties.noCollission__MIGRATION2612_ReplaceMethod(): BlockBehaviour.Properties = /* MIGRATION2612 ReplaceMethod this.noCollision() */ this.noCollission()

fun TreeConfiguration.TreeConfigurationBuilder.dirt__MIGRATION2612_ReplaceMethod(provider: BlockStateProvider): TreeConfiguration.TreeConfigurationBuilder = /* MIGRATION2612 ReplaceMethod this.belowTrunkProvider(provider) */ this.dirt(provider)

fun Entity.moveTo__MIGRATION2612_ReplaceMethod(x: Double, y: Double, z: Double, yaw: Float, pitch: Float) = /* MIGRATION2612 ReplaceMethod this.snapTo(x, y, z, yaw, pitch) */ this.moveTo(x, y, z, yaw, pitch)
