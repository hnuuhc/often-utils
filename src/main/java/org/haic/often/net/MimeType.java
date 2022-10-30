package org.haic.often.net;

import org.haic.often.Symbol;

/**
 * 常用网络MIME类型
 *
 * @author haicdust
 * @version 1.0
 * @since 2022/7/14 10:12
 */
public enum MimeType {

	/**
	 * CoolTalk
	 */
	x_conference_x_cooltalk(".ice"),

	/**
	 * SGI Movie
	 */
	video_x_sgi_movie(".movie"),

	/**
	 * Audio Video Interleave (AVI)
	 */
	video_x_msvideo(".avi"),

	/**
	 * Microsoft Windows Media Video Playlist
	 */
	video_x_ms_wvx(".wvx"),

	/**
	 * Microsoft Windows Media Audio/Video Playlist
	 */
	video_x_ms_wmx(".wmx"),

	/**
	 * Microsoft Windows Media Video
	 */
	video_x_ms_wmv(".wmv"),

	/**
	 * Microsoft Windows Media
	 */
	video_x_ms_wm(".wm"),

	/**
	 * Microsoft Advanced Systems Format (ASF)
	 */
	video_x_ms_asf(".asf"),

	/**
	 * M4v
	 */
	video_x_m4v(".m4v"),

	/**
	 * Flash Video
	 */
	video_x_flv(".flv"),

	/**
	 * FLI/FLC Animation Format
	 */
	video_x_fli(".fli"),

	/**
	 * Flash Video
	 */
	video_x_f4v(".f4v"),

	/**
	 * Open Web Media Project - Video
	 */
	video_webm(".webm"),

	/**
	 * Vivo
	 */
	video_vnd_vivo(".viv"),

	/**
	 * DECE MP4
	 */
	video_vnd_uvvu_mp4(".uvu"),

	/**
	 * Microsoft PlayReady Ecosystem Video
	 */
	video_vnd_ms_playready_media_pyv(".pyv"),

	/**
	 * MPEG Url
	 */
	video_vnd_mpegurl(".mxu"),

	/**
	 * FAST Search & Transfer ASA
	 */
	video_vnd_fvt(".fvt"),

	/**
	 * DECE Video
	 */
	video_vnd_dece_video(".uvv"),

	/**
	 * DECE SD Video
	 */
	video_vnd_dece_sd(".uvs"),

	/**
	 * DECE PD Video
	 */
	video_vnd_dece_pd(".uvp"),

	/**
	 * DECE Mobile Video
	 */
	video_vnd_dece_mobile(".uvm"),

	/**
	 * DECE High Definition Video
	 */
	video_vnd_dece_hd(".uvh"),

	/**
	 * Quicktime Video
	 */
	video_quicktime(".qt"),

	/**
	 * Ogg Video
	 */
	video_ogg(".ogv"),

	/**
	 * MPEG Video
	 */
	video_mpeg(".mpeg"),

	/**
	 * MPEG-4 Video
	 */
	video_mp4(".mp4"),

	/**
	 * Motion JPEG 2000
	 */
	video_mj2(".mj2"),

	/**
	 * JPEG 2000 Compound Image File Format
	 */
	video_jpm(".jpm"),

	/**
	 * JPGVideo
	 */
	video_jpeg(".jpgv"),

	/**
	 * H.264
	 */
	video_h264(".h264"),

	/**
	 * H.263
	 */
	video_h263(".h263"),

	/**
	 * H.261
	 */
	video_h261(".h261"),

	/**
	 * 3GP2
	 */
	video_3gpp2(".3g2"),

	/**
	 * 3GP
	 */
	video_3gpp(".3gp"),

	/**
	 * TS
	 */
	video_mp2t(".ts"),

	/**
	 * YAML Ain't Markup Language / Yet Another Markup Language
	 */
	text_yaml(".yaml"),

	/**
	 * vCard
	 */
	text_x_vcard(".vcf"),

	/**
	 * vCalendar
	 */
	text_x_vcalendar(".vcs"),

	/**
	 * UUEncode
	 */
	text_x_uuencode(".uu"),

	/**
	 * Setext
	 */
	text_x_setext(".etx"),

	/**
	 * Pascal Source File
	 */
	text_x_pascal(".p"),

	/**
	 * Java Source File
	 */
	text_x_java_source(".java"),

	/**
	 * Fortran Source File
	 */
	text_x_fortran(".f"),

	/**
	 * C Source File
	 */
	text_x_c(".c"),

	/**
	 * Assembler Source File
	 */
	text_x_asm(".s"),

	/**
	 * Wireless Markup Language Script (WMLScript)
	 */
	text_vnd_wap_wmlscript(".wmls"),

	/**
	 * Wireless Markup Language (WML)
	 */
	text_vnd_wap_wml(".wml"),

	/**
	 * J2ME App Descriptor
	 */
	text_vnd_sun_j2me_app_descriptor(".jad"),

	/**
	 * In3D - 3DML
	 */
	text_vnd_in3d_spot(".spot"),

	/**
	 * In3D - 3DML
	 */
	text_vnd_in3d_3dml(".3dml"),

	/**
	 * Graphviz
	 */
	text_vnd_graphviz(".gv"),

	/**
	 * FLEXSTOR
	 */
	text_vnd_fmi_flexstor(".flx"),

	/**
	 * mod_fly / fly.cgi
	 */
	text_vnd_fly(".fly"),

	/**
	 * Curl - Source Code
	 */
	text_vnd_curl_scurl(".scurl"),

	/**
	 * Curl - Manifest File
	 */
	text_vnd_curl_mcurl(".mcurl"),

	/**
	 * Curl - Detached Applet
	 */
	text_vnd_curl_dcurl(".dcurl"),

	/**
	 * Curl - Applet
	 */
	text_vnd_curl(".curl"),

	/**
	 * URI Resolution Services
	 */
	text_uri_list(".uri"),

	/**
	 * Turtle (Terse RDF Triple Language)
	 */
	text_turtle(".ttl"),

	/**
	 * troff
	 */
	text_troff(".t"),

	/**
	 * Tab Seperated Values
	 */
	text_tab_separated_values(".tsv"),

	/**
	 * Standard Generalized Markup Language (SGML)
	 */
	text_sgml(".sgml"),

	/**
	 * Rich Text Format (RTF)
	 */
	text_richtext(".rtx"),

	/**
	 * PRS Lines Tag
	 */
	text_prs_lines_tag(".dsc"),

	/**
	 * BAS Partitur Format
	 */
	text_plain_bas(".par"),

	/**
	 * Text File
	 */
	text_plain(".txt"),

	/**
	 * Notation3
	 */
	text_n3(".n3"),

	/**
	 * HyperText Markup Language (HTML)
	 */
	text_html(".html"),

	/**
	 * Comma-Seperated Values
	 */
	text_csv(".csv"),

	/**
	 * Cascading Style Sheets (CSS)
	 */
	text_css(".css"),

	/**
	 * iCalendar
	 */
	text_calendar(".ics"),

	/**
	 * Virtual Reality Modeling Language
	 */
	model_vrml(".wrl"),

	/**
	 * Virtue VTU
	 */
	model_vnd_vtu(".vtu"),

	/**
	 * Virtue MTS
	 */
	model_vnd_mts(".mts"),

	/**
	 * Gen-Trix Studio
	 */
	model_vnd_gtw(".gtw"),

	/**
	 * Geometric Description Language (GDL)
	 */
	model_vnd_gdl(".gdl"),

	/**
	 * Autodesk Design Web Format (DWF)
	 */
	model_vnd_dwf(".dwf"),

	/**
	 * COLLADA
	 */
	model_vnd_collada_xml(".dae"),

	/**
	 * Mesh Data Type
	 */
	model_mesh(".msh"),

	/**
	 * Initial Graphics Exchange Specification (IGES)
	 */
	model_iges(".igs"),

	/**
	 * Email Message
	 */
	message_rfc822(".eml"),

	/**
	 * X Window Dump
	 */
	image_x_xwindowdump(".xwd"),

	/**
	 * X PixMap
	 */
	image_x_xpixmap(".xpm"),

	/**
	 * X BitMap
	 */
	image_x_xbitmap(".xbm"),

	/**
	 * Silicon Graphics RGB Bitmap
	 */
	image_x_rgb(".rgb"),

	/**
	 * Portable Pixmap Format
	 */
	image_x_portable_pixmap(".ppm"),

	/**
	 * Portable Graymap Format
	 */
	image_x_portable_graymap(".pgm"),

	/**
	 * Portable Bitmap Format
	 */
	image_x_portable_bitmap(".pbm"),

	/**
	 * Portable Anymap Image
	 */
	image_x_portable_anymap(".pnm"),

	/**
	 * Portable Network Graphics (PNG) (x-token)
	 */
	image_x_png(".png"),

	/**
	 * PICT Image
	 */
	image_x_pict(".pic"),

	/**
	 * PCX Image
	 */
	image_x_pcx(".pcx"),

	/**
	 * Icon Image
	 */
	image_x_icon(".ico"),

	/**
	 * FreeHand MX
	 */
	image_x_freehand(".fh"),

	/**
	 * Corel Metafile Exchange (CMX)
	 */
	image_x_cmx(".cmx"),

