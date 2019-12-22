package libFLAC;

/** typedef FLAC__StreamEncoderWriteStatus (*FLAC__OggEncoderAspectWriteCallbackProxy)(const void *encoder, const FLAC__byte buffer[], size_t bytes, unsigned samples, unsigned current_frame, void *client_data); */
interface JFLAC__OggEncoderAspectWriteCallbackProxy {
	int write_callback(final Object encoder, final byte buffer[], int offset, int bytes, int samples, int current_frame/*, Object client_data*/);
}
