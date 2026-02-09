package goodshi.ageofquizz.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import goodshi.ageofquizz.service.UploadService;

@RestController
public class UploadController {

	@Autowired
	private UploadService uploadService;

	private static final String IMAGE_DIR = "questions/images/";
	private static final String AUDIO_DIR = "questions/audio/";

	@PostMapping("/upload/image")
	@PreAuthorize("hasAnyRole('AUTHOR', 'ADMIN')")
	public ResponseEntity<String> uploadImage(@RequestParam("file") MultipartFile file,
			@RequestParam("path") String path, @RequestParam(name = "width", required = false) int width,
			@RequestParam(name = "height", required = false) int height,
			@RequestParam(name = "quality", defaultValue = "0.8") float quality) {

		try {
			String imagePath = uploadService.uploadImage(file, path, width, height, quality);
			return ResponseEntity.ok("Image redimensionnée et compressée avec succès : " + imagePath);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body("Erreur lors du traitement de l'image : " + e.getMessage());
		}
	}

	@PostMapping("/upload/audio")
	@PreAuthorize("hasAnyRole('AUTHOR', 'ADMIN')")
	public ResponseEntity<String> uploadAudio(@RequestParam("file") MultipartFile file,
			@RequestParam("path") String path) {

		try {
			String audioPath = uploadService.uploadAudio(file, path);
			return ResponseEntity.ok("Fichier audio uploadé avec succès : " + audioPath);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body("Erreur lors de l'upload du fichier audio : " + e.getMessage());
		}
	}

	@GetMapping("/media/image/{filename:.+}")
	public ResponseEntity<Resource> getImage(@PathVariable String filename) throws IOException {
		return serveFile(IMAGE_DIR, filename);
	}

	@GetMapping("/media/audio/{filename:.+}")
	public ResponseEntity<Resource> getAudio(@PathVariable String filename) throws IOException {
		return serveFile(AUDIO_DIR, filename);
	}

	private ResponseEntity<Resource> serveFile(String baseDir, String filename) throws IOException {

		if (filename.contains("..")) {
			return ResponseEntity.badRequest().build();
		}

		Path filePath = Paths.get(baseDir).resolve(filename).normalize();

		if (!Files.exists(filePath)) {
			return ResponseEntity.notFound().build();
		}

		Resource resource = new UrlResource(filePath.toUri());

		String contentType = Files.probeContentType(filePath);
		if (contentType == null) {
			contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
		}

		return ResponseEntity.ok().header(HttpHeaders.CONTENT_TYPE, contentType)
				.header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"").body(resource);
	}
}
