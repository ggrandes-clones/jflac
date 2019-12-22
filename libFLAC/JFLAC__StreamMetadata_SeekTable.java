package libFLAC;

import java.util.Arrays;

/** FLAC SEEKTABLE structure.  (c.f. <A HREF="../format.html#metadata_block_seektable">format specification</A>)
*
* @note From the format specification:
* - The seek points must be sorted by ascending sample number.
* - Each seek point's sample number must be the first sample of the
*   target frame.
* - Each seek point's sample number must be unique within the table.
* - Existence of a SEEKTABLE block implies a correct setting of
*   total_samples in the stream_info block.
* - Behavior is undefined when more than one SEEKTABLE block is
*   present in a stream.
*/
public final class JFLAC__StreamMetadata_SeekTable extends JFLAC__StreamMetadata {
	int num_points = 0;// TODO is it possible to replace with points.length?
	JFLAC__StreamMetadata_SeekPoint[] points = null;

	JFLAC__StreamMetadata_SeekTable() {
		super.type = Jformat.FLAC__METADATA_TYPE_SEEKTABLE;
	}

	JFLAC__StreamMetadata_SeekTable(final JFLAC__StreamMetadata_SeekTable m) {
		super( m );
		num_points = m.num_points;
		points = m.points;// TODO check, C uses full copy
	}

	private final void seektable_calculate_length_()
	{
		//FLAC__ASSERT(object != NULL);
		//FLAC__ASSERT(object->type == FLAC__METADATA_TYPE_SEEKTABLE);

		this.length = this.num_points * Jformat.FLAC__STREAM_METADATA_SEEKPOINT_LENGTH;
	}

	static boolean compare_block_data_seektable_(final JFLAC__StreamMetadata_SeekTable block1, final JFLAC__StreamMetadata_SeekTable block2)
	{
		//FLAC__ASSERT(block1 != NULL);
		//FLAC__ASSERT(block2 != NULL);

		if( block1.num_points != block2.num_points ) {
			return false;
		}

		if( block1.points != null && block2.points != null ) {
			final JFLAC__StreamMetadata_SeekPoint[] points1 = block1.points;// java
			final JFLAC__StreamMetadata_SeekPoint[] points2 = block2.points;// java
			for( int i = 0; i < block1.num_points; i++ ) {
				final JFLAC__StreamMetadata_SeekPoint p1 = points1[i];// java
				final JFLAC__StreamMetadata_SeekPoint p2 = points2[i];// java
				if( p1.sample_number != p2.sample_number ) {
					return false;
				}
				if( p1.stream_offset != p2.stream_offset ) {
					return false;
				}
				if( p1.frame_samples != p2.frame_samples ) {
					return false;
				}
			}
			return true;
		} else {
			return block1.points == block2.points;
		}
	}

	public final boolean FLAC__metadata_object_seektable_resize_points(final int new_num_points)
	{
		//FLAC__ASSERT(object != NULL);
		//FLAC__ASSERT(object->type == FLAC__METADATA_TYPE_SEEKTABLE);

		if( this.points == null ) {
			//FLAC__ASSERT(object->data.seek_table.num_points == 0);
			if( new_num_points == 0 ) {
				return true;
			} else if( (this.points = JFLAC__StreamMetadata_SeekPoint.seekpoint_array_new_( new_num_points )) == null ) {
				return false;
			}
		}
		else {
			/* overflow check */
			if( new_num_points > Jformat.SIZE_MAX ) {
				return false;
			}

			//FLAC__ASSERT(object->data.seek_table.num_points > 0);
			final int old_size = this.num_points;
			final int new_size = new_num_points;
			if( new_size == 0 ) {
				this.points = null;
			}
			else if( (this.points = Arrays.copyOf( this.points, new_size )) == null ) {
				return false;
			}

			/* if growing, set new elements to placeholders */
			if( new_size > old_size ) {
				for( int i = this.num_points; i < new_num_points; i++ ) {
					final JFLAC__StreamMetadata_SeekPoint p = this.points[i];// java
					p.sample_number = Jformat.FLAC__STREAM_METADATA_SEEKPOINT_PLACEHOLDER;
					p.stream_offset = 0;
					p.frame_samples = 0;
				}
			}
		}

		this.num_points = new_num_points;

		seektable_calculate_length_();
		return true;
	}