	/**
	 * CMU Image
	 */
	image_x_cmu_raster(".ras"),

	/**
	 * Portable Network Graphics (PNG) (Citrix client)
	 */
	image_x_citrix_png(".png"),

	/**
	 * JPEG Image (Citrix client)
	 */
	image_x_citrix_jpeg(".jpg"),

	/**
	 * WebP Image
	 */
	image_webp(".webp"),

	/**
	 * eXtended Image File Format (XIFF)
	 */
	image_vnd_xiff(".xif"),

	/**
	 * WAP Bitamp (WBMP)
	 */
	image_vnd_wap_wbmp(".wbmp"),

	/**
	 * FlashPix
	 */
	image_vnd_net_fpx(".npx"),

	/**
	 * Microsoft Document Imaging Format
	 */
	image_vnd_ms_modi(".mdi"),

	/**
	 * EDMICS 2000
	 */
	image_vnd_fujixerox_edmics_rlc(".rlc"),

	/**
	 * EDMICS 2000
	 */
	image_vnd_fujixerox_edmics_mmr(".mmr"),

	/**
	 * FAST Search & Transfer ASA
	 */
	image_vnd_fst(".fst"),

	/**
	 * FlashPix
	 */
	image_vnd_fpx(".fpx"),

	/**
	 * FastBid Sheet
	 */
	image_vnd_fastbidsheet(".fbs"),

	/**
	 * AutoCAD DXF
	 */
	image_vnd_dxf(".dxf"),

	/**
	 * DWG Drawing
	 */
	image_vnd_dwg(".dwg"),

	/**
	 * Close Captioning - Subtitle
	 */
	image_vnd_dvb_subtitle(".sub"),

	/**
	 * DjVu
	 */
	image_vnd_djvu(".djvu"),

	/**
	 * DECE Graphic
	 */
	image_vnd_dece_graphic(".uvi"),

	/**
	 * Photoshop Document
	 */
	image_vnd_adobe_photoshop(".psd"),

	/**
	 * Tagged Image File Format
	 */
	image_tiff(".tiff"),

	/**
	 * Scalable Vector Graphics (SVG)
	 */
	image_svg_xml(".svg"),

	/**
	 * BTIF
	 */
	image_prs_btif(".btif"),

	/**
	 * Portable Network Graphics (PNG)
	 */
	image_png(".png"),

	/**
	 * JPEG Image (Progressive)
	 */
	image_pjpeg(".pjpeg"),

	/**
	 * OpenGL Textures (KTX)
	 */
	image_ktx(".ktx"),

	/**
	 * JPEG Image
	 */
	image_jpeg(".jpg"),

	/**
	 * Image Exchange Format
	 */
	image_ief(".ief"),

	/**
	 * Graphics Interchange Format
	 */
	image_gif(".gif"),

	/**
	 * G3 Fax Image
	 */
	image_g3fax(".g3"),

	/**
	 * Computer Graphics Metafile
	 */
	image_cgm(".cgm"),

	/**
	 * Bitmap Image File
	 */
	image_bmp(".bmp"),

	/**
	 * XYZ File Format
	 */
	chemical_x_xyz(".xyz"),

	/**
	 * Chemical Style Markup Language
	 */
	chemical_x_csml(".csml"),

	/**
	 * Chemical Markup Language
	 */
	chemical_x_cml(".cml"),

	/**
	 * CrystalMaker Data Format
	 */
	chemical_x_cmdf(".cmdf"),

	/**
	 * Crystallographic Interchange Format
	 */
	chemical_x_cif(".cif"),

	/**
	 * ChemDraw eXchange file
	 */
	chemical_x_cdx(".cdx"),

	/**
	 * Waveform Audio File Format (WAV)
	 */
	audio_x_wav(".wav"),

	/**
	 * Real Audio Sound
	 */
	audio_x_pn_realaudio_plugin(".rmp"),

	/**
	 * Real Audio Sound
	 */
	audio_x_pn_realaudio(".ram"),

	/**
	 * Microsoft Windows Media Audio
	 */
	audio_x_ms_wma(".wma"),

	/**
	 * Microsoft Windows Media Audio Redirector
	 */
	audio_x_ms_wax(".wax"),

	/**
	 * M3U (Multimedia Playlist)
	 */
	audio_x_mpegurl(".m3u"),

	/**
	 * Audio Interchange File Format
	 */
	audio_x_aiff(".aif"),

	/**
	 * Advanced Audio Coding (AAC)
	 */
	audio_x_aac(".aac"),

	/**
	 * Open Web Media Project - Audio
	 */
	audio_webm(".weba"),

	/**
	 * Hit'n'Mix
	 */
	audio_vnd_rip(".rip"),

	/**
	 * Nuera ECELP 9600
	 */
	audio_vnd_nuera_ecelp9600(".ecelp9600"),

	/**
	 * Nuera ECELP 7470
	 */
	audio_vnd_nuera_ecelp7470(".ecelp7470"),

	/**
	 * Nuera ECELP 4800
	 */
	audio_vnd_nuera_ecelp4800(".ecelp4800"),

	/**
	 * Microsoft PlayReady Ecosystem
	 */
	audio_vnd_ms_playready_media_pya(".pya"),

	/**
	 * Lucent Voice
	 */
	audio_vnd_lucent_voice(".lvp"),

	/**
	 * DTS High Definition Audio
	 */
	audio_vnd_dts_hd(".dtshd"),

	/**
	 * DTS Audio
	 */
	audio_vnd_dts(".dts"),

	/**
	 * DRA Audio
	 */
	audio_vnd_dra(".dra"),

	/**
	 * Digital Winds Music
	 */
	audio_vnd_digital_winds(".eol"),

	/**
	 * DECE Audio
	 */
	audio_vnd_dece_audio(".uva"),

	/**
	 * Ogg Audio
	 */
	audio_ogg(".oga"),

	/**
	 * MP3 Audio
	 */
	audio_mpeg(".mp3"),

	/**
	 * MPEG-4 Audio
	 */
	audio_mp4(".m4a"),

	/**
	 * MIDI - Musical Instrument Digital Interface
	 */
	audio_midi(".mid"),

	/**
	 * Sun Audio - Au file format
	 */
	audio_basic(".au"),

	/**
	 * Adaptive differential pulse-code modulation
	 */
	audio_adpcm(".adp"),

	/**
	 * Zip Archive
	 */
	application_zip(".zip"),

	/**
	 * YIN (YANG - XML)
	 */
	application_yin_xml(".yin"),

	/**
	 * YANG Data Modeling Language
	 */
	application_yang(".yang"),

	/**
	 * MXML
	 */
	application_xv_xml(".mxml"),

	/**
	 * XSPF - XML Shareable Playlist Format
	 */
	application_xspf_xml(".xspf"),

	/**
	 * XML Transformations
	 */
	application_xslt_xml(".xslt"),

	/**
	 * XML-Binary Optimized Packaging
	 */
	application_xop_xml(".xop"),

	/**
	 * Document Type Definition
	 */
	application_xml_dtd(".dtd"),

	/**
	 * XML - Extensible Markup Language
	 */
	application_xml(".xml"),

	/**
	 * XHTML - The Extensible HyperText Markup Language
	 */
	application_xhtml_xml(".xhtml"),

	/**
	 * XML Encryption Syntax and Processing
	 */
	application_xenc_xml(".xenc"),

	/**
	 * XML Configuration Access Protocol - XCAP Diff
	 */
	application_xcap_diff_xml(".xdf"),

	/**
	 * XPInstall - Mozilla
	 */
	application_x_xpinstall(".xpi"),

	/**
	 * Xfig
	 */
	application_x_xfig(".fig"),

	/**
	 * X.509 Certificate
	 */
	application_x_x509_ca_cert(".der"),

	/**
	 * WAIS Source
	 */
	application_x_wais_source(".src"),

	/**
	 * Ustar (Uniform Standard Tape Archive)
	 */
	application_x_ustar(".ustar"),

	/**
	 * GNU Texinfo Document
	 */
	application_x_texinfo(".texinfo"),

	/**
	 * TeX Font Metric
	 */
	application_x_tex_tfm(".tfm"),

	/**
	 * TeX
	 */
	application_x_tex(".tex"),

	/**
	 * Tcl Script
	 */
	application_x_tcl(".tcl"),

	/**
	 * Tar File (Tape Archive)
	 */
	application_x_tar(".tar"),

	/**
	 * System V Release 4 CPIO Checksum Data
	 */
	application_x_sv4crc(".sv4crc"),

	/**
	 * System V Release 4 CPIO Archive
	 */
	application_x_sv4cpio(".sv4cpio"),

	/**
	 * Stuffit Archive
	 */
	application_x_stuffitx(".sitx"),

	/**
	 * Stuffit Archive
	 */
	application_x_stuffit(".sit"),

	/**
	 * Microsoft Silverlight
	 */
	application_x_silverlight_app(".xap"),

	/**
	 * Adobe Flash
	 */
	application_x_shockwave_flash(".swf"),

	/**
	 * Shell Archive
	 */
	application_x_shar(".shar"),

	/**
	 * Bourne Shell Script
	 */
	application_x_sh(".sh"),

	/**
	 * RAR Archive
	 */
	application_x_rar_compressed(".rar"),

