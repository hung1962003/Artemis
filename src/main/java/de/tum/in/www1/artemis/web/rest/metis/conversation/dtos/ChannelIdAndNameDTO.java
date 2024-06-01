package de.tum.in.www1.artemis.web.rest.metis.conversation.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * A DTO representing a channel which contains only the id and name
 *
 * @param id   id of the channel
 * @param name name of the channel
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record ChannelIdAndNameDTO(Long id, String name) {
}
