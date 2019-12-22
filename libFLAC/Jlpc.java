package libFLAC;

final class Jlpc {
	/** 0.69314718055994530942 */
	private static final double M_LN2 = Math.log( 2.0 );// 0.69314718055994530942

	// private static final boolean DEBUG = false;
	// private static final boolean FLAC__OVERFLOW_DETECT = false;
	// private static final boolean FLAC__OVERFLOW_DETECT_VERBOSE = false;
	// private static final boolean FLAC__LPC_UNROLLED_FILTER_LOOPS = true;


	// bitmath.c
	/* private static int FLAC__bitmath_silog2_wide(long v)
	{
		while( true ) {
			if( v == 0 ) {
				return 0;
			}
			else if( v > 0 ) {
				int l = 0;
				while( v != 0 ) {
					l++;
					v >>= 1;
				}
				return l + 1;
			}
			else if( v == -1 ) {
				return 2;
			}
			else {
				v++;
				v = -v;
			}
		}
	} */
	// end bitmath.c

	static void FLAC__lpc_window_data(final int in[], final float window[], final float out[], final int data_len)
	{
		for( int i = 0; i < data_len; i++ ) {
			out[i] = in[i] * window[i];
		}
	}

	static void FLAC__lpc_compute_autocorrelation(final float data[], final int data_len, final int lag, final float autoc[])
	{
		/* a readable, but slower, version */
/*#if 0
		float d;
		int i;
*/
		//FLAC__ASSERT(lag > 0);
		//FLAC__ASSERT(lag <= data_len);

		/*
		 * Technically we should subtract the mean first like so:
		 *   for(i = 0; i < data_len; i++)
		 *     data[i] -= mean;
		 * but it appears not to make enough of a difference to matter, and
		 * most signals are already closely centered around zero
		 */
/*		while( lag-- != 0 ) {
			for( i = lag, d = 0.0f; i < data_len; i++ )
				d += data[i] * data[i - lag];
			autoc[lag] = d;
		}
#endif
*/
		/*
		 * this version tends to run faster because of better data locality
		 * ('data_len' is usually much larger than 'lag')
		 */
		int sample;
		final int limit = data_len - lag;

		//FLAC__ASSERT(lag > 0);
		//FLAC__ASSERT(lag <= data_len);

		for( int coeff = 0; coeff < lag; coeff++ ) {
			autoc[coeff] = 0.0f;
		}
		for( sample = 0; sample <= limit; sample++ ) {
			final float d = data[sample];
			for( int coeff = 0; coeff < lag; coeff++ ) {
				autoc[coeff] += d * data[sample + coeff];
			}
		}
		for( ; sample < data_len; sample++ ) {
			final float d = data[sample];
			for( int coeff = 0; coeff < data_len - sample; coeff++ ) {
				autoc[coeff] += d * data[sample + coeff];
			}
		}
	}

	/** @return max_order */
	static int FLAC__lpc_compute_lp_coefficients(final float autoc[], final int max_order, final float lp_coeff[][]/*[Jformat.FLAC__MAX_LPC_ORDER]*/, final double error[])
	{
		final double lpc[] = new double[Jformat.FLAC__MAX_LPC_ORDER];

		//FLAC__ASSERT(0 != max_order);
		//FLAC__ASSERT(0 < *max_order);
		//FLAC__ASSERT(*max_order <= FLAC__MAX_LPC_ORDER);
		//FLAC__ASSERT(autoc[0] != 0.0);

		double err = autoc[0];

		for( int i = 0; i < max_order; i++ ) {
			/* Sum up this iteration's reflection coefficient. */
			double r = -autoc[i + 1];
			for( int j = 0; j < i; j++ ) {
				r -= lpc[j] * autoc[i - j];
			}
			r /= err;

			/* Update LPC coefficients and total error. */
			lpc[i] = r;
			int j = 0;
			for( ; j < (i >> 1); j++ ) {
				final double tmp = lpc[j];
				lpc[j] += r * lpc[i - 1 - j];
				lpc[i - 1 - j] += r * tmp;
			}
			if( (i & 1) != 0 ) {
				lpc[j] += lpc[j] * r;
			}

			err *= (1.0 - r * r);

			/* save this order */
			for( j = 0; j <= i; j++ ) {
				lp_coeff[i][j] = (float)(-lpc[j]); /* negate FIR filter coeff to get predictor coeff */
			}
			error[i] = err;

			/* see SF bug https://sourceforge.net/p/flac/bugs/234/ */
			if( err == 0.0 ) {
				return i + i;//max_order[0] = i + 1;
				//return;
			}
		}
		return max_order;
	}

	// java: changed shift is returned. non zero status changed to negative numbers
	static int FLAC__lpc_quantize_coefficients(final float lp_coeff[], final int order, int precision, final int qlp_coeff[]/*, int[] shift*/)
	{

		//FLAC__ASSERT(precision > 0);
		//FLAC__ASSERT(precision >= FLAC__MIN_QLP_COEFF_PRECISION);

		/* drop one bit for the sign; from here on out we consider only |lp_coeff[i]| */
		precision--;
		int qmax = 1 << precision;
		final int qmin = -qmax;
		qmax--;

		/* calc cmax = max( |lp_coeff[i]| ) */
		float cmax = 0.0f;// FIXME why double?
		for( int i = 0; i < order; i++ ) {
			final float d = Math.abs( lp_coeff[i] );
			if( d > cmax ) {
				cmax = d;
			}
		}

		if( cmax <= 0.0f ) {
			/* => coefficients are all 0, which means our constant-detect didn't work */
			return -2;// return 2;
		}
		// else {
			final int max_shiftlimit = (1 << (Jformat.FLAC__SUBFRAME_LPC_QLP_SHIFT_LEN - 1)) - 1;
			final int min_shiftlimit = -max_shiftlimit - 1;
			int log2cmax = Math.getExponent( cmax );// TODO check java equivalent for (void)frexp(cmax, &log2cmax);
			log2cmax--;
			int shift = precision - log2cmax - 1;

			if( shift > max_shiftlimit ) {
				shift = max_shiftlimit;
			} else if( shift < min_shiftlimit ) {
				return -1;// return 1;
			}
		//}

		if( shift >= 0 ) {
			float error = 0.0f;// FIXME why double?
			int q;
			for( int i = 0; i < order; i++ ) {
				error += lp_coeff[i] * (1 << shift);
				q = Math.round( error );
/* if( FLAC__OVERFLOW_DETECT ) {
				if( q > qmax + 1 ) {
					System.err.printf("FLAC__lpc_quantize_coefficients: quantizer overflow: q>qmax %d>%d shift=%d cmax=%f precision=%d lpc[%u]=%f\n",q,qmax,shift,cmax,precision+1,i,lp_coeff[i]);
				} else if(q < qmin) {
					System.err.printf("FLAC__lpc_quantize_coefficients: quantizer overflow: q<qmin %d<%d shift=%d cmax=%f precision=%d lpc[%u]=%f\n",q,qmin,shift,cmax,precision+1,i,lp_coeff[i]);
				}
} */
				if( q > qmax ) {
					q = qmax;
				} else if( q < qmin ) {
					q = qmin;
				}
				error -= q;
				qlp_coeff[i] = q;
			}
		}
		/* negative shift is very rare but due to design flaw, negative shift is
		 * not allowed in the decoder, so it must be handled specially by scaling
		 * down coeffs
		 */
		else {
			final int nshift = -shift;
			float error = 0.0f;// FIXME why double?

/* if( DEBUG ) {
			System.err.printf("FLAC__lpc_quantize_coefficients: negative shift=%d order=%d cmax=%f\n", shift, order, cmax);
} */
			for( int i = 0; i < order; i++ ) {
				error += lp_coeff[i] / (1 << nshift);
				int q = Math.round( error );
/* if( FLAC__OVERFLOW_DETECT ) {
				if( q > qmax + 1 ) {
					System.err.printf("FLAC__lpc_quantize_coefficients: quantizer overflow: q>qmax %d>%d shift=%d cmax=%f precision=%d lpc[%u]=%f\n",q,qmax,shift,cmax,precision+1,i,lp_coeff[i]);
				} else if( q < qmin ) {
					System.err.printf("FLAC__lpc_quantize_coefficients: quantizer overflow: q<qmin %d<%d shift=%d cmax=%f precision=%d lpc[%u]=%f\n",q,qmin,shift,cmax,precision+1,i,lp_coeff[i]);
				}
} */
				if( q > qmax ) {
					q = qmax;
				} else if( q < qmin ) {
					q = qmin;
				}
				error -= q;
				qlp_coeff[i] = q;
			}
			shift = 0;
		}

		return shift;//return 0;
	}

