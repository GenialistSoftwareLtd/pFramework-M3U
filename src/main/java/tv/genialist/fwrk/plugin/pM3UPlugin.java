/*******************************************************************************
 * Copyright (c) 2017-2026 Genialist Software Ltd.
 * All rights reserved.
 ******************************************************************************/

package tv.genialist.fwrk.plugin;

import java.io.File;
import java.util.List;

import tv.genialist.fwrk.document.pDocument;
import tv.genialist.fwrk.document.pMediaDocument;
import tv.genialist.fwrk.document.file.pFileDocument;
import tv.genialist.fwrk.document.file.pFileDocumentTypeImpl;
import tv.genialist.fwrk.document.file.pMediaFileDocument;
import tv.genialist.fwrk.document.file.pM3UFileDocumentType;
import tv.genialist.fwrk.document.file.pPlaylistFileDocumentType;
import tv.genialist.fwrk.media.library.pMediaLibraryConstants;
import tv.genialist.fwrk.swing.util.service.pFileToPlaylist;
import tv.genialist.fwrk.swing.util.service.pPlaylistToText;
import tv.genialist.fwrk.swing.util.service.pFileToPlaylist.pFileToPlaylist_Request;
import tv.genialist.fwrk.swing.util.service.pPlaylistToText.pPlaylistToText_Request;
import tv.genialist.ptools.lang.pBaseStringBuilder;
import tv.genialist.ptools.lang.util.pBooleanUtil;
import tv.genialist.ptools.string.pString;
import tv.genialist.ptools.string.pStringProviders;
import tv.genialist.ptools.string.pStringProviders.pMainStringProvider;
import tv.genialist.ptools.trace.pTraceImpl;
import tv.genialist.ptools.util.pListUtil.pReadOnlyList;

/**
 * <p>
 * @author Genialist Software Ltd
 * @since 0.9.29
 * @version 0.9.29
 */
public class pM3UPlugin extends pBasePlugin {

	/** The prefix used in trace and log messages. */
	public static final String TRACE_PREFIX = "M3UPlugin";
	
	/**************************************************************************/
	/***  RUNTIME DATA  *******************************************************/
	/**************************************************************************/
	
	/** @since 0.9.29 */
	private static final pTraceImpl TRACE = pTraceImpl.getTrace(pM3UPlugin.class, TRACE_PREFIX);
	
	/** The default instance of this object (initialised by the method {@link #getDefaultInstance()}). */
	private static pM3UPlugin DEFAULT; 
	
	public static final String FORMAT = "M3U (MP3 URL)";
	
	/**************************************************************************/
	/***  CONSTRUCTORS  *******************************************************/
	/**************************************************************************/
	
	/**
	 * Constructs a new <code>pPDFPlugin</code> object.
	 */
	private pM3UPlugin() {
		super();
		
		addResource(new pFileToPlaylist.pAbstractChangeFileMetadata_ServiceProvider(this.getConfigHandler(), "m3u.sp.file.to.playlist", true) {
			
			@Override
			public boolean invoke(final pFileToPlaylist_Request p_request) {
				
				final File p_file = p_request.getSourceFile();
				final List<pMediaDocument> p_result = p_request.getResult();
				
				if (!pM3UFileDocumentType.getDefaultInstance().accept(p_file))
					return false;
				
				try {
					for(pDocument i_doc : pM3UFileDocumentType.parse(p_file))
						if (i_doc instanceof pFileDocument)
							p_result.add(new pMediaFileDocument(((pFileDocument)i_doc).getFile()));
					
					return p_result.size() > 0;
				}
				catch (final Exception ex) {
					if (TRACE.isErrorEnabled())
						TRACE.error("Failed to parse M3U file: ", p_file.getAbsolutePath(), ex);
				}
				return false;
			}
		});
		
		addResource(new pPlaylistToText.pAbstractChangeFileMetadata_ServiceProvider(this.getConfigHandler(), "m3u.sp.playlist.to.text", true) {
			
			@Override
			public boolean invoke(final pPlaylistToText_Request p_request) {
				
				if (!p_request.getFormat().equals(FORMAT) && !p_request.getFormat().equals("M3U"))
					return false;

				final pReadOnlyList<String> i_medias = p_request.getMediaURLs();
				final pBaseStringBuilder i_result = new pBaseStringBuilder(1024);
				
				for(int i=0 ; i < i_medias.size() ; i++) {
					i_result.append(i_medias.get(i));
					i_result.append("\r\n");
				}
				
				p_request.setResult(i_result.toString());
				p_request.setFileExtension((pString.hasNonASCIIChars(i_result))? ".m3u8" : pM3UFileDocumentType.getDefaultInstance().getFirstFileExtension().getExtension());
				
				return true;
			}
		});
		
		addResource(new pFileDocumentTypeImpl.pFileDocumentTypeInclusion(
			pM3UFileDocumentType.getDefaultInstance(), 
			pPlaylistFileDocumentType.getDefaultInstance())
		);
		
		//*** ADD PROVIDER TO ACCEPT MEDIA FILES INTO MEDIA LIBRARY
		addResource(new pStringProviders.pDefaultStringProvider<File>(pMediaLibraryConstants.MNAME_ACCEPT_FILE, File.class) {

			@Override
			public String toString(final pMainStringProvider<Object> p_parent, final File p_file) {
				return (pM3UFileDocumentType.getDefaultInstance().accept(p_file))? pBooleanUtil.STRING_TRUE : null;
			}
		});
		
		//*** ADD PLAYLIST FORMAT
		addResource(new pStringProviders.pDefaultStringProvider<Object>(pPlaylistFileDocumentType.STR_PROVIDER_PLAYLIST_FORMAT, Object.class) {

			@Override
			public String toString(final pMainStringProvider<Object> p_parent, final Object p_object) {
				return FORMAT;
			}
		});
	}

	/**************************************************************************/
	/***  METHODS  ************************************************************/
	/**************************************************************************/

	/**
	 * Stops this plug-in.
	 * This method must be thread-safe.
	 * In case of overwriting, the method of the parent class should be invoked as well in case of success.
	 */
	public void stop() {
		super.stop();
		
		pM3UFileDocumentType.getDefaultInstance().clearCache();
	}
	
	/**************************************************************************/
	/***  STATIC METHODS  *****************************************************/
	/**************************************************************************/
	
	/**
	 * Gets a default instance of this class.
	 * <p>
	 * @return The default instance (this cannot be <code>null</code>).
	 */
	public static synchronized pM3UPlugin getDefaultInstance() {
		if (null==DEFAULT)
			DEFAULT = new pM3UPlugin();
    	return DEFAULT;
	}
}

/******************************************************************************/
/***  END OF FILE  ************************************************************/
/******************************************************************************/	
