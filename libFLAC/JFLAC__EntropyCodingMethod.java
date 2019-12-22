package libFLAC;

/** Header for the entropy coding method.  (c.f. <A HREF="../format.html#residual">format specification</A>)
 */
final class JFLAC__EntropyCodingMethod {

	int /*FLAC__EntropyCodingMethodType*/ type;
	//union {
		final JFLAC__EntropyCodingMethod_PartitionedRice partitioned_rice = new JFLAC__EntropyCodingMethod_PartitionedRice();
	//} data;
}
