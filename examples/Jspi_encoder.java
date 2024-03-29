package examples;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import libFLAC.RandomAccessInputOutputStream;
import spi.file.EncoderFileFormatType;

/**
 * takes a WAV file from an input file and encodes it into
 * a FLAC output file
 */
public class Jspi_encoder {
	public static void main(final String[] args) {
		if( args.length != 2 ) {
			System.out.println("Usage:");
			System.out.println("java -jar Jspi_encoder.jar <Input File[.wav]> <Output File[.flac]>");
			System.exit( 0 );
			return;
		}

		AudioInputStream ins = null;
		RandomAccessInputOutputStream outs = null;
		try {
			ins = AudioSystem.getAudioInputStream( new BufferedInputStream( new FileInputStream( args[0] ) ) );
			if( ! AudioSystem.isConversionSupported( new AudioFormat.Encoding("FLAC"), ins.getFormat() ) ) {
				System.err.println("sorry, conversation to FLAC not supported");
				System.exit( 1 );
				return;
			}
			System.out.println("Start encoding " + args[0]);
			outs = new RandomAccessInputOutputStream( args[1] );
			// supports: compression level from 0 to 8
			AudioSystem.write( ins, new EncoderFileFormatType("FLAC", "flac", EncoderFileFormatType.UNKNOWN, 8f ), outs );
			System.out.println("Encoding complete");
		} catch(final Exception e) {
			System.err.println( e.getMessage() );
			e.printStackTrace();
		} finally {
			if( ins != null ) {
				try { ins.close(); } catch( final IOException e ) {}
			}
			if( outs != null ) {
				try { outs.close(); } catch( final IOException e ) {}
			}
		}
	}
}