	/**
	 * PKCS #7 - Cryptographic Message Syntax Standard (Certificate Request Response)
	 */
	application_x_pkcs7_certreqresp(".p7r"),

	/**
	 * PKCS #7 - Cryptographic Message Syntax Standard (Certificates)
	 */
	application_x_pkcs7_certificates(".p7b"),

	/**
	 * PKCS #12 - Personal Information Exchange Syntax Standard
	 */
	application_x_pkcs12(".p12"),

	/**
	 * Network Common Data Form (NetCDF)
	 */
	application_x_netcdf(".nc"),

	/**
	 * Microsoft Wordpad
	 */
	application_x_mswrite(".wri"),

	/**
	 * Microsoft Windows Terminal Services
	 */
	application_x_msterminal(".trm"),

	/**
	 * Microsoft Schedule+
	 */
	application_x_msschedule(".scd"),

	/**
	 * Microsoft Publisher
	 */
	application_x_mspublisher(".pub"),

	/**
	 * Microsoft Money
	 */
	application_x_msmoney(".mny"),

	/**
	 * Microsoft Windows Metafile
	 */
	application_x_msmetafile(".wmf"),

	/**
	 * Microsoft MediaView
	 */
	application_x_msmediaview(".mvb"),

	/**
	 * Microsoft Application
	 */
	application_x_msdownload(".exe"),

	/**
	 * Microsoft Clipboard Clip
	 */
	application_x_msclip(".clp"),

	/**
	 * Microsoft Information Card
	 */
	application_x_mscardfile(".crd"),

	/**
	 * Microsoft Office Binder
	 */
	application_x_msbinder(".obd"),

	/**
	 * Microsoft Access
	 */
	application_x_msaccess(".mdb"),

	/**
	 * Microsoft XAML Browser Application
	 */
	application_x_ms_xbap(".xbap"),

	/**
	 * Microsoft Windows Media Player Skin Package
	 */
	application_x_ms_wmz(".wmz"),

	/**
	 * Microsoft Windows Media Player Download Package
	 */
	application_x_ms_wmd(".wmd"),

	/**
	 * Microsoft ClickOnce
	 */
	application_x_ms_application(".application"),

	/**
	 * Mobipocket
	 */
	application_x_mobipocket_ebook(".prc"),

	/**
	 * LaTeX
	 */
	application_x_latex(".latex"),

	/**
	 * Java Network Launching Protocol
	 */
	application_x_java_jnlp_file(".jnlp"),

	/**
	 * Hierarchical Data Format
	 */
	application_x_hdf(".hdf"),

	/**
	 * GNU Tar Files
	 */
	application_x_gtar(".gtar"),

	/**
	 * Gnumeric
	 */
	application_x_gnumeric(".gnumeric"),

	/**
	 * FutureSplash Animator
	 */
	application_x_futuresplash(".spl"),

	/**
	 * Web Open Font Format
	 */
	application_x_font_woff(".woff"),

	/**
	 * PostScript Fonts
	 */
	application_x_font_type1(".pfa"),

	/**
	 * TrueType Font
	 */
	application_x_font_ttf(".ttf"),

	/**
	 * Server Normal Format
	 */
	application_x_font_snf(".snf"),

	/**
	 * Portable Compiled Format
	 */
	application_x_font_pcf(".pcf"),

	/**
	 * OpenType Font File
	 */
	application_x_font_otf(".otf"),

	/**
	 * PSF Fonts
	 */
	application_x_font_linux_psf(".psf"),

	/**
	 * Ghostscript Font
	 */
	application_x_font_ghostscript(".gsf"),

	/**
	 * Glyph Bitmap Distribution Format
	 */
	application_x_font_bdf(".bdf"),

	/**
	 * Device Independent File Format (DVI)
	 */
	application_x_dvi(".dvi"),

	/**
	 * Digital Talking Book - Resource File
	 */
	application_x_dtbresource_xml(".res"),

	/**
	 * Digital Talking Book
	 */
	application_x_dtbook_xml(".dtb"),

	/**
	 * Navigation Control file for XML (for ePub)
	 */
	application_x_dtbncx_xml(".ncx"),

	/**
	 * Doom Video Game
	 */
	application_x_doom(".wad"),

	/**
	 * Adobe Shockwave Player
	 */
	application_x_director(".dir"),

	/**
	 * Debian Package
	 */
	application_x_debian_package(".deb"),

	/**
	 * C Shell Script
	 */
	application_x_csh(".csh"),

	/**
	 * CPIO Archive
	 */
	application_x_cpio(".cpio"),

	/**
	 * Portable Game Notation (Chess Games)
	 */
	application_x_chess_pgn(".pgn"),

	/**
	 * pIRCh
	 */
	application_x_chat(".chat"),

	/**
	 * Video CD
	 */
	application_x_cdlink(".vcd"),

	/**
	 * Bzip2 Archive
	 */
	application_x_bzip2(".bz2"),

	/**
	 * Bzip Archive
	 */
	application_x_bzip(".bz"),

	/**
	 * BitTorrent
	 */
	application_x_bittorrent(".torrent"),

	/**
	 * Binary CPIO Archive
	 */
	application_x_bcpio(".bcpio"),

	/**
	 * Adobe (Macropedia) Authorware - Segment File
	 */
	application_x_authorware_seg(".aas"),

	/**
	 * Adobe (Macropedia) Authorware - Map
	 */
	application_x_authorware_map(".aam"),

	/**
	 * Adobe (Macropedia) Authorware - Binary File
	 */
	application_x_authorware_bin(".aab"),

	/**
	 * Ace Archive
	 */
	application_x_ace_compressed(".ace"),

	/**
	 * AbiWord
	 */
	application_x_abiword(".abw"),

	/**
	 * 7-Zip
	 */
	application_x_7z_compressed(".7z"),

	/**
	 * Web Services Policy
	 */
	application_wspolicy_xml(".wspolicy"),

	/**
	 * WSDL - Web Services Description Language
	 */
	application_wsdl_xml(".wsdl"),

	/**
	 * WinHelp
	 */
	application_winhlp(".hlp"),

	/**
	 * Widget Packaging and XML Configuration
	 */
	application_widget(".wgt"),

	/**
	 * VoiceXML
	 */
	application_voicexml_xml(".vxml"),

	/**
	 * Zzazz Deck
	 */
	application_vnd_zzazz_deck_xml(".zaz"),

	/**
	 * Z.U.L. Geometry
	 */
	application_vnd_zul(".zir"),

	/**
	 * CustomMenu
	 */
	application_vnd_yellowriver_custom_menu(".cmp"),

	/**
	 * SMAF Phrase
	 */
	application_vnd_yamaha_smaf_phrase(".spf"),

	/**
	 * SMAF Audio
	 */
	application_vnd_yamaha_smaf_audio(".saf"),

	/**
	 * OSFPVG
	 */
	application_vnd_yamaha_openscoreformat_osfpvg_xml(".osfpvg"),

	/**
	 * Open Score Format
	 */
	application_vnd_yamaha_openscoreformat(".osf"),

	/**
	 * HV Voice Parameter
	 */
	application_vnd_yamaha_hv_voice(".hvp"),

	/**
	 * HV Script
	 */
	application_vnd_yamaha_hv_script(".hvs"),

	/**
	 * HV Voice Dictionary
	 */
	application_vnd_yamaha_hv_dic(".hvd"),

	/**
	 * Extensible Forms Description Language
	 */
	application_vnd_xfdl(".xfdl"),

	/**
	 * CorelXARA
	 */
	application_vnd_xara(".xar"),

	/**
	 * Worldtalk
	 */
	application_vnd_wt_stf(".stf"),

	/**
	 * SundaHus WQ
	 */
	application_vnd_wqd(".wqd"),

	/**
	 * Wordperfect
	 */
	application_vnd_wordperfect(".wpd"),

	/**
	 * Mathematica Notebook Player
	 */
	application_vnd_wolfram_player(".nbp"),

	/**
	 * WebTurbo
	 */
	application_vnd_webturbo(".wtb"),

	/**
	 * WMLScript
	 */
	application_vnd_wap_wmlscriptc(".wmlsc"),

	/**
	 * Compiled Wireless Markup Language (WMLC)
	 */
	application_vnd_wap_wmlc(".wmlc"),

	/**
	 * WAP Binary XML (WBXML)
	 */
	application_vnd_wap_wbxml(".wbxml"),

	/**
	 * Viewport+
	 */
	application_vnd_vsf(".vsf"),

	/**
	 * Visionary
	 */
	application_vnd_visionary(".vis"),

	/**
	 * Microsoft Visio 2013
	 */
	application_vnd_visio2013(".vsdx"),

	/**
	 * Microsoft Visio
	 */
	application_vnd_visio(".vsd"),

	/**
	 * VirtualCatalog
	 */
	application_vnd_vcx(".vcx"),

	/**
	 * Unique Object Markup Language
	 */
	application_vnd_uoml_xml(".uoml"),

	/**
	 * Unity 3d
	 */
	application_vnd_unity(".unityweb"),

	/**
	 * UMAJIN
	 */
	application_vnd_umajin(".umj"),

	/**
	 * User Interface Quartz - Theme (Symbian)
	 */
	application_vnd_uiq_theme(".utz"),

