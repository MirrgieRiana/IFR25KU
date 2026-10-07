package miragefairy2024.wave

fun Array<Complex>.fft(): Array<Complex> {
    val fft = FFT(false)
    fft.data = this
    fft.execute()
    return fft.data
}

fun Array<Complex>.ifft(): Array<Complex> {
    val fft = FFT(true)
    fft.data = this
    fft.execute()
    return fft.data
}
