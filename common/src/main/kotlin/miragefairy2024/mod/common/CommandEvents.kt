package miragefairy2024.mod.common

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import dev.architectury.event.events.client.ClientCommandRegistrationEvent
import miragefairy2024.InitializationEventRegistry
import net.minecraft.commands.CommandSourceStack

object CommandEvents {
    val onRegisterSubCommand = InitializationEventRegistry<(LiteralArgumentBuilder<CommandSourceStack>) -> LiteralArgumentBuilder<CommandSourceStack>>()
    val onRegisterClientSubCommand = InitializationEventRegistry<(LiteralArgumentBuilder<ClientCommandRegistrationEvent.ClientCommandSourceStack>) -> LiteralArgumentBuilder<ClientCommandRegistrationEvent.ClientCommandSourceStack>>()
}
