package libFLAC;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/* WATCHOUT: assembly routines rely on the order in which these fields are declared */
/* Things should be fastest when this matches the machine word size */
/* WATCHOUT: if you change this you must also change the following #defines down to FLAC__clz_uint32 below to match */
/* WATCHOUT: there are a few places where the code will not work unless uint32_t is >= 32 bits wide */
/*           also, some sections currently only have fast versions for 4 or 8 bytes per word */
final class JFLAC__BitReader {
	private static final int FLAC__BYTES_PER_WORD = 4;// java: always 4
	private static final int FLAC__BYTES_PER_WORD_LOG2 = 2;// java: always 2
	private static final int FLAC__BITS_PER_WORD = 32;// java: always 32
	private static final int FLAC__BITS_PER_WORD_LOG2 = 5;// java: always 5
	private static final int FLAC__WORD_ALL_ONES = (0xffffffff);
	/*
	 * This should be at least twice as large as the largest number of words
	 * required to represent any 'number' (in any encoding) you are going to
	 * read.  With FLAC this is on the order of maybe a few hundred bits.
	 * If the buffer is smaller than that, the decoder won't be able to read
	 * in a whole number that is in a variable length encoding (e.g. Rice).
	 * But to be practical it should be at least 1K bytes.
	 *
	 * Increase this number to decrease the number of read callbacks, at the
	 * expense of using more memory.  Or decrease for the reverse effect,
	 * keeping in mind the limit from the first paragraph.  The optimal size
	 * also depends on the CPU cache size and other factors; some twiddling
	 * may be necessary to squeeze out the best performance.
	 */
	private static final int FLAC__BITREADER_DEFAULT_CAPACITY = 65536 / FLAC__BITS_PER_WORD; /* in words */

	private static final byte byte_to_unary_table[] = {
		8, 7, 6, 6, 5, 5, 5, 5, 4, 4, 4, 4, 4, 4, 4, 4,
		3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3,
		2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
		2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
		1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
		1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
		1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
		1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
		0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
		0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
		0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
		0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
		0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
		0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
		0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
		0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0
	};
	/** any partially-consumed word at the head will stay right-justified as bits are consumed from the left<br>
	 * any incomplete word at the tail will be left-justified, and bytes from the read callback are added on the right */
	private int[] buffer = null;
	/** byte presentation of the buffer */
	private byte[] bytebuffer = null;// java: added to read
	/** in words */
	private int capacity = 0;
	/** # of completed words in buffer */
	private int words = 0;
	/** # of bytes in incomplete word at buffer[words] */
	private int bytes = 0;
	/** # words ... */
	private int consumed_words = 0;
	/** ... + (# bits of head word) already consumed from the front of buffer */
	private int consumed_bits = 0;
	/** the running frame CRC */
	private int read_crc16 = 0;
	/** the number of bits in the current consumed word that should not be CRC'd */
	private int crc16_align = 0;
	private JFLAC__BitReaderReadCallback read_callback = null;
	// private Object client_data = null;// java: don't need. uses read_callback

	// java: extracted in place
	/** counts the # of zero MSBs in a word */
	/* Will never be emitted for MSVC, GCC, Intel compilers */
	/*static inline unsigned int FLAC__clz_soft_uint32(unsigned int word)
	{
		static const unsigned char byte_to_unary_table[] = {
			8, 7, 6, 6, 5, 5, 5, 5, 4, 4, 4, 4, 4, 4, 4, 4,
			3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3,
			2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
			2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
			1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
			1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
			1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
			1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
			0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
	    };

		return word > 0xffffff ? byte_to_unary_table[word >> 24] :
			word > 0xffff ? byte_to_unary_table[word >> 16] + 8 :
				word > 0xff ? byte_to_unary_table[word >> 8] + 16 :
					byte_to_unary_table[word] + 24;
	}*/
	/* Used when 64-bit bsr/clz is unavailable; can use 32-bit bsr/clz when possible */
	/* static inline unsigned int FLAC__clz_soft_uint64(FLAC__uint64 word)
	{
		return (FLAC__uint32)(word>>32) ? FLAC__clz_uint32((FLAC__uint32)(word>>32)) :
			FLAC__clz_uint32((FLAC__uint32)word) + 32;
	} */

	/* static inline unsigned int FLAC__clz_uint64(FLAC__uint64 v)
	{
		// Never used with input 0
		FLAC__ASSERT(v > 0);
	#if defined(__GNUC__) && (__GNUC__ >= 4 || (__GNUC__ == 3 && __GNUC_MINOR__ >= 4))
		return __builtin_clzll(v);
	#elif (defined(__INTEL_COMPILER) || defined(_MSC_VER)) && (defined(_M_IA64) || defined(_M_X64))
		{
			unsigned long idx;
			_BitScanReverse64(&idx, v);
			return idx ^ 63U;
		}
	#else
		return FLAC__clz_soft_uint64(v);
	#endif
	} */

