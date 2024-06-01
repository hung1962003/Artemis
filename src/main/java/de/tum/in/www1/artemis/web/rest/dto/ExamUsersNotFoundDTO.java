package de.tum.in.www1.artemis.web.rest.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * A DTO representing information about exam users that were not found and number of saved images.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record ExamUsersNotFoundDTO(int numberOfUsersNotFound, int numberOfImagesSaved, List<String> listOfExamUserRegistrationNumbers) {
}
