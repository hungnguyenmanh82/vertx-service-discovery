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

import io.vertx.core.http.HttpClient;
import io.vertx.core.json.JsonObject;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.ServiceReference;
import io.vertx.servicediscovery.types.HttpEndpoint;
import io.vertx.ext.web.client.WebClient;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */

/**
MicroService Provider: là bên cung cấp service
MicroService consumer: bên dùng service
ServiceDiscover server: bên điều phối thông tin.
Giả định kết nối qua internet (tcp or http or event bus).
Các vd của vertx là trường hợp đặc biệt, tất cả đều trong 1 ứng dụng vertx (hoặc Vertx cluster).
 */
// vd: Provider = Http server
public class HTTPEndpointExamples {

	/**
	 * Source code này ở phía Microservice Provider
	 * Provider đăng ký (publish) service availale với Discovery
	 */
	public void exampleProvider(ServiceDiscovery discovery) {
		Record record1 = HttpEndpoint.createRecord(
				"some-http-service", // The service name
				"localhost", // The host
				8433, // the port
				"/api" // the root of the service
				);

		/**
		 * ServiceDiscovery class ở phía Microservice Provider giúp đăng ký với Discovery Server.
		 */
		discovery.publish(record1, ar -> {
			// ...
		});

	}

	/**
	 * cách 1:
	 * Source code này ở phía Microservice Consumer, 
	 * Consumer muốn kết nối với MicroService Provider thì nó phải hỏi ServiceDiscovery Server về các Provider available.
	 *
	 */
	public void exampleConsumer1(ServiceDiscovery discovery) {
		/**
		 * dùng ServiceDiscovery class yêu cầu ServiceDiscovery Server cung cấp danh sách MicroService Provider
		 * 
		 */
		discovery.getRecord(new JsonObject().put("name", "some-http-service"), ar -> {
			if (ar.succeeded() && ar.result() != null) {
				
				// và thông báo với ServiceDiscovery server là mình dùng service này
				//ServiceReference: quản lý cấp phát Service class để kết nối Provider lấy thông tin.
				ServiceReference reference = discovery.getReference(ar.result());
				
				/**
				 * HttpClient: là class giúp service Consumer connect tới ServiceProvider để lấy thông tin. 
				 * HttpClient tương ứng với record trả về bởi ServiceDiscovery
				 */
				HttpClient client = reference.getAs(HttpClient.class);

				// You need to path the complete path
				client.get("/api/persons", response -> {

					// ...

					// Consumer thông báo ServiceDiscovery là mình ko dùng Service Provider này nữa
			        // ServiceDiscovery cần biết các Microservice đang tương tác với nhau thế nào?
					reference.release();
					
					// có 1 vài service dùng ConnectPool, or cachePool vì thế cần allocate và Release()
			         // vd: SQL connectpool
					// đã phần kết nối ServiceConsumer và ServiceProvider là asynchronous: eventbus

				});
			}
		});
	}


	/**
	 * cách 2:
	 * Source code này ở phía Microservice Consumer, 
	 * Consumer muốn kết nối với MicroService Provider thì nó phải hởi ServiceDiscovery Server về các Provider available.
	 * Source code ở vd exampleConsumer1 đc viết gọn như sau:
	 */
	public void exampleConsumer2(ServiceDiscovery discovery) {
		
		/**
		 * tất cả tương tác Consumer và Discovery đc đóng gói ở HttpEndpoint
		 * 
		 * HttpEndpoint có tránh nhiệu chuyển đổi Record ra ServiceObject (=httpClient) để connect tới Provider
		 */
		HttpEndpoint.getClient(discovery, new JsonObject().put("name", "some-http-service"), ar -> {
			if (ar.succeeded()) {
				HttpClient client = ar.result();

				/**
				 * HttpClient: là class giúp service Consumer connect tới ServiceProvider để lấy thông tin. 
				 * HttpClient tương ứng với record trả về bởi ServiceDiscovery
				 */
				client.get("/api/persons", response -> {

					// ...

					// có 1 vài service dùng ConnectPool, or cachePool vì thế cần allocate và Release()
			         // vd: SQL connectpool, http2
					ServiceDiscovery.releaseServiceObject(discovery, client);

				});
			}
		});
	}



}
