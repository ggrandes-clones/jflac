package libFLAC;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

/** FLAC metadata block structure.  (c.f. <A HREF="../format.html#metadata_block">format specification</A>)
 */
public class JFLAC__StreamMetadata implements JFLAC__StreamDecoderWriteCallback
{
	/** The type of the metadata block; used determine which member of the
	 * \a data union to dereference.  If type >= FLAC__METADATA_TYPE_UNDEFINED
	 * then \a data.unknown must be used. */
	public int /*FLAC__MetadataType*/ type = 0;

	/** \c true if this metadata block is the last, else \a false */
	public boolean is_last = false;

	/** Length, in bytes, of the block data as it appears in the stream. */
	public int length = 0;// FIXME may be FLAC__off_t instead of unsigned?

	/** Polymorphic block data; use the \a type value to determine which
	 * to use. */
	/*union {
		FLAC__StreamMetadata_StreamInfo stream_info;
		FLAC__StreamMetadata_Padding padding;
		FLAC__StreamMetadata_Application application;
		FLAC__StreamMetadata_SeekTable seek_table;
		FLAC__StreamMetadata_VorbisComment vorbis_comment;
		FLAC__StreamMetadata_CueSheet cue_sheet;
		FLAC__StreamMetadata_Picture picture;
		FLAC__StreamMetadata_Unknown unknown;
	} data;*/
	//public Object data;// java moved to child classes

	JFLAC__StreamMetadata() {
	}
	JFLAC__StreamMetadata(final JFLAC__StreamMetadata m) {
		copyFrom( m );
	}

	final void copyFrom(final JFLAC__StreamMetadata m) {
		type = m.type;
		is_last = m.is_last;
		length = m.length;
	}
	/****************************************************************************
	 *
	 * Metadata object routines
	 *
	 ***************************************************************************/

	public static JFLAC__StreamMetadata FLAC__metadata_object_new(final int /* FLAC__MetadataType */ type)
	{
		final JFLAC__StreamMetadata object;

		if( type > Jformat.FLAC__MAX_METADATA_TYPE ) {
			return null;
		}

		//object = (FLAC__StreamMetadata*)calloc(1, sizeof(FLAC__StreamMetadata));
		//if( object != NULL ) {
			//object->is_last = false;
			//object->type = type;
			switch( type ) {
				case Jformat.FLAC__METADATA_TYPE_STREAMINFO:
					object = new JFLAC__StreamMetadata_StreamInfo();
					object.length = JFLAC__StreamMetadata_StreamInfo.FLAC__STREAM_METADATA_STREAMINFO_LENGTH;
					break;
				case Jformat.FLAC__METADATA_TYPE_PADDING:
					object = new JFLAC__StreamMetadata_Padding();
					/* calloc() and java took care of this for us:
					object->length = 0;
					*/
					break;
				case Jformat.FLAC__METADATA_TYPE_APPLICATION:
					object = new JFLAC__StreamMetadata_Application();
					object.length = Jformat.FLAC__STREAM_METADATA_APPLICATION_ID_LEN / 8;
					/* calloc() took care of this for us:
					object->data.application.data = 0;
					*/
					break;
				case Jformat.FLAC__METADATA_TYPE_SEEKTABLE:
					object = new JFLAC__StreamMetadata_SeekTable();
					/* calloc() and java took care of this for us:
					object->length = 0;
					object->data.seek_table.num_points = 0;
					object->data.seek_table.points = 0;
					*/
					break;
				case Jformat.FLAC__METADATA_TYPE_VORBIS_COMMENT:
					object = new JFLAC__StreamMetadata_VorbisComment();
					((JFLAC__StreamMetadata_VorbisComment)object).vendor_string = Jformat.FLAC__VENDOR_STRING;
					((JFLAC__StreamMetadata_VorbisComment)object).vorbiscomment_calculate_length_();
					break;
				case Jformat.FLAC__METADATA_TYPE_CUESHEET:
					object = new JFLAC__StreamMetadata_CueSheet();
					((JFLAC__StreamMetadata_CueSheet)object).cuesheet_calculate_length_();
					break;
				case Jformat.FLAC__METADATA_TYPE_PICTURE:
					object = new JFLAC__StreamMetadata_Picture();
					final JFLAC__StreamMetadata_Picture picture = (JFLAC__StreamMetadata_Picture) object;
					object.length = (
						Jformat.FLAC__STREAM_METADATA_PICTURE_TYPE_LEN +
						Jformat.FLAC__STREAM_METADATA_PICTURE_MIME_TYPE_LENGTH_LEN + /* empty mime_type string */
						Jformat.FLAC__STREAM_METADATA_PICTURE_DESCRIPTION_LENGTH_LEN + /* empty description string */
						Jformat.FLAC__STREAM_METADATA_PICTURE_WIDTH_LEN +
						Jformat.FLAC__STREAM_METADATA_PICTURE_HEIGHT_LEN +
						Jformat.FLAC__STREAM_METADATA_PICTURE_DEPTH_LEN +
						Jformat.FLAC__STREAM_METADATA_PICTURE_COLORS_LEN +
						Jformat.FLAC__STREAM_METADATA_PICTURE_DATA_LENGTH_LEN +
						0 /* no data */
					) / 8;
					picture.type = Jformat.FLAC__STREAM_METADATA_PICTURE_TYPE_OTHER;
					//picture.mime_type = null;
					//picture.description = null;
					/* calloc() and java took care of this for us:
					object->data.picture.width = 0;
					object->data.picture.height = 0;
					object->data.picture.depth = 0;
					object->data.picture.colors = 0;
					object->data.picture.data_length = 0;
					object->data.picture.data = 0;
					*/
					/* now initialize mime_type and description with empty strings to make things easier on the client */
					picture.mime_type = new String();
					picture.description = new String();
					break;
				default:
					object = new JFLAC__StreamMetadata();
					/* calloc() took care of this for us:
					object->length = 0;
					object->data.unknown.data = 0;
					*/
					break;
			}
		//}
		object.is_last = false;
		object.type = type;

		return object;
	}

