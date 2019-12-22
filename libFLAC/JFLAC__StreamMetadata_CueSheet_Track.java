package libFLAC;

/** FLAC CUESHEET track structure.  (See the
 * <A HREF="../format.html#cuesheet_track">format specification</A> for
 * the full description of each field.)
 */
public final class JFLAC__StreamMetadata_CueSheet_Track {
	/** Track offset in samples, relative to the beginning of the FLAC audio stream. */
	long offset = 0;

	/** The track number. */
	byte number = 0;// TODO check using. value must be less then 128! also check castings.

	/** Track ISRC.  This is a 12-digit alphanumeric code plus a trailing \c NUL byte */
	final byte isrc[] = new byte[13];

	/** The track type: 0 for audio, 1 for non-audio. */
	int type = 0;

	/** The pre-emphasis flag: 0 for no pre-emphasis, 1 for pre-emphasis. */
	int pre_emphasis = 0;

	/** The number of track index points. */
	byte num_indices = 0;// TODO check using. value must be less then 128! also check castings.

	/** NULL if num_indices == 0, else pointer to array of index points. */
	JFLAC__StreamMetadata_CueSheet_Index[] indices = null;

	final void copyFrom(final JFLAC__StreamMetadata_CueSheet_Track track) {// java analog for *dst = *src
		offset = track.offset;
		number = track.number;
		System.arraycopy( track.isrc, 0, isrc, 0, isrc.length );
		type = track.type;
		pre_emphasis = track.pre_emphasis;
		num_indices = track.num_indices;
		indices = track.indices;
	}

	static JFLAC__StreamMetadata_CueSheet_Track[] cuesheet_track_array_new_(final int num_tracks)
	{
		//FLAC__ASSERT(num_tracks > 0);

		final JFLAC__StreamMetadata_CueSheet_Track[] array = new JFLAC__StreamMetadata_CueSheet_Track[num_tracks];
		for( int i = 0; i < num_tracks; i++ ) {
			array[i] = new JFLAC__StreamMetadata_CueSheet_Track();
		}
		return array;
	}

	private static void cuesheet_track_array_delete_(final JFLAC__StreamMetadata_CueSheet_Track[] object_array, final int num_tracks)
	{
		//FLAC__ASSERT(object_array != NULL && num_tracks > 0);

		for( int i = 0; i < num_tracks; i++ ) {
			object_array[i].indices = null;
		}

	}

	static boolean copy_track_(final JFLAC__StreamMetadata_CueSheet_Track to, final JFLAC__StreamMetadata_CueSheet_Track from)
	{
		to.offset = from.offset;
		to.number = from.number;
		System.arraycopy( from.isrc, 0, to.isrc, 0, to.isrc.length );
		to.type = from.type;
		to.pre_emphasis = from.pre_emphasis;
		to.num_indices = from.num_indices;
		if( from.indices == null ) {
			//FLAC__ASSERT(from->num_indices == 0);
			return false;
		}
		else {
			final JFLAC__StreamMetadata_CueSheet_Index[] x = new JFLAC__StreamMetadata_CueSheet_Index[from.num_indices];
			//FLAC__ASSERT(from->num_indices > 0);
			try {
				for( int i = 0; i < from.num_indices; i++ ) {
					x[i] = new JFLAC__StreamMetadata_CueSheet_Index( from.indices[i] );
				}
			} catch (final OutOfMemoryError e) {
				return false;
			}
			to.indices = x;
		}
		return true;
	}

	static JFLAC__StreamMetadata_CueSheet_Track[] cuesheet_track_array_copy_(final JFLAC__StreamMetadata_CueSheet_Track[] object_array, final int num_tracks)
	{
		final JFLAC__StreamMetadata_CueSheet_Track[] return_array;

		//FLAC__ASSERT(object_array != NULL);
		//FLAC__ASSERT(num_tracks > 0);

		return_array = cuesheet_track_array_new_( num_tracks );

		if( return_array != null ) {
			for( int i = 0; i < num_tracks; i++ ) {
				if( ! copy_track_( return_array[i], object_array[i] ) ) {
					cuesheet_track_array_delete_( return_array, num_tracks );
					return null;
				}
			}
		}

		return return_array;
	}

	public final JFLAC__StreamMetadata_CueSheet_Track FLAC__metadata_object_cuesheet_track_clone()
	{
		final JFLAC__StreamMetadata_CueSheet_Track to;

		//FLAC__ASSERT(object != NULL);

		if( (to = new JFLAC__StreamMetadata_CueSheet_Track()) != null ) {
			if( ! copy_track_( to, this ) ) {
				to.FLAC__metadata_object_cuesheet_track_delete();
				return null;
			}
		}

		return to;
	}

	public final void FLAC__metadata_object_cuesheet_track_delete_data()
	{
		//FLAC__ASSERT(object != NULL);

		this.indices = null;
	}

	public final void FLAC__metadata_object_cuesheet_track_delete()
	{
		FLAC__metadata_object_cuesheet_track_delete_data();
	}
}