	/* These two functions work with input 0 */
	/* static inline unsigned int FLAC__clz2_uint32(FLAC__uint32 v)
	{
		if (!v)
			return 32;
		return FLAC__clz_uint32(v);
	} */

	/* static inline unsigned int FLAC__clz2_uint64(FLAC__uint64 v)
	{
		if (!v)
			return 64;
		return FLAC__clz_uint64(v);
	} */

	/***********************************************************************
	 *
	 * Class constructor/destructor
	 *
	 ***********************************************************************/

	// FLAC__bitreader_new(), FLAC__bitreader_delete(JFLAC__BitReader br)
	public JFLAC__BitReader() {
	}

	private final void crc16_update_word_(final int word)
	{
		int crc = this.read_crc16;
//if( FLAC__BYTES_PER_WORD == 4 ) {
		switch( this.crc16_align ) {
			case  0: crc = JFLAC_crc.FLAC__CRC16_UPDATE( (word >>> 24), crc);
			case  8: crc = JFLAC_crc.FLAC__CRC16_UPDATE( ((word >>> 16) & 0xff), crc);
			case 16: crc = JFLAC_crc.FLAC__CRC16_UPDATE( ((word >>> 8) & 0xff), crc);
			case 24: this.read_crc16 = JFLAC_crc.FLAC__CRC16_UPDATE( (word & 0xff), crc);
		}
/*} else if( FLAC__BYTES_PER_WORD == 8 ) {
		switch( this.crc16_align ) {
			case  0: crc = JFLAC_crc.FLAC__CRC16_UPDATE( (word >>> 56), crc);
			case  8: crc = JFLAC_crc.FLAC__CRC16_UPDATE( ((word >>> 48) & 0xff), crc);
			case 16: crc = JFLAC_crc.FLAC__CRC16_UPDATE( ((word >>> 40) & 0xff), crc);
			case 24: crc = JFLAC_crc.FLAC__CRC16_UPDATE( ((word >>> 32) & 0xff), crc);
			case 32: crc = JFLAC_crc.FLAC__CRC16_UPDATE( ((word >>> 24) & 0xff), crc);
			case 40: crc = JFLAC_crc.FLAC__CRC16_UPDATE( ((word >>> 16) & 0xff), crc);
			case 48: crc = JFLAC_crc.FLAC__CRC16_UPDATE( ((word >>> 8) & 0xff), crc);
			case 56: this.read_crc16 = JFLAC_crc.FLAC__CRC16_UPDATE( (word & 0xff), crc);
		}
} else {
		for( ; this.crc16_align < FLAC__BITS_PER_WORD; this.crc16_align += 8 )
			crc = JFLAC_crc.FLAC__CRC16_UPDATE( ((word >>> (FLAC__BITS_PER_WORD - 8 - this.crc16_align)) & 0xff), crc );
		this.read_crc16 = crc;
}*/
		this.crc16_align = 0;
	}

	private final void bitreader_read_from_client_() throws IOException// java: changed, throws IOException instead boolean
	{
		/* first shift the unconsumed buffer data toward the front as much as possible */
		if( this.consumed_words > 0 ) {
			final int start = this.consumed_words;
			final int end = this.words + (this.bytes != 0 ? 1 : 0);
			System.arraycopy( this.buffer, start, this.buffer, 0, /*FLAC__BYTES_PER_WORD * */(end - start) );

			this.words -= start;
			this.consumed_words = 0;
		}

		/*
		 * set the target for reading, taking into account word alignment and endianness
		 */
		int read = ((this.capacity - this.words) << FLAC__BYTES_PER_WORD_LOG2) - this.bytes;
		if( read == 0 ) {
			throw new IOException(); /* no space left, buffer is too small; see note for FLAC__BITREADER_DEFAULT_CAPACITY  */
		}

		/* read in the data; note that the callback may return a smaller number of bytes */
		if( 0 > (read = this.read_callback.bit_read_callback( this.bytebuffer, read/*, this.client_data*/ )) ) {
			throw new IOException();
		}

		int end = read;
		if( this.bytes != 0 ) {// writing bytes in last partial word as BIG ENDIAN
			int start = FLAC__BYTES_PER_WORD - this.bytes;// temp
			int val = this.buffer[this.words];
			val &= FLAC__WORD_ALL_ONES << (start << 3);
			for( int i = --start; i >= 0 && end > 0; i--, end-- ) {
				val |= (((int)this.bytebuffer[i]) & 0xff) << ((start - i) << 3);
			}
			this.buffer[this.words] = val;

		}
		int offset = this.words;
		if( end > 0 ) {
			final int start = read - end;
			if( start > 0 ) {
				offset++;
			}
			if( 0 != (end & ((1 << FLAC__BYTES_PER_WORD_LOG2) - 1)) ) {
				end += FLAC__BYTES_PER_WORD;
			}
			end >>>= FLAC__BYTES_PER_WORD_LOG2;// TODO try to optimize wrap length parameter
			ByteBuffer.wrap( this.bytebuffer, start, this.bytes + (end << FLAC__BYTES_PER_WORD_LOG2) ).order( ByteOrder.BIG_ENDIAN ).asIntBuffer().get( this.buffer, offset, end );
		}

		end = (this.words << FLAC__BYTES_PER_WORD_LOG2) + this.bytes + read;
		this.words = (end >>> FLAC__BYTES_PER_WORD_LOG2);
		this.bytes = end & (FLAC__BYTES_PER_WORD - 1);
	}

