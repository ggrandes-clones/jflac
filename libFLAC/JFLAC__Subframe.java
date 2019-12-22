package libFLAC;

/** FLAC subframe structure.  (c.f. <A HREF="../format.html#subframe">format specification</A>)
 */
public final class JFLAC__Subframe {

	public int /*FLAC__SubframeType*/ type;
	/*union {
		JFLAC__Subframe_Constant constant;
		JFLAC__Subframe_Fixed fixed;
		JFLAC__Subframe_LPC lpc;
		JFLAC__Subframe_Verbatim verbatim;
	} data;*/
	public Object data = null;// TODO optimize using subframe types
	public int wasted_bits;

	// stream_encoder_framing.c
	static boolean add_entropy_coding_method_(final JFLAC__BitWriter bw, final JFLAC__EntropyCodingMethod method)
	{
		if( ! bw.FLAC__bitwriter_write_raw_uint32( method.type, Jformat.FLAC__ENTROPY_CODING_METHOD_TYPE_LEN ) ) {
			return false;
		}
		switch( method.type ) {
			case Jformat.FLAC__ENTROPY_CODING_METHOD_PARTITIONED_RICE:
			case Jformat.FLAC__ENTROPY_CODING_METHOD_PARTITIONED_RICE2:
				if( ! bw.FLAC__bitwriter_write_raw_uint32( method./*data.*/partitioned_rice.order, Jformat.FLAC__ENTROPY_CODING_METHOD_PARTITIONED_RICE_ORDER_LEN ) ) {
					return false;
				}
				break;
			default:
				//FLAC__ASSERT(0);
		}
		return true;
	}

	static boolean add_residual_partitioned_rice_(final JFLAC__BitWriter bw, final int residual[], final int residual_samples, final int predictor_order, final int rice_parameters[], final int raw_bits[], final int partition_order, final boolean is_extended)
	{
		final int plen = is_extended ? Jformat.FLAC__ENTROPY_CODING_METHOD_PARTITIONED_RICE2_PARAMETER_LEN : Jformat.FLAC__ENTROPY_CODING_METHOD_PARTITIONED_RICE_PARAMETER_LEN;
		final int pesc = is_extended ? Jformat.FLAC__ENTROPY_CODING_METHOD_PARTITIONED_RICE2_ESCAPE_PARAMETER : Jformat.FLAC__ENTROPY_CODING_METHOD_PARTITIONED_RICE_ESCAPE_PARAMETER;

		if( partition_order == 0 ) {

			if( raw_bits[0] == 0 ) {
				if( ! bw.FLAC__bitwriter_write_raw_uint32( rice_parameters[0], plen ) ) {
					return false;
				}
				if( ! bw.FLAC__bitwriter_write_rice_signed_block( residual, 0, residual_samples, rice_parameters[0] ) ) {
					return false;
				}
			}
			else {
				//FLAC__ASSERT(rice_parameters[0] == 0);
				if( ! bw.FLAC__bitwriter_write_raw_uint32( pesc, plen ) ) {
					return false;
				}
				if( ! bw.FLAC__bitwriter_write_raw_uint32( raw_bits[0], Jformat.FLAC__ENTROPY_CODING_METHOD_PARTITIONED_RICE_RAW_LEN ) ) {
					return false;
				}
				for( int i = 0; i < residual_samples; i++ ) {
					if( ! bw.FLAC__bitwriter_write_raw_int32( residual[i], raw_bits[0] ) ) {
						return false;
					}
				}
			}
			return true;
		}
		else {
			int k = 0, k_last = 0;
			final int default_partition_samples = (residual_samples + predictor_order) >> partition_order;
			for( int i = 0, ie = (1 << partition_order); i < ie; i++ ) {
				int partition_samples = default_partition_samples;
				if( i == 0 ) {
					partition_samples -= predictor_order;
				}
				k += partition_samples;
				if( raw_bits[i] == 0 ) {
					if( ! bw.FLAC__bitwriter_write_raw_uint32( rice_parameters[i], plen ) ) {
						return false;
					}
					if( ! bw.FLAC__bitwriter_write_rice_signed_block( residual, k_last, k - k_last, rice_parameters[i] ) ) {
						return false;
					}
				}
				else {
					if( ! bw.FLAC__bitwriter_write_raw_uint32( pesc, plen ) ) {
						return false;
					}
					if( ! bw.FLAC__bitwriter_write_raw_uint32( raw_bits[i], Jformat.FLAC__ENTROPY_CODING_METHOD_PARTITIONED_RICE_RAW_LEN ) ) {
						return false;
					}
					for( int j = k_last; j < k; j++ ) {
						if( ! bw.FLAC__bitwriter_write_raw_int32( residual[j], raw_bits[i] ) ) {
							return false;
						}
					}
				}
				k_last = k;
			}
			return true;
		}
	}
}
