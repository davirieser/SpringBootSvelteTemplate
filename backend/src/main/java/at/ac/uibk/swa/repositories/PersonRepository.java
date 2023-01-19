package at.ac.uibk.swa.repositories;

import at.ac.uibk.swa.models.Person;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface PersonRepository extends CrudRepository<Person, UUID> {
    @Override
    List<Person> findAll();

    Optional<Person> findByUsernameAndToken(String username, UUID token);

    Optional<Person> findByUsername(String username);

    @Transactional
    default <S extends Person> S updateToken(S person) {
        return updateToken(
                person.getPersonId(),
                person.getToken()
        ) == 1 ? person : null;
    }

    @Transactional
    @Modifying
    @Query("update Person p set p.token = :token where p.id = :id")
    int updateToken(@Param("id") UUID id, @Param("token") UUID token);

    @Transactional
    default <S extends Person> S updateUserDetails(S person) {
        return updateUserDetails(
                person.getPersonId(),
                person.getUsername(),
                person.getPermissions()
        ) == 1 ? person : null;
    }

    @Transactional
    @Modifying
    @Query("update Person p set p.username = :username, p.permissions = :permissions where p.id = :id")
    int updateUserDetails(
            @Param("id") UUID id,
            @Param("username") String username,
            @Param("permissions") Set<GrantedAuthority> permissions
    );
}