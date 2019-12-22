package test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Random;

import libFLAC.JFLAC__Frame;
import libFLAC.JFLAC__Metadata_Chain;
import libFLAC.JFLAC__Metadata_Iterator;
import libFLAC.JFLAC__StreamDecoder;
import libFLAC.JFLAC__StreamDecoderErrorCallback;
import libFLAC.JFLAC__StreamDecoderMetadataCallback;
import libFLAC.JFLAC__StreamDecoderWriteCallback;
import libFLAC.JFLAC__StreamMetadata;
import libFLAC.JFLAC__StreamMetadata_StreamInfo;
import libFLAC.Jformat;

/** test_seeking - Seeking tester for libFLAC
 * Copyright (C) 2004-2009  Josh Coalson
 * Copyright (C) 2011-2016  Xiph.Org Foundation
 */
public class Jtest_seeking
{// FIXME why errors are printed in different streams?
	private static final class DecoderClientData  implements
		JFLAC__StreamDecoderWriteCallback,
		JFLAC__StreamDecoderMetadataCallback,
		JFLAC__StreamDecoderErrorCallback
	{
		int[][] pcm;
		boolean got_data;
		long total_samples;
		int channels;
		int bits_per_sample;
		boolean quiet;
		boolean ignore_errors;
		boolean error_occurred;
		//
		@SuppressWarnings("boxing")
		@Override// JFLAC__StreamDecoderWriteCallback, write_callback_
		public int /* FLAC__StreamDecoderWriteStatus */dec_write_callback(final JFLAC__StreamDecoder decoder,
				final JFLAC__Frame frame, final int buffer[][], final int offset/*, Object client_data*/)
		{
			// final DecoderClientData dcd = (DecoderClientData)client_data;// java: this

			//(void)decoder, (void)buffer;

			/* if( null == dcd ) {
				System.err.print("ERROR: client_data in write callback is NULL\n");
				return JFLAC__StreamDecoder.FLAC__STREAM_DECODER_WRITE_STATUS_ABORT;
			}*/

			if( this.error_occurred ) {
				return JFLAC__StreamDecoder.FLAC__STREAM_DECODER_WRITE_STATUS_ABORT;
			}

			//FLAC__ASSERT(frame->header.number_type == FLAC__FRAME_NUMBER_TYPE_SAMPLE_NUMBER); /* decoder guarantees this */
			if( ! this.quiet ) {
				System.out.printf("frame@%d(%d)... ", frame.header/*.number*/.sample_number, frame.header.blocksize);
			}

			System.out.flush();

			/* check against PCM data if we have it */
			if( this.pcm != null ) {
				int c, i, j;
				final int end = frame.header.blocksize + offset;
				for( c = 0; c < frame.header.channels; c++ ) {
					for( i = (int)frame.header/*.number*/.sample_number, j = offset; j < end; i++, j++ ) {
						if( buffer[c][j] != this.pcm[c][i] ) {
							System.err.printf("ERROR: sample mismatch at sample#%d(%d), channel=%d, expected %d, got %d\n", i, j, c, buffer[c][j], this.pcm[c][i]);
							return JFLAC__StreamDecoder.FLAC__STREAM_DECODER_WRITE_STATUS_ABORT;
						}
					}
				}
			}

			return JFLAC__StreamDecoder.FLAC__STREAM_DECODER_WRITE_STATUS_CONTINUE;
		}

		@Override// JFLAC__StreamDecoderMetadataCallback, metadata_callback_
		public void dec_metadata_callback(final JFLAC__StreamDecoder decoder, final JFLAC__StreamMetadata metadata/*, Object client_data*/)
		{
			// final DecoderClientData dcd = (DecoderClientData)client_data;// java: this

			//(void)decoder;

			/* if( null == dcd ) {
				System.err.print("ERROR: client_data in metadata callback is NULL\n");
				return;
			}*/

			if( this.error_occurred ) {
				return;
			}

			if ( ! this.got_data && metadata.type == Jformat.FLAC__METADATA_TYPE_STREAMINFO ) {
				this.got_data = true;
				final JFLAC__StreamMetadata_StreamInfo stream_info = (JFLAC__StreamMetadata_StreamInfo)metadata;
				this.total_samples = stream_info.total_samples;
				this.channels = stream_info.channels;
				this.bits_per_sample = stream_info.bits_per_sample;
			}
		}

		@SuppressWarnings("boxing")
		@Override// JFLAC__StreamDecoderErrorCallback, error_callback_
		public void dec_error_callback(final JFLAC__StreamDecoder decoder, final int /* FLAC__StreamDecoderErrorStatus */ status/*, Object client_data*/)
		{
			// final DecoderClientData dcd = (DecoderClientData)client_data;// java: this

			//(void)decoder;

			/* if( null == dcd ) {
				System.err.print("ERROR: client_data in metadata callback is NULL\n");
				return;
			}*/

			if( ! this.ignore_errors ) {
				System.out.printf("ERROR: got error callback: err = %d (%s)\n", status, JFLAC__StreamDecoder.FLAC__StreamDecoderErrorStatusString[status]);
				this.error_occurred = true;
			}
		}
	};

