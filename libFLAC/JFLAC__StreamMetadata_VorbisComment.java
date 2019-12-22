package libFLAC;

import java.util.Arrays;

// java: FLAC__StreamMetadata_VorbisComment_Entry replaced by the String
/** FLAC VORBIS_COMMENT structure.  (c.f. <A HREF="../format.html#metadata_block_vorbis_comment">format specification</A>)
 */
public final class JFLAC__StreamMetadata_VorbisComment extends JFLAC__StreamMetadata {
	static final String ENCODING = "UTF-8";

	String vendor_string = null;
	String[] comments = null;
	int num_comments = 0;// TODO use comments.length

	JFLAC__StreamMetadata_VorbisComment() {
		super.type = Jformat.FLAC__METADATA_TYPE_VORBIS_COMMENT;
	}

	JFLAC__StreamMetadata_VorbisComment(final JFLAC__StreamMetadata_VorbisComment m) {
		copyFrom( m );
	}
	private final void copyFrom(final JFLAC__StreamMetadata_VorbisComment m) {
		super.copyFrom( m );
		vendor_string = m.vendor_string;
		comments = vorbiscomment_entry_array_copy_( m.comments, comments.length );
	}

	private static String[] vorbiscomment_entry_array_new_(final int num_comments)
	{
		//FLAC__ASSERT(num_comments > 0);

		return new String[num_comments];
	}

	/* java: do not need
	static void vorbiscomment_entry_array_delete_(FLAC__StreamMetadata_VorbisComment_Entry *object_array, unsigned num_comments)
	{
		unsigned i;

		FLAC__ASSERT(object_array != NULL && num_comments > 0);

		for(i = 0; i < num_comments; i++)
			if(0 != object_array[i].entry)
				free(object_array[i].entry);

		if(0 != object_array)
			free(object_array);
	}
	*/

	private static String[] vorbiscomment_entry_array_copy_(final String[] object_array, final int num_comments)
	{
		//FLAC__ASSERT(object_array != NULL);
		//FLAC__ASSERT(num_comments > 0);

		try {
			final String[] return_array = vorbiscomment_entry_array_new_( num_comments );

			if( return_array != null ) {
				for( int i = 0; i < num_comments; i++ ) {
					return_array[i] = object_array[i];
				}
			}

			return return_array;
		} catch (final OutOfMemoryError e) {
		}
		return null;
	}

	final void vorbiscomment_calculate_length_()
	{
		//FLAC__ASSERT(object->type == FLAC__METADATA_TYPE_VORBIS_COMMENT);

		this.length = (Jformat.FLAC__STREAM_METADATA_VORBIS_COMMENT_ENTRY_LENGTH_LEN) / 8;
		this.length += this.vendor_string.length();
		this.length += (Jformat.FLAC__STREAM_METADATA_VORBIS_COMMENT_NUM_COMMENTS_LEN) / 8;
		for( int i = 0, ie = this.num_comments; i < ie; i++ ) {
			this.length += (Jformat.FLAC__STREAM_METADATA_VORBIS_COMMENT_ENTRY_LENGTH_LEN / 8);
			final String comment = this.comments[i];// java
			if( comment != null ) {
				this.length += comment.length();
			}
		}
	}

	private final boolean vorbiscomment_set_entry_(final String[] dest, final int offset, final String src, final boolean copy)
	{// TODO return void and use OutOfMemoryError exception?
		//FLAC__ASSERT(object != NULL);
		//FLAC__ASSERT(dest != NULL);
		//FLAC__ASSERT(src != NULL);
		//FLAC__ASSERT(object->type == FLAC__METADATA_TYPE_VORBIS_COMMENT);
		//FLAC__ASSERT((src->entry != NULL && src->length > 0) || (src->entry == NULL && src->length == 0));

		if( src != null ) {
			if( copy ) {
				/* do the copy first so that if we fail we leave the dest object untouched */
				dest[offset] = new String( src );
			} else {
				dest[offset] = src;
			}
		}
		else {
			/* the src is null */
			dest[offset] = src;
		}

		vorbiscomment_calculate_length_();
		return true;
	}