	static void FLAC__lpc_compute_residual_from_qlp_coefficients(final int data[], final int data_len, final int qlp_coeff[], int order, final int lp_quantization, final int residual[])
	{// java: uses order as offset to data
/* if( FLAC__OVERFLOW_DETECT || ! FLAC__LPC_UNROLLED_FILTER_LOOPS ) {
		int r = 0;// offset to residual
		int idata = order;// offset to data[]

if( FLAC__OVERFLOW_DETECT_VERBOSE ) {
		System.err.printf("FLAC__lpc_compute_residual_from_qlp_coefficients: data_len=%d, order=%d, lpq=%d",data_len,order,lp_quantization);
		for( int i = 0; i < order; i++ ) {
			System.err.printf(", q[%d]=%d",i,qlp_coeff[i]);
		}
		System.err.print("\n");
}
		//FLAC__ASSERT(order > 0);

		for( int i = 0; i < data_len; i++ ) {
			long sumo = 0;
			int sum = 0;
			int history = idata;// offset to data;
			for( int j = 0; j < order; j++ ) {
				sum += qlp_coeff[j] * data[--history];
				sumo += (long)qlp_coeff[j] * (long)data[history];
				if( sumo > 2147483647L || sumo < -2147483648L ) {
					System.err.printf("FLAC__lpc_compute_residual_from_qlp_coefficients: OVERFLOW, i=%d, j=%d, c=%d, d=%d, sumo=%d\n",i,j,qlp_coeff[j],data[history],sumo);
				}
			}
			residual[r++] = data[idata++] - (sum >> lp_quantization);
		}

		// Here's a slower but clearer version:
		// for(i = 0; i < data_len; i++) {
		//	sum = 0;
		//	for(j = 0; j < order; j++)
		//		sum += qlp_coeff[j] * data[i-j-1];
		//	residual[i] = data[i] - (sum >> lp_quantization);
		//}
	}
else *//* fully unrolled version for normal use */
	{
		//FLAC__ASSERT(order > 0);
		//FLAC__ASSERT(order <= 32);

		/*
		 * We do unique versions up to 12th order since that's the subset limit.
		 * Also they are roughly ordered to match frequency of occurrence to
		 * minimize branching.
		 */
		if( order <= 12 ) {
			if( order > 8 ) {
				if( order > 10 ) {
					if( order == 12 ) {
						for( int i = 0; i < data_len; i++, order++ ) {
							int sum = 0;
							sum += qlp_coeff[11] * data[order-12];
							sum += qlp_coeff[10] * data[order-11];
							sum += qlp_coeff[9] * data[order-10];
							sum += qlp_coeff[8] * data[order-9];
							sum += qlp_coeff[7] * data[order-8];
							sum += qlp_coeff[6] * data[order-7];
							sum += qlp_coeff[5] * data[order-6];
							sum += qlp_coeff[4] * data[order-5];
							sum += qlp_coeff[3] * data[order-4];
							sum += qlp_coeff[2] * data[order-3];
							sum += qlp_coeff[1] * data[order-2];
							sum += qlp_coeff[0] * data[order-1];
							residual[i] = data[order] - (sum >> lp_quantization);
						}
					}
					else { /* order == 11 */
						for( int i = 0; i < data_len; i++, order++ ) {
							int sum = 0;
							sum += qlp_coeff[10] * data[order-11];
							sum += qlp_coeff[9] * data[order-10];
							sum += qlp_coeff[8] * data[order-9];
							sum += qlp_coeff[7] * data[order-8];
							sum += qlp_coeff[6] * data[order-7];
							sum += qlp_coeff[5] * data[order-6];
							sum += qlp_coeff[4] * data[order-5];
							sum += qlp_coeff[3] * data[order-4];
							sum += qlp_coeff[2] * data[order-3];
							sum += qlp_coeff[1] * data[order-2];
							sum += qlp_coeff[0] * data[order-1];
							residual[i] = data[order] - (sum >> lp_quantization);
						}
					}
				}
				else {
					if( order == 10 ) {
						for( int i = 0; i < data_len; i++, order++ ) {
							int sum = 0;
							sum += qlp_coeff[9] * data[order-10];
							sum += qlp_coeff[8] * data[order-9];
							sum += qlp_coeff[7] * data[order-8];
							sum += qlp_coeff[6] * data[order-7];
							sum += qlp_coeff[5] * data[order-6];
							sum += qlp_coeff[4] * data[order-5];
							sum += qlp_coeff[3] * data[order-4];
							sum += qlp_coeff[2] * data[order-3];
							sum += qlp_coeff[1] * data[order-2];
							sum += qlp_coeff[0] * data[order-1];
							residual[i] = data[order] - (sum >> lp_quantization);
						}
					}
					else { /* order == 9 */
						for( int i = 0; i < data_len; i++, order++ ) {
							int sum = 0;
							sum += qlp_coeff[8] * data[order-9];
							sum += qlp_coeff[7] * data[order-8];
							sum += qlp_coeff[6] * data[order-7];
							sum += qlp_coeff[5] * data[order-6];
							sum += qlp_coeff[4] * data[order-5];
							sum += qlp_coeff[3] * data[order-4];
							sum += qlp_coeff[2] * data[order-3];
							sum += qlp_coeff[1] * data[order-2];
							sum += qlp_coeff[0] * data[order-1];
							residual[i] = data[order] - (sum >> lp_quantization);
						}
					}
				}
			}
			else if( order > 4 ) {
				if( order > 6 ) {
					if( order == 8 ) {
						for( int i = 0; i < data_len; i++, order++ ) {
							int sum = 0;
							sum += qlp_coeff[7] * data[order-8];
							sum += qlp_coeff[6] * data[order-7];
							sum += qlp_coeff[5] * data[order-6];
							sum += qlp_coeff[4] * data[order-5];
							sum += qlp_coeff[3] * data[order-4];
							sum += qlp_coeff[2] * data[order-3];
							sum += qlp_coeff[1] * data[order-2];
							sum += qlp_coeff[0] * data[order-1];
							residual[i] = data[order] - (sum >> lp_quantization);
						}
					}
					else { /* order == 7 */
						for( int i = 0; i < data_len; i++, order++ ) {
							int sum = 0;
							sum += qlp_coeff[6] * data[order-7];
							sum += qlp_coeff[5] * data[order-6];
							sum += qlp_coeff[4] * data[order-5];
							sum += qlp_coeff[3] * data[order-4];
							sum += qlp_coeff[2] * data[order-3];
							sum += qlp_coeff[1] * data[order-2];
							sum += qlp_coeff[0] * data[order-1];
							residual[i] = data[order] - (sum >> lp_quantization);
						}
					}
				}
				else {
					if( order == 6 ) {
						for( int i = 0; i < data_len; i++, order++ ) {
							int sum = 0;
							sum += qlp_coeff[5] * data[order-6];
							sum += qlp_coeff[4] * data[order-5];
							sum += qlp_coeff[3] * data[order-4];
							sum += qlp_coeff[2] * data[order-3];
							sum += qlp_coeff[1] * data[order-2];
							sum += qlp_coeff[0] * data[order-1];
							residual[i] = data[order] - (sum >> lp_quantization);
						}
					}
					else { /* order == 5 */
						for( int i = 0; i < data_len; i++, order++ ) {
							int sum = 0;
							sum += qlp_coeff[4] * data[order-5];
							sum += qlp_coeff[3] * data[order-4];
							sum += qlp_coeff[2] * data[order-3];
							sum += qlp_coeff[1] * data[order-2];
							sum += qlp_coeff[0] * data[order-1];
							residual[i] = data[order] - (sum >> lp_quantization);
						}
					}
				}
			}
			else {
				if( order > 2 ) {
					if( order == 4 ) {
						for( int i = 0; i < data_len; i++, order++ ) {
							int sum = 0;
							sum += qlp_coeff[3] * data[order-4];
							sum += qlp_coeff[2] * data[order-3];
							sum += qlp_coeff[1] * data[order-2];
							sum += qlp_coeff[0] * data[order-1];
							residual[i] = data[order] - (sum >> lp_quantization);
						}
					}
					else { /* order == 3 */
						for( int i = 0; i < data_len; i++, order++ ) {
							int sum = 0;
							sum += qlp_coeff[2] * data[order-3];
							sum += qlp_coeff[1] * data[order-2];
							sum += qlp_coeff[0] * data[order-1];
							residual[i] = data[order] - (sum >> lp_quantization);
						}
					}
				}
				else {
					if( order == 2 ) {
						for( int i = 0; i < data_len; i++, order++ ) {
							int sum = 0;
							sum += qlp_coeff[1] * data[order-2];
							sum += qlp_coeff[0] * data[order-1];
							residual[i] = data[order] - (sum >> lp_quantization);
						}
					}
					else { /* order == 1 */
						for( int i = 0; i < data_len; i++, order++ ) {
							residual[i] = data[order] - ((qlp_coeff[0] * data[order-1]) >> lp_quantization);
						}
					}
				}
			}
		}
		else { /* order > 12 */
			int di = order;
			for( int i = 0; i < data_len; i++, di++ ) {
				int sum = 0;
				switch( order ) {
					case 32: sum += qlp_coeff[31] * data[di-32];
					case 31: sum += qlp_coeff[30] * data[di-31];
					case 30: sum += qlp_coeff[29] * data[di-30];
					case 29: sum += qlp_coeff[28] * data[di-29];
					case 28: sum += qlp_coeff[27] * data[di-28];
					case 27: sum += qlp_coeff[26] * data[di-27];
					case 26: sum += qlp_coeff[25] * data[di-26];
					case 25: sum += qlp_coeff[24] * data[di-25];
					case 24: sum += qlp_coeff[23] * data[di-24];
					case 23: sum += qlp_coeff[22] * data[di-23];
					case 22: sum += qlp_coeff[21] * data[di-22];
					case 21: sum += qlp_coeff[20] * data[di-21];
					case 20: sum += qlp_coeff[19] * data[di-20];
					case 19: sum += qlp_coeff[18] * data[di-19];
					case 18: sum += qlp_coeff[17] * data[di-18];
					case 17: sum += qlp_coeff[16] * data[di-17];
					case 16: sum += qlp_coeff[15] * data[di-16];
					case 15: sum += qlp_coeff[14] * data[di-15];
					case 14: sum += qlp_coeff[13] * data[di-14];
					case 13: sum += qlp_coeff[12] * data[di-13];
					         sum += qlp_coeff[11] * data[di-12];
					         sum += qlp_coeff[10] * data[di-11];
					         sum += qlp_coeff[ 9] * data[di-10];
					         sum += qlp_coeff[ 8] * data[di- 9];
					         sum += qlp_coeff[ 7] * data[di- 8];
					         sum += qlp_coeff[ 6] * data[di- 7];
					         sum += qlp_coeff[ 5] * data[di- 6];
					         sum += qlp_coeff[ 4] * data[di- 5];
					         sum += qlp_coeff[ 3] * data[di- 4];
					         sum += qlp_coeff[ 2] * data[di- 3];
					         sum += qlp_coeff[ 1] * data[di- 2];
					         sum += qlp_coeff[ 0] * data[di- 1];
				}
				residual[i] = data[di] - (sum >> lp_quantization);
			}
		}
}
	}

