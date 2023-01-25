package at.ac.uibk.swa.service;

import at.ac.uibk.swa.config.jwt_authentication.AuthContext;
import at.ac.uibk.swa.config.jwt_authentication.JwtToken;
import at.ac.uibk.swa.models.Permission;
import at.ac.uibk.swa.models.Person;
import at.ac.uibk.swa.repositories.PersonRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Service
public class PersonService {

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Gets a list of all persons in the repository
     *
     * @return list of found persons
     */
    public List<Person> getPersons() {
        return personRepository.findAll();
    }

    //region Login/Logout
    /**
     * Login via username and password
     *
     * @param username username of the person to be logged in
     * @param password password of the person to be logged in
     * @return person if successfully logged in, nothing otherwise
     */
    public Optional<Person> login(String username, String password) {
        Optional<Person> maybePerson = personRepository.findByUsername(username);
        if(maybePerson.isEmpty())
            return Optional.empty();

        Person person = maybePerson.get();
        if(!passwordEncoder.matches(password, person.getPassword()))
            return Optional.empty();

        person.setToken(UUID.randomUUID());
        try {
            if (updateToken(person)) {
                return Optional.of(person);
            }
        } catch (Exception e) {
            log.warn("Database Error while updating Token for User %s", person);
        }

        return Optional.empty();
    }

    /**
     * Logout the currently logged-in user
     *
     * @return true if user has been logged out, false otherwise
     */
    public boolean logout() {
        Optional<Person> maybePerson = AuthContext.getCurrentPerson();
        if (maybePerson.isPresent()) {
            Person person = maybePerson.get();
            person.setToken(null);
            return updateToken(person);
        } else {
            return false;
        }
    }
    //endregion

    //region Find
    /**
     * Find a person via its current token and username.
     *
     * @param token jwt token of the person to be found
     * @return person if found, otherwise nothing
     */
    public Optional<Person> findByUsernameAndToken(JwtToken token) {
        return findByUsernameAndToken(token.getUsername(), token.getToken());
    }

    /**
     * Find a person via its current token and username.
     *
     * @param token current token of the person to be found
     * @return person if found, otherwise nothing
     */
    public Optional<Person> findByUsernameAndToken(String username, UUID token) {
        return personRepository.findByUsernameAndToken(username, token);
    }

    /**
     * Find a person with its id
     *
     * @param id id of the person to be found
     * @return person if found, otherwise nothing
     */
    public Optional<Person> findById(UUID id) {
        return personRepository.findById(id);
    }
    //endregion

    //region Create/Save
    /**
     * Creates a new person in the repository
     *
     * @param person person to be created
     * @return true if person has been created, false otherwise
     */
    public boolean create(Person person) {
        if (person != null && person.getPersonId() == null) {
            return save(person) != null;
        } else {
            return false;
        }
    }

    /**
     * Saves a person to the repository
     *
     * @param person person to save
     * @return the person that has been saved if successful, null otherwise
     */
    public Person save(Person person) {
        try {
            if (!person.isPasswordHashed())
                person.hashPassword(passwordEncoder);
            return personRepository.save(person);
        } catch (Exception e) {
            return null;
        }
    }
    //endregion

    //region Update
    /**
     * Updates a Person with the values given as Parameters.
     * The User is given directly
     * The other Parameters are used to change the user.
     * Parameters that are set to null are left unchanged.
     *
     * @implNote This method does not check if the current user is permitted to delete the given user.
     * @param person The person to update
     * @param username The new Username.
     * @param permissions The set of new permissions.
     * @param password The new Password.
     * @return true if the user could be found and could be updated, false otherwise.
     */
    public boolean update(Person person, String username, String password, Set<Permission> permissions) {
        if(person != null && person.getPersonId() != null) {
            if (username    != null) person.setUsername(username);
            if (permissions != null) person.setPermissions(permissions);
            if (password    != null) person.setPassword(password);

            return save(person) != null;
        }

        return false;
    }

    /**
     * updates a person with the values given as parameters
     * person is identified by id
     *
     * @implNote This method does not check if the current user is permitted to delete the given user.
     * @param personId id of the person to update
     * @param username new username
     * @param permissions set of new permissions
     * @param password new password
     * @return true if user was successfully update, false otherwise
     */
    public boolean update(UUID personId, String username, Set<Permission> permissions, String password) {
        Optional<Person> maybePerson = findById(personId);
        return maybePerson.filter(person -> update(person, username, password, permissions)).isPresent();
    }

    private boolean updateToken(Person person) {
        return personRepository.updateToken(person) != null;
    }
    //endregion

    //region Delete
    /**
     * Deletes a Person from the Database (hard delete).
     *
     * @implNote This method does not check if the current user is permitted to delete the given user.
     * @param personId The ID of the Person to delete.
     * @return true if the person was deleted, false otherwise.
     */
    public boolean delete(UUID personId) {
        try {
            this.personRepository.deleteById(personId);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    //endregion
}
