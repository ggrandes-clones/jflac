package libFLAC;

/** FLAC CUESHEET track index structure.  (See the
 * <A HREF="../format.html#cuesheet_track_index">format specification</A> for
 * the full description of each field.)
 */
public final class JFLAC__StreamMetadata_CueSheet_Index {
	/** Offset in samples, relative to the track offset, of the index
	 * point.
	 */
	public long offset = 0;

	/** The index point number. */
	byte number = 0;// TODO check using. value must be less then 128! also check castings.

	JFLAC__StreamMetadata_CueSheet_Index() {
	}

	JFLAC__StreamMetadata_CueSheet_Index(final JFLAC__StreamMetadata_CueSheet_Index cs) {
		copyFrom( cs );
	}

	private final void copyFrom(final JFLAC__StreamMetadata_CueSheet_Index cs) {
		offset = cs.offset;
		number = cs.number;
	}

	static JFLAC__StreamMetadata_CueSheet_Index[] cuesheet_track_index_array_new_(final int num_indices)
	{
		//FLAC__ASSERT(num_indices > 0);

		final JFLAC__StreamMetadata_CueSheet_Index[] array = new JFLAC__StreamMetadata_CueSheet_Index[num_indices];
		for( int i = 0; i < num_indices; i++ ) {
			array[i] = new JFLAC__StreamMetadata_CueSheet_Index();
		}
		return array;
	}
}
