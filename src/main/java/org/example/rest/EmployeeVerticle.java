package org.example.rest;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class EmployeeVerticle extends AbstractVerticle {
    private static final AtomicInteger EMPLOYEE_ID_COUNTER = new AtomicInteger();
    private static final List<Employee> employees = new ArrayList();

    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        initData();

        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());
        router.route("/api/v1/employees/:id").handler(this::validateParams);
        router.post("/api/v1/employees").handler(this::handleAddOne);
        router.get("/api/v1/employees").handler(this::handleGetAll);
        router.get("/api/v1/employees/:id").handler(this::handleGetOne);
        router.put("/api/v1/employees/:id").handler(this::handleUpdate);
        router.delete("/api/v1/employees/:id").handler(this::handleDelete);

        vertx.createHttpServer()
                .requestHandler(router)
                .listen(8080, result -> {
                    if (result.succeeded()) {
                        System.out.println("Start OK!");
                        startPromise.complete();
                    } else {
                        System.out.println("Start failed!");
                        startPromise.fail(result.cause());
                    }
                });
    }

    private void handleAddOne(RoutingContext routingContext) {
        HttpServerResponse httpServerResponse = routingContext.response();
        Employee employee = routingContext.getBodyAsJson().mapTo(Employee.class);
        employee.setId(EMPLOYEE_ID_COUNTER.getAndIncrement());
        if (employees.add(employee)) {
            sendSuccess(httpServerResponse, 200, "Success", employee);
        } else {
            sendError(httpServerResponse, 400, "Add failed");
        }
    }

    private void handleGetAll(RoutingContext routingContext) {
        HttpServerResponse httpServerResponse = routingContext.response();
        sendSuccess(httpServerResponse, 200, "Success", employees);
    }

    private void handleGetOne(RoutingContext routingContext) {
        HttpServerResponse httpServerResponse = routingContext.response();
        Integer id = Integer.parseInt(routingContext.request().getParam("id"));
        Employee employee = employees.stream().filter(e -> e.getId() == id).findAny().orElse(null);
        if (employee == null) {
            sendError(httpServerResponse, 400, "Employee id does not exists");
            return;
        }

        sendSuccess(httpServerResponse, 200, "Success", employee);
    }

    private void handleUpdate(RoutingContext routingContext) {
        HttpServerResponse httpServerResponse = routingContext.response();
        Integer id = Integer.parseInt(routingContext.request().getParam("id"));
        Employee employee = employees.stream().filter(e -> e.getId() == id).findAny().orElse(null);
        Employee tmpEmployee = routingContext.getBodyAsJson().mapTo(Employee.class);
        if (employee == null) {
            sendError(httpServerResponse, 400, "Employee id does not exists");
            return;
        }

        employee.setName(tmpEmployee.getName());
        employee.setEmail(tmpEmployee.getEmail());
        employee.setPhoneNumber(tmpEmployee.getPhoneNumber());
        employee.setJobTitle(tmpEmployee.getJobTitle());
        sendSuccess(httpServerResponse, 200, "Success", employee);
    }

    private void handleDelete(RoutingContext routingContext) {
        HttpServerResponse httpServerResponse = routingContext.response();
        Integer id = Integer.parseInt(routingContext.request().getParam("id"));
        for (Employee e : employees) {
            if (e.getId() == id) {
                if (employees.remove(e)) {
                    sendSuccess(httpServerResponse, 200, "Success", null);
                } else {
                    sendError(httpServerResponse, 400, "Delete failed");
                }
                return;
            }
        }
        sendError(httpServerResponse, 400, "Employee id does not exists");
    }

    private void initData() {
        System.out.println("[process init data]");
        Employee employee1 = new Employee(EMPLOYEE_ID_COUNTER.getAndIncrement(), "Tina Lee", "tinalee9@gmail.com", "0978482123", "Project Manager");
        Employee employee2 = new Employee(EMPLOYEE_ID_COUNTER.getAndIncrement(), "Julien Nguyen", "juliennguyen@gmail.com", "0789328189", "Developer");
        employees.add(employee1);
        employees.add(employee2);
    }

    private void validateParams(RoutingContext routingContext) {
        HttpServerResponse httpServerResponse = routingContext.response();
        try {
            Integer.parseInt(routingContext.request().getParam("id"));
            routingContext.next();
        } catch (Exception ex) {
            sendError(httpServerResponse, 400, "Id invalid");
        }
    }

    private void sendSuccess(HttpServerResponse httpServerResponse, int statusCode, String message, Object data) {
        Response response = new Response(statusCode, message, data);
        httpServerResponse.putHeader("content-type", "application/json")
                .end(Json.encode(response));
    }

    private void sendError(HttpServerResponse httpServerResponse, int statusCode, String message) {
        Response response = new Response(statusCode, message, null);
        httpServerResponse.putHeader("content-type", "application/json")
                .setStatusCode(statusCode)
                .end(Json.encode(response));
    }
}
