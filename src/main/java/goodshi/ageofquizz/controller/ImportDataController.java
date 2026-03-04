package goodshi.ageofquizz.controller;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import goodshi.ageofquizz.service.ImportDataService;

@RestController
@RequestMapping("/data")
public class ImportDataController {

	@Autowired
	private ImportDataService importDataService;

	@PostMapping("/civ")
	public ResponseEntity<byte[]> importCiv(@RequestBody String json) {
		try {
			byte[] csvBytes = importDataService.exportToBytes(json);

			return ResponseEntity.ok().header("Content-Disposition", "attachment; filename=CIV_DESCRIPTION.csv")
					.header("Content-Type", "text/csv").body(csvBytes);

		} catch (IOException e) {
			e.printStackTrace();
			return ResponseEntity.status(500).build();
		}
	}
}