	public static JFLAC__StreamMetadata FLAC__metadata_object_clone(final JFLAC__StreamMetadata object)
	{
		JFLAC__StreamMetadata to;

		//FLAC__ASSERT(object != NULL);

		/*if( (to = FLAC__metadata_object_new( object.type )) != NULL ) {
			to.is_last = object.is_last;
			to.type = object.type;
			to.length = object.length;*/// java: inside creating every object
			switch( object.type /*to.type*/ ) {
				case Jformat.FLAC__METADATA_TYPE_STREAMINFO:
					to = new JFLAC__StreamMetadata_StreamInfo( (JFLAC__StreamMetadata_StreamInfo) object );
					break;
				case Jformat.FLAC__METADATA_TYPE_PADDING:
					to = new JFLAC__StreamMetadata_Padding( (JFLAC__StreamMetadata_Padding) object );
					break;
				case Jformat.FLAC__METADATA_TYPE_APPLICATION:
					to = new JFLAC__StreamMetadata_Application( (JFLAC__StreamMetadata_Application) object );
					if( to.length < Jformat.FLAC__STREAM_METADATA_APPLICATION_ID_LEN / 8 ) { /* underflow check */
						to = null;
						return null;
					}
					break;
				case Jformat.FLAC__METADATA_TYPE_SEEKTABLE:
					to = new JFLAC__StreamMetadata_SeekTable( (JFLAC__StreamMetadata_SeekTable) object );
					if( ((JFLAC__StreamMetadata_SeekTable)to).num_points > Jformat.SIZE_MAX ) { /* overflow check */
						to = null;
						return null;
					}
					break;
				case Jformat.FLAC__METADATA_TYPE_VORBIS_COMMENT:
					to = new JFLAC__StreamMetadata_VorbisComment( (JFLAC__StreamMetadata_VorbisComment) object );
					break;
				case Jformat.FLAC__METADATA_TYPE_CUESHEET:
					to = new JFLAC__StreamMetadata_CueSheet( (JFLAC__StreamMetadata_CueSheet) object );
					break;
				case Jformat.FLAC__METADATA_TYPE_PICTURE:
					to = new JFLAC__StreamMetadata_Picture( (JFLAC__StreamMetadata_Picture) object );
					break;
				default:
					to = new JFLAC__StreamMetadata_Unknown( (JFLAC__StreamMetadata_Unknown) object );
					break;
			}
		//}

		return to;
	}

