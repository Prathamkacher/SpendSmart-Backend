package com.spendsmart.auth.resource;

import com.spendsmart.auth.config.JwtService;
import com.spendsmart.auth.dto.admin.PlatformAnalytics;
import com.spendsmart.auth.service.AdminService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminResource.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminResourceTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdminService adminService;

    @MockBean
    private JwtService jwtService;

    @Test
    void getAllUsers_ShouldReturn200() throws Exception {
        when(adminService.getAllUsers()).thenReturn(Collections.emptyList());
        mockMvc.perform(get("/auth/admin/users"))
                .andExpect(status().isOk());
    }

    @Test
    void suspendUser_ShouldReturn200() throws Exception {
        mockMvc.perform(put("/auth/admin/users/1/suspend"))
                .andExpect(status().isOk());
    }

    @Test
    void activateUser_ShouldReturn200() throws Exception {
        mockMvc.perform(put("/auth/admin/users/1/activate"))
                .andExpect(status().isOk());
    }

    @Test
    void deleteUser_ShouldReturn200() throws Exception {
        mockMvc.perform(delete("/auth/admin/users/1"))
                .andExpect(status().isOk());
    }

    @Test
    void getAllTransactions_ShouldReturn200() throws Exception {
        when(adminService.getAllTransactions()).thenReturn(Collections.emptyList());
        mockMvc.perform(get("/auth/admin/transactions"))
                .andExpect(status().isOk());
    }

    @Test
    void getPlatformAnalytics_ShouldReturn200() throws Exception {
        when(adminService.getPlatformAnalytics()).thenReturn(new PlatformAnalytics());
        mockMvc.perform(get("/auth/admin/analytics"))
                .andExpect(status().isOk());
    }

    @Test
    void sendGlobalNotification_ShouldReturn200() throws Exception {
        mockMvc.perform(post("/auth/admin/notify")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"Test\",\"message\":\"Test\",\"severity\":\"INFO\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void exportReport_ShouldReturn200() throws Exception {
        when(adminService.exportPlatformReport()).thenReturn(new byte[0]);
        mockMvc.perform(get("/auth/admin/report"))
                .andExpect(status().isOk());
    }

    @Test
    void getTopUsers_ShouldReturn200() throws Exception {
        when(adminService.getTopSpendingUsers()).thenReturn(Collections.emptyList());
        mockMvc.perform(get("/auth/admin/top-users"))
                .andExpect(status().isOk());
    }
}
