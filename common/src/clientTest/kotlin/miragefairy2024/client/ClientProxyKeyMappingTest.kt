package miragefairy2024.client

import miragefairy2024.mixin.client.api.inputEventsHandlers
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.minecraft.client.KeyMapping
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.Mockito

class ClientProxyKeyMappingTest {

    @Test
    fun `登録したキーの未処理クリックをすべて通知し現在のキー設定で照合するのだ`() {
        val oldHandlers = inputEventsHandlers.toSet()
        val constructorArguments = mutableListOf<List<*>>()
        try {
            Mockito.mockConstruction(KeyMapping::class.java) { _, context ->
                constructorArguments += context.arguments()
            }.use { construction ->
                Mockito.mockStatic(KeyBindingHelper::class.java).use { bindings ->
                    var clicks = 0
                    val keyMapping = ClientProxyImpl().registerKeyMapping("key.test", 90, "key.categories.inventory") {
                        clicks++
                    }
                    val nativeKeyMapping: KeyMapping = construction.constructed().single()
                    Assertions.assertEquals(listOf(listOf("key.test", 90, "key.categories.inventory")), constructorArguments)
                    bindings.verify { KeyBindingHelper.registerKeyBinding(nativeKeyMapping) }

                    Mockito.`when`(nativeKeyMapping.consumeClick()).thenReturn(true, true, false)
                    val handler = inputEventsHandlers.single { it !in oldHandlers }
                    handler()
                    Assertions.assertEquals(2, clicks)
                    handler()
                    Assertions.assertEquals(2, clicks)

                    Mockito.`when`(nativeKeyMapping.matches(90, 0)).thenReturn(true)
                    Assertions.assertTrue(keyMapping.matches(90, 0))
                    Mockito.`when`(nativeKeyMapping.matches(90, 0)).thenReturn(false)
                    Mockito.`when`(nativeKeyMapping.matches(88, 0)).thenReturn(true)
                    Assertions.assertFalse(keyMapping.matches(90, 0))
                    Assertions.assertTrue(keyMapping.matches(88, 0))
                }
            }
        } finally {
            inputEventsHandlers.removeAll { it !in oldHandlers }
        }
    }

}
