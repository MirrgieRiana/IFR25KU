package miragefairy2024.client.ui

/** 何も描かずに、与えられた寸法だけを占めるコンポーネントなのだ～🌱 */
class UiSpacer : UiComponent()

/** 横に [width] だけの空きを作るのだ～🌱 */
fun horizontalSpace(width: Int) = UiSpacer().also { it.sizing(Sizing.fixed(width), Sizing.content()) }

/** 縦に [height] だけの空きを作るのだ～🌱 */
fun verticalSpace(height: Int) = UiSpacer().also { it.sizing(Sizing.content(), Sizing.fixed(height)) }
