package miragefairy2024.mod.fairy

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mod.CommandEvents
import miragefairy2024.util.Translation
import miragefairy2024.util.enJa
import miragefairy2024.util.executesThrowable
import miragefairy2024.util.eyeBlockPos
import miragefairy2024.util.failure
import miragefairy2024.util.getOrCreate
import miragefairy2024.util.invoke
import miragefairy2024.util.isNotIn
import miragefairy2024.util.itemStacks
import miragefairy2024.util.mutate
import miragefairy2024.util.opposite
import miragefairy2024.util.register
import miragefairy2024.util.registerServerDebugItem
import miragefairy2024.util.registerServerToClientPayloadType
import miragefairy2024.util.sendToClient
import miragefairy2024.util.string
import miragefairy2024.util.success
import miragefairy2024.util.text
import miragefairy2024.util.toTextureSource
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.minecraft.commands.Commands
import net.minecraft.commands.arguments.ResourceLocationArgument
import net.minecraft.core.BlockPos
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.Mth
import net.minecraft.world.Container
import net.minecraft.world.WorldlyContainerHolder
import net.minecraft.world.entity.EntityType
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.level.ClipContext
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.ChestBlock
import net.minecraft.world.level.block.entity.ChestBlockEntity
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.HitResult

private val identifier = MirageFairy2024.identifier("fairy_dream")
val GAIN_FAIRY_DREAM_TRANSLATION = Translation({ "gui.${identifier.toLanguageKey()}.gain" }, "Dreamed of a new fairy!", "新たな妖精の夢を見た！")
val GAIN_FAIRY_TRANSLATION = Translation({ "gui.${identifier.toLanguageKey()}.gain_fairy" }, "%s found!", "%sを発見した！")
val GIVE_ALL_SUCCESS_TRANSLATION = Translation({ identifier.toLanguageKey("commands", "give.all.success") }, "Gave %s fairy dreams", "%s 個の妖精の夢を付与しました")
val GIVE_ONE_SUCCESS_TRANSLATION = Translation({ identifier.toLanguageKey("commands", "give.one.success") }, "Gave %s dream", "%s の夢を付与しました")
val ALREADY_HAVE_DREAM_TRANSLATION = Translation({ identifier.toLanguageKey("commands", "already_have_dream") }, "You already have %s dream", "すでに %s の夢を持っています")
val UNKNOWN_MOTIF_TRANSLATION = Translation({ identifier.toLanguageKey("commands", "unknown_motif") }, "Unknown motif: %s", "不明なモチーフ: %s")
val REMOVE_ALL_SUCCESS_TRANSLATION = Translation({ identifier.toLanguageKey("commands", "remove.all.success") }, "Removed %s fairy dreams", "%s 個の妖精の夢を削除しました")
val REMOVE_ONE_SUCCESS_TRANSLATION = Translation({ identifier.toLanguageKey("commands", "remove.one.success") }, "Removed %s dream", "%s の夢を削除しました")
val DO_NOT_HAVE_DREAM_TRANSLATION = Translation({ identifier.toLanguageKey("commands", "do_not_have_dream") }, "You don't have %s dream", "あなたは %s の夢を持っていません")

