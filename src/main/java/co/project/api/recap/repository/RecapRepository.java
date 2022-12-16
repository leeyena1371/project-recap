package co.project.api.recap.repository;

import co.project.api.recap.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecapRepository extends JpaRepository<User, Long> {
}