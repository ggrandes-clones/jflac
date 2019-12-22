package libFLAC;

import java.io.IOException;

import libogg.Jogg_packet;
import libogg.Jogg_page;
import libogg.Jogg_stream_state;

final class JFLAC__OggEncoderAspect extends Jogg_mapping {
	private static final byte[] FLAC__OGG_MAPPING_VERSION_MAJOR = { 1 };
	private static final byte[] FLAC__OGG_MAPPING_VERSION_MINOR = { 0 };

	/* these are storage for values that can be set through the API */
	private int serial_number = 0;
	private int num_metadata = 0;

	/* these are for internal state related to Ogg encoding */
	private final Jogg_stream_state stream_state = new Jogg_stream_state();
	private final Jogg_page page = new Jogg_page();
	/** true if we've seen the fLaC magic in the write callback yet */
	private boolean seen_magic = false;
	private boolean is_first_packet = false;
	private long samples_written = 0;

	/***********************************************************************
	 *
	 * Public class methods
	 *
	 ***********************************************************************/

	final boolean FLAC__ogg_encoder_aspect_init()
	{
		/* we will determine the serial number later if necessary */
		if( this.stream_state.ogg_stream_init( this.serial_number ) != 0 ) {
			return false;
		}

		this.seen_magic = false;
		this.is_first_packet = true;
		this.samples_written = 0;

		return true;
	}

	final void FLAC__ogg_encoder_aspect_finish()
	{
		this.stream_state.ogg_stream_clear();
		/*@@@ what about the page? */
	}

	final void FLAC__ogg_encoder_aspect_set_serial_number(final int value)
	{
		this.serial_number = value;
	}

	final boolean FLAC__ogg_encoder_aspect_set_num_metadata(final int value)
	{
		if( value < (1 << FLAC__OGG_MAPPING_NUM_HEADERS_LEN) ) {
			this.num_metadata = value;
			return true;
		}
		//else
			return false;
	}

	final void FLAC__ogg_encoder_aspect_set_defaults()
	{
		this.serial_number = 0;
		this.num_metadata = 0;
	}