	/**
	 * Universal Forms Description Language
	 */
	application_vnd_ufdl(".ufd"),

	/**
	 * True BASIC
	 */
	application_vnd_trueapp(".tra"),

	/**
	 * Triscape Map Explorer
	 */
	application_vnd_triscape_mxs(".mxs"),

	/**
	 * TRI Systems Config
	 */
	application_vnd_trid_tpt(".tpt"),

	/**
	 * MobileTV
	 */
	application_vnd_tmobile_livetv(".tmo"),

	/**
	 * Tao Intent
	 */
	application_vnd_tao_intent_module_archive(".tao"),

	/**
	 * SyncML - Device Management
	 */
	application_vnd_syncml_dm_xml(".xdm"),

	/**
	 * SyncML - Device Management
	 */
	application_vnd_syncml_dm_wbxml(".bdm"),

	/**
	 * SyncML
	 */
	application_vnd_syncml_xml(".xsm"),

	/**
	 * Symbian Install Package
	 */
	application_vnd_symbian_install(".sis"),

	/**
	 * SourceView Document
	 */
	application_vnd_svd(".svd"),

	/**
	 * ScheduleUs
	 */
	application_vnd_sus_calendar(".sus"),

	/**
	 * OpenOffice - Writer Template (Text - HTML)
	 */
	application_vnd_sun_xml_writer_template(".stw"),

	/**
	 * OpenOffice - Writer (Text - HTML)
	 */
	application_vnd_sun_xml_writer_global(".sxg"),

	/**
	 * OpenOffice - Writer (Text - HTML)
	 */
	application_vnd_sun_xml_writer(".sxw"),

	/**
	 * OpenOffice - Math (Formula)
	 */
	application_vnd_sun_xml_math(".sxm"),

	/**
	 * OpenOffice - Impress Template (Presentation)
	 */
	application_vnd_sun_xml_impress_template(".sti"),

	/**
	 * OpenOffice - Impress (Presentation)
	 */
	application_vnd_sun_xml_impress(".sxi"),

	/**
	 * OpenOffice - Draw Template (Graphics)
	 */
	application_vnd_sun_xml_draw_template(".std"),

	/**
	 * OpenOffice - Draw (Graphics)
	 */
	application_vnd_sun_xml_draw(".sxd"),

	/**
	 * OpenOffice - Calc Template (Spreadsheet)
	 */
	application_vnd_sun_xml_calc_template(".stc"),

	/**
	 * OpenOffice - Calc (Spreadsheet)
	 */
	application_vnd_sun_xml_calc(".sxc"),

	/**
	 * StepMania
	 */
	application_vnd_stepmania_stepchart(".sm"),

	/**
	 * StarOffice - Writer (Global)
	 */
	application_vnd_stardivision_writer_global(".sgl"),

	/**
	 * StarOffice - Writer
	 */
	application_vnd_stardivision_writer(".sdw"),

	/**
	 * StarOffice - Math
	 */
	application_vnd_stardivision_math(".smf"),

	/**
	 * StarOffice - Impress
	 */
	application_vnd_stardivision_impress(".sdd"),

	/**
	 * StarOffice - Draw
	 */
	application_vnd_stardivision_draw(".sda"),

	/**
	 * StarOffice - Calc
	 */
	application_vnd_stardivision_calc(".sdc"),

	/**
	 * TIBCO Spotfire
	 */
	application_vnd_spotfire_sfs(".sfs"),

	/**
	 * TIBCO Spotfire
	 */
	application_vnd_spotfire_dxp(".dxp"),

	/**
	 * SudokuMagic
	 */
	application_vnd_solent_sdkm_xml(".sdkm"),

	/**
	 * SMART Technologies Apps
	 */
	application_vnd_smart_teacher(".teacher"),

	/**
	 * SMAF File
	 */
	application_vnd_smaf(".mmf"),

	/**
	 * SimTech MindMapper
	 */
	application_vnd_simtech_mindmapper(".twd"),

	/**
	 * Shana Informed Filler
	 */
	application_vnd_shana_informed_package(".ipk"),

	/**
	 * Shana Informed Filler
	 */
	application_vnd_shana_informed_interchange(".iif"),

	/**
	 * Shana Informed Filler
	 */
	application_vnd_shana_informed_formtemplate(".itp"),

	/**
	 * Shana Informed Filler
	 */
	application_vnd_shana_informed_formdata(".ifm"),

	/**
	 * Secured eMail
	 */
	application_vnd_semf(".semf"),

	/**
	 * Secured eMail
	 */
	application_vnd_semd(".semd"),

	/**
	 * Secured eMail
	 */
	application_vnd_sema(".sema"),

	/**
	 * SeeMail
	 */
	application_vnd_seemail(".see"),

	/**
	 * SailingTracker
	 */
	application_vnd_sailingtracker_track(".st"),

	/**
	 * ROUTE 66 Location Based Services
	 */
	application_vnd_route66_link66_xml(".link66"),

	/**
	 * RealMedia
	 */
	application_vnd_rn_realmedia(".rm"),

	/**
	 * Blackberry COD File
	 */
	application_vnd_rim_cod(".cod"),

	/**
	 * CryptoNote
	 */
	application_vnd_rig_cryptonote(".cryptonote"),

	/**
	 * Recordare Applications
	 */
	application_vnd_recordare_musicxml_xml(".musicxml"),

	/**
	 * Recordare Applications
	 */
	application_vnd_recordare_musicxml(".mxl"),

	/**
	 * RealVNC
	 */
	application_vnd_realvnc_bed(".bed"),

	/**
	 * QuarkXpress
	 */
	application_vnd_quark_quarkxpress(".qxd"),

	/**
	 * Princeton Video Image
	 */
	application_vnd_pvi_ptid1(".ptid"),

	/**
	 * PubliShare Objects
	 */
	application_vnd_publishare_delta_tree(".qps"),

	/**
	 * EFI Proteus
	 */
	application_vnd_proteus_magazine(".mgz"),

	/**
	 * Preview Systems ZipLock/VBox
	 */
	application_vnd_previewsystems_box(".box"),

	/**
	 * PowerBuilder
	 */
	application_vnd_powerbuilder6(".pbd"),

	/**
	 * PocketLearn Viewers
	 */
	application_vnd_pocketlearn(".plf"),

	/**
	 * Qualcomm's Plaza Mobile Internet
	 */
	application_vnd_pmi_widget(".wg"),

	/**
	 * Pcsel eFIF File
	 */
	application_vnd_picsel(".efif"),

	/**
	 * Proprietary P&G Standard Reporting System
	 */
	application_vnd_pg_osasli(".ei6"),

	/**
	 * Proprietary P&G Standard Reporting System
	 */
	application_vnd_pg_format(".str"),

	/**
	 * PawaaFILE
	 */
	application_vnd_pawaafile(".paw"),

	/**
	 * PalmOS Data
	 */
	application_vnd_palm(".pdb"),

	/**
	 * OSGi Deployment Package
	 */
	application_vnd_osgi_dp(".dp"),

	/**
	 * MapGuide DBXML
	 */
	application_vnd_osgeo_mapguide_package(".mgp"),

	/**
	 * Microsoft Office - OOXML - Word Document Template
	 */
	application_vnd_openxmlformats_officedocument_wordprocessingml_template(".dotx"),

	/**
	 * Microsoft Office - OOXML - Word Document
	 */
	application_vnd_openxmlformats_officedocument_wordprocessingml_document(".docx"),

	/**
	 * Microsoft Office - OOXML - Spreadsheet Template
	 */
	application_vnd_openxmlformats_officedocument_spreadsheetml_template(".xltx"),

	/**
	 * Microsoft Office - OOXML - Spreadsheet
	 */
	application_vnd_openxmlformats_officedocument_spreadsheetml_sheet(".xlsx"),

	/**
	 * Microsoft Office - OOXML - Presentation Template
	 */
	application_vnd_openxmlformats_officedocument_presentationml_template(".potx"),

	/**
	 * Microsoft Office - OOXML - Presentation (Slideshow)
	 */
	application_vnd_openxmlformats_officedocument_presentationml_slideshow(".ppsx"),

	/**
	 * Microsoft Office - OOXML - Presentation (Slide)
	 */
	application_vnd_openxmlformats_officedocument_presentationml_slide(".sldx"),

	/**
	 * Microsoft Office - OOXML - Presentation
	 */
	application_vnd_openxmlformats_officedocument_presentationml_presentation(".pptx"),

	/**
	 * Open Office Extension
	 */
	application_vnd_openofficeorg_extension(".oxt"),

	/**
	 * OMA Download Agents
	 */
	application_vnd_oma_dd2_xml(".dd2"),

	/**
	 * Sugar Linux Application Bundle
	 */
	application_vnd_olpc_sugar(".xo"),

	/**
	 * Open Document Text Web
	 */
	application_vnd_oasis_opendocument_text_web(".oth"),

	/**
	 * OpenDocument Text Template
	 */
	application_vnd_oasis_opendocument_text_template(".ott"),

	/**
	 * OpenDocument Text Master
	 */
	application_vnd_oasis_opendocument_text_master(".odm"),

	/**
	 * OpenDocument Text
	 */
	application_vnd_oasis_opendocument_text(".odt"),

