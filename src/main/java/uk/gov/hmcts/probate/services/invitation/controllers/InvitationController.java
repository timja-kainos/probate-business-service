package uk.gov.hmcts.probate.services.invitation.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.probate.services.idgeneration.IdGeneratorService;
import uk.gov.hmcts.probate.services.invitation.InvitationService;
import uk.gov.hmcts.probate.services.invitation.model.Invitation;
import uk.gov.service.notify.NotificationClientException;

import javax.validation.Valid;
import javax.ws.rs.core.MediaType;
import java.util.HashMap;
import java.util.Map;

@RestController
public class InvitationController {

    private static final Logger LOGGER = LoggerFactory.getLogger(InvitationController.class);

    @Autowired
    private IdGeneratorService idGeneratorService;

    @Autowired
    private InvitationService invitationService;

    @Autowired
    private RestTemplate restTemplate;

    @RequestMapping(path = "/invite", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON)
    public String invite(@Valid @RequestBody Invitation invitation,
                         BindingResult bindingResult,
                         @RequestHeader("Session-Id") String sessionId) throws NotificationClientException {
        LOGGER.info("Processing session id " + sessionId + " : " + bindingResult.getFieldErrors());

        Map<String, String> data = new HashMap<>();
        data.put("firstName", invitation.getFirstName());
        data.put("lastName", invitation.getLastName());

        String linkId = idGeneratorService.generate(data);
        invitationService.saveAndSendEmail(linkId, invitation);
        return linkId;
    }

    @GetMapping(path = "/invites/allAgreed/{formdataId:.+}")
    public Boolean invitesAllAgreed(@PathVariable String formdataId) {

        boolean allInvitedAgreed = invitationService.checkAllInvitedAgreed(formdataId);
        boolean mainApplicantAgreed = invitationService.checkMainApplicantAgreed(formdataId);

        return allInvitedAgreed && mainApplicantAgreed;
    }
}