	private final int vorbiscomment_find_entry_from_(final int offset, final String field_name, final int field_name_length)
	{
		//FLAC__ASSERT(object != NULL);
		//FLAC__ASSERT(object->type == FLAC__METADATA_TYPE_VORBIS_COMMENT);
		//FLAC__ASSERT(field_name != NULL);

		for( int i = offset; i < this.num_comments; i++ ) {
			if( FLAC__metadata_object_vorbiscomment_entry_matches( this.comments[i], field_name, field_name_length ) ) {
				return i;
			}
		}

		return -1;
	}

	static boolean compare_block_data_vorbiscomment_(final JFLAC__StreamMetadata_VorbisComment block1, final JFLAC__StreamMetadata_VorbisComment block2)
	{
		if( block1.vendor_string != null && block2.vendor_string != null ) {
			if( ! block1.vendor_string.equals( block2.vendor_string ) ) {
				return false;
			}
		}
		else if( block1.vendor_string != block2.vendor_string ) {
			return false;
		}

		if( block1.num_comments != block2.num_comments ) {
			return false;
		}

		for( int i = 0; i < block1.num_comments; i++ ) {
			if( block1.comments[i] != null && block2.comments[i] != null ) {
				if( ! block1.comments[i].equals( block2.comments[i] ) ) {
					return false;
				}
			}
			else if( block1.comments[i] != block2.comments[i] ) {
				return false;
			}
		}
		return true;
	}

	public final boolean FLAC__metadata_object_vorbiscomment_set_vendor_string(final String entry, final boolean copy)
	{
		//if( ! FLAC__format_vorbiscomment_entry_value_is_legal( entry.entry, entry.length ) )
		//	return false;
		//return object.vorbiscomment_set_entry_( object.vendor_string, entry, copy );
		if( copy ) {
			/* do the copy first so that if we fail we leave the dest object untouched */
			this.vendor_string = new String( entry );
			return true;// FIXME why length do not calculating?
		}
		this.vendor_string = entry;
		vorbiscomment_calculate_length_();
		return true;
	}

	public final boolean FLAC__metadata_object_vorbiscomment_resize_comments(final int new_num_comments)
	{
		//FLAC__ASSERT(object != NULL);
		//FLAC__ASSERT(object->type == FLAC__METADATA_TYPE_VORBIS_COMMENT);

		if( this.comments == null ) {
			//FLAC__ASSERT(object->data.vorbis_comment.num_comments == 0);
			if( new_num_comments == 0 ) {
				return true;
			} else if( (this.comments = vorbiscomment_entry_array_new_( new_num_comments )) == null ) {
				return false;
			}
		}
		else {
			/* overflow check */
			if( new_num_comments > Jformat.SIZE_MAX ) {
				return false;
			}

			//FLAC__ASSERT(object->data.vorbis_comment.num_comments > 0);

			/* if shrinking, free the truncated entries */
			if( new_num_comments < this.num_comments ) {
				for( int i = new_num_comments; i < this.num_comments; i++ ) {
					this.comments[i] = null;
				}
			}

			if( new_num_comments == 0 ) {
				this.comments = null;
			}
			else if( null == (this.comments = Arrays.copyOf( this.comments, new_num_comments )) ) {
				return false;
			}

			/* if growing, zero all the length/pointers of new elements */// java: already nulled
		}

		this.num_comments = new_num_comments;

		vorbiscomment_calculate_length_();
		return true;
	}

	public final boolean FLAC__metadata_object_vorbiscomment_set_comment(final int comment_num, final String entry, final boolean copy)
	{
		//FLAC__ASSERT(object != NULL);
		//FLAC__ASSERT(comment_num < object->data.vorbis_comment.num_comments);

		//if( ! FLAC__format_vorbiscomment_entry_is_legal( entry.entry, entry.length ) )// java: do not need
		//	return false;
		return vorbiscomment_set_entry_( this.comments, comment_num, entry, copy );
	}

