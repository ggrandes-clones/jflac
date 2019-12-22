package libFLAC;

import java.io.IOException;

/** Signature for the error callback.
*
*  A function pointer matching this signature must be passed to one of
*  the FLAC__stream_decoder_init_*() functions.
*  The supplied function will be called whenever an error occurs during
*  decoding.
*
* @note In general, FLAC__StreamDecoder functions which change the
* state should not be called on the \a decoder while in the callback.
*
* @param  decoder  The decoder instance calling the callback.
* @param  status   The error encountered by the decoder.
* @param  client_data  The callee's client data set through
*                      FLAC__stream_decoder_init_*().
*/
//typedef void (*FLAC__StreamDecoderErrorCallback)(const FLAC__StreamDecoder *decoder, FLAC__StreamDecoderErrorStatus status, void *client_data);
public interface JFLAC__StreamDecoderErrorCallback {
	//typedef void (*FLAC__StreamDecoderErrorCallback)(const FLAC__StreamDecoder *decoder, FLAC__StreamDecoderErrorStatus status, void *client_data);
	// XXX java: added throws IOException to abort decoder if it need
	public void dec_error_callback(final JFLAC__StreamDecoder decoder, int /*JFLAC__StreamDecoderErrorStatus*/ status/*, Object client_data*/) throws IOException;
}
