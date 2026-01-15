/*******************************************************************************
 * Copyright (c) 2017-2026 Genialist Software Ltd.
 * All rights reserved.
 ******************************************************************************/

package tv.genialist.fwrk.document.file;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.ArrayList;

import tv.genialist.fwrk.document.pDocument;
import tv.genialist.fwrk.document.pURIDocument;
import tv.genialist.fwrk.document.util.pDocumentUtil;
import tv.genialist.fwrk.media.pMediaUtil;
import tv.genialist.ptools.string.pString;
import tv.genialist.ptools.util.pFilenameUtil;
import tv.genialist.ptools.util.pUseEncoding;

/**
 * The <code>pM3UFileDocumentType</code> class is an implementation of the {@link pFileDocumentType} interface 
 * for playlist files (.m3u files).
 * <p>
 * @author Genialist Software Ltd
 * @since 0.9.29
 * @version 0.9.29
 */
public class pM3UFileDocumentType extends pFileDocumentTypeImpl {

	/**************************************************************************/
	/***  DEFINITIONS  ********************************************************/
	/**************************************************************************/

	/**************************************************************************/
	/***  SUB-CLASSES  ********************************************************/
	/**************************************************************************/
	
	/**
	 * Base class for parsing play-list files.
	 * <p>
	 * @author Genialist Software Ltd
	 * @version 0.9.5
	 */
	private abstract static class pParser extends Object {
		
		/**************************************************************************/
		/***  DEFINITIONS  ********************************************************/
		/**************************************************************************/

		private final File p_file;
		private int i_index = 1;
		private final ArrayList<pDocument> i_lines = new ArrayList<>();
		
		public pParser(final File p_file) {
			super();
			this.p_file = p_file;
		}
		
		public File getFile() {
			return p_file;
		}
		
		protected void addEntry(final String i_line, final String i_title) {
			if (i_line!=null) {
				try {
					i_lines.add(new pURIDocument(pString.toString(i_index), new URI(i_line), null));
					i_index++;
				}
				catch (final URISyntaxException ex) {
					File i_file = new File(i_line);
					if (!i_file.isAbsolute())
						i_file = new File(p_file.getParent(), i_line);
					
					if (i_file.exists() && i_file.isDirectory()) {
						final File[] i_children = i_file.listFiles((FileFilter)pMediaFileDocumentType.getDefaultInstance());
						if (null!=i_children)
							for(File i_cfiles : i_children)
								if (!DEFAULT.accept(i_cfiles)) {
									i_lines.add(new pFileDocument(pString.toString(i_index), i_cfiles, null));
									i_index++;
								}
					}
					else {
						final pFileDocument i_doc = new pFileDocument(pString.toString(i_index), i_file, null);
						
						if (null!=i_title)
							i_doc.putValue(pMediaUtil.MNAME_TITLE, i_title);
						
						i_lines.add(i_doc);
						i_index++;
					}
				}
			}			
		}
		
		protected pDocument[] getEntries() {
			return i_lines.toArray(pDocumentUtil.DOCUMENTS_EMPTY_ARRAY);
		}
		
		public abstract pDocument[] parse() throws Exception;
	}
	
	/**
	 * <p>
	 * @author Genialist Software Ltd
	 * @version 0.9.5
	 */
	private static class pParser_M3U extends pParser {
		
		/**************************************************************************/
		/***  DEFINITIONS  ********************************************************/
		/**************************************************************************/

		public pParser_M3U(File p_file) {
			super(p_file);
		}

		@Override
		public pDocument[] parse() throws Exception {
			
			Charset i_encoding = pUseEncoding.CHARSET_CP1252;
			
			if (pFilenameUtil.extensionMatches(getFile(), ".m3u8"))
				i_encoding = pUseEncoding.CHARSET_UTF_8;
			
			try (FileInputStream i_is = new FileInputStream(getFile()); BufferedReader i_reader = new BufferedReader(new InputStreamReader(i_is, i_encoding))) {
			
				String i_line;
				for(int i=0 ; (i_line = i_reader.readLine())!=null ; i++) {
					
					// ignore UTF-8 BOM
					if (i==0 && i_line.length()>=3 && i_line.charAt(0)=='\uFEFF')
						i_line = i_line.substring(1);
					
					i_line = i_line.trim();
					if (i_line.length()<1) continue;
					if (i_line.charAt(0)=='#') continue;
					
					addEntry(i_line, null);
				}
			}

			return getEntries();			
		}
	}
	
	/**************************************************************************/
	/***  RUNTIME DATA  *******************************************************/
	/**************************************************************************/

	/** The default instance of this object. */
	private static final pM3UFileDocumentType DEFAULT = new pM3UFileDocumentType(); 
	
	/**************************************************************************/
	/***  CONSTRUCTORS  *******************************************************/
	/**************************************************************************/

	/**
	 * Constructs a new <code>pM3UFileDocumentType</code> object.
	 */
	public pM3UFileDocumentType() {
		super();
	}
	
	/**************************************************************************/
	/***  METHODS  ************************************************************/
	/**************************************************************************/
	
	/**
	 */
	public static pDocument[] parse(final File p_file) throws Exception {
		return new pParser_M3U(p_file).parse(); 
	}
	
	/**************************************************************************/
	/***  STATIC METHODS  *****************************************************/
	/**************************************************************************/
	
	/**
	 * Gets a default instance of this class.
	 * <p>
	 * @return The default instance (this cannot be <code>null</code>).
	 */
	public static synchronized pM3UFileDocumentType getDefaultInstance() {
    	return DEFAULT;
	}
}

/******************************************************************************/
/***  END OF FILE  ************************************************************/
/******************************************************************************/
