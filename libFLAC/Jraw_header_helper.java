package libFLAC;

/**
 * Helper class to read utf-8 header
 */
final class Jraw_header_helper {
	final byte[] raw_header = new byte[16]; /* MAGIC NUMBER based on the maximum frame header size, including CRC */
	int raw_header_len = 0;
}