	static void FLAC__lpc_compute_residual_from_qlp_coefficients_wide(final int[] data, final int data_len, final int qlp_coeff[], int order, final int lp_quantization, final int residual[])
	{
/* if( FLAC__OVERFLOW_DETECT || ! FLAC__LPC_UNROLLED_FILTER_LOOPS ) {
		int r = 0;
		int idata = order;

if( FLAC__OVERFLOW_DETECT_VERBOSE ) {
		System.err.printf("FLAC__lpc_compute_residual_from_qlp_coefficients_wide: data_len=%d, order=%d, lpq=%d",data_len,order,lp_quantization);
		for( int i = 0; i < order; i++ ) {
			System.err.printf(", q[%u]=%d",i,qlp_coeff[i]);
		}
		System.err.print("\n");
}
		//FLAC__ASSERT(order > 0);

		for( int i = 0; i < data_len; i++ ) {
			long sum = 0;
			int history = idata;
			for( int j = 0; j < order; j++ ) {
				sum += (long)qlp_coeff[j] * (long)data[--history];
			}
			if( FLAC__bitmath_silog2( sum >> lp_quantization ) > 32 ) {
				System.err.printf("FLAC__lpc_compute_residual_from_qlp_coefficients_wide: OVERFLOW, i=%d, sum=%d\n", i, (sum >> lp_quantization));
				break;
			}
			if( FLAC__bitmath_silog2( (long)data[idata] - (sum >> lp_quantization) ) > 32 ) {
				System.err.printf("FLAC__lpc_compute_residual_from_qlp_coefficients_wide: OVERFLOW, i=%d, data=%d, sum=%d, residual=%d\n", i, data[idata], (sum >> lp_quantization), ((long)data[idata] - (sum >> lp_quantization)));
				break;
			}
			residual[r++] = data[idata++] - (int)(sum >> lp_quantization);
		}
}
else *//* fully unrolled version for normal use */
	{
		//FLAC__ASSERT(order > 0);
		//FLAC__ASSERT(order <= 32);

		/*
		 * We do unique versions up to 12th order since that's the subset limit.
		 * Also they are roughly ordered to match frequency of occurrence to
		 * minimize branching.
		 */
		if( order <= 12 ) {
			if( order > 8 ) {
				if( order > 10 ) {
					if( order == 12 ) {
						for( int i = 0; i < data_len; i++, order++ ) {
							long sum = 0;
							sum += qlp_coeff[11] * (long)data[order-12];
							sum += qlp_coeff[10] * (long)data[order-11];
							sum += qlp_coeff[9] * (long)data[order-10];
							sum += qlp_coeff[8] * (long)data[order-9];
							sum += qlp_coeff[7] * (long)data[order-8];
							sum += qlp_coeff[6] * (long)data[order-7];
							sum += qlp_coeff[5] * (long)data[order-6];
							sum += qlp_coeff[4] * (long)data[order-5];
							sum += qlp_coeff[3] * (long)data[order-4];
							sum += qlp_coeff[2] * (long)data[order-3];
							sum += qlp_coeff[1] * (long)data[order-2];
							sum += qlp_coeff[0] * (long)data[order-1];
							residual[i] = data[order] - (int)(sum >> lp_quantization);
						}
					}
					else { /* order == 11 */
						for( int i = 0; i < data_len; i++, order++ ) {
							long sum = 0;
							sum += qlp_coeff[10] * (long)data[order-11];
							sum += qlp_coeff[9] * (long)data[order-10];
							sum += qlp_coeff[8] * (long)data[order-9];
							sum += qlp_coeff[7] * (long)data[order-8];
							sum += qlp_coeff[6] * (long)data[order-7];
							sum += qlp_coeff[5] * (long)data[order-6];
							sum += qlp_coeff[4] * (long)data[order-5];
							sum += qlp_coeff[3] * (long)data[order-4];
							sum += qlp_coeff[2] * (long)data[order-3];
							sum += qlp_coeff[1] * (long)data[order-2];
							sum += qlp_coeff[0] * (long)data[order-1];
							residual[i] = data[order] - (int)(sum >> lp_quantization);
						}
					}
				}
				else {
					if( order == 10 ) {
						for( int i = 0; i < data_len; i++, order++ ) {
							long sum = 0;
							sum += qlp_coeff[9] * (long)data[order-10];
							sum += qlp_coeff[8] * (long)data[order-9];
							sum += qlp_coeff[7] * (long)data[order-8];
							sum += qlp_coeff[6] * (long)data[order-7];
							sum += qlp_coeff[5] * (long)data[order-6];
							sum += qlp_coeff[4] * (long)data[order-5];
							sum += qlp_coeff[3] * (long)data[order-4];
							sum += qlp_coeff[2] * (long)data[order-3];
							sum += qlp_coeff[1] * (long)data[order-2];
							sum += qlp_coeff[0] * (long)data[order-1];
							residual[i] = data[order] - (int)(sum >> lp_quantization);
						}
					}
					else { /* order == 9 */
						for( int i = 0; i < data_len; i++, order++ ) {
							long sum = 0;
							sum += qlp_coeff[8] * (long)data[order-9];
							sum += qlp_coeff[7] * (long)data[order-8];
							sum += qlp_coeff[6] * (long)data[order-7];
							sum += qlp_coeff[5] * (long)data[order-6];
							sum += qlp_coeff[4] * (long)data[order-5];
							sum += qlp_coeff[3] * (long)data[order-4];
							sum += qlp_coeff[2] * (long)data[order-3];
							sum += qlp_coeff[1] * (long)data[order-2];
							sum += qlp_coeff[0] * (long)data[order-1];
							residual[i] = data[order] - (int)(sum >> lp_quantization);
						}
					}
				}
			}
			else if( order > 4 ) {
				if( order > 6 ) {
					if( order == 8 ) {
						for( int i = 0; i < data_len; i++, order++ ) {
							long sum = 0;
							sum += qlp_coeff[7] * (long)data[order-8];
							sum += qlp_coeff[6] * (long)data[order-7];
							sum += qlp_coeff[5] * (long)data[order-6];
							sum += qlp_coeff[4] * (long)data[order-5];
							sum += qlp_coeff[3] * (long)data[order-4];
							sum += qlp_coeff[2] * (long)data[order-3];
							sum += qlp_coeff[1] * (long)data[order-2];
							sum += qlp_coeff[0] * (long)data[order-1];
							residual[i] = data[order] - (int)(sum >> lp_quantization);
						}
					}
					else { /* order == 7 */
						for( int i = 0; i < data_len; i++, order++ ) {
							long sum = 0;
							sum += qlp_coeff[6] * (long)data[order-7];
							sum += qlp_coeff[5] * (long)data[order-6];
							sum += qlp_coeff[4] * (long)data[order-5];
							sum += qlp_coeff[3] * (long)data[order-4];
							sum += qlp_coeff[2] * (long)data[order-3];
							sum += qlp_coeff[1] * (long)data[order-2];
							sum += qlp_coeff[0] * (long)data[order-1];
							residual[i] = data[order] - (int)(sum >> lp_quantization);
						}
					}
				}
				else {
					if( order == 6 ) {
						for( int i = 0; i < data_len; i++, order++ ) {
							long sum = 0;
							sum += qlp_coeff[5] * (long)data[order-6];
							sum += qlp_coeff[4] * (long)data[order-5];
							sum += qlp_coeff[3] * (long)data[order-4];
							sum += qlp_coeff[2] * (long)data[order-3];
							sum += qlp_coeff[1] * (long)data[order-2];
							sum += qlp_coeff[0] * (long)data[order-1];
							residual[i] = data[order] - (int)(sum >> lp_quantization);
						}
					}
					else { /* order == 5 */
						for( int i = 0; i < data_len; i++, order++) {
							long sum = 0;
							sum += qlp_coeff[4] * (long)data[order-5];
							sum += qlp_coeff[3] * (long)data[order-4];
							sum += qlp_coeff[2] * (long)data[order-3];
							sum += qlp_coeff[1] * (long)data[order-2];
							sum += qlp_coeff[0] * (long)data[order-1];
							residual[i] = data[order] - (int)(sum >> lp_quantization);
						}
					}
				}
			}
			else {
				if( order > 2 ) {
					if( order == 4 ) {
						for( int i = 0; i < data_len; i++, order++ ) {
							long sum = 0;
							sum += qlp_coeff[3] * (long)data[order-4];
							sum += qlp_coeff[2] * (long)data[order-3];
							sum += qlp_coeff[1] * (long)data[order-2];
							sum += qlp_coeff[0] * (long)data[order-1];
							residual[i] = data[order] - (int)(sum >> lp_quantization);
						}
					}
					else { /* order == 3 */
						for( int i = 0; i < data_len; i++, order++ ) {
							long sum = 0;
							sum += qlp_coeff[2] * (long)data[order-3];
							sum += qlp_coeff[1] * (long)data[order-2];
							sum += qlp_coeff[0] * (long)data[order-1];
							residual[i] = data[order] - (int)(sum >> lp_quantization);
						}
					}
				}
				else {
					if( order == 2 ) {
						for( int i = 0; i < data_len; i++, order++ ) {
							long sum = 0;
							sum += qlp_coeff[1] * (long)data[order-2];
							sum += qlp_coeff[0] * (long)data[order-1];
							residual[i] = data[order] - (int)(sum >> lp_quantization);
						}
					}
					else { /* order == 1 */
						for( int i = 0; i < data_len; i++, order++ ) {
							residual[i] = data[order] - (int)((qlp_coeff[0] * (long)data[order-1]) >> lp_quantization);
						}
					}
				}
			}
		}
		else { /* order > 12 */
			int di = order;
			for( int i = 0; i < data_len; i++, di++ ) {
				long sum = 0;
				switch( order ) {
					case 32: sum += qlp_coeff[31] * (long)data[di-32];
					case 31: sum += qlp_coeff[30] * (long)data[di-31];
					case 30: sum += qlp_coeff[29] * (long)data[di-30];
					case 29: sum += qlp_coeff[28] * (long)data[di-29];
					case 28: sum += qlp_coeff[27] * (long)data[di-28];
					case 27: sum += qlp_coeff[26] * (long)data[di-27];
					case 26: sum += qlp_coeff[25] * (long)data[di-26];
					case 25: sum += qlp_coeff[24] * (long)data[di-25];
					case 24: sum += qlp_coeff[23] * (long)data[di-24];
					case 23: sum += qlp_coeff[22] * (long)data[di-23];
					case 22: sum += qlp_coeff[21] * (long)data[di-22];
					case 21: sum += qlp_coeff[20] * (long)data[di-21];
					case 20: sum += qlp_coeff[19] * (long)data[di-20];
					case 19: sum += qlp_coeff[18] * (long)data[di-19];
					case 18: sum += qlp_coeff[17] * (long)data[di-18];
					case 17: sum += qlp_coeff[16] * (long)data[di-17];
					case 16: sum += qlp_coeff[15] * (long)data[di-16];
					case 15: sum += qlp_coeff[14] * (long)data[di-15];
					case 14: sum += qlp_coeff[13] * (long)data[di-14];
					case 13: sum += qlp_coeff[12] * (long)data[di-13];
					         sum += qlp_coeff[11] * (long)data[di-12];
					         sum += qlp_coeff[10] * (long)data[di-11];
					         sum += qlp_coeff[ 9] * (long)data[di-10];
					         sum += qlp_coeff[ 8] * (long)data[di- 9];
					         sum += qlp_coeff[ 7] * (long)data[di- 8];
					         sum += qlp_coeff[ 6] * (long)data[di- 7];
					         sum += qlp_coeff[ 5] * (long)data[di- 6];
					         sum += qlp_coeff[ 4] * (long)data[di- 5];
					         sum += qlp_coeff[ 3] * (long)data[di- 4];
					         sum += qlp_coeff[ 2] * (long)data[di- 3];
					         sum += qlp_coeff[ 1] * (long)data[di- 2];
					         sum += qlp_coeff[ 0] * (long)data[di- 1];
				}
				residual[i] = data[di] - (int)(sum >> lp_quantization);
			}
		}
}
	}
//#endif

//#endif /* !defined FLAC__INTEGER_ONLY_LIBRARY */

