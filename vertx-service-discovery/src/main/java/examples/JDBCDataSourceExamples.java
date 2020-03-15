/*
 * Copyright (c) 2011-2016 The original author or authors
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 *
 *      The Eclipse Public License is available at
 *      http://www.eclipse.org/legal/epl-v10.html
 *
 *      The Apache License v2.0 is available at
 *      http://www.opensource.org/licenses/apache2.0.php
 *
 * You may elect to redistribute this code under either of these licenses.
 */

package examples;

import io.vertx.core.json.JsonObject;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.ServiceReference;
import io.vertx.servicediscovery.types.JDBCDataSource;
import io.vertx.ext.jdbc.JDBCClient;

/**
MicroService Provider: là bên cung cấp service
MicroService consumer: bên dùng service
ServiceDiscover server: bên điều phối thông tin.
Giả định kết nối qua internet (tcp or http or event bus).
Các vd của vertx là trường hợp đặc biệt, tất cả đều trong 1 ứng dụng vertx (hoặc Vertx cluster).
*/
//vd Provider = SQL server
public class JDBCDataSourceExamples {

  public void example1(ServiceDiscovery discovery) {
    Record record = JDBCDataSource.createRecord(
        "some-data-source-service", // The service name
        new JsonObject().put("url", "some jdbc url"), // The location
        new JsonObject().put("some-metadata", "some-value") // Some metadata
    );

    discovery.publish(record, ar -> {
      // ...
    });
  }

  public void example2(ServiceDiscovery discovery) {
    // Get the record
    discovery.getRecord(
        new JsonObject().put("name", "some-data-source-service"),
        ar -> {
          if (ar.succeeded() && ar.result() != null) {
            // Retrieve the service reference
            ServiceReference reference = discovery.getReferenceWithConfiguration(
                ar.result(), // The record
                new JsonObject().put("username", "clement").put("password", "*****")); // Some additional metadata

            // Retrieve the service object
            JDBCClient client = reference.getAs(JDBCClient.class);

            // ...

            // when done
            reference.release();
          }
        });
  }

  public void example3(ServiceDiscovery discovery) {
    JDBCDataSource.<JsonObject>getJDBCClient(discovery,
        new JsonObject().put("name", "some-data-source-service"),
        new JsonObject().put("username", "clement").put("password", "*****"), // Some additional metadata
        ar -> {
          if (ar.succeeded()) {
            JDBCClient client = ar.result();

            // ...

            // Dont' forget to release the service
            ServiceDiscovery.releaseServiceObject(discovery, client);

          }
        });
  }

}
