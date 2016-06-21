package edu.stevens.cs522.capture.util;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Parcel;
import android.text.Editable;
import android.util.Log;
import android.widget.EditText;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.stevens.cs522.capture.DecodeHintType;

public class StringUtils {
	
	private static final String TAG = StringUtils.class.getCanonicalName();

	public static final String SHIFT_JIS = "SJIS";
	public static final String GB2312 = "GB2312";

	public static String guessEncoding(byte[] bytes, Map<DecodeHintType,?> hints) {
		return "UTF-8";
	}

	/*
	 * Byte arrays and character set encoding
	 */
	
	public static final String CHARSET = "UTF-8";
	
	public static String toString(byte[] b) {
		return toString(b, b.length);
	}
	
	public static String toString(byte[] b, int len) {
		try {
			return new String(b, 0, len, CHARSET);
		} catch (UnsupportedEncodingException e) {
			Log.e(TAG, "Unsupported UTF-8 encoding!", e);
			return null;
		}
	}
	
	public static char[] toCharArray(byte[] b) {
		return toCharArray(b, b.length);
	}
	
	public static char[] toCharArray(byte[] b, int len) {
//		TODO create a char array without an intermediate string?
//		Charset charset = Charset.forName(CHARSET);
//		return charset.decode(ByteBuffer.wrap(b, 0, len)).array();
		return toString(b, len).toCharArray();
	}
	
	public static char[] readPassword(Parcel in) {
		return in.createCharArray();
	}
	
	public static void writePassword(Parcel out, char[] password) {
		out.writeCharArray(password);
	}
	
	public static char[] readPassword(DataInputStream in) throws IOException {
		byte[] b = new byte[in.readInt()];
		in.read(b);
		char[] c = toCharArray(b);
		Arrays.fill(b, (byte)0);
		return c;
	}
	
	public static void writePassword(DataOutputStream out, char[] password) throws IOException {
		byte[] b = toBytes(password);
		out.writeInt(b.length);
		out.write(b);
		Arrays.fill(b, (byte)0);
	}
	
	public static char[] getPassword(Editable editable) {
		char[] b = new char[editable.length()];
		editable.getChars(0, editable.length(), b, 0);
		return b;
	}
	
	public static byte[] toBytes(String s) {
		try {
			return s.getBytes(CHARSET);
		} catch (UnsupportedEncodingException e) {
			Log.e(TAG, "Unsupported encoding: "+CHARSET, e);
			return null;
		}
	}
	
	public static byte[] toBytes(char[] s) {
		return toBytes(s, s.length);
	}
	
	public static byte[] toBytes(char[] s, int len) {
//		Charset charset = Charset.forName(CHARSET);
//		return charset.encode(CharBuffer.wrap(s, 0, len)).array();
		return toBytes(new String(s));
	}
	
	public static int lookup(String key, String[] keys) {
		for (int i=0; i< keys.length; i++) {
			if (key.equals(keys[i])) {
				return i;
			}
		}
		return -1;
	}
	
	public static int lookup(String key, List<String> keys) {
		int length = keys.size();
		for (int i=0; i< length; i++) {
			if (key.equals(keys.get(i))) {
				return i;
			}
		}
		return -1;
	}
	
	public static boolean contains(List<String> keys, String key) {
		return lookup(key, keys) >= 0;
	}
	
	
	
	/*
	 * Uris
	 */
	
	public static final Uri EMPTY_URI;
	
	static {
		EMPTY_URI = Uri.parse("#");
	}

	public static Uri getNullableUri(String value) {
		if (value == null) {
			return null;
		} else {
			return Uri.parse(value);
		}
	}

	public static Uri readNullableUri(Parcel in) {
		if (in.readByte() == 1) {
			return Uri.parse(in.readString());
		} else {
			return null;
		}
	}
	
	public static void writeNullableUri(Parcel out, Uri u) {
		if (u == null) {
			out.writeByte((byte)0);
		} else {
			out.writeByte((byte)1);
			out.writeString(u.toString());
		}
	}
	
