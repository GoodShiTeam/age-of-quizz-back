package goodshi.ageofquizz.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
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

import goodshi.ageofquizz.service.DownloadService;
import goodshi.ageofquizz.service.UploadService;

@RestController
public class MediaController {

	@Autowired
	private UploadService uploadService;

	@Autowired
	private DownloadService downloadService;

	private static final Logger logger = LoggerFactory.getLogger(MediaController.class);

	@PostMapping("/upload/image")
	@PreAuthorize("hasAnyRole('AUTHOR', 'ADMIN')")
	public ResponseEntity<String> uploadImage(@RequestParam("file") MultipartFile file,
			@RequestParam(name = "width", required = false) int width,
			@RequestParam(name = "height", required = false) int height,
			@RequestParam(name = "quality", defaultValue = "0.8") float quality) throws Exception {

		try {
			String imagePath = uploadService.uploadImage(file, width, height, quality);
			return ResponseEntity.ok("Image uploadée avec succès : " + imagePath);
		} catch (Exception e) {
			logger.error("Erreur lors du traitement de l'image", e); // ✅ logs la stack trace complète

			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body("Erreur lors du traitement de l'image : " + e.getMessage());
		}
	}

	@PostMapping("/upload/audio")
	@PreAuthorize("hasAnyRole('AUTHOR', 'ADMIN')")
	public ResponseEntity<String> uploadAudio(@RequestParam("file") MultipartFile file) {

		try {
			String audioPath = uploadService.uploadAudio(file);
			return ResponseEntity.ok("Fichier audio uploadé avec succès : " + audioPath);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body("Erreur lors de l'upload du fichier audio : " + e.getMessage());
		}
	}

	@GetMapping("/media/image/{filename:.+}")
	public ResponseEntity<Resource> getImage(@PathVariable String filename) throws Exception {
		Resource resource = downloadService.getImage(filename);
		MediaType mediaType = downloadService.getMediaType(filename);

		return ResponseEntity.ok().contentType(mediaType)
				.header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"").body(resource);
	}

	@GetMapping("/media/audio/{filename:.+}")
	public ResponseEntity<Resource> getAudio(@PathVariable String filename) throws Exception {
		Resource resource = downloadService.getAudio(filename);
		MediaType mediaType = downloadService.getMediaType(filename);

		return ResponseEntity.ok().contentType(mediaType)
				.header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"").body(resource);
	}

}
