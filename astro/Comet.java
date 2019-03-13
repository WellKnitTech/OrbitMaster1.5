/**
 * Comet Class
 */
package astro;
import astro.ATime;
import astro.Matrix;

public class Comet {
	private String	strName;
	private double	fT, fE, fQ;
	private double	fPeri, fNode, fIncl;
	private double	fEquinox;
	private ATime	atimeEquinox;
	private Matrix	mtxVC;		// Vector Constant

	private static final double TOLERANCE = 1.0e-12;
	private static final int    MAXAPPROX = 150; // 80;
	

	/**
	 * Constructor
	 */
	public Comet(String strName, double fT, double fE, double fQ,
				 double fPeri, double fNode, double fIncl,
				 double fEquinox) {
		this.strName = strName;
		this.fT = fT;
		this.fE = fE;
		this.fQ = fQ;
		this.fPeri = fPeri;
		this.fNode = fNode;
		this.fIncl = fIncl;
		this.fEquinox = fEquinox;	// ex. 2000.0
		// Equinox -> ATime
		int    nEqnxYear  = (int)Math.floor(fEquinox);
		double fEqnxMonth = (fEquinox - (double)nEqnxYear) * 12.0;
		int    nEqnxMonth = (int)Math.floor(fEqnxMonth);
		double fEqnxDay   = (fEqnxMonth - (double)nEqnxMonth) * 30.0;
		this.atimeEquinox = new ATime(nEqnxYear, nEqnxMonth, fEqnxDay, 0.0);
		// Vector Constant
		mtxVC = Matrix.VectorConstant(fPeri, fNode, fIncl, atimeEquinox);
	}

	/**
	 * Get Position on Orbital Plane for Elliptical Orbit
	 */
	private Xyz CometStatusEllip(double fJd) {
//		System.out.println("Comet.java - Perihelion is "+this.fQ);
		if (this.fQ == 0.0) {
			throw new ArithmeticException();
		}
		double fAxis = this.fQ / (1.0 - this.fE);
		double fM = Astro.GAUSS * (fJd - this.fT) / (Math.sqrt(fAxis) * fAxis);
//		System.out.println("Mean anomaly at JD "+fJd+" is "+fM*(180./Math.PI));	// (awp)
		double fE1 = fM + this.fE * Math.sin(fM);
		int nCount = MAXAPPROX;
		if (this.fE < 0.6) {
			double fE2;
			do {
				fE2 = fE1;
				fE1 = fM + this.fE * Math.sin(fE2); // -------------------------> Meeus p. 196 (Eqn 30.6)
			} while (Math.abs(fE1 - fE2) > TOLERANCE && --nCount > 0);
		} else if (this.fE < 0.98) {
			double fDv;
			do {
				double fDv1 = (fM + this.fE * Math.sin(fE1) - fE1);
				double fDv2 = (1.0 - this.fE * Math.cos(fE1));
				if (Math.abs(fDv1) < TOLERANCE || Math.abs(fDv2) < TOLERANCE) {
					break;
				}
				fDv = fDv1 / fDv2;	// -----------------------------------------> Meeus p. 199 (Eqn 30.7)
				fE1 += fDv;
			} while (Math.abs(fDv) > TOLERANCE && --nCount > 0);
		} else {					// ---> For e > 0.98, use binary search from Meeus p. 206 ("3rd Method")
			double fF = Math.signum(fM);
			fM = Math.abs(fM) / (2. * Math.PI);
			fM = (fM - Math.floor(fM)) * 2. * Math.PI * fF;
			if (fM < 0.) {
				fM = fM + (2. * Math.PI);
			}
			fF = 1.;
			if (fM > Math.PI) {
				fF = -1.;
			}
			if (fM > Math.PI) {
				fM = (2. * Math.PI) - fM;
			}
			fE1 = Math.PI/2.;
			double fD = Math.PI/4.;
			double fM1, fE2;
			do {
				fE2 = fE1;
				fM1 = fE1 - (this.fE * Math.sin(fE1));
				fE1 = fE1 + (fD * Math.signum(fM - fM1));
				fD = fD/2.;
			} while (Math.abs(fE1 - fE2) > TOLERANCE && --nCount > 0);
			fE1 = fE1 * fF;
		}
//		System.out.println("nCount  = "+nCount);
//		System.out.println("axis    = "+fAxis);
//		System.out.println("ecc.    = "+this.fE);
//		System.out.println("eccAnom = "+fE1*180./Math.PI);
		if (nCount == 0) {
			throw new ArithmeticException();
		}
		double fX = fAxis * (Math.cos(fE1) - this.fE);
		double fY = fAxis * Math.sqrt(1.0 - this.fE * this.fE) * Math.sin(fE1);
//		System.out.println(" x  = "+fX);
//		System.out.println(" y  = "+fY);
//		double xytot = Math.sqrt( Math.pow(fX,2.) + Math.pow(fY,2.) );
//		System.out.println("tot = "+xytot);
//		System.out.println("");

		return new Xyz(fX, fY, 0.0);
	}

