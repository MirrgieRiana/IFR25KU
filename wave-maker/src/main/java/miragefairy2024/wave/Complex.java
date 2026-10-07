package miragefairy2024.wave;

public class Complex {

    public final double re;
    public final double im;

    public Complex(double re, double im) {
        this.re = re;
        this.im = im;
    }

    public double abs() {
        return Math.hypot(this.re, this.im);
    }

    public double phase() {
        return Math.atan2(this.im, this.re);
    }

    public Complex conjugate() {
        return new Complex(this.re, -this.im);
    }

    public Complex reciprocal() {
        double scale = this.re * this.re + this.im * this.im;
        return new Complex(this.re / scale, -this.im / scale);
    }

    public Complex plus(Complex b) {
        Complex a = this;
        double real = a.re + b.re;
        double imag = a.im + b.im;
        return new Complex(real, imag);
    }

    public Complex minus(Complex b) {
        Complex a = this;
        double real = a.re - b.re;
        double imag = a.im - b.im;
        return new Complex(real, imag);
    }

    public Complex times(Complex b) {
        Complex a = this;
        double real = a.re * b.re - a.im * b.im;
        double imag = a.re * b.im + a.im * b.re;
        return new Complex(real, imag);
    }

    public Complex times(double alpha) {
        return new Complex(alpha * this.re, alpha * this.im);
    }

    public Complex divides(Complex b) {
        Complex a = this;
        return a.times(b.reciprocal());
    }

    public int hashCode() {
        int result = 1;

        long temp = Double.doubleToLongBits(this.im);
        result = 31 * result + (int) (temp ^ temp >>> 32);
        temp = Double.doubleToLongBits(this.re);
        result = 31 * result + (int) (temp ^ temp >>> 32);
        return result;
    }

    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        Complex other = (Complex) obj;
        if (Double.doubleToLongBits(this.im) != Double.doubleToLongBits(other.im)) return false;
        return Double.doubleToLongBits(this.re) == Double.doubleToLongBits(other.re);
    }

}