	/**
	 * OpenDocument Spreadsheet Template
	 */
	application_vnd_oasis_opendocument_spreadsheet_template(".ots"),

	/**
	 * OpenDocument Spreadsheet
	 */
	application_vnd_oasis_opendocument_spreadsheet(".ods"),

	/**
	 * OpenDocument Presentation Template
	 */
	application_vnd_oasis_opendocument_presentation_template(".otp"),

	/**
	 * OpenDocument Presentation
	 */
	application_vnd_oasis_opendocument_presentation(".odp"),

	/**
	 * OpenDocument Image Template
	 */
	application_vnd_oasis_opendocument_image_template(".oti"),

	/**
	 * OpenDocument Image
	 */
	application_vnd_oasis_opendocument_image(".odi"),

	/**
	 * OpenDocument Graphics Template
	 */
	application_vnd_oasis_opendocument_graphics_template(".otg"),

	/**
	 * OpenDocument Graphics
	 */
	application_vnd_oasis_opendocument_graphics(".odg"),

	/**
	 * OpenDocument Formula Template
	 */
	application_vnd_oasis_opendocument_formula_template(".odft"),

	/**
	 * OpenDocument Formula
	 */
	application_vnd_oasis_opendocument_formula(".odf"),

	/**
	 * OpenDocument Database
	 */
	application_vnd_oasis_opendocument_database(".odb"),

	/**
	 * OpenDocument Chart Template
	 */
	application_vnd_oasis_opendocument_chart_template(".otc"),

	/**
	 * OpenDocument Chart
	 */
	application_vnd_oasis_opendocument_chart(".odc"),

	/**
	 * Novadigm's RADIA and EDM products
	 */
	application_vnd_novadigm_ext(".ext"),

	/**
	 * Novadigm's RADIA and EDM products
	 */
	application_vnd_novadigm_edx(".edx"),

	/**
	 * Novadigm's RADIA and EDM products
	 */
	application_vnd_novadigm_edm(".edm"),

	/**
	 * Nokia Radio Application - Preset
	 */
	application_vnd_nokia_radio_presets(".rpss"),

	/**
	 * Nokia Radio Application - Preset
	 */
	application_vnd_nokia_radio_preset(".rpst"),

	/**
	 * N-Gage Game Installer
	 */
	application_vnd_nokia_n_gage_symbian_install(".n-gage"),

	/**
	 * N-Gage Game Data
	 */
	application_vnd_nokia_n_gage_data(".ngdat"),

	/**
	 * NobleNet Web
	 */
	application_vnd_noblenet_web(".nnw"),

	/**
	 * NobleNet Sealer
	 */
	application_vnd_noblenet_sealer(".nns"),

	/**
	 * NobleNet Directory
	 */
	application_vnd_noblenet_directory(".nnd"),

	/**
	 * neuroLanguage
	 */
	application_vnd_neurolanguage_nlu(".nlu"),

	/**
	 * Muvee Automatic Video Editing
	 */
	application_vnd_muvee_style(".msty"),

	/**
	 * MUsical Score Interpreted Code Invented for the ASCII designation of Notation
	 */
	application_vnd_musician(".mus"),

	/**
	 * 3GPP MSEQ File
	 */
	application_vnd_mseq(".mseq"),

	/**
	 * Microsoft XML Paper Specification
	 */
	application_vnd_ms_xpsdocument(".xps"),

	/**
	 * Microsoft Windows Media Player Playlist
	 */
	application_vnd_ms_wpl(".wpl"),

	/**
	 * Microsoft Works
	 */
	application_vnd_ms_works(".wps"),

	/**
	 * Microsoft Word - Macro-Enabled Template
	 */
	application_vnd_ms_word_template_macroenabled_12(".dotm"),

	/**
	 * Microsoft Word - Macro-Enabled Document
	 */
	application_vnd_ms_word_document_macroenabled_12(".docm"),

	/**
	 * Microsoft Project
	 */
	application_vnd_ms_project(".mpp"),

	/**
	 * Microsoft PowerPoint - Macro-Enabled Template File
	 */
	application_vnd_ms_powerpoint_template_macroenabled_12(".potm"),

	/**
	 * Microsoft PowerPoint - Macro-Enabled Slide Show File
	 */
	application_vnd_ms_powerpoint_slideshow_macroenabled_12(".ppsm"),

	/**
	 * Microsoft PowerPoint - Macro-Enabled Open XML Slide
	 */
	application_vnd_ms_powerpoint_slide_macroenabled_12(".sldm"),

	/**
	 * Microsoft PowerPoint - Macro-Enabled Presentation File
	 */
	application_vnd_ms_powerpoint_presentation_macroenabled_12(".pptm"),

	/**
	 * Microsoft PowerPoint - Add-in file
	 */
	application_vnd_ms_powerpoint_addin_macroenabled_12(".ppam"),

	/**
	 * Microsoft PowerPoint
	 */
	application_vnd_ms_powerpoint(".ppt"),

	/**
	 * Microsoft Trust UI Provider - Certificate Trust Link
	 */
	application_vnd_ms_pki_stl(".stl"),

	/**
	 * Microsoft Trust UI Provider - Security Catalog
	 */
	application_vnd_ms_pki_seccat(".cat"),

	/**
	 * Microsoft Office System Release Theme
	 */
	application_vnd_ms_officetheme(".thmx"),

	/**
	 * Microsoft Learning Resource Module
	 */
	application_vnd_ms_lrm(".lrm"),

	/**
	 * Microsoft Class Server
	 */
	application_vnd_ms_ims(".ims"),

	/**
	 * Microsoft Html Help File
	 */
	application_vnd_ms_htmlhelp(".chm"),

	/**
	 * Microsoft Embedded OpenType
	 */
	application_vnd_ms_fontobject(".eot"),

	/**
	 * Microsoft Excel - Macro-Enabled Template File
	 */
	application_vnd_ms_excel_template_macroenabled_12(".xltm"),

	/**
	 * Microsoft Excel - Macro-Enabled Workbook
	 */
	application_vnd_ms_excel_sheet_macroenabled_12(".xlsm"),

	/**
	 * Microsoft Excel - Binary Workbook
	 */
	application_vnd_ms_excel_sheet_binary_macroenabled_12(".xlsb"),

	/**
	 * Microsoft Excel - Add-In File
	 */
	application_vnd_ms_excel_addin_macroenabled_12(".xlam"),

	/**
	 * Microsoft Excel
	 */
	application_vnd_ms_excel(".xls"),

	/**
	 * Microsoft Cabinet File
	 */
	application_vnd_ms_cab_compressed(".cab"),

	/**
	 * Microsoft Artgalry
	 */
	application_vnd_ms_artgalry(".cil"),

	/**
	 * XUL - XML User Interface Language
	 */
	application_vnd_mozilla_xul_xml(".xul"),

	/**
	 * Mophun Certificate
	 */
	application_vnd_mophun_certificate(".mpc"),

	/**
	 * Mophun VM
	 */
	application_vnd_mophun_application(".mpn"),

	/**
	 * Mobius Management Systems - Topic Index File
	 */
	application_vnd_mobius_txf(".txf"),

	/**
	 * Mobius Management Systems - Policy Definition Language File
	 */
	application_vnd_mobius_plc(".plc"),

	/**
	 * Mobius Management Systems - Script Language
	 */
	application_vnd_mobius_msl(".msl"),

	/**
	 * Mobius Management Systems - Query File
	 */
	application_vnd_mobius_mqy(".mqy"),

	/**
	 * Mobius Management Systems - Basket file
	 */
	application_vnd_mobius_mbk(".mbk"),

	/**
	 * Mobius Management Systems - Distribution Database
	 */
	application_vnd_mobius_dis(".dis"),

	/**
	 * Mobius Management Systems - UniversalArchive
	 */
	application_vnd_mobius_daf(".daf"),

	/**
	 * FrameMaker Interchange Format
	 */
	application_vnd_mif(".mif"),

	/**
	 * Micrografx iGrafx Professional
	 */
	application_vnd_micrografx_igx(".igx"),

	/**
	 * Micrografx
	 */
	application_vnd_micrografx_flo(".flo"),

	/**
	 * Melody Format for Mobile Platform
	 */
	application_vnd_mfmp(".mfm"),

	/**
	 * Medical Waveform Encoding Format
	 */
	application_vnd_mfer(".mwf"),

	/**
	 * MediaRemote
	 */
	application_vnd_mediastation_cdkey(".cdkey"),

	/**
	 * MedCalc
	 */
	application_vnd_medcalcdata(".mc1"),

	/**
	 * Micro CADAM Helix D&D
	 */
	application_vnd_mcd(".mcd"),

	/**
	 * MacPorts Port System
	 */
	application_vnd_macports_portpkg(".portpkg"),

	/**
	 * Lotus Wordpro
	 */
	application_vnd_lotus_wordpro(".lwp"),

	/**
	 * Lotus Screencam
	 */
	application_vnd_lotus_screencam(".scm"),

	/**
	 * Lotus Organizer
	 */
	application_vnd_lotus_organizer(".org"),

	/**
	 * Lotus Notes
	 */
	application_vnd_lotus_notes(".nsf"),

	/**
	 * Lotus Freelance
	 */
	application_vnd_lotus_freelance(".pre"),

