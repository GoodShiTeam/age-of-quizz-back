package goodshi.ageofquizz.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import goodshi.ageofquizz.entity.User;
import goodshi.ageofquizz.enums.RoleEnum;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

	/** Utilisateurs ayant au moins un des rôles donnés. */
	@Query("SELECT DISTINCT u FROM User u JOIN u.roles r WHERE r.name IN :roles")
	List<User> findByAnyRole(@Param("roles") Collection<RoleEnum> roles);

	Optional<User> findByUsername(String username);

	Optional<User> findByUserProfilePseudo(String pseudo);

	Optional<User> findByEmail(String email);

	Optional<User> findByUserProfileId(Long userProfileId);
}