package com.office.purchase;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 覆盖论文中的主流程：登录、提交申请、驳回、重提、通过生成订单、更新订单状态、权限隔离。
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("h2")
class PurchaseFlowTest {

    /** 与 sql/purchase_db.sql 中的演示密码密文一致 */
    private static final String DEMO_HASH = "$2a$10$z0dFTYvkzmvyso0sCbCYFu5ox6I1T5ixFYg.00Ep/zsZE6yOzrwSO";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    /**
     * 确认 SQL 脚本里的密文确实对应明文 123456。
     */
    @Test
    void demoPasswordHashMatches() {
        assertTrue(passwordEncoder.matches("123456", DEMO_HASH));
    }

    /**
     * 员工提交申请，审核驳回后重提，再次通过生成订单，管理员更新到货状态。
     */
    @Test
    void purchaseFlow() throws Exception {
        String staffToken = login("staff01");
        String auditToken = login("audit01");
        String adminToken = login("admin");

        JsonNode goodsPage = json(getJson("/goods/page?current=1&size=10", staffToken));
        long goodsId = goodsPage.get("data").get("records").get(0).get("goodsId").asLong();

        JsonNode forbidden = json(postJson("/goods", adminBody(), staffToken));
        assertEquals(403, forbidden.get("code").asInt());

        String submitBody = "{\"applyReason\":\"部门季度补货\",\"items\":[{\"goodsId\":" + goodsId + ",\"buyNum\":2}]}";
        JsonNode submitted = json(postJson("/apply/submit", submitBody, staffToken));
        assertEquals(200, submitted.get("code").asInt(), submitted.toString());
        long applyId = submitted.get("data").asLong();

        JsonNode rejected = json(postJson("/apply/audit",
                "{\"applyId\":" + applyId + ",\"auditStatus\":\"已驳回\",\"auditOpinion\":\"数量偏多\"}",
                auditToken));
        assertEquals(200, rejected.get("code").asInt(), rejected.toString());

        JsonNode detail = json(getJson("/apply/" + applyId, staffToken));
        assertEquals("已驳回", detail.get("data").get("auditStatus").asText());
        assertEquals("数量偏多", detail.get("data").get("auditOpinion").asText());

        String resubmitBody = "{\"applyId\":" + applyId + ",\"applyReason\":\"按审核意见减量\",\"items\":[{\"goodsId\":" + goodsId + ",\"buyNum\":1}]}";
        JsonNode resubmitted = json(putJson("/apply/resubmit", resubmitBody, staffToken));
        assertEquals(200, resubmitted.get("code").asInt(), resubmitted.toString());

        JsonNode again = json(getJson("/apply/" + applyId, staffToken));
        assertEquals("待审批", again.get("data").get("auditStatus").asText());
        assertTrue(again.get("data").get("auditOpinion").isNull());

        JsonNode passed = json(postJson("/apply/audit",
                "{\"applyId\":" + applyId + ",\"auditStatus\":\"已通过\",\"auditOpinion\":\"同意采购\"}",
                auditToken));
        assertEquals(200, passed.get("code").asInt(), passed.toString());

        JsonNode repeat = json(postJson("/apply/audit",
                "{\"applyId\":" + applyId + ",\"auditStatus\":\"已通过\",\"auditOpinion\":\"再次通过\"}",
                auditToken));
        assertEquals(500, repeat.get("code").asInt());

        JsonNode staffOrders = json(getJson("/order/page?current=1&size=10", staffToken));
        assertEquals(1, staffOrders.get("data").get("total").asInt());
        assertEquals("待采购", staffOrders.get("data").get("records").get(0).get("orderStatus").asText());
        long orderId = staffOrders.get("data").get("records").get(0).get("orderId").asLong();

        JsonNode updated = json(putJson("/order/status",
                "{\"orderId\":" + orderId + ",\"orderStatus\":\"已到货\"}",
                adminToken));
        assertEquals(200, updated.get("code").asInt(), updated.toString());

        JsonNode after = json(getJson("/order/page?current=1&size=10", staffToken));
        assertEquals("已到货", after.get("data").get("records").get(0).get("orderStatus").asText());

        JsonNode staffCannotUpdate = json(putJson("/order/status",
                "{\"orderId\":" + orderId + ",\"orderStatus\":\"已完成\"}",
                staffToken));
        assertEquals(403, staffCannotUpdate.get("code").asInt());
    }

    /**
     * 登录并返回令牌。
     */
    private String login(String username) throws Exception {
        JsonNode root = json(postJson("/user/login",
                "{\"username\":\"" + username + "\",\"password\":\"123456\"}", null));
        assertEquals(200, root.get("code").asInt(), root.toString());
        return root.get("data").get("token").asText();
    }

    /**
     * 构造一个最小的新增商品请求，用来验证员工不能调用管理员接口。
     */
    private String adminBody() {
        return "{\"goodsName\":\"便签\",\"goodsType\":\"纸张文具\",\"spec\":\"小\",\"price\":1.00,\"stock\":10}";
    }

    /**
     * 发送 GET 并返回响应正文。
     */
    private String getJson(String url, String token) throws Exception {
        MvcResult result = mockMvc.perform(get(url).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();
        return result.getResponse().getContentAsString(StandardCharsets.UTF_8);
    }

    /**
     * 发送 POST JSON。token 为空时表示登录请求。
     */
    private String postJson(String url, String body, String token) throws Exception {
        org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder builder = post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body.getBytes(StandardCharsets.UTF_8));
        if (token != null) {
            builder.header("Authorization", "Bearer " + token);
        }
        return mockMvc.perform(builder).andExpect(status().isOk()).andReturn()
                .getResponse().getContentAsString(StandardCharsets.UTF_8);
    }

    /**
     * 发送 PUT JSON。
     */
    private String putJson(String url, String body, String token) throws Exception {
        return mockMvc.perform(put(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body.getBytes(StandardCharsets.UTF_8))
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse().getContentAsString(StandardCharsets.UTF_8);
    }

    /**
     * 把响应正文解析成 JSON。
     */
    private JsonNode json(String body) throws Exception {
        return objectMapper.readTree(body);
    }
}
