package libFLAC;

/** Structure that is used when a metadata block of unknown type is loaded.
 *  The contents are opaque.  The structure is used only internally to
 *  correctly handle unknown metadata.
 */
public final class JFLAC__StreamMetadata_Unknown extends JFLAC__StreamMetadata {
	byte[] data = null;

	JFLAC__StreamMetadata_Unknown() {
		super.type = Jformat.FLAC__METADATA_TYPE_UNDEFINED;
	}

	JFLAC__StreamMetadata_Unknown(final JFLAC__StreamMetadata_Unknown m) {
		super( m );
		data = m.data;// TODO check, C uses full copy
	}

	static boolean compare_block_data_unknown_(final JFLAC__StreamMetadata_Unknown block1, final JFLAC__StreamMetadata_Unknown block2, final int block_length)
	{
		//FLAC__ASSERT(block1 != NULL);
		//FLAC__ASSERT(block2 != NULL);

		if( block1.data != null && block2.data != null ) {
			return Jformat.memcmp( block1.data, 0, block2.data, 0, block_length ) == 0;
		}
		//else
			return block1.data == block2.data;
	}
}