	/***********************************************************************
	 *
	 * Public class methods
	 *
	 ***********************************************************************/

	final boolean FLAC__bitreader_init(final JFLAC__BitReaderReadCallback rcb/*, final Object cd*/)
	{
		this.words = this.bytes = 0;
		this.consumed_words = this.consumed_bits = 0;
		this.capacity = FLAC__BITREADER_DEFAULT_CAPACITY;
		this.buffer = new int[this.capacity];
		this.bytebuffer = new byte[this.capacity << 2];// int to byte
		this.read_callback = rcb;
		// this.client_data = cd;

		return true;
	}

	/*static void FLAC__bitreader_free(JFLAC__BitReader br)
	{
		br.buffer = null;
		br.bytebuffer = null;
		br.capacity = 0;
		br.words = br.bytes = 0;
		br.consumed_words = br.consumed_bits = 0;
		br.read_callback = null;
		br.client_data = null;
	}*/

	final boolean FLAC__bitreader_clear()
	{
		this.words = this.bytes = 0;
		this.consumed_words = this.consumed_bits = 0;
		return true;
	}

	/*
	@SuppressWarnings("boxing")
	private void FLAC__bitreader_dump(OutputStream out)
	{
		int i, j;
		System.out.printf("bitreader: capacity=%d words=%d bytes=%d consumed: words=%d, bits=%d\n", capacity, words, bytes, consumed_words, consumed_bits);

		for( i = 0; i < words; i++ ) {
			System.out.printf("%08X: ", i );
			for( j = 0; j < FLAC__BITS_PER_WORD; j++ )
				if( i < consumed_words || (i == consumed_words && j < consumed_bits) )
					System.out.print(".");
				else
					System.out.printf("%01d", (buffer[i] & (1 << (FLAC__BITS_PER_WORD - j - 1))) != 0 ? 1 : 0);
			System.out.print("\n");
		}
		if( bytes > 0 ) {
			System.out.printf("%08X: ", i);
			for(j = 0; j < bytes * 8; j++)
				if( i < consumed_words || (i == consumed_words && j < consumed_bits) )
					System.out.print(".");
				else
					System.out.printf("%01d", (buffer[i] & (1 << (bytes * 8 - j - 1))) != 0 ? 1 : 0);
			System.out.print("\n");
		}
	}
	*/

	final void FLAC__bitreader_reset_read_crc16(final int seed)// java: short seed
	{
		//FLAC__ASSERT( (br.consumed_bits & 7) == 0 );

		this.read_crc16 = seed;// (int)seed;
		this.crc16_align = this.consumed_bits;
	}

	final int FLAC__bitreader_get_read_crc16()
	{
		/* CRC any tail bytes in a partially-consumed word */
		final int bits = this.consumed_bits;// java
		if( bits != 0 ) {
			final int tail = this.buffer[this.consumed_words];
			int align = this.crc16_align;// java
			int crc16 = this.read_crc16;// java
			for( ; align < bits; align += 8 ) {
				crc16 = JFLAC_crc.FLAC__CRC16_UPDATE( ((tail >> (FLAC__BITS_PER_WORD - 8 - align)) & 0xff), crc16 );
			}
			this.read_crc16 = crc16;
			this.crc16_align = align;
		}
		return this.read_crc16;
	}

	final boolean FLAC__bitreader_is_consumed_byte_aligned()
	{
		return ((this.consumed_bits & 7) == 0);
	}

	final int FLAC__bitreader_bits_left_for_byte_alignment()
	{
		return 8 - (this.consumed_bits & 7);
	}

	final int FLAC__bitreader_get_input_bits_unconsumed()
	{
		return ((this.words - this.consumed_words) << FLAC__BITS_PER_WORD_LOG2) + (this.bytes << 3) - this.consumed_bits;
	}

