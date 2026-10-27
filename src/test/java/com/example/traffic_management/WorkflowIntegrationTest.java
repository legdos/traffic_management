package com.example.traffic_management;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.annotation.DirtiesContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("workflow")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class WorkflowIntegrationTest {
    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate rest;

    @Test
    @SuppressWarnings("unchecked")
    void httpWorkflowPersistsDataAndRunsSimulation() {
        String base = "http://127.0.0.1:" + port;

        Map<String, Object> factoryPayload = new HashMap<>();
        factoryPayload.put("factoryName", "接口测试工厂");
        factoryPayload.put("address", "测试地址");
        factoryPayload.put("factoryLatitude", 30.0);
        factoryPayload.put("factoryLongitude", 104.0);
        ResponseEntity<String> factoryCreate = rest.postForEntity(base + "/factories", factoryPayload, String.class);
        assertEquals(HttpStatus.OK, factoryCreate.getStatusCode());
        Map<String, Object> factory = findByName((List<Map<String, Object>>) rest.getForObject(base + "/factories", List.class), "factoryName", "接口测试工厂");
        int factoryId = number(factory.get("id"));
        Map<String, Object> factoryUpdatePayload = new HashMap<>(factoryPayload);
        factoryUpdatePayload.put("id", factoryId);
        factoryUpdatePayload.put("factoryName", "接口测试工厂-已修改");
        rest.put(base + "/factories", factoryUpdatePayload);
        assertNotNull(rest.getForObject(base + "/factories/" + factoryId, Map.class));
        rest.postForEntity(base + "/factories/" + factoryId + "/generate-need?material=工厂接口需求", null, String.class);

        Map<String, Object> needPayload = new HashMap<>();
        needPayload.put("needContent", "测试货物");
        needPayload.put("factoryId", factoryId);
        needPayload.put("cargoTotalWeight", 10.0);
        needPayload.put("requiredVehicleType", "TRUCK");
        ResponseEntity<String> needCreate = rest.postForEntity(base + "/needs", needPayload, String.class);
        assertEquals(HttpStatus.OK, needCreate.getStatusCode());
        Map<String, Object> need = findByName((List<Map<String, Object>>) rest.getForObject(base + "/needs", List.class), "needContent", "测试货物");
        int needId = number(need.get("id"));
        assertEquals("TRUCK", need.get("requiredVehicleType"));

        Map<String, Object> vehiclePayload = new HashMap<>();
        vehiclePayload.put("typeCode", "TRUCK");
        vehiclePayload.put("maxLoad", "20");
        vehiclePayload.put("licenseNumber", "TEST-001");
        vehiclePayload.put("currentLatitude", 30.1);
        vehiclePayload.put("currentLongitude", 104.1);
        vehiclePayload.put("currentAction", "AVAILABLE");
        ResponseEntity<String> vehicleCreate = rest.postForEntity(base + "/vehicles", vehiclePayload, String.class);
        assertEquals(HttpStatus.OK, vehicleCreate.getStatusCode());
        Map<String, Object> vehicle = findByName((List<Map<String, Object>>) rest.getForObject(base + "/vehicles/active", List.class), "licenseNumber", "TEST-001");
        int vehicleId = number(vehicle.get("id"));
        assertEquals("20", String.valueOf(vehicle.get("maxLoad")));
        assertEquals(0, number(vehicle.get("version")));
        Map<String, Object> vehicleUpdatePayload = new HashMap<>(vehiclePayload);
        vehicleUpdatePayload.put("id", vehicleId);
        vehicleUpdatePayload.put("maxLoad", "25");
        rest.put(base + "/vehicles", vehicleUpdatePayload);
        vehicle = findById((List<Map<String, Object>>) rest.getForObject(base + "/vehicles/active", List.class), vehicleId);
        assertEquals("25", String.valueOf(vehicle.get("maxLoad")));

        Map<String, Object> matching = rest.getForObject(base + "/matching/score?needId=" + needId + "&vehicleId=" + vehicleId, Map.class);
        assertNotNull(matching);
        assertEquals(Boolean.TRUE, matching.get("eligible"));
        assertTrue(((Number) matching.get("score")).doubleValue() > 0);

        Map<String, Object> taskPayload = Map.of("taskName", "HTTP闭环测试", "needId", needId, "vehicleId", vehicleId,
                "matchScore", matching.get("score"));
        ResponseEntity<Map> taskCreate = rest.postForEntity(base + "/tasks", taskPayload, Map.class);
        assertEquals(HttpStatus.CREATED, taskCreate.getStatusCode());
        int taskId = number(taskCreate.getBody().get("id"));
        assertEquals("ASSIGNED", taskCreate.getBody().get("status"));

        ResponseEntity<Map> step = rest.postForEntity(base + "/simulation/step", null, Map.class);
        assertEquals(HttpStatus.OK, step.getStatusCode());
        assertEquals(1, number(step.getBody().get("ticks")));
        assertEquals(1, number(step.getBody().get("movedVehicles")));
        vehicle = findById((List<Map<String, Object>>) rest.getForObject(base + "/vehicles/active", List.class), vehicleId);
        assertEquals(1, number(vehicle.get("version")));
        assertNotEquals(30.1, ((Number) vehicle.get("currentLatitude")).doubleValue(), 0.000001);

        Map<String, Object> statePayload = Map.of("version", vehicle.get("version"), "status", "LOADING");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<Map> stateUpdate = rest.exchange(base + "/vehicles/" + vehicleId + "/state", HttpMethod.PUT,
                new HttpEntity<>(statePayload, headers), Map.class);
        assertEquals(HttpStatus.OK, stateUpdate.getStatusCode());
        assertEquals("LOADING", stateUpdate.getBody().get("status"));
        Map<String, Object> stalePayload = Map.of("version", vehicle.get("version"), "status", "TRANSPORTING");
        ResponseEntity<Map> staleUpdate = rest.exchange(base + "/vehicles/" + vehicleId + "/state", HttpMethod.PUT,
                new HttpEntity<>(stalePayload, headers), Map.class);
        assertEquals(HttpStatus.CONFLICT, staleUpdate.getStatusCode());

        Map<String, Object> latestVehicle = rest.getForObject(base + "/vehicles/" + vehicleId, Map.class);
        long oldVersion = ((Number) latestVehicle.get("version")).longValue();
        Map<String, Object> positionPayload = Map.of("version", oldVersion, "latitude", 30.02, "longitude", 104.02,
                "observedAt", "2026-10-18T11:00:00Z");
        ResponseEntity<Map> positionUpdate = rest.exchange(base + "/vehicles/" + vehicleId + "/position", HttpMethod.PUT,
                new HttpEntity<>(positionPayload, headers), Map.class);
        assertEquals(HttpStatus.OK, positionUpdate.getStatusCode());
        assertEquals(30.02, ((Number) positionUpdate.getBody().get("currentLatitude")).doubleValue(), 0.000001);

        Map<String, Object> revenue = rest.getForObject(base + "/tasks/" + taskId
                + "/revenue?distanceKm=10&pricePerTonKm=10&fuelCostPerKm=2&accidentLossRate=0.05", Map.class);
        assertNotNull(revenue);
        assertEquals(1_000.00, ((Number) revenue.get("transportRevenue")).doubleValue(), 0.001);
        assertEquals(930.00, ((Number) revenue.get("netRevenue")).doubleValue(), 0.001);

        Map<String, Object> poiPayload = Map.of("poiName", "测试加油站", "poiType", "加油站", "poiAddress", "测试路口",
                "poiLatitude", 30.01, "poiLongitude", 104.01);
        ResponseEntity<Map> poiCreate = rest.postForEntity(base + "/pois", poiPayload, Map.class);
        assertEquals(HttpStatus.CREATED, poiCreate.getStatusCode());
        int poiId = number(poiCreate.getBody().get("id"));
        assertTrue(((List<?>) rest.getForObject(base + "/pois", List.class)).size() > 0);
        rest.delete(base + "/pois/" + poiId);
        rest.delete(base + "/needs/" + needId);
        rest.delete(base + "/vehicles/" + vehicleId + "/deactivate");
        rest.delete(base + "/factories/" + factoryId);

        Map<String, Object> actionPayload = Map.of("id", 9001, "actionName", "测试装货", "actionType", "LOADING",
                "duration", 120, "accidentRate", 0.01);
        rest.postForEntity(base + "/actions", actionPayload, String.class);
        Map<String, Object> action = rest.getForObject(base + "/actions/9001", Map.class);
        assertEquals("测试装货", action.get("actionName"));
        assertEquals("LOADING", action.get("actionType"));
        rest.delete(base + "/actions/9001");

        Map<String, Object> started = rest.postForObject(base + "/simulation/start", null, Map.class);
        assertEquals(Boolean.TRUE, started.get("running"));
        ResponseEntity<Map> runningStep = rest.postForEntity(base + "/simulation/step", null, Map.class);
        assertEquals(HttpStatus.CONFLICT, runningStep.getStatusCode());
        Map<String, Object> paused = rest.postForObject(base + "/simulation/pause", null, Map.class);
        assertEquals(Boolean.FALSE, paused.get("running"));
    }

    private Map<String, Object> findByName(List<Map<String, Object>> values, String key, String expected) {
        return values.stream().filter(item -> expected.equals(item.get(key))).findFirst()
                .orElseThrow(() -> new AssertionError("missing " + expected + " in " + key));
    }

    private Map<String, Object> findById(List<Map<String, Object>> values, int id) {
        return values.stream().filter(item -> number(item.get("id")) == id).findFirst()
                .orElseThrow(() -> new AssertionError("missing id " + id));
    }

    private int number(Object value) {
        if (value instanceof Number number) return number.intValue();
        return Integer.parseInt(String.valueOf(value));
    }
}
