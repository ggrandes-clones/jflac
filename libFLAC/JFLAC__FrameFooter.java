package libFLAC;

/** FLAC frame footer structure.  (c.f. <A HREF="../format.html#frame_footer">format specification</A>)
 */
final class JFLAC__FrameFooter {
	/** CRC-16 (polynomial = x^16 + x^15 + x^2 + x^0, initialized with
	 * 0) of the bytes before the crc, back to and including the frame header
	 * sync code.
	 */
	short crc = 0;
}
