package libFLAC;

import java.io.IOException;

/** Signature for the tell callback.
*
*  A function pointer matching this signature may be passed to
*  FLAC__stream_decoder_init*_stream().  The supplied function will be
*  called when the decoder wants to know the current position of the
*  stream.  The callback should return the byte offset from the
*  beginning of the stream.
*
* Here is an example of a tell callback for stdio streams:
* \code
* FLAC__StreamDecoderTellStatus tell_cb(const FLAC__StreamDecoder *decoder, FLAC__uint64 *absolute_byte_offset, void *client_data)
* {
*   FILE *file = ((MyClientData*)client_data)->file;
*   off_t pos;
*   if(file == stdin)
*     return FLAC__STREAM_DECODER_TELL_STATUS_UNSUPPORTED;
*   else if((pos = ftello(file)) < 0)
*     return FLAC__STREAM_DECODER_TELL_STATUS_ERROR;
*   else {
*     *absolute_byte_offset = (FLAC__uint64)pos;
*     return FLAC__STREAM_DECODER_TELL_STATUS_OK;
*   }
* }
* \endcode
*
* @note In general, FLAC__StreamDecoder functions which change the
* state should not be called on the \a decoder while in the callback.
*
* @param  decoder  The decoder instance calling the callback.
* @param  absolute_byte_offset  A pointer to storage for the current offset
*                               from the beginning of the stream.
* @param  client_data  The callee's client data set through
*                      FLAC__stream_decoder_init_*().
* @retval FLAC__StreamDecoderTellStatus
*    The callee's return status.
*/
//typedef FLAC__StreamDecoderTellStatus (*FLAC__StreamDecoderTellCallback)(const FLAC__StreamDecoder *decoder, FLAC__uint64 *absolute_byte_offset, void *client_data);
public interface JFLAC__StreamDecoderTellCallback {
	//typedef FLAC__StreamDecoderTellStatus (*FLAC__StreamDecoderTellCallback)(const FLAC__StreamDecoder *decoder, FLAC__uint64 *absolute_byte_offset, void *client_data);
	// java: changed: absolute_byte_offset is returned. uses IOException
	/**
     * Returns the current offset in this file.
     *
     * @return     the offset from the beginning of the file, in bytes,
     *             at which the next read or write occurs.
     * @exception  IOException  if an I/O error occurs.
     */
	public long dec_tell_callback(final JFLAC__StreamDecoder decoder/*, Object client_data*/) throws IOException, UnsupportedOperationException;
}
