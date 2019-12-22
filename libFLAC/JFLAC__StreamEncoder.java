package libFLAC;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

import libogg.Jogg_page;

public final class JFLAC__StreamEncoder implements
	JFLAC__StreamDecoderReadCallback,// verify_read_callback_
	JFLAC__StreamDecoderWriteCallback,// verify_write_callback_
	JFLAC__StreamDecoderMetadataCallback,// verify_metadata_callback_
	JFLAC__StreamDecoderErrorCallback,// verify_error_callback_
	JFLAC__StreamEncoderReadCallback,// file_read_callback_
	JFLAC__StreamEncoderWriteCallback,// file_write_callback_,
	JFLAC__StreamEncoderSeekCallback,// file_seek_callback_,
	JFLAC__StreamEncoderTellCallback// file_tell_callback_
{
	/*
	 * This is used to avoid overflow with unusual signals in 32-bit
	 * accumulator in the *precompute_partition_info_sums_* functions.
	 */
	private static final int  FLAC__MAX_EXTRA_RESIDUAL_BPS = 4;
	/** State values for a FLAC__StreamEncoder.
	 *
	 * The encoder's state can be obtained by calling FLAC__stream_encoder_get_state().
	 *
	 * If the encoder gets into any other state besides \c FLAC__STREAM_ENCODER_OK
	 * or \c FLAC__STREAM_ENCODER_UNINITIALIZED, it becomes invalid for encoding and
	 * must be deleted with FLAC__stream_encoder_delete().
	 */
	//typedef enum {
		/** The encoder is in the normal OK state and samples can be processed. */
		public static final int FLAC__STREAM_ENCODER_OK = 0;

		/** The encoder is in the uninitialized state; one of the
		 * FLAC__stream_encoder_init_*() functions must be called before samples
		 * can be processed.
		 */
		public static final int FLAC__STREAM_ENCODER_UNINITIALIZED = 1;

		/** An error occurred in the underlying Ogg layer.  */
		public static final int FLAC__STREAM_ENCODER_OGG_ERROR = 2;

		/** An error occurred in the underlying verify stream decoder;
		 * check FLAC__stream_encoder_get_verify_decoder_state().
		 */
		public static final int FLAC__STREAM_ENCODER_VERIFY_DECODER_ERROR = 3;

		/** The verify decoder detected a mismatch between the original
		 * audio signal and the decoded audio signal.
		 */
		public static final int FLAC__STREAM_ENCODER_VERIFY_MISMATCH_IN_AUDIO_DATA = 4;

		/** One of the callbacks returned a fatal error. */
		public static final int FLAC__STREAM_ENCODER_CLIENT_ERROR = 5;

		/** An I/O error occurred while opening/reading/writing a file.
		 * Check \c errno.
		 */
		public static final int FLAC__STREAM_ENCODER_IO_ERROR = 6;

		/** An error occurred while writing the stream; usually, the
		 * write_callback returned an error.
		 */
		public static final int FLAC__STREAM_ENCODER_FRAMING_ERROR = 7;

		/** Memory allocation failed. */
		public static final int FLAC__STREAM_ENCODER_MEMORY_ALLOCATION_ERROR = 8;

	//} FLAC__StreamEncoderState;

	public static final String  FLAC__StreamEncoderStateString[] = {
		"FLAC__STREAM_ENCODER_OK",
		"FLAC__STREAM_ENCODER_UNINITIALIZED",
		"FLAC__STREAM_ENCODER_OGG_ERROR",
		"FLAC__STREAM_ENCODER_VERIFY_DECODER_ERROR",
		"FLAC__STREAM_ENCODER_VERIFY_MISMATCH_IN_AUDIO_DATA",
		"FLAC__STREAM_ENCODER_CLIENT_ERROR",
		"FLAC__STREAM_ENCODER_IO_ERROR",
		"FLAC__STREAM_ENCODER_FRAMING_ERROR",
		"FLAC__STREAM_ENCODER_MEMORY_ALLOCATION_ERROR"
	};

	/** Possible return values for the FLAC__stream_encoder_init_*() functions.
	 */
	//typedef enum {
		/** Initialization was successful. */
		public static final int FLAC__STREAM_ENCODER_INIT_STATUS_OK = 0;

		/** General failure to set up encoder; call FLAC__stream_encoder_get_state() for cause. */
		public static final int FLAC__STREAM_ENCODER_INIT_STATUS_ENCODER_ERROR = 1;

		/** The library was not compiled with support for the given container
		 * format.
		 */
		public static final int FLAC__STREAM_ENCODER_INIT_STATUS_UNSUPPORTED_CONTAINER = 2;

		/** A required callback was not supplied. */
		public static final int FLAC__STREAM_ENCODER_INIT_STATUS_INVALID_CALLBACKS = 3;

		/** The encoder has an invalid setting for number of channels. */
		public static final int FLAC__STREAM_ENCODER_INIT_STATUS_INVALID_NUMBER_OF_CHANNELS = 4;

		/** The encoder has an invalid setting for bits-per-sample.
		 * FLAC supports 4-32 bps but the reference encoder currently supports
		 * only up to 24 bps.
		 */
		public static final int FLAC__STREAM_ENCODER_INIT_STATUS_INVALID_BITS_PER_SAMPLE = 5;

		/** The encoder has an invalid setting for the input sample rate. */
		public static final int FLAC__STREAM_ENCODER_INIT_STATUS_INVALID_SAMPLE_RATE = 6;

		/** The encoder has an invalid setting for the block size. */
		public static final int FLAC__STREAM_ENCODER_INIT_STATUS_INVALID_BLOCK_SIZE = 7;

		/** The encoder has an invalid setting for the maximum LPC order. */
		public static final int FLAC__STREAM_ENCODER_INIT_STATUS_INVALID_MAX_LPC_ORDER = 8;

		/** The encoder has an invalid setting for the precision of the quantized linear predictor coefficients. */
		public static final int FLAC__STREAM_ENCODER_INIT_STATUS_INVALID_QLP_COEFF_PRECISION = 9;

		/** The specified block size is less than the maximum LPC order. */
		public static final int FLAC__STREAM_ENCODER_INIT_STATUS_BLOCK_SIZE_TOO_SMALL_FOR_LPC_ORDER = 10;

		/** The encoder is bound to the <A HREF="../format.html#subset">Subset</A> but other settings violate it. */
		public static final int FLAC__STREAM_ENCODER_INIT_STATUS_NOT_STREAMABLE = 11;

		/** The metadata input to the encoder is invalid, in one of the following ways:
		 * - FLAC__stream_encoder_set_metadata() was called with a null pointer but a block count > 0
		 * - One of the metadata blocks contains an undefined type
		 * - It contains an illegal CUESHEET as checked by FLAC__format_cuesheet_is_legal()
		 * - It contains an illegal SEEKTABLE as checked by FLAC__format_seektable_is_legal()
		 * - It contains more than one SEEKTABLE block or more than one VORBIS_COMMENT block
		 */
		public static final int FLAC__STREAM_ENCODER_INIT_STATUS_INVALID_METADATA = 12;

		/** FLAC__stream_encoder_init_*() was called when the encoder was
		 * already initialized, usually because
		 * FLAC__stream_encoder_finish() was not called.
		 */
		public static final int FLAC__STREAM_ENCODER_INIT_STATUS_ALREADY_INITIALIZED = 13;

	//} FLAC__StreamEncoderInitStatus;

	public static final String FLAC__StreamEncoderInitStatusString[] = {
		"FLAC__STREAM_ENCODER_INIT_STATUS_OK",
		"FLAC__STREAM_ENCODER_INIT_STATUS_ENCODER_ERROR",
		"FLAC__STREAM_ENCODER_INIT_STATUS_UNSUPPORTED_CONTAINER",
		"FLAC__STREAM_ENCODER_INIT_STATUS_INVALID_CALLBACKS",
		"FLAC__STREAM_ENCODER_INIT_STATUS_INVALID_NUMBER_OF_CHANNELS",
		"FLAC__STREAM_ENCODER_INIT_STATUS_INVALID_BITS_PER_SAMPLE",
		"FLAC__STREAM_ENCODER_INIT_STATUS_INVALID_SAMPLE_RATE",
		"FLAC__STREAM_ENCODER_INIT_STATUS_INVALID_BLOCK_SIZE",
		"FLAC__STREAM_ENCODER_INIT_STATUS_INVALID_MAX_LPC_ORDER",
		"FLAC__STREAM_ENCODER_INIT_STATUS_INVALID_QLP_COEFF_PRECISION",
		"FLAC__STREAM_ENCODER_INIT_STATUS_BLOCK_SIZE_TOO_SMALL_FOR_LPC_ORDER",
		"FLAC__STREAM_ENCODER_INIT_STATUS_NOT_STREAMABLE",
		"FLAC__STREAM_ENCODER_INIT_STATUS_INVALID_METADATA",
		"FLAC__STREAM_ENCODER_INIT_STATUS_ALREADY_INITIALIZED"
	};

	/** Return values for the FLAC__StreamEncoder read callback.
	 */
	//typedef enum {// java: changed. uses IOException and UnsupportedOperationException
		/** The read was OK and decoding can continue. */
		//private static final int FLAC__STREAM_ENCODER_READ_STATUS_CONTINUE = 0;

		/** The read was attempted at the end of the stream. */
		//private static final int FLAC__STREAM_ENCODER_READ_STATUS_END_OF_STREAM = 1;

		/** An unrecoverable error occurred. */
		//private static final int FLAC__STREAM_ENCODER_READ_STATUS_ABORT = 2;

		/** Client does not support reading back from the output. */
		private static final int FLAC__STREAM_ENCODER_READ_STATUS_UNSUPPORTED = 3;
	//} FLAC__StreamEncoderReadStatus;

	/** Maps a FLAC__StreamEncoderReadStatus to a C string.
	 *
	 *  Using a FLAC__StreamEncoderReadStatus as the index to this array
	 *  will give the string equivalent.  The contents should not be modified.
	 */
	private static final String FLAC__StreamEncoderReadStatusString[] = {
		"FLAC__STREAM_ENCODER_READ_STATUS_CONTINUE",
		"FLAC__STREAM_ENCODER_READ_STATUS_END_OF_STREAM",
		"FLAC__STREAM_ENCODER_READ_STATUS_ABORT",
		"FLAC__STREAM_ENCODER_READ_STATUS_UNSUPPORTED"
	};

	/** Return values for the FLAC__StreamEncoder write callback.
	 */
	//typedef enum {
		/** The write was OK and encoding can continue. */
		public static final int FLAC__STREAM_ENCODER_WRITE_STATUS_OK = 0;

		/** An unrecoverable error occurred.  The encoder will return from the process call. */
		public static final int FLAC__STREAM_ENCODER_WRITE_STATUS_FATAL_ERROR = 1;

	//} FLAC__StreamEncoderWriteStatus;

	/** Maps a FLAC__StreamEncoderWriteStatus to a C string.
	 *
	 *  Using a FLAC__StreamEncoderWriteStatus as the index to this array
	 *  will give the string equivalent.  The contents should not be modified.
	 */
	public static final String FLAC__StreamEncoderWriteStatusString[] = {
		"FLAC__STREAM_ENCODER_WRITE_STATUS_OK",
		"FLAC__STREAM_ENCODER_WRITE_STATUS_FATAL_ERROR"
	};

	/** Return values for the FLAC__StreamEncoder seek callback.
	 */
	//typedef enum {
		/** The seek was OK and encoding can continue. */
		public static final int FLAC__STREAM_ENCODER_SEEK_STATUS_OK = 0;

		/** An unrecoverable error occurred. */
		public static final int FLAC__STREAM_ENCODER_SEEK_STATUS_ERROR = 1;

		/** Client does not support seeking. */
		public static final int FLAC__STREAM_ENCODER_SEEK_STATUS_UNSUPPORTED = 2;

	//} FLAC__StreamEncoderSeekStatus;

	/** Maps a FLAC__StreamEncoderSeekStatus to a C string.
	 *
	 *  Using a FLAC__StreamEncoderSeekStatus as the index to this array
	 *  will give the string equivalent.  The contents should not be modified.
	 */
	public static final String FLAC__StreamEncoderSeekStatusString[] = {
		"FLAC__STREAM_ENCODER_SEEK_STATUS_OK",
		"FLAC__STREAM_ENCODER_SEEK_STATUS_ERROR",
		"FLAC__STREAM_ENCODER_SEEK_STATUS_UNSUPPORTED"
	};

	/** Return values for the FLAC__StreamEncoder tell callback.
	 */
	//typedef enum {// java: changed. uses IOException, UnsupportedOperationException
		/** The tell was OK and encoding can continue. */
		//private static final int FLAC__STREAM_ENCODER_TELL_STATUS_OK = 0;

		/** An unrecoverable error occurred. */
		//private static final int FLAC__STREAM_ENCODER_TELL_STATUS_ERROR = 1;

		/** Client does not support seeking. */
		private static final int FLAC__STREAM_ENCODER_TELL_STATUS_UNSUPPORTED = 2;

	//} FLAC__StreamEncoderTellStatus;

	/** Maps a FLAC__StreamEncoderTellStatus to a C string.
	 *
	 *  Using a FLAC__StreamEncoderTellStatus as the index to this array
	 *  will give the string equivalent.  The contents should not be modified.
	 */
	private static final String FLAC__StreamEncoderTellStatusString[] = {
		"FLAC__STREAM_ENCODER_TELL_STATUS_OK",
		"FLAC__STREAM_ENCODER_TELL_STATUS_ERROR",
		"FLAC__STREAM_ENCODER_TELL_STATUS_UNSUPPORTED"
	};

	private static final class JFLAC__ApodizationSpecification {
		//typedef enum {
			private static final int FLAC__APODIZATION_BARTLETT = 0;
			private static final int FLAC__APODIZATION_BARTLETT_HANN = 1;
			private static final int FLAC__APODIZATION_BLACKMAN = 2;
			private static final int FLAC__APODIZATION_BLACKMAN_HARRIS_4TERM_92DB_SIDELOBE = 3;
			private static final int FLAC__APODIZATION_CONNES = 4;
			private static final int FLAC__APODIZATION_FLATTOP = 5;
			private static final int FLAC__APODIZATION_GAUSS = 6;
			private static final int FLAC__APODIZATION_HAMMING = 7;
			private static final int FLAC__APODIZATION_HANN = 8;
			private static final int FLAC__APODIZATION_KAISER_BESSEL = 9;
			private static final int FLAC__APODIZATION_NUTTALL = 10;
			private static final int FLAC__APODIZATION_RECTANGLE = 11;
			private static final int FLAC__APODIZATION_TRIANGLE = 12;
			private static final int FLAC__APODIZATION_TUKEY = 13;
			private static final int FLAC__APODIZATION_PARTIAL_TUKEY = 14;
			private static final int FLAC__APODIZATION_PUNCHOUT_TUKEY = 15;
			private static final int FLAC__APODIZATION_WELCH = 16;
		//} FLAC__ApodizationFunction;

		/** FLAC__ApodizationFunction */
		int /*FLAC__ApodizationFunction*/ type;
		/*union {// TODO check using union and try to find a better solution
			struct {
				FLAC__real stddev;
			} gauss;
			struct {
				FLAC__real p;
			} tukey;
			struct {
				FLAC__real p;
				FLAC__real start;
				FLAC__real end;
			} multiple_tukey;
		} parameters;*/
		float stddev;
		float p;
		float start;
		float end;
	}
	private static final int FLAC__MAX_APODIZATION_FUNCTIONS = 32;
	//static class JFLAC__StreamEncoderProtected {// java: changed by 'default'
		int /*JFLAC__StreamEncoderState*/ state;
		boolean do_verify;// java: duplicate, verify renamed to do_verify
		boolean streamable_subset;
		boolean do_md5;
		boolean do_mid_side_stereo;
		boolean loose_mid_side_stereo;
		int channels;
		int bits_per_sample;
		int sample_rate;
		int blocksize;
//#ifndef FLAC__INTEGER_ONLY_LIBRARY
		int num_apodizations;
		final JFLAC__ApodizationSpecification apodizations[] = new JFLAC__ApodizationSpecification[FLAC__MAX_APODIZATION_FUNCTIONS];
//#endif
		int max_lpc_order;
		int qlp_coeff_precision;
		boolean do_qlp_coeff_prec_search;
		boolean do_exhaustive_model_search;
		boolean do_escape_coding;
		int min_residual_partition_order;
		int max_residual_partition_order;
		int rice_parameter_search_dist;
		long total_samples_estimate;
		JFLAC__StreamMetadata[] metadata;
		// int num_metadata_blocks;// java: use metadata.length
		long streaminfo_offset, seektable_offset, audio_offset;
//if( FLAC__HAS_OGG ) {
		final JFLAC__OggEncoderAspect ogg_encoder_aspect = new JFLAC__OggEncoderAspect();

		/*JFLAC__StreamEncoderProtected() {// moved to base class constructor
			for( int i = 0; i < apodizations.length; i++ ) {
				apodizations[i] = new JFLAC__ApodizationSpecification();
			}
		}*/
//}
	//}
	//JFLAC__StreamEncoderProtected protected_; /* avoid the C++ keyword 'protected' */

	/***********************************************************************
	 *
	 * Private class data
	 *
	 ***********************************************************************/
	private static final class Jverify_input_fifo {
		private final int data[][] = new int[Jformat.FLAC__MAX_CHANNELS][];
		private int size; /* of each data[] in samples */
		private int tail;
		//
		private final void append_to_verify_fifo_(final int input[][], final int input_offset, final int channels, final int wide_samples)
		{
			final int[][] d = this.data;// java
			int t = this.tail;// java
			for( int channel = 0; channel < channels; channel++ ) {
				System.arraycopy( input[channel], input_offset, d[channel], t, wide_samples );
			}

			t += wide_samples;
			this.tail = t;

			//FLAC__ASSERT(fifo->tail <= fifo->size);
		}

		private final void append_to_verify_fifo_interleaved_(final int input[], final int input_offset, final int channels, final int wide_samples)
		{
			int t = this.tail;
			final int[][] d = this.data;// java

			int sample = input_offset * channels;
			for( int wide_sample = 0; wide_sample < wide_samples; wide_sample++ ) {
				for( int channel = 0; channel < channels; channel++ ) {
					d[channel][t] = input[sample++];
				}
				t++;
			}
			this.tail = t;

			//FLAC__ASSERT(fifo->tail <= fifo->size);
		}
	}

	private static final class Jverify_output {
		byte[] data = null;
		int offset = 0;
		//int capacity = 0;// FIXME never used field
		int bytes = 0;
	}

	//typedef enum {
		private static final int ENCODER_IN_MAGIC = 0;
		private static final int ENCODER_IN_METADATA = 1;
		private static final int ENCODER_IN_AUDIO = 2;
	//} EncoderStateHint;

	private static class CompressionLevels {
		private final boolean do_mid_side_stereo;
		private final boolean loose_mid_side_stereo;
		private final int max_lpc_order;
		private final int qlp_coeff_precision;
		private final boolean do_qlp_coeff_prec_search;
		private final boolean do_escape_coding;
		private final boolean do_exhaustive_model_search;
		private final int min_residual_partition_order;
		private final int max_residual_partition_order;
		private final int rice_parameter_search_dist;
		private final String apodization;

		private CompressionLevels(
					final boolean is_do_mid_side_stereo,
					final boolean is_loose_mid_side_stereo,
					final int i_max_lpc_order,
					final int i_qlp_coeff_precision,
					final boolean is_do_qlp_coeff_prec_search,
					final boolean is_do_escape_coding,
					final boolean is_do_exhaustive_model_search,
					final int i_min_residual_partition_order,
					final int i_max_residual_partition_order,
					final int i_rice_parameter_search_dist,
					final String s_apodization) {
			do_mid_side_stereo = is_do_mid_side_stereo;
			loose_mid_side_stereo = is_loose_mid_side_stereo;
			max_lpc_order = i_max_lpc_order;
			qlp_coeff_precision = i_qlp_coeff_precision;
			do_qlp_coeff_prec_search = is_do_qlp_coeff_prec_search;
			do_escape_coding = is_do_escape_coding;
			do_exhaustive_model_search = is_do_exhaustive_model_search;
			min_residual_partition_order = i_min_residual_partition_order;
			max_residual_partition_order = i_max_residual_partition_order;
			rice_parameter_search_dist = i_rice_parameter_search_dist;
			apodization = s_apodization;
		}
	}
	private static final CompressionLevels compression_levels_[] = {
		new CompressionLevels( false, false,  0, 0, false, false, false, 0, 3, 0, "tukey(5e-1)" ),
		new CompressionLevels( true , true ,  0, 0, false, false, false, 0, 3, 0, "tukey(5e-1)" ),
		new CompressionLevels( true , false,  0, 0, false, false, false, 0, 3, 0, "tukey(5e-1)" ),
		new CompressionLevels( false, false,  6, 0, false, false, false, 0, 4, 0, "tukey(5e-1)" ),
		new CompressionLevels( true , true ,  8, 0, false, false, false, 0, 4, 0, "tukey(5e-1)" ),
		new CompressionLevels( true , false,  8, 0, false, false, false, 0, 5, 0, "tukey(5e-1)" ),
		new CompressionLevels( true , false,  8, 0, false, false, false, 0, 6, 0, "tukey(5e-1);partial_tukey(2)" ),
		new CompressionLevels( true , false, 12, 0, false, false, false, 0, 6, 0, "tukey(5e-1);partial_tukey(2)" ),
		new CompressionLevels( true , false, 12, 0, false, false, false, 0, 6, 0, "tukey(5e-1);partial_tukey(2);punchout_tukey(3)" )
		/* here we use locale-independent 5e-1 instead of 0.5 or 0,5 */
	};

	//private static class JFLAC__StreamEncoderPrivate {// now replaced by 'private'
	private int input_capacity;                          /* current size (in samples) of the signal and residual buffers */
	private final int integer_signal[][] = new int[Jformat.FLAC__MAX_CHANNELS][];  /* the integer version of the input signal */
	private final int integer_signal_mid_side[][] = new int[2][];          /* the integer version of the mid-side input signal (stereo only) */
//#ifndef FLAC__INTEGER_ONLY_LIBRARY
	//private final float real_signal[][] = new float[Jformat.FLAC__MAX_CHANNELS][];      /* (@@@ currently unused) the floating-point version of the input signal */
	private final float real_signal_mid_side[][] = new float[2][];              /* (@@@ currently unused) the floating-point version of the mid-side input signal (stereo only) */
	private final float window[][] = new float[FLAC__MAX_APODIZATION_FUNCTIONS][]; /* the pre-computed floating-point window for each apodization function */
	private float[] windowed_signal;                      /* the integer_signal[] * current window[] */
//#endif
	private final int subframe_bps[] = new int[Jformat.FLAC__MAX_CHANNELS];        /* the effective bits per sample of the input signal (stream bps - wasted bits) */
	private final int subframe_bps_mid_side[] = new int[2];                /* the effective bits per sample of the mid-side input signal (stream bps - wasted bits + 0/1) */
	private final int residual_workspace[][][] = new int[Jformat.FLAC__MAX_CHANNELS][2][]; /* each channel has a candidate and best workspace where the subframe residual signals will be stored */
	private final int residual_workspace_mid_side[][][] = new int[2][2][];
	private final JFLAC__Subframe subframe_workspace[][] = new JFLAC__Subframe[Jformat.FLAC__MAX_CHANNELS][2];
	private final JFLAC__Subframe subframe_workspace_mid_side[][] = new JFLAC__Subframe[2][2];
		//final JFLAC__Subframe subframe_workspace_ptr[][] = new JFLAC__Subframe[Jformat.FLAC__MAX_CHANNELS][2];// FIXME why do not use &subframe_workspace?
		//final JFLAC__Subframe subframe_workspace_ptr_mid_side[][] = new JFLAC__Subframe[2][2];// FIXME why do not use &subframe_workspace_mid_side?
	private final JFLAC__EntropyCodingMethod_PartitionedRiceContents partitioned_rice_contents_workspace[][] = new JFLAC__EntropyCodingMethod_PartitionedRiceContents[Jformat.FLAC__MAX_CHANNELS][2];
	private final JFLAC__EntropyCodingMethod_PartitionedRiceContents partitioned_rice_contents_workspace_mid_side[][] = new JFLAC__EntropyCodingMethod_PartitionedRiceContents[Jformat.FLAC__MAX_CHANNELS][2];
		//final JFLAC__EntropyCodingMethod_PartitionedRiceContents partitioned_rice_contents_workspace_ptr[][] = new JFLAC__EntropyCodingMethod_PartitionedRiceContents[Jformat.FLAC__MAX_CHANNELS][2];// FIXME why do not use &?
		//final JFLAC__EntropyCodingMethod_PartitionedRiceContents partitioned_rice_contents_workspace_ptr_mid_side[][] = new JFLAC__EntropyCodingMethod_PartitionedRiceContents[Jformat.FLAC__MAX_CHANNELS][2];// FIXME why do not use &?
	private final int best_subframe[] = new int[Jformat.FLAC__MAX_CHANNELS];       /* index (0 or 1) into 2nd dimension of the above workspaces */
	private final int best_subframe_mid_side[] = new int[2];
	private final int best_subframe_bits[] = new int[Jformat.FLAC__MAX_CHANNELS];  /* size in bits of the best subframe for each channel */
	private final int best_subframe_bits_mid_side[] = new int[2];
	private long[] abs_residual_partition_sums;/* workspace where the sum of abs(candidate residual) for each partition is stored */
	private int[] raw_bits_per_partition;/* workspace where the sum of silog2(candidate residual) for each partition is stored */
	private JFLAC__BitWriter frame;/* the current frame being worked on */
	private int loose_mid_side_stereo_frames;/* rounded number of frames the encoder will use before trying both independent and mid/side frames again */
	private int loose_mid_side_stereo_frame_count;/* number of frames using the current channel assignment */
	private int /* FLAC__ChannelAssignment */ last_channel_assignment;
	private final JFLAC__StreamMetadata_StreamInfo streaminfo = new JFLAC__StreamMetadata_StreamInfo(); /* scratchpad for STREAMINFO as it is built */
	private JFLAC__StreamMetadata_SeekTable seek_table;/* pointer into encoder->protected_->metadata_ where the seek table is */
	private int current_sample_number;
	private int current_frame_number;
	private final JFLAC__MD5Context md5context = new JFLAC__MD5Context();
		//FLAC__CPUInfo cpuinfo;
//#ifndef FLAC__INTEGER_ONLY_LIBRARY
		//unsigned (*local_fixed_compute_best_predictor)(const FLAC__int32 data[], unsigned data_len, FLAC__float residual_bits_per_sample[FLAC__MAX_FIXED_ORDER + 1]);
//#else
//		unsigned (*local_fixed_compute_best_predictor)(const FLAC__int32 data[], unsigned data_len, FLAC__fixedpoint residual_bits_per_sample[FLAC__MAX_FIXED_ORDER+1]);
//#endif
//#ifndef FLAC__INTEGER_ONLY_LIBRARY
		//void (*local_lpc_compute_autocorrelation)(const FLAC__real data[], unsigned data_len, unsigned lag, FLAC__real autoc[]);
		//void (*local_lpc_compute_residual_from_qlp_coefficients)(const FLAC__int32 *data, unsigned data_len, const FLAC__int32 qlp_coeff[], unsigned order, int lp_quantization, FLAC__int32 residual[]);
		//void (*local_lpc_compute_residual_from_qlp_coefficients_64bit)(const FLAC__int32 *data, unsigned data_len, const FLAC__int32 qlp_coeff[], unsigned order, int lp_quantization, FLAC__int32 residual[]);
		//void (*local_lpc_compute_residual_from_qlp_coefficients_16bit)(const FLAC__int32 *data, unsigned data_len, const FLAC__int32 qlp_coeff[], unsigned order, int lp_quantization, FLAC__int32 residual[]);
//#endif
	private boolean disable_constant_subframes;
	private boolean disable_fixed_subframes;
	private boolean disable_verbatim_subframes;
	private boolean is_ogg;
	private JFLAC__StreamEncoderReadCallback read_callback; /* currently only needed for Ogg FLAC */
	private JFLAC__StreamEncoderSeekCallback seek_callback;
	private JFLAC__StreamEncoderTellCallback tell_callback;
	private JFLAC__StreamEncoderWriteCallback write_callback;
	private JFLAC__StreamEncoderMetadataCallback metadata_callback;
	private JFLAC__StreamEncoderProgressCallback progress_callback;
	// private Object client_data;// java: don't need
	private int first_seekpoint_to_check;
	private OutputStream file; /* only used when encoding to a file */
	private long bytes_written;
	private long samples_written;
	private int frames_written;
	private int total_frames_estimate;
	/* unaligned (original) pointers to allocated data */
	//private final int integer_signal_unaligned[][] = new int[Jformat.FLAC__MAX_CHANNELS][];
	//private final int integer_signal_mid_side_unaligned[][] = new int[2][];
//#ifndef FLAC__INTEGER_ONLY_LIBRARY
	//private final float real_signal_unaligned[][] = new float[Jformat.FLAC__MAX_CHANNELS][]; /* (@@@ currently unused) */
	//private final float real_signal_mid_side_unaligned[][] = new float[2][]; /* (@@@ currently unused) */
	//private final float window_unaligned[][] = new float[FLAC__MAX_APODIZATION_FUNCTIONS][];
	//private float[] windowed_signal_unaligned;
//#endif
	//private final int residual_workspace_unaligned[][][] = new int[Jformat.FLAC__MAX_CHANNELS][2][];
	//private final int residual_workspace_mid_side_unaligned[][][] = new int[2][2][];
	//private long[] abs_residual_partition_sums_unaligned;
	//private int[] raw_bits_per_partition_unaligned;
		/*
		 * These fields have been moved here from private function local
		 * declarations merely to save stack space during encoding.
		 */
//#ifndef FLAC__INTEGER_ONLY_LIBRARY
	private final float lp_coeff[][] = new float[Jformat.FLAC__MAX_LPC_ORDER][Jformat.FLAC__MAX_LPC_ORDER]; /* from process_subframe_() */
//#endif
	private final JFLAC__EntropyCodingMethod_PartitionedRiceContents partitioned_rice_contents_extra[] = new JFLAC__EntropyCodingMethod_PartitionedRiceContents[2]; /* from find_best_partition_order_() */
		/*
		 * The data for the verify section
		 */
	private static final class Jverify {
		private JFLAC__StreamDecoder decoder;
		private int /* EncoderStateHint */ state_hint;
		private boolean needs_magic_hack;
		private final Jverify_input_fifo input_fifo = new Jverify_input_fifo();
		private final Jverify_output output = new Jverify_output();

		private final Jerror_stats error_stats = new Jerror_stats();
	}
	private final Jverify verify = new Jverify();
	private boolean is_being_deleted; /* if true, call to ..._finish() from ..._delete() will not call the callbacks */

		/*private JFLAC__StreamEncoderPrivate() {// creating moved to base class constructor
			for( int i = 0; i < 2; i++ ) {
				for( int k = 0; k < 2; k++ ) {
					subframe_workspace_mid_side[k][i] = new JFLAC__Subframe();
				}
				for( int c = 0; c < Jformat.FLAC__MAX_CHANNELS; c++ ) {
					subframe_workspace[c][i] = new JFLAC__Subframe();
					partitioned_rice_contents_workspace[c][i] = new JFLAC__EntropyCodingMethod_PartitionedRiceContents();
					partitioned_rice_contents_workspace_mid_side[c][i] = new JFLAC__EntropyCodingMethod_PartitionedRiceContents();
				}
				partitioned_rice_contents_extra[i] = new JFLAC__EntropyCodingMethod_PartitionedRiceContents();
			}
		}*/
	//}// FLAC__StreamEncoderPrivate;
	//JFLAC__StreamEncoderPrivate private_; /* avoid the C++ keyword 'private' */

	/** Number of samples that will be overread to watch for end of stream.  By
	 * 'overread', we mean that the FLAC__stream_encoder_process*() calls will
	 * always try to read blocksize+1 samples before encoding a block, so that
	 * even if the stream has a total sample count that is an integral multiple
	 * of the blocksize, we will still notice when we are encoding the last
	 * block.  This is needed, for example, to correctly set the end-of-stream
	 * marker in Ogg FLAC.
	 *
	 * WATCHOUT: some parts of the code assert that OVERREAD_ == 1 and there's
	 * not really any reason to change it.
	 */
	private static final int OVERREAD_ = 1;

	/***********************************************************************
	 *
	 * Class constructor/destructor
	 *
	 */
	public JFLAC__StreamEncoder()// FLAC__stream_encoder_new()
	{
		//JFLAC__StreamEncoder encoder;
		//FLAC__ASSERT(sizeof(int) >= 4); /* we want to die right away if this is not true */

		//encoder = new JFLAC__StreamEncoder();

		//encoder.protected_ = new JFLAC__StreamEncoderProtected();
		for( int i = 0; i < this.apodizations.length; i++ ) {
			this.apodizations[i] = new JFLAC__ApodizationSpecification();
		}

		//encoder.private_ = new JFLAC__StreamEncoderPrivate();
		for( int i = 0; i < 2; i++ ) {
			for( int k = 0; k < 2; k++ ) {
				this.subframe_workspace_mid_side[k][i] = new JFLAC__Subframe();
			}
			for( int c = 0; c < Jformat.FLAC__MAX_CHANNELS; c++ ) {
				this.subframe_workspace[c][i] = new JFLAC__Subframe();
				this.partitioned_rice_contents_workspace[c][i] = new JFLAC__EntropyCodingMethod_PartitionedRiceContents();
				this.partitioned_rice_contents_workspace_mid_side[c][i] = new JFLAC__EntropyCodingMethod_PartitionedRiceContents();
			}
			this.partitioned_rice_contents_extra[i] = new JFLAC__EntropyCodingMethod_PartitionedRiceContents();
		}

		this.frame = new JFLAC__BitWriter();

		this.file = null;

		set_defaults_();

		this.is_being_deleted = false;

		/*for( i = 0; i < Jformat.FLAC__MAX_CHANNELS; i++ ) {
			encoder.private_.subframe_workspace_ptr[i][0] = encoder.private_.subframe_workspace[i][0];
			encoder.private_.subframe_workspace_ptr[i][1] = encoder.private_.subframe_workspace[i][1];
		}
		for( i = 0; i < 2; i++ ) {
			encoder.private_.subframe_workspace_ptr_mid_side[i][0] = encoder.private_.subframe_workspace_mid_side[i][0];
			encoder.private_.subframe_workspace_ptr_mid_side[i][1] = encoder.private_.subframe_workspace_mid_side[i][1];
		}
		for( i = 0; i < Jformat.FLAC__MAX_CHANNELS; i++ ) {
			encoder.private_.partitioned_rice_contents_workspace_ptr[i][0] = encoder.private_.partitioned_rice_contents_workspace[i][0];
			encoder.private_.partitioned_rice_contents_workspace_ptr[i][1] = encoder.private_.partitioned_rice_contents_workspace[i][1];
		}
		for( i = 0; i < 2; i++ ) {
			encoder.private_.partitioned_rice_contents_workspace_ptr_mid_side[i][0] = encoder.private_.partitioned_rice_contents_workspace_mid_side[i][0];
			encoder.private_.partitioned_rice_contents_workspace_ptr_mid_side[i][1] = encoder.private_.partitioned_rice_contents_workspace_mid_side[i][1];
		}*/

		for( int i = 0; i < Jformat.FLAC__MAX_CHANNELS; i++ ) {
			this.partitioned_rice_contents_workspace[i][0].FLAC__format_entropy_coding_method_partitioned_rice_contents_init();
			this.partitioned_rice_contents_workspace[i][1].FLAC__format_entropy_coding_method_partitioned_rice_contents_init(  );
		}
		for( int i = 0; i < 2; i++ ) {
			this.partitioned_rice_contents_workspace_mid_side[i][0].FLAC__format_entropy_coding_method_partitioned_rice_contents_init();
			this.partitioned_rice_contents_workspace_mid_side[i][1].FLAC__format_entropy_coding_method_partitioned_rice_contents_init();
		}
		for( int i = 0; i < 2; i++ ) {
			this.partitioned_rice_contents_extra[i].FLAC__format_entropy_coding_method_partitioned_rice_contents_init();
		}

		this.state = FLAC__STREAM_ENCODER_UNINITIALIZED;

		//return encoder;
	}

	public final void FLAC__stream_encoder_delete()
	{
		//FLAC__ASSERT(0 != encoder);
		//FLAC__ASSERT(0 != encoder->protected_);
		//FLAC__ASSERT(0 != encoder->private_);
		//FLAC__ASSERT(0 != encoder->private_->frame);

		this.is_being_deleted = true;

		FLAC__stream_encoder_finish();

		if( null != this.verify.decoder ) {
			this.verify.decoder.FLAC__stream_decoder_delete();
		}

		for( int i = 0; i < Jformat.FLAC__MAX_CHANNELS; i++ ) {
			this.partitioned_rice_contents_workspace[i][0].FLAC__format_entropy_coding_method_partitioned_rice_contents_clear();
			this.partitioned_rice_contents_workspace[i][1].FLAC__format_entropy_coding_method_partitioned_rice_contents_clear();
		}
		for( int i = 0; i < 2; i++ ) {
			this.partitioned_rice_contents_workspace_mid_side[i][0].FLAC__format_entropy_coding_method_partitioned_rice_contents_clear();
			this.partitioned_rice_contents_workspace_mid_side[i][1].FLAC__format_entropy_coding_method_partitioned_rice_contents_clear();
		}
		for( int i = 0; i < 2; i++ ) {
			this.partitioned_rice_contents_extra[i].FLAC__format_entropy_coding_method_partitioned_rice_contents_clear();
		}

		this.frame = null;
		//encoder.private_ = null;
		//encoder.protected_ = null;
		//free( encoder );
	}

	/***********************************************************************
	 *
	 * Public class methods
	 *
	 ***********************************************************************/

	private final int /* FLAC__StreamEncoderInitStatus */ init_stream_internal_(
		final JFLAC__StreamEncoderReadCallback read_cb,
		final JFLAC__StreamEncoderWriteCallback write_cb,
		final JFLAC__StreamEncoderSeekCallback seek_cb,
		final JFLAC__StreamEncoderTellCallback tell_cb,
		final JFLAC__StreamEncoderMetadataCallback metadata_cb,
		// final Object client_data,
		final boolean isogg
	)
	{
		//FLAC__ASSERT(0 != encoder);

		if( this.state != FLAC__STREAM_ENCODER_UNINITIALIZED ) {
			return FLAC__STREAM_ENCODER_INIT_STATUS_ALREADY_INITIALIZED;
		}

		if( ! Jformat.FLAC__HAS_OGG && isogg ) {
			return FLAC__STREAM_ENCODER_INIT_STATUS_UNSUPPORTED_CONTAINER;
		}

		if( null == write_cb || (seek_cb != null && null == tell_cb) ) {
			return FLAC__STREAM_ENCODER_INIT_STATUS_INVALID_CALLBACKS;
		}

		if( this.channels == 0 || this.channels > Jformat.FLAC__MAX_CHANNELS ) {
			return FLAC__STREAM_ENCODER_INIT_STATUS_INVALID_NUMBER_OF_CHANNELS;
		}

		if( this.channels != 2 ) {
			this.do_mid_side_stereo = false;
			this.loose_mid_side_stereo = false;
		}
		else if( ! this.do_mid_side_stereo ) {
			this.loose_mid_side_stereo = false;
		}

		if( this.bits_per_sample >= 32 ) {
			this.do_mid_side_stereo = false; /* since we currenty do 32-bit math, the side channel would have 33 bps and overflow */
		}

		if( this.bits_per_sample < Jformat.FLAC__MIN_BITS_PER_SAMPLE || this.bits_per_sample > Jformat.FLAC__REFERENCE_CODEC_MAX_BITS_PER_SAMPLE ) {
			return FLAC__STREAM_ENCODER_INIT_STATUS_INVALID_BITS_PER_SAMPLE;
		}

		if( ! Jformat.FLAC__format_sample_rate_is_valid( this.sample_rate ) ) {
			return FLAC__STREAM_ENCODER_INIT_STATUS_INVALID_SAMPLE_RATE;
		}

		if( this.blocksize == 0 ) {
			if( this.max_lpc_order == 0 ) {
				this.blocksize = 1152;
			} else {
				this.blocksize = 4096;
			}
		}

		if( this.blocksize < Jformat.FLAC__MIN_BLOCK_SIZE || this.blocksize > Jformat.FLAC__MAX_BLOCK_SIZE ) {
			return FLAC__STREAM_ENCODER_INIT_STATUS_INVALID_BLOCK_SIZE;
		}

		if( this.max_lpc_order > Jformat.FLAC__MAX_LPC_ORDER ) {
			return FLAC__STREAM_ENCODER_INIT_STATUS_INVALID_MAX_LPC_ORDER;
		}

		if( this.blocksize < this.max_lpc_order ) {
			return FLAC__STREAM_ENCODER_INIT_STATUS_BLOCK_SIZE_TOO_SMALL_FOR_LPC_ORDER;
		}

		if( this.qlp_coeff_precision == 0 ) {
			if( this.bits_per_sample < 16 ) {
				/* @@@ need some data about how to set this here w.r.t. blocksize and sample rate */
				/* @@@ until then we'll make a guess */
				int i = 2 + this.bits_per_sample >>> 1;
				if( i < Jformat.FLAC__MIN_QLP_COEFF_PRECISION ) {
					i = Jformat.FLAC__MIN_QLP_COEFF_PRECISION;
				}
				this.qlp_coeff_precision = i;
			}
			else if( this.bits_per_sample == 16 ) {
				if( this.blocksize <= 192 ) {
					this.qlp_coeff_precision = 7;
				} else if( this.blocksize <= 384 ) {
					this.qlp_coeff_precision = 8;
				} else if( this.blocksize <= 576 ) {
					this.qlp_coeff_precision = 9;
				} else if( this.blocksize <= 1152 ) {
					this.qlp_coeff_precision = 10;
				} else if( this.blocksize <= 2304 ) {
					this.qlp_coeff_precision = 11;
				} else if( this.blocksize <= 4608 ) {
					this.qlp_coeff_precision = 12;
				} else {
					this.qlp_coeff_precision = 13;
				}
			}
			else {
				if( this.blocksize <= 384 ) {
					this.qlp_coeff_precision = Jformat.FLAC__MAX_QLP_COEFF_PRECISION - 2;
				} else if( this.blocksize <= 1152 ) {
					this.qlp_coeff_precision = Jformat.FLAC__MAX_QLP_COEFF_PRECISION - 1;
				} else {
					this.qlp_coeff_precision = Jformat.FLAC__MAX_QLP_COEFF_PRECISION;
				}
			}
			//FLAC__ASSERT(encoder.protected_.qlp_coeff_precision <= FLAC__MAX_QLP_COEFF_PRECISION);
		}
		else if( this.qlp_coeff_precision < Jformat.FLAC__MIN_QLP_COEFF_PRECISION || this.qlp_coeff_precision > Jformat.FLAC__MAX_QLP_COEFF_PRECISION ) {
			return FLAC__STREAM_ENCODER_INIT_STATUS_INVALID_QLP_COEFF_PRECISION;
		}

		if( this.streamable_subset ) {
			if( ! Jformat.FLAC__format_blocksize_is_subset( this.blocksize, this.sample_rate ) ) {
				return FLAC__STREAM_ENCODER_INIT_STATUS_NOT_STREAMABLE;
			}
			if( ! Jformat.FLAC__format_sample_rate_is_subset( this.sample_rate ) ) {
				return FLAC__STREAM_ENCODER_INIT_STATUS_NOT_STREAMABLE;
			}
			if(
				this.bits_per_sample != 8 &&
				this.bits_per_sample != 12 &&
				this.bits_per_sample != 16 &&
				this.bits_per_sample != 20 &&
				this.bits_per_sample != 24
			) {
				return FLAC__STREAM_ENCODER_INIT_STATUS_NOT_STREAMABLE;
			}
			if( this.max_residual_partition_order > Jformat.FLAC__SUBSET_MAX_RICE_PARTITION_ORDER ) {
				return FLAC__STREAM_ENCODER_INIT_STATUS_NOT_STREAMABLE;
			}
			if(
				this.sample_rate <= 48000 &&
				(
					this.blocksize > Jformat.FLAC__SUBSET_MAX_BLOCK_SIZE_48000HZ ||
					this.max_lpc_order > Jformat.FLAC__SUBSET_MAX_LPC_ORDER_48000HZ
				)
			) {
				return FLAC__STREAM_ENCODER_INIT_STATUS_NOT_STREAMABLE;
			}
		}

		if( this.max_residual_partition_order >= (1 << Jformat.FLAC__ENTROPY_CODING_METHOD_PARTITIONED_RICE_ORDER_LEN) ) {
			this.max_residual_partition_order = (1 << Jformat.FLAC__ENTROPY_CODING_METHOD_PARTITIONED_RICE_ORDER_LEN) - 1;
		}
		if( this.min_residual_partition_order >= this.max_residual_partition_order ) {
			this.min_residual_partition_order = this.max_residual_partition_order;
		}

		final JFLAC__StreamMetadata[] meta_data = this.metadata;// java
if( Jformat.FLAC__HAS_OGG ) {
		/* reorder metadata if necessary to ensure that any VORBIS_COMMENT is the first, according to the mapping spec */
		if( isogg && null != meta_data/* && this.num_metadata_blocks > 1*/ ) {
			for( int i = 1, ie = meta_data.length; i < ie/* this.num_metadata_blocks */; i++ ) {
				if( null != meta_data[i] && meta_data[i].type == Jformat.FLAC__METADATA_TYPE_VORBIS_COMMENT ) {
					final JFLAC__StreamMetadata vc = meta_data[i];
					for( ; i > 0; i--) {
						meta_data[i] = meta_data[i - 1];
					}
					meta_data[0] = vc;
					break;
				}
			}
		}
}
		/* keep track of any SEEKTABLE block */
		if( null != meta_data/* && metadata_blocks > 0*/ ) {
			for( int i = 0, ie = meta_data.length; i < ie/* this.num_metadata_blocks */; i++ ) {
				if( null != meta_data[i] && meta_data[i].type == Jformat.FLAC__METADATA_TYPE_SEEKTABLE ) {
					this.seek_table = (JFLAC__StreamMetadata_SeekTable)meta_data[i];
					break; /* take only the first one */
				}
			}
		}

		/* validate metadata */
		/* if( null == meta_data && this.num_metadata_blocks > 0 ) {
			return FLAC__STREAM_ENCODER_INIT_STATUS_INVALID_METADATA;
		}*/
		boolean metadata_has_vorbis_comment = false;
		if( null != meta_data ) {// java
			boolean metadata_has_seektable = false;// FIXME never uses metadata_has_seektable, metadata_picture_has_type1, metadata_picture_has_type2
			boolean metadata_picture_has_type1 = false;
			boolean metadata_picture_has_type2 = false;
			for( int i = 0, ie = meta_data.length; i < ie/* this.num_metadata_blocks */; i++ ) {
				final JFLAC__StreamMetadata m = meta_data[i];
				if( m.type == Jformat.FLAC__METADATA_TYPE_STREAMINFO ) {
					return FLAC__STREAM_ENCODER_INIT_STATUS_INVALID_METADATA;
				} else if( m.type == Jformat.FLAC__METADATA_TYPE_SEEKTABLE ) {
					if( metadata_has_seektable ) {
						return FLAC__STREAM_ENCODER_INIT_STATUS_INVALID_METADATA;
					}
					metadata_has_seektable = true;
					if( ! ((JFLAC__StreamMetadata_SeekTable)m).FLAC__format_seektable_is_legal() ) {
						return FLAC__STREAM_ENCODER_INIT_STATUS_INVALID_METADATA;
					}
				}
				else if( m.type == Jformat.FLAC__METADATA_TYPE_VORBIS_COMMENT ) {
					if( metadata_has_vorbis_comment ) {
						return FLAC__STREAM_ENCODER_INIT_STATUS_INVALID_METADATA;
					}
					metadata_has_vorbis_comment = true;
				}
				else if( m.type == Jformat.FLAC__METADATA_TYPE_CUESHEET ) {
					if( null != ((JFLAC__StreamMetadata_CueSheet)m).FLAC__format_cuesheet_is_legal( ((JFLAC__StreamMetadata_CueSheet)m).is_cd ) ) {
						return FLAC__STREAM_ENCODER_INIT_STATUS_INVALID_METADATA;
					}
				}
				else if( m.type == Jformat.FLAC__METADATA_TYPE_PICTURE ) {
					final JFLAC__StreamMetadata_Picture picture = (JFLAC__StreamMetadata_Picture)m;
					if( null != picture.FLAC__format_picture_is_legal() ) {
						return FLAC__STREAM_ENCODER_INIT_STATUS_INVALID_METADATA;
					}
					if( picture.picture_type == Jformat.FLAC__STREAM_METADATA_PICTURE_TYPE_FILE_ICON_STANDARD ) {
						if( metadata_picture_has_type1 ) {
							return FLAC__STREAM_ENCODER_INIT_STATUS_INVALID_METADATA;
						}
						metadata_picture_has_type1 = true;
						/* standard icon must be 32x32 pixel PNG */
						if(
							picture.picture_type == Jformat.FLAC__STREAM_METADATA_PICTURE_TYPE_FILE_ICON_STANDARD &&
							(
								(! picture.mime_type.equals("image/png") && ! picture.mime_type.equals("-.") ) ||
									picture.width != 32 ||
										picture.height != 32
							)
						) {
							return FLAC__STREAM_ENCODER_INIT_STATUS_INVALID_METADATA;
						}
					}
					else if( picture.picture_type == Jformat.FLAC__STREAM_METADATA_PICTURE_TYPE_FILE_ICON ) {
						if( metadata_picture_has_type2 ) {
							return FLAC__STREAM_ENCODER_INIT_STATUS_INVALID_METADATA;
						}
						metadata_picture_has_type2 = true;
					}
				}
			}
		}

		this.input_capacity = 0;
/*		for( i = 0; i < this.channels; i++ ) {
			this.integer_signal_unaligned[i] = this.integer_signal[i] = null;
//#ifndef FLAC__INTEGER_ONLY_LIBRARY
			this.real_signal_unaligned[i] = this.real_signal[i] = null;
//#endif
		}*/
		for( int i = 0; i < 2; i++ ) {
			/*this.integer_signal_mid_side_unaligned[i] = */this.integer_signal_mid_side[i] = null;
//#ifndef FLAC__INTEGER_ONLY_LIBRARY
			/*this.real_signal_mid_side_unaligned[i] = */this.real_signal_mid_side[i] = null;
//#endif
		}
//#ifndef FLAC__INTEGER_ONLY_LIBRARY
		for( int i = 0; i < this.num_apodizations; i++ ) {
			/*this.window_unaligned[i] = */this.window[i] = null;
		}
		/*this.windowed_signal_unaligned = */this.windowed_signal = null;
//#endif
		for( int i = 0; i < this.channels; i++ ) {
			/*this.residual_workspace_unaligned[i][0] = */this.residual_workspace[i][0] = null;
			/*this.residual_workspace_unaligned[i][1] = */this.residual_workspace[i][1] = null;
			this.best_subframe[i] = 0;
		}
		for( int i = 0; i < 2; i++ ) {
			/*this.residual_workspace_mid_side_unaligned[i][0] = */this.residual_workspace_mid_side[i][0] = null;
			/*this.residual_workspace_mid_side_unaligned[i][1] = */this.residual_workspace_mid_side[i][1] = null;
			this.best_subframe_mid_side[i] = 0;
		}
		/*this.abs_residual_partition_sums_unaligned = */this.abs_residual_partition_sums = null;
		/*this.raw_bits_per_partition_unaligned = */this.raw_bits_per_partition = null;
//#ifndef FLAC__INTEGER_ONLY_LIBRARY
		this.loose_mid_side_stereo_frames = (int)((double)this.sample_rate * 0.4 / (double)this.blocksize + 0.5);
//#else
		/* 26214 is the approximate fixed-point equivalent to 0.4 (0.4 * 2^16) */
		/* sample rate can be up to 655350 Hz, and thus use 20 bits, so we do the multiply&divide by hand */
/*		FLAC__ASSERT(FLAC__MAX_SAMPLE_RATE <= 655350);
		FLAC__ASSERT(FLAC__MAX_BLOCK_SIZE <= 65535);
		FLAC__ASSERT(encoder.protected_.sample_rate <= 655350);
		FLAC__ASSERT(encoder.protected_.blocksize <= 65535);
		encoder.private_.loose_mid_side_stereo_frames = (unsigned)FLAC__fixedpoint_trunc((((FLAC__uint64)(encoder.protected_.sample_rate) * (FLAC__uint64)(26214)) << 16) / (encoder.protected_.blocksize<<16) + FLAC__FP_ONE_HALF);
#endif*/
		if( this.loose_mid_side_stereo_frames == 0 ) {
			this.loose_mid_side_stereo_frames = 1;
		}
		this.loose_mid_side_stereo_frame_count = 0;
		this.current_sample_number = 0;
		this.current_frame_number = 0;

		/*
		 * get the CPU info and set the function pointers
		 */
		//FLAC__cpu_info(&encoder.private_.cpuinfo);
		/* first default to the non-asm routines */
//#ifndef FLAC__INTEGER_ONLY_LIBRARY
		//encoder.private_.local_lpc_compute_autocorrelation = FLAC__lpc_compute_autocorrelation;
//#endif
		//encoder.private_.local_precompute_partition_info_sums = precompute_partition_info_sums_;
		//encoder.private_.local_fixed_compute_best_predictor = FLAC__fixed_compute_best_predictor;
		//encoder.private_.local_fixed_compute_best_predictor_wide = FLAC__fixed_compute_best_predictor_wide;
//#ifndef FLAC__INTEGER_ONLY_LIBRARY
		//encoder.private_.local_lpc_compute_residual_from_qlp_coefficients = FLAC__lpc_compute_residual_from_qlp_coefficients;
		//encoder.private_.local_lpc_compute_residual_from_qlp_coefficients_64bit = FLAC__lpc_compute_residual_from_qlp_coefficients_wide;
		//encoder.private_.local_lpc_compute_residual_from_qlp_coefficients_16bit = FLAC__lpc_compute_residual_from_qlp_coefficients;
//#endif
		/* now override with asm where appropriate */
/*#ifndef FLAC__INTEGER_ONLY_LIBRARY
# ifndef FLAC__NO_ASM
		if( encoder.private_.cpuinfo.use_asm ) {
#  ifdef FLAC__CPU_IA32
			FLAC__ASSERT(encoder.private_.cpuinfo.type == FLAC__CPUINFO_TYPE_IA32);
#   ifdef FLAC__HAS_NASM
			if( encoder.private_.cpuinfo.ia32.sse ) {
				if( encoder.protected_.max_lpc_order < 4)
					encoder.private_.local_lpc_compute_autocorrelation = FLAC__lpc_compute_autocorrelation_asm_ia32_sse_lag_4_old;
				else if( encoder.protected_.max_lpc_order < 8 )
					encoder.private_.local_lpc_compute_autocorrelation = FLAC__lpc_compute_autocorrelation_asm_ia32_sse_lag_8_old;
				else if( encoder.protected_.max_lpc_order < 12 )
					encoder.private_.local_lpc_compute_autocorrelation = FLAC__lpc_compute_autocorrelation_asm_ia32_sse_lag_12_old;
				else if( encoder.protected_.max_lpc_order < 16 )
					encoder.private_.local_lpc_compute_autocorrelation = FLAC__lpc_compute_autocorrelation_asm_ia32_sse_lag_16_old;
				else
					encoder.private_.local_lpc_compute_autocorrelation = FLAC__lpc_compute_autocorrelation_asm_ia32;
			}
			else
				encoder.private_.local_lpc_compute_autocorrelation = FLAC__lpc_compute_autocorrelation_asm_ia32;
			encoder.private_.local_lpc_compute_residual_from_qlp_coefficients_64bit = FLAC__lpc_compute_residual_from_qlp_coefficients_wide_asm_ia32; // OPT_IA32: was really necessary for GCC < 4.9

			if( encoder.private_.cpuinfo.ia32.mmx ) {
				encoder.private_.local_lpc_compute_residual_from_qlp_coefficients = FLAC__lpc_compute_residual_from_qlp_coefficients_asm_ia32;
				encoder.private_.local_lpc_compute_residual_from_qlp_coefficients_16bit = FLAC__lpc_compute_residual_from_qlp_coefficients_asm_ia32_mmx;
			}
			else {
				encoder.private_.local_lpc_compute_residual_from_qlp_coefficients = FLAC__lpc_compute_residual_from_qlp_coefficients_asm_ia32;
				encoder.private_.local_lpc_compute_residual_from_qlp_coefficients_16bit = FLAC__lpc_compute_residual_from_qlp_coefficients_asm_ia32;
			}
			if( encoder.private_.cpuinfo.ia32.mmx && encoder.private_.cpuinfo.ia32.cmov )
				encoder.private_.local_fixed_compute_best_predictor = FLAC__fixed_compute_best_predictor_asm_ia32_mmx_cmov;
#   endif /* FLAC__HAS_NASM */
/*#   if FLAC__HAS_X86INTRIN
#    if defined FLAC__SSE_SUPPORTED
		if(encoder->private_->cpuinfo.ia32.sse) {
			if(encoder->private_->cpuinfo.ia32.sse42 || !encoder->private_->cpuinfo.ia32.intel) { // use new autocorrelation functions
				if(encoder->protected_->max_lpc_order < 4)
					encoder->private_->local_lpc_compute_autocorrelation = FLAC__lpc_compute_autocorrelation_intrin_sse_lag_4_new;
				else if(encoder->protected_->max_lpc_order < 8)
					encoder->private_->local_lpc_compute_autocorrelation = FLAC__lpc_compute_autocorrelation_intrin_sse_lag_8_new;
				else if(encoder->protected_->max_lpc_order < 12)
					encoder->private_->local_lpc_compute_autocorrelation = FLAC__lpc_compute_autocorrelation_intrin_sse_lag_12_new;
				else if(encoder->protected_->max_lpc_order < 16)
					encoder->private_->local_lpc_compute_autocorrelation = FLAC__lpc_compute_autocorrelation_intrin_sse_lag_16_new;
				else
					encoder->private_->local_lpc_compute_autocorrelation = FLAC__lpc_compute_autocorrelation;
			}
			else { // use old autocorrelation functions
				if(encoder->protected_->max_lpc_order < 4)
					encoder->private_->local_lpc_compute_autocorrelation = FLAC__lpc_compute_autocorrelation_intrin_sse_lag_4_old;
				else if(encoder->protected_->max_lpc_order < 8)
					encoder->private_->local_lpc_compute_autocorrelation = FLAC__lpc_compute_autocorrelation_intrin_sse_lag_8_old;
				else if(encoder->protected_->max_lpc_order < 12)
					encoder->private_->local_lpc_compute_autocorrelation = FLAC__lpc_compute_autocorrelation_intrin_sse_lag_12_old;
				else if(encoder->protected_->max_lpc_order < 16)
					encoder->private_->local_lpc_compute_autocorrelation = FLAC__lpc_compute_autocorrelation_intrin_sse_lag_16_old;
				else
					encoder->private_->local_lpc_compute_autocorrelation = FLAC__lpc_compute_autocorrelation;
			}
		}
#    endif

#    ifdef FLAC__SSE2_SUPPORTED
		if(encoder->private_->cpuinfo.ia32.sse2) {
			encoder->private_->local_lpc_compute_residual_from_qlp_coefficients       = FLAC__lpc_compute_residual_from_qlp_coefficients_intrin_sse2;
			encoder->private_->local_lpc_compute_residual_from_qlp_coefficients_16bit = FLAC__lpc_compute_residual_from_qlp_coefficients_16_intrin_sse2;
		}
#    endif
#    ifdef FLAC__SSE4_1_SUPPORTED
		if(encoder->private_->cpuinfo.ia32.sse41) {
			encoder->private_->local_lpc_compute_residual_from_qlp_coefficients       = FLAC__lpc_compute_residual_from_qlp_coefficients_intrin_sse41;
			encoder->private_->local_lpc_compute_residual_from_qlp_coefficients_64bit = FLAC__lpc_compute_residual_from_qlp_coefficients_wide_intrin_sse41;
		}
#    endif
#    ifdef FLAC__AVX2_SUPPORTED
		if(encoder->private_->cpuinfo.ia32.avx2) {
			encoder->private_->local_lpc_compute_residual_from_qlp_coefficients_16bit = FLAC__lpc_compute_residual_from_qlp_coefficients_16_intrin_avx2;
			encoder->private_->local_lpc_compute_residual_from_qlp_coefficients       = FLAC__lpc_compute_residual_from_qlp_coefficients_intrin_avx2;
			encoder->private_->local_lpc_compute_residual_from_qlp_coefficients_64bit = FLAC__lpc_compute_residual_from_qlp_coefficients_wide_intrin_avx2;
		}
#    endif

#    ifdef FLAC__SSE2_SUPPORTED
		if (encoder->private_->cpuinfo.ia32.sse2) {
			encoder->private_->local_fixed_compute_best_predictor      = FLAC__fixed_compute_best_predictor_intrin_sse2;
			encoder->private_->local_fixed_compute_best_predictor_wide = FLAC__fixed_compute_best_predictor_wide_intrin_sse2;
		}
#    endif
#    ifdef FLAC__SSSE3_SUPPORTED
		if (encoder->private_->cpuinfo.ia32.ssse3) {
			encoder->private_->local_fixed_compute_best_predictor      = FLAC__fixed_compute_best_predictor_intrin_ssse3;
			encoder->private_->local_fixed_compute_best_predictor_wide = FLAC__fixed_compute_best_predictor_wide_intrin_ssse3;
		}
#    endif
#   endif /* FLAC__HAS_X86INTRIN */
/*#  elif defined FLAC__CPU_X86_64
		FLAC__ASSERT(encoder->private_->cpuinfo.type == FLAC__CPUINFO_TYPE_X86_64);
#   if FLAC__HAS_X86INTRIN
#    ifdef FLAC__SSE_SUPPORTED
		if(encoder->private_->cpuinfo.x86.sse42 || !encoder->private_->cpuinfo.x86.intel) { // use new autocorrelation functions
			if(encoder->protected_->max_lpc_order < 4)
				encoder->private_->local_lpc_compute_autocorrelation = FLAC__lpc_compute_autocorrelation_intrin_sse_lag_4_new;
			else if(encoder->protected_->max_lpc_order < 8)
				encoder->private_->local_lpc_compute_autocorrelation = FLAC__lpc_compute_autocorrelation_intrin_sse_lag_8_new;
			else if(encoder->protected_->max_lpc_order < 12)
				encoder->private_->local_lpc_compute_autocorrelation = FLAC__lpc_compute_autocorrelation_intrin_sse_lag_12_new;
			else if(encoder->protected_->max_lpc_order < 16)
				encoder->private_->local_lpc_compute_autocorrelation = FLAC__lpc_compute_autocorrelation_intrin_sse_lag_16_new;
		}
		else {
			if(encoder->protected_->max_lpc_order < 4)
				encoder->private_->local_lpc_compute_autocorrelation = FLAC__lpc_compute_autocorrelation_intrin_sse_lag_4_old;
			else if(encoder->protected_->max_lpc_order < 8)
				encoder->private_->local_lpc_compute_autocorrelation = FLAC__lpc_compute_autocorrelation_intrin_sse_lag_8_old;
			else if(encoder->protected_->max_lpc_order < 12)
				encoder->private_->local_lpc_compute_autocorrelation = FLAC__lpc_compute_autocorrelation_intrin_sse_lag_12_old;
			else if(encoder->protected_->max_lpc_order < 16)
				encoder->private_->local_lpc_compute_autocorrelation = FLAC__lpc_compute_autocorrelation_intrin_sse_lag_16_old;
		}
#    endif

#    ifdef FLAC__SSE2_SUPPORTED
		encoder->private_->local_lpc_compute_residual_from_qlp_coefficients_16bit = FLAC__lpc_compute_residual_from_qlp_coefficients_16_intrin_sse2;
#    endif
#    ifdef FLAC__SSE4_1_SUPPORTED
		if(encoder->private_->cpuinfo.x86.sse41) {
			encoder->private_->local_lpc_compute_residual_from_qlp_coefficients = FLAC__lpc_compute_residual_from_qlp_coefficients_intrin_sse41;
		}
#    endif
#    ifdef FLAC__AVX2_SUPPORTED
		if(encoder->private_->cpuinfo.x86.avx2) {
			encoder->private_->local_lpc_compute_residual_from_qlp_coefficients_16bit = FLAC__lpc_compute_residual_from_qlp_coefficients_16_intrin_avx2;
			encoder->private_->local_lpc_compute_residual_from_qlp_coefficients       = FLAC__lpc_compute_residual_from_qlp_coefficients_intrin_avx2;
			encoder->private_->local_lpc_compute_residual_from_qlp_coefficients_64bit = FLAC__lpc_compute_residual_from_qlp_coefficients_wide_intrin_avx2;
		}
#    endif

#    ifdef FLAC__SSE2_SUPPORTED
		encoder->private_->local_fixed_compute_best_predictor      = FLAC__fixed_compute_best_predictor_intrin_sse2;
		encoder->private_->local_fixed_compute_best_predictor_wide = FLAC__fixed_compute_best_predictor_wide_intrin_sse2;
#    endif
#    ifdef FLAC__SSSE3_SUPPORTED
		if (encoder->private_->cpuinfo.x86.ssse3) {
			encoder->private_->local_fixed_compute_best_predictor      = FLAC__fixed_compute_best_predictor_intrin_ssse3;
			encoder->private_->local_fixed_compute_best_predictor_wide = FLAC__fixed_compute_best_predictor_wide_intrin_ssse3;
		}
#    endif
#   endif /* FLAC__HAS_X86INTRIN */
/*#  endif /* FLAC__CPU_... */
//	}
/*# endif /* !FLAC__NO_ASM */
//#endif /* !FLAC__INTEGER_ONLY_LIBRARY */
/*#if !defined FLAC__NO_ASM && FLAC__HAS_X86INTRIN
	if(encoder->private_->cpuinfo.use_asm) {
# if defined FLAC__CPU_IA32
#  ifdef FLAC__SSE2_SUPPORTED
		if(encoder->private_->cpuinfo.ia32.sse2)
			encoder->private_->local_precompute_partition_info_sums = FLAC__precompute_partition_info_sums_intrin_sse2;
#  endif
#  ifdef FLAC__SSSE3_SUPPORTED
		if(encoder->private_->cpuinfo.ia32.ssse3)
			encoder->private_->local_precompute_partition_info_sums = FLAC__precompute_partition_info_sums_intrin_ssse3;
#  endif
#  ifdef FLAC__AVX2_SUPPORTED
		if(encoder->private_->cpuinfo.ia32.avx2)
			encoder->private_->local_precompute_partition_info_sums = FLAC__precompute_partition_info_sums_intrin_avx2;
#  endif
# elif defined FLAC__CPU_X86_64
#  ifdef FLAC__SSE2_SUPPORTED
		encoder->private_->local_precompute_partition_info_sums = FLAC__precompute_partition_info_sums_intrin_sse2;
#  endif
#  ifdef FLAC__SSSE3_SUPPORTED
		if(encoder->private_->cpuinfo.x86.ssse3)
			encoder->private_->local_precompute_partition_info_sums = FLAC__precompute_partition_info_sums_intrin_ssse3;
#  endif
#  ifdef FLAC__AVX2_SUPPORTED
		if(encoder->private_->cpuinfo.x86.avx2)
			encoder->private_->local_precompute_partition_info_sums = FLAC__precompute_partition_info_sums_intrin_avx2;
#  endif
# endif /* FLAC__CPU_... */
//	}
//#endif /* !FLAC__NO_ASM && FLAC__HAS_X86INTRIN */

		/* set state to OK; from here on, errors are fatal and we'll override the state then */
		this.state = FLAC__STREAM_ENCODER_OK;

if( Jformat.FLAC__HAS_OGG ) {
		this.is_ogg = isogg;
		if( isogg && ! this.ogg_encoder_aspect.FLAC__ogg_encoder_aspect_init() ) {
			this.state = FLAC__STREAM_ENCODER_OGG_ERROR;
			return FLAC__STREAM_ENCODER_INIT_STATUS_ENCODER_ERROR;
		}
}

		this.read_callback = read_cb;
		this.write_callback = write_cb;
		this.seek_callback = seek_cb;
		this.tell_callback = tell_cb;
		this.metadata_callback = metadata_cb;
		// this.client_data = client_data;

		if( ! resize_buffers_( this.blocksize ) ) {
			/* the above function sets the state for us in case of an error */
			return FLAC__STREAM_ENCODER_INIT_STATUS_ENCODER_ERROR;
		}

		if( ! this.frame.FLAC__bitwriter_init() ) {
			this.state = FLAC__STREAM_ENCODER_MEMORY_ALLOCATION_ERROR;
			return FLAC__STREAM_ENCODER_INIT_STATUS_ENCODER_ERROR;
		}

		/*
		 * Set up the verify stuff if necessary
		 */
		if( this.do_verify ) {
			/*
			 * First, set up the fifo which will hold the
			 * original signal to compare against
			 */
			this.verify.input_fifo.size = this.blocksize + OVERREAD_;
			for( int i = 0; i < this.channels; i++ ) {
				try {
					this.verify.input_fifo.data[i] = new int[this.verify.input_fifo.size];
				} catch( final OutOfMemoryError e ) {
					this.state = FLAC__STREAM_ENCODER_MEMORY_ALLOCATION_ERROR;
					return FLAC__STREAM_ENCODER_INIT_STATUS_ENCODER_ERROR;
				}
			}
			this.verify.input_fifo.tail = 0;

			/*
			 * Now set up a stream decoder for verification
			 */
			if( null == this.verify.decoder ) {
				this.verify.decoder = new JFLAC__StreamDecoder();
				if( null == this.verify.decoder ) {
					this.state = FLAC__STREAM_ENCODER_VERIFY_DECODER_ERROR;
					return FLAC__STREAM_ENCODER_INIT_STATUS_ENCODER_ERROR;
				}
			}

			if( this.verify.decoder.FLAC__stream_decoder_init_stream(
					this,// verify_read_callback_,
					/*seek_callback=*/null,
					/*tell_callback=*/null,
					/*length_callback=*/null,
					/*eof_callback=*/null,
					this,// verify_write_callback_,
					this,// verify_metadata_callback_,
					this//,// verify_error_callback_,
					/*client_data = this*/ ) != JFLAC__StreamDecoder.FLAC__STREAM_DECODER_INIT_STATUS_OK ) {
				this.state = FLAC__STREAM_ENCODER_VERIFY_DECODER_ERROR;
				return FLAC__STREAM_ENCODER_INIT_STATUS_ENCODER_ERROR;
			}
		}
		this.verify.error_stats.absolute_sample = 0;
		this.verify.error_stats.frame_number = 0;
		this.verify.error_stats.channel = 0;
		this.verify.error_stats.sample = 0;
		this.verify.error_stats.expected = 0;
		this.verify.error_stats.got = 0;

		/*
		 * These must be done before we write any metadata, because that
		 * calls the write_callback, which uses these values.
		 */
		this.first_seekpoint_to_check = 0;
		this.samples_written = 0;
		this.streaminfo_offset = 0;
		this.seektable_offset = 0;
		this.audio_offset = 0;

		/*
		 * write the stream header
		 */
		if( this.do_verify ) {
			this.verify.state_hint = ENCODER_IN_MAGIC;
		}
		if( ! this.frame.FLAC__bitwriter_write_raw_uint32( Jformat.FLAC__STREAM_SYNC, Jformat.FLAC__STREAM_SYNC_LEN ) ) {
			this.state = FLAC__STREAM_ENCODER_FRAMING_ERROR;
			return FLAC__STREAM_ENCODER_INIT_STATUS_ENCODER_ERROR;
		}
		if( ! write_bitbuffer_( 0, /*is_last_block=*/false ) ) {
			/* the above function sets the state for us in case of an error */
			return FLAC__STREAM_ENCODER_INIT_STATUS_ENCODER_ERROR;
		}

		/*
		 * write the STREAMINFO metadata block
		 */
		if( this.do_verify ) {
			this.verify.state_hint = ENCODER_IN_METADATA;
		}
		this.streaminfo.type = Jformat.FLAC__METADATA_TYPE_STREAMINFO;
		this.streaminfo.is_last = false; /* we will have at a minimum a VORBIS_COMMENT afterwards */
		this.streaminfo.length = JFLAC__StreamMetadata_StreamInfo.FLAC__STREAM_METADATA_STREAMINFO_LENGTH;
		final JFLAC__StreamMetadata_StreamInfo stream_info = this.streaminfo;
		stream_info.min_blocksize = this.blocksize; /* this encoder uses the same blocksize for the whole stream */
		stream_info.max_blocksize = this.blocksize;
		stream_info.min_framesize = 0; /* we don't know this yet; have to fill it in later */
		stream_info.max_framesize = 0; /* we don't know this yet; have to fill it in later */
		stream_info.sample_rate = this.sample_rate;
		stream_info.channels = this.channels;
		stream_info.bits_per_sample = this.bits_per_sample;
		stream_info.total_samples = this.total_samples_estimate; /* we will replace this later with the real total */
		Arrays.fill( stream_info.md5sum, 0, 16, (byte)0 );/* we don't know this yet; have to fill it in later */
		if( this.do_md5 ) {
			this.md5context.FLAC__MD5Init();
		}
		if( ! JFLAC__StreamMetadata.FLAC__add_metadata_block( this.streaminfo, this.frame ) ) {
			this.state = FLAC__STREAM_ENCODER_FRAMING_ERROR;
			return FLAC__STREAM_ENCODER_INIT_STATUS_ENCODER_ERROR;
		}
		if( ! write_bitbuffer_( 0, /*is_last_block=*/false ) ) {
			/* the above function sets the state for us in case of an error */
			return FLAC__STREAM_ENCODER_INIT_STATUS_ENCODER_ERROR;
		}

		/*
		 * Now that the STREAMINFO block is written, we can init this to an
		 * absurdly-high value...
		 */
		stream_info.min_framesize = (1 << Jformat.FLAC__STREAM_METADATA_STREAMINFO_MIN_FRAME_SIZE_LEN) - 1;
		/* ... and clear this to 0 */
		stream_info.total_samples = 0;

		/*
		 * Check to see if the supplied metadata contains a VORBIS_COMMENT;
		 * if not, we will write an empty one (FLAC__add_metadata_block()
		 * automatically supplies the vendor string).
		 *
		 * WATCHOUT: the Ogg FLAC mapping requires us to write this block after
		 * the STREAMINFO.  (In the case that metadata_has_vorbis_comment is
		 * true it will have already insured that the metadata list is properly
		 * ordered.)
		 */
		if( ! metadata_has_vorbis_comment ) {
			final JFLAC__StreamMetadata_VorbisComment vorbis_comment = new JFLAC__StreamMetadata_VorbisComment();
			vorbis_comment.type = Jformat.FLAC__METADATA_TYPE_VORBIS_COMMENT;
			vorbis_comment.is_last = (meta_data == null);// (this.num_metadata_blocks == 0);
			vorbis_comment.length = 4 + 4; /* MAGIC NUMBER */
			vorbis_comment.vendor_string = null;
			vorbis_comment.num_comments = 0;
			vorbis_comment.comments = null;
			if( ! JFLAC__StreamMetadata.FLAC__add_metadata_block( vorbis_comment, this.frame ) ) {
				this.state = FLAC__STREAM_ENCODER_FRAMING_ERROR;
				return FLAC__STREAM_ENCODER_INIT_STATUS_ENCODER_ERROR;
			}
			if( ! write_bitbuffer_( 0, /*is_last_block=*/false ) ) {
				/* the above function sets the state for us in case of an error */
				return FLAC__STREAM_ENCODER_INIT_STATUS_ENCODER_ERROR;
			}
		}

		if( meta_data != null ) {// java
			/*
			 * write the user's metadata blocks
			 */
			final int metadata_blocks = meta_data.length;
			for( int i = 0; i < metadata_blocks /* this.num_metadata_blocks */; i++ ) {
				meta_data[i].is_last = (i == metadata_blocks - 1);
				if( ! JFLAC__StreamMetadata.FLAC__add_metadata_block( meta_data[i], this.frame ) ) {
					this.state = FLAC__STREAM_ENCODER_FRAMING_ERROR;
					return FLAC__STREAM_ENCODER_INIT_STATUS_ENCODER_ERROR;
				}
				if( ! write_bitbuffer_( 0, /*is_last_block=*/false ) ) {
					/* the above function sets the state for us in case of an error */
					return FLAC__STREAM_ENCODER_INIT_STATUS_ENCODER_ERROR;
				}
			}
		}

		/* now that all the metadata is written, we save the stream offset */
		try {
			if( this.tell_callback != null ) {
				this.audio_offset = this.tell_callback.enc_tell_callback( this/*, this.client_data*/ );/* FLAC__STREAM_ENCODER_TELL_STATUS_UNSUPPORTED just means we didn't get the offset; no error */
			}
		} catch(final IOException e) {
			this.state = FLAC__STREAM_ENCODER_CLIENT_ERROR;
			return FLAC__STREAM_ENCODER_INIT_STATUS_ENCODER_ERROR;
		} catch(final UnsupportedOperationException e) {
		}

		if( this.do_verify ) {
			this.verify.state_hint = ENCODER_IN_AUDIO;
		}

		return FLAC__STREAM_ENCODER_INIT_STATUS_OK;
	}

	public final int /* FLAC__StreamEncoderInitStatus */ FLAC__stream_encoder_init_stream(
		final JFLAC__StreamEncoderWriteCallback write_cb,
		final JFLAC__StreamEncoderSeekCallback seek_cb,
		final JFLAC__StreamEncoderTellCallback tell_cb,
		final JFLAC__StreamEncoderMetadataCallback metadata_cb//,
		// final Object client_data
	)
	{
		return init_stream_internal_(
			/*read_callback=*/null,
			write_cb,
			seek_cb,
			tell_cb,
			metadata_cb,
			// client_data,
			/*is_ogg=*/false
		);
	}

	public final int /* FLAC__StreamEncoderInitStatus */ FLAC__stream_encoder_init_ogg_stream(
		final JFLAC__StreamEncoderReadCallback read_cb,
		final JFLAC__StreamEncoderWriteCallback write_cb,
		final JFLAC__StreamEncoderSeekCallback seek_cb,
		final JFLAC__StreamEncoderTellCallback tell_cb,
		final JFLAC__StreamEncoderMetadataCallback metadata_cb//,
		// final Object client_data
	)
	{
		return init_stream_internal_(
			read_cb,
			write_cb,
			seek_cb,
			tell_cb,
			metadata_cb,
			// client_data,
			/*is_ogg=*/true
		);
	}

	private final int /* FLAC__StreamEncoderInitStatus */ init_FILE_internal_(
		final OutputStream f,
		final JFLAC__StreamEncoderProgressCallback progress_cb,
		//final Object client_data,
		final boolean isogg
	)
	{
		//FLAC__ASSERT(0 != encoder);
		//FLAC__ASSERT(0 != file);

		if( this.state != FLAC__STREAM_ENCODER_UNINITIALIZED ) {
			return FLAC__STREAM_ENCODER_INIT_STATUS_ALREADY_INITIALIZED;
		}

		/* double protection */
		if( f == null ) {
			this.state = FLAC__STREAM_ENCODER_IO_ERROR;
			return FLAC__STREAM_ENCODER_INIT_STATUS_ENCODER_ERROR;
		}

		/*
		 * To make sure that our file does not go unclosed after an error, we
		 * must assign the FILE pointer before any further error can occur in
		 * this routine.
		 */
		//if( file == System.out )
		//	file = get_binary_stdout_(); /* just to be safe */

//#ifdef _WIN32
		/*
		 * Windows can suffer quite badly from disk fragmentation. This can be
		 * reduced significantly by setting the output buffer size to be 10MB.
		 */
//		if( GetFileType((HANDLE)_get_osfhandle( _fileno( file ) )) == FILE_TYPE_DISK )
//			setvbuf(file, NULL, _IOFBF, 10*1024*1024);
//#endif

		this.file = f;

		this.progress_callback = progress_cb;
		this.bytes_written = 0;
		this.samples_written = 0;
		this.frames_written = 0;

		final int /* FLAC__StreamEncoderInitStatus */ init_status = init_stream_internal_(
			this.file == (OutputStream)System.out ? null : isogg ? this /*file_read_callback_*/ : null,
					this /*file_write_callback_*/,
			this.file == (OutputStream)System.out ? null : this /*file_seek_callback_*/,
			this.file == (OutputStream)System.out ? null : this /*file_tell_callback_*/,
			/*metadata_callback=*/null,
			// client_data,
			isogg
		);
		if( init_status != FLAC__STREAM_ENCODER_INIT_STATUS_OK ) {
			/* the above function sets the state for us in case of an error */
			return init_status;
		}

		{
			final int block_size = FLAC__stream_encoder_get_blocksize();

			//FLAC__ASSERT(blocksize != 0);
			this.total_frames_estimate = (int)((FLAC__stream_encoder_get_total_samples_estimate() + (long)block_size - 1L) / block_size);
		}

		return init_status;
	}

	public final int /* FLAC__StreamEncoderInitStatus */ FLAC__stream_encoder_init_FILE(
		final RandomAccessInputOutputStream f,
		final JFLAC__StreamEncoderProgressCallback progress_cb//,
		//final Object client_data
	)
	{
		return init_FILE_internal_( f, progress_cb,/* client_data,*/ /*is_ogg=*/false );
	}

	public final int /* FLAC__StreamEncoderInitStatus */ FLAC__stream_encoder_init_ogg_FILE(
		final RandomAccessInputOutputStream f,
		final JFLAC__StreamEncoderProgressCallback progress_cb//,
		// final Object client_data
	)
	{
		return init_FILE_internal_( f, progress_cb,/* client_data,*/ /*is_ogg=*/true );
	}

	private final int /* FLAC__StreamEncoderInitStatus */ init_file_internal_(
		final String filename,
		final JFLAC__StreamEncoderProgressCallback progress_cb,
		// final Object client_data,
		final boolean isogg
	)
	{
		//FLAC__ASSERT(0 != encoder);

		/*
		 * To make sure that our file does not go unclosed after an error, we
		 * have to do the same entrance checks here that are later performed
		 * in FLAC__stream_encoder_init_FILE() before the FILE* is assigned.
		 */
		if( this.state != FLAC__STREAM_ENCODER_UNINITIALIZED ) {
			return FLAC__STREAM_ENCODER_INIT_STATUS_ALREADY_INITIALIZED;
		}

		try {
			final OutputStream f = filename != null ? new RandomAccessInputOutputStream( filename ) : System.out;
			return init_FILE_internal_( f, progress_cb,/* client_data,*/ isogg );
		} catch( final Exception e ) {
			this.state = FLAC__STREAM_ENCODER_IO_ERROR;
			return FLAC__STREAM_ENCODER_INIT_STATUS_ENCODER_ERROR;
		}
	}

	public final int /* FLAC__StreamEncoderInitStatus */ FLAC__stream_encoder_init_file(
		final String filename,
		final JFLAC__StreamEncoderProgressCallback progress_cb//,
		// final Object client_data
	)
	{
		return init_file_internal_( filename, progress_cb,/* client_data,*/ /*is_ogg=*/false );
	}

	public final int /* FLAC__StreamEncoderInitStatus */ FLAC__stream_encoder_init_ogg_file(
		final String filename,
		final JFLAC__StreamEncoderProgressCallback progress_cb//,
		//final Object client_data
	)
	{
		return init_file_internal_( filename, progress_cb,/* client_data,*/ /*is_ogg=*/true );
	}

	public final boolean FLAC__stream_encoder_finish()
	{
		//FLAC__ASSERT(0 != encoder);
		//FLAC__ASSERT(0 != encoder.private_);
		//FLAC__ASSERT(0 != encoder.protected_);

		if( this.state == FLAC__STREAM_ENCODER_UNINITIALIZED ) {
			return true;
		}

		boolean error = false;
		if( this.state == FLAC__STREAM_ENCODER_OK && ! this.is_being_deleted ) {
			if( this.current_sample_number != 0 ) {
				final boolean is_fractional_block = this.blocksize != this.current_sample_number;
				this.blocksize = this.current_sample_number;
				if( ! process_frame_( is_fractional_block, /*is_last_block=*/true ) ) {
					error = true;
				}
			}
		}

		if( this.do_md5 ) {
			this.md5context.FLAC__MD5Final( this.streaminfo.md5sum );
		}

		if( ! this.is_being_deleted ) {
			if( this.state == FLAC__STREAM_ENCODER_OK ) {
				if( this.seek_callback != null ) {
if( Jformat.FLAC__HAS_OGG
					&& this.is_ogg ) {
						update_ogg_metadata_();
} else {
					update_metadata_();
}
					/* check if an error occurred while updating metadata */
					if( this.state != FLAC__STREAM_ENCODER_OK ) {
						error = true;
					}
				}
				if( this.metadata_callback != null ) {
					this.metadata_callback.enc_metadata_callback( this, this.streaminfo/*, this.client_data*/ );
				}
			}

			if( this.do_verify && null != this.verify.decoder && ! this.verify.decoder.FLAC__stream_decoder_finish() ) {
				if( ! error ) {
					this.state = FLAC__STREAM_ENCODER_VERIFY_MISMATCH_IN_AUDIO_DATA;
				}
				error = true;
			}
		}

		if( null != this.file ) {
			if( this.file != (OutputStream)System.out ) {
				try { this.file.close(); } catch( final IOException e ) {}
			}
			this.file = null;
		}

if( Jformat.FLAC__HAS_OGG ) {
		if( this.is_ogg ) {
			this.ogg_encoder_aspect.FLAC__ogg_encoder_aspect_finish();
		}
}

		free_();
		set_defaults_();

		if( ! error ) {
			this.state = FLAC__STREAM_ENCODER_UNINITIALIZED;
		}

		return ! error;
	}

	/** Set the serial number for the FLAC stream to use in the Ogg container.
	 *
	 * @note
	 * This does not need to be set for native FLAC encoding.
	 *
	 * @note
	 * It is recommended to set a serial number explicitly as the default of '0'
	 * may collide with other streams.
	 *
	 * \default \c 0
	 * @param  encoder        An encoder instance to set.
	 * @param  serial_number  See above.
	 * \assert
	 *    \code encoder != NULL \endcode
	 * @retval FLAC__bool
	 *    \c false if the encoder is already initialized, else \c true.
	 */
	public final boolean FLAC__stream_encoder_set_ogg_serial_number(final int value)
	{
		//FLAC__ASSERT(0 != encoder);
		//FLAC__ASSERT(0 != encoder.private_);
		//FLAC__ASSERT(0 != encoder.protected_);
		if( this.state != FLAC__STREAM_ENCODER_UNINITIALIZED ) {
			return false;
		}
if( Jformat.FLAC__HAS_OGG ) {
		/* can't check encoder.private_->is_ogg since that's not set until init time */
		this.ogg_encoder_aspect.FLAC__ogg_encoder_aspect_set_serial_number( value );
		return true;
}
		return false;
	}

	/** Set the "verify" flag.  If \c true, the encoder will verify it's own
	 *  encoded output by feeding it through an internal decoder and comparing
	 *  the original signal against the decoded signal.  If a mismatch occurs,
	 *  the process call will return \c false.  Note that this will slow the
	 *  encoding process by the extra time required for decoding and comparison.
	 *
	 * \default \c false
	 * @param  encoder  An encoder instance to set.
	 * @param  value    Flag value (see above).
	 * \assert
	 *    \code encoder != NULL \endcode
	 * @retval FLAC__bool
	 *    \c false if the encoder is already initialized, else \c true.
	 */
	public final boolean FLAC__stream_encoder_set_verify(final boolean value)
	{
		//FLAC__ASSERT(0 != encoder);
		//FLAC__ASSERT(0 != encoder->private_);
		//FLAC__ASSERT(0 != encoder->protected_);
		if( this.state != FLAC__STREAM_ENCODER_UNINITIALIZED ) {
			return false;
		}
//if( ! FLAC__MANDATORY_VERIFY_WHILE_ENCODING ) {
		this.do_verify = value;
//}
		return true;
	}

	/** Set the <A HREF="../format.html#subset">Subset</A> flag.  If \c true,
	 *  the encoder will comply with the Subset and will check the
	 *  settings during FLAC__stream_encoder_init_*() to see if all settings
	 *  comply.  If \c false, the settings may take advantage of the full
	 *  range that the format allows.
	 *
	 *  Make sure you know what it entails before setting this to \c false.
	 *
	 * \default \c true
	 * @param  encoder  An encoder instance to set.
	 * @param  value    Flag value (see above).
	 * \assert
	 *    \code encoder != NULL \endcode
	 * @retval FLAC__bool
	 *    \c false if the encoder is already initialized, else \c true.
	 */
	public final boolean FLAC__stream_encoder_set_streamable_subset(final boolean value)
	{
		//FLAC__ASSERT(0 != encoder);
		//FLAC__ASSERT(0 != encoder->private_);
		//FLAC__ASSERT(0 != encoder->protected_);
		if( this.state != FLAC__STREAM_ENCODER_UNINITIALIZED ) {
			return false;
		}
		this.streamable_subset = value;
		return true;
	}

	public final boolean FLAC__stream_encoder_set_do_md5(final boolean value)
	{
		//FLAC__ASSERT(0 != encoder);
		//FLAC__ASSERT(0 != encoder->private_);
		//FLAC__ASSERT(0 != encoder->protected_);
		if( this.state != FLAC__STREAM_ENCODER_UNINITIALIZED ) {
			return false;
		}
		this.do_md5 = value;
		return true;
	}

	/** Set the number of channels to be encoded.
	 *
	 * \default \c 2
	 * @param  encoder  An encoder instance to set.
	 * @param  value    See above.
	 * \assert
	 *    \code encoder != NULL \endcode
	 * @retval FLAC__bool
	 *    \c false if the encoder is already initialized, else \c true.
	 */
	public final boolean FLAC__stream_encoder_set_channels(final int value)
	{
		//FLAC__ASSERT(0 != encoder);
		//FLAC__ASSERT(0 != encoder->private_);
		//FLAC__ASSERT(0 != encoder->protected_);
		if( this.state != FLAC__STREAM_ENCODER_UNINITIALIZED ) {
			return false;
		}
		this.channels = value;
		return true;
	}

	/** Set the sample resolution of the input to be encoded.
	 *
	 * \warning
	 * Do not feed the encoder data that is wider than the value you
	 * set here or you will generate an invalid stream.
	 *
	 * \default \c 16
	 * @param  encoder  An encoder instance to set.
	 * @param  value    See above.
	 * \assert
	 *    \code encoder != NULL \endcode
	 * @retval FLAC__bool
	 *    \c false if the encoder is already initialized, else \c true.
	 */
	public final boolean FLAC__stream_encoder_set_bits_per_sample(final int value)
	{
		//FLAC__ASSERT(0 != encoder);
		//FLAC__ASSERT(0 != encoder->private_);
		//FLAC__ASSERT(0 != encoder->protected_);
		if( this.state != FLAC__STREAM_ENCODER_UNINITIALIZED ) {
			return false;
		}
		this.bits_per_sample = value;
		return true;
	}

	/** Set the sample rate (in Hz) of the input to be encoded.
	 *
	 * \default \c 44100
	 * @param  encoder  An encoder instance to set.
	 * @param  value    See above.
	 * \assert
	 *    \code encoder != NULL \endcode
	 * @retval FLAC__bool
	 *    \c false if the encoder is already initialized, else \c true.
	 */
	public final boolean FLAC__stream_encoder_set_sample_rate(final int value)
	{
		//FLAC__ASSERT(0 != encoder);
		//FLAC__ASSERT(0 != encoder->private_);
		//FLAC__ASSERT(0 != encoder->protected_);
		if( this.state != FLAC__STREAM_ENCODER_UNINITIALIZED ) {
			return false;
		}
		this.sample_rate = value;
		return true;
	}

	/** Set the compression level
	 *
	 * The compression level is roughly proportional to the amount of effort
	 * the encoder expends to compress the file.  A higher level usually
	 * means more computation but higher compression.  The default level is
	 * suitable for most applications.
	 *
	 * Currently the levels range from \c 0 (fastest, least compression) to
	 * \c 8 (slowest, most compression).  A value larger than \c 8 will be
	 * treated as \c 8.
	 *
	 * This function automatically calls the following other \c _set_
	 * functions with appropriate values, so the client does not need to
	 * unless it specifically wants to override them:
	 * - FLAC__stream_encoder_set_do_mid_side_stereo()
	 * - FLAC__stream_encoder_set_loose_mid_side_stereo()
	 * - FLAC__stream_encoder_set_apodization()
	 * - FLAC__stream_encoder_set_max_lpc_order()
	 * - FLAC__stream_encoder_set_qlp_coeff_precision()
	 * - FLAC__stream_encoder_set_do_qlp_coeff_prec_search()
	 * - FLAC__stream_encoder_set_do_escape_coding()
	 * - FLAC__stream_encoder_set_do_exhaustive_model_search()
	 * - FLAC__stream_encoder_set_min_residual_partition_order()
	 * - FLAC__stream_encoder_set_max_residual_partition_order()
	 * - FLAC__stream_encoder_set_rice_parameter_search_dist()
	 *
	 * The actual values set for each level are:
	 * <table>
	 * <tr>
	 *  <td><b>level</b></td>
	 *  <td>do mid-side stereo</td>
	 *  <td>loose mid-side stereo</td>
	 *  <td>apodization</td>
	 *  <td>max lpc order</td>
	 *  <td>qlp coeff precision</td>
	 *  <td>qlp coeff prec search</td>
	 *  <td>escape coding</td>
	 *  <td>exhaustive model search</td>
	 *  <td>min residual partition order</td>
	 *  <td>max residual partition order</td>
	 *  <td>rice parameter search dist</td>
	 * </tr>
	 * <tr>  <td><b>0</b></td> <td>false</td> <td>false</td> <td>tukey(0.5)<td>                                     <td>0</td>  <td>0</td> <td>false</td> <td>false</td> <td>false</td> <td>0</td> <td>3</td> <td>0</td> </tr>
	 * <tr>  <td><b>1</b></td> <td>true</td>  <td>true</td>  <td>tukey(0.5)<td>                                     <td>0</td>  <td>0</td> <td>false</td> <td>false</td> <td>false</td> <td>0</td> <td>3</td> <td>0</td> </tr>
	 * <tr>  <td><b>2</b></td> <td>true</td>  <td>false</td> <td>tukey(0.5)<td>                                     <td>0</td>  <td>0</td> <td>false</td> <td>false</td> <td>false</td> <td>0</td> <td>3</td> <td>0</td> </tr>
	 * <tr>  <td><b>3</b></td> <td>false</td> <td>false</td> <td>tukey(0.5)<td>                                     <td>6</td>  <td>0</td> <td>false</td> <td>false</td> <td>false</td> <td>0</td> <td>4</td> <td>0</td> </tr>
	 * <tr>  <td><b>4</b></td> <td>true</td>  <td>true</td>  <td>tukey(0.5)<td>                                     <td>8</td>  <td>0</td> <td>false</td> <td>false</td> <td>false</td> <td>0</td> <td>4</td> <td>0</td> </tr>
	 * <tr>  <td><b>5</b></td> <td>true</td>  <td>false</td> <td>tukey(0.5)<td>                                     <td>8</td>  <td>0</td> <td>false</td> <td>false</td> <td>false</td> <td>0</td> <td>5</td> <td>0</td> </tr>
	 * <tr>  <td><b>6</b></td> <td>true</td>  <td>false</td> <td>tukey(0.5);partial_tukey(2)<td>                    <td>8</td>  <td>0</td> <td>false</td> <td>false</td> <td>false</td> <td>0</td> <td>6</td> <td>0</td> </tr>
	 * <tr>  <td><b>7</b></td> <td>true</td>  <td>false</td> <td>tukey(0.5);partial_tukey(2)<td>                    <td>12</td> <td>0</td> <td>false</td> <td>false</td> <td>false</td> <td>0</td> <td>6</td> <td>0</td> </tr>
	 * <tr>  <td><b>8</b></td> <td>true</td>  <td>false</td> <td>tukey(0.5);partial_tukey(2);punchout_tukey(3)</td> <td>12</td> <td>0</td> <td>false</td> <td>false</td> <td>false</td> <td>0</td> <td>6</td> <td>0</td> </tr>
	 * </table>
	 *
	 * \default \c 5
	 * @param  encoder  An encoder instance to set.
	 * @param  value    See above.
	 * \assert
	 *    \code encoder != NULL \endcode
	 * @retval FLAC__bool
	 *    \c false if the encoder is already initialized, else \c true.
	 */
	public final boolean FLAC__stream_encoder_set_compression_level(int value)
	{
		//FLAC__ASSERT(0 != encoder);
		//FLAC__ASSERT(0 != encoder->private_);
		//FLAC__ASSERT(0 != encoder->protected_);
		if( this.state != FLAC__STREAM_ENCODER_UNINITIALIZED ) {
			return false;
		}
		boolean ok = true;
		if( value >= compression_levels_.length ) {
			value = compression_levels_.length - 1;
		}
		final CompressionLevels level = compression_levels_[value];// java
		ok &= FLAC__stream_encoder_set_do_mid_side_stereo   ( level.do_mid_side_stereo );
		ok &= FLAC__stream_encoder_set_loose_mid_side_stereo( level.loose_mid_side_stereo );
//#ifndef FLAC__INTEGER_ONLY_LIBRARY
//#if 1
		ok &= FLAC__stream_encoder_set_apodization                 ( level.apodization );
//#else
		/* equivalent to -A tukey(0.5) */
		//this.num_apodizations = 1;
		//this.apodizations[0].type = JFLAC__ApodizationSpecification.FLAC__APODIZATION_TUKEY;
		//this.apodizations[0]./*parameters.tukey.*/p = 0.5f;
//#endif
//#endif
		ok &= FLAC__stream_encoder_set_max_lpc_order               ( level.max_lpc_order );
		ok &= FLAC__stream_encoder_set_qlp_coeff_precision         ( level.qlp_coeff_precision );
		ok &= FLAC__stream_encoder_set_do_qlp_coeff_prec_search    ( level.do_qlp_coeff_prec_search );
		ok &= FLAC__stream_encoder_set_do_escape_coding            ( level.do_escape_coding );
		ok &= FLAC__stream_encoder_set_do_exhaustive_model_search  ( level.do_exhaustive_model_search );
		ok &= FLAC__stream_encoder_set_min_residual_partition_order( level.min_residual_partition_order );
		ok &= FLAC__stream_encoder_set_max_residual_partition_order( level.max_residual_partition_order );
		ok &= FLAC__stream_encoder_set_rice_parameter_search_dist  ( level.rice_parameter_search_dist );
		return ok;
	}

	/** Set the blocksize to use while encoding.
	 *
	 * The number of samples to use per frame.  Use \c 0 to let the encoder
	 * estimate a blocksize; this is usually best.
	 *
	 * \default \c 0
	 * @param  encoder  An encoder instance to set.
	 * @param  value    See above.
	 * \assert
	 *    \code encoder != NULL \endcode
	 * @retval FLAC__bool
	 *    \c false if the encoder is already initialized, else \c true.
	 */
	public final boolean FLAC__stream_encoder_set_blocksize(final int value)
	{
		//FLAC__ASSERT(0 != encoder);
		//FLAC__ASSERT(0 != encoder->private_);
		//FLAC__ASSERT(0 != encoder->protected_);
		if( this.state != FLAC__STREAM_ENCODER_UNINITIALIZED ) {
			return false;
		}
		this.blocksize = value;
		return true;
	}

	/** Set to \c true to enable mid-side encoding on stereo input.  The
	 *  number of channels must be 2 for this to have any effect.  Set to
	 *  \c false to use only independent channel coding.
	 *
	 * \default \c false
	 * @param  encoder  An encoder instance to set.
	 * @param  value    Flag value (see above).
	 * \assert
	 *    \code encoder != NULL \endcode
	 * @retval FLAC__bool
	 *    \c false if the encoder is already initialized, else \c true.
	 */
	public final boolean FLAC__stream_encoder_set_do_mid_side_stereo(final boolean value)
	{
		//FLAC__ASSERT(0 != encoder);
		//FLAC__ASSERT(0 != encoder->private_);
		//FLAC__ASSERT(0 != encoder->protected_);
		if( this.state != FLAC__STREAM_ENCODER_UNINITIALIZED) {
			return false;
		}
		this.do_mid_side_stereo = value;
		return true;
	}

	/** Set to \c true to enable adaptive switching between mid-side and
	 *  left-right encoding on stereo input.  Set to \c false to use
	 *  exhaustive searching.  Setting this to \c true requires
	 *  FLAC__stream_encoder_set_do_mid_side_stereo() to also be set to
	 *  \c true in order to have any effect.
	 *
	 * \default \c false
	 * @param  encoder  An encoder instance to set.
	 * @param  value    Flag value (see above).
	 * \assert
	 *    \code encoder != NULL \endcode
	 * @retval FLAC__bool
	 *    \c false if the encoder is already initialized, else \c true.
	 */
	public final boolean FLAC__stream_encoder_set_loose_mid_side_stereo(final boolean value)
	{
		//FLAC__ASSERT(0 != encoder);
		//FLAC__ASSERT(0 != encoder->private_);
		//FLAC__ASSERT(0 != encoder->protected_);
		if( this.state != FLAC__STREAM_ENCODER_UNINITIALIZED) {
			return false;
		}
		this.loose_mid_side_stereo = value;
		return true;
	}

	/*@@@@add to tests*/
	/** Sets the apodization function(s) the encoder will use when windowing
	 *  audio data for LPC analysis.
	 *
	 * The \a specification is a plain ASCII string which specifies exactly
	 * which functions to use.  There may be more than one (up to 32),
	 * separated by \c ';' characters.  Some functions take one or more
	 * comma-separated arguments in parentheses.
	 *
	 * The available functions are \c bartlett, \c bartlett_hann,
	 * \c blackman, \c blackman_harris_4term_92db, \c connes, \c flattop,
	 * \c gauss(STDDEV), \c hamming, \c hann, \c kaiser_bessel, \c nuttall,
	 * \c rectangle, \c triangle, \c tukey(P), \c partial_tukey(n[/ov[/P]]),
	 * \c punchout_tukey(n[/ov[/P]]), \c welch.
	 *
	 * For \c gauss(STDDEV), STDDEV specifies the standard deviation
	 * (0<STDDEV<=0.5).
	 *
	 * For \c tukey(P), P specifies the fraction of the window that is
	 * tapered (0<=P<=1).  P=0 corresponds to \c rectangle and P=1
	 * corresponds to \c hann.
	 *
	 * Specifying \c partial_tukey or \c punchout_tukey works a little
	 * different. These do not specify a single apodization function, but
	 * a series of them with some overlap. partial_tukey specifies a series
	 * of small windows (all treated separately) while punchout_tukey
	 * specifies a series of windows that have a hole in them. In this way,
	 * the predictor is constructed with only a part of the block, which
	 * helps in case a block consists of dissimilar parts.
	 *
	 * The three parameters that can be specified for the functions are
	 * n, ov and P. n is the number of functions to add, ov is the overlap
	 * of the windows in case of partial_tukey and the overlap in the gaps
	 * in case of punchout_tukey. P is the fraction of the window that is
	 * tapered, like with a regular tukey window. The function can be
	 * specified with only a number, a number and an overlap, or a number
	 * an overlap and a P, for example, partial_tukey(3), partial_tukey(3/0.3)
	 * and partial_tukey(3/0.3/0.5) are all valid. ov should be smaller than 1
	 * and can be negative.
	 *
	 * Example specifications are \c "blackman" or
	 * \c "hann;triangle;tukey(0.5);tukey(0.25);tukey(0.125)"
	 *
	 * Any function that is specified erroneously is silently dropped.  Up
	 * to 32 functions are kept, the rest are dropped.  If the specification
	 * is empty the encoder defaults to \c "tukey(0.5)".
	 *
	 * When more than one function is specified, then for every subframe the
	 * encoder will try each of them separately and choose the window that
	 * results in the smallest compressed subframe.
	 *
	 * Note that each function specified causes the encoder to occupy a
	 * floating point array in which to store the window. Also note that the
	 * values of P, STDDEV and ov are locale-specific, so if the comma
	 * separator specified by the locale is a comma, a comma should be used.
	 *
	 * \default \c "tukey(0.5)"
	 * @param  encoder        An encoder instance to set.
	 * @param  specification  See above.
	 * \assert
	 *    \code encoder != NULL \endcode
	 *    \code specification != NULL \endcode
	 * @retval FLAC__bool
	 *    \c false if the encoder is already initialized, else \c true.
	 */
	public final boolean FLAC__stream_encoder_set_apodization(final String specification)
	{
		//FLAC__ASSERT(0 != encoder);
		//FLAC__ASSERT(0 != encoder->private_);
		//FLAC__ASSERT(0 != encoder->protected_);
		//FLAC__ASSERT(0 != specification);
		if( this.state != FLAC__STREAM_ENCODER_UNINITIALIZED ) {
			return false;
		}
//#ifdef Jformat.FLAC__INTEGER_ONLY_LIBRARY
		//(void)specification; /* silently ignore since we haven't integerized; will always use a rectangular window */
//#else
		final JFLAC__ApodizationSpecification[] as = this.apodizations;// java
		int napodizations = 0;// this.num_apodizations = 0
		int i = 0;
		while( true ) {
			final int s = specification.indexOf( ';', i );
			final int n = s >= 0 ? (s - i) : specification.length();
			if( n == 8  && specification.startsWith("bartlett", i) ) {
				as[napodizations++].type = JFLAC__ApodizationSpecification.FLAC__APODIZATION_BARTLETT;
			} else if( n == 13 && specification.startsWith("bartlett_hann", i) ) {
				as[napodizations++].type = JFLAC__ApodizationSpecification.FLAC__APODIZATION_BARTLETT_HANN;
			} else if( n == 8  && specification.startsWith("blackman", i) ) {
				as[napodizations++].type = JFLAC__ApodizationSpecification.FLAC__APODIZATION_BLACKMAN;
			} else if( n == 26 && specification.startsWith("blackman_harris_4term_92db", i) ) {
				as[napodizations++].type = JFLAC__ApodizationSpecification.FLAC__APODIZATION_BLACKMAN_HARRIS_4TERM_92DB_SIDELOBE;
			} else if( n == 6  && specification.startsWith("connes", i) ) {
				as[napodizations++].type = JFLAC__ApodizationSpecification.FLAC__APODIZATION_CONNES;
			} else if( n == 7  && specification.startsWith("flattop", i) ) {
				as[napodizations++].type = JFLAC__ApodizationSpecification.FLAC__APODIZATION_FLATTOP;
			} else if( n > 7   && specification.startsWith("gauss(", i) ) {
				final float stddev = (float)Jformat.strtod( specification.substring( i + 6 ) );
				if( stddev > 0.0f && stddev <= 0.5f ) {
					as[napodizations]./*parameters.gauss.*/stddev = stddev;
					as[napodizations++].type = JFLAC__ApodizationSpecification.FLAC__APODIZATION_GAUSS;
				}
			}
			else if( n == 7  && specification.startsWith("hamming", i) ) {
				as[napodizations++].type = JFLAC__ApodizationSpecification.FLAC__APODIZATION_HAMMING;
			} else if( n == 4  && specification.startsWith("hann", i) ) {
				as[napodizations++].type = JFLAC__ApodizationSpecification.FLAC__APODIZATION_HANN;
			} else if( n == 13 && specification.startsWith("kaiser_bessel", i) ) {
				as[napodizations++].type = JFLAC__ApodizationSpecification.FLAC__APODIZATION_KAISER_BESSEL;
			} else if( n == 7  && specification.startsWith("nuttall", i) ) {
				as[napodizations++].type = JFLAC__ApodizationSpecification.FLAC__APODIZATION_NUTTALL;
			} else if( n == 9  && specification.startsWith("rectangle", i) ) {
				as[napodizations++].type = JFLAC__ApodizationSpecification.FLAC__APODIZATION_RECTANGLE;
			} else if( n == 8  && specification.startsWith("triangle", i) ) {
				as[napodizations++].type = JFLAC__ApodizationSpecification.FLAC__APODIZATION_TRIANGLE;
			} else if( n > 7   && specification.startsWith("tukey(", i) ) {
				final float p = (float)Jformat.strtod( specification.substring( i + 6 ) );
				if( p >= 0.0f && p <= 1.0f ) {
					as[napodizations]./*parameters.tukey.*/p = p;
					as[napodizations++].type = JFLAC__ApodizationSpecification.FLAC__APODIZATION_TUKEY;
				}
			}
			else if( n > 15   && specification.startsWith("partial_tukey(", i) ) {
				final int tukey_parts = (int)Jformat.strtod( specification.substring( i + 14 ) );
				final int si_1 = specification.indexOf('/', i );
				final float overlap = si_1 >= 0 ? Math.min( (float)Jformat.strtod( specification.substring( si_1 + 1 ) ), 0.99f ) : 0.1f;
				final float overlap_units = 1.0f / (1.0f - overlap) - 1.0f;
				final int si_2 = specification.indexOf('/', (si_1 >= 0 ? (si_1 + 1) : i));
				final float tukey_p = si_2 >= 0 ? (float)Jformat.strtod( specification.substring( si_2 + 1 ) ) : 0.2f;

				if( tukey_parts <= 1 ) {
					as[napodizations]./*parameters.tukey.*/p = tukey_p;
					as[napodizations++].type = JFLAC__ApodizationSpecification.FLAC__APODIZATION_TUKEY;
				} else if( napodizations + tukey_parts < 32 ) {
					int m;
					for( m = 0; m < tukey_parts; m++ ) {
						as[napodizations]./*parameters.multiple_tukey.*/p = tukey_p;
						as[napodizations]./*parameters.multiple_tukey.*/start = m/(tukey_parts+overlap_units);
						as[napodizations]./*parameters.multiple_tukey.*/end = (m+1+overlap_units)/(tukey_parts+overlap_units);
						as[napodizations++].type = JFLAC__ApodizationSpecification.FLAC__APODIZATION_PARTIAL_TUKEY;
					}
				}
			}
			else if( n > 16   && specification.startsWith("punchout_tukey("       , i) ) {
				final int tukey_parts = (int)Jformat.strtod( specification.substring( i + 15 ) );
				final int si_1 = specification.indexOf('/', i );
				final float overlap = si_1 >= 0 ? Math.min( (float)Jformat.strtod( specification.substring( si_1 + 1 ) ), 0.99f ) : 0.2f;
				final float overlap_units = 1.0f / (1.0f - overlap) - 1.0f;
				final int si_2 = specification.indexOf('/', (si_1 >= 0 ? (si_1 + 1) : i));
				final float tukey_p = si_2 >= 0 ? (float)Jformat.strtod( specification.substring( si_2 + 1 ) ) : 0.2f;

				if( tukey_parts <= 1 ) {
					as[napodizations]./*parameters.tukey.*/p = tukey_p;
					as[napodizations++].type = JFLAC__ApodizationSpecification.FLAC__APODIZATION_TUKEY;
				} else if( napodizations + tukey_parts < 32 ) {
					int m;
					for( m = 0; m < tukey_parts; m++ ) {
						as[napodizations]./*parameters.multiple_tukey.*/p = tukey_p;
						as[napodizations]./*parameters.multiple_tukey.*/start = m / (tukey_parts + overlap_units);
						as[napodizations]./*parameters.multiple_tukey.*/end = (m + 1 + overlap_units) / (tukey_parts + overlap_units);
						as[napodizations++].type = JFLAC__ApodizationSpecification.FLAC__APODIZATION_PUNCHOUT_TUKEY;
					}
				}
			}
			else if( n == 5  && specification.startsWith("welch", i) ) {
				as[napodizations++].type = JFLAC__ApodizationSpecification.FLAC__APODIZATION_WELCH;
			}
			if( napodizations == 32 ) {
				break;
			}
			if( s >= 0 ) {
				i = s + 1;
			} else {
				break;
			}
		}
		if( napodizations == 0 ) {
			napodizations = 1;
			as[0].type = JFLAC__ApodizationSpecification.FLAC__APODIZATION_TUKEY;
			as[0]./*parameters.tukey.*/p = 0.5f;
		}
		this.num_apodizations = napodizations;
//#endif
		return true;
	}

	/** Set the maximum LPC order, or \c 0 to use only the fixed predictors.
	 *
	 * \default \c 0
	 * @param  encoder  An encoder instance to set.
	 * @param  value    See above.
	 * \assert
	 *    \code encoder != NULL \endcode
	 * @retval FLAC__bool
	 *    \c false if the encoder is already initialized, else \c true.
	 */
	public final boolean FLAC__stream_encoder_set_max_lpc_order(final int value)
	{
		//FLAC__ASSERT(0 != encoder);
		//FLAC__ASSERT(0 != encoder->private_);
		//FLAC__ASSERT(0 != encoder->protected_);
		if( this.state != FLAC__STREAM_ENCODER_UNINITIALIZED ) {
			return false;
		}
		this.max_lpc_order = value;
		return true;
	}

	/** Set the precision, in bits, of the quantized linear predictor
	 *  coefficients, or \c 0 to let the encoder select it based on the
	 *  blocksize.
	 *
	 * @note
	 * In the current implementation, qlp_coeff_precision + bits_per_sample must
	 * be less than 32.
	 *
	 * \default \c 0
	 * @param  encoder  An encoder instance to set.
	 * @param  value    See above.
	 * \assert
	 *    \code encoder != NULL \endcode
	 * @retval FLAC__bool
	 *    \c false if the encoder is already initialized, else \c true.
	 */
	public final boolean FLAC__stream_encoder_set_qlp_coeff_precision(final int value)
	{
		//FLAC__ASSERT(0 != encoder);
		//FLAC__ASSERT(0 != encoder->private_);
		//FLAC__ASSERT(0 != encoder->protected_);
		if( this.state != FLAC__STREAM_ENCODER_UNINITIALIZED ) {
			return false;
		}
		this.qlp_coeff_precision = value;
		return true;
	}

	/** Set to \c false to use only the specified quantized linear predictor
	 *  coefficient precision, or \c true to search neighboring precision
	 *  values and use the best one.
	 *
	 * \default \c false
	 * @param  encoder  An encoder instance to set.
	 * @param  value    See above.
	 * \assert
	 *    \code encoder != NULL \endcode
	 * @retval FLAC__bool
	 *    \c false if the encoder is already initialized, else \c true.
	 */
	public final boolean FLAC__stream_encoder_set_do_qlp_coeff_prec_search(final boolean value)
	{
		//FLAC__ASSERT(0 != encoder);
		//FLAC__ASSERT(0 != encoder->private_);
		//FLAC__ASSERT(0 != encoder->protected_);
		if( this.state != FLAC__STREAM_ENCODER_UNINITIALIZED ) {
			return false;
		}
		this.do_qlp_coeff_prec_search = value;
		return true;
	}

	/** Deprecated.  Setting this value has no effect.
	 *
	 * \default \c false
	 * @param  encoder  An encoder instance to set.
	 * @param  value    See above.
	 * \assert
	 *    \code encoder != NULL \endcode
	 * @retval FLAC__bool
	 *    \c false if the encoder is already initialized, else \c true.
	 */
	public final boolean FLAC__stream_encoder_set_do_escape_coding(final boolean value)
	{
		//FLAC__ASSERT(0 != encoder);
		//FLAC__ASSERT(0 != encoder->private_);
		//FLAC__ASSERT(0 != encoder->protected_);
		if( this.state != FLAC__STREAM_ENCODER_UNINITIALIZED ) {
			return false;
		}
//#if 0
		/*@@@ deprecated: */
//		encoder.protected_.do_escape_coding = value;
//#else
//		(void)value;
//#endif
		return true;
	}

	/** Set to \c false to let the encoder estimate the best model order
	 *  based on the residual signal energy, or \c true to force the
	 *  encoder to evaluate all order models and select the best.
	 *
	 * \default \c false
	 * @param  encoder  An encoder instance to set.
	 * @param  value    See above.
	 * \assert
	 *    \code encoder != NULL \endcode
	 * @retval FLAC__bool
	 *    \c false if the encoder is already initialized, else \c true.
	 */
	public final boolean FLAC__stream_encoder_set_do_exhaustive_model_search(final boolean value)
	{
		//FLAC__ASSERT(0 != encoder);
		//FLAC__ASSERT(0 != encoder->private_);
		//FLAC__ASSERT(0 != encoder->protected_);
		if( this.state != FLAC__STREAM_ENCODER_UNINITIALIZED ) {
			return false;
		}
		this.do_exhaustive_model_search = value;
		return true;
	}

	/** Set the minimum partition order to search when coding the residual.
	 *  This is used in tandem with
	 *  FLAC__stream_encoder_set_max_residual_partition_order().
	 *
	 *  The partition order determines the context size in the residual.
	 *  The context size will be approximately <tt>blocksize / (2 ^ order)</tt>.
	 *
	 *  Set both min and max values to \c 0 to force a single context,
	 *  whose Rice parameter is based on the residual signal variance.
	 *  Otherwise, set a min and max order, and the encoder will search
	 *  all orders, using the mean of each context for its Rice parameter,
	 *  and use the best.
	 *
	 * \default \c 0
	 * @param  encoder  An encoder instance to set.
	 * @param  value    See above.
	 * \assert
	 *    \code encoder != NULL \endcode
	 * @retval FLAC__bool
	 *    \c false if the encoder is already initialized, else \c true.
	 */
	public final boolean FLAC__stream_encoder_set_min_residual_partition_order(final int value)
	{
		//FLAC__ASSERT(0 != encoder);
		//FLAC__ASSERT(0 != encoder->private_);
		//FLAC__ASSERT(0 != encoder->protected_);
		if( this.state != FLAC__STREAM_ENCODER_UNINITIALIZED ) {
			return false;
		}
		this.min_residual_partition_order = value;
		return true;
	}

	/** Set the maximum partition order to search when coding the residual.
	 *  This is used in tandem with
	 *  FLAC__stream_encoder_set_min_residual_partition_order().
	 *
	 *  The partition order determines the context size in the residual.
	 *  The context size will be approximately <tt>blocksize / (2 ^ order)</tt>.
	 *
	 *  Set both min and max values to \c 0 to force a single context,
	 *  whose Rice parameter is based on the residual signal variance.
	 *  Otherwise, set a min and max order, and the encoder will search
	 *  all orders, using the mean of each context for its Rice parameter,
	 *  and use the best.
	 *
	 * \default \c 0
	 * @param  encoder  An encoder instance to set.
	 * @param  value    See above.
	 * \assert
	 *    \code encoder != NULL \endcode
	 * @retval FLAC__bool
	 *    \c false if the encoder is already initialized, else \c true.
	 */
	public final boolean FLAC__stream_encoder_set_max_residual_partition_order(final int value)
	{
		//FLAC__ASSERT(0 != encoder);
		//FLAC__ASSERT(0 != encoder->private_);
		//FLAC__ASSERT(0 != encoder->protected_);
		if( this.state != FLAC__STREAM_ENCODER_UNINITIALIZED ) {
			return false;
		}
		this.max_residual_partition_order = value;
		return true;
	}

	/** Deprecated.  Setting this value has no effect.
	 *
	 * \default \c 0
	 * @param  encoder  An encoder instance to set.
	 * @param  value    See above.
	 * \assert
	 *    \code encoder != NULL \endcode
	 * @retval FLAC__bool
	 *    \c false if the encoder is already initialized, else \c true.
	 */
	public final boolean FLAC__stream_encoder_set_rice_parameter_search_dist(final int value)
	{
		//FLAC__ASSERT(0 != encoder);
		//FLAC__ASSERT(0 != encoder->private_);
		//FLAC__ASSERT(0 != encoder->protected_);
		if( this.state != FLAC__STREAM_ENCODER_UNINITIALIZED ) {
			return false;
		}
//#if 0
		/*@@@ deprecated: */
//		encoder.protected_.rice_parameter_search_dist = value;
//#else
//		(void)value;
//#endif
		return true;
	}

	/** Set an estimate of the total samples that will be encoded.
	 *  This is merely an estimate and may be set to \c 0 if unknown.
	 *  This value will be written to the STREAMINFO block before encoding,
	 *  and can remove the need for the caller to rewrite the value later
	 *  if the value is known before encoding.
	 *
	 * \default \c 0
	 * @param  encoder  An encoder instance to set.
	 * @param  value    See above.
	 * \assert
	 *    \code encoder != NULL \endcode
	 * @retval FLAC__bool
	 *    \c false if the encoder is already initialized, else \c true.
	 */
	public final boolean FLAC__stream_encoder_set_total_samples_estimate(long value)
	{
		//FLAC__ASSERT(0 != encoder);
		//FLAC__ASSERT(0 != encoder->private_);
		//FLAC__ASSERT(0 != encoder->protected_);
		if( this.state != FLAC__STREAM_ENCODER_UNINITIALIZED ) {
			return false;
		}
		value = value < ((1L << Jformat.FLAC__STREAM_METADATA_STREAMINFO_TOTAL_SAMPLES_LEN) - 1) ?
				value : ((1L << Jformat.FLAC__STREAM_METADATA_STREAMINFO_TOTAL_SAMPLES_LEN) - 1);
		this.total_samples_estimate = value;
		return true;
	}

	/** Set the metadata blocks to be emitted to the stream before encoding.
	 *  A value of \c NULL, \c 0 implies no metadata; otherwise, supply an
	 *  array of pointers to metadata blocks.  The array is non-const since
	 *  the encoder may need to change the \a is_last flag inside them, and
	 *  in some cases update seek point offsets.  Otherwise, the encoder will
	 *  not modify or free the blocks.  It is up to the caller to free the
	 *  metadata blocks after encoding finishes.
	 *
	 * @note
	 * The encoder stores only copies of the pointers in the \a metadata array;
	 * the metadata blocks themselves must survive at least until after
	 * FLAC__stream_encoder_finish() returns.  Do not free the blocks until then.
	 *
	 * @note
	 * The STREAMINFO block is always written and no STREAMINFO block may
	 * occur in the supplied array.
	 *
	 * @note
	 * By default the encoder does not create a SEEKTABLE.  If one is supplied
	 * in the \a metadata array, but the client has specified that it does not
	 * support seeking, then the SEEKTABLE will be written verbatim.  However
	 * by itself this is not very useful as the client will not know the stream
	 * offsets for the seekpoints ahead of time.  In order to get a proper
	 * seektable the client must support seeking.  See next note.
	 *
	 * @note
	 * SEEKTABLE blocks are handled specially.  Since you will not know
	 * the values for the seek point stream offsets, you should pass in
	 * a SEEKTABLE 'template', that is, a SEEKTABLE object with the
	 * required sample numbers (or placeholder points), with \c 0 for the
	 * \a frame_samples and \a stream_offset fields for each point.  If the
	 * client has specified that it supports seeking by providing a seek
	 * callback to FLAC__stream_encoder_init_stream() or both seek AND read
	 * callback to FLAC__stream_encoder_init_ogg_stream() (or by using
	 * FLAC__stream_encoder_init*_file() or FLAC__stream_encoder_init*_FILE()),
	 * then while it is encoding the encoder will fill the stream offsets in
	 * for you and when encoding is finished, it will seek back and write the
	 * real values into the SEEKTABLE block in the stream.  There are helper
	 * routines for manipulating seektable template blocks; see metadata.h:
	 * FLAC__metadata_object_seektable_template_*().  If the client does
	 * not support seeking, the SEEKTABLE will have inaccurate offsets which
	 * will slow down or remove the ability to seek in the FLAC stream.
	 *
	 * @note
	 * The encoder instance \b will modify the first \c SEEKTABLE block
	 * as it transforms the template to a valid seektable while encoding,
	 * but it is still up to the caller to free all metadata blocks after
	 * encoding.
	 *
	 * @note
	 * A VORBIS_COMMENT block may be supplied.  The vendor string in it
	 * will be ignored.  libFLAC will use it's own vendor string. libFLAC
	 * will not modify the passed-in VORBIS_COMMENT's vendor string, it
	 * will simply write it's own into the stream.  If no VORBIS_COMMENT
	 * block is present in the \a metadata array, libFLAC will write an
	 * empty one, containing only the vendor string.
	 *
	 * @note The Ogg FLAC mapping requires that the VORBIS_COMMENT block be
	 * the second metadata block of the stream.  The encoder already supplies
	 * the STREAMINFO block automatically.  If \a metadata does not contain a
	 * VORBIS_COMMENT block, the encoder will supply that too.  Otherwise, if
	 * \a metadata does contain a VORBIS_COMMENT block and it is not the
	 * first, the init function will reorder \a metadata by moving the
	 * VORBIS_COMMENT block to the front; the relative ordering of the other
	 * blocks will remain as they were.
	 *
	 * @note The Ogg FLAC mapping limits the number of metadata blocks per
	 * stream to \c 65535.  If \a num_blocks exceeds this the function will
	 * return \c false.
	 *
	 * \default \c NULL, 0
	 * @param  encoder     An encoder instance to set.
	 * @param  meta_data    See above.
	 * @param  num_blocks  See above.
	 * \assert
	 *    \code encoder != NULL \endcode
	 * @retval FLAC__bool
	 *    \c false if the encoder is already initialized, else \c true.
	 *    \c false if the encoder is already initialized, or if
	 *    \a num_blocks > 65535 if encoding to Ogg FLAC, else \c true.
	 */
	public final boolean FLAC__stream_encoder_set_metadata(final JFLAC__StreamMetadata[] meta_data, final int num_blocks)
	{
		//FLAC__ASSERT(0 != encoder);
		//FLAC__ASSERT(0 != encoder->private_);
		//FLAC__ASSERT(0 != encoder->protected_);
		if( this.state != FLAC__STREAM_ENCODER_UNINITIALIZED ) {
			return false;
		}
		//if( null == metadata )// java: moved to another if
		//	num_blocks = 0;
		//if( 0 == num_blocks )
		//	metadata = null;// FIXME potential null access
		/* realloc() does not do exactly what we want so... */
		if( this.metadata != null ) {
			this.metadata = null;
			// this.num_metadata_blocks = 0;
		}
		if( null != meta_data /*num_blocks != 0*/ ) {
			final JFLAC__StreamMetadata[] m = new JFLAC__StreamMetadata[num_blocks];
			for( int i = 0; i < num_blocks; i++ ) {// TODO check, c uses full copy
				m[i] = meta_data[i];
			}
			this.metadata = m;
			// this.num_metadata_blocks = num_blocks;// java: this.metadata.length
		}
if( Jformat.FLAC__HAS_OGG ) {
		if( ! this.ogg_encoder_aspect.FLAC__ogg_encoder_aspect_set_num_metadata( num_blocks ) ) {
			return false;
		}
}
		return true;
	}

	/*
	 * These three functions are not static, but not publically exposed in
	 * include/FLAC/ either.  They are used by the test suite.
	 */
	public final boolean FLAC__stream_encoder_disable_constant_subframes(final boolean value)
	{
		//FLAC__ASSERT(0 != encoder);
		//FLAC__ASSERT(0 != encoder->private_);
		//FLAC__ASSERT(0 != encoder->protected_);
		if( this.state != FLAC__STREAM_ENCODER_UNINITIALIZED ) {
			return false;
		}
		this.disable_constant_subframes = value;
		return true;
	}

	public final boolean FLAC__stream_encoder_disable_fixed_subframes(final boolean value)
	{
		//FLAC__ASSERT(0 != encoder);
		//FLAC__ASSERT(0 != encoder->private_);
		//FLAC__ASSERT(0 != encoder->protected_);
		if( this.state != FLAC__STREAM_ENCODER_UNINITIALIZED ) {
			return false;
		}
		this.disable_fixed_subframes = value;
		return true;
	}

	public final boolean FLAC__stream_encoder_disable_verbatim_subframes(final boolean value)
	{
		//FLAC__ASSERT(0 != encoder);
		//FLAC__ASSERT(0 != encoder->private_);
		//FLAC__ASSERT(0 != encoder->protected_);
		if( this.state != FLAC__STREAM_ENCODER_UNINITIALIZED ) {
			return false;
		}
		this.disable_verbatim_subframes = value;
		return true;
	}

	public final int /* FLAC__StreamEncoderState */ FLAC__stream_encoder_get_state()
	{
		//FLAC__ASSERT(0 != encoder);
		//FLAC__ASSERT(0 != encoder->private_);
		//FLAC__ASSERT(0 != encoder->protected_);
		return this.state;
	}

	public final int /* FLAC__StreamDecoderState */ FLAC__stream_encoder_get_verify_decoder_state()
	{
		//FLAC__ASSERT(0 != encoder);
		//FLAC__ASSERT(0 != encoder->private_);
		//FLAC__ASSERT(0 != encoder->protected_);
		if( this.do_verify ) {
			return this.verify.decoder.FLAC__stream_decoder_get_state();
		} else {
			return JFLAC__StreamDecoder.FLAC__STREAM_DECODER_UNINITIALIZED;
		}
	}

	public final String FLAC__stream_encoder_get_resolved_state_string()
	{
		//FLAC__ASSERT(0 != encoder);
		//FLAC__ASSERT(0 != encoder->private_);
		//FLAC__ASSERT(0 != encoder->protected_);
		if( this.state != FLAC__STREAM_ENCODER_VERIFY_DECODER_ERROR) {
			return FLAC__StreamEncoderStateString[this.state];
		} else {
			return this.verify.decoder.FLAC__stream_decoder_get_resolved_state_string();
		}
	}

	// java: changed
	public final Jerror_stats FLAC__stream_encoder_get_verify_decoder_error_stats(/*long[] absolute_sample, int[] frame_number, int[] channel, int[] sample, int[] expected, int[] got*/)
	{
		return this.verify.error_stats;
	}

	public final boolean FLAC__stream_encoder_get_verify()
	{
		//FLAC__ASSERT(0 != encoder);
		//FLAC__ASSERT(0 != encoder->private_);
		//FLAC__ASSERT(0 != encoder->protected_);
		return this.do_verify;
	}

	public final boolean FLAC__stream_encoder_get_streamable_subset()
	{
		//FLAC__ASSERT(0 != encoder);
		//FLAC__ASSERT(0 != encoder->private_);
		//FLAC__ASSERT(0 != encoder->protected_);
		return this.streamable_subset;
	}

	public final boolean FLAC__stream_encoder_get_do_md5()
	{
		//FLAC__ASSERT(0 != encoder);
		//FLAC__ASSERT(0 != encoder->private_);
		//FLAC__ASSERT(0 != encoder->protected_);
		return this.do_md5;
	}

	public final int FLAC__stream_encoder_get_channels()
	{
		//FLAC__ASSERT(0 != encoder);
		//FLAC__ASSERT(0 != encoder->private_);
		//FLAC__ASSERT(0 != encoder->protected_);
		return this.channels;
	}

	public final int FLAC__stream_encoder_get_bits_per_sample()
	{
		//FLAC__ASSERT(0 != encoder);
		//FLAC__ASSERT(0 != encoder->private_);
		//FLAC__ASSERT(0 != encoder->protected_);
		return this.bits_per_sample;
	}

	public final int FLAC__stream_encoder_get_sample_rate()
	{
		//FLAC__ASSERT(0 != encoder);
		//FLAC__ASSERT(0 != encoder->private_);
		//FLAC__ASSERT(0 != encoder->protected_);
		return this.sample_rate;
	}

	public final int FLAC__stream_encoder_get_blocksize()
	{
		//FLAC__ASSERT(0 != encoder);
		//FLAC__ASSERT(0 != encoder->private_);
		//FLAC__ASSERT(0 != encoder->protected_);
		return this.blocksize;
	}

	public final boolean FLAC__stream_encoder_get_do_mid_side_stereo()
	{
		//FLAC__ASSERT(0 != encoder);
		//FLAC__ASSERT(0 != encoder->private_);
		//FLAC__ASSERT(0 != encoder->protected_);
		return this.do_mid_side_stereo;
	}

	public final boolean FLAC__stream_encoder_get_loose_mid_side_stereo()
	{
		//FLAC__ASSERT(0 != encoder);
		//FLAC__ASSERT(0 != encoder->private_);
		//FLAC__ASSERT(0 != encoder->protected_);
		return this.loose_mid_side_stereo;
	}

	public final int FLAC__stream_encoder_get_max_lpc_order()
	{
		//FLAC__ASSERT(0 != encoder);
		//FLAC__ASSERT(0 != encoder->private_);
		//FLAC__ASSERT(0 != encoder->protected_);
		return this.max_lpc_order;
	}

	public final int FLAC__stream_encoder_get_qlp_coeff_precision()
	{
		//FLAC__ASSERT(0 != encoder);
		//FLAC__ASSERT(0 != encoder->private_);
		//FLAC__ASSERT(0 != encoder->protected_);
		return this.qlp_coeff_precision;
	}

	public final boolean FLAC__stream_encoder_get_do_qlp_coeff_prec_search()
	{
		//FLAC__ASSERT(0 != encoder);
		//FLAC__ASSERT(0 != encoder->private_);
		//FLAC__ASSERT(0 != encoder->protected_);
		return this.do_qlp_coeff_prec_search;
	}

	public final boolean FLAC__stream_encoder_get_do_escape_coding()
	{
		//FLAC__ASSERT(0 != encoder);
		//FLAC__ASSERT(0 != encoder->private_);
		//FLAC__ASSERT(0 != encoder->protected_);
		return this.do_escape_coding;
	}

	public final boolean FLAC__stream_encoder_get_do_exhaustive_model_search()
	{
		//FLAC__ASSERT(0 != encoder);
		//FLAC__ASSERT(0 != encoder->private_);
		//FLAC__ASSERT(0 != encoder->protected_);
		return this.do_exhaustive_model_search;
	}

	public final int FLAC__stream_encoder_get_min_residual_partition_order()
	{
		//FLAC__ASSERT(0 != encoder);
		//FLAC__ASSERT(0 != encoder->private_);
		//FLAC__ASSERT(0 != encoder->protected_);
		return this.min_residual_partition_order;
	}

	public final int FLAC__stream_encoder_get_max_residual_partition_order()
	{
		//FLAC__ASSERT(0 != encoder);
		//FLAC__ASSERT(0 != encoder->private_);
		//FLAC__ASSERT(0 != encoder->protected_);
		return this.max_residual_partition_order;
	}

	public final int FLAC__stream_encoder_get_rice_parameter_search_dist()
	{
		//FLAC__ASSERT(0 != encoder);
		//FLAC__ASSERT(0 != encoder->private_);
		//FLAC__ASSERT(0 != encoder->protected_);
		return this.rice_parameter_search_dist;
	}

	public final long FLAC__stream_encoder_get_total_samples_estimate()
	{
		//FLAC__ASSERT(0 != encoder);
		//FLAC__ASSERT(0 != encoder->private_);
		//FLAC__ASSERT(0 != encoder->protected_);
		return this.total_samples_estimate;
	}

	public final boolean FLAC__stream_encoder_process(final int buffer[][], final int samples)
	{
		int j = 0;
		final int nchannels = this.channels, block_size = this.blocksize;

		//FLAC__ASSERT(0 != encoder);
		//FLAC__ASSERT(0 != encoder->private_);
		//FLAC__ASSERT(0 != encoder->protected_);
		//FLAC__ASSERT(encoder->protected_->state == FLAC__STREAM_ENCODER_OK);

		final int[] integer_signal_mid_side0 = this.integer_signal_mid_side[0];// java
		final int[] integer_signal_mid_side1 = this.integer_signal_mid_side[1];// java
		final int[] buffer0 = buffer[0];// java
		final int[] buffer1 = buffer[1];// java
		final int[][] signal = this.integer_signal;// java
		do {
			int i = samples - j;
			int n = block_size + OVERREAD_ - this.current_sample_number;
			if( n > i ) {
				n = i;
			}

			if( this.do_verify ) {
				this.verify.input_fifo.append_to_verify_fifo_( buffer, j, nchannels, n );
			}

			for( int channel = 0; channel < nchannels; channel++ ) {
				final int b[] = buffer[channel];// java
				if( b == null ) {
					return false;
				}
				System.arraycopy( b, j, signal[channel], this.current_sample_number, n );
			}

			if( this.do_mid_side_stereo ) {
				//FLAC__ASSERT(channels == 2);
				/* "i <= blocksize" to overread 1 sample; see comment in OVERREAD_ decl */
				for( i = this.current_sample_number; i <= block_size && j < samples; i++, j++ ) {
					integer_signal_mid_side1[i] = buffer0[j] - buffer1[j];
					integer_signal_mid_side0[i] = (buffer0[j] + buffer1[j]) >> 1; /* NOTE: not the same as 'mid = (buffer[0][j] + buffer[1][j]) / 2' ! */
				}
			} else {
				j += n;
			}

			this.current_sample_number += n;

			/* we only process if we have a full block + 1 extra sample; final block is always handled by FLAC__stream_encoder_finish() */
			if( this.current_sample_number > block_size) {
				//FLAC__ASSERT(encoder->private_->current_sample_number == blocksize+OVERREAD_);
				//FLAC__ASSERT(OVERREAD_ == 1); /* assert we only overread 1 sample which simplifies the rest of the code below */
				if( ! process_frame_( /*is_fractional_block=*/false, /*is_last_block=*/false ) ) {
					return false;
				}
				/* move unprocessed overread samples to beginnings of arrays */
				for( int channel = 0; channel < nchannels; channel++ ) {
					signal[channel][0] = signal[channel][block_size];
				}
				if( this.do_mid_side_stereo) {
					integer_signal_mid_side0[0] = integer_signal_mid_side0[block_size];
					integer_signal_mid_side1[0] = integer_signal_mid_side1[block_size];
				}
				this.current_sample_number = 1;
			}
		} while( j < samples );

		return true;
	}

	public final boolean FLAC__stream_encoder_process_interleaved(final int buffer[], final int samples)
	{
		final int nchannels = this.channels, block_size = this.blocksize;

		//FLAC__ASSERT(0 != encoder);
		//FLAC__ASSERT(0 != encoder->private_);
		//FLAC__ASSERT(0 != encoder->protected_);
		//FLAC__ASSERT(encoder->protected_->state == FLAC__STREAM_ENCODER_OK);

		int j = 0, k = 0;
		/*
		 * we have several flavors of the same basic loop, optimized for
		 * different conditions:
		 */
		if( this.do_mid_side_stereo && nchannels == 2 ) {
			/*
			 * stereo coding: unroll channel loop
			 */
			final int[] integer_signal0 = this.integer_signal[0];// java
			final int[] integer_signal1 = this.integer_signal[1];// java
			final int[] integer_signal_mid_side0 = this.integer_signal_mid_side[0];// java
			final int[] integer_signal_mid_side1 = this.integer_signal_mid_side[1];// java
			do {
				if( this.do_verify ) {
					int x = block_size + OVERREAD_ - this.current_sample_number;
					final int i = samples - j;
					if( x > i ) {
						x = i;
					}
					this.verify.input_fifo.append_to_verify_fifo_interleaved_( buffer, j, nchannels, x );
				}

				/* "i <= blocksize" to overread 1 sample; see comment in OVERREAD_ decl */
				int i;
				for( i = this.current_sample_number; i <= block_size && j < samples; i++, j++ ) {
					int mid, side;
					integer_signal0[i] = mid = side = buffer[k++];
					final int x = buffer[k++];
					integer_signal1[i] = x;
					mid += x;
					side -= x;
					mid >>= 1; /* NOTE: not the same as 'mid = (left + right) / 2' ! */
					integer_signal_mid_side1[i] = side;
					integer_signal_mid_side0[i] = mid;
				}
				this.current_sample_number = i;
				/* we only process if we have a full block + 1 extra sample; final block is always handled by FLAC__stream_encoder_finish() */
				if( i > block_size ) {
					if( ! process_frame_( /*is_fractional_block=*/false, /*is_last_block=*/false ) ) {
						return false;
					}
					/* move unprocessed overread samples to beginnings of arrays */
					//FLAC__ASSERT(i == blocksize+OVERREAD_);
					//FLAC__ASSERT(OVERREAD_ == 1); /* assert we only overread 1 sample which simplifies the rest of the code below */
					integer_signal0[0] = integer_signal0[block_size];
					integer_signal1[0] = integer_signal1[block_size];
					integer_signal_mid_side0[0] = integer_signal_mid_side0[block_size];
					integer_signal_mid_side1[0] = integer_signal_mid_side1[block_size];
					this.current_sample_number = 1;
				}
			} while( j < samples );
		}
		else {
			/*
			 * independent channel coding: buffer each channel in inner loop
			 */
			do {
				if( this.do_verify ) {
					int x = block_size + OVERREAD_ - this.current_sample_number;
					final int i = samples - j;
					if( x > i ) {
						x = i;
					}
					this.verify.input_fifo.append_to_verify_fifo_interleaved_( buffer, j, nchannels, x );
				}

				/* "i <= blocksize" to overread 1 sample; see comment in OVERREAD_ decl */
				final int[][] signal = this.integer_signal;// java
				int i;
				for( i = this.current_sample_number; i <= block_size && j < samples; i++, j++ ) {
					for( int channel = 0; channel < nchannels; channel++ ) {
						signal[channel][i] = buffer[k++];
					}
				}
				this.current_sample_number = i;
				/* we only process if we have a full block + 1 extra sample; final block is always handled by FLAC__stream_encoder_finish() */
				if( i > block_size) {
					if( ! process_frame_( /*is_fractional_block=*/false, /*is_last_block=*/false ) ) {
						return false;
					}
					/* move unprocessed overread samples to beginnings of arrays */
					//FLAC__ASSERT(i == blocksize+OVERREAD_);
					//FLAC__ASSERT(OVERREAD_ == 1); /* assert we only overread 1 sample which simplifies the rest of the code below */
					for( int channel = 0; channel < nchannels; channel++ ) {
						signal[channel][0] = signal[channel][block_size];
					}
					this.current_sample_number = 1;
				}
			} while( j < samples );
		}

		return true;
	}

	/***********************************************************************
	 *
	 * Private class methods
	 *
	 ***********************************************************************/

	private final void set_defaults_()
	{
		//FLAC__ASSERT(0 != encoder);

/*#ifdef FLAC__MANDATORY_VERIFY_WHILE_ENCODING
		encoder.do_verify = true;
#else*/
		this.do_verify = false;
//#endif
		this.streamable_subset = true;
		this.do_md5 = true;
		this.do_mid_side_stereo = false;
		this.loose_mid_side_stereo = false;
		this.channels = 2;
		this.bits_per_sample = 16;
		this.sample_rate = 44100;
		this.blocksize = 0;
//#ifndef FLAC__INTEGER_ONLY_LIBRARY
		this.num_apodizations = 1;
		this.apodizations[0].type = JFLAC__ApodizationSpecification.FLAC__APODIZATION_TUKEY;
		this.apodizations[0]./*parameters.tukey.*/p = 0.5f;
//#endif
		this.max_lpc_order = 0;
		this.qlp_coeff_precision = 0;
		this.do_qlp_coeff_prec_search = false;
		this.do_exhaustive_model_search = false;
		this.do_escape_coding = false;
		this.min_residual_partition_order = 0;
		this.max_residual_partition_order = 0;
		this.rice_parameter_search_dist = 0;
		this.total_samples_estimate = 0;
		this.metadata = null;
		// this.num_metadata_blocks = 0;

		this.seek_table = null;
		this.disable_constant_subframes = false;
		this.disable_fixed_subframes = false;
		this.disable_verbatim_subframes = false;
		this.is_ogg = false;
		this.read_callback = null;
		this.write_callback = null;
		this.seek_callback = null;
		this.tell_callback = null;
		this.metadata_callback = null;
		this.progress_callback = null;
		// this.client_data = null;

if( Jformat.FLAC__HAS_OGG ) {
		this.ogg_encoder_aspect.FLAC__ogg_encoder_aspect_set_defaults();
}
		FLAC__stream_encoder_set_compression_level( 5 );
	}

	private final void free_()
	{

		//FLAC__ASSERT(0 != encoder);
		this.metadata = null;
		// this.num_metadata_blocks = 0;
/*
		for( i = 0; i < this.channels; i++ ) {
			this.integer_signal_unaligned[i] = null;
//#ifndef FLAC__INTEGER_ONLY_LIBRARY
			this.real_signal_unaligned[i] = null;
//#endif
		}
		for( i = 0; i < 2; i++ ) {
			this.integer_signal_mid_side_unaligned[i] = null;
//#ifndef FLAC__INTEGER_ONLY_LIBRARY
			this.real_signal_mid_side_unaligned[i] = null;
//#endif
		}
//#ifndef FLAC__INTEGER_ONLY_LIBRARY
		for( i = 0; i < this.num_apodizations; i++ ) {
			this.window_unaligned[i] = null;
		}
		this.windowed_signal_unaligned = null;
//#endif
		for( channel = 0; channel < this.channels; channel++ ) {
			for( i = 0; i < 2; i++ ) {
				this.residual_workspace_unaligned[channel][i] = null;
			}
		}
		for( channel = 0; channel < 2; channel++ ) {
			for( i = 0; i < 2; i++ ) {
				this.residual_workspace_mid_side_unaligned[channel][i] = null;
			}
		}
		this.abs_residual_partition_sums_unaligned = null;

		this.raw_bits_per_partition_unaligned = null;
*/
		if( this.do_verify ) {
			for( int i = 0; i < this.channels; i++ ) {
				this.verify.input_fifo.data[i] = null;
			}
		}
		this.frame.FLAC__bitwriter_free();
	}

	private final boolean resize_buffers_(final int new_blocksize)
	{
		//FLAC__ASSERT(new_blocksize > 0);
		//FLAC__ASSERT(encoder->protected_->state == FLAC__STREAM_ENCODER_OK);
		//FLAC__ASSERT(encoder->private_->current_sample_number == 0);

		/* To avoid excessive malloc'ing, we only grow the buffer; no shrinking. */
		if( new_blocksize <= this.input_capacity ) {
			return true;
		}

	try {

		/* WATCHOUT: FLAC__lpc_compute_residual_from_qlp_coefficients_asm_ia32_mmx() and ..._intrin_sse2()
		 * require that the input arrays (in our case the integer signals)
		 * have a buffer of up to 3 zeroes in front (at negative indices) for
		 * alignment purposes; we use 4 in front to keep the data well-aligned.
		 */

		for( int i = 0; i < this.channels; i++ ) {
			/*this.integer_signal_unaligned[i] = */this.integer_signal[i] = new int[new_blocksize + 4 + OVERREAD_];// already zeroed
			//memset(encoder.private_.integer_signal[i], 0, sizeof(FLAC__int32)*4);
			//encoder.private_.integer_signal[i] += 4;
//#ifndef FLAC__INTEGER_ONLY_LIBRARY
//#if 0 /* @@@ currently unused */
//			if( encoder.protected_.max_lpc_order > 0)
//				ok = ok && FLAC__memory_alloc_aligned_real_array(new_blocksize+OVERREAD_, &encoder.private_.real_signal_unaligned[i], &encoder.private_.real_signal[i]);
//#endif
//#endif
		}
		for( int i = 0; i < 2; i++ ) {
			/*this.integer_signal_mid_side_unaligned[i] = */this.integer_signal_mid_side[i] = new int[new_blocksize + 4 + OVERREAD_];// already zeroed
			//memset(encoder.private_.integer_signal_mid_side[i], 0, sizeof(FLAC__int32)*4);
			//encoder.private_.integer_signal_mid_side[i] += 4;
//#ifndef FLAC__INTEGER_ONLY_LIBRARY
//#if 0 /* @@@ currently unused */
//			if( encoder.protected_.max_lpc_order > 0 )
//				ok = ok && FLAC__memory_alloc_aligned_real_array(new_blocksize+OVERREAD_, &encoder.private_.real_signal_mid_side_unaligned[i], &encoder.private_.real_signal_mid_side[i]);
//#endif
//#endif
		}
//#ifndef FLAC__INTEGER_ONLY_LIBRARY
		if( this.max_lpc_order > 0 ) {
			for( int i = 0; i < this.num_apodizations; i++ ) {
				/*this.window_unaligned[i] = */this.window[i] = new float[new_blocksize];
			}
			/*this.windowed_signal_unaligned = */this.windowed_signal = new float[new_blocksize];
		}
//#endif
		for( int channel = 0; channel < this.channels; channel++ ) {
			for( int i = 0; i < 2; i++ ) {
				/*this.residual_workspace_unaligned[channel][i] = */this.residual_workspace[channel][i] = new int[new_blocksize];
			}
		}
		for( int channel = 0; channel < 2; channel++ ) {
			for( int i = 0; i < 2; i++ ) {
				/*this.residual_workspace_mid_side_unaligned[channel][i] = */this.residual_workspace_mid_side[channel][i] = new int[new_blocksize];
			}
		}
		/* the *2 is an approximation to the series 1 + 1/2 + 1/4 + ... that sums tree occupies in a flat array */
		/*@@@ new_blocksize*2 is too pessimistic, but to fix, we need smarter logic because a smaller new_blocksize can actually increase the # of partitions; would require moving this out into a separate function, then checking its capacity against the need of the current blocksize&min/max_partition_order (and maybe predictor order) */
		/*this.abs_residual_partition_sums_unaligned = */this.abs_residual_partition_sums = new long[new_blocksize << 1];
		if( this.do_escape_coding ) {
			/*this.raw_bits_per_partition_unaligned = */this.raw_bits_per_partition = new int[new_blocksize << 1];
		}

		/* now adjust the windows if the blocksize has changed */
//#ifndef FLAC__INTEGER_ONLY_LIBRARY
		if( new_blocksize != this.input_capacity && this.max_lpc_order > 0 ) {
			for( int i = 0; i < this.num_apodizations; i++ ) {
				switch( this.apodizations[i].type ) {
					case JFLAC__ApodizationSpecification.FLAC__APODIZATION_BARTLETT:
						Jwindow.FLAC__window_bartlett( this.window[i], new_blocksize );
						break;
					case JFLAC__ApodizationSpecification.FLAC__APODIZATION_BARTLETT_HANN:
						Jwindow.FLAC__window_bartlett_hann( this.window[i], new_blocksize );
						break;
					case JFLAC__ApodizationSpecification.FLAC__APODIZATION_BLACKMAN:
						Jwindow.FLAC__window_blackman( this.window[i], new_blocksize );
						break;
					case JFLAC__ApodizationSpecification.FLAC__APODIZATION_BLACKMAN_HARRIS_4TERM_92DB_SIDELOBE:
						Jwindow.FLAC__window_blackman_harris_4term_92db_sidelobe( this.window[i], new_blocksize );
						break;
					case JFLAC__ApodizationSpecification.FLAC__APODIZATION_CONNES:
						Jwindow.FLAC__window_connes( this.window[i], new_blocksize );
						break;
					case JFLAC__ApodizationSpecification.FLAC__APODIZATION_FLATTOP:
						Jwindow.FLAC__window_flattop( this.window[i], new_blocksize );
						break;
					case JFLAC__ApodizationSpecification.FLAC__APODIZATION_GAUSS:
						Jwindow.FLAC__window_gauss( this.window[i], new_blocksize, this.apodizations[i]./*parameters.gauss.*/stddev );
						break;
					case JFLAC__ApodizationSpecification.FLAC__APODIZATION_HAMMING:
						Jwindow.FLAC__window_hamming( this.window[i], new_blocksize );
						break;
					case JFLAC__ApodizationSpecification.FLAC__APODIZATION_HANN:
						Jwindow.FLAC__window_hann( this.window[i], new_blocksize );
						break;
					case JFLAC__ApodizationSpecification.FLAC__APODIZATION_KAISER_BESSEL:
						Jwindow.FLAC__window_kaiser_bessel( this.window[i], new_blocksize );
						break;
					case JFLAC__ApodizationSpecification.FLAC__APODIZATION_NUTTALL:
						Jwindow.FLAC__window_nuttall( this.window[i], new_blocksize );
						break;
					case JFLAC__ApodizationSpecification.FLAC__APODIZATION_RECTANGLE:
						Jwindow.FLAC__window_rectangle( this.window[i], new_blocksize );
						break;
					case JFLAC__ApodizationSpecification.FLAC__APODIZATION_TRIANGLE:
						Jwindow.FLAC__window_triangle( this.window[i], new_blocksize );
						break;
					case JFLAC__ApodizationSpecification.FLAC__APODIZATION_TUKEY:
						Jwindow.FLAC__window_tukey( this.window[i], new_blocksize, this.apodizations[i]./*parameters.tukey.*/p );
						break;
					case JFLAC__ApodizationSpecification.FLAC__APODIZATION_PARTIAL_TUKEY:
						Jwindow.FLAC__window_partial_tukey( this.window[i], new_blocksize, this.apodizations[i]./*parameters.multiple_tukey.*/p, this.apodizations[i]./*parameters.multiple_tukey.*/start, this.apodizations[i]./*parameters.multiple_tukey.*/end );
						break;
					case JFLAC__ApodizationSpecification.FLAC__APODIZATION_PUNCHOUT_TUKEY:
						Jwindow.FLAC__window_punchout_tukey( this.window[i], new_blocksize, this.apodizations[i]./*parameters.multiple_tukey.*/p, this.apodizations[i]./*parameters.multiple_tukey.*/start, this.apodizations[i]./*parameters.multiple_tukey.*/end );
						break;
					case JFLAC__ApodizationSpecification.FLAC__APODIZATION_WELCH:
						Jwindow.FLAC__window_welch( this.window[i], new_blocksize );
						break;
					default:
						//FLAC__ASSERT(0);
						/* double protection */
						Jwindow.FLAC__window_hann( this.window[i], new_blocksize );
						break;
				}
			}
		}
//#endif

			this.input_capacity = new_blocksize;
	} catch( final OutOfMemoryError e ) {
			this.state = FLAC__STREAM_ENCODER_MEMORY_ALLOCATION_ERROR;
			return false;
	}

		return true;
	}

	private final boolean write_bitbuffer_(final int samples, final boolean is_last_block)
	{
		final Jbitwriter_helper buffer;

		//FLAC__ASSERT(FLAC__bitwriter_is_byte_aligned(encoder.private_.frame));

		if( null == (buffer = this.frame.FLAC__bitwriter_get_buffer( /* buffer, bytes */ )) ) {
			this.state = FLAC__STREAM_ENCODER_MEMORY_ALLOCATION_ERROR;
			return false;
		}

		//bytes = buffer.length;
		if( this.do_verify ) {
			this.verify.output.data = buffer.bytebuffer;
			this.verify.output.offset = 0;
			this.verify.output.bytes = buffer.bytes;
			if( this.verify.state_hint == ENCODER_IN_MAGIC ) {
				this.verify.needs_magic_hack = true;
			}
			else {
				if( ! this.verify.decoder.FLAC__stream_decoder_process_single() ) {
					this.frame.FLAC__bitwriter_release_buffer();
					this.frame.FLAC__bitwriter_clear();
					if( this.state != FLAC__STREAM_ENCODER_VERIFY_MISMATCH_IN_AUDIO_DATA ) {
						this.state = FLAC__STREAM_ENCODER_VERIFY_DECODER_ERROR;
					}
					return false;
				}
			}
		}

		if( write_frame_( buffer.bytebuffer, buffer.bytes, samples, is_last_block ) != FLAC__STREAM_ENCODER_WRITE_STATUS_OK ) {
			this.frame.FLAC__bitwriter_release_buffer();
			this.frame.FLAC__bitwriter_clear();
			this.state = FLAC__STREAM_ENCODER_CLIENT_ERROR;
			return false;
		}

		this.frame.FLAC__bitwriter_release_buffer();
		this.frame.FLAC__bitwriter_clear();

		if( samples > 0 ) {
			final JFLAC__StreamMetadata_StreamInfo stream_info = this.streaminfo;
			int m = stream_info.min_framesize;
			if( m > buffer.bytes ) {
				m = buffer.bytes;// min
			}
			stream_info.min_framesize = m;
			m = stream_info.max_framesize;
			if( m < buffer.bytes ) {
				m = buffer.bytes;// max
			}
			stream_info.max_framesize = m;
		}

		return true;
	}

	private final int /* FLAC__StreamEncoderWriteStatus */ write_frame_(final byte buffer[], final int bytes, final int samples, final boolean is_last_block)
	{
		int /* FLAC__StreamEncoderWriteStatus */ status;
		long output_position = 0;

		/* FLAC__STREAM_ENCODER_TELL_STATUS_UNSUPPORTED just means we didn't get the offset; no error */
		try {
			if( this.tell_callback != null ) {
				output_position = this.tell_callback.enc_tell_callback( this/*, this.client_data*/ );
			}
		} catch(final IOException e) {
			this.state = FLAC__STREAM_ENCODER_CLIENT_ERROR;
			return FLAC__STREAM_ENCODER_WRITE_STATUS_FATAL_ERROR;
		} catch(final UnsupportedOperationException e) {
		}

		/*
		 * Watch for the STREAMINFO block and first SEEKTABLE block to go by and store their offsets.
		 */
		if( samples == 0 ) {
			final int /* FLAC__MetadataType */ type = (buffer[0] & 0x7f);
			if( type == Jformat.FLAC__METADATA_TYPE_STREAMINFO ) {
				this.streaminfo_offset = output_position;
			} else if( type == Jformat.FLAC__METADATA_TYPE_SEEKTABLE && this.seektable_offset == 0 ) {
				this.seektable_offset = output_position;
			}
		}

		/*
		 * Mark the current seek point if hit (if audio_offset == 0 that
		 * means we're still writing metadata and haven't hit the first
		 * frame yet)
		 */
		if( null != this.seek_table && this.audio_offset > 0 && this.seek_table.num_points > 0 ) {
			final int block_size = FLAC__stream_encoder_get_blocksize();
			final long frame_first_sample = this.samples_written;
			final long frame_last_sample = frame_first_sample + (long)block_size - 1L;
			final JFLAC__StreamMetadata_SeekPoint[] points = this.seek_table.points;// java
			for( int i = this.first_seekpoint_to_check, ie = this.seek_table.num_points; i < ie; i++ ) {
				final JFLAC__StreamMetadata_SeekPoint p = points[i];// java
				final long test_sample = p.sample_number;
				if( test_sample > frame_last_sample ) {
					break;
				}
				else if( test_sample >= frame_first_sample ) {
					p.sample_number = frame_first_sample;
					p.stream_offset = output_position - this.audio_offset;
					p.frame_samples = block_size;
					this.first_seekpoint_to_check++;
					/* DO NOT: "break;" and here's why:
					 * The seektable template may contain more than one target
					 * sample for any given frame; we will keep looping, generating
					 * duplicate seekpoints for them, and we'll clean it up later,
					 * just before writing the seektable back to the metadata.
					 */
				}
				else {
					this.first_seekpoint_to_check++;
				}
			}
		}

if( Jformat.FLAC__HAS_OGG &&
			this.is_ogg ) {
			status = this.ogg_encoder_aspect.FLAC__ogg_encoder_aspect_write_callback_wrapper(
				buffer,
				bytes,
				samples,
				this.current_frame_number,
				is_last_block,
				(JFLAC__OggEncoderAspectWriteCallbackProxy)this.write_callback,
				this//, this.client_data
			);
		} else {
			status = this.write_callback.enc_write_callback( this, buffer, 0, bytes, samples, this.current_frame_number/*, this.client_data*/ );
}

		if( status == FLAC__STREAM_ENCODER_WRITE_STATUS_OK ) {
			this.bytes_written += bytes;
			this.samples_written += samples;
			/* we keep a high watermark on the number of frames written because
			 * when the encoder goes back to write metadata, 'current_frame'
			 * will drop back to 0.
			 */
			int max = this.current_frame_number + 1;
			if( max < this.frames_written ) {
				max = this.frames_written;
			}
			this.frames_written = max;
		} else {
			this.state = FLAC__STREAM_ENCODER_CLIENT_ERROR;
		}

		return status;
	}

	/** Gets called when the encoding process has finished so that we can update the STREAMINFO and SEEKTABLE blocks.  */
	private final void update_metadata_()
	{
		@SuppressWarnings("unused")
		final byte b[] = new byte[6 >= Jformat.FLAC__STREAM_METADATA_SEEKPOINT_LENGTH ? 6 : Jformat.FLAC__STREAM_METADATA_SEEKPOINT_LENGTH];
		final JFLAC__StreamMetadata_StreamInfo stream_info = this.streaminfo;
		final long samples = stream_info.total_samples;
		final int min_framesize = stream_info.min_framesize;
		final int max_framesize = stream_info.max_framesize;
		final int bps = stream_info.bits_per_sample;
		int /* FLAC__StreamEncoderSeekStatus */ seek_status;

		//FLAC__ASSERT(metadata.type == FLAC__METADATA_TYPE_STREAMINFO);

		/* All this is based on intimate knowledge of the stream header
		 * layout, but a change to the header format that would break this
		 * would also break all streams encoded in the previous format.
		 */

		/*
		 * Write MD5 signature
		 */
		{
			final int md5_offset =
					Jformat.FLAC__STREAM_METADATA_HEADER_LENGTH +
				(
					Jformat.FLAC__STREAM_METADATA_STREAMINFO_MIN_BLOCK_SIZE_LEN +
					Jformat.FLAC__STREAM_METADATA_STREAMINFO_MAX_BLOCK_SIZE_LEN +
					Jformat.FLAC__STREAM_METADATA_STREAMINFO_MIN_FRAME_SIZE_LEN +
					Jformat.FLAC__STREAM_METADATA_STREAMINFO_MAX_FRAME_SIZE_LEN +
					Jformat.FLAC__STREAM_METADATA_STREAMINFO_SAMPLE_RATE_LEN +
					Jformat.FLAC__STREAM_METADATA_STREAMINFO_CHANNELS_LEN +
					Jformat.FLAC__STREAM_METADATA_STREAMINFO_BITS_PER_SAMPLE_LEN +
					Jformat.FLAC__STREAM_METADATA_STREAMINFO_TOTAL_SAMPLES_LEN
				) / 8;

			if( (seek_status = this.seek_callback.enc_seek_callback( this, this.streaminfo_offset + md5_offset/*, this.client_data*/ )) != FLAC__STREAM_ENCODER_SEEK_STATUS_OK ) {
				if( seek_status == FLAC__STREAM_ENCODER_SEEK_STATUS_ERROR ) {
					this.state = FLAC__STREAM_ENCODER_CLIENT_ERROR;
				}
				return;
			}
			if( this.write_callback.enc_write_callback( this, stream_info.md5sum, 0, 16, 0, 0/*, this.client_data*/ ) != FLAC__STREAM_ENCODER_WRITE_STATUS_OK ) {
				this.state = FLAC__STREAM_ENCODER_CLIENT_ERROR;
				return;
			}
		}

		/*
		 * Write total samples
		 */
		{
			final int total_samples_byte_offset =
					Jformat.FLAC__STREAM_METADATA_HEADER_LENGTH +
				(
					Jformat.FLAC__STREAM_METADATA_STREAMINFO_MIN_BLOCK_SIZE_LEN +
					Jformat.FLAC__STREAM_METADATA_STREAMINFO_MAX_BLOCK_SIZE_LEN +
					Jformat.FLAC__STREAM_METADATA_STREAMINFO_MIN_FRAME_SIZE_LEN +
					Jformat.FLAC__STREAM_METADATA_STREAMINFO_MAX_FRAME_SIZE_LEN +
					Jformat.FLAC__STREAM_METADATA_STREAMINFO_SAMPLE_RATE_LEN +
					Jformat.FLAC__STREAM_METADATA_STREAMINFO_CHANNELS_LEN +
					Jformat.FLAC__STREAM_METADATA_STREAMINFO_BITS_PER_SAMPLE_LEN
					- 4
				) / 8;

			b[0] = (byte)(((bps - 1) << 4) | ((int)(samples >>> 32)/* & 0xFF*/));
			b[1] = (byte)((samples >>> 24)/* & 0xFF*/);
			b[2] = (byte)((samples >>> 16)/* & 0xFF*/);
			b[3] = (byte)((samples >>> 8)/* & 0xFF*/);
			b[4] = (byte)(samples/* & 0xFF*/);
			if( (seek_status = this.seek_callback.enc_seek_callback( this, this.streaminfo_offset + total_samples_byte_offset/*, this.client_data*/ )) != FLAC__STREAM_ENCODER_SEEK_STATUS_OK ) {
				if( seek_status == FLAC__STREAM_ENCODER_SEEK_STATUS_ERROR ) {
					this.state = FLAC__STREAM_ENCODER_CLIENT_ERROR;
				}
				return;
			}
			if( this.write_callback.enc_write_callback( this, b, 0, 5, 0, 0/*, this.client_data*/ ) != FLAC__STREAM_ENCODER_WRITE_STATUS_OK ) {
				this.state = FLAC__STREAM_ENCODER_CLIENT_ERROR;
				return;
			}
		}

		/*
		 * Write min/max framesize
		 */
		{
			final int min_framesize_offset =
					Jformat.FLAC__STREAM_METADATA_HEADER_LENGTH +
				(
					Jformat.FLAC__STREAM_METADATA_STREAMINFO_MIN_BLOCK_SIZE_LEN +
					Jformat.FLAC__STREAM_METADATA_STREAMINFO_MAX_BLOCK_SIZE_LEN
				) / 8;

			b[0] = (byte)((min_framesize >>> 16)/* & 0xFF*/);
			b[1] = (byte)((min_framesize >>> 8)/* & 0xFF*/);
			b[2] = (byte)(min_framesize/* & 0xFF*/);
			b[3] = (byte)((max_framesize >>> 16)/* & 0xFF*/);
			b[4] = (byte)((max_framesize >>> 8)/* & 0xFF*/);
			b[5] = (byte)(max_framesize/* & 0xFF*/);
			if( (seek_status = this.seek_callback.enc_seek_callback( this, this.streaminfo_offset + min_framesize_offset/*, this.client_data*/ )) != FLAC__STREAM_ENCODER_SEEK_STATUS_OK ) {
				if( seek_status == FLAC__STREAM_ENCODER_SEEK_STATUS_ERROR ) {
					this.state = FLAC__STREAM_ENCODER_CLIENT_ERROR;
				}
				return;
			}
			if( this.write_callback.enc_write_callback( this, b, 0, 6, 0, 0/*, this.client_data*/ ) != FLAC__STREAM_ENCODER_WRITE_STATUS_OK ) {
				this.state = FLAC__STREAM_ENCODER_CLIENT_ERROR;
				return;
			}
		}

		/*
		 * Write seektable
		 */
		if( null != this.seek_table && this.seek_table.num_points > 0 && this.seektable_offset > 0 ) {
			this.seek_table.FLAC__format_seektable_sort();

			//FLAC__ASSERT(FLAC__format_seektable_is_legal(encoder->private_->seek_table));

			if( (seek_status = this.seek_callback.enc_seek_callback( this, this.seektable_offset + Jformat.FLAC__STREAM_METADATA_HEADER_LENGTH/*, this.client_data*/ )) != FLAC__STREAM_ENCODER_SEEK_STATUS_OK ) {
				if( seek_status == FLAC__STREAM_ENCODER_SEEK_STATUS_ERROR ) {
					this.state = FLAC__STREAM_ENCODER_CLIENT_ERROR;
				}
				return;
			}

			final JFLAC__StreamEncoderWriteCallback write = this.write_callback;// java
			final JFLAC__StreamMetadata_SeekPoint[] points = this.seek_table.points;// java
			for( int i = 0, ie = this.seek_table.num_points; i < ie; i++ ) {
				final JFLAC__StreamMetadata_SeekPoint p = points[i];// java
				long xx = p.sample_number;
				b[7] = (byte)xx; xx >>>= 8;
				b[6] = (byte)xx; xx >>>= 8;
				b[5] = (byte)xx; xx >>>= 8;
				b[4] = (byte)xx; xx >>>= 8;
				b[3] = (byte)xx; xx >>>= 8;
				b[2] = (byte)xx; xx >>>= 8;
				b[1] = (byte)xx; xx >>>= 8;
				b[0] = (byte)xx; xx >>>= 8;
				xx = p.stream_offset;
				b[15] = (byte)xx; xx >>>= 8;
				b[14] = (byte)xx; xx >>>= 8;
				b[13] = (byte)xx; xx >>>= 8;
				b[12] = (byte)xx; xx >>>= 8;
				b[11] = (byte)xx; xx >>>= 8;
				b[10] = (byte)xx; xx >>>= 8;
				b[9] = (byte)xx; xx >>>= 8;
				b[8] = (byte)xx; xx >>>= 8;
				int x = p.frame_samples;
				b[17] = (byte)x; x >>>= 8;
				b[16] = (byte)x; x >>>= 8;
				if( write.enc_write_callback( this, b, 0, 18, 0, 0/*, this.client_data*/ ) != FLAC__STREAM_ENCODER_WRITE_STATUS_OK ) {
					this.state = FLAC__STREAM_ENCODER_CLIENT_ERROR;
					return;
				}
			}
		}
	}

	/** Gets called when the encoding process has finished so that we can update the STREAMINFO and SEEKTABLE blocks.  */
	private final void update_ogg_metadata_()
	{
if( Jformat.FLAC__HAS_OGG ) {
		/* the # of bytes in the 1st packet that precede the STREAMINFO */
		final int FIRST_OGG_PACKET_STREAMINFO_PREFIX_LENGTH =
			Jogg_mapping.FLAC__OGG_MAPPING_PACKET_TYPE_LENGTH +
			Jogg_mapping.FLAC__OGG_MAPPING_MAGIC_LENGTH +
			Jogg_mapping.FLAC__OGG_MAPPING_VERSION_MAJOR_LENGTH +
			Jogg_mapping.FLAC__OGG_MAPPING_VERSION_MINOR_LENGTH +
			Jogg_mapping.FLAC__OGG_MAPPING_NUM_HEADERS_LENGTH +
			Jformat.FLAC__STREAM_SYNC_LENGTH
		;


		//FLAC__ASSERT(metadata.type == FLAC__METADATA_TYPE_STREAMINFO);
		//FLAC__ASSERT(0 != this.seek_callback);

		/* Pre-check that client supports seeking, since we don't want the
		 * ogg_helper code to ever have to deal with this condition.
		 */
		if( this.seek_callback.enc_seek_callback( this, 0/*, this.client_data*/ ) == FLAC__STREAM_ENCODER_SEEK_STATUS_UNSUPPORTED ) {
			return;
		}

		/* All this is based on intimate knowledge of the stream header
		 * layout, but a change to the header format that would break this
		 * would also break all streams encoded in the previous format.
		 */

		/**
		 ** Write STREAMINFO stats
		 **/
		final Jogg_page page = new Jogg_page();
		JFLAC__OggEncoderAspect.simple_ogg_page__init( page );
		if( ! JFLAC__OggEncoderAspect.simple_ogg_page__get_at( this, this.streaminfo_offset, page,
				this.seek_callback, this.read_callback/*, this.client_data */ ) ) {
			JFLAC__OggEncoderAspect.simple_ogg_page__clear( page );
			return; /* state already set */
		}

		final JFLAC__StreamMetadata_StreamInfo stream_info = this.streaminfo;
		final long samples = stream_info.total_samples;
		final int min_framesize = stream_info.min_framesize;
		final int max_framesize = stream_info.max_framesize;
		/*
		 * Write MD5 signature
		 */
		{

			final int md5_offset =
				FIRST_OGG_PACKET_STREAMINFO_PREFIX_LENGTH +
				Jformat.FLAC__STREAM_METADATA_HEADER_LENGTH +
				(
					Jformat.FLAC__STREAM_METADATA_STREAMINFO_MIN_BLOCK_SIZE_LEN +
					Jformat.FLAC__STREAM_METADATA_STREAMINFO_MAX_BLOCK_SIZE_LEN +
					Jformat.FLAC__STREAM_METADATA_STREAMINFO_MIN_FRAME_SIZE_LEN +
					Jformat.FLAC__STREAM_METADATA_STREAMINFO_MAX_FRAME_SIZE_LEN +
					Jformat.FLAC__STREAM_METADATA_STREAMINFO_SAMPLE_RATE_LEN +
					Jformat.FLAC__STREAM_METADATA_STREAMINFO_CHANNELS_LEN +
					Jformat.FLAC__STREAM_METADATA_STREAMINFO_BITS_PER_SAMPLE_LEN +
					Jformat.FLAC__STREAM_METADATA_STREAMINFO_TOTAL_SAMPLES_LEN
				) / 8;

			if( md5_offset + 16 > page.body_len) {
				this.state = FLAC__STREAM_ENCODER_OGG_ERROR;
				JFLAC__OggEncoderAspect.simple_ogg_page__clear( page );
				return;
			}
			System.arraycopy( stream_info.md5sum, 0, page.body_base, page.body + md5_offset, 16 );
		}

		@SuppressWarnings("unused")
		final byte b[] = new byte[6 >= Jformat.FLAC__STREAM_METADATA_SEEKPOINT_LENGTH ? 6 : Jformat.FLAC__STREAM_METADATA_SEEKPOINT_LENGTH];
		/*
		 * Write total samples
		 */
		{
			final int total_samples_byte_offset =
				FIRST_OGG_PACKET_STREAMINFO_PREFIX_LENGTH +
				Jformat.FLAC__STREAM_METADATA_HEADER_LENGTH +
				(
					Jformat.FLAC__STREAM_METADATA_STREAMINFO_MIN_BLOCK_SIZE_LEN +
					Jformat.FLAC__STREAM_METADATA_STREAMINFO_MAX_BLOCK_SIZE_LEN +
					Jformat.FLAC__STREAM_METADATA_STREAMINFO_MIN_FRAME_SIZE_LEN +
					Jformat.FLAC__STREAM_METADATA_STREAMINFO_MAX_FRAME_SIZE_LEN +
					Jformat.FLAC__STREAM_METADATA_STREAMINFO_SAMPLE_RATE_LEN +
					Jformat.FLAC__STREAM_METADATA_STREAMINFO_CHANNELS_LEN +
					Jformat.FLAC__STREAM_METADATA_STREAMINFO_BITS_PER_SAMPLE_LEN
					- 4
				) / 8;

			if( total_samples_byte_offset + 5 > page.body_len) {
				this.state = FLAC__STREAM_ENCODER_OGG_ERROR;
				JFLAC__OggEncoderAspect.simple_ogg_page__clear( page );
				return;
			}
			final int offset = page.body + total_samples_byte_offset;
			b[0] = (byte)(page.body_base[offset] & 0xF0);
			b[0] |= (byte)((samples >>> 32) & 0x0F);
			b[1] = (byte)((samples >>> 24)/* & 0xFF*/);
			b[2] = (byte)((samples >>> 16)/* & 0xFF*/);
			b[3] = (byte)((samples >>> 8)/* & 0xFF*/);
			b[4] = (byte)(samples/* & 0xFF*/);
			System.arraycopy( b, 0, page.body_base, offset, 5 );
		}

		/*
		 * Write min/max framesize
		 */
		{
			final int min_framesize_offset =
				FIRST_OGG_PACKET_STREAMINFO_PREFIX_LENGTH +
				Jformat.FLAC__STREAM_METADATA_HEADER_LENGTH +
				(
					Jformat.FLAC__STREAM_METADATA_STREAMINFO_MIN_BLOCK_SIZE_LEN +
					Jformat.FLAC__STREAM_METADATA_STREAMINFO_MAX_BLOCK_SIZE_LEN
				) / 8;

			if( min_framesize_offset + 6 > page.body_len ) {
				this.state = FLAC__STREAM_ENCODER_OGG_ERROR;
				JFLAC__OggEncoderAspect.simple_ogg_page__clear( page );
				return;
			}
			b[0] = (byte)((min_framesize >>> 16)/* & 0xFF*/);
			b[1] = (byte)((min_framesize >>> 8)/* & 0xFF*/);
			b[2] = (byte)(min_framesize/* & 0xFF*/);
			b[3] = (byte)((max_framesize >>> 16)/* & 0xFF*/);
			b[4] = (byte)((max_framesize >>> 8)/* & 0xFF*/);
			b[5] = (byte)(max_framesize/* & 0xFF*/);
			System.arraycopy( b, 0, page.body_base, page.body + min_framesize_offset, 6 );
		}
		if( ! JFLAC__OggEncoderAspect.simple_ogg_page__set_at( this, this.streaminfo_offset, page,
				this.seek_callback, this.write_callback/*, this.client_data*/ ) ) {
			JFLAC__OggEncoderAspect.simple_ogg_page__clear( page );
			return; /* state already set */
		}
		JFLAC__OggEncoderAspect.simple_ogg_page__clear( page );

		/*
		 * Write seektable
		 */
		final int num_points = this.seek_table.num_points;// java
		if( null != this.seek_table && num_points > 0 && this.seektable_offset > 0 ) {

			this.seek_table.FLAC__format_seektable_sort();

			//FLAC__ASSERT(FLAC__format_seektable_is_legal(encoder->private_->seek_table));

			JFLAC__OggEncoderAspect.simple_ogg_page__init( page );
			if( ! JFLAC__OggEncoderAspect.simple_ogg_page__get_at( this, this.seektable_offset, page,
					this.seek_callback, this.read_callback/*, this.client_data*/ ) ) {
				JFLAC__OggEncoderAspect.simple_ogg_page__clear( page );
				return; /* state already set */
			}

			if( (Jformat.FLAC__STREAM_METADATA_HEADER_LENGTH + 18 * num_points) != page.body_len ) {
				this.state = FLAC__STREAM_ENCODER_OGG_ERROR;
				JFLAC__OggEncoderAspect.simple_ogg_page__clear( page );
				return;
			}

			final JFLAC__StreamMetadata_SeekPoint[] points = this.seek_table.points;// java
			for( int i = 0, off = page.body + Jformat.FLAC__STREAM_METADATA_HEADER_LENGTH; i < num_points; i++, off += 18 ) {
				final JFLAC__StreamMetadata_SeekPoint p = points[i];// java
				long xx = p.sample_number;
				b[7] = (byte)xx; xx >>>= 8;
				b[6] = (byte)xx; xx >>>= 8;
				b[5] = (byte)xx; xx >>>= 8;
				b[4] = (byte)xx; xx >>>= 8;
				b[3] = (byte)xx; xx >>>= 8;
				b[2] = (byte)xx; xx >>>= 8;
				b[1] = (byte)xx; xx >>>= 8;
				b[0] = (byte)xx; xx >>>= 8;
				xx = p.stream_offset;
				b[15] = (byte)xx; xx >>>= 8;
				b[14] = (byte)xx; xx >>>= 8;
				b[13] = (byte)xx; xx >>>= 8;
				b[12] = (byte)xx; xx >>>= 8;
				b[11] = (byte)xx; xx >>>= 8;
				b[10] = (byte)xx; xx >>>= 8;
				b[9] = (byte)xx; xx >>>= 8;
				b[8] = (byte)xx; xx >>>= 8;
				int x = p.frame_samples;
				b[17] = (byte)x; x >>>= 8;
				b[16] = (byte)x; x >>>= 8;
				System.arraycopy( b, 0, page.body_base, off, 18 );
			}

			if( ! JFLAC__OggEncoderAspect.simple_ogg_page__set_at( this, this.seektable_offset, page,
					this.seek_callback, this.write_callback/*, this.client_data*/ ) ) {
				JFLAC__OggEncoderAspect.simple_ogg_page__clear( page );
				return; /* state already set */
			}
			JFLAC__OggEncoderAspect.simple_ogg_page__clear( page );
		}
}
	}

	private final boolean process_frame_(final boolean is_fractional_block, final boolean is_last_block)
	{
		//FLAC__ASSERT(encoder->protected_->state == FLAC__STREAM_ENCODER_OK);

		/*
		 * Accumulate raw signal to the MD5 signature
		 */
		if( this.do_md5 && ! this.md5context.FLAC__MD5Accumulate( this.integer_signal, this.channels, this.blocksize, ((this.bits_per_sample + 7) >>> 3))) {
			this.state = FLAC__STREAM_ENCODER_MEMORY_ALLOCATION_ERROR;
			return false;
		}

		/*
		 * Process the frame header and subframes into the frame bitbuffer
		 */
		if( ! process_subframes_( is_fractional_block ) ) {
			/* the above function sets the state for us in case of an error */
			return false;
		}

		/*
		 * Zero-pad the frame to a byte_boundary
		 */
		if( ! this.frame.FLAC__bitwriter_zero_pad_to_byte_boundary() ) {
			this.state = FLAC__STREAM_ENCODER_MEMORY_ALLOCATION_ERROR;
			return false;
		}

		/*
		 * CRC-16 the whole thing
		 */
		//FLAC__ASSERT(FLAC__bitwriter_is_byte_aligned(encoder->private_->frame));
		try {
			final int crc = this.frame.FLAC__bitwriter_get_write_crc16();
			if(	! this.frame.FLAC__bitwriter_write_raw_uint32( crc, Jformat.FLAC__FRAME_FOOTER_CRC_LEN )) {
				this.state = FLAC__STREAM_ENCODER_MEMORY_ALLOCATION_ERROR;
				return false;
			}
		} catch(final OutOfMemoryError e) {
			this.state = FLAC__STREAM_ENCODER_MEMORY_ALLOCATION_ERROR;
			return false;
		}

		/*
		 * Write it
		 */
		if( ! write_bitbuffer_( this.blocksize, is_last_block ) ) {
			/* the above function sets the state for us in case of an error */
			return false;
		}

		/*
		 * Get ready for the next frame
		 */
		this.current_sample_number = 0;
		this.current_frame_number++;
		this.streaminfo.total_samples += (long)this.blocksize;

		return true;
	}

	/*
	 * These routines are private to libFLAC
	 */
	/*unsigned FLAC__format_get_max_rice_partition_order(unsigned blocksize, unsigned predictor_order)
	{
		return
			FLAC__format_get_max_rice_partition_order_from_blocksize_limited_max_and_predictor_order(
				FLAC__format_get_max_rice_partition_order_from_blocksize(blocksize),
				blocksize,
				predictor_order
			);
	}*/

	private static int FLAC__format_get_max_rice_partition_order_from_blocksize(int blocksize)
	{
		int max_rice_partition_order = 0;
		while( 0 == (blocksize & 1) ) {
			max_rice_partition_order++;
			blocksize >>>= 1;
		}
		return Jformat.FLAC__MAX_RICE_PARTITION_ORDER <= max_rice_partition_order ? Jformat.FLAC__MAX_RICE_PARTITION_ORDER : max_rice_partition_order;
	}

	private final boolean process_subframes_(final boolean is_fractional_block)
	{
		final JFLAC__FrameHeader frame_header = new JFLAC__FrameHeader();
		int min_partition_order = this.min_residual_partition_order, max_partition_order;
		boolean do_independent, do_mid_side;

		/*
		 * Calculate the min,max Rice partition orders
		 */
		if( is_fractional_block ) {
			max_partition_order = 0;
		}
		else {
			max_partition_order = FLAC__format_get_max_rice_partition_order_from_blocksize( this.blocksize );
			if( max_partition_order > this.max_residual_partition_order ) {
				max_partition_order = this.max_residual_partition_order;
			}
		}
		if( min_partition_order > max_partition_order ) {
			min_partition_order = max_partition_order;
		}

		/*
		 * Setup the frame
		 */
		frame_header.blocksize = this.blocksize;
		frame_header.sample_rate = this.sample_rate;
		frame_header.channels = this.channels;
		frame_header.channel_assignment = Jformat.FLAC__CHANNEL_ASSIGNMENT_INDEPENDENT; /* the default unless the encoder determines otherwise */
		frame_header.bits_per_sample = this.bits_per_sample;
		frame_header.number_type = Jformat.FLAC__FRAME_NUMBER_TYPE_FRAME_NUMBER;
		frame_header/*.number*/.frame_number = this.current_frame_number;

		/*
		 * Figure out what channel assignments to try
		 */
		if( this.do_mid_side_stereo ) {
			if( this.loose_mid_side_stereo ) {
				if( this.loose_mid_side_stereo_frame_count == 0 ) {
					do_independent = true;
					do_mid_side = true;
				}
				else {
					do_independent = (this.last_channel_assignment == Jformat.FLAC__CHANNEL_ASSIGNMENT_INDEPENDENT);
					do_mid_side = ! do_independent;
				}
			}
			else {
				do_independent = true;
				do_mid_side = true;
			}
		}
		else {
			do_independent = true;
			do_mid_side = false;
		}

		//FLAC__ASSERT(do_independent || do_mid_side);
		final JFLAC__Subframe[][] workspace = this.subframe_workspace;// java
		final int[] bps = this.subframe_bps;// java
		/*
		 * Check for wasted bits; set effective bps for each subframe
		 */
		if( do_independent ) {
			for( int channel = 0, nchannels = this.channels; channel < nchannels; channel++ ) {
				int w = get_wasted_bits_(this.integer_signal[channel], this.blocksize);
				if( w > this.bits_per_sample ) {
					w = this.bits_per_sample;
				}
				workspace[channel][0].wasted_bits = workspace[channel][1].wasted_bits = w;
				bps[channel] = this.bits_per_sample - w;
			}
		}
		if( do_mid_side ) {
			//FLAC__ASSERT(encoder->protected_->channels == 2);
			for( int channel = 0; channel < 2; channel++ ) {
				int w = get_wasted_bits_(this.integer_signal_mid_side[channel], this.blocksize);
				if( w > this.bits_per_sample ) {
					w = this.bits_per_sample;
				}
				this.subframe_workspace_mid_side[channel][0].wasted_bits = this.subframe_workspace_mid_side[channel][1].wasted_bits = w;
				this.subframe_bps_mid_side[channel] = this.bits_per_sample - w + (channel == 0? 0 : 1);
			}
		}

		/*
		 * First do a normal encoding pass of each independent channel
		 */
		final int[] subframe = this.best_subframe;// java
		if( do_independent ) {
			for( int channel = 0, nchannels = this.channels; channel < nchannels; channel++ ) {
				if( !
					process_subframe_(
						min_partition_order,
						max_partition_order,
						frame_header,
						bps[channel],
						this.integer_signal[channel],
						this.subframe_workspace[channel],// FIXME this.subframe_workspace_ptr[channel],
						this.partitioned_rice_contents_workspace[channel],// FIXME this.partitioned_rice_contents_workspace_ptr[channel],
						this.residual_workspace[channel],
						subframe/* + channel*/,
						this.best_subframe_bits/* + channel*/,
						channel// java: added
					)
				) {
					return false;
				}
			}
		}

		/*
		 * Now do mid and side channels if requested
		 */
		if( do_mid_side ) {
			//FLAC__ASSERT(encoder->protected_->channels == 2);

			for( int channel = 0; channel < 2; channel++ ) {
				if( !
					process_subframe_(
						min_partition_order,
						max_partition_order,
						frame_header,
						this.subframe_bps_mid_side[channel],
						this.integer_signal_mid_side[channel],
						this.subframe_workspace_mid_side[channel],// FIXME this.subframe_workspace_ptr_mid_side[channel],
						this.partitioned_rice_contents_workspace_mid_side[channel],// FIXME this.partitioned_rice_contents_workspace_ptr_mid_side[channel],
						this.residual_workspace_mid_side[channel],
						this.best_subframe_mid_side/* + channel*/,
						this.best_subframe_bits_mid_side/* + channel*/,
						channel// java: added
					)
				) {
					return false;
				}
			}
		}

		/*
		 * Compose the frame bitbuffer
		 */
		if( do_mid_side ) {
			int left_bps = 0, right_bps = 0; /* initialized only to prevent superfluous compiler warning */
			JFLAC__Subframe left_subframe = null, right_subframe = null; /* initialized only to prevent superfluous compiler warning */
			int /* FLAC__ChannelAssignment */ channel_assignment;

			//FLAC__ASSERT(encoder->protected_->channels == 2);

			if( this.loose_mid_side_stereo && this.loose_mid_side_stereo_frame_count > 0 ) {
				channel_assignment = (this.last_channel_assignment == Jformat.FLAC__CHANNEL_ASSIGNMENT_INDEPENDENT ?
						Jformat.FLAC__CHANNEL_ASSIGNMENT_INDEPENDENT : Jformat.FLAC__CHANNEL_ASSIGNMENT_MID_SIDE);
			}
			else {
				final int bits[] = new int[4]; /* WATCHOUT - indexed by FLAC__ChannelAssignment */

				//FLAC__ASSERT(FLAC__CHANNEL_ASSIGNMENT_INDEPENDENT == 0);
				//FLAC__ASSERT(FLAC__CHANNEL_ASSIGNMENT_LEFT_SIDE   == 1);
				//FLAC__ASSERT(FLAC__CHANNEL_ASSIGNMENT_RIGHT_SIDE  == 2);
				//FLAC__ASSERT(FLAC__CHANNEL_ASSIGNMENT_MID_SIDE    == 3);
				//FLAC__ASSERT(do_independent && do_mid_side);

				/* We have to figure out which channel assignent results in the smallest frame */
				bits[Jformat.FLAC__CHANNEL_ASSIGNMENT_INDEPENDENT] = this.best_subframe_bits         [0] + this.best_subframe_bits         [1];
				bits[Jformat.FLAC__CHANNEL_ASSIGNMENT_LEFT_SIDE  ] = this.best_subframe_bits         [0] + this.best_subframe_bits_mid_side[1];
				bits[Jformat.FLAC__CHANNEL_ASSIGNMENT_RIGHT_SIDE ] = this.best_subframe_bits         [1] + this.best_subframe_bits_mid_side[1];
				bits[Jformat.FLAC__CHANNEL_ASSIGNMENT_MID_SIDE   ] = this.best_subframe_bits_mid_side[0] + this.best_subframe_bits_mid_side[1];

				channel_assignment = Jformat.FLAC__CHANNEL_ASSIGNMENT_INDEPENDENT;
				int min_bits = bits[channel_assignment];
				for( int ca = 1; ca <= 3; ca++ ) {
					if( bits[ca] < min_bits ) {
						min_bits = bits[ca];
						channel_assignment = /*(FLAC__ChannelAssignment)*/ca;
					}
				}
			}

			frame_header.channel_assignment = channel_assignment;

			if( ! frame_header.FLAC__frame_add_header( this.frame ) ) {
				this.state = FLAC__STREAM_ENCODER_FRAMING_ERROR;
				return false;
			}

			switch( channel_assignment ) {
				case Jformat.FLAC__CHANNEL_ASSIGNMENT_INDEPENDENT:
					left_subframe  = this.subframe_workspace         [0][subframe                   [0]];
					right_subframe = this.subframe_workspace         [1][subframe                   [1]];
					break;
				case Jformat.FLAC__CHANNEL_ASSIGNMENT_LEFT_SIDE:
					left_subframe  = this.subframe_workspace         [0][subframe                   [0]];
					right_subframe = this.subframe_workspace_mid_side[1][this.best_subframe_mid_side[1]];
					break;
				case Jformat.FLAC__CHANNEL_ASSIGNMENT_RIGHT_SIDE:
					left_subframe  = this.subframe_workspace_mid_side[1][this.best_subframe_mid_side[1]];
					right_subframe = this.subframe_workspace         [1][subframe         [1]];
					break;
				case Jformat.FLAC__CHANNEL_ASSIGNMENT_MID_SIDE:
					left_subframe  = this.subframe_workspace_mid_side[0][this.best_subframe_mid_side[0]];
					right_subframe = this.subframe_workspace_mid_side[1][this.best_subframe_mid_side[1]];
					break;
				default:
					//FLAC__ASSERT(0);
			}

			switch( channel_assignment ) {
				case Jformat.FLAC__CHANNEL_ASSIGNMENT_INDEPENDENT:
					left_bps  = bps                       [0];
					right_bps = bps                       [1];
					break;
				case Jformat.FLAC__CHANNEL_ASSIGNMENT_LEFT_SIDE:
					left_bps  = bps                       [0];
					right_bps = this.subframe_bps_mid_side[1];
					break;
				case Jformat.FLAC__CHANNEL_ASSIGNMENT_RIGHT_SIDE:
					left_bps  = this.subframe_bps_mid_side[1];
					right_bps = bps                       [1];
					break;
				case Jformat.FLAC__CHANNEL_ASSIGNMENT_MID_SIDE:
					left_bps  = this.subframe_bps_mid_side[0];
					right_bps = this.subframe_bps_mid_side[1];
					break;
				default:
					//FLAC__ASSERT(0);
			}

			/* note that encoder_add_subframe_ sets the state for us in case of an error */
			if( ! add_subframe_( frame_header.blocksize, left_bps , left_subframe , this.frame ) ) {
				return false;
			}
			if( ! add_subframe_( frame_header.blocksize, right_bps, right_subframe, this.frame ) ) {
				return false;
			}
		}
		else {
			if( ! frame_header.FLAC__frame_add_header( this.frame ) ) {
				this.state = FLAC__STREAM_ENCODER_FRAMING_ERROR;
				return false;
			}

			for( int channel = 0, nchannels = this.channels; channel < nchannels; channel++ ) {
				if( ! add_subframe_( frame_header.blocksize, bps[channel], workspace[channel][ subframe[channel] ], this.frame ) ) {
					/* the above function sets the state for us in case of an error */
					return false;
				}
			}
		}

		if( this.loose_mid_side_stereo ) {
			this.loose_mid_side_stereo_frame_count++;
			if( this.loose_mid_side_stereo_frame_count >= this.loose_mid_side_stereo_frames ) {
				this.loose_mid_side_stereo_frame_count = 0;
			}
		}

		this.last_channel_assignment = frame_header.channel_assignment;

		return true;
	}

// fixed.c
	private static final double M_LN2 = Math.log( 2.0 );//0.69314718055994530942
//#ifndef FLAC__INTEGER_ONLY_LIBRARY
	// java: you shouldn't use an offset to call the function
	// data + Jformat.FLAC__MAX_FIXED_ORDER -> data
	// data_len - Jformat.FLAC__MAX_FIXED_ORDER -> data_len
	private static int FLAC__fixed_compute_best_predictor(final int data[], final int data_len, final float residual_bits_per_sample[]/*[FLAC__MAX_FIXED_ORDER+1]*/)
//#else
//		unsigned FLAC__fixed_compute_best_predictor(const FLAC__int32 data[], unsigned data_len, FLAC__fixedpoint residual_bits_per_sample[FLAC__MAX_FIXED_ORDER+1])
//#endif
	{
		int last_error_0 = data[Jformat.FLAC__MAX_FIXED_ORDER - 1];
		int last_error_1 = data[Jformat.FLAC__MAX_FIXED_ORDER - 1] - data[Jformat.FLAC__MAX_FIXED_ORDER - 2];
		int last_error_2 = last_error_1 - (data[Jformat.FLAC__MAX_FIXED_ORDER - 2] - data[Jformat.FLAC__MAX_FIXED_ORDER - 3]);
		int last_error_3 = last_error_2 - (data[Jformat.FLAC__MAX_FIXED_ORDER - 2] - (data[Jformat.FLAC__MAX_FIXED_ORDER - 3] << 1) + data[Jformat.FLAC__MAX_FIXED_ORDER - 4]);
		int total_error_0 = 0, total_error_1 = 0, total_error_2 = 0, total_error_3 = 0, total_error_4 = 0;

		for( int i = Jformat.FLAC__MAX_FIXED_ORDER; i < data_len; i++ ) {
			int error  = data[i] ; total_error_0 += error < 0 ? -error : error;                  int save = error;
			error -= last_error_0; total_error_1 += error < 0 ? -error : error; last_error_0 = save; save = error;
			error -= last_error_1; total_error_2 += error < 0 ? -error : error; last_error_1 = save; save = error;
			error -= last_error_2; total_error_3 += error < 0 ? -error : error; last_error_2 = save; save = error;
			error -= last_error_3; total_error_4 += error < 0 ? -error : error; last_error_3 = save;
		}

		int order;
		if( total_error_0 < Math.min(Math.min(Math.min(total_error_1, total_error_2), total_error_3), total_error_4) ) {
			order = 0;
		} else if( total_error_1 < Math.min(Math.min(total_error_2, total_error_3), total_error_4) ) {
			order = 1;
		} else if( total_error_2 < Math.min(total_error_3, total_error_4) ) {
			order = 2;
		} else if( total_error_3 < total_error_4 ) {
			order = 3;
		} else {
			order = 4;
		}

		/* Estimate the expected number of bits per residual signal sample. */
		/* 'total_error*' is linearly related to the variance of the residual */
		/* signal, so we use it directly to compute E(|x|) */
		//FLAC__ASSERT(data_len > 0 || total_error_0 == 0);
		//FLAC__ASSERT(data_len > 0 || total_error_1 == 0);
		//FLAC__ASSERT(data_len > 0 || total_error_2 == 0);
		//FLAC__ASSERT(data_len > 0 || total_error_3 == 0);
		//FLAC__ASSERT(data_len > 0 || total_error_4 == 0);
//#ifndef FLAC__INTEGER_ONLY_LIBRARY
		residual_bits_per_sample[0] = (float)((total_error_0 > 0) ? Math.log(M_LN2 * total_error_0 / data_len) / M_LN2 : 0.0);
		residual_bits_per_sample[1] = (float)((total_error_1 > 0) ? Math.log(M_LN2 * total_error_1 / data_len) / M_LN2 : 0.0);
		residual_bits_per_sample[2] = (float)((total_error_2 > 0) ? Math.log(M_LN2 * total_error_2 / data_len) / M_LN2 : 0.0);
		residual_bits_per_sample[3] = (float)((total_error_3 > 0) ? Math.log(M_LN2 * total_error_3 / data_len) / M_LN2 : 0.0);
		residual_bits_per_sample[4] = (float)((total_error_4 > 0) ? Math.log(M_LN2 * total_error_4 / data_len) / M_LN2 : 0.0);
/*#else
		residual_bits_per_sample[0] = (total_error_0 > 0) ? local__compute_rbps_integerized(total_error_0, data_len) : 0;
		residual_bits_per_sample[1] = (total_error_1 > 0) ? local__compute_rbps_integerized(total_error_1, data_len) : 0;
		residual_bits_per_sample[2] = (total_error_2 > 0) ? local__compute_rbps_integerized(total_error_2, data_len) : 0;
		residual_bits_per_sample[3] = (total_error_3 > 0) ? local__compute_rbps_integerized(total_error_3, data_len) : 0;
		residual_bits_per_sample[4] = (total_error_4 > 0) ? local__compute_rbps_integerized(total_error_4, data_len) : 0;
#endif*/

		return order;
	}

//#ifndef FLAC__INTEGER_ONLY_LIBRARY
	// java: you shouldn't use an offset to call the function
	// data + Jformat.FLAC__MAX_FIXED_ORDER -> data
	// data_len - Jformat.FLAC__MAX_FIXED_ORDER -> data_len
	private static int FLAC__fixed_compute_best_predictor_wide(final int data[], final int data_len, final float residual_bits_per_sample[]/*[FLAC__MAX_FIXED_ORDER+1]*/)
//#else
//		unsigned FLAC__fixed_compute_best_predictor_wide(const FLAC__int32 data[], unsigned data_len, FLAC__fixedpoint residual_bits_per_sample[FLAC__MAX_FIXED_ORDER+1])
//#endif
	{
		int last_error_0 = data[Jformat.FLAC__MAX_FIXED_ORDER-1];
		int last_error_1 = data[Jformat.FLAC__MAX_FIXED_ORDER-1] - data[Jformat.FLAC__MAX_FIXED_ORDER-2];
		int last_error_2 = last_error_1 - (data[Jformat.FLAC__MAX_FIXED_ORDER-2] - data[Jformat.FLAC__MAX_FIXED_ORDER-3]);
		int last_error_3 = last_error_2 - (data[Jformat.FLAC__MAX_FIXED_ORDER-2] - (data[Jformat.FLAC__MAX_FIXED_ORDER-3] << 1) + data[Jformat.FLAC__MAX_FIXED_ORDER-4]);
		/* total_error_* are 64-bits to avoid overflow when encoding
		 * erratic signals when the bits-per-sample and blocksize are
		 * large.
		 */
		long total_error_0 = 0, total_error_1 = 0, total_error_2 = 0, total_error_3 = 0, total_error_4 = 0;

		for( int i = Jformat.FLAC__MAX_FIXED_ORDER; i < data_len; i++ ) {
			int error  = data[i]     ; total_error_0 += error < 0 ? -error : error;              int save = error;
			error -= last_error_0; total_error_1 += error < 0 ? -error : error; last_error_0 = save; save = error;
			error -= last_error_1; total_error_2 += error < 0 ? -error : error; last_error_1 = save; save = error;
			error -= last_error_2; total_error_3 += error < 0 ? -error : error; last_error_2 = save; save = error;
			error -= last_error_3; total_error_4 += error < 0 ? -error : error; last_error_3 = save;
		}

		int order;
		if( total_error_0 < Math.min(Math.min(Math.min(total_error_1, total_error_2), total_error_3), total_error_4) ) {
			order = 0;
		} else if( total_error_1 < Math.min(Math.min(total_error_2, total_error_3), total_error_4) ) {
			order = 1;
		} else if( total_error_2 < Math.min(total_error_3, total_error_4) ) {
			order = 2;
		} else if( total_error_3 < total_error_4 ) {
			order = 3;
		} else {
			order = 4;
		}

		/* Estimate the expected number of bits per residual signal sample. */
		/* 'total_error*' is linearly related to the variance of the residual */
		/* signal, so we use it directly to compute E(|x|) */
		//FLAC__ASSERT(data_len > 0 || total_error_0 == 0);
		//FLAC__ASSERT(data_len > 0 || total_error_1 == 0);
		//FLAC__ASSERT(data_len > 0 || total_error_2 == 0);
		//FLAC__ASSERT(data_len > 0 || total_error_3 == 0);
		//FLAC__ASSERT(data_len > 0 || total_error_4 == 0);
//#ifndef FLAC__INTEGER_ONLY_LIBRARY
		residual_bits_per_sample[0] = (float)((total_error_0 > 0) ? Math.log(M_LN2 * total_error_0 / data_len) / M_LN2 : 0.0);
		residual_bits_per_sample[1] = (float)((total_error_1 > 0) ? Math.log(M_LN2 * total_error_1 / data_len) / M_LN2 : 0.0);
		residual_bits_per_sample[2] = (float)((total_error_2 > 0) ? Math.log(M_LN2 * total_error_2 / data_len) / M_LN2 : 0.0);
		residual_bits_per_sample[3] = (float)((total_error_3 > 0) ? Math.log(M_LN2 * total_error_3 / data_len) / M_LN2 : 0.0);
		residual_bits_per_sample[4] = (float)((total_error_4 > 0) ? Math.log(M_LN2 * total_error_4 / data_len) / M_LN2 : 0.0);
/*#else
		residual_bits_per_sample[0] = (total_error_0 > 0) ? local__compute_rbps_wide_integerized(total_error_0, data_len) : 0;
		residual_bits_per_sample[1] = (total_error_1 > 0) ? local__compute_rbps_wide_integerized(total_error_1, data_len) : 0;
		residual_bits_per_sample[2] = (total_error_2 > 0) ? local__compute_rbps_wide_integerized(total_error_2, data_len) : 0;
		residual_bits_per_sample[3] = (total_error_3 > 0) ? local__compute_rbps_wide_integerized(total_error_3, data_len) : 0;
		residual_bits_per_sample[4] = (total_error_4 > 0) ? local__compute_rbps_wide_integerized(total_error_4, data_len) : 0;
#endif*/

		return order;
	}
		// end fixed.c
	private final boolean process_subframe_(
		final int min_partition_order,
		final int max_partition_order,
		final JFLAC__FrameHeader frame_header,
		final int subframe_b_p_s,
		final int signal[],// java renamed integer_signal
		final JFLAC__Subframe subframe[],// [2],
		final JFLAC__EntropyCodingMethod_PartitionedRiceContents partitioned_rice_contents[],// [2],
		final int residual[][],// [2],
		final int[] bestsubframe,
		final int[] best_bits,
		final int channel// java: added as offset to best_subframe and best_bits
	)
	{
//#ifndef FLAC__INTEGER_ONLY_LIBRARY
		final float fixed_residual_bits_per_sample[] = new float[Jformat.FLAC__MAX_FIXED_ORDER + 1];
//#else
//		FLAC__fixedpoint fixed_residual_bits_per_sample[FLAC__MAX_FIXED_ORDER+1];
//#endif
//#ifndef FLAC__INTEGER_ONLY_LIBRARY
		final float autoc[] = new float[Jformat.FLAC__MAX_LPC_ORDER + 1]; /* WATCHOUT: the size is important even though encoder->protected_->max_lpc_order might be less; some asm and x86 intrinsic routines need all the space */
		final double lpc_error[] = new double[Jformat.FLAC__MAX_LPC_ORDER];
//#endif
		/* only use RICE2 partitions if stream bps > 16 */
		final int rice_parameter_limit = FLAC__stream_encoder_get_bits_per_sample() > 16 ? Jformat.FLAC__ENTROPY_CODING_METHOD_PARTITIONED_RICE2_ESCAPE_PARAMETER : Jformat.FLAC__ENTROPY_CODING_METHOD_PARTITIONED_RICE_ESCAPE_PARAMETER;

		//FLAC__ASSERT(frame_header.blocksize > 0);

		/* verbatim subframe is the baseline against which we measure other compressed subframes */
		int _best_subframe = 0;// 1 or 0
		int _best_bits;
		if( this.disable_verbatim_subframes && frame_header.blocksize >= Jformat.FLAC__MAX_FIXED_ORDER ) {
			_best_bits = Integer.MAX_VALUE;
		} else {
			_best_bits = evaluate_verbatim_subframe_( signal, frame_header.blocksize, subframe_b_p_s, subframe[_best_subframe] );
		}

		if( frame_header.blocksize >= Jformat.FLAC__MAX_FIXED_ORDER ) {
			boolean signal_is_constant = false;
			int guess_fixed_order;
			if( subframe_b_p_s + 4 + Jformat.FLAC__bitmath_ilog2( (frame_header.blocksize - Jformat.FLAC__MAX_FIXED_ORDER) | 1 ) <= 32 ) {
				guess_fixed_order = FLAC__fixed_compute_best_predictor( signal/* + Jformat.FLAC__MAX_FIXED_ORDER*/, frame_header.blocksize/* - Jformat.FLAC__MAX_FIXED_ORDER*/, fixed_residual_bits_per_sample );
			} else {
				guess_fixed_order = FLAC__fixed_compute_best_predictor_wide( signal/* + Jformat.FLAC__MAX_FIXED_ORDER*/, frame_header.blocksize/* - Jformat.FLAC__MAX_FIXED_ORDER*/, fixed_residual_bits_per_sample );
			}
			/* check for constant subframe */
			if(
				! this.disable_constant_subframes &&
//#ifndef FLAC__INTEGER_ONLY_LIBRARY
				fixed_residual_bits_per_sample[1] == 0.0f
//#else
//				fixed_residual_bits_per_sample[1] == FLAC__FP_ZERO
//#endif
			) {
				/* the above means it's possible all samples are the same value; now double-check it: */
				signal_is_constant = true;
				for( int i = 1, size = frame_header.blocksize; i < size; i++ ) {
					if( signal[0] != signal[i] ) {
						signal_is_constant = false;
						break;
					}
				}
			}
			if( signal_is_constant ) {
				final int _candidate_bits = evaluate_constant_subframe_( signal[0], frame_header.blocksize, subframe_b_p_s, subframe[ 1 ^ _best_subframe ] );
				if( _candidate_bits < _best_bits ) {
					_best_subframe ^= 1;// ! _best_subframe;
					_best_bits = _candidate_bits;
				}
			}
			else {
				if( ! this.disable_fixed_subframes || (this.max_lpc_order == 0 && _best_bits == Integer.MAX_VALUE) ) {
					/* encode fixed */
					int min_fixed_order, max_fixed_order;
					if( this.do_exhaustive_model_search ) {
						min_fixed_order = 0;
						max_fixed_order = Jformat.FLAC__MAX_FIXED_ORDER;
					}
					else {
						min_fixed_order = max_fixed_order = guess_fixed_order;
					}
					if( max_fixed_order >= frame_header.blocksize ) {
						max_fixed_order = frame_header.blocksize - 1;
					}
					for( int fixed_order = min_fixed_order; fixed_order <= max_fixed_order; fixed_order++ ) {
//#ifndef FLAC__INTEGER_ONLY_LIBRARY
						if( fixed_residual_bits_per_sample[fixed_order] >= (float)subframe_b_p_s ) {
							continue; /* don't even try */
						}
						int rice_parameter = (fixed_residual_bits_per_sample[fixed_order] > 0.0f) ? (int)(fixed_residual_bits_per_sample[fixed_order] + 0.5f) : 0; /* 0.5 is for rounding */
//#else
//						if( FLAC__fixedpoint_trunc(fixed_residual_bits_per_sample[fixed_order]) >= (int)subframe_bps )
//							continue; /* don't even try */
//						rice_parameter = (fixed_residual_bits_per_sample[fixed_order] > FLAC__FP_ZERO)? (unsigned)FLAC__fixedpoint_trunc(fixed_residual_bits_per_sample[fixed_order]+FLAC__FP_ONE_HALF) : 0; /* 0.5 is for rounding */
//#endif
						rice_parameter++; /* to account for the signed->unsigned conversion during rice coding */
						if( rice_parameter >= rice_parameter_limit ) {
//#ifdef DEBUG_VERBOSE
//							System.err.printf("clipping rice_parameter (%d -> %d) @0\n", rice_parameter, rice_parameter_limit - 1);
//#endif
							rice_parameter = rice_parameter_limit - 1;
						}
						final int _candidate_bits =
							evaluate_fixed_subframe_(
								signal,
								residual[ 1 ^ _best_subframe ],
								this.abs_residual_partition_sums,
								this.raw_bits_per_partition,
								frame_header.blocksize,
								subframe_b_p_s,
								fixed_order,
								rice_parameter,
								rice_parameter_limit,
								min_partition_order,
								max_partition_order,
								this.do_escape_coding,
								this.rice_parameter_search_dist,
								subframe[ 1 ^ _best_subframe ],
								partitioned_rice_contents[ 1 ^ _best_subframe ]
							);
						if( _candidate_bits < _best_bits) {
							_best_subframe ^= 1;// ! _best_subframe;
							_best_bits = _candidate_bits;
						}
					}
				}

//#ifndef FLAC__INTEGER_ONLY_LIBRARY
				/* encode lpc */
				if( this.max_lpc_order > 0 ) {
					int lpc_max_order;
					if( this.max_lpc_order >= frame_header.blocksize ) {
						lpc_max_order = frame_header.blocksize-1;
					} else {
						lpc_max_order = this.max_lpc_order;
					}
					if( lpc_max_order > 0 ) {
						for( int a = 0; a < this.num_apodizations; a++ ) {
							Jlpc.FLAC__lpc_window_data( signal, this.window[a], this.windowed_signal, frame_header.blocksize );
							//encoder.private_.local_lpc_compute_autocorrelation(encoder.private_.windowed_signal, frame_header.blocksize, max_lpc_order+1, autoc);
							Jlpc.FLAC__lpc_compute_autocorrelation( this.windowed_signal, frame_header.blocksize, lpc_max_order + 1, autoc );
							/* if autoc[0] == 0.0, the signal is constant and we usually won't get here, but it can happen */
							if( autoc[0] != 0.0f ) {
								lpc_max_order = Jlpc.FLAC__lpc_compute_lp_coefficients( autoc, lpc_max_order, this.lp_coeff, lpc_error );
								int lpc_min_order;
								if( this.do_exhaustive_model_search ) {
									lpc_min_order = 1;
								}
								else {
									final int guess_lpc_order =
										Jlpc.FLAC__lpc_compute_best_order(
											lpc_error,
											lpc_max_order,
											frame_header.blocksize,
											subframe_b_p_s + (
												this.do_qlp_coeff_prec_search?
													Jformat.FLAC__MIN_QLP_COEFF_PRECISION : /* have to guess; use the min possible size to avoid accidentally favoring lower orders */
													this.qlp_coeff_precision
											)
										);
									lpc_min_order = lpc_max_order = guess_lpc_order;
								}
								if( lpc_max_order >= frame_header.blocksize ) {
									lpc_max_order = frame_header.blocksize - 1;
								}
								for( int lpc_order = lpc_min_order; lpc_order <= lpc_max_order; lpc_order++ ) {
									final double lpc_residual_bits_per_sample = Jlpc.FLAC__lpc_compute_expected_bits_per_residual_sample( lpc_error[lpc_order-1], frame_header.blocksize - lpc_order );
									if( lpc_residual_bits_per_sample >= (double)subframe_b_p_s ) {
										continue; /* don't even try */
									}
									int rice_parameter = (lpc_residual_bits_per_sample > 0.0) ? (int)(lpc_residual_bits_per_sample + 0.5) : 0; /* 0.5 is for rounding */
									rice_parameter++; /* to account for the signed->unsigned conversion during rice coding */
									if( rice_parameter >= rice_parameter_limit ) {
//#ifdef DEBUG_VERBOSE
//										System.err.printf("clipping rice_parameter (%d -> %d) @1\n", rice_parameter, rice_parameter_limit - 1);
//#endif
										rice_parameter = rice_parameter_limit - 1;
									}
									int min_qlp_coeff_precision, max_qlp_coeff_precision;
									if( this.do_qlp_coeff_prec_search ) {
										min_qlp_coeff_precision = Jformat.FLAC__MIN_QLP_COEFF_PRECISION;
										/* try to keep qlp coeff precision such that only 32-bit math is required for decode of <=16bps(+1bps for side channel) streams */
										if( subframe_b_p_s <= 17 ) {
											max_qlp_coeff_precision = 32 - subframe_b_p_s - Jformat.FLAC__bitmath_ilog2( lpc_order );
											if( max_qlp_coeff_precision > Jformat.FLAC__MAX_QLP_COEFF_PRECISION ) {
												max_qlp_coeff_precision = Jformat.FLAC__MAX_QLP_COEFF_PRECISION;
											}
											if( max_qlp_coeff_precision < min_qlp_coeff_precision ) {
												max_qlp_coeff_precision = min_qlp_coeff_precision;
											}
										} else {
											max_qlp_coeff_precision = Jformat.FLAC__MAX_QLP_COEFF_PRECISION;
										}
									} else {
										min_qlp_coeff_precision = max_qlp_coeff_precision = this.qlp_coeff_precision;
									}
									for( int coeff_precision = min_qlp_coeff_precision; coeff_precision <= max_qlp_coeff_precision; coeff_precision++ ) {
										final int _candidate_bits =
											evaluate_lpc_subframe_(
												signal,
												residual[ 1 ^ _best_subframe ],
												this.abs_residual_partition_sums,
												this.raw_bits_per_partition,
												this.lp_coeff[lpc_order-1],
												frame_header.blocksize,
												subframe_b_p_s,
												lpc_order,
												coeff_precision,
												rice_parameter,
												rice_parameter_limit,
												min_partition_order,
												max_partition_order,
												this.do_escape_coding,
												this.rice_parameter_search_dist,
												subframe[ 1 ^ _best_subframe ],
												partitioned_rice_contents[ 1 ^ _best_subframe ]
											);
										if( _candidate_bits > 0 ) { /* if == 0, there was a problem quantizing the lpcoeffs */
											if( _candidate_bits < _best_bits ) {
												_best_subframe ^= 1;// ! _best_subframe;
												_best_bits = _candidate_bits;
											}
										}
									}
								}
							}
						}
					}
				}
//#endif /* !defined FLAC__INTEGER_ONLY_LIBRARY */
			}
		}

		/* under rare circumstances this can happen when all but lpc subframe types are disabled: */
		if( _best_bits == Integer.MAX_VALUE ) {
			//FLAC__ASSERT(_best_subframe == 0);
			_best_bits = evaluate_verbatim_subframe_( signal, frame_header.blocksize, subframe_b_p_s, subframe[_best_subframe] );
		}

		bestsubframe[channel] = _best_subframe;
		best_bits[channel] = _best_bits;

		return true;
	}

	private final boolean add_subframe_(
		final int block_size,
		final int subframe_b_p_s,
		final JFLAC__Subframe subframe,
		final JFLAC__BitWriter frame_writer
	)
	{
		switch( subframe.type ) {
			case Jformat.FLAC__SUBFRAME_TYPE_CONSTANT:
				if( ! ((JFLAC__Subframe_Constant)subframe.data).FLAC__subframe_add_constant( subframe_b_p_s, subframe.wasted_bits, frame_writer ) ) {
					this.state = FLAC__STREAM_ENCODER_FRAMING_ERROR;
					return false;
				}
				break;
			case Jformat.FLAC__SUBFRAME_TYPE_FIXED:
				final JFLAC__Subframe_Fixed fixed = (JFLAC__Subframe_Fixed) subframe.data;
				if( ! fixed.FLAC__subframe_add_fixed( block_size - fixed.order, subframe_b_p_s, subframe.wasted_bits, frame_writer ) ) {
					this.state = FLAC__STREAM_ENCODER_FRAMING_ERROR;
					return false;
				}
				break;
			case Jformat.FLAC__SUBFRAME_TYPE_LPC:
				final JFLAC__Subframe_LPC lpc = (JFLAC__Subframe_LPC) subframe.data;
				if( ! lpc.FLAC__subframe_add_lpc( block_size - lpc.order, subframe_b_p_s, subframe.wasted_bits, frame_writer ) ) {
					this.state = FLAC__STREAM_ENCODER_FRAMING_ERROR;
					return false;
				}
				break;
			case Jformat.FLAC__SUBFRAME_TYPE_VERBATIM:
				if( ! ((JFLAC__Subframe_Verbatim) subframe.data).FLAC__subframe_add_verbatim( block_size, subframe_b_p_s, subframe.wasted_bits, frame_writer ) ) {
					this.state = FLAC__STREAM_ENCODER_FRAMING_ERROR;
					return false;
				}
				break;
			default:
				//FLAC__ASSERT(0);
		}

		return true;
	}

/*#define SPOTCHECK_ESTIMATE 0
//#if SPOTCHECK_ESTIMATE
	@SuppressWarnings("boxing")
	private final void spotcheck_subframe_estimate_(
		JFLAC__StreamEncoder encoder,
		int blocksize,
		int subframe_bps,
		final JFLAC__Subframe subframe,
		int estimate
	)
	{
		//boolean ret;
		JFLAC__BitWriter frame = new JFLAC__BitWriter();
		//if( frame == null ) {
		//	System.err.print("EST: can't allocate frame\n");
		//	return;
		//}
		if( ! frame.FLAC__bitwriter_init() ) {
			System.err.print("EST: can't init frame\n");
			return;
		}
		ret = add_subframe_( blocksize, subframe_bps, subframe, frame );
		//FLAC__ASSERT(ret);
		{
			final int actual = frame.FLAC__bitwriter_get_input_bits_unconsumed();
			if( estimate != actual )
				System.err.printf("EST: bad, frame#%d sub#%%d type=%8s est=%d, actual=%d, delta=%d\n", this.current_frame_number, JFLAC__Subframe.FLAC__SubframeTypeString[subframe.type], estimate, actual, (int)actual-(int)estimate);
		}
		frame = null;
	}
//#endif*/

	private static int evaluate_constant_subframe_(
		final int signal,
		final int blocksize,
		final int subframe_bps,
		final JFLAC__Subframe subframe
	)
	{
		subframe.type = Jformat.FLAC__SUBFRAME_TYPE_CONSTANT;
		final JFLAC__Subframe_Constant constant = new JFLAC__Subframe_Constant();
		constant.value = signal;
		subframe.data = constant;

		final int estimate = Jformat.FLAC__SUBFRAME_ZERO_PAD_LEN + Jformat.FLAC__SUBFRAME_TYPE_LEN + Jformat.FLAC__SUBFRAME_WASTED_BITS_FLAG_LEN + subframe.wasted_bits + subframe_bps;

//#if SPOTCHECK_ESTIMATE
//		spotcheck_subframe_estimate_(encoder, blocksize, subframe_bps, subframe, estimate);
//#else
//		(void)encoder, (void)blocksize;
//#endif

		return estimate;
	}

	// fixed.c
	private static void FLAC__fixed_compute_residual(final int data[], final int data_len, int order, final int residual[])
	{//java: order used as offset to data index

		switch( order ) {
			case 0:
				//FLAC__ASSERT(sizeof(residual[0]) == sizeof(data[0]));
				System.arraycopy( data, order, residual, 0, data_len );
				break;
			case 1:
				for( int i = 0; i < data_len; i++, order++ ) {
					residual[i] = data[order] - data[order - 1];
				}
				break;
			case 2:
				for( int i = 0; i < data_len; i++, order++ ) {
					residual[i] = data[order] - (data[order - 1] << 1) + data[order - 2];
				}
				break;
			case 3:
				for( int i = 0; i < data_len; i++, order++ ) {
					residual[i] = data[order] + 3 * (data[order - 2] - data[order - 1]) - data[order - 3];
				}
				break;
			case 4:
				for( int i = 0; i < data_len; i++, order++ ) {
					residual[i] = data[order] + ((3 * data[order - 2] - ((data[order - 1] + data[order - 3]) << 1)) << 1) + data[order - 4];
				}
				break;
			default:
				//FLAC__ASSERT(0);
		}
	}
	// end fixed.c

	private final int evaluate_fixed_subframe_(
		final int signal[],
		final int residual[],
		final long abs__residual_partition_sums[],
		final int raw_bits__per_partition[],
		final int block_size,
		final int subframe_b_p_s,
		final int order,
		final int rice_parameter,
		final int rice_parameter_limit,
		final int min_partition_order,
		final int max_partition_order,
		final boolean is_do_escape_coding,
		final int rice_parameter_search_distance,
		final JFLAC__Subframe subframe,
		final JFLAC__EntropyCodingMethod_PartitionedRiceContents partitioned_rice_contents
	)
	{
		final int residual_samples = block_size - order;

		FLAC__fixed_compute_residual( signal/* + order*/, residual_samples, order, residual );// java: changes inside the function

		subframe.type = Jformat.FLAC__SUBFRAME_TYPE_FIXED;
		final JFLAC__Subframe_Fixed fixed = new JFLAC__Subframe_Fixed();
		subframe.data = fixed;

		fixed.entropy_coding_method.type = Jformat.FLAC__ENTROPY_CODING_METHOD_PARTITIONED_RICE;
		fixed.entropy_coding_method./*data.*/partitioned_rice.contents = partitioned_rice_contents;
		fixed.residual = residual;

		final int residual_bits =
			find_best_partition_order_(
				//encoder,// .private_,// java: uses direct access
				residual,
				abs__residual_partition_sums,
				raw_bits__per_partition,
				residual_samples,
				order,
				rice_parameter,
				rice_parameter_limit,
				min_partition_order,
				max_partition_order,
				subframe_b_p_s,
				is_do_escape_coding,
				rice_parameter_search_distance,
				fixed.entropy_coding_method
			);

		fixed.order = order;
		for( int i = 0; i < order; i++ ) {
			fixed.warmup[i] = signal[i];
		}

		final int estimate = Jformat.FLAC__SUBFRAME_ZERO_PAD_LEN + Jformat.FLAC__SUBFRAME_TYPE_LEN + Jformat.FLAC__SUBFRAME_WASTED_BITS_FLAG_LEN + subframe.wasted_bits + (order * subframe_b_p_s) + residual_bits;

//#if SPOTCHECK_ESTIMATE
//		spotcheck_subframe_estimate_(encoder, blocksize, subframe_bps, subframe, estimate);
//#endif

		return estimate;
	}

//#ifndef FLAC__INTEGER_ONLY_LIBRARY
	private final int evaluate_lpc_subframe_(
		//JFLAC__StreamEncoder encoder,
		final int signal[],
		final int residual[],
		final long abs__residual_partition_sums[],
		final int raw__bits_per_partition[],
		final float lp_coeffs[],
		final int block_size,
		final int subframe_b_p_s,
		final int order,
		int qlp_coeffs_precision,
		final int rice_parameter,
		final int rice_parameter_limit,
		final int min_partition_order,
		final int max_partition_order,
		final boolean is_do_escape_coding,
		final int rice_parameter_search_distance,
		final JFLAC__Subframe subframe,
		final JFLAC__EntropyCodingMethod_PartitionedRiceContents partitioned_rice_contents
	)
	{
		final int qlp_coeff[] = new int[Jformat.FLAC__MAX_LPC_ORDER]; /* WATCHOUT: the size is important; some x86 intrinsic routines need more than lpc order elements */
		final int residual_samples = block_size - order;

		/* try to keep qlp coeff precision such that only 32-bit math is required for decode of <=16bps(+1bps for side channel) streams */
		if( subframe_b_p_s <= 17 ) {
			//FLAC__ASSERT(order > 0);
			//FLAC__ASSERT(order <= FLAC__MAX_LPC_ORDER);
			final int i = 32 - subframe_b_p_s - Jformat.FLAC__bitmath_ilog2( order );
			qlp_coeffs_precision = qlp_coeffs_precision <= i ? qlp_coeffs_precision : i;
		}

		final int quantization = /*ret = */Jlpc.FLAC__lpc_quantize_coefficients( lp_coeffs, order, qlp_coeffs_precision, qlp_coeff/*, &quantization*/ );
		if( quantization < 0 ) {
			return 0; /* this is a hack to indicate to the caller that we can't do lp at this order on this subframe */
		}

		if( subframe_b_p_s + qlp_coeffs_precision + Jformat.FLAC__bitmath_ilog2( order ) <= 32 ) {
			//if( subframe_bps <= 16 && qlp_coeff_precision <= 16 )
				//encoder.private_.local_lpc_compute_residual_from_qlp_coefficients_16bit(signal+order, residual_samples, qlp_coeff, order, quantization, residual);
				//Jlpc.FLAC__lpc_compute_residual_from_qlp_coefficients( signal/* + order*/, residual_samples, qlp_coeff, order, quantization, residual );
			//else
				//encoder.private_.local_lpc_compute_residual_from_qlp_coefficients(signal+order, residual_samples, qlp_coeff, order, quantization, residual);
			Jlpc.FLAC__lpc_compute_residual_from_qlp_coefficients( signal/* + order*/, residual_samples, qlp_coeff, order, quantization, residual );
		} else {
			//encoder.private_.local_lpc_compute_residual_from_qlp_coefficients_64bit(signal+order, residual_samples, qlp_coeff, order, quantization, residual);
			Jlpc.FLAC__lpc_compute_residual_from_qlp_coefficients_wide(signal/* + order*/, residual_samples, qlp_coeff, order, quantization, residual );
		}

		subframe.type = Jformat.FLAC__SUBFRAME_TYPE_LPC;

		final JFLAC__Subframe_LPC lpc = new JFLAC__Subframe_LPC();
		subframe.data = lpc;
		lpc.entropy_coding_method.type = Jformat.FLAC__ENTROPY_CODING_METHOD_PARTITIONED_RICE;
		lpc.entropy_coding_method./*data.*/partitioned_rice.contents = partitioned_rice_contents;
		lpc.residual = residual;

		final int residual_bits =
			find_best_partition_order_(
				// encoder,// .private_,// java: uses direct access
				residual,
				abs__residual_partition_sums,
				raw__bits_per_partition,
				residual_samples,
				order,
				rice_parameter,
				rice_parameter_limit,
				min_partition_order,
				max_partition_order,
				subframe_b_p_s,
				is_do_escape_coding,
				rice_parameter_search_distance,
				lpc.entropy_coding_method
			);

		lpc.order = order;
		lpc.qlp_coeff_precision = qlp_coeffs_precision;
		lpc.quantization_level = quantization;
		System.arraycopy( qlp_coeff, 0, lpc.qlp_coeff, 0, Jformat.FLAC__MAX_LPC_ORDER );
		for( int i = 0; i < order; i++ ) {
			lpc.warmup[i] = signal[i];
		}

		final int estimate = Jformat.FLAC__SUBFRAME_ZERO_PAD_LEN + Jformat.FLAC__SUBFRAME_TYPE_LEN +
				Jformat.FLAC__SUBFRAME_WASTED_BITS_FLAG_LEN + subframe.wasted_bits +
				Jformat.FLAC__SUBFRAME_LPC_QLP_COEFF_PRECISION_LEN + Jformat.FLAC__SUBFRAME_LPC_QLP_SHIFT_LEN +
				(order * (qlp_coeffs_precision + subframe_b_p_s)) + residual_bits;

//#if SPOTCHECK_ESTIMATE
//		spotcheck_subframe_estimate_(encoder, blocksize, subframe_bps, subframe, estimate);
//#endif

		return estimate;
	}
//#endif

	private static int evaluate_verbatim_subframe_(
		final int signal[],
		final int blocksize,
		final int subframe_bps,
		final JFLAC__Subframe subframe
	)
	{
		subframe.type = Jformat.FLAC__SUBFRAME_TYPE_VERBATIM;

		final JFLAC__Subframe_Verbatim verbatim = new JFLAC__Subframe_Verbatim();
		verbatim.data = signal;
		subframe.data = verbatim;

		final int estimate = Jformat.FLAC__SUBFRAME_ZERO_PAD_LEN + Jformat.FLAC__SUBFRAME_TYPE_LEN + Jformat.FLAC__SUBFRAME_WASTED_BITS_FLAG_LEN + subframe.wasted_bits + (blocksize * subframe_bps);

//#if SPOTCHECK_ESTIMATE
//		spotcheck_subframe_estimate_( encoder, blocksize, subframe_bps, subframe, estimate );
//#else
//		(void)encoder;
//#endif

		return estimate;
	}

	private static int FLAC__format_get_max_rice_partition_order_from_blocksize_limited_max_and_predictor_order(final int limit, final int blocksize, final int predictor_order)
	{
		int max_rice_partition_order = limit;

		while( max_rice_partition_order > 0 && (blocksize >>> max_rice_partition_order) <= predictor_order ) {
			max_rice_partition_order--;
		}

		//FLAC__ASSERT(
		//	(max_rice_partition_order == 0 && blocksize >= predictor_order) ||
		//	(max_rice_partition_order > 0 && blocksize >> max_rice_partition_order > predictor_order)
		//);

		return max_rice_partition_order;
	}

	private final int find_best_partition_order_(
		//JFLAC__StreamEncoder private_,// JFLAC__StreamEncoderPrivate private_,
		final int residual[],
		final long abs__residual_partition_sums[],
		final int raw__bits_per_partition[],
		final int residual_samples,
		final int predictor_order,
		final int rice_parameter,
		final int rice_parameter_limit,
		int min_partition_order,
		int max_partition_order,
		final int bps,
		final boolean is_do_escape_coding,
		final int rice_parameter_search_distance,
		final JFLAC__EntropyCodingMethod best_ecm
	)
	{
		int residual_bits, best_residual_bits = 0;
		int best_parameters_index = 0;// 0 or 1
		int best_partition_order = 0;
		final int block_size = residual_samples + predictor_order;

		max_partition_order = FLAC__format_get_max_rice_partition_order_from_blocksize_limited_max_and_predictor_order( max_partition_order, block_size, predictor_order );
		if( min_partition_order > max_partition_order ) {
			min_partition_order = max_partition_order;
		}

		//local_precompute_partition_info_sums( residual, abs_residual_partition_sums, residual_samples, predictor_order, min_partition_order, max_partition_order, bps );
		precompute_partition_info_sums_( residual, abs__residual_partition_sums, residual_samples, predictor_order, min_partition_order, max_partition_order, bps );

		if( is_do_escape_coding ) {
			precompute_partition_info_escapes_( residual, raw__bits_per_partition, residual_samples, predictor_order, min_partition_order, max_partition_order );
		}

		{
			for( int partition_order = max_partition_order, sum = 0; partition_order >= min_partition_order; partition_order-- ) {
				if( 0 > (residual_bits = //!
					set_partitioned_rice_(
//#ifdef EXACT_RICE_BITS_CALCULATION
//						residual,
//#endif
						abs__residual_partition_sums/* + sum*/,
						raw__bits_per_partition/* + sum*/,
						sum,// java: added as offset
						residual_samples,
						predictor_order,
						rice_parameter,
						rice_parameter_limit,
						rice_parameter_search_distance,
						partition_order,
						is_do_escape_coding,
						this.partitioned_rice_contents_extra[ 1 ^ best_parameters_index ]//,
						//residual_bits
					))
				)
				{
					//FLAC__ASSERT(best_residual_bits != 0);
					break;
				}
				sum += 1 << partition_order;
				if( best_residual_bits == 0 || residual_bits < best_residual_bits ) {
					best_residual_bits = residual_bits;
					best_parameters_index ^= 1;
					best_partition_order = partition_order;
				}
			}
		}

		best_ecm./*data.*/partitioned_rice.order = best_partition_order;

		{
			/*
			 * We are allowed to de-const the pointer based on our special
			 * knowledge; it is const to the outside world.
			 */
			final JFLAC__EntropyCodingMethod_PartitionedRiceContents prc = best_ecm./*data.*/partitioned_rice.contents;
			int partition;

			/* save best parameters and raw_bits */
			prc.FLAC__format_entropy_coding_method_partitioned_rice_contents_ensure_size( 6 >= best_partition_order ? 6 : best_partition_order );
			System.arraycopy( this.partitioned_rice_contents_extra[best_parameters_index].parameters, 0, prc.parameters, 0, 1 << best_partition_order );
			if( is_do_escape_coding ) {
				System.arraycopy( this.partitioned_rice_contents_extra[best_parameters_index].raw_bits, 0, prc.raw_bits, 0, 1 << best_partition_order );
			}
			/*
			 * Now need to check if the type should be changed to
			 * FLAC__ENTROPY_CODING_METHOD_PARTITIONED_RICE2 based on the
			 * size of the rice parameters.
			 */
			for( partition = 0; partition < (1 << best_partition_order); partition++ ) {
				if( prc.parameters[partition] >= Jformat.FLAC__ENTROPY_CODING_METHOD_PARTITIONED_RICE_ESCAPE_PARAMETER ) {
					best_ecm.type = Jformat.FLAC__ENTROPY_CODING_METHOD_PARTITIONED_RICE2;
					break;
				}
			}
		}

		return best_residual_bits;
	}

	private static void precompute_partition_info_sums_(
		final int residual[],
		final long abs_residual_partition_sums[],
		final int residual_samples,
		final int predictor_order,
		final int min_partition_order,
		final int max_partition_order,
		final int bps
	)
	{
		final int default_partition_samples = (residual_samples + predictor_order) >> max_partition_order;
		int partitions = 1 << max_partition_order;

		//FLAC__ASSERT(default_partition_samples > predictor_order);

		/* first do max_partition_order */
		{
			final int threshold = 32 - Jformat.FLAC__bitmath_ilog2( default_partition_samples );
			int residual_sample, end = -predictor_order;
			/* WATCHOUT: "bps + FLAC__MAX_EXTRA_RESIDUAL_BPS" is the maximum assumed size of the average residual magnitude */
			if( bps + FLAC__MAX_EXTRA_RESIDUAL_BPS < threshold ) {
				for( int partition = residual_sample = 0; partition < partitions; partition++ ) {
					int abs_residual_partition_sum = 0;// uint32
					end += default_partition_samples;
					for( ; residual_sample < end; residual_sample++) {
						final int a = residual[residual_sample];
						abs_residual_partition_sum += (a < 0) ? -a : a; /* abs(INT_MIN) is undefined, but if the residual is INT_MIN we have bigger problems */
					}
					abs_residual_partition_sums[partition] = abs_residual_partition_sum;// TODO java: it hopes, that in this case int32 is enough
					// abs_residual_partition_sums[partition] = ((long)abs_residual_partition_sum) & 0xffffffff;
				}
			}
			else { /* have to pessimistically use 64 bits for accumulator */
				for( int partition = residual_sample = 0; partition < partitions; partition++ ) {
					long abs_residual_partition_sum64 = 0;
					end += default_partition_samples;
					for( ; residual_sample < end; residual_sample++) {
						final int a = residual[residual_sample];
						abs_residual_partition_sum64 += (a < 0) ? -a : a; /* abs(INT_MIN) is undefined, but if the residual is INT_MIN we have bigger problems */
					}
					abs_residual_partition_sums[partition] = abs_residual_partition_sum64;
				}
			}
		}

		/* now merge partitions for lower orders */
		{
			int from_partition = 0, to_partition = partitions;
			for( int partition_order = max_partition_order - 1; partition_order >= min_partition_order; partition_order-- ) {
				partitions >>= 1;
				for( int i = 0; i < partitions; i++ ) {
					abs_residual_partition_sums[to_partition++] =
						abs_residual_partition_sums[from_partition    ] +
						abs_residual_partition_sums[from_partition + 1];
					from_partition += 2;
				}
			}
		}
	}

	private static void precompute_partition_info_escapes_(
		final int residual[],
		final int raw_bits_per_partition[],
		final int residual_samples,
		final int predictor_order,
		final int min_partition_order,
		final int max_partition_order
	)
	{
		int partition_order;
		int to_partition = 0;
		final int blocksize = residual_samples + predictor_order;

		/* first do max_partition_order */
		for( partition_order = max_partition_order; partition_order >= 0; /* partition_order-- */ ) {// dead code
			final int partitions = 1 << partition_order;
			final int default_partition_samples = blocksize >>> partition_order;

			//FLAC__ASSERT(default_partition_samples > predictor_order);

			for( int partition = 0, residual_sample = 0; partition < partitions; partition++ ) {
				int partition_samples = default_partition_samples;
				if( partition == 0 ) {
					partition_samples -= predictor_order;
				}
				int rmax = 0;
				for( int partition_sample = 0; partition_sample < partition_samples; partition_sample++ ) {
					int r = residual[residual_sample++];
					/* OPT: maybe faster: rmax |= r ^ (r>>31) */
					/*if( r < 0 )
						rmax |= ~r;
					else
						rmax |= r;*/
					if( r < 0 ) {
						r = ~r;
					}
					rmax |= r;
				}
				/* now we know all residual values are in the range [-rmax-1,rmax] */
				raw_bits_per_partition[partition] = rmax != 0 ? Jformat.FLAC__bitmath_ilog2( rmax ) + 2 : 1;
			}
			to_partition = partitions;
			break; /*@@@ yuck, should remove the 'for' loop instead */
		}

		/* now merge partitions for lower orders */
		--partition_order;
		for( int from_partition = 0; partition_order >= min_partition_order; partition_order-- ) {
			int m, n;
			int i;
			final int partitions = 1 << partition_order;
			for( i = 0; i < partitions; i++ ) {
				m = raw_bits_per_partition[from_partition];
				from_partition++;
				n = raw_bits_per_partition[from_partition];
				raw_bits_per_partition[to_partition] = (m >= n ? m : n);
				from_partition++;
				to_partition++;
			}
		}
	}

//#ifdef EXACT_RICE_BITS_CALCULATION
//	private static int count_rice_bits_in_partition_(
//			final int rice_parameter,
//			final int partition_samples,
//			final int[] residual
//	)
//	{
//		int i, partition_bits =
//			Jformat.FLAC__ENTROPY_CODING_METHOD_PARTITIONED_RICE_PARAMETER_LEN + /* actually could end up being FLAC__ENTROPY_CODING_METHOD_PARTITIONED_RICE2_PARAMETER_LEN but err on side of 16bps */
//			(1 + rice_parameter) * partition_samples /* 1 for unary stop bit + rice_parameter for the binary portion */
//		;
//		for( i = 0; i < partition_samples; i++ )
//			partition_bits += ( ((residual[i] << 1) ^ (residual[i] >> 31)) >>> rice_parameter );
//		return partition_bits;
//	}
//#else
	private static int count_rice_bits_in_partition_(
		final int rice_parameter,
		final int partition_samples,
		final long abs_residual_partition_sum
	)
	{
		return
			Jformat.FLAC__ENTROPY_CODING_METHOD_PARTITIONED_RICE_PARAMETER_LEN + /* actually could end up being FLAC__ENTROPY_CODING_METHOD_PARTITIONED_RICE2_PARAMETER_LEN but err on side of 16bps */
			(1 + rice_parameter) * partition_samples + /* 1 for unary stop bit + rice_parameter for the binary portion */
			(
				rice_parameter != 0 ?
					(int)(abs_residual_partition_sum >>> (rice_parameter - 1)) /* rice_parameter-1 because the real coder sign-folds instead of using a sign bit */
					: (int)(abs_residual_partition_sum << 1) /* can't shift by negative number, so reverse */
			)
			- (partition_samples >>> 1)
			/* -(partition_samples>>1) to subtract out extra contributions to the abs_residual_partition_sum.
			 * The actual number of bits used is closer to the sum(for all i in the partition) of  abs(residual[i])>>(rice_parameter-1)
			 * By using the abs_residual_partition sum, we also add in bits in the LSBs that would normally be shifted out.
			 * So the subtraction term tries to guess how many extra bits were contributed.
			 * If the LSBs are randomly distributed, this should average to 0.5 extra bits per sample.
			 */
		;
	}
//#endif

	/** @return < 0 - error, >= 0 - bits */
	private static int /* boolean */ set_partitioned_rice_(
//#ifdef EXACT_RICE_BITS_CALCULATION
//		const FLAC__int32 residual[],
//#endif
		final long abs_residual_partition_sums[],
		final int raw_bits_per_partition[],
		final int offset,// java: added as offset to abs_residual_partition_sums and raw_bits_per_partition
		final int residual_samples,
		final int predictor_order,
		final int suggested_rice_parameter,
		final int rice_parameter_limit,
		final int rice_parameter_search_dist,
		final int partition_order,
		final boolean search_for_escapes,
		final JFLAC__EntropyCodingMethod_PartitionedRiceContents partitioned_rice_contents//,
		// int[] bits// java: return value
	)
	{
		int rice_parameter, partition_bits;
		int best_partition_bits, best_rice_parameter = 0;
		int bits_ = Jformat.FLAC__ENTROPY_CODING_METHOD_TYPE_LEN + Jformat.FLAC__ENTROPY_CODING_METHOD_PARTITIONED_RICE_ORDER_LEN;
//#ifdef ENABLE_RICE_PARAMETER_SEARCH
//		unsigned min_rice_parameter, max_rice_parameter;
//#else
//		(void)rice_parameter_search_dist;
//#endif

		//FLAC__ASSERT(suggested_rice_parameter < FLAC__ENTROPY_CODING_METHOD_PARTITIONED_RICE2_ESCAPE_PARAMETER);
		//FLAC__ASSERT(rice_parameter_limit <= FLAC__ENTROPY_CODING_METHOD_PARTITIONED_RICE2_ESCAPE_PARAMETER);

		partitioned_rice_contents.FLAC__format_entropy_coding_method_partitioned_rice_contents_ensure_size( (6 >= partition_order ? 6 : partition_order) );
		final int[] parameters = partitioned_rice_contents.parameters;
		final int[] raw_bits = partitioned_rice_contents.raw_bits;

		if( partition_order == 0 ) {
			best_partition_bits = Integer.MAX_VALUE;
/*#ifdef ENABLE_RICE_PARAMETER_SEARCH
			if( rice_parameter_search_dist ) {
				if( suggested_rice_parameter < rice_parameter_search_dist)
					min_rice_parameter = 0;
				else
					min_rice_parameter = suggested_rice_parameter - rice_parameter_search_dist;
				max_rice_parameter = suggested_rice_parameter + rice_parameter_search_dist;
				if( max_rice_parameter >= rice_parameter_limit ) {
#ifdef DEBUG_VERBOSE
					System.err.printf("clipping rice_parameter (%d -> %d) @5\n", max_rice_parameter, rice_parameter_limit - 1);
#endif
					max_rice_parameter = rice_parameter_limit - 1;
				}
			}
			else
				min_rice_parameter = max_rice_parameter = suggested_rice_parameter;

			for( rice_parameter = min_rice_parameter; rice_parameter <= max_rice_parameter; rice_parameter++ ) {
#else*/
				rice_parameter = suggested_rice_parameter;
//#endif
//#ifdef EXACT_RICE_BITS_CALCULATION
//				partition_bits = count_rice_bits_in_partition_(rice_parameter, residual_samples, residual);
//#else
				partition_bits = count_rice_bits_in_partition_( rice_parameter, residual_samples, abs_residual_partition_sums[offset] );
//#endif
				if( partition_bits < best_partition_bits) {
					best_rice_parameter = rice_parameter;
					best_partition_bits = partition_bits;
				}
//#ifdef ENABLE_RICE_PARAMETER_SEARCH
//			}
//#endif
			if( search_for_escapes ) {
				partition_bits = Jformat.FLAC__ENTROPY_CODING_METHOD_PARTITIONED_RICE2_PARAMETER_LEN + Jformat.FLAC__ENTROPY_CODING_METHOD_PARTITIONED_RICE_RAW_LEN + raw_bits_per_partition[offset] * residual_samples;
				if( partition_bits <= best_partition_bits ) {
					raw_bits[0] = raw_bits_per_partition[offset];
					best_rice_parameter = 0; /* will be converted to appropriate escape parameter later */
					best_partition_bits = partition_bits;
				} else {
					raw_bits[0] = 0;
				}
			}
			parameters[0] = best_rice_parameter;
			bits_ += best_partition_bits;
		}
		else {
			final int partitions = 1 << partition_order;
			for( int partition = /*residual_sample = */0; partition < partitions; partition++ ) {
				int partition_samples = (residual_samples + predictor_order) >>> partition_order;
				if( partition == 0 ) {
					if( partition_samples <= predictor_order ) {
						return -1;// false;
					} else {
						partition_samples -= predictor_order;
					}
				}
				final long mean = abs_residual_partition_sums[offset + partition];
				/* we are basically calculating the size in bits of the
				 * average residual magnitude in the partition:
				 *   rice_parameter = floor(log2(mean/partition_samples))
				 * 'mean' is not a good name for the variable, it is
				 * actually the sum of magnitudes of all residual values
				 * in the partition, so the actual mean is
				 * mean/partition_samples
				 */
//#if 0 /* old simple code */
//				for( rice_parameter = 0, k = partition_samples; k < mean; rice_parameter++, k <<= 1 )
//					;
//#else
//#if defined FLAC__CPU_X86_64 /* and other 64-bit arch, too */
//				if( mean <= 0x80000000 / 512 ) { /* 512: more or less optimal for both 16- and 24-bit input */
//#else
				if( mean <= 0x80000000 / 8 ) { /* 32-bit arch: use 32-bit math if possible */
//#endif
					final long mean2 = mean;
					rice_parameter = 0;
					long k2 = partition_samples;
					while( (k2 << 3) < mean2 ) { /* requires: mean <= (2^31)/8 */
						rice_parameter += 4; k2 <<= 4; /* tuned for 16-bit input */
					}
					while( k2 < mean2 ) { /* requires: mean <= 2^31 */
						rice_parameter++; k2 <<= 1;
					}
				}
				else {
					rice_parameter = 0;
					long k = partition_samples;
					if( mean <= 0x100000000000000L ) {
						while( (k << 7) < mean ) { /* requires: mean <= (2^63)/128 */
							rice_parameter += 8; k <<= 8; /* tuned for 24-bit input */
						}
					}
					while( k < mean ) { /* requires: mean <= 2^63 */
						rice_parameter++; k <<= 1;
					}
				}
//#endif
				if( rice_parameter >= rice_parameter_limit ) {
/*#ifdef DEBUG_VERBOSE
					System.err.printf("clipping rice_parameter (%d -> %d) @6\n", rice_parameter, rice_parameter_limit - 1);
#endif*/
					rice_parameter = rice_parameter_limit - 1;
				}

				best_partition_bits = Integer.MAX_VALUE;
/*#ifdef ENABLE_RICE_PARAMETER_SEARCH
				if( rice_parameter_search_dist ) {
					if( rice_parameter < rice_parameter_search_dist )
						min_rice_parameter = 0;
					else
						min_rice_parameter = rice_parameter - rice_parameter_search_dist;
					max_rice_parameter = rice_parameter + rice_parameter_search_dist;
					if( max_rice_parameter >= rice_parameter_limit ) {
#ifdef DEBUG_VERBOSE
						System.err.printf("clipping rice_parameter (%d -> %d) @7\n", max_rice_parameter, rice_parameter_limit - 1);
#endif
						max_rice_parameter = rice_parameter_limit - 1;
					}
				}
				else
					min_rice_parameter = max_rice_parameter = rice_parameter;

				for( rice_parameter = min_rice_parameter; rice_parameter <= max_rice_parameter; rice_parameter++ ) {
#endif*/
//#ifdef EXACT_RICE_BITS_CALCULATION
//					partition_bits = count_rice_bits_in_partition_(rice_parameter, partition_samples, residual + residual_sample);
//#else
					partition_bits = count_rice_bits_in_partition_(rice_parameter, partition_samples, abs_residual_partition_sums[offset + partition]);
//#endif
					if( partition_bits < best_partition_bits ) {
						best_rice_parameter = rice_parameter;
						best_partition_bits = partition_bits;
					}
//#ifdef ENABLE_RICE_PARAMETER_SEARCH
//				}
//#endif
				if( search_for_escapes ) {
					partition_bits = Jformat.FLAC__ENTROPY_CODING_METHOD_PARTITIONED_RICE2_PARAMETER_LEN + Jformat.FLAC__ENTROPY_CODING_METHOD_PARTITIONED_RICE_RAW_LEN + raw_bits_per_partition[offset + partition] * partition_samples;
					if( partition_bits <= best_partition_bits) {
						raw_bits[partition] = raw_bits_per_partition[offset + partition];
						best_rice_parameter = 0; /* will be converted to appropriate escape parameter later */
						best_partition_bits = partition_bits;
					} else {
						raw_bits[partition] = 0;
					}
				}
				parameters[partition] = best_rice_parameter;
				bits_ += best_partition_bits;
				//residual_sample += partition_samples;
			}
		}

		//bits[0] = bits_;
		return bits_;//return true;
	}

	private static int get_wasted_bits_(final int signal[], final int samples)
	{
		int shift;
		int x = 0;

		for( int i = 0; i < samples && 0 == (x & 1); i++ ) {
			x |= signal[i];
		}

		if( x == 0 ) {
			shift = 0;
		}
		else {
			for( shift = 0; 0 == (x & 1); shift++ ) {
				x >>= 1;
			}
		}

		if( shift > 0 ) {
			for( int i = 0; i < samples; i++ ) {
				signal[i] >>= shift;
			}
		}

		return shift;
	}

	@Override// implements JFLAC__StreamDecoderReadCallback, verify_read_callback_
	public int dec_read_callback(final JFLAC__StreamDecoder decoder, final byte buffer[], final int offset, int bytes/*, final Object client_data*/) throws IOException
	{
		// final JFLAC__StreamEncoder encoder = (JFLAC__StreamEncoder)client_data;// java: this
		final int encoded_bytes = this.verify.output.bytes;
		//(void)decoder;

		if( this.verify.needs_magic_hack ) {
			//FLAC__ASSERT(*bytes >= FLAC__STREAM_SYNC_LENGTH);
			bytes = Jformat.FLAC__STREAM_SYNC_LENGTH;
			System.arraycopy( Jformat.FLAC__STREAM_SYNC_STRING, 0, buffer, offset, bytes );
			this.verify.needs_magic_hack = false;
		}
		else {
			if( encoded_bytes == 0 ) {
				/*
				 * If we get here, a FIFO underflow has occurred,
				 * which means there is a bug somewhere.
				 */
				//FLAC__ASSERT(0);
				throw new IOException();// JFLAC__StreamDecoder.FLAC__STREAM_DECODER_READ_STATUS_ABORT;
			}
			else if( encoded_bytes < bytes ) {
				bytes = encoded_bytes;
			}
			System.arraycopy( this.verify.output.data, this.verify.output.offset, buffer, offset, bytes );
			this.verify.output.offset += bytes;// encoder.private_.verify.output.data += bytes[0];
			this.verify.output.bytes -= bytes;
		}

		return bytes;// return JFLAC__StreamDecoder.FLAC__STREAM_DECODER_READ_STATUS_CONTINUE;
	}

	@Override// JFLAC__StreamDecoderWriteCallback, verify_write_callback_
	public int /* FLAC__StreamDecoderWriteStatus */ dec_write_callback(final JFLAC__StreamDecoder decoder,
			final JFLAC__Frame fr, final int buffer[][], final int offset/*, final Object client_data*/)
	{
		// final JFLAC__StreamEncoder encoder = (JFLAC__StreamEncoder )client_data;// java: this
		final int nchannels = fr.header.channels;
		final int block_size = fr.header.blocksize;
		//final int bytes_per_block = blocksize << 2;// sizeof int32 = 4

		//(void)decoder;

		for( int channel = 0; channel < nchannels; channel++ ) {
			//if( 0 != Jformat.memcmp( buffer[channel], 0, encoder.private_.verify.input_fifo.data[channel], 0, bytes_per_block ) ) {// FIXME why need double loop?
				int i, bi, sample = 0;
				int expect = 0, got = 0;
				final int[] buff_ch = buffer[channel];
				final int[] data_ch = this.verify.input_fifo.data[channel];

				for( i = 0, bi = offset; i < block_size; i++, bi++ ) {
					if( buff_ch[bi] != data_ch[i] ) {
						sample = i;
						expect = data_ch[i];
						got = buff_ch[bi];
						break;
					}
				}
				if( i < block_size ) {// java: added
					//FLAC__ASSERT(i < blocksize);
					//FLAC__ASSERT(frame->header.number_type == FLAC__FRAME_NUMBER_TYPE_SAMPLE_NUMBER);
					this.verify.error_stats.absolute_sample = fr.header./*number.*/sample_number + sample;
					this.verify.error_stats.frame_number = (int)(fr.header./*number.*/sample_number / block_size);
					this.verify.error_stats.channel = channel;
					this.verify.error_stats.sample = sample;
					this.verify.error_stats.expected = expect;
					this.verify.error_stats.got = got;
					this.state = FLAC__STREAM_ENCODER_VERIFY_MISMATCH_IN_AUDIO_DATA;
					return JFLAC__StreamDecoder.FLAC__STREAM_DECODER_WRITE_STATUS_ABORT;
				}
			//}
		}
		/* dequeue the frame from the fifo */
		this.verify.input_fifo.tail -= block_size;
		//FLAC__ASSERT(encoder->private_->verify.input_fifo.tail <= OVERREAD_);
		for( int channel = 0; channel < nchannels; channel++ ) {
			System.arraycopy( this.verify.input_fifo.data[channel], block_size, this.verify.input_fifo.data[channel], 0, this.verify.input_fifo.tail );
		}
		return JFLAC__StreamDecoder.FLAC__STREAM_DECODER_WRITE_STATUS_CONTINUE;
	}

	@Override// implements JFLAC__StreamDecoderMetadataCallback, verify_metadata_callback_
	public void dec_metadata_callback(final JFLAC__StreamDecoder decoder, final JFLAC__StreamMetadata meta_data/*, final Object client_data*/) throws IOException
	{
		//(void)decoder, (void)metadata, (void)client_data;
	}

	@Override// implements JFLAC__StreamDecoderErrorCallback, verify_error_callback_
	public void dec_error_callback(final JFLAC__StreamDecoder decoder, final int /* FLAC__StreamDecoderErrorStatus */ status/*, final Object client_data*/)
	{
		// final JFLAC__StreamEncoder encoder = (JFLAC__StreamEncoder)client_data;// java: this
		//(void)decoder, (void)status;
		this.state = FLAC__STREAM_ENCODER_VERIFY_DECODER_ERROR;
	}

	@Override// implements JFLAC__StreamEncoderReadCallback, file_read_callback_
	public int /* FLAC__StreamEncoderReadStatus */ enc_read_callback(final JFLAC__StreamEncoder encoder, final byte buffer[], final int offset, final int bytes/* , final Object client_data*/)
			throws IOException, UnsupportedOperationException
	{
		//(void)client_data;

		final OutputStream f = encoder.file;
		if( f instanceof RandomAccessInputOutputStream ) {
			return ((RandomAccessInputOutputStream) f).read( buffer, offset, bytes );
		}
		throw new UnsupportedOperationException( FLAC__StreamEncoderReadStatusString[FLAC__STREAM_ENCODER_READ_STATUS_UNSUPPORTED] );
	}

	@Override// implements JFLAC__StreamEncoderSeekCallback, file_seek_callback_
	public int /* FLAC__StreamEncoderSeekStatus */ enc_seek_callback(final JFLAC__StreamEncoder encoder, final long absolute_byte_offset/*, final Object client_data*/)
	{
		//(void)client_data;// java: this

		final OutputStream f = encoder.file;
		if( f instanceof RandomAccessInputOutputStream ) {
			try {
				((RandomAccessInputOutputStream) f).seek( absolute_byte_offset );
			} catch(final IOException e) {
				return FLAC__STREAM_ENCODER_SEEK_STATUS_ERROR;
			}
			return FLAC__STREAM_ENCODER_SEEK_STATUS_OK;
		}
		return FLAC__STREAM_ENCODER_SEEK_STATUS_UNSUPPORTED;
	}

	@Override//implements JFLAC__StreamEncoderTellCallback, file_tell_callback_
	public long enc_tell_callback(final JFLAC__StreamEncoder encoder/*, final Object client_data*/) throws IOException, UnsupportedOperationException
	{
		//(void)client_data;
		final OutputStream f = encoder.file;
		if( f instanceof RandomAccessInputOutputStream ) {
			return ((RandomAccessInputOutputStream) f).getFilePointer();
		}
		throw new UnsupportedOperationException( FLAC__StreamEncoderTellStatusString[FLAC__STREAM_ENCODER_TELL_STATUS_UNSUPPORTED] );
	}

/*#ifdef FLAC__VALGRIND_TESTING
	private static size_t local__fwrite(final Object ptr, size_t size, size_t nmemb, OutputStream stream)
	{
		size_t ret = fwrite( ptr, size, nmemb, stream );
		if( ! ferror( stream ) )
			fflush( stream );
		return ret;
	}
#else
	#define local__fwrite fwrite
#endif*/

	@Override//implements JFLAC__StreamEncoderWriteCallback, file_write_callback_
	public int /* FLAC__StreamEncoderWriteStatus */ enc_write_callback(final JFLAC__StreamEncoder encoder,
			final byte buffer[], final int offset, final int bytes, final int samples, final int current_frame/*, final Object client_data*/)
	{
		//(void)client_data, (void)current_frame;

		try {
			/*if( */encoder.file.write( buffer, offset, bytes );// == bytes ) {
				final  boolean call_it;
if( Jformat.FLAC__HAS_OGG ) {
				/* We would like to be able to use 'samples > 0' in the
				 * clause here but currently because of the nature of our
				 * Ogg writing implementation, 'samples' is always 0 (see
				 * ogg_encoder_aspect.c).  The downside is extra progress
				 * callbacks.
				 */
				call_it = encoder.progress_callback != null && (encoder.is_ogg ? true : (samples > 0));
} else {
				call_it = encoder.progress_callback != null && (samples > 0);
}
			//}// if
			if( call_it ) {
				/* NOTE: We have to add +bytes, +samples, and +1 to the stats
				 * because at this point in the callback chain, the stats
				 * have not been updated.  Only after we return and control
				 * gets back to write_frame_() are the stats updated
				 */
				encoder.progress_callback.enc_progress_callback( encoder,
						encoder.bytes_written + bytes, encoder.samples_written + samples,
						encoder.frames_written + (samples != 0 ? 1 : 0), encoder.total_frames_estimate/*, encoder.client_data*/ );
			}
		} catch(final IOException e) {
			return FLAC__STREAM_ENCODER_WRITE_STATUS_FATAL_ERROR;
		}
		return FLAC__STREAM_ENCODER_WRITE_STATUS_OK;
	}
}