	static void FLAC__metadata_object_delete_data(final JFLAC__StreamMetadata object)
	{
		//FLAC__ASSERT(object != NULL);

		switch(object.type) {
			case Jformat.FLAC__METADATA_TYPE_STREAMINFO:
			case Jformat.FLAC__METADATA_TYPE_PADDING:
				break;
			case Jformat.FLAC__METADATA_TYPE_APPLICATION:
				((JFLAC__StreamMetadata_Application)object).data = null;
				break;
			case Jformat.FLAC__METADATA_TYPE_SEEKTABLE:
				((JFLAC__StreamMetadata_SeekTable)object).points = null;
				break;
			case Jformat.FLAC__METADATA_TYPE_VORBIS_COMMENT:
				((JFLAC__StreamMetadata_VorbisComment)object).vendor_string = null;
				((JFLAC__StreamMetadata_VorbisComment)object).comments = null;
				break;
			case Jformat.FLAC__METADATA_TYPE_CUESHEET:
				((JFLAC__StreamMetadata_CueSheet)object).tracks = null;
				break;
			case Jformat.FLAC__METADATA_TYPE_PICTURE:
				((JFLAC__StreamMetadata_Picture)object).mime_type = null;
				((JFLAC__StreamMetadata_Picture)object).description = null;
				((JFLAC__StreamMetadata_Picture)object).data = null;
				break;
			default:
				((JFLAC__StreamMetadata_Unknown)object).data = null;
				break;
		}
	}

	public static void FLAC__metadata_object_delete(final JFLAC__StreamMetadata object)
	{
		FLAC__metadata_object_delete_data( object );
		//free(object);
	}

	public static boolean FLAC__metadata_object_is_equal(final JFLAC__StreamMetadata block1, final JFLAC__StreamMetadata block2)
	{
		//FLAC__ASSERT(block1 != NULL);
		//FLAC__ASSERT(block2 != NULL);

		if( block1.type != block2.type ) {
			return false;
		}
		if( block1.is_last != block2.is_last ) {
			return false;
		}
		if( block1.length != block2.length ) {
			return false;
		}
		switch( block1.type ) {
			case Jformat.FLAC__METADATA_TYPE_STREAMINFO:
				return JFLAC__StreamMetadata_StreamInfo.compare_block_data_streaminfo_( (JFLAC__StreamMetadata_StreamInfo)block1, (JFLAC__StreamMetadata_StreamInfo)block2 );
			case Jformat.FLAC__METADATA_TYPE_PADDING:
				return true; /* we don't compare the padding guts */
			case Jformat.FLAC__METADATA_TYPE_APPLICATION:
				return JFLAC__StreamMetadata_Application.compare_block_data_application_( (JFLAC__StreamMetadata_Application)block1, (JFLAC__StreamMetadata_Application)block2, block1.length );
			case Jformat.FLAC__METADATA_TYPE_SEEKTABLE:
				return JFLAC__StreamMetadata_SeekTable.compare_block_data_seektable_( (JFLAC__StreamMetadata_SeekTable)block1, (JFLAC__StreamMetadata_SeekTable)block2 );
			case Jformat.FLAC__METADATA_TYPE_VORBIS_COMMENT:
				return JFLAC__StreamMetadata_VorbisComment.compare_block_data_vorbiscomment_( (JFLAC__StreamMetadata_VorbisComment)block1, (JFLAC__StreamMetadata_VorbisComment)block2 );
			case Jformat.FLAC__METADATA_TYPE_CUESHEET:
				return JFLAC__StreamMetadata_CueSheet.compare_block_data_cuesheet_( (JFLAC__StreamMetadata_CueSheet)block1, (JFLAC__StreamMetadata_CueSheet)block2 );
			case Jformat.FLAC__METADATA_TYPE_PICTURE:
				return JFLAC__StreamMetadata_Picture.compare_block_data_picture_( (JFLAC__StreamMetadata_Picture)block1, (JFLAC__StreamMetadata_Picture)block2 );
			default:
				return JFLAC__StreamMetadata_Unknown.compare_block_data_unknown_( (JFLAC__StreamMetadata_Unknown)block1, (JFLAC__StreamMetadata_Unknown)block2, block1.length );
		}
	}

