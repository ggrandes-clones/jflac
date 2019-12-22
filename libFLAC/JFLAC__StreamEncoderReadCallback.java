package libFLAC;

import java.io.IOException;

/** Signature for the read callback.
*
*  A function pointer matching this signature must be passed to
*  FLAC__stream_encoder_init_ogg_stream() if seeking is supported.
*  The supplied function will be called when the encoder needs to read back
*  encoded data.  This happens during the metadata callback, when the encoder
*  has to read, modify, and rewrite the metadata (e.g. seekpoints) gathered
*  while encoding.  The address of the buffer to be filled is supplied, along
*  with the number of bytes the buffer can hold.  The callback may choose to
*  supply less data and modify the byte count but must be careful not to
*  overflow the buffer.  The callback then returns a status code chosen from
*  FLAC__StreamEncoderReadStatus.
*
* Here is an example of a read callback for stdio streams:
* \code
* FLAC__StreamEncoderReadStatus read_cb(const FLAC__StreamEncoder *encoder, FLAC__byte buffer[], size_t *bytes, void *client_data)
* {
*   FILE *file = ((MyClientData*)client_data)->file;
*   if(*bytes > 0) {
*     *bytes = fread(buffer, sizeof(FLAC__byte), *bytes, file);
*     if(ferror(file))
*       return FLAC__STREAM_ENCODER_READ_STATUS_ABORT;
*     else if(*bytes == 0)
*       return FLAC__STREAM_ENCODER_READ_STATUS_END_OF_STREAM;
*     else
*       return FLAC__STREAM_ENCODER_READ_STATUS_CONTINUE;
*   }
*   else
*     return FLAC__STREAM_ENCODER_READ_STATUS_ABORT;
* }
* \endcode
*
* @note In general, FLAC__StreamEncoder functions which change the
* state should not be called on the \a encoder while in the callback.
*
* @param  encoder  The encoder instance calling the callback.
* @param  buffer   A pointer to a location for the callee to store
*                  data to be encoded.
* @param  bytes    A pointer to the size of the buffer.  On entry
*                  to the callback, it contains the maximum number
*                  of bytes that may be stored in \a buffer.  The
*                  callee must set it to the actual number of bytes
*                  stored (0 in case of error or end-of-stream) before
*                  returning.
* @param  client_data  The callee's client data set through
*                      FLAC__stream_encoder_set_client_data().
* @retval FLAC__StreamEncoderReadStatus
*    The callee's return status.
*/
//typedef FLAC__StreamEncoderReadStatus (*FLAC__StreamEncoderReadCallback)(const FLAC__StreamEncoder *encoder, FLAC__byte buffer[], size_t *bytes, void *client_data);
public interface JFLAC__StreamEncoderReadCallback {
	// java: changed. return read bytes instead FLAC__StreamDecoderReadStatus. uses IOException.
	/**
     * Reads up to <code>bytes</code> bytes of data from this file into an
     * array of bytes. This method blocks until at least one byte of input
     * is available.
     * <p>
     *
     * @param      buffer the buffer into which the data is read.
     * @param      offset the start offset in array <code>buffer</code>
     *                   at which the data is written.
     * @param      bytes  the maximum number of bytes read.
     * @return     the total number of bytes read into the buffer, or
     *             <code>-1</code> if there is no more data because the end of
     *             the file has been reached.
     * @exception  IOException If the first byte cannot be read for any reason
     * other than end of file, or if the random access file has been closed, or if
     * some other I/O error occurs.
     * @exception  NullPointerException If <code>buffer</code> is <code>null</code>.
     * @exception  IndexOutOfBoundsException If <code>offset</code> is negative,
     * <code>bytes</code> is negative, or <code>bytes</code> is greater than
     * <code>buffer.length - offset</code>
     */
	public int enc_read_callback(final JFLAC__StreamEncoder encoder, byte buffer[], int offset, int bytes/*, Object client_data*/) throws IOException, UnsupportedOperationException;
}
