package libFLAC;

public final class JFLAC__Metadata_Iterator {
	private JFLAC__Metadata_Chain chain = null;
	private JFLAC__Metadata_Node current = null;

	/* java: use iterator = null
	public static void FLAC__metadata_iterator_delete(JFLAC__Metadata_Iterator iterator)
	{
		//FLAC__ASSERT(0 != iterator);

		//free(iterator);
	}*/

	public final void FLAC__metadata_iterator_init(final JFLAC__Metadata_Chain metadata_chain)
	{
		//FLAC__ASSERT(0 != iterator);
		//FLAC__ASSERT(0 != chain);
		//FLAC__ASSERT(0 != chain->head);

		this.chain = metadata_chain;
		this.current = metadata_chain.head;
	}

	public final boolean FLAC__metadata_iterator_next()
	{
		//FLAC__ASSERT(0 != iterator);

		if( null == this.current || null == this.current.next ) {
			return false;
		}

		this.current = this.current.next;
		return true;
	}

	public final boolean FLAC__metadata_iterator_prev()
	{
		//FLAC__ASSERT(0 != iterator);

		if( null == this.current || null == this.current.prev) {
			return false;
		}

		this.current = this.current.prev;
		return true;
	}

	public final int /* FLAC__MetadataType */ FLAC__metadata_iterator_get_block_type()
	{
		//FLAC__ASSERT(0 != iterator);
		//FLAC__ASSERT(0 != iterator->current);
		//FLAC__ASSERT(0 != iterator->current->data);

		return this.current.data.type;
	}

	public final JFLAC__StreamMetadata FLAC__metadata_iterator_get_block()
	{
		//FLAC__ASSERT(0 != iterator);
		//FLAC__ASSERT(0 != iterator->current);

		return this.current.data;
	}

	public final boolean FLAC__metadata_iterator_set_block(final JFLAC__StreamMetadata block)
	{
		//FLAC__ASSERT(0 != iterator);
		//FLAC__ASSERT(0 != block);
		return FLAC__metadata_iterator_delete_block( false ) &&
			FLAC__metadata_iterator_insert_block_after( block );
	}

	public final boolean FLAC__metadata_iterator_delete_block(final boolean replace_with_padding)
	{
		//FLAC__ASSERT(0 != iterator);
		//FLAC__ASSERT(0 != iterator->current);

		if( null == this.current.prev ) {
			//FLAC__ASSERT(iterator->current->data->type == FLAC__METADATA_TYPE_STREAMINFO);
			return false;
		}

		final JFLAC__Metadata_Node save = this.current.prev;

		if( replace_with_padding ) {
			JFLAC__StreamMetadata.FLAC__metadata_object_delete_data( this.current.data );
			this.current.data.type = Jformat.FLAC__METADATA_TYPE_PADDING;
		}
		else {
			this.chain.chain_delete_node_( this.current );
		}

		this.current = save;
		return true;
	}

	private final void iterator_insert_node_(final JFLAC__Metadata_Node node)
	{
		//FLAC__ASSERT(0 != node);
		//FLAC__ASSERT(0 != node->data);
		//FLAC__ASSERT(0 != iterator);
		//FLAC__ASSERT(0 != iterator->current);
		//FLAC__ASSERT(0 != iterator->chain);
		//FLAC__ASSERT(0 != iterator->chain->head);
		//FLAC__ASSERT(0 != iterator->chain->tail);

		node.data.is_last = false;

		node.prev = this.current.prev;
		node.next = this.current;

		if( null == node.prev ) {
			this.chain.head = node;
		} else {
			node.prev.next = node;
		}

		this.current.prev = node;

		this.chain.nodes++;
	}

	public final boolean FLAC__metadata_iterator_insert_block_before(final JFLAC__StreamMetadata block)
	{
		//FLAC__ASSERT(0 != iterator);
		//FLAC__ASSERT(0 != iterator->current);
		//FLAC__ASSERT(0 != block);

		if( block.type == Jformat.FLAC__METADATA_TYPE_STREAMINFO ) {
			return false;
		}

		if( null == this.current.prev ) {
			//FLAC__ASSERT(iterator->current->data->type == FLAC__METADATA_TYPE_STREAMINFO);
			return false;
		}

		//try {
			final JFLAC__Metadata_Node node = new JFLAC__Metadata_Node();
			node.data = block;
			iterator_insert_node_( node );
			this.current = node;
			return true;
		//} catch(OutOfMemoryError e) {
		//	return false;
		//}
	}

	private final void iterator_insert_node_after_(final JFLAC__Metadata_Node node)
	{
		//FLAC__ASSERT(0 != node);
		//FLAC__ASSERT(0 != node->data);
		//FLAC__ASSERT(0 != iterator);
		//FLAC__ASSERT(0 != iterator->current);
		//FLAC__ASSERT(0 != iterator->chain);
		//FLAC__ASSERT(0 != iterator->chain->head);
		//FLAC__ASSERT(0 != iterator->chain->tail);

		this.current.data.is_last = false;

		node.prev = this.current;
		node.next = this.current.next;

		if( null == node.next ) {
			this.chain.tail = node;
		} else {
			node.next.prev = node;
		}

		node.prev.next = node;

		this.chain.tail.data.is_last = true;

		this.chain.nodes++;
	}

	public final boolean FLAC__metadata_iterator_insert_block_after(final JFLAC__StreamMetadata block)
	{
		JFLAC__Metadata_Node node;

		//FLAC__ASSERT(0 != iterator);
		//FLAC__ASSERT(0 != iterator->current);
		//FLAC__ASSERT(0 != block);

		if( block.type == Jformat.FLAC__METADATA_TYPE_STREAMINFO ) {
			return false;
		}

		//try {
			node = new JFLAC__Metadata_Node();
			node.data = block;
			iterator_insert_node_after_( node );
			this.current = node;
			return true;
		//} catch(OutOfMemoryError e) {
		//	return false;
		//}
	}

}
