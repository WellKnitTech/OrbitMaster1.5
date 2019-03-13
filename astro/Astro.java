/**
 * Astronomical Constants
 */
package astro;

public class Astro {
	public static final double GAUSS  = 0.01720209895;
	/*  Paraphrased from Wikipedia ("Gaussian gravitational constant"):
	 *  Gauss expressed the gravitational constant in solar system units.
	 *  Does not require exact knowledge of scale or masses of the solar system.
	 *  Gauss used the following units:
	 *		- length A: astronomical unit.
	 *		- time D: mean solar day.
	 *		- mass S: the mass of the Sun.
	 *	From Kepler's 3rd law applied to the motion of the Earth, he derived:
	 *		k = 0.01720209895 A^(3/2) S^(-1/2) D^(-1).
	 */
	
	public static final double JD2000 = 2451545.0;	// 2000.1.1 12h ET
	public static final double JD1900 = 2415021.0;	// 1900.1.1 12h ET
	
	public static final double GM = 132712440018.;   // km3/s2
}
