package goodshi.ageofquizz.service;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Iterator;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class UploadService {

	public String uploadImage(MultipartFile file, String path, int width, int height, float quality)
			throws IOException {
		validateFile(file);

		String fileName = file.getOriginalFilename();
		String extension = getFileExtension(fileName);

		BufferedImage originalImage = ImageIO.read(file.getInputStream());
		if (originalImage == null) {
			throw new IOException("Le fichier n'est pas une image valide.");
		}

		BufferedImage resizedImage = resizeImage(originalImage, width, height);
		File directory = ensureDirectory(path);
		File destination = new File(directory, fileName);

		saveImageWithCompression(resizedImage, destination, extension, quality);
		return destination.getAbsolutePath();
	}

	public String uploadAudio(MultipartFile file, String path) throws IOException {
		validateAudioFile(file);

		File directory = ensureDirectory(path);
		String fileName = file.getOriginalFilename();
		File destination = new File(directory, fileName);

		Files.copy(file.getInputStream(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
		return destination.getAbsolutePath();
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

	private File ensureDirectory(String path) throws IOException {
		File directory = new File(path);
		if (!directory.exists() && !directory.mkdirs()) {
			throw new IOException("Impossible de créer le dossier : " + path);
		}
		return directory;
	}

	private void saveImageWithCompression(BufferedImage image, File dest, String extension, float quality)
			throws IOException {

		if (extension.equalsIgnoreCase("jpg") || extension.equalsIgnoreCase("jpeg")) {
			try (OutputStream os = new FileOutputStream(dest)) {
				Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpeg");
				if (!writers.hasNext()) {
					throw new IllegalStateException("Aucun writer pour JPEG trouvé");
				}

				ImageWriter writer = writers.next();
				ImageOutputStream ios = ImageIO.createImageOutputStream(os);
				writer.setOutput(ios);

				ImageWriteParam param = writer.getDefaultWriteParam();
				if (param.canWriteCompressed()) {
					param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
					param.setCompressionQuality(quality);
				}

				writer.write(null, new IIOImage(image, null, null), param);
				ios.close();
				writer.dispose();
			}
		} else {
			ImageIO.write(image, extension, dest);
		}
	}

	public String uploadImage(MultipartFile file) throws IOException {
		String defaultPath = "uploads/screenshots";
		int defaultWidth = 800;
		int defaultHeight = 600;
		float defaultQuality = 0.8f;

		return uploadImage(file, defaultPath, defaultWidth, defaultHeight, defaultQuality);
	}

	private boolean hasImageExtension(String fileName) {
		return fileName != null && (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") || fileName.endsWith(".png")
				|| fileName.endsWith(".gif"));
	}

	private String getFileExtension(String fileName) {
		if (fileName != null && fileName.contains(".")) {
			return fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
		}
		return "jpg"; // par défaut
	}
}