	/**
	 * Get Position on Orbital Plane for Nearly Parabolic Orbit
	 */
	private Xyz CometStatusNearPara(double fJd) {	// --------------------> should be akin to Meeus' Chap 35
		if (this.fQ == 0.0) {
			throw new ArithmeticException();
		}

		System.out.println(""+this.fE+" : "+this.fQ+" : "+fJd+" : "+this.fT);
		double period = Math.pow(this.fQ/(1.-this.fE), 3./2.) * 365.2422;
		while (fJd - this.fT > period/2.) {
			this.fT = this.fT + period;
		}
		while (fJd - this.fT < -period/2.) {
			this.fT = this.fT - period;
		}
		double diff = fJd - this.fT;
		System.out.println("-"+period+" < "+diff+" < "+period);
		

/*		// -------------------------> Attempt at a new method (Meeus' Chap 35)
		double fR, fV;
		double fD1 = 10000.;
		double fC  = 1./3.;
		// D  = TOLERANCE	// (1.0e-12): "Adjust to suit machine precision" (starting at 1.0e-9)
		double fQ1 = Astro.GAUSS * Math.sqrt((1.+this.fE)/this.fQ)/(2.*this.fQ);
//		System.out.println("Q1 = "+fQ1);
		double fG  = (1.-this.fE)/(1.+this.fE);		// "gamma"
//		System.out.println("G  = "+fG);
		// 22: T  = (fJd - this.fT)
		
		if (fJd == this.fT) {
			// 26:
			fR = this.fQ;
			fV = 0;
			// goto 72	(printout & end)
		} else {
			int fZ;
			double fF, fG1, fQ3, fS0, fS1, fY, fZ1;
			// 28:
			double fQ2 = fQ1 * (fJd - this.fT);
//			System.out.println("Q2 = "+fQ2);
			double fS  = 2./(3.*Math.abs(fQ2));
//			System.out.println("S  = "+fS);
			fS = 2./Math.tan( 2.*Math.atan( Math.pow(Math.tan(Math.atan(fS)/2.),fC) ) );
//			System.out.println("S  = "+fS);
			if (fJd < this.fT) {
				fS = - fS;
			}
//			System.out.println(">>> S = "+fS);
			// 36:					// only nec. if we allow parabolic orbits!
			if (this.fE != 1.) {
				// 38:
				int    fL = 0;		// counting ITERATIONS on fS
				do {
					// 40:
					fS0 = fS;
					fZ  = 1;		// counting TERMS
//					System.out.println("      Z = "+fZ);
					fY  = Math.pow(fS, 2.);	// =  fS^2
//					System.out.println("Y  = "+fY);
					fG1 = - fY * fS;			// = -fS^3
//					System.out.println("G1 = "+fG1);
					// 42:
					fQ3 = fQ2 + (2. * fG * fS * fY / 3.);
//					fQ3 = fQ2 + ((2.*fG - 1.) * fS * fY / 3.);
//					System.out.println("Q3 = "+fQ3);
					do {
						// 44:
						fZ = fZ + 1;		// (counting TERMS)
//						System.out.println("      Z = "+fZ);
						// 46:
						fG1 = - fG1 * fG * fY;			// = gamma * fS^5
//						System.out.println("G1 = "+fG1);
						// 48:
						fZ1 = (fZ - (fZ+1.)*fG)/(2.*fZ + 1.);	// = (2-3gamma)/5
//						System.out.println("Z1 = "+fZ1);
						// 50:
						fF = fZ1 * fG1;
//						System.out.println("... F = "+fF);
						// 52:
						fQ3 = fQ3 + fF;
						// 54:
//						if (fZ > MAXAPPROX || Math.abs(fF) > fD1) {	// too many terms, or term too large
//							if (fZ > MAXAPPROX) {
//								System.out.println("NO CONVERGENCE! - Too many terms:  Z = "+fZ);
//							}
//							if (Math.abs(fF) > fD1) {
//								System.out.println("NO CONVERGENCE! - Term too large:  |F| = "+Math.abs(fF));
//							}
//							throw new ArithmeticException();
//						}
						// 56:
					} while (Math.abs(fF) > TOLERANCE);			// until term is "small enough"
					// 58:
					fL = fL + 1;
//					System.out.println("L = "+fL);
					if (fL > MAXAPPROX) {						// too many iterations on fS
						System.out.println("NO CONVERGENCE! - Too many iterations:  L = "+fL);
						throw new ArithmeticException();
					}
					do {
						// 60:
						fS1 = fS;
						fS  = (2. * fS * fS * fS/3. + fQ3) / (fS * fS + 1.);
						// 62:
					} while (Math.abs(fS - fS1) > TOLERANCE);	// iterate until THIS fS stops changing
//					System.out.println(">>> S = "+fS);
					// 64:
				} while (Math.abs(fS - fS0) > TOLERANCE);		// iterate until THIS fS is same as LAST fS
			} else {
				System.out.println("Parabolic orbit!");
			}
			// 66:
			fV = 2. * Math.atan(fS);
			// 68:
			fR = this.fQ * (1. + this.fE) / (1. + this.fE*Math.cos(fV));
			// 70:
			if (fV < 0.) {
				fV = fV + 2.*Math.PI;
			}
		}

		// 72:
		double trueAnomDeg = fV * 180. / Math.PI;
		System.out.println("True anomaly  = "+trueAnomDeg+" degs");
		// 74:
		System.out.println("Radius vector = "+fR+" AU");
		// 76:
//		System.out.println("");
		
		double fX = fR * Math.cos(fV);
		double fY = fR * Math.sin(fV);
		
		System.out.println(" x  = "+fX);
		System.out.println(" y  = "+fY);
		double xytot = Math.sqrt( Math.pow(fX,2.) + Math.pow(fY,2.) );
		System.out.println("tot = "+xytot);
		System.out.println("");
		
		return new Xyz(fX, fY, 0.0);
*/
/**/	// -------------------------> This was the old method.  Similar to Meeus' Chap 35, but cryptic!
		double fA = Math.sqrt((1.0 + 9.0 * this.fE) / 10.0);
		double fB = 5.0 * (1 - this.fE) / (1.0 + 9.0 * this.fE);
		double fA1, fB1, fX1, fA0, fB0, fX0, fN;
		fA1 = fB1 = fX1 = 1.0;
		int nCount1 = MAXAPPROX;
		
		do {
			fA0 = fA1;
			fB0 = fB1;
			fN = fB0 * fA * Astro.GAUSS * (fJd - this.fT)
				/ (Math.sqrt(2.0) * this.fQ * Math.sqrt(this.fQ));	// ----> similar to Meeus p. 241 (Eqn 34.1)
			int nCount2 = MAXAPPROX;
			do {
				fX0 = fX1;
				double fTmp = fX0 * fX0;
				fX1 = (fTmp * fX0 * 2.0 / 3.0 + fN) / (1.0 + fTmp);
//				System.out.println("nCount2="+nCount2+": |fX1 - fX0|="+Math.abs(fX1 - fX0));
			} while (Math.abs(fX1 - fX0) > TOLERANCE && --nCount2 > 0);
			if (nCount2 == 0) {
				throw new ArithmeticException();
			}
			fA1 = fB * fX1 * fX1;
			fB1 = (-3.809524e-03 * fA1 - 0.017142857) * fA1 * fA1 + 1.0;
//			System.out.println("nCount1="+nCount1+": |fA1 - fA0|="+Math.abs(fA1 - fA0));
		} while (Math.abs(fA1 - fA0) > TOLERANCE && --nCount1 > 0);
//		System.out.println("nCount1="+nCount1+": |fA1 - fA0|="+Math.abs(fA1 - fA0));
		if (nCount1 == 0) {
			throw new ArithmeticException();
		}
		double fC1 = ((0.12495238 * fA1 + 0.21714286) * fA1 + 0.4) * fA1 + 1.0;
//		double fD1 = ((0.00571429 * fA1 + 0.2       ) * fA1 - 1.0) * fA1 + 1.0;
		double fTanV2 = Math.sqrt(5.0 * (1.0 + this.fE)
				  / (1.0 + 9.0 * this.fE)) * fC1 * fX1;		// --------> tan(v/2) from Meeus p. 245 (Chap 35)
//		double fX = this.fQ * fD1 * (1.0 - fTanV2 * fTanV2);
//		double fY = 2.0 * this.fQ * fD1 * fTanV2;
		
		double fV = 2. * Math.atan(fTanV2);
		double trueAnomDeg = fV * 180. / Math.PI;
		double fR = this.fQ * (1. + this.fE) / (1. + this.fE*Math.cos(fV));
		double fX = fR * Math.cos(fV);
		double fY = fR * Math.sin(fV);
		
		System.out.println("True anomaly  = "+trueAnomDeg+" degs");
		System.out.println("Radius vector = "+fR+" AU");
//		System.out.println(" x  = "+fX);
//		System.out.println(" y  = "+fY);
//		double xytot = Math.sqrt( Math.pow(fX,2.) + Math.pow(fY,2.) );
//		System.out.println("tot = "+xytot);
		System.out.println("");
		
		return new Xyz(fX, fY, 0.0);

	}

