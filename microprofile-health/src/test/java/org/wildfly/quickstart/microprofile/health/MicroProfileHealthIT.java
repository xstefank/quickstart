/*
 * JBoss, Home of Professional Open Source
 * Copyright 2019, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wildfly.quickstart.microprofile.health;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.as.arquillian.container.ManagementClient;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Simple tests for MicroProfile Health quickstart. Arquillian deploys an JAR archive to the application server, which
 * contains several health checks and verifies that they are correctly invoked.
 *
 * @author <a href="mstefank@redhat.com>Martin Stefanko</a>
 *
 */
@RunWith(Arquillian.class)
@RunAsClient
public class MicroProfileHealthIT {

    private URL managementURL;

    @ArquillianResource
    private ManagementClient managementClient;

    private Client client;

    @Before
    public void before() throws MalformedURLException {
        managementURL = new URL(String.format("http://%s:%d",
            managementClient.getMgmtAddress(), managementClient.getMgmtPort()));
        client = ClientBuilder.newClient();
    }

    @After
    public void after() {
        if (client != null) {
            client.close();
        }
    }

    /**
     * Constructs a deployment archive
     *
     * @return the deployment archive
     */
    @Deployment
    public static JavaArchive createDeployment() {
        return ShrinkWrap.create(JavaArchive.class)
            .addClasses(SimpleHealthCheck.class, DatabaseConnectionHealthCheck.class, DataHealthCheck.class)
            // enable CDI
            .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    /**
     * Tests that liveness contents (/health/live) contain correct data about two defined @Liveness procedures.
     */
    @Test
    public void testLivenessContents() {
        Response response =  client
            .target(managementURL.toString())
            .path("/health/live")
            .request()
            .get();

        Assert.assertEquals(200, response.getStatus());
        JsonReader jsonReader = Json.createReader(new StringReader(response.readEntity(String.class)));
        JsonObject json = jsonReader.readObject();

        Assert.assertEquals("UP", json.getString("status"));

        JsonArray checks = json.getJsonArray("checks");
        Assert.assertEquals(2, checks.size());

        for (JsonObject check : checks.getValuesAs(JsonObject.class)) {
            String name = check.getString("name");
            Assert.assertTrue(name.equals("Simple health check") || name.equals("Health check with data"));

            Assert.assertEquals("UP", check.getString("status"));

            if (name.equals("Health check with data")) {
                JsonObject data = check.getJsonObject("data");
                Assert.assertEquals(2, data.size());

                Assert.assertTrue(data.getString("bar") != null && data.getString("bar").equals("barValue"));
                Assert.assertTrue(data.getString("foo") != null && data.getString("foo").equals("fooValue"));
            }
        }
    }

    /**
     * Tests that readiness contents (/health/ready) contain correct data about the single defined @Readiness
     * procedure.
     */
    @Test
    public void testReadinessContents() {
        Response response =  client
            .target(managementURL.toString())
            .path("/health/ready")
            .request()
            .get();

        Assert.assertEquals(503, response.getStatus());
        JsonReader jsonReader = Json.createReader(new StringReader(response.readEntity(String.class)));
        JsonObject json = jsonReader.readObject();

        Assert.assertEquals("DOWN", json.getString("status"));

        JsonArray checks = json.getJsonArray("checks");
        Assert.assertEquals(1, checks.size());

        JsonObject check = checks.getJsonObject(0);
        Assert.assertEquals("Database connection health check", check.getString("name"));
        Assert.assertEquals("DOWN", check.getString("status"));

        JsonObject data = check.getJsonObject("data");
        Assert.assertTrue(data.getString("error") != null &&
            data.getString("error").equals("Cannot contact database"));
    }
}