	/**
	 * The basic FLAC -> Ogg mapping goes like this:
	 *
	 * - 'fLaC' magic and STREAMINFO block get combined into the first
	 *   packet.  The packet is prefixed with
	 *   + the one-byte packet type 0x7F
	 *   + 'FLAC' magic
	 *   + the 2 byte Ogg FLAC mapping version number
	 *   + tne 2 byte big-endian # of header packets
	 * - The first packet is flushed to the first page.
	 * - Each subsequent metadata block goes into its own packet.
	 * - Each metadata packet is flushed to page (this is not required,
	 *   the mapping only requires that a flush must occur after all
	 *   metadata is written).
	 * - Each subsequent FLAC audio frame goes into its own packet.
	 *
	 * WATCHOUT:
	 * This depends on the behavior of FLAC__StreamEncoder that we get a
	 * separate write callback for the fLaC magic, and then separate write
	 * callbacks for each metadata block and audio frame.
	 */
	final int /* FLAC__StreamEncoderWriteStatus */ FLAC__ogg_encoder_aspect_write_callback_wrapper(
			final byte buffer[], final int bytes, final int samples,
			final int current_frame, final boolean is_last_block,
			final JFLAC__OggEncoderAspectWriteCallbackProxy write_callback, final Object encoder/*, final Object client_data*/)
	{
		/* WATCHOUT:
		 * This depends on the behavior of FLAC__StreamEncoder that 'samples'
		 * will be 0 for metadata writes.
		 */
		final boolean is_metadata = (samples == 0);

		/*
		 * Treat fLaC magic packet specially.  We will note when we see it, then
		 * wait until we get the STREAMINFO and prepend it in that packet
		 */
		if( this.seen_magic ) {
			final Jogg_packet packet = new Jogg_packet();
			final byte synthetic_first_packet_body[] = new byte[
				FLAC__OGG_MAPPING_PACKET_TYPE_LENGTH +
				FLAC__OGG_MAPPING_MAGIC_LENGTH +
				FLAC__OGG_MAPPING_VERSION_MAJOR_LENGTH +
				FLAC__OGG_MAPPING_VERSION_MINOR_LENGTH +
				FLAC__OGG_MAPPING_NUM_HEADERS_LENGTH +
				Jformat.FLAC__STREAM_SYNC_LENGTH +
				Jformat.FLAC__STREAM_METADATA_HEADER_LENGTH +
				Jformat.FLAC__STREAM_METADATA_STREAMINFO_LENGTH
			];

			packet.clear();
			packet.granulepos = this.samples_written + samples;

			if( this.is_first_packet ) {
				int b = 0;// java: offset to synthetic_first_packet_body;
				if( bytes != Jformat.FLAC__STREAM_METADATA_HEADER_LENGTH + JFLAC__StreamMetadata_StreamInfo.FLAC__STREAM_METADATA_STREAMINFO_LENGTH ) {
					/*
					 * If we get here, our assumption about the way write callbacks happen
					 * (explained above) is wrong
					 */
					//FLAC__ASSERT(0);
					return JFLAC__StreamEncoder.FLAC__STREAM_ENCODER_WRITE_STATUS_FATAL_ERROR;
				}
				/* add first header packet type */
				synthetic_first_packet_body[b] = FLAC__OGG_MAPPING_FIRST_HEADER_PACKET_TYPE;
				b += FLAC__OGG_MAPPING_PACKET_TYPE_LENGTH;
				/* add 'FLAC' mapping magic */
				System.arraycopy( FLAC__OGG_MAPPING_MAGIC, 0, synthetic_first_packet_body, b, FLAC__OGG_MAPPING_MAGIC_LENGTH );
				b += FLAC__OGG_MAPPING_MAGIC_LENGTH;
				/* add Ogg FLAC mapping major version number */
				System.arraycopy( FLAC__OGG_MAPPING_VERSION_MAJOR, 0, synthetic_first_packet_body, b, FLAC__OGG_MAPPING_VERSION_MAJOR_LENGTH );
				b += FLAC__OGG_MAPPING_VERSION_MAJOR_LENGTH;
				/* add Ogg FLAC mapping minor version number */
				System.arraycopy( FLAC__OGG_MAPPING_VERSION_MINOR, 0, synthetic_first_packet_body, b, FLAC__OGG_MAPPING_VERSION_MINOR_LENGTH );
				b += FLAC__OGG_MAPPING_VERSION_MINOR_LENGTH;
				/* add number of header packets */
				synthetic_first_packet_body[b] = (byte)(this.num_metadata >>> 8);
				b++;
				synthetic_first_packet_body[b] = (byte)(this.num_metadata);
				b++;
				/* add native FLAC 'fLaC' magic */
				System.arraycopy( Jformat.FLAC__STREAM_SYNC_STRING, 0, synthetic_first_packet_body, b, Jformat.FLAC__STREAM_SYNC_LENGTH );
				b += Jformat.FLAC__STREAM_SYNC_LENGTH;
				/* add STREAMINFO */
				System.arraycopy( buffer, 0, synthetic_first_packet_body, b, bytes );
				//FLAC__ASSERT(b + bytes - synthetic_first_packet_body == sizeof(synthetic_first_packet_body));
				packet.packet_base = synthetic_first_packet_body;
				packet.packet = 0;
				packet.bytes = synthetic_first_packet_body.length;

				packet.b_o_s = true;
				this.is_first_packet = false;
			}
			else {
				packet.packet_base = buffer;
				packet.packet = 0;
				packet.bytes = bytes;
			}

			if( is_last_block ) {
				/* we used to check:
				 * FLAC__ASSERT(total_samples_estimate == 0 || total_samples_estimate == aspect->samples_written + samples);
				 * but it's really not useful since total_samples_estimate is an estimate and can be inexact
				 */
				packet.e_o_s = true;
			}

			final Jogg_stream_state ogg_state = this.stream_state;// java
			if( ogg_state.ogg_stream_packetin( packet ) != 0 ) {
				return JFLAC__StreamEncoder.FLAC__STREAM_ENCODER_WRITE_STATUS_FATAL_ERROR;
			}

			/*@@@ can't figure out a way to pass a useful number for 'samples' to the write_callback, so we'll just pass 0 */
			if( is_metadata ) {
				while( ogg_state.ogg_stream_flush( this.page ) != 0 ) {
					if( write_callback.write_callback( encoder, this.page.header_base, this.page.header, this.page.header_len, 0, current_frame/*, client_data*/ ) != JFLAC__StreamEncoder.FLAC__STREAM_ENCODER_WRITE_STATUS_OK ) {
						return JFLAC__StreamEncoder.FLAC__STREAM_ENCODER_WRITE_STATUS_FATAL_ERROR;
					}
					if( write_callback.write_callback( encoder, this.page.body_base, this.page.body, this.page.body_len, 0, current_frame/*, client_data*/ ) != JFLAC__StreamEncoder.FLAC__STREAM_ENCODER_WRITE_STATUS_OK ) {
						return JFLAC__StreamEncoder.FLAC__STREAM_ENCODER_WRITE_STATUS_FATAL_ERROR;
					}
				}
			}
			else {
				while( ogg_state.ogg_stream_pageout( this.page ) != 0 ) {
					if( write_callback.write_callback( encoder, this.page.header_base, this.page.header, this.page.header_len, 0, current_frame/*, client_data*/ ) != JFLAC__StreamEncoder.FLAC__STREAM_ENCODER_WRITE_STATUS_OK ) {
						return JFLAC__StreamEncoder.FLAC__STREAM_ENCODER_WRITE_STATUS_FATAL_ERROR;
					}
					if( write_callback.write_callback( encoder, this.page.body_base, this.page.body, this.page.body_len, 0, current_frame/*, client_data*/ ) != JFLAC__StreamEncoder.FLAC__STREAM_ENCODER_WRITE_STATUS_OK ) {
						return JFLAC__StreamEncoder.FLAC__STREAM_ENCODER_WRITE_STATUS_FATAL_ERROR;
					}
				}
			}
		}
		else if( is_metadata && current_frame == 0 && samples == 0 && bytes == 4 && 0 == Jformat.memcmp( buffer, 0, Jformat.FLAC__STREAM_SYNC_STRING, 0, Jformat.FLAC__STREAM_SYNC_STRING.length ) ) {
			this.seen_magic = true;
		}
		else {
			/*
			 * If we get here, our assumption about the way write callbacks happen
			 * explained above is wrong
			 */
			//FLAC__ASSERT(0);
			return JFLAC__StreamEncoder.FLAC__STREAM_ENCODER_WRITE_STATUS_FATAL_ERROR;
		}

		this.samples_written += samples;

		return JFLAC__StreamEncoder.FLAC__STREAM_ENCODER_WRITE_STATUS_OK;
	}