	final int FLAC__bitreader_read_raw_uint32(int bits) throws IOException// java: changed. val is returned. if an error, throws exception
	{
		/* WATCHOUT: code does not work with <32bit words; we can make things much faster with this assertion */

		if( bits == 0 ) { /* OPT: investigate if this can ever happen, maybe change to assertion */
			return 0;
		}

		while( ((this.words - this.consumed_words) << FLAC__BITS_PER_WORD_LOG2) + (this.bytes << 3) - this.consumed_bits < bits ) {
			bitreader_read_from_client_();
		}
		if( this.consumed_words < this.words ) { /* if we've not consumed up to a partial tail word... */
			/* OPT: taking out the consumed_bits==0 "else" case below might make things faster if less code allows the compiler to inline this function */
			if( this.consumed_bits != 0 ) {
				/* this also works when consumed_bits==0, it's just a little slower than necessary for that case */
				final int n = FLAC__BITS_PER_WORD - this.consumed_bits;
				final int word = this.buffer[this.consumed_words];
				if( bits < n ) {
					final int val = (word & (FLAC__WORD_ALL_ONES >>> this.consumed_bits)) >>> (n - bits);
					this.consumed_bits += bits;
					return val;
				}
				/* (FLAC__BITS_PER_WORD - br->consumed_bits <= bits) ==> (FLAC__WORD_ALL_ONES >> br->consumed_bits) has no more than 'bits' non-zero bits */
				int val = word & (FLAC__WORD_ALL_ONES >>> this.consumed_bits);
				bits -= n;
				crc16_update_word_( word );
				this.consumed_words++;
				this.consumed_bits = 0;
				if( bits != 0 ) { /* if there are still bits left to read, there have to be less than 32 so they will all be in the next word */
					val <<= bits;
					val |= (this.buffer[this.consumed_words] >>> (FLAC__BITS_PER_WORD - bits));
					this.consumed_bits = bits;
				}
				return val;
			}
			//else {
				final int word = this.buffer[this.consumed_words];
				if( bits < FLAC__BITS_PER_WORD ) {
					final int val = word >>> (FLAC__BITS_PER_WORD - bits);
					this.consumed_bits = bits;
					return val;
				}
				/* at this point bits == FLAC__BITS_PER_WORD == 32; because of previous assertions, it can't be larger */
				final int val = word;
				crc16_update_word_( word );
				this.consumed_words++;
				return val;
			//}
		}
		//else {
			/* in this case we're starting our read at a partial tail word;
			 * the reader has guaranteed that we have at least 'bits' bits
			 * available to read, which makes this case simpler.
			 */
			/* OPT: taking out the consumed_bits==0 "else" case below might make things faster if less code allows the compiler to inline this function */
			if( this.consumed_bits != 0 ) {
				/* this also works when consumed_bits==0, it's just a little slower than necessary for that case */
				final int val = (this.buffer[this.consumed_words] & (FLAC__WORD_ALL_ONES >>> this.consumed_bits)) >>> (FLAC__BITS_PER_WORD - this.consumed_bits - bits);
				this.consumed_bits += bits;
				return val;
			}
			//else {
				final int val = this.buffer[this.consumed_words] >>> (FLAC__BITS_PER_WORD - bits);
				this.consumed_bits += bits;
				return val;
			//}
		//}
	}

	final int FLAC__bitreader_read_raw_int32(final int bits) throws IOException// java: changed. val is returned. if an error, throws exception
	{
		/* OPT: inline raw uint32 code here, or make into a macro if possible in the .h file */
		final int uval = FLAC__bitreader_read_raw_uint32( bits );
		/* sign-extend *val assuming it is currently bits wide. */
		/* From: https://graphics.stanford.edu/~seander/bithacks.html#FixedSignExtend */
		final int mask = 1 << (bits - 1);
		return (uval ^ mask) - mask;
	}

	final long FLAC__bitreader_read_raw_uint64(final int bits) throws IOException// java: changed. val is returned. if an error, throws exception
	{
		if( bits > 32 ) {
			long val = ((long)FLAC__bitreader_read_raw_uint32( bits - 32 )) << 32;
			val |= ((long)FLAC__bitreader_read_raw_uint32( 32 )) & 0xffffffffL;
			return val;
		}
		//else {
			final long val = ((long)FLAC__bitreader_read_raw_uint32( bits )) & 0xffffffffL;
		//}
		return val;
	}

	final int FLAC__bitreader_read_uint32_little_endian() throws IOException// java: changed. val is returned. if an error, throws exception
	{
		/* this doesn't need to be that fast as currently it is only used for vorbis comments */

		int x32 = FLAC__bitreader_read_raw_uint32( 8 );

		int x8 = FLAC__bitreader_read_raw_uint32( 8 );
		x32 |= (x8 << 8);

		x8 = FLAC__bitreader_read_raw_uint32( 8 );
		x32 |= (x8 << 16);

		x8 = FLAC__bitreader_read_raw_uint32( 8 );
		x32 |= (x8 << 24);

		return x32;
	}