	public final void FLAC__metadata_object_seektable_set_point(final int point_num, final JFLAC__StreamMetadata_SeekPoint point)
	{
		//FLAC__ASSERT(object != NULL);
		//FLAC__ASSERT(object->type == FLAC__METADATA_TYPE_SEEKTABLE);
		//FLAC__ASSERT(point_num < object->data.seek_table.num_points);

		this.points[point_num] = point;// TODO check. C uses copy values
	}

	public final boolean FLAC__metadata_object_seektable_insert_point(final int point_num, final JFLAC__StreamMetadata_SeekPoint point)
	{
		//FLAC__ASSERT(object != NULL);
		//FLAC__ASSERT(object->type == FLAC__METADATA_TYPE_SEEKTABLE);
		//FLAC__ASSERT(point_num <= object->data.seek_table.num_points);

		if( ! FLAC__metadata_object_seektable_resize_points( this.num_points + 1 ) ) {
			return false;
		}

		/* move all points >= point_num forward one space */
		for( int i = this.num_points - 1; i > point_num; i-- ) {
			this.points[i] = this.points[i - 1];
		}

		FLAC__metadata_object_seektable_set_point( point_num, point );
		seektable_calculate_length_();
		return true;
	}

	public final boolean FLAC__metadata_object_seektable_delete_point(final int point_num)
	{
		//FLAC__ASSERT(object != NULL);
		//FLAC__ASSERT(object->type == FLAC__METADATA_TYPE_SEEKTABLE);
		//FLAC__ASSERT(point_num < object->data.seek_table.num_points);

		/* move all points > point_num backward one space */
		for( int i = point_num; i < this.num_points - 1; i++ ) {
			this.points[i] = this.points[i + 1];
		}

		return FLAC__metadata_object_seektable_resize_points( this.num_points - 1 );
	}

	public final boolean FLAC__metadata_object_seektable_is_legal()
	{
		//FLAC__ASSERT(object != NULL);
		//FLAC__ASSERT(object->type == FLAC__METADATA_TYPE_SEEKTABLE);

		return FLAC__format_seektable_is_legal();
	}

	public final boolean FLAC__metadata_object_seektable_template_append_placeholders(final int num)
	{
		//FLAC__ASSERT(object != NULL);
		//FLAC__ASSERT(object->type == FLAC__METADATA_TYPE_SEEKTABLE);

		if( num > 0 ) {
			/* WATCHOUT: we rely on the fact that growing the array adds PLACEHOLDERS at the end */
			return FLAC__metadata_object_seektable_resize_points( this.num_points + num );
		}// else {
			return true;
		//}
	}

	public final boolean FLAC__metadata_object_seektable_template_append_point(final long sample_number)
	{
		//FLAC__ASSERT(object != NULL);
		//FLAC__ASSERT(object->type == FLAC__METADATA_TYPE_SEEKTABLE);

		if( ! FLAC__metadata_object_seektable_resize_points( this.num_points + 1 ) ) {
			return false;
		}

		final JFLAC__StreamMetadata_SeekPoint p = this.points[this.num_points - 1];// java
		p.sample_number = sample_number;
		p.stream_offset = 0;
		p.frame_samples = 0;

		return true;
	}

	public final boolean FLAC__metadata_object_seektable_template_append_points(final long sample_numbers[], final int num)
	{
		//FLAC__ASSERT(object != NULL);
		//FLAC__ASSERT(object->type == FLAC__METADATA_TYPE_SEEKTABLE);
		//FLAC__ASSERT(sample_numbers != 0 || num == 0);

		if( num > 0 ) {
			int i = this.num_points;

			if( ! FLAC__metadata_object_seektable_resize_points( this.num_points + num ) ) {
				return false;
			}

			for( int j = 0; j < num; i++, j++ ) {
				final JFLAC__StreamMetadata_SeekPoint p = this.points[i];// java
				p.sample_number = sample_numbers[j];
				p.stream_offset = 0;
				p.frame_samples = 0;
			}
		}

		return true;
	}

	public final boolean FLAC__metadata_object_seektable_template_append_spaced_points(final int num, final long total_samples)
	{
		//FLAC__ASSERT(object != NULL);
		//FLAC__ASSERT(object->type == FLAC__METADATA_TYPE_SEEKTABLE);
		//FLAC__ASSERT(total_samples > 0);

		if( num > 0 && total_samples > 0 ) {
			int i = this.num_points;

			if( ! FLAC__metadata_object_seektable_resize_points( this.num_points + num ) ) {
				return false;
			}

			for( int j = 0; j < num; i++, j++ ) {
				final JFLAC__StreamMetadata_SeekPoint p = this.points[i];// java
				p.sample_number = total_samples * (long)j / (long)num;
				p.stream_offset = 0;
				p.frame_samples = 0;
			}
		}

		return true;
	}

