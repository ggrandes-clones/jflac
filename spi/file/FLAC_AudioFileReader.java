package spi.file;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.sound.sampled.spi.AudioFileReader;

import spi.convert.FLAC_FormatConversionProvider;

import libFLAC.JFLAC__Frame;
import libFLAC.JFLAC__StreamDecoder;
import libFLAC.JFLAC__StreamDecoderErrorCallback;
import libFLAC.JFLAC__StreamDecoderWriteCallback;

public class FLAC_AudioFileReader extends AudioFileReader implements
	JFLAC__StreamDecoderWriteCallback,// only to init
	JFLAC__StreamDecoderErrorCallback// only to init
{
	// there is a real problem: a decoder must process all metadata block. this block can have a huge size.
	private static final int MAX_BUFFER = 1 << 19;// FIXME a metadata block can be 1 << 24.

	@Override
	public int dec_write_callback(final JFLAC__StreamDecoder decoder,
			final JFLAC__Frame frame, final int[][] buffer, final int offset/*, final Object client_data*/) {
		return 0;
	}
	@Override// JFLAC__StreamDecoderErrorCallback
	public void dec_error_callback(final JFLAC__StreamDecoder decoder, final int status/*, final Object client_data*/) throws IOException {
		throw new IOException();
	}

	@Override
	public AudioFileFormat getAudioFileFormat(final InputStream stream)
			throws UnsupportedAudioFileException, IOException {

		final JFLAC__StreamDecoder decoder = new JFLAC__StreamDecoder();

		decoder.FLAC__stream_decoder_set_md5_checking( true );

		final int init_status = decoder.FLAC__stream_decoder_init_FILE( stream, this/*write_callback*/, null/*metadata_callback*/, this/*error_callback*//*, client_data=null*/ );
		if( init_status == JFLAC__StreamDecoder.FLAC__STREAM_DECODER_INIT_STATUS_OK ) {
			if( decoder.FLAC__stream_decoder_process_until_end_of_metadata() &&
				decoder.FLAC__stream_decoder_get_state() < JFLAC__StreamDecoder.FLAC__STREAM_DECODER_END_OF_STREAM &&
				decoder.FLAC__stream_decoder_skip_single_frame() &&
				decoder.FLAC__stream_decoder_get_state() != JFLAC__StreamDecoder.FLAC__STREAM_DECODER_END_OF_STREAM )
			{
				// can be added properties with additional information
				final AudioFormat af = new AudioFormat( FLAC_FormatConversionProvider.ENCODING,
						decoder.FLAC__stream_decoder_get_sample_rate(),
						decoder.FLAC__stream_decoder_get_bits_per_sample(),
						decoder.FLAC__stream_decoder_get_channels(),
						1,
						decoder.FLAC__stream_decoder_get_sample_rate(),
						false );
				final AudioFileFormat aff = new AudioFileFormat(
						new AudioFileFormat.Type("FLAC", ""), af, AudioSystem.NOT_SPECIFIED );

				decoder.FLAC__stream_decoder_delete( false );
				return aff;
			}
		}
		decoder.FLAC__stream_decoder_delete( false );
		throw new UnsupportedAudioFileException();
	}

	@Override
	public AudioFileFormat getAudioFileFormat(final URL url)
			throws UnsupportedAudioFileException, IOException {

		InputStream is = null;
		try {
			is = url.openStream();
			return getAudioFileFormat( is );
		} catch(final UnsupportedAudioFileException e) {
			throw e;
		} catch(final IOException e) {
			throw e;
		} finally {
			if( is != null ) {
				try{ is.close(); } catch(final IOException e) {}
			}
		}
	}

	@Override
	public AudioFileFormat getAudioFileFormat(final File file)
			throws UnsupportedAudioFileException, IOException {

		BufferedInputStream is = null;
		try {// there is real problem: decoder must process all metadata block. this block can have huge size.
			is = new BufferedInputStream( new FileInputStream( file ), MAX_BUFFER );
			return getAudioFileFormat( is );
		} catch(final UnsupportedAudioFileException e) {
			throw e;
		} catch(final IOException e) {
			throw e;
		} finally {
			if( is != null ) {
				try{ is.close(); } catch(final IOException e) {}
			}
		}
	}

	@Override
	public AudioInputStream getAudioInputStream(final InputStream stream)
			throws UnsupportedAudioFileException, IOException {

		// doc says: If the input stream does not support this, this method may fail with an IOException.
		// if( ! stream.markSupported() ) stream = new BufferedInputStream( stream, Jformat.FLAC__MAX_BLOCK_SIZE * 2 );// possible resources leak
		try {
			stream.mark( MAX_BUFFER );
			final AudioFileFormat af = getAudioFileFormat( stream );
			stream.reset();// to start read header again
			return new AudioInputStream( stream, af.getFormat(), af.getFrameLength() );
		} catch(final UnsupportedAudioFileException e) {
			stream.reset();
			throw e;
		} catch(final IOException e) {
			System.out.println( e.getMessage() );
			stream.reset();
			throw e;
		}
	}

	@Override
	public AudioInputStream getAudioInputStream(final URL url)
			throws UnsupportedAudioFileException, IOException {

		InputStream is = null;
		try {
			is = url.openStream();
			return getAudioInputStream( is );
		} catch(final UnsupportedAudioFileException e) {
			if( is != null ) {
				try{ is.close(); } catch(final IOException ie) {}
			}
			throw e;
		} catch(final IOException e) {
			if( is != null ) {
				try{ is.close(); } catch(final IOException ie) {}
			}
			throw e;
		}
	}

	@Override
	public AudioInputStream getAudioInputStream(final File file)
			throws UnsupportedAudioFileException, IOException {

		BufferedInputStream is = null;
		try {// there is real problem: decoder must process all metadata block. this block can have huge size.
			is = new BufferedInputStream( new FileInputStream( file ), MAX_BUFFER );
			return getAudioInputStream( is );
		} catch(final UnsupportedAudioFileException e) {
			if( is != null ) {
				try{ is.close(); } catch(final IOException ie) {}
			}
			throw e;
		} catch(final IOException e) {
			if( is != null ) {
				try{ is.close(); } catch(final IOException ie) {}
			}
			throw e;
		}
	}
}
