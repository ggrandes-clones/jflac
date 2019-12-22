package libFLAC;

/** FIXED subframe.  (c.f. <A HREF="../format.html#subframe_fixed">format specification</A>)
 */
public final class JFLAC__Subframe_Fixed /* extends JFLAC__Subframe */ {
	/** The residual coding method. */
	public final JFLAC__EntropyCodingMethod entropy_coding_method = new JFLAC__EntropyCodingMethod();

	/** The polynomial order. */
	public int order = 0;

	/** Warmup samples to prime the predictor, length == order. */
	public final int warmup[] = new int[Jformat.FLAC__MAX_FIXED_ORDER];

	/** The residual signal, length == (blocksize minus order) samples. */
	public int[] residual = null;

	/*public JFLAC__Subframe_Fixed() {
		type = FLAC__SUBFRAME_TYPE_FIXED;
	}*/

	/*public JFLAC__Subframe_Fixed(int[] res) {
		residual = res;
	}*/

	// stream_encoder_framing.c
	final boolean FLAC__subframe_add_fixed(final int residual_samples, final int subframe_bps, final int wasted_bits, final JFLAC__BitWriter bw)
	{
		if( ! bw.FLAC__bitwriter_write_raw_uint32(
				Jformat.FLAC__SUBFRAME_TYPE_FIXED_BYTE_ALIGNED_MASK | (this.order << 1) | (wasted_bits != 0 ? 1 : 0),
				Jformat.FLAC__SUBFRAME_ZERO_PAD_LEN + Jformat.FLAC__SUBFRAME_TYPE_LEN + Jformat.FLAC__SUBFRAME_WASTED_BITS_FLAG_LEN ) ) {
			return false;
		}
		if( wasted_bits != 0 ) {
			if( ! bw.FLAC__bitwriter_write_unary_unsigned( wasted_bits - 1 ) ) {
				return false;
			}
		}

		for( int i = 0; i < this.order; i++ ) {
			if( ! bw.FLAC__bitwriter_write_raw_int32( this.warmup[i], subframe_bps ) ) {
				return false;
			}
		}

		final JFLAC__EntropyCodingMethod method = this.entropy_coding_method;// java
		if( ! JFLAC__Subframe.add_entropy_coding_method_( bw, method ) ) {
			return false;
		}
		switch( method.type ) {
			case Jformat.FLAC__ENTROPY_CODING_METHOD_PARTITIONED_RICE:
			case Jformat.FLAC__ENTROPY_CODING_METHOD_PARTITIONED_RICE2:
				if( ! JFLAC__Subframe.add_residual_partitioned_rice_(
					bw,
					this.residual,
					residual_samples,
					this.order,
					method./*data.*/partitioned_rice.contents.parameters,
					method./*data.*/partitioned_rice.contents.raw_bits,
					method./*data.*/partitioned_rice.order,
					/*is_extended=*/method.type == Jformat.FLAC__ENTROPY_CODING_METHOD_PARTITIONED_RICE2
				) ) {
					return false;
				}
				break;
			default:
				//FLAC__ASSERT(0);
		}

		return true;
	}
}