	// ogg_helper.c
	static boolean full_read_(final JFLAC__StreamEncoder encoder, final byte[] buffer, int offset, int bytes,
			final JFLAC__StreamEncoderReadCallback read_callback/*, final Object client_data*/)
	{
		try {
			while( bytes > 0 ) {
				final int bytes_read = read_callback.enc_read_callback( encoder, buffer, offset, bytes/*, client_data*/ );
				if( bytes_read < 0 ) {
					encoder.state = JFLAC__StreamEncoder.FLAC__STREAM_ENCODER_OGG_ERROR;
					return false;
				}
				bytes -= bytes_read;
				offset += bytes_read;
			}
		} catch(final IOException e) {
			if( bytes > 0 ) {
				encoder.state = JFLAC__StreamEncoder.FLAC__STREAM_ENCODER_OGG_ERROR;
				return false;
			}
			encoder.state = JFLAC__StreamEncoder.FLAC__STREAM_ENCODER_CLIENT_ERROR;
			return false;
		} catch(final UnsupportedOperationException e) {
			return false;
		}

		return true;
	}

	static void simple_ogg_page__init(final Jogg_page page)
	{
		page.header_base = null;
		page.header = 0;
		page.header_len = 0;
		page.body_base = null;
		page.body = 0;
		page.body_len = 0;
	}

	static void simple_ogg_page__clear(final Jogg_page page)
	{
		page.header_base = null;
		page.header = 0;
		page.body_base = null;
		page.body = 0;
		simple_ogg_page__init( page );
	}

	private static final int OGG_HEADER_FIXED_PORTION_LEN = 27;
	private static final int OGG_MAX_HEADER_LEN = 27/*OGG_HEADER_FIXED_PORTION_LEN*/ + 255;
	private static final byte[] OggS = {'O','g','g','S'};
	private static final byte[] zeroes8 = {0,0,0,0,0,0,0,0};

