package miragefairy2024.util

import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import dev.architectury.event.events.client.ClientCommandRegistrationEvent
import miragefairy2024.clientProxy
import mirrg.kotlin.slf4j.hydrogen.getLogger
import net.minecraft.commands.CommandSourceStack
import net.minecraft.network.chat.Component

sealed class CommandResult(val result: Int) : Throwable(null, null, false, false) {
    class Success(result: Int) : CommandResult(result)
    class Failure(val component: Component) : CommandResult(0)
}

fun success(result: Int = 1): Nothing = throw CommandResult.Success(result)
fun failure(component: Component): Nothing = throw CommandResult.Failure(component)

@JvmName("executesThrowableServer")
fun <S : CommandSourceStack, T : ArgumentBuilder<S, T>> ArgumentBuilder<S, T>.executesThrowable(runner: (context: CommandContext<S>) -> Nothing): T = this.executes { context ->
    try {
        runner(context)
    } catch (result: CommandResult) {
        if (result is CommandResult.Failure) {
            context.source.sendFailure(result.component)
        }
        result.result
    } catch (e: Exception) {
        context.source.sendFailure(text { "Error: ${e.message}"().red })
        getLogger(CommandResult::class.java).error("Command Error", e)
        0
    }
}

@JvmName("executesThrowableClient")
fun <S : ClientCommandRegistrationEvent.ClientCommandSourceStack, T : ArgumentBuilder<S, T>> ArgumentBuilder<S, T>.executesThrowable(runner: (context: CommandContext<S>) -> Nothing): T = this.executes { context ->
    try {
        runner(context)
    } catch (result: CommandResult) {
        if (result is CommandResult.Failure) {
            clientProxy!!.getClientPlayer()!!.displayClientMessage(result.component, false)
        }
        result.result
    } catch (e: Exception) {
        clientProxy!!.getClientPlayer()!!.displayClientMessage(text { "Error: ${e.message}"().red }, false)
        getLogger(CommandResult::class.java).error("Command Error", e)
        0
    }
}
