package libFLAC;

/** VERBATIM subframe.  (c.f. <A HREF="../format.html#subframe_verbatim">format specification</A>)
 */
public final class JFLAC__Subframe_Verbatim /* extends JFLAC__Subframe*/ {
	/** A pointer to verbatim signal. */
	int[] data = null;

	/*public JFLAC__Subframe_Verbatim() {
		type = FLAC__SUBFRAME_TYPE_VERBATIM;
	}*/

	/*public JFLAC__Subframe_Verbatim(int[] d) {
		data = d;
	}*/

	// stream_encoder_framing.c
	final boolean FLAC__subframe_add_verbatim(final int samples, final int subframe_bps, final int wasted_bits, final JFLAC__BitWriter bw)
	{
		if( ! bw.FLAC__bitwriter_write_raw_uint32( Jformat.FLAC__SUBFRAME_TYPE_VERBATIM_BYTE_ALIGNED_MASK | (wasted_bits != 0 ? 1 : 0),
				Jformat.FLAC__SUBFRAME_ZERO_PAD_LEN + Jformat.FLAC__SUBFRAME_TYPE_LEN + Jformat.FLAC__SUBFRAME_WASTED_BITS_FLAG_LEN ) ) {
			return false;
		}
		if( wasted_bits != 0 ) {
			if( ! bw.FLAC__bitwriter_write_unary_unsigned( wasted_bits - 1 ) ) {
				return false;
			}
		}

		final int[] signal = this.data;
		for( int i = 0; i < samples; i++ ) {
			if( ! bw.FLAC__bitwriter_write_raw_int32( signal[i], subframe_bps ) ) {
				return false;
			}
		}

		return true;
	}
}
