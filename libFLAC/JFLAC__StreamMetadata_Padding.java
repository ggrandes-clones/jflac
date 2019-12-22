package libFLAC;

/** FLAC PADDING structure.  (c.f. <A HREF="../format.html#metadata_block_padding">format specification</A>)
 */
public final class JFLAC__StreamMetadata_Padding extends JFLAC__StreamMetadata {
	/** Conceptually this is an empty struct since we don't store the
	 * padding bytes.  Empty structs are not allowed by some C compilers,
	 * hence the dummy.
	 */
	public JFLAC__StreamMetadata_Padding() {
		super.type = Jformat.FLAC__METADATA_TYPE_PADDING;
	}
	JFLAC__StreamMetadata_Padding(final JFLAC__StreamMetadata_Padding m) {
		super( m );
	}
}