	final void FLAC__bitreader_skip_bits_no_crc(int bits) throws IOException// java: changed. if an error, throws exception
	{
		/*
		 * OPT: a faster implementation is possible but probably not that useful
		 * since this is only called a couple of times in the metadata readers.
		 */

		if( bits > 0 ) {
			final int n = this.consumed_bits & 7;
			//int x;

			if( n != 0 ) {
				int m = 8 - n;
				if( m > bits ) {
					m = bits;
				}
				/*x = */FLAC__bitreader_read_raw_uint32( m );
				bits -= m;
			}
			final int m = bits >>> 3;
			if( m > 0 ) {
				FLAC__bitreader_skip_byte_block_aligned_no_crc( m );
				bits &= 7;
			}
			if( bits > 0 ) {
				/*x = */FLAC__bitreader_read_raw_uint32( bits );
			}
		}
	}

	final void FLAC__bitreader_skip_byte_block_aligned_no_crc(int nvals) throws IOException// java: changed. if an error, throws exception
	{
		//int x;

		/* step 1: skip over partial head word to get word aligned */
		while( nvals != 0 && this.consumed_bits != 0 ) { /* i.e. run until we read 'nvals' bytes or we hit the end of the head word */
			/*x = */FLAC__bitreader_read_raw_uint32( 8 );
			nvals--;
		}
		if( 0 == nvals )
		 {
			return;// true;
		}
		/* step 2: skip whole words in chunks */
		while( nvals >= FLAC__BYTES_PER_WORD ) {
			if( this.consumed_words < this.words ) {
				this.consumed_words++;
				nvals -= FLAC__BYTES_PER_WORD;
			} else {
				bitreader_read_from_client_();
			}
		}
		/* step 3: skip any remainder from partial tail bytes */
		while( nvals != 0 ) {
			/*x = */FLAC__bitreader_read_raw_uint32( 8 );
			nvals--;
		}
	}

	final void /*boolean*/ FLAC__bitreader_read_byte_block_aligned_no_crc(final byte[] val, int nvals) throws IOException// java: changed. if an error, throws exception
	{
		int offset = 0;

		/* step 1: read from partial head word to get word aligned */
		while( nvals != 0 && this.consumed_bits != 0 ) { /* i.e. run until we read 'nvals' bytes or we hit the end of the head word */
			final int x = FLAC__bitreader_read_raw_uint32( 8 );
			val[offset++] = (byte)x;
			nvals--;
		}
		if( 0 == nvals ) {
			return;
		}
		/* step 2: read whole words in chunks */
		while( nvals >= FLAC__BYTES_PER_WORD ) {
			if( this.consumed_words < this.words ) {
				final int word = this.buffer[this.consumed_words++];
//if( FLAC__BYTES_PER_WORD == 4 ) {
				val[offset++] = (byte)(word >>> 24);
				val[offset++] = (byte)(word >>> 16);
				val[offset++] = (byte)(word >>> 8);
				val[offset++] = (byte)word;
/*} else if( FLAC__BYTES_PER_WORD == 8 ) {
				val[offset + 0] = (byte)(word >>> 56);
				val[offset + 1] = (byte)(word >>> 48);
				val[offset + 2] = (byte)(word >>> 40);
				val[offset + 3] = (byte)(word >>> 32);
				val[offset + 4] = (byte)(word >>> 24);
				val[offset + 5] = (byte)(word >>> 16);
				val[offset + 6] = (byte)(word >>> 8);
				val[offset + 7] = (byte)word;
} else {
				for( int i = 0; i < FLAC__BYTES_PER_WORD; i++ )
					val[offset + i] = (byte)(word >>> ((FLAC__BYTES_PER_WORD - i - 1) << 3));
}*/
				// offset += FLAC__BYTES_PER_WORD;
				nvals -= FLAC__BYTES_PER_WORD;
			} else {
				bitreader_read_from_client_();
			}
		}
		/* step 3: read any remainder from partial tail bytes */
		while( nvals != 0 ) {
			final int x = FLAC__bitreader_read_raw_uint32( 8 );
			val[offset++] = (byte)x;
			nvals--;
		}
	}

