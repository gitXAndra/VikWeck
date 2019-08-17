package vikweck.main;

import java.io.File;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.webkit.MimeTypeMap;

/** Conversion between content and file uris and other stuff.*/
public class AudioFilesHelper {
	
	/** Checks whether the input uri is media content uri. 
	 * 	Check might fail with uris created from a String, which need not start with "content://media" to be valid.*/
	public static boolean isContentUri (Uri uri) {
		return uri.toString().startsWith("content://media");
	}
	
	/** Returns an actual file uri.*/
	public static Uri getFileUri ( Context context, Uri contentUri ) {
		//get actual uri from media database
		Uri fileUri = null;
		
		String[] returnColumns = { MediaStore.Audio.Media.DATA };
		Cursor dataReturned = context.getContentResolver().query(contentUri, returnColumns, null, null, null );
		int column_index = dataReturned.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
		if( dataReturned.moveToFirst() )
		{
			fileUri = Uri.parse(dataReturned.getString(column_index));
		}
		dataReturned.close();
		
		return fileUri;
	}
	
	/** Checks existence of file the uri is pointing to.*/
	public static boolean doesFileExist ( Context context, Uri uri ) {
		
		if( isContentUri(uri) )
		{
			uri = getFileUri(context, uri);
		}
		
		if( uri != null )
		{
			File file = new File( uri.getPath() );
			return file.exists();
		}
		else
		{
			return false;
		}
	}
	
	/** 
	 * Returns a name for the Audio File.
	 * Context may be null if uri is file uri, where the title will be the filename without the file extension.
	 */
	public static String getAudioTitle ( Context context, Uri uri ) {
		String title = "";
		
		if( isContentUri(uri) )
		{
			String[] what = new String[] {MediaStore.MediaColumns.TITLE};
			Cursor contentResolverCursor = context.getContentResolver().query(uri, what, null, null, null );
			if(  contentResolverCursor != null  &&  contentResolverCursor.getCount() != 0  )
			{
				contentResolverCursor.moveToFirst();
				title = contentResolverCursor.getString(contentResolverCursor.getColumnIndex("title"));
			}
			contentResolverCursor.close();
		}
		else
		{
			String fileName = uri.getLastPathSegment();
			title = fileName.substring( 0 , fileName.lastIndexOf('.'));
		}
		
		return title;
	}
	
	/** Returns Mime Type of file with fileName based on its file extension.*/
	public static String getMimeType (String fileName) {
		String fileExtension = fileName.substring( fileName.lastIndexOf('.')+1 );
		MimeTypeMap mimeMap = MimeTypeMap.getSingleton();
		return mimeMap.getMimeTypeFromExtension(fileExtension);
	}
	
	/** Determines based on mime type if file is audio.*/
	public static boolean isAudio ( File file ) {
		String fileName = file.getName();
		String fileMimeType = AudioFilesHelper.getMimeType(fileName);
		return fileMimeType.startsWith("audio/");
	}
	
	/** Returns content uri if file is registered in the media content provider.*/
	public static Uri getContentUri ( Context context, Uri fileUri, String musicTitle ) {
		Uri contentUri = null;
		
		Uri internalContentUri = MediaStore.Audio.Media.INTERNAL_CONTENT_URI;
		String[] returnColumns = { MediaStore.Audio.Media.DATA, MediaStore.Audio.Media._ID };
		String where = MediaStore.MediaColumns.TITLE+" = ? ";
		String[] whereArg = new String[] {musicTitle};
		
		Cursor dataReturned = context.getContentResolver().query( internalContentUri, returnColumns, where, whereArg, null );
		int data_column_index = dataReturned.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
		int id_column_index = dataReturned.getColumnIndexOrThrow(MediaStore.Audio.Media._ID);
		if( dataReturned != null && dataReturned.moveToFirst() )
		{
			Uri dataRetFileUri = Uri.parse(dataReturned.getString(data_column_index));
			if( dataRetFileUri.getPath().compareTo(fileUri.getPath()) == 0 )
			{
				int id = dataReturned.getInt(id_column_index);
				
				contentUri = Uri.withAppendedPath( internalContentUri, String.valueOf( id ));
			}
		}
		dataReturned.close();
		return contentUri;
	}
	
	public static String printAlarmMediaContentToString ( Context context ) {
		String soundsTxt = "";
		
		String where = MediaStore.Audio.Media.IS_ALARM+" = ? ";
		String[] whereArg = new String[] {"1"};
		
		Uri contentUri = MediaStore.Audio.Media.INTERNAL_CONTENT_URI;
		Cursor contentResolverCursor = context.getContentResolver().query(contentUri, null, where, whereArg, null );
		
		if(  contentResolverCursor != null  &&  contentResolverCursor.moveToFirst() )
		{
			soundsTxt += "\n- internal media content/ alarm sounds:";
			do
			{
				soundsTxt += "\n	- "+contentResolverCursor.getString(contentResolverCursor.getColumnIndex( MediaStore.MediaColumns.TITLE ));
				soundsTxt += ", is alarm = "+contentResolverCursor.getInt(contentResolverCursor.getColumnIndex( MediaStore.Audio.Media.IS_ALARM ));
				soundsTxt += ", id = "+contentResolverCursor.getInt(contentResolverCursor.getColumnIndex( MediaStore.MediaColumns._ID ));
			}while( contentResolverCursor.moveToNext() );
		}
		else
		{
			soundsTxt += "- no content available";
		}
		contentResolverCursor.close();
		
		
		contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
		Cursor contentResolverCursor2 = context.getContentResolver().query(contentUri, null, where, whereArg, null );
		
		if(  contentResolverCursor2 != null  &&  contentResolverCursor2.moveToFirst() )
		{
			soundsTxt += "\n- external media content/ alarm sounds:";
			do
			{
				soundsTxt += "\n	- "+contentResolverCursor2.getString(contentResolverCursor2.getColumnIndex( MediaStore.MediaColumns.TITLE ));
				soundsTxt += ", is alarm = "+contentResolverCursor2.getInt(contentResolverCursor2.getColumnIndex( MediaStore.Audio.Media.IS_ALARM ));
				soundsTxt += ", id = "+contentResolverCursor2.getInt(contentResolverCursor2.getColumnIndex( MediaStore.MediaColumns._ID ));
			}while( contentResolverCursor2.moveToNext() );
		}
		else
		{
			soundsTxt += "- no content available";
		}
		contentResolverCursor2.close();
		
		return soundsTxt;
	}
}