	/**
	 * Get Position on Orbital Plane for Parabolic Orbit
	 */
	private Xyz CometStatusPara(double fJd) {
		if (this.fQ == 0.0) {
			throw new ArithmeticException();
		}
		double fN = Astro.GAUSS * (fJd - this.fT)
			/ (Math.sqrt(2.0) * this.fQ * Math.sqrt(this.fQ));	// ---------> 1/3 of Meeus p. 241 (Eqn 34.1)
		double fTanV2 = fN;
		double fOldTanV2, fTan2V2;
		int nCount = MAXAPPROX;
		do {
			fOldTanV2 = fTanV2;
			fTan2V2 = fTanV2 * fTanV2;
			fTanV2 = (fTan2V2 * fTanV2 * 2.0 / 3.0 + fN) / (1.0 + fTan2V2); // ----> Meeus p. 242 (Eqn 34.4)
		} while (Math.abs(fTanV2 - fOldTanV2) > TOLERANCE && --nCount > 0);
		if (nCount == 0) {
			throw new ArithmeticException();
		}
		fTan2V2 = fTanV2 * fTanV2;
		double fX = this.fQ * (1.0 - fTan2V2);
		double fY = 2.0 * this.fQ * fTanV2;

		return new Xyz(fX, fY, 0.0);
	}