	/**
	 * Lotus Approach
	 */
	application_vnd_lotus_approach(".apr"),

	/**
	 * Lotus 1-2-3
	 */
	application_vnd_lotus_1_2_3("0.123"),

	/**
	 * Life Balance - Exchange Format
	 */
	application_vnd_llamagraphics_life_balance_exchange_xml(".lbe"),

	/**
	 * Life Balance - Desktop Edition
	 */
	application_vnd_llamagraphics_life_balance_desktop(".lbd"),

	/**
	 * Laser App Enterprise
	 */
	application_vnd_las_las_xml(".lasxml"),

	/**
	 * Kodak Storyshare
	 */
	application_vnd_kodak_descriptor(".sse"),

	/**
	 * SSEYO Koan Play File
	 */
	application_vnd_koan(".skp"),

	/**
	 * Kinar Applications
	 */
	application_vnd_kinar(".kne"),

	/**
	 * Kidspiration
	 */
	application_vnd_kidspiration(".kia"),

	/**
	 * Kenamea App
	 */
	application_vnd_kenameaapp(".htke"),

	/**
	 * KDE KOffice Office Suite - Kword
	 */
	application_vnd_kde_kword(".kwd"),

	/**
	 * KDE KOffice Office Suite - Kspread
	 */
	application_vnd_kde_kspread(".ksp"),

	/**
	 * KDE KOffice Office Suite - Kpresenter
	 */
	application_vnd_kde_kpresenter(".kpr"),

	/**
	 * KDE KOffice Office Suite - Kontour
	 */
	application_vnd_kde_kontour(".kon"),

	/**
	 * KDE KOffice Office Suite - Kivio
	 */
	application_vnd_kde_kivio(".flw"),

	/**
	 * KDE KOffice Office Suite - Kformula
	 */
	application_vnd_kde_kformula(".kfo"),

	/**
	 * KDE KOffice Office Suite - KChart
	 */
	application_vnd_kde_kchart(".chrt"),

	/**
	 * KDE KOffice Office Suite - Karbon
	 */
	application_vnd_kde_karbon(".karbon"),

	/**
	 * Kahootz
	 */
	application_vnd_kahootz(".ktz"),

	/**
	 * Joda Archive
	 */
	application_vnd_joost_joda_archive(".joda"),

	/**
	 * RhymBox
	 */
	application_vnd_jisp(".jisp"),

	/**
	 * Mobile Information Device Profile
	 */
	application_vnd_jcp_javame_midlet_rms(".rms"),

	/**
	 * Lightspeed Audio Lab
	 */
	application_vnd_jam(".jam"),

	/**
	 * International Society for Advancement of Cytometry
	 */
	application_vnd_isac_fcs(".fcs"),

	/**
	 * Express by Infoseek
	 */
	application_vnd_is_xpr(".xpr"),

	/**
	 * iRepository / Lucidoc Editor
	 */
	application_vnd_irepository_package_xml(".irp"),

	/**
	 * IP Unplugged Roaming Client
	 */
	application_vnd_ipunplugged_rcprofile(".rcprofile"),

	/**
	 * Quicken
	 */
	application_vnd_intu_qfx(".qfx"),

	/**
	 * Open Financial Exchange
	 */
	application_vnd_intu_qbo(".qbo"),

	/**
	 * Interactive Geometry Software
	 */
	application_vnd_intergeo(".i2g"),

	/**
	 * Intercon FormNet
	 */
	application_vnd_intercon_formnet(".xpw"),

	/**
	 * IOCOM Visimeet
	 */
	application_vnd_insors_igm(".igm"),

	/**
	 * ImmerVision PURE Players
	 */
	application_vnd_immervision_ivu(".ivu"),

	/**
	 * ImmerVision PURE Players
	 */
	application_vnd_immervision_ivp(".ivp"),

	/**
	 * igLoader
	 */
	application_vnd_igloader(".igl"),

	/**
	 * ICC profile
	 */
	application_vnd_iccprofile(".icc"),

	/**
	 * IBM Electronic Media Management System - Secure Container
	 */
	application_vnd_ibm_secure_container(".sc"),

	/**
	 * IBM DB2 Rights Manager
	 */
	application_vnd_ibm_rights_management(".irm"),

	/**
	 * MO:DCA-P
	 */
	application_vnd_ibm_modcap(".afp"),

	/**
	 * MiniPay
	 */
	application_vnd_ibm_minipay(".mpy"),

	/**
	 * 3D Crossword Plugin
	 */
	application_vnd_hzn_3d_crossword(".x3d"),

	/**
	 * Hydrostatix Master Suite
	 */
	application_vnd_hydrostatix_sof_data(".sfd-hdstx"),

	/**
	 * PCL 6 Enhanced (Formely PCL XL)
	 */
	application_vnd_hp_pclxl(".pclxl"),

	/**
	 * HP Printer Command Language
	 */
	application_vnd_hp_pcl(".pcl"),

	/**
	 * HP Indigo Digital Press - Job Layout Languate
	 */
	application_vnd_hp_jlyt(".jlt"),

	/**
	 * Hewlett-Packard's WebPrintSmart
	 */
	application_vnd_hp_hps(".hps"),

	/**
	 * Hewlett Packard Instant Delivery
	 */
	application_vnd_hp_hpid(".hpid"),

	/**
	 * HP-GL/2 and HP RTL
	 */
	application_vnd_hp_hpgl(".hpgl"),

	/**
	 * Archipelago Lesson Player
	 */
	application_vnd_hhe_lesson_player(".les"),

	/**
	 * Homebanking Computer Interface (HBCI)
	 */
	application_vnd_hbci(".hbci"),

	/**
	 * ZVUE Media Manager
	 */
	application_vnd_handheld_entertainment_xml(".zmm"),

	/**
	 * Hypertext Application Language
	 */
	application_vnd_hal_xml(".hal"),

	/**
	 * Groove - Vcard
	 */
	application_vnd_groove_vcard(".vcg"),

	/**
	 * Groove - Tool Template
	 */
	application_vnd_groove_tool_template(".tpl"),

	/**
	 * Groove - Tool Message
	 */
	application_vnd_groove_tool_message(".gtm"),

	/**
	 * Groove - Injector
	 */
	application_vnd_groove_injector(".grv"),

	/**
	 * Groove - Identity Message
	 */
	application_vnd_groove_identity_message(".gim"),

	/**
	 * Groove - Help
	 */
	application_vnd_groove_help(".ghf"),

	/**
	 * Groove - Account
	 */
	application_vnd_groove_account(".gac"),

	/**
	 * GrafEq
	 */
	application_vnd_grafeq(".gqf"),

	/**
	 * Google Earth - Zipped KML
	 */
	application_vnd_google_earth_kmz(".kmz"),

	/**
	 * Google Earth - KML
	 */
	application_vnd_google_earth_kml_xml(".kml"),

	/**
	 * GameMaker ActiveX
	 */
	application_vnd_gmx(".gmx"),

	/**
	 * GeospacW
	 */
	application_vnd_geospace(".g3w"),

	/**
	 * GeoplanW
	 */
	application_vnd_geoplan(".g2w"),

	/**
	 * GEONExT and JSXGraph
	 */
	application_vnd_geonext(".gxt"),

	/**
	 * GeoMetry Explorer
	 */
	application_vnd_geometry_explorer(".gex"),

	/**
	 * GeoGebra
	 */
	application_vnd_geogebra_tool(".ggt"),

	/**
	 * GeoGebra
	 */
	application_vnd_geogebra_file(".ggb"),

	/**
	 * Genomatix Tuxedo Framework
	 */
	application_vnd_genomatix_tuxedo(".txd"),

	/**
	 * FuzzySheet
	 */
	application_vnd_fuzzysheet(".fzs"),

	/**
	 * Fujitsu - Xerox DocuWorks Binder
	 */
	application_vnd_fujixerox_docuworks_binder(".xbd"),

	/**
	 * Fujitsu - Xerox DocuWorks
	 */
	application_vnd_fujixerox_docuworks(".xdw"),

	/**
	 * Fujitsu - Xerox 2D CAD Data
	 */
	application_vnd_fujixerox_ddd(".ddd"),

	/**
	 * Fujitsu Oasys
	 */
	application_vnd_fujitsu_oasysprs(".bh2"),

	/**
	 * Fujitsu Oasys
	 */
	application_vnd_fujitsu_oasysgp(".fg5"),

	/**
	 * Fujitsu Oasys
	 */
	application_vnd_fujitsu_oasys3(".oa3"),

	/**
	 * Fujitsu Oasys
	 */
	application_vnd_fujitsu_oasys2(".oa2"),

	/**
	 * Fujitsu Oasys
	 */
	application_vnd_fujitsu_oasys(".oas"),

	/**
	 * Friendly Software Corporation
	 */
	application_vnd_fsc_weblaunch(".fsc"),

	/**
	 * Frogans Player
	 */
	application_vnd_frogans_ltf(".ltf"),

	/**
	 * Frogans Player
	 */
	application_vnd_frogans_fnc(".fnc"),

	/**
	 * FrameMaker Normal Format
	 */
	application_vnd_framemaker(".fm"),

	/**
	 * FluxTime Clip
	 */
	application_vnd_fluxtime_clip(".ftc"),

