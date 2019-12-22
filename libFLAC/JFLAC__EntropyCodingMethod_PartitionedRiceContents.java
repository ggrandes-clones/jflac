package libFLAC;

import java.util.Arrays;

/** Contents of a Rice partitioned residual
 */
final class JFLAC__EntropyCodingMethod_PartitionedRiceContents {
	/** The Rice parameters for each context. */
	int[] parameters = null;

	/** Widths for escape-coded partitions.  Will be non-zero for escaped
	 * partitions and zero for unescaped partitions.
	 */
	int[] raw_bits = null;

	/** The capacity of the \a parameters and \a raw_bits arrays
	 * specified as an order, i.e. the number of array elements
	 * allocated is 2 ^ \a capacity_by_order.
	 */
	int capacity_by_order = 0;

	final void FLAC__format_entropy_coding_method_partitioned_rice_contents_init()
	{
		this.parameters = null;
		this.raw_bits = null;
		this.capacity_by_order = 0;
	}

	final void FLAC__format_entropy_coding_method_partitioned_rice_contents_clear()
	{
		this.parameters = null;
		this.raw_bits = null;
		FLAC__format_entropy_coding_method_partitioned_rice_contents_init();
	}

	final boolean FLAC__format_entropy_coding_method_partitioned_rice_contents_ensure_size(final int max_partition_order)
	{
		//FLAC__ASSERT(0 != object);

		//FLAC__ASSERT(object->capacity_by_order > 0 || (0 == object->parameters && 0 == object->raw_bits));

		if( this.capacity_by_order < max_partition_order ) {
			try {
				final int size = 1 << max_partition_order;// java
				this.parameters = this.parameters == null ? new int[size] : Arrays.copyOf( this.parameters, size );
				this.raw_bits = this.raw_bits == null ? new int[size] : Arrays.copyOf( this.raw_bits, size );
				Arrays.fill( this.raw_bits, 0 );
				this.capacity_by_order = max_partition_order;
			} catch(final OutOfMemoryError e) {
				return false;
			}
		}

		return true;
	}
}
