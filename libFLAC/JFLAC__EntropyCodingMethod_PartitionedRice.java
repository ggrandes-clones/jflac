package libFLAC;

/** Header for a Rice partitioned residual.  (c.f. <A HREF="../format.html#partitioned_rice">format specification</A>)
 */
final class JFLAC__EntropyCodingMethod_PartitionedRice {
	/** The partition order, i.e. # of contexts = 2 ^ \a order. */
	int order = 0;

	/** The context's Rice parameters and/or raw bits. */
	JFLAC__EntropyCodingMethod_PartitionedRiceContents contents = null;

	/*public JFLAC__EntropyCodingMethod_PartitionedRice(JFLAC__EntropyCodingMethod_PartitionedRiceContents c) {
		contents = c;
	}*/
}