	public static Uri readNullableUri(DataInputStream in) throws IOException {
		if (in.readBoolean()) {
			return Uri.parse(in.readUTF());
		} else {
			return null;
		}
	}
	
	public static void writeNullableUri(DataOutputStream out, Uri u) throws IOException {
		if (u == null) {
			out.writeBoolean(false);
		} else {
			out.writeBoolean(true);
			out.writeUTF(u.toString());
		}
	}
	
	private static final String URN_PREFIX = "urn:uuid:";
	private static final int URN_PREFIX_LENGTH = URN_PREFIX.length();
	
	public static Uri toUrn(UUID uuid) {
		return Uri.parse(URN_PREFIX + uuid.toString());
	}
	
	public static String fromUrn(Uri urn) {
		return urn.toString().substring(URN_PREFIX_LENGTH);
	}
	
	/*
	 * URLs
	 */

	public static boolean isValidUrl(String url) {	
	    try {
	        new URL(url);
	        return true;
	    } catch (MalformedURLException e) {
	        return false;
	    }
	}

	public static URL getNullableUrl(String value) {
		if (value == null) {
			return null;
		} else {
			try {
				return new URL(value);
			} catch (MalformedURLException e) {
				Log.e(TAG, "Malformed URL: " + value, e);
				return null;
			}
		}
	}

	public static URL readNullableUrl(Parcel in) {
		if (in.readByte() == 1) {
			String value = in.readString();
			try {
				return new URL(value);
			} catch (MalformedURLException e) {
				Log.e(TAG, "Malformed URL: " + value, e);
				return null;
			}
		} else {
			return null;
		}
	}
	
	public static void writeNullableUrl(Parcel out, URL u) {
		if (u == null) {
			out.writeByte((byte)0);
		} else {
			out.writeByte((byte)1);
			out.writeString(u.toString());
		}
	}
	
	public static URL readNullableUrl(DataInputStream in) throws IOException {
		if (in.readBoolean()) {
			String value = in.readUTF();
			try {
				return new URL(value);
			} catch (MalformedURLException e) {
				Log.e(TAG, "Malformed URL: " + value, e);
				return null;
			}
		} else {
			return null;
		}
	}
	
	public static void writeNullableUrl(DataOutputStream out, URL u) throws IOException {
		if (u == null) {
			out.writeBoolean(false);
		} else {
			out.writeBoolean(true);
			out.writeUTF(u.toString());
		}
	}
	
	public static URL withAppendedPath(URL u, String segment) {
		try {
			return new URL(u.toString() + "/" + segment);
		} catch (MalformedURLException e) {
			Log.e(TAG, "Malformed URL exception.", e);
			return null;
		}
	}
	
	/*
	 * UUIDs
	 */
	
	public static UUID getNullableUUID(String value) {
		if (value == null) {
			return null;
		} else {
			return UUID.fromString(value);
		}
	}

	public static UUID readNullableUUID(Parcel in) {
		if (in.readByte() == 1) {
			return UUID.fromString(in.readString());
		} else {
			return null;
		}
	}
	
	public static void writeNullableUUID(Parcel out, UUID u) {
		if (u == null) {
			out.writeByte((byte)0);
		} else {
			out.writeByte((byte)1);
			out.writeString(u.toString());
		}
	}
	
	public static UUID readNullableUUID(DataInputStream in) throws IOException {
		if (in.readBoolean()) {
			return UUID.fromString(in.readUTF());
		} else {
			return null;
		}
	}
	
	public static void writeNullableUUID(DataOutputStream out, UUID u) throws IOException {
		if (u == null) {
			out.writeBoolean(false);
		} else {
			out.writeBoolean(true);
			out.writeUTF(u.toString());
		}
	}
	
	/*
	 * Blobs
	 */
	
	public static byte[] getBlob(Cursor cursor, int columnIdx) {
		if (cursor.isNull(columnIdx)) {
			return null;
		} else {
			return cursor.getBlob(columnIdx);
		}
	}
	
	public static byte[] getBlob(Cursor cursor, String columnName) {
		return getBlob(cursor, cursor.getColumnIndexOrThrow(columnName));
	}
	
