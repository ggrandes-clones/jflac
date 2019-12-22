package spi.convert;

import java.io.IOException;
import java.io.InputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioFormat.Encoding;
import javax.sound.sampled.AudioInputStream;

import libFLAC.JFLAC__Frame;
import libFLAC.JFLAC__StreamDecoder;
import libFLAC.JFLAC__StreamDecoderErrorCallback;
import libFLAC.JFLAC__StreamDecoderWriteCallback;
import libFLAC.Jformat;

public final class FLAC_DecodedAudioInputStream extends AudioInputStream implements
	JFLAC__StreamDecoderWriteCallback,
	JFLAC__StreamDecoderErrorCallback
{
	private JFLAC__StreamDecoder mDecoder = new JFLAC__StreamDecoder();
	private final boolean mIsBigEndian;
	private final boolean mIsUnsigned;
	private final byte mBuffer[] = new byte[Jformat.FLAC__MAX_BLOCK_SIZE * Jformat.FLAC__MAX_CHANNELS * (Integer.SIZE >>> 3)]; /* WATCHOUT: can be up to 2 megs */
	private int mBufferReadPosition;
	private int mBytesInBuffer;
	//
	@Override// JFLAC__StreamDecoderErrorCallback
	public void dec_error_callback(final JFLAC__StreamDecoder decoder, final int status/*, final Object client_data*/) {
	}
	//
	public FLAC_DecodedAudioInputStream(final InputStream stream,
					final AudioFormat format, final long length) {
		super( stream, format, length );
		mIsBigEndian = format.isBigEndian();
		mIsUnsigned = format.getEncoding() != Encoding.PCM_SIGNED;
		mBufferReadPosition = 0;
		mBytesInBuffer = 0;
		mDecoder.FLAC__stream_decoder_set_md5_checking( true );
		final int init_status = mDecoder.FLAC__stream_decoder_init_FILE( stream, this/*write_callback*/, null/*metadata_callback*/, this/*error_callback*//*, client_data=null*/ );
		if( init_status != JFLAC__StreamDecoder.FLAC__STREAM_DECODER_INIT_STATUS_OK ) {
			mDecoder.FLAC__stream_decoder_delete();
		}
	}
	@Override
	public void close() throws IOException {
		if( mDecoder != null ) {
			mDecoder.FLAC__stream_decoder_delete();
		}
		mDecoder = null;
		super.close();
	}
	@Override
	public boolean markSupported() {
		return false;
	}
	@Override
	public int read() throws IOException {
		final byte[] data = new byte[1];
		if( read( data ) <= 0 ) {// we have a weird situation if read(byte[]) returns 0!
			return -1;
		}
		return ((int) data[0]) & 0xff;
	}
	/* @Override
	public int read(final byte[] b) throws IOException {
		return read( b, 0, b.length );
	} */
	@Override
	public int read(final byte[] b, int off, int len) throws IOException {
		final int bytes_in_buffer = mBytesInBuffer - mBufferReadPosition;
		if( len <= bytes_in_buffer ) {
			System.arraycopy( mBuffer, mBufferReadPosition, b, off, len );
			mBufferReadPosition += len;
			return len;
		}
		System.arraycopy( mBuffer, mBufferReadPosition, b, off, bytes_in_buffer );
		mBytesInBuffer = 0;
		while( mBytesInBuffer == 0 &&
				mDecoder.FLAC__stream_decoder_process_single() &&
				mDecoder.FLAC__stream_decoder_get_state() != JFLAC__StreamDecoder.FLAC__STREAM_DECODER_END_OF_STREAM ) {
		}
		if( mBytesInBuffer != 0 ) {
			off += bytes_in_buffer;
			len -= bytes_in_buffer;
			if( len > mBytesInBuffer ) {
				len = mBytesInBuffer;
			}
			System.arraycopy( mBuffer, 0, b, off, len );
			mBufferReadPosition = len;
			return bytes_in_buffer + len;
		}
		mDecoder.FLAC__stream_decoder_delete();
		mDecoder = null;
		return -1;
	}

	@Override
	public int /*FLAC__StreamDecoderWriteStatus*/ dec_write_callback(final JFLAC__StreamDecoder decoder,
			final JFLAC__Frame frame, final int buffer[][], final int offset/*, final Object client_data*/)
	{
		int wide_sample, channel, ibyte;
		final int bps = frame.header.bits_per_sample, channels = frame.header.channels;
		final int wide_samples = frame.header.blocksize;
		final byte[] s8buffer = mBuffer;
		int bytes_to_write = 0;
		/* generic code for the rest */
		if( bps == 16 ) {
			if( mIsUnsigned ) {
				if( channels == 2 ) {
					for( wide_sample = 0; wide_sample < wide_samples; wide_sample++ ) {
						int s = (buffer[0][wide_sample] + 0x8000);
						s8buffer[bytes_to_write++] = (byte)s;
						s8buffer[bytes_to_write++] = (byte)(s >>> 8);
						s = (buffer[1][wide_sample] + 0x8000);
						s8buffer[bytes_to_write++] = (byte)s;
						s8buffer[bytes_to_write++] = (byte)(s >>> 8);
					}
				}
				else if( channels == 1 ) {
					for( wide_sample = 0; wide_sample < wide_samples; wide_sample++ ) {
						final int s = (buffer[0][wide_sample] + 0x8000);
						s8buffer[bytes_to_write++] = (byte)s;
						s8buffer[bytes_to_write++] = (byte)(s >>> 8);
					}
				}
				else { /* works for any 'channels' but above flavors are faster for 1 and 2 */
					for( wide_sample = 0; wide_sample < wide_samples; wide_sample++ ) {
						for( channel = 0; channel < channels; channel++ ) {
							final int s = (buffer[channel][wide_sample] + 0x8000);
							s8buffer[bytes_to_write++] = (byte)s;
							s8buffer[bytes_to_write++] = (byte)(s >>> 8);
						}
					}
				}
			}
			else {
				if( channels == 2 ) {
					for( wide_sample = 0; wide_sample < wide_samples; wide_sample++ ) {
						int s = buffer[0][wide_sample];
						s8buffer[bytes_to_write++] = (byte)s;
						s8buffer[bytes_to_write++] = (byte)(s >> 8);
						s = buffer[1][wide_sample];
						s8buffer[bytes_to_write++] = (byte)s;
						s8buffer[bytes_to_write++] = (byte)(s >> 8);
					}
				}
				else if( channels == 1 ) {
					for( wide_sample = 0; wide_sample < wide_samples; wide_sample++ ) {
						final int s = buffer[0][wide_sample];
						s8buffer[bytes_to_write++] = (byte)s;
						s8buffer[bytes_to_write++] = (byte)(s >> 8);
					}
				}
				else { /* works for any 'channels' but above flavors are faster for 1 and 2 */
					for( wide_sample = 0; wide_sample < wide_samples; wide_sample++ ) {
						for( channel = 0; channel < channels; channel++ ) {
							final int s = buffer[channel][wide_sample];
							s8buffer[bytes_to_write++] = (byte)s;
							s8buffer[bytes_to_write++] = (byte)(s >>> 8);
						}
					}
				}
			}
			if( mIsBigEndian ) {
				for( ibyte = 0; ibyte < bytes_to_write; ibyte += 2 ) {
					final byte tmp = s8buffer[ibyte];
					s8buffer[ibyte] = s8buffer[ibyte + 1];
					s8buffer[ibyte + 1] = tmp;
				}
			}
		}
		else if( bps == 24 ) {
			if( mIsUnsigned ) {
				for( wide_sample = 0; wide_sample < wide_samples; wide_sample++ ) {
					for( channel = 0; channel < channels; channel++ ) {
						final int s = buffer[channel][wide_sample] + 0x800000;
						s8buffer[bytes_to_write++] = (byte)s;
						s8buffer[bytes_to_write++] = (byte)(s >>> 8);
						s8buffer[bytes_to_write++] = (byte)(s >>> 16);
					}
				}
			}
			else {
				for( wide_sample = 0; wide_sample < wide_samples; wide_sample++ ) {
					for( channel = 0; channel < channels; channel++ ) {
						final int s = buffer[channel][wide_sample];
						s8buffer[bytes_to_write++] = (byte)s;
						s8buffer[bytes_to_write++] = (byte)(s >> 8);
						s8buffer[bytes_to_write++] = (byte)(s >> 16);
					}
				}
			}
			if( mIsBigEndian ) {
				for( ibyte = 0; ibyte < bytes_to_write; ibyte += 3 ) {
					final byte tmp = s8buffer[ibyte];
					s8buffer[ibyte] = s8buffer[ibyte + 2];
					s8buffer[ibyte + 2] = tmp;
				}
			}
		}
		else if( bps == 8 ) {
			if( mIsUnsigned ) {
				for( wide_sample = 0; wide_sample < wide_samples; wide_sample++ ) {
					for( channel = 0; channel < channels; channel++ ) {
						s8buffer[bytes_to_write++] = (byte)(buffer[channel][wide_sample] + 0x80);
					}
				}
			}
			else {
				for( wide_sample = 0; wide_sample < wide_samples; wide_sample++ ) {
					for( channel = 0; channel < channels; channel++ ) {
						s8buffer[bytes_to_write++] = (byte)(buffer[channel][wide_sample]);
					}
				}
			}
		}
		else {
			return JFLAC__StreamDecoder.FLAC__STREAM_DECODER_WRITE_STATUS_ABORT;
		}
		mBytesInBuffer = bytes_to_write;
		/*if( bytes_to_write > 0 ) {
			try {
				fout.write( s8buffer, 0, bytes_to_write );
			} catch(IOException e) {
				return JFLAC__StreamDecoder.FLAC__STREAM_DECODER_WRITE_STATUS_ABORT;
			}
		}*/
		return JFLAC__StreamDecoder.FLAC__STREAM_DECODER_WRITE_STATUS_CONTINUE;
	}
}
