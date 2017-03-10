package com.ctg.itrdc.janus.demo.user.facade;

import com.ctg.itrdc.janus.demo.user.User;
import com.ctg.itrdc.janus.rpc.protocol.rest.support.ContentType;

import javax.validation.constraints.Min;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

/**
 * @author lishen
 */
@Path("u")
@Consumes({MediaType.APPLICATION_JSON, MediaType.TEXT_XML})
@Produces({ContentType.APPLICATION_JSON_UTF_8, ContentType.TEXT_XML_UTF_8})
public interface AnotherUserRestService {

    @GET
    @Path("{id : \\d+}")
    User getUser(@PathParam("id") @Min(1L) Long id);

    @POST
    @Path("register")
    RegistrationResult registerUser(User user);
}