	public final boolean FLAC__metadata_object_vorbiscomment_insert_comment(final int comment_num, final String entry, final boolean copy)
	{
		//FLAC__ASSERT(object != NULL);
		//FLAC__ASSERT(object->type == FLAC__METADATA_TYPE_VORBIS_COMMENT);
		//FLAC__ASSERT(comment_num <= object->data.vorbis_comment.num_comments);

		/* java: do not need
		if( ! FLAC__format_vorbiscomment_entry_is_legal( entry.entry, entry.length ) )
			return false;
		*/

		if( ! FLAC__metadata_object_vorbiscomment_resize_comments( this.num_comments + 1 ) ) {
			return false;
		}

		/* move all comments >= comment_num forward one space */
		System.arraycopy( this.comments, comment_num, this.comments, comment_num + 1, this.num_comments - 1 - comment_num );
		this.comments[comment_num] = null;

		return FLAC__metadata_object_vorbiscomment_set_comment( comment_num, entry, copy );
	}

	public final boolean FLAC__metadata_object_vorbiscomment_append_comment(final String entry, final boolean copy)
	{
		//FLAC__ASSERT(object != NULL);
		//FLAC__ASSERT(object->type == FLAC__METADATA_TYPE_VORBIS_COMMENT);
		return FLAC__metadata_object_vorbiscomment_insert_comment( this.num_comments, entry, copy );
	}

	public final boolean FLAC__metadata_object_vorbiscomment_replace_comment(String entry, final boolean all, final boolean copy)
	{
		//FLAC__ASSERT(entry.entry != NULL && entry.length > 0);

		//if( !FLAC__format_vorbiscomment_entry_is_legal(entry.entry, entry.length))
		//	return false;

		{
			final int field_name_length = entry.indexOf('=');

			if( field_name_length < 0 ) {
				return false; /* double protection */
			}

			int i = vorbiscomment_find_entry_from_( 0, entry, field_name_length );
			if( i >= 0 ) {
				int indx = i;
				if( ! FLAC__metadata_object_vorbiscomment_set_comment( indx, entry, copy ) ) {
					return false;
				}
				entry = this.comments[indx];
				indx++; /* skip over replaced comment */
				if( all && indx < this.num_comments ) {
					i = vorbiscomment_find_entry_from_( indx, entry, field_name_length );
					while( i >= 0 ) {
						indx = i;
						if( ! FLAC__metadata_object_vorbiscomment_delete_comment( indx ) ) {
							return false;
						}
						if( indx < this.num_comments ) {
							i = vorbiscomment_find_entry_from_( indx, entry, field_name_length );
						} else {
							i = -1;
						}
					}
				}
				return true;
			} else {
				return this.FLAC__metadata_object_vorbiscomment_append_comment( entry, copy );
			}
		}
	}

	public final boolean FLAC__metadata_object_vorbiscomment_delete_comment(final int comment_num)
	{
		//FLAC__ASSERT(object != NULL);
		//FLAC__ASSERT(object->type == FLAC__METADATA_TYPE_VORBIS_COMMENT);
		//FLAC__ASSERT(comment_num < object->data.vorbis_comment.num_comments);

		/* free the comment at comment_num */
		this.comments[comment_num] = null;

		/* move all comments > comment_num backward one space */
		final int n1 = this.num_comments - 1;// java
		System.arraycopy( this.comments, comment_num + 1, this.comments, comment_num, n1 - comment_num );
		this.comments[n1] = null;

		return this.FLAC__metadata_object_vorbiscomment_resize_comments( n1 );
	}

