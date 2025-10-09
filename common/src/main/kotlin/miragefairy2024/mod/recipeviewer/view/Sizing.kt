package miragefairy2024.mod.recipeviewer.view

import mirrg.kotlin.helium.atLeast
import mirrg.kotlin.helium.atMost

sealed class Sizing {
    abstract fun getMinSize(contentSize: Int): Int
    abstract fun getSize(contentSize: Int, regionSize: Int): Int

    data object Fill : Sizing() {
        override fun getMinSize(contentSize: Int) = 0
        override fun getSize(contentSize: Int, regionSize: Int) = regionSize
    }

    data class Max(val size: Int) : Sizing() {
        override fun getMinSize(contentSize: Int) = 0
        override fun getSize(contentSize: Int, regionSize: Int) = regionSize atMost size
    }

    data class Fixed(val size: Int) : Sizing() {
        override fun getMinSize(contentSize: Int) = size
        override fun getSize(contentSize: Int, regionSize: Int) = size
    }

    data class Min(val size: Int) : Sizing() {
        override fun getMinSize(contentSize: Int) = contentSize atLeast size
        override fun getSize(contentSize: Int, regionSize: Int) = contentSize atLeast size
    }

    data object Wrap : Sizing() {
        override fun getMinSize(contentSize: Int) = contentSize
        override fun getSize(contentSize: Int, regionSize: Int) = contentSize
    }
}
