package com.zidio.keystone.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zidio.keystone.controller.WorkOrderController;
import com.zidio.keystone.dto.StatusChangeRequest;
import com.zidio.keystone.enums.WorkOrderStatus;
import com.zidio.keystone.exception.GlobalExceptionHandler;
import com.zidio.keystone.exception.InvalidWorkOrderTransitionException;
import com.zidio.keystone.service.WorkOrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Slice test exercising the security test cases called out in the brief (section 27) at the
 * HTTP layer: unauthenticated requests must get 401, and an illegal lifecycle transition must
 * surface as 409 (not 500) via GlobalExceptionHandler. Role/ownership-level rules are covered
 * in depth by WorkOrderStateMachineTest.
 */
@WebMvcTest(WorkOrderController.class)
@Import(GlobalExceptionHandler.class)
class WorkOrderControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WorkOrderService workOrderService;

    @MockBean
    private JwtService jwtService;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Test
    void unauthenticatedRequestIsRejectedWith401() throws Exception {
        mockMvc.perform(get("/api/work-orders"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void illegalTransitionSurfacesAs409NotServerError() throws Exception {
        when(workOrderService.changeStatus(eq(1L), any()))
                .thenThrow(new InvalidWorkOrderTransitionException(WorkOrderStatus.CLOSED, WorkOrderStatus.IN_PROGRESS));

        StatusChangeRequest request = new StatusChangeRequest(WorkOrderStatus.IN_PROGRESS, "trying to reopen");

        mockMvc.perform(post("/api/work-orders/1/status")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }
}