	/** @return JFLAC__StreamMetadata_VorbisComment_Entry or null, if field_name or field_value is null */
	public static String /* boolean*/ FLAC__metadata_object_vorbiscomment_entry_from_name_value_pair(
			final String /* FLAC__StreamMetadata_VorbisComment_Entry */ field_name,
			final String /* FLAC__StreamMetadata_VorbisComment_Entry */ field_value)
	{
		//FLAC__ASSERT(entry != NULL);
		//FLAC__ASSERT(field_name != NULL);
		//FLAC__ASSERT(field_value != NULL);

		/* java: do not need
		if(!FLAC__format_vorbiscomment_entry_name_is_legal(field_name))
			return false;
		if(!FLAC__format_vorbiscomment_entry_value_is_legal((const FLAC__byte *)field_value, (unsigned)(-1)))
			return false;
		*/
		if( field_name != null && field_value != null ) {
			return field_name + "=" + field_value;
		}

		return null;
	}

	/** @return String[2] with field_name and field_value, null if error */
	public static String[] /* boolean */ FLAC__metadata_object_vorbiscomment_entry_to_name_value_pair(final String entry/*, char **field_name, char **field_value*/)
	{
		//FLAC__ASSERT(entry.entry != NULL && entry.length > 0);
		//FLAC__ASSERT(field_name != NULL);
		//FLAC__ASSERT(field_value != NULL);

		//if( ! FLAC__format_vorbiscomment_entry_is_legal( entry.entry, entry.length ) )
		//	return false;

		int p = entry.indexOf('=');
		if( p < 0 ) {
			return null; /* double protection */
		}
		final String[] ret = new String[2];
		ret[0] = entry.substring( 0, p );
		ret[1] = entry.substring( ++p );

		return ret;
	}

	public static boolean FLAC__metadata_object_vorbiscomment_entry_matches(final String entry, final String field_name, final int field_name_length)
	{
		//FLAC__ASSERT(entry.entry != NULL && entry.length > 0);
		{
			final int p = entry.indexOf('=');
			if( p < 0 || p != field_name_length ) {
				return false;
			}
			return entry.substring( 0, p ).equalsIgnoreCase( field_name );
		}
	}

	public final int FLAC__metadata_object_vorbiscomment_find_entry_from(final int offset, final String field_name)
	{
		//FLAC__ASSERT(field_name != NULL);

		return vorbiscomment_find_entry_from_( offset, field_name, field_name.length() );
	}

	public final int FLAC__metadata_object_vorbiscomment_remove_entry_matching(final String field_name)
	{
		final int field_name_length = field_name.length();

		//FLAC__ASSERT(object != NULL);
		//FLAC__ASSERT(object->type == FLAC__METADATA_TYPE_VORBIS_COMMENT);

		for( int i = 0, ncomments = this.num_comments; i < ncomments; i++ ) {
			if( FLAC__metadata_object_vorbiscomment_entry_matches( this.comments[i], field_name, field_name_length ) ) {
				if( ! FLAC__metadata_object_vorbiscomment_delete_comment( i ) ) {
					return -1;
				}// else {
					return 1;
				//}
			}
		}

		return 0;
	}

	public final int FLAC__metadata_object_vorbiscomment_remove_entries_matching(final String field_name)
	{
		boolean ok = true;
		int matching = 0;
		final int field_name_length = field_name.length();

		//FLAC__ASSERT(0 != object);
		//FLAC__ASSERT(object->type == FLAC__METADATA_TYPE_VORBIS_COMMENT);

		/* must delete from end to start otherwise it will interfere with our iteration */
		for( int i = this.num_comments - 1; ok && i >= 0; i-- ) {
			if( FLAC__metadata_object_vorbiscomment_entry_matches( this.comments[i], field_name, field_name_length ) ) {
				matching++;
				ok &= FLAC__metadata_object_vorbiscomment_delete_comment( i );
			}
		}

		return ok ? matching : -1;
	}

	// metadata_iterators.c
	public boolean FLAC__metadata_get_tags(final String filename)
	{
		//FLAC__ASSERT(0 != filename);
		//FLAC__ASSERT(0 != tags);
		final JFLAC__StreamMetadata_VorbisComment tags;
		tags = (JFLAC__StreamMetadata_VorbisComment) get_one_metadata_block_( filename, Jformat.FLAC__METADATA_TYPE_VORBIS_COMMENT );
		if( tags != null ) {
			copyFrom( tags );
		}

		return null != tags;
	}
}
