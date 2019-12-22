package libFLAC;

/** FLAC STREAMINFO structure.  (c.f. <A HREF="../format.html#metadata_block_streaminfo">format specification</A>)
 */
public final class JFLAC__StreamMetadata_StreamInfo extends JFLAC__StreamMetadata {
	/** The total stream length of the STREAMINFO block in bytes. */
	static final int FLAC__STREAM_METADATA_STREAMINFO_LENGTH = 34;

	int min_blocksize = 0, max_blocksize = 0;
	int min_framesize = 0, max_framesize = 0;
	public int sample_rate = 0;
	public int channels = 0;
	public int bits_per_sample = 0;
	public long total_samples = 0;
	public final byte md5sum[] = new byte[16];

	public JFLAC__StreamMetadata_StreamInfo() {
		super.type = Jformat.FLAC__METADATA_TYPE_STREAMINFO;
	}

	JFLAC__StreamMetadata_StreamInfo(final JFLAC__StreamMetadata_StreamInfo m) {
		copyFrom( m );
	}
	private final void copyFrom(final JFLAC__StreamMetadata_StreamInfo m) {
		super.copyFrom( m );
		min_blocksize = m.min_blocksize;
		max_blocksize = m.max_blocksize;
		min_framesize = m.min_framesize;
		max_framesize = m.max_framesize;
		sample_rate = m.sample_rate;
		channels = m.channels;
		bits_per_sample = m.bits_per_sample;
		total_samples = m.total_samples;
		System.arraycopy( m.md5sum, 0, md5sum, 0, md5sum.length );
	}

	static boolean compare_block_data_streaminfo_(final JFLAC__StreamMetadata_StreamInfo block1, final JFLAC__StreamMetadata_StreamInfo block2)
	{
		if( block1.min_blocksize != block2.min_blocksize ) {
			return false;
		}
		if( block1.max_blocksize != block2.max_blocksize ) {
			return false;
		}
		if( block1.min_framesize != block2.min_framesize ) {
			return false;
		}
		if( block1.max_framesize != block2.max_framesize ) {
			return false;
		}
		if( block1.sample_rate != block2.sample_rate ) {
			return false;
		}
		if( block1.channels != block2.channels ) {
			return false;
		}
		if( block1.bits_per_sample != block2.bits_per_sample ) {
			return false;
		}
		if( block1.total_samples != block2.total_samples ) {
			return false;
		}
		if( Jformat.memcmp( block1.md5sum, 0, block2.md5sum, 0, 16 ) != 0 ) {
			return false;
		}
		return true;
	}

	// metadata_iterators.c
	public final boolean FLAC__metadata_get_streaminfo(final String filename)
	{
		//FLAC__ASSERT(0 != filename);
		//FLAC__ASSERT(0 != streaminfo);

		final JFLAC__StreamMetadata_StreamInfo object = (JFLAC__StreamMetadata_StreamInfo) this.get_one_metadata_block_( filename, Jformat.FLAC__METADATA_TYPE_STREAMINFO );

		if( object != null ) {
			/* can just copy the contents since STREAMINFO has no internal structure */
			this.copyFrom( object );
			FLAC__metadata_object_delete( object );
			return true;
		}
		//else {
			return false;
		//}
	}
}