	/**
	 * local_lpc_restore_signal<br>
	 * local_lpc_restore_signal_16bit<br>
	 * local_lpc_restore_signal_16bit_order8<br>
	 */
	static void FLAC__lpc_restore_signal(final int residual[], final int data_len, final int qlp_coeff[], int order, final int lp_quantization, final int data[])
	{
/* if( FLAC__OVERFLOW_DETECT || ! FLAC__LPC_UNROLLED_FILTER_LOOPS ) {
		int r = 0;
		int idata = order;

if( FLAC__OVERFLOW_DETECT_VERBOSE ) {
		System.err.printf("FLAC__lpc_restore_signal: data_len=%d, order=%d, lpq=%d",data_len,order,lp_quantization);
		for( int i = 0; i < order; i++ ) {
			System.err.printf(", q[%d]=%d",i,qlp_coeff[i]);
		}
		System.err.print("\n");
}
		//FLAC__ASSERT(order > 0);

		for( int i = 0; i < data_len; i++ ) {
			long sumo = 0;
			int sum = 0;
			int history = idata;
			for( int j = 0; j < order; j++ ) {
				sum += qlp_coeff[j] * data[--history];
				sumo += (long)qlp_coeff[j] * (long)(data[history]);

				if( sumo > 2147483647L || sumo < -2147483648L ) {
					System.err.printf("FLAC__lpc_restore_signal: OVERFLOW, i=%d, j=%d, c=%d, d=%d, sumo=%d\n", i, j, qlp_coeff[j], data[history], sumo );
				}
			}
			data[idata++] = residual[r++] + (sum >> lp_quantization);
		}
}
else *//* fully unrolled version for normal use */
	{
		//FLAC__ASSERT(order > 0);
		//FLAC__ASSERT(order <= 32);

		/*
		 * We do unique versions up to 12th order since that's the subset limit.
		 * Also they are roughly ordered to match frequency of occurrence to
		 * minimize branching.
		 */
		if( order <= 12 ) {
			if( order > 8 ) {
				if( order > 10 ) {
					if( order == 12 ) {
						for( int i = 0; i < data_len; i++, order++ ) {
							int sum = 0;
							sum += qlp_coeff[11] * data[order-12];
							sum += qlp_coeff[10] * data[order-11];
							sum += qlp_coeff[9] * data[order-10];
							sum += qlp_coeff[8] * data[order-9];
							sum += qlp_coeff[7] * data[order-8];
							sum += qlp_coeff[6] * data[order-7];
							sum += qlp_coeff[5] * data[order-6];
							sum += qlp_coeff[4] * data[order-5];
							sum += qlp_coeff[3] * data[order-4];
							sum += qlp_coeff[2] * data[order-3];
							sum += qlp_coeff[1] * data[order-2];
							sum += qlp_coeff[0] * data[order-1];
							data[order] = residual[i] + (sum >> lp_quantization);
						}
					}
					else { /* order == 11 */
						for( int i = 0; i < data_len; i++, order++ ) {
							int sum = 0;
							sum += qlp_coeff[10] * data[order-11];
							sum += qlp_coeff[9] * data[order-10];
							sum += qlp_coeff[8] * data[order-9];
							sum += qlp_coeff[7] * data[order-8];
							sum += qlp_coeff[6] * data[order-7];
							sum += qlp_coeff[5] * data[order-6];
							sum += qlp_coeff[4] * data[order-5];
							sum += qlp_coeff[3] * data[order-4];
							sum += qlp_coeff[2] * data[order-3];
							sum += qlp_coeff[1] * data[order-2];
							sum += qlp_coeff[0] * data[order-1];
							data[order] = residual[i] + (sum >> lp_quantization);
						}
					}
				}
				else {
					if( order == 10 ) {
						for( int i = 0; i < data_len; i++, order++ ) {
							int sum = 0;
							sum += qlp_coeff[9] * data[order-10];
							sum += qlp_coeff[8] * data[order-9];
							sum += qlp_coeff[7] * data[order-8];
							sum += qlp_coeff[6] * data[order-7];
							sum += qlp_coeff[5] * data[order-6];
							sum += qlp_coeff[4] * data[order-5];
							sum += qlp_coeff[3] * data[order-4];
							sum += qlp_coeff[2] * data[order-3];
							sum += qlp_coeff[1] * data[order-2];
							sum += qlp_coeff[0] * data[order-1];
							data[order] = residual[i] + (sum >> lp_quantization);
						}
					}
					else { /* order == 9 */
						for( int  i = 0; i < data_len; i++, order++ ) {
							int sum = 0;
							sum += qlp_coeff[8] * data[order-9];
							sum += qlp_coeff[7] * data[order-8];
							sum += qlp_coeff[6] * data[order-7];
							sum += qlp_coeff[5] * data[order-6];
							sum += qlp_coeff[4] * data[order-5];
							sum += qlp_coeff[3] * data[order-4];
							sum += qlp_coeff[2] * data[order-3];
							sum += qlp_coeff[1] * data[order-2];
							sum += qlp_coeff[0] * data[order-1];
							data[order] = residual[i] + (sum >> lp_quantization);
						}
					}
				}
			}
			else if( order > 4 ) {
				if( order > 6 ) {
					if( order == 8 ) {
						for( int i = 0; i < data_len; i++, order++) {
							int sum = 0;
							sum += qlp_coeff[7] * data[order-8];
							sum += qlp_coeff[6] * data[order-7];
							sum += qlp_coeff[5] * data[order-6];
							sum += qlp_coeff[4] * data[order-5];
							sum += qlp_coeff[3] * data[order-4];
							sum += qlp_coeff[2] * data[order-3];
							sum += qlp_coeff[1] * data[order-2];
							sum += qlp_coeff[0] * data[order-1];
							data[order] = residual[i] + (sum >> lp_quantization);
						}
					}
					else { /* order == 7 */
						for( int i = 0; i < data_len; i++, order++ ) {
							int sum = 0;
							sum += qlp_coeff[6] * data[order-7];
							sum += qlp_coeff[5] * data[order-6];
							sum += qlp_coeff[4] * data[order-5];
							sum += qlp_coeff[3] * data[order-4];
							sum += qlp_coeff[2] * data[order-3];
							sum += qlp_coeff[1] * data[order-2];
							sum += qlp_coeff[0] * data[order-1];
							data[order] = residual[i] + (sum >> lp_quantization);
						}
					}
				}
				else {
					if( order == 6 ) {
						for( int i = 0; i < data_len; i++, order++ ) {
							int sum = 0;
							sum += qlp_coeff[5] * data[order-6];
							sum += qlp_coeff[4] * data[order-5];
							sum += qlp_coeff[3] * data[order-4];
							sum += qlp_coeff[2] * data[order-3];
							sum += qlp_coeff[1] * data[order-2];
							sum += qlp_coeff[0] * data[order-1];
							data[order] = residual[i] + (sum >> lp_quantization);
						}
					}
					else { /* order == 5 */
						for( int i = 0; i < data_len; i++, order++ ) {
							int sum = 0;
							sum += qlp_coeff[4] * data[order-5];
							sum += qlp_coeff[3] * data[order-4];
							sum += qlp_coeff[2] * data[order-3];
							sum += qlp_coeff[1] * data[order-2];
							sum += qlp_coeff[0] * data[order-1];
							data[order] = residual[i] + (sum >> lp_quantization);
						}
					}
				}
			}
			else {
				if( order > 2 ) {
					if( order == 4 ) {
						for( int i = 0; i < data_len; i++, order++ ) {
							int sum = 0;
							sum += qlp_coeff[3] * data[order-4];
							sum += qlp_coeff[2] * data[order-3];
							sum += qlp_coeff[1] * data[order-2];
							sum += qlp_coeff[0] * data[order-1];
							data[order] = residual[i] + (sum >> lp_quantization);
						}
					}
					else { /* order == 3 */
						for( int i = 0; i < data_len; i++, order++ ) {
							int sum = 0;
							sum += qlp_coeff[2] * data[order-3];
							sum += qlp_coeff[1] * data[order-2];
							sum += qlp_coeff[0] * data[order-1];
							data[order] = residual[i] + (sum >> lp_quantization);
						}
					}
				}
				else {
					if( order == 2 ) {
						for( int i = 0; i < data_len; i++, order++ ) {
							int sum = 0;
							sum += qlp_coeff[1] * data[order-2];
							sum += qlp_coeff[0] * data[order-1];
							data[order] = residual[i] + (sum >> lp_quantization);
						}
					}
					else { /* order == 1 */
						for( int i = 0; i < data_len; i++, order++ ) {
							data[order] = residual[i] + ((qlp_coeff[0] * data[order-1]) >> lp_quantization);
						}
					}
				}
			}
		}
		else { /* order > 12 */
			int di = order;
			for( int i = 0; i < data_len; i++, di++ ) {
				int sum = 0;
				switch( order ) {
					case 32: sum += qlp_coeff[31] * data[di-32];
					case 31: sum += qlp_coeff[30] * data[di-31];
					case 30: sum += qlp_coeff[29] * data[di-30];
					case 29: sum += qlp_coeff[28] * data[di-29];
					case 28: sum += qlp_coeff[27] * data[di-28];
					case 27: sum += qlp_coeff[26] * data[di-27];
					case 26: sum += qlp_coeff[25] * data[di-26];
					case 25: sum += qlp_coeff[24] * data[di-25];
					case 24: sum += qlp_coeff[23] * data[di-24];
					case 23: sum += qlp_coeff[22] * data[di-23];
					case 22: sum += qlp_coeff[21] * data[di-22];
					case 21: sum += qlp_coeff[20] * data[di-21];
					case 20: sum += qlp_coeff[19] * data[di-20];
					case 19: sum += qlp_coeff[18] * data[di-19];
					case 18: sum += qlp_coeff[17] * data[di-18];
					case 17: sum += qlp_coeff[16] * data[di-17];
					case 16: sum += qlp_coeff[15] * data[di-16];
					case 15: sum += qlp_coeff[14] * data[di-15];
					case 14: sum += qlp_coeff[13] * data[di-14];
					case 13: sum += qlp_coeff[12] * data[di-13];
					         sum += qlp_coeff[11] * data[di-12];
					         sum += qlp_coeff[10] * data[di-11];
					         sum += qlp_coeff[ 9] * data[di-10];
					         sum += qlp_coeff[ 8] * data[di- 9];
					         sum += qlp_coeff[ 7] * data[di- 8];
					         sum += qlp_coeff[ 6] * data[di- 7];
					         sum += qlp_coeff[ 5] * data[di- 6];
					         sum += qlp_coeff[ 4] * data[di- 5];
					         sum += qlp_coeff[ 3] * data[di- 4];
					         sum += qlp_coeff[ 2] * data[di- 3];
					         sum += qlp_coeff[ 1] * data[di- 2];
					         sum += qlp_coeff[ 0] * data[di- 1];
				}
				data[di] = residual[i] + (sum >> lp_quantization);
			}
		}
}
	}

