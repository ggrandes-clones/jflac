package spi.file;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.sound.sampled.AudioFileFormat.Type;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioFormat.Encoding;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.spi.AudioFileWriter;

import libFLAC.JFLAC__StreamEncoder;
import libFLAC.JFLAC__StreamEncoderSeekCallback;
import libFLAC.JFLAC__StreamEncoderTellCallback;
import libFLAC.JFLAC__StreamEncoderWriteCallback;
import libFLAC.RandomAccessInputOutputStream;

public class FLAC_AudioFileWriter extends AudioFileWriter implements
	JFLAC__StreamEncoderWriteCallback,
	JFLAC__StreamEncoderSeekCallback,
	JFLAC__StreamEncoderTellCallback
{
	private final Type[] FLAC = { new Type("FLAC", "flac") };
	private static final int COMPLESSION_LEVEL = 8;
	private OutputStream mOut = null;

	@Override
	public Type[] getAudioFileTypes() {
		return FLAC;
	}

	@Override
	public Type[] getAudioFileTypes(final AudioInputStream stream) {
		return FLAC;
	}

	private static boolean format_input(final byte[] scbuffer, final int dest[][], final int wide_samples, final boolean is_big_endian, final boolean is_unsigned_samples, final int channels, final int bytes_ps)
	{
		int wide_sample, sample, channel, ibyte;
		if( bytes_ps == 1 ) {
			if( is_unsigned_samples ) {
				for( sample = wide_sample = 0; wide_sample < wide_samples; wide_sample++ ) {
					for( channel = 0; channel < channels; channel++, sample++ ) {
						dest[channel][wide_sample] = ((int)scbuffer[sample] & 0xff) - 0x80;
					}
				}
			} else {
				for( sample = wide_sample = 0; wide_sample < wide_samples; wide_sample++ ) {
					for( channel = 0; channel < channels; channel++, sample++) {
						dest[channel][wide_sample] = (int)scbuffer[sample];
					}
				}
			}
		} else if( bytes_ps == 2 ) {
			if( is_big_endian ) {
				final int bytes = (wide_samples * channels) << 1/* ( * bytes_ps)*/;
				for( ibyte = 0; ibyte < bytes; ibyte += 2 ) {
					final byte tmp = scbuffer[ibyte];
					scbuffer[ibyte] = scbuffer[ibyte + 1];
					scbuffer[ibyte + 1] = tmp;
				}
			}
			if( is_unsigned_samples ) {
				for( sample = wide_sample = 0; wide_sample < wide_samples; wide_sample++ ) {
					for( channel = 0; channel < channels; channel++ ) {
						int tmp = (int)scbuffer[sample++] & 0xff;
						tmp |= ((int)scbuffer[sample++] & 0xff) << 8;
						tmp -= 0x8000;
						dest[channel][wide_sample] = tmp;
					}
				}
			} else {
				for( sample = wide_sample = 0; wide_sample < wide_samples; wide_sample++ ) {
					for( channel = 0; channel < channels; channel++ ) {
						int tmp = (int)scbuffer[sample++] & 0xff;
						tmp |= ((int)scbuffer[sample++]) << 8;
						dest[channel][wide_sample] = tmp;
					}
				}
			}
		} else if( bytes_ps == 3 ) {
			if( ! is_big_endian ) {
				final int bytes = wide_samples * channels * 3;
				for( ibyte = 0; ibyte < bytes; ibyte += 3 ) {
					final byte tmp = scbuffer[ibyte];
					scbuffer[ibyte] = scbuffer[ibyte + 2];
					scbuffer[ibyte + 2] = tmp;
				}
			}
			if( is_unsigned_samples ) {
				for( ibyte = wide_sample = 0; wide_sample < wide_samples; wide_sample++ ) {
					for( channel = 0; channel < channels; channel++ ) {
						int tmp = (int)scbuffer[ibyte++] & 0xff;
						tmp |= ((int)scbuffer[ibyte++] & 0xff) << 8;
						tmp |= ((int)scbuffer[ibyte++] & 0xff) << 16;
						tmp -= 0x800000;
						dest[channel][wide_sample] = tmp;
					}
				}
			} else {
				for( ibyte = wide_sample = 0; wide_sample < wide_samples; wide_sample++ ) {
					for( channel = 0; channel < channels; channel++ ) {
						int tmp = (int)scbuffer[ibyte++] & 0xff;
						tmp |= ((int)scbuffer[ibyte++] & 0xff) << 8;
						tmp |= ((int)scbuffer[ibyte++]) << 16;
						dest[channel][wide_sample] = tmp;
					}
				}
			}
		} else {
			//FLAC__ASSERT(0);
		}
		return true;
	}

	@Override//implements JFLAC__StreamEncoderWriteCallback, file_write_callback_
	public int /* FLAC__StreamEncoderWriteStatus */ enc_write_callback(final JFLAC__StreamEncoder encoder,
			final byte buffer[], final int offset, final int bytes, final int samples, final int current_frame/*, final Object client_data*/)
	{
		// final OutputStream out = (OutputStream) client_data;// java: this

		try {
			mOut.write( buffer, offset, bytes );
		} catch(final IOException e) {
			return JFLAC__StreamEncoder.FLAC__STREAM_ENCODER_WRITE_STATUS_FATAL_ERROR;
		}
		return JFLAC__StreamEncoder.FLAC__STREAM_ENCODER_WRITE_STATUS_OK;
	}
	@Override
	public int write(final AudioInputStream stream, final Type fileType, final OutputStream out) throws IOException {
		if( ! fileType.equals( FLAC[0] ) ) {
			throw new IllegalArgumentException();
		}
		final AudioFormat af = stream.getFormat();
		final boolean is_big_endian = af.isBigEndian();
		final boolean is_unsigned = (af.getEncoding() == Encoding.PCM_UNSIGNED);
		int bps = af.getSampleSizeInBits();
		final int channels = af.getChannels();
		boolean ok = true;
		final JFLAC__StreamEncoder encoder = new JFLAC__StreamEncoder();
		ok &= encoder.FLAC__stream_encoder_set_verify( true );
		int compression = COMPLESSION_LEVEL;
		if( fileType instanceof EncoderFileFormatType ) {// get user input
			compression = (int)((EncoderFileFormatType) fileType).mStreamTypeParameter;
		}
		ok &= encoder.FLAC__stream_encoder_set_compression_level( compression );
		ok &= encoder.FLAC__stream_encoder_set_channels( channels );
		ok &= encoder.FLAC__stream_encoder_set_bits_per_sample( bps );
		ok &= encoder.FLAC__stream_encoder_set_sample_rate( (int)af.getSampleRate() );
		//ok &= encoder.FLAC__stream_encoder_set_total_samples_estimate( total_samples );

		/* initialize encoder */
		if( ok ) {
			mOut = out;
			final int init_status = encoder.FLAC__stream_encoder_init_stream( this /*write_callback*/, this /*seek_callback*/, this /*tell_callback*/, null /*metadata_callback*//*, out*/ );
			if( init_status != JFLAC__StreamEncoder.FLAC__STREAM_ENCODER_INIT_STATUS_OK ) {
				System.err.println("ERROR: initializing encoder: " + JFLAC__StreamEncoder.FLAC__StreamEncoderInitStatusString[init_status] );
				ok = false;
			}
		}

		/* read blocks of samples from WAVE file and feed to encoder */
		if( ok ) {
			final byte buffer[] = new byte[4096];
			bps >>>= 3;
			final int wide_samples = buffer.length / bps / channels;
			final int[][] pcm = new int[channels][wide_samples];
			int readed;
			while( ok && (readed = stream.read( buffer, 0, buffer.length )) >= 0 ) {
				ok = format_input( buffer, pcm, wide_samples, is_big_endian, is_unsigned, channels, bps );
				if( ok ) {
					ok = encoder.FLAC__stream_encoder_process( pcm, readed / channels / bps );
				}
			}
			encoder.FLAC__stream_encoder_finish();
			encoder.FLAC__stream_encoder_delete();
		}
		return 0;
	}

	@Override
	public int write(final AudioInputStream stream, final Type fileType, final File file) throws IOException {
		if( ! fileType.equals( FLAC[0] ) ) {
			throw new IllegalArgumentException();
		}
		FileOutputStream outs = null;
		try {
			outs = new FileOutputStream( file );
			return write( stream, fileType, outs );
		} catch(final IOException e) {
			throw e;
		} finally {
			if( outs != null ) {
				try { outs.close(); } catch( final IOException e ) {}
			}
		}
	}

	@Override
	public long enc_tell_callback(final JFLAC__StreamEncoder encoder/*, final Object client_data*/) throws IOException {
		final OutputStream f = mOut;// (OutputStream) client_data;
		if( f instanceof RandomAccessInputOutputStream ) {
			return ((RandomAccessInputOutputStream) f).getFilePointer();
		}
		throw new UnsupportedOperationException();
	}

	@Override
	public int enc_seek_callback(final JFLAC__StreamEncoder encoder, final long absolute_byte_offset/*, final Object client_data*/) {
		final OutputStream f = mOut;// (OutputStream) client_data;
		if( f instanceof RandomAccessInputOutputStream ) {
			try {
				((RandomAccessInputOutputStream) f).seek( absolute_byte_offset );
			} catch(final IOException e) {
				return JFLAC__StreamEncoder.FLAC__STREAM_ENCODER_SEEK_STATUS_ERROR;
			}
			return JFLAC__StreamEncoder.FLAC__STREAM_ENCODER_SEEK_STATUS_OK;
		}
		return JFLAC__StreamEncoder.FLAC__STREAM_ENCODER_SEEK_STATUS_ERROR;
	}
}
