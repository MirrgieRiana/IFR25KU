package miragefairy2024.mod.recipeviewer.views

import miragefairy2024.mod.recipeviewer.view.ViewTexture
import miragefairy2024.mod.recipeviewer.view.size

/** 切り取る領域は、描画の側が毎フレーム読むのだ～🌱 だから、寸法を変えずに見える幅だけを変えられるのだ～🌱 */
class ImageView(var texture: ViewTexture) : FixedWidgetView(texture.bounds.size)