	/**
	 * local_lpc_restore_signal_64bit
	 */
	static void FLAC__lpc_restore_signal_wide(final int residual[], final int data_len, final int qlp_coeff[], int order, final int lp_quantization, final int data[])
	{
/* if( FLAC__OVERFLOW_DETECT || ! FLAC__LPC_UNROLLED_FILTER_LOOPS ) {

		int r = 0, history, idata = order;

if( FLAC__OVERFLOW_DETECT_VERBOSE ) {
		System.err.printf("FLAC__lpc_restore_signal_wide: data_len=%d, order=%d, lpq=%d",data_len,order,lp_quantization);
		for( int i = 0; i < order; i++ ) {
			System.err.printf(", q[%d]=%d",i,qlp_coeff[i]);
		}
		System.err.print("\n");
}
		//FLAC__ASSERT(order > 0);

		for( int i = 0; i < data_len; i++ ) {
			long sum = 0;
			history = idata;
			for( int j = 0; j < order; j++ ) {
				sum += (long)qlp_coeff[j] * (long)data[--history];
			}
			if( FLAC__bitmath_silog2( sum >> lp_quantization ) > 32 ) {
				System.err.printf("FLAC__lpc_restore_signal_wide: OVERFLOW, i=%d, sum=%d\n", i, (sum >> lp_quantization) );
				break;
			}
			if( FLAC__bitmath_silog2( residual[r] + (sum >> lp_quantization) ) > 32 ) {
				System.err.printf("FLAC__lpc_restore_signal_wide: OVERFLOW, i=%d, residual=%d, sum=%d, data=%d\n", i, residual[r], (sum >> lp_quantization), ((long)residual[r] + (sum >> lp_quantization)));
				break;
			}
			data[idata++] = residual[r++] + (int)(sum >> lp_quantization);
		}
}
else *//* fully unrolled version for normal use */
{

		//FLAC__ASSERT(order > 0);
		//FLAC__ASSERT(order <= 32);

		/*
		 * We do unique versions up to 12th order since that's the subset limit.
		 * Also they are roughly ordered to match frequency of occurrence to
		 * minimize branching.
		 */
		if( order <= 12 ) {
			if( order > 8 ) {
				if( order > 10 ) {
					if( order == 12 ) {
						for( int i = 0; i < data_len; i++, order++ ) {
							long sum = 0;
							sum += qlp_coeff[11] * (long)data[order-12];
							sum += qlp_coeff[10] * (long)data[order-11];
							sum += qlp_coeff[9] * (long)data[order-10];
							sum += qlp_coeff[8] * (long)data[order-9];
							sum += qlp_coeff[7] * (long)data[order-8];
							sum += qlp_coeff[6] * (long)data[order-7];
							sum += qlp_coeff[5] * (long)data[order-6];
							sum += qlp_coeff[4] * (long)data[order-5];
							sum += qlp_coeff[3] * (long)data[order-4];
							sum += qlp_coeff[2] * (long)data[order-3];
							sum += qlp_coeff[1] * (long)data[order-2];
							sum += qlp_coeff[0] * (long)data[order-1];
							data[order] = residual[i] + (int)(sum >> lp_quantization);
						}
					}
					else { /* order == 11 */
						for( int i = 0; i < data_len; i++, order++ ) {
							long sum = 0;
							sum += qlp_coeff[10] * (long)data[order-11];
							sum += qlp_coeff[9] * (long)data[order-10];
							sum += qlp_coeff[8] * (long)data[order-9];
							sum += qlp_coeff[7] * (long)data[order-8];
							sum += qlp_coeff[6] * (long)data[order-7];
							sum += qlp_coeff[5] * (long)data[order-6];
							sum += qlp_coeff[4] * (long)data[order-5];
							sum += qlp_coeff[3] * (long)data[order-4];
							sum += qlp_coeff[2] * (long)data[order-3];
							sum += qlp_coeff[1] * (long)data[order-2];
							sum += qlp_coeff[0] * (long)data[order-1];
							data[order] = residual[i] + (int)(sum >> lp_quantization);
						}
					}
				}
				else {
					if( order == 10 ) {
						for( int i = 0; i < data_len; i++, order++ ) {
							long sum = 0;
							sum += qlp_coeff[9] * (long)data[order-10];
							sum += qlp_coeff[8] * (long)data[order-9];
							sum += qlp_coeff[7] * (long)data[order-8];
							sum += qlp_coeff[6] * (long)data[order-7];
							sum += qlp_coeff[5] * (long)data[order-6];
							sum += qlp_coeff[4] * (long)data[order-5];
							sum += qlp_coeff[3] * (long)data[order-4];
							sum += qlp_coeff[2] * (long)data[order-3];
							sum += qlp_coeff[1] * (long)data[order-2];
							sum += qlp_coeff[0] * (long)data[order-1];
							data[order] = residual[i] + (int)(sum >> lp_quantization);
						}
					}
					else { /* order == 9 */
						for( int i = 0; i < data_len; i++, order++ ) {
							long sum = 0;
							sum += qlp_coeff[8] * (long)data[order-9];
							sum += qlp_coeff[7] * (long)data[order-8];
							sum += qlp_coeff[6] * (long)data[order-7];
							sum += qlp_coeff[5] * (long)data[order-6];
							sum += qlp_coeff[4] * (long)data[order-5];
							sum += qlp_coeff[3] * (long)data[order-4];
							sum += qlp_coeff[2] * (long)data[order-3];
							sum += qlp_coeff[1] * (long)data[order-2];
							sum += qlp_coeff[0] * (long)data[order-1];
							data[order] = residual[i] + (int)(sum >> lp_quantization);
						}
					}
				}
			}
			else if( order > 4 ) {
				if( order > 6 ) {
					if( order == 8 ) {
						for( int i = 0; i < data_len; i++, order++ ) {
							long sum = 0;
							sum += qlp_coeff[7] * (long)data[order-8];
							sum += qlp_coeff[6] * (long)data[order-7];
							sum += qlp_coeff[5] * (long)data[order-6];
							sum += qlp_coeff[4] * (long)data[order-5];
							sum += qlp_coeff[3] * (long)data[order-4];
							sum += qlp_coeff[2] * (long)data[order-3];
							sum += qlp_coeff[1] * (long)data[order-2];
							sum += qlp_coeff[0] * (long)data[order-1];
							data[order] = residual[i] + (int)(sum >> lp_quantization);
						}
					}
					else { /* order == 7 */
						for( int i = 0; i < data_len; i++, order++ ) {
							long sum = 0;
							sum += qlp_coeff[6] * (long)data[order-7];
							sum += qlp_coeff[5] * (long)data[order-6];
							sum += qlp_coeff[4] * (long)data[order-5];
							sum += qlp_coeff[3] * (long)data[order-4];
							sum += qlp_coeff[2] * (long)data[order-3];
							sum += qlp_coeff[1] * (long)data[order-2];
							sum += qlp_coeff[0] * (long)data[order-1];
							data[order] = residual[i] + (int)(sum >> lp_quantization);
						}
					}
				}
				else {
					if( order == 6 ) {
						for( int i = 0; i < data_len; i++, order++ ) {
							long sum = 0;
							sum += qlp_coeff[5] * (long)data[order-6];
							sum += qlp_coeff[4] * (long)data[order-5];
							sum += qlp_coeff[3] * (long)data[order-4];
							sum += qlp_coeff[2] * (long)data[order-3];
							sum += qlp_coeff[1] * (long)data[order-2];
							sum += qlp_coeff[0] * (long)data[order-1];
							data[order] = residual[i] + (int)(sum >> lp_quantization);
						}
					}
					else { /* order == 5 */
						for( int i = 0; i < data_len; i++, order++ ) {
							long sum = 0;
							sum += qlp_coeff[4] * (long)data[order-5];
							sum += qlp_coeff[3] * (long)data[order-4];
							sum += qlp_coeff[2] * (long)data[order-3];
							sum += qlp_coeff[1] * (long)data[order-2];
							sum += qlp_coeff[0] * (long)data[order-1];
							data[order] = residual[i] + (int)(sum >> lp_quantization);
						}
					}
				}
			}
			else {
				if( order > 2 ) {
					if( order == 4 ) {
						for( int i = 0; i < data_len; i++, order++ ) {
							long sum = 0;
							sum += qlp_coeff[3] * (long)data[order-4];
							sum += qlp_coeff[2] * (long)data[order-3];
							sum += qlp_coeff[1] * (long)data[order-2];
							sum += qlp_coeff[0] * (long)data[order-1];
							data[order] = residual[i] + (int)(sum >> lp_quantization);
						}
					}
					else { /* order == 3 */
						for( int i = 0; i < data_len; i++, order++ ) {
							long sum = 0;
							sum += qlp_coeff[2] * (long)data[order-3];
							sum += qlp_coeff[1] * (long)data[order-2];
							sum += qlp_coeff[0] * (long)data[order-1];
							data[order] = residual[i] + (int)(sum >> lp_quantization);
						}
					}
				}
				else {
					if( order == 2 ) {
						for( int i = 0; i < data_len; i++, order++ ) {
							long sum = 0;
							sum += qlp_coeff[1] * (long)data[order-2];
							sum += qlp_coeff[0] * (long)data[order-1];
							data[order] = residual[i] + (int)(sum >> lp_quantization);
						}
					}
					else { /* order == 1 */
						for( int i = 0; i < data_len; i++, order++ ) {
							data[order] = residual[i] + (int)((qlp_coeff[0] * (long)data[order-1]) >> lp_quantization);
						}
					}
				}
			}
		}
		else { /* order > 12 */
			int di = order;
			for( int i = 0; i < data_len; i++, di++ ) {
				long sum = 0;
				switch( order ) {
					case 32: sum += qlp_coeff[31] * (long)data[di-32];
					case 31: sum += qlp_coeff[30] * (long)data[di-31];
					case 30: sum += qlp_coeff[29] * (long)data[di-30];
					case 29: sum += qlp_coeff[28] * (long)data[di-29];
					case 28: sum += qlp_coeff[27] * (long)data[di-28];
					case 27: sum += qlp_coeff[26] * (long)data[di-27];
					case 26: sum += qlp_coeff[25] * (long)data[di-26];
					case 25: sum += qlp_coeff[24] * (long)data[di-25];
					case 24: sum += qlp_coeff[23] * (long)data[di-24];
					case 23: sum += qlp_coeff[22] * (long)data[di-23];
					case 22: sum += qlp_coeff[21] * (long)data[di-22];
					case 21: sum += qlp_coeff[20] * (long)data[di-21];
					case 20: sum += qlp_coeff[19] * (long)data[di-20];
					case 19: sum += qlp_coeff[18] * (long)data[di-19];
					case 18: sum += qlp_coeff[17] * (long)data[di-18];
					case 17: sum += qlp_coeff[16] * (long)data[di-17];
					case 16: sum += qlp_coeff[15] * (long)data[di-16];
					case 15: sum += qlp_coeff[14] * (long)data[di-15];
					case 14: sum += qlp_coeff[13] * (long)data[di-14];
					case 13: sum += qlp_coeff[12] * (long)data[di-13];
					         sum += qlp_coeff[11] * (long)data[di-12];
					         sum += qlp_coeff[10] * (long)data[di-11];
					         sum += qlp_coeff[ 9] * (long)data[di-10];
					         sum += qlp_coeff[ 8] * (long)data[di- 9];
					         sum += qlp_coeff[ 7] * (long)data[di- 8];
					         sum += qlp_coeff[ 6] * (long)data[di- 7];
					         sum += qlp_coeff[ 5] * (long)data[di- 6];
					         sum += qlp_coeff[ 4] * (long)data[di- 5];
					         sum += qlp_coeff[ 3] * (long)data[di- 4];
					         sum += qlp_coeff[ 2] * (long)data[di- 3];
					         sum += qlp_coeff[ 1] * (long)data[di- 2];
					         sum += qlp_coeff[ 0] * (long)data[di- 1];
				}
				data[di] = residual[i] + (int)(sum >> lp_quantization);
			}
		}
}
//#endif
	}