	/**
	 * NpGraphIt
	 */
	application_vnd_flographit(".gph"),

	/**
	 * Digital Siesmograph Networks - SEED Datafiles
	 */
	application_vnd_fdsn_seed(".seed"),

	/**
	 * Forms Data Format
	 */
	application_vnd_fdf(".fdf"),

	/**
	 * EZPix Secure Photo Album
	 */
	application_vnd_ezpix_package(".ez3"),

	/**
	 * EZPix Secure Photo Album
	 */
	application_vnd_ezpix_album(".ez2"),

	/**
	 * MICROSEC e-Szign¢
	 */
	application_vnd_eszigno3_xml(".es3"),

	/**
	 * QUASS Stream Player
	 */
	application_vnd_epson_ssf(".ssf"),

	/**
	 * SimpleAnimeLite Player
	 */
	application_vnd_epson_salt(".slt"),

	/**
	 * QuickAnime Player
	 */
	application_vnd_epson_quickanime(".qam"),

	/**
	 * QUASS Stream Player
	 */
	application_vnd_epson_msf(".msf"),

	/**
	 * QUASS Stream Player
	 */
	application_vnd_epson_esf(".esf"),

	/**
	 * Enliven Viewer
	 */
	application_vnd_enliven(".nml"),

	/**
	 * EcoWin Chart
	 */
	application_vnd_ecowin_chart(".mag"),

	/**
	 * DynaGeo
	 */
	application_vnd_dynageo(".geo"),

	/**
	 * Digital Video Broadcasting
	 */
	application_vnd_dvb_service(".svc"),

	/**
	 * Digital Video Broadcasting
	 */
	application_vnd_dvb_ait(".ait"),

	/**
	 * DreamFactory
	 */
	application_vnd_dreamfactory(".dfac"),

	/**
	 * DPGraph
	 */
	application_vnd_dpgraph(".dpg"),

	/**
	 * Dolby Meridian Lossless Packing
	 */
	application_vnd_dolby_mlp(".mlp"),

	/**
	 * New Moon Liftoff/DNA
	 */
	application_vnd_dna(".dna"),

	/**
	 * FCS Express Layout Link
	 */
	application_vnd_denovo_fcselayout_link(".fe_launch"),

	/**
	 * RemoteDocs R-Viewer
	 */
	application_vnd_data_vision_rdz(".rdz"),

	/**
	 * CURL Applet
	 */
	application_vnd_curl_pcurl(".pcurl"),

	/**
	 * CURL Applet
	 */
	application_vnd_curl_car(".car"),

	/**
	 * Adobe PostScript Printer Description File Format
	 */
	application_vnd_cups_ppd(".ppd"),

	/**
	 * PosML
	 */
	application_vnd_ctc_posml(".pml"),

	/**
	 * Critical Tools - PERT Chart EXPERT
	 */
	application_vnd_criticaltools_wbs_xml(".wbs"),

	/**
	 * CrickSoftware - Clicker - Wordbank
	 */
	application_vnd_crick_clicker_wordbank(".clkw"),

	/**
	 * CrickSoftware - Clicker - Template
	 */
	application_vnd_crick_clicker_template(".clkt"),

	/**
	 * CrickSoftware - Clicker - Palette
	 */
	application_vnd_crick_clicker_palette(".clkp"),

	/**
	 * CrickSoftware - Clicker - Keyboard
	 */
	application_vnd_crick_clicker_keyboard(".clkk"),

	/**
	 * CrickSoftware - Clicker
	 */
	application_vnd_crick_clicker(".clkx"),

	/**
	 * CosmoCaller
	 */
	application_vnd_cosmocaller(".cmc"),

	/**
	 * CIM Database
	 */
	application_vnd_contact_cmsg(".cdbcmsg"),

	/**
	 * Sixth Floor Media - CommonSpace
	 */
	application_vnd_commonspace(".csp"),

	/**
	 * ClueTrust CartoMobile - Config Package
	 */
	application_vnd_cluetrust_cartomobile_config_pkg(".c11amz"),

	/**
	 * ClueTrust CartoMobile - Config
	 */
	application_vnd_cluetrust_cartomobile_config(".c11amc"),

	/**
	 * Clonk Game
	 */
	application_vnd_clonk_c4group(".c4g"),

	/**
	 * RetroPlatform Player
	 */
	application_vnd_cloanto_rp9(".rp9"),

	/**
	 * Claymore Data Files
	 */
	application_vnd_claymore(".cla"),

	/**
	 * Interactive Geometry Software Cinderella
	 */
	application_vnd_cinderella(".cdy"),

	/**
	 * Karaoke on Chipnuts Chipsets
	 */
	application_vnd_chipnuts_karaoke_mmd(".mmd"),

	/**
	 * CambridgeSoft Chem Draw
	 */
	application_vnd_chemdraw_xml(".cdxml"),

	/**
	 * BusinessObjects
	 */
	application_vnd_businessobjects(".rep"),

	/**
	 * BMI Drawing Data Interchange
	 */
	application_vnd_bmi(".bmi"),

	/**
	 * Blueice Research Multipass
	 */
	application_vnd_blueice_multipass(".mpm"),

	/**
	 * Audiograph
	 */
	application_vnd_audiograph(".aep"),

	/**
	 * Arista Networks Software Image
	 */
	application_vnd_aristanetworks_swi(".swi"),

	/**
	 * Multimedia Playlist Unicode
	 */
	application_vnd_apple_mpegurl(".m3u8"),

	/**
	 * Apple Installer Package
	 */
	application_vnd_apple_installer_xml(".mpkg"),

	/**
	 * Antix Game Player
	 */
	application_vnd_antix_game_component(".atx"),

	/**
	 * ANSER-WEB Terminal Client - Web Funds Transfer
	 */
	application_vnd_anser_web_funds_transfer_initiation(".fti"),

	/**
	 * ANSER-WEB Terminal Client - Certificate Issue
	 */
	application_vnd_anser_web_certificate_issue_initiation(".cii"),

	/**
	 * Android Package Archive
	 */
	application_vnd_android_package_archive(".apk"),

	/**
	 * AmigaDE
	 */
	application_vnd_amiga_ami(".ami"),

	/**
	 * Active Content Compression
	 */
	application_vnd_americandynamics_acc(".acc"),

	/**
	 * Amazon Kindle eBook format
	 */
	application_vnd_amazon_ebook(".azw"),

	/**
	 * AirZip FileSECURE
	 */
	application_vnd_airzip_filesecure_azs(".azs"),

	/**
	 * AirZip FileSECURE
	 */
	application_vnd_airzip_filesecure_azf(".azf"),

	/**
	 * Ahead AIR Application
	 */
	application_vnd_ahead_space(".ahead"),

	/**
	 * Adobe XML Forms Data Format
	 */
	application_vnd_adobe_xfdf(".xfdf"),

	/**
	 * Adobe XML Data Package
	 */
	application_vnd_adobe_xdp_xml(".xdp"),

	/**
	 * Adobe Flex Project
	 */
	application_vnd_adobe_fxp(".fxp"),

	/**
	 * Adobe AIR Application
	 */
	application_vnd_adobe_air_application_installer_package_zip(".air"),

	/**
	 * ACU Cobol
	 */
	application_vnd_acucorp(".atc"),

	/**
	 * ACU Cobol
	 */
	application_vnd_acucobol(".acu"),

	/**
	 * Simply Accounting - Data Import
	 */
	application_vnd_accpac_simply_imp(".imp"),

	/**
	 * Simply Accounting
	 */
	application_vnd_accpac_simply_aso(".aso"),

	/**
	 * 3M Post It Notes
	 */
	application_vnd_3m_post_it_notes(".pwn"),

	/**
	 * 3rd Generation Partnership Project - Transaction Capabilities Application Part
	 */
	application_vnd_3gpp2_tcap(".tcap"),

	/**
	 * 3rd Generation Partnership Project - Pic Var
	 */
	application_vnd_3gpp_pic_bw_var(".pvb"),

	/**
	 * 3rd Generation Partnership Project - Pic Small
	 */
	application_vnd_3gpp_pic_bw_small(".psb"),

	/**
	 * 3rd Generation Partnership Project - Pic Large
	 */
	application_vnd_3gpp_pic_bw_large(".plb"),

	/**
	 * Time Stamped Data Envelope
	 */
	application_timestamped_data(".tsd"),

	/**
	 * Sharing Transaction Fraud Data
	 */
	application_thraud_xml(".tfi"),

	/**
	 * Text Encoding and Interchange
	 */
	application_tei_xml(".tei"),

	/**
	 * Speech Synthesis Markup Language
	 */
	application_ssml_xml(".ssml"),

	/**
	 * Search/Retrieve via URL Response Format
	 */
	application_sru_xml(".sru"),

	/**
	 * Speech Recognition Grammar Specification - XML
	 */
	application_srgs_xml(".grxml"),

	/**
	 * Speech Recognition Grammar Specification
	 */
	application_srgs(".gram"),

	/**
	 * SPARQL - Results
	 */
	application_sparql_results_xml(".srx"),

	/**
	 * SPARQL - Query
	 */
	application_sparql_query(".rq"),

	/**
	 * Synchronized Multimedia Integration Language
	 */
	application_smil_xml(".smi"),