	private static boolean stop_signal_ = false;
	private static Random rand = new Random();

	/*
	private static void our_sigint_handler_(int signum)
	{
		//(void)signum;
		System.out.print("(caught SIGINT) ");
		System.out.flush();
		stop_signal_ = true;
	}
	*/

	/*
	private static boolean die_(final String msg)
	{
		System.out.println("ERROR: " + msg);
		return false;
	}
	*/

	@SuppressWarnings("boxing")
	private static boolean die_s_(final String msg, final JFLAC__StreamDecoder decoder)
	{
		final int /* FLAC__StreamDecoderState */ state = decoder.FLAC__stream_decoder_get_state();

		if( msg != null ) {
			System.out.printf("FAILED, %s", msg);
		} else {
			System.out.print("FAILED");
		}

		System.out.printf(", state = %d (%s)\n", state, JFLAC__StreamDecoder.FLAC__StreamDecoderStateString[state]);

		return false;
	}

	@SuppressWarnings({ "boxing", "unused" })
	private static boolean read_pcm_(final int pcm[][], final String rawfilename, final String flacfilename)
	{
		FileInputStream f = null;
		int channels = 0, bps = 0, samples, i, j;

		final long rawfilesize = new File( rawfilename ).length();
		if( rawfilesize < 0 ) {
			System.err.printf("ERROR: can't determine filesize for %s\n", rawfilename);
			return false;
		}
		/* get sample format from flac file; would just use FLAC__metadata_get_streaminfo() except it doesn't work for Ogg FLAC yet */
		{
if( false ) {
			final JFLAC__StreamMetadata_StreamInfo streaminfo = new JFLAC__StreamMetadata_StreamInfo();
			if( ! streaminfo.FLAC__metadata_get_streaminfo( flacfilename ) ) {
				System.err.printf("ERROR: getting STREAMINFO from %s\n", flacfilename);
				return false;
			}
			channels = streaminfo.channels;
			bps = streaminfo.bits_per_sample;
} else {
			boolean ok = true;
			final JFLAC__Metadata_Chain chain = new JFLAC__Metadata_Chain();
			JFLAC__Metadata_Iterator it = null;
			ok &= /*(chain != null) && */(chain.FLAC__metadata_chain_read( flacfilename ) || chain.FLAC__metadata_chain_read_ogg( flacfilename ));
			it = new JFLAC__Metadata_Iterator();
			if( ok ) {
				it.FLAC__metadata_iterator_init( chain );
			}
			ok &= (it.FLAC__metadata_iterator_get_block().type == Jformat.FLAC__METADATA_TYPE_STREAMINFO);
			final JFLAC__StreamMetadata_StreamInfo stream_info = (JFLAC__StreamMetadata_StreamInfo)it.FLAC__metadata_iterator_get_block();
			ok &= (channels = stream_info.channels) != 0;
			ok &= (bps = stream_info.bits_per_sample) != 0;
			it = null;// if( it != null ) JFLAC__Metadata_Iterator.FLAC__metadata_iterator_delete( it );
			if( chain != null ) {
				chain.FLAC__metadata_chain_delete();
			}
			if( ! ok ) {
				System.err.printf("ERROR: getting STREAMINFO from %s\n", flacfilename);
				return false;
			}
}
		}
		if( channels > 2 ) {
			System.err.printf("ERROR: PCM verification requires 1 or 2 channels, got %d\n", channels);
			return false;
		}
		if( bps != 8 && bps != 16 ) {
			System.err.printf("ERROR: PCM verification requires 8 or 16 bps, got %d\n", bps);
			return false;
		}
		samples = (int)(rawfilesize / channels / (bps >> 3));
		if( samples > 10000000 ) {
			System.err.printf("ERROR: %s is too big\n", rawfilename);
			return false;
		}

		try {
			for( i = 0; i < channels; i++ ) {
				pcm[i] = new int[samples];
			}
			f = new FileInputStream( rawfilename );
			/* assumes signed big-endian data */
			if( bps == 8 ) {
				int c;
				for( i = 0; i < samples; i++ ) {
					for( j = 0; j < channels; j++ ) {
						if( (c = f.read()) >= 0 ) {
							pcm[j][i] = c;
						}
					}
				}
			}
			else { /* bps == 16 */
				final byte c[] = new byte[2];// FIXME why unsigned ?
				for( i = 0; i < samples; i++ ) {
					for( j = 0; j < channels; j++ ) {
						if( f.read( c, 0, 2 ) == 2 ) {
							// java uses signed byte, so we can do it easier
							//int value = ((int)c[0] << 8) | (c[1] & 0xff);
							//pcm[j][i] = (value & 0x8000) != 0 ? 0xffff0000 | value : value;
							//
							pcm[j][i] = (c[0] << 8) | (c[1] & 0xff);
						}
					}
				}
			}
		} catch(final OutOfMemoryError e) {
			System.err.print("ERROR: allocating space for PCM samples\n");
			return false;
		} catch(final IOException e) {
			System.err.printf( e.getMessage() );
			return false;
		} finally {
			if( f != null ) {
				try { f.close(); } catch (final IOException e) {}
			}
		}
		return true;
	}

