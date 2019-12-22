package libFLAC;

/** Signature for the EOF callback.
*
*  A function pointer matching this signature may be passed to
*  FLAC__stream_decoder_init*_stream().  The supplied function will be
*  called when the decoder needs to know if the end of the stream has
*  been reached.
*
* Here is an example of a EOF callback for stdio streams:
* FLAC__bool eof_cb(const FLAC__StreamDecoder *decoder, void *client_data)
* \code
* {
*   FILE *file = ((MyClientData*)client_data)->file;
*   return feof(file)? true : false;
* }
* \endcode
*
* @note In general, FLAC__StreamDecoder functions which change the
* state should not be called on the \a decoder while in the callback.
*
* @param  decoder  The decoder instance calling the callback.
* @param  client_data  The callee's client data set through
*                      FLAC__stream_decoder_init_*().
* @retval FLAC__bool
*    \c true if the currently at the end of the stream, else \c false.
*/
//typedef FLAC__bool (*FLAC__StreamDecoderEofCallback)(const FLAC__StreamDecoder *decoder, void *client_data);
public interface JFLAC__StreamDecoderEofCallback {
	//typedef FLAC__bool (*FLAC__StreamDecoderEofCallback)(const FLAC__StreamDecoder *decoder, void *client_data);
	public boolean dec_eof_callback(final JFLAC__StreamDecoder decoder/*, Object client_data*/);
}
