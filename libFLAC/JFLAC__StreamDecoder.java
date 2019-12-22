package libFLAC;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

/** \file include/FLAC/stream_decoder.h
	 *
	 *  \brief
	 *  This module contains the functions which implement the stream
	 *  decoder.
	 *
	 *  See the detailed documentation in the
	 *  \link flac_stream_decoder stream decoder \endlink module.
	 */
/** \defgroup flac_decoder FLAC/ \*_decoder.h: decoder interfaces
	 *  \ingroup flac
	 *
	 *  \brief
	 *  This module describes the decoder layers provided by libFLAC.
	 *
	 * The stream decoder can be used to decode complete streams either from
	 * the client via callbacks, or directly from a file, depending on how
	 * it is initialized.  When decoding via callbacks, the client provides
	 * callbacks for reading FLAC data and writing decoded samples, and
	 * handling metadata and errors.  If the client also supplies seek-related
	 * callback, the decoder function for sample-accurate seeking within the
	 * FLAC input is also available.  When decoding from a file, the client
	 * needs only supply a filename or open \c FILE* and write/metadata/error
	 * callbacks; the rest of the callbacks are supplied internally.  For more
	 * info see the \link flac_stream_decoder stream decoder \endlink module.
	 */

	/** \defgroup flac_stream_decoder FLAC/stream_decoder.h: stream decoder interface
	 *  \ingroup flac_decoder
	 *
	 *  \brief
	 *  This module contains the functions which implement the stream
	 *  decoder.
	 *
	 * The stream decoder can decode native FLAC, and optionally Ogg FLAC
	 * (check FLAC_API_SUPPORTS_OGG_FLAC) streams and files.
	 *
	 * The basic usage of this decoder is as follows:
	 * - The program creates an instance of a decoder using
	 *   FLAC__stream_decoder_new().
	 * - The program overrides the default settings using
	 *   FLAC__stream_decoder_set_*() functions.
	 * - The program initializes the instance to validate the settings and
	 *   prepare for decoding using
	 *   - FLAC__stream_decoder_init_stream() or FLAC__stream_decoder_init_FILE()
	 *     or FLAC__stream_decoder_init_file() for native FLAC,
	 *   - FLAC__stream_decoder_init_ogg_stream() or FLAC__stream_decoder_init_ogg_FILE()
	 *     or FLAC__stream_decoder_init_ogg_file() for Ogg FLAC
	 * - The program calls the FLAC__stream_decoder_process_*() functions
	 *   to decode data, which subsequently calls the callbacks.
	 * - The program finishes the decoding with FLAC__stream_decoder_finish(),
	 *   which flushes the input and output and resets the decoder to the
	 *   uninitialized state.
	 * - The instance may be used again or deleted with
	 *   FLAC__stream_decoder_delete().
	 *
	 * In more detail, the program will create a new instance by calling
	 * FLAC__stream_decoder_new(), then call FLAC__stream_decoder_set_*()
	 * functions to override the default decoder options, and call
	 * one of the FLAC__stream_decoder_init_*() functions.
	 *
	 * There are three initialization functions for native FLAC, one for
	 * setting up the decoder to decode FLAC data from the client via
	 * callbacks, and two for decoding directly from a FLAC file.
	 *
	 * For decoding via callbacks, use FLAC__stream_decoder_init_stream().
	 * You must also supply several callbacks for handling I/O.  Some (like
	 * seeking) are optional, depending on the capabilities of the input.
	 *
	 * For decoding directly from a file, use FLAC__stream_decoder_init_FILE()
	 * or FLAC__stream_decoder_init_file().  Then you must only supply an open
	 * \c FILE* or filename and fewer callbacks; the decoder will handle
	 * the other callbacks internally.
	 *
	 * There are three similarly-named init functions for decoding from Ogg
	 * FLAC streams.  Check \c FLAC_API_SUPPORTS_OGG_FLAC to find out if the
	 * library has been built with Ogg support.
	 *
	 * Once the decoder is initialized, your program will call one of several
	 * functions to start the decoding process:
	 *
	 * - FLAC__stream_decoder_process_single() - Tells the decoder to process at
	 *   most one metadata block or audio frame and return, calling either the
	 *   metadata callback or write callback, respectively, once.  If the decoder
	 *   loses sync it will return with only the error callback being called.
	 * - FLAC__stream_decoder_process_until_end_of_metadata() - Tells the decoder
	 *   to process the stream from the current location and stop upon reaching
	 *   the first audio frame.  The client will get one metadata, write, or error
	 *   callback per metadata block, audio frame, or sync error, respectively.
	 * - FLAC__stream_decoder_process_until_end_of_stream() - Tells the decoder
	 *   to process the stream from the current location until the read callback
	 *   returns FLAC__STREAM_DECODER_READ_STATUS_END_OF_STREAM or
	 *   FLAC__STREAM_DECODER_READ_STATUS_ABORT.  The client will get one metadata,
	 *   write, or error callback per metadata block, audio frame, or sync error,
	 *   respectively.
	 *
	 * When the decoder has finished decoding (normally or through an abort),
	 * the instance is finished by calling FLAC__stream_decoder_finish(), which
	 * ensures the decoder is in the correct state and frees memory.  Then the
	 * instance may be deleted with FLAC__stream_decoder_delete() or initialized
	 * again to decode another stream.
	 *
	 * Seeking is exposed through the FLAC__stream_decoder_seek_absolute() method.
	 * At any point after the stream decoder has been initialized, the client can
	 * call this function to seek to an exact sample within the stream.
	 * Subsequently, the first time the write callback is called it will be
	 * passed a (possibly partial) block starting at that sample.
	 *
	 * If the client cannot seek via the callback interface provided, but still
	 * has another way of seeking, it can flush the decoder using
	 * FLAC__stream_decoder_flush() and start feeding data from the new position
	 * through the read callback.
	 *
	 * The stream decoder also provides MD5 signature checking.  If this is
	 * turned on before initialization, FLAC__stream_decoder_finish() will
	 * report when the decoded MD5 signature does not match the one stored
	 * in the STREAMINFO block.  MD5 checking is automatically turned off
	 * (until the next FLAC__stream_decoder_reset()) if there is no signature
	 * in the STREAMINFO block or when a seek is attempted.
	 *
	 * The FLAC__stream_decoder_set_metadata_*() functions deserve special
	 * attention.  By default, the decoder only calls the metadata_callback for
	 * the STREAMINFO block.  These functions allow you to tell the decoder
	 * explicitly which blocks to parse and return via the metadata_callback
	 * and/or which to skip.  Use a FLAC__stream_decoder_set_metadata_respond_all(),
	 * FLAC__stream_decoder_set_metadata_ignore() ... or FLAC__stream_decoder_set_metadata_ignore_all(),
	 * FLAC__stream_decoder_set_metadata_respond() ... sequence to exactly specify
	 * which blocks to return.  Remember that metadata blocks can potentially
	 * be big (for example, cover art) so filtering out the ones you don't
	 * use can reduce the memory requirements of the decoder.  Also note the
	 * special forms FLAC__stream_decoder_set_metadata_respond_application(id)
	 * and FLAC__stream_decoder_set_metadata_ignore_application(id) for
	 * filtering APPLICATION blocks based on the application ID.
	 *
	 * STREAMINFO and SEEKTABLE blocks are always parsed and used internally, but
	 * they still can legally be filtered from the metadata_callback.
	 *
	 * @note
	 * The "set" functions may only be called when the decoder is in the
	 * state FLAC__STREAM_DECODER_UNINITIALIZED, i.e. after
	 * FLAC__stream_decoder_new() or FLAC__stream_decoder_finish(), but
	 * before FLAC__stream_decoder_init_*().  If this is the case they will
	 * return \c true, otherwise \c false.
	 *
	 * @note
	 * FLAC__stream_decoder_finish() resets all settings to the constructor
	 * defaults, including the callbacks.
	 *
	 * \{
	 */