	public static byte[] getBlob(ContentValues values, String columnName) {
		return values.getAsByteArray(columnName);
	}
	
	public static void putBlob(ContentValues values, String columnName, byte[] value) {
		if (value == null) {
			values.putNull(columnName);
		} else {
			values.put(columnName, value);
		}
	}
	
	public static byte[] readBlob(Parcel in) {
		return in.createByteArray();
	}
	
	public static void writeBlob(Parcel out, byte[] b) {
//		out.writeByteArray(b);
		out.writeByteArray(b, 0, b.length);
	}
	
	public static byte[] readBlob(DataInputStream in) throws IOException {
		byte[] b = new byte[in.readInt()];
		in.readFully(b, 0, b.length);
		return b;
	}
	
	public static void writeBlob(DataOutputStream out, byte[] b) throws IOException {
		out.writeInt(b.length);
		out.write(b);
	}
	
	public static byte[] getNullableBlob(Cursor c, String name) {
		int cindex = c.getColumnIndexOrThrow(name);
		if (c.isNull(cindex)) {
			return null;
		} else {
			return c.getBlob(cindex);
		}
	}

	public static byte[] getNullableBlob(ContentValues values, String columnName) {
		if (values.containsKey(columnName)) {
			return values.getAsByteArray(columnName);
		} else {
			return null;
		}
	}
	
	public static void putNullableBlob(ContentValues values, String columnName, byte[] value) {
		if (value == null) {
			values.putNull(columnName);
		} else {
			values.put(columnName, value);
		}
	}
	
	public static byte[] readNullableBlob(Parcel in) {
		if (in.readByte() == 1) {
			return in.createByteArray();
		} else {
			return null;
		}
	}
	
	public static void writeNullableBlob(Parcel out, byte[] b) {
		if (b == null) {
			out.writeByte((byte)0);
		} else {
			out.writeByte((byte)1);
			out.writeByteArray(b);
		}
	}
	
	public static byte[] readNullableBlob(DataInputStream in) throws IOException {
		if (in.readBoolean()) {
			return readBlob(in);
		} else {
			return null;
		}
	}
	
	public static void writeNullableBlob(DataOutputStream out, byte[] b) throws IOException {
		if (b == null) {
			out.writeBoolean(false);
		} else {
			out.writeBoolean(true);
			writeBlob(out, b);
		}
	}
	
	public static boolean isEqualBlobs(byte[] a, byte[] b) {
		if (a.length != b.length) {
			return false;
		}
		for (int i=0; i<a.length; i++) {
			if (a[i] != b[i]) {
				return false;
			}
		}
		return true;
	}
	
	/*
	 * For certificate chains.  
	 */
	
	public static byte[][] readChain(Parcel in) {
		int chainLength = in.readInt();
		byte[][] chain = new byte[chainLength][];
		for (int i=0; i<chainLength; i++) {
			chain[i] = readBlob(in);
		}
		return chain;
	}
	
	public static void writeChain(Parcel out, byte[][] chain) {
		out.writeInt(chain.length);
		for (int i=0; i<chain.length; i++) {
			writeBlob(out, chain[i]);
		}
	}
	
	public static byte[][] readChain(DataInputStream in) throws IOException {
		int chainLength = in.readInt();
		byte[][] chain = new byte[chainLength][];
		for (int i=0; i<chainLength; i++) {
			chain[i] = readBlob(in);
		}
		return chain;
	}
	
	public static void writeChain(DataOutputStream out, byte[][] chain) throws IOException {
		out.writeInt(chain.length);
		for (int i=0; i<chain.length; i++) {
			writeBlob(out, chain[i]);
		}
	}
	
	public static byte[][] readNullableChain(Parcel in) {
		if (in.readByte() == 0) {
			return null;
		} else {
			return readChain(in);
		}
	}
	
	public static void writeNullableChain(Parcel out, byte[][] chain) {
		if (chain == null) {
			out.writeByte((byte) 0);
		} else {
			out.writeByte((byte) 1);
			writeChain(out, chain);
		}
	}
	
