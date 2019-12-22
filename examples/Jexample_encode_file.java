package examples;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import libFLAC.JFLAC__StreamEncoder;
import libFLAC.JFLAC__StreamEncoderProgressCallback;
import libFLAC.JFLAC__StreamMetadata;
import libFLAC.JFLAC__StreamMetadata_VorbisComment;
import libFLAC.Jformat;

public class Jexample_encode_file implements JFLAC__StreamEncoderProgressCallback {
	private static final int READSIZE = 1024;

	private static int total_samples = 0; /* can use a 32-bit number due to WAVE size limitations */
	private static final byte buffer[] = new byte[READSIZE/*samples*/ * 2/*bytes_per_sample*/ * 2/*channels*/]; /* we read the WAVE data into here */
	private static final int pcm[] = new int[READSIZE/*samples*/ * 2/*channels*/];

	public static void main(final String args[])
	{
		boolean ok = true;
		final JFLAC__StreamEncoder encoder;
		int /* JFLAC__StreamEncoderInitStatus */ init_status;
		final JFLAC__StreamMetadata metadata[] = new JFLAC__StreamMetadata[2];
		String /* FLAC__StreamMetadata_VorbisComment_Entry */ entry;// java: changed to String
		InputStream fin = null;
		int sample_rate = 0;
		int channels = 0;
		int bps = 0;

		if( args.length != 2 ) {
			System.err.print("usage: java -jar Jexample_encode_file.jar infile.wav outfile.flac\n");
			System.exit( 1 );
			return;
		}

		try {
			fin = new FileInputStream( args[0] );

			/* read wav header and validate it */
			if(
				fin.read( buffer, 0, 44 ) != 44 ||
				buffer[0] != 'R' || buffer[1] != 'I' || buffer[2] != 'F' || buffer[3] != 'F' ||
				Jformat.memcmp( buffer, 8, new byte[] {'W','A','V','E','f','m','t',' ','\020','\000','\000','\000','\001','\000','\002','\000'}, 0, 16 ) != 0 ||
				Jformat.memcmp( buffer, 32, new byte[] {'\004','\000','\020','\000','d','a','t','a'}, 0, 8 ) != 0
			) {
				System.err.print("ERROR: invalid/unsupported WAVE file, only 16bps stereo WAVE in canonical form allowed\n");
				//fclose( fin );// java: finally
				System.exit( 1 );
				return;
			}
			sample_rate = (((((((int)buffer[27] & 0xff) << 8) | ((int)buffer[26] & 0xff)) << 8) | ((int)buffer[25]) & 0xff) << 8) | ((int)buffer[24] & 0xff);
			channels = 2;
			bps = 16;
			total_samples = ((((((((int)buffer[43] & 0xff) << 8) | ((int)buffer[42] & 0xff)) << 8) | ((int)buffer[41] & 0xff)) << 8) | ((int)buffer[40] & 0xff)) / 4;

			/* allocate the encoder */
			encoder = new JFLAC__StreamEncoder();

			ok &= encoder.FLAC__stream_encoder_set_verify( true );
			ok &= encoder.FLAC__stream_encoder_set_compression_level( 5 );
			ok &= encoder.FLAC__stream_encoder_set_channels( channels );
			ok &= encoder.FLAC__stream_encoder_set_bits_per_sample( bps );
			ok &= encoder.FLAC__stream_encoder_set_sample_rate( sample_rate );
			ok &= encoder.FLAC__stream_encoder_set_total_samples_estimate( total_samples );

			/* now add some metadata; we'll add some tags and a padding block */
			if( ok ) {
				if(
					(metadata[0] = JFLAC__StreamMetadata.FLAC__metadata_object_new( Jformat.FLAC__METADATA_TYPE_VORBIS_COMMENT )) == null ||
					(metadata[1] = JFLAC__StreamMetadata.FLAC__metadata_object_new( Jformat.FLAC__METADATA_TYPE_PADDING )) == null ||
					/* there are many tag (vorbiscomment) functions but these are convenient for this particular use: */
					null == (entry = JFLAC__StreamMetadata_VorbisComment.FLAC__metadata_object_vorbiscomment_entry_from_name_value_pair( "ARTIST", "Some Artist" )) ||
					! ((JFLAC__StreamMetadata_VorbisComment)metadata[0]).FLAC__metadata_object_vorbiscomment_append_comment( entry, /*copy=*/false ) || /* copy=false: let metadata object take control of entry's allocated string */
					null == (entry = JFLAC__StreamMetadata_VorbisComment.FLAC__metadata_object_vorbiscomment_entry_from_name_value_pair( "YEAR", "1984" )) ||
					! ((JFLAC__StreamMetadata_VorbisComment)metadata[0]).FLAC__metadata_object_vorbiscomment_append_comment( entry, /*copy=*/false )
				) {
					System.err.print("ERROR: out of memory or tag error\n");
					ok = false;
				}

				metadata[1].length = 1234; /* set the padding length */

				ok = encoder.FLAC__stream_encoder_set_metadata( metadata, 2 );
			}

			/* initialize encoder */
			if( ok ) {
				init_status = encoder.FLAC__stream_encoder_init_file( args[1], new Jexample_encode_file() /*progress_callback*//*, client_data=null*/ );
				if( init_status != JFLAC__StreamEncoder.FLAC__STREAM_ENCODER_INIT_STATUS_OK ) {
					System.err.printf("ERROR: initializing encoder: %s\n", JFLAC__StreamEncoder.FLAC__StreamEncoderInitStatusString[init_status] );
					ok = false;
				}
			}

			/* read blocks of samples from WAVE file and feed to encoder */
			if( ok ) {
				int left = total_samples;
				while( ok && left != 0 ) {
					int need = (left > READSIZE ? READSIZE : left) * channels * (bps >>> 3);
					if( fin.read( buffer, 0, need ) != need ) {
						System.err.print("ERROR: reading from WAVE file\n");
						ok = false;
					}
					else {
						/* convert the packed little-endian 16-bit PCM samples from WAVE into an interleaved FLAC__int32 buffer for libFLAC */
						need /= (bps >>> 3);
						int i, i2;
						for( i = 0; i < need; i++ ) {
							i2 = i << 1;
							/* inefficient but simple and works on big- or little-endian machines */
							pcm[i] = (((int)buffer[i2 + 1] << 8) | ((int)buffer[i2] & 0xff));
						}
						/* feed samples to encoder */
						need /= channels;
						ok = encoder.FLAC__stream_encoder_process_interleaved( pcm, need );
					}
					left -= need;
				}
			}

			ok &= encoder.FLAC__stream_encoder_finish();

			System.err.printf("encoding: %s\n", ok ? "succeeded" : "FAILED");
			System.err.printf("   state: %s\n", JFLAC__StreamEncoder.FLAC__StreamEncoderStateString[encoder.FLAC__stream_encoder_get_state()]);

			/* now that encoding is finished, the metadata can be freed */
			JFLAC__StreamMetadata.FLAC__metadata_object_delete( metadata[0] );
			JFLAC__StreamMetadata.FLAC__metadata_object_delete( metadata[1] );

			encoder.FLAC__stream_encoder_delete();

			System.exit( 0 );
			return;
		} catch(final Exception e) {
			System.err.println( e.getMessage() );
			e.printStackTrace();
		} finally {
			if( fin != null ) {
				try{ fin.close(); } catch( final IOException e ) {}
			}
		}
		System.exit( 1 );
		return;
	}

	@SuppressWarnings("boxing")
	@Override
	public void enc_progress_callback(final JFLAC__StreamEncoder encoder,
			final long bytes_written, final long samples_written, final int frames_written, final int total_frames_estimate/*, final Object client_data*/)
	{
		System.err.printf("wrote %d bytes, %d/%d samples, %d/%d frames\n", bytes_written, samples_written, total_samples, frames_written, total_frames_estimate);
	}
}