	/**
	 * S Hexdump Format
	 */
	application_shf_xml(".shf"),

	/**
	 * Secure Electronic Transaction - Registration
	 */
	application_set_registration_initiation(".setreg"),

	/**
	 * Secure Electronic Transaction - Payment
	 */
	application_set_payment_initiation(".setpay"),

	/**
	 * Session Description Protocol
	 */
	application_sdp(".sdp"),

	/**
	 * Server-Based Certificate Validation Protocol - Validation Policies - Response
	 */
	application_scvp_vp_response(".spp"),

	/**
	 * Server-Based Certificate Validation Protocol - Validation Policies - Request
	 */
	application_scvp_vp_request(".spq"),

	/**
	 * Server-Based Certificate Validation Protocol - Validation Response
	 */
	application_scvp_cv_response(".scs"),

	/**
	 * Server-Based Certificate Validation Protocol - Validation Request
	 */
	application_scvp_cv_request(".scq"),

	/**
	 * Systems Biology Markup Language
	 */
	application_sbml_xml(".sbml"),

	/**
	 * Rich Text Format
	 */
	application_rtf(".rtf"),

	/**
	 * RSS - Really Simple Syndication
	 */
	application_rss_xml(".xml"),

	/**
	 * Really Simple Discovery
	 */
	application_rsd_xml(".rsd"),

	/**
	 * XML Resource Lists
	 */
	application_rls_services_xml(".rs"),

	/**
	 * XML Resource Lists Diff
	 */
	application_resource_lists_diff_xml(".rld"),

	/**
	 * XML Resource Lists
	 */
	application_resource_lists_xml(".rl"),

	/**
	 * Relax NG Compact Syntax
	 */
	application_relax_ng_compact_syntax(".rnc"),

	/**
	 * IMS Networks
	 */
	application_reginfo_xml(".rif"),

	/**
	 * Resource Description Framework
	 */
	application_rdf_xml(".rdf"),

	/**
	 * Portable Symmetric Key Container
	 */
	application_pskc_xml(".pskcxml"),

	/**
	 * CU-Writer
	 */
	application_prs_cww(".cww"),

	/**
	 * PostScript
	 */
	application_postscript(".ai"),

	/**
	 * Pronunciation Lexicon Specification
	 */
	application_pls_xml(".pls"),

	/**
	 * Internet Public Key Infrastructure - Certificate Management Protocole
	 */
	application_pkixcmp(".pki"),

	/**
	 * Internet Public Key Infrastructure - Certification Path
	 */
	application_pkix_pkipath(".pkipath"),

	/**
	 * Internet Public Key Infrastructure - Certificate Revocation Lists
	 */
	application_pkix_crl(".crl"),

	/**
	 * Internet Public Key Infrastructure - Certificate
	 */
	application_pkix_cert(".cer"),

	/**
	 * Attribute Certificate
	 */
	application_pkix_attr_cert(".ac"),

	/**
	 * PKCS #8 - Private-Key Information Syntax Standard
	 */
	application_pkcs8(".p8"),

	/**
	 * PKCS #7 - Cryptographic Message Syntax Standard
	 */
	application_pkcs7_signature(".p7s"),

	/**
	 * PKCS #7 - Cryptographic Message Syntax Standard
	 */
	application_pkcs7_mime(".p7m"),

	/**
	 * PKCS #10 - Certification Request Standard
	 */
	application_pkcs10(".p10"),

	/**
	 * PICSRules
	 */
	application_pics_rules(".prf"),

	/**
	 * Pretty Good Privacy - Signature
	 */
	application_pgp_signature(".pgp"),

	/**
	 * Pretty Good Privacy
	 */
	application_pgp_encrypted(".pgp"),

	/**
	 * Adobe Portable Document Format
	 */
	application_pdf(".pdf"),

	/**
	 * XML Patch Framework
	 */
	application_patch_ops_error_xml(".xer"),

	/**
	 * Microsoft OneNote
	 */
	application_onenote(".onetoc"),

	/**
	 * Ogg
	 */
	application_ogg(".ogx"),

	/**
	 * Open eBook Publication Structure
	 */
	application_oebps_package_xml(".opf"),

	/**
	 * Office Document Architecture
	 */
	application_oda(".oda"),

	/**
	 * Binary Data
	 */
	application_octet_stream(".bin"),

	/**
	 * Material Exchange Format
	 */
	application_mxf(".mxf"),

	/**
	 * Microsoft Word
	 */
	application_msword(".doc"),

	/**
	 * MPEG4
	 */
	application_mp4(".mp4"),

	/**
	 * MPEG-21
	 */
	application_mp21(".m21"),

	/**
	 * Metadata Object Description Schema
	 */
	application_mods_xml(".mods"),

	/**
	 * Metadata Encoding and Transmission Standard
	 */
	application_mets_xml(".mets"),

	/**
	 * Metalink
	 */
	application_metalink4_xml(".meta4"),

	/**
	 * Media Server Control Markup Language
	 */
	application_mediaservercontrol_xml(".mscml"),

	/**
	 * Mbox database files
	 */
	application_mbox(".mbox"),

	/**
	 * Mathematical Markup Language
	 */
	application_mathml_xml(".mathml"),

	/**
	 * Mathematica Notebooks
	 */
	application_mathematica(".ma"),

	/**
	 * MARC21 XML Schema
	 */
	application_marcxml_xml(".mrcx"),

	/**
	 * MARC Formats
	 */
	application_marc(".mrc"),

	/**
	 * Metadata Authority Description Schema
	 */
	application_mads_xml(".mads"),

	/**
	 * Compact Pro
	 */
	application_mac_compactpro(".cpt"),

	/**
	 * Macintosh BinHex 4.0
	 */
	application_mac_binhex40(".hqx"),

	/**
	 * JavaScript Object Notation (JSON)
	 */
	application_json(".json"),

	/**
	 * JavaScript
	 */
	application_javascript(".js"),

	/**
	 * Java Bytecode File
	 */
	application_java_vm(".class"),

	/**
	 * Java Serialized Object
	 */
	application_java_serialized_object(".ser"),

	/**
	 * Java Archive
	 */
	application_java_archive(".jar"),

	/**
	 * Internet Protocol Flow Information Export
	 */
	application_ipfix(".ipfix"),

	/**
	 * Hyperstudio
	 */
	application_hyperstudio(".stk"),

	/**
	 * Portable Font Resource
	 */
	application_font_tdpfr(".pfr"),

	/**
	 * Efficient XML Interchange
	 */
	application_exi(".exi"),

	/**
	 * Electronic Publication
	 */
	application_epub_zip(".epub"),

	/**
	 * Extensible MultiModal Annotation
	 */
	application_emma_xml(".emma"),

	/**
	 * ECMAScript
	 */
	application_ecmascript(".es"),

	/**
	 * Data Structure for the Security Suitability of Cryptographic Algorithms
	 */
	application_dssc_xml(".xdssc"),

	/**
	 * Data Structure for the Security Suitability of Cryptographic Algorithms
	 */
	application_dssc_der(".dssc"),

	/**
	 * Web Distributed Authoring and Versioning
	 */
	application_davmount_xml(".davmount"),

	/**
	 * CU-SeeMe
	 */
	application_cu_seeme(".cu"),

	/**
	 * Cloud Data Management Interface (CDMI) - Queue
	 */
	application_cdmi_queue(".cdmiq"),

	/**
	 * Cloud Data Management Interface (CDMI) - Object
	 */
	application_cdmi_object(".cdmio"),

	/**
	 * Cloud Data Management Interface (CDMI) - Domain
	 */
	application_cdmi_domain(".cdmid"),

	/**
	 * Cloud Data Management Interface (CDMI) - Contaimer
	 */
	application_cdmi_container(".cdmic"),

	/**
	 * Cloud Data Management Interface (CDMI) - Capability
	 */
	application_cdmi_capability(".cdmia"),

	/**
	 * Voice Browser Call Control
	 */
	application_ccxml_xml(".ccxml"),

	/**
	 * Atom Publishing Protocol Service Document
	 */
	application_atomsvc_xml(".atomsvc"),

	/**
	 * Atom Publishing Protocol
	 */
	application_atomcat_xml(".atomcat"),

	/**
	 * Atom Syndication Format
	 */
	application_atom_xml(".xml"),

	/**
	 * Applixware
	 */
	application_applixware(".aw");

	private final String value;

	MimeType(String value) {
		this.value = value;
	}

	/**
	 * 响应头content-type对应的MIME类型
	 *
	 * @param mimeType MIME类型
	 * @return MIME类型对应的文件后缀
	 */
	public static String getMimeSuffix(String mimeType) {
		return getMimeType(mimeType).getValue();
	}

	/**
	 * 响应头content-type对应的MIME类型
	 *
	 * @param mimeType MIME类型
	 * @return 对应的MIME类型
	 */
	public static MimeType getMimeType(String mimeType) {
		return MimeType.valueOf((mimeType.contains(Symbol.SEMICOLON) ? mimeType.substring(0, mimeType.indexOf(Symbol.SEMICOLON)) : mimeType).replaceAll("[-/+.]", "_"));
	}

	/**
	 * 响应头content-type对应的MIME类型
	 *
	 * @return MIME的值
	 */
	public final String getValue() {
		return value;
	}

}