	public static byte[][] readNullableChain(DataInputStream in) throws IOException {
		if (in.readByte() == 0) {
			return null;
		} else {
			return readChain(in);
		}
	}
	
	public static void writeNullableChain(DataOutputStream out, byte[][] chain) throws IOException {
		if (chain == null) {
			out.writeByte((byte) 0);
		} else {
			out.writeByte((byte) 1);
			writeChain(out, chain);
		}
	}
	
	/*
	 * Strings
	 */
	
	public static String getString(Cursor cursor, int columnIdx) {
		if (cursor.isNull(columnIdx)) {
			return null;
		} else {
			return cursor.getString(columnIdx);
		}
	}
	
	public static String getString(Cursor cursor, String columnName) {
		return getString(cursor, cursor.getColumnIndexOrThrow(columnName));
	}
	
	public static String getString(ContentValues values, String columnName) {
		if (values.containsKey(columnName)) {
			return values.getAsString(columnName);
		} else {
			return null;
		}
	}
	
	public static void putString(ContentValues values, String columnName, String value) {
		if (value == null) {
			values.putNull(columnName);
		} else {
			values.put(columnName, value);
		}
	}
	
	public static String getNullableString(Cursor cursor, String columnName) {
		return getString(cursor, columnName);
	}
	
	public static String getNullableString(ContentValues values, String columnName) {
		return getString(values, columnName);
	}
	
	public static void putNullableString(ContentValues values, String columnName, String value) {
		if (value == null) {
			values.putNull(columnName);  // Same thing?
		} else {
			values.put(columnName, value);
		}
	}
	
	public static String readNullableString(Parcel in) {
		if (in.readByte() == 1) {
			return in.readString();
		} else {
			return null;
		}
	}
	
	public static void writeNullableString(Parcel out, String s) {
		if (s == null) {
			out.writeByte((byte)0);
		} else {
			out.writeByte((byte)1);
			out.writeString(s);
		}
	}
	
	public static String readNullableString(DataInputStream in) throws IOException {
		if (in.readBoolean()) {
			return in.readUTF();
		} else {
			return null;
		}
	}
	
	public static void writeNullableString(DataOutputStream out, String s) throws IOException {
		if (s == null) {
			out.writeBoolean(false);
		} else {
			out.writeBoolean(true);
			out.writeUTF(s);
		}
	}
	
	
	/*
	 * Editables
	 */
	
	public static boolean isEmptyInput(Editable text) {
		return text.toString().trim().length() == 0;
	}
	
	public static boolean isEmptyInput(EditText box) {
		return isEmptyInput(box.getText());
	}
	
	/*
	 * Editing text templates.
	 */
	public static String readText(InputStream is) {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		is = new BufferedInputStream(is);
		byte[] buffer = new byte[1024];
		int i;
		try {
			i = is.read(buffer, 0, buffer.length);
			while (i != -1) {
				os.write(buffer, 0, i);
				i = is.read(buffer, 0, buffer.length);
			}
			is.close();
			os.flush();
			return os.toString("UTF-8");
		} catch (UnsupportedEncodingException e) {
			Log.e(TAG, "UTF-8 unsupported for consent text.");
		} catch (IOException e) {
			Log.e(TAG, "IO Exception: " + e.toString());
		}
		return "";
	}

	protected static Pattern buildMatcher(Map<String,String> replacements) {
		StringBuilder regexp = new StringBuilder();
		int ix = 0;
		for (String key : replacements.keySet()) {
			if (ix > 0) {
				regexp.append('|');
			}
			regexp.append(key);
			ix++;
		}
		return Pattern.compile(regexp.toString());
	}
	
	public static String compileText(Map<String,String> replacements, InputStream is) {
		Pattern pattern = buildMatcher(replacements);
		String template = readText(is);
		Matcher matcher = pattern.matcher(template);
		
		StringBuffer sb = new StringBuffer();
		while (matcher.find()) {
			matcher.appendReplacement(sb, replacements.get(matcher.group()));
		}
		matcher.appendTail(sb);
		return sb.toString();
	}



}