	public final boolean FLAC__metadata_object_seektable_template_append_spaced_points_by_samples(int samples, final long total_samples)
	{
		//FLAC__ASSERT(object != NULL);
		//FLAC__ASSERT(object->type == FLAC__METADATA_TYPE_SEEKTABLE);
		//FLAC__ASSERT(samples > 0);
		//FLAC__ASSERT(total_samples > 0);

		if( samples > 0 && total_samples > 0 ) {

			long num = 1 + total_samples / samples; /* 1+ for the first sample at 0 */
			/* now account for the fact that we don't place a seekpoint at "total_samples" since samples are number from 0: */
			if( total_samples % samples == 0 ) {
				num--;
			}

			/* Put a strict upper bound on the number of allowed seek points. */
			if( num > 32768 ) {
				/* Set the bound and recalculate samples accordingly. */
				num = 32768;
				samples = (int)(total_samples / num);
			}

			int i = this.num_points;

			if( ! FLAC__metadata_object_seektable_resize_points( this.num_points + (int)num ) ) {
				return false;
			}

			long sample = 0;
			for( int j = 0; j < num; i++, j++, sample += samples ) {
				final JFLAC__StreamMetadata_SeekPoint p = this.points[i];// java
				p.sample_number = sample;
				p.stream_offset = 0;
				p.frame_samples = 0;
			}
		}

		return true;
	}

	public final boolean FLAC__metadata_object_seektable_template_sort(final boolean compact)
	{
		//FLAC__ASSERT(object != NULL);
		//FLAC__ASSERT(object->type == FLAC__METADATA_TYPE_SEEKTABLE);

		final int unique = FLAC__format_seektable_sort();

		return ! compact || FLAC__metadata_object_seektable_resize_points( unique );
	}

	/** @@@@ add to unit tests; it is already indirectly tested by the metadata_object tests */
	public final boolean FLAC__format_seektable_is_legal()
	{
		long prev_sample_number = 0;
		boolean got_prev = false;

		//FLAC__ASSERT(0 != seek_table);

		for( int i = 0, ie = this.num_points; i < ie; i++ ) {
			final JFLAC__StreamMetadata_SeekPoint p = this.points[i];// java
			if( got_prev ) {
				if(
					p.sample_number != Jformat.FLAC__STREAM_METADATA_SEEKPOINT_PLACEHOLDER &&
					p.sample_number <= prev_sample_number
				) {
					return false;
				}
			}
			prev_sample_number = p.sample_number;
			got_prev = true;
		}

		return true;
	}

	/* @@@@ add to unit tests; it is already indirectly tested by the metadata_object tests */
	public final int FLAC__format_seektable_sort()
	{

		//FLAC__ASSERT(0 != seek_table);

		if( this.num_points == 0 ) {
			return 0;
		}

		/* sort the seekpoints */
		//qsort( seek_table->points, seek_table->num_points, sizeof(FLAC__StreamMetadata_SeekPoint), (int (*)(const void *, const void *))seekpoint_compare_ );
		Arrays.sort( this.points );// TODO check sorting

		/* uniquify the seekpoints */
		boolean first = true;
		final int npoints = this.num_points;// java
		int j = 0;
		for( int i = j; i < npoints; i++ ) {
			final JFLAC__StreamMetadata_SeekPoint p = this.points[i];// java
			if( p.sample_number != Jformat.FLAC__STREAM_METADATA_SEEKPOINT_PLACEHOLDER ) {
				if( ! first ) {
					if( p.sample_number == this.points[j - 1].sample_number ) {
						continue;
					}
				}
			}
			first = false;
			this.points[j++] = p;
		}

		for( int i = j; i < npoints; i++ ) {
			final JFLAC__StreamMetadata_SeekPoint p = this.points[i];// java
			p.sample_number = Jformat.FLAC__STREAM_METADATA_SEEKPOINT_PLACEHOLDER;
			p.stream_offset = 0;
			p.frame_samples = 0;
		}

		return j;
	}
}
