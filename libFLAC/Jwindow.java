package libFLAC;

final class Jwindow {

	static void FLAC__window_bartlett(final float[] window, final int L)
	{
		final int N = L - 1;
		int n;

		if( (L & 1) != 0 ) {
			for( n = 0; n <= (N >>> 1); n++ ) {
				window[n] = 2.0f * n / (float)N;
			}
			for( ; n <= N; n++ ) {
				window[n] = 2.0f - 2.0f * n / (float)N;
			}
		}
		else {
			for( n = 0; n <= (L >>> 1) - 1; n++ ) {
				window[n] = 2.0f * n / (float)N;
			}
			for( ; n <= N; n++ ) {
				window[n] = 2.0f - 2.0f * n / (float)N;
			}
		}
	}

	static void FLAC__window_bartlett_hann(final float[] window, final int L)
	{
		final int N = L - 1;

		for( int n = 0; n < L; n++ ) {
			window[n] = (float)(0.62 - 0.48 * Math.abs((double)n / (double)N + 0.5) + 0.38 * Math.cos(2.0 * Math.PI * ((double)n / (double)N)));
		}
	}

	static void FLAC__window_blackman(final float[] window, final int L)
	{
		final int N = L - 1;
		final double k = 2.0 * Math.PI / N;

		for( int n = 0; n < L; n++ ) {
			final double a = k * n;
			window[n] = (float)(0.42 - 0.5 * Math.cos( a ) + 0.08 * Math.cos( 2.0 * a ));
		}
	}

	/** 4-term -92dB side-lobe */
	static void FLAC__window_blackman_harris_4term_92db_sidelobe(final float[] window, final int L)
	{
		final int N = L - 1;
		final double k = 2.0 * Math.PI / N;

		for( int n = 0; n <= N; n++ ) {
			final double a = k * n;
			window[n] = (float)(0.35875 - 0.48829 * Math.cos( a ) + 0.14128 * Math.cos( 2.0 * a ) - 0.01168 * Math.cos( 3.0 * a ));
		}
	}

	static void FLAC__window_connes(final float[] window, final int L)
	{
		final int N = L - 1;
		final double N2 = (double)N / 2.;

		for( int n = 0; n <= N; n++ ) {
			double k = ((double)n - N2) / N2;
			k = 1.0 - k * k;
			window[n] = (float)(k * k);
		}
	}

	static void FLAC__window_flattop(final float[] window, final int L)
	{
		final int N = L - 1;
		final double k = 2.0 * Math.PI / N;

		for( int n = 0; n < L; n++ ) {
			final double a = k * n;
			window[n] = (float)(1.0 - 1.93 * Math.cos( a ) + 1.29 * Math.cos( 2.0 * a ) - 0.388 * Math.cos( 3.0 * a ) + 0.0322 * Math.cos( 4.0 * a ));
		}
	}

	static void FLAC__window_gauss(final float[] window, final int L, final float stddev)
	{
		final int N = L - 1;
		final double N2 = (double)N / 2.;

		for( int n = 0; n <= N; n++ ) {
			final double k = ((double)n - N2) / ((double)stddev * N2);
			window[n] = (float)Math.exp( -0.5 * k * k );
		}
	}

	static void FLAC__window_hamming(final float[] window, final int L)
	{
		final int N = L - 1;
		final double k = 2.0 * Math.PI / N;

		for( int n = 0; n < L; n++ ) {
			window[n] = (float)(0.54 - 0.46 * Math.cos( k * n ));
		}
	}

	static void FLAC__window_hann(final float[] window, final int L)
	{
		final int N = L - 1;
		final double k = 2.0 * Math.PI / N;

		for( int n = 0; n < L; n++ ) {
			window[n] = (float)(0.5f - 0.5f * Math.cos( k * n ));
		}
	}

	static void FLAC__window_kaiser_bessel(final float[] window, final int L)
	{
		final int N = L - 1;
		final double k = 2.0 * Math.PI / N;

		for( int n = 0; n < L; n++ ) {
			final double a = k * n;
			window[n] = (float)(0.402 - 0.498 * Math.cos( a ) + 0.098 * Math.cos( 2.0 * a ) - 0.001 * Math.cos( 3.0 * a ));
		}
	}

	static void FLAC__window_nuttall(final float[] window, final int L)
	{
		final int N = L - 1;
		final double k = 2.0 * Math.PI / N;

		for( int n = 0; n < L; n++ ) {
			final double a = k * n;
			window[n] = (float)(0.3635819 - 0.4891775 * Math.cos( a ) + 0.1365995 * Math.cos( 2.0 * a ) - 0.0106411 * Math.cos( 3.0 * a ));
		}
	}

	static void FLAC__window_rectangle(final float[] window, final int L)
	{
		for( int n = 0; n < L; n++ ) {
			window[n] = 1.0f;
		}
	}