	// stream_encoder_framing.c
	static boolean FLAC__add_metadata_block(final JFLAC__StreamMetadata metadata, final JFLAC__BitWriter bw)
	{
		if( ! bw.FLAC__bitwriter_write_raw_uint32( metadata.is_last ? 1 : 0, Jformat.FLAC__STREAM_METADATA_IS_LAST_LEN ) ) {
			return false;
		}

		if( ! bw.FLAC__bitwriter_write_raw_uint32( metadata.type, Jformat.FLAC__STREAM_METADATA_TYPE_LEN ) ) {
			return false;
		}

		/*
		 * First, for VORBIS_COMMENTs, adjust the length to reflect our vendor string
		 */
		final int vendor_string_length = Jformat.FLAC__VENDOR_STRING_BYTES.length;
		int i = metadata.length;
		if( metadata.type == Jformat.FLAC__METADATA_TYPE_VORBIS_COMMENT ) {
			//FLAC__ASSERT(metadata->data.vorbis_comment.vendor_string.length == 0 || 0 != metadata->data.vorbis_comment.vendor_string.entry);
			if( ((JFLAC__StreamMetadata_VorbisComment)metadata).vendor_string != null ) {
				try {
					i -= ((JFLAC__StreamMetadata_VorbisComment)metadata).vendor_string.getBytes( JFLAC__StreamMetadata_VorbisComment.ENCODING ).length;
				} catch( final UnsupportedEncodingException e ) {
				}
			}
			i += vendor_string_length;
		}
		//FLAC__ASSERT(i < (1u << FLAC__STREAM_METADATA_LENGTH_LEN));
		/* double protection */
		if( i >= (1 << Jformat.FLAC__STREAM_METADATA_LENGTH_LEN ) ) {
			return false;
		}
		if( ! bw.FLAC__bitwriter_write_raw_uint32( i, Jformat.FLAC__STREAM_METADATA_LENGTH_LEN ) ) {
			return false;
		}

		switch( metadata.type ) {
			case Jformat.FLAC__METADATA_TYPE_STREAMINFO:
				final JFLAC__StreamMetadata_StreamInfo stream_info = (JFLAC__StreamMetadata_StreamInfo)metadata;
				//FLAC__ASSERT(metadata->data.stream_info.min_blocksize < (1u << FLAC__STREAM_METADATA_STREAMINFO_MIN_BLOCK_SIZE_LEN));
				if( ! bw.FLAC__bitwriter_write_raw_uint32( stream_info.min_blocksize, Jformat.FLAC__STREAM_METADATA_STREAMINFO_MIN_BLOCK_SIZE_LEN ) ) {
					return false;
				}
				//FLAC__ASSERT(metadata->data.stream_info.max_blocksize < (1u << FLAC__STREAM_METADATA_STREAMINFO_MAX_BLOCK_SIZE_LEN));
				if( ! bw.FLAC__bitwriter_write_raw_uint32( stream_info.max_blocksize, Jformat.FLAC__STREAM_METADATA_STREAMINFO_MAX_BLOCK_SIZE_LEN ) ) {
					return false;
				}
				//FLAC__ASSERT(metadata->data.stream_info.min_framesize < (1u << FLAC__STREAM_METADATA_STREAMINFO_MIN_FRAME_SIZE_LEN));
				if( ! bw.FLAC__bitwriter_write_raw_uint32( stream_info.min_framesize, Jformat.FLAC__STREAM_METADATA_STREAMINFO_MIN_FRAME_SIZE_LEN ) ) {
					return false;
				}
				//FLAC__ASSERT(metadata->data.stream_info.max_framesize < (1u << FLAC__STREAM_METADATA_STREAMINFO_MAX_FRAME_SIZE_LEN));
				if( ! bw.FLAC__bitwriter_write_raw_uint32( stream_info.max_framesize, Jformat.FLAC__STREAM_METADATA_STREAMINFO_MAX_FRAME_SIZE_LEN ) ) {
					return false;
				}
				//FLAC__ASSERT(FLAC__format_sample_rate_is_valid(metadata->data.stream_info.sample_rate));
				if( ! bw.FLAC__bitwriter_write_raw_uint32( stream_info.sample_rate, Jformat.FLAC__STREAM_METADATA_STREAMINFO_SAMPLE_RATE_LEN ) ) {
					return false;
				}
				//FLAC__ASSERT(metadata->data.stream_info.channels > 0);
				//FLAC__ASSERT(metadata->data.stream_info.channels <= (1u << FLAC__STREAM_METADATA_STREAMINFO_CHANNELS_LEN));
				if( ! bw.FLAC__bitwriter_write_raw_uint32( stream_info.channels - 1, Jformat.FLAC__STREAM_METADATA_STREAMINFO_CHANNELS_LEN ) ) {
					return false;
				}
				//FLAC__ASSERT(metadata->data.stream_info.bits_per_sample > 0);
				//FLAC__ASSERT(metadata->data.stream_info.bits_per_sample <= (1u << FLAC__STREAM_METADATA_STREAMINFO_BITS_PER_SAMPLE_LEN));
				if( ! bw.FLAC__bitwriter_write_raw_uint32( stream_info.bits_per_sample - 1, Jformat.FLAC__STREAM_METADATA_STREAMINFO_BITS_PER_SAMPLE_LEN ) ) {
					return false;
				}
				//FLAC__ASSERT(metadata->data.stream_info.total_samples < (FLAC__U64L(1) << FLAC__STREAM_METADATA_STREAMINFO_TOTAL_SAMPLES_LEN));
				if( ! bw.FLAC__bitwriter_write_raw_uint64( stream_info.total_samples, Jformat.FLAC__STREAM_METADATA_STREAMINFO_TOTAL_SAMPLES_LEN ) ) {
					return false;
				}
				if( ! bw.FLAC__bitwriter_write_byte_block( stream_info.md5sum, 16 ) ) {
					return false;
				}
				break;
			case Jformat.FLAC__METADATA_TYPE_PADDING:
				if( ! bw.FLAC__bitwriter_write_zeroes( metadata.length << 3 ) ) {
					return false;
				}
				break;
			case Jformat.FLAC__METADATA_TYPE_APPLICATION:
				final JFLAC__StreamMetadata_Application application = (JFLAC__StreamMetadata_Application) metadata;
				if( ! bw.FLAC__bitwriter_write_byte_block( application.id, Jformat.FLAC__STREAM_METADATA_APPLICATION_ID_LEN / 8 ) ) {
					return false;
				}
				if( ! bw.FLAC__bitwriter_write_byte_block( application.data, metadata.length - (Jformat.FLAC__STREAM_METADATA_APPLICATION_ID_LEN / 8 ) )) {
					return false;
				}
				break;
			case Jformat.FLAC__METADATA_TYPE_SEEKTABLE:
				final JFLAC__StreamMetadata_SeekTable seek_table = (JFLAC__StreamMetadata_SeekTable) metadata;
				for( i = 0; i < seek_table.num_points; i++ ) {
					final JFLAC__StreamMetadata_SeekPoint p = seek_table.points[i];// java
					if( ! bw.FLAC__bitwriter_write_raw_uint64( p.sample_number, Jformat.FLAC__STREAM_METADATA_SEEKPOINT_SAMPLE_NUMBER_LEN ) ) {
						return false;
					}
					if( ! bw.FLAC__bitwriter_write_raw_uint64( p.stream_offset, Jformat.FLAC__STREAM_METADATA_SEEKPOINT_STREAM_OFFSET_LEN ) ) {
						return false;
					}
					if( ! bw.FLAC__bitwriter_write_raw_uint32( p.frame_samples, Jformat.FLAC__STREAM_METADATA_SEEKPOINT_FRAME_SAMPLES_LEN ) ) {
						return false;
					}
				}
				break;
			case Jformat.FLAC__METADATA_TYPE_VORBIS_COMMENT:
				final JFLAC__StreamMetadata_VorbisComment vorbis_comment = (JFLAC__StreamMetadata_VorbisComment) metadata;
				if( ! bw.FLAC__bitwriter_write_raw_uint32_little_endian( vendor_string_length ) ) {
					return false;
				}
				if( ! bw.FLAC__bitwriter_write_byte_block( Jformat.FLAC__VENDOR_STRING_BYTES, vendor_string_length ) ) {
					return false;
				}
				if( ! bw.FLAC__bitwriter_write_raw_uint32_little_endian( vorbis_comment.num_comments ) ) {
					return false;
				}
				for( i = 0; i < vorbis_comment.num_comments; i++ ) {
					try {
						final byte[] entry = vorbis_comment.comments[i].getBytes( JFLAC__StreamMetadata_VorbisComment.ENCODING );
						if( ! bw.FLAC__bitwriter_write_raw_uint32_little_endian( entry.length ) ) {
							return false;
						}
						if( ! bw.FLAC__bitwriter_write_byte_block( entry, entry.length ) ) {
							return false;
						}
					} catch( final UnsupportedEncodingException e ) {
					}
				}
				break;
			case Jformat.FLAC__METADATA_TYPE_CUESHEET:
				//FLAC__ASSERT(FLAC__STREAM_METADATA_CUESHEET_MEDIA_CATALOG_NUMBER_LEN % 8 == 0);
				final JFLAC__StreamMetadata_CueSheet cue_sheet = (JFLAC__StreamMetadata_CueSheet) metadata;
				if( ! bw.FLAC__bitwriter_write_byte_block( cue_sheet.media_catalog_number, Jformat.FLAC__STREAM_METADATA_CUESHEET_MEDIA_CATALOG_NUMBER_LEN / 8 ) ) {
					return false;
				}
				if( ! bw.FLAC__bitwriter_write_raw_uint64( cue_sheet.lead_in, Jformat.FLAC__STREAM_METADATA_CUESHEET_LEAD_IN_LEN ) ) {
					return false;
				}
				if( ! bw.FLAC__bitwriter_write_raw_uint32( cue_sheet.is_cd ? 1 : 0, Jformat.FLAC__STREAM_METADATA_CUESHEET_IS_CD_LEN ) ) {
					return false;
				}
				if( ! bw.FLAC__bitwriter_write_zeroes( Jformat.FLAC__STREAM_METADATA_CUESHEET_RESERVED_LEN ) ) {
					return false;
				}
				if( ! bw.FLAC__bitwriter_write_raw_uint32( cue_sheet.num_tracks, Jformat.FLAC__STREAM_METADATA_CUESHEET_NUM_TRACKS_LEN ) ) {
					return false;
				}
				for( i = 0; i < cue_sheet.num_tracks; i++ ) {
					final JFLAC__StreamMetadata_CueSheet_Track track = cue_sheet.tracks[i];

					if( ! bw.FLAC__bitwriter_write_raw_uint64( track.offset, Jformat.FLAC__STREAM_METADATA_CUESHEET_TRACK_OFFSET_LEN ) ) {
						return false;
					}
					if( ! bw.FLAC__bitwriter_write_raw_uint32( ((int)track.number) & 0xff, Jformat.FLAC__STREAM_METADATA_CUESHEET_TRACK_NUMBER_LEN ) ) {
						return false;
					}
					//FLAC__ASSERT(FLAC__STREAM_METADATA_CUESHEET_TRACK_ISRC_LEN % 8 == 0);
					if( ! bw.FLAC__bitwriter_write_byte_block( track.isrc, Jformat.FLAC__STREAM_METADATA_CUESHEET_TRACK_ISRC_LEN/8 ) ) {
						return false;
					}
					if( ! bw.FLAC__bitwriter_write_raw_uint32( track.type, Jformat.FLAC__STREAM_METADATA_CUESHEET_TRACK_TYPE_LEN ) ) {
						return false;
					}
					if( ! bw.FLAC__bitwriter_write_raw_uint32( track.pre_emphasis, Jformat.FLAC__STREAM_METADATA_CUESHEET_TRACK_PRE_EMPHASIS_LEN ) ) {
						return false;
					}
					if( ! bw.FLAC__bitwriter_write_zeroes( Jformat.FLAC__STREAM_METADATA_CUESHEET_TRACK_RESERVED_LEN ) ) {
						return false;
					}
					if( ! bw.FLAC__bitwriter_write_raw_uint32( track.num_indices, Jformat.FLAC__STREAM_METADATA_CUESHEET_TRACK_NUM_INDICES_LEN ) ) {
						return false;
					}
					for( int j = 0; j < track.num_indices; j++ ) {
						final JFLAC__StreamMetadata_CueSheet_Index indx = track.indices[j];

						if( ! bw.FLAC__bitwriter_write_raw_uint64( indx.offset, Jformat.FLAC__STREAM_METADATA_CUESHEET_INDEX_OFFSET_LEN ) ) {
							return false;
						}
						if( ! bw.FLAC__bitwriter_write_raw_uint32( indx.number, Jformat.FLAC__STREAM_METADATA_CUESHEET_INDEX_NUMBER_LEN ) ) {
							return false;
						}
						if( ! bw.FLAC__bitwriter_write_zeroes( Jformat.FLAC__STREAM_METADATA_CUESHEET_INDEX_RESERVED_LEN ) ) {
							return false;
						}
					}
				}
				break;
			case Jformat.FLAC__METADATA_TYPE_PICTURE:
				{
					try {
						final JFLAC__StreamMetadata_Picture picture = (JFLAC__StreamMetadata_Picture) metadata;
						int len;
						if( ! bw.FLAC__bitwriter_write_raw_uint32( picture.type, Jformat.FLAC__STREAM_METADATA_PICTURE_TYPE_LEN ) ) {
							return false;
						}
						len = picture.mime_type.length();
						if( ! bw.FLAC__bitwriter_write_raw_uint32( len, Jformat.FLAC__STREAM_METADATA_PICTURE_MIME_TYPE_LENGTH_LEN ) ) {
							return false;
						}
						if( ! bw.FLAC__bitwriter_write_byte_block( picture.mime_type.getBytes( JFLAC__StreamMetadata_Picture.MIME_ENCODING ), len ) ) {
							return false;
						}
						len = picture.description.length();
						if( ! bw.FLAC__bitwriter_write_raw_uint32( len, Jformat.FLAC__STREAM_METADATA_PICTURE_DESCRIPTION_LENGTH_LEN ) ) {
							return false;
						}
						if( ! bw.FLAC__bitwriter_write_byte_block( picture.description.getBytes( JFLAC__StreamMetadata_Picture.DESCRIPTION_ENCODING ), len ) ) {
							return false;
						}
						if( ! bw.FLAC__bitwriter_write_raw_uint32( picture.width, Jformat.FLAC__STREAM_METADATA_PICTURE_WIDTH_LEN ) ) {
							return false;
						}
						if( ! bw.FLAC__bitwriter_write_raw_uint32( picture.height, Jformat.FLAC__STREAM_METADATA_PICTURE_HEIGHT_LEN ) ) {
							return false;
						}
						if( ! bw.FLAC__bitwriter_write_raw_uint32( picture.depth, Jformat.FLAC__STREAM_METADATA_PICTURE_DEPTH_LEN ) ) {
							return false;
						}
						if( ! bw.FLAC__bitwriter_write_raw_uint32( picture.colors, Jformat.FLAC__STREAM_METADATA_PICTURE_COLORS_LEN ) ) {
							return false;
						}
						if( ! bw.FLAC__bitwriter_write_raw_uint32( picture.data_length, Jformat.FLAC__STREAM_METADATA_PICTURE_DATA_LENGTH_LEN ) ) {
							return false;
						}
						if( ! bw.FLAC__bitwriter_write_byte_block( picture.data, picture.data_length ) ) {
							return false;
						}
					} catch(final UnsupportedEncodingException e) {
						return false;
					}
				}
				break;
			default:
				if( ! bw.FLAC__bitwriter_write_byte_block( ((JFLAC__StreamMetadata_Unknown)metadata).data, metadata.length ) ) {
					return false;
				}
				break;
		}

		//FLAC__ASSERT(FLAC__bitwriter_is_byte_aligned(bw));
		return true;
	}

