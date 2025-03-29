package com.complycore.compliance.compliance_tool.controller;

import com.complycore.compliance.compliance_tool.entity.ComplianceRule;
import com.complycore.compliance.compliance_tool.entity.Role;
import com.complycore.compliance.compliance_tool.entity.User;
import com.complycore.compliance.compliance_tool.repository.ComplianceRuleRepository;
import com.complycore.compliance.compliance_tool.repository.UserRepository;
import com.complycore.compliance.compliance_tool.security.CustomUserDetails;
import com.complycore.compliance.compliance_tool.security.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ComplianceRuleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ComplianceRuleRepository ruleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    private String adminToken;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        ruleRepository.deleteAll();

        User admin = new User();
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("adminpass"));
        admin.setEmail("admin@example.com");
        admin.setRole(Role.ADMIN);
        admin = userRepository.save(admin);

        CustomUserDetails userDetails = new CustomUserDetails(
                admin.getId(),
                admin.getUsername(),
                admin.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + admin.getRole().name()))
        );

        adminToken = "Bearer " + jwtUtil.generateAccessToken(userDetails);
    }

    @Test
    void testCreateRule() throws Exception {
        ComplianceRule rule = new ComplianceRule();
        rule.setName("Rule 1");
        rule.setDescription("Test rule");
        rule.setActive(true);

        mockMvc.perform(post("/api/rules")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rule)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Rule 1"))
                .andExpect(jsonPath("$.description").value("Test rule"))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void testGetAllRules() throws Exception {
        ComplianceRule rule = new ComplianceRule();
        rule.setName("Rule 1");
        rule.setDescription("Test rule");
        rule.setActive(true);
        ruleRepository.save(rule);

        mockMvc.perform(get("/api/rules")
                        .header("Authorization", adminToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Rule 1"));
    }
}