	final int FLAC__bitreader_read_unary_unsigned() throws IOException {// java: changed. val is returned. if an error, throws exception

//if( false ) /* slow but readable version */
/*	{
		int bit;

		val = 0;
		while( true ) {
			if( ! FLAC__bitreader_read_bit( &bit ) )
				return false;
			if( bit != 0 )
				break;
			else
				*val++;
		}
		return true;
	}*/
//else
	{
		int val = 0;
		while( true ) {
			while( this.consumed_words < this.words ) { /* if we've not consumed up to a partial tail word... */
				final int b = this.buffer[this.consumed_words] << this.consumed_bits;
				if( b != 0 ) {
					//i = COUNT_ZERO_MSBS( b );
					int i = ( (b & 0xff000000) != 0 ? byte_to_unary_table[b >>> 24] :
						(b & 0xffff0000) != 0 ? byte_to_unary_table[b >>> 16] + 8 :
						(b & 0xffffff00) != 0 ? byte_to_unary_table[b >>> 8] + 16 :
						byte_to_unary_table[b] + 24 );
					val += i;
					i++;
					this.consumed_bits += i;
					if( this.consumed_bits >= FLAC__BITS_PER_WORD ) { /* faster way of testing if(br->consumed_bits == FLAC__BITS_PER_WORD) */
						crc16_update_word_( this.buffer[this.consumed_words] );
						this.consumed_words++;
						this.consumed_bits = 0;
					}
					return val;
				}
				else {
					val += FLAC__BITS_PER_WORD - this.consumed_bits;
					crc16_update_word_( this.buffer[this.consumed_words] );
					this.consumed_words++;
					this.consumed_bits = 0;
					/* didn't find stop bit yet, have to keep going... */
				}
			}
			/* at this point we've eaten up all the whole words; have to try
			 * reading through any tail bytes before calling the read callback.
			 * this is a repeat of the above logic adjusted for the fact we
			 * don't have a whole word.  note though if the client is feeding
			 * us data a byte at a time (unlikely), br->consumed_bits may not
			 * be zero.
			 */
			if( (this.bytes << 3) > this.consumed_bits ) {
				final int end = this.bytes << 3;
				final int b = (this.buffer[this.consumed_words] & (FLAC__WORD_ALL_ONES << (FLAC__BITS_PER_WORD - end))) << this.consumed_bits;
				if( b != 0 ) {
					//i = COUNT_ZERO_MSBS( b );
					int i = ( (b & 0xff000000) != 0 ? byte_to_unary_table[b >>> 24] :
						(b & 0xffff0000) != 0 ? byte_to_unary_table[b >>> 16] + 8 :
						(b & 0xffffff00) != 0 ? byte_to_unary_table[b >>> 8] + 16 :
						byte_to_unary_table[b] + 24 );
					val += i;
					i++;
					this.consumed_bits += i;
					//FLAC__ASSERT(br.consumed_bits < FLAC__BITS_PER_WORD);
					return val;
				}
				else {
					val += end - this.consumed_bits;
					this.consumed_bits += end;
					//FLAC__ASSERT(br.consumed_bits < FLAC__BITS_PER_WORD);
					/* didn't find stop bit yet, have to keep going... */
				}
			}
			bitreader_read_from_client_();
		}
}
	}

	/* FIXME never used FLAC__bitreader_read_rice_signed
	private final int FLAC__bitreader_read_rice_signed(int parameter) throws IOException // java: changed. val is returned. if an error, throws exception
	{
		int lsbs = 0, msbs = 0;
		int uval;

		//FLAC__ASSERT(0 != br);
		//FLAC__ASSERT(0 != br->buffer);
		//FLAC__ASSERT(parameter <= 31);

		// read the unary MSBs and end bit
		msbs = FLAC__bitreader_read_unary_unsigned();

		// read the binary LSBs
		lsbs = FLAC__bitreader_read_raw_uint32( parameter );

		// compose the value
		uval = (msbs << parameter) | lsbs;
		if( (uval & 1) != 0 )
			return -(uval >>> 1) - 1;
		//else
			return uval >>> 1;

	}
	*/

