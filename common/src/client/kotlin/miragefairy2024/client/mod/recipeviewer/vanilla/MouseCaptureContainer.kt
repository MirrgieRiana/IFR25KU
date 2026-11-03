package miragefairy2024.client.mod.recipeviewer.vanilla

/** 押下を受け付けた要素へ、領域外でもドラッグと解放を届けるためのインターフェースなのだ～🌱 */
interface MouseCaptureContainer {
    /** 子孫のマウスキャプチャも含めて判定しなければならないのだ～🌱 */
    fun hasMouseCapture(button: Int): Boolean
}
