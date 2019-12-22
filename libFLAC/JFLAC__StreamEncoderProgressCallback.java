package libFLAC;

/** Signature for the progress callback.
*
*  A function pointer matching this signature may be passed to
*  FLAC__stream_encoder_init*_file() or FLAC__stream_encoder_init*_FILE().
*  The supplied function will be called when the encoder has finished
*  writing a frame.  The \c total_frames_estimate argument to the
*  callback will be based on the value from
*  FLAC__stream_encoder_set_total_samples_estimate().
*
* @note In general, FLAC__StreamEncoder functions which change the
* state should not be called on the \a encoder while in the callback.
*
* @param  encoder          The encoder instance calling the callback.
* @param  bytes_written    Bytes written so far.
* @param  samples_written  Samples written so far.
* @param  frames_written   Frames written so far.
* @param  total_frames_estimate  The estimate of the total number of
*                                frames to be written.
* @param  client_data      The callee's client data set through
*                          FLAC__stream_encoder_init_*().
*/
public interface JFLAC__StreamEncoderProgressCallback {
	//typedef void (*FLAC__StreamEncoderProgressCallback)(const FLAC__StreamEncoder *encoder, FLAC__uint64 bytes_written, FLAC__uint64 samples_written, unsigned frames_written, unsigned total_frames_estimate, void *client_data);
	public void enc_progress_callback(final JFLAC__StreamEncoder encoder, long bytes_written, long samples_written, int frames_written, int total_frames_estimate/*, Object client_data*/);
}
