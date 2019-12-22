package libFLAC;

/** CONSTANT subframe.  (c.f. <A HREF="../format.html#subframe_constant">format specification</A>)
 */
public final class JFLAC__Subframe_Constant /* extends JFLAC__Subframe */ {
	/** The constant signal value. */
	public int value = 0;

	/*public JFLAC__Subframe_Constant() {
		type = FLAC__SUBFRAME_TYPE_CONSTANT;
	}*/

	// stream_encoder_framing.c
	final boolean FLAC__subframe_add_constant(final int subframe_bps, final int wasted_bits, final JFLAC__BitWriter bw)
	{
		final boolean ok =
			bw.FLAC__bitwriter_write_raw_uint32(
				Jformat.FLAC__SUBFRAME_TYPE_CONSTANT_BYTE_ALIGNED_MASK | (wasted_bits != 0 ? 1 : 0),
				Jformat.FLAC__SUBFRAME_ZERO_PAD_LEN + Jformat.FLAC__SUBFRAME_TYPE_LEN + Jformat.FLAC__SUBFRAME_WASTED_BITS_FLAG_LEN)
				&&
				(wasted_bits != 0 ? bw.FLAC__bitwriter_write_unary_unsigned( wasted_bits - 1 ) : true)
				&&
				bw.FLAC__bitwriter_write_raw_int32( this.value, subframe_bps );

		return ok;
	}
}
