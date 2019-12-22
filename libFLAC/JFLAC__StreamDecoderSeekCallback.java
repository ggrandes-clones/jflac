package libFLAC;

/** Signature for the seek callback.
*
*  A function pointer matching this signature may be passed to
*  FLAC__stream_decoder_init*_stream().  The supplied function will be
*  called when the decoder needs to seek the input stream.  The decoder
*  will pass the absolute byte offset to seek to, 0 meaning the
*  beginning of the stream.
*
* Here is an example of a seek callback for stdio streams:
* \code
* FLAC__StreamDecoderSeekStatus seek_cb(const FLAC__StreamDecoder *decoder, FLAC__uint64 absolute_byte_offset, void *client_data)
* {
*   FILE *file = ((MyClientData*)client_data)->file;
*   if(file == stdin)
*     return FLAC__STREAM_DECODER_SEEK_STATUS_UNSUPPORTED;
*   else if(fseeko(file, (off_t)absolute_byte_offset, SEEK_SET) < 0)
*     return FLAC__STREAM_DECODER_SEEK_STATUS_ERROR;
*   else
*     return FLAC__STREAM_DECODER_SEEK_STATUS_OK;
* }
* \endcode
*
* @note In general, FLAC__StreamDecoder functions which change the
* state should not be called on the \a decoder while in the callback.
*
* @param  decoder  The decoder instance calling the callback.
* @param  absolute_byte_offset  The offset from the beginning of the stream
*                               to seek to.
* @param  client_data  The callee's client data set through
*                      FLAC__stream_decoder_init_*().
* @retval FLAC__StreamDecoderSeekStatus
*    The callee's return status.
*/
//typedef FLAC__StreamDecoderSeekStatus (*FLAC__StreamDecoderSeekCallback)(const FLAC__StreamDecoder *decoder, FLAC__uint64 absolute_byte_offset, void *client_data);
public interface JFLAC__StreamDecoderSeekCallback {
	//typedef FLAC__StreamDecoderSeekStatus (*FLAC__StreamDecoderSeekCallback)(const FLAC__StreamDecoder *decoder, FLAC__uint64 absolute_byte_offset, void *client_data);
	public int dec_seek_callback(final JFLAC__StreamDecoder decoder, long absolute_byte_offset/*, Object client_data*/);
}