	/**
	 * Get Position in Heliocentric Equatorial Coordinates 2000.0
	 */
	public Xyz GetPos(double fJd) {
		Xyz xyz;
		// CometStatuses may throw ArithmeticException
/*
		if (this.fE < 0.98) {
			xyz = CometStatusEllip(fJd);
		} else if (Math.abs(this.fE - 1.0) < TOLERANCE) {
			xyz = CometStatusPara(fJd);
		} else {
			xyz = CometStatusNearPara(fJd);
		}
*/

		if (Math.abs(this.fE - 1.0) < TOLERANCE) {
			xyz = CometStatusPara(fJd);
		} else {
			xyz = CometStatusEllip(fJd);
		}

		xyz = xyz.Rotate(mtxVC);
		Matrix mtxPrec = Matrix.PrecMatrix(this.atimeEquinox.getJd(),
										   Astro.JD2000);
		
		return xyz.Rotate(mtxPrec);
	}

	/**
	 * Get GIVEN-XYZ Position in Heliocentric Equatorial Coordinates 2000.0
	 */
	public Xyz GetPos2(Xyz xyz) {
		xyz = xyz.Rotate(mtxVC);
		Matrix mtxPrec = Matrix.PrecMatrix(this.atimeEquinox.getJd(), Astro.JD2000);
		return xyz.Rotate(mtxPrec);
	}

