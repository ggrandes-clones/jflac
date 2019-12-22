package libFLAC;

final class JFLAC__Metadata_Node {
	JFLAC__StreamMetadata data = null;
	JFLAC__Metadata_Node prev = null, next = null;

	final void node_delete_()
	{
		//FLAC__ASSERT(0 != node);
		if( null != this.data )
			JFLAC__StreamMetadata.FLAC__metadata_object_delete( this.data );
		//free(node);
	}
}