	// metadata_iterators.c
	private class Jlevel0_client_data implements JFLAC__StreamDecoderMetadataCallback, JFLAC__StreamDecoderErrorCallback{
		private boolean got_error;
		private JFLAC__StreamMetadata object;

		@Override// metadata_callback_
		public final void dec_metadata_callback(final JFLAC__StreamDecoder decoder, final JFLAC__StreamMetadata metadata/*, Object client_data*/) throws IOException {

			// final Jlevel0_client_data cd = (Jlevel0_client_data)client_data;// java: this

			/*
			 * we assume we only get here when the one metadata block we were
			 * looking for was passed to us
			 */
			if( ! this.got_error && null == this.object ) {
				if( null == (this.object = FLAC__metadata_object_clone( metadata )) ) {
					this.got_error = true;
				}
			}
		}

		@Override// error_callback_
		public final void dec_error_callback(final JFLAC__StreamDecoder decoder, final int status/*, Object client_data*/) {

			// final Jlevel0_client_data cd = (Jlevel0_client_data)client_data;// java: this

			if( status != JFLAC__StreamDecoder.FLAC__STREAM_DECODER_ERROR_STATUS_LOST_SYNC ) {
				this.got_error = true;
			}
		}
	};

	final JFLAC__StreamMetadata get_one_metadata_block_(final String filename, final int /* FLAC__MetadataType */ metadata_type)
	{
		final Jlevel0_client_data cd = new Jlevel0_client_data();

		//FLAC__ASSERT(0 != filename);

		cd.got_error = false;
		cd.object = null;

		final JFLAC__StreamDecoder decoder = new JFLAC__StreamDecoder();

		decoder.FLAC__stream_decoder_set_md5_checking( false );
		decoder.FLAC__stream_decoder_set_metadata_ignore_all();
		decoder.FLAC__stream_decoder_set_metadata_respond( metadata_type );

		if( decoder.FLAC__stream_decoder_init_file( filename,
				this,// write_callback_,
				cd,// metadata_callback_,
				cd// ,// error_callback_,
				/* cd */ ) != JFLAC__StreamDecoder.FLAC__STREAM_DECODER_INIT_STATUS_OK || cd.got_error ) {
			decoder.FLAC__stream_decoder_finish();
			decoder.FLAC__stream_decoder_delete();
			return null;
		}

		if( ! decoder.FLAC__stream_decoder_process_until_end_of_metadata() || cd.got_error ) {
			decoder.FLAC__stream_decoder_finish();
			decoder.FLAC__stream_decoder_delete();
			if( null != cd.object ) {
				FLAC__metadata_object_delete( cd.object );
			}
			return null;
		}

		decoder.FLAC__stream_decoder_finish();
		decoder.FLAC__stream_decoder_delete();

		return cd.object;
	}

	// metadata_iterators.c
	@Override// write_callback_
	public final int dec_write_callback(final JFLAC__StreamDecoder decoder, final JFLAC__Frame frame, final int[][] buffer, final int offset/*, Object client_data*/) {
		return JFLAC__StreamDecoder.FLAC__STREAM_DECODER_WRITE_STATUS_CONTINUE;
	}
}