	static void FLAC__window_triangle(final float[] window, final int L)
	{
		int n;

		if( (L & 1) != 0 ) {
			for( n = 1; n <= ((L + 1) >> 1); n++ ) {
				window[n - 1] = 2.0f * n / ((float)L + 1.0f);
			}
			for( ; n <= L; n++ ) {
				window[n - 1] = - (float)((L - n + 1) << 1) / ((float)L + 1.0f);
			}
		}
		else {
			for( n = 1; n <= (L >> 1); n++ ) {
				window[n - 1] = 2.0f * n / ((float)L + 1.0f);
			}
			for( ; n <= L; n++ ) {
				window[n - 1] = ((float)((L - n + 1) << 1)) / ((float)L + 1.0f);
			}
		}
	}

	static void FLAC__window_tukey(final float[] window, final int L, final float p)
	{
		if( p <= 0.0f ) {
			FLAC__window_rectangle( window, L );
		} else if( p >= 1.0f ) {
			FLAC__window_hann( window, L );
		} else {
			final int Np = (int)(p / 2.0f * (float)L) - 1;
			/* start with rectangle... */
			FLAC__window_rectangle( window, L );
			/* ...replace ends with hann */
			if( Np > 0 ) {
				final double k = Math.PI / Np;
				for( int n = 0; n <= Np; n++ ) {
					window[n] = (float)(0.5 - 0.5 * Math.cos( k * n ));
					window[L - Np - 1 + n] = (float)(0.5 - 0.5 * Math.cos( k * (n + Np) ));
				}
			}
		}
	}

	static void FLAC__window_partial_tukey(final float[] window, final int L, final float p, final float start, final float end)
	{
		final int start_n = (int)(start * L);
		final int end_n = (int)(end * L);
		final int N = end_n - start_n;

		if( p <= 0.0f ) {
			FLAC__window_partial_tukey( window, L, 0.05f, start, end );
		} else if( p >= 1.0f ) {
			FLAC__window_partial_tukey( window, L, 0.95f, start, end );
		} else {

			final int Np = (int)(p / 2.0f * N);

			int n;
			for( n = 0; n < start_n && n < L; n++ ) {
				window[n] = 0.0f;
			}
			for( int i = 1; n < (start_n + Np) && n < L; n++, i++ ) {
				window[n] = (float)(0.5 - 0.5 * Math.cos( Math.PI * i / Np));
			}
			for( ; n < (end_n - Np) && n < L; n++ ) {
				window[n] = 1.0f;
			}
			for( int i = Np; n < end_n && n < L; n++, i-- ) {
				window[n] = (float)(0.5 - 0.5 * Math.cos( Math.PI * i / Np));
			}
			for( ; n < L; n++ ) {
				window[n] = 0.0f;
			}
		}
	}

	static void FLAC__window_punchout_tukey(final float[] window, final int L, final float p, final float start, final float end)
	{
		final int start_n = (int)(start * L);
		final int end_n = (int)(end * L);

		if( p <= 0.0f ) {
			FLAC__window_punchout_tukey( window, L, 0.05f, start, end );
		} else if( p >= 1.0f ) {
			FLAC__window_punchout_tukey( window, L, 0.95f, start, end );
		} else {

			final int Ns = (int)(p / 2.0f * start_n);
			final int Ne = (int)(p / 2.0f * (L - end_n));

			int n = 0;
			for( int i = 1; n < Ns && n < L; n++, i++ ) {
				window[n] = (float)(0.5 - 0.5 * Math.cos( Math.PI * i / Ns));
			}
			for( ; n < start_n - Ns && n < L; n++) {
				window[n] = 1.0f;
			}
			for( int i = Ns; n < start_n && n < L; n++, i-- ) {
				window[n] = (float)(0.5 - 0.5 * Math.cos( Math.PI * i / Ns));
			}
			for( ; n < end_n && n < L; n++ ) {
				window[n] = 0.0f;
			}
			for( int i = 1; n < end_n+Ne && n < L; n++, i++ ) {
				window[n] = (float)(0.5 - 0.5 * Math.cos( Math.PI * i / Ne));
			}
			for( ; n < L - (Ne) && n < L; n++ ) {
				window[n] = 1.0f;
			}
			for( int i = Ne; n < L; n++, i-- ) {
				window[n] = (float)(0.5 - 0.5 * Math.cos( Math.PI * i / Ne));
			}
		}
	}

	static void FLAC__window_welch(final float[] window, final int L)
	{
		final int N = L - 1;
		final double N2 = (double)N / 2.;

		for( int n = 0; n <= N; n++ ) {
			final double k = ((double)n - N2) / N2;
			window[n] = (float)(1.0 - k * k);
		}
	}
}