	static double FLAC__lpc_compute_expected_bits_per_residual_sample(final double lpc_error, final int total_samples)
	{
		//FLAC__ASSERT(total_samples > 0);

		final double error_scale = 0.5 / (double)total_samples;

		return FLAC__lpc_compute_expected_bits_per_residual_sample_with_error_scale( lpc_error, error_scale );
	}

	private static double FLAC__lpc_compute_expected_bits_per_residual_sample_with_error_scale(final double lpc_error, final double error_scale)
	{
		if( lpc_error > 0.0 ) {
			final double bps = 0.5 * Math.log( error_scale * lpc_error ) / M_LN2;
			if( bps >= 0.0 ) {
				return bps;
			}// else {
				return 0.0;
			//}
		}
		else if( lpc_error < 0.0 ) { /* error should not be negative but can happen due to inadequate floating-point resolution */
			return 1e32;
		}
		//else {
			return 0.0;
		//}
	}

	static int FLAC__lpc_compute_best_order(final double lpc_error[], final int max_order, final int total_samples, final int overhead_bits_per_order)
	{
		//FLAC__ASSERT(max_order > 0);
		//FLAC__ASSERT(total_samples > 0);

		final double error_scale = 0.5 / (double)total_samples;

		int best_index = 0;/* 'index' the index into lpc_error; index==order-1 since lpc_error[0] is for order==1, lpc_error[1] is for order==2, etc */
		double best_bits = Integer.MAX_VALUE;

		for( int indx = 0, order = 1; indx < max_order; indx++, order++ ) {
			final double bits = FLAC__lpc_compute_expected_bits_per_residual_sample_with_error_scale( lpc_error[indx], error_scale ) * (double)(total_samples - order) + (double)(order * overhead_bits_per_order);
			if( bits < best_bits ) {
				best_index = indx;
				best_bits = bits;
			}
		}

		return best_index + 1; /* +1 since index of lpc_error[] is order-1 */
	}
}
