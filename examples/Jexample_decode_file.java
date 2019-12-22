package examples;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import libFLAC.JFLAC__Frame;
import libFLAC.JFLAC__StreamDecoder;
import libFLAC.JFLAC__StreamDecoderErrorCallback;
import libFLAC.JFLAC__StreamDecoderMetadataCallback;
import libFLAC.JFLAC__StreamDecoderWriteCallback;
import libFLAC.JFLAC__StreamMetadata;
import libFLAC.JFLAC__StreamMetadata_StreamInfo;
import libFLAC.Jformat;

//note: very inefficient, too slow
public class Jexample_decode_file extends FileOutputStream implements
	JFLAC__StreamDecoderWriteCallback,
	JFLAC__StreamDecoderMetadataCallback,
	JFLAC__StreamDecoderErrorCallback
{
	private static long total_samples = 0;
	private static int sample_rate = 0;
	private static int channels = 0;
	private static int bps = 0;

	private static void write_little_endian_uint16(final OutputStream f, final short x) throws IOException
	{
		f.write( (int)x );
		f.write( ((int)x >>> 8) );
	}

	private static void write_little_endian_int16(final OutputStream f, final short x) throws IOException
	{
		write_little_endian_uint16( f, x );
	}

	private static void write_little_endian_uint32(final OutputStream f, final int x) throws IOException
	{
		f.write( x );
		f.write( x >>> 8 );
		f.write( x >>> 16 );
		f.write( x >>> 24 );
	}

	public static void main(final String args[])
	{
		boolean ok = true;
		JFLAC__StreamDecoder decoder = null;
		int /*FLAC__StreamDecoderInitStatus*/ init_status;
		Jexample_decode_file fout = null;

		if( args.length != 2 ) {
			System.err.print("usage: java -jar Jexample_decode_file.jar infile.flac outfile.wav\n");
			System.exit( 1 );
			return;
		}

		try {
			fout = new Jexample_decode_file( args[1] );

			decoder = new JFLAC__StreamDecoder();

			decoder.FLAC__stream_decoder_set_md5_checking( true );

			// final Jexample_decode_file callbacks = new Jexample_decode_file();
			init_status = decoder.FLAC__stream_decoder_init_file( args[0], fout/*write_callback*/, fout/*metadata_callback*/, fout/*error_callback*//*, client_data=fout*/ );
			if( init_status != JFLAC__StreamDecoder.FLAC__STREAM_DECODER_INIT_STATUS_OK ) {
				System.err.printf("ERROR: initializing decoder: %s\n", JFLAC__StreamDecoder.FLAC__StreamDecoderInitStatusString[init_status]);
				ok = false;
			}

			if( ok ) {
				ok = decoder.FLAC__stream_decoder_process_until_end_of_stream();
				System.err.printf("decoding: %s\n", ok ? "succeeded" : "FAILED");
				System.err.printf("   state: %s\n", JFLAC__StreamDecoder.FLAC__StreamDecoderStateString[decoder.FLAC__stream_decoder_get_state()]);
			}

			decoder.FLAC__stream_decoder_delete();
		} catch( final IOException e ) {
			System.exit( 0 );
			return;
		} catch( final Exception e) {
			System.err.println( e.getMessage() );
			e.printStackTrace();
		} finally {
			if( fout != null ) {
				try{ fout.close(); } catch( final IOException e ) {}
			}
		}

		System.exit( 0 );
		return;
	}

	private Jexample_decode_file(final String fileName) throws FileNotFoundException {
		super( fileName );
	}

	@SuppressWarnings("boxing")
	@Override
	public int /*FLAC__StreamDecoderWriteStatus*/ dec_write_callback(final JFLAC__StreamDecoder decoder, final JFLAC__Frame frame, final int buffer[][], int offset/*, final Object client_data*/)
	{
		// final OutputStream f = (OutputStream)client_data;// java: this
		final int total_size = (int)(total_samples * channels * (bps >>> 3));
		int end;

		if( total_samples == 0 ) {
			System.err.print("ERROR: this example only works for FLAC files that have a total_samples count in STREAMINFO\n");
			return JFLAC__StreamDecoder.FLAC__STREAM_DECODER_WRITE_STATUS_ABORT;
		}
		if( channels != 2 || bps != 16 ) {
			System.err.print("ERROR: this example only supports 16bit stereo streams\n");
			return JFLAC__StreamDecoder.FLAC__STREAM_DECODER_WRITE_STATUS_ABORT;
		}
		if( frame.header.channels != 2 ) {
			System.err.printf("ERROR: This frame contains %d channels (should be 2)\n", frame.header.channels );
			return JFLAC__StreamDecoder.FLAC__STREAM_DECODER_WRITE_STATUS_ABORT;
		}
		if( buffer [0] == null ) {
			System.err.print("ERROR: buffer [0] is NULL\n");
			return JFLAC__StreamDecoder.FLAC__STREAM_DECODER_WRITE_STATUS_ABORT;
		}
		if( buffer [1] == null ) {
			System.err.print("ERROR: buffer [1] is NULL\n");
			return JFLAC__StreamDecoder.FLAC__STREAM_DECODER_WRITE_STATUS_ABORT;
		}

		try {
			/* write WAVE header before we write the first frame */
			if( frame.header.sample_number == 0 ) {
				this.write( new byte[] {'R','I','F','F'}, 0, 4 );
				write_little_endian_uint32( this, total_size + 36 );
				this.write( new byte[] {'W','A','V','E','f','m','t',' '}, 0, 8 );
				write_little_endian_uint32( this, 16 );
				write_little_endian_uint16( this, (short)1 );
				write_little_endian_uint16( this, (short)channels );
				write_little_endian_uint32( this, sample_rate );
				write_little_endian_uint32( this, sample_rate * channels * (bps >>> 3) );
				write_little_endian_uint16( this, (short)(channels * (bps >>> 3)) ); /* block align */
				write_little_endian_uint16( this, (short)bps );
				this.write( new byte[] {'d','a','t','a'}, 0, 4 );
				write_little_endian_uint32( this, total_size );
			}

			/* write decoded PCM samples */
			for( end = frame.header.blocksize + offset; offset < end; offset++ ) {
				write_little_endian_int16( this, (short)buffer[0][offset] );  /* left channel */
				write_little_endian_int16( this, (short)buffer[1][offset] );  /* right channel */
			}
		} catch(final IOException e) {
			System.err.print("ERROR: write error\n");
			return JFLAC__StreamDecoder.FLAC__STREAM_DECODER_WRITE_STATUS_ABORT;
		}
		return JFLAC__StreamDecoder.FLAC__STREAM_DECODER_WRITE_STATUS_CONTINUE;
	}

	@SuppressWarnings("boxing")
	@Override
	public void dec_metadata_callback(final JFLAC__StreamDecoder decoder, final JFLAC__StreamMetadata metadata/*, final Object client_data*/) throws IOException
	{
		/* print some stats */
		if( metadata.type == Jformat.FLAC__METADATA_TYPE_STREAMINFO ) {
			/* save for later */
			final JFLAC__StreamMetadata_StreamInfo info = (JFLAC__StreamMetadata_StreamInfo) metadata;
			total_samples = info.total_samples;
			sample_rate = info.sample_rate;
			channels = info.channels;
			bps = info.bits_per_sample;

			System.err.printf("sample rate    : %d Hz\n", sample_rate);
			System.err.printf("channels       : %d\n", channels);
			System.err.printf("bits per sample: %d\n", bps);
			System.err.printf("total samples  : %d\n", total_samples);
		}
	}

	@Override
	public void dec_error_callback(final JFLAC__StreamDecoder decoder, final int /*FLAC__StreamDecoderErrorStatus*/ status/*, final Object client_data*/)
	{
		System.err.printf("Got error callback: %s\n", JFLAC__StreamDecoder.FLAC__StreamDecoderErrorStatusString[status]);
	}
}