	/** read mode:
	 * 0 - no read after seek
	 * 1 - read 2 frames
	 * 2 - read until end
	 */
	@SuppressWarnings("boxing")
	private static boolean seek_barrage(final boolean is_ogg, final String filename, final long filesize, final int count, final long total_samples, final int read_mode, final int[][] pcm)
	{
		final JFLAC__StreamDecoder decoder;
		final DecoderClientData decoder_client_data = new DecoderClientData();
		int i;
		int n;

		decoder_client_data.pcm = pcm;
		decoder_client_data.got_data = false;
		decoder_client_data.total_samples = 0;
		decoder_client_data.quiet = false;
		decoder_client_data.ignore_errors = false;
		decoder_client_data.error_occurred = false;

		System.out.printf("\n+++ seek test: FLAC__StreamDecoder (%s FLAC, read_mode=%d)\n\n", is_ogg ? "Ogg":"native", read_mode);

		decoder = new JFLAC__StreamDecoder();

		if( is_ogg ) {
			if( decoder.FLAC__stream_decoder_init_ogg_file( filename,
					decoder_client_data,// write_callback_,
					decoder_client_data,// metadata_callback_,
					decoder_client_data//,// error_callback_,
					/* decoder_client_data */ ) != JFLAC__StreamDecoder.FLAC__STREAM_DECODER_INIT_STATUS_OK ) {
				return die_s_("FLAC__stream_decoder_init_file() FAILED", decoder);
			}
		}
		else {
			if( decoder.FLAC__stream_decoder_init_file( filename,
					decoder_client_data,//  write_callback_,
					decoder_client_data,// metadata_callback_,
					decoder_client_data//,// error_callback_,
					/* decoder_client_data */ ) != JFLAC__StreamDecoder.FLAC__STREAM_DECODER_INIT_STATUS_OK ) {
				return die_s_("FLAC__stream_decoder_init_file() FAILED", decoder);
			}
		}

		if( ! decoder.FLAC__stream_decoder_process_until_end_of_metadata() ) {
			return die_s_("FLAC__stream_decoder_process_until_end_of_metadata() FAILED", decoder);
		}

		if( ! is_ogg ) { /* not necessary to do this for Ogg because of its seeking method */
		/* process until end of stream to make sure we can still seek in that state */
			decoder_client_data.quiet = true;
			if( ! decoder.FLAC__stream_decoder_process_until_end_of_stream() ) {
				return die_s_("FLAC__stream_decoder_process_until_end_of_stream() FAILED", decoder);
			}
			decoder_client_data.quiet = false;

			System.out.printf("stream decoder state is %s\n", decoder.FLAC__stream_decoder_get_resolved_state_string());
			if( decoder.FLAC__stream_decoder_get_state() != JFLAC__StreamDecoder.FLAC__STREAM_DECODER_END_OF_STREAM ) {
				return die_s_("expected FLAC__STREAM_DECODER_END_OF_STREAM", decoder);
			}
		}

		System.out.printf("file's total_samples is %d\n", decoder_client_data.total_samples);

		n = (int)decoder_client_data.total_samples;

		if( n == 0 && total_samples >= 0 ) {
			n = (int)total_samples;
		}

		/* if we don't have a total samples count, just guess based on the file size */
		/* @@@ for is_ogg we should get it from last page's granulepos */
		if( n == 0 ) {
			/* 8 would imply no compression, 9 guarantees that we will get some samples off the end of the stream to test that case */
			n = (int)(9 * filesize / (decoder_client_data.channels * decoder_client_data.bits_per_sample));
		}

		System.out.printf("Begin seek barrage, count=%d\n", count);

		for( i = 0; ! stop_signal_ && (count == 0 || i < count); i++ ) {
			long pos;

			/* for the first 10, seek to the first 10 samples */
			if( n >= 10 && i < 10 ) {
				pos = i;
			}
			/* for the second 10, seek to the last 10 samples */
			else if( n >= 10 && i < 20 ) {
				pos = n - 1 - (i - 10);
			}
			/* for the third 10, seek past the end and make sure we fail properly as expected */
			else if( i < 30 ) {
				pos = n + (i - 20);
			}
			else {
				pos = (rand.nextInt( Integer.MAX_VALUE ) % n);
			}

			System.out.printf("#%d:seek(%d)... ", i, pos);

			System.out.flush();

			if( ! decoder.FLAC__stream_decoder_seek_absolute( pos ) ) {
				if( pos >= n ) {
					System.out.print("seek past end failed as expected... ");
				} else if( decoder_client_data.total_samples == 0 && total_samples <= 0 ) {
					System.out.print("seek failed, assuming it was past EOF... ");
				} else {
					return die_s_("FLAC__stream_decoder_seek_absolute() FAILED", decoder);
				}
				if( ! decoder.FLAC__stream_decoder_flush() ) {
					return die_s_("FLAC__stream_decoder_flush() FAILED", decoder);
				}
			}
			else if( read_mode == 1 ) {
				System.out.print("decode_frame... ");
				System.out.flush();
				if( ! decoder.FLAC__stream_decoder_process_single() ) {
					return die_s_("FLAC__stream_decoder_process_single() FAILED", decoder);
				}

				System.out.print("decode_frame... ");
				System.out.flush();
				if( ! decoder.FLAC__stream_decoder_process_single() ) {
					return die_s_("FLAC__stream_decoder_process_single() FAILED", decoder);
				}
			}
			else if(read_mode == 2) {
				System.out.printf("decode_all... ");
				System.out.flush();
				decoder_client_data.quiet = true;
				if( ! decoder.FLAC__stream_decoder_process_until_end_of_stream() ) {
					return die_s_("FLAC__stream_decoder_process_until_end_of_stream() FAILED", decoder);
				}
				decoder_client_data.quiet = false;
			}

			System.out.print("OK\n");
			System.out.flush();
		}
		stop_signal_ = false;

		if( decoder.FLAC__stream_decoder_get_state() != JFLAC__StreamDecoder.FLAC__STREAM_DECODER_UNINITIALIZED) {
			if( ! decoder.FLAC__stream_decoder_finish() ) {
				return die_s_("FLAC__stream_decoder_finish() FAILED", decoder);
			}
		}

		decoder.FLAC__stream_decoder_delete();
		System.out.print("\nPASSED!\n");

		return true;
	}

