package libFLAC;

/** FLAC frame structure.  (c.f. <A HREF="../format.html#frame">format specification</A>)
 */
public final class JFLAC__Frame {
	public final JFLAC__FrameHeader header = new JFLAC__FrameHeader();
	public final JFLAC__Subframe[] subframes = new JFLAC__Subframe[Jformat.FLAC__MAX_CHANNELS];
	public final JFLAC__FrameFooter footer = new JFLAC__FrameFooter();

	JFLAC__Frame() {
		for( int i = 0; i < Jformat.FLAC__MAX_CHANNELS; i++ ) {
			subframes[i] = new JFLAC__Subframe();
		}
	}
}
