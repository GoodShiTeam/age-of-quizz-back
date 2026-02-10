package goodshi.ageofquizz.service;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import javax.imageio.ImageIO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class UploadService {

	@Value("${remote.server.upload.path}")
	private String remoteServerUploadPath;

	@Value("${upload.image.path}")
	private String uploadImagePath;

	@Value("${upload.sound.path}")
	private String uploadSoundPath;

	@Autowired
	private SftpService sftpService;

	public String uploadImage(MultipartFile file, int width, int height, float quality) throws Exception {

		validateFile(file);

		String extension = getFileExtension(file.getOriginalFilename());
		String fileName = UUID.randomUUID() + "." + extension;

		String logicalPath = uploadImagePath + fileName;
		String remotePath = remoteServerUploadPath + logicalPath;

		BufferedImage originalImage = ImageIO.read(file.getInputStream());
		if (originalImage == null) {
			throw new IOException("Le fichier n'est pas une image valide.");
		}

		BufferedImage resizedImage = (width > 0 && height > 0) ? resizeImage(originalImage, width, height)
				: originalImage;

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ImageIO.write(resizedImage, extension, baos);

		try (InputStream is = new ByteArrayInputStream(baos.toByteArray())) {
			sftpService.upload(is, remotePath);
		}

		return logicalPath;
	}

	public String uploadAudio(MultipartFile file) throws Exception {

		validateAudioFile(file);

		String extension = getFileExtension(file.getOriginalFilename());
		String fileName = UUID.randomUUID() + "." + extension;

		String logicalPath = uploadSoundPath + fileName;
		String remotePath = remoteServerUploadPath + logicalPath;

		try (InputStream is = file.getInputStream()) {
			sftpService.upload(is, remotePath);
		}

		return logicalPath; // 👈 à stocker en BDD
	}

	// --------------------
	// MÉTHODES PRIVÉES
	// --------------------

	private void validateAudioFile(MultipartFile file) throws IOException {
		if (file.isEmpty()) {
			throw new IOException("Veuillez sélectionner un fichier audio.");
		}

		String contentType = file.getContentType();
		if (contentType == null || !contentType.startsWith("audio/")) {
			throw new IOException("Le fichier doit être un fichier audio.");
		}

		String fileName = file.getOriginalFilename();
		if (!hasAudioExtension(fileName)) {
			throw new IOException("Extension audio non valide (.mp3, .wav, .ogg, .aac).");
		}
	}

	private boolean hasAudioExtension(String fileName) {
		return fileName != null && (fileName.endsWith(".mp3") || fileName.endsWith(".wav") || fileName.endsWith(".ogg")
				|| fileName.endsWith(".aac"));
	}

	// --------------------
	// MÉTHODES PRIVÉES
	// --------------------

	private void validateFile(MultipartFile file) throws IOException {
		if (file.isEmpty()) {
			throw new IOException("Veuillez sélectionner une image à télécharger.");
		}

		String contentType = file.getContentType();
		if (contentType == null || !contentType.startsWith("image/")) {
			throw new IOException("Le fichier doit être une image (JPEG, PNG, GIF).");
		}

		String fileName = file.getOriginalFilename();
		if (!hasImageExtension(fileName)) {
			throw new IOException("Extension d'image non valide (.jpg, .jpeg, .png, .gif).");
		}
	}

	private BufferedImage resizeImage(BufferedImage original, int width, int height) {
		BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = resized.createGraphics();
		g.drawImage(original, 0, 0, width, height, null);
		g.dispose();
		return resized;
	}

	private boolean hasImageExtension(String fileName) {
		return fileName != null && (fileName.endsWith(".JPG") || fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")
				|| fileName.endsWith(".png") || fileName.endsWith(".gif"));
	}

	private String getFileExtension(String fileName) {
		if (fileName != null && fileName.contains(".")) {
			return fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
		}
		return "jpg"; // par défaut
	}
}