	public static void main(final String args[])
	{
		String flacfilename, rawfilename = null;
		int count = 0, read_mode;
		long samples = -1;
		long flacfilesize;
		final int pcm[][] = new int[][] { null, null };
		boolean ok = true;

		if( args.length < 1 || args.length > 4 ) {
			System.err.print("usage: java -jar Jtest_seeking.jar file.flac [#seeks] [#samples-in-file.flac] [file.raw]\n");
			System.exit( 1 );
			return;
		}

		flacfilename = args[0];

		if( args.length > 1 ) {
			count = Integer.parseInt( args[1] );
		}

		if( args.length > 2 ) {
			samples = Long.parseLong( args[2] );
		}

		if( args.length > 3 ) {
			rawfilename = args[3];
		}

		if( count < 30 ) {
			System.err.print("WARNING: random seeks don't kick in until after 30 preprogrammed ones\n");
		}

		rand.setSeed( System.currentTimeMillis() );

		flacfilesize = new File( flacfilename ).length();
		if( flacfilesize < 0 ) {
			System.err.printf("ERROR: can't determine filesize for %s\n", flacfilename);
			System.exit( 1 );
			return;
		}

		if( rawfilename != null && ! read_pcm_( pcm, rawfilename, flacfilename ) ) {
			pcm[0] = null;
			pcm[1] = null;
			System.exit( 1 );
			return;
		}

		//(void) signal(SIGINT, our_sigint_handler_);// java: hook already running

		for( read_mode = 0; ok && read_mode <= 2; read_mode++ ) {
			/* no need to do "decode all" read_mode if PCM checking is available */
			if( rawfilename != null && read_mode > 1 ) {
				continue;
			}
			if( flacfilename.length() > 4 && (flacfilename.endsWith(".oga") || flacfilename.endsWith(".ogg")) ) {
if( Jformat.FLAC__HAS_OGG ) {
				ok = seek_barrage(/*is_ogg=*/true, flacfilename, flacfilesize, count, samples, read_mode, rawfilename != null ? pcm : null );
} else {
				System.err.print("ERROR: Ogg FLAC not supported\n");
				ok = false;
}
			}
			else {
				ok = seek_barrage(/*is_ogg=*/false, flacfilename, flacfilesize, count, samples, read_mode, rawfilename != null ? pcm : null );
			}
		}

		pcm[0] = null;
		pcm[1] = null;

		System.exit( ok ? 0 : 2 );
		return;
	}

}
