package goodshi.ageofquizz.service;

import java.io.InputStream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

@Service
public class DownloadService {

	@Value("${remote.server.upload.path}")
	private String remoteServerUploadPath;

	@Value("${upload.image.path}")
	private String uploadImagePath;

	@Value("${upload.sound.path}")
	private String uploadSoundPath;

	private final SftpService sftpService;

	public DownloadService(SftpService sftpService) {
		this.sftpService = sftpService;
	}

	public Resource getImage(String filename) throws Exception {
		String remotePath = remoteServerUploadPath + uploadImagePath + filename;

		InputStream is = sftpService.download(remotePath);
		if (is == null) {
			throw new IllegalArgumentException("Image introuvable : " + filename);
		}

		return new InputStreamResource(is);
	}

	public Resource getAudio(String filename) throws Exception {
		String remotePath = remoteServerUploadPath + uploadSoundPath + filename;

		InputStream is = sftpService.download(remotePath);
		if (is == null) {
			throw new IllegalArgumentException("Audio introuvable : " + filename);
		}

		return new InputStreamResource(is);
	}

	public MediaType getMediaType(String filename) {
		String ext = filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
		return switch (ext) {
		case "jpg", "jpeg" -> MediaType.IMAGE_JPEG;
		case "png" -> MediaType.IMAGE_PNG;
		case "gif" -> MediaType.IMAGE_GIF;
		case "mp3" -> MediaType.valueOf("audio/mpeg");
		case "wav" -> MediaType.valueOf("audio/wav");
		case "ogg" -> MediaType.valueOf("audio/ogg");
		case "aac" -> MediaType.valueOf("audio/aac");
		default -> MediaType.APPLICATION_OCTET_STREAM;
		};
	}
}