	/* this is by far the most heavily used reader call.  it ain't pretty but it's fast */
	final void FLAC__bitreader_read_rice_signed_block(final int vals[], int offset, final int nvals, final int parameter) throws IOException// java: changed. if an error, throws exception
	{
		/* try and get br->consumed_words and br->consumed_bits into register;
		 * must remember to flush them back to *br before calling other
		 * bitreader functions that use them, and before returning */

		//FLAC__ASSERT(0 != br);
		//FLAC__ASSERT(0 != br->buffer);
		/* WATCHOUT: code does not work with <32bit words; we can make things much faster with this assertion */
		//FLAC__ASSERT(FLAC__BITS_PER_WORD >= 32);
		//FLAC__ASSERT(parameter < 32);
		/* the above two asserts also guarantee that the binary part never straddles more than 2 words, so we don't have to loop to read it */

		//val = offset;// java: val changed to offset
		final int end = offset + nvals;

		if( parameter == 0 ) {
			while( offset < end ) {
				/* read the unary MSBs and end bit */
				final int msbs = FLAC__bitreader_read_unary_unsigned();

				vals[offset++] = (msbs >>> 1) ^ -(msbs & 1);
			}

			return;
		}

		//FLAC__ASSERT(parameter > 0);

		int cwords = this.consumed_words;
		int rwords = this.words;

		int b;
		int ucbits; /* keep track of the number of unconsumed bits in word */
		/* if we've not consumed up to a partial tail word... */
		if( cwords >= rwords ) {
			// java: x = 0, so don't using
			//goto process_tail;
			/* at this point we've eaten up all the whole words */
//process_tail:
			do {
				/* read the unary MSBs and end bit */
				final int msbs = FLAC__bitreader_read_unary_unsigned();

				/* read the binary LSBs */
				final int lsbs = FLAC__bitreader_read_raw_uint32( parameter );

				/* compose the value */
				final int x = (msbs << parameter) | lsbs;
				vals[offset++] = (x >>> 1) ^ -(x & 1);

				cwords = this.consumed_words;
				rwords = this.words;
				ucbits = FLAC__BITS_PER_WORD - this.consumed_bits;
				b = this.buffer[cwords] << this.consumed_bits;
			} while( cwords >= rwords && offset < end );
		} else {
			ucbits = FLAC__BITS_PER_WORD - this.consumed_bits;
			b = this.buffer[cwords] << this.consumed_bits;  /* keep unconsumed bits aligned to left */
		}

main_loop:
		while( offset < end ) {
			/* read the unary MSBs and end bit */
			//x = y = COUNT_ZERO_MSBS( b );
			int x, y;
			x = y = ( b == 0 ? 32 :
				(b & 0xff000000) != 0 ? byte_to_unary_table[b >>> 24] :
				(b & 0xffff0000) != 0 ? byte_to_unary_table[b >>> 16] + 8 :
				(b & 0xffffff00) != 0 ? byte_to_unary_table[b >>> 8] + 16 :
				byte_to_unary_table[b] + 24 );
			if( x == FLAC__BITS_PER_WORD ) {
				x = ucbits;
				do {
					/* didn't find stop bit yet, have to keep going... */
					crc16_update_word_( this.buffer[cwords++] );
					if( cwords >= rwords ) {
						/* at this point we've eaten up all the whole words */
//process_tail:
						//goto incomplete_msbs;
						this.consumed_bits = 0;
						this.consumed_words = cwords;
						do {
							/* read the unary MSBs and end bit */
							int msbs = FLAC__bitreader_read_unary_unsigned();

							msbs += x;

							/* read the binary LSBs */
							final int lsbs = FLAC__bitreader_read_raw_uint32( parameter );

							/* compose the value */
							x = (msbs << parameter) | lsbs;
							vals[offset++] = (x >>> 1) ^ -(x & 1);
							x = 0;

							cwords = this.consumed_words;
							rwords = this.words;
							ucbits = FLAC__BITS_PER_WORD - this.consumed_bits;
							b = this.buffer[cwords] << this.consumed_bits;
						} while( cwords >= rwords && offset < end );
						continue main_loop;
					}
					b = this.buffer[cwords];
					//y = COUNT_ZERO_MSBS( b );
					y = ( b == 0 ? 32 :
						(b & 0xff000000) != 0 ? byte_to_unary_table[b >>> 24] :
						(b & 0xffff0000) != 0 ? byte_to_unary_table[b >>> 16] + 8 :
						(b & 0xffffff00) != 0 ? byte_to_unary_table[b >>> 8] + 16 :
						byte_to_unary_table[b] + 24 );
					x += y;
				} while( y == FLAC__BITS_PER_WORD );
			}
			b <<= y;
			b <<= 1; /* account for stop bit */
			ucbits = (ucbits - x - 1) & (FLAC__BITS_PER_WORD - 1);
			int msbs = x;

			/* read the binary LSBs */
			x = b >>> (FLAC__BITS_PER_WORD - parameter);
			if( parameter <= ucbits ) {
				ucbits -= parameter;
				b <<= parameter;
			} else {
				/* there are still bits left to read, they will all be in the next word */
				crc16_update_word_( this.buffer[cwords++] );
				if( cwords >= rwords ) {
					//goto incomplete_lsbs;
					this.consumed_bits = 0;
					this.consumed_words = cwords;

					/* read the binary LSBs */
					int lsbs = x | FLAC__bitreader_read_raw_uint32( parameter - ucbits );

					/* compose the value */
					x = (msbs << parameter) | lsbs;
					vals[offset++] = (x >>> 1) ^ -(x & 1);
					// x = 0;

					cwords = this.consumed_words;
					rwords = this.words;
					ucbits = FLAC__BITS_PER_WORD - this.consumed_bits;
					b = this.buffer[cwords] << this.consumed_bits;
					if( cwords < rwords || offset >= end ) {
						continue main_loop;
					}
					/* at this point we've eaten up all the whole words */
//process_tail:
					do {
						/* read the unary MSBs and end bit */
						msbs = FLAC__bitreader_read_unary_unsigned();

						// msbs += x;
						// x = ucbits = 0;// FIXME why?

						/* read the binary LSBs */
						// lsbs = x | FLAC__bitreader_read_raw_uint32( parameter - ucbits );
						lsbs = FLAC__bitreader_read_raw_uint32( parameter );

						/* compose the value */
						x = (msbs << parameter) | lsbs;
						vals[offset++] = (x >>> 1) ^ -(x & 1);
						// x = 0;

						cwords = this.consumed_words;
						rwords = this.words;
						ucbits = FLAC__BITS_PER_WORD - this.consumed_bits;
						b = this.buffer[cwords] << this.consumed_bits;
					} while( cwords >= rwords && offset < end );
					continue main_loop;
				}
				b = this.buffer[cwords];
				ucbits += FLAC__BITS_PER_WORD - parameter;
				x |= b >>> ucbits;
				b <<= FLAC__BITS_PER_WORD - ucbits;
			}
			// final int lsbs = x;

			/* compose the value */
			x = (msbs << parameter) | x;
			vals[offset++] = (x >>> 1) ^ -(x & 1);
		}

		if( ucbits == 0 && cwords < rwords ) {
			/* don't leave the head word with no unconsumed bits */
			crc16_update_word_( this.buffer[cwords++] );
			ucbits = FLAC__BITS_PER_WORD;
		}

		this.consumed_bits = FLAC__BITS_PER_WORD - ucbits;
		this.consumed_words = cwords;
	}

