package libFLAC;

/** Signature for the write callback.
*
*  A function pointer matching this signature must be passed to
*  FLAC__stream_encoder_init*_stream().  The supplied function will be called
*  by the encoder anytime there is raw encoded data ready to write.  It may
*  include metadata mixed with encoded audio frames and the data is not
*  guaranteed to be aligned on frame or metadata block boundaries.
*
*  The only duty of the callback is to write out the \a bytes worth of data
*  in \a buffer to the current position in the output stream.  The arguments
*  \a samples and \a current_frame are purely informational.  If \a samples
*  is greater than \c 0, then \a current_frame will hold the current frame
*  number that is being written; otherwise it indicates that the write
*  callback is being called to write metadata.
*
* @note
* Unlike when writing to native FLAC, when writing to Ogg FLAC the
* write callback will be called twice when writing each audio
* frame; once for the page header, and once for the page body.
* When writing the page header, the \a samples argument to the
* write callback will be \c 0.
*
* @note In general, FLAC__StreamEncoder functions which change the
* state should not be called on the \a encoder while in the callback.
*
* @param  encoder  The encoder instance calling the callback.
* @param  buffer   An array of encoded data of length \a bytes.
* @param  bytes    The byte length of \a buffer.
* @param  samples  The number of samples encoded by \a buffer.
*                  \c 0 has a special meaning; see above.
* @param  current_frame  The number of the current frame being encoded.
* @param  client_data  The callee's client data set through
*                      FLAC__stream_encoder_init_*().
* @retval FLAC__StreamEncoderWriteStatus
*    The callee's return status.
*/
//typedef FLAC__StreamEncoderWriteStatus (*FLAC__StreamEncoderWriteCallback)(const FLAC__StreamEncoder *encoder, const FLAC__byte buffer[], size_t bytes, unsigned samples, unsigned current_frame, void *client_data);
public interface JFLAC__StreamEncoderWriteCallback {
	public int enc_write_callback(final JFLAC__StreamEncoder encoder, final byte buffer[], int offset, int bytes, int samples, int current_frame/*, Object client_data*/);
}
