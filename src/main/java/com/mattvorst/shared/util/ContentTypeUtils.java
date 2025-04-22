package com.mattvorst.shared.util;

public class ContentTypeUtils {
	public static final String extensionFromContentType(String contentType) {
		if(contentType == null) {
			return null;
		}

		switch (contentType.toLowerCase()) {
			case "image/jpeg":
				return "jpg";
			case "image/png":
				return "png";
			case "image/gif":
				return "gif";
			case "image/bmp":
				return "bmp";
			case "image/tiff":
				return "tiff";
			case "image/svg+xml":
				return "svg";
			case "image/webp":
				return "webp";
			case "image/x-icon":
				return "ico";
			case "image/vnd.microsoft.icon":
				return "ico";
			case "image/vnd.wap.wbmp":
				return "wbmp";
			case "image/heic":
				return "heic";
			case "image/heif":
				return "heif";
			case "image/heif-sequence":
				return "heif";
			case "image/heic-sequence":
				return "heic";
			case "image/hej2k":
				return "hej2k";
			case "image/hevc":
				return "hevc";
			default:
				return null;
		}
	}

	public static String formatFromContentType(String contentType) {
		if(contentType == null) {
			return "PNG";
		}

		switch (contentType.toLowerCase()) {
			case "image/jpg":
				return "JPG";
			case "image/jpeg":
				return "JPG";
			case "image/png":
				return "PNG";
			case "image/gif":
				return "GIF";
			default:
				return "PNG";
		}
	}
}
