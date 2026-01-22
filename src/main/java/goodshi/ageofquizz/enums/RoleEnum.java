package goodshi.ageofquizz.enums;

public enum RoleEnum {
	ADMIN, USER, REVIEWER, AUTHOR;

	@Override
	public String toString() {
		return name();
	}
}