context(ModContext)
fun initFairyDream() {

    // パケットタイプ登録
    GainFairyDreamChannel.registerServerToClientPayloadType()

    // プレイヤー追加データ登録
    FAIRY_DREAM_CONTAINER_ATTACHMENT_TYPE.register()

    // デバッグアイテム
    registerServerDebugItem("debug_clear_fairy_dream", Items.STRING.toTextureSource(), 0xFF0000DD.toInt()) { world, player, _, _ ->
        player.fairyDreamContainer.mutate { it.clear() }
        player.displayClientMessage(text { "Cleared fairy dream"() }, true)
    }
    registerServerDebugItem("debug_gain_fairy_dream", Items.STRING.toTextureSource(), 0xFF0000BB.toInt()) { world, player, hand, _ ->
        val fairyItemStack = player.getItemInHand(hand.opposite)
        if (fairyItemStack isNotIn FairyCard.item()) return@registerServerDebugItem
        val motif = fairyItemStack.getFairyMotif() ?: return@registerServerDebugItem

        if (!player.isShiftKeyDown) {
            player.fairyDreamContainer.mutate { it[motif] = true }
            GainFairyDreamChannel.sendToClient(player, motif)
        } else {
            player.fairyDreamContainer.mutate { it[motif] = false }
        }
    }

    // 妖精の夢回収判定
    ServerTickEvents.END_SERVER_TICK.register { server ->
        if (server.tickCount % (20 * 5) == 0) {
            server.playerList.players.forEach { player ->
                if (player.isSpectator) return@forEach
                if (player.tickCount < 20 * 60) return@forEach
                val world = player.level()
                val random = world.random

                val motifs = mutableSetOf<Motif>()

                val items = mutableSetOf<Item>()
                val blocks = mutableSetOf<Block>()
                val entityTypes = mutableSetOf<EntityType<*>>()
                run {

                    fun insertItem(itemStack: ItemStack) {
                        val item = itemStack.item

                        items += item

                        if (item is FairyDreamProviderItem) motifs += item.getFairyDreamMotifs(itemStack)

                        val block = Block.byItem(item)
                        if (block != Blocks.AIR) blocks += block

                    }

                    fun insertBlockPos(blockPos: BlockPos) {
                        val blockState = world.getBlockState(blockPos)
                        val block = blockState.block

                        blocks += block

                        if (block is FairyDreamProviderBlock) motifs += block.getFairyDreamMotifs(world, blockPos)

                        run noInventory@{
                            val inventory = if (block is WorldlyContainerHolder) {
                                block.getContainer(blockState, world, blockPos)
                            } else if (blockState.hasBlockEntity()) {
                                val blockEntity = world.getBlockEntity(blockPos)
                                if (blockEntity is Container) {
                                    if (blockEntity is ChestBlockEntity && block is ChestBlock) {
                                        ChestBlock.getContainer(block, blockState, world, blockPos, true) ?: return@noInventory
                                    } else {
                                        blockEntity
                                    }
                                } else {
                                    return@noInventory
                                }
                            } else {
                                return@noInventory
                            }
                            inventory.itemStacks.forEach { itemStack ->
                                insertItem(itemStack)
                            }
                        }

                    }


                    // インベントリ判定
                    player.inventory.itemStacks.forEach { itemStack ->
                        insertItem(itemStack)
                    }

                    // 足元判定
                    insertBlockPos(player.blockPosition())
                    insertBlockPos(player.blockPosition().below())

                    // 視線判定
                    val start = player.eyePosition
                    val pitch = player.xRot
                    val yaw = player.yRot
                    val d = Mth.cos(-yaw * (Mth.PI / 180) - Mth.PI)
                    val a = Mth.sin(-yaw * (Mth.PI / 180) - Mth.PI)
                    val e = -Mth.cos(-pitch * (Mth.PI / 180))
                    val c = Mth.sin(-pitch * (Mth.PI / 180))
                    val end = start.add(a * e * 32.0, c * 32.0, d * e * 32.0)
                    val raycastResult = world.clip(ClipContext(start, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player))
                    if (raycastResult.type == HitResult.Type.BLOCK) insertBlockPos(raycastResult.blockPos)

                    // 周辺エンティティ判定
                    val entities = world.getEntities(player, AABB(player.eyePosition.add(-8.0, -8.0, -8.0), player.eyePosition.add(8.0, 8.0, 8.0)))
                    entities.forEach {
                        entityTypes += it.type
                    }

                    // 周辺ブロック判定
                    insertBlockPos(player.eyeBlockPos.offset(random.nextInt(17) - 8, random.nextInt(17) - 8, random.nextInt(17) - 8))

                }
                items.forEach {
                    motifs += FairyDreamRecipes.ITEM.test(it)
                }
                blocks.forEach {
                    motifs += FairyDreamRecipes.BLOCK.test(it)
                }
                entityTypes.forEach {
                    motifs += FairyDreamRecipes.ENTITY_TYPE.test(it)
                }

                player.fairyDreamContainer.getOrCreate().gain(player, motifs)

            }
        }
    }

    // 翻訳
    GAIN_FAIRY_DREAM_TRANSLATION.enJa()
    GAIN_FAIRY_TRANSLATION.enJa()
    GIVE_ALL_SUCCESS_TRANSLATION.enJa()
    GIVE_ONE_SUCCESS_TRANSLATION.enJa()
    ALREADY_HAVE_DREAM_TRANSLATION.enJa()
    UNKNOWN_MOTIF_TRANSLATION.enJa()
    REMOVE_ALL_SUCCESS_TRANSLATION.enJa()
    REMOVE_ONE_SUCCESS_TRANSLATION.enJa()
    DO_NOT_HAVE_DREAM_TRANSLATION.enJa()

    CommandEvents.onRegisterSubCommand { builder ->
        builder
            .then(
                Commands.literal("dream")
                    .requires { it.hasPermission(2) }
                    .then(
                        Commands.literal("give")
                            .then(
                                Commands.literal("all")
                                    .executesThrowable { context ->
                                        val player = context.source.playerOrException
                                        val count = player.fairyDreamContainer.getOrCreate().gain(player, motifRegistry.toSet())
                                        context.source.sendSuccess({ text { GIVE_ALL_SUCCESS_TRANSLATION("$count") } }, true)
                                        success()
                                    }
                            )
                            .then(
                                Commands.argument("motif", ResourceLocationArgument.id())
                                    .suggests { _, builder ->
                                        motifRegistry.keySet().forEach {
                                            builder.suggest(it.string)
                                        }
                                        builder.buildFuture()
                                    }
                                    .executesThrowable { context ->
                                        val player = context.source.playerOrException
                                        val id = context.getArgument("motif", ResourceLocation::class.java)
                                        val motif = motifRegistry.get(id) ?: failure(text { UNKNOWN_MOTIF_TRANSLATION(id.string) })
                                        val count = player.fairyDreamContainer.getOrCreate().gain(player, setOf(motif))
                                        if (count == 1) {
                                            context.source.sendSuccess({ text { GIVE_ONE_SUCCESS_TRANSLATION(motif.displayName) } }, true)
                                        } else {
                                            failure(text { ALREADY_HAVE_DREAM_TRANSLATION(motif.displayName) })
                                        }
                                        success()
                                    }
                            )
                    )
                    .then(
                        Commands.literal("remove")
                            .then(
                                Commands.literal("all")
                                    .executesThrowable { context ->
                                        val player = context.source.playerOrException
                                        val count = player.fairyDreamContainer.getOrCreate().entries.size
                                        player.fairyDreamContainer.mutate { it.clear() }
                                        context.source.sendSuccess({ text { REMOVE_ALL_SUCCESS_TRANSLATION("$count") } }, true)
                                        success()
                                    }
                            )
                            .then(
                                Commands.argument("motif", ResourceLocationArgument.id())
                                    .suggests { _, builder ->
                                        motifRegistry.keySet().forEach {
                                            builder.suggest(it.string)
                                        }
                                        builder.buildFuture()
                                    }
                                    .executesThrowable { context ->
                                        val player = context.source.playerOrException
                                        val id = context.getArgument("motif", ResourceLocation::class.java)
                                        val motif = motifRegistry.get(id) ?: failure(text { UNKNOWN_MOTIF_TRANSLATION(id.string) })
                                        val have = player.fairyDreamContainer.getOrCreate()[motif]
                                        if (have) {
                                            player.fairyDreamContainer.mutate { it[motif] = false }
                                            context.source.sendSuccess({ text { REMOVE_ONE_SUCCESS_TRANSLATION(motif.displayName) } }, true)
                                        } else {
                                            failure(text { DO_NOT_HAVE_DREAM_TRANSLATION(motif.displayName) })
                                        }
                                        success()
                                    }
                            )
                    )
            )
    }

}


interface FairyDreamProviderItem {
    fun getFairyDreamMotifs(itemStack: ItemStack): List<Motif>
}

interface FairyDreamProviderBlock {
    fun getFairyDreamMotifs(world: Level, blockPos: BlockPos): List<Motif>
}
