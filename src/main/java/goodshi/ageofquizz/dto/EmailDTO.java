package goodshi.ageofquizz.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class EmailDTO {

	@NotBlank(message = "Email ne doit pas être vide")
	@Email(message = "Format d'email invalide")
	private String email;

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

}
