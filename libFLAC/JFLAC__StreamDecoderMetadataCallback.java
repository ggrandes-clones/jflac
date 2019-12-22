package libFLAC;

import java.io.IOException;

/** Signature for the metadata callback.
*
*  A function pointer matching this signature must be passed to one of
*  the FLAC__stream_decoder_init_*() functions.
*  The supplied function will be called when the decoder has decoded a
*  metadata block.  In a valid FLAC file there will always be one
*  \c STREAMINFO block, followed by zero or more other metadata blocks.
*  These will be supplied by the decoder in the same order as they
*  appear in the stream and always before the first audio frame (i.e.
*  write callback).  The metadata block that is passed in must not be
*  modified, and it doesn't live beyond the callback, so you should make
*  a copy of it with FLAC__metadata_object_clone() if you will need it
*  elsewhere.  Since metadata blocks can potentially be large, by
*  default the decoder only calls the metadata callback for the
*  \c STREAMINFO block; you can instruct the decoder to pass or filter
*  other blocks with FLAC__stream_decoder_set_metadata_*() calls.
*
* @note In general, FLAC__StreamDecoder functions which change the
* state should not be called on the \a decoder while in the callback.
*
* @param  decoder  The decoder instance calling the callback.
* @param  metadata The decoded metadata block.
* @param  client_data  The callee's client data set through
*                      FLAC__stream_decoder_init_*().
*/
//typedef void (*FLAC__StreamDecoderMetadataCallback)(const FLAC__StreamDecoder *decoder, const FLAC__StreamMetadata *metadata, void *client_data);
public interface JFLAC__StreamDecoderMetadataCallback {
	//typedef void (*FLAC__StreamDecoderMetadataCallback)(const FLAC__StreamDecoder *decoder, const FLAC__StreamMetadata *metadata, void *client_data);
	public void dec_metadata_callback(final JFLAC__StreamDecoder decoder, final JFLAC__StreamMetadata metadata/*, Object client_data*/) throws IOException;
}
