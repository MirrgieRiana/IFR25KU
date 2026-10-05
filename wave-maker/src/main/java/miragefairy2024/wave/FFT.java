package miragefairy2024.wave;

public class FFT {

    private Complex[] input;
    private Complex[] output;
    private final boolean inverse;
    private int length;

    public FFT(boolean inverse) {
        this.inverse = inverse;
    }

    public void setData(Complex[] data) {
        this.length = data.length;
        this.input = data;
    }

    public Complex[] getData() {
        return this.output;
    }

    public void execute() {
        this.output = new Complex[this.length];
        if (!this.inverse) {
            fft();
        } else {
            ifft();
        }
    }

    private void fft() {
        int N = this.length;
        if (N == 1) {
            this.output = new Complex[]{this.input[0]};
        } else {
            if (N % 2 != 0) {
                throw new RuntimeException("N is not a power of 2");
            }
            Complex[] even = new Complex[N / 2];
            for (int k = 0; k < N / 2; k++) {
                even[k] = this.input[(2 * k)];
            }
            FFT fft = new FFT(false);
            fft.setData(even);
            fft.execute();
            Complex[] q = fft.getData();

            Complex[] odd = even;
            for (int k = 0; k < N / 2; k++) {
                odd[k] = this.input[(2 * k + 1)];
            }
            fft = new FFT(false);
            fft.setData(odd);
            fft.execute();
            Complex[] r = fft.getData();
            for (int k = 0; k < N / 2; k++) {
                double kth = -2 * k * 3.141592653589793D / N;
                Complex wk = new Complex(Math.cos(kth), Math.sin(kth));
                this.output[k] = q[k].plus(wk.times(r[k]));
                this.output[(k + N / 2)] = q[k].minus(wk.times(r[k]));
            }
        }
    }

    private void ifft() {
        int N = this.length;
        Complex[] val = new Complex[N];
        for (int i = 0; i < N; i++) {
            val[i] = this.input[i].conjugate();
        }
        FFT fft = new FFT(false);
        fft.setData(val);
        fft.execute();
        val = fft.getData();
        for (int i = 0; i < N; i++) {
            val[i] = val[i].conjugate();
        }
        for (int i = 0; i < N; i++) {
            this.output[i] = val[i].times(1.0D / N);
        }
    }

}