	/** on return, if *val == 0xffffffff then the utf-8 sequence was invalid, but the return value will be true */
	final int FLAC__bitreader_read_utf8_uint32(final Jraw_header_helper header) throws IOException// java: changed. val is returned. if an error, throws exception
	{
		final byte[] raw = header.raw_header;
		int rawlen = header.raw_header_len;

		int x = FLAC__bitreader_read_raw_uint32( 8 );
		if( raw != null ) {
			raw[ rawlen++ ] = (byte)x;
		}
		int v = 0;
		int i;
		if( (x & 0x80) == 0 ) { /* 0xxxxxxx */
			v = x;
			i = 0;
		}
		else if( (x & 0xC0) != 0 && (x & 0x20) == 0 ) { /* 110xxxxx */
			v = x & 0x1F;
			i = 1;
		}
		else if( (x & 0xE0) != 0 && (x & 0x10) == 0 ) { /* 1110xxxx */
			v = x & 0x0F;
			i = 2;
		}
		else if( (x & 0xF0) != 0 && (x & 0x08) == 0 ) { /* 11110xxx */
			v = x & 0x07;
			i = 3;
		}
		else if( (x & 0xF8) != 0 && (x & 0x04) == 0 ) { /* 111110xx */
			v = x & 0x03;
			i = 4;
		}
		else if( (x & 0xFC) != 0 && (x & 0x02) == 0 ) { /* 1111110x */
			v = x & 0x01;
			i = 5;
		}
		else {
			header.raw_header_len = rawlen;
			return 0xffffffff;
		}
		for( ; i > 0; i-- ) {
			x = FLAC__bitreader_read_raw_uint32( 8 );
			if( raw != null ) {
				raw[ rawlen++ ] = (byte)x;
			}
			if( 0 == (x & 0x80) || (x & 0x40) != 0 ) { /* 10xxxxxx */
				header.raw_header_len = rawlen;
				return 0xffffffff;
			}
			v <<= 6;
			v |= (x & 0x3F);
		}
		header.raw_header_len = rawlen;
		return v;
	}

	/** on return, if *val == 0xffffffffffffffff then the utf-8 sequence was invalid, but the return value will be true */
	final long FLAC__bitreader_read_utf8_uint64(final Jraw_header_helper header) throws IOException// java: changed. val is returned. if an error, throws exception
	{
		final byte[] raw = header.raw_header;
		int rawlen = header.raw_header_len;

		int x = FLAC__bitreader_read_raw_uint32( 8 );
		if( raw != null ) {
			raw[ rawlen++ ] = (byte)x;
		}
		long v = 0;
		int i;
		if( 0 == (x & 0x80) ) { /* 0xxxxxxx */
			v = x;
			i = 0;
		}
		else if( (x & 0xC0) != 0 && 0 == (x & 0x20) ) { /* 110xxxxx */
			v = x & 0x1F;
			i = 1;
		}
		else if( (x & 0xE0) != 0 && 0 == (x & 0x10) ) { /* 1110xxxx */
			v = x & 0x0F;
			i = 2;
		}
		else if( (x & 0xF0) != 0 && 0 == (x & 0x08) ) { /* 11110xxx */
			v = x & 0x07;
			i = 3;
		}
		else if( (x & 0xF8) != 0 && 0 == (x & 0x04) ) { /* 111110xx */
			v = x & 0x03;
			i = 4;
		}
		else if( (x & 0xFC) != 0 && 0 == (x & 0x02) ) { /* 1111110x */
			v = x & 0x01;
			i = 5;
		}
		else if( (x & 0xFE) != 0 && 0 == (x & 0x01) ) { /* 11111110 */
			v = 0;
			i = 6;
		}
		else {
			header.raw_header_len = rawlen;
			return (0xffffffffffffffffL);
		}
		for( ; i > 0; i-- ) {
			x = FLAC__bitreader_read_raw_uint32( 8 );
			if( raw != null ) {
				raw[ rawlen++ ] = (byte)x;
			}
			if( 0 == (x & 0x80) || (x & 0x40) != 0 ) { /* 10xxxxxx */
				header.raw_header_len = rawlen;
				return (0xffffffffffffffffL);
			}
			v <<= 6;
			v |= (x & 0x3F);
		}
		header.raw_header_len = rawlen;
		return v;
	}
}