	/**
	 * Get Internal Variables
	 */
	public String getName() {
		return this.strName;
	}
	public double getT() {
		return this.fT;
	}
	public double getE() {
		return this.fE;
	}
	public double getQ() {
		return this.fQ;
	}
	public double getPeri() {
		return this.fPeri;
	}
	public double getNode() {
		return this.fNode;
	}
	public double getIncl() {
		return this.fIncl;
	}
	public double getEquinox() {
		return this.fEquinox;
	}
	public double getEquinoxJd() {
		return this.atimeEquinox.getJd();
	}
	public Matrix getVectorConstant() {
		return this.mtxVC;
	}
	public double getA() {
		return this.fQ / (1.0 - this.fE);						// (awp)
	}
	public double getM(ATime atime) {							// (awp)
		double n = Astro.GAUSS / (getA() * Math.sqrt(getA()));
		double M = n * (atime.getJd() - this.fT);
/*
		while (M > 360.*(Math.PI/180.)) {
			M = M - (2.*Math.PI);
		}
		while (M < 0.*(Math.PI/180.)) {
			M = M + (2.*Math.PI);
		}
*/
		return M;											// (awp)
	}
	
	/**
	 * Set Internal Variables (awp)
	 */
	public void setA(double a2Set, ATime atime) {
		double nOld = Astro.GAUSS / (getA() * Math.sqrt(getA()));
		double nNew = Astro.GAUSS / (a2Set * Math.sqrt(a2Set));
		this.fT = this.fT + (getM(atime)/nOld) - (getM(atime)/nNew);	// Mean anomaly is temporarily wrong...
		this.fQ = a2Set * (1.0 - this.fE);								// ...but is now corrected (w/in 10^-12)
	}
	public void setE(double e2Set) {
		double constantA = getA();
		this.fE = e2Set;							// Semimajor axis is temporarily wrong...
		this.fQ = constantA * (1.0 - this.fE);		// ...but is now reset to correct value.
	}
	public void setIncl(double incl2Set) {
		this.fIncl = incl2Set;
		this.mtxVC = Matrix.VectorConstant(this.fPeri, this.fNode, this.fIncl, this.atimeEquinox);
	}
	public void setNode(double node2Set) {
		this.fNode = node2Set;
		this.mtxVC = Matrix.VectorConstant(this.fPeri, this.fNode, this.fIncl, this.atimeEquinox);
	}
	public void setPeri(double peri2Set) {
		this.fPeri = peri2Set;
		this.mtxVC = Matrix.VectorConstant(this.fPeri, this.fNode, this.fIncl, this.atimeEquinox);
	}
	public void setM(double m2Set, ATime atime) {
		double n = Astro.GAUSS / (getA() * Math.sqrt(getA()));
		double deltaM = getM(atime) - m2Set;
		while (deltaM >=  1.*Math.PI) deltaM = deltaM - (2.*Math.PI);
		while (deltaM <= -1.*Math.PI) deltaM = deltaM + (2.*Math.PI);
		this.fT = this.fT + (deltaM/n);
	}

}