public final class JFLAC__StreamDecoder implements
	JFLAC__StreamDecoderReadCallback,// file_read_callback_
	JFLAC__StreamDecoderSeekCallback,// file_seek_callback_
	JFLAC__StreamDecoderTellCallback,// file_tell_callback_
	JFLAC__StreamDecoderLengthCallback,// file_length_callback_
	JFLAC__StreamDecoderEofCallback,// file_eof_callback_
	JFLAC__BitReaderReadCallback, JFLAC__OggDecoderAspectReadCallbackProxy
{
	/** State values for a FLAC__StreamDecoder
	 *
	 * The decoder's state can be obtained by calling FLAC__stream_decoder_get_state().
	 */
	//typedef enum {
		/** The decoder is ready to search for metadata. */
		private static final int FLAC__STREAM_DECODER_SEARCH_FOR_METADATA = 0;

		/** The decoder is ready to or is in the process of reading metadata. */
		private static final int FLAC__STREAM_DECODER_READ_METADATA = 1;

		/** The decoder is ready to or is in the process of searching for the
		 * frame sync code.
		 */
		private static final int FLAC__STREAM_DECODER_SEARCH_FOR_FRAME_SYNC = 2;

		/** The decoder is ready to or is in the process of reading a frame. */
		private static final int FLAC__STREAM_DECODER_READ_FRAME = 3;

		/** The decoder has reached the end of the stream. */
		public static final int FLAC__STREAM_DECODER_END_OF_STREAM = 4;

		/** An error occurred in the underlying Ogg layer.  */
		static final int FLAC__STREAM_DECODER_OGG_ERROR = 5;// FIXME 1.3.1 never uses

		/** An error occurred while seeking.  The decoder must be flushed
		 * with FLAC__stream_decoder_flush() or reset with
		 * FLAC__stream_decoder_reset() before decoding can continue.
		 */
		private static final int FLAC__STREAM_DECODER_SEEK_ERROR = 6;

		/** The decoder was aborted by the read or write callback. */
		private static final int FLAC__STREAM_DECODER_ABORTED = 7;

		/** An error occurred allocating memory.  The decoder is in an invalid
		 * state and can no longer be used.
		 */
		private static final int FLAC__STREAM_DECODER_MEMORY_ALLOCATION_ERROR = 8;

		/** The decoder is in the uninitialized state; one of the
		 * FLAC__stream_decoder_init_*() functions must be called before samples
		 * can be processed.
		 */
		public static final int FLAC__STREAM_DECODER_UNINITIALIZED = 9;

	//} FLAC__StreamDecoderState;

	/** Maps a FLAC__StreamDecoderState to a C string.
	 *
	 *  Using a FLAC__StreamDecoderState as the index to this array
	 *  will give the string equivalent.  The contents should not be modified.
	 */
	public static final String FLAC__StreamDecoderStateString[] = {
			"FLAC__STREAM_DECODER_SEARCH_FOR_METADATA",
			"FLAC__STREAM_DECODER_READ_METADATA",
			"FLAC__STREAM_DECODER_SEARCH_FOR_FRAME_SYNC",
			"FLAC__STREAM_DECODER_READ_FRAME",
			"FLAC__STREAM_DECODER_END_OF_STREAM",
			"FLAC__STREAM_DECODER_OGG_ERROR",
			"FLAC__STREAM_DECODER_SEEK_ERROR",
			"FLAC__STREAM_DECODER_ABORTED",
			"FLAC__STREAM_DECODER_MEMORY_ALLOCATION_ERROR",
			"FLAC__STREAM_DECODER_UNINITIALIZED"
		};


	/** Possible return values for the FLAC__stream_decoder_init_*() functions.
	 */
	// typedef enum {
		/** Initialization was successful. */
		public static final int FLAC__STREAM_DECODER_INIT_STATUS_OK = 0;

		/** The library was not compiled with support for the given container
		 * format.
		 */
		public static final int FLAC__STREAM_DECODER_INIT_STATUS_UNSUPPORTED_CONTAINER = 1;

		/** A required callback was not supplied. */
		public static final int FLAC__STREAM_DECODER_INIT_STATUS_INVALID_CALLBACKS = 2;

		/** An error occurred allocating memory. */
		public static final int FLAC__STREAM_DECODER_INIT_STATUS_MEMORY_ALLOCATION_ERROR = 3;

		/** fopen() failed in FLAC__stream_decoder_init_file() or
		 * FLAC__stream_decoder_init_ogg_file(). */
		public static final int FLAC__STREAM_DECODER_INIT_STATUS_ERROR_OPENING_FILE = 4;

		/** FLAC__stream_decoder_init_*() was called when the decoder was
		 * already initialized, usually because
		 * FLAC__stream_decoder_finish() was not called.
		 */
		public static final int FLAC__STREAM_DECODER_INIT_STATUS_ALREADY_INITIALIZED = 5;

	//} FLAC__StreamDecoderInitStatus;

	/** Maps a FLAC__StreamDecoderInitStatus to a C string.
	 *
	 *  Using a FLAC__StreamDecoderInitStatus as the index to this array
	 *  will give the string equivalent.  The contents should not be modified.
	 */
	public static final String FLAC__StreamDecoderInitStatusString[] = {
			"FLAC__STREAM_DECODER_INIT_STATUS_OK",
			"FLAC__STREAM_DECODER_INIT_STATUS_UNSUPPORTED_CONTAINER",
			"FLAC__STREAM_DECODER_INIT_STATUS_INVALID_CALLBACKS",
			"FLAC__STREAM_DECODER_INIT_STATUS_MEMORY_ALLOCATION_ERROR",
			"FLAC__STREAM_DECODER_INIT_STATUS_ERROR_OPENING_FILE",
			"FLAC__STREAM_DECODER_INIT_STATUS_ALREADY_INITIALIZED"
		};

	/** Return values for the FLAC__StreamDecoder read callback.
	 */
	// typedef enum {// java: used IOException and -1 as End Of Stream
		/** The read was OK and decoding can continue. */
		//public static final int FLAC__STREAM_DECODER_READ_STATUS_CONTINUE = 0;

		/** The read was attempted while at the end of the stream.  Note that
		 * the client must only return this value when the read callback was
		 * called when already at the end of the stream.  Otherwise, if the read
		 * itself moves to the end of the stream, the client should still return
		 * the data and \c FLAC__STREAM_DECODER_READ_STATUS_CONTINUE, and then on
		 * the next read callback it should return
		 * \c FLAC__STREAM_DECODER_READ_STATUS_END_OF_STREAM with a byte count
		 * of \c 0.
		 */
		//public static final int FLAC__STREAM_DECODER_READ_STATUS_END_OF_STREAM = 1;

		/** An unrecoverable error occurred.  The decoder will return from the process call. */
		//public static final int FLAC__STREAM_DECODER_READ_STATUS_ABORT = 2;

	//} FLAC__StreamDecoderReadStatus;

	/** Maps a FLAC__StreamDecoderReadStatus to a C string.
	 *
	 *  Using a FLAC__StreamDecoderReadStatus as the index to this array
	 *  will give the string equivalent.  The contents should not be modified.
	 */
	/*private static final String FLAC__StreamDecoderReadStatusString[] = {
			"FLAC__STREAM_DECODER_READ_STATUS_CONTINUE",
			"FLAC__STREAM_DECODER_READ_STATUS_END_OF_STREAM",
			"FLAC__STREAM_DECODER_READ_STATUS_ABORT"
		};*/

	/** Return values for the FLAC__StreamDecoder seek callback.
	 */
	// typedef enum {
		/** The seek was OK and decoding can continue. */
		public static final int FLAC__STREAM_DECODER_SEEK_STATUS_OK = 0;

		/** An unrecoverable error occurred.  The decoder will return from the process call. */
		public static final int FLAC__STREAM_DECODER_SEEK_STATUS_ERROR = 1;

		/** Client does not support seeking. */
		public static final int FLAC__STREAM_DECODER_SEEK_STATUS_UNSUPPORTED = 2;

	//} FLAC__StreamDecoderSeekStatus;

	/** Maps a FLAC__StreamDecoderSeekStatus to a C string.
	 *
	 *  Using a FLAC__StreamDecoderSeekStatus as the index to this array
	 *  will give the string equivalent.  The contents should not be modified.
	 */
	public static final String FLAC__StreamDecoderSeekStatusString[] = {
			"FLAC__STREAM_DECODER_SEEK_STATUS_OK",
			"FLAC__STREAM_DECODER_SEEK_STATUS_ERROR",
			"FLAC__STREAM_DECODER_SEEK_STATUS_UNSUPPORTED"
		};

	/** Return values for the FLAC__StreamDecoder tell callback.
	 */
	// typedef enum {// java: changed. uses IOException, UnsupportedOperationException
		/** The tell was OK and decoding can continue. */
		//private static final int FLAC__STREAM_DECODER_TELL_STATUS_OK = 0;

		/** An unrecoverable error occurred.  The decoder will return from the process call. */
		//private static final int FLAC__STREAM_DECODER_TELL_STATUS_ERROR = 1;

		/** Client does not support telling the position. */
		public static final int FLAC__STREAM_DECODER_TELL_STATUS_UNSUPPORTED = 2;

	//} FLAC__StreamDecoderTellStatus;

	/** Maps a FLAC__StreamDecoderTellStatus to a C string.
	 *
	 *  Using a FLAC__StreamDecoderTellStatus as the index to this array
	 *  will give the string equivalent.  The contents should not be modified.
	 */
	public static final String FLAC__StreamDecoderTellStatusString[] = {
			"FLAC__STREAM_DECODER_TELL_STATUS_OK",
			"FLAC__STREAM_DECODER_TELL_STATUS_ERROR",
			"FLAC__STREAM_DECODER_TELL_STATUS_UNSUPPORTED"
		};

	/** Return values for the FLAC__StreamDecoder length callback.
	 */
	// typedef enum {// java: uses IOException instead
		/** The length call was OK and decoding can continue. */
		//public static final int FLAC__STREAM_DECODER_LENGTH_STATUS_OK = 0;

		/** An unrecoverable error occurred.  The decoder will return from the process call. */
		//public static final int FLAC__STREAM_DECODER_LENGTH_STATUS_ERROR = 1;

		/** Client does not support reporting the length. */
		private static final int FLAC__STREAM_DECODER_LENGTH_STATUS_UNSUPPORTED = 2;

	//} FLAC__StreamDecoderLengthStatus;

	/** Maps a FLAC__StreamDecoderLengthStatus to a C string.
	 *
	 *  Using a FLAC__StreamDecoderLengthStatus as the index to this array
	 *  will give the string equivalent.  The contents should not be modified.
	 */
	private static final String FLAC__StreamDecoderLengthStatusString[] = {
			"FLAC__STREAM_DECODER_LENGTH_STATUS_OK",
			"FLAC__STREAM_DECODER_LENGTH_STATUS_ERROR",
			"FLAC__STREAM_DECODER_LENGTH_STATUS_UNSUPPORTED"
		};

	/** Return values for the FLAC__StreamDecoder write callback.
	 */
	// typedef enum {
		/** The write was OK and decoding can continue. */
		public static final int FLAC__STREAM_DECODER_WRITE_STATUS_CONTINUE = 0;

		/** An unrecoverable error occurred.  The decoder will return from the process call. */
		public static final int FLAC__STREAM_DECODER_WRITE_STATUS_ABORT = 1;

	//} FLAC__StreamDecoderWriteStatus;

	/** Maps a FLAC__StreamDecoderWriteStatus to a C string.
	 *
	 *  Using a FLAC__StreamDecoderWriteStatus as the index to this array
	 *  will give the string equivalent.  The contents should not be modified.
	 */
	public static final String FLAC__StreamDecoderWriteStatusString[] = {
			"FLAC__STREAM_DECODER_WRITE_STATUS_CONTINUE",
			"FLAC__STREAM_DECODER_WRITE_STATUS_ABORT"
		};


	/** Possible values passed back to the FLAC__StreamDecoder error callback.
	 *  \c FLAC__STREAM_DECODER_ERROR_STATUS_LOST_SYNC is the generic catch-
	 *  all.  The rest could be caused by bad sync (false synchronization on
	 *  data that is not the start of a frame) or corrupted data.  The error
	 *  itself is the decoder's best guess at what happened assuming a correct
	 *  sync.  For example \c FLAC__STREAM_DECODER_ERROR_STATUS_BAD_HEADER
	 *  could be caused by a correct sync on the start of a frame, but some
	 *  data in the frame header was corrupted.  Or it could be the result of
	 *  syncing on a point the stream that looked like the starting of a frame
	 *  but was not.  \c FLAC__STREAM_DECODER_ERROR_STATUS_UNPARSEABLE_STREAM
	 *  could be because the decoder encountered a valid frame made by a future
	 *  version of the encoder which it cannot parse, or because of a false
	 *  sync making it appear as though an encountered frame was generated by
	 *  a future encoder.
	 */
	//typedef enum {
		/** An error in the stream caused the decoder to lose synchronization. */
		public static final int FLAC__STREAM_DECODER_ERROR_STATUS_LOST_SYNC = 0;

		/** The decoder encountered a corrupted frame header. */
		public static final int FLAC__STREAM_DECODER_ERROR_STATUS_BAD_HEADER = 1;

		/** The frame's data did not match the CRC in the footer. */
		public static final int FLAC__STREAM_DECODER_ERROR_STATUS_FRAME_CRC_MISMATCH = 2;

		/** The decoder encountered reserved fields in use in the stream. */
		public static final int FLAC__STREAM_DECODER_ERROR_STATUS_UNPARSEABLE_STREAM = 3;

	//} FLAC__StreamDecoderErrorStatus;

	/** Maps a FLAC__StreamDecoderErrorStatus to a C string.
	 *
	 *  Using a FLAC__StreamDecoderErrorStatus as the index to this array
	 *  will give the string equivalent.  The contents should not be modified.
	 */
	public static final String FLAC__StreamDecoderErrorStatusString[] = {
			"FLAC__STREAM_DECODER_ERROR_STATUS_LOST_SYNC",
			"FLAC__STREAM_DECODER_ERROR_STATUS_BAD_HEADER",
			"FLAC__STREAM_DECODER_ERROR_STATUS_FRAME_CRC_MISMATCH",
			"FLAC__STREAM_DECODER_ERROR_STATUS_UNPARSEABLE_STREAM"
		};

	/***********************************************************************
	 *
	 * class FLAC__StreamDecoder
	 *
	 ***********************************************************************/
	static final byte ID3V2_TAG_[] = { 'I', 'D', '3' };
	//private static class JFLAC__StreamDecoderProtected {// java: changed to 'default'
		int /*FLAC__StreamDecoderState*/ state;
		int /*FLAC__StreamDecoderInitStatus*/ initstate;
		int channels;
		int channel_assignment;
		int bits_per_sample;
		int sample_rate; /* in Hz */
		int blocksize; /* in samples (per channel) */
		boolean md5_checking; /* if true, generate MD5 signature of decoded data and compare against signature in the STREAMINFO metadata block */
//if( FLAC__HAS_OGG ) {
		final JFLAC__OggDecoderAspect ogg_decoder_aspect = new JFLAC__OggDecoderAspect();
//}
	//}
	//protected JFLAC__StreamDecoderProtected protected_;

	// static class JFLAC__StreamDecoderPrivate {// java: changed to 'private'
		private boolean is_ogg;
		private JFLAC__StreamDecoderReadCallback read_callback;
		private JFLAC__StreamDecoderSeekCallback seek_callback;
		private JFLAC__StreamDecoderTellCallback tell_callback;
		private JFLAC__StreamDecoderLengthCallback length_callback;
		private JFLAC__StreamDecoderEofCallback eof_callback;
		private JFLAC__StreamDecoderWriteCallback write_callback;
		private JFLAC__StreamDecoderMetadataCallback metadata_callback;
		private JFLAC__StreamDecoderErrorCallback error_callback;
		// used only direct calls, because don't depends from a cpu
		/* generic 32-bit datapath: */
		//void (*local_lpc_restore_signal)(final int residual[], int data_len, final int qlp_coeff[], int order, int lp_quantization, int data[]);
		/* generic 64-bit datapath: */
		//void (*local_lpc_restore_signal_64bit)(final int residual[], int data_len, final int qlp_coeff[], int order, int lp_quantization, int data[]);
		/* for use when the signal is <= 16 bits-per-sample, or <= 15 bits-per-sample on a side channel (which requires 1 extra bit): */
		//void (*local_lpc_restore_signal_16bit)(final int residual[], int data_len, final int qlp_coeff[], int order, int lp_quantization, int data[]);
		/* for use when the signal is <= 16 bits-per-sample, or <= 15 bits-per-sample on a side channel (which requires 1 extra bit), AND order <= 8: */
		//void (*local_lpc_restore_signal_16bit_order8)(final int residual[], unsigned data_len, final int qlp_coeff[], unsigned order, int lp_quantization, int data[]);
		//FLAC__bool (*local_bitreader_read_rice_signed_block)(FLAC__BitReader *br, int vals[], int nvals, int parameter);
		// private Object client_data;// java: don;t need, uses this
		private InputStream file; /* only used if FLAC__stream_decoder_init_file()/FLAC__stream_decoder_init_file() called, else NULL */
		private JFLAC__BitReader input;
		private final int[][] output = new int[Jformat.FLAC__MAX_CHANNELS][];
		private final int[][] residual = new int[Jformat.FLAC__MAX_CHANNELS][]; /* WATCHOUT: these are the aligned pointers; the real pointers that should be free()'d are residual_unaligned[] below */
		private final JFLAC__EntropyCodingMethod_PartitionedRiceContents partitioned_rice_contents[] = new JFLAC__EntropyCodingMethod_PartitionedRiceContents[Jformat.FLAC__MAX_CHANNELS];
		private int output_capacity, output_channels;
		private int fixed_block_size, next_fixed_block_size;
		private long samples_decoded;
		private boolean has_stream_info, has_seek_table;
		private final JFLAC__StreamMetadata_StreamInfo stream_info = new JFLAC__StreamMetadata_StreamInfo();
		private final JFLAC__StreamMetadata_SeekTable seek_table = new JFLAC__StreamMetadata_SeekTable();
		private final boolean metadata_filter[] = new boolean[128]; /* MAGIC number 128 == total number of metadata block types == 1 << 7 */
		private byte[] metadata_filter_ids;
		private int metadata_filter_ids_count, metadata_filter_ids_capacity; /* units for both are IDs, not bytes */
		private final JFLAC__Frame frame = new JFLAC__Frame();
		private boolean cached; /* true if there is a byte in lookahead */
		//FLAC__CPUInfo cpuinfo;
		private final byte header_warmup[] = new byte[2]; /* contains the sync code and reserved bits */
		private byte lookahead; /* temp storage when we need to look ahead one byte in the stream */
		/** unaligned (original) pointers to allocated data */
		//private final int[][] residual_unaligned = new int[Jformat.FLAC__MAX_CHANNELS][];
		private boolean do_md5_checking; /* initially gets protected_.md5_checking but is turned off after a seek or if the metadata has a zero MD5 */
		private boolean internal_reset_hack; /* used only during init() so we can call reset to set up the decoder without rewinding the input */
		private boolean is_seeking;
		private final JFLAC__MD5Context md5context = new JFLAC__MD5Context();
		private final byte computed_md5sum[] = new byte[16]; /* this is the sum we computed from the decoded data */
		/* (the rest of these are only used for seeking) */
		private JFLAC__Frame last_frame; /* holds the info of the last frame we seeked to */
		private long first_frame_offset; /* hint to the seek routine of where in the stream the first audio frame starts */
		private long target_sample;
		private int unparseable_frame_count; /* used to tell whether we're decoding a future version of FLAC or just got a bad sync */
		private boolean got_a_frame; /* hack needed in Ogg FLAC seek routine to check when process_single() actually writes a frame */
	//}
	//JFLAC__StreamDecoderPrivate private_;

	/***********************************************************************
	 *
	 * Class constructor/destructor
	 *
	 ***********************************************************************/

	/** Create a new stream decoder instance.  The instance is created with
	 *  default settings; see the individual FLAC__stream_decoder_set_*()
	 *  functions for each setting's default.
	 *
	 * @retval FLAC__StreamDecoder*
	 *    \c NULL if there was an error allocating memory, else the new instance.
	 */
	public JFLAC__StreamDecoder()// FLAC__stream_decoder_new()
	{
		//JFLAC__StreamDecoder decoder;

		//FLAC__ASSERT(sizeof(int) >= 4); /* we want to die right away if this is not true */

		//decoder = new JFLAC__StreamDecoder();

		//decoder.protected_ = new JFLAC__StreamDecoderProtected();

		//decoder.private_ = new JFLAC__StreamDecoderPrivate();

		this.input = new JFLAC__BitReader();

		this.metadata_filter_ids_capacity = 16;
		this.metadata_filter_ids = new byte[(Jformat.FLAC__STREAM_METADATA_APPLICATION_ID_LEN/8) * this.metadata_filter_ids_capacity];

		for( int i = 0; i < Jformat.FLAC__MAX_CHANNELS; i++ ) {
			this.output[i] = null;
			this.residual[i] = null;
			//this.residual_unaligned[i] = null;
		}

		this.output_capacity = 0;
		this.output_channels = 0;
		this.has_seek_table = false;

		final JFLAC__EntropyCodingMethod_PartitionedRiceContents[] prc = this.partitioned_rice_contents;// java
		for( int i = 0; i < Jformat.FLAC__MAX_CHANNELS; i++ ) {
			final JFLAC__EntropyCodingMethod_PartitionedRiceContents c = new JFLAC__EntropyCodingMethod_PartitionedRiceContents();
			c.FLAC__format_entropy_coding_method_partitioned_rice_contents_init();
			prc[i] = c;
		}

		this.file = null;

		set_defaults_();

		this.state = FLAC__STREAM_DECODER_UNINITIALIZED;

		//return decoder;
	}

	/** Free a decoder instance.  Deletes the object pointed to by \a decoder.
	 *
	 * @param decoder  A pointer to an existing decoder.
	 * \assert
	 *    \code decoder != NULL \endcode
	 */
	public final void FLAC__stream_decoder_delete() {
		FLAC__stream_decoder_delete( true );
	}
	public final void FLAC__stream_decoder_delete(final boolean isCloseFile)// java: added isCloseFile
	{
		//FLAC__ASSERT(0 != decoder);
		//FLAC__ASSERT(0 != decoder->protected_);
		//FLAC__ASSERT(0 != decoder->private_);
		//FLAC__ASSERT(0 != decoder->private_->input);

		FLAC__stream_decoder_finish( isCloseFile );

		this.metadata_filter_ids = null;

		this.input = null;

		final JFLAC__EntropyCodingMethod_PartitionedRiceContents[] prc = this.partitioned_rice_contents;// java
		for( int i = 0; i < Jformat.FLAC__MAX_CHANNELS; i++ ) {
			prc[i].FLAC__format_entropy_coding_method_partitioned_rice_contents_clear();
		}

		//decoder.private_ = null;
		//decoder.protected_ = null;
		//decoder = null;
	}


	/***********************************************************************
	 *
	 * Public class method prototypes
	 *
	 ***********************************************************************/

	private final int /*FLAC__StreamDecoderInitStatus*/ init_stream_internal_(
			final JFLAC__StreamDecoderReadCallback read_cb,
			final JFLAC__StreamDecoderSeekCallback seek_cb,
			final JFLAC__StreamDecoderTellCallback tell_cb,
			final JFLAC__StreamDecoderLengthCallback length_cb,
			final JFLAC__StreamDecoderEofCallback eof_cb,
			final JFLAC__StreamDecoderWriteCallback write_cb,
			final JFLAC__StreamDecoderMetadataCallback metadata_cb,
			final JFLAC__StreamDecoderErrorCallback error_cb,
			// final Object data,
			final boolean isogg
		)
	{
		//FLAC__ASSERT(0 != decoder);

		if( this.state != FLAC__STREAM_DECODER_UNINITIALIZED ) {
			return FLAC__STREAM_DECODER_INIT_STATUS_ALREADY_INITIALIZED;
		}

		if( ! Jformat.FLAC__HAS_OGG && isogg ) {
			return FLAC__STREAM_DECODER_INIT_STATUS_UNSUPPORTED_CONTAINER;
		}

		if(
			null == read_cb ||
			null == write_cb ||
			null == error_cb ||
			(seek_cb != null && (null == tell_cb || null == length_cb || null == eof_cb))
		) {
			return FLAC__STREAM_DECODER_INIT_STATUS_INVALID_CALLBACKS;
		}

if( Jformat.FLAC__HAS_OGG ) {
		this.is_ogg = isogg;
		if( isogg && ! this.ogg_decoder_aspect.FLAC__ogg_decoder_aspect_init() ) {
			return this.initstate = FLAC__STREAM_DECODER_INIT_STATUS_ERROR_OPENING_FILE;
		}
}

		/*
		 * get the CPU info and set the function pointers
		 */
		//FLAC__cpu_info( decoder.private_.cpuinfo );
		/* first default to the non-asm routines */
		/*
		decoder.private_.local_lpc_restore_signal = FLAC__lpc_restore_signal;
		decoder.private_.local_lpc_restore_signal_64bit = FLAC__lpc_restore_signal_wide;
		decoder.private_.local_lpc_restore_signal_16bit = FLAC__lpc_restore_signal;
		*/

		/* from here on, errors are fatal */

		if( ! this.input.FLAC__bitreader_init( /* JFLAC__BitReaderReadCallback */ this/*, this*/ ) ) {
			this.state = FLAC__STREAM_DECODER_MEMORY_ALLOCATION_ERROR;
			return FLAC__STREAM_DECODER_INIT_STATUS_MEMORY_ALLOCATION_ERROR;
		}

		this.read_callback = read_cb;
		this.seek_callback = seek_cb;
		this.tell_callback = tell_cb;
		this.length_callback = length_cb;
		this.eof_callback = eof_cb;
		this.write_callback = write_cb;
		this.metadata_callback = metadata_cb;
		this.error_callback = error_cb;
		// this.client_data = data;
		this.fixed_block_size = this.next_fixed_block_size = 0;
		this.samples_decoded = 0;
		this.has_stream_info = false;
		this.cached = false;

		this.do_md5_checking = this.md5_checking;
		this.is_seeking = false;

		this.internal_reset_hack = true; /* so the following reset does not try to rewind the input */
		if( ! FLAC__stream_decoder_reset() ) {
			/* above call sets the state for us */
			return FLAC__STREAM_DECODER_INIT_STATUS_MEMORY_ALLOCATION_ERROR;
		}

		return FLAC__STREAM_DECODER_INIT_STATUS_OK;
	}
	/** Initialize the decoder instance to decode native FLAC streams.
	 *
	 *  This flavor of initialization sets up the decoder to decode from a
	 *  native FLAC stream. I/O is performed via callbacks to the client.
	 *  For decoding from a plain file via filename or open FILE*,
	 *  FLAC__stream_decoder_init_file() and FLAC__stream_decoder_init_FILE()
	 *  provide a simpler interface.
	 *
	 *  This function should be called after FLAC__stream_decoder_new() and
	 *  FLAC__stream_decoder_set_*() but before any of the
	 *  FLAC__stream_decoder_process_*() functions.  Will set and return the
	 *  decoder state, which will be FLAC__STREAM_DECODER_SEARCH_FOR_METADATA
	 *  if initialization succeeded.
	 *
	 * @param  decoder            An uninitialized decoder instance.
	 * @param  read_cb      See FLAC__StreamDecoderReadCallback.  This
	 *                            pointer must not be \c NULL.
	 * @param  seek_cb      See FLAC__StreamDecoderSeekCallback.  This
	 *                            pointer may be \c NULL if seeking is not
	 *                            supported.  If \a seek_callback is not \c NULL then a
	 *                            \a tell_callback, \a length_callback, and \a eof_callback must also be supplied.
	 *                            Alternatively, a dummy seek callback that just
	 *                            returns \c FLAC__STREAM_DECODER_SEEK_STATUS_UNSUPPORTED
	 *                            may also be supplied, all though this is slightly
	 *                            less efficient for the decoder.
	 * @param  tell_cb      See FLAC__StreamDecoderTellCallback.  This
	 *                            pointer may be \c NULL if not supported by the client.  If
	 *                            \a seek_callback is not \c NULL then a
	 *                            \a tell_callback must also be supplied.
	 *                            Alternatively, a dummy tell callback that just
	 *                            returns \c FLAC__STREAM_DECODER_TELL_STATUS_UNSUPPORTED
	 *                            may also be supplied, all though this is slightly
	 *                            less efficient for the decoder.
	 * @param  length_cb    See FLAC__StreamDecoderLengthCallback.  This
	 *                            pointer may be \c NULL if not supported by the client.  If
	 *                            \a seek_callback is not \c NULL then a
	 *                            \a length_callback must also be supplied.
	 *                            Alternatively, a dummy length callback that just
	 *                            returns \c FLAC__STREAM_DECODER_LENGTH_STATUS_UNSUPPORTED
	 *                            may also be supplied, all though this is slightly
	 *                            less efficient for the decoder.
	 * @param  eof_cb       See FLAC__StreamDecoderEofCallback.  This
	 *                            pointer may be \c NULL if not supported by the client.  If
	 *                            \a seek_callback is not \c NULL then a
	 *                            \a eof_callback must also be supplied.
	 *                            Alternatively, a dummy length callback that just
	 *                            returns \c false
	 *                            may also be supplied, all though this is slightly
	 *                            less efficient for the decoder.
	 * @param  write_cb     See FLAC__StreamDecoderWriteCallback.  This
	 *                            pointer must not be \c NULL.
	 * @param  metadata_cb  See FLAC__StreamDecoderMetadataCallback.  This
	 *                            pointer may be \c NULL if the callback is not
	 *                            desired.
	 * @param  error_cb     See FLAC__StreamDecoderErrorCallback.  This
	 *                            pointer must not be \c NULL.
	 * @param  data        This value will be supplied to callbacks in their
	 *                            \a client_data argument.
	 * \assert
	 *    \code decoder != NULL \endcode
	 * @retval FLAC__StreamDecoderInitStatus
	 *    \c FLAC__STREAM_DECODER_INIT_STATUS_OK if initialization was successful;
	 *    see FLAC__StreamDecoderInitStatus for the meanings of other return values.
	 */
	public final int /*FLAC__StreamDecoderInitStatus*/ FLAC__stream_decoder_init_stream(
			final JFLAC__StreamDecoderReadCallback read_cb,
			final JFLAC__StreamDecoderSeekCallback seek_cb,
			final JFLAC__StreamDecoderTellCallback tell_cb,
			final JFLAC__StreamDecoderLengthCallback length_cb,
			final JFLAC__StreamDecoderEofCallback eof_cb,
			final JFLAC__StreamDecoderWriteCallback write_cb,
			final JFLAC__StreamDecoderMetadataCallback metadata_cb,
			final JFLAC__StreamDecoderErrorCallback error_cb//,
			//final Object data
		)
	{
		return init_stream_internal_(
			read_cb,
			seek_cb,
			tell_cb,
			length_cb,
			eof_cb,
			write_cb,
			metadata_cb,
			error_cb,
			//data,
			/*is_ogg=*/false
		);
	}

	/** Initialize the decoder instance to decode Ogg FLAC streams.
	 *
	 *  This flavor of initialization sets up the decoder to decode from a
	 *  FLAC stream in an Ogg container. I/O is performed via callbacks to the
	 *  client.  For decoding from a plain file via filename or open FILE*,
	 *  FLAC__stream_decoder_init_ogg_file() and FLAC__stream_decoder_init_ogg_FILE()
	 *  provide a simpler interface.
	 *
	 *  This function should be called after FLAC__stream_decoder_new() and
	 *  FLAC__stream_decoder_set_*() but before any of the
	 *  FLAC__stream_decoder_process_*() functions.  Will set and return the
	 *  decoder state, which will be FLAC__STREAM_DECODER_SEARCH_FOR_METADATA
	 *  if initialization succeeded.
	 *
	 *  @note Support for Ogg FLAC in the library is optional.  If this
	 *  library has been built without support for Ogg FLAC, this function
	 *  will return \c FLAC__STREAM_DECODER_INIT_STATUS_UNSUPPORTED_CONTAINER.
	 *
	 * @param  decoder            An uninitialized decoder instance.
	 * @param  read_cb      See FLAC__StreamDecoderReadCallback.  This
	 *                            pointer must not be \c NULL.
	 * @param  seek_cb      See FLAC__StreamDecoderSeekCallback.  This
	 *                            pointer may be \c NULL if seeking is not
	 *                            supported.  If \a seek_callback is not \c NULL then a
	 *                            \a tell_callback, \a length_callback, and \a eof_callback must also be supplied.
	 *                            Alternatively, a dummy seek callback that just
	 *                            returns \c FLAC__STREAM_DECODER_SEEK_STATUS_UNSUPPORTED
	 *                            may also be supplied, all though this is slightly
	 *                            less efficient for the decoder.
	 * @param  tell_cb      See FLAC__StreamDecoderTellCallback.  This
	 *                            pointer may be \c NULL if not supported by the client.  If
	 *                            \a seek_callback is not \c NULL then a
	 *                            \a tell_callback must also be supplied.
	 *                            Alternatively, a dummy tell callback that just
	 *                            returns \c FLAC__STREAM_DECODER_TELL_STATUS_UNSUPPORTED
	 *                            may also be supplied, all though this is slightly
	 *                            less efficient for the decoder.
	 * @param  length_cb    See FLAC__StreamDecoderLengthCallback.  This
	 *                            pointer may be \c NULL if not supported by the client.  If
	 *                            \a seek_callback is not \c NULL then a
	 *                            \a length_callback must also be supplied.
	 *                            Alternatively, a dummy length callback that just
	 *                            returns \c FLAC__STREAM_DECODER_LENGTH_STATUS_UNSUPPORTED
	 *                            may also be supplied, all though this is slightly
	 *                            less efficient for the decoder.
	 * @param  eof_cb       See FLAC__StreamDecoderEofCallback.  This
	 *                            pointer may be \c NULL if not supported by the client.  If
	 *                            \a seek_callback is not \c NULL then a
	 *                            \a eof_callback must also be supplied.
	 *                            Alternatively, a dummy length callback that just
	 *                            returns \c false
	 *                            may also be supplied, all though this is slightly
	 *                            less efficient for the decoder.
	 * @param  write_cb     See FLAC__StreamDecoderWriteCallback.  This
	 *                            pointer must not be \c NULL.
	 * @param  metadata_cb  See FLAC__StreamDecoderMetadataCallback.  This
	 *                            pointer may be \c NULL if the callback is not
	 *                            desired.
	 * @param  error_cb     See FLAC__StreamDecoderErrorCallback.  This
	 *                            pointer must not be \c NULL.
	 * @param  data        This value will be supplied to callbacks in their
	 *                            \a client_data argument.
	 * \assert
	 *    \code decoder != NULL \endcode
	 * @retval FLAC__StreamDecoderInitStatus
	 *    \c FLAC__STREAM_DECODER_INIT_STATUS_OK if initialization was successful;
	 *    see FLAC__StreamDecoderInitStatus for the meanings of other return values.
	 */
	public final int /*FLAC_API FLAC__StreamDecoderInitStatus*/ FLAC__stream_decoder_init_ogg_stream(
			final JFLAC__StreamDecoderReadCallback read_cb,
			final JFLAC__StreamDecoderSeekCallback seek_cb,
			final JFLAC__StreamDecoderTellCallback tell_cb,
			final JFLAC__StreamDecoderLengthCallback length_cb,
			final JFLAC__StreamDecoderEofCallback eof_cb,
			final JFLAC__StreamDecoderWriteCallback write_cb,
			final JFLAC__StreamDecoderMetadataCallback metadata_cb,
			final JFLAC__StreamDecoderErrorCallback error_cb//,
			//final Object data
		)
	{
		return init_stream_internal_(
			read_cb,
			seek_cb,
			tell_cb,
			length_cb,
			eof_cb,
			write_cb,
			metadata_cb,
			error_cb,
			//data,
			/*is_ogg=*/true
		);
	}
	private final int /*FLAC__StreamDecoderInitStatus*/ init_FILE_internal_(
			final InputStream f,
			final JFLAC__StreamDecoderWriteCallback write_cb,
			final JFLAC__StreamDecoderMetadataCallback metadata_cb,
			final JFLAC__StreamDecoderErrorCallback error_cb,
			// final Object client_data,
			final boolean isogg
		)
	{
		//FLAC__ASSERT(0 != decoder);
		//FLAC__ASSERT(0 != file);

		if( this.state != FLAC__STREAM_DECODER_UNINITIALIZED ) {
			return this.initstate = FLAC__STREAM_DECODER_INIT_STATUS_ALREADY_INITIALIZED;
		}

		if( null == write_cb || null == error_cb ) {
			return this.initstate = FLAC__STREAM_DECODER_INIT_STATUS_INVALID_CALLBACKS;
		}

		/*
		 * To make sure that our file does not go unclosed after an error, we
		 * must assign the FILE pointer before any further error can occur in
		 * this routine.
		 */

		this.file = f;

		/*return init_stream_internal_(
			this,// file_read_callback_,
			this.file == System.in ? null: this,// file_seek_callback_,
			this.file == System.in ? null: this,// file_tell_callback_,
			this.file == System.in ? null: this,// file_length_callback_,
			this,// file_eof_callback_,
			write_callback,
			metadata_callback,
			error_callback,
			client_data,
			is_ogg
		);*/
		return init_stream_internal_(
			this,// file_read_callback_,
			this.file instanceof RandomAccessInputStream ? this : null,// file_seek_callback_,
			this.file instanceof RandomAccessInputStream ? this : null,// file_tell_callback_,
			this.file instanceof RandomAccessInputStream ? this : null,// file_length_callback_,
			this.file instanceof RandomAccessInputStream ? this : null,// file_eof_callback_,
			write_cb,
			metadata_cb,
			error_cb,
			// client_data,
			isogg
		);
	}

	/** Initialize the decoder instance to decode native FLAC files.
	 *
	 *  This flavor of initialization sets up the decoder to decode from a
	 *  plain native FLAC file.  For non-stdio streams, you must use
	 *  FLAC__stream_decoder_init_stream() and provide callbacks for the I/O.
	 *
	 *  This function should be called after FLAC__stream_decoder_new() and
	 *  FLAC__stream_decoder_set_*() but before any of the
	 *  FLAC__stream_decoder_process_*() functions.  Will set and return the
	 *  decoder state, which will be FLAC__STREAM_DECODER_SEARCH_FOR_METADATA
	 *  if initialization succeeded.
	 *
	 * @param  decoder            An uninitialized decoder instance.
	 * @param  f            An open FLAC file.  The file should have been
	 *                            opened with mode \c "rb" and rewound.  The file
	 *                            becomes owned by the decoder and should not be
	 *                            manipulated by the client while decoding.
	 *                            Unless \a file is \c stdin, it will be closed
	 *                            when FLAC__stream_decoder_finish() is called.
	 *                            Note however that seeking will not work when
	 *                            decoding from \c stdout since it is not seekable.
	 * @param  write_cb     See FLAC__StreamDecoderWriteCallback.  This
	 *                            pointer must not be \c NULL.
	 * @param  metadata_cb  See FLAC__StreamDecoderMetadataCallback.  This
	 *                            pointer may be \c NULL if the callback is not
	 *                            desired.
	 * @param  error_cb     See FLAC__StreamDecoderErrorCallback.  This
	 *                            pointer must not be \c NULL.
	 * @param  client_data        This value will be supplied to callbacks in their
	 *                            \a client_data argument.
	 * \assert
	 *    \code decoder != NULL \endcode
	 *    \code file != NULL \endcode
	 * @retval FLAC__StreamDecoderInitStatus
	 *    \c FLAC__STREAM_DECODER_INIT_STATUS_OK if initialization was successful;
	 *    see FLAC__StreamDecoderInitStatus for the meanings of other return values.
	 */
	public final int /*FLAC__StreamDecoderInitStatus*/ FLAC__stream_decoder_init_FILE(
			final InputStream f,
			final JFLAC__StreamDecoderWriteCallback write_cb,
			final JFLAC__StreamDecoderMetadataCallback metadata_cb,
			final JFLAC__StreamDecoderErrorCallback error_cb//,
			//final Object client_data
		)
	{
		return init_FILE_internal_( f, write_cb, metadata_cb, error_cb,/* client_data,*/ /*is_ogg=*/false );
	}

	/** Initialize the decoder instance to decode Ogg FLAC files.
	 *
	 *  This flavor of initialization sets up the decoder to decode from a
	 *  plain Ogg FLAC file.  For non-stdio streams, you must use
	 *  FLAC__stream_decoder_init_ogg_stream() and provide callbacks for the I/O.
	 *
	 *  This function should be called after FLAC__stream_decoder_new() and
	 *  FLAC__stream_decoder_set_*() but before any of the
	 *  FLAC__stream_decoder_process_*() functions.  Will set and return the
	 *  decoder state, which will be FLAC__STREAM_DECODER_SEARCH_FOR_METADATA
	 *  if initialization succeeded.
	 *
	 *  @note Support for Ogg FLAC in the library is optional.  If this
	 *  library has been built without support for Ogg FLAC, this function
	 *  will return \c FLAC__STREAM_DECODER_INIT_STATUS_UNSUPPORTED_CONTAINER.
	 *
	 * @param  decoder            An uninitialized decoder instance.
	 * @param  f               An open FLAC file.  The file should have been
	 *                            opened with mode \c "rb" and rewound.  The file
	 *                            becomes owned by the decoder and should not be
	 *                            manipulated by the client while decoding.
	 *                            Unless \a file is \c stdin, it will be closed
	 *                            when FLAC__stream_decoder_finish() is called.
	 *                            Note however that seeking will not work when
	 *                            decoding from \c stdout since it is not seekable.
	 * @param  write_cb     See FLAC__StreamDecoderWriteCallback.  This
	 *                            pointer must not be \c NULL.
	 * @param  metadata_cb  See FLAC__StreamDecoderMetadataCallback.  This
	 *                            pointer may be \c NULL if the callback is not
	 *                            desired.
	 * @param  error_cb     See FLAC__StreamDecoderErrorCallback.  This
	 *                            pointer must not be \c NULL.
	 * @param  client_data        This value will be supplied to callbacks in their
	 *                            \a client_data argument.
	 * \assert
	 *    \code decoder != NULL \endcode
	 *    \code file != NULL \endcode
	 * @retval FLAC__StreamDecoderInitStatus
	 *    \c FLAC__STREAM_DECODER_INIT_STATUS_OK if initialization was successful;
	 *    see FLAC__StreamDecoderInitStatus for the meanings of other return values.
	 */
	public final int /*FLAC__StreamDecoderInitStatus*/ FLAC__stream_decoder_init_ogg_FILE(
			final InputStream f,
			final JFLAC__StreamDecoderWriteCallback write_cb,
			final JFLAC__StreamDecoderMetadataCallback metadata_cb,
			final JFLAC__StreamDecoderErrorCallback error_cb// ,
			// final Object client_data
	)
	{
		return init_FILE_internal_( f, write_cb, metadata_cb, error_cb,/* client_data,*/ /*is_ogg=*/true );
	}

	private final int /*FLAC__StreamDecoderInitStatus*/ init_file_internal_(
			final String filename,
			final JFLAC__StreamDecoderWriteCallback write_cb,
			final JFLAC__StreamDecoderMetadataCallback metadata_cb,
			final JFLAC__StreamDecoderErrorCallback error_cb,
			// final Object client_data,
			final boolean isogg
		)
	{
		//FLAC__ASSERT(0 != decoder);

		/*
		 * To make sure that our file does not go unclosed after an error, we
		 * have to do the same entrance checks here that are later performed
		 * in FLAC__stream_decoder_init_FILE() before the FILE* is assigned.
		 */
		if( this.state != FLAC__STREAM_DECODER_UNINITIALIZED ) {
			return this.initstate = FLAC__STREAM_DECODER_INIT_STATUS_ALREADY_INITIALIZED;
		}

		if( null == write_cb || null == error_cb ) {
			return this.initstate = FLAC__STREAM_DECODER_INIT_STATUS_INVALID_CALLBACKS;
		}

		try {
			final InputStream f = filename != null ? new RandomAccessInputStream( filename ) : System.in;

			return init_FILE_internal_( f, write_cb, metadata_cb, error_cb,/* client_data,*/ isogg );
		} catch(final FileNotFoundException e) {
			return FLAC__STREAM_DECODER_INIT_STATUS_ERROR_OPENING_FILE;
		}
	}

	/** Initialize the decoder instance to decode native FLAC files.
	 *
	 *  This flavor of initialization sets up the decoder to decode from a plain
	 *  native FLAC file.  If POSIX fopen() semantics are not sufficient, (for
	 *  example, with Unicode filenames on Windows), you must use
	 *  FLAC__stream_decoder_init_FILE(), or FLAC__stream_decoder_init_stream()
	 *  and provide callbacks for the I/O.
	 *
	 *  This function should be called after FLAC__stream_decoder_new() and
	 *  FLAC__stream_decoder_set_*() but before any of the
	 *  FLAC__stream_decoder_process_*() functions.  Will set and return the
	 *  decoder state, which will be FLAC__STREAM_DECODER_SEARCH_FOR_METADATA
	 *  if initialization succeeded.
	 *
	 * @param  decoder            An uninitialized decoder instance.
	 * @param  filename           The name of the file to decode from.  The file will
	 *                            be opened with fopen().  Use \c NULL to decode from
	 *                            \c stdin.  Note that \c stdin is not seekable.
	 * @param  write_cb     See FLAC__StreamDecoderWriteCallback.  This
	 *                            pointer must not be \c NULL.
	 * @param  metadata_cb  See FLAC__StreamDecoderMetadataCallback.  This
	 *                            pointer may be \c NULL if the callback is not
	 *                            desired.
	 * @param  error_cb     See FLAC__StreamDecoderErrorCallback.  This
	 *                            pointer must not be \c NULL.
	 * @param  client_data        This value will be supplied to callbacks in their
	 *                            \a client_data argument.
	 * \assert
	 *    \code decoder != NULL \endcode
	 * @retval FLAC__StreamDecoderInitStatus
	 *    \c FLAC__STREAM_DECODER_INIT_STATUS_OK if initialization was successful;
	 *    see FLAC__StreamDecoderInitStatus for the meanings of other return values.
	 */
	public final int /*FLAC__StreamDecoderInitStatus*/ FLAC__stream_decoder_init_file(
			final String filename,
			final JFLAC__StreamDecoderWriteCallback write_cb,
			final JFLAC__StreamDecoderMetadataCallback metadata_cb,
			final JFLAC__StreamDecoderErrorCallback error_cb//,
			// final Object client_data
		)
	{
		return init_file_internal_( filename, write_cb, metadata_cb, error_cb,/* client_data,*/ /*is_ogg=*/false );
	}

	/** Initialize the decoder instance to decode Ogg FLAC files.
	 *
	 *  This flavor of initialization sets up the decoder to decode from a plain
	 *  Ogg FLAC file.  If POSIX fopen() semantics are not sufficient, (for
	 *  example, with Unicode filenames on Windows), you must use
	 *  FLAC__stream_decoder_init_ogg_FILE(), or FLAC__stream_decoder_init_ogg_stream()
	 *  and provide callbacks for the I/O.
	 *
	 *  This function should be called after FLAC__stream_decoder_new() and
	 *  FLAC__stream_decoder_set_*() but before any of the
	 *  FLAC__stream_decoder_process_*() functions.  Will set and return the
	 *  decoder state, which will be FLAC__STREAM_DECODER_SEARCH_FOR_METADATA
	 *  if initialization succeeded.
	 *
	 *  @note Support for Ogg FLAC in the library is optional.  If this
	 *  library has been built without support for Ogg FLAC, this function
	 *  will return \c FLAC__STREAM_DECODER_INIT_STATUS_UNSUPPORTED_CONTAINER.
	 *
	 * @param  decoder            An uninitialized decoder instance.
	 * @param  filename           The name of the file to decode from.  The file will
	 *                            be opened with fopen().  Use \c NULL to decode from
	 *                            \c stdin.  Note that \c stdin is not seekable.
	 * @param  write_cb     See FLAC__StreamDecoderWriteCallback.  This
	 *                            pointer must not be \c NULL.
	 * @param  metadata_cb  See FLAC__StreamDecoderMetadataCallback.  This
	 *                            pointer may be \c NULL if the callback is not
	 *                            desired.
	 * @param  error_cb     See FLAC__StreamDecoderErrorCallback.  This
	 *                            pointer must not be \c NULL.
	 * @param  client_data        This value will be supplied to callbacks in their
	 *                            \a client_data argument.
	 * \assert
	 *    \code decoder != NULL \endcode
	 * @retval FLAC__StreamDecoderInitStatus
	 *    \c FLAC__STREAM_DECODER_INIT_STATUS_OK if initialization was successful;
	 *    see FLAC__StreamDecoderInitStatus for the meanings of other return values.
	 */
	public final int /*FLAC__StreamDecoderInitStatus*/ FLAC__stream_decoder_init_ogg_file(
			final String filename,
			final JFLAC__StreamDecoderWriteCallback write_cb,
			final JFLAC__StreamDecoderMetadataCallback metadata_cb,
			final JFLAC__StreamDecoderErrorCallback error_cb//,
			//final Object client_data
		)
	{
		return init_file_internal_( filename, write_cb, metadata_cb, error_cb,/* client_data,*/ /*is_ogg=*/false );
	}

	/** Finish the decoding process.
	 *  Flushes the decoding buffer, releases resources, resets the decoder
	 *  settings to their defaults, and returns the decoder state to
	 *  FLAC__STREAM_DECODER_UNINITIALIZED.
	 *
	 *  In the event of a prematurely-terminated decode, it is not strictly
	 *  necessary to call this immediately before FLAC__stream_decoder_delete()
	 *  but it is good practice to match every FLAC__stream_decoder_init_*()
	 *  with a FLAC__stream_decoder_finish().
	 *
	 * @param  decoder  An uninitialized decoder instance.
	 * \assert
	 *    \code decoder != NULL \endcode
	 * @retval FLAC__bool
	 *    \c false if MD5 checking is on AND a STREAMINFO block was available
	 *    AND the MD5 signature in the STREAMINFO block was non-zero AND the
	 *    signature does not match the one computed by the decoder; else
	 *    \c true.
	 */
	public final boolean FLAC__stream_decoder_finish() {
		return FLAC__stream_decoder_finish( true );
	}
	public final boolean FLAC__stream_decoder_finish(final boolean isCloseFile)// java: added isCloseFile
	{
		//FLAC__ASSERT(0 != decoder);
		//FLAC__ASSERT(0 != decoder->private_);
		//FLAC__ASSERT(0 != decoder->protected_);

		if( this.state == FLAC__STREAM_DECODER_UNINITIALIZED ) {
			return true;
		}

		/* see the comment in FLAC__stream_decoder_reset() as to why we
		 * always call FLAC__MD5Final()
		 */
		this.md5context.FLAC__MD5Final( this.computed_md5sum );

		this.seek_table.points = null;
		this.has_seek_table = false;

		this.input = null;
		for( int i = 0; i < Jformat.FLAC__MAX_CHANNELS; i++ ) {
			/* WATCHOUT:
			 * FLAC__lpc_restore_signal_asm_ia32_mmx() requires that the
			 * output arrays have a buffer of up to 3 zeroes in front
			 * (at negative indices) for alignment purposes; we use 4
			 * to keep the data well-aligned.
			 */
			if( null != this.output[i] ) {
				this.output[i] = null;
			}
			if( null != this.residual[i] /*this.residual_unaligned[i]*/ ) {
				//this.residual_unaligned[i] = null;
				this.residual[i] = null;
			}
		}
		this.output_capacity = 0;
		this.output_channels = 0;

if( Jformat.FLAC__HAS_OGG ) {
		if( this.is_ogg ) {
			this.ogg_decoder_aspect.FLAC__ogg_decoder_aspect_finish();
		}
}

		if( isCloseFile && null != this.file ) {
			if( this.file != System.in ) {
				try { this.file.close(); } catch( final IOException e ) {}
			}
			this.file = null;
		}
		boolean md5_failed = false;
		if( this.do_md5_checking ) {
			final byte[] md5sum = this.stream_info.md5sum;
			final byte[] comuted_md5 = this.computed_md5sum;
			int i = 16;
			do{
				i--;
				if( md5sum[i] != comuted_md5[i] ) {
					md5_failed = true;
				}
			} while( i > 0 );
		}
		this.is_seeking = false;

		set_defaults_();

		this.state = FLAC__STREAM_DECODER_UNINITIALIZED;

		return ! md5_failed;
	}
	/** Set the serial number for the FLAC stream within the Ogg container.
	 *  The default behavior is to use the serial number of the first Ogg
	 *  page.  Setting a serial number here will explicitly specify which
	 *  stream is to be decoded.
	 *
	 * @note
	 * This does not need to be set for native FLAC decoding.
	 *
	 * \default \c use serial number of first page
	 * @param  decoder        A decoder instance to set.
	 * @param  serial_number  See above.
	 * \assert
	 *    \code decoder != NULL \endcode
	 * @retval FLAC__bool
	 *    \c false if the decoder is already initialized, else \c true.
	 */
	public final boolean FLAC__stream_decoder_set_ogg_serial_number(final int serial_number)
	{
		//FLAC__ASSERT(0 != decoder);
		//FLAC__ASSERT(0 != decoder->private_);
		//FLAC__ASSERT(0 != decoder->protected_);
		if( this.state != FLAC__STREAM_DECODER_UNINITIALIZED ) {
			return false;
		}
if( Jformat.FLAC__HAS_OGG ) {
		/* can't check decoder.is_ogg since that's not set until init time */
		this.ogg_decoder_aspect.FLAC__ogg_decoder_aspect_set_serial_number( serial_number );
		return true;
} else {
		//(void)serial_number;
		return false;
}
	}
	/** Set the "MD5 signature checking" flag.  If \c true, the decoder will
	 *  compute the MD5 signature of the unencoded audio data while decoding
	 *  and compare it to the signature from the STREAMINFO block, if it
	 *  exists, during FLAC__stream_decoder_finish().
	 *
	 *  MD5 signature checking will be turned off (until the next
	 *  FLAC__stream_decoder_reset()) if there is no signature in the
	 *  STREAMINFO block or when a seek is attempted.
	 *
	 *  Clients that do not use the MD5 check should leave this off to speed
	 *  up decoding.
	 *
	 * \default \c false
	 * @param  decoder  A decoder instance to set.
	 * @param  value    Flag value (see above).
	 * \assert
	 *    \code decoder != NULL \endcode
	 * @retval FLAC__bool
	 *    \c false if the decoder is already initialized, else \c true.
	 */
	public final boolean FLAC__stream_decoder_set_md5_checking(final boolean value)
	{
		//FLAC__ASSERT(0 != decoder);
		//FLAC__ASSERT(0 != decoder->protected_);
		if( this.state != FLAC__STREAM_DECODER_UNINITIALIZED ) {
			return false;
		}
		this.md5_checking = value;
		return true;
	}
	/** Direct the decoder to pass on all metadata blocks of type \a type.
	 *
	 * \default By default, only the \c STREAMINFO block is returned via the
	 *          metadata callback.
	 * @param  decoder  A decoder instance to set.
	 * @param  type     See above.
	 * \assert
	 *    \code decoder != NULL \endcode
	 *    \a type is valid
	 * @retval FLAC__bool
	 *    \c false if the decoder is already initialized, else \c true.
	 */
	public final boolean FLAC__stream_decoder_set_metadata_respond(final int type)
	{
		//FLAC__ASSERT(0 != decoder);
		//FLAC__ASSERT(0 != decoder->private_);
		//FLAC__ASSERT(0 != decoder->protected_);
		//FLAC__ASSERT((unsigned)type <= FLAC__MAX_METADATA_TYPE_CODE);
		/* double protection */
		if( type > Jformat.FLAC__MAX_METADATA_TYPE_CODE ) {
			return false;
		}
		if( this.state != FLAC__STREAM_DECODER_UNINITIALIZED ) {
			return false;
		}
		this.metadata_filter[type] = true;
		if( type == Jformat.FLAC__METADATA_TYPE_APPLICATION ) {
			this.metadata_filter_ids_count = 0;
		}
		return true;
	}

	/** Direct the decoder to pass on all APPLICATION metadata blocks of the
	 *  given \a id.
	 *
	 * \default By default, only the \c STREAMINFO block is returned via the
	 *          metadata callback.
	 * @param  decoder  A decoder instance to set.
	 * @param  id       See above.
	 * \assert
	 *    \code decoder != NULL \endcode
	 *    \code id != NULL \endcode
	 * @retval FLAC__bool
	 *    \c false if the decoder is already initialized, else \c true.
	 */
	public final boolean FLAC__stream_decoder_set_metadata_respond_application(final byte id[])
	{
		//FLAC__ASSERT(0 != decoder);
		//FLAC__ASSERT(0 != decoder->private_);
		//FLAC__ASSERT(0 != decoder->protected_);
		//FLAC__ASSERT(0 != id);
		if( this.state != FLAC__STREAM_DECODER_UNINITIALIZED ) {
			return false;
		}

		if( this.metadata_filter[Jformat.FLAC__METADATA_TYPE_APPLICATION] ) {
			return true;
		}

		if( this.metadata_filter_ids_count == this.metadata_filter_ids_capacity ) {
			this.metadata_filter_ids = Arrays.copyOf( this.metadata_filter_ids, this.metadata_filter_ids_capacity * /*times*/2);
			this.metadata_filter_ids_capacity <<= 1;
		}

		System.arraycopy( id, 0, this.metadata_filter_ids, this.metadata_filter_ids_count * (Jformat.FLAC__STREAM_METADATA_APPLICATION_ID_LEN / 8), (Jformat.FLAC__STREAM_METADATA_APPLICATION_ID_LEN / 8) );
		this.metadata_filter_ids_count++;

		return true;
	}
	/** Direct the decoder to pass on all metadata blocks of any type.
	 *
	 * \default By default, only the \c STREAMINFO block is returned via the
	 *          metadata callback.
	 * @param  decoder  A decoder instance to set.
	 * \assert
	 *    \code decoder != NULL \endcode
	 * @retval FLAC__bool
	 *    \c false if the decoder is already initialized, else \c true.
	 */
	public final boolean FLAC__stream_decoder_set_metadata_respond_all()
	{
		//FLAC__ASSERT(0 != decoder);
		//FLAC__ASSERT(0 != decoder->private_);
		//FLAC__ASSERT(0 != decoder->protected_);
		if( this.state != FLAC__STREAM_DECODER_UNINITIALIZED ) {
			return false;
		}
		final boolean[] mf = metadata_filter;// java
		for( int i = 0, length = this.metadata_filter.length; i < length; i++ ) {
			mf[i] = true;
		}
		this.metadata_filter_ids_count = 0;
		return true;
	}
	/** Direct the decoder to filter out all metadata blocks of type \a type.
	 *
	 * \default By default, only the \c STREAMINFO block is returned via the
	 *          metadata callback.
	 * @param  decoder  A decoder instance to set.
	 * @param  type     See above.
	 * \assert
	 *    \code decoder != NULL \endcode
	 *    \a type is valid
	 * @retval FLAC__bool
	 *    \c false if the decoder is already initialized, else \c true.
	 */
	public final boolean FLAC__stream_decoder_set_metadata_ignore(final int type)
	{
		//FLAC__ASSERT(0 != decoder);
		//FLAC__ASSERT(0 != decoder->private_);
		//FLAC__ASSERT(0 != decoder->protected_);
		//FLAC__ASSERT((unsigned)type <= FLAC__MAX_METADATA_TYPE_CODE);
		/* double protection */
		if( type > Jformat.FLAC__MAX_METADATA_TYPE_CODE ) {
			return false;
		}
		if( this.state != FLAC__STREAM_DECODER_UNINITIALIZED ) {
			return false;
		}
		this.metadata_filter[type] = false;
		if( type == Jformat.FLAC__METADATA_TYPE_APPLICATION ) {
			this.metadata_filter_ids_count = 0;
		}
		return true;
	}
	/** Direct the decoder to filter out all APPLICATION metadata blocks of
	 *  the given \a id.
	 *
	 * \default By default, only the \c STREAMINFO block is returned via the
	 *          metadata callback.
	 * @param  decoder  A decoder instance to set.
	 * @param  id       See above.
	 * \assert
	 *    \code decoder != NULL \endcode
	 *    \code id != NULL \endcode
	 * @retval FLAC__bool
	 *    \c false if the decoder is already initialized, else \c true.
	 */
	public final boolean FLAC__stream_decoder_set_metadata_ignore_application(final byte id[])
	{// FIXME what difference with FLAC__stream_decoder_set_metadata_respond_application?
		//FLAC__ASSERT(0 != decoder);
		//FLAC__ASSERT(0 != decoder->private_);
		//FLAC__ASSERT(0 != decoder->protected_);
		//FLAC__ASSERT(0 != id);
		if( this.state != FLAC__STREAM_DECODER_UNINITIALIZED ) {
			return false;
		}

		if( ! this.metadata_filter[Jformat.FLAC__METADATA_TYPE_APPLICATION] ) {
			return true;
		}

		if( this.metadata_filter_ids_count == this.metadata_filter_ids_capacity ) {
			this.metadata_filter_ids = Arrays.copyOf( this.metadata_filter_ids, this.metadata_filter_ids_capacity * /*times*/2 );
			this.metadata_filter_ids_capacity <<= 1;
		}

		System.arraycopy( id, 0, this.metadata_filter_ids, this.metadata_filter_ids_count * this.metadata_filter_ids_count * (Jformat.FLAC__STREAM_METADATA_APPLICATION_ID_LEN/8), (Jformat.FLAC__STREAM_METADATA_APPLICATION_ID_LEN/8) );
		this.metadata_filter_ids_count++;

		return true;
	}
	/** Direct the decoder to filter out all metadata blocks of any type.
	 *
	 * \default By default, only the \c STREAMINFO block is returned via the
	 *          metadata callback.
	 * @param  decoder  A decoder instance to set.
	 * \assert
	 *    \code decoder != NULL \endcode
	 * @retval FLAC__bool
	 *    \c false if the decoder is already initialized, else \c true.
	 */
	public final boolean FLAC__stream_decoder_set_metadata_ignore_all()
	{
		//FLAC__ASSERT(0 != decoder);
		//FLAC__ASSERT(0 != decoder->private_);
		//FLAC__ASSERT(0 != decoder->protected_);
		if( this.state != FLAC__STREAM_DECODER_UNINITIALIZED ) {
			return false;
		}
		Arrays.fill( this.metadata_filter, 0, this.metadata_filter.length, false );
		this.metadata_filter_ids_count = 0;
		return true;
	}
	/** Get the current decoder state.
	 *
	 * @param  decoder  A decoder instance to query.
	 * \assert
	 *    \code decoder != NULL \endcode
	 * @retval FLAC__StreamDecoderState
	 *    The current decoder state.
	 */
	public final int FLAC__stream_decoder_get_state()
	{
		//FLAC__ASSERT(0 != decoder);
		//FLAC__ASSERT(0 != decoder->protected_);
		return this.state;
	}
	/** Get the current decoder state as a C string.
	 *
	 * @param  decoder  A decoder instance to query.
	 * \assert
	 *    \code decoder != NULL \endcode
	 * @retval const char *
	 *    The decoder state as a C string.  Do not modify the contents.
	 */
	public final String FLAC__stream_decoder_get_resolved_state_string()
	{
		return FLAC__StreamDecoderStateString[this.state];
	}
	/** Get the "MD5 signature checking" flag.
	 *  This is the value of the setting, not whether or not the decoder is
	 *  currently checking the MD5 (remember, it can be turned off automatically
	 *  by a seek).  When the decoder is reset the flag will be restored to the
	 *  value returned by this function.
	 *
	 * @param  decoder  A decoder instance to query.
	 * \assert
	 *    \code decoder != NULL \endcode
	 * @retval FLAC__bool
	 *    See above.
	 */
	public final boolean FLAC__stream_decoder_get_md5_checking()
	{
		//FLAC__ASSERT(0 != decoder);
		//FLAC__ASSERT(0 != decoder->protected_);
		return this.md5_checking;
	}
	/** Get the total number of samples in the stream being decoded.
	 *  Will only be valid after decoding has started and will contain the
	 *  value from the \c STREAMINFO block.  A value of \c 0 means "unknown".
	 *
	 * @param  decoder  A decoder instance to query.
	 * \assert
	 *    \code decoder != NULL \endcode
	 * @retval unsigned
	 *    See above.
	 */
	public final long FLAC__stream_decoder_get_total_samples()
	{
		//FLAC__ASSERT(0 != decoder);
		//FLAC__ASSERT(0 != decoder->protected_);
		if( this.has_stream_info ) {
			return this.stream_info.total_samples;
		}
		return 0;
	}
	/** Get the current number of channels in the stream being decoded.
	 *  Will only be valid after decoding has started and will contain the
	 *  value from the most recently decoded frame header.
	 *
	 * @param  decoder  A decoder instance to query.
	 * \assert
	 *    \code decoder != NULL \endcode
	 * @retval unsigned
	 *    See above.
	 */
	public final int FLAC__stream_decoder_get_channels()
	{
		//FLAC__ASSERT(0 != decoder);
		//FLAC__ASSERT(0 != decoder->protected_);
		return this.channels;
	}
	/** Get the current channel assignment in the stream being decoded.
	 *  Will only be valid after decoding has started and will contain the
	 *  value from the most recently decoded frame header.
	 *
	 * @param  decoder  A decoder instance to query.
	 * \assert
	 *    \code decoder != NULL \endcode
	 * @retval FLAC__ChannelAssignment
	 *    See above.
	 */
	public final int FLAC__stream_decoder_get_channel_assignment()
	{
		//FLAC__ASSERT(0 != decoder);
		//FLAC__ASSERT(0 != decoder->protected_);
		return this.channel_assignment;
	}
	/** Get the current sample resolution in the stream being decoded.
	 *  Will only be valid after decoding has started and will contain the
	 *  value from the most recently decoded frame header.
	 *
	 * @param  decoder  A decoder instance to query.
	 * \assert
	 *    \code decoder != NULL \endcode
	 * @retval unsigned
	 *    See above.
	 */
	public final int FLAC__stream_decoder_get_bits_per_sample()
	{
		//FLAC__ASSERT(0 != decoder);
		//FLAC__ASSERT(0 != decoder->protected_);
		return this.bits_per_sample;
	}
	/** Get the current sample rate in Hz of the stream being decoded.
	 *  Will only be valid after decoding has started and will contain the
	 *  value from the most recently decoded frame header.
	 *
	 * @param  decoder  A decoder instance to query.
	 * \assert
	 *    \code decoder != NULL \endcode
	 * @retval unsigned
	 *    See above.
	 */
	public final int FLAC__stream_decoder_get_sample_rate()
	{
		//FLAC__ASSERT(0 != decoder);
		//FLAC__ASSERT(0 != decoder->protected_);
		return this.sample_rate;
	}
	/** Get the current blocksize of the stream being decoded.
	 *  Will only be valid after decoding has started and will contain the
	 *  value from the most recently decoded frame header.
	 *
	 * @param  decoder  A decoder instance to query.
	 * \assert
	 *    \code decoder != NULL \endcode
	 * @retval unsigned
	 *    See above.
	 */
	public final int FLAC__stream_decoder_get_blocksize()
	{
		//FLAC__ASSERT(0 != decoder);
		//FLAC__ASSERT(0 != decoder->protected_);
		return this.blocksize;
	}
	/** Returns the decoder's current read position within the stream.
	 *  The position is the byte offset from the start of the stream.
	 *  Bytes before this position have been fully decoded.  Note that
	 *  there may still be undecoded bytes in the decoder's read FIFO.
	 *  The returned position is correct even after a seek.
	 *
	 *  \warning This function currently only works for native FLAC,
	 *           not Ogg FLAC streams.
	 *
	 * @param  decoder   A decoder instance to query.
	 * @param  position  Address at which to return the desired position.
	 * \assert
	 *    \code decoder != NULL \endcode
	 *    \code position != NULL \endcode
	 * @retval FLAC__bool
	 *    \c true if successful, \c false if the stream is not native FLAC,
	 *    or there was an error from the 'tell' callback or it returned
	 *    \c FLAC__STREAM_DECODER_TELL_STATUS_UNSUPPORTED.
	 */
	public final long FLAC__stream_decoder_get_decode_position() throws IOException, UnsupportedOperationException// java: changed. if an error, throws exception
	{
		//FLAC__ASSERT(0 != decoder);
		//FLAC__ASSERT(0 != decoder->private_);
		//FLAC__ASSERT(0 != position);

		if( Jformat.FLAC__HAS_OGG && this.is_ogg ) {
			throw new IOException();// return false;
		}
		if( null == this.tell_callback ) {
			throw new IOException();// return false;
		}

		long position = this.tell_callback.dec_tell_callback( this/*, this.client_data*/ );
		/* should never happen since all FLAC frames and metadata blocks are byte aligned, but check just in case */
		if( ! this.input.FLAC__bitreader_is_consumed_byte_aligned() )
		 {
			throw new IOException();// return false;
		}

		//FLAC__ASSERT(*position >= FLAC__stream_decoder_get_input_bytes_unconsumed(decoder));
		position -= (long)FLAC__stream_decoder_get_input_bytes_unconsumed();
		return position;
	}


	/** Flush the stream input.
	 *  The decoder's input buffer will be cleared and the state set to
	 *  \c FLAC__STREAM_DECODER_SEARCH_FOR_FRAME_SYNC.  This will also turn
	 *  off MD5 checking.
	 *
	 * @param  decoder  A decoder instance.
	 * \assert
	 *    \code decoder != NULL \endcode
	 * @retval FLAC__bool
	 *    \c true if successful, else \c false if a memory allocation
	 *    error occurs (in which case the state will be set to
	 *    \c FLAC__STREAM_DECODER_MEMORY_ALLOCATION_ERROR).
	 */
	public final boolean FLAC__stream_decoder_flush()
	{
		//FLAC__ASSERT(0 != decoder);
		//FLAC__ASSERT(0 != decoder->private_);
		//FLAC__ASSERT(0 != decoder->protected_);

		if( ! this.internal_reset_hack && this.state == FLAC__STREAM_DECODER_UNINITIALIZED ) {
			return false;
		}

		this.samples_decoded = 0;
		this.do_md5_checking = false;

if( Jformat.FLAC__HAS_OGG ) {
		if( this.is_ogg ) {
			this.ogg_decoder_aspect.FLAC__ogg_decoder_aspect_flush();
		}
}

		if( ! this.input.FLAC__bitreader_clear() ) {
			this.state = FLAC__STREAM_DECODER_MEMORY_ALLOCATION_ERROR;
			return false;
		}
		this.state = FLAC__STREAM_DECODER_SEARCH_FOR_FRAME_SYNC;

		return true;
	}
	/** Reset the decoding process.
	 *  The decoder's input buffer will be cleared and the state set to
	 *  \c FLAC__STREAM_DECODER_SEARCH_FOR_METADATA.  This is similar to
	 *  FLAC__stream_decoder_finish() except that the settings are
	 *  preserved; there is no need to call FLAC__stream_decoder_init_*()
	 *  before decoding again.  MD5 checking will be restored to its original
	 *  setting.
	 *
	 *  If the decoder is seekable, or was initialized with
	 *  FLAC__stream_decoder_init*_FILE() or FLAC__stream_decoder_init*_file(),
	 *  the decoder will also attempt to seek to the beginning of the file.
	 *  If this rewind fails, this function will return \c false.  It follows
	 *  that FLAC__stream_decoder_reset() cannot be used when decoding from
	 *  \c stdin.
	 *
	 *  If the decoder was initialized with FLAC__stream_encoder_init*_stream()
	 *  and is not seekable (i.e. no seek callback was provided or the seek
	 *  callback returns \c FLAC__STREAM_DECODER_SEEK_STATUS_UNSUPPORTED), it
	 *  is the duty of the client to start feeding data from the beginning of
	 *  the stream on the next FLAC__stream_decoder_process() or
	 *  FLAC__stream_decoder_process_interleaved() call.
	 *
	 * @param  decoder  A decoder instance.
	 * \assert
	 *    \code decoder != NULL \endcode
	 * @retval FLAC__bool
	 *    \c true if successful, else \c false if a memory allocation occurs
	 *    (in which case the state will be set to
	 *    \c FLAC__STREAM_DECODER_MEMORY_ALLOCATION_ERROR) or a seek error
	 *    occurs (the state will be unchanged).
	 */
	public final boolean FLAC__stream_decoder_reset()
	{
		//FLAC__ASSERT(0 != decoder);
		//FLAC__ASSERT(0 != decoder->private_);
		//FLAC__ASSERT(0 != decoder->protected_);

		if( ! FLAC__stream_decoder_flush() ) {
			/* above call sets the state for us */
			return false;
		}

if( Jformat.FLAC__HAS_OGG ) {
		/*@@@ could go in !internal_reset_hack block below */
		if( this.is_ogg ) {
			this.ogg_decoder_aspect.FLAC__ogg_decoder_aspect_reset();
		}
}

		/* Rewind if necessary.  If FLAC__stream_decoder_init() is calling us,
		 * (internal_reset_hack) don't try to rewind since we are already at
		 * the beginning of the stream and don't want to fail if the input is
		 * not seekable.
		 */
		if( ! this.internal_reset_hack ) {
			if( this.file == System.in ) {
				return false; /* can't rewind stdin, reset fails */
			}
			if( this.seek_callback != null && this.seek_callback.dec_seek_callback( this, 0/*, this.client_data*/ ) == FLAC__STREAM_DECODER_SEEK_STATUS_ERROR ) {
				return false; /* seekable and seek fails, reset fails */
			}
		} else {
			this.internal_reset_hack = false;
		}

		this.state = FLAC__STREAM_DECODER_SEARCH_FOR_METADATA;

		this.has_stream_info = false;

		this.seek_table.points = null;
		this.has_seek_table = false;

		this.do_md5_checking = this.md5_checking;
		/*
		 * This goes in reset() and not flush() because according to the spec, a
		 * fixed-blocksize stream must stay that way through the whole stream.
		 */
		this.fixed_block_size = this.next_fixed_block_size = 0;

		/* We initialize the FLAC__MD5Context even though we may never use it.  This
		 * is because md5 checking may be turned on to start and then turned off if
		 * a seek occurs.  So we init the context here and finalize it in
		 * FLAC__stream_decoder_finish() to make sure things are always cleaned up
		 * properly.
		 */
		this.md5context.FLAC__MD5Init();

		this.first_frame_offset = 0;
		this.unparseable_frame_count = 0;

		return true;
	}

	/** Decode one metadata block or audio frame.
	 *  This version instructs the decoder to decode a either a single metadata
	 *  block or a single frame and stop, unless the callbacks return a fatal
	 *  error or the read callback returns
	 *  \c FLAC__STREAM_DECODER_READ_STATUS_END_OF_STREAM.
	 * <p>
	 *  As the decoder needs more input it will call the read callback.
	 *  Depending on what was decoded, the metadata or write callback will be
	 *  called with the decoded metadata block or audio frame.
	 * <p>
	 *  Unless there is a fatal read error or end of stream, this function
	 *  will return once one whole frame is decoded.  In other words, if the
	 *  stream is not synchronized or points to a corrupt frame header, the
	 *  decoder will continue to try and resync until it gets to a valid
	 *  frame, then decode one frame, then return.  If the decoder points to
	 *  a frame whose frame CRC in the frame footer does not match the
	 *  computed frame CRC, this function will issue a
	 *  FLAC__STREAM_DECODER_ERROR_STATUS_FRAME_CRC_MISMATCH error to the
	 *  error callback, and return, having decoded one complete, although
	 *  corrupt, frame.  (Such corrupted frames are sent as silence of the
	 *  correct length to the write callback.)
	 *
	 * @param  decoder  An initialized decoder instance.
	 * \assert
	 *    \code decoder != NULL \endcode
	 * @retval FLAC__bool
	 *    \c false if any fatal read, write, or memory allocation error
	 *    occurred (meaning decoding must stop), else \c true; for more
	 *    information about the decoder, check the decoder state with
	 *    FLAC__stream_decoder_get_state().
	 */
	public final boolean FLAC__stream_decoder_process_single()
	{
		//boolean got_a_frame;// java: read_frame_ returns got_a_frame
		//FLAC__ASSERT(0 != decoder);
		//FLAC__ASSERT(0 != decoder->protected_);

		while( true ) {
			switch( this.state ) {
				case FLAC__STREAM_DECODER_SEARCH_FOR_METADATA:
					try {
						find_metadata_(); /* above function sets the status for us */
					} catch(final IOException e) {
						return false;
					}
					break;
				case FLAC__STREAM_DECODER_READ_METADATA:
					return read_metadata_();/* above function sets the status for us */// FIXME why need if else block?
				case FLAC__STREAM_DECODER_SEARCH_FOR_FRAME_SYNC:
					try{ frame_sync_(); } catch(final IOException e) {
						return true;// FIXME is this correct? return true; /* above function sets the status for us */
					}
					break;
				case FLAC__STREAM_DECODER_READ_FRAME:
					//if( ! read_frame_( decoder, &got_a_frame, /*do_full_decode=*/true ) )
					//	return false; /* above function sets the status for us */
					//if( got_a_frame )
					//	return true; /* above function sets the status for us */
					try {
						if( read_frame_( /* &got_a_frame,*/ /*do_full_decode=*/true ) ) {
							return true;
						}
					} catch(final IOException e) {
						return false;
					}
					break;
				case FLAC__STREAM_DECODER_END_OF_STREAM:
				case FLAC__STREAM_DECODER_ABORTED:
					return true;
				default:
					//FLAC__ASSERT(0);
					return false;
			}
		}
	}
	/** Decode until the end of the metadata.
	 *  This version instructs the decoder to decode from the current position
	 *  and continue until all the metadata has been read, or until the
	 *  callbacks return a fatal error or the read callback returns
	 *  \c FLAC__STREAM_DECODER_READ_STATUS_END_OF_STREAM.
	 *
	 *  As the decoder needs more input it will call the read callback.
	 *  As each metadata block is decoded, the metadata callback will be called
	 *  with the decoded metadata.
	 *
	 * @param  decoder  An initialized decoder instance.
	 * \assert
	 *    \code decoder != NULL \endcode
	 * @retval FLAC__bool
	 *    \c false if any fatal read, write, or memory allocation error
	 *    occurred (meaning decoding must stop), else \c true; for more
	 *    information about the decoder, check the decoder state with
	 *    FLAC__stream_decoder_get_state().
	 */
	public final boolean FLAC__stream_decoder_process_until_end_of_metadata()
	{
		//FLAC__ASSERT(0 != decoder);
		//FLAC__ASSERT(0 != decoder->protected_);

		while( true ) {
			switch( this.state ) {
				case FLAC__STREAM_DECODER_SEARCH_FOR_METADATA:
					try {
						find_metadata_(); /* above function sets the status for us */
					} catch(final IOException e) {
						return false;
					}
					break;
				case FLAC__STREAM_DECODER_READ_METADATA:
					if( ! read_metadata_() ) {
						return false; /* above function sets the status for us */
					}
					break;
				case FLAC__STREAM_DECODER_SEARCH_FOR_FRAME_SYNC:
				case FLAC__STREAM_DECODER_READ_FRAME:
				case FLAC__STREAM_DECODER_END_OF_STREAM:
				case FLAC__STREAM_DECODER_ABORTED:
					return true;
				default:
					//FLAC__ASSERT(0);
					return false;
			}
		}

	}
	/** Decode until the end of the stream.
	 *  This version instructs the decoder to decode from the current position
	 *  and continue until the end of stream (the read callback returns
	 *  \c FLAC__STREAM_DECODER_READ_STATUS_END_OF_STREAM), or until the
	 *  callbacks return a fatal error.
	 *
	 *  As the decoder needs more input it will call the read callback.
	 *  As each metadata block and frame is decoded, the metadata or write
	 *  callback will be called with the decoded metadata or frame.
	 *
	 * @param  decoder  An initialized decoder instance.
	 * \assert
	 *    \code decoder != NULL \endcode
	 * @retval FLAC__bool
	 *    \c false if any fatal read, write, or memory allocation error
	 *    occurred (meaning decoding must stop), else \c true; for more
	 *    information about the decoder, check the decoder state with
	 *    FLAC__stream_decoder_get_state().
	 */
	public final boolean FLAC__stream_decoder_process_until_end_of_stream()
	{
		//boolean dummy;
		//FLAC__ASSERT(0 != decoder);
		//FLAC__ASSERT(0 != decoder->protected_);

		while( true ) {
			switch( this.state ) {
				case FLAC__STREAM_DECODER_SEARCH_FOR_METADATA:
					try {
						find_metadata_(); /* above function sets the status for us */
					} catch(final IOException e) {
						return false;
					}
					break;
				case FLAC__STREAM_DECODER_READ_METADATA:
					if( ! read_metadata_() ) {
						return false; /* above function sets the status for us */
					}
					break;
				case FLAC__STREAM_DECODER_SEARCH_FOR_FRAME_SYNC:
					try{ frame_sync_(); } catch(final IOException e) {
						return true;// FIXME is this correct? return true; /* above function sets the status for us */
					}
					break;
				case FLAC__STREAM_DECODER_READ_FRAME:
					//if( ! read_frame_( decoder, &dummy, /*do_full_decode=*/true ) )
					//	return false; /* above function sets the status for us */
					try {
						read_frame_( /* &dummy,*/ /*do_full_decode=*/true );
					} catch(final IOException e) {
						return false; /* above function sets the status for us */
					}
					break;
				case FLAC__STREAM_DECODER_END_OF_STREAM:
				case FLAC__STREAM_DECODER_ABORTED:
					return true;
				default:
					//FLAC__ASSERT(0);
					return false;
			}
		}
	}
	/** Skip one audio frame.
	 *  This version instructs the decoder to 'skip' a single frame and stop,
	 *  unless the callbacks return a fatal error or the read callback returns
	 *  \c FLAC__STREAM_DECODER_READ_STATUS_END_OF_STREAM.
	 *
	 *  The decoding flow is the same as what occurs when
	 *  FLAC__stream_decoder_process_single() is called to process an audio
	 *  frame, except that this function does not decode the parsed data into
	 *  PCM or call the write callback.  The integrity of the frame is still
	 *  checked the same way as in the other process functions.
	 *
	 *  This function will return once one whole frame is skipped, in the
	 *  same way that FLAC__stream_decoder_process_single() will return once
	 *  one whole frame is decoded.
	 *
	 *  This function can be used in more quickly determining FLAC frame
	 *  boundaries when decoding of the actual data is not needed, for
	 *  example when an application is separating a FLAC stream into frames
	 *  for editing or storing in a container.  To do this, the application
	 *  can use FLAC__stream_decoder_skip_single_frame() to quickly advance
	 *  to the next frame, then use
	 *  FLAC__stream_decoder_get_decode_position() to find the new frame
	 *  boundary.
	 *
	 *  This function should only be called when the stream has advanced
	 *  past all the metadata, otherwise it will return \c false.
	 *
	 * @param  decoder  An initialized decoder instance not in a metadata
	 *                  state.
	 * \assert
	 *    \code decoder != NULL \endcode
	 * @retval FLAC__bool
	 *    \c false if any fatal read, write, or memory allocation error
	 *    occurred (meaning decoding must stop), or if the decoder
	 *    is in the FLAC__STREAM_DECODER_SEARCH_FOR_METADATA or
	 *    FLAC__STREAM_DECODER_READ_METADATA state, else \c true; for more
	 *    information about the decoder, check the decoder state with
	 *    FLAC__stream_decoder_get_state().
	 */
	public final boolean FLAC__stream_decoder_skip_single_frame()
	{
		//boolean got_a_frame;
		//FLAC__ASSERT(0 != decoder);
		//FLAC__ASSERT(0 != decoder->protected_);

		while( true ) {
			switch( this.state ) {
				case FLAC__STREAM_DECODER_SEARCH_FOR_METADATA:
				case FLAC__STREAM_DECODER_READ_METADATA:
					return false; /* above function sets the status for us */
				case FLAC__STREAM_DECODER_SEARCH_FOR_FRAME_SYNC:
					try{ frame_sync_(); } catch(final IOException e) {
						return true;// FIXME is this correct? return true; /* above function sets the status for us */
					}
					break;
				case FLAC__STREAM_DECODER_READ_FRAME:
					//if( ! read_frame_( decoder, &got_a_frame, /*do_full_decode=*/false ) )
					//	return false; /* above function sets the status for us */
					//if( got_a_frame )
					//	return true; /* above function sets the status for us */
					try {
						if( read_frame_( /* &got_a_frame,*/ /*do_full_decode=*/false ) ) {
							return true; /* above function sets the status for us */
						}
					}
					catch(final IOException e) {
						return false;
					}
					break;
				case FLAC__STREAM_DECODER_END_OF_STREAM:
				case FLAC__STREAM_DECODER_ABORTED:
					return true;
				default:
					//FLAC__ASSERT(0);
					return false;
			}
		}
	}
	/** Flush the input and seek to an absolute sample.
	 *  Decoding will resume at the given sample.  Note that because of
	 *  this, the next write callback may contain a partial block.  The
	 *  client must support seeking the input or this function will fail
	 *  and return \c false.  Furthermore, if the decoder state is
	 *  \c FLAC__STREAM_DECODER_SEEK_ERROR, then the decoder must be flushed
	 *  with FLAC__stream_decoder_flush() or reset with
	 *  FLAC__stream_decoder_reset() before decoding can continue.
	 *
	 * @param  decoder  A decoder instance.
	 * @param  sample   The target sample number to seek to.
	 * \assert
	 *    \code decoder != NULL \endcode
	 * @retval FLAC__bool
	 *    \c true if successful, else \c false.
	 */
	public final boolean FLAC__stream_decoder_seek_absolute(final long sample)
	{
		//FLAC__ASSERT(0 != decoder);

		if(
			this.state != FLAC__STREAM_DECODER_SEARCH_FOR_METADATA &&
			this.state != FLAC__STREAM_DECODER_READ_METADATA &&
			this.state != FLAC__STREAM_DECODER_SEARCH_FOR_FRAME_SYNC &&
			this.state != FLAC__STREAM_DECODER_READ_FRAME &&
			this.state != FLAC__STREAM_DECODER_END_OF_STREAM
		) {
			return false;
		}

		if( null == this.seek_callback ) {
			return false;
		}

		//FLAC__ASSERT(decoder->private_->seek_callback);
		//FLAC__ASSERT(decoder->private_->tell_callback);
		//FLAC__ASSERT(decoder->private_->length_callback);
		//FLAC__ASSERT(decoder->private_->eof_callback);

		if( FLAC__stream_decoder_get_total_samples() > 0 && sample >= FLAC__stream_decoder_get_total_samples() ) {
			return false;
		}

		this.is_seeking = true;

		/* turn off md5 checking if a seek is attempted */
		this.do_md5_checking = false;

		/* get the file length (currently our algorithm needs to know the length so it's also an error to get FLAC__STREAM_DECODER_LENGTH_STATUS_UNSUPPORTED) */
		long length;
		try {
			length = this.length_callback.dec_length_callback( this/*, this.client_data*/ );
		} catch(final IOException e) {
			this.is_seeking = false;
			return false;
		} catch(final UnsupportedOperationException e) {
			this.is_seeking = false;
			return false;
		}

		/* if we haven't finished processing the metadata yet, do that so we have the STREAMINFO, SEEK_TABLE, and first_frame_offset */
		if(
			this.state == FLAC__STREAM_DECODER_SEARCH_FOR_METADATA ||
			this.state == FLAC__STREAM_DECODER_READ_METADATA
		) {
			if( ! FLAC__stream_decoder_process_until_end_of_metadata() ) {
				/* above call sets the state for us */
				this.is_seeking = false;
				return false;
			}
			/* check this again in case we didn't know total_samples the first time */
			if( FLAC__stream_decoder_get_total_samples() > 0 && sample >= FLAC__stream_decoder_get_total_samples() ) {
				this.is_seeking = false;
				return false;
			}
		}

		{
			final boolean ok;
if( Jformat.FLAC__HAS_OGG ) {
			ok =
				this.is_ogg ?
				seek_to_absolute_sample_ogg_( length, sample ) :
				seek_to_absolute_sample_( length, sample );
} else {
			ok = seek_to_absolute_sample_( length, sample );
}
			this.is_seeking = false;
			return ok;
		}
	}
	/***********************************************************************
	 *
	 * Protected class methods
	 *
	 ***********************************************************************/

	final int FLAC__stream_decoder_get_input_bytes_unconsumed()
	{
		//FLAC__ASSERT(0 != decoder);
		//FLAC__ASSERT(FLAC__bitreader_is_consumed_byte_aligned(decoder->private_->input));
		//FLAC__ASSERT(!(FLAC__bitreader_get_input_bits_unconsumed(decoder->private_->input) & 7));
		return this.input.FLAC__bitreader_get_input_bits_unconsumed() >>> 3;// / 8;
	}

	/***********************************************************************
	 *
	 * Private class methods
	 *
	 ***********************************************************************/

	private final void set_defaults_()
	{
		this.is_ogg = false;
		this.read_callback = null;
		this.seek_callback = null;
		this.tell_callback = null;
		this.length_callback = null;
		this.eof_callback = null;
		this.write_callback = null;
		this.metadata_callback = null;
		this.error_callback = null;
		// this.client_data = null;

		Arrays.fill( this.metadata_filter, 0, this.metadata_filter.length, false );
		this.metadata_filter[Jformat.FLAC__METADATA_TYPE_STREAMINFO] = true;
		this.metadata_filter_ids_count = 0;

		this.md5_checking = false;

if( Jformat.FLAC__HAS_OGG ) {
		this.ogg_decoder_aspect.FLAC__ogg_decoder_aspect_set_defaults();
}
	}

	private final boolean allocate_output_(final int size, final int nchannels)
	{
		//int[] tmp;

		if( size <= this.output_capacity && nchannels <= this.output_channels ) {
			return true;
		}

		/* simply using realloc() is not practical because the number of channels may change mid-stream */

		for( int i = 0; i < Jformat.FLAC__MAX_CHANNELS; i++ ) {
			if( null != this.output[i] ) {
				this.output[i] = null;
				this.output[i] = null;
			}
			if( null != this.residual[i] /*this.residual_unaligned[i]*/ ) {
				//this.residual_unaligned[i] = null;
				this.residual[i] = null;
			}
		}

		for( int i = 0; i < nchannels; i++ ) {
			this.output[i] = new int[size];// already zeroed
			/*this.residual_unaligned[i] = */this.residual[i] = new int[size];
		}

		this.output_capacity = size;
		this.output_channels = nchannels;

		return true;
	}

	private final boolean has_id_filtered_(final byte[] id)
	{
		//FLAC__ASSERT(0 != decoder);
		//FLAC__ASSERT(0 != decoder->private_);

		final byte[] meta_ids = this.metadata_filter_ids;
		for( int i = 0; i < this.metadata_filter_ids_count; i++ ) {
			final int offset = i * (Jformat.FLAC__STREAM_METADATA_APPLICATION_ID_LEN / 8);
			int k = (Jformat.FLAC__STREAM_METADATA_APPLICATION_ID_LEN / 8);
			do {
				k--;
				if( meta_ids[offset + k] != id[k] ) {
					break;
				}
			} while( k > 0 );
			if( k == 0 ) {
				return true;
			}
		}
		return false;
	}

	private final void find_metadata_() throws IOException// java: changed. if an error, throws exception
	{
		int x, i, id;
		boolean first = true;

		//FLAC__ASSERT(FLAC__bitreader_is_consumed_byte_aligned(decoder->private_->input));

		try {// java: added to catch an error if incorrect format
			for( i = id = 0; i < 4; ) {
				if( this.cached ) {
					x = ((int)this.lookahead) & 0xff;
					this.cached = false;
				}
				else {
					x = this.input.FLAC__bitreader_read_raw_uint32( 8 );/* read_callback_ sets the state for us */
				}
				if( x == Jformat.FLAC__STREAM_SYNC_STRING[i] ) {
					first = true;
					i++;
					id = 0;
					continue;
				}

				if( id >= 3 ) {
					throw new IOException();// java: return false;
				}

				if( x == ID3V2_TAG_[id] ) {
					id++;
					i = 0;
					if( id == 3 ) {
						skip_id3v2_tag_();/* skip_id3v2_tag_ sets the state for us */
					}
					continue;
				}
				id = 0;
				if( x == 0xff ) { /* MAGIC NUMBER for the first 8 frame sync bits */
					this.header_warmup[0] = (byte)x;
					x = this.input.FLAC__bitreader_read_raw_uint32( 8 );/* read_callback_ sets the state for us */

					/* we have to check if we just read two 0xff's in a row; the second may actually be the beginning of the sync code */
					/* else we have to check if the second byte is the end of a sync code */
					if( x == 0xff ) { /* MAGIC NUMBER for the first 8 frame sync bits */
						this.lookahead = (byte)x;
						this.cached = true;
					}
					else if( x >>> 1 == 0x7c ) { /* MAGIC NUMBER for the last 6 sync bits and reserved 7th bit */
						this.header_warmup[1] = (byte)x;
						this.state = FLAC__STREAM_DECODER_READ_FRAME;
						return;
					}
				}
				i = 0;
				if( first ) {
					send_error_to_client_( FLAC__STREAM_DECODER_ERROR_STATUS_LOST_SYNC );
					first = false;
				}
			}

			this.state = FLAC__STREAM_DECODER_READ_METADATA;
		} catch(final Exception e) {
			throw new IOException( e );
		}
		return;
	}

	private final boolean read_metadata_()
	{
		boolean is_last;
		int type, length;

		//FLAC__ASSERT( JFLAC__BitReader.FLAC__bitreader_is_consumed_byte_aligned( decoder.input ) );

		try {
			is_last = (0 != this.input.FLAC__bitreader_read_raw_uint32( Jformat.FLAC__STREAM_METADATA_IS_LAST_LEN ));/* read_callback_ sets the state for us */

			type = this.input.FLAC__bitreader_read_raw_uint32( Jformat.FLAC__STREAM_METADATA_TYPE_LEN );/* read_callback_ sets the state for us */

			length = this.input.FLAC__bitreader_read_raw_uint32( Jformat.FLAC__STREAM_METADATA_LENGTH_LEN );/* read_callback_ sets the state for us */
		} catch(final IOException ie) {
			return false;
		}
		if( type == Jformat.FLAC__METADATA_TYPE_STREAMINFO ) {
			try {
				read_metadata_streaminfo_( is_last, length );
			} catch(final IOException ie) {
				return false;
			}

			this.has_stream_info = true;
			final byte[] md5sum = this.stream_info.md5sum;
			int i = 16;
			do{
				i--;
				if( md5sum[i] != 0 ) {
					break;
				}
			} while( i > 0 );
			if( i == 0 ) {
				this.do_md5_checking = false;
			}

			if( ! this.is_seeking && this.metadata_filter[Jformat.FLAC__METADATA_TYPE_STREAMINFO] && this.metadata_callback != null ) {
				try { this.metadata_callback.dec_metadata_callback( this, this.stream_info/*, this.client_data*/ ); } catch(final IOException ie) {}
			}
		}
		else if( type == Jformat.FLAC__METADATA_TYPE_SEEKTABLE ) {
			/* just in case we already have a seek table, and reading the next one fails: */
			this.has_seek_table = false;
			try {
				read_metadata_seektable_( is_last, length );
			} catch(final IOException ie) {
				return false;
			}

			this.has_seek_table = true;
			if( ! this.is_seeking && this.metadata_filter[Jformat.FLAC__METADATA_TYPE_SEEKTABLE] && this.metadata_callback != null ) {
				try { this.metadata_callback.dec_metadata_callback( this, this.seek_table/*, this.client_data*/ ); } catch(final IOException ie) {}
			}
		}
		else {
			boolean skip_it = ! this.metadata_filter[ type ];
			int real_length = length;
			final JFLAC__StreamMetadata block;

			// java: added, instead memset(&block, 0, sizeof(block));
			switch( type ) {
			case Jformat.FLAC__METADATA_TYPE_STREAMINFO:
				block = new JFLAC__StreamMetadata_StreamInfo();
				break;
			case Jformat.FLAC__METADATA_TYPE_PADDING:
				block = new JFLAC__StreamMetadata();
				break;
			case Jformat.FLAC__METADATA_TYPE_APPLICATION:
				block = new JFLAC__StreamMetadata_Application();
				break;
			case Jformat.FLAC__METADATA_TYPE_SEEKTABLE:
				block = new JFLAC__StreamMetadata_SeekTable();
				break;
			case Jformat.FLAC__METADATA_TYPE_VORBIS_COMMENT:
				block = new JFLAC__StreamMetadata_VorbisComment();
				break;
			case Jformat.FLAC__METADATA_TYPE_CUESHEET:
				block = new JFLAC__StreamMetadata_CueSheet();
				break;
			case Jformat.FLAC__METADATA_TYPE_PICTURE:
				block = new JFLAC__StreamMetadata_Picture();
				break;
			case Jformat.FLAC__METADATA_TYPE_UNDEFINED:
				block = new JFLAC__StreamMetadata_Unknown();
				break;
			default:
				return false;// error
			}// end java added
			block.is_last = is_last;
			block.type = type;
			block.length = length;

			if( type == Jformat.FLAC__METADATA_TYPE_APPLICATION ) {
				final JFLAC__StreamMetadata_Application application = (JFLAC__StreamMetadata_Application)block;
				try {
					this.input.FLAC__bitreader_read_byte_block_aligned_no_crc( application.id, Jformat.FLAC__STREAM_METADATA_APPLICATION_ID_LEN / 8 );/* read_callback_ sets the state for us */
				} catch(final IOException ie) {
					return false;
				}

				if( real_length < Jformat.FLAC__STREAM_METADATA_APPLICATION_ID_LEN / 8 ) { /* underflow check */
					this.state = FLAC__STREAM_DECODER_MEMORY_ALLOCATION_ERROR;/*@@@@@@ maybe wrong error? need to resync?*/
					return false;
				}

				real_length -= Jformat.FLAC__STREAM_METADATA_APPLICATION_ID_LEN/8;

				if( this.metadata_filter_ids_count > 0 && has_id_filtered_( application.id ) ) {
					skip_it = ! skip_it;
				}
			}

			if( skip_it ) {
				try {
					this.input.FLAC__bitreader_skip_byte_block_aligned_no_crc( real_length );/* read_callback_ sets the state for us */
				} catch(final IOException ie) {
					return false;
				}
			}
			else {
				boolean ok = true;
				switch( type ) {
					case Jformat.FLAC__METADATA_TYPE_PADDING:
						/* skip the padding bytes */
						try {
							this.input.FLAC__bitreader_skip_byte_block_aligned_no_crc( real_length );/* read_callback_ sets the state for us */
						} catch(final IOException ie) {
							ok = false;
						}
						break;
					case Jformat.FLAC__METADATA_TYPE_APPLICATION:
						final JFLAC__StreamMetadata_Application application = (JFLAC__StreamMetadata_Application)block;
						/* remember, we read the ID already */
						if( real_length > 0 ) {
							try {
								application.data = new byte[real_length];
								this.input.FLAC__bitreader_read_byte_block_aligned_no_crc( application.data, real_length );/* read_callback_ sets the state for us */
							} catch(final OutOfMemoryError oe) {
								state = FLAC__STREAM_DECODER_MEMORY_ALLOCATION_ERROR;
								ok = false;
							} catch(final IOException ie) {
								ok = false;
							}
						} else {
							application.data = null;
						}
						break;
					case Jformat.FLAC__METADATA_TYPE_VORBIS_COMMENT:
						try {
							read_metadata_vorbiscomment_( (JFLAC__StreamMetadata_VorbisComment)block, real_length );
						} catch(final IOException ie) {
							ok = false;
						}
						break;
					case Jformat.FLAC__METADATA_TYPE_CUESHEET:
						try {
							read_metadata_cuesheet_( (JFLAC__StreamMetadata_CueSheet)block );
						} catch(final IOException ie) {
							ok = false;
						}
						break;
					case Jformat.FLAC__METADATA_TYPE_PICTURE:
						try {
							read_metadata_picture_( (JFLAC__StreamMetadata_Picture)block );
						} catch(final IOException ie) {
							ok = false;
						}
						break;
					case Jformat.FLAC__METADATA_TYPE_STREAMINFO:
					case Jformat.FLAC__METADATA_TYPE_SEEKTABLE:
						// FLAC__ASSERT(0);
						break;
					default:
						final JFLAC__StreamMetadata_Unknown unknown = (JFLAC__StreamMetadata_Unknown)block;
						if( real_length > 0) {
							try {
								unknown.data = new byte[real_length];
								this.input.FLAC__bitreader_read_byte_block_aligned_no_crc( unknown.data, real_length );/* read_callback_ sets the state for us */
							} catch(final OutOfMemoryError oe) {
								state = FLAC__STREAM_DECODER_MEMORY_ALLOCATION_ERROR;
								ok = false;
							} catch(final IOException ie) {
								ok = false;
							}
						} else {
							unknown.data = null;
						}
						break;
				}
				if( ok && ! this.is_seeking && this.metadata_callback != null ) {
					try{ this.metadata_callback.dec_metadata_callback( this, block/*, this.client_data*/ ); } catch(final IOException ie){}
				}

				/* now we have to free any malloc()ed data in the block */
				switch( type ) {
					case Jformat.FLAC__METADATA_TYPE_PADDING:
						break;
					case Jformat.FLAC__METADATA_TYPE_APPLICATION:
						((JFLAC__StreamMetadata_Application)block).data = null;
						break;
					case Jformat.FLAC__METADATA_TYPE_VORBIS_COMMENT:
						final JFLAC__StreamMetadata_VorbisComment vorbis_comment = (JFLAC__StreamMetadata_VorbisComment)block;
						vorbis_comment.vendor_string = null;
						if( vorbis_comment.comments.length > 0 ) {
							final String[] comments = vorbis_comment.comments;// java
							for( int i = 0, ie = vorbis_comment.comments.length; i < ie; i++ ) {
								comments[i] = null;
							}
						}
						vorbis_comment.comments = null;
						break;
					case Jformat.FLAC__METADATA_TYPE_CUESHEET:
						final JFLAC__StreamMetadata_CueSheet cue_sheet = (JFLAC__StreamMetadata_CueSheet)block;
						if( cue_sheet.num_tracks > 0 ) {
							final JFLAC__StreamMetadata_CueSheet_Track[] tracks = cue_sheet.tracks;// java
							for( int i = 0, ie = cue_sheet.num_tracks; i < ie; i++ ) {
								tracks[i].indices = null;
							}
						}
						cue_sheet.tracks = null;
						break;
					case Jformat.FLAC__METADATA_TYPE_PICTURE:
						final JFLAC__StreamMetadata_Picture picture = (JFLAC__StreamMetadata_Picture)block;
						picture.mime_type = null;
						picture.description = null;
						picture.data = null;
						break;
					case Jformat.FLAC__METADATA_TYPE_STREAMINFO:
					case Jformat.FLAC__METADATA_TYPE_SEEKTABLE:
						//FLAC__ASSERT( 0 );
					default:
						((JFLAC__StreamMetadata_Unknown)block).data = null;
						break;
				}

				if( ! ok ) {
					return false;
				}
			}
		}

		if( is_last ) {
			/* if this fails, it's OK, it's just a hint for the seek routine */
			this.first_frame_offset = 0;
			try {
				this.first_frame_offset = FLAC__stream_decoder_get_decode_position();
			} catch(final IOException e) {
			} catch(final UnsupportedOperationException e) {
			}
			this.state = FLAC__STREAM_DECODER_SEARCH_FOR_FRAME_SYNC;
		}
		return true;
	}

	private final void read_metadata_streaminfo_(final boolean is_last, int length) throws IOException// java: changed. if an error, throws exception
	{
		int used_bits = 0;

		//FLAC__ASSERT( FLAC__bitreader_is_consumed_byte_aligned( decoder.private_.input));

		this.stream_info.type = Jformat.FLAC__METADATA_TYPE_STREAMINFO;
		this.stream_info.is_last = is_last;
		this.stream_info.length = length;

		final JFLAC__StreamMetadata_StreamInfo sinfo = this.stream_info;

		int bits = Jformat.FLAC__STREAM_METADATA_STREAMINFO_MIN_BLOCK_SIZE_LEN;
		sinfo.min_blocksize = this.input.FLAC__bitreader_read_raw_uint32( bits );/* read_callback_ sets the state for us */
		used_bits += bits;

		bits = Jformat.FLAC__STREAM_METADATA_STREAMINFO_MAX_BLOCK_SIZE_LEN;
		sinfo.max_blocksize = this.input.FLAC__bitreader_read_raw_uint32( Jformat.FLAC__STREAM_METADATA_STREAMINFO_MAX_BLOCK_SIZE_LEN );/* read_callback_ sets the state for us */
		used_bits += bits;

		bits = Jformat.FLAC__STREAM_METADATA_STREAMINFO_MIN_FRAME_SIZE_LEN;
		sinfo.min_framesize = this.input.FLAC__bitreader_read_raw_uint32( Jformat.FLAC__STREAM_METADATA_STREAMINFO_MIN_FRAME_SIZE_LEN );/* read_callback_ sets the state for us */
		used_bits += bits;

		bits = Jformat.FLAC__STREAM_METADATA_STREAMINFO_MAX_FRAME_SIZE_LEN;
		sinfo.max_framesize = this.input.FLAC__bitreader_read_raw_uint32( Jformat.FLAC__STREAM_METADATA_STREAMINFO_MAX_FRAME_SIZE_LEN );/* read_callback_ sets the state for us */
		used_bits += bits;

		bits = Jformat.FLAC__STREAM_METADATA_STREAMINFO_SAMPLE_RATE_LEN;
		sinfo.sample_rate = this.input.FLAC__bitreader_read_raw_uint32( Jformat.FLAC__STREAM_METADATA_STREAMINFO_SAMPLE_RATE_LEN );/* read_callback_ sets the state for us */
		used_bits += bits;

		bits = Jformat.FLAC__STREAM_METADATA_STREAMINFO_CHANNELS_LEN;
		sinfo.channels = 1 + this.input.FLAC__bitreader_read_raw_uint32( Jformat.FLAC__STREAM_METADATA_STREAMINFO_CHANNELS_LEN );/* read_callback_ sets the state for us */
		used_bits += bits;

		bits = Jformat.FLAC__STREAM_METADATA_STREAMINFO_BITS_PER_SAMPLE_LEN;
		sinfo.bits_per_sample = 1 + this.input.FLAC__bitreader_read_raw_uint32( Jformat.FLAC__STREAM_METADATA_STREAMINFO_BITS_PER_SAMPLE_LEN );/* read_callback_ sets the state for us */
		used_bits += bits;

		bits = Jformat.FLAC__STREAM_METADATA_STREAMINFO_TOTAL_SAMPLES_LEN;
		sinfo.total_samples = this.input.FLAC__bitreader_read_raw_uint64( Jformat.FLAC__STREAM_METADATA_STREAMINFO_TOTAL_SAMPLES_LEN );/* read_callback_ sets the state for us */
		used_bits += bits;

		this.input.FLAC__bitreader_read_byte_block_aligned_no_crc( sinfo.md5sum, 16 );/* read_callback_ sets the state for us */
		used_bits += 16 * 8;

		/* skip the rest of the block */
		//FLAC__ASSERT( used_bits % 8 == 0);
		length -= (used_bits >>> 3);
		this.input.FLAC__bitreader_skip_byte_block_aligned_no_crc( length );/* read_callback_ sets the state for us */
	}

	private final void read_metadata_seektable_(final boolean is_last, int length) throws IOException// java: changed. if an error, throws exception
	{
		//FLAC__ASSERT( FLAC__bitreader_is_consumed_byte_aligned( decoder.private_.input ) );

		this.seek_table.type = Jformat.FLAC__METADATA_TYPE_SEEKTABLE;
		this.seek_table.is_last = is_last;
		this.seek_table.length = length;

		final JFLAC__StreamMetadata_SeekTable table = this.seek_table;

		final int num_points = length / Jformat.FLAC__STREAM_METADATA_SEEKPOINT_LENGTH;
		table.num_points = num_points;// java

		/* use realloc since we may pass through here several times (e.g. after seeking) */
		table.points = Arrays.copyOf( table.points, num_points );

		for( int i = 0; i < num_points; i++ ) {
			table.points[i].sample_number = this.input.FLAC__bitreader_read_raw_uint64( Jformat.FLAC__STREAM_METADATA_SEEKPOINT_SAMPLE_NUMBER_LEN );/* read_callback_ sets the state for us */

			table.points[i].stream_offset = this.input.FLAC__bitreader_read_raw_uint64( Jformat.FLAC__STREAM_METADATA_SEEKPOINT_STREAM_OFFSET_LEN );/* read_callback_ sets the state for us */

			table.points[i].frame_samples = this.input.FLAC__bitreader_read_raw_uint32( Jformat.FLAC__STREAM_METADATA_SEEKPOINT_FRAME_SAMPLES_LEN );/* read_callback_ sets the state for us */
		}
		length -= (num_points * Jformat.FLAC__STREAM_METADATA_SEEKPOINT_LENGTH);
		/* if there is a partial point left, skip over it */
		if( length > 0 ) {
			/*@@@ do a send_error_to_client_() here?  there's an argument for either way */
			this.input.FLAC__bitreader_skip_byte_block_aligned_no_crc( length );/* read_callback_ sets the state for us */
		}
	}

	private final void read_metadata_vorbiscomment_(final JFLAC__StreamMetadata_VorbisComment obj, int length) throws IOException// java: changed. if an error, throws exception
	{// FIXME what about non-ASCII string encodings?
		//FLAC__ASSERT(FLAC__bitreader_is_consumed_byte_aligned(decoder->private_->input));

		/* read vendor string */
		if( length >= 8 ) {
			length -= 8; /* vendor string length + num comments entries alone take 8 bytes */
			//FLAC__ASSERT(FLAC__STREAM_METADATA_VORBIS_COMMENT_ENTRY_LENGTH_LEN == 32);

			int n = this.input.FLAC__bitreader_read_uint32_little_endian();/* read_callback_ sets the state for us */
			if( n > 0 ) {
				if( length < obj.vendor_string.length() ) {
					//obj.vendor_string.length = 0;
					obj.vendor_string = null;
					//goto skip;
					if( length > 0 ) {
						/* This will only happen on files with invalid data in comments */
						this.input.FLAC__bitreader_skip_byte_block_aligned_no_crc( length ); /* read_callback_ sets the state for us */
					}
				} else {
					length -= obj.vendor_string.length();
				}

				final byte[] entry = new byte[ n ];
				this.input.FLAC__bitreader_read_byte_block_aligned_no_crc( entry, n );/* read_callback_ sets the state for us */
				try { obj.vendor_string = new String( entry, JFLAC__StreamMetadata_VorbisComment.ENCODING ); } catch( final UnsupportedEncodingException e ) {}
			} else {
				obj.vendor_string = null;
			}

			/* read num comments */
			//FLAC__ASSERT(FLAC__STREAM_METADATA_VORBIS_COMMENT_NUM_COMMENTS_LEN == 32);
			obj.num_comments = this.input.FLAC__bitreader_read_uint32_little_endian();/* read_callback_ sets the state for us */

			/* read comments */
			if( obj.num_comments > 100000 ) {
				/* Possibly malicious file. */
				obj.num_comments = 0;
				throw new IOException();// return false;
			}
			if( obj.num_comments > 0 ) {
				try{
					obj.comments = new String[obj.num_comments];
				} catch(final OutOfMemoryError oe) {
					obj.num_comments = 0;
					this.state = FLAC__STREAM_DECODER_MEMORY_ALLOCATION_ERROR;
					throw new IOException();// return false;
				}
				for( int i = 0; i < obj.num_comments; i++ ) {
					/* Initialize here just to make sure. */
					obj.comments[i] = null;

					//FLAC__ASSERT(FLAC__STREAM_METADATA_VORBIS_COMMENT_ENTRY_LENGTH_LEN == 32);
					if( length < 4 ) {
						obj.num_comments = i;
						break;// FIXME why need goto skip; ? break enough
					} else {
						length -= 4;
					}
					try {
						n = this.input.FLAC__bitreader_read_uint32_little_endian(); /* read_callback_ sets the state for us */
					} catch(final IOException e) {
						obj.num_comments = i;
						throw e;
					}
					if( n > 0 ) {
						if( length < n ) {
							obj.num_comments = i;
							break;// FIXME why need goto skip; ? break enough
						} else {
							length -= n;
						}
						try {
							final byte[] comments = new byte[ n ];
							this.input.FLAC__bitreader_read_byte_block_aligned_no_crc( comments, n ); /* read_callback_ sets the state for us */
							try { obj.comments[i] = new String( comments, JFLAC__StreamMetadata_VorbisComment.ENCODING ); } catch( final UnsupportedEncodingException e ) {}
						} catch(final OutOfMemoryError oe) {
							this.state = FLAC__STREAM_DECODER_MEMORY_ALLOCATION_ERROR;
							obj.num_comments = i;
							throw new IOException();// return false;
						} catch(final IOException e) {
							obj.num_comments = i;
							break;
						}
					} else {
						obj.comments[i] = null;
					}
				}
			}
		}
//skip:
		if( length > 0 ) {
			/* length > 0 can only happen on files with invalid data in comments */
			if( obj.num_comments < 1 ) {
				obj.comments = null;
			}
			this.input.FLAC__bitreader_skip_byte_block_aligned_no_crc( length ); /* read_callback_ sets the state for us */
		}
	}

	private final void read_metadata_cuesheet_(final JFLAC__StreamMetadata_CueSheet obj) throws IOException// java: changed. if an error, throws exception
	{
		//FLAC__ASSERT( FLAC__bitreader_is_consumed_byte_aligned( decoder.private_.input));

		obj.clear();

		//FLAC__ASSERT( FLAC__STREAM_METADATA_CUESHEET_MEDIA_CATALOG_NUMBER_LEN % 8 == 0);
		final JFLAC__BitReader reader = this.input;// java
		reader.FLAC__bitreader_read_byte_block_aligned_no_crc( obj.media_catalog_number, Jformat.FLAC__STREAM_METADATA_CUESHEET_MEDIA_CATALOG_NUMBER_LEN / 8 );/* read_callback_ sets the state for us */

		obj.lead_in = reader.FLAC__bitreader_read_raw_uint64( Jformat.FLAC__STREAM_METADATA_CUESHEET_LEAD_IN_LEN );/* read_callback_ sets the state for us */

		obj.is_cd = (0 != reader.FLAC__bitreader_read_raw_uint32( Jformat.FLAC__STREAM_METADATA_CUESHEET_IS_CD_LEN )); /* read_callback_ sets the state for us */

		reader.FLAC__bitreader_skip_bits_no_crc( Jformat.FLAC__STREAM_METADATA_CUESHEET_RESERVED_LEN );/* read_callback_ sets the state for us */

		obj.num_tracks = reader.FLAC__bitreader_read_raw_uint32( Jformat.FLAC__STREAM_METADATA_CUESHEET_NUM_TRACKS_LEN );/* read_callback_ sets the state for us */

		if( obj.num_tracks > 0 ) {
			obj.tracks = new JFLAC__StreamMetadata_CueSheet_Track[obj.num_tracks];
			for( int i = 0; i < obj.num_tracks; i++ ) {
				final JFLAC__StreamMetadata_CueSheet_Track track = new JFLAC__StreamMetadata_CueSheet_Track();
				obj.tracks[i] = track;
				track.offset = reader.FLAC__bitreader_read_raw_uint64( Jformat.FLAC__STREAM_METADATA_CUESHEET_TRACK_OFFSET_LEN );/* read_callback_ sets the state for us */

				track.number = (byte)reader.FLAC__bitreader_read_raw_uint32( Jformat.FLAC__STREAM_METADATA_CUESHEET_TRACK_NUMBER_LEN );/* read_callback_ sets the state for us */

				//FLAC__ASSERT( FLAC__STREAM_METADATA_CUESHEET_TRACK_ISRC_LEN % 8 == 0);
				reader.FLAC__bitreader_read_byte_block_aligned_no_crc( track.isrc, Jformat.FLAC__STREAM_METADATA_CUESHEET_TRACK_ISRC_LEN/8 );/* read_callback_ sets the state for us */

				track.type = reader.FLAC__bitreader_read_raw_uint32( Jformat.FLAC__STREAM_METADATA_CUESHEET_TRACK_TYPE_LEN );/* read_callback_ sets the state for us */

				track.pre_emphasis = reader.FLAC__bitreader_read_raw_uint32( Jformat.FLAC__STREAM_METADATA_CUESHEET_TRACK_PRE_EMPHASIS_LEN );/* read_callback_ sets the state for us */

				reader.FLAC__bitreader_skip_bits_no_crc( Jformat.FLAC__STREAM_METADATA_CUESHEET_TRACK_RESERVED_LEN );/* read_callback_ sets the state for us */

				final int num_indices = reader.FLAC__bitreader_read_raw_uint32( Jformat.FLAC__STREAM_METADATA_CUESHEET_TRACK_NUM_INDICES_LEN );/* read_callback_ sets the state for us */
				track.num_indices = (byte)num_indices;
				if( num_indices > 0 ) {
					track.indices = new JFLAC__StreamMetadata_CueSheet_Index[num_indices];
					for( int j = 0; j < num_indices; j++) {
						final JFLAC__StreamMetadata_CueSheet_Index indx = new JFLAC__StreamMetadata_CueSheet_Index();
						track.indices[j] = indx;
						indx.offset = reader.FLAC__bitreader_read_raw_uint64( Jformat.FLAC__STREAM_METADATA_CUESHEET_INDEX_OFFSET_LEN );/* read_callback_ sets the state for us */

						indx.number = (byte)reader.FLAC__bitreader_read_raw_uint32( Jformat.FLAC__STREAM_METADATA_CUESHEET_INDEX_NUMBER_LEN );/* read_callback_ sets the state for us */

						reader.FLAC__bitreader_skip_bits_no_crc( Jformat.FLAC__STREAM_METADATA_CUESHEET_INDEX_RESERVED_LEN );/* read_callback_ sets the state for us */
					}
				}
			}
		}
	}

	private final void read_metadata_picture_(final JFLAC__StreamMetadata_Picture obj) throws IOException// java: changed. if an error, throws exception
	{
		//FLAC__ASSERT( FLAC__bitreader_is_consumed_byte_aligned( decoder.private_.input ) );
		final JFLAC__BitReader reader = this.input;// java
		/* read type */
		obj.picture_type = reader.FLAC__bitreader_read_raw_uint32( Jformat.FLAC__STREAM_METADATA_PICTURE_TYPE_LEN );/* read_callback_ sets the state for us */

		/* read MIME type */
		int x = reader.FLAC__bitreader_read_raw_uint32( Jformat.FLAC__STREAM_METADATA_PICTURE_MIME_TYPE_LENGTH_LEN );/* read_callback_ sets the state for us */
		try {
			byte[] buffer = new byte[ x/* + 1*/ ];
			if( x > 0 ) {
				reader.FLAC__bitreader_read_byte_block_aligned_no_crc( buffer, x );/* read_callback_ sets the state for us */
				obj.mime_type = new String( buffer, JFLAC__StreamMetadata_Picture.MIME_ENCODING );
			}
			//obj.mime_type[ x[0] ] = '\0';

			/* read description */
			x = reader.FLAC__bitreader_read_raw_uint32( Jformat.FLAC__STREAM_METADATA_PICTURE_DESCRIPTION_LENGTH_LEN );/* read_callback_ sets the state for us */
			buffer = new byte[ x/* + 1*/ ];
			if( x > 0 ) {
				reader.FLAC__bitreader_read_byte_block_aligned_no_crc( buffer, x );/* read_callback_ sets the state for us */
				obj.description = new String( buffer, JFLAC__StreamMetadata_Picture.DESCRIPTION_ENCODING );
			}
			//obj.description[ x[0] ] = '\0';
		} catch(final UnsupportedEncodingException e) {
			return;
		}

		/* read width */
		obj.width = reader.FLAC__bitreader_read_raw_uint32( Jformat.FLAC__STREAM_METADATA_PICTURE_WIDTH_LEN );/* read_callback_ sets the state for us */

		/* read height */
		obj.height = reader.FLAC__bitreader_read_raw_uint32( Jformat.FLAC__STREAM_METADATA_PICTURE_HEIGHT_LEN );/* read_callback_ sets the state for us */

		/* read depth */
		obj.depth = reader.FLAC__bitreader_read_raw_uint32( Jformat.FLAC__STREAM_METADATA_PICTURE_DEPTH_LEN );/* read_callback_ sets the state for us */

		/* read colors */
		obj.colors = reader.FLAC__bitreader_read_raw_uint32( Jformat.FLAC__STREAM_METADATA_PICTURE_COLORS_LEN );/* read_callback_ sets the state for us */

		/* read data */
		obj.data_length = reader.FLAC__bitreader_read_raw_uint32( Jformat.FLAC__STREAM_METADATA_PICTURE_DATA_LENGTH_LEN );/* read_callback_ sets the state for us */

		obj.data = new byte[obj.data_length];
		if( obj.data_length > 0 ) {
			reader.FLAC__bitreader_read_byte_block_aligned_no_crc( obj.data, obj.data_length );/* read_callback_ sets the state for us */
		}
	}

	private final void skip_id3v2_tag_() throws IOException// java: changed. if an error, throws exception
	{
		final JFLAC__BitReader reader = this.input;// java
		/* skip the version and flags bytes */
		int x = reader.FLAC__bitreader_read_raw_uint32( 24 );/* read_callback_ sets the state for us */
		/* get the size (in bytes) to skip */
		int skip = 0;
		for( int i = 0; i < 4; i++ ) {
			x = reader.FLAC__bitreader_read_raw_uint32( 8 );/* read_callback_ sets the state for us */
			skip <<= 7;
			skip |= (x & 0x7f);
		}
		/* skip the rest of the tag */
		reader.FLAC__bitreader_skip_byte_block_aligned_no_crc( skip );/* read_callback_ sets the state for us */
	}

	private final void frame_sync_() throws IOException// java: changed. if an error, throws exception
	{

		/* If we know the total number of samples in the stream, stop if we've read that many. */
		/* This will stop us, for example, from wasting time trying to sync on an ID3V1 tag. */
		if( FLAC__stream_decoder_get_total_samples() > 0 ) {
			if( this.samples_decoded >= FLAC__stream_decoder_get_total_samples() ) {
				this.state = FLAC__STREAM_DECODER_END_OF_STREAM;
				return;
			}
		}

		final JFLAC__BitReader reader = this.input;// java
		/* make sure we're byte aligned */
		int x;
		if( ! reader.FLAC__bitreader_is_consumed_byte_aligned() ) {
			x = reader.FLAC__bitreader_read_raw_uint32( reader.FLAC__bitreader_bits_left_for_byte_alignment() );/* read_callback_ sets the state for us */
		}
		boolean first = true;
		while( true ) {
			if( this.cached ) {
				x = this.lookahead & 0xff;
				this.cached = false;
			}
			else {
				x = reader.FLAC__bitreader_read_raw_uint32( 8 );/* read_callback_ sets the state for us */
			}
			if( x == 0xff ) { /* MAGIC NUMBER for the first 8 frame sync bits */
				this.header_warmup[0] = (byte)x;
				x = reader.FLAC__bitreader_read_raw_uint32( 8 );/* read_callback_ sets the state for us */

				/* we have to check if we just read two 0xff's in a row; the second may actually be the beginning of the sync code */
				/* else we have to check if the second byte is the end of a sync code */
				if( x == 0xff ) { /* MAGIC NUMBER for the first 8 frame sync bits */
					this.lookahead = (byte)x;
					this.cached = true;
				}
				else if( x >>> 1 == 0x7c ) { /* MAGIC NUMBER for the last 6 sync bits and reserved 7th bit */
					this.header_warmup[1] = (byte)x;
					this.state = FLAC__STREAM_DECODER_READ_FRAME;
					return;
				}
			}
			if( first ) {
				send_error_to_client_( FLAC__STREAM_DECODER_ERROR_STATUS_LOST_SYNC );
				first = false;
			}
		}

		//return true;// FIXME Unreachable code
	}

	/**
	 * java: return changed
	 * @return got_a_frame
	 */
	@SuppressWarnings("unused")
	private final boolean read_frame_(/* boolean[] got_a_frame,*/ final boolean do_full_decode) throws IOException// XXX java: changed. if an error, throws exception
	{
		//got_a_frame = false;

		/* init the CRC */
		int frame_crc = 0;/* the one we calculate from the input stream */
		frame_crc = JFLAC_crc.FLAC__CRC16_UPDATE( this.header_warmup[0], frame_crc );
		frame_crc = JFLAC_crc.FLAC__CRC16_UPDATE( this.header_warmup[1], frame_crc );
		final JFLAC__BitReader reader = this.input;// java
		reader.FLAC__bitreader_reset_read_crc16( frame_crc );

		read_frame_header_();// IOException return false;
		if( this.state == FLAC__STREAM_DECODER_SEARCH_FOR_FRAME_SYNC ) {
			return false;// true;
		}
		final int size = this.frame.header.blocksize;// java
		final int nchannels = this.frame.header.channels;// java
		if( ! allocate_output_( size, nchannels ) ) {
			throw new IOException();// false; may be better make OutOfMemory?
		}
		for( int channel = 0; channel < nchannels; channel++ ) {
			/*
			 * first figure the correct bits-per-sample of the subframe
			 */
			int bps = this.frame.header.bits_per_sample;
			switch( this.frame.header.channel_assignment ) {
				case Jformat.FLAC__CHANNEL_ASSIGNMENT_INDEPENDENT:
					/* no adjustment needed */
					break;
				case Jformat.FLAC__CHANNEL_ASSIGNMENT_LEFT_SIDE:
					//FLAC__ASSERT( decoder.private_.frame.header.channels == 2 );
					if( channel == 1 ) {
						bps++;
					}
					break;
				case Jformat.FLAC__CHANNEL_ASSIGNMENT_RIGHT_SIDE:
					//FLAC__ASSERT( decoder.private_.frame.header.channels == 2 );
					if( channel == 0 ) {
						bps++;
					}
					break;
				case Jformat.FLAC__CHANNEL_ASSIGNMENT_MID_SIDE:
					//FLAC__ASSERT( decoder.private_.frame.header.channels == 2 );
					if( channel == 1 ) {
						bps++;
					}
					break;
				default:
					//assert( 0 );
					throw new IOException();// false;
			}
			/*
			 * now read it
			 */
			read_subframe_( channel, bps, do_full_decode );
			if( this.state == FLAC__STREAM_DECODER_SEARCH_FOR_FRAME_SYNC ) {
				return false;// true;
			}
		}
		read_zero_padding_();
		if( this.state == FLAC__STREAM_DECODER_SEARCH_FOR_FRAME_SYNC ) {
			return false;// true;
		}

		/*
		 * Read the frame CRC-16 from the footer and check
		 */
		frame_crc = reader.FLAC__bitreader_get_read_crc16();
		final int x = reader.FLAC__bitreader_read_raw_uint32( Jformat.FLAC__FRAME_FOOTER_CRC_LEN );/* read_callback_ sets the state for us */

		if( frame_crc == x ) {
			if( do_full_decode ) {
				/* Undo any special channel coding */
				switch( this.frame.header.channel_assignment ) {
					case Jformat.FLAC__CHANNEL_ASSIGNMENT_INDEPENDENT:
						/* do nothing */
						break;
					case Jformat.FLAC__CHANNEL_ASSIGNMENT_LEFT_SIDE: {
						//FLAC__ASSERT( decoder.private_.frame.header.channels == 2 );
						final int[] output0 = this.output[0];// java
						final int[] output1 = this.output[1];// java
						for( int i = 0; i < size; i++ ) {
							output1[i] = output0[i] - output1[i];
						}
						break; }
					case Jformat.FLAC__CHANNEL_ASSIGNMENT_RIGHT_SIDE: {
						//FLAC__ASSERT( decoder.private_.frame.header.channels == 2 );
						final int[] output0 = this.output[0];// java
						final int[] output1 = this.output[1];// java
						for( int i = 0; i < size; i++ ) {
							output0[i] += output1[i];
						}
						break; }
					case Jformat.FLAC__CHANNEL_ASSIGNMENT_MID_SIDE: {
						//FLAC__ASSERT( decoder.private_.frame.header.channels == 2 );
						final int[] output0 = this.output[0];// java
						final int[] output1 = this.output[1];// java
						for( int i = 0; i < size; i++ ) {
if( true ) {
							int mid = output0[i];
							final int side = output1[i];
							mid <<= 1;// FIXME why need uint32 casting? mid = ((uint32_t) mid) << 1;
							mid |= (side & 1); /* i.e. if 'side' is odd... */
							output0[i] = (mid + side) >> 1;
							output1[i] = (mid - side) >> 1;
} else {
							/* OPT: without 'side' temp variable */
							final int mid = (output0[i] << 1) | (output1[i] & 1); /* i.e. if 'side' is odd... */
							output0[i] = (mid + output1[i]) >> 1;
							output1[i] = (mid - output1[i]) >> 1;
}
						}
						break; }
					default:
						//FLAC__ASSERT( 0 );
						break;
				}
			}
		}
		else {
			/* Bad frame, emit error and zero the output signal */
			send_error_to_client_( FLAC__STREAM_DECODER_ERROR_STATUS_FRAME_CRC_MISMATCH );
			if( do_full_decode ) {
				for( int channel = 0; channel < nchannels; channel++ ) {
					Arrays.fill( this.output[channel], 0, size, 0 );
				}
			}
		}

		//got_a_frame = true;

		/* we wait to update fixed_block_size until here, when we're sure we've got a proper frame and hence a correct blocksize */
		if( this.next_fixed_block_size != 0 ) {
			this.fixed_block_size = this.next_fixed_block_size;
		}

		/* put the latest values into the public section of the decoder instance */
		this.channels = nchannels;
		this.channel_assignment = this.frame.header.channel_assignment;
		this.bits_per_sample = this.frame.header.bits_per_sample;
		this.sample_rate = this.frame.header.sample_rate;
		this.blocksize = size;

		//FLAC__ASSERT( decoder.private_.frame.header.number_type == Jformat.FLAC__FRAME_NUMBER_TYPE_SAMPLE_NUMBER );
		this.samples_decoded = this.frame.header.sample_number + (long)size;

		/* write it */
		if( do_full_decode ) {
			if( write_audio_frame_to_client_( this.frame, this.output ) != FLAC__STREAM_DECODER_WRITE_STATUS_CONTINUE ) {
				this.state = FLAC__STREAM_DECODER_ABORTED;
				throw new IOException();// return false;
			}
		}

		this.state = FLAC__STREAM_DECODER_SEARCH_FOR_FRAME_SYNC;
		return true;// true;
	}

	private final void read_frame_header_() throws IOException// java: changed. if an error, throws exception
	{
		//final byte[] raw_header = new byte[16]; /* MAGIC NUMBER based on the maximum frame header size, including CRC */
		final Jraw_header_helper header = new Jraw_header_helper();
		final byte[] raw_header = header.raw_header;
		boolean is_unparseable = false;

		//FLAC__ASSERT(FLAC__bitreader_is_consumed_byte_aligned(decoder->private_->input));

		/* init the raw header with the saved bits from synchronization */
		raw_header[0] = this.header_warmup[0];
		raw_header[1] = this.header_warmup[1];
		int raw_header_len = 2;

		/* check to make sure that reserved bit is 0 */
		if( (raw_header[1] & 0x02) != 0 ) {
			is_unparseable = true;
		}

		/*
		 * Note that along the way as we read the header, we look for a sync
		 * code inside.  If we find one it would indicate that our original
		 * sync was bad since there cannot be a sync code in a valid header.
		 *
		 * Three kinds of things can go wrong when reading the frame header:
		 *  1) We may have sync'ed incorrectly and not landed on a frame header.
		 *     If we don't find a sync code, it can end up looking like we read
		 *     a valid but unparseable header, until getting to the frame header
		 *     CRC.  Even then we could get a false positive on the CRC.
		 *  2) We may have sync'ed correctly but on an unparseable frame (from a
		 *     future encoder).
		 *  3) We may be on a damaged frame which appears valid but unparseable.
		 *
		 * For all these reasons, we try and read a complete frame header as
		 * long as it seems valid, even if unparseable, up until the frame
		 * header CRC.
		 */

		/*
		 * read in the raw header as bytes so we can CRC it, and parse it on the way
		 */
		final JFLAC__BitReader reader = this.input;// java
		int x;
		for( int i = 0; i < 2; i++ ) {
			x = reader.FLAC__bitreader_read_raw_uint32( 8 );/* read_callback_ sets the state for us */
			if( x == 0xff ) { /* MAGIC NUMBER for the first 8 frame sync bits */
				/* if we get here it means our original sync was erroneous since the sync code cannot appear in the header */
				this.lookahead = (byte)x;
				this.cached = true;
				send_error_to_client_( FLAC__STREAM_DECODER_ERROR_STATUS_BAD_HEADER );
				this.state = FLAC__STREAM_DECODER_SEARCH_FOR_FRAME_SYNC;
				return;
			}
			raw_header[raw_header_len++] = (byte)x;
		}

		int blocksize_hint = 0, sample_rate_hint = 0;
		final JFLAC__FrameHeader frame_header = this.frame.header;// java
		switch( x = ((((int)raw_header[2]) & 0xff) >> 4) ) {
			case 0:
				is_unparseable = true;
				break;
			case 1:
				frame_header.blocksize = 192;
				break;
			case 2:
			case 3:
			case 4:
			case 5:
				frame_header.blocksize = 576 << (x - 2);
				break;
			case 6:
			case 7:
				blocksize_hint = x;
				break;
			case 8:
			case 9:
			case 10:
			case 11:
			case 12:
			case 13:
			case 14:
			case 15:
				frame_header.blocksize = 256 << (x - 8);
				break;
			default:
				//FLAC__ASSERT(0);
				break;
		}

		switch( x = ((int)raw_header[2]) & 0x0f ) {
			case 0:
				if( this.has_stream_info ) {
					frame_header.sample_rate = this.stream_info.sample_rate;
				} else {
					is_unparseable = true;
				}
				break;
			case 1:
				frame_header.sample_rate = 88200;
				break;
			case 2:
				frame_header.sample_rate = 176400;
				break;
			case 3:
				frame_header.sample_rate = 192000;
				break;
			case 4:
				frame_header.sample_rate = 8000;
				break;
			case 5:
				frame_header.sample_rate = 16000;
				break;
			case 6:
				frame_header.sample_rate = 22050;
				break;
			case 7:
				frame_header.sample_rate = 24000;
				break;
			case 8:
				frame_header.sample_rate = 32000;
				break;
			case 9:
				frame_header.sample_rate = 44100;
				break;
			case 10:
				frame_header.sample_rate = 48000;
				break;
			case 11:
				frame_header.sample_rate = 96000;
				break;
			case 12:
			case 13:
			case 14:
				sample_rate_hint = x;
				break;
			case 15:
				send_error_to_client_( FLAC__STREAM_DECODER_ERROR_STATUS_BAD_HEADER );
				this.state = FLAC__STREAM_DECODER_SEARCH_FOR_FRAME_SYNC;
				return;
			default:
				//FLAC__ASSERT( 0);
		}

		x = (((int)raw_header[3]) & 0xff) >> 4;
		if( (x & 8) != 0 ) {
			frame_header.channels = 2;
			switch( x & 7 ) {
				case 0:
					frame_header.channel_assignment = Jformat.FLAC__CHANNEL_ASSIGNMENT_LEFT_SIDE;
					break;
				case 1:
					frame_header.channel_assignment = Jformat.FLAC__CHANNEL_ASSIGNMENT_RIGHT_SIDE;
					break;
				case 2:
					frame_header.channel_assignment = Jformat.FLAC__CHANNEL_ASSIGNMENT_MID_SIDE;
					break;
				default:
					is_unparseable = true;
					break;
			}
		}
		else {
			frame_header.channels = x + 1;
			frame_header.channel_assignment = Jformat.FLAC__CHANNEL_ASSIGNMENT_INDEPENDENT;
		}

		switch( x = (raw_header[3] & 0x0e) >> 1 ) {
			case 0:
				if( this.has_stream_info ) {
					frame_header.bits_per_sample = this.stream_info.bits_per_sample;
				} else {
					is_unparseable = true;
				}
				break;
			case 1:
				frame_header.bits_per_sample = 8;
				break;
			case 2:
				frame_header.bits_per_sample = 12;
				break;
			case 4:
				frame_header.bits_per_sample = 16;
				break;
			case 5:
				frame_header.bits_per_sample = 20;
				break;
			case 6:
				frame_header.bits_per_sample = 24;
				break;
			case 3:
			case 7:
				is_unparseable = true;
				break;
			default:
				//FLAC__ASSERT(0);
				break;
		}

		/* check to make sure that reserved bit is 0 */
		if( (raw_header[3] & 0x01) != 0 ) {
			is_unparseable = true;
		}

		/* read the frame's starting sample number (or frame number as the case may be) */
		if(
			(raw_header[1] & 0x01) != 0 ||
			/*@@@ this clause is a concession to the old way of doing variable blocksize; the only known implementation is flake and can probably be removed without inconveniencing anyone */
			(this.has_stream_info && this.stream_info.min_blocksize != this.stream_info.max_blocksize)
		) { /* variable blocksize */
			header.raw_header_len = raw_header_len;
			final long xx = reader.FLAC__bitreader_read_utf8_uint64( header );/* read_callback_ sets the state for us */
			raw_header_len = header.raw_header_len;
			if( xx == 0xffffffffffffffffL ) { /* i.e. non-UTF8 code... */
				this.lookahead = raw_header[raw_header_len - 1]; /* back up as much as we can */
				this.cached = true;
				send_error_to_client_( FLAC__STREAM_DECODER_ERROR_STATUS_BAD_HEADER );
				this.state = FLAC__STREAM_DECODER_SEARCH_FOR_FRAME_SYNC;
				return;
			}
			frame_header.number_type = Jformat.FLAC__FRAME_NUMBER_TYPE_SAMPLE_NUMBER;
			frame_header.sample_number = xx;
		}
		else { /* fixed blocksize */
			header.raw_header_len = raw_header_len;
			x = reader.FLAC__bitreader_read_utf8_uint32( header );/* read_callback_ sets the state for us */
			raw_header_len = header.raw_header_len;
			if( x == 0xffffffff ) { /* i.e. non-UTF8 code... */
				this.lookahead = raw_header[raw_header_len - 1]; /* back up as much as we can */
				this.cached = true;
				send_error_to_client_( FLAC__STREAM_DECODER_ERROR_STATUS_BAD_HEADER );
				this.state = FLAC__STREAM_DECODER_SEARCH_FOR_FRAME_SYNC;
				return;
			}
			frame_header.number_type = Jformat.FLAC__FRAME_NUMBER_TYPE_FRAME_NUMBER;
			frame_header.frame_number = x;
		}

		if( blocksize_hint != 0 ) {
			x = reader.FLAC__bitreader_read_raw_uint32( 8 );/* read_callback_ sets the state for us */
			raw_header[raw_header_len++] = (byte)x;
			if( blocksize_hint == 7 ) {
				final int _x = reader.FLAC__bitreader_read_raw_uint32( 8 );/* read_callback_ sets the state for us */
				raw_header[raw_header_len++] = (byte)_x;
				x = (x << 8) | _x;
			}
			frame_header.blocksize = x + 1;
		}

		if( sample_rate_hint != 0 ) {
			x = reader.FLAC__bitreader_read_raw_uint32( 8 );/* read_callback_ sets the state for us */
			raw_header[raw_header_len++] = (byte)x;
			if( sample_rate_hint != 12) {
				final int _x = reader.FLAC__bitreader_read_raw_uint32( 8 );/* read_callback_ sets the state for us */
				raw_header[raw_header_len++] = (byte)_x;
				x = (x << 8) | _x;
			}
			if( sample_rate_hint == 12 ) {
				frame_header.sample_rate = x * 1000;
			} else if( sample_rate_hint == 13 ) {
				frame_header.sample_rate = x;
			} else {
				frame_header.sample_rate = x * 10;
			}
		}

		/* read the CRC-8 byte */
		final int crc8 = reader.FLAC__bitreader_read_raw_uint32( 8 );/* read_callback_ sets the state for us */

		if( JFLAC_crc.FLAC__crc8( raw_header, raw_header_len ) != crc8 ) {
			send_error_to_client_( FLAC__STREAM_DECODER_ERROR_STATUS_BAD_HEADER );
			this.state = FLAC__STREAM_DECODER_SEARCH_FOR_FRAME_SYNC;
			return;
		}

		/* calculate the sample number from the frame number if needed */
		this.next_fixed_block_size = 0;
		if( frame_header.number_type == Jformat.FLAC__FRAME_NUMBER_TYPE_FRAME_NUMBER ) {
			x = frame_header.frame_number;
			frame_header.number_type = Jformat.FLAC__FRAME_NUMBER_TYPE_SAMPLE_NUMBER;
			if( this.fixed_block_size != 0 ) {
				frame_header.sample_number = (long)this.fixed_block_size * (long)x;
			} else if( this.has_stream_info) {
				final JFLAC__StreamMetadata_StreamInfo info = this.stream_info;
				if( info.min_blocksize == info.max_blocksize ) {
					frame_header.sample_number = (long)info.min_blocksize * (long)x;
					this.next_fixed_block_size = info.max_blocksize;
				} else {
					is_unparseable = true;
				}
			}
			else if( x == 0 ) {
				frame_header.sample_number = 0;
				this.next_fixed_block_size = frame_header.blocksize;
			}
			else {
				/* can only get here if the stream has invalid frame numbering and no STREAMINFO, so assume it's not the last (possibly short) frame */
				frame_header.sample_number = (long)frame_header.blocksize * (long)x;
			}
		}

		if( is_unparseable ) {
			send_error_to_client_( FLAC__STREAM_DECODER_ERROR_STATUS_UNPARSEABLE_STREAM );
			this.state = FLAC__STREAM_DECODER_SEARCH_FOR_FRAME_SYNC;
			return;
		}
	}

	private final void read_subframe_(final int channel, int bps, final boolean do_full_decode) throws IOException// java: changed. on error it throws IOException
	{
		int x = this.input.FLAC__bitreader_read_raw_uint32( 8 ); /* MAGIC NUMBER *//* read_callback_ sets the state for us */

		final boolean wasted_bits = ((x & 1) != 0);
		x &= 0xfe;

		final JFLAC__Subframe subframe = this.frame.subframes[channel];// java
		if( wasted_bits ) {
			int u;
			u = this.input.FLAC__bitreader_read_unary_unsigned();/* read_callback_ sets the state for us */
			subframe.wasted_bits = u + 1;
			if( this.frame.subframes[channel].wasted_bits >= bps ) {
				throw new IOException();
			}
			bps -= subframe.wasted_bits;
		} else {
			subframe.wasted_bits = 0;
		}

		/*
		 * Lots of magic numbers here
		 */
		if( (x & 0x80) != 0 ) {
			send_error_to_client_( FLAC__STREAM_DECODER_ERROR_STATUS_LOST_SYNC );
			this.state = FLAC__STREAM_DECODER_SEARCH_FOR_FRAME_SYNC;
			return;
		}
		else if( x == 0 ) {
			read_subframe_constant_( channel, bps, do_full_decode );
		}
		else if( x == 2 ) {
			read_subframe_verbatim_( channel, bps, do_full_decode );
		}
		else if( x < 16 ) {
			send_error_to_client_( FLAC__STREAM_DECODER_ERROR_STATUS_UNPARSEABLE_STREAM );
			this.state = FLAC__STREAM_DECODER_SEARCH_FOR_FRAME_SYNC;
			return;
		}
		else if( x <= 24 ) {
			read_subframe_fixed_( channel, bps, (x >> 1) & 7, do_full_decode );
			if( this.state == FLAC__STREAM_DECODER_SEARCH_FOR_FRAME_SYNC ) {
				return;
			}
		}
		else if( x < 64 ) {
			send_error_to_client_( FLAC__STREAM_DECODER_ERROR_STATUS_UNPARSEABLE_STREAM );
			this.state = FLAC__STREAM_DECODER_SEARCH_FOR_FRAME_SYNC;
			return;
		}
		else {
			read_subframe_lpc_( channel, bps, ((x >> 1) & 31) + 1, do_full_decode );
			if( this.state == FLAC__STREAM_DECODER_SEARCH_FOR_FRAME_SYNC) {
				return;
			}
		}

		if( wasted_bits && do_full_decode ) {
			x = subframe.wasted_bits;
			final int[] out = this.output[channel];// java
			for( int i = 0, ie = this.frame.header.blocksize; i < ie; i++ ) {
				// final int val = out[i];
				// out[i] = (val << x);
				out[i] <<= x;
			}
		}
	}

	private final void read_subframe_constant_(final int channel, final int bps, final boolean do_full_decode) throws IOException// java: changed. if an error, throws exception
	{
		//final JFLAC__Subframe_Constant subframe = (JFLAC__Subframe_Constant)this.frame.subframes[channel].data;
		final JFLAC__Subframe_Constant subframe = new JFLAC__Subframe_Constant();
		final int[] out = this.output[channel];

		final JFLAC__Subframe sf = this.frame.subframes[channel];// java
		sf.type = Jformat.FLAC__SUBFRAME_TYPE_CONSTANT;
		sf.data = subframe;// java: added

		final int x = this.input.FLAC__bitreader_read_raw_int32( bps );/* read_callback_ sets the state for us */

		subframe.value = x;

		/* decode the subframe */
		if( do_full_decode ) {
			for( int i = 0; i < this.frame.header.blocksize; i++) {
				out[i] = x;
			}
		}
	}

	// fixed.c
	private static void FLAC__fixed_restore_signal(final int residual[], final int data_len, int order, final int data[])
	{
		switch( order ) {
			case 0:
				//FLAC__ASSERT(sizeof(residual[0]) == sizeof(data[0]));
				System.arraycopy( residual, 0, data, order, data_len );
				break;
			case 1:
				for( int i = 0; i < data_len; i++, order++ ) {
					data[order] = residual[i] + data[order - 1];
				}
				break;
			case 2:
				for( int i = 0; i < data_len; i++, order++ ) {
					data[order] = residual[i] + (data[order - 1] << 1) - data[order - 2];
				}
				break;
			case 3:
				for( int i = 0; i < data_len; i++, order++ ) {
					data[order] = residual[i] + 3 * (data[order - 1] - data[order - 2]) + data[order - 3];
				}
				break;
			case 4:
				for( int i = 0; i < data_len; i++, order++ ) {
					data[order] = residual[i] + ((((data[order - 1] + data[order - 3]) << 1) - 3 * data[order - 2]) << 1) - data[order - 4];
				}
				break;
			default:
				//FLAC__ASSERT(0);
				break;
		}
	}
	// end fixed.c

	private final void read_subframe_fixed_(final int channel, final int bps, final int order, final boolean do_full_decode) throws IOException// java: changed. on error it throws IOException
	{
		//final JFLAC__Subframe_Fixed subframe = (JFLAC__Subframe_Fixed)this.frame.subframes[channel].data;
		final JFLAC__Subframe_Fixed subframe = new JFLAC__Subframe_Fixed();

		final JFLAC__Subframe sf = this.frame.subframes[channel];// java
		sf.type = Jformat.FLAC__SUBFRAME_TYPE_FIXED;
		sf.data = subframe;// java: added

		subframe.residual = this.residual[channel];
		subframe.order = order;

		/* read warm-up samples */
		final JFLAC__BitReader reader = this.input;// java
		final int[] warmup = subframe.warmup;// java
		for( int u = 0; u < order; u++ ) {
			warmup[u] = reader.FLAC__bitreader_read_raw_int32( bps );/* read_callback_ sets the state for us */
		}

		/* read entropy coding method info */
		final JFLAC__EntropyCodingMethod method = subframe.entropy_coding_method;// java
		method.type = reader.FLAC__bitreader_read_raw_uint32( Jformat.FLAC__ENTROPY_CODING_METHOD_TYPE_LEN );/* read_callback_ sets the state for us */
		switch( method.type ) {
			case Jformat.FLAC__ENTROPY_CODING_METHOD_PARTITIONED_RICE:
			case Jformat.FLAC__ENTROPY_CODING_METHOD_PARTITIONED_RICE2:
				final int u32 = reader.FLAC__bitreader_read_raw_uint32( Jformat.FLAC__ENTROPY_CODING_METHOD_PARTITIONED_RICE_ORDER_LEN );
					// return false; /* read_callback_ sets the state for us */
				if( this.frame.header.blocksize >> u32 < order ) {
					send_error_to_client_( FLAC__STREAM_DECODER_ERROR_STATUS_LOST_SYNC );
					this.state = FLAC__STREAM_DECODER_SEARCH_FOR_FRAME_SYNC;
					return;
				}
				method.partitioned_rice.order = u32;
				method.partitioned_rice.contents = this.partitioned_rice_contents[channel];
				break;
			default:
				send_error_to_client_( FLAC__STREAM_DECODER_ERROR_STATUS_UNPARSEABLE_STREAM );
				this.state = FLAC__STREAM_DECODER_SEARCH_FOR_FRAME_SYNC;
				return;
		}

		/* read residual */
		switch( method.type ) {
			case Jformat.FLAC__ENTROPY_CODING_METHOD_PARTITIONED_RICE:
			case Jformat.FLAC__ENTROPY_CODING_METHOD_PARTITIONED_RICE2:
				read_residual_partitioned_rice_( order, method.partitioned_rice.order,
						this.partitioned_rice_contents[channel], this.residual[channel],
						/*is_extended=*/method.type == Jformat.FLAC__ENTROPY_CODING_METHOD_PARTITIONED_RICE2 );
				break;
			default:
				//FLAC__ASSERT(0);
		}

		/* decode the subframe */
		if( do_full_decode ) {
			System.arraycopy( warmup, 0, this.output[channel], 0, order );
			FLAC__fixed_restore_signal( this.residual[channel], this.frame.header.blocksize - order, order, this.output[channel]/* + order*/ );// java: changes inside the function
		}
	}

	private final void read_subframe_lpc_(final int channel, final int bps, final int order, final boolean do_full_decode) throws IOException// java: changed. if an error, throws exception
	{
		//final JFLAC__Subframe_LPC subframe = (JFLAC__Subframe_LPC)this.frame.subframes[channel].data;
		final JFLAC__Subframe_LPC subframe = new JFLAC__Subframe_LPC();

		final JFLAC__Subframe sf = this.frame.subframes[channel];// java
		sf.type = Jformat.FLAC__SUBFRAME_TYPE_LPC;
		sf.data = subframe;// java: added

		subframe.residual = this.residual[channel];
		subframe.order = order;

		/* read warm-up samples */
		final JFLAC__BitReader reader = this.input;// java
		final int[] warmup = subframe.warmup;// java
		for( int u = 0; u < order; u++ ) {
			warmup[u] = reader.FLAC__bitreader_read_raw_int32( bps );
		}

		/* read qlp coeff precision */
		int u32 = reader.FLAC__bitreader_read_raw_uint32( Jformat.FLAC__SUBFRAME_LPC_QLP_COEFF_PRECISION_LEN ); /* read_callback_ sets the state for us */
		if( u32 == (1 << Jformat.FLAC__SUBFRAME_LPC_QLP_COEFF_PRECISION_LEN) - 1 ) {
			send_error_to_client_( FLAC__STREAM_DECODER_ERROR_STATUS_LOST_SYNC );
			this.state = FLAC__STREAM_DECODER_SEARCH_FOR_FRAME_SYNC;
			return;
		}
		subframe.qlp_coeff_precision = u32 + 1;

		/* read qlp shift */
		final int i32 = reader.FLAC__bitreader_read_raw_int32( Jformat.FLAC__SUBFRAME_LPC_QLP_SHIFT_LEN );
			// return false; /* read_callback_ sets the state for us */
		if( i32 < 0 ) {
			send_error_to_client_( FLAC__STREAM_DECODER_ERROR_STATUS_LOST_SYNC );
			this.state = FLAC__STREAM_DECODER_SEARCH_FOR_FRAME_SYNC;
			return;
		}
		subframe.quantization_level = i32;

		/* read quantized lp coefficiencts */
		final int[] qlp_coeff = subframe.qlp_coeff;// java
		for( int u = 0; u < order; u++ ) {
			qlp_coeff[u] = reader.FLAC__bitreader_read_raw_int32( subframe.qlp_coeff_precision );
		}

		/* read entropy coding method info */
		final JFLAC__EntropyCodingMethod method = subframe.entropy_coding_method;// java
		method.type = reader.FLAC__bitreader_read_raw_uint32( Jformat.FLAC__ENTROPY_CODING_METHOD_TYPE_LEN );
		switch( method.type ) {
			case Jformat.FLAC__ENTROPY_CODING_METHOD_PARTITIONED_RICE:
			case Jformat.FLAC__ENTROPY_CODING_METHOD_PARTITIONED_RICE2:
				u32 = reader.FLAC__bitreader_read_raw_uint32( Jformat.FLAC__ENTROPY_CODING_METHOD_PARTITIONED_RICE_ORDER_LEN );
					// return false; /* read_callback_ sets the state for us */
				if( this.frame.header.blocksize >> u32 < order ) {
					send_error_to_client_( FLAC__STREAM_DECODER_ERROR_STATUS_LOST_SYNC );
					this.state = FLAC__STREAM_DECODER_SEARCH_FOR_FRAME_SYNC;
					return;
				}
				method.partitioned_rice.order = u32;
				method.partitioned_rice.contents = this.partitioned_rice_contents[channel];
				break;
			default:
				send_error_to_client_( FLAC__STREAM_DECODER_ERROR_STATUS_UNPARSEABLE_STREAM );
				this.state = FLAC__STREAM_DECODER_SEARCH_FOR_FRAME_SYNC;
				return;
		}

		/* read residual */
		switch( method.type ) {
			case Jformat.FLAC__ENTROPY_CODING_METHOD_PARTITIONED_RICE:
			case Jformat.FLAC__ENTROPY_CODING_METHOD_PARTITIONED_RICE2:
				read_residual_partitioned_rice_( order, method.partitioned_rice.order,
						this.partitioned_rice_contents[channel], this.residual[channel],
						/*is_extended=*/method.type == Jformat.FLAC__ENTROPY_CODING_METHOD_PARTITIONED_RICE2 );
				break;
			default:
				//FLAC__ASSERT( 0 );
				throw new IOException();// false;
		}

		/* decode the subframe */
		if( do_full_decode ) {
			System.arraycopy( warmup, 0, this.output[channel], 0, order );
			if( bps + subframe.qlp_coeff_precision + Jformat.FLAC__bitmath_ilog2( order ) <= 32 ) {
				//if( bps <= 16 && subframe.qlp_coeff_precision <= 16 )
				//Jlpc.FLAC__lpc_restore_signal( this.residual[channel], this.frame.header.blocksize - order, subframe.qlp_coeff, order, subframe.quantization_level, this.output[channel] );
//else
				Jlpc.FLAC__lpc_restore_signal( this.residual[channel], this.frame.header.blocksize - order, qlp_coeff, order, subframe.quantization_level, this.output[channel] );
			} else {
				Jlpc.FLAC__lpc_restore_signal_wide( this.residual[channel], this.frame.header.blocksize - order, qlp_coeff, order, subframe.quantization_level, this.output[channel] );
			}
		}
	}

	private final void read_subframe_verbatim_(final int channel, final int bps, final boolean do_full_decode) throws IOException// java: changed. if an error, throws exception
	{
		//final JFLAC__Subframe_Verbatim subframe = (JFLAC__Subframe_Verbatim)this.frame.subframes[channel].data;
		final JFLAC__Subframe_Verbatim subframe = new JFLAC__Subframe_Verbatim();
		final int[] r = this.residual[channel];

		final JFLAC__Subframe sf = this.frame.subframes[channel];// java
		sf.type = Jformat.FLAC__SUBFRAME_TYPE_VERBATIM;
		sf.data = subframe;// java: added

		subframe.data = r;

		final int size = this.frame.header.blocksize;// java
		final JFLAC__BitReader reader = this.input;// java
		for( int i = 0; i < size; i++ ) {
			r[i] = reader.FLAC__bitreader_read_raw_int32( bps );/* read_callback_ sets the state for us */
		}

		/* decode the subframe */
		if( do_full_decode ) {
			System.arraycopy( subframe.data, 0, this.output[channel], 0, size );
		}
	}

	private final void read_residual_partitioned_rice_(final int predictor_order, final int partition_order,
			final JFLAC__EntropyCodingMethod_PartitionedRiceContents rice_contents, final int[] res, final boolean is_extended)
			throws IOException// java: changed. if an error, throws exception
	{
		final int partitions = 1 << partition_order;
		final int partition_samples = partition_order > 0 ? this.frame.header.blocksize >> partition_order : this.frame.header.blocksize - predictor_order;
		final int plen = is_extended ? Jformat.FLAC__ENTROPY_CODING_METHOD_PARTITIONED_RICE2_PARAMETER_LEN : Jformat.FLAC__ENTROPY_CODING_METHOD_PARTITIONED_RICE_PARAMETER_LEN;
		final int pesc = is_extended ? Jformat.FLAC__ENTROPY_CODING_METHOD_PARTITIONED_RICE2_ESCAPE_PARAMETER : Jformat.FLAC__ENTROPY_CODING_METHOD_PARTITIONED_RICE_ESCAPE_PARAMETER;

		/* invalid predictor and partition orders mush be handled in the callers */
		// FLAC__ASSERT(partition_order > 0? partition_samples >= predictor_order : decoder->private_->frame.header.blocksize >= predictor_order);

		if( ! rice_contents.FLAC__format_entropy_coding_method_partitioned_rice_contents_ensure_size( (6 >= partition_order ? 6 : partition_order) ) ) {
			this.state = FLAC__STREAM_DECODER_MEMORY_ALLOCATION_ERROR;
			throw new IOException();// TODO may be, OutOfMemoryException? return false;
		}

		int sample = 0;
		for( int partition = 0; partition < partitions; partition++ ) {
			int rice_parameter = this.input.FLAC__bitreader_read_raw_uint32( plen );/* read_callback_ sets the state for us */
			rice_contents.parameters[partition] = rice_parameter;
			if( rice_parameter < pesc) {
				rice_contents.raw_bits[partition] = 0;
				final int u = (partition_order == 0 || partition > 0) ? partition_samples : partition_samples - predictor_order;
				this.input.FLAC__bitreader_read_rice_signed_block( res, sample, u, rice_parameter );/* read_callback_ sets the state for us */
				sample += u;
			}
			else {
				rice_parameter = this.input.FLAC__bitreader_read_raw_uint32( Jformat.FLAC__ENTROPY_CODING_METHOD_PARTITIONED_RICE_RAW_LEN );/* read_callback_ sets the state for us */
				rice_contents.raw_bits[partition] = rice_parameter;
				for( int u = (partition_order == 0 || partition > 0) ? 0 : predictor_order; u < partition_samples; u++, sample++ ) {
					res[sample] = this.input.FLAC__bitreader_read_raw_int32( rice_parameter );/* read_callback_ sets the state for us */
				}
			}
		}
	}

	private final void read_zero_padding_() throws IOException// java: changed. if an error, throws exception
	{
		final JFLAC__BitReader reader = this.input;// java
		if( ! reader.FLAC__bitreader_is_consumed_byte_aligned() ) {
			final int zero = reader.FLAC__bitreader_read_raw_uint32( reader.FLAC__bitreader_bits_left_for_byte_alignment() );/* read_callback_ sets the state for us */
			if( zero != 0 ) {
				send_error_to_client_( FLAC__STREAM_DECODER_ERROR_STATUS_LOST_SYNC );
				this.state = FLAC__STREAM_DECODER_SEARCH_FOR_FRAME_SYNC;
			}
		}
	}

	@Override// JFLAC__BitReaderReadCallback, read_callback_
	public final int bit_read_callback(final byte buffer[], int bytes/*, final Object data*/) throws IOException
	{
		// final JFLAC__StreamDecoder decoder = (JFLAC__StreamDecoder)data;

		if(
( ! Jformat.FLAC__HAS_OGG || (Jformat.FLAC__HAS_OGG &&
			/* see [1] HACK NOTE below for why we don't call the eof_callback when decoding Ogg FLAC */
			! this.is_ogg)) &&

			this.eof_callback != null && this.eof_callback.dec_eof_callback( this/*, this.client_data*/ )
		) {
			this.state = FLAC__STREAM_DECODER_END_OF_STREAM;
			return -1;
		}
		else if( bytes > 0 ) {
			/* While seeking, it is possible for our seek to land in the
			 * middle of audio data that looks exactly like a frame header
			 * from a future version of an encoder.  When that happens, our
			 * error callback will get an
			 * FLAC__STREAM_DECODER_UNPARSEABLE_STREAM and increment its
			 * unparseable_frame_count.  But there is a remote possibility
			 * that it is properly synced at such a "future-codec frame",
			 * so to make sure, we wait to see many "unparseable" errors in
			 * a row before bailing out.
			 */
			if( this.is_seeking && this.unparseable_frame_count > 20 ) {
				this.state = FLAC__STREAM_DECODER_ABORTED;
				throw new IOException( FLAC__stream_decoder_get_resolved_state_string() );// return false;
			}
			else {
				try {
if( Jformat.FLAC__HAS_OGG ) {
					bytes = this.is_ogg ?
						this.read_callback_ogg_aspect_( buffer, bytes ) :
							this.read_callback.dec_read_callback( this, buffer, 0, bytes/*, this.client_data*/ );
} else {

					bytes = this.read_callback.dec_read_callback( this, buffer, 0, bytes/*, this.client_data*/ );
}
					if( bytes < 0  ||
							(
( ! Jformat.FLAC__HAS_OGG || (Jformat.FLAC__HAS_OGG &&
								/* see [1] HACK NOTE below for why we don't call the eof_callback when decoding Ogg FLAC */
								! this.is_ogg ) ) &&

								this.eof_callback != null && this.eof_callback.dec_eof_callback( this/*, this.client_data*/ )
							)
						) {
							this.state = FLAC__STREAM_DECODER_END_OF_STREAM;
							return bytes;
					}
					return bytes;
				} catch(final Exception e) {
					this.state = FLAC__STREAM_DECODER_ABORTED;
					throw new IOException( FLAC__stream_decoder_get_resolved_state_string() );// return false;
				}
			}
		}
		else {
			/* abort to avoid a deadlock */
			this.state = FLAC__STREAM_DECODER_ABORTED;
			throw new IOException( FLAC__stream_decoder_get_resolved_state_string() );// return false;
		}
		/* [1] @@@ HACK NOTE: The end-of-stream checking has to be hacked around
		 * for Ogg FLAC.  This is because the ogg decoder aspect can lose sync
		 * and at the same time hit the end of the stream (for example, seeking
		 * to a point that is after the beginning of the last Ogg page).  There
		 * is no way to report an Ogg sync loss through the callbacks (see note
		 * in read_callback_ogg_aspect_()) so it returns CONTINUE with *bytes==0.
		 * So to keep the decoder from stopping at this point we gate the call
		 * to the eof_callback and let the Ogg decoder aspect set the
		 * end-of-stream state when it is needed.
		 */
	}

	// java: changed. return readed bytes instead FLAC__StreamDecoderReadStatus. uses IOException.
	private final int read_callback_ogg_aspect_(final byte buffer[], final int bytes) throws IOException
	{
if( Jformat.FLAC__HAS_OGG ) {

		return this.ogg_decoder_aspect.FLAC__ogg_decoder_aspect_read_callback_wrapper( buffer, bytes, this /*read_callback_proxy_*/, this/*, this.client_data*/ );

/*		switch( JFLAC__OggDecoderAspect.FLAC__ogg_decoder_aspect_read_callback_wrapper( this.ogg_decoder_aspect, buffer, bytes, this read_callback_proxy_, this, this.client_data ) ) {
			case JFLAC__OggDecoderAspect.FLAC__OGG_DECODER_ASPECT_READ_STATUS_OK:
				return FLAC__STREAM_DECODER_READ_STATUS_CONTINUE;
			 we don't really have a way to handle lost sync via read
			 * callback so we'll let it pass and let the underlying
			 * FLAC decoder catch the error

			case JFLAC__OggDecoderAspect.FLAC__OGG_DECODER_ASPECT_READ_STATUS_LOST_SYNC:
				return FLAC__STREAM_DECODER_READ_STATUS_CONTINUE;
			case JFLAC__OggDecoderAspect.FLAC__OGG_DECODER_ASPECT_READ_STATUS_END_OF_STREAM:
				return FLAC__STREAM_DECODER_READ_STATUS_END_OF_STREAM;
			case JFLAC__OggDecoderAspect.FLAC__OGG_DECODER_ASPECT_READ_STATUS_NOT_FLAC:
			case JFLAC__OggDecoderAspect.FLAC__OGG_DECODER_ASPECT_READ_STATUS_UNSUPPORTED_MAPPING_VERSION:
			case JFLAC__OggDecoderAspect.FLAC__OGG_DECODER_ASPECT_READ_STATUS_ABORT:
			case JFLAC__OggDecoderAspect.FLAC__OGG_DECODER_ASPECT_READ_STATUS_ERROR:
			case JFLAC__OggDecoderAspect.FLAC__OGG_DECODER_ASPECT_READ_STATUS_MEMORY_ALLOCATION_ERROR:
				return FLAC__STREAM_DECODER_READ_STATUS_ABORT;
			default:
				//FLAC__ASSERT(0);
				 double protection
				return FLAC__STREAM_DECODER_READ_STATUS_ABORT;
		}*/
}
		throw new IOException();//  FLAC__STREAM_DECODER_READ_STATUS_ABORT;
	}

	/** FLAC__OggDecoderAspectReadCallbackProxy */
	@Override
	//static int read_callback_proxy_(final Object void_decoder, byte buffer[], int[] bytes, Object client_data)
	public final int ogg_read_callback(final Object void_decoder, final byte buffer[], final int offset, final int bytes/*, final Object client_data*/) throws IOException
	{
if( Jformat.FLAC__HAS_OGG ) {
		final JFLAC__StreamDecoder decoder = (JFLAC__StreamDecoder)void_decoder;

		return decoder.read_callback.dec_read_callback( decoder, buffer, offset, bytes/*, client_data*/ );
/*
		switch( decoder.read_callback.dec_read_callback( decoder, buffer, offset, bytes, client_data ) ) {
			case FLAC__STREAM_DECODER_READ_STATUS_CONTINUE:
				return JFLAC__OggDecoderAspect.FLAC__OGG_DECODER_ASPECT_READ_STATUS_OK;
			case FLAC__STREAM_DECODER_READ_STATUS_END_OF_STREAM:
				return JFLAC__OggDecoderAspect.FLAC__OGG_DECODER_ASPECT_READ_STATUS_END_OF_STREAM;
			case FLAC__STREAM_DECODER_READ_STATUS_ABORT:
				return JFLAC__OggDecoderAspect.FLAC__OGG_DECODER_ASPECT_READ_STATUS_ABORT;
			default:
				// double protection:
				//FLAC__ASSERT( 0);
				return JFLAC__OggDecoderAspect.FLAC__OGG_DECODER_ASPECT_READ_STATUS_ABORT;
		}
*/
}
		throw new IOException();//return JFLAC__OggDecoderAspect.FLAC__OGG_DECODER_ASPECT_READ_STATUS_ABORT;
	}

	private final int write_audio_frame_to_client_(final JFLAC__Frame curr_frame, final int buffer[][])
	{// TODO using IOException instead status
		if( this.is_seeking ) {
			final long this_frame_sample = curr_frame.header.sample_number;
			final long next_frame_sample = this_frame_sample + (long)curr_frame.header.blocksize;
			final long sample = this.target_sample;

			//FLAC__ASSERT(frame.header.number_type == FLAC__FRAME_NUMBER_TYPE_SAMPLE_NUMBER);

if( Jformat.FLAC__HAS_OGG ) {
			this.got_a_frame = true;
}
			//this.private_.last_frame = *frame; /* save the frame */
			this.last_frame = curr_frame;// TODO check setting, c uses copy
			if( this_frame_sample <= sample && sample < next_frame_sample ) { /* we hit our target frame */
				final int delta = (int)(sample - this_frame_sample);
				/* kick out of seek mode */
				this.is_seeking = false;
				/* shift out the samples before target_sample */
				if( delta > 0 ) {
					//int channel;
					//final int newbuffer[][] = new int[Jformat.FLAC__MAX_CHANNELS][];
					//for( channel = 0; channel < frame.header.channels; channel++ )
					//	newbuffer[channel] = buffer[channel] + delta;
					this.last_frame.header.blocksize -= delta;
					this.last_frame.header.sample_number += (long)delta;
					/* write the relevant samples */
					return this.write_callback.dec_write_callback( this, this.last_frame, buffer, delta/*, this.client_data*/ );
				}
				else {
					/* write the relevant samples */
					return this.write_callback.dec_write_callback( this, curr_frame, buffer, 0/*, this.client_data*/ );
				}
			}
			else {
				return FLAC__STREAM_DECODER_WRITE_STATUS_CONTINUE;
			}
		}
		else {
			/*
			 * If we never got STREAMINFO, turn off MD5 checking to save
			 * cycles since we don't have a sum to compare to anyway
			 */
			if( ! this.has_stream_info ) {
				this.do_md5_checking = false;
			}
			if( this.do_md5_checking ) {
				if( ! this.md5context.FLAC__MD5Accumulate( buffer, curr_frame.header.channels, curr_frame.header.blocksize, (curr_frame.header.bits_per_sample + 7) >>> 3 ) ) {
					return FLAC__STREAM_DECODER_WRITE_STATUS_ABORT;
				}
			}
			return this.write_callback.dec_write_callback( this, curr_frame, buffer, 0/*, this.client_data*/ );
		}
	}

	private final void send_error_to_client_(final int /*FLAC__StreamDecoderErrorStatus*/ status) throws IOException// java: IOException added for possibilty abort decoding
	{
		if( ! this.is_seeking ) {
			this.error_callback.dec_error_callback( this, status/*, this.client_data*/ );
		} else if( status == FLAC__STREAM_DECODER_ERROR_STATUS_UNPARSEABLE_STREAM ) {
			this.unparseable_frame_count++;
		}
	}

	private final boolean seek_to_absolute_sample_(final long stream_length, final long sample)
	{
		final long total_samples = FLAC__stream_decoder_get_total_samples();
		final JFLAC__StreamMetadata_StreamInfo sinfo = this.stream_info;
		final int min_blocksize = sinfo.min_blocksize;
		final int max_blocksize = sinfo.max_blocksize;
		final int max_framesize = sinfo.max_framesize;
		final int min_framesize = sinfo.min_framesize;
		/* take these from the current frame in case they've changed mid-stream */
		int nchannels = FLAC__stream_decoder_get_channels();
		int bps = FLAC__stream_decoder_get_bits_per_sample();
		final JFLAC__StreamMetadata_SeekTable stable = this.has_seek_table ? this.seek_table : null;

		/* use values from stream info if we didn't decode a frame */
		if( nchannels == 0 ) {
			nchannels = sinfo.channels;
		}
		if( bps == 0 ) {
			bps = sinfo.bits_per_sample;
		}

		/* we are just guessing here */
		int approx_bytes_per_frame;
		if( max_framesize > 0 ) {
			approx_bytes_per_frame = ((max_framesize + min_framesize) >>> 1) + 1;
		} else if( min_blocksize == max_blocksize && min_blocksize > 0 ) {
			/* note there are no () around 'bps/8' to keep precision up since it's an integer calulation */
			approx_bytes_per_frame = min_blocksize * nchannels * (bps >> 3) + 64;
		} else {
			approx_bytes_per_frame = 4096 * nchannels * (bps >> 3) + 64;
		}

		/*
		 * First, we set an upper and lower bound on where in the
		 * stream we will search.  For now we assume the worst case
		 * scenario, which is our best guess at the beginning of
		 * the first frame and end of the stream.
		 */
		final long firstframe_offset = this.first_frame_offset;
		long lower_bound = firstframe_offset;
		long lower_bound_sample = 0;
		long upper_bound = stream_length;
		long upper_bound_sample = total_samples > 0 ? total_samples : sample /*estimate it*/;

		/*
		 * Now we refine the bounds if we have a seektable with
		 * suitable points.  Note that according to the spec they
		 * must be ordered by ascending sample number.
		 *
		 * Note: to protect against invalid seek tables we will ignore points
		 * that have frame_samples==0 or sample_number>=total_samples
		 */
		if( stable != null ) {
			long new_lower_bound = lower_bound;
			long new_upper_bound = upper_bound;
			long new_lower_bound_sample = lower_bound_sample;
			long new_upper_bound_sample = upper_bound_sample;

			/* find the closest seek point <= target_sample, if it exists */
			final JFLAC__StreamMetadata_SeekPoint[] points = stable.points;// java
			int i;
			for( i = stable.num_points - 1; i >= 0; i-- ) {
				final JFLAC__StreamMetadata_SeekPoint p = points[i];// java
				if(
					p.sample_number != Jformat.FLAC__STREAM_METADATA_SEEKPOINT_PLACEHOLDER &&
					p.frame_samples > 0 && /* defense against bad seekpoints */
					(total_samples <= 0 || p.sample_number < total_samples) && /* defense against bad seekpoints */
					p.sample_number <= sample
				) {
					break;
				}
			}
			if( i >= 0 ) { /* i.e. we found a suitable seek point... */
				new_lower_bound = firstframe_offset + points[i].stream_offset;
				new_lower_bound_sample = points[i].sample_number;
			}

			/* find the closest seek point > target_sample, if it exists */
			for( i = 0; i < stable.num_points; i++ ) {
				final JFLAC__StreamMetadata_SeekPoint p = points[i];// java
				if(
					p.sample_number != Jformat.FLAC__STREAM_METADATA_SEEKPOINT_PLACEHOLDER &&
					p.frame_samples > 0 && /* defense against bad seekpoints */
					(total_samples <= 0 || p.sample_number < total_samples) && /* defense against bad seekpoints */
					p.sample_number > sample
				) {
					break;
				}
			}
			if( i < stable.num_points ) { /* i.e. we found a suitable seek point... */
				final JFLAC__StreamMetadata_SeekPoint p = points[i];// java
				new_upper_bound = firstframe_offset + p.stream_offset;
				new_upper_bound_sample = p.sample_number;
			}
			/* final protection against unsorted seek tables; keep original values if bogus */
			if( new_upper_bound >= new_lower_bound ) {
				lower_bound = new_lower_bound;
				upper_bound = new_upper_bound;
				lower_bound_sample = new_lower_bound_sample;
				upper_bound_sample = new_upper_bound_sample;
			}
		}

		/* there are 2 insidious ways that the following equality occurs, which
		 * we need to fix:
		 *  1) total_samples is 0 (unknown) and target_sample is 0
		 *  2) total_samples is 0 (unknown) and target_sample happens to be
		 *     exactly equal to the last seek point in the seek table; this
		 *     means there is no seek point above it, and upper_bound_samples
		 *     remains equal to the estimate (of target_samples) we made above
		 * in either case it does not hurt to move upper_bound_sample up by 1
		 */
		if( upper_bound_sample == lower_bound_sample ) {
			upper_bound_sample++;
		}

		boolean first_seek = true;
		this.target_sample = sample;
		// long pos = -1;
		while( true ) {
			/* check if the bounds are still ok */
			if ( lower_bound_sample >= upper_bound_sample || lower_bound > upper_bound ) {
				this.state = FLAC__STREAM_DECODER_SEEK_ERROR;
				return false;
			}
//if( ! FLAC__INTEGER_ONLY_LIBRARY ) {
			long pos = lower_bound + (long)((double)(sample - lower_bound_sample) / (double)(upper_bound_sample - lower_bound_sample) * (double)(upper_bound - lower_bound)) - (long)approx_bytes_per_frame;
//} else {
			/* a little less accurate: */
//			if( upper_bound - lower_bound < 0xffffffffL )
//				pos = lower_bound + (((target_sample - lower_bound_sample) * (upper_bound - lower_bound)) / (upper_bound_sample - lower_bound_sample)) - (long)approx_bytes_per_frame;
//			else /* @@@ WATCHOUT, ~2TB limit */
//				pos = lower_bound + ((((target_sample - lower_bound_sample)>>8) * ((upper_bound - lower_bound)>>8)) / ((upper_bound_sample - lower_bound_sample)>>16)) - (long)approx_bytes_per_frame;
//}
			if( pos >= upper_bound ) {
				pos = upper_bound - 1;
			}
			if( pos < lower_bound ) {
				pos = lower_bound;
			}
			if( this.seek_callback.dec_seek_callback( this, pos/*, this.client_data*/ ) != FLAC__STREAM_DECODER_SEEK_STATUS_OK ) {
				this.state = FLAC__STREAM_DECODER_SEEK_ERROR;
				return false;
			}
			if( ! FLAC__stream_decoder_flush() ) {
				/* above call sets the state for us */
				return false;
			}
			/* Now we need to get a frame.  First we need to reset our
			 * unparseable_frame_count; if we get too many unparseable
			 * frames in a row, the read callback will return
			 * FLAC__STREAM_DECODER_READ_STATUS_ABORT, causing
			 * FLAC__stream_decoder_process_single() to return false.
			 */
			this.unparseable_frame_count = 0;
			if( ! FLAC__stream_decoder_process_single() ||
					this.state == FLAC__STREAM_DECODER_ABORTED ) {
				this.state = FLAC__STREAM_DECODER_SEEK_ERROR;
				return false;
			}
			/* our write callback will change the state when it gets to the target frame */
			/* actually, we could have got_a_frame if our decoder is at FLAC__STREAM_DECODER_END_OF_STREAM so we need to check for that also */
			if( ! this.is_seeking ) {
				break;
			}

			//FLAC__ASSERT( decoder.last_frame.header.number_type == FLAC__FRAME_NUMBER_TYPE_SAMPLE_NUMBER );
			final JFLAC__FrameHeader last_frame_header = this.last_frame.header;// java
			final long this_frame_sample = last_frame_header.sample_number;

			if( 0 == this.samples_decoded || (this_frame_sample + last_frame_header.blocksize >= upper_bound_sample && ! first_seek) ) {
				if( pos == lower_bound ) {
					/* can't move back any more than the first frame, something is fatally wrong */
					this.state = FLAC__STREAM_DECODER_SEEK_ERROR;
					return false;
				}
				/* our last move backwards wasn't big enough, try again */
				approx_bytes_per_frame = approx_bytes_per_frame != 0 ? (approx_bytes_per_frame << 1) : 16;
				continue;
			}
			/* allow one seek over upper bound, so we can get a correct upper_bound_sample for streams with unknown total_samples */
			first_seek = false;

			/* make sure we are not seeking in corrupted stream */
			if( this_frame_sample < lower_bound_sample ) {
				this.state = FLAC__STREAM_DECODER_SEEK_ERROR;
				return false;
			}

			try {
				/* we need to narrow the search */
				if( sample < this_frame_sample ) {
					upper_bound_sample = this_frame_sample + last_frame_header.blocksize;
/*@@@@@@ what will decode position be if at end of stream? */
					upper_bound = FLAC__stream_decoder_get_decode_position();
					approx_bytes_per_frame = (int)(((upper_bound - pos) << 1) / 3 + 16);
				}
				else { /* target_sample >= this_frame_sample + this frame's blocksize */
					lower_bound_sample = this_frame_sample + last_frame_header.blocksize;
					lower_bound = FLAC__stream_decoder_get_decode_position();
					approx_bytes_per_frame = (int)(((lower_bound - pos) << 1) / 3 + 16);
				}
			} catch(final IOException e) {
				this.state = FLAC__STREAM_DECODER_SEEK_ERROR;
				return false;
			} catch(final UnsupportedOperationException e) {
				this.state = FLAC__STREAM_DECODER_SEEK_ERROR;
				return false;
			}
		}// while( true )

		return true;
	}

	/* We will switch to a linear search once our current sample is less
	 * than this number of samples ahead of the target sample
	 */
	static final long LINEAR_SEARCH_WITHIN_SAMPLES = Jformat.FLAC__MAX_BLOCK_SIZE * 2L;

	private final boolean seek_to_absolute_sample_ogg_(final long stream_length, final long sample)
	{
if( Jformat.FLAC__HAS_OGG ) {
		long left_pos = 0, right_pos = stream_length;
		long left_sample = 0, right_sample = FLAC__stream_decoder_get_total_samples();
		long this_frame_sample = 0L - 1;
		long pos = 0; /* only initialized to avoid compiler warning */
		boolean did_a_seek;
		int iteration = 0;

		/* In the first iterations, we will calculate the target byte position
		 * by the distance from the target sample to left_sample and
		 * right_sample (let's call it "proportional search").  After that, we
		 * will switch to binary search.
		 */
		int BINARY_SEARCH_AFTER_ITERATION = 2;

		/* If the total number of samples is unknown, use a large value, and
		 * force binary search immediately.
		 */
		if( right_sample == 0 ) {
			right_sample = -1L;
			BINARY_SEARCH_AFTER_ITERATION = 0;
		}

		this.target_sample = sample;
		final JFLAC__StreamDecoderSeekCallback seek = this.seek_callback;// java
		for( ; ; iteration++ ) {
			if( iteration == 0 || this_frame_sample > sample || sample - this_frame_sample > LINEAR_SEARCH_WITHIN_SAMPLES ) {
				if( iteration >= BINARY_SEARCH_AFTER_ITERATION ) {
					pos = (right_pos + left_pos) >> 1;
				}
				else {
//if( ! FLAC__INTEGER_ONLY_LIBRARY ) {
					pos = (long)((double)(sample - left_sample) / (double)(right_sample - left_sample) * (double)(right_pos - left_pos));
//} else {
					/* a little less accurate: */
//					if ((target_sample-left_sample <= 0xffffffff) && (right_pos - left_pos <= 0xffffffff))
//						pos = (FLAC__int64)(((target_sample - left_sample) * (right_pos - left_pos)) / (right_sample - left_sample));
//					else /* @@@ WATCHOUT, ~2TB limit */
//						pos = (FLAC__int64)((((target_sample - left_sample) >> 8) * ((right_pos - left_pos) >> 8)) / ((right_sample - left_sample) >> 16));
//}
					/* @@@ TODO: might want to limit pos to some distance
					 * before EOF, to make sure we land before the last frame,
					 * thereby getting a this_frame_sample and so having a better
					 * estimate.
					 */
				}

				/* physical seek */
				if( seek.dec_seek_callback( this, pos/*, this.client_data*/ ) != FLAC__STREAM_DECODER_SEEK_STATUS_OK ) {
					this.state = FLAC__STREAM_DECODER_SEEK_ERROR;
					return false;
				}
				if( ! FLAC__stream_decoder_flush() ) {
					/* above call sets the state for us */
					return false;
				}
				did_a_seek = true;
			} else {
				did_a_seek = false;
			}

			this.got_a_frame = false;
			if( ! FLAC__stream_decoder_process_single() ||
					this.state == FLAC__STREAM_DECODER_ABORTED ) {
				this.state = FLAC__STREAM_DECODER_SEEK_ERROR;
				return false;
			}
			if( ! this.got_a_frame ) {
				if( did_a_seek ) {
					/* this can happen if we seek to a point after the last frame; we drop
					 * to binary search right away in this case to avoid any wasted
					 * iterations of proportional search.
					 */
					right_pos = pos;
					BINARY_SEARCH_AFTER_ITERATION = 0;
				}
				else {
					/* this can probably only happen if total_samples is unknown and the
					 * target_sample is past the end of the stream
					 */
					this.state = FLAC__STREAM_DECODER_SEEK_ERROR;
					return false;
				}
			}
			/* our write callback will change the state when it gets to the target frame */
			else if( ! this.is_seeking ) {
				break;
			}
			else {
				this_frame_sample = this.last_frame.header.sample_number;
				//FLAC__ASSERT( decoder.private_.last_frame.header.number_type == Jformat.FLAC__FRAME_NUMBER_TYPE_SAMPLE_NUMBER );

				if( did_a_seek ) {
					if( this_frame_sample <= sample ) {
						/* The 'equal' case should not happen, since
						 * FLAC__stream_decoder_process_single()
						 * should recognize that it has hit the
						 * target sample and we would exit through
						 * the 'break' above.
						 */
						//FLAC__ASSERT( this_frame_sample != target_sample );

						left_sample = this_frame_sample;
						/* sanity check to avoid infinite loop */
						if( left_pos == pos ) {
							this.state = FLAC__STREAM_DECODER_SEEK_ERROR;
							return false;
						}
						left_pos = pos;
					}
					else if( this_frame_sample > sample ) {
						right_sample = this_frame_sample;
						/* sanity check to avoid infinite loop */
						if( right_pos == pos ) {
							this.state = FLAC__STREAM_DECODER_SEEK_ERROR;
							return false;
						}
						right_pos = pos;
					}
				}
			}
		}

		return true;
}
		return false;
	}

	@Override// implements JFLAC__StreamDecoderReadCallback, file_read_callback_
	public final int dec_read_callback(final JFLAC__StreamDecoder decoder, final byte buffer[], final int offset, final int bytes/*, final Object client_data*/) throws IOException
	{
		return decoder.file.read( buffer, offset, bytes );
	}

	@Override// implements JFLAC__StreamDecoderSeekStatus, file_seek_callback_
	public int dec_seek_callback(final JFLAC__StreamDecoder decoder, final long absolute_byte_offset/*, final Object client_data*/)
	{
		final InputStream is = decoder.file;
		if( is instanceof RandomAccessInputStream ) {
			try {
				((RandomAccessInputStream) is).seek( absolute_byte_offset );
				return FLAC__STREAM_DECODER_SEEK_STATUS_OK;
			} catch(final IOException e) {
				return FLAC__STREAM_DECODER_SEEK_STATUS_ERROR;
			}
		}
		// if( is == System.in )
		return FLAC__STREAM_DECODER_SEEK_STATUS_UNSUPPORTED;
	}

	@Override// implements JFLAC__StreamDecoderTellStatus, file_tell_callback_
	public long dec_tell_callback(final JFLAC__StreamDecoder decoder/*, final Object client_data*/) throws IOException, UnsupportedOperationException
	{
		final InputStream is = decoder.file;
		if( is instanceof RandomAccessInputStream ) {
			return ((RandomAccessInputStream) is).getFilePointer();
		}
		// if( is == System.in )
		throw new UnsupportedOperationException( FLAC__StreamDecoderTellStatusString[FLAC__STREAM_DECODER_TELL_STATUS_UNSUPPORTED] );
	}

	@Override// JFLAC__StreamDecoderLengthCallback, file_length_callback_
	public final long dec_length_callback(final JFLAC__StreamDecoder decoder/*, final Object client_data*/) throws IOException, UnsupportedOperationException
	{
		final InputStream is = decoder.file;
		if( is instanceof RandomAccessInputStream ) {
			return ((RandomAccessInputStream) is).length();
		}
		throw new UnsupportedOperationException( FLAC__StreamDecoderLengthStatusString[FLAC__STREAM_DECODER_LENGTH_STATUS_UNSUPPORTED] );
	}

	@Override// JFLAC__StreamDecoderEofCallback, file_eof_callback_
	public final boolean dec_eof_callback(final JFLAC__StreamDecoder decoder/*, final Object client_data*/) {
		//return feof(decoder.file) ? true : false;
		final InputStream is = decoder.file;
		if( is instanceof RandomAccessInputStream ) {
			try {
				return ((RandomAccessInputStream) is).length() == ((RandomAccessInputStream) is).getFilePointer();
			} catch( final IOException e ) {
			}
		}
		return false;
	}
}