	static boolean simple_ogg_page__get_at(final JFLAC__StreamEncoder encoder, final long position, final Jogg_page page,
			final JFLAC__StreamEncoderSeekCallback seek_callback, final JFLAC__StreamEncoderReadCallback read_callback//,
			/* final Object client_data*/)
	{
		//FLAC__ASSERT(page->header == 0);
		//FLAC__ASSERT(page->header_len == 0);
		//FLAC__ASSERT(page->body == 0);
		//FLAC__ASSERT(page->body_len == 0);

		/* move the stream pointer to the supposed beginning of the page */
		if( null == seek_callback ) {
			return false;
		}
		int /* FLAC__StreamEncoderSeekStatus */ seek_status;
		if( (seek_status = seek_callback.enc_seek_callback( (JFLAC__StreamEncoder)encoder, position/*, client_data*/)) != JFLAC__StreamEncoder.FLAC__STREAM_ENCODER_SEEK_STATUS_OK ) {
			if( seek_status == JFLAC__StreamEncoder.FLAC__STREAM_ENCODER_SEEK_STATUS_ERROR ) {
				encoder.state = JFLAC__StreamEncoder.FLAC__STREAM_ENCODER_CLIENT_ERROR;
			}
			return false;
		}

		/* allocate space for the page header */
		page.header_base = new byte[OGG_MAX_HEADER_LEN];
		page.header = 0;

		/* read in the fixed part of the page header (up to but not including
		 * the segment table */
		if( ! full_read_( encoder, page.header_base, page.header, OGG_HEADER_FIXED_PORTION_LEN, read_callback/*, client_data */) ) {
			return false;
		}

		page.header_len = OGG_HEADER_FIXED_PORTION_LEN + (((int)page.header_base[page.header + 26]) & 0xff);

		/* check to see if it's a correct, "simple" page (one packet only) */
		if(
			Jformat.memcmp( page.header_base, page.header, OggS, 0, 4 ) != 0 ||               /* doesn't start with OggS */
			(page.header_base[page.header + 5] & 0x01) != 0 ||                      /* continued packet */
			Jformat.memcmp( page.header_base, page.header + 6, zeroes8, 0, 8 ) != 0 || /* granulepos is non-zero */
			page.header_base[page.header + 26] == 0                            /* packet is 0-size */
		) {
			encoder.state = JFLAC__StreamEncoder.FLAC__STREAM_ENCODER_OGG_ERROR;
			return false;
		}

		/* read in the segment table */
		if( ! full_read_( encoder, page.header_base, page.header + OGG_HEADER_FIXED_PORTION_LEN, ((int)page.header_base[page.header + 26]) & 0xff, read_callback/*, client_data*/ ) ) {
			return false;
		}

		{
			int i;
			final int len = (((int)page.header_base[page.header + 26]) & 0xff) - 1;

			/* check to see that it specifies a single packet */
			for( i = 0; i < len; i++ ) {
				if( (int)page.header_base[page.header + i + OGG_HEADER_FIXED_PORTION_LEN] != -1 ) {// != 255 ) {// java: signed 255 to int = -1
					encoder.state = JFLAC__StreamEncoder.FLAC__STREAM_ENCODER_OGG_ERROR;
					return false;
				}
			}

			page.body_len = 255 * i + (((int)page.header_base[page.header + i + OGG_HEADER_FIXED_PORTION_LEN]) & 0xff);
		}

		/* allocate space for the page body */
		page.body_base = new byte[page.body_len];
		page.body = 0;

		/* read in the page body */
		if( ! full_read_( encoder, page.body_base, page.body, page.body_len, read_callback/*, client_data*/ ) ) {
			return false;
		}

		/* check the CRC */
		final byte crc[] = new byte[4];
		System.arraycopy( page.header_base, page.header + 22, crc, 0, 4 );
		page.ogg_page_checksum_set();
		if( Jformat.memcmp( crc, 0, page.header_base, page.header + 22, 4 ) != 0 ) {
			encoder.state = JFLAC__StreamEncoder.FLAC__STREAM_ENCODER_OGG_ERROR;
			return false;
		}

		return true;
	}

	static boolean simple_ogg_page__set_at(final JFLAC__StreamEncoder encoder, final long position, final Jogg_page page,
			final JFLAC__StreamEncoderSeekCallback seek_callback, final JFLAC__StreamEncoderWriteCallback write_callback//,
			/* final Object client_data*/)
	{
		int /* FLAC__StreamEncoderSeekStatus */ seek_status;

		//FLAC__ASSERT(page->header != 0);
		//FLAC__ASSERT(page->header_len != 0);
		//FLAC__ASSERT(page->body != 0);
		//FLAC__ASSERT(page->body_len != 0);

		/* move the stream pointer to the supposed beginning of the page */
		if( null == seek_callback ) {
			return false;
		}
		if( (seek_status = seek_callback.enc_seek_callback( (JFLAC__StreamEncoder)encoder, position/*, client_data*/) ) != JFLAC__StreamEncoder.FLAC__STREAM_ENCODER_SEEK_STATUS_OK ) {
			if( seek_status == JFLAC__StreamEncoder.FLAC__STREAM_ENCODER_SEEK_STATUS_ERROR ) {
				encoder.state = JFLAC__StreamEncoder.FLAC__STREAM_ENCODER_CLIENT_ERROR;
			}
			return false;
		}

		page.ogg_page_checksum_set();

		/* re-write the page */
		if( write_callback.enc_write_callback( (JFLAC__StreamEncoder)encoder, page.header_base, page.header, page.header_len, 0, 0/*, client_data*/) != JFLAC__StreamEncoder.FLAC__STREAM_ENCODER_WRITE_STATUS_OK ) {
			encoder.state = JFLAC__StreamEncoder.FLAC__STREAM_ENCODER_CLIENT_ERROR;
			return false;
		}
		if( write_callback.enc_write_callback( (JFLAC__StreamEncoder)encoder, page.body_base, page.body, page.body_len, 0, 0/*, client_data*/) != JFLAC__StreamEncoder.FLAC__STREAM_ENCODER_WRITE_STATUS_OK ) {
			encoder.state = JFLAC__StreamEncoder.FLAC__STREAM_ENCODER_CLIENT_ERROR;
			return false;
		}

		return true;
	}
}
