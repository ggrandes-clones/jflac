package libFLAC;

import java.io.IOException;

/** Signature for the length callback.
*
*  A function pointer matching this signature may be passed to
*  FLAC__stream_decoder_init*_stream().  The supplied function will be
*  called when the decoder wants to know the total length of the stream
*  in bytes.
*
* Here is an example of a length callback for stdio streams:
* \code
* FLAC__StreamDecoderLengthStatus length_cb(const FLAC__StreamDecoder *decoder, FLAC__uint64 *stream_length, void *client_data)
* {
*   FILE *file = ((MyClientData*)client_data)->file;
*   struct stat filestats;
*
*   if(file == stdin)
*     return FLAC__STREAM_DECODER_LENGTH_STATUS_UNSUPPORTED;
*   else if(fstat(fileno(file), &filestats) != 0)
*     return FLAC__STREAM_DECODER_LENGTH_STATUS_ERROR;
*   else {
*     *stream_length = (FLAC__uint64)filestats.st_size;
*     return FLAC__STREAM_DECODER_LENGTH_STATUS_OK;
*   }
* }
* \endcode
*
* @note In general, FLAC__StreamDecoder functions which change the
* state should not be called on the \a decoder while in the callback.
*
* @param  decoder  The decoder instance calling the callback.
* @param  stream_length  A pointer to storage for the length of the stream
*                        in bytes.
* @param  client_data  The callee's client data set through
*                      FLAC__stream_decoder_init_*().
* @retval FLAC__StreamDecoderLengthStatus
*    The callee's return status.
*/
//typedef FLAC__StreamDecoderLengthStatus (*FLAC__StreamDecoderLengthCallback)(const FLAC__StreamDecoder *decoder, FLAC__uint64 *stream_length, void *client_data);
public interface JFLAC__StreamDecoderLengthCallback {
	//typedef FLAC__StreamDecoderLengthStatus (*FLAC__StreamDecoderLengthCallback)(const FLAC__StreamDecoder *decoder, FLAC__uint64 *stream_length, void *client_data);
	//java: changed. stream_length is returned. uses IOException
	/**
     * Returns the length of this file.
     *
     * @return     the length of this file, measured in bytes.
     * @exception  IOException  if an I/O error occurs.
     */
	public long dec_length_callback(final JFLAC__StreamDecoder decoder/*, Object client_data*/) throws IOException, UnsupportedOperationException;
}
